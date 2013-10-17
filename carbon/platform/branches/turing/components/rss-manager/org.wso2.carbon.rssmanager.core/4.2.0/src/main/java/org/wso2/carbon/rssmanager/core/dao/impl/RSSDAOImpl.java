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
package org.wso2.carbon.rssmanager.core.dao.impl;

import org.wso2.carbon.rssmanager.core.config.RSSManagementRepository;
import org.wso2.carbon.rssmanager.core.dao.*;
import org.wso2.carbon.rssmanager.core.dao.util.EntityManager;
import org.wso2.carbon.rssmanager.core.environment.dao.DatabasePrivilegeTemplateDAO;
import org.wso2.carbon.rssmanager.core.environment.dao.impl.DatabasePrivilegeTemplateDAOImpl;


/**
 * DAO implementation for RSSDAO interface.
 */
public class RSSDAOImpl extends RSSDAO {

    public RSSDAOImpl(RSSManagementRepository repository, EntityManager entityManager) {
        super(repository, entityManager);
    }

    @Override
    public DatabaseDAO getDatabaseDAO() {
        return new DatabaseDAOImpl();
    }

    @Override
    public DatabaseUserDAO getDatabaseUserDAO() {
        return new DatabaseUserDAOImpl();
    }

    @Override
    public DatabasePrivilegeTemplateDAO getDatabasePrivilegeTemplateDAO() {
        return new DatabasePrivilegeTemplateDAOImpl();
    }

    @Override
    public UserDatabaseEntryDAO getUserDatabaseEntryDAO() {
        return new UserDatabaseEntryDAOImpl();
    }

}
