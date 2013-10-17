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

package org.wso2.carbon.rssmanager.core.entity;

import org.wso2.carbon.rssmanager.core.config.datasource.RDBMSConfig;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class to represent an RSS Server Instance.
 */
@XmlRootElement(name = "RSSInstance")
public class RSSInstance {

    private int id;
    private String environmentName;
    private String name;
    private String dbmsType;
    private String instanceType;
    private String serverCategory;
    private RDBMSConfig dataSourceConfig;

    public RSSInstance(int id, String name, String dbmsType, String instanceType,
                       String serverCategory, RDBMSConfig dataSourceConfig, String environmentName) {
        this.id = id;
        this.name = name;
        this.dbmsType = dbmsType;
        this.instanceType = instanceType;
        this.serverCategory = serverCategory;
        this.dataSourceConfig = dataSourceConfig;
        this.environmentName = environmentName;
    }

    public RSSInstance() {}

    @XmlElement(name = "Name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement (name = "DbmsType")
    public String getDbmsType() {
        return dbmsType;
    }

    public void setDbmsType(String dbmsType) {
        this.dbmsType = dbmsType;
    }

    @XmlElement (name = "InstanceType")
    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    @XmlElement(name = "ServerCategory")
    public String getServerCategory() {
        return serverCategory;
    }

    public void setServerCategory(String serverCategory) {
        this.serverCategory = serverCategory;
    }

    @XmlElement (name = "Definition")
    public RDBMSConfig getDataSourceConfig() {
        return dataSourceConfig;
    }

    public void setDataSourceConfig(RDBMSConfig dataSourceConfig) {
        this.dataSourceConfig = dataSourceConfig;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName() {
        this.environmentName = environmentName;
    }

}
