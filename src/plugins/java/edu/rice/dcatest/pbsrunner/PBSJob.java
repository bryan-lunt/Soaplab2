package edu.rice.dcatest.pbsrunner;
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

import java.util.List;
import java.util.Map;
import java.util.Enumeration;
import java.util.Properties;
import java.io.*;

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

public class PBSJob
    extends CmdLineJob {

    private static org.apache.commons.logging.Log log =
       org.apache.commons.logging.LogFactory.getLog (PBSJob.class);

    // control the init() method
    private static boolean jobClassInitialized = false;

    // process controlling the external program
    protected Process process;
    
    protected boolean terminated = false;
    
    boolean lsf;
    
    public String pbs_jid;
    public static final int QSTAT_INTERVAL = 10000;
    public static final boolean DEBUG = true;
    
    /**************************************************************************
     * The main constructor.
     **************************************************************************/
    public PBSJob (String jobId,
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

    ProcessBuilder qsubpb;
	ProcessBuilder pb;
	
	File stdout2Report;
	File stderr2Report;
	IOData[] ioData;

	boolean running = false;

	public ExecuteThread (final ProcessBuilder pb) throws SoaplabException {
	    this.pb = pb;
	    setName (getName().replace ("Thread", "PBSSowaRunner"));

	    // create IO streams (as defined in service metadata)
	    ioData = createIO();
	    try {

			// catch STDOUT and STDERR even if not defined in service metadata
			    stdout2Report = new File (getJobDir(), "_log_STDOUT_");

			    stderr2Report = new File (getJobDir(), "_log_STDERR_");


	    } catch (FileNotFoundException e) {
		internalError ("Problems with creating file for a standard stream: " +
			       e.getMessage());
	    }
	    
	    /*Copy the ProcessBuilder*/
	    this.qsubpb = new ProcessBuilder();
	   //Don't copy the command! Just the environment and directory and stuff!
	    this.qsubpb.environment().putAll(pb.environment());
	    this.qsubpb.directory(pb.directory());
	    this.qsubpb.redirectErrorStream(pb.redirectErrorStream());
	    
	    //Setup the qsub command
	    List<String> pbsCommand = this.qsubpb.command();
	    pbsCommand.clear();
	    pbsCommand.add("/usr/bin/qsub");
	    
	    pbsCommand.add("-d");
	    pbsCommand.add(getJobDir().getAbsolutePath());
	    
	    pbsCommand.add("-o ");
	    pbsCommand.add(stdout2Report.getAbsolutePath());
	    
	    pbsCommand.add("-e ");
	    pbsCommand.add(stderr2Report.getAbsolutePath());
	    
	    pbsCommand.add("-V");//Because we used a copied process builder, it now gets all the environment variables it would have gotten anyway.
	    
	    pbsCommand.add("__thescript");//Script file to qsub...
	    
	    
	    //setup the script for qsub
	    try{
	    File script_file = new File(getJobDir(),"__thescript");
	    PrintWriter fw = new PrintWriter(new BufferedWriter(new FileWriter(script_file.getAbsoluteFile())));

	    List<String> theCommand = pb.command();
	    
	    for(int i = 0;i<theCommand.size();i++)
	    	fw.print(theCommand.get(i));
	    	fw.print(" ");
	    fw.println("");
	    
	    fw.flush();
	    fw.close();
	    }catch(Exception e){internalError("Problems creating a script for qsub : " + e.getMessage());}
	    
	    if(DEBUG){
	    	System.out.println("PBSJOB : About to run: " + qsubpb.command());
	    	System.out.println("PBSJOB : The script is : " + pb.command());
	    }
	}

	public void run() {
	    try {
			// submit via qsub

			process = qsubpb.start();
			
			synchronized (reporter) {
			    reporter.getState().set (JobState.CREATED);
			    reporter.notifyAll();
			}
	
			// wait until the qsub process ends
			int qsubexitCode = -324;
			while (true) {
			    try {
			    	qsubexitCode = process.waitFor();//waiting for qsub to finish
			    	break;
			    } catch (InterruptedException e) {/*Don't care*/}
			}
		
			if(DEBUG){
				System.out.println("PBSJOB : qsub exit code :" + qsubexitCode);
				
				try{
					BufferedReader ebr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
					String tmp = ebr.readLine();
					while(tmp != null){
						System.out.print("PBSJOB : qsub error: " + tmp);
						tmp = ebr.readLine();
					}
				}catch(Exception e){e.printStackTrace();}
				
			}
			
			
			StringBuilder jid_builder = new StringBuilder();
			
			try{
				BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String tmp = br.readLine();
				while(tmp != null){
					jid_builder.append(tmp.trim());
					tmp = br.readLine();
				}
				
				if(DEBUG){
					System.out.print("PBSJOB : qsub stdout: " + jid_builder.toString());
				}
				
			}catch(Exception e){e.printStackTrace();}
			
			pbs_jid = jid_builder.toString();
			int jobExitCode = qsubexitCode;
			if(qsubexitCode != 0){
				running = false;
				
			}else{
				running = true;
			
			try{Thread.sleep(QSTAT_INTERVAL);}catch(InterruptedException e){}
			
			while(!terminated && running) {
				try{
					pbsTorque.Job pbJ = pbsTorque.Job.getJobById(pbs_jid);

					if(pbJ == null){
						running = false;
						synchronized (reporter){
							reporter.getState().set (JobState.UNKNOWN);
							reporter.notifyAll();
						}
					}else{
					
						char jobStatus = pbJ.getStatus().charAt(0);
						
						synchronized (reporter) {
							switch(jobStatus){
							case 'Q':
								reporter.getState().set (JobState.CREATED);
								break;
							case 'R':
								reporter.getState().set (JobState.RUNNING);
								break;
							case 'C':
								reporter.getState().set (JobState.COMPLETED);
								jobExitCode = 0;
								running = false;
								break;
							case 'E':
								reporter.getState().set (JobState.TERMINATED_BY_ERROR);
								jobExitCode=1;
								running = false;
								break;
							default:
								reporter.getState().set (JobState.UNKNOWN);
								running = false;
							}
						    reporter.notifyAll();
						}
					}
					
					Thread.sleep(QSTAT_INTERVAL);
				}catch(InterruptedException e){}
			}
			}
			
			reportOutput();
			postJobOutputAndReporting(jobExitCode);
			
	    }catch(Exception e){e.printStackTrace();}		
	}
 
    protected void reportOutput() {


    			// put STDOUT and STDERR to report
    			// (if not defined in the service metadata)
    			if (this.stdout2Report != null && stdout2Report.length() > 0)
    			    reporter.report ("Standard output stream", stdout2Report);
    			if (this.stderr2Report != null && stderr2Report.length() > 0) {
    			    error ("Some error messages were reported.");
    			    reporter.report ("Standard error stream", stderr2Report);
    			}
    	}
    
    private void postJobOutputAndReporting(int exitCode) {
    	// process exit code and results
    			try{
    			report ("Exit: " + exitCode);
    			reporter.getState().setDetailed ("" + exitCode);
    			if ( exitCode == 0 ||
    			     Config.isEnabled (Config.PROP_ACCEPT_ANY_EXITCODE,
    					       false,
    					       getServiceName(),
    					       PBSJob.this) ) {
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
