<%@ page import="org.wso2.carbon.context.CarbonContext" %>
<%@ page import="org.wso2.carbon.context.RegistryType" %>
<%@ page import="org.wso2.carbon.registry.core.Registry" %>
<%@ page import="org.wso2.carbon.registry.core.Resource" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.apache.commons.httpclient.HttpClient" %>
<%@ page import="org.apache.commons.httpclient.HttpStatus" %>
<%@ page import="org.apache.commons.httpclient.methods.GetMethod" %>

<%@ page import="org.apache.commons.httpclient.methods.PostMethod" %>
<%@ page import="sun.misc.BASE64Encoder" %>

<%@ page import="org.json.simple.JSONObject" %>
<%@ page import="org.json.simple.parser.JSONParser" %>
<%@ page import="org.json.simple.parser.ParseException" %>

<% 
    String endpoint = request.getParameter("endpoint");
    String consumerkey = request.getParameter("consumerkey");
    String secretkey = request.getParameter("seckey");
    String apiManagerUrl =  request.getParameter("apiManagerUrl");	
    String username = request.getParameter("username");
    String password = request.getParameter("password");
    
    if (endpoint == null) {
        endpoint = "";
        consumerkey = "";
        secretkey = "";
		apiManagerUrl = "";
		username = "";
		password = "";
    } else {
        CarbonContext cCtx = CarbonContext.getCurrentContext();
        Registry registry = (Registry) cCtx.getRegistry(RegistryType.SYSTEM_GOVERNANCE);
    	//curl -v -H "Authorization: Bearer KY7QoTynVZShrfguQGTNPST88X8a" http://apimanager.appfactorypreview.wso2.com:8280/twitter/1.0.0/search.atom?q=wso2  	
        Resource resource = registry.get("dependencies/consumerkey");
        if(resource.getContent() instanceof String){
        	consumerkey = (String) resource.getContent();
        }else{
        	consumerkey = new String((byte[]) resource.getContent());
        }
		
        resource = registry.get("dependencies/consumersecret");
        if(resource.getContent() instanceof String){
        	secretkey = (String) resource.getContent();
        }else{
        	secretkey = new String((byte[]) resource.getContent());
        }
    }    
%>

<html>
<body>
<h2>Calling API</h2>
<form action="index.jsp" method="get">
<p>API Manager Url : <input type="text" name="apiManagerUrl" value="<%=apiManagerUrl%>"></input> </p>
<p>Enter the endpoint URL : <input type="text" name="endpoint" value="<%=endpoint%>"></input> </p>
<p>Username : <input type="text" name="username" value="<%=username%>" ></input>
<p>Password : <input type="text" name="password" value="<%=password%>" ></input>
<p>API Manager Consumer Key : <input type="text" name="consumerkey" value="<%=consumerkey%>" disabled="disabled"></input> </p>
<p>API Manager Secret Key : <input type="text" name="secretkey" value="<%=secretkey%>" disabled="disabled"></input>
<input type="submit" value="Invoke"/>
</form>
<%

if (endpoint != null && endpoint.length() > 0) {
	
	String submitUrl = apiManagerUrl + "/oauth2endpoints/token";
	String applicationToken = consumerkey + ":" + secretkey;
    BASE64Encoder base64Encoder = new BASE64Encoder();
    applicationToken = "Basic " + base64Encoder.encode(applicationToken.getBytes()).trim();

    HttpClient client = new HttpClient();
    PostMethod method = new PostMethod(submitUrl);
    method.addRequestHeader("Authorization", applicationToken);
    method.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    
    method.addParameter("grant_type", "password");
    method.addParameter("username", username);
    method.addParameter("password", password);
    
    int httpStatusCode = client.executeMethod(method);
    
    String accessTokenJson="";
    String values = "";
    if (HttpStatus.SC_OK == httpStatusCode) {
    	accessTokenJson = method.getResponseBodyAsString();
        
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(accessTokenJson);
        JSONObject jsonObject = (JSONObject)obj;
        
        String accessToken = (String)jsonObject.get("access_token");
        String refreshToken = (String)jsonObject.get("refresh_token");
        out.println("Access Token Received - " + accessToken);
        
        GetMethod apiMethod = new GetMethod(endpoint);
        apiMethod.addRequestHeader("Authorization","Bearer " + accessToken);
        httpStatusCode = client.executeMethod(apiMethod);
        if (HttpStatus.SC_OK == httpStatusCode) {
        	values = apiMethod.getResponseBodyAsString();
        	values = StringUtils.replaceEach(values, new String[]{"&", "\"", "<", ">"}, new String[]{"&amp;", "&quot;", "&lt;", "&gt;"});
        } else {
        	values = "Eror occurred invoking the service " + httpStatusCode;
        }
        
    } else {
    	accessTokenJson = "Eror occurred while getting authentication token " + httpStatusCode;
    }
  
    %>
    <p><%="API Output\n " + values%></p>
    <%
}
  
%>
</body>
</html>

