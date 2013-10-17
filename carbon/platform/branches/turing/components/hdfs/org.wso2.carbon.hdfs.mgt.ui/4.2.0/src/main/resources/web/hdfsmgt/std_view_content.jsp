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
<%@page import="org.wso2.carbon.hdfs.mgt.ui.Utils"%>
<%@page import="javax.swing.text.Document"%>
<%@page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@page import="org.wso2.carbon.utils.ServerConstants"%>
<%@page import="org.wso2.carbon.hdfs.mgt.stub.fs.xsd.FolderInformation"%>
<%@page import="org.wso2.carbon.registry.common.ui.UIConstants"%>

<%@page import="org.wso2.carbon.hdfs.mgt.ui.HDFSAdminClient"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<script type="text/javascript" src="js/hdfs_util.js?v1"></script>


<%@ page import="java.util.List" %>
<%@ page import="java.util.LinkedList" %>

<jsp:include page="../dialog/display_messages.jsp"/>


<%  
	//set the tree view session
	session.setAttribute( "viewType", "std" );
	String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
	String viewMode =null;
	String resourceConsumer= null ;
	String targetDivID = null;

    String requestedPage = request.getParameter(UIConstants.REQUESTED_PAGE);
    String path = request.getParameter("path");

    HDFSAdminClient client;
    FolderInformation[] folderInfo = null;
    FolderInformation[] allFolders = null;
    try {
        client = new HDFSAdminClient(config.getServletContext(), session);
        folderInfo = client.getCurrentUserFSObjects(path);
        if(folderInfo != null){
        	//need for pagination.
        	allFolders = folderInfo.clone();
       	 }
        } catch (Exception e) {
        }
%>
<div id="entryList">
	<fmt:bundle basename="org.wso2.carbon.hdfs.mgt.ui.i18n.Resources">
		<div id="whileUpload" style="display:none;padding-top:0px;margin-top:20px;margin-bottom:20px;" class="ajax-loading-message">
			<img align="top" src="../hdfsmgt/images/ajax-loader.gif"/>
			<span>Process may take some time. Please wait..</span>
		</div>
		<table cellpadding="0" cellspacing="0" border="0" style="width:100%"
		       class="styledLeft" id="resourceColTable">
<%
    if (folderInfo != null && folderInfo.length != 0) {
%>
<%
		//pagination related code 
		int totalCount = folderInfo.length;
	
	    int start;
	    int end;
	    //need to move this to a configurable variable.
	    int itemsPerPage = 10;
	
	    int pageNumber;
	    if (requestedPage != null && requestedPage != "") {
	        pageNumber = new Integer(requestedPage);
	    } else {
	        pageNumber = 1;
	    }
	
	    int numberOfPages = 1;
	    if (totalCount % itemsPerPage == 0) {
	        numberOfPages = totalCount / itemsPerPage;
	    } else {
	        numberOfPages = totalCount / itemsPerPage + 1;
	    }
	
	    if (totalCount < itemsPerPage) {
	        start = 0;
	        end = totalCount;
	    } else {
	        start = (pageNumber - 1) * itemsPerPage;
	        end = (pageNumber - 1) * itemsPerPage + itemsPerPage;
	    }
		
		folderInfo = Utils.getChildren(start, itemsPerPage, folderInfo);
		%>
		<thead>
		<tr>
		    <th><fmt:message key="name"/></th>
		</tr>
		</thead>
		<tbody>
		<% 
			if(folderInfo != null){
				
		    int entryNumber = 0;
		    FolderInformation folderInformation = null;
		    
		    for (int ri = 0; ri < folderInfo.length; ri++) {
		    	folderInformation = folderInfo[ri];
		         entryNumber++;
				%>
				
				<tr id="1">
				   
				    <td valign="top"  id="actionPaneHelper<%=entryNumber%>" class="action-pane-helper">
				         <table cellpadding="0" cellspacing="0" border="0" style="width:100%">
				         <tr>
				         <td class="entryName-left" style="border:none !important;padding:0px !important;margin:0px !important;">
				      
					        <% if (folderInformation.getFolder()) { %>
					        <a class="folder-small-icon-link trimer"
					           onclick="loadResourcePage('<%=folderInformation.getFolderPath()%>','<%=viewMode%>','<%=resourceConsumer%>','<%=targetDivID%>')"
					           id="resourceView<%=entryNumber%>"
					           title="<%=folderInformation.getName()%>"><%=folderInformation.getName()%>
					        </a>
			
	
					        <% } else { %>
					         <a class="resource-icon-link trimer"
				               onclick="loadResourcePage('<%=folderInformation.getFolderPath()%>','<%=viewMode%>','<%=resourceConsumer%>','<%=targetDivID%>')"
				           		 id="resourceView<%=entryNumber%>" title="<%=folderInformation.getName()%>"><%=folderInformation.getName()%>
					        </a>
								<%} %>
	      
							 </td>
							  
							 <td class="entryName-right" style="border:none !important;padding:0px !important;margin:0px !important;float:right;" nowrap="nowrap">
						         <table cellspacing="0" cellpadding="0" border="0"><tr><td style="border:none !important;padding:0px !important;margin:0px !important;" width="100%" nowrap="nowrap">
						         </td><td style="border:none;" nowrap="nowrap">
						             <a style="float:right;" id="actionLink<%=entryNumber%>" onclick="loadActionPane('<%=entryNumber%>','action')" title="<fmt:message key='actions'/>" class="entryName-contracted"><fmt:message key='actions'/></a>
						         </td></tr></table>
						        </td>
						      
						        </tr>
						        <tr id="actionPane<%=entryNumber%>" style="display:none" class="actionPaneSelector">
								   <td colspan="3" class="action-pane" >
								   <div>
								
								          <% if (folderInformation.getFolder()) { %> 
								            
								               <a class="create-icon-link registryWriteOperation"
 													onclick="javascript:showHideCommon('create_panel<%=entryNumber%>');hideOthers(<%=entryNumber%>,'create_panel');if($('create_panel<%=entryNumber%>').style.display!='none')$('createFolder<%=entryNumber%>').focus();">
									                <fmt:message key="hdfs.create.menu.text"/></a>
								               <a class="edit-icon-link registryWriteOperation"
								              	 onclick="javascript:showHideCommon('rename_panel<%=entryNumber%>');hideOthers(<%=entryNumber%>,'rename_panel');if($('rename_panel<%=entryNumber%>').style.display!='none')$('itemEdit<%=entryNumber%>').focus();">
								                       <fmt:message key="hdfs.rename.menu.text"/></a>
								               <a class="delete-icon-link registryWriteOperation"
								               	   onclick="this.disabled = true; hideOthers(<%=entryNumber%>,'del');deleteFolder('<%=folderInformation.getFolderPath()%>'); this.disabled = false; ">
									                <fmt:message key="hdfs.delete.menu.text"/></a>
								               <a class="upload-icon-link registryWriteOperation"
								               		 onclick="javascript:showHideCommon('upload_panel<%=entryNumber%>');hideOthers(<%=entryNumber%>,'upload_panel');if($('upload_panel<%=entryNumber%>').style.display!='none')$('uploadedFileName').focus();">
									                <fmt:message key="hdfs.upload.menu.text"/></a>
									                <!-- For future development -->
<!-- 									           <a class="add-link-icon-link registryWriteOperation" -->
<%-- 									            	 onclick="this.disabled = true; hideOthers(<%=entryNumber%>,'symlinkContentUI'); this.disabled = false; "> --%>
<%--                									    <fmt:message key="hdfs.make.link"/></a> --%>
								         								
								            <% } else { %>
									         <a class="edit-icon-link registryWriteOperation" 
									         	onclick="javascript:showHideCommon('rename_panel<%=entryNumber%>');hideOthers(<%=entryNumber%>,'rename_panel');if($('rename_panel<%=entryNumber%>').style.display!='none')$('folderEdit<%=entryNumber%>').focus();">
								                       <fmt:message key="hdfs.rename.menu.text"/></a>
								             <a class="delete-icon-link registryWriteOperation"
								                  onclick="this.disabled = true; hideOthers(<%=entryNumber%>,'del');deleteFile('<%=folderInformation.getFolderPath()%>'); this.disabled = false; ">
									                <fmt:message key="hdfs.delete.menu.text"/></a>
									         <a class="download-icon-link"
		           									href="javascript:sessionAwareFunction(function() {window.location = '<%=Utils.getResourceDownloadURL(request, folderInformation.getFolderPath(), folderInformation.getName())%>'}, org_wso2_carbon_hdfs_mgt_ui_jsi18n['session.timed.out']);"
		         							  		target="_self"><fmt:message key="download"/></a>	
								                <%} %>
								     </div>
								   </td>
								</tr>
						         </table>
       				  </td>
    			</tr>
    			<tr class="copy-move-panel registryWriteOperation" id="rename_panel<%=entryNumber%>" style="display:none;">
   				 <td colspan="3" align="left">
		        <table cellpadding="0" cellspacing="0" class="styledLeft">
		            <thead>
		            <tr>
		                <th><fmt:message key="hdfs.rename.menu.text"/>
		                </th>
		            </tr>
		            </thead>
		            <tbody>
		            <tr>
		                <td>New <% if (folderInformation.getFolder()) { %>
		                    <fmt:message key="folder"/><% } else {%><fmt:message key="file"/><% } %>
		                    Name <span class="required">*</span>  <input value="<%=folderInformation.getName()%>" type="text"
		                                id="itemEdit<%=entryNumber%>" name ="itemEdit<%=entryNumber%>"/></td>
		            </tr>
		            <tr>
		                <td class="buttonRow">
		                    <input type="button" class="button" value="<fmt:message key="rename"/>"
		                           onclick="this.disabled = true; renameItem('<%=folderInformation.getFolderPath()%>', 'itemEdit<%=entryNumber%>', <%=folderInformation.getFolder()%>)"/>
		                    <input
		                        type="button" style="margin-left:5px;" class="button"
		                        value="<fmt:message key="cancel"/>"
		                        onclick="showHideCommon('rename_panel<%=entryNumber%>')"/>
		                </td>
		            </tr>
		            </tbody>
		        </table>
   			 </td>
			</tr>
		    	<tr class="copy-move-panel registryWriteOperation" id="create_panel<%=entryNumber%>" style="display:none;">
   				 <td colspan="3" align="left">
		        <table cellpadding="0" cellspacing="0" class="styledLeft">
		            <thead>
		            <tr>
		                <th><fmt:message key="hdfs.create.menu.text"/>
		                </th>
		            </tr>
		            </thead>
		            <tbody>
		            <tr>
		                <td>New <fmt:message key="folder"/> Name <span class="required">*</span>  <input value="<%=folderInformation.getName()%>" type="text"
		                                id="createFolder<%=entryNumber%>"/></td>
		            </tr>
		            <tr>
		                <td class="buttonRow">
		                    <input type="button" class="button" value="<fmt:message key="create"/>"
		                           onclick="this.disabled = true; createFolder('<%=folderInformation.getFolderPath()%>', 'createFolder<%=entryNumber%>')"/>
		                    <input
		                        type="button" style="margin-left:5px;" class="button"
		                        value="<fmt:message key="cancel"/>"
		                        onclick="showHideCommon('create_panel<%=entryNumber%>')"/>
		                </td>
		            </tr>
		            </tbody>
		        </table>
   			 </td>
			</tr>
				<tr class="copy-move-panel registryWriteOperation" id="upload_panel<%=entryNumber%>" style="display:none;">
				 <td colspan="3" align="left">
				 <form onsubmit="return submitUploadContentForm('<%=folderInformation.getFolderPath()%>');"  method="post" name="fileUploadForm"
	      				id="fileUploadForm"
	      				action ="../../fileupload/hdfs?path=<%=folderInformation.getFolderPath()%>"
	      				enctype="multipart/form-data" target="_self">
	   			   <table cellpadding="0" cellspacing="0" class="styledLeft">
			            <thead>
			            <tr>
			                <th colspan="3"><fmt:message key="hdfs.upload.file"/></th>
			            </tr>
			            </thead>
			            <tbody>
				            <tr>
						   	  <td valign="top" style="width:120px;">
				            	<span><fmt:message key="file"/> <span class="required">*</span></span></td>
				        	 <td>
					            <input id="uploadFile" type="file" name="uploadFile" style="background-color:#cccccc;position:relative" onkeypress="return blockManual(event)"  onchange="fillFileUploadDetails()" />
					
					            <div class="helpText" id="fileHelpText">
					                <fmt:message key="content.path.help.text"/>
					            </div>
				       		</td>
						</tr>
						 <tr>
	        				<td valign="top"><fmt:message key="name"/> <span class="required">*</span></td>
	
	        				<td><input id="uploadedFileName" type="text" name="uploadedFileName"
	                  				 style="margin-bottom:10px;"/></td>
	    				</tr>
					<tr>
			        <td class="buttonRow" colspan="2">
			            <input type="submit" class="button" value="<fmt:message key="add"/>"
			                   />
			            <input type="button" class="button" value="<fmt:message key="cancel"/>"
			                   onclick="showHideCommon('upload_panel<%=entryNumber%>')"/>
			        </td>
	    		 </tbody>
			  </table>
			  </form>
		</td>
		</tr>
				<tr id="symlinkContentUI<%=entryNumber%>" style="display:none;" class="copy-move-panel registryWriteOperation">
					<td colspan="2">
					<form name="symlinkContentForm1" id="symlinkContentForm1" action="/wso2registry/system/addSymbolicLink"
					      method="post">
					    <input type="hidden" id="srParentPath" name="path" />
					
					    <table class="styledLeft">
					    <tr>
					        <td class="middle-header" colspan="2"><fmt:message key="hdfs.makeLink.menu.text"/></td>
					    </tr>
					    <tr>
					        <td valign="top" style="width:120px;"><fmt:message key="name"/> <span
					                class="required">*</span></td>
					        <td><input type="text" id="srFileName" name="filename" style="margin-bottom:10px;"/></td>
					    </tr>
					    <tr>
					        <td valign="top"><fmt:message key="path"/><span
					                class="required">*</span></td>
					        <td>
					            <input id="srPath" name="targetpath"/>
					            <input type="button" class="button"
					                                       value=".." title="<fmt:message key="resource.tree"/>"
					                                       onclick="showHDFStree('srPath');"/>
					        </td>
					    </tr>
			    <tr>
			        <td colspan="2" class="buttonRow">
			            <input type="button" class="button" value="<fmt:message key="add"/>"
			                   style="margin-top:10px;"
			                   onclick="whileUpload();submitSymlinkContentForm();"/>
			            <input type="button" class="button"
			                   value="<fmt:message key="cancel"/>"
			                   onclick="showHide('add-link-div')"/>
			        </td>
			    </tr>
			    </table>
			
			</form>
			</td>
			</tr>
				
	   	<% 
		    }
		    if (totalCount <= itemsPerPage) {
		        //No paging
		      } else {
		%>
			<tr>
			    <td colspan="3" class="pagingRow" style="padding-top:10px; padding-bottom:10px;">
			
			        <%
			            if (pageNumber == 1) {
			        %>
			        <span class="disableLink">< Prev</span>
			        <%
			        } else {
			        %>
			        <a class="pageLinks" title="<fmt:message key="page.x.to.y"><fmt:param value="<%=(pageNumber - 1)%>"/><fmt:param value="<%=Utils.getFirstPage(pageNumber - 1, itemsPerPage, allFolders)%>"/><fmt:param value="<%=Utils.getLastPage(pageNumber - 1, itemsPerPage, allFolders)%>"/></fmt:message>"
			           onclick="navigatePages(<%=(pageNumber-1)%>, '<%=path%>','<%=viewMode%>','<%=resourceConsumer%>','<%=targetDivID%>')"><
			            <fmt:message key="prev"/></a>
			        <%
			            }
			            if (numberOfPages <= 10) {
			                for (int pageItem = 1; pageItem <= numberOfPages; pageItem++) { %>
			
			        <a title="<fmt:message key="page.x.to.y"><fmt:param value="<%=pageItem%>"/><fmt:param value="<%=Utils.getFirstPage(pageItem, itemsPerPage, allFolders)%>"/><fmt:param value="<%=Utils.getLastPage(pageItem, itemsPerPage, allFolders)%>"/></fmt:message>" class=<% if(pageNumber==pageItem){ %>"pageLinks-selected"<% } else { %>"pageLinks" <% } %>
			        onclick="navigatePages(<%=pageItem%>, '<%=path%>','<%=viewMode%>','<%=resourceConsumer%>','<%=targetDivID%>')" ><%=pageItem%></a>
			        <% }
			        } else {
			            // FIXME: The equals comparisons below looks buggy. Need to test whether the desired
			            // behaviour is met, when there are more than ten pages.
			            String place = "middle";
			            int pageItemFrom = pageNumber - 2;
			            int pageItemTo = pageNumber + 2;
			
			            if (numberOfPages - pageNumber <= 5) place = "end";
			            if (pageNumber <= 5) place = "start";
			
			            if (place == "start") {
			                pageItemFrom = 1;
			                pageItemTo = 7;
			            }
			            if (place == "end") {
			                pageItemFrom = numberOfPages - 7;
			                pageItemTo = numberOfPages;
			            }
			
			            if (place == "end" || place == "middle") {
			
			
			                for (int pageItem = 1; pageItem <= 2; pageItem++) { %>
			
			        <a class="pageLinks" title="<fmt:message key="page.x.to.y"><fmt:param value="<%=pageItem%>"/><fmt:param value="<%=Utils.getFirstPage(pageItem, itemsPerPage, allFolders)%>"/><fmt:param value="<%=Utils.getLastPage(pageItem, itemsPerPage, allFolders)%>"/></fmt:message>"
			           onclick="navigatePages(<%=pageItem%>, '<%=path%>','<%=viewMode%>','<%=resourceConsumer%>','<%=targetDivID%>')"><%=pageItem%>
			        </a>
			        <% } %>
			        ...
			        <%
			            }
			
			            for (int pageItem = pageItemFrom; pageItem <= pageItemTo; pageItem++) { %>
			
			        <a title="<fmt:message key="page.x.to.y"><fmt:param value="<%=pageItem%>"/><fmt:param value="<%=Utils.getFirstPage(pageItem, itemsPerPage, allFolders)%>"/><fmt:param value="<%=Utils.getLastPage(pageItem, itemsPerPage, allFolders)%>"/></fmt:message>" class=<% if(pageNumber==pageItem){ %>"pageLinks-selected"<% } else {%>"pageLinks"<% } %>
			        onclick="navigatePages(<%=pageItem%>, '<%=path%>','<%=viewMode%>','<%=resourceConsumer%>','<%=targetDivID%>')"><%=pageItem%></a>
			        <% }
			
			            if (place == "start" || place == "middle") {
			        %>
			        ...
			        <%
			            for (int pageItem = (numberOfPages - 1); pageItem <= numberOfPages; pageItem++) { %>
			
			        <a class="pageLinks" title="<fmt:message key="page.x.to.y"><fmt:param value="<%=pageItem%>"/><fmt:param value="<%=Utils.getFirstPage(pageItem, itemsPerPage, allFolders)%>"/><fmt:param value="<%=Utils.getLastPage(pageItem, itemsPerPage, allFolders)%>"/></fmt:message>"
			           onclick="navigatePages(<%=pageItem%>, '<%=path%>','<%=viewMode%>','<%=resourceConsumer%>','<%=targetDivID%>')"
			           style="margin-left:5px;margin-right:5px;"><%=pageItem%>
			        </a>
			        <% }
			        }
			
			            if (place == "middle") {
			
			            }
			            //End middle display
			        }
			            if (pageNumber == numberOfPages) {
			        %>
			        <span class="disableLink"><fmt:message key="next"/> ></span>
			        <%
			        } else {
			        %>
			        <a class="pageLinks" title="<fmt:message key="page.x.to.y"><fmt:param value="<%=(pageNumber + 1)%>"/><fmt:param value="<%=Utils.getFirstPage(pageNumber + 1, itemsPerPage, allFolders)%>"/><fmt:param value="<%=Utils.getLastPage(pageNumber + 1, itemsPerPage, allFolders)%>"/></fmt:message>"
			           onclick="navigatePages(<%=(pageNumber+1)%>, '<%=path%>','<%=viewMode%>','<%=resourceConsumer%>','<%=targetDivID%>')">Next
			            ></a>
			        <%
			            }
			        %>
				<span id="xx<%=pageNumber%>" style="display:none" />
			    </td>
			</tr>
<%
		    
        }
%>
    	</tbody>
    	  <%}
    	  }%>
     </table>
   </fmt:bundle>
 
</div>
