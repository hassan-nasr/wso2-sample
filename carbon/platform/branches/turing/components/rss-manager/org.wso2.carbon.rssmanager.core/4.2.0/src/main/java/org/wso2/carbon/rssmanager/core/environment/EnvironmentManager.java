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

package org.wso2.carbon.rssmanager.core.environment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.config.RSSConfig;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dao.util.EntityManager;
import org.wso2.carbon.rssmanager.core.entity.DatabasePrivilegeTemplate;
import org.wso2.carbon.rssmanager.core.entity.RSSInstance;
import org.wso2.carbon.rssmanager.core.environment.dao.EnvironmentManagementDAO;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.manager.adaptor.RSSManagerAdaptor;
import org.wso2.carbon.rssmanager.core.manager.adaptor.RSSManagerAdaptorFactory;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.HashMap;
import java.util.Map;

public class EnvironmentManager {

    private Environment[] environments;
    private EntityManager entityManager;
    private EnvironmentManagementDAO environmentMgtDAO;
    private static final Log log = LogFactory.getLog(EnvironmentManager.class);

    public EnvironmentManager(Environment[] environments) {
        this.environments = environments;
    }

    public void removeEnvironment(String environmentName) throws RSSManagerException {

    }

    public RSSInstance addRSSInstance(RSSInstance rssInstance) throws RSSManagerException {
        boolean inTx = this.getEntityManager().beginTransaction();
        try {
            int tenantId = RSSManagerUtil.getTenantId();
            this.getEnvironmentMgtDAO().getRSSInstanceDAO().addRSSInstance(
                    rssInstance.getEnvironmentName(), rssInstance, tenantId);
            return rssInstance;
        } catch (RSSDAOException e) {
            if (inTx && this.getEntityManager().hasNoActiveTransaction()) {
                this.getEntityManager().rollbackTransaction();
            }
            throw new RSSManagerException("Error occurred while adding RSS instance '" +
                    rssInstance.getName() + "' to environment '" +
                    rssInstance.getEnvironmentName() + "'");
        } finally {
            if (inTx) {
                this.getEntityManager().endTransaction();
            }
        }
    }

    public void removeRSSInstance(String environmentName,
                                  String rssInstanceName) throws RSSManagerException {
        boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            //getDSWrapperRepository().removeRSSInstanceDSWrapper(rssInstanceName);
            this.getEnvironmentMgtDAO().getRSSInstanceDAO().removeRSSInstance(environmentName,
                    rssInstanceName, tenantId);
            //TODO : Drop dependent databases etc.
        } catch (RSSDAOException e) {
            if (inTx && this.getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while removing metadata related to " +
                    "RSS instance '" + rssInstanceName + "' from RSS metadata repository : " +
                    e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                this.getEntityManager().endTransaction();
            }
        }
    }

    public void updateRSSInstance(String environmentName,
                                  RSSInstance rssInstance) throws RSSManagerException {
        boolean inTx = this.getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            this.getEnvironmentMgtDAO().getRSSInstanceDAO().updateRSSInstance(environmentName,
                    rssInstance, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && this.getEntityManager().hasNoActiveTransaction()) {
                this.getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while updating metadata related to " +
                    "RSS instance '" + rssInstance.getName() + "' in RSS metadata repository : " +
                    e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                this.getEntityManager().endTransaction();
            }
        }
    }

    private EntityManager getEntityManager() {
        return entityManager;
    }

    public RSSInstance getRSSInstance(String environmentName,
                                      String rssInstanceName) throws RSSManagerException {
        RSSInstance rssInstance = null;
        boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            rssInstance = this.getEnvironmentMgtDAO().getRSSInstanceDAO().getRSSInstance(
                    environmentName, rssInstanceName, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while retrieving metadata " +
                    "corresponding to RSS instance '" + rssInstanceName + "', from RSS metadata " +
                    "repository : " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return rssInstance;
    }

    public int getSystemRSSInstanceCount(String environmentName) throws RSSManagerException {
        boolean inTx = getEntityManager().beginTransaction();
        try {
            RSSInstance[] sysRSSInstances =
                    this.getEnvironmentMgtDAO().getRSSInstanceDAO().getRSSInstances(environmentName,
                            MultitenantConstants.SUPER_TENANT_ID);
            return sysRSSInstances.length;
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while retrieving the system RSS instance count : " +
                    e.getMessage();
            throw new RSSManagerException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
    }


    public RSSInstance[] getRSSInstances(String environmentName) throws RSSManagerException {
        RSSInstance[] rssInstances = new RSSInstance[0];
        boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            rssInstances =
                    this.getEnvironmentMgtDAO().getRSSInstanceDAO().getRSSInstances(environmentName,
                            tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                this.getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while retrieving metadata related to " +
                    "RSS instances from RSS metadata repository : " + e.getMessage();
            this.handleException(msg, e);
        } finally {
            if (inTx) {
                this.getEntityManager().endTransaction();
            }
        }
        return rssInstances;
    }

//    public RSSInstance getRoundRobinAssignedDatabaseServer(
//            String environmentName) throws RSSManagerException {
//        RSSInstance rssInstance = null;
//        boolean inTx = getEntityManager().beginTransaction();
//        try {
//            RSSInstance[] rssInstances =
//                    this.getEnvironmentMgtDAO().getRSSInstanceDAO().getRSSInstances(environmentName,
//                            MultitenantConstants.SUPER_TENANT_ID);
//            int count =
//                    this.getEnvironmentMgtDAO().getDatabaseDAO().getSystemRSSDatabaseCount(
//                            environmentName);
//
//            int rssInstanceCount = rssInstances.length;
//            for (int i = 0; i < rssInstanceCount; i++) {
//                if (i == count % rssInstanceCount) {
//                    rssInstance = rssInstances[i];
//                    if (rssInstance != null) {
//                        return rssInstance;
//                    }
//                }
//            }
//        } catch (RSSDAOException e) {
//            if (inTx && getEntityManager().hasNoActiveTransaction()) {
//                this.getEntityManager().rollbackTransaction();
//            }
//            String msg = "Error occurred while retrieving metadata " +
//                    "corresponding to the round robin assigned RSS instance, from RSS metadata " +
//                    "repository : " + e.getMessage();
//            this.handleException(msg, e);
//        } finally {
//            if (inTx) {
//                this.getEntityManager().endTransaction();
//            }
//        }
//        return rssInstance;
//    }

    public void initEnvironments(String rssProvider, RSSConfig config) throws RSSManagerException {
        for (Environment environment : this.getEnvironments()) {
            this.addEnvironment(environment);
            RSSManagerAdaptor rmAdaptor =
                    RSSManagerAdaptorFactory.getRSSManagerAdaptor(rssProvider, config);
            environment.init(rmAdaptor);
        }
    }

    public void addEnvironment(Environment environment) throws RSSManagerException {
        boolean inTx = this.getEntityManager().beginTransaction();
        try {
            int tenantId = RSSManagerUtil.getTenantId();
            if (!this.getEnvironmentMgtDAO().getEnvironmentDAO().isEnvironmentExist(
                    environment.getName())) {
                this.getEnvironmentMgtDAO().getEnvironmentDAO().addEnvironment(environment);
            }
            Map<String, RSSInstance> rssInstances = new HashMap<String, RSSInstance>();
            for (RSSInstance rssInstance : environment.getRSSInstances()) {
                rssInstances.put(rssInstance.getName(), rssInstance);
            }
            for (RSSInstance tmpInst :
                    this.getEnvironmentMgtDAO().getRSSInstanceDAO().getSystemRSSInstances(
                            environment.getName(), tenantId)) {
                RSSInstance reloadedRssInst = rssInstances.get(tmpInst.getName());
                RSSInstance prevKey = rssInstances.remove(tmpInst.getName());
                if (prevKey == null) {
                    log.warn("Configuration corresponding to RSS instance named '" + tmpInst.getName() +
                            "' is missing in the rss-config.xml");
                    continue;
                }
                this.getEnvironmentMgtDAO().getRSSInstanceDAO().updateRSSInstance(
                        environment.getName(), reloadedRssInst, tenantId);
            }
            for (RSSInstance inst : rssInstances.values()) {
                //Checks if the rss instance is one of wso2's rss instance's or if it is a user defined instance. Throws an error if it is neither.
                if (RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE.equals(inst.getInstanceType()) ||
                        RSSManagerConstants.USER_DEFINED_INSTANCE_TYPE.equals(inst.getInstanceType())) {
                    this.getEnvironmentMgtDAO().getRSSInstanceDAO().addRSSInstance(
                            environment.getName(), inst, tenantId);
                } else {
                    throw new RSSManagerException("The instance type '" + inst.getInstanceType() +
                            "' is invalid.");
                }
            }
        } catch (RSSDAOException e) {
            if (inTx && this.getEntityManager().hasNoActiveTransaction()) {
                this.getEntityManager().rollbackTransaction();
            }
            String msg =
                    "Error occurred while initialize RSS environment '" +environment.getName() + "'";
            handleException(msg, e);
        } finally {
            if (inTx) {
                this.getEntityManager().endTransaction();
            }
        }
    }

    public void handleException(String msg, Exception e) throws RSSManagerException {
        log.error(msg, e);
        throw new RSSManagerException(msg, e);
    }

    private EnvironmentManagementDAO getEnvironmentMgtDAO() {
        return environmentMgtDAO;
    }

    public Environment[] getEnvironments() {
        return environments;
    }

    public Environment getEnvironment(String environmentName) {
        for (Environment environment : this.getEnvironments()) {
            if (environment.getName().equals(environmentName)) {
                return environment;
            }
        }
        return null;
    }

       public boolean isDatabasePrivilegeTemplateExist(
               String environmentName, String templateName) throws RSSManagerException {
        boolean isExist = false;
        final boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            isExist =
                    this.getEnvironmentMgtDAO().getDatabasePrivilegeTemplateDAO().
                            isDatabasePrivilegeTemplateExist(environmentName, templateName, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while checking whether the database " +
                    "privilege template named '" + templateName + "' already exists : " +
                    e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return isExist;
    }

    public void dropDatabasePrivilegesTemplate(String environmentName,
                                               String templateName) throws RSSManagerException {
        final boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            this.getEnvironmentMgtDAO().getDatabasePrivilegeTemplateDAO().
                    removeDatabasePrivilegesTemplateEntries(environmentName, templateName,
                            tenantId);
            this.getEnvironmentMgtDAO().getDatabasePrivilegeTemplateDAO().
                    removeDatabasePrivilegesTemplate(environmentName, templateName, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while removing metadata related to " +
                    "database privilege template '" + templateName + "', from RSS metadata " +
                    "repository : " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
    }

    public DatabasePrivilegeTemplate[] getDatabasePrivilegeTemplates(
            String environmentName) throws RSSManagerException {
        DatabasePrivilegeTemplate[] templates = new DatabasePrivilegeTemplate[0];
        final boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            templates =
                    this.getEnvironmentMgtDAO().getDatabasePrivilegeTemplateDAO().
                            getDatabasePrivilegesTemplates(environmentName, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while retrieving metadata " +
                    "corresponding to database privilege templates : " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return templates;
    }

    public DatabasePrivilegeTemplate getDatabasePrivilegeTemplate(
            String environmentName, String templateName) throws RSSManagerException {
        DatabasePrivilegeTemplate template = null;
        boolean inTx = getEntityManager().beginTransaction();
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            template =
                    this.getEnvironmentMgtDAO().getDatabasePrivilegeTemplateDAO().
                            getDatabasePrivilegesTemplate(environmentName, templateName, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while retrieving metadata " +
                    "corresponding to database privilege template '" + templateName +
                    "', from RSS metadata repository : " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return template;
    }

      public void createDatabasePrivilegesTemplate(
            String environmentName, DatabasePrivilegeTemplate template) throws RSSManagerException {
        boolean inTx = getEntityManager().beginTransaction();
        try {
            if (template == null) {
                getEntityManager().rollbackTransaction();
                String msg = "Database privilege template information cannot be null";
                log.error(msg);
                throw new RSSManagerException(msg);
            }
            final int tenantId = RSSManagerUtil.getTenantId();
            boolean isExist =
                    this.getEnvironmentMgtDAO().getDatabasePrivilegeTemplateDAO().isDatabasePrivilegeTemplateExist(
                            environmentName, template.getName(), tenantId);
            if (isExist) {
                getEntityManager().rollbackTransaction();
                String msg = "A database privilege template named '" + template.getName() +
                        "' already exists";
                log.error(msg);
                throw new RSSManagerException(msg);
            }
            this.getEnvironmentMgtDAO().getDatabasePrivilegeTemplateDAO().
                    addDatabasePrivilegesTemplate(environmentName, template, tenantId);
            this.getEnvironmentMgtDAO().getDatabasePrivilegeTemplateDAO().
                    setPrivilegeTemplateProperties(environmentName, template, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while adding metadata related to " +
                    "database privilege template '" + template.getName() + "', to RSS metadata " +
                    "repository : " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
    }

    public void editDatabasePrivilegesTemplate(String environmentName,
            DatabasePrivilegeTemplate template) throws RSSManagerException {
        boolean inTx = getEntityManager().beginTransaction();
        try {
            if (template == null) {
                getEntityManager().rollbackTransaction();
                String msg = "Database privilege template information cannot be null";
                log.error(msg);
                throw new RSSManagerException(msg);
            }
            final int tenantId = RSSManagerUtil.getTenantId();
            this.getEnvironmentMgtDAO().getDatabasePrivilegeTemplateDAO().
                    updateDatabasePrivilegesTemplate(environmentName, template, tenantId);
        } catch (RSSDAOException e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while updating metadata " +
                    "corresponding to database privilege template '" + template.getName() +
                    "', in RSS metadata repository : " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
    }

}
