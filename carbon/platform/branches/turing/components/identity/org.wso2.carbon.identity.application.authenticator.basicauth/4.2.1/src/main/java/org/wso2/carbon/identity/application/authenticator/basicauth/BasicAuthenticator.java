package org.wso2.carbon.identity.application.authenticator.basicauth;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.core.services.authentication.AuthenticationUtil;
import org.wso2.carbon.core.services.util.CarbonAuthenticationUtil;
import org.wso2.carbon.core.util.AnonymousSessionUtil;
import org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticationSessionDTO;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticatorConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * Username Password based Authenticator
 *
 */
public class BasicAuthenticator extends AbstractApplicationAuthenticator {

	private static Log log = LogFactory.getLog(BasicAuthenticator.class);
	
	private RegistryService registryService;
	private RealmService realmService;
	
	public BasicAuthenticator(RegistryService registryService, RealmService realmService) {
		this.registryService = registryService;
		this.realmService = realmService;
	}
	
	@Override
    public int doAuthentication(HttpServletRequest request, HttpServletResponse response) {
		int status = getStatus(request);
		
		if (status == BasicAuthenticatorConstants.CUSTOM_STATUS_AUTHENTICATE
				|| request.getSession().getAttribute(ApplicationAuthenticatorConstants.DO_AUTHENTICATION) != null) {
			
			if (canHandle(request)) {
				try {
    				if (authenticate(request)) {
    					status = ApplicationAuthenticatorConstants.STATUS_AUTHENTICATION_PASS;
    					cleanUpSession(request);
    				} else {
    					//send to re-authenticate
    					String loginPage = getAuthenticatorConfig().getStatusMap().get(String.valueOf(BasicAuthenticatorConstants.CUSTOM_STATUS_SEND_TO_LOGIN));
    					status = BasicAuthenticatorConstants.CUSTOM_STATUS_AUTHENTICATE;
    					
    					try {
    						String sessionDataKey = request.getParameter(ApplicationAuthenticatorConstants.SESSION_DATA_KEY);
    						ApplicationAuthenticationSessionDTO sessionDTO = (ApplicationAuthenticationSessionDTO)request.getSession().getAttribute(sessionDataKey);
    			            response.sendRedirect(loginPage + sessionDTO.getQueryParams() + "&authFailure=true");
    		            } catch (IOException e) {
    			            e.printStackTrace();
    		            }
    					
    					request.getSession().setAttribute(BasicAuthenticatorConstants.AUTHENTICATOR_STATUS, status);
    				}
    				
				} catch (Exception e) {
		            String msg = "Error on BasicAuthenticator authentication";
		            log.error(msg, e);
		            status = ApplicationAuthenticatorConstants.STATUS_AUTHENTICATION_FAIL;
		        }
			} else {
				status = ApplicationAuthenticatorConstants.STATUS_AUTHENTICATION_CANNOT_HANDLE;
			}
			
		} else if (status == BasicAuthenticatorConstants.CUSTOM_STATUS_SEND_TO_LOGIN) {
			String loginPage = getAuthenticatorConfig().getStatusMap().get(String.valueOf(status));
			status = BasicAuthenticatorConstants.CUSTOM_STATUS_AUTHENTICATE;
			
			if (isSingleFactorMode()) {
				request.getSession().setAttribute(ApplicationAuthenticatorConstants.DO_AUTHENTICATION, Boolean.TRUE);
			}
			
			try {
	            response.sendRedirect(loginPage + request.getAttribute("commonAuthQueryParams"));
            } catch (IOException e) {
	            e.printStackTrace();
            }
			
			request.getSession().setAttribute(BasicAuthenticatorConstants.AUTHENTICATOR_STATUS, status);
		} 
		
		return status;
    }
	
	@Override
	public int getStatus(HttpServletRequest request) {
		Integer status = (Integer)request.getSession().getAttribute(BasicAuthenticatorConstants.AUTHENTICATOR_STATUS);
		
		//Read from the configuration , if this is the first time this method is called, 
		if (status == null){
			status = super.getStatus(request);
		} 
		
	    return status;
    }
	
	@Override
    public String getAuthenticatorName() {
	    return BasicAuthenticatorConstants.AUTHENTICATOR_NAME;
    }
	
	@Override
    public void resetStatus(HttpServletRequest request) {
		request.getSession().removeAttribute(BasicAuthenticatorConstants.AUTHENTICATOR_STATUS);
    }

    public boolean canHandle(HttpServletRequest request) {

        String userName = request.getParameter("username");
        String password = request.getParameter("password");

        if (userName != null && password != null) {
            return true;
        }

        // This is to login with Remember Me.
//        Cookie[] cookies = request.getCookies();
//        if (cookies != null) {
//            for (Cookie cookie : cookies) {
//                if (cookie.getName().equals(CarbonConstants.REMEMBER_ME_COOKE_NAME)) {
//                    return true;
//                }
//            }
//        }

        return false;
    }
    
	private boolean authenticate(HttpServletRequest request) throws Exception {

		String username = request.getParameter("username");
		String password = request.getParameter("password");

		try {

			boolean isAuthenticated = false;
			UserRealm realm = AnonymousSessionUtil.getRealmByUserName(registryService,
					realmService, username);

			if (realm == null) {
				log.warn("Realm creation failed. Tenant may be inactive or invalid.");
				return false;
			}

			UserStoreManager userStoreManager = realm.getUserStoreManager();

			// Check the authentication
			isAuthenticated = userStoreManager.authenticate(
					MultitenantUtils.getTenantAwareUsername(username), password);
			if (!isAuthenticated) {
				if (log.isDebugEnabled()) {
					log.debug("user authentication failed due to invalid credentials.");
				}
				return false;
			}

			int index = username.indexOf("/");

			if (index < 0) {
				String domain = UserCoreUtil.getDomainFromThreadLocal();

				if (domain != null) {
					username = domain + "/" + username;
				}
			}

			// Check the authorization
			boolean isAuthorized = realm.getAuthorizationManager().isUserAuthorized(
					MultitenantUtils.getTenantAwareUsername(username), "/permission/admin/login",
					CarbonConstants.UI_PERMISSION_ACTION);

			if (!isAuthorized) {
				if (log.isDebugEnabled()) {
					log.debug("Authorization Failure when performing log-in action");
				}
				return false;
			}

			if (log.isDebugEnabled()) {
				log.debug("User is successfully authenticated.");
			}

			request.getSession().setAttribute("username", username);

			String remoteAddress = AuthenticationUtil.getRemoteAddress(MessageContext
					.getCurrentMessageContext());

			int tenantId = IdentityUtil.getTenantIdOFUser(username);

			CarbonAuthenticationUtil.onSuccessAdminLogin(request.getSession(), username, tenantId,
					MultitenantUtils.getTenantDomain(username), remoteAddress);

			return true;

		} finally {
			// remove thread local variable.
			UserCoreUtil.setDomainInThreadLocal(null);
		}
	}
    
    private void cleanUpSession(HttpServletRequest request){
    	request.getSession().setAttribute(BasicAuthenticatorConstants.AUTHENTICATOR_STATUS, null);
    }
}
