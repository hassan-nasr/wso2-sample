<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<link href="../tenant-dashboard/css/dashboard-common.css" rel="stylesheet" type="text/css" media="all"/>
<%
        Object param = session.getAttribute("authenticated");
        String passwordExpires = (String) session.getAttribute(ServerConstants.PASSWORD_EXPIRATION);
        boolean hasPermission = CarbonUIUtil.isUserAuthorized(request,
		"/permission/admin/manage/mediation");
        boolean loggedIn = false;
        if (param != null) {
            loggedIn = (Boolean) param;             
        } 
%>
  
<div id="passwordExpire">
         <%
         if (loggedIn && passwordExpires != null) {
         %>
              <div class="info-box"><p>Your password expires at <%=passwordExpires%>. Please change by visiting <a href="../user/change-passwd.jsp?isUserChange=true&returnPath=../admin/index.jsp">here</a></p></div>
         <%
             }
         %>
</div>
<div id="middle">
<div id="workArea">
<style type="text/css">
    .tip-table td.cluster-monitoring {
        background-image: url(../../carbon/tenant-dashboard/images/cluster-monitoring.png);
    }

    .tip-table td.column-storage {
        background-image: url(../../carbon/tenant-dashboard/images/column-storage.png);
    }
    .tip-table td.relational-storage{
        background-image: url(../../carbon/tenant-dashboard/images/relational-storage.png);
    }

</style>
 <h2 class="dashboard-title">WSO2 Storage Server Quick Start Dashboard</h2>
        <table class="tip-table">
            <tr>
                <td class="tip-top column-storage"></td>
                <td class="tip-empty"></td>
                <td class="tip-top cluster-monitoring"></td>
                <td class="tip-empty "></td>
                <td class="tip-top relational-storage"></td>
            </tr>
            <tr>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <%
							if (hasPermission) {
						%>
                        <a class="tip-title" href="../cassandramgt/cassandra_keyspaces.jsp?region=region1&item=cassandra_ks_list_menu">Column Storage</a> <br/>
						<%
							} else {
						%>
						<h3>Column Storage</h3> <br/>
						<%
							}
						%>
                        <p>Cassandra Based Column Storage</p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                   	   <%
							if (hasPermission) {
						%>
                        <a class="tip-title" href="../cassandraTools/cassandra_operation.jsp?region=region1&item=cassandra_cluster_operation_menu">Column Storage Cluster Monitoring</a><br/>
                        <%
							} else {
						%>
						<h3>Column Storage Cluster Monitoring</h3><br/>
						<%
							}
						%>
                        <p>Cassandra Cluster Monitoring system. </p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                         <%
							if (hasPermission) {
						%>
                        <a class="tip-title" href="../rssmanager/databases.jsp?region=region1&item=databases_submenu">Relational Storage</a> <br/>
                         <%
							} else {
						%>
						<h3>Relational Storage </h3><br/>
						<%
							}
						%>
                        <p>Relational Storage provisioning. </p>

                    </div>
                </td>
            
            </tr>
            <tr>
                <td class="tip-bottom"></td>
                <td class="tip-empty"></td>
                <td class="tip-bottom"></td>
                <td class="tip-empty"></td>
                <td class="tip-bottom"></td>
                
            </tr>
        </table>
</div>
