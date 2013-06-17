<%@ page contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:useBean id="controller" class="org.soaplab.clients.spinet.ServiceController">
  <jsp:setProperty name="controller" property="*"/>
</jsp:useBean>
<c:set var="status" value="${controller.status}"/>
<c:set var="prefix" value="${controller.serviceName}$${controller.jobElementId}$"/>
<c:choose>

  <c:when test="${status eq 'RUNNING'}">
    <div class="status-running">RUNNING <span id="${prefix}secs"><img src="images/loading.gif" align="absbottom" class="loading-img"/></span></div>
    <div id="${prefix}body">Status updated every&nbsp;<input id="${prefix}period" class="input-period" type="text" value="2"/></span>&nbsp;seconds<br></div>
    <div id="${prefix}body">
    <c:import url="resultslist.jsp">
      <c:param name="serviceName" value="${param.serviceName}"/>
      <c:param name="jobId" value="${param.jobId}"/>
      <c:param name="jobElementId" value="${param.jobElementId}"/>
    </c:import>
    </div>
    <input type="button" class="small-button" value=" Update now " onClick="updateStatus('${controller.serviceName}','${controller.jobElementId}','${param['jobId']}')"/>
    <input type="button" class="small-button" value=" Terminate " onClick="terminateJob('${param.serviceName}','${controller.jobElementId}','${param.jobId}')"/>
  </c:when>

  <c:when test="${status eq 'COMPLETED'}">
    <div class="status-completed">COMPLETED</div>
    <div id="${prefix}body">
    <c:import url="resultslist.jsp">
      <c:param name="serviceName" value="${param.serviceName}"/>
      <c:param name="jobId" value="${param.jobId}"/>
      <c:param name="jobElementId" value="${param.jobElementId}"/>
    </c:import>
    </div>
    <input type="button" class="small-button" value=" Remove " onClick="removeAndDestroyJob('${param.serviceName}','${controller.jobElementId}','${param.jobId}')"/>
  </c:when>

  <c:when test="${status eq 'TERMINATED_BY_REQUEST'}">
    <div class="status-user-terminated">TERMINATED BY REQUEST</div>
    <div id="${prefix}body">
    <c:import url="resultslist.jsp">
      <c:param name="serviceName" value="${param.serviceName}"/>
      <c:param name="jobId" value="${param.jobId}"/>
      <c:param name="jobElementId" value="${param.jobElementId}"/>
    </c:import>
    </div>
    <input type="button" class="small-button" value=" Remove " onClick="removeAndDestroyJob('${param.serviceName}','${controller.jobElementId}','${param.jobId}')"/>
  </c:when>

  <c:when test="${status eq 'TERMINATED_BY_ERROR'}">
    <div class="status-error-terminated">TERMINATED BY ERROR</div>
    <div id="${prefix}body">
    <c:import url="resultslist.jsp">
      <c:param name="serviceName" value="${param.serviceName}"/>
      <c:param name="jobId" value="${param.jobId}"/>
      <c:param name="jobElementId" value="${param.jobElementId}"/>
    </c:import>
    </div>
    <input type="button" class="small-button" value=" Remove " onClick="removeAndDestroyJob('${param.serviceName}','${controller.jobElementId}','${param.jobId}')"/>
  </c:when>

  <c:otherwise>
    <div class="status-unknown">${status}</div>
    <div id="${prefix}body">
    <c:import url="resultslist.jsp">
      <c:param name="serviceName" value="${param.serviceName}"/>
      <c:param name="jobId" value="${param.jobId}"/>
      <c:param name="jobElementId" value="${param.jobElementId}"/>
    </c:import>
    </div>
    <input type="button" class="small-button" value=" Remove " onClick="removeAndDestroyJob('${param.serviceName}','${controller.jobElementId}','${param.jobId}')"/>
  </c:otherwise>

</c:choose>
<input type="hidden" name="${prefix}jobid" value="${param['jobId']}"/>
<input type="hidden" name="${prefix}status" id="${prefix}status" value="${controller.status}"/>
