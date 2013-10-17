/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.business.messaging.salesforce.mediator.samples.test.factory.impl;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;

public class Sample1PayloadFactory extends LoginPayloadFactory{
    public OMElement createPayload(Object... params){
        OMElement payload = super.createPayload(params);

        OMFactory factory = OMAbstractFactory.getOMFactory();
		OMNamespace ns = factory.createOMNamespace("urn:sobject.enterprise.soap.sforce.com",
				"ns1");
        OMElement extElement = factory.createOMElement("USER" ,ns);
        extElement.setText("SAMPLE USER");
        payload.addChild(extElement);
        return payload;
    }
}
