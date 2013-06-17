// ExploreStorage.java
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

package org.soaplab.admin;

import org.soaplab.share.SoaplabConstants;
import org.soaplab.clients.CmdLineHelper;
import org.soaplab.clients.ResultUtils;
import org.soaplab.services.storage.PersistenceManager;
import org.soaplab.services.storage.PersistentStorage;
import org.soaplab.services.storage.FileStorage;
import org.soaplab.services.JobManager;
import org.soaplab.services.JobState;
import org.soaplab.services.Job;
import org.soaplab.services.GenUtils;
import org.soaplab.services.AbstractJob;
import org.soaplab.services.Reporter;
import org.soaplab.share.SoaplabException;
import org.soaplab.services.metadata.MetadataAccessor;

import org.tulsoft.tools.BaseCmdLine;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.commons.lang.time.DateFormatUtils;

import java.util.Map;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * A command-line tool that shows the contents of one or more storage
 * handlers. Start it with <tt>-help</tt> to see available
 * options. <p>
 *
 * A storage handler is any implementation of {@link
 * org.soaplab.services.storage.PersistentStorage PersistentStorage}
 * interface. It stores analysis results and status of analysis jobs
 * in more permanent place than is the main memory. A slightly unique
 * implementation is class {@link
 * org.soaplab.services.storage.PersistenceManager PersistenceManager}
 * because it can pass requests to other (regular) storage
 * handlers. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: ExploreStorage.java,v 1.12 2007/10/22 00:31:17 marsenger Exp $
 */
public class ExploreStorage
    extends CmdLineHelper {

    static ExploreStorage worker = new ExploreStorage();

    /*************************************************************************
     * Create and run a testing job.
     *************************************************************************/
    static void createAndRun (PersistentStorage storage)
	throws SoaplabException {

	title ("Creating and running a job:");
	MyJobManager jobman = worker.new MyJobManager (storage);
	Job job = jobman.createJob ("testing.service",
				    null, null, null);
	String jobId = job.getId();
	jobInfo (storage, jobId, "Job state before run");
	job.run();
	jobInfo (storage, jobId, "Job state after run");
    }

    /*************************************************************************
     * Print job info.
     *************************************************************************/
    static void jobInfo (PersistentStorage storage, String jobId, String title)
	throws SoaplabException {
	JobState state = storage.getJobState (jobId);
	msgln (title + ": " + jobId + " (" + state + ")");
	msgln ("\tcreated: " + DateFormatUtils.format (state.getCreated(),
						       SoaplabConstants.DT_FORMAT));
	msgln ("\tstarted: " + DateFormatUtils.format (state.getStarted(),
						       SoaplabConstants.DT_FORMAT));
	msgln ("\tended:   " + DateFormatUtils.format (state.getEnded(),
						       SoaplabConstants.DT_FORMAT));
	msgln ("\telapsed: " + DurationFormatUtils.formatDurationHMS (state.getElapsed()));
	msgln ("\tlast event: " + state.getLastEvent());
    }

    /*************************************************************************
     *
     *  An entry point
     *
     *************************************************************************/
    public static void main (String[] args) {

	try {
	    BaseCmdLine cmd = getCmdLine (args, ExploreStorage.class);
	    String param = null;

	    // what kind of persistent storage to test/use
	    PersistentStorage storage = null;
	    if (cmd.hasOption ("-all"))
		storage = new PersistenceManager();
	    else if (cmd.hasOption ("-file"))
		storage = new FileStorage();
	    else if (cmd.hasOption ("-db"))
		;
// 		storage = new DBStorage();
	    else
		storage = new PersistenceManager();

	    // only storage(s) description?
	    if (cmd.params.length == 0) {
		title ("Available storage:");
		msgln (storage.getDescription());
		return;
	    }

	    // removal?
	    param = cmd.getParam ("-clean");
	    if (param != null) {
		try {
		    storage.removeJob (param);
		    msgln ("Removed: " + param);
		} catch (SoaplabException e2) {
		    System.err.println (e2.getMessage());
		}
	    }
	    param = cmd.getParam ("-cleanlike");
	    if (param != null) {
		storage.removeJobs (param);
		msgln ("Jobs containing '" + param + "' removed");
	    }
	    if (cmd.hasOption ("-cleanall")) {
		storage.removeJobs ("[");
		msgln ("All jobs removed");
	    }
	    Date dateFrom = null;
	    Date dateTo = null;
	    long fromDate = GenUtils.str2Date (cmd.getParam ("-rf"), null);
	    long toDate = GenUtils.str2Date (cmd.getParam ("-rt"), null);
	    if (fromDate != 0)
		dateFrom = new Date (System.currentTimeMillis() + fromDate);
	    if (toDate != 0)
		dateTo = new Date (System.currentTimeMillis() + toDate);
	    if (dateFrom != null || dateTo != null) {
		storage.removeJobs (dateFrom, dateTo);
	    }

	    // create and run a job?
	    if (cmd.hasOption ("-run")) {
		createAndRun (storage);
	    }

	    // list all jobs?
	    if (cmd.hasOption ("-l")) {
		title ("Available jobs:");
		String[] list = storage.listJobs();
		for (int i = 0; i < list.length; i++)
		    msgln (list[i]);
	    }

	    // info about a job
	    String jobId = cmd.getParam ("-job");
	    if (cmd.hasOption ("-i")) {
		if (jobId == null)
		    System.err.println ("Option '-i' is valid only with parameter '-job <job-id>'.");
		else
		    jobInfo (storage, jobId, "Job");
	    }

	    // info about job results?
	    if (cmd.hasOption ("-ri")) {
		if (jobId == null) {
		    System.err.println ("Option '-ri' is valid only with parameter '-job <job-id>'.");
		} else {
		    title ("Results info for job " + jobId);
		    ResultUtils.formatResultInfo (storage.getResultsInfo (jobId), System.out);
		}
	    }

	    // get results?
	    if ((param = cmd.getParam ("-r")) != null) {
		if (jobId == null) {
		    System.err.println ("Parameter '-r' is valid only with parameter '-job <job-id>'.");
		} else {
		    boolean saveResults = cmd.hasOption("-sr");
		    title (param);
		    String[] files =
			ResultUtils.handleResult (storage.getResult (jobId, param),
						  param, jobId, saveResults, System.out);
		    if (files != null) {
			for (int i = 0; i < files.length; i++)
			    msgln ("(written into: " + files[i] + ")");
		    }
		}
	    }

	} catch (Exception e) {
	    processErrorAndExit (e);
	}
    }

    //
    // a primitive job manager, creating just my testing job
    //
    class MyJobManager extends JobManager {
	public MyJobManager (PersistentStorage percy) {
	    super();
	    this.percy = percy;
	}
	protected void init() {
	}
	protected Job getJobInstance (String jobId,
				      MetadataAccessor accessor,
				      Reporter reporter,
				      Map<String,Object> sharedAttributes,
				      boolean jobRecreated)
	    throws SoaplabException {
	    return new MyJob (jobId, accessor, reporter,
			      sharedAttributes, jobRecreated);
	}
    }

    //
    // a testing job, creating boggy results
    //
    class MyJob extends AbstractJob {
	protected MyJob (String jobId,
			 MetadataAccessor metadataAccessor,
			 Reporter reporter,
			 Map<String,Object> sharedAttributes,
			 boolean jobRecreated)
	    throws SoaplabException {
	    super (jobId, metadataAccessor, reporter, sharedAttributes, jobRecreated);
	}
	protected void realRun()
	    throws SoaplabException {
	    reporter.setResult ("a_string", "This is a string");
	    reporter.setResult ("a_string_array",
				new String[] { "The first element",
					       "The second element",
					       "The third element" });
	    reporter.setResult ("a_bytes",
				("These are bytes").getBytes());
	    reporter.setResult ("a_bytes_array",
				new byte[][] {
				    ("Bytes 1").getBytes(),
				    ("Bytes 2").getBytes() });
	    try {
		File file = File.createTempFile ("testing.", null);
		FileUtils.writeStringToFile (file,
					     "string in a file",
					     System.getProperty ("file.encoding"));
		reporter.setResult ("a_file", file);
		file.delete();

		File file1 = File.createTempFile ("testing.1.", null);
		FileUtils.writeStringToFile (file1,
					     "string 1 in a file",
					     System.getProperty ("file.encoding"));
		File file2 = File.createTempFile ("testing.2.", null);
		FileUtils.writeStringToFile (file2,
					     "string 2 in a file",
					     System.getProperty ("file.encoding"));
		reporter.setResult ("a_file_array",
				    new File[] { file1, file2 });
		file1.delete();
		file2.delete();

	    } catch (IOException e) {
		System.err.println ("Cannot create file: " + e.toString());
	    }

	}
    }
}
