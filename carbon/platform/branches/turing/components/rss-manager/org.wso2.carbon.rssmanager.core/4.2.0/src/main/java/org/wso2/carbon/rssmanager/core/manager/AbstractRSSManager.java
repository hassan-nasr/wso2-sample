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

package org.wso2.carbon.rssmanager.core.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.core.RSSTransactionManager;
import org.wso2.carbon.rssmanager.core.config.RSSConfig;
import org.wso2.carbon.rssmanager.core.dao.RSSDAO;
import org.wso2.carbon.rssmanager.core.dao.RSSDAOFactory;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dao.util.EntityManager;
import org.wso2.carbon.rssmanager.core.entity.*;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.internal.RSSManagerDataHolder;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

import java.sql.Connection;

public abstract class AbstractRSSManager implements RSSManager {

    private RSSDAO rssDAO;
    private EntityManager entityManager;
    private Environment environment;
    private static final Log log = LogFactory.getLog(RSSManager.class);

    public AbstractRSSManager(Environment environment, RSSConfig config) {
        this.environment = environment;
        /* Initializing RSS transaction manager wrapper */
        RSSTransactionManager rssTxManager =
                new RSSTransactionManager(RSSManagerDataHolder.getInstance().
                        getTransactionManager());
        this.entityManager = new EntityManager(rssTxManager);
        try {
            this.rssDAO = 
                    RSSDAOFactory.getRSSDAO(config.getRSSManagementRepository(), getEntityManager());
        } catch (RSSDAOException e) {
            throw new RuntimeException("Error occurred while initializing RSSDAO", e);
        }
    }

    public Database[] getDatabases() throws RSSManagerException {
        Database[] databases = new Database[0];
        boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            databases =
                    getRSSDAO().getDatabaseDAO().getDatabases(getEnvironmentName(), tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while retrieving metadata " +
                    "corresponding to databases, from RSS metadata repository : " +
                    e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return databases;
    }

    public DatabaseUser[] getDatabaseUsers() throws RSSManagerException {
        DatabaseUser[] users = new DatabaseUser[0];
        boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            users = getRSSDAO().getDatabaseUserDAO().getDatabaseUsers(getEnvironmentName(),
                    tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while retrieving metadata " +
                    "corresponding to database users, from RSS metadata repository : " +
                    e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return users;
    }
    
    public Database[] getDatabasesRestricted() throws RSSManagerException {
        boolean inTx = false;
        Database[] databases = new Database[0];
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            inTx = getEntityManager().beginTransaction();
            databases =
                    getRSSDAO().getDatabaseDAO().getAllDatabases(getEnvironmentName(), tenantId);
        } catch (RSSDAOException e) {
            getEntityManager().rollbackTransaction();
            String msg = "Error occurred while retrieving databases list";
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return databases;
    }

    public RSSDAO getRSSDAO() {
        return rssDAO;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }
    
    protected Connection getConnection(String rssInstanceName) throws RSSManagerException {
        RSSInstanceDSWrapper dsWrapper = getEnvironment().getDSWrapperRepository().
                getRSSInstanceDSWrapper(rssInstanceName);
        if (dsWrapper == null) {
            throw new RSSManagerException("Cannot fetch a connection. RSSInstanceDSWrapper " +
                    "associated with '" + rssInstanceName + "' RSS instance is null.");
        }
        return dsWrapper.getConnection();
    }

    protected Connection getConnection(String rssInstanceName,
                                       String dbName) throws RSSManagerException {
        RSSInstanceDSWrapper dsWrapper =
                getEnvironment().getDSWrapperRepository().getRSSInstanceDSWrapper(rssInstanceName);
        if (dsWrapper == null) {
            throw new RSSManagerException("Cannot fetch a connection. RSSInstanceDSWrapper " +
                    "associated with '" + rssInstanceName + "' RSS instance is null.");
        }
        return dsWrapper.getConnection(dbName);
    }

	public boolean deleteTenantRSSData() throws RSSManagerException {
		boolean inTx = false;
		Database[] databases;
		DatabaseUser[] dbUsers;
		DatabasePrivilegeTemplate[] templates;
		try {
            final int tenantId = RSSManagerUtil.getTenantId();
			// Delete tenant specific tables along with it's meta data
			databases = getRSSDAO().getDatabaseDAO().getDatabases(getEnvironmentName(), tenantId);
			log.info("Deleting rss tables and meta data");
			for (Database db : databases) {
				String databaseName = db.getName();
				String rssInstanceName = db.getRssInstanceName();
				dropDatabase(rssInstanceName, databaseName);
			}
			dbUsers = getRSSDAO().getDatabaseUserDAO().getDatabaseUsers(
					getEnvironmentName(), tenantId);
			log.info("Deleting rss users and meta data");
			for (DatabaseUser user : dbUsers) {
				String userName = user.getName();
				String rssInstanceName = user.getRssInstanceName();
				dropDatabaseUser(rssInstanceName, userName);
			}
			log.info("Deleting rss templates and meta data");
			templates = getRSSDAO().getDatabasePrivilegeTemplateDAO()
					.getDatabasePrivilegesTemplates(getEnvironmentName(),
							tenantId);
			inTx = this.getEntityManager().beginTransaction();
			for (DatabasePrivilegeTemplate template : templates) {
				//dropDatabasePrivilegesTemplate(template.getName());
			}
			log.info("Successfully deleted rss data");

		} catch (Exception e) {
			if (inTx && getEntityManager().hasNoActiveTransaction()) {
				getEntityManager().rollbackTransaction();
			}
			String msg = "Error occurred while retrieving metadata "
					+ "corresponding to databases, from RSS metadata repository : "
					+ e.getMessage();
			handleException(msg, e);
		} finally {
			if (inTx) {
				getEntityManager().endTransaction();
			}
		}
		return true;
	}

    public void handleException(String msg, Exception e) throws RSSManagerException {
        log.error(msg, e);
        throw new RSSManagerException(msg, e);
    }

    public void handleException(String msg) throws RSSManagerException {
        log.error(msg);
        throw new RSSManagerException(msg);
    }

    public String getEnvironmentName() {
        return environment.getName();
    }

    public Environment getEnvironment() {
        return environment;
    }
    
}
