/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.appfactory.git;

import com.gitblit.Constants;
import com.gitblit.models.RepositoryModel;
import com.gitblit.models.UserModel;
import org.wso2.carbon.appfactory.gitblit.AppFactoryGitBlitUserService;

/**
 * This is custom user model to implement custom repository authorization
 */
public class AppFactoryGitBlitUserModel extends UserModel {
    private transient AppFactoryRepositoryAuthorizationClient appFactoryRepositoryAuthorizationClient;
    private transient GitBlitConfiguration configuration;

    public String getAdminCookie() {
        return cookie;
    }

    public void setAdminCookie(String adminCookie) {
        this.cookie = adminCookie;
    }

    public GitBlitConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(GitBlitConfiguration configuration) {
        this.configuration = configuration;
    }

    public AppFactoryGitBlitUserModel(String username) {
        super(username);

    }

    public AppFactoryGitBlitUserModel(String username,
                                      GitBlitConfiguration config, AppFactoryRepositoryAuthorizationClient appFactoryRepositoryAuthorizationClient) {
        this(username);
        this.appFactoryRepositoryAuthorizationClient = appFactoryRepositoryAuthorizationClient;
        this.configuration = config;

    }

    @Override
    protected boolean canAccess(RepositoryModel repository, Constants.AccessRestrictionType ifRestriction, Constants.AccessPermission requirePermission) {
        if (!username.equals(configuration.getProperty(GitBlitConstants
                .APPFACTORY_ADMIN_USERNAME,
                "admin@admin.com"))) {
            appFactoryRepositoryAuthorizationClient.setCookie(getAdminCookie());
            if (getName().substring(getName().lastIndexOf("@") + 1).equals(repository.name.substring
                    (0, repository.name.lastIndexOf("/")))) {
                String appName = AppFactoryGitBlitUserService.getAppFactoryApplicationName(repository.name);
                return appFactoryRepositoryAuthorizationClient.authorize(getName(), appName);
            } else {
                return false;
            }

        }
        return true;
    }

    /**
     * This is to additionally to check the authorization of repository for the user using
     * App Factory RepositoryAuthentication service.Here we are login as admin user because the
     * purpose is to check authorization.
     *
     * @param repository
     * @return
     *//*
    @Override
    public boolean canAccessRepository(RepositoryModel repository) {
        String repositoryName = repository.name;
        String applicationName = repositoryName.substring(0, repositoryName.lastIndexOf(".git"));
        return super.canAccessRepository(repository) && getAppFactoryRepositoryAuthorizationClient()
                .authorize(super.getName(), applicationName);
    }*/
    public AppFactoryRepositoryAuthorizationClient getAppFactoryRepositoryAuthorizationClient() {
        return new AppFactoryRepositoryAuthorizationClient(getConfiguration());
    }
}
