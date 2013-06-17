// BaseParameter.java
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

import org.soaplab.services.IOData;
import org.soaplab.services.Job;
import org.soaplab.services.metadata.ParamDef;
import org.soaplab.tools.DefaultParameterAccessor;
import org.soaplab.tools.ParameterAccessor;
import org.soaplab.tools.Substitutor;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ArrayUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

/**
 * A base command-line argument. It can be used on its own, or as a
 * parent class for more specialized arguments. <p>
 *
 * The BaseParameter is actually a boolean argument (an option). Its
 * value (unless defined differently using metadata <tt>METHOD</tt>)
 * is a command-line tag (or its absence). <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: BaseParameter.java,v 1.16 2008/05/16 13:19:27 marsenger Exp $
 */

public class BaseParameter
    implements Parameter {

    private static final org.apache.commons.logging.Log log =
       org.apache.commons.logging.LogFactory.getLog (BaseParameter.class);

    // char used for cmdline tags (if not convenient, use 'method')
    protected static final String CMD = "-";

    // definition (metadata) of this parameter
    protected ParamDef paramDef;

    /**************************************************************************
     * Constructor getting this parameter metadata.
     **************************************************************************/
    public BaseParameter (ParamDef paramDef) {
	this.paramDef = paramDef;
    }

    /*************************************************************************
     * Indicates an internal error, usually something wrong in service
     * metadata, or in general configuration. <p>
     *
     * It logs an error, and throws ParameterException.
     *************************************************************************/
    protected void configError (String msg)
	throws ParameterException {
	ParameterException e =
	    new ParameterException (paramDef.id,
				    "Configuration error: " + msg);
	log.error (e.getMessage());
	throw e;
    }

    /*************************************************************************
     * Create a command line argument from 'tag' (may be empty) and
     * 'value' (should not be null, but may be an empty string, but it
     * is unusual), separated by a space, or as defined in metadata
     * TAGSEPAR. The 'tag' is prepended by 'tagPrefix' (which is
     * usually a minus sign).
     *
     * If 'tag' is empty, both 'tagPrefix' and 'tag' are ignored, and
     * only 'value' (even if it is an empty string) is returned.
     *
     * If TAGSEPAR contains only whitespaces, the 'tag' and 'value'
     * are returned as two different strings (so they will become two
     * different command-line arguments later). Otherwise, one string
     * is returned (a typical use cas would be a TAGSEPAR equal '='
     * which would result in a returned string 'tag=value'). In the
     * latter case, 'tagPrefix' is ignored, as well.
     *************************************************************************/
    protected List<String> taggedArg (String tagPrefix,
				      String tag,
				      String value) {
	if (value == null)
	    return CmdLineJob.EMPTY_LIST;

	if (StringUtils.isBlank (tag))
	    return array2list (value);
	
	String tagsepar;
	if (paramDef.options.containsKey (ParamDef.TAGSEPAR)) {
	    tagsepar = paramDef.get (ParamDef.TAGSEPAR);
	    if (tagsepar.equals ("true"))
		tagsepar = " ";   // a way how to specify default tagsepar
	} else {
	    tagsepar = " ";
	}
	if (StringUtils.isBlank (tagsepar) && StringUtils.isNotEmpty (tagsepar)) {
	    return array2list (tagPrefix + tag, value);
	} else {
	    return array2list (tag + tagsepar + value);
	}
    }

    /**************************************************************************
     *
     **************************************************************************/
    protected List<String> array2list (String... args) {
	List<String> list = new ArrayList<String> (args.length);
	for (String str: args)
	    list.add (str);
	return list;
    }

    /**************************************************************************
     * Convert a list of created arguments into (environment) properties.
     *
     * TBD: for now, until we decide differently, take just the first
     * two elements
     **************************************************************************/
    protected Properties list2props (List<String> args) {

	if (args.size() == 0)
	    return CmdLineJob.EMPTY_PROPS;
	Properties env = new Properties();
	if (args.size() > 1)
	    env.put (args.get (0), args.get (1));
	else
	    env.put (args.get (0), "true");
	return env;
    }
    

    /**************************************************************************
     * If the given 'value' is an array, return just its first
     * element, but log warning about it. Otherwise return the same
     * 'value'. Be aware that the given 'value' may be null, or it can
     * be a byte array.
     *
     * This is used for parameters that should not receive repetitive
     * values.
     **************************************************************************/
    protected Object ignoreArray (Object value) {
	if (value == null) return value;
	
	if (value instanceof byte[])
	    return new String((byte[])value);

	if (value.getClass().isArray()) {
	    log.warn ("'" + paramDef.id +
		      "' got an array input. Second and other elements ignored.");
	    Object[] vals = (Object[])value;
	    if (vals.length > 0) {
		if (vals[0] instanceof byte[])
		    return new String ((byte[])vals[0]);
		else
		    return vals[0];
	    } else {
		return null;
	    }
	}
	return value;
    }

    /*************************************************************************
     * Extract and return value for this parameter from 'inputs', or
     * from default. If there is no input for this parameter, return
     * false.
     *
     * TBD: Perhaps it should be changed to String[] getValues(...),
     * returning an empty array if none inputs, and one element of a
     * boolean character if an input found (as it is with other types
     * of parameters).
     *************************************************************************/
    protected boolean getValue (Map<String,Object> inputs)
	throws ParameterException {

	// get value (real or from default)
	Object objValue = ignoreArray (inputs.get (paramDef.id));
	if (objValue != null && StringUtils.isNotBlank (objValue.toString())) {
	    return BooleanUtils.toBoolean (objValue.toString());
	} else {
	    return BooleanUtils.toBoolean (getDefaultValue());
	}
    }

    /**************************************************************************
     * It returns a default value - but only if the option
     * USE_DEFAULTS is on. It returns null if there is no default
     * value, or if the USE_DEFAULTS is off.
     **************************************************************************/
    protected String getDefaultValue() {
	if (! paramDef.is (ParamDef.USE_DEFAULTS))
	    return null;
	if (StringUtils.isEmpty (paramDef.dflt))
	    return null;
	return paramDef.dflt;
    }

    /*************************************************************************
     * It builds an argument. The created argument does not contain
     * any value from 'inputs', but only TAG (if there was a value in
     * 'inputs'), or whatever is in METHOD.
     *
     * Sometimes, it is good to include even FALSE value (e.g for
     * setting environment variables) in the result. By default, FALSE
     * value is not considered (and empty list is returned), unless
     * 'insistOnFalse' is true.
     *************************************************************************/
    protected List<String> build (Map<String,Object> inputs,
				  Job job,
				  String tagPrefix,
				  boolean insistOnFalse)
	throws ParameterException {

	// get value (real or from default)
	boolean value = getValue (inputs);

	if (! value) {
	    if (insistOnFalse) {
		return taggedArg (tagPrefix, paramDef.get (ParamDef.TAG), "false");
	    } else {
		return CmdLineJob.EMPTY_LIST;
	    }
	}

	if (StringUtils.isBlank (paramDef.get (ParamDef.METHOD))) {

	    // no METHOD defined, so do it traditionally with tags
	    if (StringUtils.isNotBlank (paramDef.get (ParamDef.TAG))) {
		List<String> result = new ArrayList<String>();
		result.add (tagPrefix + paramDef.get (ParamDef.TAG));
		return result;
	    } else {
		configError ("No TAG defined!");
	    }
	} else {
	    // use parameter template METHOD
	    return createArgByMethod
		(new DefaultParameterAccessor (paramDef.id,
					       paramDef.get (ParamDef.TAG),
					       new String[] { paramDef.get (ParamDef.TAG) }));
	}
	return null;   // never comes here
    }

    /*************************************************************************
     * Similar as 'build' but creating different structure.
     *************************************************************************/
    protected NamedValues buildNamed (Map<String,Object> inputs,
				      Job job)
	throws ParameterException {

	// get value (real or from default)
	boolean value = getValue (inputs);
	if (! value)
	    return null;

	// build named values
	NamedValues result = new NamedValues (paramDef.id);
	if (StringUtils.isBlank (paramDef.get (ParamDef.METHOD))) {

	    // no METHOD defined, fill the tag, leave value empty or put there 'truevalue'
	    if (StringUtils.isNotBlank (paramDef.get (ParamDef.TAG))) {
		result.setTag (paramDef.get (ParamDef.TAG));
		String trueValue = paramDef.get (ParamDef.TRUEVALUE);
		if (StringUtils.isNotEmpty (trueValue))
		    result.addValue (trueValue);
	    } else {
		configError ("No TAG defined!");
	    }
	} else {
	    // use parameter template METHOD
	    result.setValues
		(createArgByMethod
		 (new DefaultParameterAccessor (paramDef.id,
						paramDef.get (ParamDef.TAG),
						new String[] { paramDef.get (ParamDef.TAG) })));
	}
	return result;
    }

    /*************************************************************************
     * It creates one or more command line arguments for a given
     * parameter using its METHOD. <p>
     *
     * The values (all repetitions) of this parameter are accessible
     * using given 'paramAccessor' (this accessor is used also by
     * class Substitutor). <p>
     *************************************************************************/
    protected List<String> createArgByMethod (ParameterAccessor paramAccessor) {
	if (paramAccessor.getCount (paramDef.id) == 0 ||
	    StringUtils.isEmpty (paramAccessor.getValue (paramDef.id, 0)))
	    return CmdLineJob.EMPTY_LIST;

	Substitutor engine = new Substitutor (paramDef.get (ParamDef.METHOD),
					      paramDef.get (ParamDef.SEPARATOR),
					      paramAccessor);
	return array2list (engine.process());
    }


    //
    // Methods implementing interface Parameter
    //

    /*************************************************************************
     *
     *************************************************************************/
    public ParamDef getDefinition() {
	return paramDef;
    }

    /**************************************************************************
     *
     **************************************************************************/
    public NamedValues createNamedArg (Map<String,Object> inputs, Job job)
	throws ParameterException {

	if (paramDef.is (ParamDef.ENVAR))
	    return null;

	return buildNamed (inputs, job);
    }

    /**************************************************************************
     *
     **************************************************************************/
    public String[] createArg (Map<String,Object> inputs, Job job)
	throws ParameterException {

	if (paramDef.is (ParamDef.ENVAR))
	    return ArrayUtils.EMPTY_STRING_ARRAY;

	return build (inputs, job, CMD, false).toArray (new String[] {});
    }

    /**************************************************************************
     *
     **************************************************************************/
    public Properties createEnv (Map<String,Object> inputs, Job job)
	throws ParameterException {

	if (! paramDef.is (ParamDef.ENVAR))
	    return CmdLineJob.EMPTY_PROPS;

	return list2props (build (inputs, job, "", true));
    }

    /**************************************************************************
     *
     **************************************************************************/
    public IOData[] createIO (Map<String,Object> inputs, Job job)
	throws ParameterException {
	return CmdLineJob.EMPTY_IODATA;
    }

    /*************************************************************************
     * Nothing implemented here.
     *************************************************************************/
    public void validate (Map<String,Object> inputs, Job job, Object testedValue)
	throws ParameterException {
    }

}
