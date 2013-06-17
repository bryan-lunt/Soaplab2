<%@ page contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
<title>${param.serviceName} result</title>
<link rel="stylesheet" href="images/stylesheet.css" type="text/css">
<jsp:useBean id="controller" class="org.soaplab.clients.spinet.ServiceController">
   <jsp:setProperty name="controller" property="*"/>
</jsp:useBean>
</head>

<body>
<div class="result-bar">
<div class="result-title">${param.serviceName}<span class="result-note"><br>[select page]</span></div>
<ul class="result-links">
<c:forEach varStatus='status' var="item" items="${controller.resultArray}">
  <li><a href="${item}" target="result">${status.count}</a>
</c:forEach>
</ul>
</div>
<iframe name="result" class="result-body">
</iframe>
<br clear="all">
</body>
</html>
