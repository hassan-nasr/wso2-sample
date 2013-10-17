/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.registry.jira2.issues.test2;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleAdminServiceClient;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleManagementClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.services.ArrayOfString;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

/**
 * https://wso2.org/jira/browse/CARBON-11407
 */
public class Carbon11634 {

    private Registry governance;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String pathPrefix = "/_system/governance";
    private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
    private ServiceManager serviceManager;
    private String servicePath;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private final String NEW_VERSION = "1.2.3";


    @BeforeClass
    public void init() throws Exception {
        int userId = ProductConstant.ADMIN_USER_ID;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        serviceManager = new ServiceManager(governance);
        lifeCycleAdminServiceClient =
                new LifeCycleAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                environment.getGreg().getSessionCookie());
        lifeCycleManagementClient =
                new LifeCycleManagementClient(environment.getGreg().getBackEndUrl(),
                                              environment.getGreg().getSessionCookie());

        wsRegistryServiceClient = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               environment.getGreg().getSessionCookie());

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        governance = registryProviderUtil.getGovernanceRegistry(wsRegistryServiceClient, userId);

    }

    @Test(groups = {"wso2.greg"}, description = "add a service")
    public void testAddService() throws RegistryException, IOException {
        String OLDER_VERSION = "1.0.0";
        addService("test_name1", "test_namespace1", OLDER_VERSION);
    }

    @Test(groups = {"wso2.greg"}, description = "create lifecycle", dependsOnMethods = {"testAddService"})
    public void testCreateLc()
            throws GovernanceException, IOException, LifeCycleManagementServiceExceptionException {

        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                              File.separator + "GREG" + File.separator + "lifecycle" + File.separator +
                              "MultiplePromoteDemoteLCViewVersionsTrue.xml";
        String lifeCycleContent = FileManager.readFile(resourcePath);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);

        String[] lifeClycles = lifeCycleManagementClient.getLifecycleList();
        boolean lccreated = false;
        for (String lc : lifeClycles) {
            if (lc.equals("DiffEnvironmentLC")) {
                lccreated = true;
            }
        }

        Assert.assertTrue(lccreated);
    }

    @Test(groups = {"wso2.greg"}, description = "promote service", dependsOnMethods = {"testAddLC"})
    public void testPromote() throws RegistryException,
                                     CustomLifecyclesChecklistAdminServiceExceptionException,
                                     RemoteException {
        promoteService(NEW_VERSION, pathPrefix + servicePath, "testing");
    }

    @Test(groups = {"wso2.greg"}, description = "add WSDL", dependsOnMethods = {"testPromote"})
    public void testAttachWSDL() throws IOException, RegistryException {

        Service[] services = serviceManager.getAllServices();
        for (Service service : services) {
            String name = service.getQName().getLocalPart();
            if (name.equals("test_name1")) {
                String WSDL_URL = "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration/" +
                                  "clarity-tests/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/" +
                                  "GREG/wsdl/Axis2ImportedWsdl.wsdl";
                service.addAttribute("interface_wsdlURL", WSDL_URL);
                serviceManager.addService(service);
                String version = service.getAttribute("overview_version");
                Assert.assertEquals(NEW_VERSION, version);
            }
        }
    }


    @Test(groups = {"wso2.greg"}, description = "add lifecycle to service",
          dependsOnMethods = {"testCreateLc", "testAddService"})
    public void testAddLC()
            throws RegistryException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException {
        wsRegistryServiceClient.associateAspect(pathPrefix + servicePath, "DiffEnvironmentLC");

        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(pathPrefix + servicePath);

        Property[] properties = lifeCycle.getLifecycleProperties();

        boolean lcAdded = false;
        for (Property prop : properties) {
            if (prop.getKey().contains("DiffEnvironmentLC")) {
                lcAdded = true;
            }
        }
        Assert.assertTrue(lcAdded);
    }

    public String addService(String servicename, String namespace, String version)
            throws RegistryException, IOException {
        serviceManager = new ServiceManager(governance);
        Service service;
        service = serviceManager.newService(new QName(namespace, servicename));
        service.addAttribute("overview_version", version);
        serviceManager.addService(service);
        servicePath = service.getPath();
        return service.getId();
    }

    public String promoteService(String version, String currentPath, String promoteStatus)
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   RegistryException {
        ArrayOfString[] parameters = new ArrayOfString[2];
        String[] dependencyList = lifeCycleAdminServiceClient.getAllDependencies(currentPath);

        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{dependencyList[0], version});
        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{"preserveOriginal", "false"});

        String ACTION_PROMOTE = "Promote";
        String ASPECT_NAME = "DiffEnvironmentLC";
        lifeCycleAdminServiceClient.invokeAspectWithParams(currentPath, ASPECT_NAME, ACTION_PROMOTE, null, parameters);

        String newPath = "/_system/governance/branches/" + promoteStatus + "/services/" +
                         "test_namespace1" + "/" + version + "/" + "test_name1";

        Resource service = wsRegistryServiceClient.get(newPath);
        Assert.assertNotNull(service, "Service Not found on registry path " + newPath);
        Assert.assertEquals(service.getPath(), newPath, "Service not in branches/testing. " + newPath);


        return newPath;

    }

    @AfterClass
    public void clean()
            throws RegistryException, ResourceAdminServiceExceptionException, RemoteException,
                   LifeCycleManagementServiceExceptionException {

        lifeCycleManagementClient.deleteLifeCycle("DiffEnvironmentLC");
        delete("/_system/governance/branches/testing/services/test_namespace1/1.2.3/test_name1");
        delete("/_system/governance/trunk/wsdls/org/wso2/carbon/service/test_name1.wsdl");

        governance = null;
        resourceAdminServiceClient = null;
        registryProviderUtil = null;
        serviceManager = null;
        lifeCycleManagementClient = null;
        wsRegistryServiceClient = null;
        lifeCycleAdminServiceClient = null;
    }

    public void delete(String destPath)
            throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        if (wsRegistryServiceClient.resourceExists(destPath)) {
            resourceAdminServiceClient.deleteResource(destPath);
        }
    }
}
