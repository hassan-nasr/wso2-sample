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
package org.wso2.carbon.automation.core.utils.jmsbrokerutils.controller;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.core.utils.jmsbrokerutils.controller.config.JMSBrokerConfiguration;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.net.URI;

public class JMSBrokerController {

    private static final Log log = LogFactory.getLog(JMSBrokerController.class);

    private String serverName;
    private JMSBrokerConfiguration configuration;
    private BrokerService broker;
    private static boolean isBrokerStarted = false;

    public JMSBrokerController(String serverName,
                               JMSBrokerConfiguration configuration) {
        this.serverName = serverName;
        this.configuration = configuration;
    }

    public String getServerName() {
        return serverName;
    }

    public String getProviderURL() {
        return configuration.getProviderURL();
    }

    /**
     *  starting ActiveMQ embedded broker
     * @return  true if the broker is registered successfully
     */
    public boolean start() {
        try {

            log.info("JMSServerController: Preparing to start JMS Broker: " + serverName);
            broker = new BrokerService();

            // configure the broker
            TransportConnector connector = new TransportConnector();
            connector.setUri(new URI(configuration.getProviderURL()));
            broker.setBrokerName("testBroker");
            log.info(broker.getBrokerDataDirectory());
            broker.setDataDirectory(System.getProperty(ServerConstants.CARBON_HOME) + File.separator + broker.getBrokerDataDirectory());
            broker.addConnector(connector);
            broker.start();
            isBrokerStarted = true;
            log.info("JMSServerController: Broker is Successfully started. continuing tests");
            return true;
        } catch (Exception e) {
            log.error(
                    "JMSServerController: There was an error starting JMS broker: "
                    + serverName, e);
            return false;
        }
    }

    /**
     *  Stopping ActiveMQ embedded broker
     * @return true if broker is successfully stopped
     */
    public boolean stop() {
        try {
            log.info(" ************* Stopping **************");
            if (broker.isStarted()) {
                broker.stop();
                isBrokerStarted = false;
            }
            return true;
        } catch (Exception e) {
            log.error("Error while shutting down the broker", e);
            return false;
        }
    }

    public static boolean isBrokerStarted() {
        return isBrokerStarted;
    }

}
