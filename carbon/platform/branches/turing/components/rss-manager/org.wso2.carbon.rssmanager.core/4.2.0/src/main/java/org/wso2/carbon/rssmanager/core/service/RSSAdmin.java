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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.rssmanager.common.exception.RSSManagerCommonException;
import org.wso2.carbon.rssmanager.core.config.RSSConfigurationManager;
import org.wso2.carbon.rssmanager.core.entity.*;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.manager.adaptor.EnvironmentAdaptor;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class RSSAdmin extends AbstractAdmin implements RSSManagerService {

    private static final Log log = LogFactory.getLog(RSSAdmin.class);

    public void addRSSInstance(String environmentName,
                               RSSInstance rssInstance) throws RSSManagerException {
        this.getEnvironmentAdaptor().addRSSInstance(environmentName, rssInstance);
    }

    public void removeRSSInstance(String environmentName, String rssInstanceName,
                                  String type) throws RSSManagerException {
        this.getEnvironmentAdaptor().removeRSSInstance(environmentName, rssInstanceName, type);
    }

    public void updateRSSInstance(String environmentName,
                                  RSSInstance rssInstance) throws RSSManagerException {
        this.getEnvironmentAdaptor().updateRSSInstance(environmentName, rssInstance);
    }

    public RSSInstance getRSSInstance(String environmentName, String rssInstanceName,
                                      String type) throws RSSManagerException {
        return this.getEnvironmentAdaptor().getRSSInstance(environmentName, rssInstanceName, type);
    }

    public RSSInstance[] getRSSInstances(String environmentName) throws RSSManagerException {
        return this.getEnvironmentAdaptor().getRSSInstances(environmentName);
    }

    public Database addDatabase(String environmentName,
                                Database database) throws RSSManagerException {
        return this.getEnvironmentAdaptor().addDatabase(environmentName, database);
    }

    public void removeDatabase(String environmentName, String rssInstanceName,
                               String databaseName) throws RSSManagerException {
        this.getEnvironmentAdaptor().removeDatabase(environmentName, rssInstanceName, databaseName);
    }

    public Database[] getDatabases(String environmentName) throws RSSManagerException {
        return this.getEnvironmentAdaptor().getDatabases(environmentName);
    }

    public Database getDatabase(String environmentName, String rssInstanceName,
                                String databaseName) throws RSSManagerException {
        return this.getEnvironmentAdaptor().getDatabase(environmentName, rssInstanceName,
                databaseName);
    }

    public boolean isDatabaseExist(String environmentName, String rssInstanceName,
                                   String databaseName) throws RSSManagerException {
        return this.getEnvironmentAdaptor().isDatabaseExist(environmentName, rssInstanceName,
                databaseName);
    }

    public boolean isDatabaseUserExist(String environmentName, String rssInstanceName,
                                       String username) throws RSSManagerException {
        return this.getEnvironmentAdaptor().isDatabaseUserExist(environmentName, rssInstanceName,
                username);
    }

    public boolean isDatabasePrivilegeTemplateExist(
            String environmentName, String templateName) throws RSSManagerException {
        return this.getEnvironmentAdaptor().isDatabasePrivilegeTemplateExist(environmentName,
                templateName);
    }

    public DatabaseUser addDatabaseUser(String environmentName,
                                        DatabaseUser user) throws RSSManagerException {
        return this.getEnvironmentAdaptor().addDatabaseUser(environmentName, user);
    }

    public void removeDatabaseUser(String environmentName,
                                   String username, String type) throws RSSManagerException {
        this.getEnvironmentAdaptor().removeDatabaseUser(environmentName, username, type);
    }

    public boolean deleteTenantRSSData(String environmentName, String tenantDomain)
            throws RSSManagerException {
        return this.getEnvironmentAdaptor().deleteTenantRSSData(environmentName, tenantDomain);
    }

    public void updateDatabaseUserPrivileges(String environmentName,
                                             DatabasePrivilegeSet privileges,
                                             DatabaseUser user,
                                             String databaseName) throws RSSManagerException {
        this.getEnvironmentAdaptor().updateDatabaseUserPrivileges(environmentName, privileges, user,
                databaseName);
    }

    public DatabaseUser getDatabaseUser(String environmentName,
                                        String username, String type) throws RSSManagerException {
        return this.getEnvironmentAdaptor().getDatabaseUser(environmentName, username, type);
    }

    public DatabaseUser[] getDatabaseUsers(
            String environmentName) throws RSSManagerException {
        return this.getEnvironmentAdaptor().getDatabaseUsers(environmentName);
    }

    public void addDatabasePrivilegeTemplate(
            String environmentName,
            DatabasePrivilegeTemplate template) throws RSSManagerException {
        this.getEnvironmentAdaptor().addDatabasePrivilegeTemplate(environmentName, template);
    }

    public void removeDatabasePrivilegeTemplate(String environmentName,
                                                String templateName) throws RSSManagerException {
        this.getEnvironmentAdaptor().removeDatabasePrivilegeTemplate(environmentName, templateName);
    }

    public void updateDatabasePrivilegeTemplate(
            String environmentName,
            DatabasePrivilegeTemplate template) throws RSSManagerException {
        this.getEnvironmentAdaptor().updateDatabasePrivilegeTemplate(environmentName, template);
    }

    public DatabasePrivilegeTemplate[] getDatabasePrivilegeTemplates(
            String environmentName) throws RSSManagerException {
        return this.getEnvironmentAdaptor().getDatabasePrivilegeTemplates(environmentName);
    }

    public DatabasePrivilegeTemplate getDatabasePrivilegeTemplate(
            String environmentName, String templateName) throws RSSManagerException {
        return this.getEnvironmentAdaptor().getDatabasePrivilegeTemplate(environmentName,
                templateName);
    }

    public void attachUser(String environmentName, UserDatabaseEntry ude,
                           String templateName) throws RSSManagerException {
        this.getEnvironmentAdaptor().attachUser(environmentName, ude, templateName);
    }

    public void detachUser(String environmentName,
                           UserDatabaseEntry ude) throws RSSManagerException {
        this.getEnvironmentAdaptor().detachUser(environmentName, ude);
    }

    public DatabaseUser[] getAttachedUsers(String environmentName, String rssInstanceName,
                                           String databaseName) throws RSSManagerException {
        return this.getEnvironmentAdaptor().getAttachedUsers(environmentName, rssInstanceName,
                databaseName);
    }

    public DatabaseUser[] getAvailableUsers(String environmentName, String rssInstanceName,
                                            String databaseName) throws RSSManagerException {
        return this.getEnvironmentAdaptor().getAvailableUsers(environmentName, rssInstanceName,
                databaseName);
    }

    public void addCarbonDataSource(String environmentName,
                                    UserDatabaseEntry entry) throws RSSManagerException {
        this.getEnvironmentAdaptor().addCarbonDataSource(environmentName, entry);
    }

    public DatabasePrivilegeSet getUserDatabasePrivileges(
            String environmentName, String rssInstanceName, String databaseName,
            String username) throws RSSManagerException {
        return this.getEnvironmentAdaptor().getUserDatabasePrivileges(environmentName,
                rssInstanceName, databaseName, username);
    }

    public Database[] getDatabasesForTenant(String environmentName,
                                            String tenantDomain) throws RSSManagerException {
        int tenantId = -1;
        Database[] databases = null;
        if (!RSSManagerUtil.isSuperTenantUser()) {
            String msg = "Unauthorized operation, only super tenant is authorized. " +
                    "Tenant domain :" + CarbonContext.getThreadLocalCarbonContext().getTenantDomain() +
                    " permission denied";
            throw new RSSManagerException(msg);
        }
        try {
            tenantId = RSSManagerUtil.getTenantId(tenantDomain);
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            databases = this.getDatabases(environmentName);
        } catch (RSSManagerCommonException e) {
            String msg = "Error occurred while retrieving database list of tenant '" +
                    tenantDomain + "'";
            throw new RSSManagerException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return databases;
    }

    public void addDatabaseForTenant(String environmentName, Database database,
                                     String tenantDomain) throws RSSManagerException {
        if (!RSSManagerUtil.isSuperTenantUser()) {
            String msg = "Unauthorized operation, only super tenant is authorized to perform " +
                    "this operation permission denied";
            log.error(msg);
            throw new RSSManagerException(msg);
        }
        try {
            int tenantId = RSSManagerUtil.getTenantId(tenantDomain);
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            this.addDatabase(environmentName, database);
        } catch (RSSManagerException e) {
            log.error("Error occurred while creating database for tenant : " + e.getMessage(), e);
            throw e;
        } catch (RSSManagerCommonException e) {
            String msg = "Error occurred while creating database '" + database.getName() +
                    "' for tenant '" + tenantDomain + "' on RSS instance '" +
                    database.getRssInstanceName() + "'";
            throw new RSSManagerException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public Database getDatabaseForTenant(String environmentName, String rssInstanceName,
                                         String databaseName,
                                         String tenantDomain) throws RSSManagerException {
        Database metaData = null;
        if (!RSSManagerUtil.isSuperTenantUser()) {
            String msg = "Unauthorized operation, only super tenant is authorized to perform " +
                    "this operation permission denied";
            log.error(msg);
            throw new RSSManagerException(msg);
        }
        try {
            int tenantId = RSSManagerUtil.getTenantId(tenantDomain);
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            metaData = this.getDatabase(environmentName, rssInstanceName, databaseName);
        } catch (RSSManagerCommonException e) {
            String msg = "Error occurred while retrieving metadata of the database '" +
                    databaseName + "' belonging to tenant '" + tenantDomain + "'";
            throw new RSSManagerException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return metaData;
    }

    public String[] getEnvironments() throws RSSManagerException {
        return this.getEnvironmentAdaptor().getEnvironments();
    }

    /**
     * Test the RSS instance connection using a mock database connection test.
     *
     * @param driverClass JDBC Driver class
     * @param jdbcURL     JDBC url
     * @param username    username
     * @param password    password
     * @return Success or failure message
     * @throws RSSManagerException RSSDAOException
     */
    public void testConnection(String driverClass, String jdbcURL, String username,
                               String password) throws RSSManagerException {
        Connection conn = null;
        int tenantId = RSSManagerUtil.getTenantId();

        if (driverClass == null || driverClass.length() == 0) {
            String msg = "Driver class is missing";
            throw new RSSManagerException(msg);
        }
        if (jdbcURL == null || jdbcURL.length() == 0) {
            String msg = "Driver connection URL is missing";
            throw new RSSManagerException(msg);
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);

            Class.forName(driverClass).newInstance();
            conn = DriverManager.getConnection(jdbcURL, username, password);
            if (conn == null) {
                String msg = "Unable to establish a JDBC connection with the database server";
                throw new RSSManagerException(msg);
            }
        } catch (Exception e) {
            String msg = "Error occurred while testing database connectivity : " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error(e);
                }
            }
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private EnvironmentAdaptor getEnvironmentAdaptor() throws RSSManagerException {
        EnvironmentAdaptor adaptor =
                RSSConfigurationManager.getInstance().getRSSManagerEnvironmentAdaptor();
        if (adaptor == null) {
            throw new IllegalArgumentException("RSS Manager Environment Adaptor is not " +
                    "initialized properly");
        }
        return adaptor;
    }

    private void handleException(String msg, Exception e) throws RSSManagerException {
        log.error(msg, e);
        throw new RSSManagerException(msg, e);
    }

}
