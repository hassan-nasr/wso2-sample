<!DOCTYPE html>
<!--
~ Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.identity.application.authentication.endpoint.i18n.oauth2.Resources">

<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.oauth2.OAuth2Constants" %>

<%
    String errorMessage = "login.fail.message";
    boolean loginFailed = false;
    if (request.getParameter(OAuth2Constants.AUTH_FAILURE) != null &&
            "true".equals(request.getParameter(OAuth2Constants.AUTH_FAILURE))) {
        loginFailed = true;
        if(request.getParameter(OAuth2Constants.AUTH_FAILURE_MSG) != null){
            errorMessage = request.getParameter(OAuth2Constants.AUTH_FAILURE_MSG);
        }
    }
%>

<html lang="en">
<head>
    <meta charset="utf-8">
    <title>OAuth2.0 Login</title>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <!-- Le styles -->
    <link href="oauth2/assets/css/bootstrap.min.css" rel="stylesheet">
    <link href="oauth2/css/localstyles.css" rel="stylesheet">
    <!--[if lt IE 8]>
    <link href="oauth2/css/localstyles-ie7.css" rel="stylesheet">
    <![endif]-->

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="oauth2/assets/js/html5.js"></script>
    <![endif]-->
    <script src="oauth2/assets/js/jquery-1.7.1.min.js"></script>
    <script src="oauth2/js/scripts.js"></script>


</head>

<body>

<div class="header-strip">&nbsp;</div>
<div class="header-back">
    <div class="container">
        <div class="row">
            <div class="span4 offset3">
                <a class="logo">&nbsp</a>
            </div>
        </div>
    </div>
</div>

<div class="header-text">
    <strong>Please login to continue</strong>
</div>

<div class="container">
    <div class="row">
        <div class="span5 offset3 content-section">
            <p class="download-info">
                <a class="btn btn-primary btn-large" id="authorizeLink"><i
                        class="icon-ok icon-white"></i> Continue</a>
                <a class="btn btn-large" id="denyLink"><i class=" icon-exclamation-sign"></i>
                    Cancel</a>
            </p -->

            <form class="well form-horizontal" id="loginForm" method="POST" action="../../commonauth"
                  <% if(!(loginFailed)) { %>style="display:none"<% } %> >

                    <div class="alert alert-error" id="errorMsg" <% if (!loginFailed) { %> style="display:none" <% } %> >
                        <% if (loginFailed) { %>
                            <fmt:message key='<%=errorMessage%>'/>
                        <% } %>
                    </div>

                <!--Username-->
                <div class="control-group">
                    <label class="control-label" for="username">Username:</label>

                    <div class="controls">
                        <input type="text" class="input-large" id='username'
                               name="username">
                    </div>
                </div>

                <!--Password-->
                <div class="control-group">
                    <label class="control-label" for="password">Password:</label>

                    <div class="controls">
                        <input type="password" class="input-large" id='password'
                               name="password">
                    </div>
                </div>

                <input type="hidden" name="<%=OAuth2Constants.SESSION_DATA_KEY%>"
                       value="<%=request.getParameter(OAuth2Constants.SESSION_DATA_KEY)%>"/>

                <div class="form-actions">
                    <button type="button" class="btn btn-primary" id="loginBtn">Login</button>
                </div>
            </form>
            
            <form action="../../commonauth" id="denyForm" method="post">
            	<input type="hidden" name="<%=OAuth2Constants.SESSION_DATA_KEY%>"
                       value="<%=request.getParameter(OAuth2Constants.SESSION_DATA_KEY)%>"/>
                <input type="hidden" name="deny" value="true">       
            </form>
        </div>
    </div>
</div>
<!-- /container -->


</body>
</html>
</fmt:bundle>