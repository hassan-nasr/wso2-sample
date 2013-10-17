/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.http.impl.nio.reactor;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

public class ExceptionEventTest {

    @Test
    public void testGetCause() {
        NullPointerException npe = new NullPointerException("npe");
        ExceptionEvent ee = new ExceptionEvent(npe);
        Assert.assertSame(npe, ee.getCause());
        ee = new ExceptionEvent(npe, new Date());
        Assert.assertSame(npe, ee.getCause());
    }

    @Test
    public void testGetTimestamp() {
        NullPointerException npe = new NullPointerException("npe");
        ExceptionEvent ee = new ExceptionEvent(npe);
        Assert.assertNotNull(ee.getTimestamp());
        ee = new ExceptionEvent(npe, new Date(1234567890L));
        Assert.assertEquals(new Date(1234567890L), ee.getTimestamp());
    }

    @Test
    public void testToString() {
        Assert.assertNotNull(new ExceptionEvent(new NullPointerException()));
    }

}
