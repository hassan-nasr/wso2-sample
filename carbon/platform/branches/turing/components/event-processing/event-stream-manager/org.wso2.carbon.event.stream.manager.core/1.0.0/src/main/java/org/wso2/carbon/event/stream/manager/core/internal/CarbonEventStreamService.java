package org.wso2.carbon.event.stream.manager.core.internal;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.event.processor.api.passthrough.PassthroughReceiverConfigurator;
import org.wso2.carbon.event.processor.api.passthrough.exception.PassthroughConfigurationException;
import org.wso2.carbon.event.processor.api.receive.exception.EventReceiverException;
import org.wso2.carbon.event.stream.manager.core.EventStreamListener;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.event.stream.manager.core.internal.ds.EventStreamServiceValueHolder;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class CarbonEventStreamService implements EventStreamService {

    private static final Log log = LogFactory.getLog(CarbonEventStreamService.class);
    private static final String STREAM_DEFINITION_STORE = "/StreamDefinitions";
    private List<EventStreamListener> eventStreamListenerList = new ArrayList<EventStreamListener>();


    @Override
    public void addStreamDefinitionToStore(Credentials credentials,
                                            StreamDefinition streamDefinition, AxisConfiguration axisConfiguration)
            throws StreamDefinitionStoreException {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();

            privilegedCarbonContext.setTenantId(EventStreamServiceValueHolder.getRealmService().getTenantManager().getTenantId(credentials.getDomainName()));
            privilegedCarbonContext.setTenantDomain(credentials.getDomainName());

            PassthroughReceiverConfigurator passthroughReceiverConfigurator = EventStreamServiceValueHolder.getPassthroughReceiverConfigurator();

            try {
                UserRegistry registry = EventStreamServiceValueHolder.getRegistryService().getGovernanceUserRegistry(credentials.getUsername(), credentials.getPassword());
                Resource resource = registry.newResource();
                resource.setContent(EventDefinitionConverterUtils.convertToJson(streamDefinition));
                resource.setMediaType("application/json");
                registry.put(STREAM_DEFINITION_STORE + RegistryConstants.PATH_SEPARATOR + streamDefinition.getName() + RegistryConstants.PATH_SEPARATOR + streamDefinition.getVersion(), resource);
                passthroughReceiverConfigurator.deployDefaultEventBuilder(streamDefinition.getStreamId(), axisConfiguration);

                for (EventStreamListener eventStreamListener : eventStreamListenerList) {
                    eventStreamListener.addedEventStream(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), streamDefinition.getName(), streamDefinition.getVersion());
                }

            } catch (RegistryException e) {
                log.error("Error in saving Stream Definition " + streamDefinition);
            }

        } catch (UserStoreException e) {
            throw new StreamDefinitionStoreException("Error in saving definition " + streamDefinition + " to registry, " + e.getMessage(), e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }


    }

    @Override
    public void addEventStreamDefinitionToStore(StreamDefinition streamDefinition, AxisConfiguration axisConfiguration)
            throws EventStreamConfigurationException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        StreamDefinition existingDefinition;
        existingDefinition = getStreamDefinitionFromStore(streamDefinition.getName(), streamDefinition.getVersion(), tenantId);

        if (existingDefinition == null) {
            saveStreamDefinitionToStore(streamDefinition);

            PassthroughReceiverConfigurator passthroughReceiverConfigurator = EventStreamServiceValueHolder.getPassthroughReceiverConfigurator();
            if(passthroughReceiverConfigurator != null){
                try {
                    passthroughReceiverConfigurator.deployDefaultEventBuilder(streamDefinition.getStreamId(), axisConfiguration);
                } catch (EventReceiverException e) {
                    throw new EventStreamConfigurationException(e);
                }
            }

            for (EventStreamListener eventStreamListener : eventStreamListenerList) {
                eventStreamListener.addedEventStream(tenantId, streamDefinition.getName(), streamDefinition.getVersion());
            }
            return;
        }
        if (!existingDefinition.equals(streamDefinition)) {

            throw new EventStreamConfigurationException("Another Stream with same name and version" +
                                                        " exist :" + EventDefinitionConverterUtils
                    .convertToJson(existingDefinition));
        }
    }

    @Override
    public void removeEventStreamDefinition(String streamName, String streamVersion, int tenantId)
            throws EventStreamConfigurationException {

        if (removeStreamDefinitionFromStore(streamName, streamVersion, tenantId)) {
            log.info("Stream definition is deleted from the store successfully");
        }

        for (EventStreamListener eventStreamListener : eventStreamListenerList) {
            eventStreamListener.removedEventStream(tenantId, streamName, streamVersion);
        }

    }

    @Override
    public void registerEventStreamListener(EventStreamListener eventStreamListener) {
        if (eventStreamListener != null) {
            eventStreamListenerList.add(eventStreamListener);
        }
    }

    public void saveStreamDefinitionToStore(StreamDefinition streamDefinition)
            throws EventStreamConfigurationException {
        try {
            UserRegistry registry = EventStreamServiceValueHolder.getRegistryService().getGovernanceSystemRegistry();
            Resource resource = registry.newResource();

            resource.setContent(EventDefinitionConverterUtils.convertToJson(streamDefinition));
            resource.setMediaType("application/json");
            registry.put(STREAM_DEFINITION_STORE + RegistryConstants.PATH_SEPARATOR + streamDefinition.getName() + RegistryConstants.PATH_SEPARATOR + streamDefinition.getVersion(), resource);
        } catch (RegistryException e) {
            log.error("Error in saving Stream Definition " + streamDefinition);
            throw new EventStreamConfigurationException("Error in saving Stream Definition " + streamDefinition, e);
        }
    }

    @Override
    public StreamDefinition getStreamDefinitionFromStore(String name, String version, int tenantId)
            throws EventStreamConfigurationException {

        try {
            UserRegistry registry = EventStreamServiceValueHolder.getRegistryService().getGovernanceSystemRegistry(tenantId);
            if (registry.resourceExists(STREAM_DEFINITION_STORE + RegistryConstants.PATH_SEPARATOR + name + RegistryConstants.PATH_SEPARATOR + version)) {
                Resource resource = registry.get(STREAM_DEFINITION_STORE + RegistryConstants.PATH_SEPARATOR + name + RegistryConstants.PATH_SEPARATOR + version);
                Object content = resource.getContent();
                if (content != null) {
                    return EventDefinitionConverterUtils.convertFromJson(RegistryUtils.decodeBytes((byte[]) resource.getContent()));
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Error in getting Stream Definition " + name + ":" + version, e);
            throw new EventStreamConfigurationException("Error in getting Stream Definition " + name + ":" + version, e);
        }
    }

    @Override
    public boolean removeStreamDefinitionFromStore(String name, String version, int tenantId)
            throws EventStreamConfigurationException {
        try {
            UserRegistry registry = EventStreamServiceValueHolder.getRegistryService().getGovernanceSystemRegistry(tenantId);
            registry.delete(STREAM_DEFINITION_STORE + RegistryConstants.PATH_SEPARATOR + name + RegistryConstants.PATH_SEPARATOR + version);
            return !registry.resourceExists(STREAM_DEFINITION_STORE + RegistryConstants.PATH_SEPARATOR + name + RegistryConstants.PATH_SEPARATOR + version);
        } catch (RegistryException e) {
            log.error("Error in deleting Stream Definition " + name + ":" + version);
            throw new EventStreamConfigurationException("Error in deleting Stream Definition " + name + ":" + version, e);
        }
    }

    @Override
    public Collection<StreamDefinition> getAllStreamDefinitionsFromStore(int tenantId)
            throws EventStreamConfigurationException {
        ConcurrentHashMap<String, StreamDefinition> map = new ConcurrentHashMap<String, StreamDefinition>();
        try {
            UserRegistry registry = EventStreamServiceValueHolder.getRegistryService().getGovernanceSystemRegistry(tenantId);

            if (!registry.resourceExists(STREAM_DEFINITION_STORE)) {
                registry.put(STREAM_DEFINITION_STORE, registry.newCollection());
            } else {
                org.wso2.carbon.registry.core.Collection collection = (org.wso2.carbon.registry.core.Collection) registry.get(STREAM_DEFINITION_STORE);
                for (String streamNameCollection : collection.getChildren()) {

                    org.wso2.carbon.registry.core.Collection innerCollection = (org.wso2.carbon.registry.core.Collection) registry.get(streamNameCollection);
                    for (String streamVersionCollection : innerCollection.getChildren()) {

                        Resource resource = registry.get(streamVersionCollection);
                        try {
                            StreamDefinition streamDefinition = EventDefinitionConverterUtils.convertFromJson(RegistryUtils.decodeBytes((byte[]) resource.getContent()));
                            map.put(streamDefinition.getStreamId(), streamDefinition);
                        } catch (Throwable e) {
                            log.error("Error in retrieving streamDefinition from the resource at " + resource.getPath(), e);
                            throw new EventStreamConfigurationException("Error in retrieving streamDefinition from the resource at " + resource.getPath(), e);
                        }
                    }
                }
            }

        } catch (RegistryException e) {
            log.error("Error in retrieving streamDefinitions from the registry", e);
            throw new EventStreamConfigurationException("Error in retrieving streamDefinitions from the registry", e);
        }

        return map.values();
    }

    @Override
    public StreamDefinition getStreamDefinitionFromStore(String streamId, int tenantId)
            throws EventStreamConfigurationException {

        return getStreamDefinitionFromStore(DataBridgeCommonsUtils.getStreamNameFromStreamId(streamId),
                                            DataBridgeCommonsUtils.getStreamVersionFromStreamId(streamId), tenantId);
    }

    @Override
    public List<String> getStreamIds(int tenantId) throws EventStreamConfigurationException {
        Collection<StreamDefinition> streamDefinitions = getAllStreamDefinitionsFromStore(tenantId);
        List<String> streamDefinitionsIds = new ArrayList<String>(streamDefinitions.size());
        for(StreamDefinition streamDefinition: streamDefinitions) {
            streamDefinitionsIds.add(streamDefinition.getStreamId());
        }

        return streamDefinitionsIds;
    }

    @Override
    public void registerPassthroughReceiverConfigurator(PassthroughReceiverConfigurator passthroughReceiverConfigurator) throws
                                                                                                                 PassthroughConfigurationException{
        if(passthroughReceiverConfigurator != null){
            EventStreamServiceValueHolder.registerEventBuilderService(passthroughReceiverConfigurator);
            if(EventStreamServiceValueHolder.getPendingStreamIdList()!= null && (! EventStreamServiceValueHolder.getPendingStreamIdList().isEmpty())){
                for (StreamDefinition streamDefinition : EventStreamServiceValueHolder.getPendingStreamIdList()) {
                    try {
                        saveStreamDefinitionToStore(streamDefinition);
                    } catch (EventStreamConfigurationException e) {
                        throw new PassthroughConfigurationException("Error while loading streams from config ",e);
                    }
                }
                EventStreamServiceValueHolder.setPendingStreamIdList(null);
            }
        }
    }


//    protected void saveStreamDefinitionToStore(Credentials credentials,
//                                               StreamDefinition streamDefinition)
//            throws StreamDefinitionStoreException {
//        try {
//            PrivilegedCarbonContext.startTenantFlow();
//            PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
//
//            privilegedCarbonContext.setTenantId());
//            privilegedCarbonContext.setTenantDomain(credentials.getDomainName());
//
//
//            try {
//                UserRegistry registry = EventStreamServiceValueHolder.getRegistryService().getGovernanceUserRegistry(credentials.getUsername(), credentials.getPassword());
//                Resource resource = registry.newResource();
//                resource.setContent(EventDefinitionConverterUtils.convertToJson(streamDefinition));
//                resource.setMediaType("application/json");
//                registry.put(STREAM_DEFINITION_STORE + RegistryConstants.PATH_SEPARATOR + streamDefinition.getName() + RegistryConstants.PATH_SEPARATOR + streamDefinition.getVersion(), resource);
//            } catch (RegistryException e) {
//                log.error("Error in saving Stream Definition " + streamDefinition);
//            }
//
//        } catch (UserStoreException e) {
//            throw new StreamDefinitionStoreException("Error in saving definition " + streamDefinition + " to registry, " + e.getMessage(), e);
//        } finally {
//            PrivilegedCarbonContext.endTenantFlow();
//        }
//
//    }

//    public void saveConfigurationToRegistry(BrokerConfiguration brokerConfiguration, int tenantId)
//            throws BMConfigurationException {
//        String brokerName = brokerConfiguration.getName();
//        String pathToBroker = BROKER_BASE + "/" + brokerName;
//        Resource brokerResource = null;
//        Registry registry = null;
//        try {
//            registry = RegistryHolder.getInstance().getRegistry(tenantId);
//        } catch (RegistryException e) {
//            log.error("Error in getting registry for the tenant :" + tenantId, e);
//            throw new BMConfigurationException("Error in getting registry for the tenant :" + tenantId, e);
//        }
//        try {
//            brokerResource = registry.newResource();
//            Map<String, String> propertyMap = brokerConfiguration.getProperties();
//            brokerResource.addProperty("name", brokerConfiguration.getName());
//            brokerResource.addProperty("type", brokerConfiguration.getType());
//            for (Map.Entry<String, String> entry : propertyMap.entrySet()) {
//                brokerResource.addProperty(entry.getKey(), entry.getValue());
//            }
//        } catch (RegistryException e) {
//            if (log.isErrorEnabled()) {
//                log.error("Failed to create new resource in registry", e);
//            }
//            throw new BMConfigurationException("Failed to create new resource in registry", e);
//        }
//        try {
//            registry.put(pathToBroker, brokerResource);
//        } catch (RegistryException e) {
//            if (log.isErrorEnabled()) {
//                log.error("Failed to saveConfigurationToRegistry new resource in registry", e);
//            }
//            throw new BMConfigurationException("Failed to saveConfigurationToRegistry new resource in registry", e);
//        }
//    }


//
//    private Map<Integer, Map<String, EventFormatter>> tenantSpecificEventFormatterConfigurationMap;
//    private Map<Integer, List<EventFormatterConfigurationFile>> eventFormatterConfigurationFileMap;
//
//
//    public CarbonEventStreamService() {
//        tenantSpecificEventFormatterConfigurationMap = new ConcurrentHashMap<Integer, Map<String, EventFormatter>>();
//        eventFormatterConfigurationFileMap = new ConcurrentHashMap<Integer, List<EventFormatterConfigurationFile>>();
//    }
//
//    @Override
//    public void deployEventFormatterConfiguration(
//            EventFormatterConfiguration eventFormatterConfiguration,
//            AxisConfiguration axisConfiguration)
//            throws EventStreamConfigurationException {
//
//        String eventFormatterName = eventFormatterConfiguration.getEventFormatterName();
//
//        OMElement omElement = FormatterConfigurationBuilder.eventFormatterConfigurationToOM(eventFormatterConfiguration);
//        EventFormatterConfigurationHelper.validateEventFormatterConfiguration(omElement);
//        if (EventFormatterConfigurationHelper.getOutputMappingType(omElement) != null) {
//
//            File directory = new File(axisConfiguration.getRepository().getPath());
//            if (!directory.exists()) {
//                if (directory.mkdir()) {
//                    throw new EventStreamConfigurationException("Cannot create directory to add tenant specific Event Formatter : " + eventFormatterName);
//                }
//            }
//            directory = new File(directory.getAbsolutePath() + File.separator + EventFormatterConstants.TM_ELE_DIRECTORY);
//            if (!directory.exists()) {
//                if (!directory.mkdir()) {
//                    throw new EventStreamConfigurationException("Cannot create directory " + EventFormatterConstants.TM_ELE_DIRECTORY + " to add tenant specific event adaptor :" + eventFormatterName);
//                }
//            }
//            validateToRemoveInactiveEventFormatterConfiguration(eventFormatterName, axisConfiguration);
//            EventFormatterConfigurationFilesystemInvoker.save(omElement, eventFormatterName + ".xml", axisConfiguration);
//
//        } else {
//            throw new EventStreamConfigurationException("Mapping type of the Event Formatter " + eventFormatterName + " cannot be null");
//        }
//
//    }
//
//    @Override
//    public void undeployActiveEventFormatterConfiguration(String eventFormatterName,
//                                                          AxisConfiguration axisConfiguration)
//            throws EventStreamConfigurationException {
//
//        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
//        String fileName = getFileName(tenantId, eventFormatterName);
//        if (fileName != null) {
//            EventFormatterConfigurationFilesystemInvoker.delete(fileName, axisConfiguration);
//        } else {
//            throw new EventStreamConfigurationException("Couldn't undeploy the Event Formatter configuration : " + eventFormatterName);
//        }
//
//    }
//
//    @Override
//    public void undeployInactiveEventFormatterConfiguration(String filename,
//                                                            AxisConfiguration axisConfiguration)
//            throws EventStreamConfigurationException {
//
//        EventFormatterConfigurationFilesystemInvoker.delete(filename, axisConfiguration);
//    }
//
//    @Override
//    public void editInactiveEventFormatterConfiguration(
//            String eventFormatterConfiguration,
//            String filename,
//            AxisConfiguration axisConfiguration)
//            throws EventStreamConfigurationException {
//
//        editEventFormatterConfiguration(filename, axisConfiguration, eventFormatterConfiguration, null);
//    }
//
//    @Override
//    public void editActiveEventFormatterConfiguration(String eventFormatterConfiguration,
//                                                      String eventFormatterName,
//                                                      AxisConfiguration axisConfiguration)
//            throws EventStreamConfigurationException {
//        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
//        String fileName = getFileName(tenantId, eventFormatterName);
//        if (fileName == null) {
//            fileName = eventFormatterName + EventFormatterConstants.EF_CONFIG_FILE_EXTENSION_WITH_DOT;
//        }
//        editEventFormatterConfiguration(fileName, axisConfiguration, eventFormatterConfiguration, eventFormatterName);
//
//    }
//
//    @Override
//    public EventFormatterConfiguration getActiveEventFormatterConfiguration(
//            String eventFormatterName,
//            int tenantId)
//            throws EventStreamConfigurationException {
//
//        EventFormatterConfiguration eventFormatterConfiguration = null;
//
//        Map<String, EventFormatter> tenantSpecificEventFormatterMap = tenantSpecificEventFormatterConfigurationMap.get(tenantId);
//        if (tenantSpecificEventFormatterMap != null && tenantSpecificEventFormatterMap.size() > 0) {
//            eventFormatterConfiguration = tenantSpecificEventFormatterMap.get(eventFormatterName).getEventFormatterConfiguration();
//        }
//        return eventFormatterConfiguration;
//    }
//
//    @Override
//    public List<EventFormatterConfiguration> getAllActiveEventFormatterConfiguration(
//            AxisConfiguration axisConfiguration) throws EventStreamConfigurationException {
//        List<EventFormatterConfiguration> eventFormatterConfigurations = new ArrayList<EventFormatterConfiguration>();
//        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
//        if (tenantSpecificEventFormatterConfigurationMap.get(tenantId) != null) {
//            for (EventFormatter eventFormatter : tenantSpecificEventFormatterConfigurationMap.get(
//                    tenantId).values()) {
//                eventFormatterConfigurations.add(eventFormatter.getEventFormatterConfiguration());
//            }
//        }
//        return eventFormatterConfigurations;
//    }
//
//    @Override
//    public List<EventFormatterConfigurationFile> getAllInactiveEventFormatterConfiguration(
//            AxisConfiguration axisConfiguration) {
//
//        List<EventFormatterConfigurationFile> undeployedEventFormatterFileList = new ArrayList<EventFormatterConfigurationFile>();
//        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
//        if (eventFormatterConfigurationFileMap.get(tenantId) != null) {
//            for (EventFormatterConfigurationFile eventFormatterConfigurationFile : eventFormatterConfigurationFileMap.get(tenantId)) {
//                if (!eventFormatterConfigurationFile.getStatus().equals(EventFormatterConfigurationFile.Status.DEPLOYED)) {
//                    undeployedEventFormatterFileList.add(eventFormatterConfigurationFile);
//                }
//            }
//        }
//        return undeployedEventFormatterFileList;
//    }
//
//
//    @Override
//    public String getInactiveEventFormatterConfigurationContent(String filename,
//                                                                AxisConfiguration axisConfiguration)
//            throws EventStreamConfigurationException {
//        return EventFormatterConfigurationFilesystemInvoker.readEventFormatterConfigurationFile(filename, axisConfiguration);
//    }
//
//    @Override
//    public String getActiveEventFormatterConfigurationContent(String eventFormatterName,
//                                                              AxisConfiguration axisConfiguration)
//            throws EventStreamConfigurationException {
//        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
//        String fileName = getFileName(tenantId, eventFormatterName);
//        return EventFormatterConfigurationFilesystemInvoker.readEventFormatterConfigurationFile(fileName, axisConfiguration);
//    }
//
//
//    public List<String> getAllEventStreams(AxisConfiguration axisConfiguration)
//            throws EventStreamConfigurationException {
//
//        List<String> streamList = new ArrayList<String>();
//
//        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
//        List<EventProducer> eventProducerList = EventStreamServiceValueHolder.getEventProducerList();
//        if (eventProducerList != null) {
//            for (EventProducer eventProducer : eventProducerList) {
//                List<String> streamNameList = eventProducer.getAllStreamIds(tenantId);
//                if (streamNameList != null) {
//                    for (String streamName : streamNameList) {
//                        streamList.add(streamName);
//                    }
//                }
//            }
//        }
//        return streamList;
//    }
//
//    public StreamDefinition getStreamDefinition(String streamNameWithVersion,
//                                                AxisConfiguration axisConfiguration)
//            throws EventStreamConfigurationException {
//        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
//        List<EventProducer> eventProducerList = EventStreamServiceValueHolder.getEventProducerList();
//
//        for (EventProducer currentEventProducer : eventProducerList) {
//            List<String> streamList = currentEventProducer.getAllStreamIds(tenantId);
//            if (streamList != null) {
//                for (String stream : streamList) {
//                    if (stream.equals(streamNameWithVersion)) {
//                        return currentEventProducer.getStreamDefinition(streamNameWithVersion, tenantId);
//                    }
//                }
//            }
//        }
//
//        return null;
//    }
//
//
//    public String getRegistryResourceContent(String resourcePath, int tenantId)
//            throws EventStreamConfigurationException {
//        RegistryService registryService = EventStreamServiceValueHolder.getRegistryService();
//
//        String registryData;
//        Resource registryResource = null;
//        try {
//            String pathPrefix = resourcePath.substring(0, resourcePath.indexOf(':') + 2);
//            if (pathPrefix.equalsIgnoreCase(EventFormatterConstants.REGISTRY_CONF_PREFIX)) {
//                resourcePath = resourcePath.replace(pathPrefix, "");
//                registryResource = registryService.getConfigSystemRegistry().get(resourcePath);
//            } else if (pathPrefix.equalsIgnoreCase(EventFormatterConstants.REGISTRY_GOVERNANCE_PREFIX)) {
//                resourcePath = resourcePath.replace(pathPrefix, "");
//                registryResource = registryService.getGovernanceSystemRegistry().get(resourcePath);
//            }
//
//            if (registryResource != null) {
//                Object registryContent = registryResource.getContent();
//                if (registryContent != null) {
//                    registryData = (RegistryUtils.decodeBytes((byte[]) registryContent));
//                }
//                else{
//                    throw new EventStreamConfigurationException("There is no registry resource content available at "+resourcePath);
//                }
//
//            } else {
//                throw new EventStreamConfigurationException("Resource couldn't found from registry at " + resourcePath);
//            }
//
//        } catch (RegistryException e) {
//            throw new EventStreamConfigurationException("Error while retrieving the resource from registry at " + resourcePath, e);
//        }catch (ClassCastException e){
//            throw new EventStreamConfigurationException("Invalid mapping content found in " + resourcePath, e);
//        }
//        return registryData;
//    }
//
//    @Override
//    public void setStatisticsEnabled(String eventFormatterName, AxisConfiguration axisConfiguration,
//                                     boolean flag)
//            throws EventStreamConfigurationException {
//
//        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
//        EventFormatterConfiguration eventFormatterConfiguration = getActiveEventFormatterConfiguration(eventFormatterName, tenantId);
//        eventFormatterConfiguration.setEnableStatistics(flag);
//        editTracingStatistics(eventFormatterConfiguration, eventFormatterName, tenantId, axisConfiguration);
//    }
//
//    @Override
//    public void setTraceEnabled(String eventFormatterName, AxisConfiguration axisConfiguration,
//                                boolean flag)
//            throws EventStreamConfigurationException {
//        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
//        EventFormatterConfiguration eventFormatterConfiguration = getActiveEventFormatterConfiguration(eventFormatterName, tenantId);
//        eventFormatterConfiguration.setEnableTracing(flag);
//        editTracingStatistics(eventFormatterConfiguration, eventFormatterName, tenantId, axisConfiguration);
//    }
//
//    //Non-Interface public methods
//
//    public boolean checkEventFormatterValidity(int tenantId, String eventFormatterName) {
//
//        if (eventFormatterConfigurationFileMap.size() > 0) {
//            List<EventFormatterConfigurationFile> eventFormatterConfigurationFileList = eventFormatterConfigurationFileMap.get(tenantId);
//            if (eventFormatterConfigurationFileList != null) {
//                for (EventFormatterConfigurationFile eventFormatterConfigurationFile : eventFormatterConfigurationFileList) {
//                    if ((eventFormatterConfigurationFile.getEventFormatterName().equals(eventFormatterName)) && (eventFormatterConfigurationFile.getStatus().equals(EventFormatterConfigurationFile.Status.DEPLOYED))) {
//                        log.error("Event Formatter " + eventFormatterName + " is already registered with this tenant");
//                        return false;
//                    }
//                }
//            }
//        }
//        return true;
//    }
//
//    public void addEventFormatterConfigurationFile(int tenantId,
//                                                   EventFormatterConfigurationFile eventFormatterConfigurationFile) {
//
//        List<EventFormatterConfigurationFile> eventFormatterConfigurationFileList = eventFormatterConfigurationFileMap.get(tenantId);
//
//        if (eventFormatterConfigurationFileList == null) {
//            eventFormatterConfigurationFileList = new ArrayList<EventFormatterConfigurationFile>();
//        } else {
//            for (EventFormatterConfigurationFile anEventFormatterConfigurationFileList : eventFormatterConfigurationFileList) {
//                if (anEventFormatterConfigurationFileList.getFileName().equals(eventFormatterConfigurationFile.getFileName())) {
//                    return;
//                }
//            }
//        }
//        eventFormatterConfigurationFileList.add(eventFormatterConfigurationFile);
//        eventFormatterConfigurationFileMap.put(tenantId, eventFormatterConfigurationFileList);
//    }
//
//    public void addEventFormatterConfiguration(
//            int tenantId, EventFormatterConfiguration eventFormatterConfiguration)
//            throws EventStreamConfigurationException {
//
//        Map<String, EventFormatter> eventFormatterConfigurationMap
//                = tenantSpecificEventFormatterConfigurationMap.get(tenantId);
//
//        if (eventFormatterConfigurationMap == null) {
//            eventFormatterConfigurationMap = new ConcurrentHashMap<String, EventFormatter>();
//        }
//
//        EventFormatter eventFormatter = new EventFormatter(eventFormatterConfiguration, tenantId);
//        eventFormatterConfigurationMap.put(eventFormatterConfiguration.getEventFormatterName(), eventFormatter);
//
//        tenantSpecificEventFormatterConfigurationMap.put(tenantId, eventFormatterConfigurationMap);
//    }
//
//    public void removeEventFormatterConfigurationFromMap(String fileName, int tenantId) {
//        List<EventFormatterConfigurationFile> eventFormatterConfigurationFileList = eventFormatterConfigurationFileMap.get(tenantId);
//        if (eventFormatterConfigurationFileList != null) {
//            for (EventFormatterConfigurationFile eventFormatterConfigurationFile : eventFormatterConfigurationFileList) {
//                if ((eventFormatterConfigurationFile.getFileName().equals(fileName))) {
//                    if (eventFormatterConfigurationFile.getStatus().equals(EventFormatterConfigurationFile.Status.DEPLOYED)) {
//                        String eventFormatterName = eventFormatterConfigurationFile.getEventFormatterName();
//                        if (tenantSpecificEventFormatterConfigurationMap.get(tenantId) != null) {
//                            EventFormatter eventFormatter = tenantSpecificEventFormatterConfigurationMap.get(tenantId).get(eventFormatterName);
//                            List<EventProducer> eventProducerList = eventFormatter.getEventProducerList();
//                            for (EventProducer eventProducer : eventProducerList) {
//                                eventProducer.unsubscribe(tenantId, eventFormatter.getStreamId(), eventFormatter.getEventFormatterSender());
//                            }
//                            tenantSpecificEventFormatterConfigurationMap.get(tenantId).remove(eventFormatterName);
//                        }
//                    }
//                    eventFormatterConfigurationFileList.remove(eventFormatterConfigurationFile);
//                    return;
//                }
//            }
//        }
//    }
//
//    public void activateInactiveEventFormatterConfiguration(int tenantId, String dependency)
//            throws EventStreamConfigurationException {
//
//        List<EventFormatterConfigurationFile> fileList = new ArrayList<EventFormatterConfigurationFile>();
//
//        if (eventFormatterConfigurationFileMap != null && eventFormatterConfigurationFileMap.size() > 0) {
//            List<EventFormatterConfigurationFile> eventFormatterConfigurationFileList = eventFormatterConfigurationFileMap.get(tenantId);
//
//            if (eventFormatterConfigurationFileList != null) {
//                for (EventFormatterConfigurationFile eventFormatterConfigurationFile : eventFormatterConfigurationFileList) {
//                    if ((eventFormatterConfigurationFile.getStatus().equals(EventFormatterConfigurationFile.Status.WAITING_FOR_DEPENDENCY)) && eventFormatterConfigurationFile.getDependency().equalsIgnoreCase(dependency)) {
//                        fileList.add(eventFormatterConfigurationFile);
//                    }
//                }
//            }
//        }
//        for (EventFormatterConfigurationFile eventFormatterConfigurationFile : fileList) {
//            try {
//                EventFormatterConfigurationFilesystemInvoker.reload(eventFormatterConfigurationFile.getFileName(), eventFormatterConfigurationFile.getAxisConfiguration());
//            } catch (Exception e) {
//                log.error("Exception occurred while trying to deploy the Event formatter configuration file : " + new File(eventFormatterConfigurationFile.getFileName()).getName());
//            }
//        }
//
//    }
//
//    public void deactivateActiveEventFormatterConfiguration(int tenantId, String dependency)
//            throws EventStreamConfigurationException {
//
//        List<EventFormatterConfigurationFile> fileList = new ArrayList<EventFormatterConfigurationFile>();
//        if (tenantSpecificEventFormatterConfigurationMap != null && tenantSpecificEventFormatterConfigurationMap.size() > 0) {
//            Map<String, EventFormatter> eventFormatterMap = tenantSpecificEventFormatterConfigurationMap.get(tenantId);
//            if (eventFormatterMap != null) {
//                for (EventFormatter eventFormatter : eventFormatterMap.values()) {
//                    String streamNameWithVersion = eventFormatter.getEventFormatterConfiguration().getFromStreamName() + ":" + eventFormatter.getEventFormatterConfiguration().getFromStreamVersion();
//                    String eventAdaptorName = eventFormatter.getEventFormatterConfiguration().getToPropertyConfiguration().getEventAdaptorName();
//                    if (streamNameWithVersion.equals(dependency) || eventAdaptorName.equals(dependency)) {
//                        EventFormatterConfigurationFile eventFormatterConfigurationFile = getEventFormatterConfigurationFile(eventFormatter.getEventFormatterConfiguration().getEventFormatterName(), tenantId);
//                        if (eventFormatterConfigurationFile != null) {
//                            fileList.add(eventFormatterConfigurationFile);
//                        }
//                    }
//                }
//            }
//        }
//
//        for (EventFormatterConfigurationFile eventFormatterConfigurationFile : fileList) {
//            EventFormatterConfigurationFilesystemInvoker.reload(eventFormatterConfigurationFile.getFileName(), eventFormatterConfigurationFile.getAxisConfiguration());
//            log.info("EventFormatterSender  is undeployed because dependency : " + eventFormatterConfigurationFile.getEventFormatterName() + " , dependency couldn't found : " + dependency);
//        }
//    }
//
//    //Private Methods are below
//
//    private void editTracingStatistics(
//            EventFormatterConfiguration eventFormatterConfiguration,
//            String eventFormatterName, int tenantId, AxisConfiguration axisConfiguration)
//            throws EventStreamConfigurationException {
//
//        String fileName = getFileName(tenantId, eventFormatterName);
//        undeployActiveEventFormatterConfiguration(eventFormatterName, axisConfiguration);
//        OMElement omElement = FormatterConfigurationBuilder.eventFormatterConfigurationToOM(eventFormatterConfiguration);
//        EventFormatterConfigurationFilesystemInvoker.delete(fileName, axisConfiguration);
//        EventFormatterConfigurationFilesystemInvoker.save(omElement, fileName, axisConfiguration);
//    }
//
//    private String getFileName(int tenantId, String eventFormatterName) {
//
//        if (eventFormatterConfigurationFileMap.size() > 0) {
//            List<EventFormatterConfigurationFile> eventFormatterConfigurationFileList = eventFormatterConfigurationFileMap.get(tenantId);
//            if (eventFormatterConfigurationFileList != null) {
//                for (EventFormatterConfigurationFile eventFormatterConfigurationFile : eventFormatterConfigurationFileList) {
//                    if ((eventFormatterConfigurationFile.getEventFormatterName().equals(eventFormatterName)) && (eventFormatterConfigurationFile.getStatus().equals(EventFormatterConfigurationFile.Status.DEPLOYED))) {
//                        return eventFormatterConfigurationFile.getFileName();
//                    }
//                }
//            }
//        }
//        return null;
//    }
//
//
//    private void editEventFormatterConfiguration(String filename,
//                                                 AxisConfiguration axisConfiguration,
//                                                 String eventFormatterConfiguration,
//                                                 String originalEventFormatterName)
//            throws EventStreamConfigurationException {
//        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
//        try {
//            OMElement omElement = AXIOMUtil.stringToOM(eventFormatterConfiguration);
//            omElement.toString();
//            EventFormatterConfigurationHelper.validateEventFormatterConfiguration(omElement);
//            String mappingType = EventFormatterConfigurationHelper.getOutputMappingType(omElement);
//            if (mappingType != null) {
//                EventFormatterConfiguration eventFormatterConfigurationObject = FormatterConfigurationBuilder.getEventFormatterConfiguration(omElement, tenantId, mappingType);
//                if (!(eventFormatterConfigurationObject.getEventFormatterName().equals(originalEventFormatterName))) {
//                    if (checkEventFormatterValidity(tenantId, eventFormatterConfigurationObject.getEventFormatterName())) {
//                        EventFormatterConfigurationFilesystemInvoker.delete(filename, axisConfiguration);
//                        EventFormatterConfigurationFilesystemInvoker.save(omElement, filename, axisConfiguration);
//                    } else {
//                        throw new EventStreamConfigurationException("There is a Event Formatter " + eventFormatterConfigurationObject.getEventFormatterName() + " with the same name");
//                    }
//                } else {
//                    EventFormatterConfigurationFilesystemInvoker.delete(filename, axisConfiguration);
//                    EventFormatterConfigurationFilesystemInvoker.save(omElement, filename, axisConfiguration);
//                }
//            } else {
//                throw new EventStreamConfigurationException("Mapping type of the Event Formatter " + originalEventFormatterName + " cannot be null");
//
//            }
//
//        } catch (XMLStreamException e) {
//            throw new EventStreamConfigurationException("Not a valid xml object : " + e.getMessage(), e);
//        }
//
//    }
//
//    private EventFormatterConfigurationFile getEventFormatterConfigurationFile(
//            String eventFormatterName, int tenantId) {
//        List<EventFormatterConfigurationFile> eventFormatterConfigurationFileList = eventFormatterConfigurationFileMap.get(tenantId);
//
//        if (eventFormatterConfigurationFileList != null) {
//            for (EventFormatterConfigurationFile eventFormatterConfigurationFile : eventFormatterConfigurationFileList) {
//                if (eventFormatterConfigurationFile.getEventFormatterName().equals(eventFormatterName)) {
//                    return eventFormatterConfigurationFile;
//                }
//            }
//        }
//        return null;
//
//    }
//
//    private void validateToRemoveInactiveEventFormatterConfiguration(String eventFormatterName,
//                                                                     AxisConfiguration axisConfiguration)
//            throws EventStreamConfigurationException {
//        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
//
//        String fileName = eventFormatterName + EventFormatterConstants.EF_CONFIG_FILE_EXTENSION_WITH_DOT;
//        List<EventFormatterConfigurationFile> eventFormatterConfigurationFiles = eventFormatterConfigurationFileMap.get(tenantId);
//        if (eventFormatterConfigurationFiles != null) {
//            for (EventFormatterConfigurationFile eventFormatterConfigurationFile : eventFormatterConfigurationFiles) {
//                if ((eventFormatterConfigurationFile.getFileName().equals(fileName))) {
//                    if (!(eventFormatterConfigurationFile.getStatus().equals(EventFormatterConfigurationFile.Status.DEPLOYED))) {
//                        EventFormatterConfigurationFilesystemInvoker.delete(fileName, axisConfiguration);
//                        break;
//                    }
//                }
//            }
//        }
//


}


//}