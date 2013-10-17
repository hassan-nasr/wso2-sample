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
package org.jclouds.vcloud;

import org.jclouds.vcloud.domain.Task;

/**
 * 
 * @author Adrian Cole
 * 
 */
public class TaskInErrorStateException extends RuntimeException {

   private static final long serialVersionUID = 1L;

   private final Task task;

   public TaskInErrorStateException(Task task) {
      super("error on task: " + task + " error: " + task.getError());
      this.task = task;
   }

   public Task getTask() {
      return task;
   }

}
