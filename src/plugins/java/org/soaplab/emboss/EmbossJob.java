package org.soaplab.emboss;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.BooleanUtils;
import org.soaplab.services.Config;
import org.soaplab.services.IOData;
import org.soaplab.services.Reporter;
import org.soaplab.services.cmdline.BaseParameter;
import org.soaplab.services.cmdline.Parameter;
import org.soaplab.services.metadata.MetadataAccessor;
import org.soaplab.services.metadata.ParamDef;
import org.soaplab.share.SoaplabConstants;
import org.soaplab.share.SoaplabException;
import org.soaplab.sowa.SowaJob;

/**
 * This is a job for executing EMBOSS applications.
 * 
 * @version $Id: EmbossJob.java,v 1.18 2011/04/06 12:38:53 mahmutuludag Exp $
 */

public class EmbossJob extends SowaJob {

	// String constants used in this class
	private static final String OPT_AUTO = "-auto";
	private static final String OPT_GRAPH = "-graph";
	private static final String OPT_GOUTFILE = "-goutfile";
	private static final String GRAPH_FORMAT = "graph_format";
	public static final String EMBOSS_HOME = "emboss.home";
	private static final String EMBOSS_DATA = "emboss.data";
	private static final String EMBOSS_PATH = "emboss.path";

    // controls the init() method
    private static boolean jobClassInitialized = false;
    
    static String embossData;

    private static org.apache.commons.logging.Log log =
        org.apache.commons.logging.LogFactory.getLog (EmbossJob.class);

	public EmbossJob(String jobId, MetadataAccessor metadataAccessor,
			Reporter reporter, Map<String, Object> sharedAttributes,
			boolean jobRecreated) throws SoaplabException {
		super(jobId, metadataAccessor, reporter, sharedAttributes, jobRecreated);
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

	String embossHome = createEmbossHome();
	// add EMBOSS binaries to the path
	String embossPath = Config.getString (EMBOSS_PATH, null,
					      getServiceName(), this);
	if (embossPath == null && embossHome != null)
	    embossPath = embossHome + File.separator + "bin";
	if (embossPath != null) {
	    Config.get().setProperty (Config.PROP_ADDTOPATH_DIR,
				      (String[])ArrayUtils.addAll
				      ( new String[] { embossPath },
					Config.get().getStringArray (Config.PROP_ADDTOPATH_DIR) ));
	}
    }
    
    
    /**
     * sets environment variables for EMBOSS - put them into
     * current configuration (the method getProcessBuilder() takes
     * care about propagating them into process environment
     */
    private String createEmbossHome()
    {
        String embossHome = Config.getString(EMBOSS_HOME, null,
                getServiceName(), this);
        if (embossHome != null)
            Config.get().setProperty(Config.PROP_ENVAR + ".EMBOSS_HOME",
                    embossHome);

        embossData = Config.getString(EMBOSS_DATA, null,
                getServiceName(), this);

        if (embossData != null)
            Config.get().setProperty(Config.PROP_ENVAR + ".EMBOSS_DATA",
                    embossData);
        else if (embossHome != null)
            embossData = embossHome + File.separator + "share" + File.separator
            + "EMBOSS" + File.separator + "data";
        
        return embossHome;
    }

	/***************************************************************************
     * Deletes the graph options from the command line arguments list
     **************************************************************************/
	@Override
	protected void preExec(ProcessBuilder pb) throws SoaplabException {
		// following call was commented  on EBI servers since Dec 18
		// we were getting null pointer exceptions apparently due to concurrency problems
		// in apache commons configuration libraries
		// this should stop service providers changing EMBOSS parameters without restarting 
		// webapplication. If we really want t have this support synchronizing the
		// createEmbossHome function should probably prevent the concurrency problem arising
		// do we really need this?
        //createEmbossHome();
        String format = checkforGraphFormat();
        List<String> args = pb.command();
        
        if ( format != null )
        for (int i = 0; i < args.size(); i++) {
            String arg = args.get(i);
            if ((arg.startsWith("%%%Graphics_in_PNG%%%") && format
                    .equals("colourps"))
                    || (arg.startsWith("%%%Graphics_in_Postscript%%%") && format
                            .equals("png"))) {
                args.remove(i);
                args.remove(i--);
            } else if ((arg.startsWith("%%%Graphics_in_PNG%%%") && format
                    .equals("png"))
                    || (arg.startsWith("%%%Graphics_in_Postscript%%%") && format
                            .equals("colourps"))) {
                args.remove(i);
                args.set(i, OPT_GOUTFILE + "=" + args.get(i));
            }
        }
        checkforFalseValuesOfBooleanParameters(args);
        // EMBOSS -auto option turns off prompts and disables reporting the
        // one-line descriptions
        args.add(OPT_AUTO);
        if (format != null) {
            String opt = OPT_GRAPH + "=" + format;
            args.add(opt);
        }
        // following is a workaround while we update metadata xml files to include an option
        // that can be used for specifying format for output alignments  
        for (String key:inputs.keySet()){
            if (key.startsWith("aformat_")){
                args.add("-"+key+"="+inputs.get(key));
            }
        }
        super.preExec(pb);
    }


	
	
	@Override
	protected void processResults(IOData[] ioData) throws SoaplabException {
	    for (IOData io : ioData) {
	        if (io.getDefinition().isOutput()) {
	            File data = io.getData();
	            if (data == null)
	                continue;
	            if (data.exists()) {
	                reporter.setResult(io.getDefinition().id, data);
	            } else {
	                File dir = data.getParentFile();
	                if (dir==null)
	                    continue;
	                final String fname = data.getName();
	                FilenameFilter filter = new FilenameFilter() {
	                    public boolean accept(File dir, String name) {
	                        if (name.startsWith(fname + "."))
	                            return true;
	                        return false;
	                    }
	                };
	                File[] files = dir.listFiles(filter);
	                if (files.length > 0) {	                    
	                    String filename = files[0].getName();
	                    String ext = FilenameUtils.getExtension(filename);
	                    io.getDefinition().options.put (SoaplabConstants.FILE_EXT, ext);
	                    if (io.getDefinition().type.equals("string"))
	                        reporter.setResult(io.getDefinition().id, files[0]);
	                    else
	                        reporter.setResult(io.getDefinition().id, files);
	                }
	            }
	        }
	    }
	}

	/**
	 * Checks the graph_format option, if it is null then returns the default
	 * graph format
	 */
	private String checkforGraphFormat() {
	    for (Parameter p : (Parameter[])sharedAttributes.get (JOB_SHARED_PARAMETERS)) {
			if (p.getDefinition().id.equals(GRAPH_FORMAT)) {
				String format = (String) inputs.get(GRAPH_FORMAT);
				if (format == null)
					format = p.getDefinition().dflt;
				return format;
			}
		}
		return null;
	}
	

	/**
     * Checks for 'false' values of boolean parameters with a default value 'true'. 
     * If the user specified value is 'false' then adds a new command line argument
     * that starts with the prefix "-no" followed by the parameter name.
     */
    private void checkforFalseValuesOfBooleanParameters(List<String> args) {
        for (Parameter p : (Parameter[]) sharedAttributes
                .get(JOB_SHARED_PARAMETERS)) {
            ParamDef paramDef = p.getDefinition();
            if ((p instanceof BaseParameter)
                    && inputs.get(paramDef.id) != null
                    && !BooleanUtils.toBoolean(inputs.get(paramDef.id)
                            .toString()) && paramDef.type.equals("boolean")) {
                args.add("-no" + paramDef.id);
            }
        }
        return;
    }
	
    
    public boolean isDataReferenceSafe (String dataRef, ParamDef paramDef)
    {
        if (dataRef.equals("."))
            try {
                dataRef = getJobDir().getPath();
            } catch (SoaplabException e) {
                log.warn("Problem while getting pathname of job directory. " +
                		e.getMessage());
            }
            
        if( super.isDataReferenceSafe(dataRef, paramDef) )
            return true;
        
        if (embossData==null)
            return false;
        
        File embossData_ = new File(embossData);
        File dataRef_ = new File (dataRef);
        if (dataRef_.getAbsolutePath().startsWith (embossData_.getAbsolutePath()))
            return true;
        return false;
    }
}
