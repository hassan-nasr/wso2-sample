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

import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.DataSourceMetaInfo;
import org.wso2.carbon.rssmanager.core.entity.*;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.environment.EnvironmentManager;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.internal.RSSManagerDataHolder;
import org.wso2.carbon.rssmanager.core.service.RSSManagerService;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

public class EnvironmentAdaptor implements RSSManagerService {

    private EnvironmentManager environmentManager;

    public EnvironmentAdaptor(EnvironmentManager environmentManager) {
        this.environmentManager = environmentManager;
    }

    public void addRSSInstance(String environmentName,
                               RSSInstance rssInstance) throws RSSManagerException {
        this.getEnvironmentManager().addRSSInstance(rssInstance);
    }

    public void removeRSSInstance(String environmentName, String rssInstanceName,
                                  String type) throws RSSManagerException {
        this.getEnvironmentManager().removeRSSInstance(environmentName, rssInstanceName);
    }

    public void updateRSSInstance(String environmentName,
                                  RSSInstance rssInstance) throws RSSManagerException {
        this.getEnvironmentManager().updateRSSInstance(environmentName, rssInstance);
    }

    public RSSInstance getRSSInstance(String environmentName, String rssInstanceName,
                                      String type) throws RSSManagerException {
        return this.getEnvironmentManager().getRSSInstance(environmentName, rssInstanceName);
    }

    public RSSInstance[] getRSSInstances(String environmentName) throws RSSManagerException {
        return this.getEnvironmentManager().getRSSInstances(environmentName);
    }

    public Database addDatabase(String environmentName,
                                Database database) throws RSSManagerException {
        return this.getRSSManagerAdaptor(environmentName).addDatabase(database);
    }

    public void removeDatabase(String environmentName, String rssInstanceName,
                               String databaseName) throws RSSManagerException {
        this.getRSSManagerAdaptor(environmentName).removeDatabase(rssInstanceName, databaseName);
    }

    public Database[] getDatabases(String environmentName) throws RSSManagerException {
        return this.getRSSManagerAdaptor(environmentName).getDatabases();
    }

    public Database getDatabase(String environmentName, String rssInstanceName, String
            databaseName) throws RSSManagerException {
        return this.getRSSManagerAdaptor(environmentName).getDatabase(rssInstanceName, databaseName);
    }

    public boolean isDatabaseExist(String environmentName, String rssInstanceName,
                                   String databaseName) throws RSSManagerException {
        return this.getRSSManagerAdaptor(environmentName).isDatabaseExist(rssInstanceName,
                databaseName);
    }

    public boolean isDatabaseUserExist(String environmentName, String rssInstanceName,
                                       String username) throws RSSManagerException {
        return this.getRSSManagerAdaptor(environmentName).isDatabaseUserExist(rssInstanceName,
                username);
    }

    public DatabaseUser addDatabaseUser(String environmentName,
                                        DatabaseUser user) throws RSSManagerException {
        return this.getRSSManagerAdaptor(environmentName).addDatabaseUser(user);
    }

    public void removeDatabaseUser(String environmentName, String rssInstanceName,
                                   String username) throws RSSManagerException {
        this.getRSSManagerAdaptor(environmentName).removeDatabaseUser(rssInstanceName, username);
    }

    public void updateDatabaseUserPrivileges(String environmentName, DatabasePrivilegeSet privileges,
                                             DatabaseUser user,
                                             String databaseName) throws RSSManagerException {
        this.getRSSManagerAdaptor(environmentName).updateDatabaseUserPrivileges(privileges, user,
                databaseName);
    }

    public DatabaseUser getDatabaseUser(String environmentName, String rssInstanceName,
                                        String username) throws RSSManagerException {
        return this.getRSSManagerAdaptor(environmentName).getDatabaseUser(rssInstanceName, username);
    }

    public DatabaseUser[] getDatabaseUsers(String environmentName) throws RSSManagerException {
        return this.getRSSManagerAdaptor(environmentName).getDatabaseUsers();
    }

    public void attachUser(String environmentName, UserDatabaseEntry ude,
                           String templateName) throws RSSManagerException {
        //TODO fix this with a proper DatabasePrivilegeTemplate
        this.getRSSManagerAdaptor(environmentName).attachUser(ude, null);
    }

    public void detachUser(String environmentName,
                           UserDatabaseEntry ude) throws RSSManagerException {
        this.getRSSManagerAdaptor(environmentName).detachUser(ude);
    }

    public DatabaseUser[] getAttachedUsers(String environmentName, String rssInstanceName,
                                           String databaseName) throws RSSManagerException {
        return this.getRSSManagerAdaptor(environmentName).getAttachedUsers(rssInstanceName,
                databaseName);
    }

    public DatabaseUser[] getAvailableUsers(String environmentName,
                                            String rssInstanceName,
                                            String databaseName) throws RSSManagerException {
        return this.getRSSManagerAdaptor(environmentName).getAvailableUsers(
                rssInstanceName, databaseName);
    }

    public DatabasePrivilegeSet getUserDatabasePrivileges(String environmentName,
                                                          String rssInstanceName,
                                                          String databaseName,
                                                          String username) throws RSSManagerException {
        return this.getRSSManagerAdaptor(environmentName).getUserDatabasePrivileges(rssInstanceName,
                databaseName, username);
    }

    public Database[] getDatabasesForTenant(String environmentName,
                                            String tenantDomain) throws RSSManagerException {
        return new Database[0];
    }

    public void addDatabaseForTenant(String environmentName, Database database,
                                     String tenantDomain) throws RSSManagerException {

    }

    public Database getDatabaseForTenant(String environmentName, String rssInstanceName,
                                         String databaseName,
                                         String tenantDomain) throws RSSManagerException {
        return null;
    }

    public boolean isDatabasePrivilegeTemplateExist(String environmentName,
                                                    String templateName) throws RSSManagerException {
        return this.getEnvironmentManager().isDatabasePrivilegeTemplateExist(environmentName,
                templateName);
    }

    public boolean deleteTenantRSSData(String environmentName,
                                       String tenantDomain) throws RSSManagerException {
//        try {
//            PrivilegedCarbonContext.startTenantFlow();
//            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(
//                    tenantId);
//            return this.getRSSManager(environmentName).deleteTenantRSSData();
//        } catch (RSSManagerException e) {
//            String msg = "Error occurred while deleting RSS tenant data tenantId '"
//                    + tenantId + "'";
//            throw new RSSManagerException(msg, e);
//        } finally {
//            PrivilegedCarbonContext.endTenantFlow();
//        }
        return false;
    }

    public void addDatabasePrivilegeTemplate(
            String environmentName, DatabasePrivilegeTemplate template) throws RSSManagerException {
        this.getEnvironmentManager().createDatabasePrivilegesTemplate(environmentName, template);
    }

    public void removeDatabasePrivilegeTemplate(String environmentName,
                                                String templateName) throws RSSManagerException {
        this.getEnvironmentManager().dropDatabasePrivilegesTemplate(environmentName, templateName);
    }

    public void updateDatabasePrivilegeTemplate(
            String environmentName, DatabasePrivilegeTemplate template) throws RSSManagerException {
        this.getEnvironmentManager().editDatabasePrivilegesTemplate(environmentName, template);
    }

    public DatabasePrivilegeTemplate[] getDatabasePrivilegeTemplates(
            String environmentName) throws RSSManagerException {
        return this.getEnvironmentManager().getDatabasePrivilegeTemplates(environmentName);
    }

    public DatabasePrivilegeTemplate getDatabasePrivilegeTemplate(
            String environmentName, String templateName) throws RSSManagerException {
        return this.getEnvironmentManager().getDatabasePrivilegeTemplate(environmentName,
                templateName);
    }

    public void addCarbonDataSource(String environmentName,
                                    UserDatabaseEntry entry) throws RSSManagerException {
        Database database =
                this.getRSSManagerAdaptor(environmentName).getDatabase(entry.getRssInstanceName(),
                        entry.getDatabaseName());
        DataSourceMetaInfo metaInfo =
                RSSManagerUtil.createDSMetaInfo(database, entry.getUsername());
        try {
            RSSManagerDataHolder.getInstance().getDataSourceService().addDataSource(metaInfo);
        } catch (DataSourceException e) {
            String msg = "Error occurred while creating carbon datasource for the database '" +
                    entry.getDatabaseName() + "'";
            throw new RSSManagerException(msg, e);
        }
    }

    private RSSManagerAdaptor getRSSManagerAdaptor(
            String environmentName) throws RSSManagerException {
        EnvironmentManager environmentManager = this.getEnvironmentManager();
        Environment environment = environmentManager.getEnvironment(environmentName);
        if (environment == null) {
            throw new IllegalArgumentException("Invalid RSS environment '" + environmentName + "'");
        }
        RSSManagerAdaptor adaptor = environment.getRSSManagerAdaptor();
        if (adaptor == null) {
            throw new RSSManagerException("RSS Manager is not initialized properly and " +
                    "thus, is null");
        }
        return adaptor;
    }

    private EnvironmentManager getEnvironmentManager() {
        return environmentManager;
    }

    public String[] getEnvironments() throws RSSManagerException {
        Environment[] environments = this.getEnvironmentManager().getEnvironments();
        String[] names = new String[environments.length];
        for (int i = 0; i < environments.length; i++) {
            names[i] = environments[i].getName();
        }
        return names;
    }

}
