/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.esb.mediator.test.call;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.esb.ESBIntegrationTest;

import static org.testng.Assert.assertTrue;

public class CallMediatorInSwitchTestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        loadESBConfigurationFromClasspath("/artifacts/ESB/mediatorconfig/call/synapse6.xml");
    }

    @Test(groups = {"wso2.esb"})
    public void callMediatorInSwitchTestCase() throws AxisFault {

        OMElement response =
                axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURL("TestCallProxy"), null, "WSO2");
        boolean responseContainsWSO2 = response.getFirstElement().toString().contains("WSO2");
        assertTrue(responseContainsWSO2);

        response =
                axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURL("TestCallProxy"), null, "IBM");
        boolean responseContainsIBM = response.getFirstElement().toString().contains("IBM");
        assertTrue(responseContainsIBM);

        response =
                axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURL("TestCallProxy"), null, "MSFT");
        boolean responseContainsMSFT = response.getFirstElement().toString().contains("MSFT");
        assertTrue(responseContainsMSFT);

    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }

}
