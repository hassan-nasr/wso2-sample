/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.cassandra.search.data.json;

import java.util.ArrayList;
import java.util.List;

public class ActivityEvents {

    private String id = null;
    private String startTime= "";
    private String endTime= "";

    private List<ActivityEvent> events = null;

    public ActivityEvents() {
        events = new ArrayList<ActivityEvent>();
    }

    public ActivityEvents(String id) {
        this.id = id;
        events = new ArrayList<ActivityEvent>();
    }

    public ActivityEvents(String id, String startTime, String endTime) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        events = new ArrayList<ActivityEvent>();
    }

    public void addEvent(ActivityEvent record) {
        events.add(record);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setEvents(List<ActivityEvent> events) {
        this.events = events;
    }
}


