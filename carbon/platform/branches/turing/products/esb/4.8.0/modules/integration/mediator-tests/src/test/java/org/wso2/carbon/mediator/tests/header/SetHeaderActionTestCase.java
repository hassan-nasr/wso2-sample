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
package org.wso2.carbon.mediator.tests.header;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.testng.annotations.Test;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.SampleAxis2Server;
import org.wso2.esb.integration.axis2.StockQuoteClient;

import static org.testng.Assert.assertTrue;

public class SetHeaderActionTestCase extends ESBIntegrationTestCase {

    public void init() throws Exception {
        String filePath = "/mediators/header/action_header_set_synapse.xml";
        loadESBConfigurationFromClasspath(filePath);
        launchBackendAxis2Service(SampleAxis2Server.SIMPLE_STOCK_QUOTE_SERVICE);
    }

    @Test(groups = {"wso2.esb"}, description = "set action in headers of incoming messages")
    public void setHeaderAction() throws AxisFault {
        OMElement response;

        response = sendReceive(
                getProxyServiceURL("HeaderProxy", false),
                "http://localhost:9000/services/SimpleStockQuoteService",
                "IBM");
        assertTrue(response.toString().contains("IBM"));

    }

    @Override
    protected void cleanup() {
        super.cleanup();
    }

    private OMElement sendReceive(String trpUrl, String addressingUrl, String symbol)
                throws AxisFault {
            ServiceClient sender;
            Options options;
            OMElement response;

            sender = new ServiceClient();
            options = new Options();
            options.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
            options.setAction("urn:getPrice");

            if (trpUrl != null && !"null".equals(trpUrl)) {
                options.setProperty(Constants.Configuration.TRANSPORT_URL, trpUrl);
            }

            if (addressingUrl != null && !"null".equals(addressingUrl)) {
                sender.engageModule("addressing");
                options.setTo(new EndpointReference(addressingUrl));
            }

            sender.setOptions(options);

            response = sender.sendReceive(createStandardRequest(symbol));

            return response;
        }

    private OMElement createStandardRequest(String symbol) {
            OMFactory fac = OMAbstractFactory.getOMFactory();
            OMNamespace omNs = fac.createOMNamespace("http://services.samples", "ns");
            OMElement method = fac.createOMElement("getPrice", omNs);
            OMElement value1 = fac.createOMElement("request", omNs);
            OMElement value2 = fac.createOMElement("symbol", omNs);

            value2.addChild(fac.createOMText(value1, symbol));
            value1.addChild(value2);
            method.addChild(value1);

            return method;
        }

}
