// Analysis.java
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

package org.soaplab.share;

/**
 * An interface to a service (usually a web service, but nothing
 * precludes to have, for example, a REST implementtaion of such
 * service), accessing and controlling an analysis. <p>
 *
 * An analysis is usually a remote program, mostly a command-line
 * tool, but it can also be fetching a web site, or starting a local
 * Java class (providing, for example, some computation). <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: Analysis.java,v 1.7 2010/08/06 10:41:55 marsenger Exp $
*/

public interface Analysis {

    // 
    // Methods dealing with metadata: describing an analysis
    //

    /**
     * Return an XML string containing metadata describing this
     * analysis service. Its format complies with the <a
     * href="http://soaplab.sourceforge.net/soaplab2/SoaplabAnalysis.dtd">SoaplabAnalysis.dtd</a>. <p>
     *
     * @throws SoaplabException if communication with the service fails */
    String describe()
	throws SoaplabException;

    /**
     * Return information about all possible data inputs. Each element
     * of the returned array is a Map describing one data input (it
     * does not contain, however, the input data themselves). <p>
     *
     * Each Map element should have at least keys {@link SoaplabConstants#INPUT_NAME name}
     * and {@link SoaplabConstants#INPUT_TYPE type}
     * defining input data name and its Java type. An implementation can
     * choose to provide more characteristics. Typically they are:
     * <P>
     * <table border=1 cellpadding=5>
     * <tr><th>Key</th><th>Type</th><th>Note</th></tr>
     * <tr>
     * <td>{@link SoaplabConstants#INPUT_NAME name}</td> <td>String</td>
     * <td>
     * A name of analysis data input.
     * The input names are needed when a job is created and fed with the input data
     * (see methods {@link #createJob} or {@link #runAndWaitFor}).
     * </td></tr>
     * <tr>
     * <td>{@link SoaplabConstants#INPUT_TYPE type}</td> <td>String</td>
     * <td>
     * A type of this data input. Expressed as Java type (e.g. <em>String</em>,
     * or <em>byte[][]</em>).
     * </td></tr>
     * <tr>
     * <td>{@link SoaplabConstants#INPUT_ALLOWED_VALUES allowed_values}</td> <td>String[]</td>
     * <td>
     * A list of possible (allowed, expected) values for this data input.
     * (Note that the values are here always as Strings. This may be rectify later.)
     * </td></tr>
     * <tr>
     * <td>{@link SoaplabConstants#INPUT_MANDATORY mandatory}</td> <td>String</td>
     * <td>
     * The <em>true</em> is this data input must be present when the analysis starts.
     * Otherwise there is <em>false</em> here, or this key is missing at all.
     * </td></tr>
     * <tr>
     * <td>{@link SoaplabConstants#INPUT_DEFAULT_VALUE default}</td> <td>String</td>
     * <td>
     * A default value used when no data given for this data input.
     * (The same note applies as for <em>allowed_values</em> above.)
     * </td></tr>
     * </table>
     *<P>
     *
     * Additionally, some keys may be terms from an ontology defining
     * semantics of this input. For Soaplab implementation, typically
     * such keys have format <tt>EDAM:NNN</tt> indicating the usage of
     * the <a href="http://edamontology.sourceforge.net/"
     * target="_blank">EDAM ontology</a>.
     *
     *<P>
     * This is just a convenient method retrieving the same data (but only part of it)
     * as method {@link #describe} but without need to parse any XML stream.
     *<P>
     * @throws SoaplabException if communication with the service fails
     */
    SoaplabMap[] getInputSpec()
	throws SoaplabException;

    /**
     * Return information about all possible results. Each element of
     * the returned Map describes one result (it does not contain,
     * however, the result itself).
     *<P>
     * Each Map element should have at least keys {@link SoaplabConstants#RESULT_NAME name}
     * and {@link SoaplabConstants#RESULT_TYPE type}
     * defining result name and its Java type. An implementation can
     * choose to provide more characteristics. Typically they are:
     * <P>
     * <table border=1 cellpadding=5>
     * <tr><th>Key</th><th>Type</th><th>Note</th></tr>
     * <tr>
     * <td>{@link SoaplabConstants#RESULT_NAME name}</td> <td>String</td>
     * <td>
     * A name of analysis result.
     * The result names are needed when asking a job to retrieve output data
     * (see method {@link #getResults}).
     * </td></tr>
     * <tr>
     * <td>{@link SoaplabConstants#RESULT_TYPE type}</td> <td>String</td>
     * <td>
     * A type of this result. Expressed as Java type (e.g. <em>String</em>,
     * or <em>byte[][]</em>).
     * </td></tr>
     * </table>
     *<P>
     *
     * Additionally, some keys may be terms from an ontology defining
     * semantics of this result. For Soaplab implementation, typically
     * such keys have format <tt>EDAM:NNN</tt> indicating the usage of
     * the <a href="http://edamontology.sourceforge.net/"
     * target="_blank">EDAM ontology</a>.
     *<P>
     *
     * This is just a convenient method retrieving the same data (but only part of it)
     * as method {@link #describe} but without need to parse any XML stream.
     *<P>
     * The result names can be used to retrieve only some, named, results from
     * a job representing an execution of this analysis
     * (see method {@link #getResults}).
     *<P>
     * @throws SoaplabException if communication with the service fails
     */
    SoaplabMap[] getResultSpec()
	throws SoaplabException;

    /**
     * Return attributes describing this analysis.
     *<P>
     * Some attribute keys may be terms from an ontology defining
     * semantics of this analysis. For Soaplab implementation, typically
     * such keys have format <tt>EDAM:NNN</tt> indicating the usage of
     * the <a href="http://edamontology.sourceforge.net/"
     * target="_blank">EDAM ontology</a>.
     *<P>
     * This is just a convenient method retrieving the same data (but only part of it)
     * as method {@link #describe} but without need to parse any XML stream.
     *<P>
     * @return a hash table with the following keys (some may be missing, however):
     *          type, name, supplier, version, installation, description;
     *          the implementation can also add some keys
     * @throws SoaplabException if communication with the service fails
     */
    SoaplabMap getAnalysisType()
	throws SoaplabException;


    // 
    // Methods controlling an execution of this analysis
    //

    /**
     * Prepare a job with given input data but do not start it.  It
     * returns a job identifier which can be used by other methods
     * (such as {@link #run(String) run}) to start and to control the
     * job.
     *
     * <a name="inputs">
     * <h5>How to specify input data</h5>
     *
     * Generally speaking, each data input is a name/value pair.  The
     * name identifies uniquely each data input. The available names
     * can be retrieved using method {@link #getInputSpec}.  The
     * values are any objects - but an implementation may limit the
     * types which are recognized.
     * <p>
     * @throws SoaplabException if communication with the service fails,
     *         or more specifically
     *         (see <a href="{@docRoot}/overview-summary.html#faults">defining exceptions</a>) using:
     *         <dl>
     *           <dt>fault string {@link SoaplabConstants#FAULT_UNKNOWN_NAME} if there is
     *               an unrecognized input name
     *           <dt>fault string {@link SoaplabConstants#FAULT_NOT_VALID_INPUTS} if
     *               the input data are invalid (e.g. they do not follow
     *               some inter-data dependency conditions)
     *         </dl>
     * @return a job ID
     */
    String createJob (SoaplabMap inputs)
	throws SoaplabException;

    /**
     * Start an underlying analysis and return immediately, without
     * waiting for its completion. It uses data which were sent (and
     * possibly validated) by method {@link #createJob}. With the same
     * <tt>jobID</tt> it can be run only once. It raises a
     * NotRunnableSoaplabException if there is an attempt to start it
     * more than once.
     *<p>
     * @throws SoaplabException if communication with the service fails,
     *         or more specifically
     *         (see <a href="{@docRoot}/overview-summary.html#faults">defining exceptions</a>) using:
     *         <dl>
     *           <dt>fault string {@link SoaplabConstants#FAULT_NOT_FOUND} if the given job ID
     *               does not exist
     *           <dt>fault string {@link SoaplabConstants#FAULT_NOT_RUNNABLE}
     *               if the same job was alredy executed, or
     *               if the data given previously by {@link #createJob}
     *                  do not exist or are not accessible anymore
     *         </dl>
     */
    void run (String jobID)
	throws SoaplabException;

    /**
     * The same as {@link #run} with addition of the
     * channel notification negotiation.
     * See <a href="{@docRoot}/overview-summary.html#notify">details here</a>.
     *<p>
     * @throws SoaplabException as in {@link #run}
     *         plus
     *         (see <a href="{@docRoot}/overview-summary.html#faults">defining exceptions</a>) using:
     *         <dl>
     *           <dt>fault string {@link SoaplabConstants#FAULT_NOTIFY_UNACCEPTED}
     *               if the server does not agree on using the proposed
     *               notification channel
     *         </dl>
     */
    void runNotifiable (String jobID, String notificationDescriptor)
	throws SoaplabException;

    /**
     * A convenient combination of methods {@link #createJob} and
     * {@link #run(String) run}. See
     * <a href="#inputs">how to specify input data</a>.
     *<p>
     * @throws SoaplabException if communication with the service fails,
     *         or more specifically
     *         (see <a href="{@docRoot}/overview-summary.html#faults">defining exceptions</a>) using:
     *         <dl>
     *           <dt>fault string {@link SoaplabConstants#FAULT_UNKNOWN_NAME} if there is
     *               an unrecognized input name
     *           <dt>fault string {@link SoaplabConstants#FAULT_NOT_VALID_INPUTS} if
     *               the input data are invalid (e.g. they do not follow
     *               some inter-data dependency conditions)
     *         </dl>
     * @return a job ID
     */
    String createAndRun (SoaplabMap inputs)
	throws SoaplabException;

    /**
     * The same as {@link #createAndRun} with addition of the
     * channel notification negotiation.
     * See <a href="{@docRoot}/overview-summary.html#notify">details here</a>.
     *<p>
     * @throws SoaplabException as in {@link #createAndRun}
     *         plus
     *         (see <a href="{@docRoot}/overview-summary.html#faults">defining exceptions</a>) using:
     *         <dl>
     *           <dt>fault string {@link SoaplabConstants#FAULT_NOTIFY_UNACCEPTED}
     *               if the server does not agree on using the proposed
     *               notification channel
     *         </dl>
     */
    String createAndRunNotifiable (SoaplabMap inputs, String notificationDescriptor)
	throws SoaplabException;

    /**
     * Block the client until the underlying running analysis is
     * completed. The running job is identified by the given job ID.
     *<P>
     * <a name="waiting">
     * Note that this method keeps an open connection with the server
     * until the job is finished which may be for quite a long time.
     * Therefore, an implementation may refuse this request by throwing
     * an exception. Also even if the implementation allows there may
     * be some time-outs set by other components (e.g. a proxy server)
     * which may make this request unreliable.
     * See <a href="{@docRoot}/overview-summary.html#notify">notification</a> for an alternative.
     *<p>
     * @throws SoaplabException if communication with the service fails,
     *         or more specifically
     *         (see <a href="{@docRoot}/overview-summary.html#faults">defining exceptions</a>) using:
     *         <dl>
     *           <dt>fault string {@link SoaplabConstants#FAULT_NOT_FOUND} if the given
     *               job ID does not exist
     *           <dt>fault string {@link SoaplabConstants#FAULT_NOT_IMPLEMENTED} if the
     *               implementation refuses this request
     *         </dl>
     */
    void waitFor (String jobID)
	throws SoaplabException;

    /**
     * Block the client until the underlying running analysis is
     * completed and return immediately all results. The running job
     * is identified by the given job ID.
     *<P>
     * This is designed for a full state-less server implementation -
     * however, see the <a href="#waiting">note about dangers</a>.
     *<P>
     * Note that by using this method there is no way how to get
     * additional information usually passed by a notification channel
     * (e.g. events happenning during the job life-cycle). A service
     * implementation should consider to pass some of those as an
     * additional result (e.g. termination status/code).
     *<p>
     * @throws SoaplabException if communication with the service fails,
     *         or more specifically
     *         (see <a href="{@docRoot}/overview-summary.html#faults">defining exceptions</a>) using:
     *         <dl>
     *           <dt>fault string {@link SoaplabConstants#FAULT_UNKNOWN_NAME} if there is
     *               an unrecognized input name
     *           <dt>fault string {@link SoaplabConstants#FAULT_NOT_VALID_INPUTS} if
     *               the input data are invalid (e.g. they do not follow
     *               some inter-data dependency conditions)
     *           <dt>fault string {@link SoaplabConstants#FAULT_NOT_IMPLEMENTED} if the
     *               implementation refuses this request
     *         </dl>
     * @return all results (see {@link #getResults}) */
    SoaplabMap runAndWaitFor (SoaplabMap inputs)
	throws SoaplabException;

    /**
     * Terminate the running analysis, identified by its job ID.
     *<P>
     * @throws SoaplabException if communication with the service fails,
     *         or more specifically
     *         (see <a href="{@docRoot}/overview-summary.html#faults">defining exceptions</a>) using:
     *         <dl>
     *           <dt>fault string {@link SoaplabConstants#FAULT_NOT_FOUND} if the given
     *               job ID does not exist
     *           <dt>fault string {@link SoaplabConstants#FAULT_NOT_RUNNING} if the given
     *               job was not yet started (note that this exception
     *               <em>is not</em> raised when the job had been already
     *               finished - from any reasons)
     *           <dt>fault string {@link SoaplabConstants#FAULT_NOT_TERMINATED} if the given
     *               analysis is not interruptible from some reason
     *         </dl>
     */
    void terminate (String jobID)
	throws SoaplabException;

    // 
    // Methods giving analysis status and results
    //

    /**
     * Return the last event which happened to the given job.
     * It returns an XML string describing the event (see
     * <a href="{@docRoot}/overview-summary.html#events">events description</a>).
     *<P>
     * @throws SoaplabException if communication with the service fails
     *         or more specifically
     *         (see <a href="{@docRoot}/overview-summary.html#faults">defining exceptions</a>) using:
     *         <dl>
     *           <dt>fault string {@link SoaplabConstants#FAULT_NOT_FOUND} if the given
     *               job ID does not exist
     *         </dl>
     */
    String getLastEvent (String jobID)
	throws SoaplabException;

    /**
     * Return an XML string describing location and available protocol
     * (or even protocols) of a notification channel which will have
     * messages regarding given job.
     * More details in
     * <a href="{@docRoot}/overview-summary.html#notify">notification description</a>.
     *<P>
     * @throws SoaplabException if communication with the service fails
     *         or more specifically
     *         (see <a href="{@docRoot}/overview-summary.html#faults">defining exceptions</a>) using:
     *         <dl>
     *           <dt>fault string {@link SoaplabConstants#FAULT_NOT_FOUND} if the given
     *               job ID does not exist
     *         </dl>
     */
    String getNotificationDescriptor (String jobID)
	throws SoaplabException;

    /**
     * Return a string describing the current job status.
     * At least the following values are recognized:
     * <ul>
     *  <li> {@link SoaplabConstants#JOB_CREATED CREATED} - job has been created but not yet executed
     *  <li> {@link SoaplabConstants#JOB_RUNNING RUNNING} - job is running
     *  <li> {@link SoaplabConstants#JOB_COMPLETED COMPLETED} - job has completed execution
     *  <li> {@link SoaplabConstants#JOB_TERMINATED_BY_REQUEST TERMINATED_BY_REQUEST} - job was terminated by user request
     *  <li> {@link SoaplabConstants#JOB_TERMINATED_BY_ERROR TERMINATED_BY_ERROR} - job was terminated due to an error
     * </ul>
     *<P>
     * @throws SoaplabException if communication with the service fails
     *         or more specifically
     *         (see <a href="{@docRoot}/overview-summary.html#faults">defining exceptions</a>) using:
     *         <dl>
     *           <dt>fault string {@link SoaplabConstants#FAULT_NOT_FOUND} if the given
     *               job ID does not exist
     *         </dl>
     */
    String getStatus (String jobID)
	throws SoaplabException;

    /**
     * Returning job's time characteristics. The recognized map's keys
     * are: {@link SoaplabConstants#TIME_CREATED}, {@link
     * SoaplabConstants#TIME_STARTED}, {@link
     * SoaplabConstants#TIME_ENDED}, {@link
     * SoaplabConstants#TIME_ELAPSED}. The values are in milliseconds
     * from the beginning of the Epoch (1.1.1971) - even though they
     * are passed as type String. <p>
     *
     * @throws SoaplabException if communication with the service fails
     *         or more specifically
     *         (see <a href="{@docRoot}/overview-summary.html#faults">defining exceptions</a>) using:
     *         <dl>
     *           <dt>fault string {@link SoaplabConstants#FAULT_NOT_FOUND} if the given
     *               job ID does not exist
     *         </dl>
     */
    SoaplabMap getCharacteristics (String jobID)
	throws SoaplabException;


    SoaplabMap[] getResultsInfo (String jobId)
	throws SoaplabException;

    /**
     * Return all (available) results from the given job.
     * <a name="results">
     * <h5>How to deal with results</h5>
     *
     * Generally speaking, each result is a name/value pair.  The
     * name identifies uniquely each result. The available names
     * can be retrieved using method {@link #getResultSpec}.  The
     * values are any objects - but an implementation may limit the
     * types which are recognized.
     * <p>
     * @throws SoaplabException if communication with the service fails
     *         or more specifically
     *         (see <a href="{@docRoot}/overview-summary.html#faults">defining exceptions</a>) using:
     *         <dl>
     *           <dt>fault string {@link SoaplabConstants#FAULT_NOT_FOUND} if the given
     *               job ID does not exist
     *         </dl>
     */
    SoaplabMap getResults (String jobID)
	throws SoaplabException;

    /**
     * Return named (and available) results from the given job.
     * See <a href="#results">how to deal with results</a>.
     *<P>
     * @throws SoaplabException if communication with the service fails
     *         or more specifically
     *         (see <a href="{@docRoot}/overview-summary.html#faults">defining exceptions</a>) using:
     *         <dl>
     *           <dt>fault string {@link SoaplabConstants#FAULT_NOT_FOUND} if the given
     *               job ID does not exist
     *           <dt>fault string {@link SoaplabConstants#FAULT_UNKNOWN_NAME} if there
     *               is an unrecognized result name
     *         </dl>
     */
    SoaplabMap getSomeResults (String jobID, String[] resultNames)
	throws SoaplabException;


    //
    // Methods for releasing resources
    //

    /**
     * Release related resources, including results.
     * A client uses this method to suggest that she will not need
     * anymore any data from the given job. However, implementation
     * can decide both to ignore this request, or to release them
     * even earlier.
     *<P>
     * @throws SoaplabException if communication with the service fails
     *         or more specifically
     *         (see <a href="{@docRoot}/overview-summary.html#faults">defining exceptions</a>) using:
     *         <dl>
     *           <dt>fault string {@link SoaplabConstants#FAULT_NOT_FOUND} if the given
     *               job ID does not exist
     *         </dl>
     */
    void destroy (String jobID)
	throws SoaplabException;

}
