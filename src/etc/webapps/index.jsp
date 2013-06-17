<!--
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
-->

<html lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>@SPINET_WELCOME_MSG@</title>
<link rel="stylesheet" href="images/stylesheet.css" type="text/css" />
<link rel="stylesheet" href="images/supernote.css" type="text/css" />

<script src="js/prototype.js" type="text/javascript"></script>
<script src="js/supernote.js" type="text/javascript"></script>
<script src="js/services.js" type="text/javascript"></script>

<jsp:useBean id="list" class="org.soaplab.clients.spinet.CategoryList"/>

</head>

<body>

<script type="text/javascript">
var supernote = new SuperNote ('supernote', { 'hideDelay': 250 });
</script>

<!-- header -->
<table summary="header">
<tr>

<td valign="top">
<a href="@SOAPLAB_URL@"><img class="motto-img" src="images/SoaplabLogo-small.png" border="0" align="left" width="141" height="113" alt="Soaplab logo" /></a>
</td>

<td align="center" width="100%">
<h1>@SPINET_WELCOME_MSG@</h1>
<div class="version">
[Soaplab version: @SOAPLAB_VERSION@, build: @SOAPLAB_BUILD@]
<br/><br/>
</div>
</td>

<td valign="top" align="right">
<a href="@SOAPLAB_URL@/SpinetClient.html" class="supernote-hover-spinethelpbutton"><img src="images/help_view.gif" border="0" width="16" height="16" alt="HELP" /></a><br/><br/>

<!--
<a href="javascript:void%200" class="supernote-hover-spinetfilldatabutton"><img src="images/filldata.gif" border="0" width="16" height="16" /></a><br/><br/>
-->

<a href="javascript:void%200" class="supernote-hover-spinetcollapsebutton" onClick="collapseAll()"><img src="images/collapse.gif" border="0" width="16" height="16" alt="COLLAPSE" /></a>

</td>

</tr>
</table>

<!-- services -->
<table border="0" cellpadding="5" width="100%" summary="services">

<tr>
   <th valign="top" class="main-header">Category</th>
   <th valign="top" class="main-header">Service name</th>
   <th valign="top" class="main-header">Description</th>
</tr>

<c:forEach var="category" items="${list.categories}">
 
  <tr valign="top" class="category-header">
    <td valign="top" class="category-name">
      <a name="${category.name}">${category.displayName}</a>
    </td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
  </tr>
  
  <c:forEach var="service" items="${category.services}">

    <tr valign="top" class="${category.name}-service-row" id="${service.name}_row">
      <td>&nbsp;</td>
      <td>
      	<a href="javascript:void%200" onClick="togglePanel('${service.name}')">${service.displayName}</a>
      	<c:if test="@SPINET_INDIVIDUAL_WSDL_LINKS@">
            (<a  style="font-size: smaller;" title="for programmatic access" href="typed/services/${service.name}?wsdl" >WSDL</a>)
        </c:if>
      </td>
      <td class="description">${service.description}</td>
    </tr>

    <tr valign="top" id="${service.name}_wrapper" style="display:none">
      <td colspan="3"><div class="panel" id="${service.name}_panel" style="display:none;"></div></td>
    </tr>

  </c:forEach>
</c:forEach>

</table>
<br/>

<!-- bottom footer -->
<hr/>
<div align="right"><font size="-2">
<a href="@SOAPLAB_URL@">Soaplab2 documentation</a><br/>
Contact: @SOAPLAB_CONTACT@
</font></div>

<!-- footnotes for the tooltips notes -->
<div class="snp-mouseoffset pinnable tooltip" id="supernote-note-spinethelpbutton">
<h5>Spinet Web Client to Soaplab2 services</h5>

Click on this Help button to get detailed information how to use it
and what to avoid.<br/><br/>

And, by the way, this is a <a href="http://en.wikipedia.org/wiki/Oval_spinet" target="_blank">spinet</a>:
<br/><br/>

<center>
<img src="images/oval-spinet.jpg" border="0" align="bottom" width="250" height="74" alt="What is a spinet" />
</center>
<br clear="all">
<div class="tooltip-credit">Tooltips library from <a href="http://www.twinhelix.com" target="_blank">TwinHelix
Designs</a></div>
</div>

<div class="snp-mouseoffset pinnable tooltip" id="supernote-note-spinetfilldatabutton">
<h5>Inject example input data</h5>

Clicking on this button fills many of the services above by a testing
input data. You can then run them without bothering to find how to
feed them.<br/><br/>

<div class="message">Sorry, not yet implemented...</div>
<br/>
</div>

<div class="snp-mouseoffset pinnable tooltip" id="supernote-note-spinetcollapsebutton">
<h5>Collapse/close all open panels</h5>

All currently open service panels will be closed - and your screen
will be again manageable.<br/><br/>

However, all contents of the panel input forms will be kept. When you
click again on any previously open service name, you will see what you
left there.
</div>
@WEB_MONITOR@
</body>
</html>
