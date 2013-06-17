<%@ page contentType="text/html; charset=UTF-8" errorPage="error.jsp" isThreadSafe="false" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:useBean id="controller" scope="page" class="org.soaplab.clients.spinet.ServiceController">
  <jsp:setProperty name="controller" property="request" value="${pageContext.request}"/>
</jsp:useBean>
<c:set var="jobId" value="${controller.run}"/>
<script type="text/javascript">
window.parent.jobSubmitted('${jobId}','${controller.jobElementId}','${controller.serviceName}');
</script>
