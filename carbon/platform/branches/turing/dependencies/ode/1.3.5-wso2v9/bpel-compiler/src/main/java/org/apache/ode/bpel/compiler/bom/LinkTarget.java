/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.bpel.compiler.bom;

import org.w3c.dom.Element;

/**
 * A representation of a BPEL link target. A link target is a tuple that joins a
 * link decleration (by reference) and an activity (by context).
 */
public class LinkTarget extends BpelObject {
    public LinkTarget(Element el) {
        super(el);
        // TODO Auto-generated constructor stub
    }

    /**
     * Get the name of the refernced link.
     *
     * @return link name
     */
    public String getLinkName() {
        return getAttribute("linkName", null);
    }
}
