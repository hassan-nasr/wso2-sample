/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.rssmanager.core.config.node.allocation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NodeAllocationStrategyFactory {

    public enum NodeAllocationStrategyTypes {
        ROUND_ROBIN
    }

    private static final Log log = LogFactory.getLog(NodeAllocationStrategyFactory.class);

    public static  NodeAllocationStrategy getNodeAllocationStrategy(
            NodeAllocationStrategyTypes type) {
        switch (type) {
            case ROUND_ROBIN:
                return new RoundRobinNodeAllocationStrategy();
            default:
                if (log.isDebugEnabled()) {
                    log.debug("Unsupported node allocation strategy type defined. Falling back " +
                            "to 'Round Robin' node allocation strategy, which is the default");
                }
                return new RoundRobinNodeAllocationStrategy();
        }
    }

}
