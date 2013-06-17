// JobState.java
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

import java.util.Hashtable;

import org.soaplab.services.events.AnalysisEvent;
import org.soaplab.services.events.EventManager;
import org.soaplab.services.events.StateChangeEvent;
import org.soaplab.services.storage.PersistentStorage;
import org.soaplab.share.SoaplabConstants;
import org.soaplab.share.SoaplabException;

/**
 * A structure defining the current status of a "job" (an analysis
 * invocation). The status also includes some time characteristics
 * (when was job created, started and when it ended), and it also
 * keeps track of the last notification event. <p>
 *
 * Note, however, that an "analysis" can be something very far from a
 * command-line oriented tool - for example, it can be a process of
 * fetching data from a remote web site. In such case a "job" is just
 * one fetch from that site. Because of this variety this class keeps
 * actually two states - a major one, which is always expressed by the
 * same set of constants (even though the set can be extended by
 * subclasses), and a minor one, which will be dependent on the nature
 * of the "job". An implementation can set an explanatory description
 * of the minor mode using method {@link #setDescription}. <p>
 *
 * The major state is an integer - in an increasing order of its
 * occurrence in the job life-cycle (e.g. {@link #JOB_RUNNING} is
 * higher than {@link #JOB_CREATED}). But these integers are used only
 * internally. For the outside world (for the services clients), there
 * are corresponding string equivalents - they are defined in the
 * {@link org.soaplab.share.SoaplabConstants}. <p>
 *
 * A usual way of creating a JobState instance is to get (in the
 * constructor) also an {@link
 * org.soaplab.services.events.EventManager EventManager}. If so than
 * the JobState can send events (using this EventManager) whenever the
 * job's state changes. <p>
 *
 * The JobState must make sure that it synchronizes its contents with
 * other instances of JobState, representing the same Job in a
 * different JVM. The real synchronization is done on the level of the
 * persistent storage, not here, but JobState must take care about
 * updating its state from the persistent storage - not always, but
 * often enough. That's why the get/set methods are paired with
 * corresponding getWithoutSync/setWithoutSave methods, defining
 * whether to do or not to do the synchronization/save. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: JobState.java,v 1.13 2011/04/06 13:14:37 mahmutuludag Exp $
 */
public class JobState {

    private static org.apache.commons.logging.Log log =
       org.apache.commons.logging.LogFactory.getLog (JobState.class);

    private static org.apache.commons.logging.Log jobrecords =
        org.apache.commons.logging.LogFactory.getLog ("JobRecords");

    // -------------------------------------------------
    // here are the constants for the job's major states
    // -------------------------------------------------

    /** Job has an unknown major state. */
    public final static int UNKNOWN = -1;

    /** Job has been created. */
    public final static int CREATED = 0;

    /** Job is running. */
    public final static int RUNNING = 1;

    /** Job has been completed, as far as one can guess, correctly. */
    public final static int COMPLETED = 2;

    /** Job has been terminated by an external request. A typical
     * external request is killing the job by a method call.
     */
    public final static int TERMINATED_BY_REQUEST = 3;

    /** Job has been terminated caused by an error. */
    public final static int TERMINATED_BY_ERROR = 4;

    /** Job has been removed. */
    public final static int REMOVED = 99;

    // for conversions between internal and external representation of
    // the job's major state
    private static final Hashtable<String,Integer> jobStates = new Hashtable<String,Integer>();
    static {
        jobStates.put (SoaplabConstants.JOB_UNKNOWN,               new Integer (UNKNOWN));
        jobStates.put (SoaplabConstants.JOB_CREATED,               new Integer (CREATED));
        jobStates.put (SoaplabConstants.JOB_RUNNING,               new Integer (RUNNING));
        jobStates.put (SoaplabConstants.JOB_COMPLETED,             new Integer (COMPLETED));
        jobStates.put (SoaplabConstants.JOB_TERMINATED_BY_REQUEST, new Integer (TERMINATED_BY_REQUEST));
        jobStates.put (SoaplabConstants.JOB_TERMINATED_BY_ERROR,   new Integer (TERMINATED_BY_ERROR));
        jobStates.put (SoaplabConstants.JOB_REMOVED,               new Integer (REMOVED));
    }

    // -------------------------------
    // this is the full state of a job
    // -------------------------------
    int majorState = UNKNOWN;
    String minorState = null;
    String description = null;

    // --------------------------------------
    // the are the job's time characteristics
    // --------------------------------------
    long created = -1;
    long started = -1;
    long ended   = -1;
    long elapsed = -1;

    // -----------------------
    // and here is the rest...
    // -----------------------
    protected PersistentStorage percy = null;
    protected EventManager eman = null;
    protected AnalysisEvent lastEvent = new AnalysisEvent ("");
    protected Job job = null;

    private String remoteIP;
    
    /* type of the client, one of the following types
     * - spinet
     * - axis1 (generic interface)
     * - jaxws (generic interface)
     * - jaxwstyped
     */    
    private String client;

    /**************************************************************************
     * Constructor setting a major state (without saving it to any
     * persistent storage - because there is no such storage given).
     **************************************************************************/
    public JobState (int majorState) {
	set (majorState);
    }

    /**************************************************************************
     * A usual constructor, setting a major state, and remembering an
     * Event Manager that will be used to record and broadcast all job
     * state changes, and a persistent manager where this class will
     * be reporting job state changes.
     **************************************************************************/
     public JobState (int majorState,
		      PersistentStorage percy,
		      EventManager eman) {
 	this.percy = percy;
 	this.eman = eman;
 	set (majorState);
     }

    /**************************************************************************
     * Set a job whose state is maintained by this instance.
     **************************************************************************/
    public void setJob (Job job) {
	this.job = job;
    }

    /**************************************************************************
     * Set a persistent storage where this job state will be reporting
     * job state changes.
     **************************************************************************/
    public void setPersistentStorage (PersistentStorage percy) {
	this.percy = percy;
    }

    /**************************************************************************
     * A utility method converting a stringified version of a job
     * state to an instance of a JobState.
     **************************************************************************/
    public static JobState checkAndGetState (String jobState) {
        return new JobState (GenUtils.checkAndGetStr2Int (jobState, jobStates));
    }

    /**************************************************************************
     * Return a major state.
     **************************************************************************/
    public int get() {
	synchronizeState();
	return getWithoutSync();
    }

    public int getWithoutSync() {
	return majorState;
    }

    /**************************************************************************
     * Return a stringified version of the major state.
     **************************************************************************/
    public String getAsString() {
	return GenUtils.inverseMapping (get(), jobStates);
    }

    public String getAsStringWithoutSync() {
	return GenUtils.inverseMapping (getWithoutSync(), jobStates);
    }

    /**************************************************************************
     * Set the new major state - but only if the new state is a valid
     * one. <p>
     *
     * Also, it does not allow to set "earlier" states if a "later"
     * one is already in place. For example, if the current state
     * indicates that it was terminated ({@link
     * #TERMINATED_BY_REQUEST} or {@link #TERMINATED_BY_ERROR}) it
     * does not allow to set the major state to {@link #RUNNING} or {@link
     * #COMPLETED}. <p>
     *
     * Note that there is no "non-synchronized" method to set a new
     * job state: this is always synchronized, meaning that the new
     * job state is always saved to the persistent storage (and the
     * persistent storage can reject it if it has already a newer
     * state). [ Well, not always, after all: if there is no
     * persistence manager known, we cannot save it, can we? ] <p>
     *
     **************************************************************************/
    public void set (int majorState) {
	if ( jobStates.containsValue (new Integer (majorState)) && 
	     isValidChange (majorState) ) {
	    String previousState = toString();
	    this.majorState = majorState;

	    // set times
	    switch (majorState) {
	    case CREATED:
		setCreatedWithoutSave (System.currentTimeMillis());
		break;
	    case COMPLETED:
	    case TERMINATED_BY_REQUEST:
	    case TERMINATED_BY_ERROR:
		setEndedWithoutSave (System.currentTimeMillis());
		break;
	    case RUNNING:
		setStartedWithoutSave (System.currentTimeMillis());
		break;
	    }

	    // broadcast the state change using an Event Manager
	    StateChangeEvent event =
		new StateChangeEvent ((job == null ? null : job.getId()),
				      null, previousState, toString());
	    setLastEventWithoutSave (event);
	    if (eman != null && majorState != UNKNOWN)
		eman.handleStateChangeEvent (event);

	    // tell also the persistent manager to update this job
	    saveState();
 	}
    }

    /**************************************************************************
     * Tell the persistent manager to update this job.
     **************************************************************************/
    protected void saveState() {
	if (job != null && percy != null) {
	    try {
		percy.setJob (job);
	    } catch (SoaplabException e) {
		log.error ("Cannot save JobState: " + e.getMessage());
	    }
	}
    }

    /**************************************************************************
     * Set both (major and minor/detailed) states in one go. Only a
     * convenient method.
     **************************************************************************/
    public void set (int majorState, String minorState) {
	setDetailedWithoutSave (minorState);
	set (majorState);
    }

    /**************************************************************************
     * Return a minor state.
     **************************************************************************/
    public String getDetailed() {
	synchronizeState();
	return getDetailedWithoutSync();
    }

    public String getDetailedWithoutSync() {
	if (minorState != null)
	    return minorState;
	if (isCompletedWithoutSync())
	    return (isErroneousWithoutSync() ? "-1" : "0");
	return null;
    }

    /**************************************************************************
     * Set a minor state.
     **************************************************************************/
    public void setDetailed (String minorState) {
	setDetailedWithoutSave (minorState);
	saveState();
    }

    public void setDetailedWithoutSave (String minorState) {
	this.minorState = minorState;
    }

    /**************************************************************************
     * Return a description of this state (related usually to the
     * minor (detailed) state because the major state is standardized
     * enough by the pre-defined constants).
     **************************************************************************/
    public String getDescription() {
	synchronizeState();
	return getDescriptionWithoutSync();
    }

    public String getDescriptionWithoutSync() {
	return description;
    }

    /**************************************************************************
     * Set a description for this state (again, related usually to the
     * minor (detailed) state).
     **************************************************************************/
    public void setDescription (String description) {
	setDescriptionWithoutSave (description);
	saveState();
    }

    public void setDescriptionWithoutSave (String description) {
	this.description = description;
    }

    /**************************************************************************
     * Return true if the job has been completed (successfully or
     * erroneously).
     **************************************************************************/
    public boolean isCompleted() {
	synchronizeState();
	return isCompletedWithoutSync();
    }

    public boolean isCompletedWithoutSync() {
	return (isSuccessfulWithoutSync() || isErroneousWithoutSync());
    }

    /**************************************************************************
     * Return true if the job has been completed
     * successfully. 'Success' is based on the job major status, not
     * on other conditions, such as the job exit code (which may be
     * part of the minor/detailed status).
     **************************************************************************/
    public boolean isSuccessful() {
	synchronizeState();
	return isSuccessfulWithoutSync();
    }

    public boolean isSuccessfulWithoutSync() {
	return (getWithoutSync() == COMPLETED);
    }

    /**************************************************************************
     * Return true if the job has been terminated, either by an error
     * or by a user request.
     **************************************************************************/
    public boolean isErroneous() {
	synchronizeState();
	return isErroneousWithoutSync();
    }

    public boolean isErroneousWithoutSync() {
	return (getWithoutSync() == TERMINATED_BY_ERROR ||
		getWithoutSync() == TERMINATED_BY_REQUEST);
    }

    /**************************************************************************
     * Return true if the job is still running.
     **************************************************************************/
    public boolean isInProgress() {
	synchronizeState();
	return isInProgressWithoutSync();
    }

    public boolean isInProgressWithoutSync() {
	return (getWithoutSync() == RUNNING);
    }

    /*********************************************************************
     *
     *       Set/get methods for job's time characteristics
     *
     ********************************************************************/
    public long getCreated() {
	synchronizeState();
	return getCreatedWithoutSync();
    }
    public long getCreatedWithoutSync() {
	return created;
    }

    public long getStarted() {
	synchronizeState();
	return getStartedWithoutSync();
    }
    public long getStartedWithoutSync() {
	return started;
    }

    public long getEnded() {
	synchronizeState();
	return getEndedWithoutSync();
    }
    public long getEndedWithoutSync() {
	return ended;
    }

    public long getElapsed() {
	synchronizeState();
	return getElapsedWithoutSync();
    }
    public long getElapsedWithoutSync() {
	if (elapsed == -1 && started > -1 && ended > started)
	    elapsed = ended - started;
	return elapsed;
    }

    public void setCreated (long value) {
	setCreatedWithoutSave (value);
	saveState();
    }
    public void setCreatedWithoutSave (long value) {
	created = value;
    }

    public void setStarted (long value) {
	setStartedWithoutSave (value);
	saveState();
    }
    public void setStartedWithoutSave (long value) {
	started = value;
    }

    public void setEnded (long value) {
	setEndedWithoutSave (value);
	saveState();
    }
    
    public void setClientInfo(String client, String remoteIP)
    {
        this.client = client;
        this.remoteIP = remoteIP;
    }
    

    public void setEndedWithoutSave (long value)
    {
        ended = value;
        
        if(job != null) //means constructed by Reporter
        {
            try
            {
                String service = job.getMetadataAccessor().getServiceName();
                jobrecords.debug(
                        "rh="+remoteIP+
                        " cl="+client+
                        " tn="+service.substring(service.lastIndexOf('.')+1)+
                        " ji="+ job.getId()+
                        " success="+isSuccessfulWithoutSync()+
                        " ti="+getElapsedWithoutSync()
                );

            }
            catch (SoaplabException e)
            {
                log.error("Problem with writing job records: " +
                        e.getMessage());
            }
        }

    }


    /*********************************************************************
     * Set/get the last event - what happened with this job most
     * recently. If there is no last event, return an empty one.
     ********************************************************************/
    public AnalysisEvent getLastEvent() {
	synchronizeState();
	return getLastEventWithoutSync();
    }

    public AnalysisEvent getLastEventWithoutSync() {
	return lastEvent;
    }

    public void setLastEvent (AnalysisEvent event) {
	setLastEventWithoutSave (event);
	saveState();
    }
    public void setLastEventWithoutSave (AnalysisEvent event) {
	if (event != null)
	    lastEvent = event;
    }

    /*********************************************************************
     * The subclasses can influence the result by writing their own
     * method formatDetailedState().
     ********************************************************************/
    public String toString() {
	String strState = getAsStringWithoutSync();
	return (getDetailedWithoutSync() == null
		? strState
		: strState + " (" + formatDetailedState() + ")");
    }

    public String formatDetailedState() {
	String minSta = getDetailedWithoutSync();
	if (minSta == null) return null;
	if (getDescriptionWithoutSync() == null)
	    return minSta;
	else
	    return minSta + ": " + getDescriptionWithoutSync();
    }

    /**************************************************************************
     * Return true only if the change from the current major state to
     * the 'newMajorState' is allowed.
     **************************************************************************/
    public boolean isValidChange (int newMajorState) {
	if (getWithoutSync() > RUNNING) return false;
	return getWithoutSync() < newMajorState;
    }

    /**************************************************************************
     * Read the last stored state from a persistent storage (if you
     * can - which means if you have an access there) and update this
     * instance from it. But be careful not to update if this instance
     * has a more recent state (this can be checked only for the major
     * state and for the last event). <p>
     *
     * If there is an error accessing the persistent storage, just log
     * it.
     **************************************************************************/
    protected void synchronizeState() {
	    // read the latest state from a persistent storage
	    try {
	        synchronized(job){
		JobState lastSavedState = percy.getJobState (job.getId());
		if (isValidChange (lastSavedState.getWithoutSync())) {
		    // simple cloning
		    this.majorState = lastSavedState.getWithoutSync();
		    this.minorState = lastSavedState.getDetailedWithoutSync();
		    this.description = lastSavedState.getDescriptionWithoutSync();
		    this.created = lastSavedState.getCreatedWithoutSync();
		    this.started = lastSavedState.getStartedWithoutSync();
		    this.ended = lastSavedState.getEndedWithoutSync();
		    this.elapsed = lastSavedState.getElapsedWithoutSync();

		    AnalysisEvent lastSavedEvent = lastSavedState.getLastEventWithoutSync();
		    if (lastEvent.isOlderThan (lastSavedEvent))
			lastEvent = lastSavedEvent;
		}
	          }
	    } catch (SoaplabException e) {
		log.error ("Cannot synchronize JobState: " + e.getMessage());
	    }
	}
}
