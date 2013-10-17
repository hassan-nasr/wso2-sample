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
package org.wso2.carbon.bps.uploadscenarios;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.business.processes.BpelInstanceManagementClient;
import org.wso2.carbon.automation.api.clients.business.processes.BpelPackageManagementClient;
import org.wso2.carbon.automation.api.clients.business.processes.BpelProcessManagementClient;
import org.wso2.carbon.automation.core.RequestSender;
import org.wso2.carbon.bpel.stub.mgt.PackageManagementException;
import org.wso2.carbon.bpel.stub.mgt.types.LimitedInstanceInfoType;
import org.wso2.carbon.bpel.stub.mgt.types.PaginatedInstanceList;
import org.wso2.carbon.bps.BPSMasterTest;

import java.io.File;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

public class BpelInMemoryDeploymentTest extends BPSMasterTest {

    private static final Log log = LogFactory.getLog(BpelInMemoryDeploymentTest.class);

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
            throws Exception {
        uploadBpelForTest("CustomerInfo");
        requestSender.waitForProcessDeployment(serviceUrl + File.separator + "CustomerInfoService");
    }


    @Test(groups = {"wso2.bps", "wso2.bps.manage"}, description = "Tests uploading Bpel Service with In memory",priority=0)
    public void testInmemoryUolpad() throws Exception {
        bpelProcrss.getStatus(bpelProcrss.getProcessId("CustomerInfo"));
        RequestSender requestSender = new RequestSender();
        requestSender.waitForProcessDeployment(serviceUrl + File.separator + "CustomerInfoService");

        requestSender.assertRequest(serviceUrl + "/CustomerInfoService", "getCustomerSSN", "<p:CustomerInfo xmlns:p=\"http://wso2.org/bps/samples/loan_process/schema\">\n" +
                "      <!--Exactly 1 occurrence-->\n" +
                "      <Name xmlns=\"http://wso2.org/bps/samples/loan_process/schema\">Dharshana</Name>\n" +
                "      <!--Exactly 1 occurrence-->\n" +
                "      <Email xmlns=\"http://wso2.org/bps/samples/loan_process/schema\">dharshanaw@wso2.com</Email>\n" +
                "      <!--Exactly 1 occurrence-->\n" +
                "      <tns:CustomerID xmlns:tns=\"http://wso2.org/bps/samples/loan_process/schema\">?</tns:CustomerID>\n" +
                "      <!--Exactly 1 occurrence-->\n" +
                "      <CreditRating xmlns=\"http://wso2.org/bps/samples/loan_process/schema\">?</CreditRating>\n" +
                "   </p:CustomerInfo>\n", 1, "43235678SSN", true);

        PaginatedInstanceList instanceList = bpelInstance.filterPageInstances(bpelProcrss.getProcessId("CustomerInfo"));
        Assert.assertTrue( instanceList != null,"Service is not running inmemory");
    }

    @AfterClass(alwaysRun = true)
    public void removeArtifacts()
            throws PackageManagementException, InterruptedException, RemoteException,
                   LogoutAuthenticationExceptionException {
        bpelManager.undeployBPEL("CustomerInfo");
        adminServiceAuthentication.logOut();
    }
}

