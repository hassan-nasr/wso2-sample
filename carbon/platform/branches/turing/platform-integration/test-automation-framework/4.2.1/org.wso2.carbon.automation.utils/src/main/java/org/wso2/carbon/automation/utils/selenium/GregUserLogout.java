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
package org.wso2.carbon.automation.utils.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;


public class GregUserLogout {

    public void userLogout(WebDriver driver) throws InterruptedException {
//        driver.findElement(By.xpath("//li[7]/a")).click();
        Thread.sleep(3000L);
        driver.findElement(By.xpath("/html/body/table/tbody/tr/td/div/div[4]/div/ul/li[7]/a")).click();
    }
}
