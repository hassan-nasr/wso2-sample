<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.wso2.carbon.identity.sso.agent.util.SSOAgentConfigs" %>
<%@ page import="org.wso2.carbon.identity.sso.agent.bean.SSOAgentSessionBean" %>
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
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <link rel="stylesheet" type="text/css" href="css/cart-styles.css">
    <title>WSO2</title>
</head>
<body>
<%
    String claimedId = null;
    if(request.getSession().getAttribute(
            SSOAgentConfigs.getSessionBeanName()) != null){
        if(((SSOAgentSessionBean) request.getSession().getAttribute(
                SSOAgentConfigs.getSessionBeanName())).getClaimedId() != null){
            claimedId = ((SSOAgentSessionBean) request.getSession().getAttribute(
                    SSOAgentConfigs.getSessionBeanName())).getClaimedId();
        } else {
            if(((SSOAgentSessionBean) request.getSession().getAttribute(
                    SSOAgentConfigs.getSessionBeanName())).getSubjectId() != null) {
%>
                <script type="text/javascript">
                    location.href = "samlsso-home.jsp";
                </script>
<%
                return;
            } else {
%>
                <script type="text/javascript">
                    location.href = "index.jsp";
                </script>
<%
                return;
            }
        }

    } else {
%>
        <script type="text/javascript">
            location.href = "index.jsp";
        </script>
<%
        return;
    }
%>

<div id="container">
    <div id="header-area">
        <img src="images/cart-logo.gif" alt="Logo" vspace="10" />

    </div>
    <!-- Header-are end -->

    <div id="content-area">
        <div class="cart-tabs">
            <table cellpadding="0" cellspacing="0" border="0">
                <tr>
                    <td class="cart-tab-left"><img src="images/cart-tab-left.gif"
                                                   alt="-"></td>
                    <td class="cart-tab-mid"><a>Home</a></td>
                    <td class="cart-tab-right"><img
                            src="images/cart-tab-right.gif" alt="-"></td>
                </tr>
            </table>
        </div>
        <table cellpadding="0" cellspacing="0" border="0" class="cart-expbox">
            <tr>
                <td><img src="images/cart-expbox-01.gif" alt="-"></td>
                <td class="cart-expbox-02">&nbsp</td>
                <td><img src="images/cart-expbox-03.gif" alt="-"></td>
            </tr>
            <tr>
                <td class="cart-expbox-08">&nbsp</td>
                <td class="cart-expbox-09">
                    <!--all content for cart and links goes here-->
                </td>
                <td class="cart-expbox-04">&nbsp</td>
            </tr>
            <tr>
                <td><img src="images/cart-expbox-07.gif" alt="-"></td>
                <td class="cart-expbox-06">&nbsp</td>
                <td><img src="images/cart-expbox-05.gif" alt="-"></td>
            </tr>

        </table>
        <h1>Travelocity.COM</h1>
        <hr />
        <div class="product-box">
            <h2> You are logged in as <%=claimedId%></h2>
            <a href="../avis.com/openid-home.jsp"> Avis.COM </a>
            <table>
                <%
                    if(session.getAttribute(SSOAgentConfigs.getSessionBeanName()) != null){
                        Set<Map.Entry<String,List>> attributes = (((SSOAgentSessionBean)session.getAttribute(
                                SSOAgentConfigs.getSessionBeanName())).getOpenIdAttributes()).entrySet();
                        for (Map.Entry<String, List> entry:attributes) {
                %>
                            <tr>
                                <td><%=entry.getKey()%></td>
                                <td>
                                    <%
                                        Iterator it = entry.getValue().iterator();
                                        if(it.hasNext()){
                                    %>
                                            <%=it.next().toString()%>
                                    <%
                                        }
                                    %>
                                </td>
                            </tr>
                <%
                        }
                    }
                %>
            </table>
            <a href="index.jsp">Go to Login page</a>
        </div>
    </div>
    <!-- content-area end -->


    <div id="footer-area">
        <p>©2013 WSO2</p>
    </div>
</div>
</body>
</html>