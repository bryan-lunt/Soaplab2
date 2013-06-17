package org.soaplab.ebi.interproscan;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.soaplab.ebi.AbstractWSJob;
import org.soaplab.services.IOData;
import org.soaplab.services.JobState;
import org.soaplab.services.Reporter;
import org.soaplab.services.cmdline.NamedValues;
import org.soaplab.services.metadata.MetadataAccessor;
import org.soaplab.share.SoaplabException;

import uk.ac.ebi.webservices.interproscan.Data;
import uk.ac.ebi.webservices.interproscan.InputParams;
import uk.ac.ebi.webservices.interproscan.WSInterProScanProxy;

/**
 * A plug-in job for the EBI Interproscan webservices.
 * <p>
 * 
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: WSJob.java,v 1.5 2008/03/13 17:00:38 mahmutuludag Exp $
 */

public class WSJob extends AbstractWSJob {

	private static Log log = LogFactory.getLog(WSJob.class);

	private WSInterProScanProxy proxy = null;

	private String outformat = "toolraw";

	/***************************************************************************
	 * The main constructor.
	 **************************************************************************/
	public WSJob(String jobId, MetadataAccessor metadataAccessor,
			Reporter reporter, Map<String, Object> sharedAttributes,
			boolean jobRecreated) throws SoaplabException {
		super(jobId, metadataAccessor, reporter, sharedAttributes, jobRecreated);
		proxy = new WSInterProScanProxy();

	}

	/***************************************************************************
	 * Returns status of the webservices job
	 * 
	 **************************************************************************/
	protected String checkStatus() {
		try {
			return proxy.checkStatus(jobId);
		} catch (RemoteException e) {
			log.error("Problem while querying the job status", e);
			reporter.getState().set(JobState.UNKNOWN);
			reporter.getState().setDescription("UNKNOWN");
		}
		return "UNKNOWN";
	}

	private static final String PARAM_SEQTYPE = "seqtype";
	private static final String PARAM_APPL = "appl";
	private static final String PARAM_CRC = "crc";
	private static final String PARAM_TRTABLE = "trtable";
	private static final String PARAM_TRLEN = "trlen";
	private static final String PARAM_GOTERMS = "goterms";
	
	// inputs (this is filled from global 'inputs')
	protected NamedValues[] formData;

	protected IOData[] fileData;
	
	/**
	 * Transfers the inputs into the form the underlying webservices interface
	 * requires, also fills in the default values of the required parameters in
	 * case user doesn't provide an input for these parameters
	 * 
	 * @return
	 * @throws SoaplabException
	 */
	protected InputParams prepareInputParams() throws SoaplabException {
		InputParams params = new InputParams();
		for (NamedValues nvs : formData) {
			String id = nvs.getId();
			if (id.equals(PARAM_OUTFORMAT)) {
				outformat = nvs.getValues().get(0);
				params.setOutformat(outformat);
			} else if (id.equals(PARAM_SEARCHTYPE)) {
				String val = nvs.getValues().get(0);
				params.setAsync(val.equals("1") ? new Boolean(true)
						: new Boolean(false));
			} else if (id.equals(PARAM_APPL)) {
				String appls = StringUtils.join(nvs.getValues().toArray(
						new String[] {}), "+");
				params.setApp(appls);
			} else if (id.equals(PARAM_CRC)) {
				params.setCrc(Boolean.valueOf(nvs.getValues().get(0)));
			}else if (id.equals(PARAM_SEQTYPE)) {
				params.setSeqtype(nvs.getValues().get(0));
			} else if (id.equals(PARAM_TRTABLE)) {
				params.setTrtable(Integer.getInteger(nvs.getValues().get(0)));
			} else if (id.equals(PARAM_TRLEN)) {
				params.setTrlen(Integer.getInteger(nvs.getValues().get(0)));
			} else if (id.equals(PARAM_GOTERMS)) {
				params.setGoterms(Boolean.valueOf(nvs.getValues().get(0)));
			}
		}
		params.setCrc(new Boolean(true));
		
		// TODO: get this from a configuration file
		params.setEmail("uludag@ebi.ac.uk");
		return params;

	}

	/**
	 * Transfers the input sequence to the form the underlying webservice
	 * requires
	 * 
	 * @return
	 */
	protected Data[] getSequence() throws SoaplabException {
		Vector<Data> v = new Vector<Data>();

		for (IOData io : fileData) {
			if (!io.getDefinition().isRegularInput())
				continue;
			File file = io.getData();
			if (file == null)
				continue;
			try {
				FileReader fr = new FileReader(file);
				char[] a = new char[(int) file.length()];
				fr.read(a);
			} catch (FileNotFoundException e) {
				log.error("Problem while reading input '" + io.getId() + "'", e);
				throw new SoaplabException("Problem while reading input '"
						+ io.getId() + "'", e);
			} catch (IOException e) {
				log.error("Problem while reading input '" + io.getId() + "'", e);
				throw new SoaplabException("Problem while reading input '"
						+ io.getId() + "'", e);
			}
			Data d = new Data();
			d.setContent((String) inputs.get(PARAM_SEQUENCE));
			d.setType("sequence");
			v.add(d);
		}
		return v.toArray(new Data[] {});
	}

	/***************************************************************************
	 * Does not report that a job is completed, it is the StateWatchingThread
	 * who does that.
	 **************************************************************************/
	public void run() throws SoaplabException {
		if (runAllowed()) {
			checkInputs();
			formData = createNamedArgs();
			fileData = createIO();
			Data[] content = getSequence();
			InputParams params = prepareInputParams();
			reporter.getState().set(JobState.RUNNING);
			reportInputs();
			realRun(content, params);
		}
	}

	/**
	 * Calls the webservice and starts a new thread for monitoring the state of
	 * the webservices job
	 * 
	 * @param content
	 * @param params
	 * @throws SoaplabException
	 */
	protected void realRun(Data[] content, InputParams params)
			throws SoaplabException {

		reporter.report("Calling service\n");
		try {
			jobId = proxy.runInterProScan(params, content);
			log.info("Webservices job submitted: " + jobId);
		} catch (RemoteException e) {
			throw new SoaplabException(
					"Problem while submitting webservices job: "
			        +e.getMessage(), e);
		}

		// check and set status
		Thread stateWatching = getStateWatchingThread();
		stateWatching.setName(stateWatching.getName().replace("Thread",
				"StateWatcher"));
		stateWatching.start();
	}

	/**
	 * Retrieves the output of the interproscan job
	 */
	protected byte[] retrieveResults() {
		byte[] r = null;
		try {
		    if (outformat.equals("toolxml"))
		        r = proxy.poll(jobId, "toolxml");
		    else{
		        r = proxy.poll(jobId, "tooloutput");
		    }
		    if (r==null){
		        reporter.error("Null output returned: " + jobId);
		        r = proxy.poll(jobId, "toolerrors");
		        if (r!=null)
		        reporter.error("Error output returned: " + new String(r));
		    }
		    else
		        reporter.setResult("result", r);
		} catch (Exception e) {
			reporter.error("Error when retrieving results: " + e.toString());
			SoaplabException.formatAndLog(e, log);
		}
		return r;
	}

}
