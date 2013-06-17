// BatchTestClient.java
//
// Created: January 2007
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

package org.soaplab.clients;

import org.soaplab.share.SoaplabConstants;
import org.soaplab.share.SoaplabMap;
import org.soaplab.services.Config;

import org.tulsoft.tools.BaseCmdLine;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.time.DurationFormatUtils;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Iterator;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * A command-line tool that calls in parallel more services, feeding
 * them by test inputs, and reporting on successes and failures. <p>
 *
 * Start it with <tt>-help</tt> to see available options. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: BatchTestClient.java,v 1.9 2008/11/24 16:12:56 mahmutuludag Exp $
 */
public class BatchTestClient
    extends CmdLineHelper {

    private static org.apache.commons.logging.Log log =
       org.apache.commons.logging.LogFactory.getLog (BatchTestClient.class);

    public static final String PREFIX_SERVICE_TO_TEST  = "sn.";

    // names of the properties in the resulting report
    public static final String REPORT_SERVICE_NAME  = "service.name";
    public static final String REPORT_INPUT_LINE    = "input.line";
    public static final String REPORT_ERROR_MESSAGE = "error.msg";
    public static final String REPORT_STACK_TRACE   = "stack.trace";
    public static final String REPORT_JOB_ID        = "job.id";
    public static final String REPORT_JOB_STATUS    = "job.status";
    public static final String REPORT_ELAPSED_TIME  = "elapsed.time";
    public static final String REPORT_LAST_EVENT    = "last.event";
    public static final String REPORT_RESULTS_INFO  = "results.info";

    /*************************************************************************
     *
     * Entry point...
     *
     *************************************************************************/
    public static void main (String [] args) {
	try {
	    BaseCmdLine cmd = getCmdLine (args, BatchTestClient.class);

	    // service location and protocol parameters
	    ServiceLocator mainLocator = InputUtils.getServiceLocator (cmd);

	    // file(s) with the testing data (a list of tested
	    // services and their inputs)
	    String[] batchFiles = null;
	    String batchFile = cmd.getParam ("-batchfile");
	    if (batchFile != null) {
		// take it from the command-line
		batchFiles = new String[] { batchFile };
	    } else {
		// take it from the client configuration file
		batchFiles =
		    ClientConfig.get().getStringArray (ClientConfig.PROP_BATCH_TEST_FILE);
	    }
	    if (batchFiles == null || batchFiles.length == 0) {
		log.error ("A file with a list of service to test must be given. " +
			   "Use '-batchfile' or a property '" +
			   ClientConfig.PROP_BATCH_TEST_FILE + "'.");
		System.exit (1);
	    }

	    // other arguments
	    boolean tableReport =
		( cmd.hasOption ("-table") ||
		  ClientConfig.isEnabled (ClientConfig.PROP_BATCH_REPORT_TABLE, false) );
	    boolean keepResults  =
		( cmd.hasOption ("-keep") ||
		  ClientConfig.isEnabled (ClientConfig.PROP_BATCH_KEEP_RESULTS, false) );
	    int maxThreads = -1;
	    String strMaxThreads = cmd.getParam ("-maxthreads");
	    if (strMaxThreads == null)
		maxThreads = ClientConfig.getInt (ClientConfig.PROP_BATCH_MAX_THREADS, 25);
	    else
		maxThreads = getInt (strMaxThreads);
	    if (maxThreads < 0)
		maxThreads = 0;
	    boolean oneByOne  = (maxThreads == 1);

	    // get a list of available services
	    // (for validation purposes later)
	    Set<String> services = new HashSet<String>();
	    for (String name: new SoaplabBaseClient (mainLocator)
		     .getAvailableAnalyses()) {
		services.add (name);
	    }

	    // loop and do it...
 	    List<Properties> reports = Collections.synchronizedList (new ArrayList<Properties>());
	    List<Thread> threads = Collections.synchronizedList (new ArrayList<Thread>());
	    int countNotAvailable = 0;

	    title ("Progress");
	    for (String file: batchFiles) {

		log.info ("Using batch file " + file);

		// treat each batch file as a property configuration
		// file - together with a usual Soaplab Client
		// configuration file; this allows handling
		// substitutions of referenced properties, etc.
		CompositeConfiguration cfg = new CompositeConfiguration();
		cfg.addConfiguration (ClientConfig.get());
		cfg.addConfiguration (Config.get());
		try {
		    cfg.addConfiguration (new PropertiesConfiguration (file));
		} catch (ConfigurationException e) {
		    log.error ("Loading batch file from '" +
			       file + "' failed: " +
			       e.getMessage());
		    continue;
		}

		//
		for (Iterator it = cfg.getKeys(); it.hasNext(); ) {
		    String propertyName = (String)it.next();
		    if (! propertyName.startsWith (PREFIX_SERVICE_TO_TEST))
			continue;
		    String serviceName =
			StringUtils.removeStart (propertyName,
						 PREFIX_SERVICE_TO_TEST);
		    if (! services.contains (serviceName)) {
// 			log.warn (serviceName + " is not available for testing");
			countNotAvailable += 1;
			continue;
		    }
 		    String[] inputs = cfg.getStringArray (propertyName);
		    for (String input: inputs) {
			MyLocator locator = new MyLocator (serviceName, mainLocator);
			locator.enableKeepResults (keepResults);
			locator.setInputLine (input);
			if (oneByOne) {
			    // sequential invocation
			    qmsg (String.format ("%-50s", "Running " + serviceName + "... "));
			    Properties report = callService (locator, reports);
			    qmsgln ("finished: " + report.getProperty (REPORT_JOB_STATUS));

			} else {
			    // do it in parallel
			    startService (threads, locator, reports);
			    if (maxThreads > 0) {
				// limit the number of threads
				// (just wait for some to finish)
				while (threads.size() >= maxThreads) {
				    try { Thread.sleep (1000); } catch (Exception e) {}
				}
			    }
			}
		    }
		}
	    }

	    if (! oneByOne) {
		// wait for all the threads to finish
		while (threads.size() > 0) {
		    try { Thread.sleep (1000); } catch (Exception e) {}
		}
	    }
	    msgln ("");

	    // report all tests
	    if (tableReport)
		createTableReport (reports);

	    // report results
	    int countSuccessful = 0;
	    int countErrors = 0;
	    for (Properties report: reports) {
		if (report.containsKey (REPORT_ERROR_MESSAGE)) {
		    report.remove (REPORT_STACK_TRACE);
		    countErrors += 1;
		    createErrorReport (report);
		} else {
		    String status = report.getProperty (REPORT_JOB_STATUS);
		    if (SoaplabConstants.JOB_COMPLETED.equals (status)) {
			countSuccessful += 1;
		    } else {
			countErrors += 1;
			createErrorReport (report);
		    }
		}
	    }

	    // make a summary
	    title ("Summary");
	    msgln ("Successfully:  " + countSuccessful);
	    msgln ("Erroneously:   " + countErrors);
	    msgln ("Not available: " + countNotAvailable);

   	    exit (0);

	} catch (Throwable e) {
	    processErrorAndExit (e);
	}

    }


    /*************************************************************************
     * 
     *************************************************************************/
    private static void createErrorReport (Properties report) {
	String serviceName = report.getProperty (REPORT_SERVICE_NAME);
	String inputLine = report.getProperty (REPORT_INPUT_LINE);
	String error = report.getProperty (REPORT_ERROR_MESSAGE);
	String status = report.getProperty (REPORT_JOB_STATUS);
	String jobId = report.getProperty (REPORT_JOB_ID);
	String reportResult = report.getProperty (SoaplabConstants.RESULT_REPORT);
	emsgln ("------------------------------------------------------------------------------");
	emsgln ("ERROR: " + serviceName);
	emsgln ("------------------------------------------------------------------------------");
	if (error != null)
	    emsgln (error);
	emsgln ("\tInput:  " + (inputLine == null ? "" : inputLine));
	if (status != null)
	    emsgln ("\tStatus: " + status);
	if (jobId != null)
	    emsgln ("\tJob ID: " + jobId);
	if (reportResult != null) {
	    emsgln (reportResult);
	    emsgln ("--- [end of report result] ---");
	}
	emsgln ("");
    }

    /*************************************************************************
     * 
     *************************************************************************/
    private static void createTableReport (List<Properties> reports) {

	// find the longest service name
	int maxLenName = 10;
	for (Properties report: reports) {
	    String serviceName = report.getProperty (REPORT_SERVICE_NAME);
	    if (serviceName != null) {
		int lenName = serviceName.length();
		if (lenName > maxLenName)
		    maxLenName = lenName;
	    }
	}

	// report on System.out
	title ("All tests");
	for (Properties report: reports) {
	    String serviceName = report.getProperty (REPORT_SERVICE_NAME);
	    String elapsed = report.getProperty (REPORT_ELAPSED_TIME);
	    String status = report.getProperty (REPORT_JOB_STATUS);
	    String jobId = report.getProperty (REPORT_JOB_ID);
	    msgln (String.format ("%-" + maxLenName + "s  %15s %-22s %s",
				  serviceName,
				  (elapsed == null ? "" : elapsed),
				  (status  == null ? "" : status),
				  (jobId   == null ? "" : jobId) ));
	}
	msgln ("");
    }

    /*************************************************************************
     * Fill 'report' with enough information from 'e'.
     *************************************************************************/
    private static void reportError (Properties report, Throwable e) {
	report.setProperty (REPORT_ERROR_MESSAGE,
	        e.getMessage()==null ? e.toString():e.getMessage());
	StringWriter sw = new StringWriter (500);
	e.printStackTrace (new PrintWriter (sw));
	report.setProperty (REPORT_STACK_TRACE, sw.toString());
	if (! report.containsKey (REPORT_JOB_STATUS))
	    report.setProperty (REPORT_JOB_STATUS, SoaplabConstants.JOB_UNKNOWN);
    }

    /*************************************************************************
     *
     *************************************************************************/
    private static void startService (List<Thread> threads,
				      MyLocator locator,
				      List<Properties> reports) {
	CallThread thread = new CallThread (threads, locator, reports);
	thread.setName (String.format ("%4s", thread.getId()) +
			"_" + locator.getServiceName());
	threads.add (thread);
	qmsgln (String.format ("%-22s %s", "[Start ]", thread.getName()));
	thread.start();
    }

    private static class CallThread extends Thread {

	private List<Thread> threads;
	private MyLocator locator;
	private List<Properties> reports;

	public CallThread (List<Thread> threads,
			   MyLocator locator,
			   List<Properties> reports) {
	    this.threads = threads;
	    this.locator = locator;
	    this.reports = reports;
	}

	public void run() {
	    try{
	    Properties report = callService (locator, reports);
	    String jobId = report.getProperty (REPORT_JOB_ID);
	    qmsgln (String.format ("%-22s %s %s",
				   "[" + report.getProperty (REPORT_JOB_STATUS) + "] ",
				   this.getName(),
				   (jobId == null ? "" : jobId) ));
	    } finally {
	    threads.remove (this);
	    }
	}
    }


    /*************************************************************************
     * Call service as define in 'locator' (which also has an input
     * data for the service) and report results into a new entry in
     * 'reports'.
     *
     * It also returns its own report.
     *************************************************************************/
    private static Properties callService (MyLocator locator,
					   List<Properties> reports) {

	Properties report = new Properties();
	reports.add (report);
	String serviceName = locator.getServiceName();
	String inputLine = locator.getInputLine();
	report.setProperty (REPORT_SERVICE_NAME, serviceName);
	report.setProperty (REPORT_INPUT_LINE, inputLine);

	try {
	    SoaplabBaseClient client = new SoaplabBaseClient (locator);

	    // collect inputs from the input line
	    BaseCmdLine cmd = null;
	    if (StringUtils.isBlank (inputLine)) {
		cmd = new BaseCmdLine (new String[] {}, true);
	    } else {
		cmd = new BaseCmdLine
		    (new StrTokenizer (inputLine,
				       StrMatcher.charSetMatcher (" \t\f"),
				       StrMatcher.quoteMatcher()).getTokenArray(),
		     true);
	    }
	    SoaplabMap inputs = SoaplabMap.fromMap
		(InputUtils.collectInputs (cmd,
					   SoaplabMap.toMaps (client.getInputSpec())));

	    // any unrecognized inputs on the command-line?
	    if (cmd.params.length > 0) {
		StringBuilder buf = new StringBuilder();
		buf.append ("Unrecognized inputs: ");
		for (String arg: cmd.params)
		    buf.append (arg + " ");
		report.setProperty (REPORT_ERROR_MESSAGE, buf.toString());
		report.setProperty (REPORT_JOB_STATUS, "NOT STARTED");
		return report;
	    }

	    // start service and wait for its completion
	    String jobId = client.createAndRun (inputs);
	    report.setProperty (REPORT_JOB_ID, jobId);
	    client.waitFor (jobId);

	    // save all info about just finished job
	    String status = client.getStatus (jobId);
	    report.setProperty (REPORT_JOB_STATUS, status);

	    SoaplabMap times = client.getCharacteristics (jobId);
	    Object elapsed = times.get (SoaplabConstants.TIME_ELAPSED);
	    if (elapsed != null) {
		try {
		    report.setProperty (REPORT_ELAPSED_TIME,
					DurationFormatUtils.formatDurationHMS
					(Long.decode (elapsed.toString())));
		} catch (NumberFormatException e) {
		}
	    }

	    String lastEvent = client.getLastEvent (jobId);
	    if (lastEvent != null)
		report.setProperty (REPORT_LAST_EVENT, lastEvent);

	    if(!client.getLocator().getProtocol().equals(ClientConfig.PROTOCOL_AXIS1))
	    {
	    // get result infos (about all available results)
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    ResultUtils.formatResultInfo
		(SoaplabMap.toStringMaps (client.getResultsInfo (jobId)),
		 new PrintStream (bos));
	    report.setProperty (REPORT_RESULTS_INFO, bos.toString());
	    }
	    // get results (but ignore them - except the special ones)
	    client.getResults (jobId);
	    Map<String,Object> results = 
		SoaplabMap.toMap
		(client.getSomeResults (jobId,
					new String[] {
					    SoaplabConstants.RESULT_REPORT,
					    SoaplabConstants.RESULT_DETAILED_STATUS
					}));
	    for (Iterator<String> it = results.keySet().iterator(); it.hasNext(); ) {
		String resultName = it.next();
		report.setProperty (resultName, results.get (resultName).toString());
	    }

	    // clean the job
	    if (! locator.isEnabledKeepResults()) {
		client.destroy (jobId);
		report.remove (REPORT_JOB_ID);
	    }

	} catch (Throwable e) {
	    reportError (report, e);
	}
	return report;
    }

    /*************************************************************************
     * Get an integer from 'value'; return 0 if 'value' is null or
     * non-numeric or negative.
     *************************************************************************/
    private static int getInt (String value) {
	if (value == null) return 0;
	try {
	    int maxThreads = Integer.decode (value.toString().trim()).intValue();
	    if (maxThreads < 0) {
		log.warn ("Parameter '-maxthreads " + value + "' is negative. Ignored.");
		return 0;
	    }
	    return maxThreads;
	} catch (NumberFormatException e) {
	    log.warn ("Parameter '-maxthreads " + value + "' is not numeric. Ignored.");
	    return 0;
	}
    }

    /*************************************************************************
     * Get Long from Object returned by a SoaplabMap; may be null.
     *************************************************************************/
    @SuppressWarnings("unused")
    private static Long getLong (Object value) {
	if (value == null) return null;
	try {
	    return Long.decode (value.toString());
	} catch (NumberFormatException e) {
	    return null;
	}
    }


    /*************************************************************************
     *
     * MyLocator ... serves as an extended container
     *
     *************************************************************************/
    static private class MyLocator extends ServiceLocator {

	private boolean keepResults = false;
	private String inputLine = "";

	public MyLocator (String serviceName, ServiceLocator patternLocator) {
	    super();
	    this.setServiceName (serviceName);

	    // from here, it is a pure cloning
	    this.setHost (patternLocator.getHost());
	    this.setPort (patternLocator.getPort());
	    this.setContext (patternLocator.getContext());
	    this.setServiceEndpoint (patternLocator.getServiceEndpoint());
	    this.setProtocol (patternLocator.getProtocol());
	}

	public void setInputLine (String inputLine) {
	    this.inputLine = inputLine;
	}
	public String getInputLine() {
	    return inputLine;
	}

	public void enableKeepResults (boolean keepResults) {
	    this.keepResults = keepResults;
	}
	public boolean isEnabledKeepResults() {
	    return keepResults;
	}

    }

}
