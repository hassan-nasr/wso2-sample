package org.wso2.carbon.identity.oauth.cache;

import org.wso2.carbon.identity.oauth2.model.OAuth2Parameters;

public class SessionDataCacheEntry  extends CacheEntry {

    private static final long serialVersionUID = -4123547630178387354L;

    private OAuth2Parameters oAuth2Parameters;

    String loggedInUser;

    public OAuth2Parameters getoAuth2Parameters() {
        return oAuth2Parameters;
    }

    public void setoAuth2Parameters(OAuth2Parameters oAuth2Parameters) {
        this.oAuth2Parameters = oAuth2Parameters;
    }

    public String getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(String loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

}
