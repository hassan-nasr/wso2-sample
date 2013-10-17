package org.wso2.carbon.identity.tests.user.store.config;

import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.identity.user.store.config.UserStoreConfigAdminServiceClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.tests.ISIntegrationTest;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO;
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.rmi.RemoteException;


public class UserStoreDeployerTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(UserStoreDeployerTestCase.class);
    public static final String USERSTORES = "userstores";
    private static final String deploymentDirectory = CarbonUtils.getCarbonRepository() + USERSTORES;
    private String userStoreConfigFilePath;
    private File srcFile = new File("../src/test/resources/wso2_com.xml");
    private File destFile;
    private UserStoreConfigAdminServiceClient userStoreConfigurationClient;
    private UserManagementClient userMgtClient;
    private String jdbcClass = "org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager";
    private String rwLDAPClass = "org.wso2.carbon.user.core.ldap.ReadWriteLDAPUserStoreManager";
    private String roLDAPClass = "org.wso2.carbon.user.core.ldap.ReadOnlyLDAPUserStoreManager";
    private String adLDAPClass = "org.wso2.carbon.user.core.ldap.ActiveDirectoryUserStoreManager";


    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init(0);
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        userStoreConfigFilePath = deploymentDirectory + File.separator;
        userStoreConfigurationClient = new UserStoreConfigAdminServiceClient(isServer.getBackEndUrl(), isServer.getSessionCookie());
        userMgtClient = new UserManagementClient(isServer.getBackEndUrl(), isServer.getSessionCookie());
    }

    @Test(groups = "wso2.is", description = "Deploy a user store config file", priority = 1)
    public void testDroppingFile() throws Exception {
        destFile = new File(userStoreConfigFilePath + srcFile.getName());

        FileUtils.copyFile(srcFile, destFile);
        Thread.sleep(30000);
        UserStoreDTO[] userStoreDTOs = userStoreConfigurationClient.getActiveDomains();
        Boolean isAdditionSuccess = false;
        for (UserStoreDTO userStoreDTO : userStoreDTOs) {
            if (userStoreDTO.getDomainId().equalsIgnoreCase("wso2.com")) {
                isAdditionSuccess = true;
            }
        }
        Assert.assertTrue("After 30s user store is still not deployed.", isAdditionSuccess);
    }

    @Test(groups = "wso2.is", description = "Test multiple user stores", priority = 2)
    public void testMultipleUserStores() throws RemoteException, UserAdminUserAdminException {
        Assert.assertTrue("Multiple user stores not detected.",userMgtClient.hasMultipleUserStores());
    }

    @Test(groups = "wso2.is", description = "Delete a user store config file", priority = 3)
    public void testDeletingFile() throws Exception {
        destFile = new File(userStoreConfigFilePath + srcFile.getName());

        FileUtils.forceDelete(destFile);
        Thread.sleep(30000);
        UserStoreDTO[] userStoreDTOs = userStoreConfigurationClient.getActiveDomains();
        Boolean isPresent = false;
        if (userStoreDTOs[0] != null) {
            for (UserStoreDTO userStoreDTO : userStoreDTOs) {
                if (userStoreDTO.getDomainId().equalsIgnoreCase("wso2.com")) {
                    isPresent = true;
                }
            }
        }
        Assert.assertFalse("After 30s user store is still not deleted.", isPresent);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
    }
}