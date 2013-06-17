// ServiceProviderFactory.java
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

import static org.soaplab.clients.ClientConfig.*;

import org.soaplab.share.SoaplabException;
import org.soaplab.tools.ICreator;

/**
 * A class that is able to find, instantiate and return a proper
 * implementation of a class that represents a Soaplab Analysis
 * service and (another instance of) a Soaplab List service. <p>
 *
 * The class uses given Service locator (that uses internally Soaplab
 * client configuration (see {@link ClientConfig}) to find what
 * protocol is wanted and from that it deduces which classes should be
 * used. The classes are instantiated dynamically in the run-time (so
 * the compiler does not need to know classes for all possible
 * protocols). <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: ServiceProviderFactory.java,v 1.5 2007/10/06 18:32:13 marsenger Exp $
 */
public class ServiceProviderFactory {

    private static org.apache.commons.logging.Log log =
	org.apache.commons.logging.LogFactory.getLog (ServiceProviderFactory.class);

    ServiceLocator locator;

    /*************************************************************************
     * A non-args constructor. Just for sub-classes.
     *************************************************************************/
    protected ServiceProviderFactory() {
    }

    /*************************************************************************
     * The main constructor. <p>
     *
     * @param locator identifies (together with properties from a
     * Soaplab client configuration file) what service should be
     * called. <p>
     *************************************************************************/
    public ServiceProviderFactory (ServiceLocator locator) {
	this.locator = locator;
    }

    /**************************************************************************
     * Return an instance of a service provider that provides services
     * according to the service locator provided in the constructor,
     * and according to the properties defined in the client
     * configuration file. <p>
     *
     * @throw SoaplabException if the locator contains an unknown
     * protocol, or if it cannot instantiate the class providing such
     * protocol
     *************************************************************************/
    public ServiceProvider getServiceProvider()
	throws SoaplabException {

	if (locator == null) {
	    locator = new ServiceLocator();
	}

	String protocol = locator.getProtocol();
	String className = null;
	if (protocol.equals (PROTOCOL_JAXWS)) {
	    className =	ClientConfig.get().getString (PROP_CLASS_PROVIDER_JAXWS,
						      DEFAULT_CLASS_PROVIDER_JAXWS);
	} else if (protocol.equals (PROTOCOL_LOCAL)) {
	    className =	ClientConfig.get().getString (PROP_CLASS_PROVIDER_LOCAL,
						      DEFAULT_CLASS_PROVIDER_LOCAL);
	} else if (protocol.equals (PROTOCOL_AXIS1)) {
	    className =	ClientConfig.get().getString (PROP_CLASS_PROVIDER_AXIS1,
						      DEFAULT_CLASS_PROVIDER_AXIS1);
	} else {
	    throw new SoaplabException ("Unknown service protocol: " + protocol);
	}
	log.debug ("Using ServiceProvider class: " + className);

	try {
	    // all our ServiceProviders have one-arg constructor but
	    // perhaps not all are ours - so try first and ignore errors
	    return (ServiceProvider)ICreator.createInstance
		(className,
		 new Class[] { ServiceLocator.class },
		 new Object[] { locator });
	} catch (SoaplabException e) {
	    // ...now go for a non-args constructor
	    log.warn ("Using a non-arg constructor for: " + className +
		      " (" + e.getMessage() + ")");
	    return (ServiceProvider)ICreator.createInstance (className);
	}
    }

}
