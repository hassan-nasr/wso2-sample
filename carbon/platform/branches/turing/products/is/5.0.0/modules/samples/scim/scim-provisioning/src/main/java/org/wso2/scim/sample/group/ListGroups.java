/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.wso2.scim.sample.group;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.wso2.charon.core.schema.SCIMConstants;
import org.wso2.scim.sample.utils.SCIMSamplesUtils;

import java.io.IOException;

public class ListGroups {
    public static void main(String[] args) {
        try {
            SCIMSamplesUtils.loadConfiguration();
            //set the keystore
            SCIMSamplesUtils.setKeyStore();
            //create http client
            HttpClient httpListGroupClient = new HttpClient();
            //create get method for filtering
            GetMethod getMethod = new GetMethod(SCIMSamplesUtils.groupEndpointURL);
            //add authorization header
            String authHeader = SCIMSamplesUtils.getAuthorizationHeader();
            getMethod.addRequestHeader(SCIMConstants.AUTHORIZATION_HEADER, authHeader);

            int responseCode = httpListGroupClient.executeMethod(getMethod);
            String response = getMethod.getResponseBodyAsString();

            System.out.println("");
            System.out.println("/******SCIM: list groups response status: " + responseCode);
            System.out.println("SCIM: list groups response data: " + response + "******/");
            System.out.println("");

        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
}
