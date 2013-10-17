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

package org.wso2.carbon.automation.api.selenium.appfactory.appmanagement;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.wso2.carbon.automation.api.selenium.util.UIElementMapper;

import java.io.IOException;

public class TeamPage {

    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public TeamPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.
        if (!(driver.getCurrentUrl().contains("user.jag"))) {
            throw new IllegalStateException("This is not the Team page");
        }
    }

    public TeamManagementPage gotoTeamManagementPage() throws IOException {
        driver.findElement(By.id(uiElementMapper.getElement("app.add.member.page.id"))).click();
        return new TeamManagementPage(driver);
    }

    public AppManagementPage gotoAppManagementPage() throws IOException {
        driver.findElement(By.linkText(uiElementMapper.getElement("app.application.overview.page"))).click();
        return new AppManagementPage(driver);
    }
}
