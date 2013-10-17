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
package org.wso2.carbon.mediator.respond;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;


public class RespondMediator extends AbstractMediator {

    public String getTagLocalName() {
        return "respond";   
    }

    public OMElement serialize(OMElement parent) {
        OMElement respond = fac.createOMElement("respond", synNS);
        saveTracingState(respond, this);

        if (parent != null) {
            parent.addChild(respond);
        }
        return respond;
    }

    public void build(OMElement elem) {
        processAuditStatus(this, elem);        
    }
}
