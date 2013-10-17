/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.rssmanager.core.manager.adaptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.config.RSSConfig;
import org.wso2.carbon.rssmanager.core.entity.*;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.manager.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RSSManagerAdaptorImpl implements RSSManagerAdaptor {

    private SystemRSSManager systemRM;
    private UserDefinedRSSManager userDefinedRM;
    private static final Log log = LogFactory.getLog(RSSManagerAdaptorImpl.class);

    public RSSManagerAdaptorImpl(Environment environment, String type, RSSConfig config) {
        RSSManagerFactory rmFactory =
                RSSManagerFactoryLoader.getRMFactory(type, config, environment);
        this.systemRM = rmFactory.getSystemRSSManager();
        this.userDefinedRM = rmFactory.getUserDefinedRSSManager();
        if (systemRM == null) {
            String msg =
                    "Configured System RSS Manager is null, thus RSS Manager cannot be initialized";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        if (userDefinedRM == null) {
            String msg = "Configured User Defined RSS Manager is null. RSS Manager " +
                    "initialization will not be interrupted as a proper System RSS Manager is " +
                    "available. But any task related to User Defined RSS Manager would not be " +
                    "functional";
            log.warn(msg);
        }
    }

    public SystemRSSManager getSystemRM() {
        return systemRM;
    }

    public UserDefinedRSSManager getUserDefinedRM() {
        return userDefinedRM;
    }

    public RSSManager resolveRM(String rssInstanceName) {
        String type = (rssInstanceName == null || "".equals(rssInstanceName) ||
                RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM.equals(rssInstanceName)) ?
                RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM :
                RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED;

        if (RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM.equals(type)) {
            return this.getSystemRM();
        } else if (RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED.equals(type)) {
            return this.getUserDefinedRM();
        } else {
            throw new IllegalArgumentException("Invalid RSS instance name provided");
        }
    }

    public Database addDatabase(Database database) throws RSSManagerException {
        return this.resolveRM(database.getType()).createDatabase(database);
    }

    public void removeDatabase(String rssInstanceName,
                               String databaseName) throws RSSManagerException {
        this.resolveRM(rssInstanceName).dropDatabase(rssInstanceName, databaseName);
    }

    public DatabaseUser addDatabaseUser(
            DatabaseUser user) throws RSSManagerException {
        return this.resolveRM(user.getType()).createDatabaseUser(user);
    }


    public void removeDatabaseUser(String rssInstanceName,
                                   String username) throws RSSManagerException {
        this.resolveRM(rssInstanceName).dropDatabaseUser(rssInstanceName, username);
    }

    public void updateDatabaseUserPrivileges(
            DatabasePrivilegeSet privileges, DatabaseUser user,
            String databaseName) throws RSSManagerException {
        this.resolveRM(user.getRssInstanceName()).editDatabaseUserPrivileges(privileges, user,
                databaseName);
    }

    public void attachUser(UserDatabaseEntry ude, String templateName) throws RSSManagerException {
        DatabasePrivilegeTemplate template = null;
        this.resolveRM(ude.getRssInstanceName()).attachUserToDatabase(ude, template);
    }

    public void detachUser(UserDatabaseEntry ude) throws RSSManagerException {
        this.resolveRM(ude.getRssInstanceName()).detachUserFromDatabase(ude);
    }

    public DatabaseUser getDatabaseUser(String rssInstanceName,
                                        String username) throws RSSManagerException {
        return this.resolveRM(rssInstanceName).getDatabaseUser(rssInstanceName, username);
    }

    public Database getDatabase(String rssInstanceName,
                                String databaseName) throws RSSManagerException {
        return this.resolveRM(rssInstanceName).getDatabase(rssInstanceName, databaseName);
    }

    public DatabaseUser[] getAttachedUsers(String rssInstanceName,
                                           String databaseName) throws RSSManagerException {
        return this.resolveRM(rssInstanceName).getUsersAttachedToDatabase(rssInstanceName,
                databaseName);
    }

    public DatabaseUser[] getAvailableUsers(
            String rssInstanceName, String databaseName) throws RSSManagerException {
        return this.resolveRM(rssInstanceName).getAvailableUsersToAttachToDatabase(rssInstanceName,
                databaseName);
    }

    public DatabasePrivilegeSet getUserDatabasePrivileges(String rssInstanceName,
                                                          String databaseName,
                                                          String username) throws RSSManagerException {
        return this.resolveRM(rssInstanceName).getUserDatabasePrivileges(rssInstanceName,
                databaseName, username);
    }

    public Database[] getDatabases() throws RSSManagerException {
        List<Database> databases = new ArrayList<Database>();
        databases.addAll(Arrays.asList(
                this.resolveRM(RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM).
                        getDatabases()));
        databases.addAll(Arrays.asList(
                this.resolveRM(RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED).
                        getDatabases()));
        return databases.toArray(new Database[databases.size()]);
    }

    public DatabaseUser[] getDatabaseUsers() throws RSSManagerException {
        List<DatabaseUser> users = new ArrayList<DatabaseUser>();
        users.addAll(Arrays.asList(
                this.resolveRM(RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM).
                        getDatabaseUsers()));
        users.addAll(Arrays.asList(
                this.resolveRM(RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED).
                        getDatabaseUsers()));
        return users.toArray(new DatabaseUser[users.size()]);
    }

    public boolean isDatabaseExist(String rssInstanceName,
                                   String databaseName) throws RSSManagerException {
        return this.resolveRM(rssInstanceName).isDatabaseExist(rssInstanceName, databaseName);
    }

    public boolean isDatabaseUserExist(String rssInstanceName,
                                       String username) throws RSSManagerException {
        return this.resolveRM(rssInstanceName).isDatabaseUserExist(rssInstanceName, username);
    }

}
