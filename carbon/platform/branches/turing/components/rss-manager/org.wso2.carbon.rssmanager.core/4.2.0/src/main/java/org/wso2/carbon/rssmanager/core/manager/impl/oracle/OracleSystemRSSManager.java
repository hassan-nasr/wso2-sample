/*
 *  Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.rssmanager.core.manager.impl.oracle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.config.RSSConfig;
import org.wso2.carbon.rssmanager.core.entity.*;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.manager.SystemRSSManager;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class OracleSystemRSSManager extends SystemRSSManager {

    private static final Log log = LogFactory.getLog(OracleSystemRSSManager.class);

    public OracleSystemRSSManager(Environment environment, RSSConfig config) {
        super(environment, config);
    }

    public Database createDatabase(Database database) throws RSSManagerException {
        throw new UnsupportedOperationException("CreateDatabase operation is not supported " +
                "for Oracle");
    }

    public void dropDatabase(String rssInstanceName, String name) throws RSSManagerException {
        throw new UnsupportedOperationException("dropDatabase operation is not supported " +
                "for Oracle");
    }

    public DatabaseUser createDatabaseUser(DatabaseUser user) throws RSSManagerException {
        boolean inTx = false;
        Connection conn = null;
        PreparedStatement stmt = null;

        String qualifiedUsername = RSSManagerUtil.getFullyQualifiedUsername(user.getName());
        boolean isExist =
                this.isDatabaseUserExist(user.getRssInstanceName(), qualifiedUsername);
        if (isExist) {
            String msg = "Database user '" + qualifiedUsername + "' already exists";
            log.error(msg);
            throw new RSSManagerException(msg);
        }

        /* Sets the fully qualified username */
        user.setName(qualifiedUsername);
        user.setRssInstanceName(user.getRssInstanceName());
        user.setType(RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);

        RSSInstance rssInstance = this.getEnvironment().getNextAllocatedNode();
        if (rssInstance == null) {
            throw new RuntimeException("No valid RSS instance is available for database user " +
                    "creation");
        }
        try {
            conn = this.getConnection(rssInstance.getName());
            conn.setAutoCommit(false);
            String sql = "CREATE USER " + qualifiedUsername + " IDENTIFIED BY '" +
                    user.getPassword() + "'";
            stmt = conn.prepareStatement(sql);

            inTx = this.getEntityManager().beginTransaction();

            final int tenantId = RSSManagerUtil.getTenantId();
            this.getRSSDAO().getDatabaseDAO().addDatabase(getEnvironmentName(), null, tenantId);
            this.getRSSDAO().getDatabaseUserDAO().addDatabaseUser(getEnvironmentName(),
                    rssInstance, user, tenantId);
            this.getRSSDAO().getUserDatabaseEntryDAO().addUserDatabaseEntry(getEnvironmentName(),
                    null, tenantId);

            stmt.execute();
            if (inTx) {
                this.getEntityManager().endTransaction();
            }
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.warn("Error occurred while rollbacking the transaction", e);
                }
            }
            if (inTx) {
                this.getEntityManager().rollbackTransaction();
            }
            throw new RSSManagerException("Error occurred while creating database user '" +
                    user.getName() + "'", e);
        } finally {
            RSSManagerUtil.cleanupResources(null, stmt, conn);
        }
        return user;
    }
    
    public void dropDatabaseUser(String rssInstanceName, String username) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean inTx = false;
        try {
            RSSInstance rssInstance = null; //TODO: Assign proper RSS instance
            //RSSInstance rssInstance = this.resolveRSSInstanceByDatabaseUser(username);
            if (rssInstance == null) {
                throw new RuntimeException("Unable to resolve the RSS instance on which the " +
                        "database user '" + username + "' exists");
            }

            conn = getConnection(rssInstance.getName());
            conn.setAutoCommit(false);

            String sql = "DROP USER " + username;
            stmt = conn.prepareStatement(sql);

            /* Initiating the transaction */
            inTx = this.getEntityManager().beginTransaction();

            final int tenantId = RSSManagerUtil.getTenantId();
            this.getRSSDAO().getDatabaseUserDAO().removeDatabasePrivileges(
                    getEnvironmentName(), rssInstance.getId(), username, tenantId);
            this.getRSSDAO().getUserDatabaseEntryDAO().removeUserDatabaseEntriesByUser(
                    getEnvironmentName(), rssInstance.getId(), username,
                    rssInstance.getInstanceType(), tenantId);
            this.getRSSDAO().getDatabaseUserDAO().removeDatabaseUser(getEnvironmentName(),
                    rssInstance.getName(), username, tenantId);

            this.getRSSDAO().getDatabaseDAO().removeDatabase(getEnvironmentName(),
                    rssInstanceName, username, tenantId);

            /* Actual database creation is committed just before committing the meta info into RSS
          * management repository. This is done as it is not possible to control CREATE, DROP,
          * ALTER operations within a JTA transaction since those operations are committed
          * implicitly */
            stmt.execute();

            /* committing the distributed transaction */
            if (inTx) {
                this.getEntityManager().endTransaction();
            }
            conn.commit();
        } catch (Exception e) {
            if (inTx) {
                this.getEntityManager().rollbackTransaction();
            }
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Error occurred while rollbacking the transaction", e);
                }
            }
            throw new RSSManagerException("Error occurred while dropping database user '" +
                    username + "'", e);
        } finally {
            RSSManagerUtil.cleanupResources(null, stmt, conn);
        }
}

    public void editDatabaseUserPrivileges(DatabasePrivilegeSet privileges, DatabaseUser user,
                                           String databaseName) throws RSSManagerException {

    }

    public void attachUserToDatabase(UserDatabaseEntry entry,
                                     DatabasePrivilegeTemplate template) throws RSSManagerException {
        throw new UnsupportedOperationException("attachUserToDatabase operation is not " +
                "supported for Oracle");
    }


    public void detachUserFromDatabase(UserDatabaseEntry entry) throws RSSManagerException {
        throw new UnsupportedOperationException("detachUserFromDatabase operation is not " +
                "supported for Oracle");
    }
    

}
