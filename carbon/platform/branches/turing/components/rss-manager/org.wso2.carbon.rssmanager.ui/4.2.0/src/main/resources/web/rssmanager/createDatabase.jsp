<!--
~ Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.rssmanager.common.RSSManagerHelper" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.RSSManagerClient" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.stub.types.RSSInstance" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.stub.types.config.environment.RSSEnvironmentContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.utils.multitenancy.MultitenantConstants" %>

<script type=text/javascript src="js/uiValidator.js"></script>

<fmt:bundle basename="org.wso2.carbon.rssmanager.ui.i18n.Resources">
    <carbon:breadcrumb
            label="Create database"
            resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <%
        RSSManagerClient client = null;
        int systemRSSInstanceCount = 0;
        RSSInstance[] rssInstances = new RSSInstance[0];
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
                getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String tenantDomain = (String) session.getAttribute(MultitenantConstants.TENANT_DOMAIN);
        String envName = request.getParameter("envName");
        String[] environments = (String[]) session.getAttribute("environments");
        RSSEnvironmentContext rssContext = null;

        try {
            client = new RSSManagerClient(cookie, backendServerURL, configContext,
                    request.getLocale());
        } catch (Exception e) {
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(),
                    CarbonUIMessage.ERROR, request, e);
        }
        if (envName == null) {
            envName = environments[0];
        }
        rssContext = new RSSEnvironmentContext();
        rssContext.setEnvironmentName(envName);
    %>

    <div id="middle">
        <h2><fmt:message key="rss.manager.new.database"/></h2>

        <div>

        </div>

        <div id="workArea">
            <form method="post" action="#" name="addDatabaseForm"
                  id="addDatabaseForm">
                <%
                    if (client != null) {
                        try {
                            systemRSSInstanceCount = client.getSystemRSSInstanceCount(rssContext);
                            rssInstances = client.getRSSInstanceList(rssContext);
                        } catch (Exception e) {
                            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(),
                                    CarbonUIMessage.ERROR, request, e);
                        }
                        }
                %>
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th>Create New Database</th>
                    </tr>
                    </thead>
                    <tr>
                        <td>
                            <table class="normal">
                                <tr>
                                </tr>
                                <tr>
                                    <td class="leftCol-med">
                                            <fmt:message key="rss.environment.name"/><font
                                            color='red'>*</font>
                                    <td>
                                        <select id="envCombo" name="envCombo"
                                                onchange="onComboChange(this)">
                                            <%
                                                for (String env : environments) {
                                                    if (envName != null && env.equals(envName.trim())) {
                                            %>
                                            <option id="<%=env%>" value="<%=env%>"
                                                    selected="selected"><%=env%>
                                            </option>
                                            <%
                                            } else {
                                            %>
                                            <option id="<%=env%>" value="<%=env%>"><%=env%>
                                            </option>
                                            <%
                                                    }
                                                }
                                            %>
                                        </select>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="leftCol-med"><fmt:message
                                            key="rss.manager.instance.name"/><font
                                            color='red'>*</font></td>
                                    <td><select id="rssInstances"
                                                name="rssInstances">
                                       
                                        <%
                                            if (rssInstances.length > 0) {
                                                for (RSSInstance rssIns : rssInstances) {
                                                    if (rssIns != null) {
                                        %>
                                        <option id="<%=rssIns.getName()%>"
                                                value="<%=rssIns.getName()%>"><%=rssIns.getName()%>
                                        </option>
                                        <%
                                                        }
                                                    }
                                                }
                                       %>
                                    </select></td>
                                </tr>
                                <tr>
                                    <td align="left"><fmt:message key="rss.manager.db.name"/><font
                                            color='red'>*</font></td>
                                    <td><input value="" id="databaseName"
                                               name="databaseName"
                                               size="30" type="text"><font
                                            color='black'><%=(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) ? "" : "_" + RSSManagerHelper.processDomainName(tenantDomain)%>
                                    </font></td>
                                </tr>

                            </table>
                        </td>
                    </tr>
                    <div id="connectionStatusDiv" style="display: none;"></div>
                    <tr>
                        <td class="buttonRow" colspan="2">
                            <input class="button" type="button"
                                   value="<fmt:message key="rss.manager.create"/>"
                                   onclick="return createDatabase('<%=envName%>');return false;"/>

                            <input class="button" type="button"
                                   value="<fmt:message key="rss.manager.cancel"/>"
                                   onclick="document.location.href = 'databases.jsp'"/>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </div>
    <script type="text/javascript">
        function onComboChange(combo) {
            var opt = combo.options[combo.selectedIndex].value;
            window.location = 'createDatabase.jsp?envName=' + opt;
        }
    </script>
</fmt:bundle>