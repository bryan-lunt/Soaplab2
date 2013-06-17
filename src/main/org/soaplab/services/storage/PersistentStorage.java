// PersistentManager.java
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

package org.soaplab.services.storage;

import org.soaplab.services.Job;
import org.soaplab.services.JobState;
import org.soaplab.share.SoaplabException;

import java.util.Map;
import java.util.Date;
import java.util.Properties;

/**
 * An interface defining how to store results, and other
 * characteristics of service invocations ("jobs"), in more persistent
 * way. Typical implementations will use a file system or a local
 * database. <p>
 *
 * Some implementations may be actually just "data consumers", or
 * "sinks", that will never return any data back. This may be used for
 * various user interfaces displaying or transforming analysis
 * results. A storage that behaves like that returns <em>true</em> from
 * the {@link #isSink isSink} method. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: PersistentStorage.java,v 1.5 2007/10/17 15:55:16 marsenger Exp $
 */

public interface PersistentStorage {

    /**************************************************************************
     * Return a (human-readable) description of this persistent
     * storage. It is intended for admin purposes, not for the Soaplab
     * end-users. <p>
     **************************************************************************/
    String getDescription();

    /**************************************************************************
     * Return names (job Ids) of all jobs still available from this
     * storage handler. <p>
     *
     * @return names in the same format as expected in other methods
     * (of this interface) requiring job IDs. Return an empty array
     * (not null) if there are no jobs stored.
     **************************************************************************/
    String[] listJobs();

    /**************************************************************************
     * This is as close to re-creating a stored job as possible. <p>
     *
     * @param jobId an identifier of a job whose state will be returned
     *
     * @return a re-created job status of the wanted job; everything
     * as read from the persistent storage
     *
     * @throws SoaplabException if the permanent storage cannot be
     * accessed, or if the given 'jobId' does not exist there
     **************************************************************************/
    JobState getJobState (String jobId)
	throws SoaplabException;

    /**************************************************************************
     * Return known properties of the given job. <p>
     *
     * @param jobId identifies the job whose properties are asked for
     *
     * @return job properties, or an empty container but never null
     * @throws SoaplabException if the permanent storage cannot be
     * accessed, or if the given 'jobId' does not exist there
     **************************************************************************/
    Properties getJobProperties (String jobId)
	throws SoaplabException;

    /**************************************************************************
     * Store properties for the given job. <p>
     *
     * @param jobId identifies the job whose properties are being saved
     *
     * @throws SoaplabException if the permanent storage cannot be
     * accessed, or if the given 'jobId' does not exist there
     **************************************************************************/
    void setJobProperties (String jobId, Properties props)
	throws SoaplabException;

    /**************************************************************************
     * Store Job's state information to the persistent storage. It
     * includes job's time properties, job state and the last
     * event. <p>
     *
     * @param job whose state is being saved
     * 
     * @throws SoaplabException if the permanent storage cannot be
     * accessed
     **************************************************************************/
    void setJob (Job job)
	throws SoaplabException;

    /**************************************************************************
     * Remove given job from this persistent storage. <p>
     *
     * @throws SoaplabException if the storage cannot be accessed
     **************************************************************************/
    void removeJob (String jobId)
	throws SoaplabException;

    /**************************************************************************
     * Remove jobs from this persistent storage whose IDs contain the
     * string specified by 'jobIdPattern'. <p>
     *
     * @param jobIdPattern is a simple pattern, not a regular
     * expression
     **************************************************************************/
    void removeJobs (String jobIdPattern);

    /**************************************************************************
     * Remove jobs from this persistent storage that were last updated
     * within given time interval. <p>
     *
     * @param fromDate begins the interval of removal
     * @param toDate finishes the interval of removal
     **************************************************************************/
    void removeJobs (Date fromDate, Date toDate);

    /**************************************************************************
     * Find in this persistent storage all results for the given job
     * and return them back. <p>
     *
     * @param jobId a job whose results to return
     *
     * @return a map whose keys are result names and values results
     * themselves
     *
     * @throws SoaplabException if the permanent storage cannot be
     * accessed, or if the given 'jobId' does not exist there
     **************************************************************************/
    Map<String,Object> getResults (String jobId)
	throws SoaplabException;

    /**************************************************************************
     * Find in this persistent storage the given result for the given
     * job and return them back. <p>
     *
     * @param jobId a job whose result to return
     * @param name of a result to be returned
     *
     * @return the wanted result, or null if 'name' result does not exist
     *
     * @throws SoaplabException if the permanent storage cannot be
     * accessed, or if the given 'jobId' does not exist there
     **************************************************************************/
    Object getResult (String jobId, String name)
	throws SoaplabException;

    /**************************************************************************
     * Return properties associated with all results from the given
     * job. <p>
     *
     * @param jobId identifies a job whose results are being
     * investigated
     *
     * @return a Map for each existing result (for the given job); the
     * keys are the result properies names - such as the type and size
     * of the result
     *
     * @throws SoaplabException if the storage cannot be accessed
     *************************************************************************/
    Map<String,String>[] getResultsInfo (String jobId)
	throws SoaplabException;

    /**************************************************************************
     * Store result in the persistent storage. <p>
     *
     * @param job where the result belongs to
     * @param name of the result (as defined in the service metadata)
     * @param result to be stored (allowed types may depend on the
     * storage handler, but reasonable effort should be made to store
     * most of the common types)
     *
     * @throws SoaplabException if the storage cannot be accessed
     **************************************************************************/
    void setResult (Job job, String name, Object result)
	throws SoaplabException;

    /**************************************************************************
     * Return 'true' if this storage allows only setting data (jobs,
     * state, results, etc.) but never returns them back. <p>
     *
     * This may be used if the "storage" is a user interface for
     * displaying job's data. For example, the HTML pages may be a
     * "persistent storage", showing results and job state. <p>
     **************************************************************************/
    boolean isSink();

}
