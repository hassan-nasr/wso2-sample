<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@page import="org.apache.axis2.context.ConfigurationContext" %>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@page import="org.wso2.carbon.security.mgt.stub.config.xsd.KerberosConfigData" %>
<%@page import="org.wso2.carbon.security.mgt.stub.config.xsd.SecurityConfigData" %>
<%@page import="org.wso2.carbon.security.mgt.stub.config.xsd.SecurityScenarioData" %>
<%@page import="org.wso2.carbon.security.mgt.stub.keystore.xsd.KeyStoreData" %>
<%@page import="org.wso2.carbon.security.ui.SecurityUIConstants" %>
<%@page import="org.wso2.carbon.security.ui.Util" %>
<%@page import="org.wso2.carbon.security.ui.client.KeyStoreAdminClient" %>
<%@ page import="org.wso2.carbon.security.ui.client.SecurityAdminClient" %>
<%@page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@page import="org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName" %>
<%@ page import="org.wso2.carbon.user.mgt.stub.types.carbon.UserRealmInfo" %>
<%@page import="org.wso2.carbon.user.mgt.ui.UserAdminClient" %>


<%@page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ResourceBundle" %>
<link href="../../styles/main.css" rel="stylesheet" type="text/css" media="all"/>
<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../../main/admin/js/main.js" type="text/javascript"></script>
<script type="text/javascript" src="../../main/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../../main/admin/js/cookies.js"></script>
<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle basename="org.wso2.carbon.security.ui.i18n.Resources">
<carbon:breadcrumb label="activate.security"
                   resourceBundle="org.wso2.carbon.security.ui.i18n.Resources"
                   topPage="false" request="<%=request%>"/>

<script type="text/javascript">
    function doValidation(isPolicyFromReg, isKerberos) {
        if (isPolicyFromReg) {
            return true;
        }

        if (isKerberos) {

            var errorValue = validateEmpty("org.wso2.kerberos.service.principal.name");
            if (errorValue != '') {
                CARBON.showWarningDialog("<fmt:message key="please.specify.valid.principal.name"/>");
                return false;
            }

            errorValue = validateEmpty("org.wso2.kerberos.service.principal.password");

            if (errorValue != '') {
                CARBON.showWarningDialog("<fmt:message key="please.specify.valid.principal.password"/>");
                return false;
            }

        } else {
            var isChecked = false;
            isChecked = isAtleastOneCheckedIfExisting("userGroups");
            if (isChecked != true) {
                CARBON.showWarningDialog("<fmt:message key="please.select.at.leaset.one.user.group"/>");
                return false;
            }

            isChecked = isAtleastOneCheckedIfExisting("trustStore");
            if (isChecked != true) {
                CARBON.showWarningDialog("<fmt:message key="please.select.at.least.one.trust.store"/>");
                return false;
            }

        }


        return true;
    }
</script>
<%
    FlaggedName[] groupData = null;
    KeyStoreData[] datas = null;
    String curr_pvtks = "";
    List<String> curr_tstks = new ArrayList<String>();
    List<String> curr_ugs = new ArrayList<String>();
    String category = null;
    boolean isPolicyFromRegistry = false;
    boolean fault = false;
    UserAdminClient userAdminClient = null;
    String[] domainNames = null;
    boolean newFilter = false;
    UserRealmInfo userRealmInfo = null;
    userRealmInfo = (UserRealmInfo) session.getAttribute(SecurityUIConstants.USER_STORE_INFO);

    String cancelLink = (String)session.getAttribute("cancelLink");


        // search filter
    String selectedDomain = request.getParameter("domain");
    if(selectedDomain == null || selectedDomain.trim().length() == 0){
        selectedDomain = (String) session.getAttribute(SecurityUIConstants.ROLE_LIST_DOMAIN_FILTER);
        if (selectedDomain == null || selectedDomain.trim().length() == 0) {
            selectedDomain = SecurityUIConstants.ALL_DOMAINS;
        }
    } else {
        newFilter = true;
    }

    session.setAttribute(SecurityUIConstants.ROLE_LIST_DOMAIN_FILTER, selectedDomain.trim());

    String filter = request.getParameter(SecurityUIConstants.ROLE_LIST_FILTER);
    if (filter == null || filter.trim().length() == 0) {
        filter = (String) session.getAttribute(SecurityUIConstants.ROLE_LIST_FILTER);
        if (filter == null || filter.trim().length() == 0) {
            filter = "*";
        }
    } else {
        if(filter.contains(SecurityUIConstants.DOMAIN_SEPARATOR)){
            selectedDomain = SecurityUIConstants.ALL_DOMAINS;
            session.removeAttribute(SecurityUIConstants.ROLE_LIST_DOMAIN_FILTER);
        }
        newFilter = true;
    }


    String modifiedFilter = filter.trim();
    if(!SecurityUIConstants.ALL_DOMAINS.equalsIgnoreCase(selectedDomain)){
        modifiedFilter = selectedDomain + SecurityUIConstants.DOMAIN_SEPARATOR + filter;
        modifiedFilter = modifiedFilter.trim();
    }

    session.setAttribute(SecurityUIConstants.ROLE_LIST_FILTER, filter.trim());


    int numberOfPages = 0;
    String isPaginatedString = request.getParameter("isPaginated");
    if (isPaginatedString != null && isPaginatedString.equals("true")) {
        userAdminClient = (UserAdminClient) session.getAttribute(SecurityUIConstants.USER_ADMIN_CLIENT);
        numberOfPages = (Integer) session.getAttribute(SecurityUIConstants.FLAGGED_NAME_PAGE_COUNT);
    }
    String paginationValue = "isPaginated=true";


    String pageNumber = request.getParameter("pageNumber");
    if (pageNumber == null) {
        pageNumber = "0";
    }
    int pageNumberInt = 0;
    try {
        pageNumberInt = Integer.parseInt(pageNumber);
    } catch (NumberFormatException ignored) {
    }

    String serviceName = (String) session.getAttribute("serviceName");

    String BUNDLE = "org.wso2.carbon.security.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    String info = MessageFormat.format(resourceBundle.getString("service.secured.using.a.default.scenario"), serviceName);

    String scenId = request.getParameter("scenarioId");
    String registryPolicyPath = null;
    if ("policyFromRegistry".equals(scenId)) {
        isPolicyFromRegistry = true;
        registryPolicyPath = request.getParameter("secPolicyRegText");
        info = MessageFormat.format(resourceBundle.getString("service.secured.using.custom.policy.select.users.and.key.stores"), serviceName);
    }
    if (scenId != null) {
        session.setAttribute("scenarioId", scenId);
    } else {
        /**
         * This is needed for proper functionality of breadcrumbs. If the user goes
         * forward and clicks on "Activate Security" breadcrumb, scenario Id can only
         * be accessed from the session.
         */
        scenId = (String) session.getAttribute("scenarioId");
    }

    boolean kerberosScenario = false;
    KerberosConfigData kerberosConfigData = null;
    
    if (cancelLink==null){
    	cancelLink = "../service-mgt/service_info.jsp?serviceName="+ serviceName;
    }

    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        SecurityAdminClient secClient = new SecurityAdminClient(cookie, backendServerURL, configContext);
        SecurityScenarioData scenData = secClient.getSecurityScenario(scenId);

        SecurityConfigData configData = secClient.getSecurityConfigData(serviceName, scenId, registryPolicyPath);
        category = scenData.getCategory();

        //place holders for existing configs
        if (configData != null) {
            if (configData.getPrivateStore() != null) {
                curr_pvtks = configData.getPrivateStore();
            }

            if (configData.getTrustedKeyStores() != null &&
                configData.getTrustedKeyStores().length > 0 &&
                configData.getTrustedKeyStores()[0] != null) {
                curr_tstks = Arrays.asList(configData.getTrustedKeyStores());
            }

            if (configData.getUserGroups() != null &&
                configData.getUserGroups().length > 0 &&
                configData.getUserGroups()[0] != null) {
                curr_ugs = Arrays.asList(configData.getUserGroups());
            }

            if (category.contains("kerberos")) {
                kerberosConfigData = configData.getKerberosConfigurations();
            }
        }

        if (category.contains("ut")) {
            if (userAdminClient == null) {
                int itemsPerPageInt = SecurityUIConstants.DEFAULT_ITEMS_PER_PAGE;
                userAdminClient = new UserAdminClient(cookie, backendServerURL, configContext);
                userRealmInfo = userAdminClient.getUserRealmInfo();
                numberOfPages = (int) Math.ceil((double) userAdminClient.getAllRolesNames(modifiedFilter, -1).length / itemsPerPageInt);
                session.setAttribute(SecurityUIConstants.USER_ADMIN_CLIENT, userAdminClient);
                session.setAttribute(SecurityUIConstants.FLAGGED_NAME_PAGE_COUNT, numberOfPages);
                session.setAttribute(SecurityUIConstants.USER_STORE_INFO, userRealmInfo);


            }
            groupData = Util.doFlaggedNamePaging(pageNumberInt, userAdminClient.getAllRolesNames(modifiedFilter, -1));

            if(userRealmInfo != null){
                   domainNames = userRealmInfo.getDomainNames();
                   if(domainNames != null){
                       List<String> list = new ArrayList<String>(Arrays.asList(domainNames));
                       list.add(SecurityUIConstants.ALL_DOMAINS);
                       domainNames = list.toArray(new String[list.size()]);
                   }
               }

        }

        if (category.contains("keystore")) {
            KeyStoreAdminClient client = new KeyStoreAdminClient(cookie, backendServerURL, configContext);
            datas = client.getKeyStores();
        }

        if (category.contains("kerberos")) {
            kerberosScenario = true;
        }

    } catch (Exception e) {
        fault = true;
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
</script>
<%
    }
    if (!fault) {
%>
<div id="middle">
<h2><fmt:message key="activate.security"/></h2>

<div id="workArea">
<p><%=info%>
</p>

<p>&nbsp;</p>
<%
    if (category.contains("ut")) {
%>

<form name="filterForm" method="post" action="ut-ks-advance.jsp">
    <table class="styledLeft noBorders">
        <thead>
        <tr>
            <th colspan="2"><fmt:message key="role.search"/></th>
        </tr>
        </thead>
        <tbody>
        <%
           // String[] domainNames = {"Internal"};
           // String selectedDomain = "Internal";
            if (domainNames != null && domainNames.length > 0) {
        %>
        <tr>
            <td class="leftCol-big" style="padding-right: 0 !important;"><fmt:message
                    key="select.domain.search"/></td>
            <td><select id="domain" name="domain">
                <%
                    for (String domainName : domainNames) {
                        if (selectedDomain.equals(domainName)) {
                %>
                <option selected="selected" value="<%=domainName%>"><%=domainName%>
                </option>
                <%
                } else {
                %>
                <option value="<%=domainName%>"><%=domainName%>
                </option>
                <%
                        }
                    }
                %>
            </select>
            </td>
        </tr>
        <%
            }
        %>

        <tr>
            <td class="leftCol-big" style="padding-right: 0 !important;"><fmt:message
                    key="list.roles"/></td>
            <td>
                <input type="text" name="<%=SecurityUIConstants.ROLE_LIST_FILTER%>"
                       value="<%=filter%>"/>

                <input class="button" type="submit"
                       value="<fmt:message key="role.search"/>"/>
            </td>

        </tr>
        </tbody>
    </table>
</form>
<%
    }
%>
<form method="post" action="add-security.jsp" name="dataForm"
      onsubmit="return doValidation(<%= isPolicyFromRegistry%>, <%=kerberosScenario%>)">
<input type="hidden" name="scenarioId" id="scenarioId"
       value="<%= scenId%>"/>
<input type="hidden" name="policyPath" id="policyPath"
       value="<%= registryPolicyPath%>"/>
<%
    if (category.contains("ut")) {
%>
<table id="ut" class="styledLeft">
    <thead>
    <tr>
        <th><fmt:message key="user.groups"/></th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td class="formRow">
            <table class="normal">
                <%
                    if (groupData != null) {
                        for (FlaggedName data : groupData) {
                            if (data != null) { //Confusing!!. Sometimes a null object comes. Maybe a bug in Axis!!

                                if (CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME.equals(data.getItemName())) {
                                    continue;
                                }

                                String checked = "";
                                if (curr_ugs.contains(data.getItemName().toLowerCase())) {
                                    checked = "checked=\"checked\"";
                                }
                %>
                <tr>
                    <td><input type="checkbox" name="userGroups"
                               value="<%=data.getItemName()%>" <%=checked%>/> <%=data.getItemName()%>
                    </td>
                </tr>
                <%
                            }
                        }
                    }
                %>
            </table>
        </td>
    </tr>
    <tr>
        <carbon:paginator pageNumber="<%=pageNumberInt%>"
                          numberOfPages="<%=numberOfPages%>"
                          page="ut-ks-advance.jsp"
                          pageNumberParameterName="pageNumber"
                          parameters="<%=paginationValue%>"
                          resourceBundle="org.wso2.carbon.security.ui.i18n.Resources"
                          prevKey="prev" nextKey="next"/>
    </tr>
    </tbody>
</table>


<%
    }

%>
<%

    if (category.contains("keystore")) {
%>
<table id="trtks" class="styledLeft">
    <thead>
    <tr>
        <th><fmt:message key="trusted.key.stores"/></th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td class="formRow">
            <table class="normal">
                <%
                    if (datas != null) {
                        for (KeyStoreData data : datas) {
                            if (data != null) { //Confusing!!. Sometimes a null object comes. Maybe a bug in Axis!!

                                String checked = "";
                                if (curr_tstks.contains(data.getKeyStoreName())) {
                                    checked = "checked=\"checked\"";
                                }
                %>
                <tr>
                    <td><input type="checkbox" name="trustStore"
                               value="<%=data.getKeyStoreName()%>" <%=checked%>/> <%=data.getKeyStoreName()%>
                    </td>
                </tr>
                <%
                            }
                        }
                    }
                %>
            </table>
        </td>
    </tr>
    </tbody>
</table>
<table id="pvtks" class="styledLeft">
    <thead>
    <tr>
        <th><fmt:message key="private.key.store"/></th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td class="formRow">
            <table class="normal">
                <tr>
                    <td>
                        <select name="privateStore">
                            <%
                                if (datas != null) {
                                    for (KeyStoreData data : datas) {
                                        if (data != null && data.getPrivateStore()) {
                                            String selected = "";
                                            if (data.getKeyStoreName().equals(curr_pvtks)) {
                                                selected = "selected=\"selected\"";
                                            }

                            %>
                            <option value="<%=data.getKeyStoreName()%>" <%=selected %>><%=data.getKeyStoreName()%>
                            </option>
                            <%
                                        }
                                    }
                                }
                            %>
                        </select>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
    </tbody>
</table>
<%
    }
%>

<!-- If the scenario is a kerberos category one, configure, KDC, service principle etc ... -->
<%
    if (category.contains("kerberos")) {

        String servicePrincipleName = "";
        String servicePrinciplePassword = "";
        if (kerberosConfigData != null) {

            servicePrincipleName = kerberosConfigData.getServicePrincipleName();
            servicePrinciplePassword = kerberosConfigData.getServicePrinciplePassword();
        }
%>

<table id="kerberosTable" class="styledLeft">
    <thead>
    <tr>
        <th><fmt:message key="configure.kerberos.parameters"/></th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td class="formRow">
            <table class="normal">

                <tr>
                    <td>
                        <fmt:message key="kerberos.service.principal.name"/><font
                            color="red">*</font>
                    </td>
                    <td>
                        <input type="text" name="org.wso2.kerberos.service.principal.name"
                               value="<%=servicePrincipleName%>"/>
                    </td>
                </tr>
                <tr>
                    <td>
                        <fmt:message key="kerberos.service.principal.password"/><font
                            color="red">*</font>
                    </td>
                    <td>
                        <input type="password" name="org.wso2.kerberos.service.principal.password"
                               value="<%=servicePrinciplePassword%>"/>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
    </tbody>
</table>
<input type="hidden" name="org.wso2.security.category" value="kerberos">

<%
    }
%>


<p></p>
<table class="styledLeft">
    <tr class="buttonRow">
        <td>
            <input class="button" type="button" value="< <fmt:message key="back"/>"
                   onclick="location.href = 'index.jsp?serviceName=<%=serviceName%>'"/>
            <input class="button" type="submit" value="<fmt:message key="finish"/>"/>
            <input class="button" type="button" value="<fmt:message key="cancel"/>"
                   onclick="location.href = '<%=cancelLink%>'"/>
        </td>
    </tr>
</table>
<%

%>
</form>
</div>
</div>
<%
    }
%>
</fmt:bundle>
