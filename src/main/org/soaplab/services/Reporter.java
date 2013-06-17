// Reporter.java
//
// Created: October 2006
//
// Copyright 2006 Martin Senger
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

package org.soaplab.services;

import org.soaplab.services.storage.PersistentStorage;
import org.soaplab.services.events.EventManager;
import org.soaplab.services.events.AnalysisEvent;
import org.soaplab.services.metadata.MetadataAccessor;
import org.soaplab.services.metadata.MetadataUtils;
import org.soaplab.services.metadata.ParamDef;
import org.soaplab.services.metadata.IOParamDef;
import org.soaplab.services.adaptor.DataAdaptor;

import org.soaplab.share.SoaplabException;
import org.soaplab.share.SoaplabConstants;
import org.soaplab.tools.ICreator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.commons.lang.time.DateFormatUtils;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Collections;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * An instance of a Reporter accompanies each "job" (an analysis
 * invocation). It keeps the job status, it adapts job results (and
 * passes them to a persistent manager to store them), etc. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: Reporter.java,v 1.31 2011/05/13 10:14:45 marsenger Exp $
 */

public class Reporter {

    private static org.apache.commons.logging.Log log =
       org.apache.commons.logging.LogFactory.getLog (Reporter.class);

    // what job to report on...
    protected Job job;

    // ...and few other things given in the constructor
    protected PersistentStorage percy;
    protected EventManager eman;
    protected MetadataAccessor metadataAccessor;

    // keep here state of my job/analysis
    protected JobState state;

    // reports (for the 'report' result)
    protected StringBuffer msgBuf = new StringBuffer();   // for the main report
    protected StringBuffer errBuf = new StringBuffer();   // for the errors
    protected Properties reportProps = new Properties();  // for other reporting
    protected Hashtable<String,File> fileProps = new Hashtable<String,File>();    // for reports in files

    protected static final String RESULTS_URL_FIX_NAME = "results";

    /******************************************************************************
     *
     ******************************************************************************/
    protected Reporter() {
    }

    /******************************************************************************
     * The main constructor. <p>
     *
     * @param metadataAccessor gives access to all service metadata;
     * for example, it is used to find names of result adapters; in
     * rare cases it can be null (when this reporter reports on a
     * semi-functional job that was retrieved from the persistent
     * storage, for example)
     *
     * @param percy manages all persistent storage handlers; used to
     * store results; it cannot be null
     *
     * @param eman manages broadcasting events (about the status of
     * the job) to all event listeners; it cannot be null
     ******************************************************************************/
    public Reporter (MetadataAccessor metadataAccessor,
		     PersistentStorage percy,
		     EventManager eman) {
	this.metadataAccessor = metadataAccessor;
	this.percy = percy;
	this.eman = eman;

	// create an empty job state
	state = new JobState (JobState.UNKNOWN, percy, eman);
    }

    /******************************************************************************
     * Remember what job this reporter is serving. Should be call just
     * once, and usually from a constructor of the Job. <p>
     *
     * Note that this method is here only because we have a
     * chicken-egg problem: a job needs to know its reporter, and the
     * reporter needs to know its job. Is there perhaps any
     * established pattern to deal with such situations better?
     ******************************************************************************/
    public void setJob (Job job) {
 	this.job = job;
	state.setJob (job);
    }


    // -----------------------
    //
    // Dealing with job status
    //
    // -----------------------


    /******************************************************************************
     * Set the whole job state. This is rarely used - because the
     * reporter always creates a job state in its constructor,
     * anyway. The method is here for specialized implementation of a
     * Job that wishes to use its own state (a subclass of a regular
     * JobState). It is also used when a semi-functional job (a job
     * that was re-created from a persistent storage) is created long
     * after it had finished. <p>
     *
     * Usually, for changing status of a job, use rather:
     *<pre>
     *   reporter.getState().set (...);
     *</pre>.
     *
     ******************************************************************************/
    public void setState (JobState state) {
	this.state = state;
    }

    /******************************************************************************
     * Return a container with job status.
     ******************************************************************************/
    public JobState getState() {
	return state;
    }


    // -------------------
    //
    // Dealing with events
    //
    // -------------------


    /**************************************************************************
     * Send an event to all event listeners.
     **************************************************************************/
    public void sendEvent (AnalysisEvent event) {
	state.setLastEvent (event);
	eman.handleEvent (event);
    }

    // ----------------------------------------------------------------
    //
    // Dealing with specialized job results (report and detailed_status
    //
    // ----------------------------------------------------------------

    /**************************************************************************
     * Update my report in the persistent storage. Ignore
     * errors. <p>
     *
     * TBD: Don't do it too often. Wait for a while because more
     * report properties may be on their way.
     **************************************************************************/
    protected void updateReportResult() {
	try {
	    setReportResult();
	} catch (Throwable e) {
	    log.error ("Cannot update report result: " + e.getMessage());
	}
    }

    /******************************************************************************
     * Clean-up all so far reported messages. <p>
     *
     * I wonder if we really need this method... (because the report
     * will go away when its Reporter instance is garbage collected,
     * anyway).
     ******************************************************************************/
    public void cleanReport() {
	msgBuf = new StringBuffer();
	errBuf = new StringBuffer();
	reportProps.clear();
	fileProps.clear();
    }

    /******************************************************************************
     * Add a message to the 'report' result. No newlines are
     * automatically added.
     ******************************************************************************/
    public void report (String msg) {
	msgBuf.append (msg);
	updateReportResult();
    }

    /******************************************************************************
     * Add an error message to the 'report' result. No newlines are
     * automatically added. <p>
     *
     * Adding an error also influences what the 'summary report' will contain.
     ******************************************************************************/
    public void error (String msg) {
	errBuf.append (msg);
	updateReportResult();
    }

    /******************************************************************************
     * Add a property to the 'report' result. It will be listed in the
     * 'report' in the same order as added by this method. An already
     * existing property will be extended by the new 'msg'. <p>
     *
     * @param msgName names the property (will be used as a label in the report)
     * @param msg message to be reported
     *************************************************************************/
    public void report (String msgName, String msg) {
	String oldValue = reportProps.getProperty (msgName);
	if (oldValue == null)
	    reportProps.put (msgName, msg);
	else if (oldValue.endsWith ("\n"))
	    reportProps.put (msgName, oldValue + msg);
	else
	    reportProps.put (msgName, oldValue + "\n" + msg);
	updateReportResult();
    }

    /******************************************************************************
     * Add a contents of the given file to the 'report' result. It
     * will be listed at the end the 'report' in the same order as
     * added by this method, under the given label. <p>
     *
     * @param msgName names the property (will be used as a label in
     * the report, unless it is empty)
     *
     * @param msgFile file containing reported message (TBD: when is
     * this file read?)
     ******************************************************************************/
    public void report (String msgName, File msgFile) {
	fileProps.put (msgName, msgFile);
	updateReportResult();
    }

    /******************************************************************************
     * Create a 'report' result from data reported so far, and from
     * the current job status. <p>
     *
     * Note that a report can be created more than once, potentially
     * with richer property set each time. <p>
     *
     * TBD: how to deal with BIG reports - with memory problem...
     ******************************************************************************/
    protected String createReportResult() {

	// 1) create "termination status"
	StringBuilder buf = new StringBuilder();
	buf.append ("Summary:\n\tCompleted: ");
	if (state.isSuccessful())
	    if (errBuf.length() > 0)   buf.append ("Maybe\n");
	    else                       buf.append ("Successfully\n");
	else if (state.isErroneous())  buf.append ("Erroneously\n");
	else if (state.isInProgress()) buf.append ("...in progress\n");
	else                           buf.append ("Unknown\n");
	String ts = state.formatDetailedState();
	if (ts != null)
	    buf.append ("\tTermination status: " + ts + "\n");
	long started = state.getStarted();
	if (started > 0) {
	    buf.append ("\tStarted:   ");
	    buf.append (DateFormatUtils.format (started, SoaplabConstants.DT_FORMAT));
	    buf.append ("\n");
	}
	long ended = state.getEnded();
	if (ended > 0) {
	    buf.append ("\tEnded:     ");
	    buf.append (DateFormatUtils.format (ended, SoaplabConstants.DT_FORMAT));
	    buf.append ("\n");
	}
	long elapsed = state.getElapsed();
	if (elapsed > 0) {
	    buf.append ("\tDuration:  ");
	    buf.append (DurationFormatUtils.formatDurationHMS (elapsed));
	    buf.append ("\n");
	}
	
	// 2) create report from report properties
	if (reportProps.size() > 0 || errBuf.length() > 0 || msgBuf.length() > 0)
	    buf.append ("Report:\n");
	buf.append (new String (reportProperties()));

	// 3) add the main and error messages
	if (errBuf.length() > 0) {
	    buf.append (errBuf);  buf.append ("\n");
	}
	if (msgBuf.length() > 0) {
	    buf.append (msgBuf);  buf.append ("\n");
	}
	
	// 4) add contents of registered files
	for (Enumeration<String> en = fileProps.keys(); en.hasMoreElements(); ) {
	    String key = en.nextElement();
	    File file = fileProps.get (key);
	    buf.append (key);            buf.append (":\n");
	    buf.append (tryFile (file)); buf.append ("\n");
	}
	
	return buf.toString();
    }

    @SuppressWarnings("unchecked")
    protected Iterator sortKeys (Set keys) {
	Vector<String> sorted = new Vector<String> (keys);
	Collections.sort (sorted);
	return sorted.iterator();
    }

    /******************************************************************************
     * Create and return part of report from the report properties.
     ******************************************************************************/
    protected StringBuffer reportProperties() {
	StringBuffer buf = new StringBuffer ();
	for (Iterator it = sortKeys (reportProps.keySet()); it.hasNext(); ) {
	    String key = (String)it.next();
	    
	    buf.append ("\t");
	    buf.append (key);
	    if (! key.trim().endsWith (":"))
		buf.append (": ");
	    buf.append (reportProps.getProperty (key));
	    buf.append ("\n");
	}
	if (reportProps.size() > 0)
	    buf.append ("\n");

	return buf;
    }


    /******************************************************************************
     * If 'file' represents an existing file, return its contents,
     * otherwise return an empty string. In case of IO error, return
     * the error itself.
     ******************************************************************************/
    protected String tryFile (File file) {
	try {
	    if (file.exists())
		return FileUtils.readFileToString (file,
						   System.getProperty ("file.encoding"));
	    else
		return "";
	} catch (Exception e) {
	    return "Error by reading '" + file + "': " + e.toString();
	}
    }

    /******************************************************************************
     * Store 'report' result persistently. But keep it still in the
     * memory so it can be updated.
     ******************************************************************************/
    public void setReportResult()
	throws SoaplabException {
	percy.setResult (job, SoaplabConstants.RESULT_REPORT,
			 createReportResult());
    }


    /**************************************************************************
     * Create and return a special result containing detailed status
     * of this job.
     *************************************************************************/
    protected String createDetailedStatusResult() {
	return (state.getDetailed() == null ? "0" : state.getDetailed());
    }

    /******************************************************************************
     * Store 'detailed_status' result persistently.
     ******************************************************************************/
    public void setDetailedStatusResult()
	throws SoaplabException {
	percy.setResult (job,
			 SoaplabConstants.RESULT_DETAILED_STATUS,
			 createDetailedStatusResult());
    }


    // ---------------------------------
    //
    // Dealing with ordinary job results
    //
    // ---------------------------------


    /******************************************************************************
     * It stores given result under given name. For specialized output
     * (report and detailed status), use specialized methods. <p>
     *
     * Before storing the result, it looks if there is an adaptor to
     * convert the result. If so, it is called and the adapted result
     * is put into a local temporary file. If there is no adaptor,
     * it processes the result in the following way: <ul>
     *
     * <li> If it is an input stream, it reads it and puts it into a
     * local temporary file,
     *
     * <li> If it is already a file, it makes its copy into a local
     * temporary file,
     *
     * <li> If it is a URL, it fetches its contents into a local file,
     *
     * <li> If it is a string, it keeps it as it is.
     * </ul> <p>
     *
     * The result is now either in a local file, or in memory. In any
     * case, it is now passed to the persistence manager (which passes
     * it further to all storage handlers). <p>
     *
     * TBD: A, yet not fully solved, question is what to do with these
     * local temporary files? When to get rid of them? Note that we
     * cannot remove files immediately, because the persistence
     * manager call may return before the files are fully
     * consumed). So, definitely, make them disappear when this
     * application exits. But it can be alive for a long time.  Other
     * potential options are: (a) start a thread removing too old
     * temporary files, or (b) change the persistent storage API so it
     * can call me back when files are consumed, or (c) keep them and
     * let some external, administrative tool to get rid of them time
     * to time. <p>
     *
     * <u>Result as a URL</u>
     *
     * Then, it checks if this output can be also accessible as a
     * URL. If it is the case - and if it can find enough properties
     * to do so, it creates another output (derived from the
     * 'resultName' by adding a "_url" suffix).
     *
     ******************************************************************************/
    public void setResult (String resultName, Object result)
	throws SoaplabException {

	// do I have an output adaptor for this result?
	DataAdaptor adaptor = loadOutputAdaptor (resultName);
	OutputStream os = null;

	try {
	    Class resultClass = result.getClass();
	    if (resultClass.isArray() &&
		! resultClass.getComponentType().equals (byte.class)) {

		// result is an array (but not just array of bytes)
		if (adaptor == null) {
		    processArray (resultName, (Object[])result);
		} else {
		    Object adaptedResult =
			adaptor.processFromList (resultName,
						 Arrays.asList ((Object[])result),
						 job);
		    Class adaptedClass = adaptedResult.getClass();
		    if (adaptedClass.isArray() &&
			! adaptedClass.getComponentType().equals (byte.class)) {
			processArray (resultName, (Object[])adaptedResult);
		    } else {
			Object convertedResult = convert2percy (adaptedResult);
			percy.setResult (job, resultName, convertedResult);
			setURLResult (resultName, 
				      createURLResult (resultName, convertedResult, ""));
		    }
		}

	    } else {
		
		// result is not an array (or just an array of bytes)
		if (adaptor == null) {
		    Object convertedResult = convert2percy (result);
		    percy.setResult (job, resultName, convertedResult);
		    setURLResult (resultName, 
				  createURLResult (resultName, convertedResult, ""));
		} else {
		    // adapt data into a temporary file...
		    File resultFile = IOData.createTempFile(job.getJobDir());
		    os = new BufferedOutputStream
			(new FileOutputStream (resultFile));
		    adaptor.process (resultName, result2stream (result), os, job);
		    os.close();
		    percy.setResult (job, resultName, resultFile);
		    setURLResult (resultName, 
				  createURLResult (resultName, resultFile, ""));
		}
	    }

 	} catch (Throwable e) {     // be prepare for "out-of-memory" error
 	    throw new SoaplabException (e.getMessage(), e);

	} finally {
	    IOUtils.closeQuietly (os);
	}
    }

    /**************************************************************************
     *
     *************************************************************************/
    private void processArray (String resultName, Object[] elements)
	throws SoaplabException, IOException {
	Object[] results = new Object [elements.length];
	String[] urls = new String [elements.length];
	for (int i = 0; i < elements.length; i++) {
	    results[i] = convert2percy (elements[i]);
	    urls[i] = createURLResult (resultName,
				       results[i],
				       String.format (".%1$03d", i+1));
	}
	percy.setResult (job, resultName, results);
	setURLResult (resultName, urls);
    }

    /**************************************************************************
     *
     *************************************************************************/
    protected void setURLResult (String resultName, String[] urls)
	throws SoaplabException {
	if (urls.length > 0 && urls[0] != null) {
	    percy.setResult (job, resultName + SoaplabConstants.URL_RESULT_SUFFIX, urls);
	}
    }

    /**************************************************************************
     *
     *************************************************************************/
    protected void setURLResult (String resultName, String url)
	throws SoaplabException {
	if (StringUtils.isNotEmpty (url)) {
	    percy.setResult (job, resultName + SoaplabConstants.URL_RESULT_SUFFIX, url);
	}
    }

    /**************************************************************************
     * Copy (if wanted) 'result' (which represents an output named
     * 'resultName') to a location that can be visited by HTTP
     * etc. Return the URL of such location, or null. <p>
     *
     * 'result' can be of type File, String or byte[].
     *************************************************************************/
    protected String createURLResult (String resultName,
				      Object result,
				      String filenameCount)
	throws SoaplabException {

	String serviceName = metadataAccessor.getServiceName();

	// URL results may be banned
	if (Config.isEnabled (Config.PROP_RESULTS_URL_IGNORE,
			      false, // default: do not ignore it
			      serviceName, job))
	    return null;

	// is there a need for a URL result for this particular output?
	ParamDef paramDef = findParamDef (resultName);
	if (paramDef == null ||
	    ! paramDef.get (IOParamDef.SPECIAL_TYPE).equals ("url"))
	    return null;

	// where to create it?
	String targetDir = Config.getString (Config.PROP_RESULTS_URL_DIR,
					     null,
					     serviceName, job);
	if (targetDir == null) {
	    // invent some reasonable default
	    String fileSeparator = System.getProperty ("file.separator");
	    targetDir = Config.getString (Config.PROP_BASE_DIR, ".",
					  serviceName, job) +
		fileSeparator + RESULTS_URL_FIX_NAME;
	}

	// file name for this result
	String filename =
	    IOData.cleanFileName (job.getId() + "_" + resultName) +
	    filenameCount;
	String extension = paramDef.get (SoaplabConstants.FILE_EXT);
	if (StringUtils.isNotEmpty (extension))
	    filename += "." + extension;

	// finally, copy it
	File destination = new File (targetDir, filename);
	try {
	    if (result instanceof File) {
		FileUtils.copyFile ((File)result, destination, true );
	    } else if (result instanceof byte[]) {
		FileUtils.writeByteArrayToFile (destination, (byte[])result);
	    } else {
		FileUtils.writeStringToFile (destination, result.toString(),
					     System.getProperty ("file.encoding"));
	    }
	} catch (Throwable e) {
	    throw new SoaplabException
		("Storing URL-based result " + resultName + " failed: " +
		 e.getMessage());
	}

	// last: put together a URL of the just copied file
	String targetUrl =
	    Config.getString (Config.PROP_RESULTS_URL,
			      null,
			      serviceName, job);
	if (targetUrl == null) {
	    String host = Config.getString (Config.PROP_TOMCAT_HOST,
					    "localhost",
					    serviceName, job);
	    String port = Config.getString (Config.PROP_TOMCAT_PORT,
					    "8080",
					    serviceName, job);
	    targetUrl =
		"http://" + host + ":" + port +
		"/soaplab2/" + RESULTS_URL_FIX_NAME +
		"/" + filename;
	} else {
	    targetUrl += "/" + filename;
	}
// 	String targetUrl =
// 	    Config.getString (Config.PROP_RESULTS_URL,
// 			      "http://localhost:8080/soaplab2/" + RESULTS_URL_FIX_NAME,
// 			      serviceName, job) + "/" + filename;
	return targetUrl;
    }

    /******************************************************************************
     * Convert (whatever type of) result to an InputStream.
     * @throws SoaplabException 
     ******************************************************************************/
    protected InputStream result2stream (Object result)
	throws IOException, SoaplabException {

	if (result instanceof InputStream)
	    return (InputStream)result;
	if (result instanceof byte[])
	    return new ByteArrayInputStream ((byte[])result);
	if (result instanceof File)
	    return new BufferedInputStream (new FileInputStream ((File)result));
	if (result instanceof URL) {
	    File tmp = IOData.createTempFile(job.getJobDir());
	    FileUtils.copyURLToFile ((URL)result, tmp);
	    return new BufferedInputStream (new FileInputStream (tmp));
	}
	// TBD?: perhaps I am loosing here some bits?
	return new ByteArrayInputStream (result.toString().getBytes());
    }

    /******************************************************************************
     * Convert (whatever type of) result to something understood by
     * persistent storage handlers (which is File, String or byte[]).
     *
     * TBD: should I use finally {} for closing open streams/files?
     * @throws SoaplabException 
     ******************************************************************************/
    protected Object convert2percy (Object result)
	throws IOException, SoaplabException {

	if (result instanceof InputStream) {
	    File tmpFile = IOData.createTempFile(job.getJobDir());
	    FileOutputStream os = new FileOutputStream (tmpFile);
	    IOUtils.copy ((InputStream)result,
			  new FileOutputStream (tmpFile));
	    ((InputStream)result).close();
	    os.close();
	    return tmpFile;
	}
	if (result instanceof byte[])
	    return result;
	if (result instanceof File) {
	    File result_ = (File)result;
	    if (result_.isDirectory()){
            // here we get directory contents
	        // TBD: how this can be done without loosing file names?
	        File[] files = result_.listFiles();
	        byte[][] contents = new byte[files.length][];
	        int i=0;
	        for (File file:files){
	            contents[i++] = FileUtils.readFileToByteArray(file);
	        }
	        return contents;
	    } else {
	    File tmpFile = IOData.createTempFile(job.getJobDir());
	    FileUtils.copyFile ((File)result, tmpFile);
	    return tmpFile;
	    }
	}
	if (result instanceof URL) {
	    File tmpFile = IOData.createTempFile(job.getJobDir());
	    FileUtils.copyURLToFile ((URL)result, tmpFile);
	    return tmpFile;
	}
	return result.toString();
    }

    /******************************************************************************
     * Retrieve and return all available results, set so far.
     ******************************************************************************/
    public Map<String,Object> getResults()
	throws SoaplabException {
	Map<String,Object> results = percy.getResults (job.getId());
	if (! results.containsKey (SoaplabConstants.RESULT_REPORT)) {
	    results.put (SoaplabConstants.RESULT_REPORT,
			 createReportResult());
	}
	results.put (SoaplabConstants.RESULT_DETAILED_STATUS,
		     createDetailedStatusResult());


	// in special cases, some (non-URL) outputs may be ignored
	// (added May 2011)
	if (metadataAccessor != null) {
	    List<String> toBeIgnored = new ArrayList<String>();
	    Map<String,Object>[] resultSpec = MetadataUtils.outputSpec2Map (metadataAccessor);

	    // find those to be ignored...
	    for (int i = 0; i < resultSpec.length; i++) {
		String name = (String)resultSpec[i].get (SoaplabConstants.RESULT_NAME);
		if (name.endsWith (SoaplabConstants.URL_RESULT_SUFFIX)) {
		    continue;   // never ignore the URL-based results
		}
		// only outputs with a specific option in metadata are ignored
		String ignored = (String)resultSpec[i].get (SoaplabConstants.IGNORED_RESULT);
		if (ignored != null && BooleanUtils.toBoolean (ignored)) {
		    toBeIgnored.add (name);
		}
	    }

	    // ...and remove them from the result
	    for (String name: toBeIgnored) {
		results.remove (name);
	    }
	}

	return results;
    }

    /******************************************************************************
     *
     ******************************************************************************/
    public Map<String,Object> getResults (String[] nameList)
	throws SoaplabException {
	Map<String,Object> results = new Hashtable<String,Object>();
	boolean reportWanted = false;
	for (int i = 0; i < nameList.length; i++) {
	    String name = nameList[i];
	    if (name.equals (SoaplabConstants.RESULT_DETAILED_STATUS)) {
		results.put (SoaplabConstants.RESULT_DETAILED_STATUS, createDetailedStatusResult());
		continue;
	    }
	    Object result = percy.getResult (job.getId(), name);
	    if (name.equals (SoaplabConstants.RESULT_REPORT)) {
		if (result != null) {
		    // report is already in percy => so use it
		    results.put (name, result);
		} else {
		    // report was not yet created => wait; others may still contribute to it
		    reportWanted = true;
		}
	    } else {
		if (result != null) {
		    results.put (name, result);
		}
	    }
	}
	if (reportWanted) {
	    results.put (SoaplabConstants.RESULT_REPORT,
			 createReportResult());
	}
	return results;
    }

    /**************************************************************************
     *
     *************************************************************************/
    public Map<String,String>[] getResultsInfo()
	throws SoaplabException {
	return percy.getResultsInfo (job.getId());
    }

    /**************************************************************************
     *
     *************************************************************************/
    public Properties getJobProperties()
	throws SoaplabException {
	return percy.getJobProperties (job.getId());
    }

    /**************************************************************************
     *
     *************************************************************************/
    public void setJobProperties (Properties props)
	throws SoaplabException {
	percy.setJobProperties (job.getId(), props);
    }

    /******************************************************************************
     * Find an adaptor for the given result and return its
     * instance. Return null if there is no adaptor found.
     ******************************************************************************/
    protected DataAdaptor loadOutputAdaptor (String resultName)
	throws SoaplabException {

	if (metadataAccessor == null)
	    return null;

	String adaptorClassName = null;

	// find an adaptor
	ParamDef paramDef = findParamDef (resultName);
	if (paramDef == null)
	    return null;
	adaptorClassName = paramDef.get (IOParamDef.OUTPUT_ADAPTOR);
	if (StringUtils.isBlank (adaptorClassName))
	    return null;

	return (DataAdaptor)ICreator.createInstance (adaptorClassName);
    }

    /**************************************************************************
     *
     *************************************************************************/
    protected ParamDef findParamDef (String paramName) {

	ParamDef[] defs = metadataAccessor.getParamDefs();
	for (int i = 0; i < defs.length; i++) {
	    if (defs[i].id.equals (paramName))
		return defs[i];
	}
	return null;
    }

}
