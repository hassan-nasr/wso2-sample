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
package org.wso2.carbon.automation.core.utils.jmsbrokerutils.client;

import org.wso2.carbon.automation.core.utils.jmsbrokerutils.controller.config.JMSBrokerConfiguration;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class JMSQueueMessageProducer {
    private Connection connection = null;
    private Session session = null;
    private MessageProducer producer = null;
    private QueueConnectionFactory connectionFactory = null;

    public JMSQueueMessageProducer(JMSBrokerConfiguration brokerConfiguration)
            throws NamingException {

        // Create a ConnectionFactory
        Properties props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, brokerConfiguration.getInitialNamingFactory());

        if (brokerConfiguration.getProviderURL().startsWith("amqp://")) {
            //setting property for Qpid running on WSO2 MB
            props.put("connectionfactory.QueueConnectionFactory", brokerConfiguration.getProviderURL());
        } else {
            //setting property for ActiveMQ
            props.setProperty(Context.PROVIDER_URL, brokerConfiguration.getProviderURL());
        }

        Context ctx = new InitialContext(props);
        connectionFactory = (QueueConnectionFactory) ctx.lookup("QueueConnectionFactory");
    }

    /**
     * This will establish  the connection with the given Queue. This must be called before calling pushMessage() to send messages
     *
     * @param queueName name of the Queue
     * @throws JMSException
     * @throws NamingException
     */
    public void connect(String queueName) throws JMSException, NamingException {

        // Create a Connection
        connection = connectionFactory.createConnection();
        connection.start();

        // Create a Session
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Create a MessageConsumer from the Session to the Queue

        Destination destination = session.createQueue(queueName);
        // Create a MessageProducer from the Session to the Topic or Queue
        producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

    }

    /**
     * This will disconnect  the connection with the given Queue. This must be called after sending the messages to release
     * the connection
     */
    public void disconnect() {
        if (producer != null) {
            try {
                producer.close();
            } catch (JMSException e) {
                //ignore
            }
        }
        if (session != null) {
            try {
                session.close();
            } catch (JMSException e) {
                //ignore
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException e) {
                //ignore
            }
        }
    }

    /**
     * This will send the message to the destination Queue
     *
     * @param messageContent returns the message contents
     * @throws Exception
     */
    public void pushMessage(String messageContent) throws Exception {
        if (producer == null) {
            throw new Exception("No Connection with Queue. Please connect");
        }
        // Create a messages;
        TextMessage message = session.createTextMessage(messageContent);
        producer.send(message);
    }


}
