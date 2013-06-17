// ChoiceListParameter.java
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

import org.soaplab.share.SoaplabConstants;
import org.soaplab.services.Job;
import org.soaplab.services.metadata.ParamDef;
import org.soaplab.services.metadata.ChoiceParamDef;
import org.soaplab.tools.DefaultParameterAccessor;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ArrayUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * This parameter represents a "list of check-boxes" type. Which means
 * that zero or more values are sent for this parameter. Internally,
 * each of the possible options for this parameter is defined in a
 * separate ParamDef structure. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: ChoiceListParameter.java,v 1.8 2007/11/12 04:29:14 marsenger Exp $
 */

public class ChoiceListParameter
    extends ChoiceParameter {

    /**************************************************************************
     * Constructor getting this parameter metadata.
     **************************************************************************/
    public ChoiceListParameter (ChoiceParamDef paramDef) {
	super (paramDef);
    }

    /**************************************************************************
     * Here we deal only with non-repetitive values, therefore, an
     * expected element type in 'inputs' is String - and this
     * string is considered to have a boolean contents.
     *
     * The resulting value of this parameter may be created from more
     * user values so we have to go always through all 'inputs'.
     **************************************************************************/
    protected List<String> build (Map<String,Object> inputs,
				  Job job,
				  String tagPrefix)
	throws ParameterException {

	String[] values = getValues (inputs, job);
	if (values.length == 0)
	    return array2list (values);

	// concatenate values if more of them found
        String separator = paramDef.get (ParamDef.SEPARATOR);
        StringBuilder buf = new StringBuilder();
	for (String arg: values) {
	    if (buf.length() > 0) buf.append (separator);
	    buf.append (arg);
	}
        String value = buf.toString();

	// create an argument from concatenated values
        return createArgFromSubBool (value, tagPrefix);
    }

    /*************************************************************************
     * Similar as 'build' but creating different structure.
     *************************************************************************/
    protected NamedValues buildNamed (Map<String,Object> inputs,
				      Job job)
	throws ParameterException {
	String[] values = getValues (inputs, job);
	if (values.length == 0)
	    return null;

	NamedValues result = new NamedValues (paramDef.id);

	if (StringUtils.isBlank (paramDef.get (ParamDef.METHOD))) {

	    // no METHOD defined, so keep the tag
	    result.setTag (paramDef.get (ParamDef.TAG));
	    result.setValues (array2list (values));

	} else {
	    // ...or use parameter template METHOD
	    result.setValues
		(createArgByMethod
		 (new DefaultParameterAccessor (paramDef.id,
						paramDef.get (ParamDef.TAG),
						new String[] { paramDef.get (ParamDef.TAG) })));
	}
	return result;
    }

    /**************************************************************************
     * Extract and return values for this parameter from 'inputs', or
     * from default. Actually, it does not return values from
     * 'inputs', but if the 'inputs' have a TRUE value for this
     * parameter, then the tag of this parameter is returned as its
     * value.
     *
     * Return an empty array (not null) if there was no value in
     * 'inputs' and no default value is defined.
     *
     * The returned value can have more values because this parameter
     * represents zero, one or more booleans.
     *
     * Parameter 'job' (where this parameter belongs to) is not used
     * here.
     **************************************************************************/
    protected String[] getValues (Map<String,Object> inputs,
				  Job job)
	throws ParameterException {

	List<String> values = new ArrayList<String>();

	// look for values of individual 'bools' which are subparts of
	// this parameter, concatenate values if more of them found
	for (ParamDef boolDef: ((ChoiceParamDef)paramDef).bools) {

	    // extract value from input - look only for TRUE,
	    // and ignore default value, for now
	    Object value = ignoreArray (inputs.get (boolDef.id));
	    if (value != null && StringUtils.isNotBlank (value.toString()))
		if (BooleanUtils.toBoolean (value.toString())) {

		    // if a TRUE value found, add it to the result
		    values.add (boolDef.get (ParamDef.TAG));
		}
	}
	if (values.size() > 0)
	    return values.toArray (new String[] {});

	// if we have not found _any_ value...

	boolean useDefaults = paramDef.is (ParamDef.USE_DEFAULTS);

 	// for ZERO_OR_MORE type, and without a need to use defaults,
 	// just return an empty array
 	if ( ((ChoiceParamDef)paramDef).choiceType == ChoiceParamDef.ZERO_OR_MORE &&
	     ! useDefaults ) {
 	    return ArrayUtils.EMPTY_STRING_ARRAY;
	}

	// for ONE_OR_MORE type, look for defaults (if there are more
	// default values I am adding them here all, but AppLab1 was
	// just taking the last found value - what is more correct?)
	for (ParamDef boolDef: ((ChoiceParamDef)paramDef).bools) {

	    // if a positive default found, remember it (it may be
	    // used if USE_DEFAULTS is on)
	    if (BooleanUtils.toBoolean (boolDef.dflt)) {
		values.add (boolDef.get (ParamDef.TAG));
	    }
	}
	if (values.size() > 0 && useDefaults)
	    return values.toArray (new String[] {});

	// if the value is still empty, we have a problem because the
	// choice type must be now ONE_OR_MORE - and we have nothing,
	// what a shame...
	throw new ParameterException (paramDef.id,
				      "At least one option must be specified for: " +
				      paramDef.get (SoaplabConstants.DATA_PROMPT));
    }

}
