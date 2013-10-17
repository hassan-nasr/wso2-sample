/*
 *  Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.bam.notification.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.RangeSlicesQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.datasource.utils.DataSourceUtils;
import org.wso2.carbon.bam.notification.task.internal.NotificationDispatchComponent;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.ndatasource.rdbms.RDBMSConfiguration;
import org.wso2.carbon.ntask.core.AbstractTask;

/**
 * BAM notification message dispatcher task implementation.
 */
public class NotificationDispatchTask extends AbstractTask {

    private static final String STREAM_ID = "streamId";

    public static final String BAM_NOTIFICATION_CF = "bam_notification_messages";

    public static final String TASK_TYPE = "BAM_NOTIFICATION_DISPATCHER_TASK";
    
    public static final String TASK_NAME = "NOTIFIER";
    
    public static final int TASK_INTERVAL = 5000;
    
    public static final String BAM_CASSANDRA_UTIL_DATASOURCE = "WSO2BAM_UTIL_DATASOURCE";
    
    private static StringSerializer STRING_SERIALIZER = new StringSerializer();
    
    private static DataPublisher publisher;
    
    private static Keyspace keyspace;
    
    private static final Log log = LogFactory.getLog(NotificationDispatchTask.class);
    
    private void initPublisherKS() throws Exception {
        Agent agent = new Agent(new AgentConfiguration());
        String url = this.getAgentURL();
        String[] creds = this.getCredentials();
        String username = creds[0];
        String password = creds[1];
        publisher = new DataPublisher(url, username, password, agent);
        keyspace = (Keyspace) DataSourceUtils.getClusterKeyspaceFromRDBMSDataSource(
                MultitenantConstants.SUPER_TENANT_ID, BAM_CASSANDRA_UTIL_DATASOURCE)[1];
    }
    
    private String getAgentURL() {
        String host = System.getProperty("carbon.local.ip");
        int port = 7611;
        String portOffsetStr = System.getProperty("portOffset");
        if (portOffsetStr != null) {
            port += Integer.parseInt(portOffsetStr);
        }
        String url = "tcp://" + host + ":" + port;
        return url;
    }
    
    private String[] getCredentials() throws Exception {
        RDBMSConfiguration config = DataSourceUtils.getRDBMSDataSourceConfig(
                MultitenantConstants.SUPER_TENANT_ID, 
                BAM_CASSANDRA_UTIL_DATASOURCE);
        if (config == null) {
            throw new Exception("The WSO2 BAM Util Cassandra Data Source is not available");
        }
        return new String[] { config.getUsername(), config.getPassword() };
    }
    
    @Override
    public void execute() {
        try {
            if (publisher == null) {
                this.initPublisherKS();
            }
            this.processNotificationRecords();
        } catch (Exception e) {
            log.error("Error executing notification dispatch task: " + e.getMessage(), e);
        }
    }
    
    private void processNotificationRecords() throws Exception {
        RangeSlicesQuery<String, String, String> sliceQuery = HFactory.createRangeSlicesQuery(
                keyspace, STRING_SERIALIZER, STRING_SERIALIZER, STRING_SERIALIZER);
        sliceQuery.setColumnFamily(BAM_NOTIFICATION_CF);
        sliceQuery.setKeys("", "");
        sliceQuery.setRange(null, null, false, Integer.MAX_VALUE);
        QueryResult<OrderedRows<String, String, String>> result  = sliceQuery.execute();
        OrderedRows<String, String, String> rows = result.get();
        Record record;
        for (Row<String, String, String> row : rows.getList()) {
            record = this.recordFromRow(row);
            if (record != null) {
                this.publishData(record);
            }
            this.deleteRow(row.getKey());
        }
    }
    
    private void deleteRow(String key) {
        Mutator<String> mutator = HFactory.createMutator(keyspace, STRING_SERIALIZER);
        mutator.delete(key, BAM_NOTIFICATION_CF, null, STRING_SERIALIZER);
        mutator.execute();
    }
    
    private Record recordFromRow(Row<String, String, String> row) {
        HColumn<String, String> streamIdColumn = row.getColumnSlice().getColumnByName(STREAM_ID);
        System.out.println("*** ROW: " + streamIdColumn);
        if (streamIdColumn == null) {
            return null;
        }
        String streamId = streamIdColumn.getValue();
        System.out.println("** SID: " + streamId);
        Map<String, String> data = new HashMap<String, String>();
        for (HColumn<String, String> col : row.getColumnSlice().getColumns()) {
            if (!col.getName().equals(STREAM_ID)) {
                data.put(col.getName(), col.getValue());
            }
        }
        System.out.println("** DATA: " + data);
        Record record = new Record(streamId, data);
        if (record.getData() == null) {
            return null;
        }
        System.out.println("** RECORD: " + record);
        return record;
    }
    
    private synchronized void publishData(Record record) throws Exception {
        Event event = new Event();
        event.setStreamId(record.getStreamId());
        event.setPayloadData(record.getData());
        publisher.publish(event);
    }
    
    public static class Record {
        
        private String streamId;
        
        private Object[] data;
        
        private static Map<String, StreamDefinition> streamDefs = new HashMap<String, StreamDefinition>();
        
        public Record(String streamId, Map<String, String> data) {
            this.streamId = streamId;
            this.data = this.convertStreamData(streamId, data);
        }
        
        public String getStreamId() {
            return streamId;
        }

        public Object[] getData() {
            return data;
        }
        
        private Object[] convertStreamData(String streamId, Map<String, String> data) {
            EventStreamService esService = NotificationDispatchComponent.getEventStreamService();
            try {
                StreamDefinition streamDef = streamDefs.get(streamId);
                if (streamDef == null) {
                    streamDef = esService.getStreamDefinitionFromStore(streamId, 
                            MultitenantConstants.SUPER_TENANT_ID);
                    if (streamDef != null) {
                        streamDefs.put(streamId, streamDef);
                    }
                }
                if (streamDef == null) {
                    System.out.println("** NULL STREAM DEF");
                    return null;
                }
                List<Object> result = new ArrayList<Object>();
                System.out.println("** STREAM_DEF: " + streamDef);
                String value;
                for (Attribute attr : streamDef.getPayloadData()) {
                    value = data.get(attr.getName());
                    result.add(this.convert(value, attr.getType()));
                }
                return result.toArray();
            } catch (Exception e) {
                return null;
            }
        }
        
        private Object convert(String value, AttributeType type) {
            if (value == null) {
                return null;
            }
            switch (type) {
            case BOOL:
                return Boolean.parseBoolean(value);
            case DOUBLE:
                return Double.parseDouble(value);
            case FLOAT:
                return Float.parseFloat(value);
            case INT:
                return Integer.parseInt(value);
            case LONG:
                return Long.parseLong(value);
            case STRING:
                return value;
            }
            return value;
        }
        
    }

}
