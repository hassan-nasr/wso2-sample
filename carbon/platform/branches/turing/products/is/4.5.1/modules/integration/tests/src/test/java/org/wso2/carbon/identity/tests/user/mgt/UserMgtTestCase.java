package org.wso2.carbon.identity.tests.user.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.identity.tests.ISIntegrationTest;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;

import com.mchange.util.AssertException;


public class UserMgtTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(UserMgtTestCase.class);
    private UserManagementClient userMgtClient;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(0);
        userMgtClient = new UserManagementClient(isServer.getBackEndUrl(), isServer.getSessionCookie());
        userMgtClient.addUser("lanka", "lanka123", new String[]{"admin"}, "default");
    }

    @Test(groups = "wso2.is", description = "Get the users by user name", priority = 1)
    public void testListUsersByUserName() throws Exception {
        FlaggedName[] names = userMgtClient.listUsers("lanka", -1);
        Assert.assertEquals(names[0].getItemName(), "lanka");
    }

    @Test(groups = "wso2.is", description = "List the roles", priority = 2)
    public void testListRoles() throws Exception {
        FlaggedName[] roles = userMgtClient.listRoles("admin", -1);
        Assert.assertEquals(roles[0].getItemName(), "admin", "listRoles not including 'admin' for filter [admin]");
        FlaggedName[] roles2 = userMgtClient.listRoles("testRole", -1);
        Assert.assertEquals(roles2[0].getItemName(), "testRole", "listRoles not including 'testRole' for filter [testRole]");
    }


    @Test(groups = "wso2.is", description = "Get the roles by user name", priority = 3)
    public void testGetRolesByUserName() throws Exception {
        FlaggedName[] names = userMgtClient.getRolesOfUser("lanka", "admin", -1);
        Assert.assertEquals(names[0].getItemName(), "admin", "Returned role is not 'admin' for user 'lanka'");

    }

    @Test(groups = "wso2.is", description = "Check user existence", priority = 4)
    public void testUserNameExist() throws Exception {
        Boolean isExist = userMgtClient.userNameExists("admin", "lanka");
        Assert.assertTrue(isExist, "Existing user 'lanka' is not detected");

    }

    @Test(groups = "wso2.is", description = "Check role existence", priority = 5)
    public void testRoleNameExist() throws Exception {
        Boolean isExist = userMgtClient.roleNameExists("admin");
        Assert.assertTrue(isExist, "Role 'admin' is not found");
        Boolean isExistEveryoneRole = userMgtClient.roleNameExists("Internal/everyone");
        Assert.assertTrue(isExistEveryoneRole, "Role 'Internal/Everyone' is not found");

    }

    @Test(groups = "wso2.is", description = "Check role addition", priority = 6)
    public void testAddRole() throws Exception {
        userMgtClient.addRole("architect", new String[]{"lanka"}, new String[]{"login"}, false);
        FlaggedName[] roles = userMgtClient.getRolesOfUser("lanka", "architect", -1);
        Boolean isIncluded = false;
        for (FlaggedName role : roles) {
            if (role.getItemName().equalsIgnoreCase("architect")) {
                isIncluded = true;
            }
        }
        Assert.assertTrue(isIncluded, "Added role with user 'lanka' could not be retrieved.");
    }
    
    @Test(groups = "wso2.is", description = "Check internal role operations")
    public void testAddDeleteInternalRoleOperations() throws Exception {

    	FlaggedName[] allRoles = null;
    	String[] permissionList = new String[]{"/permission"};
    	
//    	Test add internal role without user
    	userMgtClient.addInternalRole("manager_", null, permissionList);    	
    	allRoles = userMgtClient.getAllRolesNames("manager_", 0);
    	
    	Assert.assertTrue(roleExists(allRoles, "Internal/manager_"), "The internal role add has failed");
    	
    	userMgtClient.deleteRole("Internal/manager_");
    	allRoles = userMgtClient.getAllRolesNames("manager_", 0);
    	Assert.assertFalse(roleExists(allRoles, "Internal/manager_"), "The internal role without user delete has failed");
    	
    	String[] userList = new String[]{"lanka1_"};
//    	Test add internal role with user
    	userMgtClient.addInternalRole("sales_", userList, permissionList);    	
    	allRoles = userMgtClient.getAllRolesNames("sales_", 0);
    	
    	Assert.assertTrue(roleExists(allRoles, "Internal/sales_"), "The internal role add has failed");
		  	  	
    	userMgtClient.deleteRole("Internal/sales_");
    	allRoles = userMgtClient.getAllRolesNames("manager_", 0);
    	Assert.assertFalse(roleExists(allRoles, "Internal/sales_"), "The internal role with user delete has failed");
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        userMgtClient.deleteUser("lanka");
        userMgtClient.deleteRole("architect");    
    }
    
    /**
     * Checks whether the passed roleName exists in the allRoles.
     * 
     * @param allRoles
     * @param roleName
     * @return
     */
	private boolean roleExists(FlaggedName[] allRoles, String roleName) {
		boolean roleExists = false;

		for (FlaggedName flaggedName : allRoles) {
			String name = flaggedName.getItemName();

			if (name.equals(roleName)) {
				roleExists = true;
				break;
			} else {
				roleExists = false;
			}
		}

		return roleExists;
	}
}