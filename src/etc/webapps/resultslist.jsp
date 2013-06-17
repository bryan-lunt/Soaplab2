<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:useBean id="controller" class="org.soaplab.clients.spinet.ServiceController">
  <jsp:setProperty name="controller" property="*"/>
</jsp:useBean>
<table CELLPADDING="2" class="result-list">
  <tr>
  <th colspan="2">Result</th>
  <th>Size</th>
  <th>Type</th>
  </tr>
<c:forEach var="info" items="${controller.resultsInfoList}" varStatus="iterstatus">
  <c:if test="${ (info.name eq 'report') and (not iterstatus.first) }">
     <tr><td colspan="4" class="input-separator"><hr></td></tr>
  </c:if>
  <c:choose>
  <c:when test="${empty info.href}">
    <c:choose>
    <c:when test="${info.isArray eq 'true'}">
       <c:url var="href" value="resultlinks.jsp">
         <c:param name="serviceName" value="${param.serviceName}"/>
         <c:param name="jobId" value="${param.jobId}"/>
         <c:param name="resultName" value="${info.name}"/>
       </c:url>
    </c:when>
    <c:otherwise>
       <c:url var="href" value="result.jsp">
         <c:param name="serviceName" value="${param.serviceName}"/>
         <c:param name="jobId" value="${param.jobId}"/>
         <c:param name="resultName" value="${info.name}"/>
       </c:url>
    </c:otherwise>
    </c:choose>
  </c:when>
  <c:otherwise>
    <c:url var="href" value="${info.href}"/>
  </c:otherwise>
  </c:choose>
  <tr>
  <td><img src="images/<c:choose><c:when test="${info.name eq 'report'}">report.gif</c:when><c:when test="${info.name eq 'detailed_status'}">detailed_status.gif</c:when><c:otherwise>result.gif</c:otherwise></c:choose>" class="result-img"/></td>
   <td>
<a href="${href}" target="_blank" onclick="return getResult('${param.jobId}','${info.name}','${param.serviceName}','${param.jobElementId}')">${info.displayName}</a>&nbsp;
   </td>
   <td align="right">${info.size}</td>
   <td>${info.displayType}</td>
   <tr>
</c:forEach>
</table>
