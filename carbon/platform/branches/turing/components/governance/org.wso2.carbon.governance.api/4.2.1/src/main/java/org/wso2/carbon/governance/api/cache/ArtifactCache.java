/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.governance.api.cache;

import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;

public class ArtifactCache {

    private LRUCache<String, GovernanceArtifact> artifacts;
    
    public ArtifactCache() {
        this.artifacts = new LRUCache<String, GovernanceArtifact>(10000);
    }

    public void invalidateCache() {
        artifacts.clear();
    }

    public void invalidateArtifact(String key) {
        artifacts.remove(key);
    }

    public void addArtifact(String key, GovernanceArtifact value) {
        artifacts.put(key, value);
    }

    public GovernanceArtifact getArtifact(String key) {
        return artifacts.get(key);
    }

}
