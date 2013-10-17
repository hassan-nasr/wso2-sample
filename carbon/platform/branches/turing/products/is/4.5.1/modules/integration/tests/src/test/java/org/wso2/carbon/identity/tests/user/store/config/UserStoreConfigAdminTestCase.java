package org.wso2.carbon.identity.tests.user.store.config;

import edu.emory.mathcs.backport.java.util.Arrays;
import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.identity.user.store.config.UserStoreConfigAdminServiceClient;
import org.wso2.carbon.identity.tests.ISIntegrationTest;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.util.List;


public class UserStoreConfigAdminTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(UserStoreConfigAdminTestCase.class);
    public static final String USERSTORES = "userstores";
    private static final String deploymentDirectory = CarbonUtils.getCarbonRepository() + USERSTORES;
    private String userStoreConfigFilePath;
    private File srcFile = new File("../src/test/resources/wso2_com.xml");
    private File destFile;
    private UserStoreConfigAdminServiceClient userStoreConfigurationClient;
    private String jdbcClass = "org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager";
    private String rwLDAPClass = "org.wso2.carbon.user.core.ldap.ReadWriteLDAPUserStoreManager";
    private String roLDAPClass = "org.wso2.carbon.user.core.ldap.ReadOnlyLDAPUserStoreManager";
    private String adLDAPClass = "org.wso2.carbon.user.core.ldap.ActiveDirectoryUserStoreManager";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init(0);
        userStoreConfigurationClient = new UserStoreConfigAdminServiceClient(isServer.getBackEndUrl(), isServer.getSessionCookie());
    }

    @Test(groups = "wso2.is", description = "Check user store manager implementations", priority = 1)
    public void testAvailableUserStoreClasses() throws Exception {
        String[] classes = userStoreConfigurationClient.getAvailableUserStoreClasses();
        List<String> classNames = Arrays.asList(classes);
        Assert.assertTrue(jdbcClass + " not present.", classNames.contains(jdbcClass));
        Assert.assertTrue(rwLDAPClass + " not present.", classNames.contains(rwLDAPClass));
        Assert.assertTrue(roLDAPClass + " not present.", classNames.contains(roLDAPClass));
        Assert.assertTrue(adLDAPClass + " not present.", classNames.contains(adLDAPClass));

    }


    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
    }
}