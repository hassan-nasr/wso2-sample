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
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<jsp:include page="../dialog/display_messages.jsp"/>
<link href="../tenant-dashboard/css/dashboard-common.css" rel="stylesheet" type="text/css" media="all"/>
<%
        Object param = session.getAttribute("authenticated");
        String passwordExpires = (String) session.getAttribute(ServerConstants.PASSWORD_EXPIRATION);
    boolean hasCAppMgtPermission = CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage");
    boolean hasTopicMgtPermission = CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/topic");
    boolean hasServiceMgtPermission = CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/add/service");
    boolean hasCEPMgtPermission = CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/cep");
    boolean hasBrokerMgtPermission = CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/brokerManager");
    boolean hasStatsPermission = CarbonUIUtil.isUserAuthorized(request, "/permission/protected/manage");

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
        .tip-table td.cep1 {
            background-image: url(../../carbon/tenant-dashboard/images/cep1.png);
        }
        .tip-table td.cep2 {
            background-image: url(../../carbon/tenant-dashboard/images/cep2.png);
        }
        .tip-table td.cep3 {
            background-image: url(../../carbon/tenant-dashboard/images/cep3.png);
        }
        .tip-table td.cep4 {
            background-image: url(../../carbon/tenant-dashboard/images/cep4.png);
        }



        .tip-table td.cep5 {
            background-image: url(../../carbon/tenant-dashboard/images/cep5.png);
        }
        .tip-table td.cep6 {
            background-image: url(../../carbon/tenant-dashboard/images/cep6.png);
        }
        /*.tip-table td.cep7 {*/
            /*background-image: url(../../carbon/tenant-dashboard/images/cep7.png);*/
        /*}*/
        /*.tip-table td.cep8 {*/
            /*background-image: url(../../carbon/tenant-dashboard/images/cep8.png);*/
        /*}*/
    </style>
    <%--
    Capp, Bucket, Broker, Topic
   Services.  Monitor CEP, MonotorBucket, Mon-Broker,


    --%>
    <h2 class="dashboard-title">WSO2 CEP Quick Start Dashboard</h2>
    <table class="tip-table">
        <tr>
            <td class="tip-top cep1"></td>
            <td class="tip-empty"></td>
            <td class="tip-top cep2"></td>
            <td class="tip-empty "></td>
            <td class="tip-top cep3"></td>
            <td class="tip-empty "></td>
            <td class="tip-top cep4"></td>
        </tr>
        <tr>
            <td class="tip-content">
                <div class="tip-content-lifter">
                    <%
                        if (hasCAppMgtPermission) {
                    %>
                    <a class="tip-title" href="../inputeventadaptormanager/index.jsp">Input Event Adaptors</a> <br/>
                    <%
                    } else {
                    %>
                    <h3 class="tip-title">Input Event Adaptors</h3> <br/>
                    <%
                        }
                    %>

                    <p>Input Event Adaptors receives events from external event sources</p>

                </div>
            </td>
            <td class="tip-empty"></td>
            <td class="tip-content">
                <div class="tip-content-lifter">

                    <%
                        if (hasCEPMgtPermission) {
                    %>
                    <a class="tip-title" href="../eventbuilder/index.jsp">Event Builder</a> <br/>
                    <%
                    } else {
                    %>
                    <h3 class="tip-title">Event Builder</h3> <br/>
                    <%
                        }
                    %>

                    <p>Event Builder maps the incoming messages/events to event streams.</p>

                </div>
            </td>
            <td class="tip-empty"></td>
            <td class="tip-content">
                <div class="tip-content-lifter">
                    <%
                        if (hasBrokerMgtPermission) {
                    %>
                    <a class="tip-title" href="../eventprocessor/index.jsp">Execution Plan</a> <br/>
                    <%
                    } else {
                    %>
                    <h3 class="tip-title">Execution Plan</h3> <br/>
                    <%
                        }
                    %>


                    <p>Execution Plan contains the execution logic that need to be performed on the event streams.</p>

                </div>
            </td>
            <td class="tip-empty"></td>
            <td class="tip-content">
                <div class="tip-content-lifter">
                    <%
                        if (hasTopicMgtPermission) {
                    %>
                    <a class="tip-title" href="../eventformatter/index.jsp">Event Formatter</a> <br/>
                    <%
                    } else {
                    %>
                    <h3 class="tip-title">Event Formatter</h3> <br/>
                    <%
                        }
                    %>

                     <p>Event Formatter maps the event streams to output messages/events.</p>

                </div>
            </td>
        </tr>
        <tr>
            <td class="tip-bottom"></td>
            <td class="tip-empty"></td>
            <td class="tip-bottom"></td>
            <td class="tip-empty"></td>
            <td class="tip-bottom"></td>
            <td class="tip-empty"></td>
            <td class="tip-bottom"></td>
        </tr>
    </table>
    <div class="tip-table-div"></div>
    <table class="tip-table">
        <tr>
            <td class="tip-top cep5"></td>
            <td class="tip-empty"></td>
            <td class="tip-top cep6"></td>
            <td class="tip-empty"></td>
            <td class="tip-top cep8"></td>
            <td class="tip-empty "></td>
            <td class="tip-empty "></td>
        </tr>
        <tr>
            <td class="tip-content">
                <div class="tip-content-lifter">
                    <%
                    if (hasServiceMgtPermission) {
                    %>
                    <a class="tip-title" href="../outputeventadaptormanager/index.jsp">Output Event Adaptors</a> <br/>
                    <%
                    } else {
                    %>
                    <h3 class="tip-title">Output Event Adaptors</h3> <br/>
                    <%
                        }
                    %>


                    <p>Output Event Adaptors sends events out</p>

                </div>
            </td>
            <td class="tip-empty"></td>
            <td class="tip-content">
                <div class="tip-content-lifter">
                    <%
                        if (hasStatsPermission) {
                    %>
                    <a class="tip-title" href="../event-statistics/event_statistics_view.jsp?region=region4&item=event_statistics_menu">Monitoring CEP</a> <br/>
                    <%
                    } else {
                    %>
                    <h3 class="tip-title">Monitoring CEP</h3> <br/>
                    <%
                        }
                    %>
                    <p>Shows real-time accumulative statistics of all CEP operations.</p>
                </div>
            </td>
            <%--<td class="tip-empty"></td>--%>
            <%--<td class="tip-content">--%>
                <%--<div class="tip-content-lifter">--%>
                    <%--<%--%>
                        <%--if (hasStatsPermission) {--%>
                    <%--%>--%>
                    <%--<a class="tip-title" href="../event-tracer/index.jsp">Tracing Events</a> <br/>--%>
                    <%--<%--%>
                    <%--} else {--%>
                    <%--%>--%>
                    <%--<h3 class="tip-title">Tracing Events</h3> <br/>--%>
                    <%--<%--%>
                        <%--}--%>
                    <%--%>--%>
                    <%--<p>this helps to debug execution flows by showing which events went through which module at runtime.</p>--%>

                <%--</div>--%>
            <%--</td>--%>
            <%--<td class="tip-empty"></td>--%>
            <%--<td class="tip-content">--%>
                <%--<div class="tip-content-lifter">--%>
                    <%--<%--%>
                        <%--if (hasStatsPermission) {--%>
                    <%--%>--%>
                    <%--<a class="tip-title" href="../eventexecutionplanwizard/create_executionPlanWizard.jsp?region=region1&item=execution_plan_add_menu">Execution Plan Config Wizard</a> <br/>--%>
                    <%--<%--%>
                    <%--} else {--%>
                    <%--%>--%>
                    <%--<h3 class="tip-title">Execution Plan Config Wizard</h3> <br/>--%>
                    <%--<%--%>
                        <%--}--%>
                    <%--%>--%>
                    <%--<p>The wizard to configure the the full execution flow</p>--%>

                <%--</div>--%>
            <%--</td>--%>
            <td class="tip-empty"></td>
            <td class="tip-content-empty"></td>
        </tr>
        <tr>
            <td class="tip-bottom"></td>
            <td class="tip-empty"></td>
            <td class="tip-bottom"></td>
            <td class="tip-empty"></td>
            <td class="tip-bottom"></td>
            <td class="tip-empty"></td>
            <td class="tip-empty"></td>
        </tr>
    </table>
<p>
    <br/>
</p> </div>
</div>
