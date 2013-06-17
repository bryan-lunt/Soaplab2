// AnalysisService.java
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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.soaplab.services.metadata.DefaultMetadataAccessorFactory;
import org.soaplab.services.metadata.MetadataAccessor;
import org.soaplab.services.metadata.MetadataAccessorFactory;
import org.soaplab.services.metadata.MetadataUtils;
import org.soaplab.services.protocol.ProtocolToolkit;
import org.soaplab.share.Analysis;
import org.soaplab.share.SoaplabConstants;
import org.soaplab.share.SoaplabException;
import org.soaplab.share.SoaplabMap;


/**
 * An implementation of a service representing an analysis. <p>
 *
 * Actually, it is a service which can be called under different names
 * and depending on the name it creates an instance of itself
 * representing one particular analysis. The created instances are
 * remembered and re-used every time the same service name is
 * called. <p>
 *
 * So this class is both a factory - it factorizes instances of
 * analysis - and an implementation for any particular analysis. The
 * role what to do when a request comes depends on the contents of the
 * 'serviceName' field. If this field is not empty we are in an
 * instance representing this particular "analysis". However, if the
 * field is empty, we create an instance with such name, or find
 * already existing instance with such name, and ask it to fulfil the
 * request. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: AnalysisService.java,v 1.19 2010/08/05 12:11:31 mahmutuludag Exp $
 */

public class AnalysisService
    implements Analysis, ProtocolToolkit {

    // store all instances of this class (except the first one which
    // is a factory of the others) - each representing one analysis - as:
    //    key:   service name - String
    //    value: an instance of Analysis
    protected static Map<String,Analysis> analyses = new ConcurrentHashMap<String,Analysis>();

    // a factory for service metadata accessors
    protected static MetadataAccessorFactory accessorFactory;

    // manage all jobs (and their persistent results, and the events
    // they produce, etc.)
    protected static JobManager jobManager;

    // a thread periodically removing service instances form the
    // memory when/if they are no longer used
    protected static Thread serviceCleaningThread;

    private static org.apache.commons.logging.Log log =
       org.apache.commons.logging.LogFactory.getLog (AnalysisService.class);

    // some scripts should be make executable - do it only once
    protected static boolean scriptsPermissionsChanged = false;

    //
    // For individual service instances:
    //

    // remembered once (in constructor), used in log messages etc.
    // (it says for which analysis/service this instance was created)
    protected String serviceName = null;

    // access to this analaysis metadata
    protected MetadataAccessor metadataAccessor;

    // a container where all jobs instances of this service can store
    // their shared attributes (the service does not use them, it just
    // keeps them for its jobs)
    protected Map<String,Object> sharedAttributes = new Hashtable<String,Object>();

    /**************************************************************************
     * An empty constructor. This is used when the first request (for
     * any method of any analysis service) comes. The constructor
     * makes some global initialization used by all analyses.
     *************************************************************************/
    public AnalysisService()
	throws SoaplabException {

	// the accessor factory may be already initialized by a
	// previous instance (we need only one instance - but someone
	// may have deployed this with a different scope, such as
	// "request" or "session") which would lead to more instances
	// of this (which I hope does not do any harm)
	if (accessorFactory == null) {
	    // only the first loaded class interests us
	    Enumeration<MetadataAccessorFactory> spe =
		GenUtils.spiProviders (MetadataAccessorFactory.class);
	    while (spe.hasMoreElements()) {
		accessorFactory = spe.nextElement();
		break;
	    }
	    if (accessorFactory == null)
		// a default factory
		accessorFactory = new DefaultMetadataAccessorFactory();
	    log.info ("Using metadata accessor factory: "
		      + accessorFactory.getClass().getName());
	}

	// make some scripts executable
	if (! scriptsPermissionsChanged) {
	    makeScriptsExecutable();
	    scriptsPermissionsChanged = true;
	}

	synchronized (AnalysisService.class){
	// create a Job manager
	if (jobManager == null)
	    jobManager = new JobManager();

	// start cleaning thread (one is enough)
	if (serviceCleaningThread == null) {
	    serviceCleaningThread = getServiceCleaningThread();
	    serviceCleaningThread.setDaemon(true);
	    serviceCleaningThread.setName ("ServiceCleaning");
	    serviceCleaningThread.start();
	}
	}
    }

    // separated here if anybody wants to have a different cleaning
    // policy - in which case subclass this class and deploy your class
    protected Thread getServiceCleaningThread() {
	return new ServiceCleaningThread();
    }


    /**************************************************************************
     * A thread that periodically scans the list of instantiated
     * services in order to remove them from the memory - they will be
     * able to be instantiated again when a new request comes (unless
     * they are in the meantime undeployed by the service provider).
     *
     * Only those services are being removed that have all jobs
     * completed longer than a "cleaning interval" ago.
     **************************************************************************/
    class ServiceCleaningThread
	extends Thread {

	public ServiceCleaningThread() {
	}

	private int getCleaningInterval() {
	    return
		Config.get().getInt (Config.PROP_SERVICES_CLEANING_INTERVAL,
				     5 * 60 * 1000);  // every 5 minutes
	}

	public void run() {
	    while (true) {
		// re-read configuration property (it may have been changed)
		long cleaningInterval = getCleaningInterval();

		// start by sleeping for a while
		try { Thread.sleep (cleaningInterval); }
		catch (InterruptedException e) {}

		try {
		    // check inactive services
		    for (String key: analyses.keySet() ) {
			int countActiveJobs = jobManager.getCountActiveJobs (key);
			if (countActiveJobs == 0) {
			    int countStillActive = analyses.size();
			    // remove this service
			    countStillActive--;
			    analyses.remove (key);
			    if (log.isInfoEnabled()) {
				StringBuilder buf = new StringBuilder (200);
				buf.append ("Service ");
				buf.append (key);
				buf.append (" removed from memory");
				if (countStillActive > 0) {
				    buf.append (" [still ");
				    buf.append (countStillActive);
				    buf.append (" active service(s)]");
				}
				buf.append (" (cleaning interval is ");
				buf.append (DurationFormatUtils.formatDurationWords
					    (cleaningInterval, true, false));
				buf.append (")");
				log.info (buf.toString());
			    }
			}
                    }
		} catch (RuntimeException e) {
		    if (log==null){//means web application reloaded
			System.err.printf("Services cleaning thread: "+
					  "error:%s, web application was probably reloaded\n", e);
			return;
		    }
		    log.error(e);
		}
	    }
	}
    }

    /**************************************************************************
     * A constructor that is used by the AnalysisService factory
     * (which is again an instance of this same class). By simply
     * remembering the 'serviceName' it makes sure that this instance
     * will be used as a ready-to-serve instance (and not as a
     * factory).
     *************************************************************************/
    public AnalysisService (String serviceName)
	throws SoaplabException {
	this();
	this.serviceName = serviceName;
	log.info ("Service instance created: " + serviceName);
	Analysis instance = analyses.get (serviceName);
    if (instance == null) {
        // create an access to the metadata of this service
        metadataAccessor = accessorFactory.getInstance (serviceName);       
        analyses.put (serviceName, this);
    }
    else
        metadataAccessor = ((AnalysisService)instance).getMetadataAccessor();        	
    }

    /**************************************************************************
     *
     *************************************************************************/
    protected void checkServiceExistence()
	throws SoaplabException {
	if (AnalysisInventoryProvider.instance()
	    .getServiceInstallation (serviceName) == null)
	    throw new SoaplabException ("Unknown service '" + serviceName + "'");
    }

    /**************************************************************************
     * It tries to make scripts in the PROP_SCRIPTS_DIR
     * executable. There is no sense to do it for Ms-Windows. Report
     * failure to a log, otherwise be silent.
     *************************************************************************/
    protected void makeScriptsExecutable() {
	try {
	    if (System.getProperty ("os.name").toLowerCase().contains ("windows"))
		return;
	    String scriptsDir =
		Config.get().getString (Config.PROP_SCRIPTS_DIR, null);
	    if (StringUtils.isBlank (scriptsDir))
		return;

	    ProcessBuilder pb = new ProcessBuilder ("chmod", "-R", "a+x", scriptsDir);
	    Process process = pb.start();

	    // wait until the child process ends
	    while (true) {
		try {
		    int exitCode = process.waitFor();
		    if (exitCode != 0) {
			log.error ("Changing scripts permissions failed with exit code: " + exitCode);
			return;
		    }
		    break;
		} catch (InterruptedException e) {
		}
	    }

	    // reading errors
	    String line;
	    BufferedReader input =
		new BufferedReader
		(new InputStreamReader (process.getErrorStream()));
	    while ((line = input.readLine()) != null) {
		log.error ("Changing scripts permissions failed: " + line);
	    }
	    input.close();
	    log.info ("Scripts made executable in " + scriptsDir);


	} catch (Throwable e) {
	    SoaplabException.formatAndLog (e, log);
	}
    }

    /**************************************************************************
     * Using the name of the service which was called by the current
     * request, find an existing instance representing such service,
     * or create a new instance and remember it. In any case, return
     * it also back. This method is called at the beginning of each
     * request.
     *************************************************************************/
    protected Analysis findOrCreate()
	throws SoaplabException {

	String name = getServiceName();
	synchronized (analyses){
	Analysis instance = analyses.get (name);
	if (instance == null) {
  	    instance = new AnalysisService (name);
	    analyses.put (name, instance);
	}
	return instance;
	}
    }


    // ------------------------------------------------------------------------
    //
    //  Methods implementing ProtocolToolkit interface
    //
    // ------------------------------------------------------------------------

    /**************************************************************************
     * Return a name of service which was called by the current
     * request. <p>
     *
     * This implementation just returns a service name given in the
     * constructor (if any) - but subclasses overwrite this according
     * to the protocol that they use. <p>
     *
     *************************************************************************/
    public String getServiceName()
	throws SoaplabException {
	return serviceName;
    }


    // ------------------------------------------------------------------------
    //
    //  Methods implementing Analysis interface
    //
    // ------------------------------------------------------------------------

    /**************************************************************************
     * Return an XML string containing metadata describing this
     * analysis service.
     *************************************************************************/
    public String describe()
	throws SoaplabException {
	if (serviceName == null)
	    return findOrCreate().describe();
	return metadataAccessor.describe();
    }

    /**************************************************************************
     * Return an array with attributes of all possible inputs for this
     * analysis service.
     *************************************************************************/
    public SoaplabMap[] getInputSpec()
	throws SoaplabException {
	if (serviceName == null)
	    return findOrCreate().getInputSpec();

	Map<String,Object>[] inputSpec = MetadataUtils.inputSpec2Map (metadataAccessor);
	SoaplabMap[] result = new SoaplabMap [inputSpec.length];
	for (int i = 0; i < inputSpec.length; i++) {
	    String type = (String)inputSpec[i].get (SoaplabConstants.INPUT_TYPE);
	    inputSpec[i].put (SoaplabConstants.INPUT_TYPE, convertType (type));
	    result[i] = SoaplabMap.fromMap (inputSpec [i]);
	}
	return result;
    }

    /**************************************************************************
     * Return a hashtable with names and types of all possible results
     * for this analysis service.
     *************************************************************************/
    public SoaplabMap[] getResultSpec()
	throws SoaplabException {
	if (serviceName == null)
	    return findOrCreate().getResultSpec();

	Map<String,Object>[] resultSpec = MetadataUtils.outputSpec2Map (metadataAccessor);
	SoaplabMap[] result = new SoaplabMap [resultSpec.length];
	for (int i = 0; i < resultSpec.length; i++) {
	    String type = (String)resultSpec[i].get (SoaplabConstants.RESULT_TYPE);
	    resultSpec[i].put (SoaplabConstants.RESULT_TYPE, convertType (type));
	    result[i] = SoaplabMap.fromMap (resultSpec [i]);
	}
	return result;
    }

    /**************************************************************************
     * Return attributes describing this analysis service.
     *************************************************************************/
    public SoaplabMap getAnalysisType()
	throws SoaplabException {
	if (serviceName == null)
	    return findOrCreate().getAnalysisType();

	return SoaplabMap.fromMap (MetadataUtils.analysisSpec2Map (metadataAccessor));
    }

    /**************************************************************************
     * Convert string representing an input/output type as read from
     * the XML metadata file, to a string how I want it to have
     * here. The need of this conversion is purely historical - the
     * XML files are still being created by the generators that
     * believe in CORBA and its type system :-). <p>
     *
     * @param type to be converted
     * @return converted type
     **************************************************************************/
    public static String convertType (String type) {

	if ( type.equalsIgnoreCase ("sequence<octet>") || type.equalsIgnoreCase ("byte[]") )
	    return SoaplabConstants.TYPE_BINARY_SIMPLE;
	else if ( type.equalsIgnoreCase ("sequence<any>") || type.equalsIgnoreCase ("byte[][]") )
	    return SoaplabConstants.TYPE_BINARY_ARRAY;
	else if (type.equalsIgnoreCase ("String[]"))
	    return SoaplabConstants.TYPE_TEXT_ARRAY;
	else if (type.equalsIgnoreCase ("boolean"))
	    return SoaplabConstants.TYPE_BOOLEAN_SIMPLE;
    else if (type.equalsIgnoreCase ("long"))
        return type;
    else if (type.equalsIgnoreCase ("float"))
        return type;
	else
 	    return SoaplabConstants.TYPE_TEXT_SIMPLE;
    }

    // 
    // Methods controlling an execution of this analysis
    //

    /**************************************************************************
     * Prepare a job with given input data but do not start it.  It
     * returns a job identifier which can be used by other methods to
     * start and to control the job.
     *************************************************************************/
    public String createJob (SoaplabMap inputs)
	throws SoaplabException {
	if (serviceName == null)
	    return findOrCreate().createJob (inputs);
	checkServiceExistence();
	return jobManager
	    .createJob (serviceName,
			metadataAccessor,
			SoaplabMap.toMap (inputs),
			sharedAttributes)
	    .getId();
    }

    /**************************************************************************
     * Start an underlying analysis and return immediately, without
     * waiting for its completion.
     *************************************************************************/
    public void run (String jobID)
	throws SoaplabException {
	if (serviceName == null) {
	    findOrCreate().run (jobID);
	} else {
	    checkServiceExistence();
	    jobManager.findJob (jobID,
				serviceName,
				metadataAccessor,
				sharedAttributes).run();
	}
    }

    /**************************************************************************
     * Not implemented.
     *************************************************************************/
    public void runNotifiable (String jobID, String notificationDescriptor)
	throws SoaplabException {
	run (jobID);
    }

    /**************************************************************************
     * A convenient combination of methods 'createJob' and 'run'.
     *************************************************************************/
    public String createAndRun (SoaplabMap inputs)
	throws SoaplabException {
	if (serviceName == null)
	    return findOrCreate().createAndRun (inputs);

	checkServiceExistence();
	Job job = jobManager
	    .createJob (serviceName,
			metadataAccessor,
			SoaplabMap.toMap (inputs),
			sharedAttributes);
	job.run();
	return job.getId();
    }

    /**************************************************************************
     * Not implemented.
     *************************************************************************/
    public String createAndRunNotifiable (SoaplabMap inputs,
					  String notificationDescriptor)
	throws SoaplabException {
	return createAndRun (inputs);
    }

    /**************************************************************************
     * Block the client until the underlying running analysis is
     * completed. The running job is identified by 'jobID'.
     *************************************************************************/
    public void waitFor (String jobID)
	throws SoaplabException {
	if (serviceName == null)
	    findOrCreate().waitFor (jobID);
	else
	    jobManager.findJob (jobID,
				serviceName,
				metadataAccessor,
				sharedAttributes)
		.waitFor();
    }

    /**************************************************************************
     * Block the client until the underlying running analysis is
     * completed and return immediately all results. The running job
     * is identified by the given job ID.
     *************************************************************************/
    public SoaplabMap runAndWaitFor (SoaplabMap inputs)
	throws SoaplabException {
	if (serviceName == null)
	    return findOrCreate().runAndWaitFor (inputs);
	checkServiceExistence();
	Job job =
	    jobManager.createJob (serviceName,
				  metadataAccessor,
				  SoaplabMap.toMap (inputs),
				  sharedAttributes);
	job.run();
	job.waitFor();
	return SoaplabMap.fromMap (job.getResults());
    }

    /**************************************************************************
     * Terminate the running analysis, identified by its job ID.
     *************************************************************************/
    public void terminate (String jobID)
	throws SoaplabException {
	if (serviceName == null)
	    findOrCreate().terminate (jobID);
	else
	    jobManager.findJob (jobID,
				serviceName,
				metadataAccessor,
				sharedAttributes)
		.terminate();
    }

    // 
    // Methods giving analysis status and results
    //

    /**************************************************************************
     * Return the last event which happened to the given job.
     *************************************************************************/
    public String getLastEvent (String jobID)
	throws SoaplabException {
	if (serviceName == null)
	    return findOrCreate().getLastEvent (jobID);
	return jobManager.findJob (jobID,
				   serviceName,
				   metadataAccessor,
				   sharedAttributes)
	    .getLastEvent();
    }

    /**************************************************************************
     * Not implemented.
     *************************************************************************/
    public String getNotificationDescriptor (String jobID)
	throws SoaplabException {
	if (serviceName == null)
	    return findOrCreate().getNotificationDescriptor (jobID);
	jobManager.findJob (jobID,
			    serviceName,
			    metadataAccessor,
			    sharedAttributes);   // TBD
	return "";
    }

    /**************************************************************************
     * Return a string describing the current job status.
     *************************************************************************/
    public String getStatus (String jobID)
	throws SoaplabException {
	if (serviceName == null)
	    return findOrCreate().getStatus (jobID);
	return jobManager.findJob (jobID,
				   serviceName,
				   metadataAccessor,
				   sharedAttributes)
	    .getStatus();
    }

    /**************************************************************************
     * Return times of various stages of execution. The values in the
     * returned map are of long type but stored as Strings.
     *************************************************************************/
    public SoaplabMap getCharacteristics (String jobID)
	throws SoaplabException {
	if (serviceName == null)
	    return findOrCreate().getCharacteristics (jobID);
	Job job = jobManager.findJob (jobID,
				      serviceName,
				      metadataAccessor,
				      sharedAttributes);
	Map<String,Object> result = new HashMap<String,Object>();
	long time;
	if ((time = job.getCreated()) > 0)
	    result.put (SoaplabConstants.TIME_CREATED, ""+time);
        if ((time = job.getStarted()) > 0)
	    result.put (SoaplabConstants.TIME_STARTED, ""+time);
        if ((time = job.getEnded())   > 0)
	    result.put (SoaplabConstants.TIME_ENDED,   ""+time);
        result.put (SoaplabConstants.TIME_ELAPSED, ""+job.getElapsed());
	return SoaplabMap.fromMap (result);
    }

    /**************************************************************************
     * Return all (available) results from the given job.
     * This implementation recognizes the following result types:
     *    - basic types and arrays of basic types
     *    - byte[] and byte[][] (e.g. for image and set of images)
     *************************************************************************/
    public SoaplabMap getResults (String jobID)
	throws SoaplabException {
	if (serviceName == null)
	    return findOrCreate().getResults (jobID);
	return SoaplabMap.fromMap (jobManager.findJob (jobID,
						       serviceName,
						       metadataAccessor,
						       sharedAttributes)
				   .getResults());
    }

    /**************************************************************************
     * Return named (and available) results from the given job.
     *************************************************************************/
    public SoaplabMap getSomeResults (String jobID, String[] resultNames)
	throws SoaplabException {
	if (serviceName == null)
	    return findOrCreate().getSomeResults (jobID, resultNames);
	return SoaplabMap.fromMap (jobManager.findJob (jobID,
						       serviceName,
						       metadataAccessor,
						       sharedAttributes)
				   .getResults (resultNames));
    }

    /**************************************************************************
     *
     *************************************************************************/
    public SoaplabMap[] getResultsInfo (String jobID)
	throws SoaplabException {
	if (serviceName == null)
	    return findOrCreate().getResultsInfo (jobID);
	Map<String,String>[] resultInfo =
	    jobManager.findJob (jobID,
				serviceName,
				metadataAccessor,
				sharedAttributes)
	    .getResultsInfo();

	SoaplabMap[] result = new SoaplabMap [resultInfo.length];
	for (int i = 0; i < resultInfo.length; i++) {
	    result[i] = SoaplabMap.fromStringMap (resultInfo [i]);
	}
	return result;
    }


    //
    // Methods for releasing resources
    //

    /**************************************************************************
     * Release related resources, including results.
     *************************************************************************/
    public void destroy (final String jobID)
	throws SoaplabException
	{
        Runnable r = new Runnable()
        {

            public void run()
            {
                jobManager.removeJob (jobID);
            }

        };

        new Thread(r, "Remove job- "+jobID);
	}


    // ------------------------------------------------------------------------
    //
    //  Other public methods (not part of Analysis interface)
    //
    // ------------------------------------------------------------------------

    /**************************************************************************
     * Allow access to this service metadata API.
     *************************************************************************/
    public MetadataAccessor getMetadataAccessor() {
	return metadataAccessor;
    }
    
}
