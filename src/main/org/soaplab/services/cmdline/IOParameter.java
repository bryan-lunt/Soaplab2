// IOParameter.java
//
// Created: December 2006
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
import org.soaplab.services.Job;
import org.soaplab.services.IOData;
import org.soaplab.services.metadata.ParamDef;
import org.soaplab.services.metadata.IOParamDef;
import org.soaplab.services.adaptor.DataAdaptor;
import org.soaplab.tools.DefaultParameterAccessor;
import org.soaplab.tools.ICreator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.ArrayUtils;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * A class representing parameter defining input and output data - the
 * "real" input and output data (like file names), not just
 * command-line options. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: IOParameter.java,v 1.24 2009/11/09 09:16:26 mahmutuludag Exp $
 */

public class IOParameter
    extends StdParameter {

    final protected static String ERR_NOT_TOGETHER =
	"Cannot be sent together with the parameter ";

    /**************************************************************************
     * Constructor getting this parameter metadata.
     **************************************************************************/
    public IOParameter (IOParamDef paramDef) {
	super (paramDef);
    }

    // method 'build()' stores here data containers
    // (that's why the build() is synchronized)
    protected List<IOData> ioData = new ArrayList<IOData>();

    /**************************************************************************
     * Explore user 'inputs' in order to find if they contain direct
     * data or reference data:
     *
     * - If the parameter definition allows only one kind of data
     * (direct of reference, but not both), don't even look into
     * inputs and return true if only direct data are allowed, or
     * false if only reference data are allowed.
     *
     * - If they contain both, direct and reference data, raise an
     * exception.
     *
     * - If they contain direct data, return true.
     *
     * - If they contain reference data, return false.
     *
     * - If they do not contain any data for this parameter, return
     * false, as well (this method's result will not be actually used
     * in such case, so it does not matter what is returned).
     **************************************************************************/
    protected boolean isValueDirect (Map<String,Object> inputs)
	throws ParameterException {

	// first simple cases - decide even without looking into inputs
	if ( ((IOParamDef)paramDef).isDirectData() )
	    return true;
	if ( ((IOParamDef)paramDef).isReferenceData() )
	    return false;

	// ...and the usual case
	String directName = paramDef.id + SoaplabConstants.DIRECT_DATA_SUFFIX;
	if ( inputs.containsKey (directName) ) {
	    // found direct data: check that we have *only* them
	    String referenceName1 = paramDef.id + SoaplabConstants.URL_SUFFIX;
	    String referenceName2 = paramDef.id + SoaplabConstants.USA_SUFFIX;
	    if ( inputs.containsKey (referenceName1) )
		throw new ParameterException (referenceName1,
					      ERR_NOT_TOGETHER + directName);
	    else if ( inputs.containsKey (referenceName2) )
		throw new ParameterException (referenceName1,
					      ERR_NOT_TOGETHER + directName);
	    else
		// fine; we have just direct data
		return true;
	} else {
	    // otherwise return false (either reference or none data)
	    return false;
	}
    }

    /**************************************************************************
     * Extract from 'inputs' a value (may be none). In order to find a
     * value, use an ID that is suggested by 'isDirect' and by this
     * parameter's definition. 'isDirect' is true if the real value in
     * 'inputs' represents direct data. [The 'isDirect' was just
     * created by method isValueDirect().]
     **************************************************************************/
    protected Object getRealValue (Map<String,Object> inputs,
				   boolean isDirect) {

	// first simple cases - use unchanged ID
	if ( ((IOParamDef)paramDef).isDirectData() ||
	     ((IOParamDef)paramDef).isReferenceData() )
	    return inputs.get (paramDef.id);

	// direct data?
	if (isDirect)
	    return inputs.get (paramDef.id + SoaplabConstants.DIRECT_DATA_SUFFIX);

	// reference data (may be of two kinds)
	if (inputs.containsKey (paramDef.id + SoaplabConstants.URL_SUFFIX))
	    return inputs.get (paramDef.id + SoaplabConstants.URL_SUFFIX);
	return inputs.get (paramDef.id + SoaplabConstants.USA_SUFFIX);
    }

    /**************************************************************************
     *
     **************************************************************************/
    protected synchronized List<String> build (Map<String,Object> inputs,
					       Job job,
					       String tagPrefix)
	throws ParameterException {

	// clean-up
	// (TBD: this is bad, it makes this class not thread-safe...)
	ioData = new ArrayList<IOData>();

	// get values
	String[] values = null;
	boolean isDirect = isValueDirect (inputs);
	Object value = adaptInput (getRealValue (inputs, isDirect), job);
	if (value == null) {
	    String defaultValue = paramDef.dflt;
	    isDirect = paramDef.is (SoaplabConstants.DEFAULT_FOR_DIRECT);

	    // ignore default value for outputs (unless it is a name
	    // of a standard stream)
	    if ( ((IOParamDef)paramDef).isRegularOutput() )
		defaultValue = "";

	    // ignore default value for standard streams (because
	    // it contains just a name of such stream)
	    if (StringUtils.isNotEmpty (defaultValue) &&
		! ((IOParamDef)paramDef).isStandardStream()) {
		values = new String[] { addToIOData (isDirect, defaultValue, job, "") };
	    } else {
		if (((IOParamDef)paramDef).isRegularInput()) {
		    // here we have to check mandatorness
		    if (paramDef.is (ParamDef.MANDATORY)) {
			throw new ParameterException
			    (paramDef.id,
			     "Input is mandatory and cannot remain empty.");
		    }
		    values = ArrayUtils.EMPTY_STRING_ARRAY;
		} else {
		    // except for regular INPUT, we need to create an IOData
		    String ref = null;
		    if (((IOParamDef)paramDef).isStdin()) {
			// ...and empty file for STDIN
			ref = addToIOData (true, "", job, "");
		    } else {
			// ...and only a name of a file for outputs  // STDOUT and STDERR
			ref = addToIOData (false, null, job, "");
		    }
		    if (ref == null || ((IOParamDef)paramDef).isStandardStream()) {
			values = ArrayUtils.EMPTY_STRING_ARRAY;
		    } else {
			values = new String[] { ref };
		    }
		}
	    }

	} else if (value.getClass().isArray() && ! (value instanceof byte[])) {
	    Object[] vals = (Object[])value;
	    values = new String [vals.length];
	    for (int i = 0; i < vals.length; i++) {
		values[i] = addToIOData (isDirect, vals[i], job, String.format ("%03d", i));
	    }
	} else {
	    values = new String[] { addToIOData (isDirect, value, job, "") };
	}

	// check values
	checkRepeatability (values.length);
	if (values.length == 0)
	    return CmdLineJob.EMPTY_LIST;

	// no argument is needed for STDIN
	if (((IOParamDef)paramDef).isStdin())
	    return CmdLineJob.EMPTY_LIST;

	// build args from values
	if (StringUtils.isBlank (paramDef.get (ParamDef.METHOD))) {

	    // no METHOD defined, so do it traditionally, with tags
	    List<String> result = new ArrayList<String>();
	    String tag = paramDef.get (ParamDef.TAG);
	    for (int i = 0; i < values.length; i++)
		result.addAll (taggedArg (tagPrefix, tag, values[i]));
	    return result;

	} else {
	    // use parameter template (METHOD) to create one or more
	    // command-line arguments
	    return createArgByMethod
		(new DefaultParameterAccessor (paramDef.id,
					       paramDef.get (ParamDef.TAG),
					       values));
	}
    }

    /**************************************************************************
     * Create an IOData object (using given 'data' - a value, a job in
     * charge, and an 'id' for repetitive values), add it to the global
     * 'ioData', and return its reference (that can be used in the
     * command-line arguments). The returned reference could be null.
     **************************************************************************/
    protected String addToIOData (boolean isDirect,
				  Object data,
				  Job job,
				  String id)
	throws ParameterException {
	try {
	    IOData io = new IOData ((IOParamDef)paramDef, job, id);
	    if (data != null)
		io.setData (isDirect, data);
	    ioData.add (io);
	    return io.getReferenceName();
	} catch (SoaplabException e) {
	    throw new ParameterException (paramDef.id, e.getMessage());
	}
    }

    /**************************************************************************
     *
     **************************************************************************/
    protected Object adaptInput (Object input, Job job)
	throws ParameterException {

	if (input == null)
	    return null;

	// do we have an input adaptor?
	String adaptorClassName =
	    paramDef.get (IOParamDef.INPUT_ADAPTOR);
	if (StringUtils.isBlank (adaptorClassName))
	    return input;

	try {
	    DataAdaptor adaptor =
		(DataAdaptor)ICreator.createInstance (adaptorClassName);
	    Class inputClass = input.getClass();
	    if (inputClass.isArray() &&
		! inputClass.getComponentType().equals (byte.class)) {

		// case 1: input is an array of "something"
		return adaptor.processFromList (paramDef.id,
						Arrays.asList ((Object[])input),
						job);
	    } else {
		// case 2: input is adapted in memory
		// (TBD: here, we could create a local file with the adapted results
		//       but it would interfere with the duties of IOData class;
		//       let's keep it simple for now (but memory more demanding)...)
		InputStream is = null;
		if (input instanceof byte[]) {
		    is = new ByteArrayInputStream ((byte[])input);
		} else {
		    is = new ByteArrayInputStream (input.toString().getBytes());
		}
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		adaptor.process (paramDef.id, is, os, job);
		// return the adapted input as the same type as input
		if (input instanceof byte[]) {
		    return os.toByteArray();
		} else {
		    return os.toString();
		}
	    }

	} catch (SoaplabException e) {
	    throw new ParameterException (paramDef.id, e.getMessage());
	} catch (IOException e) {
	    throw new ParameterException (paramDef.id, e.getMessage());
	}
    }

    /**************************************************************************
     *
     **************************************************************************/
    public IOData[] createIO (Map<String,Object> inputs, Job job)
	throws ParameterException {
	build (inputs, job, "");
	return ioData.toArray (new IOData[] {});
    }

    /**************************************************************************
     * Does nothing. The IO parameters are not part of the "named
     * arguments" (at least, I believe).
     **************************************************************************/
    public NamedValues createNamedArg (Map<String,Object> inputs, Job job)
	throws ParameterException {
	return null;
    }

}
