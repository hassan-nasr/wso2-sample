/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.appfactory.integration.ui;

public class AppCredentialsGenerator {

    private static AppCredentialsGenerator value = new AppCredentialsGenerator();
    private static String appKey;
    private static String appName;
    private static String dbName;

    private AppCredentialsGenerator() {
    }

    public static AppCredentialsGenerator getInstance() {
        return value;
    }

    public static void setAppKey(String Key) {
        appKey = Key;
    }

    public static void setAppName(String name) {
        appName = name;
    }

    public static void setDbName(String database) {
        dbName = database;
    }

    public static String getAppKey() {
        return appKey;
    }

    public static String getAppName() {
        return appName;
    }
    public static String getDbName() {
        return dbName;
    }
}



