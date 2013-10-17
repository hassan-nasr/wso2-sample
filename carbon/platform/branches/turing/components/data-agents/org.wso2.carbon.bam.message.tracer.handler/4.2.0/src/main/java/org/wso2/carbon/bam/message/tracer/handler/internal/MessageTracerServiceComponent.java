/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.message.tracer.handler.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.bam.message.tracer.handler.conf.RegistryPersistenceManager;
import org.wso2.carbon.bam.message.tracer.handler.util.MessageTracerConstants;
import org.wso2.carbon.bam.message.tracer.handler.util.ServiceHolder;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="org.wso2.carbon.bam.message.tracer.handler " immediate="true"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 * @scr.reference name="org.wso2.carbon.registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService"
 * unbind="unsetRegistryService"
 */

public class MessageTracerServiceComponent {

    private static Log log = LogFactory.getLog(MessageTracerServiceComponent.class);

    private static ConfigurationContext configurationContext;

    protected void activate(ComponentContext context) {

        if (log.isDebugEnabled()) {
            log.debug("BAM message tracer handler bundle is activated");
        }
        try {
            // Engaging MessageTracerModule as a global module
            configurationContext.getAxisConfiguration().engageModule(
                    MessageTracerConstants.BAM_SERVICE_MESSAGE_TRACER_MODULE_NAME);
            BundleContext bundleContext = context.getBundleContext();
            bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(),
                                          new MessageTracerAxis2ConfigurationContextObserver(), null);

            new RegistryPersistenceManager().load(CarbonContext.getThreadLocalCarbonContext().getTenantId());

        } catch (AxisFault axisFault) {
            if (log.isErrorEnabled()) {
                log.error("Failed to activate BAM message tracer handler bundle", axisFault);
            }
        } catch (Throwable t) {
            log.error("Failed to activate BAM message tracer handler bundle", t);
        }
    }

    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("BAM message tracer handler bundle is deactivated");
        }
    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        configurationContext = configurationContextService.getServerConfigContext();
        ServiceHolder.setConfigurationContextService(configurationContextService);

    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        configurationContext = null;
        ServiceHolder.setConfigurationContextService(null);
    }

    protected void setRegistryService(RegistryService registryService) {
        ServiceHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        ServiceHolder.setRegistryService(null);
    }
}
