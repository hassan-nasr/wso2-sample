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


import org.wso2.carbon.bam.message.tracer.internal.conf.EventPublisherConfig;

import java.util.HashMap;
import java.util.Map;

public class EventPublishConfigHolder {

    private static Map<String, EventPublisherConfig> eventPublisherConfigMap
            = new HashMap<String, EventPublisherConfig>();

    public static EventPublisherConfig getEventPublisherConfig(String key) {
        return eventPublisherConfigMap.get(key);
    }

    public static Map<String, EventPublisherConfig> getEventPublisherConfigMap() {
        return eventPublisherConfigMap;
    }
}