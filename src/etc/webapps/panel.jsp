<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:useBean id="controller" class="org.soaplab.clients.spinet.ServiceController">
  <jsp:setProperty name="controller" property="*"/>
</jsp:useBean>

<c:forEach var="part" items="${controller.panelMap}">
   <c:choose>
      <c:when test="${part.key eq 'inputs'}">
         <c:set var="inputs" value="${part.value}"/>
      </c:when>
      <c:when test="${part.key eq 'apphelp'}">
         <c:set var="apphelp" value="${part.value}"/>
      </c:when>
      <c:when test="${part.key eq 'outputs'}">
         <c:set var="outputs" value="${part.value}"/>
      </c:when>
   </c:choose>
</c:forEach>
<c:set var="cleanServiceName" value="${controller.cleanServiceName}"/>

<form accept-charset="UTF-8" method="post" name="${param.serviceName}_form" enctype="multipart/form-data" action="javascript:void%200" onSubmit="return submitForm(this)">
<input type="hidden" name="serviceName" value="${param.serviceName}"/>
<input type="hidden" name="jobElementId" value=""/>

<script type="text/javascript">
var supernote = new SuperNote ('${cleanServiceName}', { 'hideDelay': 250 });
</script>

<!-- TOP: button(s) and service status -->
<div style="float:left;margin:5px">
<a href="javascript:void%200" class="${cleanServiceName}-hover-${cleanServiceName}"><img src="images/help_view.gif" border="0" alt="HELP" width="16" height="16" /></a>
<input type="submit" name="${param.serviceName}_run" value=" Run service "  class="button"/><br>
</div>
<div id="${param.serviceName}_status" class="status"></div>
<br clear="both">


<!-- MIDDLE: inputs, outputs and messages -->
<table border=0 cellpadding=5>

<tr>
<th align="left">Inputs</th>
<th align="left">Report</th>
</tr>

<tr>
<td valign="top">
${inputs}
</td>
<td valign="top" rowspan="2" class="message" id="${param.serviceName}$message">
&nbsp;
</td>
</tr>

<tr>
<td valign="top">
<input type="button" name="${param.serviceName}_res" value=" Reset fields " class="button" onClick="resetForm(this.form)"/>
</td>
</tr>

</table>
</form>
<!-- footnotes for the application tooltips notes -->
<div class="snp-mouseoffset pinnable tooltip" id="${cleanServiceName}-note-${cleanServiceName}">
${apphelp}
</div>
<!--- end of footnotes -->
