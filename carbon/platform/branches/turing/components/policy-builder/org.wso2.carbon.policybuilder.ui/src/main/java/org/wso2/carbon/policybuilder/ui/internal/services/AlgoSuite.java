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
package org.wso2.carbon.policybuilder.ui.internal.services;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Nov 26, 2008
 * Time: 11:19:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class AlgoSuite {

	private String suiteName;
	private int priority;


	public AlgoSuite(String suiteName, int priority) {
		this.suiteName = suiteName;
		this.priority = priority;
	}

	public String getSuite() {
		return suiteName;
	}

	public int getPriority() {
		return priority;
	}
}
