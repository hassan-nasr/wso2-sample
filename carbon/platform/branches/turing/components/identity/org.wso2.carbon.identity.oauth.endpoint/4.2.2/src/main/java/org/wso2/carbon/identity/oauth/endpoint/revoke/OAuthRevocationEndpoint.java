/*
*Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.oauth.endpoint.revoke;

import org.apache.amber.oauth2.as.response.OAuthASResponse;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.OAuthResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.oauth.common.OAuth2ErrorCodes;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.common.exception.OAuthClientException;
import org.wso2.carbon.identity.oauth.endpoint.OAuthRequestWrapper;
import org.wso2.carbon.identity.oauth.endpoint.util.EndpointUtil;
import org.wso2.carbon.identity.oauth2.dto.OAuthRevocationRequestDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuthRevocationResponseDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.util.Enumeration;

@Path("/revoke")
public class OAuthRevocationEndpoint {

    private static Log log = LogFactory.getLog(OAuthRevocationEndpoint.class);

    @POST
    @Path("/")
    @Consumes("application/x-www-form-urlencoded")
    public Response revokeAccessToken(@Context HttpServletRequest request,
            MultivaluedMap<String, String> paramMap) throws OAuthSystemException {

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext
                    .getThreadLocalCarbonContext();
            carbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            carbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);

            HttpServletRequestWrapper httpRequest = new OAuthRequestWrapper(request, paramMap);

            if (log.isDebugEnabled()) {
                logAccessTokenRevocationRequest(httpRequest);
            }
            String token = httpRequest.getParameter("token");
            if (token == null || token.equals("")) {
                if (paramMap.get("token") != null && !paramMap.isEmpty()) {
                    token = paramMap.get("token").get(0);
                }
            }
            String token_type = httpRequest.getParameter("token_type_hint");
            if (token_type == null || token_type.equals("")) {
                if (paramMap.get("token_type_hint") != null
                        && !paramMap.get("token_type_hint").isEmpty()) {
                    token_type = paramMap.get("token_type_hint").get(0);
                }
            }
            String callback = httpRequest.getParameter("callback");
            if (callback == null || callback.equals("")) {
                if (paramMap.get("callback") != null && !paramMap.get("callback").isEmpty()) {
                    callback = paramMap.get("callback").get(0);
                }
            }

            // extract the basic auth credentials if present in the request and use for
            // authentication.
            boolean basicAuthUsed = false;
            if (request.getHeader(OAuthConstants.HTTP_REQ_HEADER_AUTHZ) != null) {
                try {
                    // If the client has included client credentials in any in any other format except Basic Auth headers,
                    // then it is not recommended as per the specification. sending invalid_client error back.
                    String[] clientCredentials = EndpointUtil
                            .extractCredentialsFromAuthzHeader(request
                                                                       .getHeader(OAuthConstants.HTTP_REQ_HEADER_AUTHZ));

                    // The client MUST NOT use more than one authentication method in each request
                    if (paramMap.containsKey(OAuth.OAUTH_CLIENT_ID)
                            && paramMap.containsKey(OAuth.OAUTH_CLIENT_SECRET)) {
                        return handleBasicAuthFailure(callback);
                    }

                    // add the credentials available in Authorization to the parameter map
                    paramMap.add(OAuth.OAUTH_CLIENT_ID, clientCredentials[0]);
                    paramMap.add(OAuth.OAUTH_CLIENT_SECRET, clientCredentials[1]);

                } catch (OAuthClientException e) {
                    // malformed credential string is considered as an auth failure.
                    return handleBasicAuthFailure(callback);
                }
            }

            try {
                OAuthRevocationRequestDTO revokeRequest = new OAuthRevocationRequestDTO();
                revokeRequest.setConsumerKey(paramMap.get(OAuth.OAUTH_CLIENT_ID).get(0));
                revokeRequest.setConsumerSecret(paramMap.get(OAuth.OAUTH_CLIENT_SECRET).get(0));
                if (token != null && !token.equals("")) {
                    revokeRequest.setToken(token);
                } else {
                    handleClientFailure(callback);
                }
                if (token_type != null && !token_type.equals("")) {
                    revokeRequest.setToken_type(token_type);
                }
                OAuthRevocationResponseDTO oauthRevokeResp = revokeTokens(revokeRequest);
                // if there BE has returned an error
                if (oauthRevokeResp.getErrorMsg() != null) {
                    // if there is an auth failure, HTTP 401 Status Code should be sent back to the client.
                    if (OAuth2ErrorCodes.INVALID_CLIENT.equals(oauthRevokeResp.getErrorCode())) {
                        return handleBasicAuthFailure(callback);
                    } else if (OAuth2ErrorCodes.UNAUTHORIZED_CLIENT.equals(oauthRevokeResp.getErrorCode())) {
                        return handleAuthorizationFailure(callback);
                    }
                    // Otherwise send back HTTP 400 Status Code
                    return handleClientFailure(callback, oauthRevokeResp);
                } else {
                    OAuthResponse response;
                    if (callback != null && !callback.equals("")) {
                        response = CarbonOAuthASResponse.revokeResponse(HttpServletResponse.SC_OK).buildBodyMessage();
                        response.setBody(callback + "();");
                    } else {
                        response = CarbonOAuthASResponse.revokeResponse(HttpServletResponse.SC_OK).buildBodyMessage();
                    }

                    ResponseBuilder respBuilder = Response
                            .status(response.getResponseStatus())
                            .header(OAuthConstants.HTTP_RESP_HEADER_CACHE_CONTROL,
                                    OAuthConstants.HTTP_RESP_HEADER_VAL_CACHE_CONTROL_NO_STORE)
                            .header(OAuthConstants.HTTP_RESP_HEADER_PRAGMA,
                                    OAuthConstants.HTTP_RESP_HEADER_VAL_PRAGMA_NO_CACHE);

                    if (callback != null && !callback.equals("")) {
                        respBuilder.header("Content-Type", "application/javascript");
                    } else {
                        respBuilder.header("Content-Type", "text/html");
                    }

                    return respBuilder.entity(response.getBody()).build();
                }

            } catch (OAuthClientException e) {
                return handleServerFailure(callback, e);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

    }

    private Response handleBasicAuthFailure(String callback)
            throws OAuthSystemException {
        if(callback == null && callback.equals("")){
            OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                    .setError(OAuth2ErrorCodes.INVALID_CLIENT)
                    .setErrorDescription("Client Authentication failed.").buildJSONMessage();
            return Response.status(response.getResponseStatus())
                    .header(OAuthConstants.HTTP_RESP_HEADER_AUTHENTICATE, EndpointUtil.getRealmInfo())
                    .header("Content-Type", "text/html")
                    .entity(response.getBody()).build();
        } else {
            OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                    .setError(OAuth2ErrorCodes.INVALID_CLIENT).buildJSONMessage();
            return Response.status(response.getResponseStatus())
                    .header(OAuthConstants.HTTP_RESP_HEADER_AUTHENTICATE, EndpointUtil.getRealmInfo())
                    .header("Content-Type", "application/javascript")
                    .entity(callback+"(" + response.getBody() + ");").build();
        }
    }

    private Response handleAuthorizationFailure(String callback)
            throws OAuthSystemException {
        if(callback == null && callback.equals("")){
            OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                    .setError(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT)
                    .setErrorDescription("Client Authentication failed.").buildJSONMessage();
            return Response.status(response.getResponseStatus())
                    .header(OAuthConstants.HTTP_RESP_HEADER_AUTHENTICATE, EndpointUtil.getRealmInfo())
                    .header("Content-Type", "text/html")
                    .entity(response.getBody()).build();
        } else {
            OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                    .setError(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT).buildJSONMessage();
            return Response.status(response.getResponseStatus())
                    .header(OAuthConstants.HTTP_RESP_HEADER_AUTHENTICATE, EndpointUtil.getRealmInfo())
                    .header("Content-Type", "application/javascript")
                    .entity(callback+"(" + response.getBody() + ");").build();
        }
    }

    private Response handleServerFailure(String callback, Exception e)
            throws OAuthSystemException {
        if(callback == null && callback.equals("")){
            OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                    .setError(OAuth2ErrorCodes.SERVER_ERROR)
                    .setErrorDescription(e.getMessage()).buildJSONMessage();
            return Response.status(response.getResponseStatus())
                    .header("Content-Type", "text/html")
                    .entity(response.getBody()).build();
        } else {
            OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                    .setError(OAuth2ErrorCodes.SERVER_ERROR).buildJSONMessage();
            return Response.status(response.getResponseStatus())
                    .header("Content-Type", "application/javascript")
                    .entity(callback+"(" + response.getBody() + ");").build();
        }
    }

    private Response handleClientFailure(String callback)
            throws OAuthSystemException {
        if(callback == null && callback.equals("")){
            OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                    .setError(OAuth2ErrorCodes.INVALID_REQUEST)
                    .setErrorDescription("Invalid revocation request").buildJSONMessage();
            return Response.status(response.getResponseStatus())
                    .header("Content-Type", "text/html")
                    .entity(response.getBody()).build();
        } else {
            OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                    .setError(OAuth2ErrorCodes.INVALID_REQUEST).buildJSONMessage();
            return Response.status(response.getResponseStatus())
                    .header("Content-Type", "application/javascript")
                    .entity(callback+"(" + response.getBody() + ");").build();
        }
    }

    private Response handleClientFailure(String callback, OAuthRevocationResponseDTO dto)
            throws OAuthSystemException {
        if(callback == null && callback.equals("")){
            OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                    .setError(dto.getErrorCode())
                    .setErrorDescription(dto.getErrorMsg()).buildJSONMessage();
            return Response.status(response.getResponseStatus())
                    .header("Content-Type", "text/html")
                    .entity(response.getBody()).build();
        } else {
            OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                    .setError(dto.getErrorCode()).buildJSONMessage();
            return Response.status(response.getResponseStatus())
                    .header("Content-Type", "application/javascript")
                    .entity(callback+"(" + response.getBody() + ");").build();
        }
    }

    private void logAccessTokenRevocationRequest(HttpServletRequest request) {
        log.debug("Received a access token revocation request : " + request.getRequestURI());
        // log the headers.
        log.debug("----------logging request headers.----------");
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = (String)headerNames.nextElement();
            Enumeration headers = request.getHeaders(headerName);
            while (headers.hasMoreElements()) {
                log.debug(headerName + " : " + headers.nextElement());
            }
        }
        // log the parameters.
        log.debug("----------logging request parameters.----------");
        log.debug("token - " + request.getParameter("token"));
    }

    private OAuthRevocationResponseDTO revokeTokens(OAuthRevocationRequestDTO oauthRequest)
            throws OAuthClientException {

        OAuthRevocationRequestDTO revokeReqDTO = new OAuthRevocationRequestDTO();

        revokeReqDTO.setConsumerKey(oauthRequest.getConsumerKey());
        revokeReqDTO.setConsumerSecret(oauthRequest.getConsumerSecret());
        revokeReqDTO.setToken(oauthRequest.getToken());
        revokeReqDTO.setToken_type(oauthRequest.getToken_type());

        return EndpointUtil.getOAuth2Service().revokeTokenByOAuthClient(revokeReqDTO);
    }
}
