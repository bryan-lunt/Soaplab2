// JobManager.java
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.soaplab.services.events.EventManager;
import org.soaplab.services.metadata.AnalysisInstallation;
import org.soaplab.services.metadata.MetadataAccessor;
import org.soaplab.services.storage.PersistenceManager;
import org.soaplab.services.storage.PersistentStorage;
import org.soaplab.share.SoaplabConstants;
import org.soaplab.share.SoaplabException;
import org.soaplab.tools.ICreator;

/**
 * A job manager is a factory (and also a crematorium) for Jobs. It
 * can keep their IDs in memory, find them later, and provide them or
 * remove them. It also creates (for each job) links to persistence
 * storage handlers and to event handlers. <p>
 *
 * For each job, it creates an instance of a class (implementing the
 * {@link Job} interface) that is defined in the service metadata, or
 * in the service list metadata. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: JobManager.java,v 1.28 2011/04/06 13:19:45 mahmutuludag Exp $
 */

public class JobManager {

    private static org.apache.commons.logging.Log log =
       org.apache.commons.logging.LogFactory.getLog (JobManager.class);

    // in-memory storage for all jobs
    protected Map<String,Job> jobs = new ConcurrentHashMap<String,Job>();

    // persistence manager for storing jobs out-of-memory
    protected PersistentStorage percy;

    // event manager for informing all event listeners what is
    // happenning with jobs
    protected EventManager eman;


    /**************************************************************************
     *
     **************************************************************************/
    public JobManager() {
	init();
    }

    /**************************************************************************
     *
     **************************************************************************/
    protected void init() {

	// create manager knowing about all persistent storages
	percy = new PersistenceManager();

	// create manager knowing about all event listeners
	eman = new EventManager();

	// start cleaning thread
	CleaningThread cleaningThread = new CleaningThread();
	cleaningThread.setDaemon(true);
	cleaningThread.setName ("JobCleaning");
	cleaningThread.start();

	log.debug ("Created.");
    }

    /**************************************************************************
     * A thread that periodically scans the list of jobs in order to
     * remove them from the memory (but not to destroy them; their results
     * will still be available).
     **************************************************************************/
    class CleaningThread
	extends Thread {
	int cleaningInterval;
	public CleaningThread() {
	    cleaningInterval =
		Config.get().getInt (Config.PROP_JOBS_CLEANING_INTERVAL,
				     60 * 1000);  // every minute
	}
	
	private boolean checkforTimeouts(Job job) {
        try {
            String servicename = job.getMetadataAccessor().getServiceName();
            int timeout = Config.getInt(Config.PROP_JOBS_TIMEOUT,
                    60 * 60 * 24 * 10000,  // 10 days
                    servicename, this);
            Reporter reporter = job.getReporter();
            JobState jobstate = reporter.getState();            
            long elapsedTime = job.getElapsed();
            if (elapsedTime == -1) {
                long currentTime = System.currentTimeMillis();
                if (jobstate.getWithoutSync() == JobState.CREATED)
                    elapsedTime = currentTime - job.getCreated();
                else
                    elapsedTime = currentTime - job.getStarted();
            }
            if (elapsedTime > timeout) {
                if (jobstate.getWithoutSync() == JobState.CREATED){
                    jobstate.set(JobState.REMOVED);
                    String desc = "removed because it was not started"+
                            " before the timeout limit ("+ timeout + " msecs),"+
                            " elapsed time was ("+ elapsedTime + " msecs)";
                    jobstate.setDescription(desc);
                    log.info("job '"+job.getId()+"' "+desc);
                    reporter.updateReportResult();
                    jobs.remove(job.getId());
                    return true;
                } else {
                    job.terminate();
                    if (jobstate.isErroneousWithoutSync()){
                        // it is possible that the job has been completed 
                        // while the above terminate call is in progress
                    String desc =  "terminated by Soaplab Job Manager as" +
                    		" it was not completed"+
                            " before the timeout limit which was "+ timeout + " msecs";
                    jobstate.setDescription(desc);
                    log.info("job '"+job.getId()+"' "+desc);
                    reporter.updateReportResult();
                    }
                    return true;
                }
            }
        } catch (SoaplabException e) {
            log.warn("error while checking whether job '"+job.getId()+"' has timedout: "
                    + e.getMessage());
        }
        return false;
    }
	
	public void run() {
	    while (true) {
		// start by sleeping for a while
		try { Thread.sleep (cleaningInterval); }
		catch (InterruptedException e) {}

        if (log==null){//means web application reloaded
            System.err.printf("Job cleaning thread: null log object, "+
                    "web application was probably reloaded\n");
            return;
        }
        
        try {
            // check completed jobs
            for (String key: jobs.keySet())
            {
                Job job = jobs.get(key);
                if (job==null){
                    // job should have been explicitly removed 
                    // while the cleaning thread was iterating over the job list
                    continue;
                }
                JobState jobstate = null;
                synchronized(job){
                    Reporter reporter = job.getReporter();
                    jobstate = reporter.getState();
                    // checks whether job was already deleted while waiting for the lock
                    if(jobstate.get() == JobState.REMOVED){
                        continue;
                    }
                    if (jobstate.isCompletedWithoutSync()) {
                        try {
                            if (!job.isRecreated()){
                                reporter.setReportResult();
                                reporter.setDetailedStatusResult();
                            }
                            jobs.remove(key);
                            if (log.isInfoEnabled()) {
                                StringBuilder buf = new StringBuilder (200);
                                buf.append ("Job ");
                                buf.append (job.getId());
                                buf.append (" removed from memory ");
                                if (jobs.size() > 0) {
                                    buf.append (" [still ");
                                    buf.append (jobs.size());
                                    buf.append (" live job(s)]");
                                }
                                log.info (buf.toString());
                            }
                        } catch (SoaplabException e) {
                            // it looks helpful if we have the stack trace printed
                            SoaplabException.formatAndLog(e, log);
                            //log.error (e.getMessage());
                        }
                        continue;
                    }
                }
                if (jobstate.isInProgressWithoutSync() || jobstate.getWithoutSync() == JobState.CREATED)
                        checkforTimeouts(job);
                }
        } catch (RuntimeException e) {
            if (log==null){//means web application reloaded
                log.warn("Error while job cleaning: "+e+
                        ", null log objects suggests that web application was probably reloaded", e);
                return;
            }
            // it looks helpful if we have the stack trace printed
            SoaplabException.formatAndLog(e, log);
            //log.error(e);
        }
	    }
	}
    }

    /**************************************************************************
     * Create an instance of a "job" - which is a single service
     * invocation. The class representing (implementing) this job can
     * be defined in the service metadata. <p>
     *
     * Each job gets its "reporter" - where the job stores its
     * results, its status and where it sends events about its
     * progres. <p>
     *
     **************************************************************************/
    public Job createJob (String serviceName,
 			  MetadataAccessor metadataAccessor,
			  Map<String,Object> inputs,
			  Map<String,Object> sharedAttributes)
	throws SoaplabException
	{

	// instantiate a new Job reporter (that manages job's state
	// and results)
	Reporter reporter = new Reporter (metadataAccessor,
					  percy,
					  eman);
	// instantiate a new Job object
	String jobId = createJobId (serviceName);
	Job job = getJobInstance (jobId,              // job identity
				  metadataAccessor,   // service metadata
				  reporter,           // storing state & results
				  sharedAttributes,   // shared with other jobs
				  false);             // false = job newly created
		
	// fill it with inputs
	if (inputs != null)
	{
	    String remoteIP = (String)inputs.remove(SoaplabConstants.REMOTE_IP);
	    String client = (String)inputs.remove(SoaplabConstants.INTERFACE);

	    if(remoteIP != null)
	        job.getReporter().getState().setClientInfo(client, remoteIP);
	    
	    job.setInputs (inputs);
	}	    

	// remember the new Job object (both, in memory and in
	// persistent storages)
	jobs.put (jobId, job);
	percy.setJob (job);

	return job;
    }

    /**************************************************************************
     * Create a unique job ID. Make sure that it could be used as a
     * file name - as a favour to the {@link
     * org.soaplab.services.storage.FileStorage FileStorage} class.
     *
     * Actually, the syntax of the created ID is also used by the
     * service cleaning thread in AnalysisService class. If, for some
     * reasons, the ID syntax must be changed, make also changes there
     * (and in CmdLineClient).
     **************************************************************************/
    protected String createJobId (String serviceName) {
	return
	    ("[" + serviceName + "]" + new java.rmi.server.UID().toString())
	    .replace (':', '.').replace ('-', '_');
    }

    /**************************************************************************
     *
     **************************************************************************/
    protected String serviceNameFromJobId (String jobId) {
	int posBegToken = jobId.indexOf ("[");
	int posEndToken = jobId.indexOf ("]");
	if (posBegToken > -1 && posEndToken > posBegToken) {
	    return (jobId.substring (posBegToken + 1, posEndToken));
	} else {
	    return null;
	}
    }

    /**************************************************************************
     * Return number of jobs created by the given service that are
     * still in the memory (that means they are not yet completed, or
     * completed just recently and not been yet removed by the
     * cleaning thread).
     **************************************************************************/
    public int getCountActiveJobs (String serviceName) {
	int jobCount = 0;
	for (String jobId: jobs.keySet()) {
	    String sName = serviceNameFromJobId (jobId);
	    if (sName != null && sName.equals (serviceName))
		jobCount++;
	}
	return jobCount;
    }

    /**************************************************************************
     * Instantiate a job.
     *
     * It uses the given metadata accessor, and general configuration,
     * to find what class should be used to instantiate a job. An
     * example how to do it differently is in {@link
     * org.soaplab.admin.ExploreStorage}).
     **************************************************************************/
    protected Job getJobInstance (String jobId,
				  MetadataAccessor metadataAccessor,
				  Reporter reporter,
				  Map<String,Object> sharedAttributes,
				  boolean jobRecreated)
	throws SoaplabException {

	// find what class should represent this job
	String module = metadataAccessor.getAnalysisDef().module;
	String className = null;
	if ( StringUtils.isEmpty (module) ||
	     module.equals (AnalysisInstallation.DEFAULT_MODULE) ) {
	    className = Config.DEFAULT_JOB_FACTORY_CLASS;
	} else {
	    className = Config.getString (Config.PREFIX_SYNONYM + "." + module,
					  module,
					  metadataAccessor.getServiceName(),
					  this);
	}

	// and create it
	log.debug ("Using JobFactory class (module): " + className);
	JobFactory factory =
	    (JobFactory)ICreator.createInstance (className);
	return factory.newInstance (jobId, metadataAccessor, reporter,
				    sharedAttributes, jobRecreated);
    }

    /**************************************************************************
     * Remove and let destroy given job.
     **************************************************************************/
    public void removeJob (String jobId) {
	if (jobId != null) {
		// remove from the memory
		Job job = jobs.remove (jobId);
		if (job != null){
		    synchronized(job){
		        // checks whether job was already deleted while waiting for the lock
		        if(job.getReporter().getState().getWithoutSync() != JobState.REMOVED)
		            job.destroy();
		        // remove from persistent storages
		        try {
		            percy.removeJob (jobId);
		        }catch (SoaplabException e) {
		            // ignore
		        }
		    }
		}
	}
    }

    /**************************************************************************
     * Find and return given job. Try also the persistence manager -
     * perhaps its storage handlers have the job stored. The
     * parameters other then 'jobId' are used only if a job cannot be
     * found in the main memory and is re-created from a persistent
     * storage. <p>
     *
     * @throw SoaplabException if it cannot access any persistent
     * storage, or if such job does not exist in any of the accessible
     * ones
     **************************************************************************/
    public Job findJob (String jobId,
			String serviceName,
			MetadataAccessor metadataAccessor,
			Map<String,Object> sharedAttributes)
	throws SoaplabException {
 	if (jobId == null) {
 	    log.error ("Job manager request with a null job identifier.");
 	    throw new SoaplabException (SoaplabConstants.FAULT_NOT_FOUND);
 	}

	// first, look in the memory
	Job job = jobs.get (jobId);
	if (job != null) return job;

	// then, try persistence managers...
	JobState jobState = percy.getJobState (jobId);
	jobState.setPersistentStorage (percy);
	Reporter reporter = new Reporter (metadataAccessor, percy, eman);
	reporter.setState (jobState);
	job = getJobInstance (jobId,              // job identity
			      metadataAccessor,   // service metadata
			      reporter,           // storing state & results
			      sharedAttributes,   // shared with other jobs
			      true);              // true = job is re-created

	// ... and remember the re-created Job object in memory
	jobs.put (jobId, job);
	return job;
    }

}
