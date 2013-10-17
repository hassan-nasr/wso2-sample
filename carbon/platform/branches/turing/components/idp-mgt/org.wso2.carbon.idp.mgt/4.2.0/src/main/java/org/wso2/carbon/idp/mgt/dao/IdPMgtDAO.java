/*
*Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.idp.mgt.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.idp.mgt.exception.IdentityProviderMgtException;
import org.wso2.carbon.idp.mgt.model.TrustedIdPDO;
import org.wso2.carbon.idp.mgt.util.IdentityProviderMgtConstants;
import org.wso2.carbon.idp.mgt.util.IdentityProviderMgtUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class IdPMgtDAO {

    private static final Log log = LogFactory.getLog(IdPMgtDAO.class);

    public List<String> getTenantIdPs(Connection dbConnection, int tenantId, String tenantDomain) throws IdentityProviderMgtException {

        boolean dbConnInitialized = true;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        List<String> tenantIdPs = new ArrayList<String>();
        try {
            if(dbConnection == null){
                dbConnection = IdentityProviderMgtUtil.getDBConnection();
            } else {
                dbConnInitialized = false;
            }
            String sqlStmt = IdentityProviderMgtConstants.SQLQueries.GET_TENANT_IDPS_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            rs = prepStmt.executeQuery();
            while(rs.next()){
                tenantIdPs.add(rs.getString(1));
            }
            return tenantIdPs;
        } catch (SQLException e){
            String msg = "Error occurred while retrieving registered IdP Issuers for tenant " + tenantDomain;
            log.error(msg, e);
            IdentityProviderMgtUtil.rollBack(dbConnection);
            throw new IdentityProviderMgtException(msg);
        } finally {
            if(dbConnInitialized){
                IdentityProviderMgtUtil.closeConnection(dbConnection);
            }
        }
    }

    public TrustedIdPDO getTenantIdP(String idPName, int tenantId, String tenantDomain) throws IdentityProviderMgtException {
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        TrustedIdPDO trustedIdPDO = null;
        try {
            dbConnection = IdentityProviderMgtUtil.getDBConnection();
            String sqlStmt = IdentityProviderMgtConstants.SQLQueries.GET_TENANT_IDP_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, idPName);
            rs = prepStmt.executeQuery();
            if(rs.next()){
                trustedIdPDO = new TrustedIdPDO();
                int idPId = rs.getInt(1);
                trustedIdPDO.setIdPName(idPName);
                trustedIdPDO.setIdPIssuerId(rs.getString(2));
                trustedIdPDO.setIdPUrl(rs.getString(3));
                trustedIdPDO.setPublicCertThumbPrint(rs.getString(4));
                if(rs.getString(5).equals("1")){
                    trustedIdPDO.setPrimary(true);
                } else {
                    trustedIdPDO.setPrimary(false);
                }
                String audience = rs.getString(6);
                if(audience != null){
                    trustedIdPDO.setAudience(new ArrayList<String>(Arrays.asList(rs.getString(6).split("\\s"))));
                } else {
                    trustedIdPDO.setAudience(new ArrayList<String>());
                }
                trustedIdPDO.setTokenEPAlias(rs.getString(7));

                Map<String, Integer> roleIdMap = new HashMap<String, Integer>();
                sqlStmt = IdentityProviderMgtConstants.SQLQueries.GET_TENANT_IDP_ROLES_SQL;
                prepStmt = dbConnection.prepareStatement(sqlStmt);
                prepStmt.setInt(1, idPId);
                rs = prepStmt.executeQuery();
                while(rs.next()){
                    int id = rs.getInt(1);
                    String role = rs.getString(2);
                    roleIdMap.put(role, id);
                }
                trustedIdPDO.setRoles(new ArrayList<String>(roleIdMap.keySet()));

                Map<String,String> roleMapping = new HashMap<String, String>();
                for(Map.Entry<String,Integer> roleId:roleIdMap.entrySet()){
                    String idPRole = roleId.getKey();
                    int id = roleId.getValue();
                    sqlStmt = IdentityProviderMgtConstants.SQLQueries.GET_TENANT_IDP_ROLE_MAPPINGS_SQL;
                    prepStmt = dbConnection.prepareStatement(sqlStmt);
                    prepStmt.setInt(1, id);
                    rs = prepStmt.executeQuery();
                    while(rs.next()){
                        String tenantRole = rs.getString(1);
                        if(roleMapping.containsKey(idPRole)){
                            roleMapping.put(idPRole, roleMapping.get(idPRole)+ "," +tenantRole);
                        } else {
                            roleMapping.put(idPRole, tenantRole);
                        }
                    }
                }
                trustedIdPDO.setRoleMappings(roleMapping);
            }
            return trustedIdPDO;
        } catch (SQLException e){
            String msg = "Error occurred while retrieving IdP information for tenant " + tenantDomain;
            log.error(msg, e);
            IdentityProviderMgtUtil.rollBack(dbConnection);
            throw new IdentityProviderMgtException(msg);
        } finally {
            IdentityProviderMgtUtil.closeConnection(dbConnection);
        }
    }

    public void addTenantIdP(TrustedIdPDO trustedIdP, int tenantId, String tenantDomain) throws IdentityProviderMgtException {
        Connection dbConnection = null;
        try {
            dbConnection = IdentityProviderMgtUtil.getDBConnection();
            String idPName = trustedIdP.getIdPName();
            String issuerId = trustedIdP.getIdPIssuerId();
            boolean isPrimary = trustedIdP.isPrimary();
            String trustedIdPUrl = trustedIdP.getIdPUrl();
            String thumbPrint = trustedIdP.getPublicCertThumbPrint();
            List<String> roles = trustedIdP.getRoles();
            Map<String,String> roleMappings = trustedIdP.getRoleMappings();
            if(isPrimary){
                doSwitchPrimary(dbConnection, tenantId);
            }
            String audience = IdentityProviderMgtUtil.convertListToString(trustedIdP.getAudience());
            String tokenEPAlias = trustedIdP.getTokenEPAlias();

            doAddIdP(dbConnection, tenantId, idPName, issuerId, trustedIdPUrl, thumbPrint, isPrimary, audience, tokenEPAlias);
            int idPId = isTenantIdPExisting(dbConnection, trustedIdP, tenantId, tenantDomain);
            if(idPId <= 0){
                String msg = "Error adding trusted IdP for tenant " + tenantDomain;
                log.error(msg);
                throw new IdentityProviderMgtException(msg);
            }
            if(roles != null && roles.size() > 0){
                doAddIdPRoles(dbConnection, idPId, roles);
            }
            if(roleMappings != null && roleMappings.size() > 0){
                doAddIdPRoleMappings(dbConnection, idPId, tenantId, roleMappings, tenantDomain);
            }
            dbConnection.commit();
        } catch (SQLException e){
            String msg = "Error occurred while adding IdP for tenant";
            IdentityProviderMgtUtil.rollBack(dbConnection);
            log.error(msg + " " + tenantDomain, e);
            throw new IdentityProviderMgtException(msg);
        } finally {
            IdentityProviderMgtUtil.closeConnection(dbConnection);
        }
    }

    public void updateTenantIdP(TrustedIdPDO trustedIdPDO1, TrustedIdPDO trustedIdPDO2, int tenantId, String tenantDomain) throws IdentityProviderMgtException {

        Connection dbConnection = null;

        String idPName1 = trustedIdPDO1.getIdPName();
        String issuerId1 = trustedIdPDO1.getIdPIssuerId();
        boolean isPrimary1 = false;
        if(trustedIdPDO1.isPrimary()){
            isPrimary1 = true;
        }
        String trustedIdPUrl1 = trustedIdPDO1.getIdPUrl();
        String thumbPrint1 = trustedIdPDO1.getPublicCertThumbPrint();
        List<String> roles1 = trustedIdPDO1.getRoles();
        Map<String,String> roleMappings1 = trustedIdPDO1.getRoleMappings();
        String audience1 = IdentityProviderMgtUtil.convertListToString(trustedIdPDO1.getAudience());
        String tokenEPAlias1 = trustedIdPDO1.getTokenEPAlias();

        String issuerId2 = trustedIdPDO2.getIdPIssuerId();
        String idPName2 = trustedIdPDO2.getIdPName();
        boolean isPrimary2 = false;
        if(trustedIdPDO2.isPrimary()){
            isPrimary2 =true;
        }
        String trustedIdPUrl2 = trustedIdPDO2.getIdPUrl();
        String thumbPrint2 = trustedIdPDO2.getPublicCertThumbPrint();
        List<String> roles2 = trustedIdPDO2.getRoles();
        Map<String,String> roleMappings2 = trustedIdPDO2.getRoleMappings();
        String audience2 = IdentityProviderMgtUtil.convertListToString(trustedIdPDO2.getAudience());
        String tokenEPAlias2 = trustedIdPDO2.getTokenEPAlias();

        if(roles2.size() < roles1.size()){
            String msg = "Input error: new set of roles cannot be smaller than old set of roles. " + roles2.size() +
                    " < " + roles1.size();
            log.error(msg);
            throw new IdentityProviderMgtException(msg);
        }

        Map<String,String> addedMappings = new HashMap<String, String>();
        Map<String,String> deletedMappings = new HashMap<String, String>();
        for(Map.Entry<String,String> entry:roleMappings1.entrySet()){
            if(!IdentityProviderMgtUtil.containsEntry(roleMappings2, entry)){
                deletedMappings.put(entry.getKey(), entry.getValue());
            }
        }
        for(Map.Entry<String,String> entry:roleMappings2.entrySet()){
            if(!IdentityProviderMgtUtil.containsEntry(roleMappings1, entry)){
                addedMappings.put(entry.getKey(), entry.getValue());
            }
        }
        List<String> renamedOldRoles = new ArrayList<String>();
        List<String> renamedNewRoles = new ArrayList<String>();
        List<String> addedRoles = new ArrayList<String>();
        List<String> deletedRoles = new ArrayList<String>();
        for(int i = 0; i < roles1.size(); i++){
            if(roles2.get(i) == null){
                deletedRoles.add(roles1.get(i));
            }
            if(roles2.get(i) != null && !roles2.get(i).equals(roles1.get(i))){
                renamedOldRoles.add(roles1.get(i));
                renamedNewRoles.add(roles2.get(i));
            }
        }
        for(int i = roles1.size(); i < roles2.size(); i++){
            addedRoles.add(roles2.get(i));
        }

        try {
            dbConnection = IdentityProviderMgtUtil.getDBConnection();
            int idPId = isTenantIdPExisting(dbConnection, trustedIdPDO1, tenantId, tenantDomain);
            if(idPId <= 0){
                String msg = "Trying to update non-existent IdP for tenant " + tenantDomain;
                log.error(msg);
                throw new IdentityProviderMgtException(msg);
            }
            if(idPName1 != idPName2 ||
                    (issuerId1 != null && issuerId2 != null && !issuerId1.equals(issuerId2) ||
                            issuerId1!= null && issuerId2 == null ||
                            issuerId1 == null && trustedIdPUrl2 != null) ||
                    (trustedIdPUrl1 != null && issuerId2 != null && !issuerId1.equals(issuerId2) ||
                            trustedIdPUrl1!= null && trustedIdPUrl2 == null ||
                            trustedIdPUrl1 == null && trustedIdPUrl2 != null) ||
                    (thumbPrint1 != null && thumbPrint2 != null && !thumbPrint1.equals(thumbPrint2) ||
                            thumbPrint1 != null && thumbPrint2 == null ||
                            thumbPrint1 == null && thumbPrint2 != null) ||
                    isPrimary1 != isPrimary2 ||
                    audience1 != null && audience2 != null && !audience1.equals(audience2) ||
                    audience1!= null && audience2 == null ||
                    audience1 == null && audience2 != null ||
                    tokenEPAlias1 != null && tokenEPAlias2 != null && !tokenEPAlias1.equals(tokenEPAlias2)) {
                if(isPrimary1 != isPrimary2){
                    doSwitchPrimary(dbConnection, tenantId);
                }
                doUpdateIdP(dbConnection, tenantId, idPName1, idPName2, issuerId2, trustedIdPUrl2, thumbPrint2, isPrimary2, audience2, tokenEPAlias2);
            }
            if(!addedRoles.isEmpty() || !deletedRoles.isEmpty() || !renamedOldRoles.isEmpty()){
                doUpdateIdPRoles(dbConnection, idPId, addedRoles, deletedRoles, renamedOldRoles, renamedNewRoles);
            }
            if(!addedMappings.isEmpty() || !deletedMappings.isEmpty()){
                doUpdateRoleMappings(dbConnection, idPId, tenantId, renamedOldRoles, renamedNewRoles, addedMappings,
                        deletedMappings, tenantDomain);
            }
            dbConnection.commit();
        } catch(SQLException e){
            String msg = "Error occurred while updating IdP information  for tenant " + tenantDomain;
            log.error(msg, e);
            IdentityProviderMgtUtil.rollBack(dbConnection);
            throw new IdentityProviderMgtException(msg);
        } finally {
            IdentityProviderMgtUtil.closeConnection(dbConnection);
        }
    }

    public void deleteTenantIdP(TrustedIdPDO trustedIdP, int tenantId, String tenantDomain) throws IdentityProviderMgtException {
        Connection dbConnection = null;
        try {
            dbConnection = IdentityProviderMgtUtil.getDBConnection();
            String idPName = trustedIdP.getIdPName();
            int idPId = isTenantIdPExisting(dbConnection, trustedIdP, tenantId, tenantDomain);
            if(idPId <= 0){
                String msg = "Trying to delete non-existent IdP for tenant " + tenantDomain;
                log.error(msg);
                return;
            }

            trustedIdP.setPrimary(true);
            int primaryIdPId = isPrimaryTenantIdPExisting(dbConnection, trustedIdP, tenantId, tenantDomain);
            if(primaryIdPId <= 0){
                String msg = "Cannot find primary IdP for tenant " + tenantDomain;
                log.warn(msg);
            }

            doDeleteIdP(dbConnection, tenantId, idPName);

            if(idPId == primaryIdPId){
                doAppointPrimary(dbConnection, tenantId, tenantDomain);
            }

            dbConnection.commit();
        } catch (SQLException e){
            String msg = "Error occurred while deleting IdP of tenant " + tenantDomain;
            log.error(msg, e);
            IdentityProviderMgtUtil.rollBack(dbConnection);
            throw new IdentityProviderMgtException(msg);
        } finally {
            IdentityProviderMgtUtil.closeConnection(dbConnection);
        }
    }

    private void doSwitchPrimary(Connection conn, int tenantId) throws SQLException {
        PreparedStatement prepStmt = null;
        String sqlStmt = IdentityProviderMgtConstants.SQLQueries.SWITCH_TENANT_IDP_PRIMARY_SQL;
        prepStmt = conn.prepareStatement(sqlStmt);
        prepStmt.setString(1, "0");
        prepStmt.setInt(2, tenantId);
        prepStmt.setString(3, "1");
        prepStmt.executeUpdate();
    }

    private void doAppointPrimary(Connection conn, int tenantId, String tenantDomain) throws SQLException, IdentityProviderMgtException {
        List<String> tenantIdPs = getTenantIdPs(conn, tenantId, tenantDomain);
        if(!tenantIdPs.isEmpty()){
            PreparedStatement prepStmt = null;
            String sqlStmt = IdentityProviderMgtConstants.SQLQueries.SWITCH_TENANT_IDP_PRIMARY_ON_DELETE_SQL;
            prepStmt = conn.prepareStatement(sqlStmt);
            prepStmt.setString(1, "1");
            prepStmt.setInt(2, tenantId);
            prepStmt.setString(3, tenantIdPs.get(0));
            prepStmt.setString(4, "0");
            prepStmt.executeUpdate();
        } else {
            String msg = "No IdPs registered for tenant " + tenantDomain;
            log.warn(msg);
        }
    }

    private void doAddIdP(Connection conn, int tenantId, String idPName, String issuer, String idpUrl, String thumbPrint,
                          boolean isPrimary, String audience, String tokenEPAlias) throws SQLException {
        PreparedStatement prepStmt = null;
        String sqlStmt = IdentityProviderMgtConstants.SQLQueries.ADD_TENANT_IDP_SQL;
        prepStmt = conn.prepareStatement(sqlStmt);
        prepStmt.setInt(1, tenantId);
        prepStmt.setString(2, idPName);
        prepStmt.setString(3, issuer);
        prepStmt.setString(4, idpUrl);
        prepStmt.setString(5, thumbPrint);
        if(isPrimary){
            prepStmt.setString(6, "1");
        } else {
            prepStmt.setString(6, "0");
        }
        prepStmt.setString(7, audience);
        prepStmt.setString(8, tokenEPAlias);

        prepStmt.executeUpdate();
    }

    private void doAddIdPRoles(Connection conn, int idPId, List<String> roles) throws SQLException {
        PreparedStatement prepStmt = null;
        String sqlStmt = IdentityProviderMgtConstants.SQLQueries.ADD_TENANT_IDP_ROLES_SQL;
        for(String role:roles){
            prepStmt = conn.prepareStatement(sqlStmt);
            prepStmt.setInt(1, idPId);
            prepStmt.setString(2, role);
            prepStmt.executeUpdate();
        }
    }

    private void doAddIdPRoleMappings(Connection conn, int idPId, int tenantId, Map<String,String> roleMappings, String tenantDomain)
            throws SQLException, IdentityProviderMgtException {
        Map<String, Integer> roleIdMap = new HashMap<String, Integer>();
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String sqlStmt = IdentityProviderMgtConstants.SQLQueries.GET_TENANT_IDP_ROLES_SQL;
        prepStmt = conn.prepareStatement(sqlStmt);
        prepStmt.setInt(1, idPId);
        rs = prepStmt.executeQuery();
        while(rs.next()){
            int id = rs.getInt(1);
            String role = rs.getString(2);
            roleIdMap.put(role, id);
        }
        if(roleIdMap.isEmpty()){
            String message = "No IdP roles defined for tenant " + tenantDomain;
            log.error(message);
            throw new IdentityProviderMgtException(message);
        }
        for(Map.Entry<String,String> entry : roleMappings.entrySet()){
            if(roleIdMap.containsKey(entry.getKey())){
                int idpRoleId = roleIdMap.get(entry.getKey());
                String localRole = entry.getValue();
                sqlStmt = IdentityProviderMgtConstants.SQLQueries.ADD_TENANT_IDP_ROLE_MAPPINGS_SQL;
                prepStmt = conn.prepareStatement(sqlStmt);
                prepStmt.setInt(1, idpRoleId);
                prepStmt.setInt(2, tenantId);
                prepStmt.setString(3, localRole);
                prepStmt.executeUpdate();
            } else {
                String msg = "Cannot find IdP role " + entry.getKey() + " for tenant " + tenantDomain;
                log.error(msg);
                throw new IdentityProviderMgtException(msg);
            }
        }
    }
    
    private void doUpdateIdP(Connection conn, int tenantId, String oldIdpName, String newIdPName, String issuer, String idpUrl,
                             String thumbPrint, boolean isPrimary, String audience, String tokenEPAlias) throws SQLException {
        PreparedStatement prepStmt = null;
        String sqlStmt = IdentityProviderMgtConstants.SQLQueries.UPDATE_TENANT_IDP_SQL;
        prepStmt = conn.prepareStatement(sqlStmt);
        prepStmt.setString(1, newIdPName);
        prepStmt.setString(2, issuer);
        prepStmt.setString(3, idpUrl);
        prepStmt.setString(4, thumbPrint);
        if(isPrimary){
            prepStmt.setString(5, "1");
        } else {
            prepStmt.setString(5, "0");
        }
        prepStmt.setString(6, audience);
        prepStmt.setString(7, tokenEPAlias);
        prepStmt.setInt(8, tenantId);
        prepStmt.setString(9, oldIdpName);
        prepStmt.executeUpdate();
    }

    private void doUpdateIdPRoles(Connection conn, int idPId, List<String> addedRoles, List<String> deletedRoles,
                                  List<String> renamedOldRoles, List<String> renamedNewRoles)
            throws SQLException {
        PreparedStatement prepStmt = null;
        String sqlStmt = null;
        for(String deletedRole:deletedRoles){
            sqlStmt = IdentityProviderMgtConstants.SQLQueries.DELETE_TENANT_IDP_ROLES_SQL;
            prepStmt = conn.prepareStatement(sqlStmt);
            prepStmt.setInt(1, idPId);
            prepStmt.setString(2, deletedRole);
            prepStmt.executeUpdate();
        }
        for(String addedRole:addedRoles){
            sqlStmt = IdentityProviderMgtConstants.SQLQueries.ADD_TENANT_IDP_ROLES_SQL;
            prepStmt = conn.prepareStatement(sqlStmt);
            prepStmt.setInt(1, idPId);
            prepStmt.setString(2, addedRole);
            prepStmt.executeUpdate();
        }
        for(int i = 0; i < renamedOldRoles.size(); i++){
            sqlStmt = IdentityProviderMgtConstants.SQLQueries.UPDATE_TENANT_IDP_ROLES_SQL;
            prepStmt = conn.prepareStatement(sqlStmt);
            prepStmt.setString(1, renamedNewRoles.get(i));
            prepStmt.setInt(2, idPId);
            prepStmt.setString(3, renamedOldRoles.get(i));
            prepStmt.executeUpdate();
        }
    }

    private void doUpdateRoleMappings(Connection conn, int idPId, int tenantId, List<String> renamedOldRoles,
                                      List<String> renamedNewRoles, Map<String,String> addedRoleMappings,
                                      Map<String,String> deletedRoleMappings, String tenantDomain)
            throws SQLException, IdentityProviderMgtException {
        Map<String, Integer> roleIdMap = new HashMap<String, Integer>();
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String sqlStmt = IdentityProviderMgtConstants.SQLQueries.GET_TENANT_IDP_ROLES_SQL;
        prepStmt = conn.prepareStatement(sqlStmt);
        prepStmt.setInt(1, idPId);
        rs = prepStmt.executeQuery();
        while(rs.next()){
            int id = rs.getInt(1);
            String role = rs.getString(2);
            roleIdMap.put(role, id);
        }
        if(roleIdMap.isEmpty()){
            String msg = "No IdP roles defined for tenant " + tenantDomain;
            log.error(msg);
            throw new IdentityProviderMgtException(msg);
        }
        if(!deletedRoleMappings.isEmpty()){
            Map<String,String> temp = new HashMap<String, String>();
            for(Map.Entry<String,String> entry : deletedRoleMappings.entrySet()){
                if(renamedOldRoles.contains(entry.getKey())){
                    int index = renamedOldRoles.indexOf(entry.getKey());
                    String value = renamedNewRoles.get(index);
                    temp.put(value, entry.getValue());
                } else {
                    temp.put(entry.getKey(), entry.getValue());
                }
            }
            deletedRoleMappings = temp;
            for(Map.Entry<String,String> entry : deletedRoleMappings.entrySet()){
                if(roleIdMap.containsKey(entry.getKey())){
                    int idpRoleId = roleIdMap.get(entry.getKey());
                    String localRole = entry.getValue();
                    sqlStmt = IdentityProviderMgtConstants.SQLQueries.DELETE_TENANT_IDP_ROLE_MAPPINGS_SQL;
                    prepStmt = conn.prepareStatement(sqlStmt);
                    prepStmt.setInt(1, idpRoleId);
                    prepStmt.setInt(2, tenantId);
                    prepStmt.setString(3, localRole);
                    prepStmt.executeUpdate();
                } else {
                    String msg = "Cannot find IdP role " + entry.getKey() + " for tenant " + tenantDomain;
                    log.error(msg);
                    throw new IdentityProviderMgtException(msg);
                }
            }
        }
        if(!addedRoleMappings.isEmpty()){
            for(Map.Entry<String,String> entry : addedRoleMappings.entrySet()){
                if(roleIdMap.containsKey(entry.getKey())){
                    int idpRoleId = roleIdMap.get(entry.getKey());
                    String localRole = entry.getValue();
                    sqlStmt = IdentityProviderMgtConstants.SQLQueries.ADD_TENANT_IDP_ROLE_MAPPINGS_SQL;
                    prepStmt = conn.prepareStatement(sqlStmt);
                    prepStmt.setInt(1, idpRoleId);
                    prepStmt.setInt(2, tenantId);
                    prepStmt.setString(3, localRole);
                    prepStmt.executeUpdate();
                } else {
                    String msg = "Cannot find IdP role " + entry.getKey() + " for tenant " + tenantDomain;
                    log.error(msg);
                    throw new IdentityProviderMgtException(msg);
                }
            }
        }
    }

    private void doDeleteIdP(Connection conn, int tenantId, String idPName) throws SQLException {
        PreparedStatement prepStmt = null;
        String sqlStmt = IdentityProviderMgtConstants.SQLQueries.DELETE_TENANT_IDP_SQL;
        prepStmt = conn.prepareStatement(sqlStmt);
        prepStmt.setInt(1, tenantId);
        prepStmt.setString(2, idPName);
        prepStmt.executeUpdate();
    }

    public int isTenantIdPExisting(Connection dbConnection, TrustedIdPDO trustedIdPDO, int tenantId, String tenantDomain)
            throws IdentityProviderMgtException {

        boolean dbConnInitialized = true;
        PreparedStatement prepStmt = null;
        try {
            if(dbConnection == null){
                dbConnection = IdentityProviderMgtUtil.getDBConnection();
            } else {
                dbConnInitialized = false;
            }
            String sqlStmt = IdentityProviderMgtConstants.SQLQueries.IS_EXISTING_TENANT_IDP_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, trustedIdPDO.getIdPName());
            ResultSet rs = prepStmt.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving IdP information for tenant " + tenantDomain;
            log.error(msg, e);
            throw new IdentityProviderMgtException(msg);
        } finally {
            if(dbConnInitialized){
                IdentityProviderMgtUtil.closeConnection(dbConnection);
            }
        }
        return 0;
    }

    public int isPrimaryTenantIdPExisting(Connection dbConnection, TrustedIdPDO trustedIdPDO, int tenantId, String tenantDomain)
            throws IdentityProviderMgtException {
        PreparedStatement prepStmt = null;
        try {
            String sqlStmt = IdentityProviderMgtConstants.SQLQueries.IS_EXISTING_PRIMARY_TENANT_IDP_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, "1");
            ResultSet rs = prepStmt.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving IdP information for tenant " + tenantDomain;
            log.error(msg, e);
            throw new IdentityProviderMgtException(msg);
        }
        return 0;
    }

    public void deleteTenantRole(int tenantId, String role, String tenantDomain) throws IdentityProviderMgtException {
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        try {
            dbConnection = IdentityProviderMgtUtil.getDBConnection();
            String sqlStmt = IdentityProviderMgtConstants.SQLQueries.DELETE_TENANT_ROLE_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, role);
            prepStmt.executeUpdate();
            dbConnection.commit();
        } catch (SQLException e) {
            String msg = "Error occurred while deleting tenant role " + role + " of tenant " + tenantDomain;
            log.error(msg, e);
            IdentityProviderMgtUtil.rollBack(dbConnection);
            throw new IdentityProviderMgtException(msg);
        } finally {
            IdentityProviderMgtUtil.closeConnection(dbConnection);
        }
    }

    public void renameTenantRole(String newRoleName, int tenantId, String oldRoleName, String tenantDomain) throws IdentityProviderMgtException {
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        try {
            dbConnection = IdentityProviderMgtUtil.getDBConnection();
            String sqlStmt = IdentityProviderMgtConstants.SQLQueries.RENAME_TENANT_ROLE_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, newRoleName);
            prepStmt.setInt(2, tenantId);
            prepStmt.setString(3, oldRoleName);
            prepStmt.executeUpdate();
            dbConnection.commit();
        } catch (SQLException e) {
            String msg = "Error occurred while renaming tenant role " + oldRoleName +
                    " to " + newRoleName + " of tenant " + tenantDomain;
            log.error(msg, e);
            IdentityProviderMgtUtil.rollBack(dbConnection);
            throw new IdentityProviderMgtException(msg);
        } finally {
            IdentityProviderMgtUtil.closeConnection(dbConnection);
        }
    }

}
