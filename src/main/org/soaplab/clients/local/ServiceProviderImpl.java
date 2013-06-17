// ServiceProviderImpl.java
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

package org.soaplab.clients.local;

import org.soaplab.clients.ServiceProvider;
import org.soaplab.clients.ServiceLocator;
import org.soaplab.share.SoaplabException;
import org.soaplab.share.Analysis;
import org.soaplab.share.AnalysisList;

import org.apache.commons.lang.StringUtils;

/**
 * A specific implementation of the {@link ServiceProvider} interface
 * using local Soaplab classes (without calling any remote Web
 * Service). <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: ServiceProviderImpl.java,v 1.3 2007/09/22 13:44:49 marsenger Exp $
 */
public class ServiceProviderImpl
    implements ServiceProvider {

    ServiceLocator locator;

    /*************************************************************************
     * A non-args constructor. Just for sub-classes.
     *************************************************************************/
    protected ServiceProviderImpl() {
    }

    /*************************************************************************
     * The main constructor. <p>
     *
     * @param locator identifies (together with properties from a
     * Soaplab client configuration file) what service should be
     * called.
     *************************************************************************/
    public ServiceProviderImpl (ServiceLocator locator) {
	this.locator = locator;
    }

    /**************************************************************************
     *
     *************************************************************************/
    public AnalysisList getListService()
	throws SoaplabException {
  	return new org.soaplab.services.AnalysisListService();
    }

    /**************************************************************************
     *
     *************************************************************************/
    public Analysis getService()
	throws SoaplabException {
	String serviceName = locator.getServiceName();
	if (StringUtils.isEmpty (serviceName))
	    throw new SoaplabException ("No service name given (for 'local' protocol).");
  	return new org.soaplab.services.AnalysisService (serviceName);
    }

}
