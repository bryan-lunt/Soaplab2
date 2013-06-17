<%@ page contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<html>
<head>
<title>${param.serviceName} result</title>
<link rel="stylesheet" href="images/stylesheet.css" type="text/css">
<jsp:useBean id="controller" class="org.soaplab.clients.spinet.ServiceController">
   <jsp:setProperty name="controller" property="*"/>
</jsp:useBean>
</head>

<body>
<c:set var="items" value="${controller.resultArray}"/>
<c:set var="count" value="${fn:length(items)}"/>
<c:choose>

<c:when test="${count == 1}">
<jsp:forward page="result.jsp">
   <jsp:param name="serviceName" value="${param.serviceName}"/>
   <jsp:param name="jobId"       value="${param.jobId}"/>
   <jsp:param name="resultName"  value="${param.resultName}"/>
   <jsp:param name="resultIdx"   value="1"/>
</jsp:forward>
</c:when>

<c:otherwise>
<div class="result-bar">
<div class="result-title">${param.serviceName}<span class="result-note"><br>[select page]</span></div>
<ul class="result-links">
<c:forEach varStatus='status' var="item" items="${controller.resultArray}">
  <c:url var="href" value="result.jsp">
     <c:param name="serviceName" value="${param.serviceName}"/>
     <c:param name="jobId" value="${param.jobId}"/>
     <c:param name="resultName" value="${param.resultName}"/>
     <c:param name="resultIdx" value="${status.count}"/>
  </c:url>
  <li><a href="${href}" target="result">${status.count}</a>
</c:forEach>
</ul>
</div>
<iframe name="result" class="result-body">
</iframe>
<br clear="all">
</c:otherwise>

</c:choose>

</body>
</html>
