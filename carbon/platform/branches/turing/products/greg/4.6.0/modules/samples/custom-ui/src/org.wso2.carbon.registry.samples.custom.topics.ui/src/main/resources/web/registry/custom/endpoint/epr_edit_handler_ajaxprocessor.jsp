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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.wso2.carbon.registry.samples.custom.topics.ui.utils.SaveEndpointUtil" %>
<%@ page import="org.wso2.carbon.registry.common.utils.RegistryUtil" %>
<%@ page import="org.wso2.carbon.registry.common.ui.UIException" %>
<%
    String esPath = request.getParameter("path");
    try {
        SaveEndpointUtil.saveEndpointBean(esPath, request, config, session);
    } catch (UIException e) {
        %>Could not save the endpoint details<%
        return;
    }

    String eprMainPath = "epr_main_ajaxprocessor.jsp?path=" + esPath;
%>

<jsp:forward page="<%=eprMainPath%>"/>