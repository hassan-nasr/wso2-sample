/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.identity.thrift.authentication.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.persistence.JDBCPersistenceManager;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.thrift.authentication.util.ThriftAuthenticationConstants;
import org.wso2.carbon.utils.ThriftSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to manipulate thrift session info in database.
 */
public class ThriftSessionDAO {

    private static Log log = LogFactory.getLog(ThriftSessionDAO.class);

    public List<ThriftSession> getAllSessions() throws IdentityException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rSet = null;
        List<ThriftSession> thriftSessions;
        try {
            connection = JDBCPersistenceManager.getInstance().getDBConnection();
            prepStmt = connection.prepareStatement(ThriftAuthenticationConstants.GET_ALL_THRIFT_SESSIONS_SQL);

            rSet = prepStmt.executeQuery();

            thriftSessions = new ArrayList<ThriftSession>();
            while (rSet.next()) {
                if (rSet.getString(1) != null && rSet.getString(1).length() > 0) {
                    ThriftSession thriftSession = new ThriftSession();
                    thriftSession.setSessionId(rSet.getString(1));
                    thriftSession.setUserName(rSet.getString(2));
                    thriftSession.setCreatedAt(rSet.getLong(3));
                    thriftSession.setLastAccess(rSet.getLong(4));

                    thriftSessions.add(thriftSession);
                }
            }
        } catch (IdentityException e) {
            String errorMsg = "Error when getting an Identity Persistence Store instance.";
            log.error(errorMsg, e);
            throw new IdentityException(errorMsg, e);
        } catch (SQLException e) {
            log.error("Error when executing the SQL : " + ThriftAuthenticationConstants.GET_ALL_THRIFT_SESSIONS_SQL);
            log.error(e.getMessage(), e);
            throw new IdentityException("Error when reading the thrift session information from " +
                                            "the persistence store.");
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, rSet, prepStmt);
        }
        return thriftSessions;
    }

    public boolean isSessionExisting(String sessionId) throws IdentityException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rSet = null;

        boolean isExistingProvider = false;

        try {
            connection = JDBCPersistenceManager.getInstance().getDBConnection();
            prepStmt = connection.prepareStatement(ThriftAuthenticationConstants.CHECK_EXISTING_THRIFT_SESSION_SQL);
            prepStmt.setString(1, sessionId);

            rSet = prepStmt.executeQuery();
            if (rSet.next()) {
                isExistingProvider = true;
            }
        } catch (IdentityException e) {
            String errorMsg = "Error when getting an Identity Persistence Store instance.";
            log.error(errorMsg, e);
            throw new IdentityException(errorMsg, e);
        } catch (SQLException e) {
            log.error("Error when executing the SQL : " + ThriftAuthenticationConstants.CHECK_EXISTING_THRIFT_SESSION_SQL);
            log.error(e.getMessage(), e);
            throw new IdentityException("Error when reading thrift session information from " +
                                            "the persistence store.");
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, rSet, prepStmt);
        }
        return isExistingProvider;
    }

    public void addSession(ThriftSession session) throws IdentityException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        String sqlStmt = null;

        if (!isSessionExisting(session.getSessionId())) {
            try {
                connection = JDBCPersistenceManager.getInstance().getDBConnection();
                sqlStmt = ThriftAuthenticationConstants.ADD_THRIFT_SESSION_SQL;
                prepStmt = connection.prepareStatement(sqlStmt);
                prepStmt.setString(1, session.getSessionId());
                prepStmt.setString(2, session.getUserName());
                prepStmt.setLong(3, session.getCreatedAt());
                prepStmt.setLong(4, session.getLastAccess());

                prepStmt.execute();

                connection.commit();

            } catch (IdentityException e) {
                String errorMsg = "Error when getting an Identity Persistence Store instance.";
                log.error(errorMsg, e);
                throw new IdentityException(errorMsg, e);
            } catch (SQLException e) {
                log.error("Error when executing the SQL : " + sqlStmt);
                log.error(e.getMessage(), e);
                throw new IdentityException("Error when adding a new thrift session.");
            } finally {
                IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
            }

        } else {
            String errorMessage = "Thrift session with given Session Id already exists.";
            log.error(errorMessage);
            throw new IdentityException(errorMessage);
        }
    }

    public void removeSession(String sessionId) throws IdentityException {
        Connection connection = null;
        PreparedStatement prepStmt = null;

        if (isSessionExisting(sessionId)) {
            try {
                connection = JDBCPersistenceManager.getInstance().getDBConnection();
                prepStmt = connection.prepareStatement(ThriftAuthenticationConstants.DELETE_SESSION_SQL);
                prepStmt.setString(1, sessionId);

                prepStmt.execute();
                connection.commit();

            } catch (IdentityException e) {
                String errorMsg = "Error when getting an Identity Persistence Store instance.";
                log.error(errorMsg, e);
                throw new IdentityException(errorMsg, e);
            } catch (SQLException e) {
                log.error("Error when executing the SQL : " + ThriftAuthenticationConstants.DELETE_SESSION_SQL);
                log.error(e.getMessage(), e);
                throw new IdentityException("Error deleting the Thrift Session.");
            } finally {
                IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
            }
            
        } else {
            String errorMessage = "Thrift session with given Session Id already exists.";
            log.error(errorMessage);
            throw new IdentityException(errorMessage);
        }
    }

    public void updateLastAccessTime(String sessionId, long lastAccessTime)
            throws IdentityException {
        Connection connection = null;
        PreparedStatement prepStmt = null;

        if (isSessionExisting(sessionId)) {
            try {
                connection = JDBCPersistenceManager.getInstance().getDBConnection();
                prepStmt = connection.prepareStatement(ThriftAuthenticationConstants.UPDATE_LAST_MODIFIED_TIME_SQL);

                prepStmt.setLong(1, lastAccessTime);
                prepStmt.setString(2, sessionId);

                int count = prepStmt.executeUpdate();
                if (log.isDebugEnabled()) {
                    log.debug("No. of records updated for updating Thrift Session : " + count);
                }
                connection.commit();

            } catch (IdentityException e) {
                String errorMsg = "Error when getting an Identity Persistence Store instance.";
                log.error(errorMsg, e);
                throw new IdentityException(errorMsg, e);
            } catch (SQLException e) {
                log.error("Error when executing the SQL : " + ThriftAuthenticationConstants.UPDATE_LAST_MODIFIED_TIME_SQL);
                log.error(e.getMessage(), e);
                throw new IdentityException("Error updating the Thrift Session.");
            } finally {
                IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
            }
        } else {
            String errorMessage = "Thrift session with given Session Id already exists.";
            log.error(errorMessage);
            throw new IdentityException(errorMessage);
        }
    }

    public ThriftSession getSession(String sessionId) throws IdentityException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rSet = null;
        ThriftSession thriftSession = new ThriftSession();
        try {
            connection = JDBCPersistenceManager.getInstance().getDBConnection();
            prepStmt = connection.prepareStatement(ThriftAuthenticationConstants.GET_THRIFT_SESSION_SQL);
            prepStmt.setString(1, sessionId);
            rSet = prepStmt.executeQuery();

            while (rSet.next()) {
                if (rSet.getString(1) != null && rSet.getString(1).length() > 0) {
                    thriftSession.setSessionId(rSet.getString(1));
                    thriftSession.setUserName(rSet.getString(2));
                    thriftSession.setCreatedAt(rSet.getLong(3));
                    thriftSession.setLastAccess(rSet.getLong(4));
                }
            }
        } catch (IdentityException e) {
            String errorMsg = "Error when getting an Identity Persistence Store instance.";
            log.error(errorMsg, e);
            throw new IdentityException(errorMsg, e);
        } catch (SQLException e) {
            log.error("Error when executing the SQL : " + ThriftAuthenticationConstants.GET_THRIFT_SESSION_SQL);
            log.error(e.getMessage(), e);
            throw new IdentityException("Error when reading the Thrift session information from " +
                                            "the persistence store.");
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, rSet, prepStmt);
        }
        return thriftSession;    
    }
}
