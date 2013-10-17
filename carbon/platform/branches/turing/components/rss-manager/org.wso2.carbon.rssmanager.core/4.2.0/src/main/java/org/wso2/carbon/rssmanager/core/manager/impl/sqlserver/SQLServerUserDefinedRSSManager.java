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

package org.wso2.carbon.rssmanager.core.manager.impl.sqlserver;

import org.wso2.carbon.rssmanager.core.config.RSSConfig;
import org.wso2.carbon.rssmanager.core.entity.*;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.manager.UserDefinedRSSManager;

public class SQLServerUserDefinedRSSManager extends UserDefinedRSSManager {

    public SQLServerUserDefinedRSSManager(Environment environment, RSSConfig config) {
        super(environment, config);
    }


    public Database createDatabase(Database database) throws RSSManagerException {
        return null;  
    }

    public void dropDatabase(String rssInstanceName,
                             String databaseName) throws RSSManagerException {
        
    }

    public DatabaseUser createDatabaseUser(DatabaseUser user) throws RSSManagerException {
        return null;  
    }

    public void dropDatabaseUser(String rssInstanceName,
                                 String username) throws RSSManagerException {
        
    }

    public void attachUserToDatabase(UserDatabaseEntry ude,
                                     DatabasePrivilegeTemplate template) throws RSSManagerException {
        
    }

    public void detachUserFromDatabase(UserDatabaseEntry ude) throws RSSManagerException {
        
    }

    public void editDatabaseUserPrivileges(DatabasePrivilegeSet privileges, DatabaseUser user,
                                           String databaseName) throws RSSManagerException {
        
    }
    
}
