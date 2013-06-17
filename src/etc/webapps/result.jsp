<jsp:useBean
  id="controller" class="org.soaplab.clients.spinet.ServiceController"
  ><jsp:setProperty name="controller" property="*"
  /></jsp:useBean><%
String idxStr = request.getParameter ("resultIdx");
Object result = controller.getResult();
if (result.getClass().isArray() && !(result instanceof byte[])) {
   if (idxStr == null) { 
       %><jsp:forward page="resultlinks2.jsp">  
         <jsp:param name="serviceName" value="${param.serviceName}"/> 
         <jsp:param name="resultName" value="${param.resultName}"/> 
         <jsp:param name="jobId" value="${param.jobId}"/> 
      </jsp:forward><%
      return;
   } else {
      Object[] parts = (Object[])result;
      int idx = Integer.parseInt (idxStr) - 1;
      if (idx < 0 || idx >= parts.length)
         idx = 0;
      result = parts[idx];
   }
}

response.reset();
org.soaplab.clients.spinet.ResultInfo info = controller.getResultTypes();
response.setCharacterEncoding ("UTF-8");
response.setContentType (info.getContentType());
if (result instanceof byte[]) {
   byte[] bytes = (byte[])result;
   response.addHeader ("Content-disposition",
                       "inline; filename=" +
                       request.getParameter ("serviceName") + "-" +
                       request.getParameter ("resultName") +
                       info.getFileExtension());
   response.setContentLength (bytes.length);
   ServletOutputStream outputStream = response.getOutputStream();
   outputStream.write (bytes);
   outputStream.flush();
   return;
} else {
   response.setContentLength (result.toString().length());
   java.io.PrintWriter writer = response.getWriter();
   writer.print (result.toString());
   writer.flush();
}
%>
