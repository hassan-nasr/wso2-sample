/*
Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

WSO2 Inc. licenses this file to you under the Apache License,
Version 2.0 (the "License"); you may not use this file except
in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

package org.wso2.carbon.registry.activity.search;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.registry.ActivityAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

/**
 * A test case which tests registry activity search operation
 */
public class ActivitySearchByUserNameTestCase {
    private static final Log log = LogFactory.getLog(ActivitySearchByUserNameTestCase.class);

    private String wsdlPath = "/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/";
    private String resourceName = "sample.wsdl";
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private ActivityAdminServiceClient activityAdminServiceClient;
    private UserManagementClient userManagementClient;
    private ManageEnvironment environment;
    private int userId = ProductConstant.ADMIN_USER_ID;
    private UserInfo userInfo;
    private ServiceManager serviceManager;
    private WsdlManager wsdlManager;

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        log.info("Initializing Tests for Activity Search");
        log.debug("Activity Search Tests Initialised");
        userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        log.debug("Running SuccessCase");
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());
        activityAdminServiceClient =
                new ActivityAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());
        userManagementClient =
                new UserManagementClient(environment.getGreg().getBackEndUrl(),
                                         environment.getGreg().getSessionCookie());
        WSRegistryServiceClient wsRegistry =
                new RegistryProviderUtil().getWSRegistry(userId,
                                                         ProductConstant.GREG_SERVER_NAME);
        Registry governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, userId);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        serviceManager = new ServiceManager(governance);
        wsdlManager = new WsdlManager(governance);
    }


    @Test(groups = {"wso2.greg"})
    public void addResource() throws InterruptedException, MalformedURLException,
                                     ResourceAdminServiceExceptionException, RemoteException {
        String resource = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                          File.separator + "GREG" + File.separator +
                          "wsdl" + File.separator + "sample.wsdl";

        resourceAdminServiceClient.addResource(wsdlPath + resourceName,
                                               "application/wsdl+xml", "test resource",
                                               new DataHandler(new URL("file:///" + resource)));


        // wait for sometime until the resource has been added. The activity logs are written
        // every 10 seconds, so you'll need to wait until that's done.
        Thread.sleep(20000);
        assertTrue(resourceAdminServiceClient.getResource(wsdlPath + resourceName)[0].getAuthorUserName().
                contains(userInfo.getUserNameWithoutDomain()));

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addResource"})
    public void searchActivityByAvailableUser() throws RegistryExceptionException, RemoteException,
                                                       ResourceAdminServiceExceptionException {
        assertNotNull(activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(),
                                                               userInfo.getUserNameWithoutDomain(),
                                                               "/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/" +
                                                               resourceName, "", "", "", 0).getActivity());

    }


    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"searchActivityByAvailableUser"})
    public void searchActivityByUnAvailableUser()
            throws Exception {
        int id = 0;
        String unAvailableUser = "testUser" + id;
        while (userManagementClient.userNameExists(unAvailableUser, unAvailableUser)) {
            if (!userManagementClient.userNameExists(unAvailableUser, unAvailableUser)) {
                id++;
            }
            unAvailableUser = "testUser" + id;
        }
        assertNull(activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(),
                                                            unAvailableUser, "", "", "", "", 0).getActivity());
    }

    @AfterClass(groups = {"wso2.greg"})
    public void deleteResources()
            throws ResourceAdminServiceExceptionException, RemoteException, GovernanceException {
        Endpoint[] endpoints = null;
        Wsdl[] wsdls = wsdlManager.getAllWsdls();
        for (Wsdl wsdl : wsdls) {
            if (wsdl.getQName().getLocalPart().equals("sample.wsdl")) {
                endpoints = wsdlManager.getWsdl(wsdl.getId()).getAttachedEndpoints();
            }
        }
        resourceAdminServiceClient.deleteResource(wsdlPath + "sample.wsdl");
        for (Endpoint path : endpoints) {
            resourceAdminServiceClient.deleteResource("_system/governance/" + path.getPath());
        }

        Service[] services = serviceManager.getAllServices();
        for (Service service : services) {
            if (service.getQName().getLocalPart().equals("Info")) {
                serviceManager.removeService(service.getId());
            }
        }

        resourceAdminServiceClient = null;
        serviceManager = null;
        wsdlManager = null;

    }

}

