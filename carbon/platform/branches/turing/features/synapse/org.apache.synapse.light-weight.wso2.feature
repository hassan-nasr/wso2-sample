<!--
 ~ Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
 <project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">

    <parent>
        <groupId>org.wso2.carbon</groupId>
        <artifactId>synapse-features</artifactId>
        <version>4.0.0</version>
    </parent>

    <modelVersion>4.0.0</modelVersion>
    <artifactId>org.apache.synapse.wso2.feature</artifactId>
    <packaging>pom</packaging>
    <name>WSO2 Carbon - Synapse Feature</name>
    <version>${synapse.version}</version>
    <url>http://wso2.org</url>
    <description>This feature contains the synapse core feature</description>

    <dependencies>
        <dependency>
            <groupId>org.apache.synapse</groupId>
            <artifactId>synapse-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.synapse</groupId>
            <artifactId>synapse-commons</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.synapse</groupId>
            <artifactId>synapse-tasks</artifactId>
        </dependency>
        <dependency>
            <groupId>net.sf.saxon.wso2</groupId>
            <artifactId>saxon</artifactId>
        </dependency>
        <dependency>
            <groupId>org.quartz-scheduler</groupId>
            <artifactId>quartz</artifactId>
        </dependency>
        <dependency>
	    <groupId>org.wso2.uri.template</groupId>
	    <artifactId>wso2-uri-templates</artifactId>
	    <version>1.0.0</version>
	</dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.wso2.maven</groupId>
                <artifactId>carbon-p2-plugin</artifactId>
                <version>${carbon.p2.plugin.version}</version>
                <executions>
                    <execution>
                        <id>4-p2-feature-generation</id>
                        <phase>package</phase>
                        <goals>
                            <goal>p2-feature-gen</goal>
                        </goals>
                        <configuration>
                            <id>org.apache.synapse.wso2</id>
                            <propertiesFile>../etc/feature.properties</propertiesFile>
                            <adviceFile>
                                <properties>
                                    <propertyDef>org.wso2.carbon.p2.category.type:server</propertyDef>
                                </properties>
                            </adviceFile>
                            <bundles>
                                <bundleDef>org.apache.synapse:synapse-core</bundleDef>
                                <bundleDef>org.apache.synapse:synapse-commons</bundleDef>
                                <bundleDef>org.apache.synapse:synapse-tasks</bundleDef>
                                <bundleDef>net.sf.saxon.wso2:saxon</bundleDef>
                                <bundleDef>org.quartz-scheduler.wso2:quartz</bundleDef>
                                <bundleDef>org.wso2.uri.template:wso2-uri-templates</bundleDef>
                            </bundles>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

</project>
