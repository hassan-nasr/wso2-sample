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

/**
 * Class to represent a database user.
 */
public class DatabaseUser {

    private int id;
	private String name;
	private String password;
	private String rssInstanceName;
    private String type;

	public DatabaseUser(String name, String password, String rssInstanceName, String type) {
		this.name = name;
		this.password = password;
		this.rssInstanceName = rssInstanceName;
        this.type = type;
	}

    public DatabaseUser(int id, String name, String password, String rssInstanceName, String type) {
        this.id = id;
		this.name = name;
		this.password = password;
		this.rssInstanceName = rssInstanceName;
        this.type = type;
	}

    public DatabaseUser() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRssInstanceName() {
        return rssInstanceName;
    }

    public void setRssInstanceName(String rssInstanceName) {
        this.rssInstanceName = rssInstanceName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
