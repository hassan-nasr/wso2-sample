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
package org.wso2.carbon.automation.core.utils;

/**
 * class to store artifact dependencies
 */
public class ArtifactDependency {
    private String dependencyArtifactName;
    private String dependencyArtifactLocation;
    private ArtifactType dependencyArtifactType;

    public void setDepArtifactName(String dependencyArtifactName) {
        this.dependencyArtifactName = dependencyArtifactName;
    }

    public void setDepArtifactLocation(String dependencyArtifactLocation) {
        this.dependencyArtifactLocation = dependencyArtifactLocation;
    }

    public void setDepArtifactType(ArtifactType dependencyArtifactType) {
        this.dependencyArtifactType = dependencyArtifactType;
    }

    public String getDepArtifactName() {
        return dependencyArtifactName;
    }

    public String getDepArtifactLocation() {
        return dependencyArtifactLocation;
    }

    public ArtifactType getDepArtifactType() {
        return dependencyArtifactType;
    }
}
