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
package org.wso2.carbon.rssmanager.core.manager.impl.mysql;

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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySQLSystemRSSManager extends SystemRSSManager {

    private static final Log log = LogFactory.getLog(MySQLSystemRSSManager.class);

    public MySQLSystemRSSManager(Environment environment, RSSConfig config) {
        super(environment, config);
    }

    public Database createDatabase(Database database) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean inTx = false;

        final String qualifiedDatabaseName =
                RSSManagerUtil.getFullyQualifiedDatabaseName(database.getName());

        boolean isExist =
                this.isDatabaseExist(database.getRssInstanceName(), qualifiedDatabaseName);
        if (isExist) {
            String msg = "Database '" + qualifiedDatabaseName + "' already exists";
            log.error(msg);
            throw new EntityAlreadyExistsException(msg);
        }

        RSSInstance rssInstance = this.getEnvironment().getNextAllocatedNode();
        if (rssInstance == null) {
            String msg = "RSS instance " + database.getRssInstanceName() + " does not exist";
            log.error(msg);
            throw new EntityNotFoundException(msg);
        }

        try {
            /* Validating database name to avoid any possible SQL injection attack */
            RSSManagerUtil.checkIfParameterSecured(qualifiedDatabaseName);

            conn = this.getConnection(rssInstance.getName());
            conn.setAutoCommit(false);
            String sql = "CREATE DATABASE " + qualifiedDatabaseName;
            stmt = conn.prepareStatement(sql);

            inTx = getEntityManager().beginTransaction();
            database.setName(qualifiedDatabaseName);
            database.setRssInstanceName(rssInstance.getName());
            String databaseUrl = RSSManagerUtil.composeDatabaseUrl(rssInstance, qualifiedDatabaseName);
            database.setUrl(databaseUrl);
            database.setType(RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);

            final int tenantId = RSSManagerUtil.getTenantId();
            /* creates a reference to the database inside the metadata repository */
            this.getRSSDAO().getDatabaseDAO().addDatabase(getEnvironmentName(),database, tenantId);
            this.getRSSDAO().getDatabaseDAO().incrementSystemRSSDatabaseCount(
                    getEnvironmentName(), Connection.TRANSACTION_SERIALIZABLE);

            /* Actual database creation is committed just before committing the meta info into RSS
             * management repository. This is done as it is not possible to control CREATE, DROP,
             * ALTER operations within a JTA transaction since those operations are committed
             * implicitly */
            stmt.execute();

            if (inTx) {
                getEntityManager().endTransaction();
            }
            /* committing the changes to RSS instance */
            conn.commit();
        } catch (Exception e) {
            if (inTx) {
                this.getEntityManager().rollbackTransaction();
            }
            try {
                conn.rollback();
            } catch (Exception e1) {
                log.error(e1);
            }
            String msg = "Error while creating the database '" + qualifiedDatabaseName +
                    "' on RSS instance '" + rssInstance.getName() + "' : " + e.getMessage();
            handleException(msg, e);
        } finally {
            RSSManagerUtil.cleanupResources(null, stmt, conn);
        }
        return database;
    }

    public void dropDatabase(String rssInstanceName,
                             String databaseName) throws RSSManagerException {
        boolean inTx = false;
        Connection conn = null;
        PreparedStatement stmt = null;
        PreparedStatement delStmt = null;

        RSSInstance rssInstance = resolveRSSInstanceByDatabase(databaseName);
        if (rssInstance == null) {
            String msg = "Unresolvable RSS Instance. Database " + databaseName + " does not exist";
            log.error(msg);
            throw new EntityNotFoundException(msg);
        }
        try {
            /* Validating database name to avoid any possible SQL injection attack */
            RSSManagerUtil.checkIfParameterSecured(databaseName);

            conn = getConnection(rssInstance.getName());
            conn.setAutoCommit(false);
            String sql = "DROP DATABASE " + databaseName;
            stmt = conn.prepareStatement(sql);
            /* delete from mysql.db */
            delStmt = deletePreparedStatement(conn, databaseName);

            inTx = getEntityManager().beginTransaction();
            int tenantId = RSSManagerUtil.getTenantId();
            this.getRSSDAO().getDatabaseUserDAO().deleteUserDatabasePrivilegeEntriesByDatabase(
                    rssInstance, databaseName, tenantId);
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
            delStmt.execute();

            if (inTx) {
                getEntityManager().endTransaction();
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
                    log.error(e1);
                }
            }
            String msg = "Error while dropping the database '" + databaseName +
                    "' on RSS " + "instance '" + rssInstance.getName() + "' : " +
                    e.getMessage();
            handleException(msg, e);
        } finally {
            RSSManagerUtil.cleanupResources(null, delStmt, null);
            RSSManagerUtil.cleanupResources(null, stmt, conn);
        }
    }

    public DatabaseUser createDatabaseUser(DatabaseUser user) throws RSSManagerException {
        boolean inTx = false;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            String qualifiedUsername = RSSManagerUtil.getFullyQualifiedUsername(user.getName());

            boolean isExist =
                    this.isDatabaseUserExist(user.getRssInstanceName(), qualifiedUsername);
            if (isExist) {
                String msg = "Database user '" + qualifiedUsername + "' already exists";
                log.error(msg);
                throw new EntityAlreadyExistsException(msg);
            }

            /* Sets the fully qualified username */
            user.setName(qualifiedUsername);
            user.setRssInstanceName(user.getRssInstanceName());
            user.setType(RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);

            /* Validating user information to avoid any possible SQL injection attacks */
            RSSManagerUtil.validateDatabaseUserInfo(user);

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

                    conn = getConnection(rssInstance.getName());
                    conn.setAutoCommit(false);

                    String sql =
                            "CREATE DATABASE USER '" + qualifiedUsername + "'@'%' IDENTIFIED BY '" +
                                    user.getPassword() + "'";
                    stmt = conn.prepareStatement(sql);

                    /* Initiating the distributed transaction */
                    inTx = getEntityManager().beginTransaction();
                    final int tenantId = RSSManagerUtil.getTenantId();
                    user.setRssInstanceName(rssInstance.getName());
                    this.getRSSDAO().getDatabaseUserDAO().addDatabaseUser(getEnvironmentName(), rssInstance, user, tenantId);

                    /* Actual database user creation is committed just before committing the meta
                     * info into RSS management repository. This is done as it is not possible to
                     * control CREATE, DROP, ALTER, etc operations within a JTA transaction since
                     * those operations are committed implicitly */
                    stmt.execute();

                    /* Committing distributed transaction */
                    if (inTx) {
                        getEntityManager().endTransaction();
                    }
                    conn.commit();
                } catch (Exception e) {
                    if (inTx) {
                        this.getEntityManager().rollbackTransaction();
                    }
                    if (conn != null) {
                        conn.rollback();
                    }
                    String msg = "Error occurred while creating the database " +
                            "user '" + qualifiedUsername + "' on RSS instance '" +
                            wrapper.getName() + "'";
                    handleException(msg, e);
                } finally {
                    RSSManagerUtil.cleanupResources(null, stmt, conn);
                }
            }
            for (RSSInstanceDSWrapper wrapper :
                    getEnvironment().getDSWrapperRepository().getAllRSSInstanceDSWrappers()) {
                this.flushPrivileges(wrapper.getRssInstance());
            }
        } catch (SQLException e) {
            if (inTx) {
                this.getEntityManager().rollbackTransaction();
            }
            String msg = "Error while creating the database user '" +
                    user.getName() + "' on RSS instance '" + user.getRssInstanceName() +
                    "' : " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error(e);
                }
            }
        }
        return user;
    }

    public void dropDatabaseUser(String rssInstanceName,
                                 String username) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean inTx = false;
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

                    conn = getConnection(rssInstance.getName());
                    conn.setAutoCommit(false);

                    String sql = "DELETE FROM mysql.user WHERE User = ? AND Host = ?";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, username);
                    stmt.setString(2, "%");

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
                } catch (RSSManagerException e) {
                    if (inTx) {
                        this.getEntityManager().rollbackTransaction();
                    }
                    if (conn != null) {
                        conn.rollback();
                    }
                    throw e;
                } finally {
                    RSSManagerUtil.cleanupResources(null, stmt, conn);
                }
            }
            for (RSSInstanceDSWrapper wrapper :
                    getEnvironment().getDSWrapperRepository().getAllRSSInstanceDSWrappers()) {
                this.flushPrivileges(wrapper.getRssInstance());
            }
        } catch (Exception e) {
            if (inTx) {
                this.getEntityManager().rollbackTransaction();
            }
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            String msg = "Error while dropping the database user '" + username +
                    "' on RSS instances : " + e.getMessage();
            handleException(msg, e);
        } finally {
            RSSManagerUtil.cleanupResources(null, stmt, conn);
        }
    }

    public void editDatabaseUserPrivileges(DatabasePrivilegeSet privileges, DatabaseUser user,
                                           String databaseName) throws RSSManagerException {
        boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            String rssInstanceName =
                    this.getRSSDAO().getDatabaseUserDAO().resolveRSSInstanceByUser(
                            this.getEnvironmentName(), user.getRssInstanceName(), user.getType(),
                            user.getName(), tenantId);
            RSSInstance rssInstance = this.getEnvironment().getRSSInstance(rssInstanceName);
            if (rssInstance == null) {
                if (inTx) {
                    this.getEntityManager().rollbackTransaction();
                }
                String msg = "Database '" + databaseName + "' does not exist " +
                        "in RSS instance '" + user.getRssInstanceName() + "'";
                throw new EntityNotFoundException(msg);
            }
            if (RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE.equals(user.getRssInstanceName())) {
                user.setRssInstanceName(rssInstance.getName());
            }
            this.getRSSDAO().getDatabaseUserDAO().updateDatabaseUser(getEnvironmentName(),
                    privileges, rssInstance, user, databaseName);
        } catch (RSSDAOException e) {
            if (inTx) {
                this.getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while updating database user privileges: " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
    }

    public void attachUserToDatabase(UserDatabaseEntry entry,
                                     DatabasePrivilegeTemplate template) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean inTx = false;

        String rssInstanceName = entry.getRssInstanceName();
        String databaseName = entry.getDatabaseName();
        String username = entry.getUsername();

        RSSInstance rssInstance = resolveRSSInstanceByDatabase(databaseName);
        if (rssInstance == null) {
            String msg = "RSS instance " + rssInstanceName + " does not exist";
            log.error(msg);
            throw new EntityNotFoundException(msg);
        }

        Database database = this.getDatabase(rssInstanceName, databaseName);
        if (database == null) {
            String msg = "Database '" + entry.getDatabaseName() + "' does not exist";
            log.error(msg);
            throw new EntityNotFoundException(msg);
        }

        DatabaseUser user = this.getDatabaseUser(rssInstanceName, username);
        if (user == null) {
            String msg = "Database user '" + entry.getUsername() + "' does not exist";
            log.error(msg);
            throw new EntityNotFoundException(msg);
        }

        entry.setDatabaseId(database.getId());
        entry.setUserId(user.getId());

        try {
            conn = this.getConnection(rssInstance.getName());
            conn.setAutoCommit(false);
            stmt = this.composePreparedStatement(conn, databaseName, username, template);

            inTx = getEntityManager().beginTransaction();
            final int tenantId = RSSManagerUtil.getTenantId();
            int id = this.getRSSDAO().getUserDatabaseEntryDAO().addUserDatabaseEntry(
                    this.getEnvironmentName(), entry, tenantId);
            this.getRSSDAO().getDatabaseUserDAO().setUserDatabasePrivileges(
                    this.getEnvironmentName(), id, template, tenantId);

            /* Actual database user attachment is committed just before committing the meta info into RSS
          * management repository. This is done as it is not possible to control CREATE, DROP,
          * ALTER operations within a JTA transaction since those operations are committed
          * implicitly */
            stmt.execute();

            /* ending distributed transaction */
            if (inTx) {
                this.getEntityManager().endTransaction();
            }
            conn.commit();

            this.flushPrivileges(rssInstance);
        } catch (Exception e) {
            if (inTx) {
                this.getEntityManager().rollbackTransaction();
            }
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            String msg = "Error occurred while attaching the database user '" + username + "' to " +
                    "the database '" + databaseName + "' : " + e.getMessage();
            handleException(msg, e);
        } finally {
            RSSManagerUtil.cleanupResources(null, stmt, conn);
        }
    }
    
    public void detachUserFromDatabase(UserDatabaseEntry entry) throws RSSManagerException {
        boolean inTx = false;
        Connection conn = null;
        PreparedStatement stmt = null;

        Database database = this.getDatabase(entry.getRssInstanceName(), entry.getDatabaseName());
        if (database == null) {
            String msg = "Database '" + entry.getDatabaseName() + "' does not exist";
            log.error(msg);
            throw new EntityNotFoundException(msg);
        }

        RSSInstance rssInstance = resolveRSSInstanceByDatabase(entry.getDatabaseName());
        if (rssInstance == null) {
            String msg = "RSS instance '" + entry.getRssInstanceName() + "' does not exist";
            log.error(msg);
            throw new EntityNotFoundException(msg);
        }

        try {
            conn = getConnection(rssInstance.getName());
            conn.setAutoCommit(false);
            String sql = "DELETE FROM mysql.db WHERE host = ? AND user = ? AND db = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%");
            stmt.setString(2, entry.getUsername());
            stmt.setString(3, entry.getDatabaseName());

            /* Initiating the distributed transaction */
            inTx = getEntityManager().beginTransaction();
            final int tenantId = RSSManagerUtil.getTenantId();
            this.getRSSDAO().getDatabaseUserDAO().removeDatabasePrivileges(getEnvironmentName(),
                    rssInstance.getId(), entry.getUsername(), tenantId);
            this.getRSSDAO().getUserDatabaseEntryDAO().removeUserDatabaseEntry(
                    getEnvironmentName(), rssInstance.getId(), entry.getUsername(),
                    rssInstance.getInstanceType(), tenantId);

            /* Actual database user detachment is committed just before committing the meta info
          * into RSS management repository. This is done as it is not possible to control CREATE,
          * DROP, ALTER operations within a JTA transaction since those operations are committed
          * implicitly */
            stmt.execute();

            /* Committing the transaction */
            if (inTx) {
                this.getEntityManager().endTransaction();
            }
            conn.commit();

            this.flushPrivileges(rssInstance);
        } catch (Exception e) {
            if (inTx) {
                this.getEntityManager().rollbackTransaction();
            }
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            String msg = "Error occurred while attaching the database user '" +
                    entry.getUsername() + "' to " + "the database '" + entry.getDatabaseName() +
                    "': " + e.getMessage();
            handleException(msg, e);
        } finally {
            RSSManagerUtil.cleanupResources(null, stmt, conn);
        }
    }

    private PreparedStatement composePreparedStatement(Connection con,
                                                       String databaseName,
                                                       String username,
                                                       DatabasePrivilegeTemplate template) throws
            SQLException {
        DatabasePrivilegeSet privileges = template.getPrivileges();
        String sql = "INSERT INTO mysql.db VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setString(1, "%");
        stmt.setString(2, databaseName);
        stmt.setString(3, username);
        stmt.setString(4, privileges.getSelectPriv());
        stmt.setString(5, privileges.getInsertPriv());
        stmt.setString(6, privileges.getUpdatePriv());
        stmt.setString(7, privileges.getDeletePriv());
        stmt.setString(8, privileges.getCreatePriv());
        stmt.setString(9, privileges.getDropPriv());
        stmt.setString(10, privileges.getGrantPriv());
        stmt.setString(11, privileges.getReferencesPriv());
        stmt.setString(12, privileges.getIndexPriv());
        stmt.setString(13, privileges.getAlterPriv());
        stmt.setString(14, privileges.getCreateTmpTablePriv());
        stmt.setString(15, privileges.getLockTablesPriv());
        stmt.setString(16, privileges.getCreateViewPriv());
        stmt.setString(17, privileges.getShowViewPriv());
        stmt.setString(18, privileges.getCreateRoutinePriv());
        stmt.setString(19, privileges.getAlterRoutinePriv());
        stmt.setString(20, privileges.getExecutePriv());
        stmt.setString(21, privileges.getEventPriv());
        stmt.setString(22, privileges.getTriggerPriv());

        return stmt;
    }

    private PreparedStatement deletePreparedStatement(final Connection con,
                                                      final String databaseName) throws SQLException {
        String sql = " DELETE FROM mysql.db where Db=?";
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setString(1, databaseName);
        return stmt;
    }

    private void flushPrivileges(RSSInstance rssInstance) throws RSSManagerException, SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection(rssInstance.getName());
            String sql = "FLUSH PRIVILEGES";
            stmt = conn.prepareStatement(sql);
            stmt.execute();
        } finally {
            RSSManagerUtil.cleanupResources(null, stmt, conn);
        }
    }

}
