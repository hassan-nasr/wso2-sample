/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.identity.oauth.endpoint.user.impl;

import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.utils.JSONUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONException;
import org.wso2.carbon.identity.oauth.endpoint.user.UserInfoClaimRetriever;
import org.wso2.carbon.identity.oauth.endpoint.user.UserInfoEndpointException;
import org.wso2.carbon.identity.oauth.endpoint.user.UserInfoResponseBuilder;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationResponseDTO;

import java.util.Map;

/**
 * 
 */
public class UserInfoJSONResponseBuilder implements UserInfoResponseBuilder {

	Log log = LogFactory.getLog(UserInfoJSONResponseBuilder.class);

	public String getResponseString(OAuth2TokenValidationResponseDTO tokenResponse)
	                                                                               throws UserInfoEndpointException,
	                                                                               OAuthSystemException {
		
		UserInfoClaimRetriever retriever = UserInfoEndpointConfig.getInstance().getUserInfoClaimRetriever();
		Map<String, Object> claims = retriever.getClaimsMap(tokenResponse);
		try {
			return JSONUtils.buildJSON(claims);
		} catch (JSONException e) {
			throw new UserInfoEndpointException("Error while generating the response JSON");
		}
	}

}
