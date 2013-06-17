// ServiceController.java
//
// Created: November 2007
//
// Copyright 2007 Martin Senger
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package org.soaplab.clients.spinet;

import org.soaplab.share.SoaplabException;
import org.soaplab.share.SoaplabConstants;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.HeadMethod;

import javax.servlet.http.HttpServletRequest;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.net.URLConnection;

/**
 * This class is a controller (in the MVC pattern sense; at least
 * probably) sitting between JSP components and several Soaplab
 * classes doing the real job, mainly the {@link
 * org.soaplab.clients.spinet.Service Service} class. All its public
 * methods are beans-like (but not all of them have both setter and
 * getter). <p>
 *
 * It expects requests coming in the multipart-form encoding (becase
 * some of the incoming data may be from the uploaded client
 * files). <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: ServiceController.java,v 1.16 2011/04/06 13:00:34 mahmutuludag Exp $
 */

public class ServiceController {

    private static org.apache.commons.logging.Log log =
	org.apache.commons.logging.LogFactory.getLog (ServiceController.class);

    private static final String RESULT_HREF = "href";
    private static final String RESULT_ISARRAY = "isarray";
    private static final String RESULT_DISPLAY_NAME = "dname";

    private String serviceName;
    private String jobElementId;
    private String jobId;
    private String resultName;
    private HttpServletRequest request;

    /*************************************************************************
     *
     *************************************************************************/
    public String getServiceName() {
	return serviceName;
    }

    /*************************************************************************
     *
     *************************************************************************/
    public void setServiceName (String serviceName) {
	this.serviceName = serviceName;
    }

    /*************************************************************************
     *
     *************************************************************************/
    public String getCleanServiceName() {
	if (getServiceName() == null)
	    return "";
	return getServiceName().replaceAll ("[^a-z0-9]", "X");
    }

    /*************************************************************************
     *
     *************************************************************************/
    public String getJobElementId() {
	return jobElementId;
    }

    /*************************************************************************
     *
     *************************************************************************/
    public void setJobElementId (String jobElementId) {
	this.jobElementId = jobElementId;
    }

    /*************************************************************************
     *
     *************************************************************************/
    public String getJobId() {
	return jobId;
    }

    /*************************************************************************
     *
     *************************************************************************/
    public void setJobId (String jobId) {
	this.jobId = jobId;
    }

    /*************************************************************************
     *
     *************************************************************************/
    public void setRequest (HttpServletRequest request) {
	this.request = request;
    }

    /*************************************************************************
     *
     *************************************************************************/
    public String getResultName() {
	return resultName;
    }

    /*************************************************************************
     *
     *************************************************************************/
    public void setResultName (String resultName) {
	this.resultName = resultName;
    }

    /*************************************************************************
     *
     *************************************************************************/
    @SuppressWarnings("unchecked")
    private Map<String,Object> retrieveParameters()
	throws SoaplabException {

	if (request == null) {
	    String msg = "Request is null. Strange... Probably an internal error.";
	    log.error (msg);
	    throw new SoaplabException (msg);
	}

	Map<String,Object> params = new HashMap<String,Object>();

	// check that we have a file upload request
	boolean isMultipart = ServletFileUpload.isMultipartContent (request);
	if (isMultipart) {
	    try {
		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload (factory);
		List<FileItem> items = upload.parseRequest (request);
		for (FileItem item: items) {
		    String name = item.getFieldName();
 		    // String value = item.getString ("UTF-8");
 		    // params.put (name, value);
		    if (item.isFormField()) {
			params.put (name, item.getString ("UTF-8"));
		    } else {
			byte[] value = item.get();
			if (value != null && value.length > 0) {
			    params.put (name, value);
			}
		    }
    		}
	    } catch (Exception e) {
		throw new SoaplabException (e.getMessage(), e);
	    }

	} else {
	    for (Enumeration en = request.getParameterNames(); en.hasMoreElements() ;) {
		String name = (String)en.nextElement();
		String value = request.getParameter (name);
		params.put (name, value);
	    }
	}

	// some request params are not inputs
 	setServiceName ((String)params.get ("serviceName"));
	setJobElementId ((String)params.get ("jobElementId"));
	return params;
    }

    /*************************************************************************
     * Retrieve and validate request input data. Make a Map from them.
     *************************************************************************/
    private Map<String,Object> extractInputs (Map<String,Object> params) {

	Map<String,Object> result = new HashMap<String,Object>();
	for (Iterator<String> it = params.keySet().iterator(); it.hasNext(); ) {
	    String name = it.next();

	    if (name.endsWith (Service.S_INPUT)) {
		// this is a regular, no "real-data"-based, input
		String strippedName = StringUtils.removeEnd (name, Service.S_INPUT);
		String value = params.get (name).toString();
		if (! Service.DEFAULT_SELECT_TEXT.equals (value)) {
		    addIfSuitable (params, result, strippedName, value);
		}

	    } else if (name.endsWith (Service.S_INPUTDATA)) {
		// this is a "real-data"-based input; will be taken by its toggle
		continue;

	    } else if (name.endsWith (Service.S_UPLOAD)) {
		// this is a file-upload input; will be taken by its toggle
		continue;

	    } else if (name.endsWith (Service.S_INPUTDATAALONE)) {
		// this is a standalone "real-data" input (no similar ref data exists)
		String strippedName = StringUtils.removeEnd (name, Service.S_INPUTDATAALONE);
		String value = null;
		if (params.containsKey (strippedName + Service.S_UPLOAD)) {
		    value = params.get (strippedName + Service.S_UPLOAD).toString();
		}
		boolean added = addIfSuitable (params, result, strippedName, value);
		if (! added) {
		    addIfSuitable (params, result, strippedName, params.get (name).toString());
		}

	    } else if (name.endsWith (Service.S_TOGGLE)) {
		// this is a toggle for a "real-data"-based input
		String referringToName =
		    StringUtils.removeEnd (name, Service.S_TOGGLE) +
		    params.get (name).toString();  // e.g. input_url
		String value = null;
		if (params.containsKey (referringToName + Service.S_UPLOAD)) {
		    Object rawValue = params.get (referringToName + Service.S_UPLOAD);
		    if (rawValue instanceof byte[]) {
			// this is the usual case (at least from the usual spinet UI)
			result.put (referringToName, rawValue);
			continue;
		    } else {
			value = rawValue.toString();
		    }
		}
		boolean added = addIfSuitable (params, result, referringToName, value);
		if (! added) {
		    value = params.get (referringToName + Service.S_INPUTDATA).toString();
		    addIfSuitable (params, result, referringToName, value);
		}
	    }
	}
	return result;
    }

//     /*************************************************************************
//      *
//      *************************************************************************/
//     private List<String> extractOutputs (Map<String,Object> params) {

// 	List<String> result = new ArrayList<String>();
// 	for (Iterator<String> it = params.keySet().iterator(); it.hasNext(); ) {
// 	    String name = it.next();
// 	    if (name.endsWith (Service.S_OUTPUT)) {
// 		String strippedName = StringUtils.removeEnd (name, Service.S_OUTPUT);
// 		String value = (String)params.get (name);
// 		if (StringUtils.isNotBlank (value)) {
// 		    result.add (strippedName);
// 		}
// 	    }
// 	}
// 	return result;
//     }

    /*************************************************************************
     * Add a pair 'name' - 'value' into 'results under certain
     * circumstances:
     *
     * - It is not added if 'value' is empty. In this case it returns false.
     *
     * - It is also not added if 'value' is equal to its default
     * value (which can be found in 'inputs' under a name derived from
     * 'name'). But now it returns true.
     *************************************************************************/
    private boolean addIfSuitable (Map<String,Object> inputs,
				   Map<String,Object> result,
				   String name,
				   String value) {
	if (StringUtils.isNotBlank (value)) {
	    if (! value.toString().equals (inputs.get (name + Service.S_DEFAULTVALUE))) {
		result.put (name, value);
	    }
	    return true;
	}
	return false;
    }

    /*************************************************************************
     * Show all received request parameters, including the uploaded
     * files. <p>
     *
     * This is more or less for debugging only.
     *************************************************************************/
    public String getShow()
	throws SoaplabException {
	StringBuilder buf = new StringBuilder();

	buf.append ("<h3>Raw inputs</h3>");
	Map<String,Object> rawInputs = retrieveParameters();
	buf.append (showMap (rawInputs));

	buf.append ("<h3>Extracted inputs</h3>");
	Map<String,Object> inputs = extractInputs (rawInputs);
	buf.append (showMap (inputs));

	return jsEscape (buf.toString());
    }

    // tmp
    private String showMap (Map<String,Object> map) {
	StringBuilder buf = new StringBuilder();
	buf.append ("<pre>");
	for (Iterator<String> it = map.keySet().iterator(); it.hasNext(); ) {
	    String name = it.next();
	    String value = map.get (name).toString();
	    buf.append ("<b>" + name + "</b>");
	    buf.append ("=");
	    buf.append (value);
	    buf.append ("\n");
	}
	buf.append ("</pre>");
	return buf.toString();
    }

    /*************************************************************************
     * Escape and return given 'str' in order to be as a single-quoted
     * constant in the JavaScript expression. The 'str' is expected to
     * be an HTML.
     *************************************************************************/
    protected static String jsEscape (String str) {
	return str.replaceAll ("\n", "<br>").replaceAll ("'", "\\\\'");
    }

    /*************************************************************************
     *
     *************************************************************************/
    public Map<String,String> getPanelMap()
	throws SoaplabException {
	if (getServiceName() == null)
	    throw new SoaplabException
		("No service name given (definitely a bug). Sorry, I cannot do much without it...");

	return new Service (getServiceName()).getPanelMap();
    }

    /*************************************************************************
     * It reads input data from the request, converts them into a form
     * acceptable by Analysis interface, and calls
     * Service.createAndRun(). <p>
     *
     * It returns what it gets back from the Service - which is an
     * HTML for controlling further actions with the submitted
     * job. <p>
     *
     * If something goes wrong, the SoaplabException is thrown. In
     * which case, the page 'error.jsp' is called to deliver the error
     * message.
     *************************************************************************/
    public String getRun()
	throws SoaplabException {
	Map<String,Object> params = retrieveParameters();
	// for (Iterator<String> it = params.keySet().iterator(); it.hasNext(); ) {
	//     String name = it.next();
	//     log.info ("getRun1: " + name + " => " + params.get (name));
	// }
	Map<String,Object> inputs = extractInputs (params);
	// for (Iterator<String> it = inputs.keySet().iterator(); it.hasNext(); ) {
	//     String name = it.next();
	//     log.info ("getRun2: " + name + " => " + inputs.get (name));
	// }
 	Service service = new Service (getServiceName());
 	
 	  // Client IP address...
    String remoteIP = null;
    if(request.getRemoteAddr() != null)
        remoteIP = request.getRemoteAddr();
    // Handle proxy or load balancer client IP.
    if(request.getHeader("X-Cluster-Client-Ip") != null) {
        remoteIP =  request.getHeader("X-Cluster-Client-Ip");
    }
    // Default value in case of problems
    if(remoteIP == null)
        remoteIP = "unknown";
    
    inputs.put(SoaplabConstants.REMOTE_IP, remoteIP);
    inputs.put(SoaplabConstants.INTERFACE, "spinet");
 	
 	return service.createAndRun (inputs);
    }

    /*************************************************************************
     *
     *************************************************************************/
    public String getStatus()
	throws SoaplabException {
 	Service service = new Service (getServiceName());
 	return service.getStatus (getJobId());
    }

    /*************************************************************************
     *
     *************************************************************************/
    public List<ResultInfo> getResultsInfoList()
	throws SoaplabException {

	List<ResultInfo> result = new ArrayList<ResultInfo>();

 	Service service = new Service (getServiceName());
	Map<String,String>[] infos = service.getResultsInfo (getJobId());

	// having a _url result is enough: put it on the place of its
	// originator (a non-_url result)
	for (int i = 0; i < infos.length; i++) {
	    String name = infos[i].get (SoaplabConstants.RESULT_NAME);
	    if (name.endsWith (SoaplabConstants.URL_RESULT_SUFFIX)) {
		String strippedName =
		    StringUtils.removeEnd (name, SoaplabConstants.URL_RESULT_SUFFIX);
		int partnerIdx = -1;
		for (int j = 0; j < infos.length; j++) {
		    if (infos[j] == null)
			continue;
		    if (strippedName.equals (infos[j].get (SoaplabConstants.RESULT_NAME))) {
			partnerIdx = j;
			break;
		    }
		}
		if (partnerIdx < 0)
		    continue;
		// we found a "partner" result (the one not ending with '_url');
		try {
		    // fetch the contents of the '_url' result
		    Object obj = service.getResult (getJobId(), name);
		    if (obj == null)
			continue;   // something wrong; keep all results
		    if (obj.getClass().isArray()) {
			String[] urls = (String[])obj;
			if (urls.length > 0 && urlExists (urls[0])) {
			    infos [partnerIdx].put (RESULT_ISARRAY, "true");
			    infos [partnerIdx].put (RESULT_DISPLAY_NAME,
						    infos [partnerIdx].get (SoaplabConstants.RESULT_NAME));
			    infos [partnerIdx].put (SoaplabConstants.RESULT_NAME, name);
			}
		    } else {
			if (urlExists (obj.toString())) {
			    infos [partnerIdx].put (RESULT_HREF, obj.toString());
			}
		    }
		} catch (SoaplabException e2) {
		}
		// remove the url result from the list of results
		infos[i] = null;
	    }
	}

	int fromTheEndIdx = 0;
	for (int i = 0; i < infos.length; i++) {
	    if (infos[i] == null)
		continue;
	    ResultInfo info = new ResultInfo();
	    info.setName (infos[i].get (SoaplabConstants.RESULT_NAME));
	    info.setDisplayName (infos[i].get (RESULT_DISPLAY_NAME));
	    info.setType (infos[i].get (SoaplabConstants.RESULT_TYPE));
	    info.setSize (infos[i].get (SoaplabConstants.RESULT_SIZE));
	    info.setLength (infos[i].get (SoaplabConstants.RESULT_LENGTH));
	    info.setHref (infos[i].get (RESULT_HREF));
	    info.setIsArray (new Boolean (infos[i].get (RESULT_ISARRAY)).booleanValue());

	    // kind of sorting: detailed_status always at the end, report
	    // on the last but one
	    if (SoaplabConstants.RESULT_DETAILED_STATUS.equals (info.getName())) {
		fromTheEndIdx++;
		result.add (info);
	    } else if (SoaplabConstants.RESULT_REPORT.equals (info.getName())) {
		result.add (result.size() - fromTheEndIdx, info);
		fromTheEndIdx++;
	    } else {
		result.add (result.size() - fromTheEndIdx, info);
	    }
	}

	return result;
    }

    /*************************************************************************
     *
     *************************************************************************/
    protected boolean urlExists (String url) {

	HttpMethod method = null;
	try {
	    // instantiating an HttpClient and its HEAD method
	    HttpClient httpClient = new HttpClient();
	    URI uri = new URI (url, false);
	    method = new HeadMethod (uri.toString());
	    method.setFollowRedirects (true);

	    // execute the method
	    int statusCode = httpClient.executeMethod (method);
	    return (statusCode == HttpStatus.SC_OK);

	} catch (Throwable e) {
	    SoaplabException.formatAndLog (e, log);
	    return false;

	} finally {
	    // release the connection
	    if (method != null)
		method.releaseConnection();
	}  
    }

    /*************************************************************************
     *
     *************************************************************************/
    public Object getResult()
	throws SoaplabException {
 	Service service = new Service (getServiceName());
	Object result = service.getResult (getJobId(), getResultName());
	if (result == null)
	    throw new SoaplabException ("Result '" + getResultName() + "' was not found.");
	return result;
    }

    /*************************************************************************
     * Used for array results only...
     *************************************************************************/
    public Object[] getResultArray()
	throws SoaplabException {
	return (Object[])getResult();
    }

    /*************************************************************************
     * Get metadata about one result. Return an empty map if the
     * result name does not exist.
     *************************************************************************/
    public Map<String,Object> getResultSpec()
	throws SoaplabException {
 	Service service = new Service (getServiceName());
	return service.getResultSpec (getResultName());
    }

    /*************************************************************************
     * Get a suggested file extension and content (MIME) type for the
     * given result. Return a reasonable default if the real ones are
     * not specified.
     *************************************************************************/
    public ResultInfo getResultTypes()
	throws SoaplabException {
	Map<String,Object> spec = getResultSpec();

	ResultInfo info = new ResultInfo();
	info.setContentType ((String)spec.get (SoaplabConstants.MIME_TYPE));
	String extension = (String)spec.get (SoaplabConstants.FILE_EXT);
	if (StringUtils.isNotBlank (extension)) {
	    if (! extension.startsWith ("."))
		extension = "." + extension;
	    info.setFileExtension (extension);
	    if (StringUtils.isBlank (info.getContentType())) {
		info.setContentType (URLConnection
				     .getFileNameMap()
				     .getContentTypeFor ("x" + extension));
	    }
	}

	// default values
	if (StringUtils.isBlank (info.getContentType())) {
	    String type = (String)spec.get (SoaplabConstants.RESULT_TYPE);
	    if (SoaplabConstants.TYPE_BINARY_SIMPLE.equals (type) ||
		SoaplabConstants.TYPE_BINARY_ARRAY.equals (type)) {
		info.setContentType ("application/octet-stream");
	    } else {
		info.setContentType ("text/plain");
	    }
	}
	if (StringUtils.isBlank (info.getFileExtension()))
	    info.setFileExtension ("");

	if (log.isDebugEnabled()) {
	    log.debug (getServiceName() +
		       " RES: "  + getResultName() +
		       ", CT: "  + info.getContentType() +
		       ", EXT: " + info.getFileExtension());
	}
	return info;
    }

    /*************************************************************************
     *
     *************************************************************************/
    public String getKill()
	throws SoaplabException {
 	Service service = new Service (getServiceName());
	service.terminate (getJobId());
	return "";
    }

    /*************************************************************************
     *
     *************************************************************************/
    public String getDestroy() {
	try {
	    Service service = new Service (getServiceName());
	    service.destroy (getJobId());
	} catch (SoaplabException e) {
	}
	return "";
    }

}
