// Config.java
//
// Created: October 2006
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

package org.soaplab.services;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.ArrayUtils;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;
import java.io.File;

/**
 * An abstract class giving access to the global Soaplab
 * configuration. It also allows to add additional configuration files
 * (which may be useful for Soaplab's plugins, for example). <p>
 *
 * It also contains names and description of (most of) the Soaplab
 * configuration properties. <p>
 *
 * The class uses <a
 * href="http://jakarta.apache.org/commons/configuration/">Apache
 * Commons Configuration</a> - the method {@link #get} gives back
 * directly an Apache's <tt>CompositeConfiguration</tt> instance
 * allowing to fine-tune your configuration. But in most cases,
 * few convenient methods are used. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: Config.java,v 1.26 2009/12/10 09:40:34 smoretti Exp $
 */

public abstract class Config {

    private static org.apache.commons.logging.Log log =
       org.apache.commons.logging.LogFactory.getLog (Config.class);

    private static CompositeConfiguration config;
    private static Set<String> configFilenames = new HashSet<String>();

    /**
     * A filename indicating a file containing Soaplab run-time
     * properties. This file name can be changed by setting Java
     * property {@link #PROP_SOAPLAB_CONFIGURATION} to point to a
     * different file name.
     */
    public static final String SOAPLAB_CONFIG_FILENAME = "soaplab.properties";

    /**
     * A part of property names. It indicates that the whole property
     * represents a synonym and its resolution. For example:
     *
     *<pre>
     *PREFIX_SYNONYM.module = org.soaplab.plugins.God
     *</pre>
     *
     * means that the class name <tt>org.soaplab.plugins.God</tt> is
     * known under a synonym <tt>module</tt>.
     */
    public static final String PREFIX_SYNONYM = "synonym";

    /**
     * A default name for a class representing a Job factory. This is
     * used only if all other ways fail. The other ways are: (a) take
     * the class name from the service metadata (TBD, not yet), (b)
     * take the class name from the module tag in the applications
     * list, (c) use the module tag in the applications list as a
     * synonym and take the class name from the run-time configuration
     * file.
     */
    public static final String DEFAULT_JOB_FACTORY_CLASS = "org.soaplab.sowa.SowaJobFactory";


    /**************************************************************************
     * The main method returning a configuration object. The returned
     * object is a singleton - but its contents (the properties it
     * carries) can be changed dynamically (the properties are
     * reloaded if their files are modified). <p>
     *
     * The configuration object contains all Java system properties
     * and properties read from the Soaplab configuration file. This
     * file name is given by the system property {@link
     * #PROP_SOAPLAB_CONFIGURATION}, or using a default name {@link
     * #SOAPLAB_CONFIG_FILENAME}. If the filename does not specify an
     * absolute path, the file will be searched automatically in the
     * following locations:
     *
     * <ul>
     *  <li> in the current directory,
     *  <li> in the user home,
     *  <li> directory in the classpath
     * </ul><p>
     *
     * The System properties take precedence over properties read from
     * the Soaplab property file. <p>
     *
     * The configuration object can be anytime extended by properties
     * from other sources by using either methods defined in the
     * <tt>CompositeConfiguration</tt>'s API, or using a convenient
     * methods {@link #addConfigPropertyFile} and {@link
     * #addConfigXMLFile} defined here. Properties from these
     * additional files have higher priority than properties added
     * earlier - except System properties, they are always the most
     * prioritized. <p>
     *
     * @return a configuration object
     **************************************************************************/
    public static synchronized CompositeConfiguration get() {
	if (config == null) {

	    // set the Soaplab global configuration
	    CompositeConfiguration cfg = new CompositeConfiguration();

	    // first, add System properties
	    cfg.addConfiguration (new SystemConfiguration());

	    // second, add Soaplab properties file(s)
	    String[] configFilenames = cfg.getStringArray (PROP_SOAPLAB_CONFIGURATION);
	    if (configFilenames == null || configFilenames.length == 0)
		configFilenames = new String[] { SOAPLAB_CONFIG_FILENAME };
	    for (int i = 0; i < configFilenames.length; i++) {
		log.info ("Using configuration file: " + configFilenames[i]);
		addPropertiesConfiguration (cfg, configFilenames[i]);
	    }

	    // keep it for other calls
	    config = cfg;
	}
	return config;
    }

    /**************************************************************************
     * It returns all existing configurations from the main composite
     * configuration 'config', while removing them also from this
     * 'config'. This is a step needed before inserting a new
     * configuration,
     **************************************************************************/
    private static ArrayList<Configuration> getAndClearCurrentConfigurations() {
	get();
	ArrayList<Configuration> cfgs = new ArrayList<Configuration>();

	// keep there always the first (System properties) and the
	// last (inMemoryConfiguration) configuration
	while (config.getNumberOfConfigurations() > 2) {
	    Configuration cfg = config.getConfiguration (1);
	    cfgs.add (cfg);
	    config.removeConfiguration (cfg);
	}
	return cfgs;
    }

    /**************************************************************************
     *
     **************************************************************************/
    private static boolean isExistingConfig (FileConfiguration cfg) {
	if (cfg.getFile() == null)
	    return false;
	String filename = cfg.getFile().getAbsolutePath();
	if (configFilenames.contains (filename))
	    return true;
	configFilenames.add (filename);
	return false;
    }

    /**************************************************************************
     * Add given property files as a new configuration to 'cfg'
     * composite configuration. Return true on success.
     **************************************************************************/
    private static boolean addPropertiesConfiguration (CompositeConfiguration cfg,
						       String configFilename) {
	try {
	    PropertiesConfiguration propsConfig =
		new PropertiesConfiguration (configFilename);
	    if (isExistingConfig (propsConfig))
		return true;
	    propsConfig.setReloadingStrategy (new FileChangedReloadingStrategy());
	    cfg.addConfiguration (propsConfig);
	    return true;
	} catch (ConfigurationException e) {
	    log.error ("Loading properties configuration from '" +
		       configFilename + "' failed: " +
		       e.getMessage());
	    return false;
	}
    }

    /**************************************************************************
     * Add given XML files as a new configuration to 'cfg' composite
     * configuration. Return true on success.
     **************************************************************************/
    private static boolean addXMLConfiguration (CompositeConfiguration cfg,
						String configFilename) {
	try {
	    XMLConfiguration xmlConfig = new XMLConfiguration (configFilename);
	    if (isExistingConfig (xmlConfig))
		return true;
	    xmlConfig.setReloadingStrategy (new FileChangedReloadingStrategy());
	    cfg.addConfiguration (xmlConfig);
	    return true;
	} catch (ConfigurationException e) {
	    log.error ("Loading XML configuration from '" +
		       configFilename + "' failed: " +
		       e.getMessage());
	    return false;
	}
    }

    /**************************************************************************
     * Add new configuration properties from a property file. <p>
     *
     * The newly added properties have higher priority than properties
     * added earlier - except System properties, they are always the
     * most prioritized. <p>
     *
     * @param configFilename is a filename indicating a file
     * (formatted as a Java properties file) with new properties (see
     * {@link #get} explaining where is this file looked for)

     * @return true if 'configFilename' is successfully added, false
     * otherwise (in which case the cause is recorded in the log)
     **************************************************************************/
    public static synchronized boolean addConfigPropertyFile (String configFilename) {

	log.info ("Adding property configuration file: " + configFilename);
	ArrayList<Configuration> cfgs = getAndClearCurrentConfigurations();
	boolean success = addPropertiesConfiguration (get(), configFilename);
	for (Configuration cfg: cfgs) {
	    get().addConfiguration (cfg);
	}
	return success;
    }

    /**************************************************************************
     * Add new configuration properties from an XML file. The details
     * of such XML file structure are explained in <a
     * href="http://jakarta.apache.org/commons/configuration/">Apache
     * Commons Configuration</a>. <p>
     *
     * The newly added properties have higher priority than properties
     * added earlier - except System properties, they are always the
     * most prioritized. <p>
     *
     * @param configFilename is an XML filename indicating a file with
     * new properties (see {@link #get} explaining where is this file
     * looked for)

     * @return true if 'configFilename' is successfully added, false
     * otherwise (in which case the cause is recorded in the log)
     **************************************************************************/
    public static synchronized boolean addConfigXMLFile (String configFilename) {

	log.info ("Adding XML configuration file: " + configFilename);
	ArrayList<Configuration> cfgs = getAndClearCurrentConfigurations();
	boolean success = addXMLConfiguration (get(), configFilename);
	for (Configuration cfg: cfgs) {
	    get().addConfiguration (cfg);
	}
	return success;
    }

    /**************************************************************************
     * Get a string associated with the given configuration key, or -
     * if not found - get the given default value (which still may be
     * null). <p>
     *
     * So far, it looks like the same-named method in
     * <tt>org.apache.commons.configuration.Configuration</tt>. So if
     * you need just that functionality, go directly there. For
     * example, do this:
     *
     *<pre>
     * String value = Config.get().getString ("my.property", "yes");
     *</pre>
     *
     * The other two parameters, 'serviceName' and 'owner' - if not
     * null - act as prefixes of the given property key, and they are
     * tried first. This way you can select some property to be used
     * only for specified service, or for a specified class name. For
     * example, a property file can have these properties:
     *
     *<pre>
     *    applist = ...
     *    org.soaplab.lists.MyList.applist = ...
     *</pre>
     *
     * or:
     *
     *<pre>
     *    delay = ...
     *    big.service.delay = ...
     *</pre>
     *
     * The method looks first for property
     * &lt;serviceName&gt;.&lt;key&gt;. Then, if not found, for
     * property &lt;class-name&gt;.&lt;key&gt;. And finally, just for
     * &lt;key&gt;. The class-name is a fuly-qualified class name of
     * 'owner'. <p>
     *
     * @param key is a property name
     * @param defaultValue used if the 'key' cannot be found
     * @param serviceName is a preferred prefix for the 'key'
     * @param owner is a preferred prefix for the 'key': if 'owner' is
     * of type Class its name is used, otherwise, the name of the
     * class of the instance 'owner' is used
     *
     * @return the property value, or 'defaultValue' if such property
     * does not exist
     **************************************************************************/
    public static String getString (String key, String defaultValue,
				    String serviceName, Object owner) {
	String value = null;
	if (serviceName != null) {
	    value = get().getString (serviceName + "." + key);
	    if (value != null) return value;
	}
	if (owner != null) {
	    value = get().getString ( (owner instanceof Class ?
				       ((Class)owner).getName() :
				       owner.getClass().getName()) + "." + key);
	    if (value != null) return value;
	}
	return get().getString (key, defaultValue);
    }

    /**************************************************************************
     * Almost the same functionality as {@link #getString getString}
     * method - see there details about parameters. The different is
     * the return value - it allows to return several values of the
     * same property. In the configuration file, the property can be
     * repeated, or can have several comma-separated values. <p>
     *
     * @return all values of the given property, or - if such property
     * does not exist - return a one-element array with the
     * 'defaultValue' unless the 'defaultValue' is also null in which
     * case return an empty array
     **************************************************************************/
    public static String[] getStrings (String key, String defaultValue,
				       String serviceName, Object owner) {
	String[] values = null;
	if (serviceName != null) {
	    values = get().getStringArray (serviceName + "." + key);
	    if (values.length > 0) return values;
	}
	if (owner != null) {
	    values = get().getStringArray ( (owner instanceof Class ?
					     ((Class)owner).getName() :
					     owner.getClass().getName()) + "." + key);
	    if (values.length > 0) return values;
	}
	values = get().getStringArray (key);
	if (values.length > 0) return values;

	if (defaultValue == null)
	    return ArrayUtils.EMPTY_STRING_ARRAY;
	else
	    return new String[] { defaultValue };
    }

    /**************************************************************************
     * Almost the same functionality as {@link #getString getString}
     * method - see there details about parameters - except that it
     * expects property value to be an integer. <p>
     *
     * If the key is found but its value is not an integer, or if it
     * is not found at all, it returns the given default value. <p>
     **************************************************************************/
    public static int getInt (String key, int defaultValue,
			      String serviceName, Object owner) {
	String strValue = getString (key, ""+defaultValue, serviceName, owner);
	try {
	    return Integer.decode (strValue).intValue();
	} catch (NumberFormatException e) {
	    return defaultValue;
	}
    }

    /**************************************************************************
     * Almost the same functionality as {@link #getString getString}
     * method - see there details about parameters - except that it
     * expects property value to be a boolean. <p>
     *
     * For the property values 'true', 'on', 'yes' (case insensitive),
     * and for an empty value it returns true. For other values, false
     * is returned. If the property does not exist at all, the
     * 'defaultValue' is returned. <p>
     **************************************************************************/
    public static boolean isEnabled (String key, boolean defaultValue,
				     String serviceName, Object owner) {
	String strValue = getString (key, null, serviceName, owner);
	if (strValue == null)
	    return defaultValue;
	if (StringUtils.isBlank (strValue))
	    return true;
	return BooleanUtils.toBoolean (strValue);
    }

    /**************************************************************************
     * Get all properties contained in the configuration whose keys
     * (names) match the specified prefix. <p>
     *
     * The remaining parameters are used in the similar way as in
     * {@link #getString getString} method. Meaning if they are not
     * null, the following prefixes are actually tried:
     *
     *<pre>
     * &lt;prefix&gt;.
     * &lt;class-of-owner&gt;.&lt;prefix&gt;.
     * &lt;serviceName&gt;.&lt;prefix&gt;.
     *</pre>
     *
     * These prefixes are tried in the order as indicated above -
     * causing the properties with earlier prefixes being overwritten
     * by those having newer ones. For example, having the following
     * properties in the configuration (<tt>classic.helloworld</tt> is
     * a name of a service):
     *
     *<pre>
     * classic.helloworld.grid.env.One = ein
     * classic.helloworld.grid.env.Two = zwei
     * grid.env.One = un
     * grid.env.Two = deux
     * grid.env.Three = trois
     *</pre>
     *
     * and calling:
     *
     *<pre>
     * getMatchingProperties ("grid.env", "classic.helloworld", null)
     *</pre>
     *
     * will return properties:
     *
     *<pre>
     * One = ein
     * Two = zwei
     * Three = troi
     *</pre>
     *
     * @param prefix is a part of property name (may or may not end
     * with a dot)
     *
     * @param serviceName is a possibly 'prefix' for the 'prefix' (can
     * be null)
     *
     * @param owner is another possible 'prefix' for the 'prefix': if
     * 'owner' is of type Class its name is used, otherwise, the name
     * of the class of the instance 'owner' is used
     *
     * @return properties matching the prefix - the prefix is not part
     * of the returned property names; if nothing matches, an empty
     * list is returned (not null)
     **************************************************************************/
    public static Properties getMatchingProperties (String prefix,
						    String serviceName,
						    Object owner) {
	Properties props = new Properties();

	if (prefix.endsWith ("."))
	    prefix = prefix.substring (0, prefix.length() - 1);

	// try with a pure prefix
	int prefixLen = prefix.length() + 1;   // 1 is for ending dor
	CompositeConfiguration cfg = get();
	synchronized (cfg) {
	    for (Iterator it = cfg.getKeys(prefix); it.hasNext();) {
	        String key = (String) it.next();
	        props.put(key.substring(prefixLen), cfg.getString(key));
	    }
	}

	String extendedPrefix = null;

	// try with an owner class and prefix
	if (owner != null) {
	    extendedPrefix =
		(owner instanceof Class ?
		 ((Class)owner).getName() :
		 owner.getClass().getName()) + "." + prefix;
	    prefixLen = extendedPrefix.length() + 1;
	    synchronized(cfg){
	        for (Iterator it = cfg.getKeys (extendedPrefix); it.hasNext(); ) {
	            String key = (String)it.next();
	            props.put (key.substring (prefixLen), cfg.getString (key));
	        }
	    }
	}

	// try with a service name and prefix
	if (serviceName != null) {
	    extendedPrefix = serviceName + "." + prefix;
	    prefixLen = extendedPrefix.length() + 1;
	    synchronized(cfg){
	        for (Iterator it = cfg.getKeys (extendedPrefix); it.hasNext(); ) {
	            String key = (String)it.next();
	            props.put (key.substring (prefixLen), cfg.getString (key));
	        }
	    }
	}

	return props;
    }

    /**************************************************************************
     *
     **************************************************************************/
    public static File[] getConfigFiles() {
	get();
	ArrayList<File> files = new ArrayList<File>();

	for (int i = 0; i < config.getNumberOfConfigurations(); i++) {
	    Configuration cfg = config.getConfiguration (i);
	    if (cfg instanceof FileConfiguration) {
		files.add ( ((FileConfiguration)cfg).getFile() );
	    }
	}
	return files.toArray (new File[] {});
    }


    // ----------------------------------------------------------
    //
    //                      Property names
    //
    // ----------------------------------------------------------

    /**
     * A property name. Its value contains a filename indicating a
     * file containing Soaplab configuration properties. Default value
     * is {@link #SOAPLAB_CONFIG_FILENAME}.
     */
    public static final String PROP_SOAPLAB_CONFIGURATION = "soaplab.configuration";

    /**
     * A property name. Its value is a filename containing a list of
     * analysis provided by this list. The property name can be also
     * prefixed by this class full name. <p>
     *
     * There is no default value.
     */
    public static final String PROP_APPLIST_FILENAME = "applist";

    /**
     * A property name. Its value (an integer, number of milliseconds)
     * indicates to the JobManager how often to scan a list of
     * completed jobs in order to remove them from the memory (not to
     * destroy them; their results will still be available). <p>
     *
     * Default value is 60000 (one minute).
     */
    public static final String PROP_JOBS_CLEANING_INTERVAL = "jobs.cleaning.interval";


    /**
     * A property name. Its value (an integer, number of milliseconds)
     * indicates to the JobManager how long after it is started a job should be
     * terminated if it is still runnig.<p>
     *
     * Default value is 864000000 (10 days).
     */
    public static final String PROP_JOBS_TIMEOUT = "jobs.timeout";

    /**
     * A property name. Its value (an integer, number of milliseconds)
     * indicates to Soaplab how long after a waitfor call made 
     * the call should return if the job is still running after that period.<p>
     *
     * Default value is 864000000 (10 days).
     */
    public static final String PROP_JOBS_WAITFOR_TIMEOUT = "jobs.waitfor.timeout";

    /**
     * A property name. Its value (an integer, number of milliseconds)
     * indicates to the AnalysisService how often to scan a list of
     * instantiated services in order to remove them from the memory
     * (they can be still later instantiated if a request comes). <p>
     *
     * Default value is 300000 (five minutes).
     */
    public static final String PROP_SERVICES_CLEANING_INTERVAL = "services.cleaning.interval";

    /**
     * A property name. Its value is an absolute path to a directory
     * where FileStorage handler keeps analysis results. <p>
     *
     * Default value (also used when the given directory is not
     * writable to the current user) is "soaplab.results" in the
     * system-dependent default temporary-file directory (the default
     * temporary-file directory is specified by the system property
     * "java.io.tmpdir").
     */
    public static final String PROP_RESULTS_DIR = "results.dir";

    /**
     * A property name. Its value is a name of a directory containing
     * analysis metadata. <p>
     *
     * There is no default value. <p>
     */
    public static final String PROP_METADATA_DIR = "metadata.dir";

    /**
     * A part of a property name. Its value is a name of a file
     * containing metadata for a concrete analysis. <p>
     *
     * The property name must be prefixed by the analysis (service)
     * name whose metadata this file contains. For example, for
     * service "edit.seqret", the full property name should be:
     * "edit.seqret" + PROP_METADATA_FILE. <p>
     *
     * There is no default value. <p>
     */
    public static final String PROP_METADATA_FILE = ".metadata.file";

    /**
     * A property name. Its boolean true value indicates that event
     * listeners should ignore Heartbeat Progress events (because
     * there may be too many of them). <p>
     *
     * Default value is false. <p>
     */
    public static final String PROP_EVENT_IGNORE_HEARBEAT = "ignore.heartbeat.events";


    /**
     * A property name. Its value indicates a logging level under
     * which class <tt>DefaultEventListener</tt> logs Soaplab's
     * events. The accepted values are (in case-insensitive way):
     * trace, debug, info, warn, error, fatal, and off ('off' means no
     * logging at all). <p>
     *
     * Default value is debug. <p>
     */
    public static final String PROP_EVENT_LOG_LEVEL = "event.log.level";


    /**
     * A property name. Its value contains a directory name where the
     * inputs and outputs of the running external process will be
     * stored in. <p>
     *
     * No default value; instead a temporary directory will be
     * used. <p>
     */
    public static final String PROP_WORKING_DIR = "working.dir";

    /**
     * A property name. The same meaning as {@link #PROP_WORKING_DIR}.
     */
    public static final String PROP_SANDBOX_DIR = PROP_WORKING_DIR;

    /**
     * A property name. Its boolean true value indicates that inputs
     * are kept in the working directory (in the "sandbox") even after
     * they had been used by an external program. This is good for
     * debugging. <p>
     *
     * Default value is false. <p>
     */
    public static final String PROP_KEEP_INPUTS = "keep.inputs";

    /**
     * A property name. Its value contains a URL for Gowlab-based
     * services. <p>
     */
    public static final String PROP_SUPPLIER = "supplier.url";

    /**
     * A property name. Its value specifies a service launcher. <p>
     */
    public static final String PROP_LAUNCHER = "launcher";

    /**
     * A property name. Its value is a prefix used in names of those
     * properties who will become environment variables.
     */
    public static final String PROP_ENVAR = "env";

    /**
     * A property name. Its value contains one or more directory names
     * (separated by commas) that are added to the PATH environment
     * variable before starting an external tool. <p>
     *
     * There is no default value.
     */
    public static final String PROP_ADDTOPATH_DIR = "addtopath.dir";

    /**
     * A property name. Its value is a directory name with scripts
     * that could be called by Soaplab services. In order to achieve
     * it, however, the same directory must be also added to the
     * property {@link #PROP_ADDTOPATH_DIR} - Soaplab does not do it
     * automatically. But Soaplab tries to make these scripts
     * executable by changing their permissions appropriately. This is
     * useful when the scripts were transported to the servlet
     * container in a war file (because unpacking the war file does
     * not preserve the files permissions). <p>
     *
     * There is no default value.
     */
    public static final String PROP_SCRIPTS_DIR = "scripts.dir";

    /**
     * A property name. Its boolean true value indicates that any exit
     * code returned by the invoked external program is treated as a
     * good one (used in sowa). Otherwise only zero exit code is
     * acceptable as a good one. <p>
     *
     * Default value is false.
     */
    public static final String PROP_ACCEPT_ANY_EXITCODE = "accept.any.exitcode";

    /**
     * A property name. Its boolean true value indicates to ignore
     * creation of the results of type URL. <p>
     *
     * Default value is false.
     */
    public static final String PROP_RESULTS_URL_IGNORE = "results.url.ignore";

    /**
     * A property name. Its value is a hostname where a Tomcat server
     * is serving Soaplab2 services from. <p>
     *
     * Default value is localhost.
     */
    public static final String PROP_TOMCAT_HOST = "tomcat.host";

    /**
     * A property name. Its integer value is a port number where a
     * Tomcat server is serving Soaplab2 services from. <p>
     *
     * Default value is 8080.
     */
    public static final String PROP_TOMCAT_PORT = "tomcat.port";

    /**
     * A property name. Its value is a directory (usually with the
     * full path) where Soaplab will be copying results that should be
     * viewed (accessed) via protocols such as HTTP or FTP. This
     * property is a full path of the same directory where the
     * property {@link #PROP_RESULTS_URL} points to as a URL. <p>
     *
     * Default value depends on the context: if a Soaplab service is
     * running in a servlet container (such as Tomcat - which is a
     * usual case), a default directory is created inside Soaplab web
     * application. Otherwise, there is no default (which means that
     * the URL-based results will not be created).
     */
    public static final String PROP_RESULTS_URL_DIR = "results.url.target.dir";

    /**
     * A property name. Its value is a URL (without the last file
     * name) where the Soaplab's results are accessible from. This URL
     * points to the same place as specified by the property {@link
     * #PROP_RESULTS_URL_DIR}. <p>
     *
     * Default value is http://localhost:8080/soaplab2/results.
     */
    public static final String PROP_RESULTS_URL = "results.url";

    /**
     * A property name. Its value contains a full path to directory
     * that is "home" directory of the project. <p>
     *
     * Default value depends on the context: if a Soaplab service is
     * running in a servlet container (such as Tomcat - which is a
     * usual case), a default value points to the Soaplab web
     * application (inside Tomcat). Otherwise, it is usually the place
     * current directory. <p>
     */
    public static final String PROP_BASE_DIR = "base.dir";
    
    /**
     * A property name. Its boolean true value indicates that sowa
     * jobs are executed using LSF. <p>
     *
     */
    public static final String LSF_ENABLED   = "lsf.enabled";

    /**
     * A property name. Its value is a queue name for submitting 
     * LSF jobs. <p>
     *
     */
    public static final String LSF_QUEUENAME = "lsf.queuename";

    /**
     * A property name. Its value is an architecture for submitting 
     * LSF jobs on. <p>
     *
     */
    public static final String LSF_ARCH      = "lsf.arch";

    /**
     * A property name. Its value is a project name for 
     * LSF jobs. <p>
     *
     */
    public static final String LSF_PROJECT   = "lsf.project";
}
