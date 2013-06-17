// Parameter.java
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

import java.util.Map;
import java.util.Properties;

/**
 * An interface defining a paramter of a Soaplab2 job, usualy a
 * command-line parameter. Once given input data and a reference to a
 * particular job, the instances implementing this interface are able
 * to produce command-line arguments (by methods {@link #createArg
 * createArg} and {@link #createNamedArg createNamedArg}), environment
 * variables (by method {@link #createEnv createEnv}) and IO elements
 * (by method {@link #createIO createIO}) with this input data,
 * wrapped/created as defined by service metadata. Usually, however,
 * only some of these methods return non-empty results (for example, a
 * single parameter is usually either used as a command-line argument,
 * or as an environment variable, but rarely as both in the same
 * time). <p>
 *
 * There is a strong recommendation to implement this interface as a
 * sub-class of
 * <tt>org.soaplab.services.cmdline.BaseParameter</tt>. Or, at least,
 * to include the same constructor:
 *
 * <pre>
 * public YourParameter (ParamDef paramDef)
 * </pre>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: Parameter.java,v 1.10 2007/10/22 17:56:18 marsenger Exp $
 */

public interface Parameter {

    /*************************************************************************
     * Return a definition of this parameter.
     *************************************************************************/
    ParamDef getDefinition();

    /**************************************************************************
     * Return an array that can be directly used on the
     * command-line. <p>
     *
     * @param inputs are the end-user's input data (which will become
     * parts of the returned command-line arguments). It may contain
     * more data than just for this parameter.
     *
     * @param job whose arguments are being created
     *
     * @return command-line arguments, separated as they should be on
     * the analysis's command-line; the returned value may be empty,
     * but not null
     *
     * @throws ParameterException if there are not-valid (or missing)
     * data for this parameter in the given 'inputs'
     **************************************************************************/
    String[] createArg (Map<String,Object> inputs, Job job)
	throws ParameterException;

    /**************************************************************************
     * Return properties that represent an "environment" of the
     * analysis that is going to be started. These properties are
     * (usually) taken from those parameters that have defined an
     * option "envar" in their metadata. <p>
     *
     * The property keys are usually not prefixed by the '-'
     * character (as often are the command-line arguments). <p>
     *
     * It is left to the 'user' of the parameters (which is, for
     * example, a class implementing the {@link
     * org.soaplab.services.Job Job} interface) how to use the
     * returned value. Obvious choice is to use it for setting
     * environment variables. But some classes may use it to set Java
     * System proprties instead, etc. <p>
     *
     * @param inputs are the end-user's input data (which will become
     * parts of the returned environment). It may contain more data
     * than just for this parameter.
     *
     * @param job whose environment variables are being created
     *
     * @return properties that will used as environment variables, or
     * Java properties, or otherwise (it depends on implementation);
     * the returned value may be empty, but not null
     *
     * @throws ParameterException if there are not-valid (or missing)
     * data for this parameter in the given 'inputs'
     **************************************************************************/
    Properties createEnv (Map<String,Object> inputs, Job job)
	throws ParameterException;

    /**************************************************************************
     * Create an array of data containers that kept (usually local)
     * data whose references are mentioned on the command-line - for
     * that see method {@link #createArg}, or are not mentioned
     * anywhere (e.g. data given to an analysis tool on its standard
     * input). <p>
     *
     * For example, if an analysis tool expects an input file name on
     * its command-line (let's say prefixed by the <tt>-infile</tt>),
     * the {@link #createArg} method would return this command-line:
     * *<pre>-infile 12avd5467dfh-tmp</pre> and this method would
     * return a data container that makes sure that the input data are
     * stored in the local file <tt>12avd5467dfh-tmp</tt>. <p>
     *
     * @param inputs are the end-user's input data (which will become
     * parts of the returned containers). It may contain more data
     * than just for this parameter.
     *
     * @param job whose data containers are being created
     *
     * @return data containers; the returned value may be empty, but
     * not null
     *
     * @throws ParameterException if there are not-valid (or missing)
     * data for this parameter in the given 'inputs', or if there were
     * IO problems when localizing 'inputs'
     **************************************************************************/
    IOData[] createIO (Map<String,Object> inputs, Job job)
	throws ParameterException;

    /*************************************************************************
     * Verify value (or values, if they are repetitive) of this
     * parameter. It is recommended to start validation by calling
     * <tt>super.validate(...)</tt> first. <p>
     *
     * @param inputs are the end-user's input data - they are passed
     * here all of them because they might be needed for some
     * inter-parameters value validation
     *
     * @param job whose input is being validated
     *
     * @param testedValue is the value that should be validated. A
     * value with the same name often appears also in 'inputs' (sent
     * by a user) - but if not than it indicates that the
     * 'testedValue' is a default value taken from the metadata
     * definition (which may have some impact on the validation
     * procedure). It may be also null - in which case try to find it
     * first in the 'inputs'.
     *
     * @throws ParameterException is validation failed
     *************************************************************************/
    void validate (Map<String,Object> inputs, Job job, Object testedValue)
	throws ParameterException;

    /**************************************************************************
     * Return a structure similar to the arguments for a command-line
     * (as returned by {@link #createArg createArg}) but slightly more
     * general. It can be used for jobs that do not create command
     * lines but still need to get user data in a structured way. A
     * typical example are Gowlab jobs. More details are in the class
     * {@link NamedValues}. <p>
     *
     * @param inputs are the end-user's input data (which will become
     * parts of the returned structure). It may contain
     * more data than just for this parameter.
     *
     * @param job whose arguments are being created
     *
     * @return a list of user values, labelled by this parameter ID
     * and possibly by a tag (qualifier); the returned value may be
     * empty, but not null
     *
     * @throws ParameterException if there are not-valid (or missing)
     * data for this parameter in the given 'inputs'
     *
     **************************************************************************/
    NamedValues createNamedArg (Map<String,Object> inputs, Job job)
	throws ParameterException;

}
