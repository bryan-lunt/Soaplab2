package org.soaplab.ebi.fasta;

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

import uk.ac.ebi.webservices.fasta.Data;
import uk.ac.ebi.webservices.fasta.InputParams;
import uk.ac.ebi.webservices.fasta.WSFastaProxy;

/**
 * Job class for the EBI Fasta webservices.
 * <p>
 * 
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @author <A HREF="mailto:uludag@ebi.ac.uk">Mahmut Uludag</A>
 * @version $Id: WSJob.java,v 1.12 2008/02/04 12:55:22 mahmutuludag Exp $
 */

public class WSJob extends AbstractWSJob {

	private static Log log = LogFactory.getLog(WSJob.class);

	private WSFastaProxy proxy = null;

	private String outformat = "txt";

	protected static final String PARAM_NUCLEOTIDE = "nucleotide";

	protected static final String PARAM_PROGRAM = "program";

	protected static final String PARAM_MATRIX = "matrix";

	protected static final String PARAM_DB = "db";

	protected static final String PARAM_SCORES = "scores";

	protected static final String PARAM_ALIGNMENTS = "alignments";

	protected static final String PARAM_GAPOPEN = "gapopen";

	protected static final String PARAM_GAPEXT = "gapext";

	protected static final String PARAM_EUPPER = "eupper";

	protected static final String PARAM_ELOWER = "elower";

	protected static final String PARAM_KTUP = "ktup";

	protected static final String PARAM_TOPSTRAND = "topstrand";

	protected static final String PARAM_BOTTOMSTRAND = "bottomstrand";

	protected static final String PARAM_HISTOGRAM = "histogram";

	protected static final String PARAM_SEQRANGE = "seqrange";

	protected static final String PARAM_DBRANGE = "dbrange";

	/***************************************************************************
	 * Main constructor
	 **************************************************************************/
	public WSJob(String jobId, MetadataAccessor metadataAccessor,
			Reporter reporter, Map<String, Object> sharedAttributes,
			boolean jobRecreated) throws SoaplabException {
		super(jobId, metadataAccessor, reporter, sharedAttributes, jobRecreated);
		proxy = new WSFastaProxy();

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
			if (id.equals(PARAM_NUCLEOTIDE)) {
				params.setNucleotide(Boolean.parseBoolean(nvs.getValues()
						.get(0)));
			} else if (id.equals(PARAM_OUTFORMAT)) {
				outformat = nvs.getValues().get(0);
			} else if (id.equals(PARAM_SEARCHTYPE)) {
				String val = nvs.getValues().get(0);
				params.setAsync(val.equals("1") ? new Boolean(true)
						: new Boolean(false));
			} else if (id.equals(PARAM_PROGRAM)) {
				params.setProgram(nvs.getValues().get(0));
			} else if (id.equals(PARAM_MATRIX)) {
				params.setMatrix(nvs.getValues().get(0));
			} else if (id.equals(PARAM_DB)) {
				String db = StringUtils.join(nvs.getValues().toArray(
						new String[] {}), "+");
				params.setDatabase(db);
			} else if (id.equals(PARAM_SCORES)) {
				params.setScores(Integer.getInteger(nvs.getValues().get(0)));
			} else if (id.equals(PARAM_ALIGNMENTS)) {
				params
						.setAlignments(Integer.getInteger(nvs.getValues()
								.get(0)));
			} else if (id.equals(PARAM_GAPOPEN)) {
				params.setGapopen(Integer.getInteger(nvs.getValues().get(0)));
			} else if (id.equals(PARAM_GAPEXT)) {
				params.setGapext(Integer.getInteger(nvs.getValues().get(0)));
			} else if (id.equals(PARAM_EUPPER)) {
				params.setEupper(Float.valueOf(nvs.getValues().get(0)));
			} else if (id.equals(PARAM_ELOWER)) {
				params.setElower(Float.valueOf(nvs.getValues().get(0)));
			} else if (id.equals(PARAM_KTUP)) {
				params.setKtup(Integer.getInteger(nvs.getValues().get(0)));
			} else if (id.equals(PARAM_TOPSTRAND)) {
				params.setTopstrand(Boolean.valueOf(nvs.getValues().get(0)));
			} else if (id.equals(PARAM_BOTTOMSTRAND)) {
				params.setBottomstrand(Boolean.valueOf(nvs.getValues().get(0)));
			} else if (id.equals(PARAM_HISTOGRAM)) {
				params.setHistogram(Boolean.valueOf(nvs.getValues().get(0)));
			} else if (id.equals(PARAM_SEQRANGE)) {
				params.setSeqrange(nvs.getValues().get(0));
			} else if (id.equals(PARAM_DBRANGE)) {
				params.setDbrange(nvs.getValues().get(0));
			}
		}
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
				log
						.error("Problem while reading input '" + io.getId()
								+ "'", e);
				throw new SoaplabException("Problem while reading input '"
						+ io.getId() + "'", e);
			} catch (IOException e) {
				log
						.error("Problem while reading input '" + io.getId()
								+ "'", e);
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

	// inputs (this is filled from global 'inputs')
	protected NamedValues[] formData;

	protected IOData[] fileData;

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
			jobId = proxy.runFasta(params, content);
			log.info("Webservices job submitted: " + jobId);
		} catch (RemoteException e) {
			throw new SoaplabException(
					"Problem while submitting webservices job"
			        +e.getMessage(), e);
		}
		// check and set status
		Thread stateWatching = getStateWatchingThread();
		stateWatching.setName(stateWatching.getName().replace("Thread",
				"StateWatcher"));
		stateWatching.start();
	}

	/**
	 * Retrieves the output of the fasta job
	 */
	protected byte[] retrieveResults() {
		byte[] r = null;
		try {
			if (outformat.equals("xml"))
				r = proxy.poll(jobId, "toolxml");
			else
				r = proxy.poll(jobId, "tooloutput");
			reporter.setResult("result", r);
		} catch (Exception e) {
			reporter.error("Error when retrieving results: " + e.toString());
			SoaplabException.formatAndLog(e, log);
		}
		return r;
	}

}
