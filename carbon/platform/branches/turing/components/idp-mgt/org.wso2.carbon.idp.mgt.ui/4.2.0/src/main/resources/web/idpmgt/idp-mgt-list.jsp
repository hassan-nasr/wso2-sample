<!--
~ Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"%>

<carbon:breadcrumb label="trusted.idp" resourceBundle="org.wso2.carbon.idp.mgt.ui.i18n.Resources"
                   topPage="true" request="<%=request%>" />
<jsp:include page="../dialog/display_messages.jsp"/>

<script type="text/javascript" src="../admin/js/main.js"></script>

<%
    String[] trustedIdPs = (String[])session.getAttribute("tenantIdPList");
    if(trustedIdPs == null){
%>
        <script type="text/javascript">
            location.href = "idp-mgt-list-load.jsp";
        </script>
<%
    }
%>

<script>

    jQuery(document).ready(function(){
        jQuery('#idpAddLink').click(function(){
            location.href = "idp-mgt-edit.jsp";
        })
    })
    function editIdPName(obj){
        location.href = "idp-mgt-edit-load.jsp?idPName=" + jQuery(obj).parent().prev().text();
    }
    function deleteIdPName(obj){
        CARBON.showConfirmationDialog('Are you sure you want to delete "'  + jQuery(obj).parent().prev().text() + '" IdP information?',
                function (){
                    location.href = "idp-mgt-delete-finish.jsp?idPName=" + jQuery(obj).parent().prev().text();
                },
                null);
    }

</script>

<fmt:bundle basename="org.wso2.carbon.idp.mgt.ui.i18n.Resources">
    <div id="middle">
        <h2>
            <fmt:message key='identity.providers'/>
        </h2>
        <div id="workArea">
            <a id="idpAddLink" class="icon-link" style="background-image:url(images/add.gif);"><fmt:message key='add.idp'/></a>
            <div style="clear:both"/>
            <div class="sectionHelp">
                <fmt:message key='idp.add.help'/>
            </div>
            <div class="sectionSub">
            <table class="styledLeft" id="idPsListTable">
                <thead><tr><th class="leftCol-big"><fmt:message key='registered.idps'/></th><th><fmt:message key='idp.actions'/></th></tr></thead>
                <tbody>
                    <% if(trustedIdPs != null && trustedIdPs.length > 0){ %>
                        <% for(int i = 0; i < trustedIdPs.length; i++){ %>
                            <tr>
                                <td><%=trustedIdPs[i]%></td>
                                <td>
                                    <a title="<fmt:message key='idp.name.edit'/>"
                                       onclick="editIdPName(this);return false;"
                                       href="#"
                                       class="icon-link"
                                       style="background-image: url(images/edit.gif)">
                                       <fmt:message key='idp.name.edit'/>
                                    </a>
                                    <a title="<fmt:message key='idp.name.delete'/>"
                                       onclick="deleteIdPName(this);return false;"
                                       href="#"
                                       class="icon-link"
                                       style="background-image: url(images/delete.gif)">
                                       <fmt:message key='idp.name.delete'/>
                                    </a>
                                </td>
                            </tr>
                        <% } %>
                    <% } else { %>
                        <tr>
                            <td colspan="2"><i><fmt:message key='no.idp'/></i></td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
            </div>
        </div>
    </div>

</fmt:bundle>