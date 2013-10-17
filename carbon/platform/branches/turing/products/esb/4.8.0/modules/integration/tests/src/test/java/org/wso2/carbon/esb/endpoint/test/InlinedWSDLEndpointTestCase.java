/**
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.endpoint.test;

import org.apache.axiom.om.OMElement;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.endpoint.EndPointAdminClient;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.carbon.automation.utils.esb.ESBTestCaseUtils;
import org.wso2.carbon.endpoint.stub.types.EndpointAdminEndpointAdminException;
import org.wso2.carbon.esb.endpoint.test.util.EndpointTestUtils;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class InlinedWSDLEndpointTestCase {

    private final String ENDPOINT_NAME = "wsdlEpTest";
    private EndPointAdminClient endPointAdminClient;

    @Test(groups = {"wso2.esb"})
    public void testInlineWSDLEndpoint() throws Exception {
        EnvironmentBuilder builder = new EnvironmentBuilder().esb(1);
        EnvironmentVariables esbServer = builder.build().getEsb();
        endPointAdminClient = new EndPointAdminClient(esbServer.getBackEndUrl(), esbServer.getSessionCookie());

        cleanupEndpoints();
        endpointAdditionScenario();
        endpointStatisticsScenario();
        endpointDeletionScenario();
        endPointAdminClient = null;
    }

    private void cleanupEndpoints()
            throws RemoteException, EndpointAdminEndpointAdminException {
        EndpointTestUtils.cleanupDefaultEndpoint(ENDPOINT_NAME, endPointAdminClient);
    }

    private void endpointAdditionScenario()
            throws Exception {
        int beforeCount = endPointAdminClient.getEndpointCount();

        OMElement wsdlTestEp = new ESBTestCaseUtils().loadClasspathResource("/artifacts/ESB/endpoint/wsdlTestEp.xml");
        endPointAdminClient.addEndPoint(wsdlTestEp);
        Thread.sleep(2000);

        int afterCount = endPointAdminClient.getEndpointCount();
        assertEquals(1, afterCount - beforeCount);

        String[] endpoints = endPointAdminClient.getEndpointNames();
        if (endpoints != null && endpoints.length > 0 && endpoints[0] != null) {
            List endpointList = Arrays.asList(endpoints);
            assertTrue(endpointList.contains(ENDPOINT_NAME));
        } else {
            fail("Endpoint has not been added to the system properly");
        }
    }

    private void endpointStatisticsScenario()
            throws RemoteException, EndpointAdminEndpointAdminException {
        EndpointTestUtils.enableEndpointStatistics(ENDPOINT_NAME, endPointAdminClient);
    }

    private void endpointDeletionScenario()
            throws RemoteException, EndpointAdminEndpointAdminException {
        int beforeCount = endPointAdminClient.getEndpointCount();
        endPointAdminClient.deleteEndpoint(ENDPOINT_NAME);
        EndpointTestUtils.assertDefaultEndpointDeletion(beforeCount, endPointAdminClient);
    }
}
