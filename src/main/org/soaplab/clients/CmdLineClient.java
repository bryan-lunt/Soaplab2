// CmdLineClient.java
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

package org.soaplab.clients;

import org.soaplab.share.SoaplabConstants;
import org.soaplab.share.SoaplabMap;
import org.soaplab.share.MapEntry;

import org.tulsoft.tools.BaseCmdLine;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.commons.io.IOUtils;

import java.util.Map;
import java.util.Iterator;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.File;


/**
 * A command-line tool accessing Soaplab services and their lists.
 * Start it with <tt>-help</tt> to see available options. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: CmdLineClient.java,v 1.25 2009/02/05 14:10:57 mahmutuludag Exp $
 */
public class CmdLineClient
    extends CmdLineHelper {

    /*************************************************************************
     *
     * Entry point...
     *
     *************************************************************************/
    public static void main (String [] args) {
	try {
	    BaseCmdLine cmd = getCmdLine (args, CmdLineClient.class);

	    // service location and protocol parameters
	    ServiceLocator locator = InputUtils.getServiceLocator (cmd);

	    // if we have a -job we may not need the -name (this is
	    // just a hack, but not an important one, so who cares...)
	    String jobId = cmd.getParam ("-job");
	    if ( StringUtils.isNotEmpty (jobId) &&
		 StringUtils.isEmpty (locator.getServiceName()) ) {
		int posBegToken = jobId.indexOf ("[");
		int posEndToken = jobId.indexOf ("]");
		if (posBegToken > -1 && posEndToken > posBegToken) {
		    locator.setServiceName
			(jobId.substring (posBegToken + 1, posEndToken));
		}
	    }

	    // list requests
	    boolean listAll = cmd.hasOption ("-l");
	    boolean listServices = cmd.hasOption ("-la");
	    boolean listServicesWithDesc = cmd.hasOption ("-lad");
	    boolean listCategories = cmd.hasOption ("-lc");
	    String category = cmd.getParam ("-category");
	    String listSaveFile = cmd.getParam ("-sl");

	    if (cmd.params.length > 0){
		// there are also non-list requests so a service name
		// is mandatory

		// where is the service
		if (StringUtils.isEmpty (locator.getServiceName())) {
		    System.err.println ("Service name must be given. Use '-name' or '-e'.");
		    System.exit (1);
		}
	    }

	    SoaplabBaseClient client = new SoaplabBaseClient (locator);

	    //
	    // deal with list requests
	    //

	    PrintWriter pw = null;
	    if (listSaveFile != null) {
		pw = new PrintWriter (listSaveFile);
	    }

	    if (listServices) {
		title ("Available analyses:", pw);
		for (String analysis: client.getAvailableAnalyses())
		    msgln (analysis, pw);
	    }

	    if (listServicesWithDesc) {
		title ("Available analyses (with descriptions):", pw);
		SoaplabMap mapWithDesc = client.getAvailableAnalysesWithDescription();
		for (MapEntry entry: mapWithDesc.getEntries()) {
		    msgln (entry.getKey(), pw);
		    msgln ("\t" + entry.getObjectValue(), pw);
		}
	    }

	    if (listCategories) {
		title ("Available categories:", pw);
		for (String categ: client.getAvailableCategories())
		    msgln (categ, pw);
	    }

	    if (listAll) {
		title ("Available categories and analyses:", pw);
		for (String categ: client.getAvailableCategories()) {
		    msgln (categ, pw);
		    for (String analysis: client.getAvailableAnalysesInCategory (categ))
			msgln ("\t" + analysis, pw);
		}
	    }

	    if (category != null) {
		title ("Category: " + category, pw);
		for (String analysis: client.getAvailableAnalysesInCategory (category))
		    msgln (analysis, pw);
	    }

	    IOUtils.closeQuietly (pw);

	    //
	    // give me service's metadata
	    //

	    if (cmd.hasOption ("-d")) {
		msgln (client.describe());
	    }

	    if (cmd.hasOption ("-i") || cmd.hasOption ("-ii")) {
		SoaplabMap[] attrs = client.getInputSpec();
		InputUtils.formatInputMetadata (SoaplabMap.toMaps (attrs),
						System.out,
						cmd.hasOption ("-ii"),
						verbose);
	    }

	    if (cmd.hasOption ("-o") || cmd.hasOption ("-oo")) {
		SoaplabMap[] attrs = client.getResultSpec();
		InputUtils.formatOutputMetadata (SoaplabMap.toMaps (attrs),
						 System.out,
						 cmd.hasOption ("-oo"),
						 verbose);
	    }

	    if (cmd.hasOption ("-t")) {
		Map<String,Object> aType = SoaplabMap.toMap (client.getAnalysisType());
		title ("Analysis properties:");
  		if (aType != null) {
		    for (Iterator<String> it = aType.keySet().iterator(); it.hasNext(); ) {
			String key = it.next();
			msgln (key + " => " + aType.get (key));
		    }
		    msgln ("");
		}
	    }

	    //
	    // deal with a job (creation, invocation, waiting for, killing...)
	    //
	    String jobIdFile   = cmd.getParam ("-jp");
	    boolean createJob  = cmd.hasOption ("-j");
	    boolean runJob     = cmd.hasOption ("-x");
	    boolean waitForJob = cmd.hasOption ("-w");
	    boolean killJob    = cmd.hasOption ("-k");

	    // collect inputs from the command line
	    // (unless the job had been already created)
	    SoaplabMap inputs = null;
	    if (jobId == null &&
		(createJob || runJob || waitForJob)) {
		inputs = SoaplabMap.fromMap
		    (InputUtils.collectInputs (cmd,
					       SoaplabMap.toMaps (client.getInputSpec())));
	    }

	    //
	    // read the rest of the command-line, including the result
	    // names - so we can check that there are no unrecognized
	    // parameters before we start any job
	    //
	    boolean getStatus     = cmd.hasOption ("-s");
	    boolean getTimes      = cmd.hasOption ("-c");
	    boolean getLastEvent  = cmd.hasOption ("-le");
	    boolean doClean       = cmd.hasOption ("-clean");
	    boolean getResultInfo = cmd.hasOption ("-ri");
	    boolean getResults    = cmd.hasOption ("-r");
	    boolean printBinary   = cmd.hasOption ("-bp");
	    boolean saveResults   = cmd.hasOption ("-sr");        

	    String[] resultNames = null;
	    if (getResults) {
		// collect names of the results (from the command-line)
		resultNames =
		    ResultUtils.collectResultNames (cmd,
						    SoaplabMap.toMaps (client.getResultSpec()));
	    }

	    // any unrecognized parameter/option on the command-line?
	    if (cmd.params.length > 0) {
		msg ("Unrecognized or unused arguments (perhaps an \"action\" option missing?): ");
		for (String arg: cmd.params)
		    msg (arg + " ");
		msgln ("");
		System.exit (1);
	    }

	    // when we do not have a job ID from a previous invocation
	    if (jobId == null) {
		if (waitForJob) {		    
		    jobId = client.createAndRun (inputs);
		    saveJobId (jobId, jobIdFile);
		    client.waitFor (jobId);
		} else if (runJob) {
		    jobId = client.createAndRun (inputs);
		    saveJobId (jobId, jobIdFile);
		} else if (createJob) {
		    jobId = client.createJob (inputs);
		    saveJobId (jobId, jobIdFile);
		}

		// when we use a previously stored job ID
	    } else {
		if (waitForJob) {
		    client.waitFor (jobId);
		} else if (runJob) {
		    client.run (jobId);
		} else if (killJob) {
		    client.terminate (jobId);
		}
	    }

	    //
	    // deal with the job status, characteristics and events
	    // (makes sense only if we have a jobID)
	    //
	    if (jobId != null) {
		if (getStatus) {
		    String status = client.getStatus (jobId);
		    title ("Status of job: " + jobId);
		    msgln (status);
		}
		if (getTimes) {
		    SoaplabMap times = client.getCharacteristics (jobId);
		    title ("Characteristics of job: " + jobId);
		    Long millis = getLong (times.get (SoaplabConstants.TIME_CREATED));
		    if (millis != null)
 			msgln ("\tCreated: " + DateFormatUtils.format (millis.longValue(),
								       SoaplabConstants.DT_FORMAT));
		    millis = getLong (times.get (SoaplabConstants.TIME_STARTED));
		    if (millis != null)
			msgln ("\tStarted: " + DateFormatUtils.format (millis.longValue(),
								       SoaplabConstants.DT_FORMAT));
		    millis = getLong (times.get (SoaplabConstants.TIME_ENDED));
		    if (millis != null)
			msgln ("\tEnded:   " + DateFormatUtils.format (millis.longValue(),
								       SoaplabConstants.DT_FORMAT));
		    Long ms = getLong (times.get (SoaplabConstants.TIME_ELAPSED));
		    if (ms != null)
			msgln ("\tElapsed: " + DurationFormatUtils.formatDurationHMS (ms));
		}
		if (getLastEvent) {
		    String lastEvent = client.getLastEvent (jobId);
		    title ("Last event reported for job: " + jobId);
		    msgln (lastEvent);
		}
	    }

	    //
	    // cleaning it all...
	    // (does not make sense together with other options, except -job)
	    //
	    if (jobId != null && doClean)
		client.destroy (jobId);

	    //
	    // deal with results (makes sense only if we have a job ID)
	    //
	    if (jobId != null) {
		if (getResultInfo) {
		    title ("Results info for job: " + jobId);
		    ResultUtils.formatResultInfo
			(SoaplabMap.toStringMaps (client.getResultsInfo (jobId)),
			 System.out);
		}

		if (getResults || saveResults) {

		    // getting results from the service (all or just some)
		    Map<String,Object> results;
		    if (resultNames.length == 0)
			results = SoaplabMap.toMap (client.getResults (jobId));
		    else
			results = SoaplabMap.toMap (client.getSomeResults (jobId, resultNames));
		    for (Iterator<String> it = results.keySet().iterator(); it.hasNext(); ) {
			String resultName = it.next();
			title (resultName);
			String[] files =
			    ResultUtils.handleResult (results.get (resultName),
						      resultName,
						      (printBinary && !saveResults ? null : jobId),
						      saveResults,
						      System.out);
			if (files != null) {
			    for (int i = 0; i < files.length; i++)
				msgln ("(written into: " + files[i] + ")");
			}
		    }
		}
	    }

   	    exit (0);

	} catch (Throwable e) {
	    processErrorAndExit (e);
	}
    }

    // save or show job ID
    static void saveJobId (String jobId, String jobIdFile) {
	if (jobId != null) {
	    // do not mix this with the standard output
	    if (jobIdFile == null) {
		System.err.println ("Job ID: " + jobId);
	    } else {
		try {
		    PrintStream fileout = new PrintStream
			(new FileOutputStream (new File (jobIdFile)));
		    fileout.println (jobId);
		    fileout.close();
		    
		} catch (IOException e) {
		    System.err.println ("Saving Job ID into '" + jobIdFile + "' failed. " + e.toString());
		    System.err.println ("Job ID: " + jobId);
		}
	    }
	}
    }

    // get Long from Object returned by a SoaplabMap; may be null
    static Long getLong (Object value) {
	if (value == null) return null;
	try {
	    return Long.decode (value.toString());
	} catch (NumberFormatException e) {
	    return null;
	}
    }

}
