<%@ page import="org.wso2.carbon.context.CarbonContext" %>
<%@ page import="org.wso2.carbon.context.RegistryType" %>
<%@ page import="org.wso2.carbon.registry.core.Registry" %>
<%@ page import="org.wso2.carbon.registry.core.Resource" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.apache.commons.httpclient.HttpClient" %>
<%@ page import="org.apache.commons.httpclient.HttpStatus" %>
<%@ page import="org.apache.commons.httpclient.methods.GetMethod" %>

<% 
    String key = request.getParameter("key");
    String endpoint = "";
    String error = "";

    if (key == null || key.length() == 0) {
	key = "";
        error = "Please provide a valid resource name as the key";

    } else {
        CarbonContext cCtx = CarbonContext.getCurrentContext();
        Registry registry = (Registry) cCtx.getRegistry(RegistryType.SYSTEM_GOVERNANCE);
   	
        Resource resource = registry.get("dependencies/" + key);
        if(resource.getContent() instanceof String){
            endpoint = (String) resource.getContent();
        }else{
            endpoint = new String((byte[]) resource.getContent());
        }
    }
    
%>

<html>
<body>
<h2>Calling API : <%=endpoint%> </h2>

<% 
if(!"".equals(error)){
%>

<h3><%=error%></h3>

<%
}
%>

<form action="index.jsp" method="get">
<p>Enter the resource name : <input type="text" name="key" value="<%=key%>"></input> </p>
<input type="submit" value="Invoke"/>
</form>
<%
if (endpoint != null && endpoint.length() > 0) {
    String value = "";
       
    HttpClient client = new HttpClient();
    GetMethod method = new GetMethod(endpoint);
    int httpStatusCode = client.executeMethod(method);

    if (HttpStatus.SC_ACCEPTED != httpStatusCode) {
        value = method.getResponseBodyAsString();
        value = StringUtils.replaceEach(value, new String[]{"&", "\"", "<", ">"}, new String[]{"&amp;", "&quot;", "&lt;", "&gt;"});
    } else {
        value = "Eror occurred invoking the service " + httpStatusCode;
    }
    %>
    <p><%=value%></p>
    <%
}
    
%>
</body>
</html>

