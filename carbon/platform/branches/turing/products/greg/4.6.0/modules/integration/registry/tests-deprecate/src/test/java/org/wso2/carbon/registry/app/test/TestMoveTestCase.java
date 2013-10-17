/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.app.test;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import static org.testng.Assert.*;

/**
 * A test case which tests registry move operations
 */

public class TestMoveTestCase {
    public RemoteRegistry registry;

    @BeforeClass(groups = {"wso2.greg"})
    public void init() {
        InitializeAPI initializeAPI = new InitializeAPI();
        registry = initializeAPI.getRegistry(FrameworkSettings.CARBON_HOME, FrameworkSettings.HTTPS_PORT, FrameworkSettings.HTTP_PORT);
    }

    @Test(groups = {"wso2.greg"})
    public void ResourceMoveFromRootTest() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setProperty("test", "move");
        r1.setContent("c");
        registry.put("/move1", r1);

        Collection c1 = registry.newCollection();
        registry.put("/test/move", c1);

        registry.move("/move1", "/test/move/move1");

        Resource newR1 = registry.get("/test/move/move1");
        assertEquals(newR1.getProperty("test"), "move",
                "Moved resource should have a property named 'test' with value 'move'.");

        boolean failed = false;
        try {
            Resource oldR1 = registry.get("/move1");
        } catch (RegistryException e) {
            failed = true;
        }
        assertTrue(failed, "Moved resource should not be accessible from the old path.");
    }

    @Test(groups = {"wso2.greg"})
    public void ResourceMoveToRootTest() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setProperty("test", "move");
        r1.setContent("c");
        registry.put("/test/move/move2", r1);

        registry.move("/test/move/move2", "/move2");

        Resource newR1 = registry.get("/move2");
        assertEquals(newR1.getProperty("test"), "move",
                "Moved resource should have a property named 'test' with value 'move'.");

        boolean failed = false;
        try {
            Resource oldR1 = registry.get("/test/move/move2");
        } catch (RegistryException e) {
            failed = true;
        }
        assertTrue(failed, "Moved resource should not be accessible from the old path.");
    }

    @Test(groups = {"wso2.greg"})
    public void GeneralResourceMoveTest() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setProperty("test", "move");
        r1.setContent("c");
        registry.put("/test/c1/move/move3", r1);

        Collection c2 = registry.newCollection();
        registry.put("/test/c2/move", c2);

        registry.move("/test/c1/move/move3", "/test/c2/move/move3");

        Resource newR1 = registry.get("/test/c2/move/move3");
        assertEquals(newR1.getProperty("test"), "move",
                "Moved resource should have a property named 'test' with value 'move'.");

        boolean failed = false;
        try {
            Resource oldR1 = registry.get("/test/c1/move/move3");
        } catch (RegistryException e) {
            failed = true;
        }
        assertTrue(failed, "Moved resource should not be accessible from the old path.");
    }

    @Test(groups = {"wso2.greg"})
    public void GeneralCollectionMoveTest() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setProperty("test", "move");
        r1.setContent("c");
        registry.put("/test/c1/move5/move/dummy", r1);

        Collection c2 = registry.newCollection();
        registry.put("/test/c3", c2);

        registry.move("/test/c1/move5", "/test/c3/move5");

        Resource newR1 = registry.get("/test/c3/move5/move/dummy");
        assertEquals(newR1.getProperty("test"), "move",
                "Moved resource should have a property named 'test' with value 'move'.");

        boolean failed = false;
        try {
            Resource oldR1 = registry.get("/test/c1/move5/move/dummy");
        } catch (RegistryException e) {
            failed = true;
        }
        assertTrue(failed, "Moved resource should not be accessible from the old path.");
    }


}
