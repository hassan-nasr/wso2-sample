<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<fmt:bundle basename="org.wso2con.feedback.form.i18n.Resources">

	<html>
<head>

</head>
<%  String message=request.getParameter("message");;
%>

<body>
	<% if (message.equals("add")) {%>
	<h1>
		<b><font color="black">Question Successfully added </font> </b>
	</h1>
	<%}%>
	<% if (message.equals("delete")) {%>
	<h1>
		<b><font color="black">Question Successfully deleted </font> </b>
	</h1>
	<%}%>
	<% if (message.equals("edit")) {%>
	<h1>
		<b><font color="black">Question Successfully updated </font> </b>
	</h1>
	<%}%>
</body>
	</html>
</fmt:bundle>