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

package org.wso2.carbon.rssmanager.core.manager;

import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.config.RSSConfig;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.entity.Database;
import org.wso2.carbon.rssmanager.core.entity.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.entity.DatabaseUser;
import org.wso2.carbon.rssmanager.core.entity.RSSInstance;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class SystemRSSManager extends AbstractRSSManager {

    public SystemRSSManager(Environment environment, RSSConfig config) {
        super(environment, config);
    }

    public DatabaseUser getDatabaseUser(String rssInstanceName,
                                        String username) throws RSSManagerException {
        DatabaseUser user = null;
        boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            rssInstanceName =
                    getRSSDAO().getDatabaseUserDAO().resolveRSSInstanceByUser(
                            this.getEnvironmentName(), rssInstanceName,
                            RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM, username, tenantId);
            RSSInstance rssInstance = this.getEnvironment().getRSSInstance(rssInstanceName);
            if (rssInstance == null) {
                getEntityManager().rollbackTransaction();
                throw new RSSManagerException("Database user '" + username + "' does not exist " +
                        "in RSS instance '" + rssInstanceName + "'");
            }
            user = getRSSDAO().getDatabaseUserDAO().getDatabaseUser(getEnvironmentName(),
                    rssInstance.getName(), username, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg ="Error occurred while retrieving metadata " +
                    "corresponding to the database user '" + username + "' from RSS metadata " +
                    "repository : " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return user;
    }

    public Database getDatabase(String rssInstanceName,
                                String databaseName) throws RSSManagerException {
        Database database = null;
        boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            rssInstanceName =
                    getRSSDAO().getDatabaseDAO().resolveRSSInstanceByDatabase(
                            getEnvironmentName(), rssInstanceName, databaseName,
                            RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM, tenantId);
            RSSInstance rssInstance = this.getEnvironment().getRSSInstance(rssInstanceName);
            if (rssInstance == null) {
                if (inTx && getEntityManager().hasNoActiveTransaction()) {
                    getEntityManager().rollbackTransaction();
                }
                throw new RSSManagerException("Database '" + databaseName + "' does not exist " +
                        "in RSS instance '" + rssInstanceName + "'");
            }
            database = getRSSDAO().getDatabaseDAO().getDatabase(getEnvironmentName(),
                    rssInstance.getName(), databaseName, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while retrieving metadata " +
                    "corresponding to the database '" + databaseName + "' from RSS metadata " +
                    "repository : " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return database;
    }

    public DatabaseUser[] getUsersAttachedToDatabase(String rssInstanceName,
                                                     String databaseName) throws RSSManagerException {
        DatabaseUser[] users = new DatabaseUser[0];
        boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            rssInstanceName =
                    getRSSDAO().getDatabaseDAO().resolveRSSInstanceByDatabase(
                            getEnvironmentName(), rssInstanceName, databaseName,
                            RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM, tenantId);
            RSSInstance rssInstance = this.getEnvironment().getRSSInstance(rssInstanceName);
            if (rssInstance == null) {
                getEntityManager().rollbackTransaction();
                throw new RSSManagerException("Database '" + databaseName
                        + "' does not exist " + "in RSS instance '"
                        + rssInstanceName + "'");
            }
            users = getRSSDAO().getDatabaseUserDAO().getAssignedDatabaseUsers(getEnvironmentName(),
                    rssInstance.getName(), databaseName, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while retrieving metadata " +
                    "corresponding to the database users attached to the database '" +
                    databaseName + "' from RSS metadata repository : " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return users;
    }

    public DatabaseUser[] getAvailableUsersToAttachToDatabase(
            String rssInstanceName, String databaseName) throws RSSManagerException {
        DatabaseUser[] users = new DatabaseUser[0];
        boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            rssInstanceName =
                    getRSSDAO().getDatabaseDAO().resolveRSSInstanceByDatabase(
                            getEnvironmentName(), rssInstanceName, databaseName,
                            RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM, tenantId);
            RSSInstance rssInstance = this.getEnvironment().getRSSInstance(rssInstanceName);
            DatabaseUser[] existingUsers =
                    getRSSDAO().getDatabaseUserDAO().getAssignedDatabaseUsers(
                            getEnvironmentName(), rssInstance.getName(), databaseName, tenantId);
            Set<String> usernames = new HashSet<String>();
            for (DatabaseUser user : existingUsers) {
                usernames.add(user.getName());
            }

            List<DatabaseUser> availableUsers = new ArrayList<DatabaseUser>();
            for (DatabaseUser user : getRSSDAO().getDatabaseUserDAO().getDatabaseUsers(
                    getEnvironmentName(), tenantId)) {
                String username = user.getName();
                if (!usernames.contains(username)) {
                    availableUsers.add(user);
                }
            }
            users = availableUsers.toArray(new DatabaseUser[availableUsers.size()]);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while retrieving metadata " +
                    "corresponding to available database users to be attached to the database'" +
                    databaseName + "' from RSS metadata repository : " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return users;
    }

    public DatabasePrivilegeSet getUserDatabasePrivileges(
            String rssInstanceName, String databaseName, String username) throws RSSManagerException {
        DatabasePrivilegeSet privileges = null;
        boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            rssInstanceName =
                    getRSSDAO().getDatabaseDAO().resolveRSSInstanceByDatabase(
                            getEnvironmentName(), rssInstanceName, databaseName,
                            RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM, tenantId);
            RSSInstance rssInstance = this.getEnvironment().getRSSInstance(rssInstanceName);
            if (rssInstance == null) {
                if (inTx) {
                    getEntityManager().rollbackTransaction();
                }
                throw new RSSManagerException("Database '" + databaseName + "' does not exist " +
                        "in RSS instance '" + rssInstanceName + "'");
            }
            privileges =
                    getRSSDAO().getDatabaseUserDAO().getUserDatabasePrivileges(getEnvironmentName(),
                    rssInstance.getId(), databaseName, username, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while retrieving metadata " +
                    "corresponding to the database privileges assigned to database user '" +
                    username + "' from RSS metadata repository : " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return privileges;
    }

    public RSSInstance resolveRSSInstanceByDatabase(String databaseName) throws RSSManagerException {
        RSSInstance rssInstance;
        boolean inTx = this.getEntityManager().beginTransaction();
        try {
            int tenantId = RSSManagerUtil.getTenantId();
            String rssInstanceName =
                    this.getRSSDAO().getDatabaseDAO().resolveRSSInstanceByDatabase(
                            this.getEnvironmentName(), null,
                            RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE,
                            databaseName, tenantId);
            return this.getEnvironment().getRSSInstance(rssInstanceName);
        } catch (RSSDAOException e) {
            if (inTx && this.getEntityManager().hasNoActiveTransaction()) {
                this.getEntityManager().rollbackTransaction();
            }
            throw new RSSManagerException("Error occurred while resolving RSS instance", e);
        } finally {
            if (inTx) {
                this.getEntityManager().endTransaction();
            }
        }
    }

    public boolean isDatabaseExist(String rssInstanceName,
                                   String databaseName) throws RSSManagerException {
        boolean isExist = false;
        boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            isExist = getRSSDAO().getDatabaseDAO().isDatabaseExist(getEnvironmentName(),
                    rssInstanceName, databaseName, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while checking whether the database " +
                    "named '" + databaseName + "' exists in RSS instance '" + rssInstanceName +
                    "': " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return isExist;
    }

    public boolean isDatabaseUserExist(String rssInstanceName,
                                       String username) throws RSSManagerException {
        boolean isExist = false;
        boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            isExist = getRSSDAO().getDatabaseUserDAO().isDatabaseUserExist(getEnvironmentName(),
                    rssInstanceName, username, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while checking whether the database " +
                    "user named '" + username + "' already exists in RSS instance '" +
                    rssInstanceName + "': " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return isExist;
    }

}
