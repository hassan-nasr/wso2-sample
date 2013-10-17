/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */
package org.wso2.carbon.appfactory.core.services;

import com.hazelcast.impl.ClientEndpoint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.appfactory.core.task.AppFactoryTenantCloudInitializerTask;
import org.wso2.carbon.appfactory.core.task.AppFactoryTenantRepositoryInitializerTask;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Service used to initialize all the 3rd party tools on tenant creation
 * .
 */
public class AppFactoryTenantInfraStructureInitializerService extends AbstractAdmin {
    private static final Log log = LogFactory.getLog(AppFactoryTenantInfraStructureInitializerService.class);
    public static final String APP_FACTORY_TASK_MANAGER = "appfactory.task.manager";
    public static final String REPOSITORY_INITIALIZER_TASK = "org.wso2.carbon.appfactory.core.task" +
            ".AppFactoryTenantRepositoryInitializerTask";
    public static final String BUILD_MANAGER_INITIALIZER_TASK = "org.wso2.carbon.appfactory.core.task" +
            ".AppFactoryTenantBuildManagerInitializerTask";
    public static final String CLOUD_INITIALIZER_TASK = "org.wso2.carbon.appfactory.core.task" +
            ".AppFactoryTenantCloudInitializerTask";
    private String ENVIRONMENT = "ApplicationDeployment.DeploymentStage";
    private TaskManager taskManager;

    public AppFactoryTenantInfraStructureInitializerService() throws AppFactoryException {
        try {
            taskManager = ServiceHolder.getInstance().getTaskService().getTaskManager
                    (APP_FACTORY_TASK_MANAGER);
        } catch (TaskException e) {
            String msg = "Error while getting " + APP_FACTORY_TASK_MANAGER;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
    }

    /**
     * Used to initialize a repository manager
     *
     * @param tenantDomain
     * @param usagePlan
     * @return
     */
    public boolean initializeRepositoryManager(String tenantDomain, String usagePlan) throws AppFactoryException {
        TaskInfo.TriggerInfo triggerInfo = getTriggerWithDalay();

        String taskName = "repository-init-" + tenantDomain;
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(AppFactoryTenantRepositoryInitializerTask.TENANT_DOMAIN, tenantDomain);
        properties.put(AppFactoryTenantRepositoryInitializerTask.TENANT_USAGE_PLAN, usagePlan);
        TaskInfo taskInfo = new TaskInfo(taskName, AppFactoryTenantInfraStructureInitializerService.REPOSITORY_INITIALIZER_TASK,
                properties, triggerInfo);
        try {
            taskManager.registerTask(taskInfo);
        } catch (TaskException e) {
            String msg = "Error while registering " + taskName;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        try {
            taskManager.scheduleTask(taskName);
        } catch (TaskException e) {
            String msg = "Error while scheduling " + taskName;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        return true;
    }

    /**
     * Used to initialize build manager
     *
     * @param tenantDomain
     * @param usagePlan
     * @return
     */
    public boolean initializeBuildManager(String tenantDomain, String usagePlan) throws AppFactoryException {
        TaskInfo.TriggerInfo triggerInfo = getTriggerWithDalay();
        String taskName = "build-init-" + tenantDomain;
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(AppFactoryTenantRepositoryInitializerTask.TENANT_DOMAIN, tenantDomain);
        properties.put(AppFactoryTenantRepositoryInitializerTask.TENANT_USAGE_PLAN, usagePlan);
        TaskInfo taskInfo = new TaskInfo(taskName, AppFactoryTenantInfraStructureInitializerService.BUILD_MANAGER_INITIALIZER_TASK,
                properties, triggerInfo);
        try {
            taskManager.registerTask(taskInfo);
        } catch (TaskException e) {
            String msg = "Error while registering " + taskName;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        try {
            taskManager.scheduleTask(taskName);
        } catch (TaskException e) {
            String msg = "Error while scheduling " + taskName;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        return true;
    }

    /**
     * Used to initialize cloud in different stages
     *
     * @param bean  with tenant details
     * @param stage Environment
     * @return true if the operation is success
     * @throws AppFactoryException
     */
    public boolean initializeCloudManager(TenantInfoBean bean, String stage) throws AppFactoryException {
        AppFactoryConfiguration configuration = ServiceHolder.getAppFactoryConfiguration();
        String serverURL = configuration.getFirstProperty(ENVIRONMENT + "." + stage + "." + "TenantMgtUrl");
        String serviceEPR=serverURL+"/services/TenantMgtAdminService";
        String adminUsername=configuration.getFirstProperty(AppFactoryConstants.SERVER_ADMIN_NAME);
        String adminPassword=configuration.getFirstProperty(AppFactoryConstants.SERVER_ADMIN_PASSWORD);;
        TaskInfo.TriggerInfo triggerInfo = getTriggerWithDalay();
        String taskName = "cloud-init-" + stage + "-" + bean.getTenantDomain();
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(AppFactoryTenantCloudInitializerTask.TENANT_USAGE_PLAN, bean.getUsagePlan());
        properties.put(AppFactoryTenantCloudInitializerTask.TENANT_DOMAIN, bean.getTenantDomain());
        properties.put(AppFactoryTenantCloudInitializerTask.SUCCESS_KEY, bean.getSuccessKey());
        properties.put(AppFactoryTenantCloudInitializerTask.ADMIN_USERNAME, bean.getAdmin());
        properties.put(AppFactoryTenantCloudInitializerTask.ADMIN_EMAIL, bean.getEmail());
        properties.put(AppFactoryTenantCloudInitializerTask.ADMIN_FIRST_NAME, bean.getFirstname());
        properties.put(AppFactoryTenantCloudInitializerTask.ADMIN_LAST_NAME, bean.getLastname());
        properties.put(AppFactoryTenantCloudInitializerTask.ORIGINATED_SERVICE,
                "WSO2 Stratos Manager");

        properties.put(AppFactoryTenantCloudInitializerTask.SERVICE_EPR, serviceEPR);
        properties.put(AppFactoryTenantCloudInitializerTask.SUPER_TENANT_ADMIN, adminUsername);
        properties.put(AppFactoryTenantCloudInitializerTask.SUPER_TENANT_ADMIN_PASSWORD,adminPassword);

        TaskInfo taskInfo = new TaskInfo(taskName, AppFactoryTenantInfraStructureInitializerService.CLOUD_INITIALIZER_TASK,
                properties, triggerInfo);
        try {
            taskManager.registerTask(taskInfo);
        } catch (TaskException e) {
            String msg = "Error while registering " + taskName;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        try {
            taskManager.rescheduleTask(taskInfo.getName());
        } catch (TaskException e) {
            String msg = "Error while scheduling " + taskName;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        return true;
    }

    private TaskInfo.TriggerInfo getTriggerWithDalay() {
        TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo();
        //trigger immediately after 5s and one time
        Calendar triggerTime=Calendar.getInstance();
        triggerTime.roll(Calendar.SECOND, 5);
        triggerInfo.setStartTime(triggerTime.getTime());
        triggerInfo.setRepeatCount(0);
        return triggerInfo;
    }
}
