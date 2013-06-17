// Job.java
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
import org.soaplab.services.metadata.MetadataAccessor;
import org.soaplab.services.metadata.ParamDef;
import java.util.Map;
import java.io.File;

/**
 * An interface defining a job. A job represents an invocation of an
 * analysis tool. The invocation can be synchronous (blocking) or
 * asynchronous. The invocation itself was historically meant to be an
 * execution of an analysis tool but actually it can also mean
 * fetching contents of a remote web site and returning its (parsed)
 * contents as a result of such "invocation", or other things (access
 * to database, or whatever). <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: Job.java,v 1.9 2008/08/21 20:59:24 marsenger Exp $
 */

public interface Job {

    /**************************************************************************
     * Get this job ID.
     **************************************************************************/
    String getId();

    /**************************************************************************
     * Set this job ID.
     **************************************************************************/
    void setId (String jobId);

    /**************************************************************************
     * Get reporter.
     **************************************************************************/
    Reporter getReporter();

    /**************************************************************************
     * Set reporter.
     **************************************************************************/
    void setReporter (Reporter reporter);

    /**************************************************************************
     * Get metadata accessor.
     **************************************************************************/
    MetadataAccessor getMetadataAccessor();

    /**************************************************************************
     * Set metadata accessor.
     **************************************************************************/
    void setMetadataAccessor (MetadataAccessor metadataAccessor);

    /**************************************************************************
     * Set input data into this job.
     **************************************************************************/
    void setInputs (Map<String,Object> inputs)
	throws SoaplabException;

    /**************************************************************************
     * Set one input value. This is allowed only if this job was
     * created initially with empty input set (in other words it is
     * not allowed to add new inputs into already created one unless
     * it was created empty).
     **************************************************************************/
    void setInputValue (String inputName, Object value)
	throws SoaplabException;

    /**************************************************************************
     * Run it.
     **************************************************************************/
    void run() throws SoaplabException;

    /**************************************************************************
     * Block and wait for completion.
     **************************************************************************/
    void waitFor() throws SoaplabException;

    /**************************************************************************
     * Terminate this running job.
     **************************************************************************/
    void terminate() throws SoaplabException;

    /**************************************************************************
     * Job created - returns number of seconds from the BOE.
     **************************************************************************/
    long getCreated() throws SoaplabException;

    /**************************************************************************
     * Job started - returns number of seconds from the BOE.
     **************************************************************************/
    long getStarted() throws SoaplabException;

    /**************************************************************************
     * Job ended - returns number of seconds from the BOE.
     **************************************************************************/
    long getEnded() throws SoaplabException;

    /**************************************************************************
     * Job time elapsed - returns number in milliseconds. The
     * implementation may choose to return a correct value of the
     * elapsed time only after the job has been finished.
     **************************************************************************/
    long getElapsed() throws SoaplabException;

    /**************************************************************************
     * Get job status.
     **************************************************************************/
    String getStatus() throws SoaplabException;

    /**************************************************************************
     * Get the last event.
     **************************************************************************/
    String getLastEvent() throws SoaplabException;

    /**************************************************************************
     * Return all (available) results.
     **************************************************************************/
    Map<String,Object> getResults() throws SoaplabException;

    /**************************************************************************
     * Return all wanted (and available) results.
     **************************************************************************/
    Map<String,Object> getResults (String[] resultNames) throws SoaplabException;

    /**************************************************************************
     *
     *************************************************************************/
    Map<String,String>[] getResultsInfo() throws SoaplabException;

    /**************************************************************************
     * Destroy resources related to this job.
     **************************************************************************/
    void destroy();

    /**************************************************************************
     * Shared attributes are any objects that are shared by more job
     * instances. Usually, one service keeps this container and gives
     * it to its jobs. It depends on jobs what they store here.
     *************************************************************************/
    void setSharedAttributes (Map<String,Object> sharedAttributes);

    /******************************************************************************
     * Check if the 'dataRef' (which is a file name or a URL) is safe
     * for this job. It is a security measure not to allow users to
     * explore server's file system (too much). <p>
     *
     * @param dataRef is a data reference (file name, URL) to be
     * checked <p>
     *
     * @param paramDef is a metadata definition of the input parameter
     * whose data reference is being checked; an implementation can
     * use it, for example, if inputs have different policy what is
     * safe and what not (but this AbstractJob class does not use it)
     * <p>
     *
     * @return true if the data reference is considered safe to use
     ******************************************************************************/
    boolean isDataReferenceSafe (String dataRef, ParamDef paramDef);
    
    /******************************************************************************
     * Checks if the job is re-created
     *
     * @return true if the job was re-created
     ******************************************************************************/
    boolean isRecreated();

    /**************************************************************************
     * Create (unless it already exists) and return a new directory
     * which will become a working directory for this job. <p>
     *
     * @return a File instance representing a directory
     *
     * @throws SoaplabException if the directory creation failed
     **************************************************************************/
    File getJobDir() throws SoaplabException;

}
