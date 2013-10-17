/**
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bam.message.tracer.internal.utils;


import org.wso2.carbon.bam.message.tracer.data.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventConfigUtil {

    public static List<Object> getCorrelationData(Message message) {

        List<Object> correlationData = new ArrayList<Object>(1);
        correlationData.add(message.getActivityId());
        return correlationData;
    }

    public static List<Object> getMetaData(Message message) {

        List<Object> metaData = new ArrayList<Object>(1);
        metaData.add(message.getHost());
        return metaData;
    }

    public static List<Object> getEventData(Message message) {

        List<Object> payloadData = new ArrayList<Object>(5);
        payloadData.add(message.getPayload());
        payloadData.add(message.getType());
        payloadData.add(message.getRequestMethod());
        payloadData.add(message.getResourceUrl());
        payloadData.add(message.getTimestamp());

        return payloadData;
    }

    public static Map<String, String> getArbitraryDataMap(Message message) {
        return message.getAdditionalValues();
    }
}
