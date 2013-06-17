<%@ page contentType="text/html" errorPage="error.jsp" isThreadSafe="false" %>
<jsp:useBean id="controller" scope="page" class="org.soaplab.clients.spinet.ServiceController">
  <jsp:setProperty name="controller" property="request" value="${pageContext.request}"/>
</jsp:useBean>
<script type="text/javascript">
window.parent.showInputs ('${controller.show}','${controller.jobElementId}');
</script>
