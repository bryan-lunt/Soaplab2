package org.soaplab.ebi;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.soaplab.gowlab.GowlabJob;
import org.soaplab.services.JobState;
import org.soaplab.services.Reporter;
import org.soaplab.services.events.HeartbeatProgressEvent;
import org.soaplab.services.metadata.MetadataAccessor;
import org.soaplab.share.SoaplabException;

/**
 * Abstract job for the EBI webservices calls. Most parts of this class was
 * copied from the GridJob class Martin has developed.
 * <p>
 * 
 * @author
 * @version $Id: AbstractWSJob.java,v 1.10 2009/06/19 12:58:35 mahmutuludag Exp $
 */

public abstract class AbstractWSJob extends GowlabJob {

	/**
	 * It cannot report back any errors found when waiting for a job completion.
	 * Therefore, it puts them into the 'report' result as errors.
	 * <p>
	 * 
	 * It polls for the job status (how often, it depends on several
	 * properties). It ends when: a) an error (retrieving status) occurs, or b)
	 * the reporter reports a job completion, or c) a timeout occurs (if timeout
	 * is set).
	 */
	protected class StateWatchingThread extends Thread {

		int maxPollingInterval = 100;

		int pollingInterval = 1;

		protected void endOfWatching() {
			log.info("end of watching: "+this.getName());
			try {
				// create informative results
				reporter.setDetailedStatusResult();
				reporter.setReportResult();

				// inform other threads waiting for the termination that
				// it has been done
				synchronized (reporter) {
					reporter.notifyAll();
				}
			} catch (Throwable e) {
				log.error("Trouble in EndOfWatching: " + e.getMessage());
			}
		}

		public void run() {
			long started = System.currentTimeMillis();
			int pollingCount = 10;
			int state = reporter.getState().get();
			while (!reporter.getState().isCompleted()) {
				processJobStatus();
				if (state != reporter.getState().get())
				    log.info("status from the reporter: "+reporter.getState());
				
				state = reporter.getState().get();
				
				long remainingTime = getRemainingTimeout(System
							.currentTimeMillis()
							- started);
				if (remainingTime < 0) {
						reporter
								.error("Soaplab lost patience and ordered TIMEOUT...");
						try {
							terminate();
						} catch (SoaplabException e) {
							// our 'terminate' does not produce any exception
						}
				}
				// sleep for a while
				if (pollingCount-- <= 0) {
					pollingInterval *= 2;
					if (pollingInterval > maxPollingInterval)
						pollingInterval = maxPollingInterval;
					pollingCount = 10;
				}
				synchronized (reporter) {
					try {
						reporter.wait(pollingInterval * 1000);
					} catch (Exception e) {
					}
				}
			}
			endOfWatching();
		}
	}

	private static Log log = LogFactory.getLog(AbstractWSJob.class);

	protected String jobId = null;

	protected static final String PARAM_SEQUENCE = "sequence";

	protected static final String PARAM_SEARCHTYPE = "searchtype";

	protected static final String PARAM_OUTFORMAT = "outformat";

	/***************************************************************************
	 * The main constructor.
	 **************************************************************************/
	public AbstractWSJob(String jobId, MetadataAccessor metadataAccessor,
			Reporter reporter, Map<String, Object> sharedAttributes,
			boolean jobRecreated) throws SoaplabException {
		super(jobId, metadataAccessor, reporter, sharedAttributes, jobRecreated);
	}

	public static final int DEFAULT_JOB_TIMEOUT = 1 * 60 * 60; // 1 hour

	/***************************************************************************
	 * Returns a timeout (in millis) that should be maximally spent waiting for
	 * a job completion. The method gets parameter 'alreadySpent' showing how
	 * much time (in millis) was already spent on the job. At the beginning,
	 * during the first call) it is usually zero, and it gradually increases.
	 * 
	 **************************************************************************/
	protected long getRemainingTimeout(long alreadySpent) {

		int timeout = DEFAULT_JOB_TIMEOUT;
		return ((timeout * 1000) - alreadySpent);
	}

	/***************************************************************************
	 * Return a thread that monitors a submitted job until it is completed. The
	 * core is done in method 'processJobStatus' - look there for details if you
	 * wish to overwrite the watching thread.
	 **************************************************************************/
	protected Thread getStateWatchingThread() {
		return new StateWatchingThread();
	}


	abstract protected String checkStatus();
	abstract protected byte[] retrieveResults();

	/***************************************************************************
	 * Find the status of a webservices job identified by given 'jobId', and
	 * pass it (perhaps adjusted) to the Soaplab's reporter. This is quite
	 * important because other methods regularly check the status in the
	 * reporter, and may change their behaviour according to it.
	 * <p>
	 * 
	 * If the status cannot be retrieved, report it but continue.
	 * <p>
	 * 
	 * If the status indicates that the job was completed, change the Soaplab's
	 * minor state description to "Retrieving results" but keep the major state
	 * still RUNNING. Then invoke a result retriever that will bring job results
	 * here. Only after that, change the major Soaplab's state to COMPLETED (or
	 * TERMINATED_BY_ERROR).
	 * <p>
	 * 
	 * When a job is completed, report also status details, and exit code.
	 * <p>
	 * 
	 **************************************************************************/
	protected void processJobStatus() {
			String status = checkStatus();
			reporter.sendEvent(new HeartbeatProgressEvent(id, status));
			if (status.equals("FAILED")) {
				reporter.getState().setDescription(status);
				reporter.getState().set(JobState.TERMINATED_BY_ERROR, "-2");
			} else if (status.equals("DONE")) {
				byte[] r = retrieveResults();
				if (r!= null){
				    String r_ = new String(r);
				    int i = r_.indexOf("EXIT CODE ");
				    if ( i!= -1){
				        String ec = r_.substring(i+10, i+12).trim();
				        reporter.getState().set(JobState.TERMINATED_BY_ERROR, ec);
				    }
				    reporter.getState().set(JobState.COMPLETED);
				}
				else
				    reporter.getState().set(JobState.COMPLETED);
				reporter.getState().setDescription(status);
				reporter.report("Service job is now complete: "+this.id+"\n");
			}		
	}

	
    /**************************************************************************
     * Report a list of input data.
     *************************************************************************/
    protected void reportInputs() {
    StringBuilder buf = new StringBuilder (500);
    buf.append ("Parameters:\n");
    for (String key: inputs.keySet()){
       buf.append(key+" - "+inputs.get(key)+"\n");
    }
    buf.append ("--- end of parameters\n");
    report (new String (buf));
    }

}
