/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.commons.evaluators.source;

import org.apache.synapse.commons.evaluators.EvaluatorContext;
import org.apache.synapse.commons.evaluators.EvaluatorException;

public class PropertyTextRetriever implements SourceTextRetriever {

    private String source;

    public PropertyTextRetriever(String source) {
        this.source = source;
    }

    public String getSourceText(EvaluatorContext context) throws EvaluatorException {
        Object value = context.getProperty(source);
        if (value != null) {
            return value.toString();
        }
        return null;
    }

    public String getSource() {
        return source;
    }
}
