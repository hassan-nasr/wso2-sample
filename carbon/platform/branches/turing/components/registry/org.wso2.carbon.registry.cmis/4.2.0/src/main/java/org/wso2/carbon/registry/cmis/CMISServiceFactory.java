/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.cmis;

import org.apache.axis2.AxisFault;

import org.apache.catalina.connector.RequestFacade;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.server.AbstractServiceFactory;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.server.CmisServiceFactory;
import org.apache.chemistry.opencmis.server.support.CmisServiceWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.wso2.carbon.registry.cmis.util.UserInfo;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import org.wso2.carbon.registry.cmis.impl.DocumentTypeHandler;
import org.wso2.carbon.registry.cmis.impl.FolderTypeHandler;
import org.wso2.carbon.registry.cmis.impl.UnversionedDocumentTypeHandler;

import org.wso2.carbon.context.PrivilegedCarbonContext;

import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.math.BigInteger;
import java.util.*;

/**
 * A {@link CmisServiceFactory} implementation which returns {@link RegistryService} instances.
 */
public class CMISServiceFactory extends AbstractServiceFactory {
    private static final Logger log = LoggerFactory.getLogger(CMISServiceFactory.class);

    //The values should match the keys in repository.properties
    public static final String CARBON_HOME = "carbon-home";
    public static final String TRUST_STORE = "trustStore";
    public static final String AXIS2_REPO = "axis2repo";
    public static final String AXIS2_CONF = "axis2Conf";
    public static final String SERVER_URL = "serverUrl";

    public static final BigInteger DEFAULT_MAX_ITEMS_TYPES = BigInteger.valueOf(50);
    public static final BigInteger DEFAULT_DEPTH_TYPES = BigInteger.valueOf(-1);
    public static final BigInteger DEFAULT_MAX_ITEMS_OBJECTS = BigInteger.valueOf(200);
    public static final BigInteger DEFAULT_DEPTH_OBJECTS = BigInteger.valueOf(10);
    
    private static final String uriPart = "/cmis/atom/WSO2%20CMIS%20Repository/content/";

    private RegistryTypeManager typeManager;
    private PathManager pathManager;
    private Map<String, String> gregConfig;
    private String mountPath = "/";
    private CMISRepository gregRepository;
    private Map<UserInfo, CMISRepository> sessions = Collections.synchronizedMap(new HashMap<UserInfo,CMISRepository>());
    @Override
    public void init(Map<String, String> parameters) {
        

        //Read Configuration sets the gregConfig map
        readConfiguration(parameters);

    	typeManager = createTypeManager();
        pathManager = new PathManager();

        DocumentTypeHandler documentTypeHandler = new DocumentTypeHandler(null, pathManager, typeManager);
        FolderTypeHandler folderTypeHandler = new FolderTypeHandler(null, pathManager, typeManager);
        UnversionedDocumentTypeHandler unversionedDocumentTypeHandler = new UnversionedDocumentTypeHandler(null, pathManager, typeManager);

        typeManager.addType(documentTypeHandler.getTypeDefinition());
        typeManager.addType(folderTypeHandler.getTypeDefinition());
        typeManager.addType(unversionedDocumentTypeHandler.getTypeDefinition());
    }

    @Override
    public void destroy() {
        gregRepository = null;
        typeManager = null;
    }

    @Override
    public CmisService getService(CallContext context) {
        //Called for each service request, context contains username, password etc.
        //The registry clients are stored in the map "sessions"
        //If there is an existing session, use it. Otherwise make a new one.

        CMISRepository repository = null;
        String username = context.getUsername();

        String ip = ((RequestFacade)context.get(context.HTTP_SERVLET_REQUEST)).getRemoteAddr();
        String uri = ((RequestFacade)context.get(context.HTTP_SERVLET_REQUEST)).getRequestURI();


        UserInfo userInfoObj = new UserInfo(ip, username);

        if(uri.contains(uriPart)) {
            repository = getRepo(userInfoObj);

            if (repository == null)
                throw new CmisRuntimeException("User is not authenticated to the repository to view the content");

        } else if(sessions.containsKey(userInfoObj)) {
                repository = sessions.get(userInfoObj);
            //TODO check for session timeout
        } else {
            try {
                repository = new CMISRepository(acquireGregRepository(context), pathManager, typeManager);
                //put to sessions for future reference
                sessions.put(new UserInfo(ip, username), repository);

            } catch (RegistryException e) {
                e.printStackTrace();
                throw new CmisRuntimeException(e.getMessage(), e);

            } catch (AxisFault axisFault) {
                axisFault.printStackTrace();
                throw new CmisRuntimeException(axisFault.getMessage());
            }
        }

        CmisServiceWrapper<CMISService> serviceWrapper = new CmisServiceWrapper<CMISService>(
                createGregService(repository, context), DEFAULT_MAX_ITEMS_TYPES, DEFAULT_DEPTH_TYPES,
                DEFAULT_MAX_ITEMS_OBJECTS, DEFAULT_DEPTH_OBJECTS);

        serviceWrapper.getWrappedService().setCallContext(context);
        return serviceWrapper;
    }

    /**
     * 
     * @return result  whether a given user is authorized to view the content from URL
     */
    
    private CMISRepository getRepo (UserInfo obj) {
        
        boolean result = false;
        CMISRepository repo = null;

        for (UserInfo uInfo : sessions.keySet()) {
            if (uInfo.getIp().equals(obj.getIp())) {
                repo = sessions.get(uInfo);
                break;
            }
        }
        return repo;
    }
    


   /**
     * @param context  context with user data
     * @return
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     */
    private Registry acquireGregRepository(CallContext context) throws RegistryException, AxisFault {


        String username = context.getUsername();
        String password = context.getPassword();

        UserRegistry userRegistry = null;
        try{

             RegistryService registryService =
                                      (RegistryService) PrivilegedCarbonContext.getThreadLocalCarbonContext().getOSGiService(RegistryService.class);
             userRegistry = registryService.getRegistry(username, password);

           }  catch (RegistryException e) {
             log.error("unable to create registry instance for the respective enduser", e);
           }

           return userRegistry;
    }

    /**
     * Create a <code>org.wso2.registry.chemistry.greg.GregService</code> from a <code>org.wso2.registry.chemistry.greg.GregRepository</code>org.wso2.registry.chemistry.greg.GregRepository> and
     * <code>CallContext</code>.
     *
     * @param gregRepository
     * @param context
     * @return
     */
    protected CMISService createGregService(CMISRepository gregRepository, CallContext context) {
        return new CMISService(gregRepository);
    }

    protected RegistryTypeManager createTypeManager() {
        return  new RegistryTypeManager();
    }

   private void readConfiguration(Map<String, String> parameters) {
        Map<String, String> map = new HashMap<String, String>();
        List<String> keys = new ArrayList<String>(parameters.keySet());
        Collections.sort(keys);

        map.put(CARBON_HOME, parameters.get(CARBON_HOME)); 
        map.put(TRUST_STORE, parameters.get(TRUST_STORE));
        map.put(AXIS2_REPO, parameters.get(AXIS2_REPO));
        map.put(AXIS2_CONF, parameters.get(AXIS2_CONF));
        map.put(SERVER_URL, parameters.get(SERVER_URL));

        gregConfig = Collections.unmodifiableMap(map);
        //log.debug("Configuration: greg=" + gregConfig);
   }

}
