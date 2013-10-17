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
package org.wso2.carbon.automation.api.clients.governance;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.api.clients.utils.AuthenticateStub;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceStub;

import java.rmi.RemoteException;

public class LifeCycleManagementClient {
    private static final Log log = LogFactory.getLog(LifeCycleManagementClient.class);

    private final String serviceName = "LifeCycleManagementService";
    private LifeCycleManagementServiceStub lifeCycleManagementServiceStub;

    public LifeCycleManagementClient(String backEndUrl, String sessionCookie) throws AxisFault {
        String endPoint = backEndUrl + serviceName;
        lifeCycleManagementServiceStub = new LifeCycleManagementServiceStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, lifeCycleManagementServiceStub);
    }

    public LifeCycleManagementClient(String backEndUrl, String userName, String password)
            throws AxisFault {
        String endPoint = backEndUrl + serviceName;
        lifeCycleManagementServiceStub = new LifeCycleManagementServiceStub(endPoint);
        AuthenticateStub.authenticateStub(userName, password, lifeCycleManagementServiceStub);
    }

    public boolean addLifeCycle(String lifeCycleConfiguration)
            throws LifeCycleManagementServiceExceptionException, RemoteException {

        return lifeCycleManagementServiceStub.createLifecycle(lifeCycleConfiguration);
    }

    public boolean editLifeCycle(String oldLifeCycleName,
                                 String lifeCycleConfiguration)
            throws LifeCycleManagementServiceExceptionException, RemoteException {
        return lifeCycleManagementServiceStub.updateLifecycle(oldLifeCycleName, lifeCycleConfiguration);
    }

    public boolean deleteLifeCycle(String lifeCycleName)
            throws LifeCycleManagementServiceExceptionException, RemoteException {
        return lifeCycleManagementServiceStub.deleteLifecycle(lifeCycleName);
    }

    public String[] getLifecycleList()
            throws LifeCycleManagementServiceExceptionException, RemoteException {
        return lifeCycleManagementServiceStub.getLifecycleList();
    }

    public String getLifecycleConfiguration(String lifeCycleName)
            throws LifeCycleManagementServiceExceptionException, RemoteException {
        return lifeCycleManagementServiceStub.getLifecycleConfiguration(lifeCycleName);
    }

    public boolean isLifecycleNameInUse(String lifeCycleName)
            throws LifeCycleManagementServiceExceptionException, RemoteException {
        return lifeCycleManagementServiceStub.isLifecycleNameInUse(lifeCycleName);
    }

    public boolean parseConfiguration(String configuration)
            throws RemoteException, LifeCycleManagementServiceExceptionException {
        try {
            return lifeCycleManagementServiceStub.parseConfiguration(configuration);
        } catch (RemoteException e) {
            log.error("Cannot parse the configuration : " + e.getMessage());
            throw new RemoteException("Cannot parse the configuration : ", e);
        } catch (LifeCycleManagementServiceExceptionException e) {
            log.error("Cannot parse the configuration : " + e.getMessage());
            throw new LifeCycleManagementServiceExceptionException(
                    "Cannot parse the configuration : ", e);
        }
    }

}
