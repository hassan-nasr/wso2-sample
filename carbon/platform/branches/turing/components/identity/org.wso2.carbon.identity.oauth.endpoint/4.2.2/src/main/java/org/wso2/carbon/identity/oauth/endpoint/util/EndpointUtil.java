/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.oauth.endpoint.util;

import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.axiom.util.base64.Base64Utils;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.common.exception.OAuthClientException;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.OAuth2Service;
import org.wso2.carbon.identity.oauth2.OAuth2TokenValidationService;
import org.wso2.carbon.identity.oauth2.model.OAuth2Parameters;
import org.wso2.carbon.ui.CarbonUIUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class EndpointUtil {

    /**
     * Returns the {@code OAuth2Service} instance
     *
     * @return
     */
    public static OAuth2Service getOAuth2Service() {
        return (OAuth2Service) PrivilegedCarbonContext.getCurrentContext()
                .getOSGiService(OAuth2Service.class);
    }

    /**
     * Returns the {@code OAuthServerConfiguration} instance
     *
     * @return
     */
    public static OAuthServerConfiguration getOAuthServerConfiguration() {
        return (OAuthServerConfiguration) PrivilegedCarbonContext.getCurrentContext()
                .getOSGiService(OAuthServerConfiguration.class);
    }

    /**
     * Returns the {@code OAuthServerConfiguration} instance
     *
     * @return
     */
    public static OAuth2TokenValidationService getOAuth2TokenValidationService() {
        return (OAuth2TokenValidationService) PrivilegedCarbonContext.getCurrentContext()
                .getOSGiService(OAuth2TokenValidationService.class);
    }

    /**
     * Returns the request validator class name
     * @return
     * @throws OAuthSystemException
     */
    public static String getUserInfoRequestValidator() throws OAuthSystemException {
        return getOAuthServerConfiguration().getOpenIDConnectUserInfoEndpointRequestValidator();
    }

    /**
     * Returns the access token validator class name
     * @return
     */
    public static String getAccessTokenValidator() {
        return getOAuthServerConfiguration().getOpenIDConnectUserInfoEndpointAccessTokenValidator();
    }

    /**
     * Returns the response builder class name
     * @return
     */
    public static String getUserInfoResponseBuilder() {
        return getOAuthServerConfiguration().getOpenIDConnectUserInfoEndpointResponseBuilder();
    }

    /**
     * Returns the claim retriever class name
     * @return
     */
    public static String getUserInfoClaimRetriever() {
        return getOAuthServerConfiguration().getOpenIDConnectUserInfoEndpointClaimRetriever();
    }

    /**
     * Return the claim dialect for the claim retriever
     * @return
     */
    public static String getUserInfoClaimDialect() {
        return getOAuthServerConfiguration().getOpenIDConnectUserInfoEndpointClaimDialect();
    }

    /**
     * Extracts the username and password info from the HTTP Authorization Header
     * @param authorizationHeader "Basic " + base64encode(username + ":" + password)
     * @return String array with client id and client secret.
     * @throws org.wso2.carbon.identity.base.IdentityException If the decoded data is null.
     */
    public static String[] extractCredentialsFromAuthzHeader(String authorizationHeader)
            throws OAuthClientException {
        String[] splitValues = authorizationHeader.trim().split(" ");
        byte[] decodedBytes = Base64Utils.decode(splitValues[1].trim());
        if (decodedBytes != null) {
            String userNamePassword = new String(decodedBytes);
            return userNamePassword.split(":");
        } else {
            String errMsg = "Error decoding authorization header. Could not retrieve client id and client secret.";
            throw new OAuthClientException(errMsg);
        }
    }

    /**
     * Returns the error page URL. If appName is not <code>null</code> it will be added as query parameter
     * to be displayed to the user. If redirect_uri is <code>null</code> the common error page URL will be returned.
     *
     * @param errorCode
     * @param errorMessage
     * @param appName
     * @return
     */
    public static String getErrorPageURL(String errorCode, String errorMessage, String appName, String redirect_uri) {

        String errorPageUrl = null;
        if(redirect_uri != null && !redirect_uri.equals("")){
            errorPageUrl = redirect_uri;
        } else {
            errorPageUrl = CarbonUIUtil.getAdminConsoleURL("/")+ "../authenticationendpoint/oauth2_error.do";
        }
        try {
            errorPageUrl += "?" + OAuthConstants.OAUTH_ERROR_CODE + "="
                    + URLEncoder.encode(errorCode, "UTF-8") + "&" + OAuthConstants.OAUTH_ERROR_MESSAGE + "="
                    + URLEncoder.encode(errorMessage, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // ignore
        }

        if(appName != null){
            try {
                errorPageUrl += "application" + "=" + URLEncoder.encode(appName,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                // ignore
            }
        }

        return errorPageUrl;
    }

    /**
     * Returns the login page URL.
     *
     * @param checkAuthentication
     * @param forceAuthenticate
     * @return
     */
    public static String getLoginPageURL(String sessionDataKey, boolean forceAuthenticate, boolean checkAuthentication) {

        String commonAuthURL = CarbonUIUtil.getAdminConsoleURL("/");
        commonAuthURL = commonAuthURL.replace("carbon", "commonauth");
        String selfPath = "../../oauth2";
        String queryParams = "";
        queryParams = "?" + OAuthConstants.SESSION_DATA_KEY + "=" + sessionDataKey + "&type=oauth2"
                + "&commonAuthCallerPath=" + selfPath
                + "&" + "forceAuthenticate" + "=" + forceAuthenticate
                + "&" + "checkAuthentication" + "=" + checkAuthentication;

        return commonAuthURL + queryParams;
    }

    /**
     * Returns the consent page URL.
     *
     * @param params
     * @param loggedInUser
     * @return
     */
    public static String getUserConsentURL(OAuth2Parameters params, String loggedInUser, String sessiondataKey, boolean  isOIDC) {
        String consentPage = null;
        try {
            if(isOIDC) {
                consentPage = CarbonUIUtil.getAdminConsoleURL("/") +
                        "../authenticationendpoint/oauth2_consent.do";
            } else {
                consentPage = CarbonUIUtil.getAdminConsoleURL("/") +
                        "../authenticationendpoint/oauth2_authz.do";
            }
            consentPage +=
                    "?" + OAuthConstants.OIDC_LOGGED_IN_USER + "=" +
                            URLEncoder.encode(loggedInUser, "UTF-8") + "&" +
                            "application" + "=" + URLEncoder.encode(params.getApplicationName(), "ISO-8859-1" ) + "&" +
                            OAuthConstants.OAuth20Params.SCOPE + "=" + URLEncoder.encode(EndpointUtil.getScope(params), "ISO-8859-1" ) + "&" +
                            OAuthConstants.SESSION_DATA_KEY_CONSENT + "=" + URLEncoder.encode(sessiondataKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // ignore
        }
        return consentPage;
    }

    public static String getScope(OAuth2Parameters params) {
        StringBuffer scopes = new StringBuffer();
        for(String scope : params.getScopes() ) {
            scopes.append(EndpointUtil.getSafeText(scope) + " ");
        }
        return scopes.toString().trim();
    }

    public static String getSafeText(String text) {
        if (text == null) {
            return text;
        }
        text = text.trim();
        if (text.indexOf('<') > -1) {
            text = text.replace("<", "&lt;");
        }
        if (text.indexOf('>') > -1) {
            text = text.replace(">", "&gt;");
        }
        return text;
    }

    public static String getRealmInfo(){
        ServerConfiguration serverConfig = ServerConfiguration.getInstance();
        String hostname = serverConfig.getFirstProperty("HostName");
        return "Basic realm=" + hostname;
    }

}
