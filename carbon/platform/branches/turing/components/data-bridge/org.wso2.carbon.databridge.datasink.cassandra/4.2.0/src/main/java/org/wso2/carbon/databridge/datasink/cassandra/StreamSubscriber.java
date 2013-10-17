package org.wso2.carbon.databridge.datasink.cassandra;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.datasink.cassandra.internal.util.DataSinkConstants;
import org.wso2.carbon.databridge.datasink.cassandra.internal.util.ServiceHolder;
import org.wso2.carbon.databridge.persistence.cassandra.datastore.ClusterFactory;
import org.wso2.carbon.event.builder.core.EventBuilderService;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.internal.config.InputStreamConfiguration;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.core.message.config.InputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.input.adaptor.manager.core.InputEventAdaptorManagerService;
import org.wso2.carbon.event.input.adaptor.manager.core.exception.InputEventAdaptorManagerConfigurationException;
import org.wso2.carbon.event.processor.api.receive.EventReceiver;
import org.wso2.carbon.event.processor.api.receive.Wso2EventListener;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class StreamSubscriber {

    private static Log log = LogFactory.getLog(StreamSubscriber.class);

    private static volatile StreamSubscriber instance = null;

    private Map<String, Wso2EventListener> streamListeners = new ConcurrentHashMap<String, Wso2EventListener>();

    private StreamSubscriber () { }

    public static StreamSubscriber getInstance() {
        if (instance == null) {
            synchronized (StreamSubscriber.class){
                if (instance == null) {
                    instance = new StreamSubscriber();
                }
            }
        }
        return instance;
    }

    public void subscribeStream(StreamDefinition streamDefinition, Credentials credentials) throws DataBridgeException{
        boolean isValidStream = this.validateStream(streamDefinition);
        if(isValidStream) {
            EventReceiver eventReceiverService = ServiceHolder.getEventReceiverService();
            if (eventReceiverService != null) {
                this.insertStreamToCassandra(streamDefinition, credentials);
                this.addInputEventAdaptor();
                this.addEventBuilder(streamDefinition);
                this.subscribeDataSink(streamDefinition, credentials);
            } else {
                String errorMsg = "Event Builder Service was not set.";
                log.error(errorMsg);
                throw new DataBridgeException(errorMsg);
            }
        }
    }

    private boolean validateStream(StreamDefinition streamDefinition) throws DataBridgeException{
        StreamValidatorUtil streamValidatorUtil = new StreamValidatorUtil();
        return streamValidatorUtil.isStreamValid(streamDefinition.getName(), streamDefinition.getVersion());
    }

    private void insertStreamToCassandra(StreamDefinition streamDefinition, Credentials credentials) {
        ServiceHolder.getCassandraConnector().definedStream(ClusterFactory.getCluster(credentials), streamDefinition);
    }

    private void addInputEventAdaptor() throws DataBridgeException{
        InputEventAdaptorManagerService inputEventAdaptorManagerService = ServiceHolder.getInputEventAdaptorManagerService();
        AxisConfiguration axisConfiguration = this.getAxisConfiguration();
        InputEventAdaptorConfiguration inputEventAdaptorConfiguration;
        try {
            inputEventAdaptorConfiguration = inputEventAdaptorManagerService.getActiveInputEventAdaptorConfiguration(DataSinkConstants.TRANSPORT_ADAPTOR_NAME, this.getTenantId());
            if(inputEventAdaptorConfiguration == null) { // If the Input Event Adapter is not yet created
                inputEventAdaptorConfiguration = this.createInputEventAdaptorConfiguration();
                inputEventAdaptorManagerService.deployInputEventAdaptorConfiguration(inputEventAdaptorConfiguration, axisConfiguration);
            }
        } catch (InputEventAdaptorManagerConfigurationException e) {
            String errorMsg = "Error while getting Input Event Adaptor Manager Configuration:\n" + e.getMessage();
            log.error(errorMsg, e);
            throw new DataBridgeException(errorMsg, e);
        }
    }

    private InputEventAdaptorConfiguration createInputEventAdaptorConfiguration() {
        InputEventAdaptorConfiguration inputEventAdaptorConfiguration = new InputEventAdaptorConfiguration();
        inputEventAdaptorConfiguration.setName(DataSinkConstants.TRANSPORT_ADAPTOR_NAME);
        inputEventAdaptorConfiguration.setType(DataSinkConstants.TRANSPORT_ADAPTOR_TYPE);
        return inputEventAdaptorConfiguration;
    }

    private void addEventBuilder(StreamDefinition streamDefinition) throws DataBridgeException{
        EventBuilderService eventBuilderService = ServiceHolder.getEventBuilderService();
        EventBuilderConfiguration eventBuilderConfiguration = eventBuilderService.getActiveEventBuilderConfiguration(this.createEventBuilderName(streamDefinition), this.getTenantId());
        if(eventBuilderConfiguration == null || eventBuilderConfiguration.getEventBuilderName() == null || eventBuilderConfiguration.getEventBuilderName().equals("")) {
            InputEventAdaptorManagerService inputEventAdaptorManagerService = ServiceHolder.getInputEventAdaptorManagerService();
            try {
                InputEventAdaptorConfiguration inputEventAdaptorConfiguration = inputEventAdaptorManagerService.getActiveInputEventAdaptorConfiguration(DataSinkConstants.TRANSPORT_ADAPTOR_NAME, this.getTenantId());
                InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration = new InputEventAdaptorMessageConfiguration();
                inputEventAdaptorMessageConfiguration.addInputMessageProperty(DataSinkConstants.INPUT_EVENT_ADAPTOR_STREAM, streamDefinition.getName());
                inputEventAdaptorMessageConfiguration.addInputMessageProperty(DataSinkConstants.INPUT_EVENT_ADAPTOR_VERSION, streamDefinition.getVersion());
                eventBuilderConfiguration = new EventBuilderConfiguration();
                StreamDefinitionMapperUtil streamDefinitionMapperUtil = new StreamDefinitionMapperUtil();
                InputStreamConfiguration inputStreamConfiguration = new InputStreamConfiguration();
                inputStreamConfiguration.setInputEventAdaptorMessageConfiguration(inputEventAdaptorMessageConfiguration);
                inputStreamConfiguration.setInputEventAdaptorName(inputEventAdaptorConfiguration.getName());
                inputStreamConfiguration.setInputEventAdaptorType(inputEventAdaptorConfiguration.getType());
                eventBuilderConfiguration.setInputStreamConfiguration(inputStreamConfiguration);
                eventBuilderConfiguration.setToStreamName(streamDefinition.getName());
                eventBuilderConfiguration.setToStreamVersion(streamDefinition.getVersion());
                eventBuilderConfiguration.setInputMapping(streamDefinitionMapperUtil.generateEventMapping(streamDefinition));
                eventBuilderConfiguration.setEventBuilderName(this.createEventBuilderName(streamDefinition));
                eventBuilderService.deployEventBuilderConfiguration(eventBuilderConfiguration, this.getAxisConfiguration());
            } catch (InputEventAdaptorManagerConfigurationException e) {
                String errorMsg = "Error while getting Input Event Adaptor Manager Configuration:\n" + e.getMessage();
                log.error(errorMsg, e);
                throw new DataBridgeException(errorMsg, e);
            } catch (EventBuilderConfigurationException e) {
                String errorMsg = "Error when subscribing to event builder:\n" + e.getMessage();
                log.error(errorMsg, e);
                throw new DataBridgeException(errorMsg, e);
            }
        }
    }
    
    private String createEventBuilderName(StreamDefinition streamDefinition) {
        return DataSinkConstants.EVENT_BUILDER_NAME_PREPEND + streamDefinition.getStreamId();
    }

    private void subscribeDataSink(StreamDefinition streamDefinition, Credentials credentials) {
        if(streamListeners.get(DataSinkConstants.CASSANDRA_STREAM_LISTENER_PREFIX + streamDefinition.getStreamId()) == null) {
            EventReceiver eventReceiver = ServiceHolder.getEventReceiverService();
            DatasinkWso2EventListener datasinkWso2EventListener = new DatasinkWso2EventListener(credentials);
            streamListeners.put(DataSinkConstants.CASSANDRA_STREAM_LISTENER_PREFIX + streamDefinition.getStreamId(), datasinkWso2EventListener);
            eventReceiver.subscribe(streamDefinition.getStreamId(), datasinkWso2EventListener, this.getTenantId());
        }
    }

    private int getTenantId() {
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    private AxisConfiguration getAxisConfiguration() {
        ConfigurationContextService configurationContextService = ServiceHolder.getConfigurationContextService();
        return configurationContextService.getServerConfigContext().getAxisConfiguration();
        //return super.getAxisConfig();
    }

}
