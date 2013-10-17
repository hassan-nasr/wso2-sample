/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package org.wso2.carbon.appfactory.userstore.service;

import org.wso2.carbon.appfactory.userstore.UserClaimStore;
import org.wso2.carbon.appfactory.userstore.internal.RegistryBasedUserClaimStore;
import org.wso2.carbon.user.api.Claim;

public class UserClaimStoreService {
    UserClaimStore userClaimStore = new RegistryBasedUserClaimStore();

    public void addUserClaims(String username, Claim[] claims) throws Exception {
        userClaimStore.addUserClaims(username, claims);
    }

    public Claim[] getUserClaims(String username) throws Exception {
        return userClaimStore.getUserClaims(username);
    }

    public Claim getUserClaim(String username, String claimURI) throws Exception {
        return userClaimStore.getUserClaim(username, claimURI);
    }
}