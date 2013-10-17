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
package org.wso2.carbon.rssmanager.core.service;

import org.wso2.carbon.rssmanager.core.entity.*;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;

public interface RSSManagerService {

    void addRSSInstance(String environmentName,
                        RSSInstance rssInstance) throws RSSManagerException;

    void removeRSSInstance(String environmentName, String rssInstanceName,
                           String type) throws RSSManagerException;

    void updateRSSInstance(String environmentName,
                           RSSInstance rssInstance) throws RSSManagerException;

    RSSInstance getRSSInstance(String environmentName, String rssInstanceName,
                               String type) throws RSSManagerException;

    RSSInstance[] getRSSInstances(String environmentName) throws RSSManagerException;

    Database addDatabase(String environmentName, Database database) throws RSSManagerException;

    void removeDatabase(String environmentName, String rssInstanceName,
                        String databaseName) throws RSSManagerException;

    Database[] getDatabases(String environmentName) throws RSSManagerException;

    Database getDatabase(String environmentName, String rssInstanceName,
                         String databaseName) throws RSSManagerException;

    boolean isDatabaseExist(String environmentName, String rssInstanceName,
                            String databaseName) throws RSSManagerException;

    boolean isDatabaseUserExist(String environmentName, String rssInstanceName,
                                String username) throws RSSManagerException;

    DatabaseUser addDatabaseUser(String environmentName,
                                 DatabaseUser user) throws RSSManagerException;

    void removeDatabaseUser(String environmentName, String rssInstanceName,
                            String username) throws RSSManagerException;

    void updateDatabaseUserPrivileges(String environmentName, DatabasePrivilegeSet privileges,
                                      DatabaseUser user,
                                      String databaseName) throws RSSManagerException;

    DatabaseUser getDatabaseUser(String environmentName, String rssInstanceName,
                                 String username) throws RSSManagerException;

    DatabaseUser[] getDatabaseUsers(String environmentName) throws RSSManagerException;

    void attachUser(String environmentName, UserDatabaseEntry ude,
                    String templateName) throws RSSManagerException;

    void detachUser(String environmentName,
                    UserDatabaseEntry ude) throws RSSManagerException;

    DatabaseUser[] getAttachedUsers(String environmentName, String rssInstanceName,
                                    String databaseName) throws RSSManagerException;

    DatabaseUser[] getAvailableUsers(String environmentName, String rssInstanceName,
                                     String databaseName) throws RSSManagerException;

    DatabasePrivilegeSet getUserDatabasePrivileges(String environmentName, String rssInstanceName,
                                                   String databaseName,
                                                   String username) throws RSSManagerException;

    Database[] getDatabasesForTenant(String environmentName,
                                     String tenantDomain) throws RSSManagerException;

    void addDatabaseForTenant(String environmentName, Database database,
                              String tenantDomain) throws RSSManagerException;

    Database getDatabaseForTenant(String environmentName, String rssInstanceName,
                                  String databaseName,
                                  String tenantDomain) throws RSSManagerException;

    boolean isDatabasePrivilegeTemplateExist(String environmentName,
                                             String templateName) throws RSSManagerException;

    boolean deleteTenantRSSData(String environmentName,
                                String tenantDomain) throws RSSManagerException;

    void addDatabasePrivilegeTemplate(String environmentName,
                                      DatabasePrivilegeTemplate template) throws RSSManagerException;

    void removeDatabasePrivilegeTemplate(String environmentName,
                                         String templateName) throws RSSManagerException;

    void updateDatabasePrivilegeTemplate(
            String environmentName, DatabasePrivilegeTemplate template) throws RSSManagerException;

    DatabasePrivilegeTemplate[] getDatabasePrivilegeTemplates(
            String environmentName) throws RSSManagerException;

    DatabasePrivilegeTemplate getDatabasePrivilegeTemplate(
            String environmentName, String templateName) throws RSSManagerException;

    void addCarbonDataSource(String environmentName,
                             UserDatabaseEntry entry) throws RSSManagerException;

    String[] getEnvironments() throws RSSManagerException;

}