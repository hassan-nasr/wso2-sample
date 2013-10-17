/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.message.tracer.handler.stream;

import org.wso2.carbon.bam.data.publisher.util.BAMDataPublisherConstants;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;

import java.util.ArrayList;
import java.util.List;

public class StreamDefCreator {

    public static String streamName = "BAM_MESSAGE_TRACE";

    public static String version = "1.0.0";

    public static String nickName = "MessageTracerAgent";

    public static String description = "Publish Message Tracing Event";


    public static StreamDefinition getStreamDef() throws MalformedStreamDefinitionException {

        StreamDefinition streamDefinition = new StreamDefinition(streamName, version);
        streamDefinition.setDescription(description);
        streamDefinition.setNickName(nickName);
        streamDefinition.setMetaData(getMetaDefinitions());
        streamDefinition.setPayloadData(getPayloadDefinition());
        streamDefinition.setCorrelationData(getCorrelationDefinition());
        return streamDefinition;
    }


    private static List<Attribute> getMetaDefinitions() {

        List<Attribute> metaList = new ArrayList<Attribute>(6);

        metaList.add(new Attribute(BAMDataPublisherConstants.REQUEST_URL,
                                   AttributeType.STRING));
        metaList.add(new Attribute(BAMDataPublisherConstants.REMOTE_ADDRESS,
                                   AttributeType.STRING));
        metaList.add(new Attribute(BAMDataPublisherConstants.CONTENT_TYPE,
                                   AttributeType.STRING));
        metaList.add(new Attribute(BAMDataPublisherConstants.USER_AGENT,
                                   AttributeType.STRING));
        metaList.add(new Attribute(BAMDataPublisherConstants.HOST,
                                   AttributeType.STRING));
        metaList.add(new Attribute(BAMDataPublisherConstants.REFERER,
                                   AttributeType.STRING));
        metaList.add(new Attribute("server", AttributeType.STRING));

        return metaList;
    }

    private static List<Attribute> getPayloadDefinition() {

        List<Attribute> payloadList = new ArrayList<Attribute>(7);
        payloadList.add(new Attribute(BAMDataPublisherConstants.SERVICE_NAME,
                                      AttributeType.STRING));
        payloadList.add(new Attribute(BAMDataPublisherConstants.OPERATION_NAME,
                                      AttributeType.STRING));
        payloadList.add(new Attribute(BAMDataPublisherConstants.MSG_DIRECTION,
                                      AttributeType.STRING));
        payloadList.add(new Attribute(BAMDataPublisherConstants.SOAP_BODY,
                                      AttributeType.STRING));
        payloadList.add(new Attribute(BAMDataPublisherConstants.SOAP_HEADER,
                                      AttributeType.STRING));
        payloadList.add(new Attribute(BAMDataPublisherConstants.TIMESTAMP,
                                      AttributeType.LONG));
        return payloadList;
    }

    private static List<Attribute> getCorrelationDefinition() {

        List<Attribute> payloadList = new ArrayList<Attribute>(7);
        payloadList.add(new Attribute("activity_id", AttributeType.STRING));
        return payloadList;
    }
}
