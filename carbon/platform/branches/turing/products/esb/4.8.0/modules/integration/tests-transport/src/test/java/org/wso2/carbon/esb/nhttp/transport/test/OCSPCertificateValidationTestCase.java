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

package org.wso2.carbon.esb.nhttp.transport.test;

import org.apache.http.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.utils.httpserverutils.SimpleHttpClient;
import org.wso2.carbon.automation.core.utils.serverutils.ServerConfigurationManager;
import org.wso2.carbon.esb.ESBIntegrationTest;

import java.io.File;

public class OCSPCertificateValidationTestCase extends ESBIntegrationTest{

    private ServerConfigurationManager serverManager;
    private SimpleHttpClient httpClient;
    @BeforeClass(alwaysRun = true)
    public void uploadSynapseConfig() throws Exception {
        super.init();
        httpClient = new SimpleHttpClient();
        serverManager = new ServerConfigurationManager(esbServer.getBackEndUrl());
        serverManager.applyConfiguration(new File(getClass()
                .getResource("/artifacts/ESB/nhttp/transport/certificatevalidation/axis2.xml").getPath()));
        super.init();
        loadESBConfigurationFromClasspath("/artifacts/ESB/nhttp/transport/certificatevalidation/simple_proxy.xml");
    }

    @Test(groups = {"wso2.esb"},
            description = "Sends https request to the backend with OCSP certificate validation " +
                    "enabled ", enabled = false)
    public void sendHTTPSRequest() throws Exception {

        //HttpResponse response = HttpRequestUtil.sendGetRequest(esbServer.getServiceUrl().replace("/services","") + "/slive/echo/WSO2", null);
        String epr = esbServer.getServiceUrl().replace("/services","") + "/slive/echo/WSO2";
        HttpResponse response = httpClient.doGet(epr, null);
        System.out.println("This is the response");
        System.out.println(response.toString());
        Assert.assertTrue(response.toString().contains("WSO2"), "Asserting response for string 'WSO2'");
    }

    @AfterClass(alwaysRun = true)
    private void destroy() throws Exception {
        try {
            cleanup();
        } finally {
            Thread.sleep(3000);
            serverManager.restoreToLastConfiguration();
            serverManager = null;
        }
    }
}
