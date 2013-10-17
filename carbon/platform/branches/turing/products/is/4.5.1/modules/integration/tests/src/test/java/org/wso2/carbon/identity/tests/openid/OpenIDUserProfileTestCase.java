/*
 *Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */
package org.wso2.carbon.identity.tests.openid;

import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.provider.openid.stub.dto.OpenIDClaimDTO;
import org.wso2.carbon.identity.provider.openid.stub.dto.OpenIDParameterDTO;
import org.wso2.carbon.identity.provider.openid.stub.dto.OpenIDUserProfileDTO;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;

public class OpenIDUserProfileTestCase extends MasterOpenIDInitiator {

    String userName = "suresh";
    String[] roles = { "admin" };
    String profileName = "default";

    // claims
    String emailClaimURI = "http://axschema.org/contact/email";
    String emailClaimValue = "suresh@wso2.com";
    String firstNameClaimURI = "http://axschema.org/namePerson/first";
    String firstNameClaimValue = "Suresh";
    String lastNameClaimURI = "http://axschema.org/namePerson/last";
    String lastNameClaimValue = "Attanayake";
    String countryClaimURI = "http://axschema.org/contact/country/home";
    String countryClaimValue = "Sri Lanka";

    ClaimValue[] claimValues = new ClaimValue[4];

    @BeforeClass(alwaysRun = true)
    public void intiTest() throws AxisFault {
        this.init(1);

    }

    /**
     * Create a user with claim. Try to read user claims
     */
    @Test(alwaysRun = true, description = "Add user Provider", priority = 1)
    public void userProfileTest() {

        ClaimValue email = new ClaimValue();
        email.setClaimURI(emailClaimURI);
        email.setValue(emailClaimValue);
        claimValues[0] = email;

        ClaimValue firstName = new ClaimValue();
        firstName.setClaimURI(firstNameClaimURI);
        firstName.setValue(firstNameClaimValue);
        claimValues[1] = firstName;

        ClaimValue lastName = new ClaimValue();
        lastName.setClaimURI(lastNameClaimURI);
        lastName.setValue(lastNameClaimValue);
        claimValues[2] = lastName;

        ClaimValue country = new ClaimValue();
        country.setClaimURI(countryClaimURI);
        country.setValue(countryClaimValue);
        claimValues[3] = country;

        try {
            // creating the user
            remoteUSMServiceClient.addUser(userName, "Wso2@123", roles, claimValues, profileName, true);
        } catch (Exception e) {
            Assert.fail("Error while creating the user", e);
        }

        String openId = Util.getDefaultOpenIDIdentifier(userName);
        OpenIDParameterDTO[] openidRequestParams = Util.getDummyOpenIDParameterDTOArray();

        OpenIDUserProfileDTO[] userProfiles = null;
        // reading back user profiles
        try {
            userProfiles = openidServiceClient.getUserProfiles(openId, openidRequestParams);
        } catch (Exception e) {
            Assert.fail("Error while reading user profiles", e);
        }

        Assert.assertEquals(userProfiles[0].getProfileName(), profileName);
        
        OpenIDClaimDTO[] claims = null;
        try {
            // reading back user claims 
            claims = openidServiceClient.getClaimValues(openId, profileName, openidRequestParams);
        } catch (Exception e) {
            Assert.fail("Error while reading user claims", e);
        }
        
        // we expect 4 claims : email, firstname, lastname and country
        Assert.assertEquals(claims.length, 4);
        
        // now checking claim values
        for (OpenIDClaimDTO dto : claims) {
            if (emailClaimURI.equals(dto.getClaimUri())) {
                Assert.assertEquals(dto.getClaimValue(), emailClaimValue);
            } else if (firstNameClaimURI.equals(dto.getClaimUri())) {
                Assert.assertEquals(dto.getClaimValue(), firstNameClaimValue);
            } else if (lastNameClaimURI.equals(dto.getClaimUri())) {
                Assert.assertEquals(dto.getClaimValue(), lastNameClaimValue);
            } else if (countryClaimURI.equals(dto.getClaimUri())) {
                Assert.assertEquals(dto.getClaimValue(), countryClaimValue);
            } else {
                Assert.fail("Invalid claim returned");
            }
        }

    }
}