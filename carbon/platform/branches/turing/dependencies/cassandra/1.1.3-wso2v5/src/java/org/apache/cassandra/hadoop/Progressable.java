package org.apache.cassandra.hadoop;

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

import org.apache.hadoop.mapreduce.TaskAttemptContext;


public class Progressable
{
    private TaskAttemptContext context;
    private org.apache.hadoop.util.Progressable progressable;

    Progressable(TaskAttemptContext context)
    {
        this.context = context;
    }

    Progressable(org.apache.hadoop.util.Progressable progressable)
    {
        this.progressable = progressable;
    }

    public void progress()
    {
        if (context != null)
            context.progress();
        else
            progressable.progress();
    }

}
