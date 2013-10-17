/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */
package org.wso2.carbon.appfactory.repository.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.core.TenantRepositoryManagerInitializer;

/**
 * Default implementation of {@link TenantRepositoryManagerInitializer}
 */
public class TenantRepositoryManagerInitializerImpl implements TenantRepositoryManagerInitializer {
    private static final Log log = LogFactory.getLog(TenantRepositoryManagerInitializerImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTenantCreation(String tenantDomain, String usagePlan) {
        log.info("**********************Initializing rep manager for " + tenantDomain + " with " + usagePlan + " *************");
    }
}
