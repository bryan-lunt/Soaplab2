// all URLs separated here
var PANEL_URL   = "panel.jsp";
var RUN_URL     = "run.jsp";
//var RUN_URL     = "show.jsp";
var STATUS_URL  = "status.jsp";
var RESULT_URL  = "result.jsp";
var KILL_URL    = "kill.jsp";
var DESTROY_URL = "destroy.jsp";

// for assigning unique DOM's IDs
var JOB_COUNT = 0;

function submitForm (form) {
   if (!checkForm (form))
      return false;

   var serviceName = getServiceName (form);
   var elementForJobId = $(form['jobElementId']);
   var status = $(serviceName + "_status");

   // create a new status sub-element for this submit/job
   JOB_COUNT++;
   var job = $(document.createElement ('div')).addClassName ('job-status');
   job.id = 'J' + JOB_COUNT;
   job.update ('Uploading inputs...' + '<br>' + cancelButton (job.id));
   status.appendChild (job);
   elementForJobId.value = job.id;

   // create a new iframe sub-element...
   var iframeId = 'F' + JOB_COUNT;
   var d = $(document.createElement ('div')).addClassName ('smallmessage');
   d.innerHTML = '<iframe style="display:none" src="about:blank" id="' + iframeId + '" name="' + iframeId + '" onload="iframeLoaded(\'' + iframeId + '\')"></iframe>';
   job.appendChild (d);
   var iframeElement = document.getElementById (iframeId);
   iframeElement.jelid = job.id;

   // ...as a submit target
   form.setAttribute ('target', iframeId);
   form.setAttribute ('action', RUN_URL);
   return true;
}

// called when we have a response showing user's inputs
// (note that this is called from within an iframe):
// response ... inputs,
// jobElementId ... where to display the response,
function showInputs (response, jobElementId) {
   var jobElement = $(jobElementId);
   jobElement.okay = 1;  // signal to iframe.onload
   jobElement.update (response);
}

// called when we have a response from submitting a job
// (note that this is called from within an iframe):
// response ... Soaplab's jobId,
// jobElementId ... where to display the response,
// serviceName ... for which service
function jobSubmitted (response, jobElementId, serviceName) {
   var jobElement = $(jobElementId);
   jobElement.okay = 1;  // signal to iframe.onload
   var period = checkAndGetPeriod (serviceName, jobElementId);
   updateStatus (serviceName, jobElementId, response, period);
}

// called when an iFrame is loaded
// - in case of error, only this is called;
// - if no error, jobSumbitted() is called first, then this one;
// it checks if jobSubmitted() left an okay message - if yes, it does nothing,
// else it make itself visible (because in such case it contains
// an error message created by error.jsp);
// iframeId ... an Id of this (just loaded) iframe
function iframeLoaded (iframeId) {
   var ife = window.parent.$(iframeId);
   var jobElement = window.parent.$(ife.jelid);
   if (jobElement.okay != null)
      return;
   ife.show();
}

// cancelling/terminating/removing a job
// (it also removes the iframe accompanied with the job)
function cancelButton (jobElementId, label) {
   if (label == null) label = " Cancel ";
   return '<input type="button" class="small-button" value="' + label + '" onClick="removeJob(\'' + jobElementId + '\')"/>';
}
function removeJob (jobElementId) {
   var job = $(jobElementId);
   job.parentNode.removeChild (job);
}

function terminateJob (serviceName, jobElementId, jobId) {
   if (jobId == null)
      return;
   var bag = {
      target       : $(jobElementId),
      serviceName  : serviceName,
      jobElementId : jobElementId,
      jobId        : jobId
   };
   new Ajax.Request (KILL_URL, {
      method: 'post',
      parameters: {
	 serviceName  : serviceName,
	 jobId        : jobId
      },
      onSuccess: function (data) {
	 updateStatus (this.serviceName, this.jobElementId, this.jobId, 1);
      }.bind (bag),
      onFailure: function (data) {
	 var message = data.responseText.replace (/^.*<body>/i, "").replace (/<\/body>.*$/i, "");
	 this.target.addClassName ('tomcat')
	    .update (message + '<br>' + cancelButton (this.jobElementId, " Remove "));
      }.bind (bag)
   });
}

function removeAndDestroyJob (serviceName, jobElementId, jobId) {
   removeJob (jobElementId);
   if (jobId == null)
      return;
   new Ajax.Request (DESTROY_URL, {
      method: 'post',
      parameters: {
	 serviceName  : serviceName,
	 jobId        : jobId
      },
      onSuccess: function (data) {
      },
      onFailure: function (data) {
      }
   });
}

// keys: service names (those who have been clicked on)
// values: 1 ... service panel is here, use it
//         0 ... service panel needs to be reloaded
//               (equals to non existent panels)
var panels = {};   // keys: service names

// called when a service name is clicked on
function togglePanel (serviceName) {
   $(serviceName + '_wrapper').toggle();
   var target = $(serviceName + "_panel").toggle();
   if (! target.visible()) {
      return;
   }
   var pos = Position.cumulativeOffset (target);
   window.scrollTo (pos[0], pos[1] - 35);
   
   // a bag packed for the callback function
   var bag = {
      target: target,
      serviceName: serviceName
   };

   if (! panels.hasOwnProperty (serviceName) || panels [serviceName] == 0) {
      target.update ('Loading...');
      new Ajax.Request (PANEL_URL, {
         method: 'get',
	 parameters: 'serviceName=' + serviceName,
	 onSuccess: function (data) {
	    this.target.update (data.responseText);
	    panels [this.serviceName] = 1;
	 }.bind (bag),
	 onFailure: function (data) {
	    var message = data.responseText.replace (/^.*<body>/i, "").replace (/<\/body>.*$/i, "");
	    this.target.addClassName ('tomcat').update (message);
	    panels [this.serviceName] = 0;
	 }.bind (bag)
      });
   }
}

// select the given radio element in the given form
function checkRadio (form, element) {
   var radio = form [element];
   if (radio != null)
      radio.checked = true;
}

// propagate DB selection to its USA-suffix field:
// form ... where is a 'selectElement' from;
// usaElementName ... where to put selected text;
// strippedUsaName ... simplest 'usaElementName'
// commentName ... where to put selected value
function injectDB (form, selectElement, usaElementName, strippedUsaName, commentName) {
   var idx = selectElement.selectedIndex;
   var selectedValue = selectElement.options[idx].value;
   var selectedText = selectElement.options[idx].text;
   $(commentName).update (selectedValue);
   if (selectedText == "--Select DB--")
      return;
   var target = $(form[usaElementName]);
   var tValue = target.value;
   if (tValue.blank() || tValue.endsWith (":")) {
      target.value = selectedText + ":";
      Form.Element.focus (target);
   } else {
      target.value = tValue.sub (/.*:/, selectedText + ":");
   }
   checkRadio (form, strippedUsaName + "$TOGGLE_usa");
}

function resetForm (form) {
   Form.reset (form);
   var serviceName = getServiceName (form);
   var msgElems = $$(".smallmessage").concat ($$(".showdb-comment"));
   for (var i = 0; i < msgElems.length; i++) {
      if (msgElems[i].id.startsWith (serviceName)) {
	 msgElems[i].update ("");
      }
   }
}

function collapseAll() {
   for (var serviceName in panels) {
      $(serviceName + '_wrapper').hide();
      $(serviceName + "_panel").hide();
   }
}

function getServiceName (form) {
   return $(form['serviceName']).value;
}

function clearMsg (element) {
   element.setStyle ({display: 'none' });
}

function updateStatus (serviceName, jobElementId, jobId, period) {
   // a bag packed for the callback function
   var bag = {
      target       : $(jobElementId),
      serviceName  : serviceName,
      jobElementId : jobElementId,
      jobId        : jobId,
      period       : (period == null ? 2 : period)
   };
   new Ajax.Request (STATUS_URL, {
      method: 'post',
      parameters: {
	 serviceName  : serviceName,
	 jobElementId : jobElementId,
	 jobId        : jobId
      },
      onSuccess: function (data) {
	 this.period = checkAndGetPeriod (this.serviceName, this.jobElementId);
	 this.target.update (data.responseText);
	 var period = $([this.serviceName, this.jobElementId, 'period'].join ('$'));
	 if (period != null)
	    period.value = this.period;

	 var status = $([this.serviceName, this.jobElementId, 'status'].join ('$')).getValue();
	 if (status == 'COMPLETED' || status == 'TERMINATED_BY_ERROR' || status == 'TERMINATED_BY_REQUEST') {
	    // need some time to get from this stack
	    setTimeout (function() {
	       getResult (this.jobId, 'report', this.serviceName, this.jobElementId);
	    }.bind (bag), 1000);
	 } else {
            // otherwise start another update request
            setTimeout (function() {
	       updateStatus (this.serviceName, this.jobElementId, this.jobId, this.period);
	    }.bind (bag),
			this.period * 1000);
	 }
      }.bind (bag),
      onFailure: function (data) {
	 var message = data.responseText.replace (/^.*<body>/i, "").replace (/<\/body>.*$/i, "");
	 this.target.addClassName ('tomcat')
	    .update (message + '<br>' + cancelButton (this.jobElementId, " Remove "));
      }.bind (bag)
   });
}

function checkAndGetPeriod (serviceName, jobElementId) {
   var period = $([serviceName, jobElementId, 'period'].join ('$'));
   if (period == null)
      return 2;
   period.value = period.value.strip();
   if (period.getValue().blank())
      return 2;
   if (INT_REGEX.test (period.getValue()))
      return period.getValue();
   return 2;
}

// report and detailed status is gotten by Ajax,
// other results are obtained by  normal link and
// open in a new window
function getResult (jobId, resultName, serviceName, jobElementId) {
   var specialResults = /^(report|detailed_status)$/;
   if (! specialResults.test (resultName)) {
      return true;
   }

   // a bag packed for the callback function
   var bag = {
      target: $(serviceName + '$message'),
      jobElementId : jobElementId,
      serviceName: serviceName
   };

   new Ajax.Request (RESULT_URL, {
      method: 'post',
      parameters: {
	 serviceName : serviceName,
	 resultName  : resultName,
	 jobId       : jobId
      },
      onSuccess: function (data) {
	 this.target.addClassName ('inline-result')
	    .update ('<pre>' + data.responseText.escapeHTML() + '</pre>');
      }.bind (bag),
      onFailure: function (data) {
	 var message = data.responseText.replace (/^.*<body>/i, "").replace (/<\/body>.*$/i, "");
	 this.target.addClassName ('tomcat')
	    .update (message + '<br>' + cancelButton (this.jobElementId, " Remove "));
      }.bind (bag)
   });
   return false;
}

// ------------------------------
// --- tests for input fields ---
// ------------------------------

var TESTS = {};

// validate input fields before submitting,
// return true or false depending on success
function checkForm (form) {
   var serviceName = getServiceName (form);

   // clear previous messages (belonging to this form)
   var msgElems = $$(".smallmessage");
   for (var i = 0; i < msgElems.length; i++) {
      if (msgElems[i].id.startsWith (serviceName)) {
	 clearMsg (msgElems[i]);
      }
   }

   var result = true;
   var serviceTests = TESTS [serviceName];
   for (var i = 0; i < serviceTests.length; i++) {
      var exp = serviceTests[i];
      if (exp!=undefined && ! eval (exp)){
	  result = false;
	 }
   }
   return result;
}

// report an error message
function reportMsg (element, msg) {
   element.update ("<br>" + msg);
   element.setStyle ({display: 'block' });
}

// remove trailing $XXX part
function stripName (name) {
   return name.sub (/\$.*/, "");
}

// form where 'elementName' exists,
// elementName - a name of the tested element
// [report - where to report errors]
function isMandatory (form, elementName, report) {
   var e = $(form[elementName]);
   var serviceName = getServiceName (form);
   if (report == null)
      report = $(serviceName + "$" + elementName + "$MESSAGE");
   if (e.value.blank()) {
      reportMsg (report, stripName (elementName) + " is mandatory");
      return false;
   }
   return true;
}

// form where 'elementName' exists,
// elementName - a name of the tested element
// (element's name is only a base name, needs various suffixes)
function isInputMandatory (form, elementName) {
   var result = true;
   var serviceName = getServiceName (form);
   var report = $(serviceName + "$" + elementName + "$MESSAGE");
   var toggle = form[elementName + "$TOGGLE"];
   if (toggle == null) {
      // standalone direct input (textarea + fileupload)
      var dataName = elementName + "$INPUTDATAALONE";
      var uploadName = elementName + "$UPLOAD";
      result = (isMandatory (form, dataName, report) ||
		isMandatory (form, uploadName, report));
   } else {
      if ((toggle[1].checked && toggle[1].value == "_direct_data") ||
	  (toggle[0].checked && toggle[0].value == "_direct_data")) {
	 // test textarea + fileupload data
	 var dataName = elementName + "_direct_data$INPUTDATA";
	 var uploadName = elementName + "_direct_data$UPLOAD";
	 result = (isMandatory (form, dataName, report) ||
		   isMandatory (form, uploadName, report));
      } else {
         // test url data
	 var dataName = elementName + "_url$INPUTDATA";
	 if (form[dataName] == null)
	    dataName = elementName + "_usa$INPUTDATA";
	 result = isMandatory (form, dataName, report);
      }
   }
   if (result)
      clearMsg (report);
   else
      reportMsg (report, elementName + " is mandatory");
   return result;
}

var INT_REGEX = new RegExp ("^[-+]?\\d*$");
var FLOAT_REGEX = new RegExp ("^[-+]?[0-9]*\\.?[0-9]*([eE][-+]?[0-9]+)?$");

// form where 'elementName' exists,
// elementName - a name of the tested element
function isInteger (form, elementName) {
   var e = $(form[elementName]);
   var serviceName = getServiceName (form);
   var report = $(serviceName + "$" + elementName + "$MESSAGE");
   e.value = e.getValue().strip();   // no whitespaces around numbers
   if (e.value.blank() || INT_REGEX.test (e.value))
      return true;
   reportMsg (report, stripName (elementName) + " should be an integer");
   return false;
}
function isFloat (form, elementName) {
   var e = $(form[elementName]);
   var serviceName = getServiceName (form);
   var report = $(serviceName + "$" + elementName + "$MESSAGE");
   e.value = e.getValue().strip();   // no whitespaces around numbers
   if (e.value.blank() || FLOAT_REGEX.test (e.value))
      return true;
   reportMsg (report, stripName (elementName) + " should be a floating point number");
   return false;
}
function isInTheRange (form, elementName, min, max) {
   var e = $(form[elementName]);
   var serviceName = getServiceName (form);
   var report = $(serviceName + "$" + elementName + "$MESSAGE");
   e.value = e.getValue().strip();   // no whitespaces around numbers
   if (e.value.blank() || ((min==null || e.value >= min) && (max==null || e.value <= max)))
      return true;
   reportMsg (report, stripName (elementName) + " should be between "
   +min+" and "+ max);
   return false;
}
//function isStringLengthInTheRange (form, elementName, minl, maxl) {
//   var e = $(form[elementName]);
//   var serviceName = getServiceName (form);
//   var report = $(serviceName + "$" + elementName + "$MESSAGE");
//   e.value = e.getValue().strip();   // no whitespaces around numbers
//   if (e.value.blank() || ((minl==null || e.value.length >= minl) && (maxl==null || e.value.length <= maxl)))
//      return true;
//   reportMsg (report, stripName (elementName) + " should be between "
//   +min+" and "+ max);
//   return false;
//}
