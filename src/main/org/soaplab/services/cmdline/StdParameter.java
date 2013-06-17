// StdParameter.java
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

import org.soaplab.services.Job;
import org.soaplab.services.metadata.ParamDef;
import org.soaplab.services.metadata.StdParamDef;
import org.soaplab.services.metadata.RepeatableDef;
import org.soaplab.tools.DefaultParameterAccessor;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.ArrayUtils;

import java.util.Map;
import java.util.Properties;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Parameter} implementation representing a typical
 * command-line argument, consisting from a tag (optionally) and a
 * value. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: StdParameter.java,v 1.11 2007/10/22 17:56:18 marsenger Exp $
 */

public class StdParameter
    extends BaseParameter {

    /**************************************************************************
     * Constructor getting this parameter metadata.
     **************************************************************************/
    public StdParameter (StdParamDef paramDef) {
	super (paramDef);
    }

    /**************************************************************************
     * Extract and return values for this parameter from 'inputs', or
     * from default. Return an empty array (not null) if there was no
     * value in 'inputs' and no default value is defined.
     *
     * The returned value can have more values if this parameter is
     * repetitive (more precisely: if the Object representing this
     * parameter in 'inputs' is an array). This method also checks if
     * the repetitivness is allowed for this parameter.
     *
     * Parameter 'job' (where this parameter belongs to) is not used
     * here.
     **************************************************************************/
    protected String[] getValues (Map<String,Object> inputs,
				  Job job)
	throws ParameterException {

	String[] values = null;
 	Object value = inputs.get (paramDef.id);
	if (value == null) {
	    String defaultValue = getDefaultValue();
	    values = (defaultValue == null ?
		      ArrayUtils.EMPTY_STRING_ARRAY :
		      new String[] { defaultValue });
	} else if (value.getClass().isArray()) {
	    Object[] vals = (Object[])value;
	    values = new String [vals.length];
	    for (int i = 0; i < vals.length; i++)
		values[i] = vals[i].toString();
	} else {
	    values = new String[] { value.toString() };
	}

	// check values
	checkRepeatability (values.length);

	return values;
    }

    /**************************************************************************
     *
     **************************************************************************/
    protected List<String> build (Map<String,Object> inputs,
				  Job job,
				  String tagPrefix)
	throws ParameterException {

	// get values
	String[] values = getValues (inputs, job);
	if (values.length == 0)
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
     * Similar as 'build' but creating different structure.
     **************************************************************************/
    protected NamedValues buildNamed (Map<String,Object> inputs,
				      Job job)
	throws ParameterException {

	// get values
	String[] values = getValues (inputs, job);
	if (values.length == 0)
	    return null;

	// build named values
	NamedValues result = new NamedValues (paramDef.id);
	if (StringUtils.isBlank (paramDef.get (ParamDef.METHOD))) {

	    // no METHOD defined, so keep there a tag
	    result.setTag (paramDef.get (ParamDef.TAG));
	    result.setValues (array2list (values));

	} else {
	    // use parameter template METHOD
	    result.setValues
		(createArgByMethod
		 (new DefaultParameterAccessor (paramDef.id,
						paramDef.get (ParamDef.TAG),
						values)));
	}
	return result;
    }

    /******************************************************************************
     * Check if this parameter can have 'realCount' number of
     * repetitions. Throw an exception if there is a problem.
     ******************************************************************************/
    protected void checkRepeatability (int realCount)
	throws ParameterException {

        RepeatableDef repDef = ((StdParamDef)paramDef).repeat;
	if (realCount < repDef.minRep)
	    repError ("Minimal number of repetition is " +
		      (repDef.minRep == 0 ? "1": repDef.minRep) +
		      " and only " + realCount + " non-empty value(s) were provided.");
    
	int realMaxRep = (repDef.maxRep == 0 ? 1 : repDef.maxRep);
	if (repDef.maxRep > -1 && realCount > realMaxRep)
	    repError ("Maximal number of repetition is " +
		      realMaxRep +
		      " and " + realCount + " non-empty value(s) were provided.");
    }

    /******************************************************************************
     * Throw an exception about the wrong number of repetitions. A
     * parameter-specific message, taken from this parameter metadata,
     * has precedence over given 'msg'.
     ******************************************************************************/
    void repError (String msg)
	throws ParameterException {

        RepeatableDef repDef = ((StdParamDef)paramDef).repeat;
	String userMsg = repDef.get (RepeatableDef.REPEAT_ERROR);
        if (StringUtils.isBlank (userMsg))
	    userMsg = msg;

	throw new ParameterException (paramDef.id, userMsg);
    }


    //
    // Methods implementing interface Parameter
    //

    /**************************************************************************
     *
     **************************************************************************/
    public String[] createArg (Map<String,Object> inputs, Job job)
	throws ParameterException {

	if (paramDef.is (ParamDef.ENVAR))
	    return ArrayUtils.EMPTY_STRING_ARRAY;

	List<String> built = build (inputs, job, CMD);
	return built.toArray (new String[] {});
    }

    /**************************************************************************
     *
     **************************************************************************/
    public Properties createEnv (Map<String,Object> inputs, Job job)
	throws ParameterException {

	if (! paramDef.is (ParamDef.ENVAR))
	    return CmdLineJob.EMPTY_PROPS;

	return list2props (build (inputs, job, ""));
    }

}
