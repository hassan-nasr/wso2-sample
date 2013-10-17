/**
 *
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.databridge.commons;

import java.util.*;

public class IndexDefinition {
    private static final String STREAM_VERSION_KEY = "Version";
    private static final String STREAM_TIMESTAMP_KEY = "Timestamp";

    private List<Attribute> secondaryIndexData;
    private List<Attribute> customIndexData;
    private List<Attribute> fixedSearchData;

    private Map<String, Attribute> metaCustomIndex;
    private Map<String, Attribute> payloadCustomIndex;
    private Map<String, Attribute> correlationCustomIndex;
    private Map<String, Attribute> generalCustomIndex;
    private Map<String, Attribute> fixedPropertiesMap;

    private Set<String> metaFixProps;
    private Set<String> payloadFixProps;
    private Set<String> correlationFixProps;
    private Set<String> generalFixProps;

    private boolean isIndexTimestamp;
    private boolean isIncrementalIndex;

    public IndexDefinition() {
    }

    public void setIndexData(String indexDefnStr, StreamDefinition streamDefinition) {
        if(indexDefnStr == null)
            return;

        String[] indexData = indexDefnStr.split("\\|");

        Set<String> secIndexSet  = new HashSet<String>(Arrays.asList(indexData[0].split(",")));
        Set<String> custIndexSet = new HashSet<String>(Arrays.asList(indexData[1].split(",")));
        List<String> fixedPropertiesList = Arrays.asList(indexData[2].split(","));
        isIncrementalIndex = Boolean.parseBoolean(indexData[3]);
        //order matters
        Map<String, Attribute> tempFixProperties = null;

        if(secIndexSet.size() > 0) {
            secondaryIndexData = new ArrayList<Attribute>(secIndexSet.size());
        }

        if(custIndexSet.size() > 0) {
            customIndexData = new ArrayList<Attribute>(custIndexSet.size());
        }

        if(fixedPropertiesList.size() > 0) {
            fixedSearchData   = new ArrayList<Attribute>(fixedPropertiesList.size());
            tempFixProperties = new HashMap<String, Attribute>(fixedPropertiesList.size());
        }

        if (streamDefinition.getMetaData() != null) {
            for(Attribute attribute : streamDefinition.getMetaData()) {
                if (secIndexSet.contains(attribute.getName())) {
                    secondaryIndexData.add(new Attribute("meta_" + attribute.getName(), attribute.getType()));
                }
                if (custIndexSet.contains(attribute.getName())) {
                    customIndexData.add(new Attribute("meta_" + attribute.getName(), attribute.getType()));
                }
                if (fixedPropertiesList.contains(attribute.getName())) {
                    tempFixProperties.put(attribute.getName(),
                            new Attribute("meta_" + attribute.getName(), attribute.getType()));
                }
            }
        }

        if (streamDefinition.getCorrelationData() != null) {
            for(Attribute attribute : streamDefinition.getCorrelationData()) {
                if (secIndexSet.contains(attribute.getName())) {
                    secondaryIndexData.add(new Attribute("correlation_" + attribute.getName(), attribute.getType()));
                }
                if (custIndexSet.contains(attribute.getName())) {
                    customIndexData.add(new Attribute("correlation_" + attribute.getName(), attribute.getType()));
                }
                if (fixedPropertiesList.contains(attribute.getName())) {
                    tempFixProperties.put(attribute.getName(),
                            new Attribute("correlation_" + attribute.getName(), attribute.getType()));
                }
            }
        }

        if (streamDefinition.getPayloadData() != null) {
            for(Attribute attribute : streamDefinition.getPayloadData()) {
                if (secIndexSet.contains(attribute.getName())) {
                    secondaryIndexData.add(new Attribute("payload_" + attribute.getName(), attribute.getType()));
                }
                if (custIndexSet.contains(attribute.getName())) {
                    customIndexData.add(new Attribute("payload_" + attribute.getName(), attribute.getType()));
                }
                if (fixedPropertiesList.contains(attribute.getName())) {
                    tempFixProperties.put(attribute.getName(),
                            new Attribute("payload_" + attribute.getName(), attribute.getType()));
                }
            }
        }

        //to avoid below code block by indexing timestamp by defualt
        if(custIndexSet.contains(STREAM_TIMESTAMP_KEY)) {
            customIndexData.add(new Attribute(STREAM_TIMESTAMP_KEY, AttributeType.LONG));
        }

        for(String property : fixedPropertiesList) {
            if(!property.equalsIgnoreCase(STREAM_VERSION_KEY)) {
                if (tempFixProperties.get(property) != null) {
                    fixedSearchData.add(tempFixProperties.get(property));
                }
            } else {
                fixedSearchData.add(new Attribute(STREAM_VERSION_KEY , AttributeType.STRING));
            }
        }
    }

    public void setIndexDataFromStore(String indexDefnStr) {
        if(indexDefnStr == null)
            return;
        List<String> secIndexList     = null;
        List<String> custIndexList    = null;
        List<String> fixedSearchProps = null;

        String[] indexData = indexDefnStr.split("\\|");

        if(!indexData[0].isEmpty()) {
            secIndexList = Arrays.asList(indexData[0].split(","));
            secondaryIndexData = new ArrayList<Attribute>(secIndexList.size());
            for(String secIndex : secIndexList) {
                String[] secIndexData = secIndex.split(":");
                secondaryIndexData.add(new Attribute(secIndexData[0], AttributeType.valueOf(secIndexData[1])));
            }
        }

        if (!indexData[1].isEmpty()) {
            custIndexList= Arrays.asList(indexData[1].split(","));
            customIndexData = new ArrayList<Attribute>(custIndexList.size());
            for(String custIndex : custIndexList) {
                String[] custIndexData = custIndex.split(":");
                String name = custIndexData[0];
                AttributeType attributeType = AttributeType.valueOf(custIndexData[1]);

                customIndexData.add(new Attribute(name, attributeType));

                String attributeName = name.substring(name.indexOf("_") + 1);
                if(name.startsWith("meta_")) {
                    if(metaCustomIndex == null) {
                        metaCustomIndex = new HashMap<String, Attribute>();
                    }

                    metaCustomIndex.put(attributeName,
                            new Attribute(attributeName, attributeType));
                } else if(name.startsWith("correlation_")) {
                    if(correlationCustomIndex == null) {
                        correlationCustomIndex = new HashMap<String, Attribute>();
                    }
                    correlationCustomIndex.put(attributeName,
                            new Attribute(attributeName, attributeType));
                } else if(name.startsWith("payload_")) {
                    if(payloadCustomIndex == null) {
                        payloadCustomIndex = new HashMap<String, Attribute>();
                    }
                    payloadCustomIndex.put(attributeName,
                            new Attribute(attributeName, attributeType));
                } else {
                    if(generalCustomIndex == null) {
                        generalCustomIndex = new HashMap<String, Attribute>();
                    }
                    generalCustomIndex.put(attributeName,
                            new Attribute(attributeName, attributeType));

                    if(attributeName.equals(STREAM_TIMESTAMP_KEY)) {
                        isIndexTimestamp = true;
                    }
                }
            }
        }

        if (!indexData[2].isEmpty()) {
            fixedSearchProps = Arrays.asList(indexData[2].split(","));
            fixedSearchData = new ArrayList<Attribute>(fixedSearchProps.size());
            fixedPropertiesMap = new LinkedHashMap<String, Attribute>();
            for(String fixedProperty : fixedSearchProps) {
                String[] fixedPropertyData = fixedProperty.split(":");
                String name = fixedPropertyData[0];
                AttributeType attributeType = AttributeType.valueOf(fixedPropertyData[1]);
                Attribute attribute = new Attribute(name, attributeType);

                fixedSearchData.add(attribute);
                String attributeName = name.substring(name.indexOf("_") + 1);

                fixedPropertiesMap.put(attributeName, attribute);
                if(name.startsWith("meta_")) {
                    if(metaFixProps == null) {
                        metaFixProps = new LinkedHashSet<String>();
                    }

                    metaFixProps.add(attributeName);
                } else if(name.startsWith("correlation_")) {
                    if(correlationFixProps == null) {
                        correlationFixProps = new LinkedHashSet<String>();
                    }
                    correlationFixProps.add(attributeName);
                } else if(name.startsWith("payload_")) {
                    if(payloadFixProps == null) {
                        payloadFixProps = new LinkedHashSet<String>();
                    }
                    payloadFixProps.add(attributeName);
                } else {
                    if(generalFixProps == null) {
                        generalFixProps = new LinkedHashSet<String>();
                    }
                    generalFixProps.add(attributeName);
                }
            }
        }
        isIncrementalIndex = Boolean.parseBoolean(indexData[3]);
    }

    public List<Attribute> getSecondaryIndexData() {
        return secondaryIndexData;
    }

    public List<Attribute> getCustomIndexData() {
        return customIndexData;
    }

    public String getSecondaryIndexDefn() {
        if(secondaryIndexData == null)
            return null;

        StringBuilder indexStringBuilder = new StringBuilder();

        if(secondaryIndexData != null) {
            boolean isRecordAdded = false;
            for(Attribute attribute : secondaryIndexData ) {
                if(isRecordAdded) {
                    indexStringBuilder.append(",");
                }
                indexStringBuilder.append(attribute.getName())
                        .append(":").append(attribute.getType());
                isRecordAdded = true;
            }
        }
        return indexStringBuilder.toString();
    }



    public String getCustomIndexDefn() {
        if(customIndexData == null)
            return null;

        StringBuilder indexStringBuilder = new StringBuilder();

        if(customIndexData != null) {
            boolean isRecordAdded = false;
            for(Attribute attribute : customIndexData ) {
                if(isRecordAdded) {
                    indexStringBuilder.append(",");
                }
                indexStringBuilder.append(attribute.getName())
                        .append(":").append(attribute.getType());;
                isRecordAdded = true;
            }
        }
        return indexStringBuilder.toString();
    }

    public String getFixedSearchDefn() {
        if(fixedSearchData == null)
            return null;

        StringBuilder indexStringBuilder = new StringBuilder();

        if(fixedSearchData != null) {
            boolean isRecordAdded = false;
            for(Attribute attribute : fixedSearchData ) {
                if(isRecordAdded) {
                    indexStringBuilder.append(",");
                }
                indexStringBuilder.append(attribute.getName())
                        .append(":").append(attribute.getType());;
                isRecordAdded = true;
            }
        }
        return indexStringBuilder.toString();
    }

    public boolean isIncrementalIndex() {
        return isIncrementalIndex;
    }

    public AttributeType getAttributeTypeforProperty(String property) {
        if(payloadCustomIndex != null && payloadCustomIndex.containsKey(property)) {
            return payloadCustomIndex.get(property).getType();
        } else if(correlationCustomIndex != null && correlationCustomIndex.containsKey(property)) {
            return correlationCustomIndex.get(property).getType();
        } else if(metaCustomIndex != null && metaCustomIndex.containsKey(property)) {
            return metaCustomIndex.get(property).getType();
        } else if(generalCustomIndex != null && generalCustomIndex.containsKey(property)) {
            return generalCustomIndex.get(property).getType();
        }
        return null;
    }

    public String getAttributeNameforProperty(String property) {
        if(payloadCustomIndex != null && payloadCustomIndex.containsKey(property)) {
            return "payload_" + property;
        } else if(correlationCustomIndex != null && correlationCustomIndex.containsKey(property)) {
            return "correlation_" + property;
        } else if(metaCustomIndex != null && metaCustomIndex.containsKey(property)) {
            return "meta_" + property;
        } else if(generalCustomIndex != null && generalCustomIndex.containsKey(property)) {
            return property;
        }
        return null;
    }

    public AttributeType getAttributeTypeforFixedProperty(String property) {
        return fixedPropertiesMap.get(property).getType();
    }

    public void clearIndexInformation() {
        if(this.secondaryIndexData != null) {
            this.secondaryIndexData.clear();
            this.secondaryIndexData = null;
        }
        if(this.customIndexData != null) {
            this.customIndexData.clear();
            this.customIndexData = null;
        }
    }

    public Map<String, Attribute> getMetaCustomIndex() {
        return metaCustomIndex;
    }

    public Map<String, Attribute> getPayloadCustomIndex() {
        return payloadCustomIndex;
    }

    public Map<String, Attribute> getCorrelationCustomIndex() {
        return correlationCustomIndex;
    }

    public Map<String, Attribute> getFixedPropertiesMap() {
        return fixedPropertiesMap;
    }

    public List<Attribute> getFixedSearchData() {
        return fixedSearchData;
    }

    public void setFixedSearchData(List<Attribute> fixedSearchData) {
        this.fixedSearchData = fixedSearchData;
    }

    public Map<String, Attribute> getGeneralCustomIndex() {
        return generalCustomIndex;
    }

    public boolean isIndexTimestamp() {
        return isIndexTimestamp;
    }

    public void setIndexTimestamp(boolean indexTimestamp) {
        isIndexTimestamp = indexTimestamp;
    }

    public Set<String> getMetaFixProps() {
        return metaFixProps;
    }

    public Set<String> getPayloadFixProps() {
        return payloadFixProps;
    }

    public Set<String> getCorrelationFixProps() {
        return correlationFixProps;
    }

    public Set<String> getGeneralFixProps() {
        return generalFixProps;
    }
}
