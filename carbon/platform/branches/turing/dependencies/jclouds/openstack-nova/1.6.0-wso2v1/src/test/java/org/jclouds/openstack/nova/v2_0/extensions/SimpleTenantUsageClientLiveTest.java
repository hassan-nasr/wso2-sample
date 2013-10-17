/**
 * Licensed to jclouds, Inc. (jclouds) under one or more
 * contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  jclouds licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jclouds.openstack.nova.v2_0.extensions;

import static org.testng.Assert.assertNotNull;

import java.util.Set;

import org.jclouds.openstack.nova.v2_0.domain.SimpleTenantUsage;
import org.jclouds.openstack.nova.v2_0.extensions.SimpleTenantUsageClient;
import org.jclouds.openstack.nova.v2_0.internal.BaseNovaClientLiveTest;
import org.testng.annotations.Test;

import com.google.common.base.Optional;

/**
 * Tests behavior of SimpleTenantUsageClient
 *
 * @author Adam Lowe
 */
@Test(groups = "live", testName = "SimpleTenantUsageClientLiveTest")
public class SimpleTenantUsageClientLiveTest extends BaseNovaClientLiveTest {

   public void testList() throws Exception {
      for (String zoneId : novaContext.getApi().getConfiguredZones()) {
         Optional<SimpleTenantUsageClient> optClient = novaContext.getApi().getSimpleTenantUsageExtensionForZone(zoneId);
         if (optClient.isPresent() && identity.endsWith(":admin")) {
            SimpleTenantUsageClient client = optClient.get();
            Set<SimpleTenantUsage> usages = client.listTenantUsages();
            assertNotNull(usages);
            for (SimpleTenantUsage usage : usages) {
               SimpleTenantUsage details = client.getTenantUsage(usage.getTenantId());
               assertNotNull(details);
            }
         }
      }
   }
}
