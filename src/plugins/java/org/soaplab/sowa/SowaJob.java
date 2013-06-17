// SowaJob.java
//
// Created: April 2007
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

package org.soaplab.sowa;

import org.soaplab.share.SoaplabConstants;
import org.soaplab.share.SoaplabException;
import org.soaplab.services.cmdline.CmdLineJob;
import org.soaplab.services.Config;
import org.soaplab.services.Reporter;
import org.soaplab.services.JobState;
import org.soaplab.services.IOData;
import org.soaplab.services.metadata.MetadataAccessor;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.Enumeration;
import java.util.Properties;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

/**
 * This is a job executing external command-line tools. It represents
 * what was previously done by AppLab project. <p>
 *
 * It does not use, however, any "launcher". In the AppLab times, the
 * launcher was a Perl script that was a inter-mediator between a Java
 * class and an external program. This class calls the external tools
 * directly. <p>
 *
 * The class may also serve as a parent for those classes that need to
 * fiddle with command-line parameters before an external tool is
 * invoked. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: SowaJob.java,v 1.15 2010/08/05 12:11:31 mahmutuludag Exp $
 */

public class SowaJob
    extends CmdLineJob {

    private static org.apache.commons.logging.Log log =
       org.apache.commons.logging.LogFactory.getLog (SowaJob.class);

    // control the init() method
    private static boolean jobClassInitialized = false;

    // process controlling the external program
    protected Process process;
    
    protected boolean terminated = false;
    
    boolean lsf;
    
    /**************************************************************************
     * The main constructor.
     **************************************************************************/
    public SowaJob (String jobId,
		    MetadataAccessor metadataAccessor,
		    Reporter reporter,
		    Map<String,Object> sharedAttributes,
		    boolean jobRecreated)
	throws SoaplabException {
	super (jobId, metadataAccessor, reporter,
	       sharedAttributes, jobRecreated);
    lsf = Config.isEnabled(Config.LSF_ENABLED, false,
            getServiceName(), this);
    }

    /**************************************************************************
     *
     **************************************************************************/
    @Override protected synchronized void init()
	throws SoaplabException {
	super.init();

	if (jobClassInitialized)
	    return;
	jobClassInitialized = true;

	// here comes my own initialization (done just once for all jobs)

    }

    /**************************************************************************
     *
     **************************************************************************/
    @Override public void run()
	throws SoaplabException {
	if (! runAllowed())
	    return;

	checkInputs();
	if (log.isDebugEnabled())
	    log.debug ("Inputs check passed (" + inputs.size() + " inputs).");
	try {
        ProcessBuilder pb = getProcessBuilder();
        preExec (pb);
        Thread executeThread = getExecuteThread (pb);
        executeThread.start();
    } catch (RuntimeException e) {
    	// it looks helpful if we have the stack trace printed
    	SoaplabException.formatAndLog(e, log);
        internalError ("Problems with starting the job process: " + e.getMessage());        
    }
	synchronized (reporter){
	    while (reporter.getState().get() == JobState.CREATED)
	    try {
	        reporter.wait();
	    }
	    catch (InterruptedException e) {
	    }
	}
    }


    /**************************************************************************
     * Create and return a ProcessBuilder filled with the command-line
     * arguments and environment as defined in the metadata and user
     * input data for this job.
     *
     * It throws an exception when it was not able to fill the
     * ProcessBuilder.
     **************************************************************************/
    protected ProcessBuilder getProcessBuilder()
	throws SoaplabException {

	// command-line arguments
	ProcessBuilder pb = new ProcessBuilder (createArgs());
	pb.command().add (0, getExecutableName());

	// environment from several sources...
	Map<String,String> env = pb.environment();
	// ...from the user (as defined by the service metadata)
	addProperties (env, createEnvs());
	// ...from the service configuration
	addProperties (env,
		       Config.getMatchingProperties (Config.PROP_ENVAR,
						     getServiceName(), this));
	// ...combine the current PATH and Soaplab's addtopath properties
        addToPath (env, Config.getStrings (Config.PROP_ADDTOPATH_DIR,
					   null,
					   getServiceName(),
					   this));

	// working directory
	pb.directory (getJobDir());

	return pb;
    }

    /**************************************************************************
     * Update the environment variable PATH by our own directories
     * 'dirs' (taken originally from the Soaplab's property {@link
     * Config#PROP_ADDTOPATH_DIR}).
     *
     * Note that updating the PATH variable does not have any effect
     * on finding the executable - but it is made available to the
     * executable once the executable has been started (which solves,
     * for example, EMBOSS's wossname problem).
     *
     * The updated variable is stored in the given 'env'.
     **************************************************************************/
    protected void addToPath (Map<String,String> env,
			      String[] dirs) {
	if (dirs == null || dirs.length == 0)
	    return;
	String path = System.getenv ("PATH");
	if (path == null)
	    return;

	StringBuilder buf = new StringBuilder();
	for (String dir: dirs) {
	    buf.append (dir);
	    buf.append (File.pathSeparator);
	}
	buf.append (path);
	env.put ("PATH", buf.toString());
    }

    /**************************************************************************
     * Return a fully qualified executable name for this application -
     * if it can be found on the additions to the PATH (defined by
     * property PROP_ADDTOPATH_DIR) AND unless the executable already
     * has a fully qualified path.
     *
     * Note that this cannot be substituted by what the method
     * addToPath() does - because the environment variable PATH may be
     * changed only after the executable has been started.
     *
     * Otherwise return a plain executable name as it appears in the
     * service metadata.
     *
     * Throw an exception if the access to service metadata failed.
     **************************************************************************/
    @Override protected String getExecutableName()
	throws SoaplabException {

	String executable = super.getExecutableName();
	String[] paths = Config.getStrings (Config.PROP_ADDTOPATH_DIR,
					    null,
					    getServiceName(),
					    this);
	if (paths.length == 0 ||
	    new File (executable).isAbsolute())
	    return executable;

	// look into our (if any) additions to the PATH
	for (String path: paths) {
	    File file = new File (path, executable);
	    if (file.exists())
		return file.getAbsolutePath();
	}
 	return executable;
    }

    /**************************************************************************
     * Add the contents of 'props' to 'result'.
     **************************************************************************/
    private void addProperties (Map<String,String> result, Properties props) {
	for (Enumeration<Object> en = props.keys(); en.hasMoreElements(); ) {
	    String envName = (String)en.nextElement();
	    result.put (envName, props.getProperty (envName));
	}
    }

    /**************************************************************************
     * Called before the execution. Here, a sub-class has a chance to
     * change/add/remove some command-line arguments and/or
     * environment variables, or even the working directory (all of
     * these are in the given ProcessBuilder).
     *
     * The method can thrown an exception if the current set of
     * arguments is not good for being executed.
     **************************************************************************/
    protected void preExec (ProcessBuilder pb)
	throws SoaplabException {

	report ("Name: " + getServiceName());
	report ("Job ID: " + getId());
	StringBuilder buf = new StringBuilder();
	buf.append ("Program and parameters:\n");
	for (String str: pb.command()) {
	    buf.append (str);
	    buf.append ("\n");
	}
	buf.append ("--- end of parameters\n");
	report (buf.toString());
    }
    
    Thread executeThread = null;

    /**************************************************************************
     *
     **************************************************************************/
    protected synchronized Thread getExecuteThread (final ProcessBuilder pb)
	throws SoaplabException {
    	
        if (lsf)
        	executeThread = new ExecuteThreadForLSF(pb, getId());
        else
        	executeThread = new ExecuteThread (pb);

        return executeThread;
    }

    /**************************************************************************
     *
     **************************************************************************/
    protected void processResults (IOData[] ioData)
	throws SoaplabException {

	// find a result name (as defined in service metadata)
	for (IOData io: ioData) {
	    if (io.getDefinition().isOutput()) {
		File data = io.getData();
		if (data.length() > 0) {
		    reporter.setResult (io.getDefinition().id, data);
		}
	    }
	}
    }

    /**************************************************************************
     * Terminate this running job.
     **************************************************************************/
    public void terminate()
    throws SoaplabException {
		log.debug(getId() + " has been requested to terminate");
		synchronized (reporter) {
			if (process == null || terminated == true) {
				log.debug(getId() + " terminate request ignored");
				return;
			}
			terminated = true;
			process.destroy();
			try {
				// we shouldn't wait here much, under normal circumstances above
				// process.destroy() call should return fairly quickly
				reporter.wait(SoaplabConstants.JOB_TIMEOUT_FOR_TERMINATE_CALLS);
			} catch (InterruptedException e) {
			}
			if(process==null)
			    return;
			try {
				int ev = process.exitValue();
				log.debug(getId() + " process exit value: " + ev);
			} catch (IllegalThreadStateException e) {
				log.debug(getId() + " second call for process.destroy()");
				// we need this second call for LSF jobs as LSF replaces bsub
				// process by a bkill process
				// and the second process doesn't always dies smoothly,
				// especially when using LSF 7
				process.destroy();
			}
			try {
				process.exitValue();
			} catch (IllegalThreadStateException e) {
				// if it was not possible to terminate the process
				// then interrupt the execute thread
				executeThread.interrupt();
			}
		}
	}
    
    /**************************************************************************
     *
     * A thread invoking an external process and waiting for its completion
     *
     **************************************************************************/
    protected class ExecuteThread
	extends Thread {

	ProcessBuilder pb;
	InputStream stdin;
	OutputStream stdout;
	OutputStream stderr;
	File stdout2Report;
	File stderr2Report;
	IOData[] ioData;

	public ExecuteThread (final ProcessBuilder pb)
	    throws SoaplabException {
	    this.pb = pb;
	    setName (getName().replace ("Thread", "SowaRunner"));

	    // create IO streams (as defined in service metadata)
	    ioData = createIO();
	    try {
		for (IOData io: ioData) {
		    if (io.getDefinition().isStdout()) {
			stdout = new FileOutputStream (io.getReference());
			continue;
		    } 
		    if (io.getDefinition().isStderr()) {
			stderr = new FileOutputStream (io.getReference());
			continue;
		    } 
		    if (io.getDefinition().isStdin()) {
			stdin = new FileInputStream (io.getReference());
			continue;
		    } 
		}

		// catch STDOUT and STDERR even if not defined in service metadata
		if (stdout == null) {
		    stdout2Report = new File (getJobDir(), "_log_STDOUT_");
		    stdout = new FileOutputStream (stdout2Report);
		}
		if (stderr == null) {
		    stderr2Report = new File (getJobDir(), "_log_STDERR_");
		    stderr = new FileOutputStream (stderr2Report);
		}

	    } catch (FileNotFoundException e) {
		internalError ("Problems with creating file for a standard stream: " +
			       e.getMessage());
	    }
	}

	public void run() {
	    try {
		// start an external process
		process = pb.start();

		// chain the process's standard stream (if wanted)
		if (stdin != null)
 		    new CopyThread (stdin, process.getOutputStream()).start();
		CopyThread toStdout =
		    new CopyThread (process.getInputStream(), stdout);
		toStdout.start();
		CopyThread toStderr =
		    new CopyThread (process.getErrorStream(), stderr);
		toStderr.start();
		synchronized (reporter) {
		    reporter.getState().set (JobState.RUNNING);
		    reporter.notifyAll();
		}

		// wait until the child process ends
		int exitCode = -324;
		while (true) {
		    try {
			exitCode = process.waitFor();
			break;
		    } catch (InterruptedException e) {
		    }
		}

		// wait for the standard streams to finalize
		try {
		    toStdout.join();
		    toStderr.join();
		} catch (InterruptedException e) {
		}

		// put STDOUT and STDERR to report
		// (if not defined in the service metadata)
		if (stdout2Report != null && stdout2Report.length() > 0)
		    reporter.report ("Standard output stream", stdout2Report);
		if (stderr2Report != null && stderr2Report.length() > 0) {
		    error ("Some error messages were reported.");
		    reporter.report ("Standard error stream", stderr2Report);
		}

		// process exit code and results
		report ("Exit: " + exitCode);
		reporter.getState().setDetailed ("" + exitCode);
		if ( exitCode == 0 ||
		     Config.isEnabled (Config.PROP_ACCEPT_ANY_EXITCODE,
				       false,
				       getServiceName(),
				       SowaJob.this) ) {
		    processResults (ioData);
		    reporter.getState().set (JobState.COMPLETED);
		}
		else if (process == null){
        	reporter.getState().set(JobState.TERMINATED_BY_REQUEST);
        }
		else {
		    reporter.getState().set (JobState.TERMINATED_BY_ERROR);
		}
		
	    } catch (SoaplabException e) {
		error (e.getMessage());
		log.error (e.getMessage());
		reporter.getState().set (JobState.TERMINATED_BY_ERROR);

	    } catch (IOException e) {
		StringBuilder buf = new StringBuilder();
		buf.append ("Not able to execute an external program: '");
		try { 
		    buf.append (pb.command().get (0));
		} catch (IndexOutOfBoundsException e2) {
		}
		buf.append ("': ");
		buf.append (StringUtils.removeStart (e.getMessage(),
						     "java.io.IOException:"));
		error (buf.toString());
		log.error (buf.toString());
		reporter.getState().set (JobState.TERMINATED_BY_ERROR);
		
	    } catch (RuntimeException e) {
		error (INTERNAL_ERROR + e.getMessage());
		reporter.getState().set (JobState.TERMINATED_BY_ERROR);
		SoaplabException.formatAndLog (e, log);
		
	    } finally {
		try {
		    reporter.setDetailedStatusResult();
		    reporter.setReportResult();
		} catch (SoaplabException e) {
		    log.error ("Setting special results failed. " + e.getMessage());
		}
		finally {
		// inform other threads waiting for the termination that
		// it has been done
		synchronized (reporter) {
		    if (process != null){
		    	process.destroy();
		    	process = null;
		    }
		    reporter.notifyAll();
		}
		}
	    }
	}
    }

    
    /**************************************************************************
     * 
     * A thread invoking an external process using LSF and waiting for its
     * completion
     * 
     **************************************************************************/

    protected class ExecuteThreadForLSF extends Thread {

        ProcessBuilder pb;        
        File stdout2Report;
        File stderr2Report;
        File bsubstderr2Report;
        IOData[] ioData;
        final static String LSF_submit_command = "bsub";

        public ExecuteThreadForLSF(final ProcessBuilder pb, String jobId)
                throws SoaplabException {
            this.pb = pb;
            setName(getName().replace("Thread", "SowaRunnerForLSF"));

            String queue = Config.getString(Config.LSF_QUEUENAME, null,
                    getServiceName(), this);
            String arch    = Config.getString(Config.LSF_ARCH,  null,
                    getServiceName(), this);
            String project = Config.getString(Config.LSF_PROJECT,   null,
                    getServiceName(), this);

            String lsfbindir = Config.getString(Config.PROP_ENVAR
                    + ".LSF_BINDIR", "", getServiceName(), this);
            short i = 0;

            String bsub = LSF_submit_command;

            if (lsfbindir.length() > 0)
                bsub = lsfbindir + System.getProperty("file.separator")
                        + LSF_submit_command;

            pb.command().add(i++, bsub);
            if (queue != null) {
                pb.command().add(i++, "-q");
                pb.command().add(i++, queue);
            }
            if (arch    != null) {
                pb.command().add(i++, "-m");
                pb.command().add(i++, arch);
            }
            if (project != null) {
                pb.command().add(i++, "-P");
                pb.command().add(i++, project);
            }

            pb.command().add(i++, "-K");// waits until the job is finished
            pb.command().add(i++, "-J");
            pb.command().add(i++, jobId.replace("[", "").replace("]", ""));

            // create IO arguments (as defined in service metadata)
            ioData = createIO();
            for (IOData io : ioData) {
                if (io.getDefinition().isStdout()) {
                    pb.command().add("-" + io.getDefinition().id);
                    pb.command().add(io.getReference());
                    continue;
                }
                if (io.getDefinition().isStderr()) {
                    pb.command().add("-" + io.getDefinition().id);
                    pb.command().add(io.getReference());
                    continue;
                }
                if (io.getDefinition().isStdin()) {
                    pb.command().add("-" + io.getDefinition().id);
                    pb.command().add(io.getReference());
                    continue;
                }

            }

            // catch STDOUT and STDERR when you want to debug ???
            //if (false) {
                // stdout2Report garbaged with LSF output and doesn't look like 
                // usefull
              //  stdout2Report = new File(getJobDir(), "_log_STDOUT_");
                //pb.command().add(i++, "-oo");
                //pb.command().add(i++, stdout2Report.getPath());
            //}


            // append the standard error output of the job to the specified file
            
            stderr2Report = new File(getJobDir(), "_log_STDERR_");
            pb.command().add(i++, "-e");
            pb.command().add(i++, stderr2Report.getPath());
        }

        
        
        
        public void run() {
            try {
                int exitCode = -324;
                synchronized (reporter) {
                    // start an external process
                    process = pb.start();
                    reporter.getState().set(JobState.RUNNING);
                    reporter.notifyAll();
                }

                // wait until the child process ends
                while (true) {
                    try {
                        exitCode = process.waitFor();
                        break;
                    } catch (InterruptedException e) {
                    	break;
                    }
                }

                // put STDOUT and STDERR to report ???
                // stdout2Report garbaged with LSF output and doesn't look like 
                // useful
                //if (stdout2Report != null && stdout2Report.length() >= 0)
                //    reporter.report("Standard output stream", stdout2Report);
                if (stderr2Report != null && stderr2Report.length() > 0) {
                    error("Some error messages were reported.");
                    reporter.report("Standard error stream", stderr2Report);
                }
                if (exitCode != 0 && process != null && terminated != true) {
                    // reading errors
                    String line1, line2, line;
                    BufferedReader input = new BufferedReader(
                            new InputStreamReader(process.getErrorStream()));
                    line1 = input.readLine();
                    line2 = input.readLine();
                    // if the first line says Waiting for dispatch
                    // and the second line says Job is finished then ignore
                    if (!(line1 != null
                            && line1.contains("Waiting for dispatch")
                            && line2 != null && line2
                            .contains("Job is finished"))) {
                        //input.reset();
                        log.error("LSF stderr output: "+(line1!=null?line1:"")
                                +(line2!=null?"\n"+line2:""));
                        error("Some error messages were reported.");
                        while ((line = input.readLine()) != null) {
                            log.error(line);
                            reporter.report(line);
                        }
                    }
                    input.close();
                }

                // process exit code and results
                if (exitCode != -324) {//don't report the exit code if we didn't have one
                	report("Exit code: " + exitCode);
                	reporter.getState().setDetailed("" + exitCode);
                }
                if (terminated == true){
                	reporter.getState().set(JobState.TERMINATED_BY_REQUEST);
                }
                else if (exitCode == 0 
                        || Config.isEnabled(Config.PROP_ACCEPT_ANY_EXITCODE,
                                false, getServiceName(), SowaJob.this)) {
                    processResults(ioData);
                    reporter.getState().set(JobState.COMPLETED);
                } else {
                    reporter.getState().set(JobState.TERMINATED_BY_ERROR);
                }

            } catch (SoaplabException e) {
                error(e.getMessage());
                log.error(e.getMessage());
                reporter.getState().set(JobState.TERMINATED_BY_ERROR);
            } catch (IOException e) {
                StringBuilder buf = new StringBuilder();
                buf.append("Not able to execute LSF submit program: '");
                try {
                    buf.append(pb.command().get(0));
                } catch (IndexOutOfBoundsException e2) {
                }
                buf.append("': ");
                buf.append(StringUtils.removeStart(e.getMessage(),
                        "java.io.IOException:"));
                error(buf.toString());
                log.error(buf.toString());
                reporter.getState().set(JobState.TERMINATED_BY_ERROR);
            } catch (RuntimeException e) {
                error(INTERNAL_ERROR + e.getMessage());
                reporter.getState().set(JobState.TERMINATED_BY_ERROR);
                SoaplabException.formatAndLog(e, log);
            } finally {
                try {
                    reporter.setDetailedStatusResult();
                    reporter.setReportResult();
                } catch (SoaplabException e) {
                    log.error("Setting special results failed. "
                            + e.getMessage());
                } finally {
                    // inform other threads waiting for the termination that
                    // it has been done
                    synchronized (reporter) {
                        if (process != null){
                        	process.destroy();
                        	process = null;
                        }
                        reporter.notifyAll();
                    }
                }
            }
        }
    }

    
    /**************************************************************************
     *
     * CopyThread - reading/writing standard stream from/to external process
     *
     **************************************************************************/
    protected class CopyThread extends Thread {

	final InputStream inStream;
	final OutputStream outStream;

	public CopyThread (InputStream is, OutputStream os) {
	    inStream = is;
	    outStream = os;
	    setName (getName().replace ("Thread", "SowaCopier"));
	}

	public void run() {
	    try {
		IOUtils.copy (inStream, outStream);
	    } catch (Exception e) {
		synchronized (reporter){
		    reporter.getState().set (JobState.TERMINATED_BY_ERROR);
		    if (process != null){
		        // if the process has already been destroyed it is normal to get errors
		        // no need to report in that case
		        error ("Error when copying data to/from the analysis: " + e.toString());
		        terminated=true;
		        process.destroy();
		        process = null;
		    }
		}
	    } finally {
		IOUtils.closeQuietly (inStream);
		IOUtils.closeQuietly (outStream);
	    }
	}
    }

}
