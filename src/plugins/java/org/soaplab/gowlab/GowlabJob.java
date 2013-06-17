// GowlabJob.java
//
// Created: April 2007
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

package org.soaplab.gowlab;

import org.soaplab.share.SoaplabException;
import org.soaplab.share.SoaplabConstants;
import org.soaplab.services.cmdline.CmdLineJob;
import org.soaplab.services.cmdline.NamedValues;
import org.soaplab.services.IOData;
import org.soaplab.services.Reporter;
import org.soaplab.services.JobState;
import org.soaplab.services.Config;
import org.soaplab.services.metadata.MetadataAccessor;
import org.soaplab.services.metadata.MetadataUtils;
import org.soaplab.services.metadata.ParamDef;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.httpclient.params.HttpMethodParams;

import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * An implementation of a Job interface where a "Job" means to fetch a
 * web page, and extracting useful data from it. <p>
 *
 * TBD more. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: GowlabJob.java,v 1.8 2007/10/23 22:09:50 marsenger Exp $
 */

public class GowlabJob
    extends CmdLineJob {

    private static org.apache.commons.logging.Log log =
       org.apache.commons.logging.LogFactory.getLog (GowlabJob.class);

    // HTTP method's names (used in metadata, in launcher)
    static final String METHOD_GET  = "get";
    static final String METHOD_POST = "post";
    static final String METHOD_HEAD = "head";

    // control the init() method
    private static boolean jobClassInitialized = false;

    // connection to the web site
    protected HttpMethod httpMethod;

    // inputs (this is filled from global 'inputs')
    protected NameValuePair[] formData;
    protected IOData[] fileData;
    protected String queryString;

    /**************************************************************************
     * The main constructor.
     **************************************************************************/
    public GowlabJob (String jobId,
		      MetadataAccessor metadataAccessor,
		      Reporter reporter,
		      Map<String,Object> sharedAttributes,
		      boolean jobRecreated)
	throws SoaplabException {
	super (jobId, metadataAccessor, reporter,
	       sharedAttributes, jobRecreated);
    }

    /**************************************************************************
     * Nothing done here... (so far)
     **************************************************************************/
    @Override protected synchronized void init()
	throws SoaplabException {
	super.init();

	if (jobClassInitialized)
	    return;
	jobClassInitialized = true;

	// here would come my own initialization (done just once for
	// all Gowlab jobs)

    }

    /**************************************************************************
     * Create an appropriate HTTP method (as defined in service
     * metadata) an return it.
     *
     * A sub-class can overwrite it if it wishes to set some specific
     * HTTP parameters to this method. For example:
     *
     *<pre>
     * HttpMethod method = super.getHttpMethod();
     * method.getParams().setParameter (HttpMethodParams.RETRY_HANDLER, 
     *                                  new DefaultHttpMethodRetryHandler (3, false));
     * return method;
     *</pre>
     *
     **************************************************************************/
    protected HttpMethod getHttpMethod()
	throws SoaplabException {

	// find the URL where to fetch data from
	String supplier =
	    Config.getString (Config.PROP_SUPPLIER,
			      metadataAccessor.getAnalysisDef().get (SoaplabConstants.ANALYSIS_SUPPLIER),
			      getServiceName(),
			      this);

	// which HTTP method to use
	String launcherMethod =
	    MetadataUtils. parseLauncher (metadataAccessor,
					  getServiceName(),
					  this) [0];
	if (StringUtils.isNotEmpty (launcherMethod)) {

	    String l = launcherMethod.toLowerCase();
	    if (l.equals (METHOD_GET)) {
		if (isMultipartUsed()) {
		    internalError ("Launcher requires a GET method. But it is not compatible with uploading data.");
		}
		return new GetMethod (supplier);
	    }
	    if (l.equals (METHOD_POST))
		return new PostMethod (supplier);
	    if (l.equals (METHOD_HEAD)) {
		if (isMultipartUsed()) {
		    internalError ("Launcher requires a HEAD method. But it is not compatible with uploading data.");
		}
		return new HeadMethod (supplier);
	    }

	    internalError ("Unknown launcher method '" + launcherMethod + "'.");
	}
	
	// default behaviour
	HttpMethod method;
	if (isMultipartUsed()) {
	    method = new PostMethod (supplier);
	} else {
	    method = new GetMethod (supplier);
	    method.setFollowRedirects (true);
	}
	return method;
    }

    /**************************************************************************
     * Fill global variables 'formData' (or 'queryString') and
     * 'fileData' from user inputs.
     **************************************************************************/
    protected void convertInputs()
	throws SoaplabException {
	NamedValues[] params = createNamedArgs();

	if ( params != null &&
	     params.length == 1 &&
	     StringUtils.isEmpty (params[0].getId()) ) {
	    queryString =
		StringUtils.join (params[0].getValues().iterator(), " ");

	} else {
	    List<NameValuePair> list = new ArrayList<NameValuePair>();
	    for (NamedValues arg: params) {
		if (StringUtils.isNotEmpty (arg.getTag())) {
		    if (arg.getValues().size() == 0) {
			// no value means a boolean true value
			list.add (new NameValuePair (arg.getTag(), "on"));
		    } else {
			// more values: put them all with the same tag
			for (int i = 0; i < arg.getValues().size(); i++) {
			    list.add (new NameValuePair (arg.getTag(),
							 arg.getValues().get (i)));
			}
		    }
		}
	    }
	    formData = list.toArray (new NameValuePair[] {});
	}

	fileData = createIO();
	inputs.clear();
    }

    /**************************************************************************
     * Return true only if GET method should be used. Decision depends
     * on existence of files to be uploaded, and on the method given
     * in metadata.
     *
     * Calling this method makes sense only during the run() method
     * (when all input data are definitely in place).
     *************************************************************************/
    protected boolean isGetUsed() {
	if (isMultipartUsed())
	    return false;   // definitely not a GET
	if (httpMethod == null)
	    return false;   // it is too early to tell
	return ("GetMethod".equals (httpMethod.getClass().getSimpleName()));
    }

    /**************************************************************************
     * Return true only if POST method should be used. Decision depends
     * on existence of files to be uploaded, and on the method given
     * in metadata.
     *
     * Calling this method makes sense only during the run() method
     * (when all input data are definitely in place).
     *************************************************************************/
    protected boolean isPostUsed() {
	if (isMultipartUsed())
	    return true;   // definitely a POST
	if (httpMethod == null)
	    return false;   // it is too early to tell
	return ("PostMethod".equals (httpMethod.getClass().getSimpleName()));
    }

    /**************************************************************************
     * Return true only if 'multipart/form-data' encoding should be
     * used. Decision depends on existence of input files to be
     * uploaded.
     *
     * Calling this method makes sense only during the run() method
     * (when all input data are definitely in place).
     *************************************************************************/
    protected boolean isMultipartUsed() {
	if (fileData == null)
	    return false;
	for (IOData io: fileData) {
	    if (io.getDefinition().isRegularInput())
		return true;
	}
	return false;
    }

    /**************************************************************************
     * Create (but not start it yet) and return a thread that will do
     * the data fetching. It is rarely overwritten - but still it is
     * possible to create here your own thread. Make sure that your
     * thread also notifies other threads at its end (as it is done
     * here).
     *
     * The subclasses usually just overwrite method 'fetch' (which is
     * called from this thread) in order to do their fetching job.
     **************************************************************************/
    protected Thread getExecuteThread()
	throws SoaplabException {

	Thread executeThread = new Thread() {
		public void run() {
		    try {
			try {
			    fetch();
			    reporter.getState().set (JobState.COMPLETED);
			} catch (SoaplabException e) {
			    error (e.getMessage());
			    reporter.getState().set (JobState.TERMINATED_BY_ERROR);
			}

		    } catch (RuntimeException e) {
			reporter.getState().set (JobState.TERMINATED_BY_ERROR);
			SoaplabException.formatAndLog (e, log);

		    } finally {
			try {
			    reporter.setDetailedStatusResult();
			    reporter.setReportResult();
			} catch (SoaplabException e) {
			    log.error ("Setting special results failed. " + e.getMessage());
			}
			// release connection
			if (httpMethod != null)
			    httpMethod.releaseConnection();

			// inform other threads waiting for the termination that
			// it has been done
			synchronized (reporter) {
			    reporter.notifyAll();
			}
		    }
		}
	    };
	executeThread.setName
	    (executeThread.getName().replace ("Thread", "GowlabFetcher"));
	return executeThread;
    }

    /**************************************************************************
     * Fill 'httpMethod' with the user data (from 'formData',
     * 'queryString' and 'fileData') and execute it. It will do the
     * real data fetching.
     *
     * If the fetching finished successfully the 'httpMethod' has the
     * response.
     *************************************************************************/
    protected void getResponse()
	throws SoaplabException {

	if (isGetUsed()) {
	    
	    // GET method...
	    
	    if (StringUtils.isNotEmpty (queryString)) {
		// ...from a query string
		try {
		    httpMethod.setQueryString
			(URIUtil.encodeQuery (queryString));
		} catch (URIException e) {
		    httpMethod.setQueryString (queryString);
		}
		
	    } else {
		// ...from name-value pairs
		httpMethod.setQueryString (formData);
	    }
	    
	} else if (isPostUsed()) {
	    
	    // POST method...
	    
	    // ...from name-value pairs
	    ((PostMethod)httpMethod).setRequestBody (formData);
	    
	    // ...files to be uploaded
	    if (isMultipartUsed()) {
		httpMethod
		    .getParams()
		    .setBooleanParameter (HttpMethodParams.USE_EXPECT_CONTINUE, true);
		List<Part> parts = new ArrayList<Part>();
		for (IOData io: fileData) {
		    if (! io.getDefinition().isRegularInput())
			continue;
		    File forUpload = io.getData();
		    if (forUpload == null)
			continue;
		    try {
			String tag = io.getDefinition().get (ParamDef.TAG);
			if (StringUtils.isEmpty (tag))
			    tag = io.getDefinition().id;
			parts.add (new FilePart (tag, forUpload));
		    } catch (FileNotFoundException e) {
			internalError ("A file for uploading was not found: " +
				       forUpload.getAbsolutePath());
		    }
		}
		((PostMethod)httpMethod)
		    .setRequestEntity
		    (new MultipartRequestEntity (parts.toArray (new Part[] {}),
						 httpMethod.getParams()));
	    }
	}
   
	// finally, execute the method
	try {
	    // instantiating an HttpClient
	    new HttpClient().executeMethod (httpMethod);

	} catch (HttpException e) {
	    internalError ("Fatal protocol violation: " + e.getMessage());
	} catch (IOException e) {
	    logAndThrow ("Fatal transport error: " + e.getMessage());
	}

    }

    /**************************************************************************
     * Report a list of input data.
     *************************************************************************/
    protected void reportInputs() {
	boolean usingQueryString = StringUtils.isNotEmpty (queryString);
	StringBuilder buf = new StringBuilder (500);
	buf.append ("Inputs (using ");
	buf.append (httpMethod.getName());
	if (isMultipartUsed())
	    buf.append (", using multipart/form-data encoding");
	if (usingQueryString)
	    buf.append (", using Query String");
	buf.append ("):\n");
	if (usingQueryString) {
	    buf.append (queryString);
	    buf.append ("\n");
	} else {
	    for (NameValuePair arg: formData) {
		buf.append (arg.getName());
		buf.append ("=");
		buf.append (arg.getValue());
		buf.append ("\n");
	    }
	}
	for (IOData io: fileData) {
	    if (io.getDefinition().isRegularInput()) {
		buf.append (io.getDefinition().id);
		buf.append (" = ");
		buf.append (io.toString());
		buf.append ("\n");
	    }
	}
	buf.append ("--- end of inputs\n");
	report (new String (buf));
    }

    /**************************************************************************
     * Report what you are going to do.
     **************************************************************************/
    protected void preFetch() {

	report ("Name: " + getServiceName());
	try {
	    report ("Location: " + httpMethod.getURI());
	} catch (Exception e) {
	    log.error ("Possible configuration problem: " + e.toString());
	}
	reportInputs();
    }

    /**************************************************************************
     * Report what was done.
     **************************************************************************/
    protected void postFetch() {

	// report HTTP headers of the response
	StringBuilder buf = new StringBuilder (500);
	buf.append ("HTTP response headers:\n");
	for (Header header: httpMethod.getResponseHeaders()) {
	    buf.append ("\t");
	    buf.append (header.toExternalForm());
	}
	report (buf.toString());
    }

    /**************************************************************************
     * It evaluates the status in 'httpMethod'.
     *
     * If the status indicates that everything went fine, it passes
     * the response to the 'sendResult' method, and set the new job
     * status as successfully completed.
     *
     * Otherwise it reports error to the reporter and changes job status
     * reflecting the error.
     **************************************************************************/
    protected void processResponse()
	throws SoaplabException {

	if (httpMethod.getStatusCode() >= HttpStatus.SC_MOVED_PERMANENTLY) {

	    // something wrong
	    error (httpMethod.getStatusLine().toString());
	    reporter.getState().set (JobState.TERMINATED_BY_ERROR,
				     "" + httpMethod.getStatusCode());
	    reporter.getState().setDescription (httpMethod.getStatusText());

	} else {
	    // okay
	    sendResults();
	    reporter.getState().set (JobState.COMPLETED,
				     "" + HttpStatus.SC_OK);
	}
    }

    /**************************************************************************
     * Send result, or results, given in 'httpMethod' (that was just
     * executed) to the reporter.
     *
     * It is separated here so sub-classes can make adaption of
     * results (a typical example would be to apply an XSLT on it).
     **************************************************************************/
    protected void sendResults()
	throws SoaplabException {

	// find a result name (as defined in service metadata): this
	// implementation takes the first found result (which should
	// be the only one, anyway
	String resultName = null;
	for (IOData io: fileData) {
	    if (io.getDefinition().isRegularOutput()) {
		resultName = io.getDefinition().id;
		break;
	    }
	}
	if (resultName == null)
	    internalError ("No result defined!");

	try {
	    // read the response body
	    InputStream is = httpMethod.getResponseBodyAsStream();
	    if (is != null)
		reporter.setResult (resultName, is);
	} catch (IOException e) {
	    logAndThrow (e.toString());
	}
    }

    /**************************************************************************
     *
     **************************************************************************/
    protected void fetch()
	throws SoaplabException {

	// report what is going to be done
	preFetch();

	// put data into method; execute the method
	getResponse();

	// evaluate the reponse; store results; set job status
	processResponse();

	// report what was done
	postFetch();
    }

    /**************************************************************************
     *
     **************************************************************************/
    public void run()
	throws SoaplabException {
	if (! runAllowed())
	    return;

	checkInputs();
	if (log.isDebugEnabled())
	    log.debug ("Inputs check passed (" + inputs.size() + " inputs).");
 	convertInputs();
	reporter.getState().set (JobState.RUNNING);

	// where to go, open a connection
	httpMethod = getHttpMethod();

	// create and execute a thread thyat fetches data
	Thread executeThread = getExecuteThread();
	executeThread.start();
    }

}
