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

package org.wso2.carbon.cassandra.search.engine;

import org.wso2.carbon.cassandra.search.exception.CassandraSearchException;
import org.wso2.carbon.cassandra.search.utils.OperationType;
import org.wso2.carbon.cassandra.search.utils.SearchConstants;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.IndexDefinition;

import java.util.*;

public final class SearchQuery {
    private List<String> mandatoryProperties= null;
    private List<String> searchProperties   = null;
    private Map<String, List<Filter>> allFiltersMap = null;
    private List<Filter> timestampFilter    = null;
    private List<List<Filter>> subFilters = new ArrayList<List<Filter>>();
    private boolean isMultiQuery   = false;
    private boolean hasNonFixProps = false;
    private IndexDefinition indexDefinition = null;
    private OperationType joinOp = OperationType.AND;

    public SearchQuery() {
        allFiltersMap    = new LinkedHashMap<String, List<Filter>>();
        searchProperties = new ArrayList<String>();
    }

    public SearchQuery(List<Attribute> fixedFilters) {
        setMandatoryProperties(fixedFilters);
        allFiltersMap = new LinkedHashMap<String, List<Filter>>();
        searchProperties = new ArrayList<String>();
    }

    public SearchQuery(IndexDefinition indexDefinition) {
        this.indexDefinition = indexDefinition;
        setMandatoryProperties(indexDefinition.getFixedSearchData());
        allFiltersMap = new LinkedHashMap<String, List<Filter>>();
        searchProperties = new ArrayList<String>();
    }

    //Stream1$Version#=#1.0.0|AND|Timestamp#>#20130901000000|AND|Timestamp#<#20130931000000|AND|tenant_id#=#50~Stream2$Version#=#1.0.0|AND|tenant_id#=#50
    public void buildQuery(String filter) throws CassandraSearchException {
        String[] conditions = filter.split("\\|");
        try {
            for (int i = 0; i < conditions.length; i++) {
                OperationType joinOperation = null;

                if (i != 0) {
                    try {
                        joinOperation = OperationType.valueOf(conditions[i++]);
                    } catch (Exception e) {
                        --i;
                    }
                }

                String fragment = conditions[i];
                String[] tokens = fragment.split("#");
                String criteria = tokens[0];
                String operator = tokens[1];
                String value    = tokens[2];

                Filter searchFilter = new Filter(criteria, operator, value, joinOperation);

                List<Filter> filtersList = null;
                if(!allFiltersMap.containsKey(criteria)) {
                    filtersList = new ArrayList<Filter>();
                    filtersList.add(searchFilter);
                    allFiltersMap.put(criteria, filtersList);
                } else {
                    allFiltersMap.get(criteria).add(searchFilter);
                }

                if(mandatoryProperties == null) {
                    searchProperties.add(criteria);
                } else {
                    if(!(mandatoryProperties.contains(criteria)
                            || criteria.equals(SearchConstants.TIMESTAMP_PROPERTY)
                            || searchProperties.contains(criteria))) {
                        searchProperties.add(criteria);
                    }
                }
            }
        } catch (Exception e) {
            throw new CassandraSearchException("Unable to extractFilters the search query: " + e.getMessage());
        }
    }

    public void buildQuery(List<Filter> filterList) throws CassandraSearchException {
        try {
            for (Filter searchFilter : filterList) {
                String property      = searchFilter.getProperty();

                List<Filter> filtersList = null;
                if(!allFiltersMap.containsKey(property)) {
                    filtersList = new ArrayList<Filter>();
                    filtersList.add(searchFilter);
                    allFiltersMap.put(property, filtersList);
                } else {
                    allFiltersMap.get(property).add(searchFilter);
                }

                if(mandatoryProperties == null) {
                    if (!(property.equals(SearchConstants.TIMESTAMP_PROPERTY)
                            || searchProperties.contains(property))) {
                        searchProperties.add(property);
                    }
                } else {
                    if(!(mandatoryProperties.contains(property)
                            || property.equals(SearchConstants.TIMESTAMP_PROPERTY)
                            || searchProperties.contains(property))) {
                        searchProperties.add(property);
                    }
                }
            }
        } catch (Exception e) {
            throw new CassandraSearchException("Unable to extractFilters the search query: " + e.getMessage());
        }
    }

    public void setMandatoryProperties(List<Attribute> fixedFilters) {
        if(fixedFilters == null) {
            return;
        }
        mandatoryProperties = new ArrayList<String>();
        for(Attribute attribute : fixedFilters) {
            mandatoryProperties.add(attribute.getName());
        }
    }

    public boolean isValidQuery() {
        if(mandatoryProperties == null) {
            return !searchProperties.isEmpty();
        }

        for(String filterProperty : mandatoryProperties) {
            if(!allFiltersMap.containsKey(filterProperty)) {
                return false;
            }
        }

        return true;
    }

    public void organizeSearchFilters() {
        timestampFilter = allFiltersMap.remove(SearchConstants.TIMESTAMP_PROPERTY);

        if(!searchProperties.isEmpty()) {
            hasNonFixProps = true;
        }

        if(!hasNonFixProps) {
            String lastMandatoryProp = mandatoryProperties.get(mandatoryProperties.size() -1);
            if(isMultiPropFilter(allFiltersMap.get(lastMandatoryProp))) {
                isMultiQuery = true;
                addSubFiltersForSingleFilter(allFiltersMap.get(lastMandatoryProp));
            }
        } else {
            if(searchProperties.size() == 1) {
                String property = searchProperties.get(0);
                if(isMultiPropFilter(allFiltersMap.get(property))) {
                    isMultiQuery = true;
                    addSubFiltersForSingleFilter(allFiltersMap.get(property));
                }
            } else {
                isMultiQuery = true;
                constructSubFiltersForSearchFilters();
            }
        }

        //Check whether Non fixed property has note been indexed. So Timestamp may be indexed
        if(!hasNonFixProps && timestampFilter != null) {
            if(indexDefinition.getAttributeTypeforProperty
                    (mandatoryProperties.get(mandatoryProperties.size() -1)) == null) {
                hasNonFixProps = true;
                searchProperties.add(SearchConstants.TIMESTAMP_PROPERTY);
                allFiltersMap.put(SearchConstants.TIMESTAMP_PROPERTY, timestampFilter);
                if(isMultiPropFilter(timestampFilter)) {
                    isMultiQuery = true;
                    addSubFiltersForSingleFilter(timestampFilter);
                }
                //Timestamp is already added as non fixed index property.
                timestampFilter = null;
            }
        }
    }

    public void constructSubFiltersForSearchFilters() {
        for(String searchProp : searchProperties) {
            List<Filter> filters = allFiltersMap.get(searchProp);
            if(!isMultiPropFilter(filters)) {
                subFilters.add(filters);
            } else {
                addSubFiltersForSingleFilter(filters);
            }
        }
    }

    public void addSubFiltersForSingleFilter(List<Filter> filterList) {
        for(Filter filter : filterList) {
            List<Filter> filters = new ArrayList<Filter>();
            filters.add(filter);
            subFilters.add(filters);
        }
    }

    public boolean isMultiPropFilter(List<Filter> criteriaList) {
        if(criteriaList.size() > 1) {
            for(Filter filter : criteriaList) {
                if(filter.getJoinOp().equals(OperationType.OR)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isMultipleFilterQuery() {
        if(searchProperties.size() > 1) {
            return true;
        } else if(searchProperties.size() == 1)  {
            List<Filter> criteriaList = allFiltersMap.get(searchProperties.get(0));
            if(criteriaList.size() > 1 && criteriaList.get(1).getJoinOp().equals(OperationType.OR)) {
                for(Filter filter : criteriaList) {
                    if(filter.getJoinOp().equals(OperationType.OR)) {
                        return true;
                    }
                }
            }

        }
        return false;
    }

    public Map<String, List<Filter>> getAllFiltersMap() {
        return allFiltersMap;
    }

    public List<String> getMandatoryProperties() {
        return mandatoryProperties;
    }

    public List<String> getSearchProperties() {
        return searchProperties;
    }

    public boolean isMultiQuery() {
        return isMultiQuery;
    }

    public void setMultiQuery(boolean multiQuery) {
        isMultiQuery = multiQuery;
    }

    public boolean isHasNonFixProps() {
        return hasNonFixProps;
    }

    public void setHasNonFixProps(boolean hasNonFixProps) {
        this.hasNonFixProps = hasNonFixProps;
    }

    public List<Filter> getTimestampFilter() {
        return timestampFilter;
    }

    public List<List<Filter>> getSubFilters() {
        return subFilters;
    }

    public OperationType getJoinOp() {
        return joinOp;
    }

    public void setJoinOp(OperationType joinOp) {
        this.joinOp = joinOp;
    }
}
