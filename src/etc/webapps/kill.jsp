<%@ page contentType="text/plain" %>
<jsp:useBean id="controller" class="org.soaplab.clients.spinet.ServiceController">
<jsp:setProperty name="controller" property="*"/>
</jsp:useBean>
${controller.kill}
