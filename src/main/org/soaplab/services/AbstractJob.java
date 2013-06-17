// AbstractJob.java
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

import org.soaplab.share.SoaplabException;
import org.soaplab.share.SoaplabConstants;
import org.soaplab.services.metadata.MetadataAccessor;
import org.soaplab.services.metadata.InputPropertyDef;
import org.soaplab.services.metadata.ParamDef;

import org.apache.commons.lang.StringUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;
import java.util.HashSet;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A convenient parent of other Job's implementations. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: AbstractJob.java,v 1.23 2010/10/11 14:32:53 marsenger Exp $
 */

public abstract class AbstractJob
    implements Job {

    private static org.apache.commons.logging.Log log =
       org.apache.commons.logging.LogFactory.getLog (AbstractJob.class);

    // job identity
    protected String id;

    // input data
    protected Map<String,Object> inputs = new Hashtable<String,Object>();

    // access to all metadata describing this analysis
    protected MetadataAccessor metadataAccessor;

    // my reporter
    protected Reporter reporter;

    // does this instance represent a completely new job, or a
    // re-created job?
    protected boolean jobRecreated = false;

    // turn it on when a run method was called
    protected boolean jobRunStarted = false;

    // attributes shared with other jobs (but of the same service)
    protected Map<String,Object> sharedAttributes;

    /**
     * A shared attribute name. Its value is a <tt>Parameter</tt>
     * array with instances being able to create command-line
     * parameters, and few other things.
     */
    protected final static String JOB_SHARED_PARAMETERS = "job.shared.parameters";

    // a piece of an error message
    public final static String INTERNAL_ERROR = "[Internal error] ";


    /**************************************************************************
     * An empty constructor (for sub-classes).
     **************************************************************************/
    protected AbstractJob() {
    }

    /**************************************************************************
     * The main constructor.
     **************************************************************************/
    protected AbstractJob (String jobId,
			   MetadataAccessor metadataAccessor,
			   Reporter reporter,
			   Map<String,Object> sharedAttributes,
			   boolean jobRecreated)
	throws SoaplabException {
	setId (jobId);
	setMetadataAccessor (metadataAccessor);
	setReporter (reporter);
	setSharedAttributes (sharedAttributes);
	this.jobRecreated = jobRecreated;
	log.info ((jobRecreated ? "Re-created" : "Created") +
		  " job: " + id);
	init();
    }

    /**************************************************************************
     * Do things that are usually done only once for all Job instances
     * (for a particular service). If there is need to store something
     * to be shared by all these job instances (which usually is), put
     * it into 'sharedAttributes'.
     *
     * It is an empty method here - it's up to the subclasses not only
     * to fill it with some behaviour, but also make sure that things
     * are done really only once (normally, a subclass instance will
     * check whether some attribute is already set in the
     * 'sharedAttribute').
     *
     * Throw an exception if the initialization fails.
     **************************************************************************/
    protected synchronized void init()
	throws SoaplabException {
    }

    /**************************************************************************
     * Set this job ID.
     **************************************************************************/
    public void setId (String jobId) {
	id = jobId;
    }

    /**************************************************************************
     * Set reporter.
     **************************************************************************/
    public void setReporter (Reporter reporter) {
	this.reporter = reporter;
 	reporter.setJob (this);
	reporter.getState().set (JobState.CREATED);
    }

    /**************************************************************************
     * Get reporter.
     **************************************************************************/
    public Reporter getReporter() {
	return reporter;
    }

    /**************************************************************************
     * Set metadata accessor.
     **************************************************************************/
    public void setMetadataAccessor (MetadataAccessor metadataAccessor) {
	this.metadataAccessor = metadataAccessor;
    }

    /**************************************************************************
     * Get metadata accessor.
     **************************************************************************/
    public MetadataAccessor getMetadataAccessor() {
	return metadataAccessor;
    }

    /**************************************************************************
     * Return this job identity (an ID).
     **************************************************************************/
    public String getId() { return id; }

    /**************************************************************************
     * Destroy resources related to this job. <p>
     *
     * It does *not* include results in persistent storages. These
     * will be taken care of by the JobManager (the creator and
     * creamatorium for Jobs).
     **************************************************************************/
    public void destroy() {
	reporter.getState().set (JobState.REMOVED);
    }

    /**************************************************************************
     * Log the given error message and throw SoaplabException using
     * the same message.
     *************************************************************************/
    protected void logAndThrow (String msg)
	throws SoaplabException {
	log.error (msg);
	throw new SoaplabException (msg);
    }

    /**************************************************************************
     * Log and throw an internal error.
     **************************************************************************/
    protected void internalError (String msg)
	throws SoaplabException {
	log.error (INTERNAL_ERROR + msg);
	throw new SoaplabException (SoaplabConstants.FAULT_INTERNAL_ERROR);
    }

    /**************************************************************************
     * Report to the reporter, potentially adding a new line to the
     * given message.
     *************************************************************************/
    protected void report (String msg) {
	reporter.report (addNL (msg));
    }

    /**************************************************************************
     * Report an error message to the reporter, potentially adding a
     * new line to the given message.
     *************************************************************************/
    protected void error (String msg) {
	reporter.error (addNL (msg));
    }

    //
    private String addNL (String msg) {
	return (msg.endsWith ("\n") ? msg : msg + "\n");
    }

    /**************************************************************************
     * Return a name of a service invoking this job, or null. This is
     * a convenient method - so subclasses do not need to catch
     * SoaplabException when interrogating service metadata.
     **************************************************************************/
    protected String getServiceName() {
	if (serviceName == null) {
	    try {
		serviceName = metadataAccessor.getServiceName();
	    } catch (SoaplabException e) {
		return null;
	    }
	}
	return serviceName;
    }

    // optimalization (extract it only once)
    String serviceName;



    // ------------------------------------------
    //
    // A specific/special place just for this job
    //
    // ------------------------------------------

    /**************************************************************************
     * Establish the root of the "sandbox" directory. A "sandbox" is a
     * directory where all inputs and outputs files are kept during an
     * invocation of an external tool. It serves also as a "working
     * directory" when an external tool is invoked.
     **************************************************************************/
    protected File getSandbox()
	throws SoaplabException {
	String dir = Config.getString (Config.PROP_SANDBOX_DIR,
				       null,   // no default value
				       getServiceName(),
				       this);
	if (dir == null) {
	    return createAndCheckTemporary();
	} else {
	    File sandboxDir = new File (dir);
	    if (! createAndCheck (sandboxDir)) {
		sandboxDir = createAndCheckTemporary();
	    }
	    return sandboxDir;
	}
    }

    /**************************************************************************
     * Create a directory for inputs/outputs in a temporary directory,
     * and returns a File representing it. Throw an internal error if
     * it failed - because we cannot do much without a space for
     * creating and storing inputs and outputs, can we?
     **************************************************************************/
    protected File createAndCheckTemporary()
	throws SoaplabException {
	File dir = new File (System.getProperty ("java.io.tmpdir"),
			     "soaplab.sandbox");
	if (createAndCheck (dir))
	    return dir;
	internalError ("Problems with temporary directory '" + dir.getAbsolutePath());
	return null;  // never comes here
    }

    /**************************************************************************
     * Create the given directory 'dir' (unless it already exists - in
     * which case check that it is realy a directory), and return
     * true. If failed, log an error and return false.
     **************************************************************************/
    protected boolean createAndCheck (File dir) {
	if (! dir.exists())
	    dir.mkdirs();
	if (dir.isDirectory() && dir.exists() && dir.canWrite())
	    return true;
	if (log.isErrorEnabled()) {
	    String path = dir.getAbsolutePath();
	    if (! alreadyReportedFiles.contains (path)) {
		log.error ("Problems with directory '" + path);
		alreadyReportedFiles.add (path);
	    }
	}
	return false;
    }

    private Set<String> alreadyReportedFiles = new HashSet<String>();

    /**************************************************************************
     * The job directory is created in a SANDBOX (see more in getSandbox()).
     **************************************************************************/
    public File getJobDir()
	throws SoaplabException {

	File dir = new File (getSandbox(), getId());
	dir.mkdir();
	if (! dir.exists()) {
	    internalError ("Cannot create " + dir.getAbsolutePath());
	}
	if (! dir.canWrite()) {
	    internalError ("Cannot write to " + dir.getAbsolutePath());
	}
	return dir;
    }


    /******************************************************************************
     * Check if the 'dataRef' (which is a file name or a URL) is safe
     * for this job. This implementation consider it safe if: <p>
     *
     * - It is an empty reference, or
     *
     * - It is a URL with any protocol except of 'file', or
     *
     * - The path of the URL with the 'file' protocol or an ordinary
     *   file name point to a non-existent file, or
     *
     * - The path of the URL with the 'file' protocol or an ordinary
     *   file name have an absolute path starting in SANDBOX directory
     *   (see getSandbox()) and it does not contain anywhere '..'.
     *
     ******************************************************************************/
    public boolean isDataReferenceSafe (String dataRef,
					ParamDef paramDef) {

	if (StringUtils.isEmpty (dataRef))
	    return true;

	try {
	    // do we have a safe URL?
	    URL url = new URL (dataRef);
	    if ("file".equals (url.getProtocol())) {
		// ...yes, but it is a local file, so check more
		return isDataReferenceSafe (url.getPath(), paramDef);
	    } else {
		// ...yes, and it is not a file, let's conisder it safe
		return true;
	    }

	} catch (MalformedURLException e) {
	    // given reference is not a URL; but is it safe?
	    if (dataRef.contains (".."))
		return false;
	    File file = new File (dataRef);
	    if (! file.exists())
		return true;
	    try {
		File sandbox = getSandbox();
		if (file.getAbsolutePath().startsWith (sandbox.getAbsolutePath()))
		    return true;
	    } catch (SoaplabException e2) {
		// I cannot be sure if it is safe or not...
	    }
	    return false;
	}
    }


    // --------------------------------------
    //
    // Dealing with job inputs and invocation
    //
    // --------------------------------------


    /**************************************************************************
     * Set input data into this job. The 'inputs' are not cloned -
     * only their reference is stored (so it can be still changed by
     * the caller - which is not, however, recommended. <p>
     *
     * @throws SoaplabException if the job's status does not allow to
     * set new inputs anymore, or if 'inputs' are null
     **************************************************************************/
    public void setInputs (Map<String,Object> inputs)
	throws SoaplabException {
	if (inputsAllowed() && inputs != null) {
	    this.inputs = inputs;
	} else {
	    logAndThrow (SoaplabConstants.FAULT_NOT_ACCEPTABLE_INPUTS);
	}
    }

    /**************************************************************************
     * Set an input value, given by its name. <p>
     *
     * @throws SoaplabException if the job's status does not allow to
     * set new inputs anymore, or if 'value' is null
     **************************************************************************/
    public void setInputValue (String inputName, Object value)
	throws SoaplabException {
	if (inputsAllowed() && value != null) {
	    inputs.put (inputName, value);
	} else {
	    logAndThrow (SoaplabConstants.FAULT_NOT_ACCEPTABLE_INPUTS);
	}
    }

    /**************************************************************************
     * Return true if the job can accept input data.
     **************************************************************************/
    protected boolean inputsAllowed() {
	int jobState = reporter.getState().get();
	return (jobState == JobState.CREATED || jobState == JobState.UNKNOWN);
    }

    /**************************************************************************
     * The meat of Soaplab. <p>
     *
     * It is very probable, almost indisputable, that this method
     * needs to be overridden in order to do something useful. Or,
     * override only the 'realRun' method.
     **************************************************************************/
    public void run()
	throws SoaplabException {
	if (! runAllowed()) return;
	checkInputs();
	reporter.getState().set (JobState.RUNNING);
	realRun();
	reporter.getState().set (JobState.COMPLETED);
	reporter.setDetailedStatusResult();
	reporter.setReportResult();

	// inform other threads waiting for the termination that
	// it has been done
	synchronized (reporter) {
	    reporter.notifyAll();
	}
    }

    /**************************************************************************
     * Overwrite this. <p>
     *
     * This is called from the default {@link #run} method. The 'run'
     * method takes care about reporting starting status and about
     * synchronization with the {@link #waitFor} method. But the real
     * meat must be written here by you. Or, overwrite the whole
     * {@link #run} method instead.
     **************************************************************************/
    protected void realRun()
	throws SoaplabException {
    }

    /**************************************************************************
     * Make sure that the run method will be used maximum once. Return
     * 'false' if the job run was already started. Return also 'false'
     * for re-created jobs (because they do not have any iputs, so
     * they cannot be - at least usually - run).
     **************************************************************************/
    protected synchronized boolean runAllowed() {
	if (jobRunStarted || jobRecreated)
	    return false;
	jobRunStarted = true;
	return true;
    }

    /**************************************************************************
     * Block and wait until job completes. <p>
     *
     * Be sure that it is correctly synchronized with your own {@link
     * #run} method. By default, it waits for "notify()" executer on
     * the 'reporter' instance.
     **************************************************************************/
    public void waitFor()
    throws SoaplabException {
    	int maxreplytime = Config.getInt(Config.PROP_JOBS_WAITFOR_TIMEOUT,
    			60 * 60 * 24 * 10000,  // 10 days
    			getServiceName(), this);
    	synchronized (reporter) {
    		while (reporter.getState().get() == JobState.RUNNING) {
    			try {
    				reporter.wait(maxreplytime);
    			} catch (Exception e) {
    			}	
    			if (getElapsed() > maxreplytime){
    				String errmsg = "Job "+id+" was not completed within the maximum waitfor limit," +
    				" "+maxreplytime+" msecs. You can learn status of this job using the getStatus" +
    				" method and retrieve its results once it is completed.";
    				log.info(errmsg);
    				//TODO: error msg is not propogated into the Axis fault msg? why?
    				//throw new SoaplabException(errmsg);
    			}
    		}
    	}
    }

    /**************************************************************************
     * Terminate this running job.
     **************************************************************************/
    public void terminate()
	throws SoaplabException {
	reporter.getState().set (JobState.TERMINATED_BY_REQUEST);

	// inform other threads waiting for the termination that
	// it has been done
	synchronized (reporter) {
	    reporter.notifyAll();
	}
    }

    /**************************************************************************
     * Check input data. <p>
     *
     * It make the following checks on the inputs (the inputs were
     * given in the time of createing this job):
     *
     *<ul>
     * <li> Chcek whether all input names are recognized.
     * <li> Check whether inputs with predefined lists are from the allowed values.
     * <li> Check whether all mandatory inputs are present
     *</ul>
     *
     * @throws SoaplabException if any of the check fails. It tries to
     * report all possible problems (not just to stop after finding
     * the first error).
     **************************************************************************/
    protected void checkInputs()
	throws SoaplabException {

	if (metadataAccessor == null)
	    return;

	// inputDefs are created for better access to input
	// definitions by name
	Map<String,InputPropertyDef> inputDefs = new HashMap<String,InputPropertyDef>();
	InputPropertyDef[] ipd = metadataAccessor.getInputDefs();
	for (int i = 0; i < ipd.length; i++)
	    inputDefs.put (ipd[i].name, ipd[i]);

	StringBuilder errBuf = new StringBuilder();
	String key;
	for (Iterator<Map.Entry<String,Object>> it = inputs.entrySet().iterator();
	     it.hasNext(); ) {
	    Map.Entry<String,Object> entry = it.next();
	    key = entry.getKey();

	    // an unknown name is an error - but delay to report it
	    // until we collect all errors
	    InputPropertyDef def = inputDefs.get (key);
	    if (def == null) {
	    	// TODO: following is an exception for typed interface
		    // current metadata files for EMBOSS doesn't include alignment format specification input 
		    // more work needed towards addressing this issue
		    boolean exception = this.getClass().getName().equals("org.soaplab.emboss.EmbossJob") &&
		     key.startsWith("aformat_");
		    if (!exception)	
		errBuf.append ("Unknown input (not recognized input name): " + key + "\n");
		continue;
	    }

	    // allow only allowed values
	    if (def.possibleValues != null && def.possibleValues.length > 0) {
		String value = entry.getValue().toString();
		boolean found = false;
		for (int i = 0; i < def.possibleValues.length; i++) {
		    if (def.possibleValues[i].equals (value)) {
			found = true;
			break;
		    }
		}
		if (!found) {
		    errBuf.append ("Input '" + key + "' cannot have value '" + value + "'. " +
				   "Only the following values are allowed: " +
				   StringUtils.join (def.possibleValues, ", ") + "\n");
		    continue;
		}
	    }
	}

	// check mandatorness (but not for "pairs" - such as
	// '..._direct_data' and '..._url')
	for (int i = 0; i < ipd.length; i++) {
	    if (ipd[i].mandatory && ! inputs.containsKey (ipd[i].name)) {
		errBuf.append ("Input '" + ipd[i].name + "' is mandatory and cannot remain empty.\n");
		continue;
	    }
	} 

	// any errors to report?
	reportErrors (errBuf);

    }

    //
    // any errors to report?
    //
    protected void reportErrors (StringBuilder errBuf)
	throws SoaplabException {
	if (errBuf.length() > 0) {
	    String msg = errBuf.toString();
	    log.debug ("Invalid input: " + msg);
	    throw new SoaplabException
		(SoaplabConstants.FAULT_NOT_VALID_INPUTS + "\n" + msg);
	}
    }

    // ----------------------------------
    //
    // Dealing with job status and events
    //
    // ----------------------------------


    /**************************************************************************
     * Job created - returns number of millis from the BOE.
     **************************************************************************/
    public long getCreated() {
	return reporter.getState().getCreated();
    }

    /**************************************************************************
     * Job started - returns number of millis from the BOE.
     **************************************************************************/
    public long getStarted() {
	return reporter.getState().getStarted();
    }

    /**************************************************************************
     * Job ended - returns number of millis from the BOE.
     **************************************************************************/
    public long getEnded() {
	return reporter.getState().getEnded();
   }

    /**************************************************************************
     * Job time elapsed - returns number in milliseconds.
     **************************************************************************/
    public long getElapsed() {
	return reporter.getState().getElapsed();
    }

    /**************************************************************************
     *
     **************************************************************************/
    public String getStatus() {
	return reporter.getState().getAsString();
    }

    /**************************************************************************
     *
     **************************************************************************/
    public String getLastEvent() {
	return reporter.getState().getLastEvent().toString();
    }


    // ------------------------
    //
    // Dealing with job results
    //
    // ------------------------


    /**************************************************************************
     * Return all (available) results.
     **************************************************************************/
    public Map<String,Object> getResults()
	throws SoaplabException {
	return reporter.getResults();
    }

    /**************************************************************************
     * Return all wanted (and available) results.
     **************************************************************************/
    public Map<String,Object> getResults (String[] resultNames)
	throws SoaplabException {
	return reporter.getResults (resultNames);
    }

    /**************************************************************************
     *
     *************************************************************************/
    public Map<String,String>[] getResultsInfo()
	throws SoaplabException {
	return reporter.getResultsInfo();
    }

    /**************************************************************************
     *
     *************************************************************************/
    public void setSharedAttributes (Map<String,Object> sharedAttributes) {
	this.sharedAttributes = sharedAttributes;
    }
    
    /**************************************************************************
    *
    *************************************************************************/
    public boolean isRecreated(){
        return jobRecreated;
    }

}
