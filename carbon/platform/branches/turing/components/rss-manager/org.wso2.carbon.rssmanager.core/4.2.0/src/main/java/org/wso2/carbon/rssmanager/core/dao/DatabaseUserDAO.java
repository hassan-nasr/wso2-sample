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

package org.wso2.carbon.rssmanager.core.dao;

import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.entity.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.entity.DatabasePrivilegeTemplate;
import org.wso2.carbon.rssmanager.core.entity.DatabaseUser;
import org.wso2.carbon.rssmanager.core.entity.RSSInstance;

public interface DatabaseUserDAO {

    void addDatabaseUser(String environmentName, RSSInstance rssInstance, DatabaseUser user,
                         int tenantId) throws RSSDAOException;

    void removeDatabaseUser(String environmentName, String rssInstanceName, String username,
                            int tenantId) throws RSSDAOException;

    void updateDatabaseUser(String environmentName, DatabasePrivilegeSet privileges,
                            RSSInstance rssInstance, DatabaseUser user,
                            String databaseName) throws RSSDAOException;

    boolean isDatabaseUserExist(String environmentName, String rssInstanceName, String username,
                                int tenantId) throws RSSDAOException;

    DatabaseUser getDatabaseUser(String environmentName, String rssInstanceName, String username,
                                 int tenantId) throws RSSDAOException;

    DatabaseUser[] getDatabaseUsers(String environmentName, int tenantId) throws RSSDAOException;

    DatabaseUser[] getDatabaseUsersByRSSInstance(String environmentName, String rssInstanceName,
                                                 int tenantId) throws RSSDAOException;

    DatabaseUser[] getDatabaseUsersByDatabase(String environmentName, String rssInstanceName,
                                              String database, int tenantId) throws RSSDAOException;

    DatabaseUser[] getAssignedDatabaseUsers(String environmentName, String rssInstanceName,
                                            String databaseName, int tenantId) throws RSSDAOException;

    DatabaseUser[] getAvailableDatabaseUsers(String environmentName, String rssInstanceName,
                                             String databaseName, int tenantId) throws RSSDAOException;

    void removeDatabasePrivileges(String environmentName, int rssInstanceId, String username,
                                  int tenantId) throws RSSDAOException;

    DatabasePrivilegeSet getUserDatabasePrivileges(String environmentName, int rssInstanceId,
                                                   String databaseName, String username,
                                                   int tenantId) throws RSSDAOException;

    void setUserDatabasePrivileges(String environmentName, int id,
                                   DatabasePrivilegeTemplate template,
                                   int tenantId) throws RSSDAOException;

    void deleteUserDatabasePrivilegeEntriesByDatabase(RSSInstance rssInstance, String dbName,
                                                      int tenantId) throws RSSDAOException;

    String resolveRSSInstanceByUser(String environmentName, String rssInstanceName, 
                                    String rssInstanceType, String username,
                                    int tenantId) throws RSSDAOException;


}
