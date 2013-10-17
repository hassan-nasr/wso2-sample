/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.mediation.statistics;

import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.apache.synapse.core.SynapseEnvironment;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * Keeps references to various system services such as the Registry service.
 * References should be initialized and managed by the corresponding service
 * component implementation.
 */
public class ServiceReferenceHolder {   
    private RegistryService registrySvc;

    private static final ServiceReferenceHolder INSTANCE = new ServiceReferenceHolder();

    private ServiceReferenceHolder() {

    }

    public static ServiceReferenceHolder getInstance() {
        return INSTANCE;
    }

    public Registry getGovernanceRegistry() throws RegistryException {
        if (registrySvc != null) {
            return registrySvc.getGovernanceSystemRegistry();
        }
        return null;
    }

    public void setRegistrySvc(RegistryService registrySvc) {
        this.registrySvc = registrySvc;
    }

    public RegistryService getRegistryService() {
        return registrySvc;
    }
}