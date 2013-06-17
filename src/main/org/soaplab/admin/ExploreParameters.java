// ExploreParameters.java
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

package org.soaplab.admin;

import org.soaplab.clients.CmdLineHelper;
import org.soaplab.clients.InputUtils;
import org.soaplab.share.SoaplabException;
import org.soaplab.share.SoaplabConstants;
import org.soaplab.share.SoaplabMap;
import org.soaplab.services.cmdline.CmdLineJob;
import org.soaplab.services.cmdline.Parameter;
import org.soaplab.services.cmdline.NamedValues;
import org.soaplab.services.cmdline.ParameterException;
import org.soaplab.services.AnalysisService;
import org.soaplab.services.Reporter;
import org.soaplab.services.IOData;
import org.soaplab.services.Job;
import org.soaplab.services.JobFactory;
import org.soaplab.services.Config;
import org.soaplab.services.metadata.MetadataAccessor;
import org.soaplab.services.metadata.ParamDef;

import org.tulsoft.tools.BaseCmdLine;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.ArrayUtils;

import java.util.Properties;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.io.File;

/**
 * A command-line tool that explores and tests command-line parameters
 * created from service metadata and user inputs. Start it with
 * <tt>-help</tt> to see available options. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: ExploreParameters.java,v 1.17 2007/11/22 02:02:15 marsenger Exp $
 */
public class ExploreParameters
    extends CmdLineHelper {

    final static int CREATE_ARG  = 1;
    final static int CREATE_ALL  = 2;
    final static int CREATE_IO   = 3;
    final static int CREATE_NARG = 4;

    AnalysisService service;
    static int whatToDo;
    static String method;
    static Map<String,String> methods;
    static Set<String> envars;

    /*************************************************************************
     * Constructor that creates a service with a dummy job that can
     * only accept inputs and create from them the arguments.
     *************************************************************************/
    public ExploreParameters (String serviceName)
	throws SoaplabException {

	service = new AnalysisService (serviceName);
	MetadataAccessor metadataAccessor = service.getMetadataAccessor();
	metadataAccessor.getAnalysisDef().module = MyJobFactory.class.getName();

    }

    /*************************************************************************
     * Delegated to a service...
     *************************************************************************/
    public SoaplabMap[] getResultSpec()
	throws SoaplabException {
	return service.getResultSpec();
    }

    // here we get input data...
    public SoaplabMap[] getInputSpec()
	throws SoaplabException {
	return service.getInputSpec();
    }

    // ...and here we do whatever asked for (whatToDo)
    public String doArgs (Map<String,Object> inputs,   // input data
			  Map<String,String> methods,  // parameter's METHODs
			  Set<String> envars,          // treat parameters as ENVAR
			  String method,               // global METHOD
			  int whatToDo)
	throws SoaplabException {
	ExploreParameters.whatToDo = whatToDo;
	ExploreParameters.methods = methods;
	ExploreParameters.envars = envars;
	ExploreParameters.method = method;
	return service.createAndRun (SoaplabMap.fromMap (inputs));
    }

    // it must be static... (because of how ICreator works)
    public static class MyJobFactory implements JobFactory {
	public MyJobFactory() {
	}
	public Job newInstance (String jobId,
				MetadataAccessor metadataAccessor,
				Reporter reporter,
				Map<String,Object> sharedAttributes,
				boolean jobRecreated)
	    throws SoaplabException {
	    return new MyJob (jobId, metadataAccessor, reporter,
			      sharedAttributes, jobRecreated);
	}
    }

    public static class MyJob extends CmdLineJob {
	public MyJob (String jobId,
		      MetadataAccessor metadataAccessor,
		      Reporter reporter,
		      Map<String,Object> sharedAttributes,
		      boolean jobRecreated)
	    throws SoaplabException {
	    super (jobId, metadataAccessor, reporter,
		   sharedAttributes, jobRecreated);
	}

	//
	// a real work is done here
	//
	public void run()
	    throws SoaplabException {

	    // set global METHOD
	    if (StringUtils.isNotBlank (method))
		metadataAccessor.getAnalysisDef().method = method;

	    Parameter[] parameters =
		(Parameter[])sharedAttributes.get (JOB_SHARED_PARAMETERS);
	    if (parameters == null) {
		System.err.println ("No shared parameters found! Strange...");
		return;
	    }

	    checkInputs();

	    // set parameters METHODs
	    if (methods.size() > 0) {
		for (Parameter p: parameters) {
		    ParamDef def = p.getDefinition();
		    if (methods.containsKey (def.id))
			def.options.put (ParamDef.METHOD,
					 resolveShortcuts (methods.get (def.id), def));
		}
	    }

	    // set parameters as ENVARs
	    if (envars.size() > 0) {
		for (Parameter p: parameters) {
		    ParamDef def = p.getDefinition();
		    if (envars.contains (def.id))
			def.options.put (ParamDef.ENVAR, "true");
		}
	    }


	    try {
		String format = "%-25s%-15s %s";
		switch (whatToDo) {
		case CREATE_NARG:
		    boolean nothingCreated = true;
		    for (Parameter p: parameters) {
			NamedValues namedArg = p.createNamedArg (inputs, this);
			if (namedArg == null)
			    continue;
			if (nothingCreated) {
			    title (String.format (format, "Param ID", "Param Tag", "Value(s)"));
			    nothingCreated = false;
			}
			msgln (String.format (format,
					      namedArg.getId(),
					      namedArg.getTag(),
					      namedArg.getValues()));
		    }
		    if (! nothingCreated) msgln ("");
		    break;

		case CREATE_ARG:
		    nothingCreated = true;
		    for (Parameter p: parameters) {
			String[] args = p.createArg (inputs, this);
			if (! verbose) {
			    for (String a: args)
				msgln (a);
			    continue;
			}
			String arg = StringUtils.join (args, ' ');
			if ("".equals (arg))
			    continue;
			if (nothingCreated) {
			    title ("Command-line arguments");
			    nothingCreated = false;
			}
			msgln (String.format ("%-25s", "Parameter '" + p.getDefinition().id + "': ") +
			       arg);
			if (args.length > 1)
			    for (int i = 0; i < args.length; i++)
				msgln (String.format ("\t(%2d) %s", (i+1), args[i]));
		    }
		    if (! nothingCreated) msgln ("");

		    nothingCreated = true;
		    for (Parameter p: parameters) {
			Properties env = p.createEnv (inputs, this);
			if (env.size() == 0)
			    continue;
			if (nothingCreated) {
			    title ("Environment properties");
			    nothingCreated = false;
			}
			msgln (String.format ("%-25s", "Parameter '" + p.getDefinition().id + "': ") +
			       env.toString());
		    }
		    if (! nothingCreated) msgln ("");

		    nothingCreated = true;
		    for (Parameter p: parameters) {
			IOData[] ioData = p.createIO (inputs, this);
			if (! verbose) {
			    for (IOData io: ioData) {
				if (! io.getDefinition().isStandardStream())
				    msgln (io.toString());
			    }
			    continue;
			}
			if (ioData.length == 0)
			    continue;
			if (nothingCreated) {
			    title ("IO data containers");
			    nothingCreated = false;
			}
			msg (String.format ("%-25s", "Parameter '" + p.getDefinition().id + "': "));
			if (ioData.length == 1) {
			    msgln (ioData[0].toString());
			} else {
			    msgln ("");
			    for (int i = 0; i < ioData.length; i++)
				msgln (String.format ("\t(%2d) %s", (i+1), ioData[i].toString()));
			}
		    }
		    if (! nothingCreated) msgln ("");

		    break;
		    
		case CREATE_ALL:
		    title ("Command line");
		    int count = 1;
		    List<String> args = createArgs();
		    for (String arg: args.toArray(new String[] {})) {
			msgln (String.format ("\t(%2d) %s", count++, arg));
		    }
		    Properties envProps = createEnvs();
		    if (envProps.size() > 0) {
			title ("Environment poperties");
			for (Enumeration en = envProps.propertyNames(); en.hasMoreElements(); ) {
			    String key = (String)en.nextElement();
			    msgln ("\t" + key + "=" + envProps.get (key));
			}
		    }
		    IOData[] ioData = createIO();
		    if (ioData.length > 0) {
			title ("IO data containers");
			for (int i = 0; i < ioData.length; i++)
			    msgln ("\t" + ioData[i].toString());
		    }
		    NamedValues[] named = createNamedArgs();
		    if (named.length > 0) {
			title (String.format (format, "Param ID", "Param Tag", "Value(s)"));
			for (NamedValues namedArg: named) {
			    msgln (String.format (format,
						  namedArg.getId(),
						  namedArg.getTag(),
						  namedArg.getValues()));
			}
		    }
		    break;
		}

	    } catch (ParameterException e) {
		throw new SoaplabException (e.getMessage());
	    }
	}
    }

    // substitute some shortcuts in METHOD template using a parameter
    // definition 'def'; return 'method' (changed or unchanged)
    static String resolveShortcuts (String method, ParamDef def) {

	System.out.println ("M: " + method);
	return method
 	    .replaceAll ("\\$\\$",         "\\${" + def.id + "}")      // $$ => ${id}
 	    .replaceAll ("\\$\"\\$\"",     "\\$\"" + def.id + "\"")    // $"$" => $"id"
 	    .replaceAll ("\\$value",       "\\${" + def.id + "}")      // $value => ${id}
 	    .replaceAll ("\\$\\{value\\}", "\\${" + def.id + "}")      // ${value} => ${id}
 	    .replaceAll ("(\\&\\&)|(\\$name)|(\\$\\{name\\})", def.id) // parameter name
	    ;
    }

    /*************************************************************************
     *
     *  An entry point
     *
     *************************************************************************/
    public static void main (String[] args) {

	try {
	    BaseCmdLine cmd = getCmdLine (args, ExploreParameters.class);
	    String param = null;

	    // perhaps add a new application list
	    param = cmd.getParam ("-addlist");
	    if (param != null) {
		File file = new File (param);
		if (! file.exists())
		    param = "${metadata.dir}/" + param;
		Config.get().setProperty
		    (Config.PROP_APPLIST_FILENAME,
		     (String[])ArrayUtils.addAll
		     (Config.get().getStringArray (Config.PROP_APPLIST_FILENAME),
		      new String[] { param }));
	    }

	    // what is the service name whose metadata are going to be used?
	    param = cmd.getParam ("-name");
	    if (param == null) {
		System.err.println ("Service name must be given. Use '-name'.");
		System.exit (1);
	    }

	    ExploreParameters worker = new ExploreParameters (param);

	    SoaplabMap[] inputSpec = worker.getInputSpec();

	    // collect data inputs from the command line
	    Map<String,Object> inputs =
		InputUtils.collectInputs (cmd,
					  SoaplabMap.toMaps (inputSpec));

	    // collect METHODs and ENVARs from the command-line
	    String method = cmd.getParam ("-m");
	    Map<String,String> methods = new HashMap<String,String>();
	    Set<String> envars = new HashSet<String>();
	    for (int i = 0; i < inputSpec.length; i++) {
		String key = (String)inputSpec[i].get (SoaplabConstants.INPUT_NAME);
		param = cmd.getParam ("-m" + key);
		if (param == null)
		    param = cmd.getParam ("-m_" + key);
		if (param != null)
		    methods.put (key, param);
		if (cmd.hasOption ("-e" + key) || cmd.hasOption ("-e_" + key))
		    envars.add (key);

		// hack to allow setting method/envar also to choice
		// parameter (that o not have their main names in
		// input spec)
		int pos = key.lastIndexOf ('_');
		if (pos > -1) {
		    String subkey = key.substring (0, pos);
		    param = cmd.getParam ("-m" + subkey);
		    if (param == null)
			param = cmd.getParam ("-m_" + subkey);
		    if (param != null)
			methods.put (subkey, param);
		    if (cmd.hasOption ("-e" + subkey) || cmd.hasOption ("-e_" + subkey))
			envars.add (subkey);
		}
	    }

	    boolean somethingDone = false;

	    // show metadata
	    if (cmd.hasOption ("-i") || cmd.hasOption ("-ii")) {
		InputUtils.formatInputMetadata (SoaplabMap.toMaps (inputSpec),
						System.out,
						cmd.hasOption ("-ii"),
						verbose);
		somethingDone = true;
	    }
	    if (cmd.hasOption ("-o") || cmd.hasOption ("-oo")) {
		SoaplabMap[] resultSpec = worker.getResultSpec();
		InputUtils.formatOutputMetadata (SoaplabMap.toMaps (resultSpec),
						 System.out,
						 cmd.hasOption ("-oo"),
						 verbose);
		somethingDone = true;
	    }

	    // explore parameters 
	    if (cmd.hasOption ("-arg") || cmd.hasOption ("-args")) {
		worker.doArgs (inputs, methods, envars, method, CREATE_ARG);
		somethingDone = true;
	    }

	    if (cmd.hasOption ("-narg") || cmd.hasOption ("-nargs")) {
		worker.doArgs (inputs, methods, envars, method, CREATE_NARG);
		somethingDone = true;
	    }

	    // if no other option left...
	    // ...check unrecognized arguments
	    if (cmd.params.length > 0) {
		msg ("Unrecognized arguments: ");
		for (String arg: cmd.params)
		    msg (arg + " ");
		msgln ("");
	    }

	    // ...and create the whole command-line, and all
	    // environment variables
	    if (! somethingDone)
		worker.doArgs (inputs, methods, envars, method, CREATE_ALL);

	    exit (0);

	} catch (Throwable e) {
	    processErrorAndExit (e);
	}
    }

}
