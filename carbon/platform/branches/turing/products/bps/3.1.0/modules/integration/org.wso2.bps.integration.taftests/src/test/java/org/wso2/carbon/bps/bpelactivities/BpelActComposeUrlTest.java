package org.wso2.carbon.bps.bpelactivities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.business.processes.BpelInstanceManagementClient;
import org.wso2.carbon.automation.api.clients.business.processes.BpelPackageManagementClient;
import org.wso2.carbon.automation.api.clients.business.processes.BpelProcessManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.RequestSender;
import org.wso2.carbon.bpel.stub.mgt.PackageManagementException;
import org.wso2.carbon.bpel.stub.mgt.types.LimitedInstanceInfoType;
import org.wso2.carbon.bpel.stub.mgt.types.PaginatedInstanceList;
import org.wso2.carbon.bps.BPSMasterTest;


import java.io.File;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class BpelActComposeUrlTest extends BPSMasterTest {

    private static final Log log = LogFactory.getLog(BpelActComposeUrlTest.class);

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

    @BeforeClass(alwaysRun = true,groups = {"wso2.bps", "wso2.bps.bpelactivities"})
    public void deployArtifact()
            throws Exception {
        uploadBpelForTest("TestComposeUrl");
        requestSender.waitForProcessDeployment(serviceUrl + File.separator + "TestComposeUrlService");
    }

    @Test(groups = {"wso2.bps", "wso2.bps.bpelactivities"}, description = "Invike combine URL Bpel")
    public void testComposeUrl() throws Exception, RemoteException {
    int instanceCount = 0;

        String processID = bpelProcrss.getProcessId("TestComposeUrl");
        PaginatedInstanceList instanceList = new PaginatedInstanceList();
        instanceList = bpelInstance.filterPageInstances(processID);
        if (instanceList.getInstance() != null) {
            instanceCount = instanceList.getInstance().length;
        }
        if (!processID.isEmpty()) {
            try {
                this.forEachRequest();
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

    @AfterTest(alwaysRun = true,groups = {"wso2.bps", "wso2.bps.bpelactivities"})
    public void removeArtifacts()
            throws PackageManagementException, InterruptedException, RemoteException,
                   LogoutAuthenticationExceptionException {
        bpelManager.undeployBPEL("TestComposeUrl");
        adminServiceAuthentication.logOut();
    }

    public void forEachRequest() throws Exception {
         String payload = " <p:composeUrl xmlns:p=\"http://ode/bpel/unit-test.wsdl\">\n" +
                 "      <!--Exactly 1 occurrence-->\n" +
                 "      <template>www.google.com</template>\n" +
                 "      <!--Exactly 1 occurrence-->\n" +
                 "      <name>google</name>\n" +
                 "      <!--Exactly 1 occurrence-->\n" +
                 "      <value>ee</value>\n" +
                 "      <!--Exactly 1 occurrence-->\n" +
                 "      <pairs>\n" +
                 "         <!--Exactly 1 occurrence-->\n" +
                 "         <user>er</user>\n" +
                 "         <!--Exactly 1 occurrence-->\n" +
                 "         <tag>ff</tag>\n" +
                 "      </pairs>\n" +
                 "   </p:composeUrl>";
        String operation = "composeUrl";
        String serviceName = File.separator+ "TestComposeUrlService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add("www.google");

        requestSender.sendRequest(serviceUrl + serviceName, operation, payload,
                1, expectedOutput, true);
    }
}

