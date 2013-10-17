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
package org.wso2.carbon.rssmanager.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.ui.stub.RSSAdminRSSManagerExceptionException;
import org.wso2.carbon.rssmanager.ui.stub.RSSAdminStub;
import org.wso2.carbon.rssmanager.ui.stub.types.*;
import org.wso2.carbon.rssmanager.ui.stub.types.config.environment.RSSEnvironmentContext;

import java.rmi.RemoteException;
import java.util.Locale;
import java.util.ResourceBundle;

public class RSSManagerClient {

    private RSSAdminStub stub;

    private ResourceBundle bundle;

    private static final String BUNDLE = "org.wso2.carbon.rssmanager.ui.i18n.Resources";

    private static final Log log = LogFactory.getLog(RSSManagerClient.class);

    public RSSManagerClient(String cookie, String backendServerUrl,
                            ConfigurationContext configurationContext, Locale locale) {
        String serviceEndpoint = backendServerUrl + "RSSAdmin";
        bundle = java.util.ResourceBundle.getBundle(BUNDLE, locale);
        try {
            stub = new RSSAdminStub(configurationContext, serviceEndpoint);
            ServiceClient serviceClient = stub._getServiceClient();
            Options options = serviceClient.getOptions();
            options.setManageSession(true);
            options.setProperty(HTTPConstants.COOKIE_STRING, cookie);
        } catch (AxisFault axisFault) {
            log.error(axisFault);
        }
    }

    public void dropDatabasePrivilegesTemplate(RSSEnvironmentContext ctx,
                                               String templateName) throws AxisFault {
        try {
            stub.dropDatabasePrivilegesTemplate(ctx, templateName);
        } catch (Exception e) {
            handleException(bundle.getString(
                    "rss.manager.failed.to.drop.database.privilege.template") + " '" +
                    templateName + "' : " + e.getMessage(), e);
        }
    }

    public void editDatabasePrivilegesTemplate(
            RSSEnvironmentContext ctx, DatabasePrivilegeTemplate template) throws AxisFault {
        try {
            stub.editDatabasePrivilegesTemplate(ctx, template);
        } catch (Exception e) {
            handleException(bundle.getString(
                    "rss.manager.failed.to.edit.database.privilege.template") +
                    " '" + template.getName() + "' : " + e.getMessage(), e);
        }
    }

    public void createDatabasePrivilegesTemplate(
            RSSEnvironmentContext ctx, DatabasePrivilegeTemplate template) throws AxisFault {
        try {
            stub.createDatabasePrivilegesTemplate(ctx, template);
        } catch (Exception e) {
            handleException(bundle.getString(
                    "rss.manager.failed.to.create.database.privilege.template") +
                    " '" + template.getName() + "' : " + e.getMessage(), e);
        }
    }

    public DatabasePrivilegeTemplate[] getDatabasePrivilegesTemplates(
            RSSEnvironmentContext ctx) throws AxisFault {
        DatabasePrivilegeTemplate[] templates = new DatabasePrivilegeTemplate[0];
        try {
            templates = stub.getDatabasePrivilegesTemplates(ctx);
            if (templates == null) {
                return new DatabasePrivilegeTemplate[0];
            }
        } catch (Exception e) {
            handleException(bundle.getString(
                    "rss.manager.failed.to.retrieve.database.privilege.template.list") + " : " +
                    e.getMessage(), e);
        }
        return templates;
    }

    public void editUserPrivileges(RSSEnvironmentContext ctx, DatabasePrivilegeSet privileges,
                                   DatabaseUser user, String databaseName) throws AxisFault {
        try {
            stub.editDatabaseUserPrivileges(ctx, privileges, user, databaseName);
        } catch (Exception e) {
            handleException(bundle.getString("rss.manager.failed.to.edit.user") + " : '" +
                    user.getName() + "' : " + e.getMessage(), e);
        }
    }

    public void createDatabase(RSSEnvironmentContext ctx, Database database) throws AxisFault {
        try {
            stub.createDatabase(ctx, database);
        } catch (RemoteException e) {
            handleException(bundle.getString("rss.manager.failed.to.create.database") + " '" +
                    database.getName() + "' : " + e.getMessage(), e);
        } catch (RSSAdminRSSManagerExceptionException e) {
            handleException(bundle.getString("rss.manager.failed.to.create.database") + " '" +
                    database.getName() + "' : " + e.getFaultMessage().getRSSManagerException().
                    getErrorMessage(), e);
        }

    }

    public Database[] getDatabaseList(RSSEnvironmentContext ctx) throws AxisFault {
    	Database[] databases = new Database[0];
	        try {
	            databases = stub.getDatabases(ctx);
	            if (databases == null) {
	                return new Database[0];
	            }
	        } catch (Exception e) {
	            handleException(bundle.getString(
	                    "rss.manager.failed.to.retrieve.database.instance.list") + " : " +
                        e.getMessage(), e);
	        }
    	
        return databases;
    }

    public Database getDatabase(RSSEnvironmentContext ctx,
                                        String databaseName) throws AxisFault {
        Database database = null;
        try {
            database = stub.getDatabase(ctx, databaseName);
        } catch (Exception e) {
            handleException(bundle.getString(
                    "rss.manager.failed.to.retrieve.database.instance.data") + " : " +
                    e.getMessage(), e);
        }
        return database;
    }

    public void dropDatabase(RSSEnvironmentContext ctx, String databaseName) throws AxisFault {
        try {
            stub.dropDatabase(ctx, databaseName);
        } catch (Exception e) {
            handleException(bundle.getString("rss.manager.failed.to.drop.database") + " : " +
                    e.getMessage(), e);
        }
    }

    public RSSInstance[] getRSSInstanceList(RSSEnvironmentContext ctx) throws AxisFault {
        RSSInstance[] rssInstances = new RSSInstance[0];
        try {
            rssInstances = stub.getRSSInstances(ctx);
            if (rssInstances == null) {
                return new RSSInstance[0];
            }
        } catch (Exception e) {
            handleException(bundle.getString(
                    "rss.manager.failed.to.retrieve.RSS.instance.list") + " : " +
                    e.getMessage(), e);
        }
        return rssInstances;
    }

    public void createRSSInstance(RSSEnvironmentContext ctx,
                                  RSSInstance rssInstance) throws AxisFault {
        try {
            stub.createRSSInstance(ctx, rssInstance);
        } catch (Exception e) {
            handleException(bundle.getString("rss.manager.failed.to.add.database.server.instance") +
                    " :" + rssInstance.getName() + " : " + e.getMessage(), e);
        }
    }

    public void testConnection(String driverClass, String jdbcUrl,
                               String username, String password) throws AxisFault {
        try {
            stub.testConnection(driverClass, jdbcUrl, username, password);
        } catch (Exception e) {
            handleException("Error occurred while connecting to '" + jdbcUrl +
                    "' with the username '" + username + "' and the driver class '" +
                    driverClass + "' : " + e.getMessage(), e);
        }
    }

    public void editRSSInstance(RSSEnvironmentContext ctx,
                                RSSInstance rssInstance) throws AxisFault {
        try {
            stub.editRSSInstance(ctx, rssInstance);
        } catch (Exception e) {
            handleException(bundle.getString("rss.manager.failed.to.edit.database.server.instance")
                    + " :" + rssInstance.getName() + " : " + e.getMessage(), e);
        }
    }

    public DatabaseUser getDatabaseUser(RSSEnvironmentContext ctx,
                                                String username) throws AxisFault {
        DatabaseUser user = null;
        try {
            user = stub.getDatabaseUser(ctx, username);
        } catch (Exception e) {
            handleException(bundle.getString(
                    "rss.manager.failed.to.retrieve.database.user.data") + " : " + e.getMessage(), e);
        }
        return user;
    }

    public void dropDatabaseUser(RSSEnvironmentContext ctx, String username) throws AxisFault {
        try {
            stub.dropDatabaseUser(ctx, username);
        } catch (Exception e) {
            handleException(bundle.getString("rss.manager.failed.to.drop.database.user") + " : " +
                    e.getMessage(), e);
        }
    }

    public void createCarbonDataSource(RSSEnvironmentContext ctx,
                                       UserDatabaseEntry entry) throws AxisFault {
        try {
            stub.createCarbonDataSource(ctx, entry);
        } catch (Exception e) {
            handleException(bundle.getString("rss.manager.failed.to.create.carbon.datasource") +
                    " : " + e.getMessage(), e);
        }
    }

    public void createDatabaseUser(RSSEnvironmentContext ctx, DatabaseUser user) throws AxisFault {

        try {
            stub.createDatabaseUser(ctx, user);
        } catch (RemoteException e) {
            handleException(bundle.getString("rss.manager.failed.to.create.database.user") + " : " +
                    e.getMessage(), e);
        } catch (RSSAdminRSSManagerExceptionException e) {
            handleException(bundle.getString("rss.manager.failed.to.create.database.user") + " : " +
                    e.getFaultMessage().getRSSManagerException().getErrorMessage(), e);
        }

    }

    public DatabasePrivilegeTemplate getDatabasePrivilegesTemplate(
            RSSEnvironmentContext ctx, String templateName) throws AxisFault {
        DatabasePrivilegeTemplate tempalte = null;
        try {
            tempalte = stub.getDatabasePrivilegesTemplate(ctx, templateName);
        } catch (Exception e) {
            handleException(bundle.getString(
                    "rss.manager.failed.to.retrieve.database.privilege.template.data") + " : " +
                    e.getMessage(), e);
        }
        return tempalte;
    }

    private void handleException(String msg, Exception e) throws AxisFault {
        //log.error(msg, e);
        throw new AxisFault(msg, e);
    }

    public RSSInstance getRSSInstance(RSSEnvironmentContext ctx) throws AxisFault {
        RSSInstance rssIns = null;
        try {
            rssIns = stub.getRSSInstance(ctx);
        } catch (Exception e) {
            handleException(bundle.getString(
                    "rss.manager.failed.to.retrieve.database.server.instance.properties") + " : " +
                    e.getMessage(), e);
        }
        return rssIns;
    }

    public DatabaseUser[] getDatabaseUsers(RSSEnvironmentContext ctx) throws AxisFault {
        DatabaseUser[] users = new DatabaseUser[0];
        try {
            users = stub.getDatabaseUsers(ctx);
        } catch (Exception e) {
            handleException(bundle.getString("rss.manager.failed.to.retrieve.database.users") +
                    ": " + e.getMessage(), e);
        }
        return users;
    }

    public void dropRSSInstance(RSSEnvironmentContext ctx) throws AxisFault {
        try {
            stub.dropRSSInstance(ctx, ctx.getRssInstanceName());
        } catch (Exception e) {
            handleException(bundle.getString(
                    "rss.manager.failed.to.drop.database.server.instance") + " '" +
                    ctx.getRssInstanceName() + "' : " + e.getMessage(), e);
        }
    }

    public int getSystemRSSInstanceCount(RSSEnvironmentContext ctx) throws AxisFault {
        int count = 0;
        try {
            count = stub.getSystemRSSInstanceCount(ctx);
        } catch (Exception e) {
            handleException(bundle.getString(
                    "rss.manager.failed.to.retrieve.system.rss.instance.count") + " : " +
                    e.getMessage(), e);
        }
        return count;
    }

    public void attachUserToDatabase(RSSEnvironmentContext ctx, String databaseName,
                                     String username, String templateName) throws AxisFault {
        try {
            UserDatabaseEntry entry = new UserDatabaseEntry();
            entry.setRssInstanceName(ctx.getRssInstanceName());
            entry.setDatabaseName(databaseName);
            entry.setUsername(username);
            stub.attachUserToDatabase(ctx, entry, templateName);
        } catch (Exception e) {
            String msg =
                    bundle.getString("rss.manager.failed.to.attach.user.to.database")
                            + " '" + databaseName + "' : " + e.getMessage();
            handleException(msg, e);
        }
    }

    public void detachUserFromDatabase(RSSEnvironmentContext ctx, String databaseName,
                                       String username) throws AxisFault {
        try {
            UserDatabaseEntry entry = new UserDatabaseEntry();
            entry.setDatabaseName(databaseName);
            entry.setRssInstanceName(ctx.getRssInstanceName());
            entry.setUsername(username);

            stub.detachUserFromDatabase(ctx, entry);
        } catch (Exception e) {
            String msg =
                    bundle.getString("rss.manager.failed.to.detach.user.from.database")
                            + " '" + databaseName + "' : " + e.getMessage();
            handleException(msg, e);
        }
    }

    public DatabaseUser[] getUsersAttachedToDatabase(RSSEnvironmentContext ctx,
                                                             String databaseName) throws AxisFault {
        DatabaseUser[] users = new DatabaseUser[0];
        try {
            users = stub.getUsersAttachedToDatabase(ctx, databaseName);
        } catch (Exception e) {
            String msg =
                    bundle.getString("rss.manager.failed.to.retrieve.users.attached.to.the.database")
                            + " '" + databaseName + "' : " + e.getMessage();
            handleException(msg, e);
        }
        return users;
    }

    public DatabaseUser[] getAvailableUsersToAttachToDatabase(
            RSSEnvironmentContext ctx, String databaseName) throws AxisFault {
        DatabaseUser[] users = new DatabaseUser[0];
        try {
            users = stub.getAvailableUsersToAttachToDatabase(ctx, databaseName);
        } catch (Exception e) {
            String msg =
                    bundle.getString("rss.manager.failed.to.retrieve.available.database.users") +
                            " '" + databaseName + "' : " + e.getMessage();
            handleException(msg, e);
        }
        return users;
    }


    public DatabasePrivilegeSet getUserDatabasePermissions(
            RSSEnvironmentContext ctx, String databaseName, String username) throws AxisFault {
        DatabasePrivilegeSet privileges = null;
        try {
            privileges = stub.getUserDatabasePermissions(ctx, databaseName, username);
        } catch (Exception e) {
            String msg =
                    bundle.getString("rss.manager.failed.to.retrieve.database.permissions.granted.to.the.user") +
                            " '" + username + "' on the database '" + databaseName + "' : " + e.getMessage();
            handleException(msg, e);
        }
        return privileges;
    }

    public String[] getRSSEnvironmentNames() throws AxisFault {
        String[] environments = new String[0];
        try {
            environments = stub.getRSSEnvironmentNames();
            if (environments == null) {
                return new String[0];
            }
        } catch (Exception e) {
            String msg =
                    bundle.getString("rss.manager.failed.to.retrieve.rss.environments.list") +
                            " : " + e.getMessage();
            handleException(msg, e);
        }
        return environments;
    }
  
}
