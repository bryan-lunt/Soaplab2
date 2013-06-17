// CmdLineJob.java
//
// Created: November 2006
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

package org.soaplab.services.cmdline;

import org.soaplab.share.SoaplabException;
import org.soaplab.share.SoaplabConstants;
import org.soaplab.services.AbstractJob;
import org.soaplab.services.Reporter;
import org.soaplab.services.IOData;
import org.soaplab.services.metadata.MetadataAccessor;
import org.soaplab.services.metadata.ParamDef;
import org.soaplab.services.metadata.StdParamDef;
import org.soaplab.services.metadata.IOParamDef;
import org.soaplab.services.metadata.ChoiceParamDef;
import org.soaplab.services.metadata.AnalysisDef;
import org.soaplab.tools.DefaultParameterAccessor;
import org.soaplab.tools.Substitutor;
import org.soaplab.tools.ICreator;

import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.Properties;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * An abstract parent of jobs that need to create any kind of command
 * line (from metadata and from the user real inputs). Typical example
 * of such kind of job is a job invoking command-line analysis, or a
 * job creating job description files for grids. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: CmdLineJob.java,v 1.21 2007/10/23 08:20:53 marsenger Exp $
 */

public abstract class CmdLineJob
    extends AbstractJob {

    public static final List<String> EMPTY_LIST = new ArrayList<String>();
    public static final Properties EMPTY_PROPS = new Properties();
    public static final IOData[] EMPTY_IODATA = new IOData[] {};
    public static final NamedValues[] EMPTY_NAMED = new NamedValues[] {};

    /**************************************************************************
     * An empty constructor (for sub-classes).
     **************************************************************************/
    protected CmdLineJob() {
    }

    /**************************************************************************
     * The main constructor.
     **************************************************************************/
    protected CmdLineJob (String jobId,
			  MetadataAccessor metadataAccessor,
			  Reporter reporter,
			  Map<String,Object> sharedAttributes,
			  boolean jobRecreated)
	throws SoaplabException {
	super (jobId, metadataAccessor, reporter,
	       sharedAttributes, jobRecreated);
    }

    /**************************************************************************
     * From service metadata, create representations of individual
     * parameters, and store them in the shared attributes (because
     * they can be shared by all job instances of this service, they
     * are not dependent on the user real inputs). This can be done
     * only once (per service) - that's why the method is called
     * 'init'. <p>
     *
     * The name of this shared attribute is {@link #JOB_SHARED_PARAMETERS}. <p>
     *
     **************************************************************************/
    protected synchronized void init()
	throws SoaplabException {

	if (sharedAttributes.containsKey (JOB_SHARED_PARAMETERS) &&
	    ! jobRecreated)
	    return;

	// sort parameter definitions by their ordering
        ParamDef[] paramDefs = metadataAccessor.getParamDefs();
	Arrays.sort (paramDefs, new Comparator<ParamDef>() {
	    public int compare (ParamDef a, ParamDef b) {
		return a.getInt (ParamDef.ORDERING) - b.getInt (ParamDef.ORDERING);
	    }
	});

	Parameter[] parameters = new Parameter [paramDefs.length];
	for (int i = 0; i < paramDefs.length; i++) {
	    ParamDef def = paramDefs[i];

	    // try a user-defined parameter class first
	    String className = def.get (ParamDef.SERVER_CLASS);
	    if (StringUtils.isNotEmpty (className)) {
		try {
		    // all our Parameters have one-arg constructor but
		    // perhaps not all are ours - so try first and ignore errors
		    parameters[i] = (Parameter)ICreator.createInstance
			(className,
			 new Class[] { ParamDef.class },
			 new Object[] { def });
		} catch (SoaplabException e) {
		    // ...now go for a non-args constructor
		    parameters[i] = (Parameter)ICreator.createInstance (className);
		}
		continue;
	    }

	    // then go for usual parameters
	    switch (def.createdFor) {
	    case ParamDef.BASE_PARAMETER:
		parameters[i] = new BaseParameter (def);
		break;
	    case ParamDef.RANGE_PARAMETER:
		// intentionally no break here
	    case ParamDef.STANDARD_PARAMETER:
		parameters[i] = new StdParameter ((StdParamDef)def);
		break;
 	    case ParamDef.IO_PARAMETER:
 		parameters[i] = new IOParameter ((IOParamDef)def);
 		break;
	    case ParamDef.CHOICE_PARAMETER:
		parameters[i] = new ChoiceParameter ((ChoiceParamDef)def);
		break;
	    case ParamDef.CHOICE_LIST_PARAMETER:
		parameters[i] = new ChoiceListParameter ((ChoiceParamDef)def);
		break;
	    default:
		parameters[i] = new BaseParameter (def);
	    }
	}
	sharedAttributes.put (JOB_SHARED_PARAMETERS, parameters);
    }

    /**************************************************************************
     * Create all arguments for a command-line from individual
     * parameters (they are in 'sharedAttributes') and from the user
     * inputs (they are in 'inputs'). <p>
     *
     * Return an empty list (not null) if there are no command-line
     * arguments.
     **************************************************************************/
    protected List<String> createArgs()
	throws SoaplabException {

	Parameter[] parameters =
	    (Parameter[])sharedAttributes.get (JOB_SHARED_PARAMETERS);
	if (parameters == null)
	    return EMPTY_LIST;

	AnalysisDef analysisDef = metadataAccessor.getAnalysisDef();

	List<String> cmdline = new ArrayList<String>();
	StringBuilder errBuf = new StringBuilder();

	// be prepared for applying a global 'method'
	boolean methodUsed = StringUtils.isNotBlank (analysisDef.method);
	DefaultParameterAccessor accessor = null;
        if (methodUsed)
	    accessor = new DefaultParameterAccessor();

	// concatenate all arguments created by individual parameters
        ParamDef paramDef;
	String[] args;
	for (Parameter parameter: parameters) {
            paramDef = parameter.getDefinition();
	    try {
		args = parameter.createArg (inputs, this);
		if (methodUsed)
                    accessor.add (paramDef.id, paramDef.get (ParamDef.TAG), args);
		else
		    for (String arg: args) { cmdline.add (arg); }
	    } catch (ParameterException e) {
		errBuf.append (e.getMessage());
		errBuf.append ("\n");
	    } catch (Exception e) {
		throw new SoaplabException (getServiceName() + ": " +
					    "Unexpected exception. " +
					    e.getMessage(),
					    e);
	    }
	}
	if (errBuf.length() > 0)
	    throw new SoaplabException (SoaplabConstants.FAULT_NOT_VALID_INPUTS +
					"\n" + errBuf.toString());

	// apply global 'method' to create (finally) command-line arguments
	if (methodUsed) {
	    Substitutor engine =
		new Substitutor (analysisDef.method,
				 analysisDef.get (ParamDef.SEPARATOR),
				 accessor);
	    for (String arg: engine.process()) {
		cmdline.add (arg);
	    }
	}

	return cmdline;
    }

    /**************************************************************************
     *
     * Return an empty list (not null) if there are no command-line
     * arguments, or if there is a global METHOD defined.
     **************************************************************************/
    protected NamedValues[] createNamedArgs()
	throws SoaplabException {

	Parameter[] parameters =
	    (Parameter[])sharedAttributes.get (JOB_SHARED_PARAMETERS);
	if (parameters == null)
	    return EMPTY_NAMED;

	// be prepared for a global 'method'
	AnalysisDef analysisDef = metadataAccessor.getAnalysisDef();
	if (StringUtils.isNotBlank (analysisDef.method)) {
	    NamedValues oneNamed = new NamedValues();
	    oneNamed.setValues (createArgs());
	    return new NamedValues[] { oneNamed };
	}

	// get named values created by individual parameters
	StringBuilder errBuf = new StringBuilder();
	List<NamedValues> result = new ArrayList<NamedValues>();
	for (Parameter parameter: parameters) {
	    try {
		NamedValues named = parameter.createNamedArg (inputs, this);
		if (named != null)
		    result.add (named);
	    } catch (ParameterException e) {
		errBuf.append (e.getMessage());
		errBuf.append ("\n");
	    } catch (Exception e) {
		throw new SoaplabException (getServiceName() + ": " +
					    "Unexpected exception. " +
					    e.getMessage(),
					    e);
	    }
	}
	if (errBuf.length() > 0)
	    throw new SoaplabException (SoaplabConstants.FAULT_NOT_VALID_INPUTS +
					"\n" + errBuf.toString());
	return result.toArray (new NamedValues[] {});
    }

    /**************************************************************************
     * Create all environment variables from individual parameters
     * (from those who claim ENVAR in their metadata) and from the
     * user inputs (they are in 'inputs'). <p>
     *
     * Return an empty properties (not null) if there are no
     * environment variables to create. <p>
     **************************************************************************/
    protected Properties createEnvs()
	throws SoaplabException {

	Parameter[] parameters =
	    (Parameter[])sharedAttributes.get (JOB_SHARED_PARAMETERS);
	if (parameters == null)
	    return EMPTY_PROPS;

	Properties envProps = new Properties();
	StringBuilder errBuf = new StringBuilder();

	for (Parameter parameter: parameters) {
	    try {
		Properties props = parameter.createEnv (inputs, this);
		for (Enumeration en = props.propertyNames(); en.hasMoreElements(); ) {
		    String key = (String)en.nextElement();
		    envProps.put (key, props.get (key));
		}
	    } catch (ParameterException e) {
		errBuf.append (e.getMessage());
		errBuf.append ("\n");
	    } catch (Exception e) {
		throw new SoaplabException (getServiceName() + ": " +
					    "Unexpected exception. " +
					    e.getMessage(),
					    e);
	    }
	}
	if (errBuf.length() > 0)
	    throw new SoaplabException (SoaplabConstants.FAULT_NOT_VALID_INPUTS +
					"\n" + errBuf.toString());
	return envProps;
    }

    /**************************************************************************
     * Create an array of all data containers from individual
     * parameters (from those who represent inputs and outputs) and
     * from the user inputs (they are in 'inputs'). <p>
     *
     * Return an empty array (not null) if there are no
     * inputs/outputs to create (which is rare). <p>
     **************************************************************************/
    protected IOData[] createIO()
	throws SoaplabException {

	Parameter[] parameters =
	    (Parameter[])sharedAttributes.get (JOB_SHARED_PARAMETERS);
	if (parameters == null)
	    return EMPTY_IODATA;

	List<IOData> ioList = new ArrayList<IOData>();
	StringBuilder errBuf = new StringBuilder();

	for (Parameter parameter: parameters) {
	    try {
		IOData[] ioData = parameter.createIO (inputs, this);
		for (IOData io: ioData) { ioList.add (io); }
	    } catch (ParameterException e) {
		errBuf.append (e.getMessage());
		errBuf.append ("\n");
	    } catch (Exception e) {
		throw new SoaplabException (getServiceName() + ": " +
					    "Unexpected exception. " +
					    e.getMessage(),
					    e);
	    }
	}
	if (errBuf.length() > 0)
	    throw new SoaplabException (SoaplabConstants.FAULT_NOT_VALID_INPUTS +
					"\n" + errBuf.toString());
	return ioList.toArray (new IOData[] {});
    }

    /**************************************************************************
     *
     **************************************************************************/
    protected String getExecutableName()
	throws SoaplabException {

	AnalysisDef analysisDef = metadataAccessor.getAnalysisDef();
	return analysisDef.file;
    }

    /**************************************************************************
     * Adding calls to individual parameters to run their own validation.
     **************************************************************************/
    protected void checkInputs()
	throws SoaplabException {
	super.checkInputs();

	StringBuilder errBuf = new StringBuilder();

	// call individual parameters to add their own validation
	Parameter[] parameters =
	    (Parameter[])sharedAttributes.get (JOB_SHARED_PARAMETERS);
	if (parameters != null) {
	    for (Parameter p: parameters) {
		try {
		    p.validate (inputs, this, null);
		} catch (ParameterException e) {
		    errBuf.append (e.getMessage());
		    errBuf.append ("\n");
		}
	    }
	}

	// any errors to report?
	reportErrors (errBuf);
    }

}
