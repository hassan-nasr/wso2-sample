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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.cartridge.mgt.ui.CartridgeAdminClient" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.adc.mgt.dto.xsd.RepositoryInformation" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="java.util.Map" %>
<jsp:include page="../dialog/display_messages.jsp"/>


<%
	response.setHeader("Cache-Control", "no-cache");

	ResourceBundle bundle = ResourceBundle
        .getBundle(CartridgeAdminClient.BUNDLE, request.getLocale());

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
          (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    CartridgeAdminClient client;
    String repoUrl = "";
    String repoUserName = "";
    String repoPassword = "";
    String repoType = null;
    
    repoUrl = request.getParameter("repo_url");
    repoUserName = request.getParameter("repo_username");
    repoPassword = request.getParameter("repo_password");
    repoType = request.getParameter("repoType");
    try{
   		client = new CartridgeAdminClient(cookie, backendServerURL, configContext, request.getLocale());
   		RepositoryInformation repositoryInformation = client.testRepositoryConnection(repoUrl, repoUserName, repoPassword, "private".equals(repoType));
   		String message = "";
   		if (repositoryInformation != null) {
   			message = "Successfully connected to the repository: " + repositoryInformation.getRepoURL();
   		}
%>
<span id="responseMsg"><%=message%></span>
<%
	} catch (Exception e) {
		response.setStatus(500);
%>
<span id="responseMsg"><%=e.getMessage()%></span>
<%
	}
%>

