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
package org.wso2.carbon.rssmanager.core.manager.impl.postgres;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.config.RSSConfig;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.entity.*;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.exception.EntityAlreadyExistsException;
import org.wso2.carbon.rssmanager.core.exception.EntityNotFoundException;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.manager.SystemRSSManager;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.xml.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PostgresSystemRSSManager extends SystemRSSManager {

    private static final Log log = LogFactory.getLog(PostgresSystemRSSManager.class);

    public PostgresSystemRSSManager(Environment environment, RSSConfig config) {
        super(environment, config);
    }

    public Database createDatabase(Database database) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean inTx = false;

        String qualifiedDatabaseName =
                RSSManagerUtil.getFullyQualifiedDatabaseName(database.getName());

        boolean isExist =
                this.isDatabaseExist(database.getRssInstanceName(), qualifiedDatabaseName);
        if (isExist) {
            throw new EntityAlreadyExistsException("Database '" + qualifiedDatabaseName + "' " +
                    "already exists");
        }

        RSSInstance rssInstance = this.getEnvironment().getNextAllocatedNode();
        if (rssInstance == null) {
            throw new EntityNotFoundException("RSS instance " + database.getRssInstanceName() +
                    " does not exist");
        }

        RSSManagerUtil.checkIfParameterSecured(qualifiedDatabaseName);

        try {
            conn = getConnection(rssInstance.getName());
            conn.setAutoCommit(true);
            String sql = "CREATE DATABASE " + qualifiedDatabaseName;
            stmt = conn.prepareStatement(sql);

            inTx = this.getEntityManager().beginTransaction();
            database.setName(qualifiedDatabaseName);
            database.setRssInstanceName(rssInstance.getName());
            String databaseUrl = RSSManagerUtil.composeDatabaseUrl(rssInstance, qualifiedDatabaseName);
            database.setUrl(databaseUrl);
            database.setType(RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);

            int tenantId = RSSManagerUtil.getTenantId();
            /* creates a reference to the database inside the metadata repository */
            this.getRSSDAO().getDatabaseDAO().addDatabase(getEnvironmentName(), database, tenantId);
            this.getRSSDAO().getDatabaseDAO().incrementSystemRSSDatabaseCount(getEnvironmentName(),
                    Connection.TRANSACTION_SERIALIZABLE);

            /* Actual database creation is committed just before committing the meta info into RSS
          * management repository. This is done as it is not possible to control CREATE, DROP,
          * ALTER operations within a JTA transaction since those operations are committed
          * implicitly */
            stmt.execute();
            disAllowedConnect(conn, qualifiedDatabaseName, "PUBLIC");

            if (inTx) {
                this.getEntityManager().endTransaction();
            }
            /* committing the changes to RSS instance */
            //conn.commit();

            return database;
        } catch (SQLException e) {
            if (inTx) {
                this.getEntityManager().rollbackTransaction();
            }
            throw new RSSManagerException("Error while creating the database '" +
                    qualifiedDatabaseName + "' on RSS instance '" + rssInstance.getName() + "' : " +
                    e.getMessage(), e);
        } catch (RSSDAOException e) {
            if (inTx) {
                this.getEntityManager().rollbackTransaction();
            }
            try {
                //conn.rollback();
            } catch (Exception e1) {
                log.error(e1);
            }
            throw new RSSManagerException("Error while creating the database '" +
                    qualifiedDatabaseName + "' on RSS instance '" + rssInstance.getName() + "' : " +
                    e.getMessage(), e);
        } finally {
            RSSManagerUtil.cleanupResources(null, stmt, conn);
        }
    }

    public void dropDatabase(String rssInstanceName, String databaseName) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean inTx = false;
        PreparedStatement delStmt = null;

        RSSInstance rssInstance = resolveRSSInstanceByDatabase(databaseName);
        if (rssInstance == null) {
            throw new EntityNotFoundException("Unresolvable RSS Instance. Database "
                    + databaseName + " does not exist");
        }

        RSSManagerUtil.checkIfParameterSecured(databaseName);
        try {
            conn = getConnection(rssInstance.getName());
            conn.setAutoCommit(true);
            String sql = "DROP DATABASE " + databaseName;
            stmt = conn.prepareStatement(sql);
            //delete from mysql.db
            // delStmt = deletePreparedStatement(conn, databaseName);

            inTx = this.getEntityManager().beginTransaction();

            int tenantId = RSSManagerUtil.getTenantId();
            this.getRSSDAO().getDatabaseUserDAO().deleteUserDatabasePrivilegeEntriesByDatabase(rssInstance, databaseName, tenantId);
            this.getRSSDAO().getUserDatabaseEntryDAO().removeUserDatabaseEntriesByDatabase(
                    getEnvironmentName(), rssInstance.getId(), databaseName,
                    rssInstance.getInstanceType(), tenantId);
            this.getRSSDAO().getDatabaseDAO().removeDatabase(getEnvironmentName(),
                    rssInstance.getName(), databaseName, tenantId);

            /* Actual database creation is committed just before committing the meta info into RSS
          * management repository. This is done as it is not possible to control CREATE, DROP,
          * ALTER operations within a JTA transaction since those operations are committed
          * implicitly */
            stmt.execute();
            //delStmt.execute();

            if (inTx) {
                this.getEntityManager().endTransaction();
            }
            //conn.commit();
        } catch (SQLException e) {
            if (inTx) {
                this.getEntityManager().rollbackTransaction();
            }

            throw new RSSManagerException("Error while dropping the database '" + databaseName +
                    "' on RSS " + "instance '" + rssInstance.getName() + "' : " +
                    e.getMessage(), e);
        } catch (RSSDAOException e) {
            if (inTx) {
                this.getEntityManager().rollbackTransaction();
            }

            throw new RSSManagerException("Error while dropping the database '" + databaseName +
                    "' on RSS " + "instance '" + rssInstance.getName() + "' : " +
                    e.getMessage(), e);
        } finally {
            RSSManagerUtil.cleanupResources(null, delStmt, null);
            RSSManagerUtil.cleanupResources(null, stmt, conn);
        }
    }

    public DatabaseUser createDatabaseUser(DatabaseUser user) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean inTx = false;
        try {
            String qualifiedUsername = RSSManagerUtil.getFullyQualifiedUsername(user.getName());

            boolean isExist =
                    this.isDatabaseUserExist(user.getRssInstanceName(), qualifiedUsername);
            if (isExist) {
                throw new EntityAlreadyExistsException("Database user '" + qualifiedUsername +
                        "' already exists");
            }

            /* Sets the fully qualified username */
            user.setName(qualifiedUsername);
            user.setRssInstanceName(user.getRssInstanceName());
            user.setType(RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);

            RSSManagerUtil.checkIfParameterSecured(qualifiedUsername);

            for (RSSInstanceDSWrapper wrapper : getEnvironment().getDSWrapperRepository().
                    getAllRSSInstanceDSWrappers()) {
                try {
                    RSSInstance rssInstance;
                    try {
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(
                                MultitenantConstants.SUPER_TENANT_ID);
                        rssInstance = this.getEnvironment().getRSSInstance(wrapper.getName());
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }

                    conn = getConnection(rssInstance.getName());
                    conn.setAutoCommit(true);

                    boolean hasPassword = (!StringUtils.isEmpty(user.getPassword()));

                    StringBuilder sql = new StringBuilder(" CREATE USER " + qualifiedUsername);
                    if (hasPassword) {
                        RSSManagerUtil.checkIfParameterSecured(user.getPassword());
                        sql.append(" WITH PASSWORD '").append(user.getPassword()).append("'");
                    }
                    stmt = conn.prepareStatement(sql.toString());
                    /* Initiating the distributed transaction */
                    inTx = this.getEntityManager().beginTransaction();
                    int tenantId = RSSManagerUtil.getTenantId();
                    user.setRssInstanceName(rssInstance.getName());
                    this.getRSSDAO().getDatabaseUserDAO().addDatabaseUser(this.getEnvironmentName(),
                            rssInstance, user, tenantId);

                    /* Actual database user creation is committed just before committing the meta
              * info into RSS management repository. This is done as it is not possible to
              * control CREATE, DROP, ALTER, etc operations within a JTA transaction since
              * those operations are committed implicitly */
                    stmt.execute();

                    /* Committing distributed transaction */
                    if (inTx) {
                        this.getEntityManager().endTransaction();
                    }
                    //conn.commit();
                } catch (SQLException e) {
                    if (inTx) {
                        this.getEntityManager().rollbackTransaction();
                    }
                    throw new RSSManagerException("Error occurred while creating the database " +
                            "user '" + qualifiedUsername + "' on RSS instance '" +
                            wrapper.getName() + "'", e);
                } catch (RSSDAOException e) {
                    if (inTx) {
                        this.getEntityManager().rollbackTransaction();
                    }
                    throw new RSSManagerException("Error occurred while creating the database " +
                            "user '" + qualifiedUsername + "' on RSS instance '" +
                            wrapper.getName() + "'", e);
                } finally {
                    RSSManagerUtil.cleanupResources(null, stmt, conn);
                }
            }

            return user;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error(e);
                }
            }
        }
    }

    public void dropDatabaseUser(String rssInstanceName, String username) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement dropOwnedStmt = null;
        PreparedStatement dropUserStmt = null;
        boolean inTx = false;

        RSSManagerUtil.checkIfParameterSecured(username);
        try {
            for (RSSInstanceDSWrapper wrapper :
                    getEnvironment().getDSWrapperRepository().getAllRSSInstanceDSWrappers()) {
                try {
                    RSSInstance rssInstance;
                    try {
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(
                                MultitenantConstants.SUPER_TENANT_ID);
                        rssInstance = this.getEnvironment().getRSSInstance(wrapper.getName());
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }

                    conn = getConnection(wrapper.getRssInstance().getName());
                    conn.setAutoCommit(true);

                    String sql = "drop owned by " + username;
                    dropOwnedStmt = conn.prepareStatement(sql);
                    dropUserStmt = conn.prepareStatement(" drop user " + username);

                    /* Initiating the transaction */
                    inTx = this.getEntityManager().beginTransaction();

                    int tenantId = RSSManagerUtil.getTenantId();
                    this.getRSSDAO().getDatabaseUserDAO().removeDatabasePrivileges(
                            getEnvironmentName(), rssInstance.getId(), username, tenantId);
                    this.getRSSDAO().getUserDatabaseEntryDAO().removeUserDatabaseEntriesByDatabase(
                            getEnvironmentName(), rssInstance.getId(), username,
                            rssInstance.getInstanceType(), tenantId);
                    this.getRSSDAO().getDatabaseUserDAO().removeDatabaseUser(rssInstance.getName(),
                            getEnvironmentName(), username, tenantId);

                    /* Actual database creation is committed just before committing the meta info into RSS
                  * management repository. This is done as it is not possible to control CREATE, DROP,
                  * ALTER operations within a JTA transaction since those operations are committed
                  * implicitly */
                    dropOwnedStmt.execute();
                    dropUserStmt.execute();

                    /* committing the distributed transaction */
                    if (inTx) {
                        this.getEntityManager().endTransaction();
                    }
                    //conn.commit();
                } catch (RSSManagerException e) {
                    if (inTx) {
                        this.getEntityManager().rollbackTransaction();
                    }
                    throw e;
                } catch (RSSDAOException e) {
                    if (inTx) {
                        this.getEntityManager().rollbackTransaction();
                    }
                    throw new RSSManagerException(e);
                } finally {
                    RSSManagerUtil.cleanupResources(null, dropOwnedStmt, conn);
                }
            }
            for (RSSInstanceDSWrapper wrapper :
                    getEnvironment().getDSWrapperRepository().getAllRSSInstanceDSWrappers()) {
                //  this.flushPrivileges(wrapper.getRssInstance());
            }
        } catch (SQLException e) {
            if (inTx) {
                this.getEntityManager().rollbackTransaction();
            }
            String msg = "Error while dropping the database user '" + username +
                    "' on RSS instances : " + e.getMessage();
            throw new RSSManagerException(msg, e);
        } catch (RSSManagerException e) {
            if (inTx) {
                this.getEntityManager().rollbackTransaction();
            }
            throw e;
        } finally {
            RSSManagerUtil.cleanupResources(null, dropOwnedStmt, conn);
        }
    }

    public void editDatabaseUserPrivileges(DatabasePrivilegeSet privileges,
                                           DatabaseUser user,
                                           String databaseName) throws RSSManagerException {
        Connection dbConn = null;
        Connection conn = null;

        boolean inTx = this.getEntityManager().beginTransaction();
        try {
            int tenantId = RSSManagerUtil.getTenantId();
            String rssInstanceName =
                    this.getRSSDAO().getDatabaseUserDAO().resolveRSSInstanceByUser(
                            getEnvironmentName(), user.getRssInstanceName(), user.getType(),
                            user.getName(), tenantId);
            RSSInstance rssInstance = this.getEnvironment().getRSSInstance(rssInstanceName);
            if (rssInstance == null) {
                if (inTx) {
                    this.getEntityManager().rollbackTransaction();
                }
                throw new EntityNotFoundException("Database '" + databaseName + "' does not " +
                        "exist in RSS instance '" + user.getRssInstanceName() + "'");
            }
            dbConn = getConnection(rssInstance.getName(), databaseName);
            dbConn.setAutoCommit(true);

            conn = getConnection(rssInstance.getName());
            conn.setAutoCommit(true);

            if (RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE.equals(user.getRssInstanceName())) {
                user.setRssInstanceName(rssInstance.getName());
            }
            this.getRSSDAO().getDatabaseUserDAO().updateDatabaseUser(this.getEnvironmentName(),
                    privileges, rssInstance, user, databaseName);

            revokeAllPrivileges(conn, databaseName, user.getName());
            composePreparedStatement(dbConn, databaseName, user.getName(), privileges);
            if (inTx) {
                this.getEntityManager().endTransaction();
            }
        } catch (SQLException e) {
            if (inTx) {
                this.getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while editing privileges  : " + e.getMessage();
            throw new RSSManagerException(msg, e);
        } catch (RSSDAOException e) {
            if (inTx) {
                this.getEntityManager().rollbackTransaction();
            }
            throw new RSSManagerException(e);
        } finally {
            RSSManagerUtil.cleanupResources(null, null, conn);
            RSSManagerUtil.cleanupResources(null, null, dbConn);
        }
    }

    public void attachUserToDatabase(UserDatabaseEntry entry,
                                     DatabasePrivilegeTemplate template) throws RSSManagerException {
        Connection conn = null;
        Connection dbConn = null;
        boolean inTx = false;

        String rssInstanceName = entry.getRssInstanceName();
        String databaseName = entry.getDatabaseName();
        String username = entry.getUsername();

        RSSInstance rssInstance = resolveRSSInstanceByDatabase(databaseName);
        if (rssInstance == null) {
            throw new EntityNotFoundException("Database '" + databaseName + "' does not exist in " +
                    "RSS instance '" + rssInstanceName + "'");
        }

        Database database = this.getDatabase(rssInstanceName, databaseName);
        if (database == null) {
            throw new EntityNotFoundException("Database '" + databaseName + "' does not exist");
        }

        try {
            conn = getConnection(rssInstance.getName());
            conn.setAutoCommit(true);
            dbConn = getConnection(rssInstance.getName(), databaseName);
            dbConn.setAutoCommit(true);

            inTx = this.getEntityManager().beginTransaction();

            int tenantId = RSSManagerUtil.getTenantId();
            int id = this.getRSSDAO().getUserDatabaseEntryDAO().addUserDatabaseEntry(
                    getEnvironmentName(), entry, tenantId);
            this.getRSSDAO().getDatabaseUserDAO().setUserDatabasePrivileges(getEnvironmentName(),
                    id, template, tenantId);

            /* Actual database user attachment is committed just before committing the meta info into RSS
          * management repository. This is done as it is not possible to control CREATE, DROP,
          * ALTER operations within a JTA transaction since those operations are committed
          * implicitly */
            grantConnect(conn, databaseName, username);
            /*
            grantUsage(dbConn, databaseName, username);*/
            DatabasePrivilegeSet privileges = template.getPrivileges();
            composePreparedStatement(dbConn, databaseName, username, privileges);

            /* ending distributed transaction */
            if (inTx) {
                this.getEntityManager().endTransaction();
            }
            //conn.commit();

            //this.flushPrivileges(rssInstance);
        } catch (SQLException e) {
            if (inTx) {
                this.getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while attaching the database user '" + username + "' to " +
                    "the database '" + databaseName + "' : " + e.getMessage();
            throw new RSSManagerException(msg, e);
        } catch (RSSDAOException e) {
            if (inTx) {
                this.getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while adding metadata into the RSS Management " +
                    "Repository on user attachment";
            throw new RSSManagerException(msg, e);
        } finally {
            RSSManagerUtil.cleanupResources(null, null, conn);
            RSSManagerUtil.cleanupResources(null, null, dbConn);
        }
    }

    public void detachUserFromDatabase(UserDatabaseEntry entry) throws RSSManagerException {
        Connection conn = null;
        boolean inTx = false;
        String rssInstanceName = entry.getRssInstanceName();
        String databaseName = entry.getDatabaseName();
        String username = entry.getUsername();
        Database database = this.getDatabase(rssInstanceName, databaseName);
        if (database == null) {
            throw new EntityNotFoundException("Database '" + databaseName + "' does not exist");
        }
        /* Initiating the distributed transaction */
        RSSInstance rssInstance = resolveRSSInstanceByDatabase(databaseName);
        if (rssInstance == null) {
            throw new EntityNotFoundException("RSS instance '" + rssInstanceName +
                    "' does not exist");
        }

        RSSManagerUtil.checkIfParameterSecured(username);
        RSSManagerUtil.checkIfParameterSecured(databaseName);

        try {
            conn = getConnection(rssInstance.getName());
            conn.setAutoCommit(true);

            /* Initiating the distributed transaction */
            inTx = this.getEntityManager().beginTransaction();

            int tenantId = RSSManagerUtil.getTenantId();
            this.getRSSDAO().getDatabaseUserDAO().removeDatabasePrivileges(getEnvironmentName(),
                    rssInstance.getId(), entry.getUsername(), tenantId);
            this.getRSSDAO().getUserDatabaseEntryDAO().removeUserDatabaseEntry(
                    getEnvironmentName(), rssInstance.getId(), entry.getUsername(),
                    rssInstance.getInstanceType(), tenantId);

            /* Actual database user detachment is committed just before committing the meta info
          * into RSS management repository. This is done as it is not possible to control CREATE,
          * DROP, ALTER operations within a JTA transaction since those operations are committed
          * implicitly */
            revokeAllPrivileges(conn, databaseName, username);
            disAllowedConnect(conn, databaseName, username);

            /* Committing the transaction */
            if (inTx) {
                this.getEntityManager().endTransaction();
            }
            //conn.commit();

            //this.flushPrivileges(rssInstance);
        } catch (SQLException e) {
            if (inTx) {
                this.getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while attaching the database user '" + username + "' to " +
                    "the database '" + databaseName + "' : " + e.getMessage();
            throw new RSSManagerException(msg, e);
        } catch (RSSDAOException e) {
            if (inTx) {
                this.getEntityManager().rollbackTransaction();
            }
            throw new RSSManagerException(e);
        } finally {
            RSSManagerUtil.cleanupResources(null, null, conn);
        }
    }

    private void composePreparedStatement(Connection con,
                                          String databaseName,
                                          String username,
                                          DatabasePrivilegeSet privileges) throws
            SQLException, RSSManagerException {
        RSSManagerUtil.checkIfParameterSecured(databaseName);
        RSSManagerUtil.checkIfParameterSecured(username);

        boolean grantEnable = false;
        if ("Y".equalsIgnoreCase(privileges.getDropPriv())) {
            grantEnable = true;
        }
        composePreparedStatement(con, databaseName, username, privileges, PRIVILEGESTYPE.DATABASE,
                grantEnable);
        composePreparedStatement(con, databaseName, username, privileges, PRIVILEGESTYPE.SCHEMA,
                grantEnable);
        composePreparedStatement(con, databaseName, username, privileges, PRIVILEGESTYPE.TABLE,
                grantEnable);
        composePreparedStatement(con, databaseName, username, privileges, PRIVILEGESTYPE.FUNCTION,
                grantEnable);
        //composePreparedStatement(con, databaseName, username, privileges, PRIVILEGESTYPE.LANGUAGE,
        // grantEnable);
        // composePreparedStatement(con, databaseName, username, privileges,
        // PRIVILEGESTYPE.LARGE_OBJECT, grantEnable);
        composePreparedStatement(con, databaseName, username, privileges, PRIVILEGESTYPE.SEQUENCE,
                grantEnable);
        //composePreparedStatement(con, databaseName, username, privileges,
        // PRIVILEGESTYPE.TABLESPACE, grantEnable);
    }

    private void composePreparedStatement(Connection con, String databaseName,
                                          String username, DatabasePrivilegeSet privileges,
                                          PRIVILEGESTYPE type, boolean grantEnable)
            throws SQLException, RSSManagerException {

        String grantOption = " WITH GRANT OPTION ";
        String grantee = "";
        if (type.equals(PRIVILEGESTYPE.TABLE) || type.equals(PRIVILEGESTYPE.SEQUENCE) || type.equals(PRIVILEGESTYPE.FUNCTION)) {
            grantee = " ALL " + type.name() + "S IN SCHEMA PUBLIC ";
        } else if (type.equals(PRIVILEGESTYPE.DATABASE)) {
            grantee = type.name() + " " + databaseName;
        } else if (type.equals(PRIVILEGESTYPE.SCHEMA)) {
            grantee = type.name() + " PUBLIC ";
        }
        String privilegesString = createPrivilegesString(privileges, type);
        if (privilegesString == null) {
            return;
        }
        StringBuilder sql = new StringBuilder("GRANT " + privilegesString + " ON " + grantee + " TO " + username);
        if (grantEnable) {
            sql.append(grantOption);
        }
        PreparedStatement stmt = con.prepareStatement(sql.toString());
        stmt.executeUpdate();
        stmt.close();
    }

    private void grantConnect(Connection con, String databaseName,
                              String username) throws SQLException, RSSManagerException {
        RSSManagerUtil.checkIfParameterSecured(databaseName);
        RSSManagerUtil.checkIfParameterSecured(username);
        PreparedStatement st =
                con.prepareStatement(" GRANT CONNECT ON DATABASE " + databaseName + " TO " +
                        username);
        st.executeUpdate();
        st.close();
    }

    private void grantUsage(Connection con, String databaseName,
                            String username) throws SQLException, RSSManagerException {
        RSSManagerUtil.checkIfParameterSecured(databaseName);
        RSSManagerUtil.checkIfParameterSecured(username);
        PreparedStatement st = con.prepareStatement(" GRANT USAGE ON SCHEMA public TO " + username);
        st.executeUpdate();
        st.close();
    }


    private String createPrivilegesString(final DatabasePrivilegeSet privileges, PRIVILEGESTYPE type) {

        List<PRIVILEGES> privList = new ArrayList<PRIVILEGES>();

        switch (type) {
            case TABLE:
                addToPrivilegesList(privList, PRIVILEGES.SELECT, privileges.getSelectPriv());
                addToPrivilegesList(privList, PRIVILEGES.INSERT, privileges.getInsertPriv());
                addToPrivilegesList(privList, PRIVILEGES.UPDATE, privileges.getUpdatePriv());
                addToPrivilegesList(privList, PRIVILEGES.DELETE, privileges.getDeletePriv());
                addToPrivilegesList(privList, PRIVILEGES.REFERENCES, privileges.getReferencesPriv());
                addToPrivilegesList(privList, PRIVILEGES.TRIGGER, privileges.getTriggerPriv());
                privList.add(PRIVILEGES.TRUNCATE);
                break;

            case DATABASE:
                addToPrivilegesList(privList, PRIVILEGES.CREATE, privileges.getCreatePriv());
                addToPrivilegesList(privList, PRIVILEGES.TEMPORARY, privileges.getCreateTmpTablePriv());
                privList.add(PRIVILEGES.TEMP);
                privList.add(PRIVILEGES.CONNECT);
                break;

            case SEQUENCE:
                addToPrivilegesList(privList, PRIVILEGES.SELECT, privileges.getSelectPriv());
                addToPrivilegesList(privList, PRIVILEGES.UPDATE, privileges.getUpdatePriv());
                privList.add(PRIVILEGES.USAGE);
                break;

            case FUNCTION:
                addToPrivilegesList(privList, PRIVILEGES.EXECUTE, privileges.getExecutePriv());
                break;

            case LANGUAGE:
                privList.add(PRIVILEGES.USAGE);
                break;

            case LARGE_OBJECT:
                addToPrivilegesList(privList, PRIVILEGES.SELECT, privileges.getSelectPriv());
                addToPrivilegesList(privList, PRIVILEGES.UPDATE, privileges.getUpdatePriv());
                break;

            case SCHEMA:
                addToPrivilegesList(privList, PRIVILEGES.CREATE, privileges.getCreatePriv());
                privList.add(PRIVILEGES.USAGE);
                break;

            case TABLESPACE:
                addToPrivilegesList(privList, PRIVILEGES.CREATE, privileges.getCreatePriv());
                break;
        }

        //addToPrivilegesList(privList,Privileges.SELECT, privileges.getDropPriv());
        //addToPrivilegesList(privList,Privileges.SELECT, privileges.getGrantPriv());

        //addToPrivilegesList(privList,Privileges.SELECT, privileges.getIndexPriv());
        //addToPrivilegesList(privList,Privileges.SELECT, privileges.getAlterPriv());

        // addToPrivilegesList(privList,Privileges.SELECT, privileges.getLockTablesPriv());
        //addToPrivilegesList(privList,Privileges.SELECT, privileges.getCreateViewPriv());
        // addToPrivilegesList(privList,Privileges.SELECT, privileges.getShowViewPriv());
        // addToPrivilegesList(privList,Privileges.SELECT, privileges.getCreateRoutinePriv());
        // addToPrivilegesList(privList,Privileges.SELECT, privileges.getAlterRoutinePriv());

        // addToPrivilegesList(privList,Privileges.SELECT, privileges.getEventPriv());

        if (privList.isEmpty()) {
            return null;
        }

        StringBuilder privilegesPart = new StringBuilder();

        Iterator<PRIVILEGES> iter = privList.iterator();
        while (iter.hasNext()) {
            privilegesPart.append(iter.next().name());
            if (iter.hasNext()) {
                privilegesPart.append(" , ");
            }
        }
        return privilegesPart.toString();
    }

    private void addToPrivilegesList(final List<PRIVILEGES> privList, PRIVILEGES privEnum, String priv) {
        if ("Y".equalsIgnoreCase(priv)) {
            privList.add(privEnum);
        }
    }

    private void disAllowedConnect(Connection con, String databaseName,
                                   String userName) throws SQLException, RSSManagerException {
        RSSManagerUtil.checkIfParameterSecured(databaseName);
        RSSManagerUtil.checkIfParameterSecured(userName);
        PreparedStatement st = con.prepareStatement("REVOKE connect ON DATABASE " + databaseName +
                " FROM " + userName);
        st.executeUpdate();
        st.close();
    }

    private void revokeAllPrivileges(Connection con, String databaseName,
                                     String userName) throws SQLException, RSSManagerException {
        RSSManagerUtil.checkIfParameterSecured(databaseName);
        RSSManagerUtil.checkIfParameterSecured(userName);
        PreparedStatement st =
                con.prepareStatement("revoke all on database " + databaseName + " from " +
                        userName);
        st.executeUpdate();
        st.close();
    }

    private enum PRIVILEGES {
        SELECT, INSERT, UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER, CREATE, CONNECT, TEMPORARY,
        EXECUTE, USAGE, TEMP;
    }

    private enum PRIVILEGESTYPE {
        TABLE, DATABASE, SEQUENCE, FUNCTION, LANGUAGE, LARGE_OBJECT, SCHEMA, TABLESPACE;
    }
    

}