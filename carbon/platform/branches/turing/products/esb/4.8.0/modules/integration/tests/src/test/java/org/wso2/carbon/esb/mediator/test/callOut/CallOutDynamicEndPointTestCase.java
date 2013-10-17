package org.wso2.carbon.esb.mediator.test.callOut;


import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.core.annotations.SetEnvironment;
import org.wso2.carbon.esb.ESBIntegrationTest;

import static org.testng.Assert.assertTrue;

/*
*This  test needs ESB to be up with system parameters as follows
 * -Dwso2.stock.host=localhost -Dwso2.stock.port=9000
*/


public class CallOutDynamicEndPointTestCase  extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.platform_user})
    public void setEnvironment() throws Exception {

        super.init();
        loadESBConfigurationFromClasspath("/artifacts/ESB/mediatorconfig/callout/CalloutWithDynamicEndPointsProxy.xml");
    }

    @Test(groups = {"wso2.esb"})
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.platform_user})
    public void TestDynamicEndPoints() throws AxisFault {

        OMElement response = axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURL("CalloutProxy"), "", "IBM");    // send the simplestockquote request. service url is set at the synapse
        boolean ResponseContainsIBM = response.getFirstElement().toString().contains("IBM");      //checks whether the  response contains IBM
         assertTrue(ResponseContainsIBM);


    }


    @AfterClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.platform_user})
    public void destroy() throws Exception {
        super.cleanup();
    }

}
