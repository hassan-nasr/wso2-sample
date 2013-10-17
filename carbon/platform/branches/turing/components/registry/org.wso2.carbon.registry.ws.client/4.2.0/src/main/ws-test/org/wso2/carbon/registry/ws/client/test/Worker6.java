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

package org.wso2.carbon.registry.ws.client.test;

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

public class Worker6 extends Worker {

    public Worker6(String threadName, int iterations, WSRegistryServiceClient registry) {
        super(threadName, iterations, registry);
    }

    // run this with 1 user and 1 iteration
    public void run() {

        long time1 = System.nanoTime();
        long timePerThread = 0;

        try {
            Resource resource1 = registry.newResource();
            resource1.setContent("ABC");
            for (int i = 0; i < 400; i++) {
                registry.put("/t1/" + i, resource1);
            }
            registry.rename("/t1", "/" + threadName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
    