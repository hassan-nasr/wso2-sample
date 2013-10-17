package org.apache.cassandra.service;
/*
 *
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
 *
 */


import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.cassandra.concurrent.Stage;
import org.apache.cassandra.concurrent.StageManager;
import org.apache.cassandra.net.IAsyncCallback;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.utils.WrappedRunnable;

public class AsyncRepairCallback implements IAsyncCallback
{
    private final RowRepairResolver repairResolver;
    private final int blockfor;
    protected final AtomicInteger received = new AtomicInteger(0);

    public AsyncRepairCallback(RowRepairResolver repairResolver, int blockfor)
    {
        this.repairResolver = repairResolver;
        this.blockfor = blockfor;
    }

    public void response(Message message)
    {
        repairResolver.preprocess(message);
        if (received.incrementAndGet() == blockfor)
        {
            StageManager.getStage(Stage.READ_REPAIR).execute(new WrappedRunnable()
            {
                protected void runMayThrow() throws DigestMismatchException, IOException
                {
                    repairResolver.resolve();
                }
            });
        }
    }

    public boolean isLatencyForSnitch()
    {
        return true;
    }
}
