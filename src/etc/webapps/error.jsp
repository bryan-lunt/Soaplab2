<!--
<%@ page contentType="text/html" isErrorPage="true"%>
-->
<div style="font-weight: bold; font-size:80%; color:red; background-color: yellow">
<%= ((java.lang.Exception)exception).getCause().getMessage() %>
<!-- Setting response code to 'Bad request' -->
<%
if (exception instanceof org.soaplab.share.SoaplabException) {
   ((javax.servlet.http.HttpServletResponse)pageContext.getResponse()).setStatus (400);
}
%>
</div>
