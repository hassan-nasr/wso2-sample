/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.user.core.hybrid;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.authorization.AuthorizationCache;
import org.wso2.carbon.user.core.common.UserRolesCache;
import org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

public class HybridRoleManager {

	private static Log log = LogFactory.getLog(JDBCUserStoreManager.class);

	private DataSource dataSource;
	int tenantId;
	private RealmConfiguration realmConfig;
	protected UserRealm userRealm = null;
	protected UserRolesCache userRolesCache = null;
	private boolean userRolesCacheEnabled = true;

	private final int DEFAULT_MAX_ROLE_LIST_SIZE = 1000;
	private final int DEFAULT_MAX_SEARCH_TIME = 1000;

	public HybridRoleManager(DataSource dataSource, int tenantId, RealmConfiguration realmConfig,
			UserRealm realm) throws UserStoreException {
		super();
		this.dataSource = dataSource;
		this.tenantId = tenantId;
		this.realmConfig = realmConfig;
		this.userRealm = realm;
        //persist internal domain
        UserCoreUtil.persistDomain(UserCoreConstants.INTERNAL_DOMAIN, tenantId, dataSource);

	}

	/**
	 * 
	 * @param roleName Domain-less role
	 * @param userList Domain-aware user list
	 * @throws UserStoreException
	 */
	public void addHybridRole(String roleName, String[] userList) throws UserStoreException {
		Connection dbConnection = null;
		try {

			// ########### Domain-less Roles and Domain-aware Users from here onwards #############

			// This method is always invoked by the primary user store manager.
			String primaryDomainName = getMyDomainName();
			
			if (primaryDomainName!=null){
				primaryDomainName = primaryDomainName.toUpperCase();
			}

			dbConnection = getDBConnection();

			if (!this.isExistingRole(roleName)) {
				DatabaseUtil.updateDatabase(dbConnection, HybridJDBCConstants.ADD_ROLE_SQL,
						roleName, tenantId);
			} else {
				throw new UserStoreException("Role name: " + roleName
						+ " in the system. Please pick another role name.");
			}
			if (userList != null) {
				String sql = HybridJDBCConstants.ADD_USER_TO_ROLE_SQL;
				String type = DatabaseCreator.getDatabaseType(dbConnection);
				if (UserCoreConstants.MSSQL_TYPE.equals(type)) {
					sql = HybridJDBCConstants.ADD_USER_TO_ROLE_SQL_MSSQL;
				}
				if (UserCoreConstants.OPENEDGE_TYPE.equals(type)) {
					sql = HybridJDBCConstants.ADD_USER_TO_ROLE_SQL_OPENEDGE;
					DatabaseUtil.udpateUserRoleMappingInBatchModeForInternalRoles(dbConnection,
							sql, primaryDomainName, userList, tenantId, roleName, tenantId);
				} else {
					DatabaseUtil.udpateUserRoleMappingInBatchModeForInternalRoles(dbConnection,
							sql, primaryDomainName, userList, roleName, tenantId, tenantId, tenantId);
				}
			}
			dbConnection.commit();
		} catch (SQLException e) {
			throw new UserStoreException(e.getMessage(), e);
		} catch (Exception e) {
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection);
		}
	}

	/**
	 * 
	 * @param tenantID
	 */
	protected void clearUserRolesCacheByTenant(int tenantID) {
		if (userRolesCache != null) {
			userRolesCache.clearCacheByTenant(tenantID);
			AuthorizationCache authorizationCache = AuthorizationCache.getInstance();
			authorizationCache.clearCacheByTenant(tenantID);
		}
	}

	/**
	 * 
	 * @param roleName
	 * @return
	 * @throws UserStoreException
	 */
	public boolean isExistingRole(String roleName) throws UserStoreException {

		Connection dbConnection = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		boolean isExisting = false;

		try {

			// ########### Domain-less Roles and Domain-aware Users from here onwards #############

			dbConnection = getDBConnection();
			prepStmt = dbConnection.prepareStatement(HybridJDBCConstants.GET_ROLE_ID);
			prepStmt.setString(1, roleName);
			prepStmt.setInt(2, tenantId);
			rs = prepStmt.executeQuery();
			if (rs.next()) {
				int value = rs.getInt(1);
				if (value > -1) {
					isExisting = true;
				}
			}
			return isExisting;
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
		}
	}

	/**
	 * 
	 * @param filter
	 * @return
	 * @throws UserStoreException
	 */
	public String[] getHybridRoles(String filter) throws UserStoreException {

		Connection dbConnection = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;

		String sqlStmt = HybridJDBCConstants.GET_ROLES;
		int maxItemLimit = UserCoreConstants.MAX_USER_ROLE_LIST;
		int searchTime = UserCoreConstants.MAX_SEARCH_TIME;

		try {
			maxItemLimit = Integer.parseInt(realmConfig
					.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_ROLE_LIST));
		} catch (Exception e) {
			maxItemLimit = DEFAULT_MAX_ROLE_LIST_SIZE;
		}

		try {
			searchTime = Integer.parseInt(realmConfig
					.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_SEARCH_TIME));
		} catch (Exception e) {
			searchTime = DEFAULT_MAX_SEARCH_TIME;
		}

		try {
			if (filter != null && filter.trim().length() != 0) {
				filter = filter.trim();
				filter = filter.replace("*", "%");
				filter = filter.replace("?", "_");
			} else {
				filter = "%";
			}

			dbConnection = getDBConnection();

			if (dbConnection == null) {
				throw new UserStoreException("null connection");
			}

			dbConnection.setAutoCommit(false);
			dbConnection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

			prepStmt = dbConnection.prepareStatement(sqlStmt);
			prepStmt.setString(1, filter);
			if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
				prepStmt.setInt(2, tenantId);
			}
			prepStmt.setMaxRows(maxItemLimit);
			prepStmt.setQueryTimeout(searchTime);

			List<String> filteredRoles = new ArrayList<String>();

			try {
				rs = prepStmt.executeQuery();
			} catch (SQLException e) {
				log.error("Error while retrieving roles from Internal JDBC role store", e);
				// May be due time out, therefore ignore this exception
			}

			if (rs != null) {
				while (rs.next()) {
					String name = rs.getString(1);
					// Append the domain
					name = UserCoreConstants.INTERNAL_DOMAIN + CarbonConstants.DOMAIN_SEPARATOR
							+ name;
					filteredRoles.add(name);
				}
			}
			return filteredRoles.toArray(new String[filteredRoles.size()]);
		} catch (SQLException e) {
			if (log.isDebugEnabled()) {
				log.debug("Using sql : " + sqlStmt);
			}
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
		}
	}

	/**
	 * 
	 * @param roleName
	 * @return
	 * @throws UserStoreException
	 */
	public String[] getUserListOfHybridRole(String roleName) throws UserStoreException {

		if (UserCoreUtil.isEveryoneRole(roleName, realmConfig)) {
			throw new UserStoreException(
					"Invalid operation. You are trying to retrieve all users from the external userstore.");
		}

		// ########### Domain-less Roles and Domain-aware Users from here onwards #############

		String sqlStmt = HybridJDBCConstants.GET_USER_LIST_OF_ROLE_SQL;
		Connection dbConnection = null;
		try {
			dbConnection = getDBConnection();
			String[] names = DatabaseUtil.getStringValuesFromDatabaseForInternalRoles(dbConnection, sqlStmt,
					roleName, tenantId, tenantId);
			return names;
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection);
		}
	}

	/**
	 * 
	 * @param roleName
	 * @param deletedUsers
	 * @param newUsers
	 * @throws UserStoreException
	 */
	public void updateUserListOfHybridRole(String roleName, String[] deletedUsers, String[] newUsers)
			throws UserStoreException {

		String sqlStmt1 = HybridJDBCConstants.REMOVE_USER_FROM_ROLE_SQL;
		String sqlStmt2 = HybridJDBCConstants.ADD_USER_TO_ROLE_SQL;
		Connection dbConnection = null;

		try {

			// ########### Domain-less Roles and Domain-aware Users from here onwards #############
            String primaryDomainName = getMyDomainName();
            
            if (primaryDomainName != null){
            	primaryDomainName = primaryDomainName.toUpperCase();
            }
            
			dbConnection = getDBConnection();
			String type = DatabaseCreator.getDatabaseType(dbConnection);

			if (UserCoreConstants.MSSQL_TYPE.equals(type)) {
				sqlStmt2 = HybridJDBCConstants.ADD_USER_TO_ROLE_SQL_MSSQL;
			}

			if (deletedUsers != null && deletedUsers.length > 0) {
				DatabaseUtil.udpateUserRoleMappingInBatchModeForInternalRoles(
                        dbConnection, sqlStmt1, primaryDomainName, deletedUsers,
						roleName, tenantId, tenantId, tenantId);
				// authz cache of deleted users from role, needs to be updated
				for (String deletedUser : deletedUsers) {
					userRealm.getAuthorizationManager().clearUserAuthorization(deletedUser);
				}
			}
            
			if (newUsers != null && newUsers.length > 0) {
				if (UserCoreConstants.OPENEDGE_TYPE.equals(type)) {
					sqlStmt2 = HybridJDBCConstants.ADD_USER_TO_ROLE_SQL_OPENEDGE;
					DatabaseUtil.udpateUserRoleMappingInBatchModeForInternalRoles(dbConnection,
							sqlStmt2, primaryDomainName, newUsers, tenantId, roleName, tenantId);
				} else {
					DatabaseUtil.udpateUserRoleMappingInBatchModeForInternalRoles(dbConnection,
							sqlStmt2, primaryDomainName, newUsers, roleName, tenantId, tenantId, tenantId);
				}
			}

			dbConnection.commit();
		} catch (SQLException e) {
			throw new UserStoreException(e.getMessage(), e);
		} catch (Exception e) {
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection);
		}
	}

	/**
	 * 
	 * @param userName
	 * @return
	 * @throws UserStoreException
	 */
	public String[] getHybridRoleListOfUser(String userName) throws UserStoreException {

		String sqlStmt = HybridJDBCConstants.GET_ROLE_LIST_OF_USER_SQL;
		Connection dbConnection = null;
		try {

			userName = UserCoreUtil.addDomainToName(userName, getMyDomainName());
            String domain = UserCoreUtil.extractDomainFromName(userName);
			// ########### Domain-less Roles and Domain-aware Users from here onwards #############

			dbConnection = getDBConnection();
			
			if (domain!=null){
				domain = domain.toUpperCase();
			}

			String[] roles = DatabaseUtil.getStringValuesFromDatabase(dbConnection, sqlStmt,
					UserCoreUtil.removeDomainFromName(userName), tenantId, tenantId, tenantId, domain);

			if (!CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equals(userName)) {
				// Adding everyone role
				if (roles == null || roles.length == 0) {
					return new String[] { realmConfig.getEveryOneRoleName() };
				}
				List<String> allRoles = new ArrayList<String>();
				boolean isEveryone = false;
				for (String role : roles) {
					role = UserCoreConstants.INTERNAL_DOMAIN + CarbonConstants.DOMAIN_SEPARATOR
							+ role;
					if (role.equals(realmConfig.getEveryOneRoleName())) {
						isEveryone = true;
					}
					allRoles.add(role);
				}

				if (!isEveryone) {
					allRoles.add(realmConfig.getEveryOneRoleName());
				}
				return allRoles.toArray(new String[allRoles.size()]);
			} else {
				return roles;
			}
		} catch (SQLException e) {
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection);
		}
	}

	/**
	 * 
	 * @param user
	 * @param deletedRoles
	 * @param addRoles
	 * @throws UserStoreException
	 */
	public void updateHybridRoleListOfUser(String user, String[] deletedRoles, String[] addRoles)
			throws UserStoreException {

		String sqlStmt1 = HybridJDBCConstants.REMOVE_ROLE_FROM_USER_SQL;
		String sqlStmt2 = HybridJDBCConstants.ADD_ROLE_TO_USER_SQL;
		Connection dbConnection = null;

		try {

			user = UserCoreUtil.addDomainToName(user, getMyDomainName());
            String domain = UserCoreUtil.extractDomainFromName(user);
			// ########### Domain-less Roles and Domain-aware Users from here onwards #############

			dbConnection = getDBConnection();
			String type = DatabaseCreator.getDatabaseType(dbConnection);
			if (UserCoreConstants.MSSQL_TYPE.equals(type)) {
				sqlStmt2 = HybridJDBCConstants.ADD_ROLE_TO_USER_SQL_MSSQL;
			}
			
			if (domain!=null){
				domain = domain.toUpperCase();
			}
			
			if (deletedRoles != null && deletedRoles.length > 0) {
				DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt1, deletedRoles,
						tenantId, UserCoreUtil.removeDomainFromName(user), tenantId, tenantId, domain);
			}
			if (addRoles != null && addRoles.length > 0) {
				if (UserCoreConstants.OPENEDGE_TYPE.equals(type)) {
					sqlStmt2 = HybridJDBCConstants.ADD_ROLE_TO_USER_SQL_OPENEDGE;
					DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2, user,
							tenantId, addRoles, tenantId);
				} else {
					DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2, addRoles,
							tenantId, UserCoreUtil.removeDomainFromName(user), tenantId, tenantId, domain);
				}
			}
			dbConnection.commit();
		} catch (SQLException e) {
			throw new UserStoreException(e.getMessage(), e);
		} catch (Exception e) {
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection);
		}
		// Authorization cache of user should also be updated if deleted roles are involved
		if (deletedRoles != null && deletedRoles.length > 0) {
			userRealm.getAuthorizationManager().clearUserAuthorization(user);
		}
	}

	/**
	 * 
	 * @param roleName
	 * @throws UserStoreException
	 */
	public void deleteHybridRole(String roleName) throws UserStoreException {

		// ########### Domain-less Roles and Domain-aware Users from here onwards #############

		if (UserCoreUtil.isEveryoneRole(roleName, realmConfig)) {
			throw new UserStoreException("Invalid operation");
		}

		Connection dbConnection = null;
		try {
			dbConnection = getDBConnection();
			DatabaseUtil.updateDatabase(dbConnection,
					HybridJDBCConstants.ON_DELETE_ROLE_REMOVE_USER_ROLE_SQL, roleName, tenantId,
					tenantId);
			DatabaseUtil.updateDatabase(dbConnection, HybridJDBCConstants.DELETE_ROLE_SQL,
					roleName, tenantId);
			dbConnection.commit();
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection);
		}
		// also need to clear role authorization
		userRealm.getAuthorizationManager().clearRoleAuthorization(roleName);
	}

	/**
	 * 
	 * @param roleName
	 * @param newRoleName
	 * @throws UserStoreException
	 */
	public void updateHybridRoleName(String roleName, String newRoleName) throws UserStoreException {

		// ########### Domain-less Roles and Domain-aware Users from here onwards #############

		if (this.isExistingRole(newRoleName)) {
			throw new UserStoreException("Role name: " + newRoleName
					+ " in the system. Please pick another role name.");
		}

		String sqlStmt = HybridJDBCConstants.UPDATE_ROLE_NAME_SQL;
		if (sqlStmt == null) {
			throw new UserStoreException("The sql statement for update hybrid role name is null");
		}

		Connection dbConnection = null;
		try {

			dbConnection = getDBConnection();
			if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
				DatabaseUtil.updateDatabase(dbConnection, sqlStmt, newRoleName, roleName, tenantId);
			} else {
				DatabaseUtil.updateDatabase(dbConnection, sqlStmt, newRoleName, roleName);
			}
			dbConnection.commit();
			this.userRealm.getAuthorizationManager().resetPermissionOnUpdateRole(roleName,
					newRoleName);
		} catch (SQLException e) {
			if (log.isDebugEnabled()) {
				log.debug("Using sql : " + sqlStmt);
			}
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection);
		}
	}

	/**
	 * ##### This method is not used anywhere
	 * @param userName
	 * @param roleName
	 * @return
	 * @throws UserStoreException
	 */
	public boolean isUserInRole(String userName, String roleName) throws UserStoreException {

		if (UserCoreUtil.isEveryoneRole(roleName, realmConfig)) {
			return true;
		}

		userName = UserCoreUtil.addDomainToName(userName, getMyDomainName());

		// ########### Domain-less Roles and Domain-aware Users from here onwards #############

		Connection dbConnection = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		boolean isUserInRole = false;
		try {
			dbConnection = getDBConnection();
			prepStmt = dbConnection.prepareStatement(HybridJDBCConstants.IS_USER_IN_ROLE_SQL);
			prepStmt.setString(1, userName);
			prepStmt.setString(2, roleName);
			rs = prepStmt.executeQuery();
			if (rs.next()) {
				int value = rs.getInt(1);
				if (value != -1) {
					isUserInRole = true;
				}
			}
			dbConnection.commit();
		} catch (SQLException e) {
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
		}
		return isUserInRole;
	}

	/**
	 * If a user is added to a hybrid role, that entry should be deleted upon deletion of the user.
	 * 
	 * @param userName
	 * @throws UserStoreException
	 */
	public void deleteUser(String userName) throws UserStoreException {

		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;

		userName = UserCoreUtil.addDomainToName(userName, getMyDomainName());
        String domain = UserCoreUtil.extractDomainFromName(userName);
		// ########### Domain-less Roles and Domain-aware Users from here onwards #############

        if (domain!=null){
			domain = domain.toUpperCase();
		}
        
		try {
			dbConnection = getDBConnection();
			preparedStatement = dbConnection.prepareStatement(HybridJDBCConstants.REMOVE_USER_SQL);
			preparedStatement.setString(1, UserCoreUtil.removeDomainFromName(userName));
            preparedStatement.setInt(2, tenantId);
            preparedStatement.setInt(3, tenantId);
            preparedStatement.setString(4, domain);
			preparedStatement.execute();
			dbConnection.commit();
		} catch (SQLException e) {
			throw new UserStoreException(e.getMessage(), e);
		} finally {
			DatabaseUtil.closeAllConnections(dbConnection, preparedStatement);
		}
	}

	/**
	 * 
	 * @return
	 * @throws SQLException
	 */
	private Connection getDBConnection() throws SQLException {
		Connection dbConnection = dataSource.getConnection();
		dbConnection.setAutoCommit(false);
		return dbConnection;
	}

	/**
	 * 
	 */
	protected void initUserRolesCache() {

		String userRolesCacheEnabledString = (realmConfig
				.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_ROLES_CACHE_ENABLED));

		if (userRolesCacheEnabledString != null && !userRolesCacheEnabledString.equals("")) {
			userRolesCacheEnabled = Boolean.parseBoolean(userRolesCacheEnabledString);
			if (log.isDebugEnabled()) {
				log.debug("User Roles Cache is configured to:" + userRolesCacheEnabledString);
			}
		} else {
			if (log.isDebugEnabled()) {
				log.info("User Roles Cache is not configured. Default value: "
						+ userRolesCacheEnabled + " is taken.");
			}
		}

		if (userRolesCacheEnabled) {
			userRolesCache = UserRolesCache.getInstance();
		}
	}

	/**
	 * 
	 * @return
	 */
	protected String getMyDomainName() {
		return UserCoreUtil.getDomainName(realmConfig);
	}

}
