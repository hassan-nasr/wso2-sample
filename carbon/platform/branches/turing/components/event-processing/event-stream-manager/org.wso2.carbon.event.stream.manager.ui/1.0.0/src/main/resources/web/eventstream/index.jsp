<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page import="org.wso2.carbon.event.stream.manager.stub.EventStreamAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.stream.manager.stub.types.EventStreamInfoDto" %>
<%@ page import="org.wso2.carbon.event.stream.manager.ui.EventStreamUIUtils" %>

<fmt:bundle basename="org.wso2.carbon.event.stream.manager.ui.i18n.Resources">

    <carbon:breadcrumb
            label="eventstream.list"
            resourceBundle="org.wso2.carbon.event.stream.manager.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>

    <script type="text/javascript">

        var ENABLE = "enable";
        var DISABLE = "disable";
        var STAT = "statistics";
        var TRACE = "Tracing";

        function doDelete(eventStreamName, eventStreamVersion) {
            CARBON.showConfirmationDialog(
                    "If event stream is deleted then related configuration files will be go into inactive state! Are you sure want to delete?", function () {
                        var theform = document.getElementById('deleteForm');
                        theform.eventStream.value = eventStreamName;
                        theform.eventStreamVersion.value = eventStreamVersion;
                        theform.submit();
                    },null,null);
        }

    </script>
    <%
        EventStreamAdminServiceStub stub = EventStreamUIUtils.getEventStreamAdminService(config, session, request);
        String eventStreamName = request.getParameter("eventStream");
        String eventStreamVersion = request.getParameter("eventStreamVersion");
        int totalEventStreams = 0;
        if (eventStreamName != null && eventStreamVersion != null) {
            stub.removeEventStreamInfo(eventStreamName, eventStreamVersion);
    %>
    <script type="text/javascript">CARBON.showInfoDialog('Event Stream successfully deleted.');</script>
    <%
        }

        EventStreamInfoDto[] eventStreamDetailsArray = stub.getAllEventStreamInfoDto();
        if (eventStreamDetailsArray != null) {
            totalEventStreams = eventStreamDetailsArray.length;
        }

    %>

    <div id="middle">
        <h2><fmt:message key="available.event.streams"/></h2>
        <a href="create_event_stream.jsp?ordinal=1"
           style="background-image:url(images/add.gif);"
           class="icon-link">
            Add Event Stream
        </a>
        <br/> <br/>

        <div id="workArea">

            <%=totalEventStreams%> <fmt:message
                key="total.event.streams"/>
            <br/><br/>
            <table class="styledLeft">
                <%

                    if (eventStreamDetailsArray != null) {
                %>
                <thead>
                <tr>
                    <th><fmt:message key="event.stream.name"/></th>
                    <th><fmt:message key="event.stream.version"/></th>
                    <th width="65%"><fmt:message key="event.stream.definition"/></th>
                    <th width="10%"><fmt:message key="actions"/></th>
                </tr>
                </thead>
                <tbody>
                <%
                    for (EventStreamInfoDto eventStreamInfoDto : eventStreamDetailsArray) {
                %>
                <tr>
                        <%--<td>--%>
                        <%--<a href="eventFormatter_details.jsp?ordinal=1&eventFormatterName=<%=eventStreamInfoDto.getEventFormatterName()%>"><%=eventStreamInfoDto.getEventFormatterName()%>--%>
                        <%--</a>--%>

                        <%--</td>--%>
                    <td><%=eventStreamInfoDto.getStreamName()%>
                    </td>
                    <td><%=eventStreamInfoDto.getStreamVersion()%>
                    </td>
                    <td><%=eventStreamInfoDto.getStreamDefinition()%>
                    </td>
                    <td>
                        <a style="background-image: url(../admin/images/delete.gif);"
                           class="icon-link"
                           onclick="doDelete('<%=eventStreamInfoDto.getStreamName()%>','<%=eventStreamInfoDto.getStreamVersion()%>')"><font
                                color="#4682b4">Delete</font></a>
                            <%--<a style="background-image: url(../admin/images/edit.gif);"--%>
                            <%--class="icon-link"--%>
                            <%--href="edit_event_formatter_details.jsp?ordinal=1&eventFormatterName=<%=eventStreamInfoDto.getEventFormatterName()%>"><font--%>
                            <%--color="#4682b4">Edit</font></a>--%>

                    </td>


                </tr>
                </tbody>
                <%
                    }

                } else {
                %>

                <tbody>
                <tr>
                    <td class="formRaw">
                        <table id="noEventStreamInputTable" class="normal-nopadding"
                               style="width:100%">
                            <tbody>

                            <tr>
                                <td class="leftCol-med" colspan="2"><fmt:message
                                        key="empty.event.stream.msg"/>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </td>
                </tr>
                </tbody>
                <%
                    }
                %>

            </table>

            <div>
                <br/>

                <form id="deleteForm" name="input" action="" method="get"><input type="HIDDEN"
                                                                                 name="eventStream"
                                                                                 value=""/>
                    <input type="HIDDEN"
                           name="eventStreamVersion"
                           value=""/>
                </form>

            </div>
        </div>
    </div>

    <script type="text/javascript">
        alternateTableRows('expiredsubscriptions', 'tableEvenRow', 'tableOddRow');
        alternateTableRows('validsubscriptions', 'tableEvenRow', 'tableOddRow');
    </script>

</fmt:bundle>
