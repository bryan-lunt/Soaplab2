// ChoiceParameter.java
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
import org.soaplab.services.metadata.ChoiceParamDef;
import org.soaplab.tools.DefaultParameterAccessor;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ArrayUtils;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This parameter represents a "radio-button" type. Which means that
 * zero or one value is sent for this parameter. Internally, each of
 * the possible options for this parameter is defined in a separate
 * ParamDef structure. <p>
 *
 * There is no support for repetitions of this
 * parameter. Theoretically, it would be possible, but I think that
 * using it would be confusing, and therefore rarely used anyway. If
 * it is needed, a StdParameter with static list of possible values
 * can be used instead. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: ChoiceParameter.java,v 1.6 2007/04/19 22:17:05 marsenger Exp $
 */

public class ChoiceParameter
    extends BaseParameter {

    /**************************************************************************
     * Constructor getting this parameter metadata.
     **************************************************************************/
    public ChoiceParameter (ChoiceParamDef paramDef) {
	super (paramDef);
    }

    /**************************************************************************
     * Here we deal only with non-repetitive values, therefore, an
     * expected element type in 'inputs' is String - and this
     * string is considered to have boolean contents.
     *
     * Only one value from 'inputs' is used (which is where
     * ChoiceListParameter differs from this one) - so we stop when we
     * find the first sub-parameter belonging here.
     **************************************************************************/
    protected List<String> build (Map<String,Object> inputs,
				  Job job,
				  String tagPrefix)
	throws ParameterException {

	String[] values = getValues (inputs, job);
	if (values.length == 0) {
	    // no value found, but it was allowed because of type
	    // ZERO_OR_ONE (otherwise it would throw an exception)
	    return CmdLineJob.EMPTY_LIST;
	}

	// using only the first element (should be the only one, anyway)
	return createArgFromSubBool (values[0], tagPrefix);
    }

    /*************************************************************************
     * Similar as 'build' but creating different structure.
     *************************************************************************/
    protected NamedValues buildNamed (Map<String,Object> inputs,
				      Job job)
	throws ParameterException {
	String[] values = getValues (inputs, job);
	if (values.length == 0) {
	    // no value found, but it was allowed because of type
	    // ZERO_OR_ONE (otherwise it would throw an exception)
	    return null;
	}

	// using only the first element (should be the only one, anyway)
	NamedValues result = new NamedValues (paramDef.id);
	result.setValues (createArgFromSubBool (values[0], ""));
	return result;
    }

    /**************************************************************************
     * Extract and return value for this parameter from 'inputs', or
     * from default. Actually, it does not return a value from
     * 'inputs', but if the 'inputs' have a TRUE value for this
     * parameter, then the tag of this parameter is returned as its
     * value.
     *
     * Return an empty array (not null) if there was no value in
     * 'inputs' and no default value is defined.
     *
     * The returned array is expected to have always maximum one
     * element.
     *
     * Parameter 'job' (where this parameter belongs to) is not used
     * here.
     **************************************************************************/
    protected String[] getValues (Map<String,Object> inputs,
				  Job job)
	throws ParameterException {

	// look for values of individual 'bools' which are subparts of
	// this parameter, stop when the first is found
	for (ParamDef boolDef: ((ChoiceParamDef)paramDef).bools) {

	    // extract value from input - look only for TRUE,
	    // and ignore default value, for now
	    Object value = ignoreArray (inputs.get (boolDef.id));
	    if (value != null && StringUtils.isNotBlank (value.toString()))
		if (BooleanUtils.toBoolean (value.toString())) {

		    // a TRUE value found
		    return new String[] { boolDef.get (ParamDef.TAG) };
		}
	}

	// for ZERO_OR_ONE type, we do not need to use defaults, just
	// return an empty array
	if (((ChoiceParamDef)paramDef).choiceType == ChoiceParamDef.ZERO_OR_ONE)
	    return ArrayUtils.EMPTY_STRING_ARRAY;    // no defaults needed

	// for JUST_ONE type, if we are here it means that no value
	// was found - not for a single boolean sub-parameter; find
	// the first (should be the only one) default value
	for (ParamDef boolDef: ((ChoiceParamDef)paramDef).bools) {

	    if (BooleanUtils.toBoolean (boolDef.dflt)) {

		// a TRUE default value found
                return new String[] { boolDef.get (ParamDef.TAG) };
	    }
	}
    
	// if we are here it means that no value was found for
	// JUST_ONE type (and no default was specified)
	throw new ParameterException (paramDef.id,
				      "At least one option must be specified.");
    }

    /**************************************************************************
     * 'boolValue' is usually a TAG of the current sub-bool parameter,
     * but if this method is called from ChoiceListParameter then it
     * can be several sub-bool tags already joined together.
     *
     * Note, that the name 'boolValue' does not mean a boolean value.
     *
     * 'tagPrefix' is used when a tag is used (more about it explained
     * in the 'taggedArg' method): it is usually CMD for command-line
     * arguments, and an empty string for environment variables.
     **************************************************************************/
    protected List<String> createArgFromSubBool (String boolValue,
						 String tagPrefix) {
         
	// no METHOD defined, so do it traditionally with tags...
	if (StringUtils.isBlank (paramDef.get (ParamDef.METHOD)))
	    return taggedArg (tagPrefix, paramDef.get (ParamDef.TAG), boolValue);

	// ...or use parameter template METHOD
	return createArgByMethod
	    (new DefaultParameterAccessor (paramDef.id,
					   paramDef.get (ParamDef.TAG),
					   new String[] { boolValue }));
    }


    //
    // Methods implementing interface Parameter
    //

    /******************************************************************************
     *
     ******************************************************************************/
    public String[] createArg (Map<String,Object> inputs, Job job)
	throws ParameterException {

	if (paramDef.is (ParamDef.ENVAR))
	    return ArrayUtils.EMPTY_STRING_ARRAY;

	return build (inputs, job, CMD).toArray (new String[] {});
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
