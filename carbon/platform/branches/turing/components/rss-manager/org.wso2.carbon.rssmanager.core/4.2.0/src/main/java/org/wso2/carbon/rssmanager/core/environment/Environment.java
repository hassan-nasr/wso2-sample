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

package org.wso2.carbon.rssmanager.core.environment;

import org.wso2.carbon.rssmanager.core.RSSInstanceDSWrapperRepository;
import org.wso2.carbon.rssmanager.core.config.node.allocation.NodeAllocationStrategy;
import org.wso2.carbon.rssmanager.core.config.node.allocation.NodeAllocationStrategyFactory;
import org.wso2.carbon.rssmanager.core.entity.RSSInstance;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.manager.adaptor.RSSManagerAdaptor;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement(name = "Environment")
public class Environment {

    private int id;
    private String name;
    private RSSInstance[] rssInstances;
    //TODO directly populate the map if possible
    private Map<String, RSSInstance> rssInstanceMap = new HashMap<String, RSSInstance>();
    private RSSInstanceDSWrapperRepository repository;
    private RSSManagerAdaptor adaptor;
    private NodeAllocationStrategy nodeAllocStrategy;
    private String nodeAllocationStrategyType;

    public synchronized void init(RSSManagerAdaptor adaptor) throws RSSManagerException {
        this.adaptor = adaptor;
        this.repository = new RSSInstanceDSWrapperRepository(this.getRSSInstances());
        this.rssInstanceMap = RSSManagerUtil.getRSSInstanceMap(this.getRSSInstances());
        NodeAllocationStrategyFactory.NodeAllocationStrategyTypes type =
                NodeAllocationStrategyFactory.NodeAllocationStrategyTypes.valueOf(
                        this.getNodeAllocationStrategyType());
        this.nodeAllocStrategy = NodeAllocationStrategyFactory.getNodeAllocationStrategy(type);
    }

    @XmlElement(name = "Name", nillable = false, required = true)
    public String getName() {
        return name;
    }

    @XmlElementWrapper(name = "RSSInstances", nillable = false)
    @XmlElement(name = "RSSInstance", nillable = false)
    public RSSInstance[] getRSSInstances() {
        return rssInstances;
    }

    @XmlElement(name = "NodeAllocationStrategy", nillable = false)
    private String getNodeAllocationStrategyType() {
        return nodeAllocationStrategyType;
    }

    private void setNodeAllocationStrategyType(String nodeAllocationStrategyType) {
        this.nodeAllocationStrategyType = nodeAllocationStrategyType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRSSInstances(RSSInstance[] rssInstances) {
        this.rssInstances = rssInstances;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Environment)) {
            return false;
        }
        Environment environment = (Environment) o;
        return this.getName().equals(environment.getName());
    }

    @Override
    public int hashCode() {
        assert false : "hashCode() is not implemented";
        return -1;
    }

    public RSSInstanceDSWrapperRepository getDSWrapperRepository() {
        return repository;
    }

    private Map<String, RSSInstance> getRSSInstanceMap() {
        return rssInstanceMap;
    }

    public RSSManagerAdaptor getRSSManagerAdaptor() {
        if (adaptor == null) {
            /* The synchronize block is added to prevent a concurrent thread trying to access the
            RSS manager while it is being initialized. */
            synchronized (this) {
                return adaptor;
            }
        }
        return adaptor;
    }

    public RSSInstance getRSSInstance(String rssInstanceName) {
        return rssInstanceMap.get(rssInstanceName);
    }

    public RSSInstance getNextAllocatedNode() throws RSSManagerException {
        return nodeAllocStrategy.getNextAllocatedNode();
    }

}