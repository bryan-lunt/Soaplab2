// ClientConfig.java
//
// Created: June 2007
//
// Copyright 2007 Martin Senger
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

package org.soaplab.clients;

import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.BooleanUtils;

/**
 * An abstract class giving access to the global Soaplab client
 * configuration. It does similar (but less) things as the
 * <tt>org.soaplab.services.Config</tt>. But from the various
 * technical reasons, it is not connected to it by inheritance. <p>
 *
 * It also contains names and description of (most of) the Soaplab
 * client configuration properties. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: ClientConfig.java,v 1.7 2008/01/25 12:27:34 marsenger Exp $
 */

public abstract class ClientConfig {

    private static org.apache.commons.logging.Log log =
       org.apache.commons.logging.LogFactory.getLog (ClientConfig.class);

    private static CompositeConfiguration config;

    /**
     * A filename indicating a file containing Soaplab client
     * properties. This file name can be changed by setting Java
     * property {@link #PROP_SOAPLAB_CLIENT_CONFIGURATION} to point to
     * a different file name.
     */
    public static final String SOAPLAB_CLIENT_CONFIG_FILENAME = "soaplab.client.properties";

    /**************************************************************************
     * The main method returning a configuration object. The returned
     * object is a singleton - but its contents (the properties it
     * carries) can be changed dynamically (the properties are
     * reloaded if their files are modified). <p>
     *
     * The configuration object contains all Java system properties
     * and properties read from the Soaplab client configuration
     * file. This file name is given by the system property {@link
     * #PROP_SOAPLAB_CLIENT_CONFIGURATION}, or using a default name
     * {@link #SOAPLAB_CLIENT_CONFIG_FILENAME}. If the filename does
     * not specify an absolute path, the file will be searched
     * automatically in the following locations:
     *
     * <ul>
     *  <li> in the current directory,
     *  <li> in the user home,
     *  <li> directory in the classpath
     * </ul><p>
     *
     * The System properties take precedence over properties read from
     * the Soaplab client property file. <p>
     *
     * @return a configuration object
     **************************************************************************/
    public static synchronized CompositeConfiguration get() {
	if (config == null) {

	    // set the global configuration
	    CompositeConfiguration cfg = new CompositeConfiguration();

	    // first, add System properties
	    cfg.addConfiguration (new SystemConfiguration());

	    // second, add Soaplab client properties file(s)
	    String[] configFilenames =
		cfg.getStringArray (PROP_SOAPLAB_CLIENT_CONFIGURATION);
	    if (configFilenames == null || configFilenames.length == 0)
		configFilenames = new String[] { SOAPLAB_CLIENT_CONFIG_FILENAME };
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
     * Add given property files as a new configuration to 'cfg'
     * composite configuration. Return true on success.
     **************************************************************************/
    private static boolean addPropertiesConfiguration (CompositeConfiguration cfg,
						       String configFilename) {
	try {
	    PropertiesConfiguration propsConfig =
		new PropertiesConfiguration (configFilename);
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
     * For the property values 'true', 'on', 'yes' (case insensitive),
     * and for an empty value it returns true. For other values, false
     * is returned. If the property does not exist at all, the
     * 'defaultValue' is returned. <p>
     **************************************************************************/
    public static boolean isEnabled (String key, boolean defaultValue) {

	String strValue = get().getString (key, null);
	if (strValue == null)
	    return defaultValue;
	if (StringUtils.isBlank (strValue))
	    return true;
	return BooleanUtils.toBoolean (strValue);
    }

    /**************************************************************************
     * If the key is found but its value is not an integer, or if it
     * is not found at all, it returns the given default value. <p>
     **************************************************************************/
    public static int getInt (String key, int defaultValue) {

	String strValue = get().getString (key, ""+defaultValue);
	try {
	    return Integer.decode (strValue).intValue();
	} catch (NumberFormatException e) {
	    return defaultValue;
	}
    }

    // ----------------------------------------------------------
    //
    //                      Property names
    //
    // ----------------------------------------------------------

    /**
     * A property name. Its value contains a filename indicating a
     * file containing Soaplab client configuration
     * properties. Default value is {@link
     * #SOAPLAB_CLIENT_CONFIG_FILENAME}.
     */
    public static final String PROP_SOAPLAB_CLIENT_CONFIGURATION = "soaplab.client.configuration";

    // -----------------
    // Protocols related
    // -----------------

    /**
     * A property name. Its value is a protocol used to access Soaplab
     * services. Its value can be: {@link #DEFAULT_PROTOCOL}, {@link
     * #PROTOCOL_JAXWS}, {@link #PROTOCOL_LOCAL}, {@link
     * #PROTOCOL_AXIS1} and perhaps more.<p>
     *
     * Default value is (usually) {@link #DEFAULT_PROTOCOL}.
     */
    public static final String PROP_PROTOCOL = "protocol";

    /**
     * A JAX-WS protocol, for accessing Soaplab services.
     */
    public static final String PROTOCOL_JAXWS = "jaxws";

    /**
     * A protocol using local classes to access Soaplab services.
     */
    public static final String PROTOCOL_LOCAL = "local";

    /**
     * An Axis 1 protocol, for accessing Soaplab services.
     */
    public static final String PROTOCOL_AXIS1 = "axis1";

    /**
     * A default protocol, for accessing Soaplab services.
     */
    public static final String DEFAULT_PROTOCOL = PROTOCOL_JAXWS;

    // ------------------------------------------
    // Providers of classes for various protocols
    // ------------------------------------------

    /**
     * A property name. Its value is a class name implementing {@link
     * ServiceProvider} interface for the {@link #PROTOCOL_JAXWS}
     * protocol. <p>
     *
     * Default value is (usually) {@link #DEFAULT_CLASS_PROVIDER_JAXWS}.
     */
    public static final String PROP_CLASS_PROVIDER_JAXWS
	= "class.provider.jaxws";

    /**
     * A default class name implementing {@link ServiceProvider}
     * interface for {@link #PROTOCOL_JAXWS} protocol.
     */
    public static final String DEFAULT_CLASS_PROVIDER_JAXWS
	= "org.soaplab.clients.jaxws.ServiceProviderImpl";

    /**
     * A property name. Its value is a class name implementing {@link
     * ServiceProvider} interface for the {@link #PROTOCOL_AXIS1}
     * protocol. <p>
     *
     * Default value is (usually) {@link #DEFAULT_CLASS_PROVIDER_AXIS1}.
     */
    public static final String PROP_CLASS_PROVIDER_AXIS1
	= "class.provider.axis1";

    /**
     * A default class name implementing {@link ServiceProvider}
     * interface for {@link #PROTOCOL_AXIS1} protocol.
     */
    public static final String DEFAULT_CLASS_PROVIDER_AXIS1
	= "org.soaplab.clients.axis1.ServiceProviderImpl";

    /**
     * A property name. Its value is a class name implementing {@link
     * ServiceProvider} interface for the {@link #PROTOCOL_LOCAL}
     * protocol. <p>
     *
     * Default value is (usually) {@link #DEFAULT_CLASS_PROVIDER_LOCAL}.
     */
    public static final String PROP_CLASS_PROVIDER_LOCAL
	= "class.provider.local";

    /**
     * A default class name implementing {@link ServiceProvider}
     * interface for {@link #PROTOCOL_LOCAL} protocol.
     */
    public static final String DEFAULT_CLASS_PROVIDER_LOCAL
	= "org.soaplab.clients.local.ServiceProviderImpl";

    // -----------------
    // Endpoints related
    // -----------------

    /**
     * A property name. Its value is a hostname serving Soaplab
     * services. <p>
     *
     * Default value is (usually) {@link #DEFAUTL_HOST}.
     */
    public static final String PROP_HOST = "host";

    /**
     * A default value for {@link #PROP_HOST}.
     */
    public static final String DEFAULT_HOST = "localhost";

    /**
     * A property name. Its value is a port serving Soaplab
     * services. <p>
     *
     * Default value is (usually) {@link #DEFAULT_PORT}.
     */
    public static final String PROP_PORT = "port";

    /**
     * A default value for {@link #PROP_PORT}.
     */
    public static final String DEFAULT_PORT = "8080";

    /**
     * A property name. Its value is a context name (used by a servlet
     * container) for Soaplab services. <p>
     *
     * Default value is (usually) {@link #DEFAUT_CONTEXT_NAME}.
     */
    public static final String PROP_CONTEXT_NAME = "context.name";

    /**
     * A default value for {@link #PROP_CONTEXT_NAME}.
     */
    public static final String DEFAULT_CONTEXT_NAME = "soaplab2";


    //
    // properties for the batch testing client
    //


    /**
     * A property name. Its value is a filename containing list of
     * services to be tested and their inputs. See {@link
     * org.soaplab.clients.BatchTestClient} for deatils. <p>
     *
     * There is no default value.
     */
    public static final String PROP_BATCH_TEST_FILE = "batch.test.file";

    /**
     * A property name. Its boolean value true means that a tabular
     * report will be created. <p>
     *
     * Default value is false.
     */
    public static final String PROP_BATCH_REPORT_TABLE = "batch.report.table";

    /**
     * A property name. Its boolean value true means that the testing
     * jobs will not be removed after finishing the whole test
     * suite. <p>
     *
     * Default value is false.
     */
    public static final String PROP_BATCH_KEEP_RESULTS = "batch.keep.results";

    /**
     * A property name. Its integer value indicates maximum number of
     * services tested in parallel. Value 1 means to test services
     * sequentially. Value 0 means test all of them at once. <p>
     *
     * Default value is 25.
     */
    public static final String PROP_BATCH_MAX_THREADS = "batch.max.threads";


}
