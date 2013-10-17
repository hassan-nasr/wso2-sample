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
package org.wso2.carbon.cassandra.search.service;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import me.prettyprint.cassandra.serializers.*;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.Serializer;
import me.prettyprint.hector.api.beans.*;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.query.*;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.wso2.carbon.cassandra.search.connection.ConnectionManager;
import org.wso2.carbon.cassandra.search.data.*;
import org.wso2.carbon.cassandra.search.data.json.*;
import org.wso2.carbon.cassandra.search.engine.Filter;
import org.wso2.carbon.cassandra.search.engine.StatementParser;
import org.wso2.carbon.cassandra.search.engine.SearchQuery;
import org.wso2.carbon.cassandra.search.exception.CassandraSearchException;
import org.wso2.carbon.cassandra.search.utils.CFInfo;
import org.wso2.carbon.cassandra.search.utils.CassandraUtils;
import org.wso2.carbon.cassandra.search.utils.OperationType;
import org.wso2.carbon.cassandra.search.utils.SearchConstants;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.IndexDefinition;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Search for Cassandra
 */
public class CassandraSearchAdmin extends AbstractAdmin {
    private static Gson gson = new Gson();
    private static final StringSerializer STRING_SERIALIZER          = new StringSerializer();
    private static final ByteBufferSerializer BYTE_BUFFER_SERIALIZER = new ByteBufferSerializer();
    private static final DynamicCompositeSerializer DYNAMIC_COMPOSITE_SERIALIZER = new DynamicCompositeSerializer();
    private ByteBuffer emptyByteBuffer = ByteBufferUtil.bytes("");

    private final SimpleDateFormat eventTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final SimpleDateFormat queryTimeFormatter = new SimpleDateFormat("yyyyMMddHHmmss");

    public CassandraSearchAdmin() {
    }

    public boolean connectToCassandraCluster(String clusterName, String connectionUrl,
                                             String userName, String password)
            throws CassandraSearchException {
        Map<String, String> credentials = new HashMap<String, String>();
        if (connectionUrl == null || connectionUrl.isEmpty()) {
            throw new CassandraSearchException("Connection URL is empty. Please provide Cassandra"
                    + " Connection URL to connect");
        }
        if (userName != null && !userName.isEmpty() && password != null) {
            credentials.put("username", userName);
            credentials.put("password", password);
        }
        String parsedClusterName = "";
        if (clusterName.contains(":")) {
            parsedClusterName = clusterName.replace(":", "_");
        }
        ConnectionManager connectionManager = null;
        try {
            connectionManager = new
                    ConnectionManager(parsedClusterName, connectionUrl, credentials);
        } catch (CassandraSearchException e) {
            return false;
        }
        return connectionManager.isConnected();
    }

    public Cluster connectAndGetCassandraCluster(String clusterName, String connectionUrl,
                                                 String userName, String password) {
        Map<String, String> credentials = new HashMap<String, String>();
        if (connectionUrl == null || connectionUrl.isEmpty()) {
            return null;
        }
        if (userName != null && !userName.isEmpty() && password != null) {
            credentials.put("username", userName);
            credentials.put("password", password);
        }
        String parsedClusterName = "";
        if (clusterName.contains(":")) {
            parsedClusterName = clusterName.replace(":", "_");
        }
        Cluster cluster = null;
        try {
            cluster = ConnectionManager.getCassandraCluster(parsedClusterName, connectionUrl, credentials);
            return cluster;
        } catch (CassandraSearchException e) {
            return null;
        }
    }

    public org.wso2.carbon.cassandra.search.data.Row[] getEventSearchResults(String query,
                                                                             String lastSearchRowKey,
                                                                             int sizeLimit)
            throws CassandraSearchException {
        int limit;
        String lastSearchRowKeyMod;
        List<org.wso2.carbon.cassandra.search.data.Row> rowList =
                new ArrayList<org.wso2.carbon.cassandra.search.data.Row>();
        StatementParser statementParser = new StatementParser();
        Map<String, List<Filter>> streamFiltersMap = statementParser.extractFilters(query);

        Cluster cluster = ConnectionManager.getClusterFromSession();

        if (cluster == null) {
            throw new CassandraSearchException(SearchConstants.ERR_NO_CLUSTER_AVAILABLE);
        }

        Keyspace eventKeyspace = ConnectionManager.getKeyspace(cluster, CassandraUtils.getKeySpaceName());
        Keyspace indexKeyspace = ConnectionManager.getKeyspace(cluster, CassandraUtils.getIndexKeySpaceName());

        if(streamFiltersMap.containsKey("*")) {
            modifyStreamFiltersMap(indexKeyspace, streamFiltersMap);
        }

        List<org.wso2.carbon.cassandra.search.data.Row> rowListForStream = null;
        int noOFStreams= streamFiltersMap.size();

        //todo : handling multiple stream result sizes properly
        if(noOFStreams > 0) {
            limit = sizeLimit/noOFStreams;
            lastSearchRowKeyMod = null;
        } else {
            limit = sizeLimit;
            lastSearchRowKeyMod = lastSearchRowKey;
        }

        for(Map.Entry<String, List<Filter>> entry : streamFiltersMap.entrySet()) {
            //todo - now just adding all results - discuss about intersection depending on correlation
            String streamName= entry.getKey();
            rowListForStream = (List<org.wso2.carbon.cassandra.search.data.Row>)getResultsRowsForStream(
                    cluster, eventKeyspace,  indexKeyspace, streamName,
                    lastSearchRowKeyMod, limit, streamFiltersMap.get(streamName), false);

            if (rowListForStream == null) {
                continue;
            }

            rowList.addAll(rowListForStream.size() > limit ? rowListForStream.subList(0, limit) : rowListForStream);
        }

        if (rowList.isEmpty()) {
            return null;
        }

        org.wso2.carbon.cassandra.search.data.Row rows[] =
                new org.wso2.carbon.cassandra.search.data.Row[rowList.size()];
        return rowList.toArray(rows);
    }

    public Activity[] getActivitySearchResults(String query,
                                               int sizeLimit)
            throws CassandraSearchException {
        int limit;
        limit = sizeLimit < SearchConstants.MAX_ACTIVITY_SEARCH_RESULTS ?
                sizeLimit : SearchConstants.MAX_ACTIVITY_SEARCH_RESULTS;
        List<Activity> activityList   = new ArrayList<Activity>();
        Set<String> allActivityIDSet  = new LinkedHashSet<String>();
        List<String> allActivityIDList= new ArrayList<String>();

        StatementParser statementParser = new StatementParser();
        Map<String, List<Filter>> streamFiltersMap = statementParser.extractFilters(query);

        Cluster cluster = ConnectionManager.getClusterFromSession();

        if (cluster == null) {
            throw new CassandraSearchException(SearchConstants.ERR_NO_CLUSTER_AVAILABLE);
        }

        Keyspace eventKeyspace = ConnectionManager.getKeyspace(cluster, CassandraUtils.getKeySpaceName());
        Keyspace indexKeyspace = ConnectionManager.getKeyspace(cluster, CassandraUtils.getIndexKeySpaceName());

        if(streamFiltersMap.containsKey("*")) {
            modifyStreamFiltersMap(indexKeyspace, streamFiltersMap);
        }

        int streamFiltersCount = 0;

        for(Map.Entry<String, List<Filter>> entry : streamFiltersMap.entrySet()) {
            //todo - now just adding all results - discuss about intersection depending on correlation
            String streamName   = entry.getKey();
            OperationType joinOp= streamFiltersMap.get(streamName).get(0).getJoinOp();
            Set<String> resultRowSet =
                    (Set<String>) getResultsRowsForStream(ConnectionManager.getClusterFromSession(),
                            eventKeyspace, indexKeyspace,
                            streamName, null, Integer.MAX_VALUE,
                            streamFiltersMap.get(streamName), true);

            if (resultRowSet != null) {
                if(streamFiltersCount == 0) {
                    allActivityIDSet = resultRowSet;
                } else {
                    allActivityIDSet = getMergedActivitySet(allActivityIDSet, resultRowSet, joinOp);
                }
            }
            streamFiltersCount++;
        }
        allActivityIDList.addAll(allActivityIDSet);

        if(allActivityIDList.size() > limit) {
            allActivityIDList = allActivityIDList.subList(0, limit);
        }

        for(String activityID : allActivityIDList) {
            activityList.add(getActivity(indexKeyspace, activityID));
        }

        Activity activities[] = new Activity[allActivityIDSet.size()];
        return activityList.toArray(activities);
    }

    public String getActivitySearchResultsAsJson(Cluster cluster,
                                                 String query,
                                                 int sizeLimit)
            throws CassandraSearchException {
        try {
            int limit;
            limit = sizeLimit < SearchConstants.MAX_ACTIVITY_SEARCH_RESULTS ?
                    sizeLimit : SearchConstants.MAX_ACTIVITY_SEARCH_RESULTS;
            List<ActivityInfo> activityList= new ArrayList<ActivityInfo>();
            Set<String> allActivityIDSet   = new LinkedHashSet<String>();
            List<String> allActivityIDList = new ArrayList<String>();

            StatementParser statementParser = new StatementParser();
            Map<String, List<Filter>> streamFiltersMap = statementParser.extractFilters(query);

            if (cluster == null) {
                throw new CassandraSearchException(SearchConstants.ERR_NO_CLUSTER_AVAILABLE);
            }

            Keyspace eventKeyspace = ConnectionManager.getKeyspace(cluster, CassandraUtils.getKeySpaceName());
            Keyspace indexKeyspace = ConnectionManager.getKeyspace(cluster, CassandraUtils.getIndexKeySpaceName());

            if(streamFiltersMap.containsKey("*")) {
                modifyStreamFiltersMap(indexKeyspace, streamFiltersMap);
            }

            int streamFiltersCount = 0;

            for(Map.Entry<String, List<Filter>> entry : streamFiltersMap.entrySet()) {
                //todo - now just adding all results - discuss about intersection depending on correlation
                String streamName   = entry.getKey();
                OperationType joinOp= streamFiltersMap.get(streamName).get(0).getJoinOp();

                Set<String> resultRowSet =
                        (Set<String>) getResultsRowsForStream(cluster,
                                eventKeyspace, indexKeyspace,
                                streamName, null, Integer.MAX_VALUE,
                                streamFiltersMap.get(streamName), true);

                if (resultRowSet != null) {
                    if(streamFiltersCount == 0) {
                        allActivityIDSet = resultRowSet;
                    } else {
                        allActivityIDSet = getMergedActivitySet(allActivityIDSet, resultRowSet, joinOp);
                    }
                }
                streamFiltersCount++;
            }
            allActivityIDList.addAll(allActivityIDSet);

            if(allActivityIDList.size() > limit) {
                allActivityIDList = allActivityIDList.subList(0, limit);
            }

            for(String activityID : allActivityIDList) {
                activityList.add(getActivityInfo(indexKeyspace, activityID));
            }

            Activities activities = new Activities();
            activities.setActivities(activityList);
            return gson.toJson(activities);
        } catch (CassandraSearchException e) {
            return gson.toJson(new Activities());
        }
    }

    public org.wso2.carbon.cassandra.search.data.Row[] getEventsForActivity(String activityID)
            throws CassandraSearchException {
        List<String> resultEvents = new ArrayList<String>();
        List<org.wso2.carbon.cassandra.search.data.Row> rowList =
                new ArrayList<org.wso2.carbon.cassandra.search.data.Row>();

        Cluster cluster = ConnectionManager.getClusterFromSession();
        if (cluster == null) {
            throw new CassandraSearchException(SearchConstants.ERR_NO_CLUSTER_AVAILABLE);
        }
        Keyspace eventKeyspace = ConnectionManager.getKeyspace(cluster, CassandraUtils.getKeySpaceName());
        Keyspace indexKeyspace = ConnectionManager.getKeyspace(cluster, CassandraUtils.getIndexKeySpaceName());

        SliceQuery<String, String, String> sliceQuery =
                HFactory.createSliceQuery(indexKeyspace, STRING_SERIALIZER, STRING_SERIALIZER,
                        STRING_SERIALIZER);
        sliceQuery.setColumnFamily(SearchConstants.GLOBAL_ACTIVITY_MONITORING_INDEX_CF).setKey(activityID);
        sliceQuery.setRange(null, null, false, Integer.MAX_VALUE);

        QueryResult<ColumnSlice<String,String>> result = sliceQuery.execute();

        for (HColumn<String, String> column : result.get().getColumns()) {
            resultEvents.add(column.getName());
        }

        if(resultEvents.isEmpty()) {
            return null;
        }

        List<String> resultRowKeys = new ArrayList<String>();
        for(String event : resultEvents) {
            int firstColon = event.indexOf(':');
            int lastColon  = event.lastIndexOf(':');
            String rowKey  = event.substring(firstColon + 1, lastColon);
            String columnFamily = event.substring(lastColon + 1);
            if(resultRowKeys.size() > 0) {
                resultRowKeys.set(0, rowKey);
            } else {
                resultRowKeys.add(rowKey);
            }

            rowList.addAll(getRowsFromRowKeys(cluster, eventKeyspace, columnFamily, resultRowKeys));
        }
        org.wso2.carbon.cassandra.search.data.Row rows[] =
                new org.wso2.carbon.cassandra.search.data.Row[rowList.size()];
        return rowList.toArray(rows);

    }

    public String getEventsForActivityAsJson(Cluster cluster,
                                             String activityID)
            throws CassandraSearchException {
        List<String> resultEvents = new ArrayList<String>();

        List<ActivityEvent> events = new ArrayList<ActivityEvent>();
        if (cluster == null) {
            throw new CassandraSearchException(SearchConstants.ERR_NO_CLUSTER_AVAILABLE);
        }
        Keyspace eventKeyspace = ConnectionManager.getKeyspace(cluster, CassandraUtils.getKeySpaceName());
        Keyspace indexKeyspace = ConnectionManager.getKeyspace(cluster, CassandraUtils.getIndexKeySpaceName());

        SliceQuery<String, String, String> sliceQuery =
                HFactory.createSliceQuery(indexKeyspace, STRING_SERIALIZER, STRING_SERIALIZER,
                        STRING_SERIALIZER);
        sliceQuery.setColumnFamily(SearchConstants.GLOBAL_ACTIVITY_MONITORING_INDEX_CF).setKey(activityID);
        sliceQuery.setRange(null, null, false, Integer.MAX_VALUE);

        QueryResult<ColumnSlice<String,String>> result = sliceQuery.execute();

        ActivityEvents activityEvents = new ActivityEvents(activityID);

        if (result.get().getColumns().size() > 0) {
            String startingColumn = result.get().getColumns().get(0).getName();
            String endingColumn   = result.get().getColumns().get(result.get().getColumns().size() - 1).getName();
            activityEvents.setStartTime(getTimestampString
                    (Long.parseLong(startingColumn.substring(0, startingColumn.indexOf(':')))));
            activityEvents.setEndTime(getTimestampString
                    (Long.parseLong(endingColumn.substring(0, startingColumn.indexOf(':')))));

            for (HColumn<String, String> column : result.get().getColumns()) {
                resultEvents.add(column.getName());
            }
        }

        for(int i = 0; i < resultEvents.size(); i++) {
            String event = resultEvents.get(i);
            int firstColon = event.indexOf(':');
            int lastColon  = event.lastIndexOf(':');
            String rowKey  = event.substring(firstColon + 1, lastColon);
            String columnFamily = event.substring(lastColon + 1);
            ActivityEvent activityEvent = getDetailedEventOfActivity(cluster, eventKeyspace, columnFamily, rowKey);
            activityEvent.setIndex(i);
            events.add(activityEvent);
        }

        activityEvents.setEvents(events);

        return gson.toJson(activityEvents);

    }

    public Column[] getColumnInformationForRow(String streamName, String rowName, int startingNo, int limit)
            throws CassandraSearchException {
        Cluster cluster = ConnectionManager.getClusterFromSession();
        if (cluster == null) {
            throw new CassandraSearchException(SearchConstants.ERR_NO_CLUSTER_AVAILABLE);
        }
        Keyspace keyspace = ConnectionManager.getKeyspace(cluster, CassandraUtils.getKeySpaceName());
        String columnFamily = CassandraUtils.convertStreamNameToCFName(streamName);
        CFInfo columnFamilyInfo = CassandraUtils.getColumnFamilyInfo(cluster, keyspace,
                columnFamily);

        //get the results up to the startingNo
        SliceQuery<ByteBuffer, ByteBuffer, ByteBuffer> sliceQuery =
                HFactory.createSliceQuery(keyspace, BYTE_BUFFER_SERIALIZER, BYTE_BUFFER_SERIALIZER,
                        BYTE_BUFFER_SERIALIZER);
        sliceQuery.setColumnFamily(columnFamily);
        sliceQuery.setKey(ByteBuffer.wrap(rowName.getBytes()));

        QueryResult<ColumnSlice<ByteBuffer, ByteBuffer>> result;
        if (startingNo != 0) {
            sliceQuery.setRange(emptyByteBuffer, emptyByteBuffer, false,
                    startingNo + 1);
            try {
                result = sliceQuery.execute();
            } catch (HectorException exception) {
                throw new CassandraSearchException(exception.getMessage(), exception);
            }
            List<HColumn<ByteBuffer, ByteBuffer>> tmpHColumnsList = result.get().getColumns();

            //TODO handle if results are empty
            HColumn startingColumn = tmpHColumnsList.get(tmpHColumnsList.size() - 1);
            ByteBuffer startingColumnName = (ByteBuffer) startingColumn.getName();

            sliceQuery.setRange(startingColumnName, emptyByteBuffer, false, limit);
        } else {
            sliceQuery.setRange(emptyByteBuffer, emptyByteBuffer, false, limit);
        }
        try {
            result = sliceQuery.execute();
        } catch (HectorException exception) {
            throw new CassandraSearchException(exception.getMessage(), exception);
        }

        List<HColumn<ByteBuffer, ByteBuffer>> hColumnsList;
        ArrayList<Column> columnsList = new ArrayList<Column>();
        hColumnsList = result.get().getColumns();

        for (HColumn hColumn : hColumnsList) {
            Column column = new Column();

            String key = CassandraUtils.getStringDeserialization(
                    columnFamilyInfo.getColumnCassandraSerializer(), hColumn.getNameBytes());
            String value = CassandraUtils.getStringDeserialization(columnFamilyInfo.
                    getColumnValueCassandraSerializer(hColumn.getNameBytes()), hColumn.getValueBytes());

            key = cleanNonXmlChars(key);
            value = cleanNonXmlChars(value);

            column.setName(key);
            column.setValue(value);
            column.setTimeStamp(hColumn.getClock());

            columnsList.add(column);
        }
        Column[] columnArray = new Column[columnsList.size()];
        return columnsList.toArray(columnArray);
    }

    public boolean isValidQuery(Cluster cluster, String query) {
        StatementParser statementParser = new StatementParser();
        try {
            Map<String, List<Filter>> streamFiltersMap = statementParser.extractFilters(query);
            Keyspace indexKeyspace = ConnectionManager.getKeyspace(cluster, CassandraUtils.getIndexKeySpaceName());

            if(streamFiltersMap.containsKey("*")) {
                modifyStreamFiltersMap(indexKeyspace, streamFiltersMap);
            }
            for(Map.Entry<String, List<Filter>> entry : streamFiltersMap.entrySet()) {
                String streamName= entry.getKey();
                List<Filter> filterList = streamFiltersMap.get(streamName);

                IndexDefinition indexDef   = getIndexDefinition(indexKeyspace, streamName);
                if(indexDef == null) {
                    return false;
                }

                SearchQuery searchQuery = new SearchQuery(indexDef);
                searchQuery.buildQuery(filterList);

                if(!searchQuery.isValidQuery()) {
                    return false;
                }

            }
        } catch (CassandraSearchException e) {
            return false;
        }
        return true;
    }

    private void modifyStreamFiltersMap(Keyspace indexKeyspace,
                                        Map<String, List<Filter>> streamFiltersMap)
            throws CassandraSearchException {
        List<Filter> allStreamFiltersList = streamFiltersMap.remove("*");
        Map<String, IndexDefinition> indexDefinitionMap = getAllIndexDefinitionsFromCassandra(indexKeyspace);

        for(Map.Entry<String, IndexDefinition> entry : indexDefinitionMap.entrySet()) {
            String streamName = entry.getKey();
            if(streamFiltersMap.containsKey(streamName)) {
                continue;
            }

            if (validateIndexPropsForStream(entry.getValue(), allStreamFiltersList)) {
                streamFiltersMap.put(streamName, allStreamFiltersList);
            }
        }
    }

    private boolean validateIndexPropsForStream(IndexDefinition indexDefinition, List<Filter> allStreamFiltersList) {
        boolean isNonFixFilterExists = false;
        List<Filter> tempList = new ArrayList<Filter>();
        tempList.addAll(allStreamFiltersList);

        if(indexDefinition == null) {
            return false;
        }

        Iterator<Filter> it = tempList.iterator();
        while (it.hasNext()) {
            Filter filter = it.next();
            String propertyName = filter.getProperty();
            if (!propertyName.equals(SearchConstants.TIMESTAMP_PROPERTY)) {
                if (indexDefinition.getAttributeTypeforProperty(propertyName) != null) {
                    isNonFixFilterExists = true;
                    it.remove();
                    continue;
                }

                if (indexDefinition.getAttributeTypeforFixedProperty(propertyName) != null) {
                    it.remove();
                } else {
                    return false;
                }
            }
        }

        return isNonFixFilterExists || !tempList.isEmpty() &&
                indexDefinition.getAttributeTypeforProperty(SearchConstants.TIMESTAMP_PROPERTY) != null;

    }

    private Object getResultsRowsForStream(Cluster cluster,
                                           Keyspace eventKeyspace,
                                           Keyspace indexKeyspace,
                                           String streamName,
                                           String lastSearchRowKey,
                                           int limit,
                                           List<Filter> filterList,
                                           boolean isActivitySearch)
            throws CassandraSearchException {
        List<String> resultRowKeys = null;

        IndexDefinition indexDef   = getIndexDefinition(indexKeyspace, streamName);

        //todo proper exception handling
        if(indexDef == null) {
            return null;
        }

        String primaryCFName = CassandraUtils.convertStreamNameToCFName(streamName);

        SearchQuery searchQuery = new SearchQuery(indexDef);
        searchQuery.buildQuery(filterList);

        if(!searchQuery.isValidQuery()) {
            throw new CassandraSearchException(SearchConstants.ERR_INVALID_SEARCH_QUERY);
        } else {
            searchQuery.organizeSearchFilters();
        }

        List<String> nonFixedProperties = searchQuery.getSearchProperties();
        List<String> fixedSearchProperties = searchQuery.getMandatoryProperties();
        DynamicComposite startRange = new DynamicComposite();
        DynamicComposite endRange   = new DynamicComposite();

        if(!searchQuery.isHasNonFixProps()) {
            if(fixedSearchProperties == null ) {
                throw new CassandraSearchException(SearchConstants.ERR_INVALID_SEARCH_QUERY);
            }

            prepareRangeQueries(startRange, endRange, searchQuery, fixedSearchProperties.size() - 1, indexDef);

            if(!searchQuery.isMultiQuery()) {
                String propertyName = fixedSearchProperties.get(fixedSearchProperties.size() - 1);
                String indexCFName  = CassandraUtils.getCustomIndexCFName(primaryCFName, propertyName);
                List<Filter> filtersList = searchQuery.getAllFiltersMap().get(propertyName);
                resultRowKeys = getSearchRowKeysForFilter(indexKeyspace, eventKeyspace, indexCFName, primaryCFName, filtersList,
                        indexDef, searchQuery, startRange, endRange, limit, fixedSearchProperties, lastSearchRowKey);
            } else {
                resultRowKeys = getJoinedResultList(indexKeyspace, primaryCFName, indexDef,
                        searchQuery, startRange,endRange, fixedSearchProperties);
            }

        } else {
            //Adding Fixed properties to slice query
            if (fixedSearchProperties != null) {
                prepareRangeQueries(startRange, endRange, searchQuery, fixedSearchProperties.size(), indexDef);
            }

            if (!searchQuery.isMultiQuery()) {
                //Checking for non fixed search property. There should be only one.
                String propertyName = nonFixedProperties.get(0);
                String indexCFName  = CassandraUtils.getCustomIndexCFName(primaryCFName, propertyName);
                List<Filter> filtersList = searchQuery.getAllFiltersMap().get(propertyName);

                resultRowKeys = getSearchRowKeysForFilter(indexKeyspace, eventKeyspace, indexCFName, primaryCFName, filtersList,
                        indexDef, searchQuery, startRange, endRange, limit, fixedSearchProperties, lastSearchRowKey);
            } else {
                resultRowKeys = getJoinedResultList(indexKeyspace, primaryCFName, indexDef,
                        searchQuery, startRange,endRange, fixedSearchProperties);
            }

        }
        if(resultRowKeys != null && !resultRowKeys.isEmpty()) {
            return !isActivitySearch ? getRowsFromRowKeys(cluster, eventKeyspace, primaryCFName, resultRowKeys)
                    : getActivityIDsForRowKeys(cluster, eventKeyspace, primaryCFName, resultRowKeys);
        }
        return null;
    }

    private void prepareRangeQueries(DynamicComposite startRange,
                                     DynamicComposite endRange,
                                     SearchQuery searchQuery,
                                     int propertyLength,
                                     IndexDefinition indexDef)
            throws CassandraSearchException {
        List<String> fixedSearchProperties = searchQuery.getMandatoryProperties();

        for(int i = 0; i < propertyLength; i++) {
            String fixProperty = fixedSearchProperties.get(i);
            AttributeType attributeType = indexDef.getAttributeTypeforFixedProperty(fixProperty);

            if(attributeType == null) {
                throw new CassandraSearchException(SearchConstants.ERR_INVALID_SEARCH_QUERY);
            }

            Filter filter         = searchQuery.getAllFiltersMap().get(fixProperty).get(0);
            String comparator     = CassandraUtils.getComparator(attributeType);
            Serializer serializer = CassandraUtils.getSerializer(comparator);

            Object value          = CassandraUtils.getValue(filter.getValue(), attributeType);

            if(!(serializer instanceof DoubleSerializer)) {
                startRange.addComponent(startRange.size(), value,
                        serializer,
                        comparator,
                        AbstractComposite.ComponentEquality.EQUAL);
                endRange.addComponent(endRange.size(), value,
                        serializer,
                        comparator,
                        AbstractComposite.ComponentEquality.EQUAL);
            } else {
                startRange.addComponent(startRange.size(), DoubleSerializer.get().toByteBuffer((Double) value),
                        CassandraUtils.getSerializer(CassandraUtils.BYTESTYPE),
                        CassandraUtils.BYTESTYPE,
                        AbstractComposite.ComponentEquality.EQUAL);
                endRange.addComponent(endRange.size(), DoubleSerializer.get().toByteBuffer((Double) value),
                        CassandraUtils.getSerializer(CassandraUtils.BYTESTYPE),
                        CassandraUtils.BYTESTYPE,
                        AbstractComposite.ComponentEquality.EQUAL);
            }
        }

    }

    private List<String> getSearchRowKeysForFilter(Keyspace indexKeyspace,
                                                   Keyspace eventKeyspace,
                                                   String indexCFName,
                                                   String primaryCFName,
                                                   List<Filter> filterList,
                                                   IndexDefinition indexDef,
                                                   SearchQuery searchQuery,
                                                   DynamicComposite startRange,
                                                   DynamicComposite endRange,
                                                   int limit,
                                                   List<String> fixedSearchProperties,
                                                   String lastSearchRowKey) throws CassandraSearchException {
        String propertyName = filterList.get(0).getProperty();
        List<String> resultRowKeys = new ArrayList<String>();
        List<Filter> timestampFilters = searchQuery.getTimestampFilter();
        //Checking for non fixed search property. There should be only one.
        AttributeType attributeType = indexDef.getAttributeTypeforProperty(propertyName);
        if(attributeType == null) {
            throw new CassandraSearchException(SearchConstants.ERR_INVALID_SEARCH_QUERY);
        }

        boolean isLastRowKeyProvided = lastSearchRowKey != null && !lastSearchRowKey.isEmpty();

        //Has only one operation on property. Equality Operation found. So we can directly add to above range query
        if(filterList.size() == 1 && filterList.get(0).getOperator().equals(SearchConstants.EQ)) {
            Filter filter = filterList.get(0);

            String comparator     = CassandraUtils.getComparator(attributeType);
            Serializer serializer = CassandraUtils.getSerializer(comparator);

            Object value          = CassandraUtils.getValue(filter.getValue(), attributeType);

            if(!(serializer instanceof DoubleSerializer)) {
                startRange.setComponent(startRange.size(), value,
                        serializer,
                        comparator,
                        AbstractComposite.ComponentEquality.EQUAL);
                endRange.setComponent(endRange.size(), value,
                        serializer,
                        comparator,
                        (timestampFilters != null || isLastRowKeyProvided) ? AbstractComposite.ComponentEquality.EQUAL :
                                AbstractComposite.ComponentEquality.GREATER_THAN_EQUAL);
            } else {
                startRange.setComponent(startRange.size(), DoubleSerializer.get().toByteBuffer((Double) value),
                        CassandraUtils.getSerializer(CassandraUtils.BYTESTYPE),
                        CassandraUtils.BYTESTYPE,
                        AbstractComposite.ComponentEquality.EQUAL);
                endRange.setComponent(endRange.size(), DoubleSerializer.get().toByteBuffer((Double) value),
                        CassandraUtils.getSerializer(CassandraUtils.BYTESTYPE),
                        CassandraUtils.BYTESTYPE,
                        (timestampFilters != null || isLastRowKeyProvided) ? AbstractComposite.ComponentEquality.EQUAL :
                                AbstractComposite.ComponentEquality.GREATER_THAN_EQUAL);
            }

            if (!isLastRowKeyProvided) {
                addTimeStampFilters(timestampFilters, startRange, endRange, startRange.size());
            } else {
                Map<String, String> valueMap = getSelectedPropertyValuesForRow(eventKeyspace, primaryCFName,
                        lastSearchRowKey, SearchConstants.TIMESTAMP_PROPERTY);
                long timeStart = Long.parseLong(valueMap.get(SearchConstants.TIMESTAMP_PROPERTY));
                addTimeStampFilterForPaging(timestampFilters, startRange, endRange, timeStart, startRange.size());
            }

            addResultRowKeys(indexKeyspace, indexCFName, startRange, endRange, limit,
                    indexDef, fixedSearchProperties, attributeType, resultRowKeys);

        } else {
            //range query on property has to be performed. So another range should be defined
            String comparator     = CassandraUtils.getComparator(attributeType);
            Serializer serializer = CassandraUtils.getSerializer(comparator);
            List<Object> valueList= getRangeValueListForFilter(indexKeyspace, indexCFName, propertyName, filterList, attributeType);
            int resultCount       = 0;
            int nextComponentPosition = startRange.size();

            if (!isLastRowKeyProvided) {
                AbstractComposite.ComponentEquality componentEquality = (timestampFilters != null) ?
                        AbstractComposite.ComponentEquality.EQUAL :
                        AbstractComposite.ComponentEquality.GREATER_THAN_EQUAL;

                for(Object val : valueList) {
                    if(!(serializer instanceof DoubleSerializer)) {
                        startRange.setComponent(nextComponentPosition,
                                val,
                                serializer,
                                comparator,
                                AbstractComposite.ComponentEquality.EQUAL);
                        endRange.setComponent(nextComponentPosition,
                                val,
                                serializer,
                                comparator,
                                componentEquality);
                    } else {
                        startRange.setComponent(nextComponentPosition, DoubleSerializer.get().toByteBuffer((Double) val),
                                CassandraUtils.getSerializer(CassandraUtils.BYTESTYPE),
                                CassandraUtils.BYTESTYPE,
                                AbstractComposite.ComponentEquality.EQUAL);
                        endRange.setComponent(nextComponentPosition, DoubleSerializer.get().toByteBuffer((Double) val),
                                CassandraUtils.getSerializer(CassandraUtils.BYTESTYPE),
                                CassandraUtils.BYTESTYPE,
                                componentEquality);
                    }

                    addTimeStampFilters(timestampFilters, startRange, endRange, nextComponentPosition + 1);

                    addResultRowKeys(indexKeyspace, indexCFName, startRange, endRange,
                            limit - resultRowKeys.size(), indexDef, fixedSearchProperties, attributeType, resultRowKeys);

                    if(resultCount >= resultRowKeys.size()) {
                        break;
                    }
                }
            } else {
                String propertyNameInCF = indexDef.getAttributeNameforProperty(propertyName);
                Map<String, String> valueMap = getSelectedPropertyValuesForRow(eventKeyspace, primaryCFName,
                        lastSearchRowKey, propertyNameInCF);

                Object propertyValue = CassandraUtils.getValue(valueMap.get(propertyNameInCF), attributeType);
                long timeStart       = Long.parseLong(valueMap.get(SearchConstants.TIMESTAMP_PROPERTY));

                int valuePosition = valueList.indexOf(propertyValue);

                if(valuePosition < 0) {
                    throw new CassandraSearchException("Invalid Search Query. Provided last row ID is incorrect.");
                }

                for(int i = valuePosition; i < valueList.size(); i++) {
                    Object val = valueList.get(i);
                    if(!(serializer instanceof DoubleSerializer)) {
                        startRange.setComponent(nextComponentPosition,
                                val,
                                serializer,
                                comparator,
                                AbstractComposite.ComponentEquality.EQUAL);
                        endRange.setComponent(nextComponentPosition,
                                val,
                                serializer,
                                comparator,
                                AbstractComposite.ComponentEquality.EQUAL);
                    } else {
                        startRange.setComponent(nextComponentPosition, DoubleSerializer.get().toByteBuffer((Double) val),
                                CassandraUtils.getSerializer(CassandraUtils.BYTESTYPE),
                                CassandraUtils.BYTESTYPE,
                                AbstractComposite.ComponentEquality.EQUAL);
                        endRange.setComponent(nextComponentPosition, DoubleSerializer.get().toByteBuffer((Double) val),
                                CassandraUtils.getSerializer(CassandraUtils.BYTESTYPE),
                                CassandraUtils.BYTESTYPE,
                                AbstractComposite.ComponentEquality.EQUAL);
                    }

                    addTimeStampFilterForPaging(timestampFilters, startRange, endRange,
                            timeStart, nextComponentPosition + 1);

                    addResultRowKeys(indexKeyspace, indexCFName, startRange, endRange,
                            limit - resultRowKeys.size(), indexDef, fixedSearchProperties, attributeType, resultRowKeys);
                    if(resultCount >= resultRowKeys.size()) {
                        break;
                    }
                }
            }
        }
        return resultRowKeys;
    }

    //todo remove unwanted method parameters after testing completed
    private List<String> addResultRowKeys(Keyspace indexKeyspace,
                                          String indexCFName,
                                          DynamicComposite startRange,
                                          DynamicComposite endRange,
                                          int limit,
                                          IndexDefinition indexDef,
                                          List<String> fixedSearchProperties,
                                          AttributeType attributeType,
                                          List<String> resultRowKeys) {
        SliceQuery<String, DynamicComposite, String> sliceQuery =
                HFactory.createSliceQuery(indexKeyspace,
                        STRING_SERIALIZER,
                        DYNAMIC_COMPOSITE_SERIALIZER,
                        STRING_SERIALIZER);
        sliceQuery.setColumnFamily(indexCFName);
        sliceQuery.setKey(SearchConstants.CUSTOM_INDEX_ROWS_KEY);

        sliceQuery.setRange(startRange, endRange, false, limit);

        QueryResult<ColumnSlice<DynamicComposite, String>> result = sliceQuery.execute();
        ColumnSlice<DynamicComposite, String> dcs = result.get();
        for ( HColumn<DynamicComposite, String> col: dcs.getColumns() ) {
//            int columnKeyPosition = 0;
            String columnValue = col.getValue();
//            StringBuilder columnKey = new StringBuilder();
//
//            for(String fixProperty : fixedSearchProperties) {
//                columnKey.append(col.getName().get(columnKeyPosition++,
//                        CassandraUtils.getSerializer(CassandraUtils.getComparator
//                                (indexDef.getAttributeTypeforProperty(fixProperty))
//                        ))).append(":");
//            }
//            columnKey.append(col.getName().get(columnKeyPosition++,
//                    CassandraUtils.getSerializer(CassandraUtils.getComparator(attributeType)))).append(":");
//            columnKey.append(col.getName().get(columnKeyPosition++, LongSerializer.get()));

            resultRowKeys.add(columnValue);
        }
        return resultRowKeys;
    }

    private void addTimeStampFilterForPaging(List<Filter> timestampFilters,
                                             DynamicComposite startRange,
                                             DynamicComposite endRange,
                                             long timeStart,
                                             int componentPosition) throws CassandraSearchException {
        long value2 = -1;
        Filter propFilter2 = null;
        if(timestampFilters != null) {
            timestampFilters = getModifiedTimeRangeFilters(timestampFilters);
            propFilter2 = timestampFilters.get(1);
            value2    = getTimestampFromString(propFilter2.getValue());
        } else {
            value2    = getTimestampFromString("20301231235959");
        }

        String comparator = CassandraUtils.getComparator(AttributeType.LONG);
        Serializer serializer = LongSerializer.get();

        startRange.setComponent(componentPosition, timeStart,
                serializer,
                comparator,
                AbstractComposite.ComponentEquality.EQUAL);

        if (propFilter2 != null) {
            endRange.setComponent(componentPosition, value2,
                    serializer,
                    comparator,
                    propFilter2.getOperator().equals(SearchConstants.LT) ?
                            AbstractComposite.ComponentEquality.EQUAL :
                            AbstractComposite.ComponentEquality.GREATER_THAN_EQUAL);
        } else {
            endRange.setComponent(componentPosition, value2,
                    serializer,
                    comparator,
                    AbstractComposite.ComponentEquality.GREATER_THAN_EQUAL);
        }
    }

    private void addTimeStampFilters(List<Filter> timestampFilters,
                                     DynamicComposite startRange,
                                     DynamicComposite endRange,
                                     int componentPosition) throws CassandraSearchException {
        if(timestampFilters == null) {
            return;
        }

        timestampFilters = getModifiedTimeRangeFilters(timestampFilters);

        String comparator = CassandraUtils.getComparator(AttributeType.LONG);
        Serializer serializer = LongSerializer.get();

        Filter propFilter1 = timestampFilters.get(0);
        Filter propFilter2 = timestampFilters.get(1);

        long value1 = getTimestampFromString(propFilter1.getValue());
        long value2 = getTimestampFromString(propFilter2.getValue());

        startRange.setComponent(componentPosition, value1,
                serializer,
                comparator,
                propFilter1.getOperator().equals(SearchConstants.GT) ?
                        AbstractComposite.ComponentEquality.GREATER_THAN_EQUAL :
                        AbstractComposite.ComponentEquality.EQUAL);
        endRange.setComponent(componentPosition, value2,
                serializer,
                comparator,
                propFilter2.getOperator().equals(SearchConstants.LT) ?
                        AbstractComposite.ComponentEquality.EQUAL :
                        AbstractComposite.ComponentEquality.GREATER_THAN_EQUAL);
    }

    private List<Object> getRangeValueListForFilter(Keyspace indexKeyspace,
                                                    String indexCFName,
                                                    String propertyName,
                                                    List<Filter> filterList,
                                                    AttributeType attributeType)
            throws CassandraSearchException {
        List<Filter> propertyFilters = null;
        Object value1   = null;
        Object value2   = null;
        Filter propFilter1 = null;
        Filter propFilter2 = null;

        DynamicComposite startRange = new DynamicComposite();
        DynamicComposite endRange   = new DynamicComposite();

        String comparator     = CassandraUtils.getComparator(attributeType);
        Serializer serializer = CassandraUtils.getSerializer(comparator);

        if(!propertyName.equals(SearchConstants.TIMESTAMP_PROPERTY)) {
            propertyFilters = getModifiedRangeFilters(filterList, attributeType);
            propFilter1 = propertyFilters.get(0);
            propFilter2 = propertyFilters.get(1);

            value1   = CassandraUtils.getValue(propFilter1.getValue(), attributeType);
            value2   = CassandraUtils.getValue(propFilter2.getValue(), attributeType);
        } else {
            propertyFilters =  getModifiedTimeRangeFilters(filterList);
            comparator = CassandraUtils.getComparator(AttributeType.LONG);
            serializer = LongSerializer.get();

            propFilter1 = propertyFilters.get(0);
            propFilter2 = propertyFilters.get(1);

            value1 = getTimestampFromString(propFilter1.getValue());
            value2 = getTimestampFromString(propFilter2.getValue());
        }

        if(!(serializer instanceof DoubleSerializer)) {
            startRange.setComponent(0, value1,
                    serializer,
                    comparator,
                    propFilter1.getOperator().equals(SearchConstants.GT) ?
                            AbstractComposite.ComponentEquality.GREATER_THAN_EQUAL :
                            AbstractComposite.ComponentEquality.EQUAL);
            endRange.setComponent(0, value2,
                    serializer,
                    comparator,
                    propFilter2.getOperator().equals(SearchConstants.LT) ?
                            AbstractComposite.ComponentEquality.LESS_THAN_EQUAL :
                            AbstractComposite.ComponentEquality.GREATER_THAN_EQUAL);
        } else {
            startRange.setComponent(0,
                    DoubleSerializer.get().toByteBuffer((Double) value1),
                    CassandraUtils.getSerializer(CassandraUtils.BYTESTYPE),
                    CassandraUtils.BYTESTYPE,
                    propFilter1.getOperator().equals(SearchConstants.GT) ?
                            AbstractComposite.ComponentEquality.GREATER_THAN_EQUAL :
                            AbstractComposite.ComponentEquality.EQUAL);
            endRange.setComponent(0,
                    DoubleSerializer.get().toByteBuffer((Double) value2),
                    CassandraUtils.getSerializer(CassandraUtils.BYTESTYPE),
                    CassandraUtils.BYTESTYPE,
                    propFilter2.getOperator().equals(SearchConstants.LT) ?
                            AbstractComposite.ComponentEquality.LESS_THAN_EQUAL :
                            AbstractComposite.ComponentEquality.GREATER_THAN_EQUAL);
        }

        //search here
        SliceQuery<String, DynamicComposite, String> sliceQuery =
                HFactory.createSliceQuery(indexKeyspace,
                        STRING_SERIALIZER,
                        DYNAMIC_COMPOSITE_SERIALIZER,
                        STRING_SERIALIZER);
        sliceQuery.setColumnFamily(indexCFName);
        sliceQuery.setKey(SearchConstants.CUSTOM_INDEX_VALUE_ROW_KEY);

        sliceQuery.setRange(startRange, endRange, false, Integer.MAX_VALUE);

        QueryResult<ColumnSlice<DynamicComposite, String>> result = null;
        try {
            result = sliceQuery.execute();
        } catch (HectorException exception) {
            throw new CassandraSearchException(exception.getMessage(), exception);
        }

        ColumnSlice<DynamicComposite, String> dcs = result.get();
        List<Object> valueList = new ArrayList<Object>();
        for (HColumn<DynamicComposite, String> col: dcs.getColumns()) {
            valueList.add(col.getName().get(0, serializer)) ;
        }

        return valueList;
    }

    private List<String> getJoinedResultList(Keyspace indexKeyspace,
                                             String primaryCFName,
                                             IndexDefinition indexDef,
                                             SearchQuery searchQuery,
                                             DynamicComposite startRange,
                                             DynamicComposite endRange,
                                             List<String> fixedSearchProperties)
            throws CassandraSearchException {
        int subFilterCount = 0;
        List<String> allResultRowKeys = new ArrayList<String>();
        int originalRangeSize = startRange.size();
        for(List<Filter> subFilter : searchQuery.getSubFilters()) {
            String propertyName  = subFilter.get(0).getProperty();
            String indexCFName   = CassandraUtils.getCustomIndexCFName(primaryCFName, propertyName);
            OperationType joinOp = subFilter.get(0).getJoinOp();

            List<String> resultRowKeys = getSearchRowKeysForFilter(indexKeyspace, null, indexCFName, null, subFilter,
                    indexDef, searchQuery, startRange, endRange, Integer.MAX_VALUE, fixedSearchProperties, null);

            if(subFilterCount == 0) {
                allResultRowKeys = resultRowKeys;
            } else {
                allResultRowKeys = joinOp == OperationType.AND
                        ? intersection(allResultRowKeys, resultRowKeys)
                        : union(allResultRowKeys, resultRowKeys);

            }
            subFilterCount++;

            int newRangeSize = startRange.size();
            for(int i = newRangeSize; i > originalRangeSize; i--) {
                startRange.remove(i-1);
                endRange.remove(i-1);
            }
        }

        return allResultRowKeys;

    }

    private Set<String> getMergedActivitySet(Set<String> allResultsSet,
                                             Set<String> newResultsSet,
                                             OperationType joinOp) {
        Set<String> resultsKeySet = null;
        if(joinOp == OperationType.AND) {
            resultsKeySet = Sets.intersection(allResultsSet, newResultsSet);
        } else {
            resultsKeySet = Sets.union(allResultsSet, newResultsSet);
        }
        return resultsKeySet;
    }

    private List<String> union(List<String> list1, List<String> list2) {
        Set<String> set = new HashSet<String>();

        set.addAll(list1);
        set.addAll(list2);

        return new ArrayList<String>(set);
    }

    private List<String> intersection(List<String> list1, List<String> list2) {
        List<String> list = new ArrayList<String>();

        if (list1.size() < list2.size()) {
            for (String t : list1) {
                if(list2.contains(t)) {
                    list.add(t);
                }
            }
        } else {
            for (String t : list2) {
                if(list1.contains(t)) {
                    list.add(t);
                }
            }
        }

        return list;
    }

    private List<Filter> getModifiedTimeRangeFilters(List<Filter> originalFilters) {
        List<Filter> modifiedFilter = new ArrayList<Filter>();
        Filter filter1 = null;
        Filter filter2 = null;
        if(originalFilters.size() == 1) {
            filter1 = originalFilters.get(0);

            if(filter1.getOperator().equals(SearchConstants.EQ)) {
                filter2 = new Filter(filter1.getOperator(),
                        filter1.getOperator(), filter1.getValue(), null);
            } else if(filter1.getOperator().equals(SearchConstants.LT) ||
                    filter1.getOperator().equals(SearchConstants.LE)) {
                filter2 = new Filter(filter1.getProperty(),
                        filter1.getOperator(), filter1.getValue(), null);
                filter1 = new Filter(filter1.getProperty(),
                        SearchConstants.GT, "19700101000000", OperationType.AND);
            } else {
                filter2 = new Filter(filter1.getProperty(),
                        SearchConstants.LT, "20301231235959", null);
            }
        } else if(originalFilters.size() == 2) {
            if(originalFilters.get(0).getOperator().equals(SearchConstants.GT) ||
                    originalFilters.get(0).getOperator().equals(SearchConstants.GE))  {
                filter1 = originalFilters.get(0);
                filter2 = originalFilters.get(1);
            } else {
                filter1 = originalFilters.get(1);
                filter2 = originalFilters.get(0);
            }
        }
        modifiedFilter.add(filter1);
        modifiedFilter.add(filter2);

        return modifiedFilter;
    }

    private List<Filter> getModifiedRangeFilters(List<Filter> originalFilters, AttributeType attributeType) {
        List<Filter> modifiedFilter = new ArrayList<Filter>();
        Filter filter1 = null;
        Filter filter2 = null;
        if(originalFilters.size() == 1) {
            filter1 = originalFilters.get(0);

            if(filter1.getOperator().equals(SearchConstants.EQ)) {
                filter2 = new Filter(filter1.getProperty(),
                        filter1.getOperator(), filter1.getValue(), null);
            } else if(filter1.getOperator().equals(SearchConstants.LT) ||
                    filter1.getOperator().equals(SearchConstants.LE)) {
                filter2 = new Filter(filter1.getProperty(),
                        filter1.getOperator(), filter1.getValue(), null);
                filter1 = new Filter(filter1.getProperty(),
                        SearchConstants.GT, CassandraUtils.getMinValueString(attributeType) , OperationType.AND);
            } else {
                filter2 = new Filter(filter1.getProperty(),
                        SearchConstants.LT, CassandraUtils.getMaxValueString(attributeType), null);
            }
        } else if(originalFilters.size() == 2) {
            if(originalFilters.get(0).getOperator().equals(SearchConstants.GT) ||
                    originalFilters.get(0).getOperator().equals(SearchConstants.GE))  {
                filter1 = originalFilters.get(0);
                filter2 = originalFilters.get(1);
            } else {
                filter1 = originalFilters.get(1);
                filter2 = originalFilters.get(0);
            }
        }
        modifiedFilter.add(filter1);
        modifiedFilter.add(filter2);

        return modifiedFilter;
    }

    private Activity getActivity(Keyspace indexKeyspace, String activityID)
            throws CassandraSearchException {
        Activity activity = new Activity(activityID);

        SliceQuery<String, String, String> sliceQuery =
                HFactory.createSliceQuery(indexKeyspace, STRING_SERIALIZER, STRING_SERIALIZER,
                        STRING_SERIALIZER);
        sliceQuery.setColumnFamily(SearchConstants.GLOBAL_ACTIVITY_MONITORING_INDEX_CF).setKey(activityID);
        sliceQuery.setRange(null, null, false, Integer.MAX_VALUE);

        QueryResult<ColumnSlice<String,String>> result = sliceQuery.execute();

        //TODO handle if results are empty
        String startingColumn = result.get().getColumns().get(0).getName();
        String endingColumn   = result.get().getColumns().get(result.get().getColumns().size() - 1).getName();
        activity.setStartTime(Long.parseLong(startingColumn.substring(0, startingColumn.indexOf(':'))));
        activity.setEndTime(Long.parseLong(endingColumn.substring(0, startingColumn.indexOf(':'))));

        return activity;
    }

    private ActivityInfo getActivityInfo(Keyspace indexKeyspace, String activityID)
            throws CassandraSearchException {
        ActivityInfo activityInfo = new ActivityInfo(activityID);

        SliceQuery<String, String, String> sliceQuery =
                HFactory.createSliceQuery(indexKeyspace, STRING_SERIALIZER, STRING_SERIALIZER,
                        STRING_SERIALIZER);
        sliceQuery.setColumnFamily(SearchConstants.GLOBAL_ACTIVITY_MONITORING_INDEX_CF).setKey(activityID);
        sliceQuery.setRange(null, null, false, Integer.MAX_VALUE);

        QueryResult<ColumnSlice<String,String>> result = sliceQuery.execute();

        //TODO handle if results are empty
        String startingColumn = result.get().getColumns().get(0).getName();
        String endingColumn   = result.get().getColumns().get(result.get().getColumns().size() - 1).getName();
        activityInfo.setStartTime(getTimestampString
                (Long.parseLong(startingColumn.substring(0, startingColumn.indexOf(':')))));
        activityInfo.setEndTime(getTimestampString
                (Long.parseLong(endingColumn.substring(0, startingColumn.indexOf(':')))));

        return activityInfo;
    }

    private Set<String> getActivityIDsForRowKeys(Cluster cluster,
                                                 Keyspace keyspace,
                                                 String primaryCFName,
                                                 List<String> rowKeys)
            throws CassandraSearchException {
        CFInfo columnFamilyInfo = CassandraUtils.getColumnFamilyInfo(cluster, keyspace,
                primaryCFName);


        Set<String> activitySet = new LinkedHashSet<String>();

        SliceQuery<String, ByteBuffer, ByteBuffer> sliceQuery =
                HFactory.createSliceQuery(keyspace, STRING_SERIALIZER, BYTE_BUFFER_SERIALIZER,
                        BYTE_BUFFER_SERIALIZER);
        sliceQuery.setColumnFamily(primaryCFName);
        sliceQuery.setColumnNames(STRING_SERIALIZER.toByteBuffer(SearchConstants.BAM_ACTIVITY_ID));

        QueryResult<ColumnSlice<ByteBuffer, ByteBuffer>> result = null;

        for(String rowKey : rowKeys) {
            sliceQuery.setKey(rowKey);
            try {
                result = sliceQuery.execute();
            } catch (HectorException exception) {
                throw new CassandraSearchException(exception.getMessage(), exception);
            }
            List<HColumn<ByteBuffer, ByteBuffer>> hColumnsList = result.get().getColumns();
            for (HColumn<ByteBuffer, ByteBuffer> hColumn : hColumnsList) {
                String columnName = STRING_SERIALIZER.fromByteBuffer(hColumn.getNameBytes());

                if (columnName.equals(SearchConstants.BAM_ACTIVITY_ID)) {
                    activitySet.add(CassandraUtils.getStringDeserialization(columnFamilyInfo.
                            getColumnValueCassandraSerializer(hColumn.getNameBytes()), hColumn.getValueBytes()));
                }
            }

        }
        return activitySet;
    }

    private List<org.wso2.carbon.cassandra.search.data.Row> getRowsFromRowKeys(Cluster cluster,
                                                                               Keyspace keyspace,
                                                                               String primaryCFName,
                                                                               List<String> rowKeys)
            throws CassandraSearchException {
        CFInfo columnFamilyInfo = CassandraUtils.getColumnFamilyInfo(cluster, keyspace,
                primaryCFName);

        List<org.wso2.carbon.cassandra.search.data.Row> rowList =
                new ArrayList<org.wso2.carbon.cassandra.search.data.Row>();

        SliceQuery<String, ByteBuffer, ByteBuffer> sliceQuery =
                HFactory.createSliceQuery(keyspace, STRING_SERIALIZER, BYTE_BUFFER_SERIALIZER,
                        BYTE_BUFFER_SERIALIZER);
        sliceQuery.setColumnFamily(primaryCFName);
        sliceQuery.setColumnNames(STRING_SERIALIZER.toByteBuffer(SearchConstants.TIMESTAMP_PROPERTY),
                STRING_SERIALIZER.toByteBuffer(SearchConstants.NAME_PROPERTY),
                STRING_SERIALIZER.toByteBuffer(SearchConstants.VERSION_PROPERTY));

        QueryResult<ColumnSlice<ByteBuffer, ByteBuffer>> result = null;

        for(String rowKey : rowKeys) {
            org.wso2.carbon.cassandra.search.data.Row row =
                    new org.wso2.carbon.cassandra.search.data.Row();
            row.setRowId(rowKey);

            sliceQuery.setKey(rowKey);
            try {
                result = sliceQuery.execute();
            } catch (HectorException exception) {
                throw new CassandraSearchException(exception.getMessage(), exception);
            }
            List<HColumn<ByteBuffer, ByteBuffer>> hColumnsList = result.get().getColumns();
            for (HColumn<ByteBuffer, ByteBuffer> aHColumnsList : hColumnsList) {
                Column column = new Column();
                column.setName(cleanNonXmlChars(CassandraUtils.getStringDeserialization(
                        columnFamilyInfo.getColumnCassandraSerializer(), aHColumnsList.getNameBytes())));
                String value = CassandraUtils.getStringDeserialization(columnFamilyInfo.
                        getColumnValueCassandraSerializer(aHColumnsList.getNameBytes()), aHColumnsList.getValueBytes());
                column.setValue(cleanNonXmlChars(value));
                column.setTimeStamp(aHColumnsList.getClock());

                if (column.getName().equals(SearchConstants.TIMESTAMP_PROPERTY)) {
                    row.setTimestamp(Long.parseLong(column.getValue()));
                } else if (column.getName().equals(SearchConstants.NAME_PROPERTY)) {
                    row.setStream(column.getValue());
                } else if (column.getName().equals(SearchConstants.VERSION_PROPERTY)) {
                    row.setVersion(column.getValue());
                }
            }
            rowList.add(row);
        }
        return rowList;
    }

    private ActivityEvent getDetailedEventOfActivity(Cluster cluster,
                                                     Keyspace keyspace,
                                                     String primaryCFName,
                                                     String rowKey)
            throws CassandraSearchException {
        CFInfo columnFamilyInfo = CassandraUtils.getColumnFamilyInfo(cluster, keyspace,
                primaryCFName);

        ActivityEvent activityEvent = new ActivityEvent(rowKey);

        SliceQuery<String, ByteBuffer, ByteBuffer> sliceQuery =
                HFactory.createSliceQuery(keyspace, STRING_SERIALIZER, BYTE_BUFFER_SERIALIZER,
                        BYTE_BUFFER_SERIALIZER);
        sliceQuery.setColumnFamily(primaryCFName);
        sliceQuery.setRange(emptyByteBuffer, emptyByteBuffer, false, Integer.MAX_VALUE);
        sliceQuery.setKey(rowKey);

        QueryResult<ColumnSlice<ByteBuffer, ByteBuffer>> result = null;

        try {
            result = sliceQuery.execute();
        } catch (HectorException exception) {
            throw new CassandraSearchException(exception.getMessage(), exception);
        }
        List<HColumn<ByteBuffer, ByteBuffer>> hColumnsList = result.get().getColumns();
        for (HColumn<ByteBuffer, ByteBuffer> aHColumnsList : hColumnsList) {
            String columnName = cleanNonXmlChars(CassandraUtils.getStringDeserialization(
                    columnFamilyInfo.getColumnCassandraSerializer(), aHColumnsList.getNameBytes()));
            String value = cleanNonXmlChars(CassandraUtils.getStringDeserialization(columnFamilyInfo.
                    getColumnValueCassandraSerializer(aHColumnsList.getNameBytes()), aHColumnsList.getValueBytes()));

            if (columnName.equals(SearchConstants.TIMESTAMP_PROPERTY)) {
                activityEvent.setTimestamp(getTimestampString(Long.parseLong(value)));
            } else if (columnName.equals(SearchConstants.NAME_PROPERTY)) {
                activityEvent.setStream(value);
            } else if(columnName.equals(SearchConstants.VERSION_PROPERTY)) {
                activityEvent.setVersion(value);
            }
            activityEvent.addColumnValue(columnName, value);
        }

        return activityEvent;
    }

    private Map<String, String> getSelectedPropertyValuesForRow(Keyspace keyspace, String columnFamily,
                                                                String rowName, String property)
            throws CassandraSearchException {
        Map<String, String> valueMap = new HashMap<String, String>();
        Cluster cluster = ConnectionManager.getClusterFromSession();
        if (cluster == null) {
            throw new CassandraSearchException(SearchConstants.ERR_NO_CLUSTER_AVAILABLE);
        }
        CFInfo columnFamilyInfo = CassandraUtils.getColumnFamilyInfo(cluster, keyspace,
                columnFamily);

        //get the results up to the startingNo
        SliceQuery<String, String, ByteBuffer> sliceQuery =
                HFactory.createSliceQuery(keyspace, STRING_SERIALIZER, STRING_SERIALIZER,
                        BYTE_BUFFER_SERIALIZER);
        sliceQuery.setColumnFamily(columnFamily);
        sliceQuery.setKey(rowName);
        sliceQuery.setColumnNames(property, SearchConstants.TIMESTAMP_PROPERTY);

        QueryResult<ColumnSlice<String, ByteBuffer>> result;

        try {
            result = sliceQuery.execute();
        } catch (HectorException exception) {
            throw new CassandraSearchException(exception.getMessage(), exception);
        }

        List<HColumn<String, ByteBuffer>> hColumnsList;
        hColumnsList = result.get().getColumns();

        for (HColumn<String, ByteBuffer> hColumn : hColumnsList) {

            String key = hColumn.getName();
            String value = CassandraUtils.getStringDeserialization(columnFamilyInfo.
                    getColumnValueCassandraSerializer(hColumn.getNameBytes()), hColumn.getValueBytes());

            key = cleanNonXmlChars(key);
            value = cleanNonXmlChars(value);

            if(key.equals(property) || key.equals(SearchConstants.TIMESTAMP_PROPERTY)) {
                valueMap.put(key, value);
            }

        }
        return valueMap;
    }

    private Map<String, IndexDefinition> getAllIndexDefinitionsFromCassandra(Keyspace keyspace)
            throws CassandraSearchException {
        boolean incrementalIndex = false;
        Map<String, IndexDefinition> indexDefinitionMap = new HashMap<String, IndexDefinition>();

        RangeSlicesQuery<String, String, String> rangeSlicesQuery =
                HFactory.createRangeSlicesQuery(keyspace, STRING_SERIALIZER, STRING_SERIALIZER,
                        STRING_SERIALIZER);
        rangeSlicesQuery.setColumnFamily(SearchConstants.INDEX_DEF_CF);
        rangeSlicesQuery.setKeys("", "");
        rangeSlicesQuery.setColumnNames(SearchConstants.SECONDARY_INDEX_DEF,
                SearchConstants.CUSTOM_INDEX_DEF, SearchConstants.FIXED_SEARCH_DEF);
        rangeSlicesQuery.setRowCount(Integer.MAX_VALUE);

        QueryResult<OrderedRows<String, String, String>> result;

        try {
            result = rangeSlicesQuery.execute();
        } catch (HectorException exception) {
            throw new CassandraSearchException(exception.getMessage(), exception);
        }

        for (Row<String, String, String> cassandraRow : result.get().getList()) {
            StringBuilder indexSB = new StringBuilder();
            String secIndex  = "";
            String custIndex = "";
            String fixedIndex= "";
            String streamName = cassandraRow.getKey();

            List<HColumn<String, String>> hColumnsList = cassandraRow.
                    getColumnSlice().getColumns();
            for (HColumn<String, String> hColumn : hColumnsList) {
                String columnName = hColumn.getName();
                if(columnName.equals(SearchConstants.SECONDARY_INDEX_DEF)) {
                    secIndex = hColumn.getValue();
                } else if(columnName.equals(SearchConstants.CUSTOM_INDEX_DEF)) {
                    custIndex= hColumn.getValue();
                } else if(columnName.equals(SearchConstants.FIXED_SEARCH_DEF)) {
                    fixedIndex= hColumn.getValue();
                }
            }
            if(secIndex.isEmpty() && custIndex.isEmpty()) {
                continue;
            }

            IndexDefinition indexDefinition = new IndexDefinition();
            indexDefinition.setIndexDataFromStore(indexSB.append(secIndex).append("|").
                    append(custIndex).append("|").append(fixedIndex).append("|").append(incrementalIndex).toString());
            indexDefinitionMap.put(streamName, indexDefinition);
        }

        return indexDefinitionMap;
    }

    private IndexDefinition getIndexDefinition(Keyspace indexKeyspace, String streamName)
            throws CassandraSearchException {
        boolean incrementalIndex = false;
        StringBuilder indexSB = new StringBuilder();
        String secIndex  = "";
        String custIndex = "";
        String fixedIndex= "";

        SliceQuery<String, String, String> sliceQuery =
                HFactory.createSliceQuery(indexKeyspace, STRING_SERIALIZER, STRING_SERIALIZER,
                        STRING_SERIALIZER);
        sliceQuery.setColumnFamily(SearchConstants.INDEX_DEF_CF).setKey(streamName);
        sliceQuery.setColumnNames(SearchConstants.SECONDARY_INDEX_DEF,
                SearchConstants.CUSTOM_INDEX_DEF, SearchConstants.FIXED_SEARCH_DEF);

        QueryResult<ColumnSlice<String,String>> result = sliceQuery.execute();

        for (HColumn<String, String> column : result.get().getColumns()) {
            if(column.getName().equals(SearchConstants.SECONDARY_INDEX_DEF)) {
                secIndex = column.getValue();
            } else if(column.getName().equals(SearchConstants.CUSTOM_INDEX_DEF)) {
                custIndex= column.getValue();
            } else if(column.getName().equals(SearchConstants.FIXED_SEARCH_DEF)) {
                fixedIndex= column.getValue();
            }
        }

        if(secIndex.isEmpty() && custIndex.isEmpty()) {
            return null;
        }
        IndexDefinition indexDefinition = new IndexDefinition();
        indexDefinition.setIndexDataFromStore(indexSB.append(secIndex).append("|").
                append(custIndex).append("|").append(fixedIndex).append("|").append(incrementalIndex).toString());
        return indexDefinition;
    }

    private String cleanNonXmlChars(String value) {
        String parsedString = "";
        if (value != null) {
            parsedString = value.replaceAll("[\\x00-\\x09\\x0B\\x0C\\x0E-\\x1F\\x7F]", " ");
        }
        if (parsedString.trim().isEmpty()) {
            parsedString = "***Non displayable value***";
        }
        return parsedString;
    }

    public String getTimestampString(long val) throws CassandraSearchException {
        Date date = null;
        date = new Date(val);
        return eventTimeFormatter.format(date);
    }

    public long getTimestampFromString(String val) throws CassandraSearchException {
        Date date = null;

        try {
            date = queryTimeFormatter.parse(val);
        } catch (ParseException e) {
            throw new CassandraSearchException("Invalid timestamp format : " + e.getMessage());
        }

        return date.getTime();
    }

}

