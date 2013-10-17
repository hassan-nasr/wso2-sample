/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.provider;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.rahas.RahasConstants;
import org.apache.rahas.RahasData;
import org.apache.rahas.impl.util.SAMLAttributeCallback;
import org.apache.rahas.impl.util.SAMLCallback;
import org.apache.rahas.impl.util.SAMLCallbackHandler;
import org.opensaml.Configuration;
import org.opensaml.SAMLAttribute;
import org.opensaml.SAMLException;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSStringBuilder;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.IdentityClaimManager;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.namespace.QName;
import java.util.*;

public class AttributeCallbackHandler implements SAMLCallbackHandler {

    private static Log log = LogFactory.getLog(AttributeCallbackHandler.class);
    protected Map<String, RequestedClaimData> requestedClaims = new HashMap<String, RequestedClaimData>();
    protected Map<String, Claim> supportedClaims = new HashMap<String, Claim>();

    public void handle(SAMLCallback callback) throws SAMLException {
        SAMLAttributeCallback attrCallback = null;
        RahasData data = null;
        OMElement claimElem = null;
        String userIdentifier = null;
        String splitArr[] = null;
        IdentityAttributeService[] attributeCallbackServices = null;
        
        try {
            if (callback instanceof SAMLAttributeCallback) {
                attrCallback = (SAMLAttributeCallback) callback;
                data = attrCallback.getData();
                claimElem = data.getClaimElem();
                userIdentifier = data.getPrincipal().getName();
                
                if(userIdentifier != null){
                    /*Extract 'Common Name' as the user id if authenticated
                      via X.509 certificates*/
                    splitArr = userIdentifier.split(",")[0].split("=");
                    if(splitArr.length == 2){
                        userIdentifier = splitArr[1];
                    }
                }
                
                loadClaims(claimElem, userIdentifier);
                processClaimData(data, claimElem);
                populateClaimValues(userIdentifier, attrCallback);

                attributeCallbackServices = IdentityAttributeServiceStore.getAttributeServices();
                for (int i = 0; i < attributeCallbackServices.length; i++) {
                    try {
                        attributeCallbackServices[i].handle(attrCallback);
                    } catch (Exception e) {
                        log.error("Error occuerd while calling attribute callback", e);
                    }
                }
                
                if (RahasConstants.TOK_TYPE_SAML_20.equals(data.getTokenType())) {
                    if (attrCallback.getSAML2Attributes() == null
                            || attrCallback.getSAML2Attributes().length == 0) {
                        attrCallback.addAttributes(getSAML2Attribute("Name", "Colombo",
                                "https://rahas.apache.org/saml/attrns"));
                    }
                } else {
                    if (attrCallback.getAttributes() == null
                            || attrCallback.getAttributes().length == 0) {
                        SAMLAttribute attribute = new SAMLAttribute("Name",
                                "https://rahas.apache.org/saml/attrns", null, -1, Arrays
                                        .asList(new String[]{"Colombo/Rahas"}));
                        attrCallback.addAttributes(attribute);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error occuerd while populating claim data", e);
        }
    }

    private Attribute getSAML2Attribute(String name, String value, String namespace) {
        XMLObjectBuilderFactory builderFactory = null;
        SAMLObjectBuilder<Attribute> attrBuilder = null;
        Attribute attribute = null;
        XSStringBuilder attributeValueBuilder = null;
        XSString stringValue = null;

        builderFactory = Configuration.getBuilderFactory();
        attrBuilder = (SAMLObjectBuilder<Attribute>) builderFactory
                .getBuilder(Attribute.DEFAULT_ELEMENT_NAME);
        attribute = attrBuilder.buildObject();
        attribute.setName(name);
        attribute.setNameFormat(namespace);

        attributeValueBuilder = (XSStringBuilder) builderFactory.getBuilder(XSString.TYPE_NAME);
        stringValue = attributeValueBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME,
                XSString.TYPE_NAME);
        stringValue.setValue(value);
        attribute.getAttributeValues().add(stringValue);
        return attribute;
    }

    /**
     * This method loads claim according to the claim dialect that is defined in the request
     * 
     * @param claimsElement
     * @param userIdentifier
     * @throws IdentityProviderException
     */
    private void loadClaims(OMElement claimsElement, String userIdentifier) throws IdentityProviderException {
        IdentityClaimManager claimManager = null;
        Claim[] claims = null;
        String claimDialect = null;

        if(claimsElement.getNamespace() != null){
            claimDialect = claimsElement.
                getAttributeValue(new QName(claimsElement.getNamespace().getNamespaceURI(), "Dialect"));
        }

        if(claimDialect == null || claimDialect.trim().length() == 0){
            claimDialect = UserCoreConstants.DEFAULT_CARBON_DIALECT;    
        }

        if (log.isDebugEnabled()) {
            log.debug("Loading claims");
        }

        try {
            claimManager = IdentityClaimManager.getInstance();
            claims = claimManager.getAllSupportedClaims(claimDialect,
                    IdentityTenantUtil.getRealm(null, userIdentifier));
            for (int i = 0; i < claims.length; i++) {
                Claim temp = claims[i];
                supportedClaims.put(temp.getClaimUri(), temp);
            }
        } catch (IdentityException e) {
            log.error("Error while loading claims", e);
            throw new IdentityProviderException("Error while loading claims", e);
        }
    }

    protected void loadClaims(String userIdentifier) throws IdentityProviderException {

        IdentityClaimManager claimManager = null;
        Claim[] claims = null;

        if (log.isDebugEnabled()) {
            log.debug("Loading claims");
        }

        try {
            claimManager = IdentityClaimManager.getInstance();
            claims = claimManager.getAllSupportedClaims(UserCoreConstants.DEFAULT_CARBON_DIALECT,
                    IdentityTenantUtil.getRealm(null, userIdentifier));
            for (int i = 0; i < claims.length; i++) {
                Claim temp = claims[i];
                supportedClaims.put(temp.getClaimUri(), temp);
            }
        } catch (IdentityException e) {
            log.error("Error while loading claims", e);
            throw new IdentityProviderException("Error while loading claims", e);
        }
    }

    /**
     * 
     * @param rahasData
     * @param claims
     * @throws IdentityProviderException
     */
    protected void processClaimData(RahasData rahasData, OMElement claims)
            throws IdentityProviderException {

        if (claims == null) {
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Processing claim data");
        }

        Iterator iterator = null;
        iterator = claims.getChildrenWithName(new QName(IdentityConstants.NS,
                IdentityConstants.LocalNames.IDENTITY_CLAIM_TYPE));

        while (iterator.hasNext()) {
            OMElement omElem = null;
            RequestedClaimData claim = null;
            String uriClaim = null;
            String optional = null;

            omElem = (OMElement) iterator.next();
            claim = getRequestedClaim();
            uriClaim = omElem.getAttributeValue(new QName(null, "Uri"));

            if (uriClaim == null) {
                log.error("Empty claim uri found while procession claim data");
                throw new IdentityProviderException(
                        "Empty claim uri found while procession claim data");
            }
            claim.setUri(uriClaim);
            optional = (omElem.getAttributeValue(new QName(null, "Optional")));
            if (optional != null) {
                claim.setBOptional((optional.equals("true")) ? true : false);
            } else {
                claim.setBOptional(true);
            }

            requestedClaims.put(claim.getUri(), claim);
        }
    }

    protected void populateClaimValues(String userIdentifier, SAMLAttributeCallback callback)
            throws IdentityProviderException {
        UserStoreManager connector = null;
        RahasData rahasData = null;

        if (log.isDebugEnabled()) {
            log.debug("Populating claim values");
        }

        if (requestedClaims.isEmpty()) {
            return;
        }

        try {
            connector = IdentityTenantUtil.getRealm(null, userIdentifier).getUserStoreManager();
        } catch (Exception e) {
            log.error("Error while instantiating IdentityUserStore", e);
            throw new IdentityProviderException("Error while instantiating IdentityUserStore", e);
        }

        // get the column names for the URIs
        Iterator<RequestedClaimData> ite = requestedClaims.values().iterator();
        List<String> claimList = new ArrayList<String>();
        rahasData = callback.getData();

        while (ite.hasNext()) {
            RequestedClaimData claim = (RequestedClaimData) ite.next();
            if (claim != null && !claim.getUri().equals(IdentityConstants.CLAIM_PPID)) {
                claimList.add(claim.getUri());
            }
        }

        String[] claimArray = new String[claimList.size()];
        String userId = userIdentifier;
        Map<String, String> mapValues = null;

        try {
            mapValues = connector.getUserClaimValues(MultitenantUtils.getTenantAwareUsername(userId),
                                                                claimList.toArray(claimArray), null);
            ite = requestedClaims.values().iterator();
            while (ite.hasNext()) {
                SAMLAttribute attribute = null;
                Attribute saml2Attribute = null;
                RequestedClaimData claimData = ite.next();
                claimData.setValue(mapValues.get(claimData.getUri()));
                if (claimData.getValue() != null) {
                    if (RahasConstants.TOK_TYPE_SAML_20.equals(rahasData.getTokenType())) {
                        saml2Attribute = getSAML2Attribute(claimData.getUri(),
                                claimData.getValue(), claimData.getUri());
                        callback.addAttributes(saml2Attribute);
                    } else {
                        if (supportedClaims.get(claimData.getUri()) != null) {
                            attribute = new SAMLAttribute(supportedClaims.get(claimData.getUri())
                                    .getDisplayTag(), claimData.getUri(), null, -1,
                                    Arrays.asList(new String[] { claimData.getValue() }));
                            callback.addAttributes(attribute);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new IdentityProviderException(e.getMessage(), e);
        }
    }
    protected RequestedClaimData getRequestedClaim() {
        return new RequestedClaimData();
    }
}
