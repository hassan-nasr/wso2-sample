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
package org.wso2.carbon.bps.mgtstructuredactivities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.business.processes.BpelProcessManagementClient;
import org.wso2.carbon.automation.api.clients.business.processes.BpelPackageManagementClient;
import org.wso2.carbon.automation.api.clients.business.processes.BpelInstanceManagementClient;
import org.wso2.carbon.automation.core.RequestSender;
import org.wso2.carbon.bpel.stub.mgt.PackageManagementException;
import org.wso2.carbon.bpel.stub.mgt.types.LimitedInstanceInfoType;
import org.wso2.carbon.bpel.stub.mgt.types.PaginatedInstanceList;
import org.wso2.carbon.bps.BPSMasterTest;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class BpelStructActionFlowTest extends BPSMasterTest {

    private static final Log log = LogFactory.getLog(BpelStructActionFlowTest.class);

    LimitedInstanceInfoType instanceInfo = null;
    BpelPackageManagementClient bpelManager;
    BpelProcessManagementClient bpelProcrss;
    BpelInstanceManagementClient bpelInstance;

    RequestSender requestSender;

    @BeforeTest(alwaysRun = true)
    public void setEnvironment() throws LoginAuthenticationExceptionException, RemoteException {
        init();
        bpelManager = new BpelPackageManagementClient(backEndUrl, sessionCookie);
        bpelProcrss = new BpelProcessManagementClient(backEndUrl, sessionCookie);
        bpelInstance = new BpelInstanceManagementClient(backEndUrl, sessionCookie);
        requestSender = new RequestSender();
    }
    @BeforeClass(alwaysRun = true)
    public void deployArtifact()
            throws InterruptedException, RemoteException, MalformedURLException, PackageManagementException {
        uploadBpelForTest("TestFlowLinks");
    }

    @Test(groups = {"wso2.bps", "wso2.bps.structures"}, description = "Deploys Bpel with Flow", priority = 1)
    public void testFlowLinks() throws Exception, RemoteException {
        int instanceCount = 0;

        String processID = bpelProcrss.getProcessId("TestCase");
        PaginatedInstanceList instanceList = new PaginatedInstanceList();
        instanceList = bpelInstance.filterPageInstances(processID);
        if (instanceList.getInstance() != null) {
            instanceCount = instanceList.getInstance().length;
        }
        if (!processID.isEmpty()) {
            try {
                this.flowLinksRequest();
                Thread.sleep(5000);
                if (instanceCount >= bpelInstance.filterPageInstances(processID).getInstance().length) {
                    Assert.fail("Instance is not created for the request");
                }
            } catch (InterruptedException e) {
                log.error("Process management failed" + e);
                Assert.fail("Process management failed" + e);
            }
            bpelInstance.clearInstancesOfProcess(processID);
        }
    }

    @AfterClass(alwaysRun = true)
    public void removeArtifacts()
            throws PackageManagementException, InterruptedException, RemoteException,
                   LogoutAuthenticationExceptionException {
         bpelManager.undeployBPEL("TestFlowLinks");
        adminServiceAuthentication.logOut();
    }

    private void flowLinksRequest() throws Exception {
        String payload = "<ns1:ExecuteWorkflow xmlns:ns1=\"workflowns\"><value>foo</value></ns1:ExecuteWorkflow>";
        String operation = "ExecuteWorkflow";
        String serviceName = "/FlowLinkTest";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add("foo");
        requestSender.sendRequest(serviceUrl + serviceName, operation, payload,
                                  1, expectedOutput, true);
    }
}
