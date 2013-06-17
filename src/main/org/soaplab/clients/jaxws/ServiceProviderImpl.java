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

package org.soaplab.clients.jaxws;

import org.soaplab.clients.ServiceProvider;
import org.soaplab.clients.ServiceLocator;
import org.soaplab.share.SoaplabException;
import org.soaplab.share.SoaplabConstants;
import org.soaplab.share.Analysis;
import org.soaplab.share.AnalysisList;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import java.net.URL;

/**
 * A JAX-WS specific implementation of the {@link ServiceProvider}
 * interface. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: ServiceProviderImpl.java,v 1.2 2007/09/22 13:44:49 marsenger Exp $
 */
public class ServiceProviderImpl
    implements ServiceProvider {

    // names of some resources
    protected static String RES_LIST_WSDL = "wsdl/AnalysisList.wsdl";
    protected static String RES_WSDL      = "wsdl/Analysis.wsdl";

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

        try {
	    String wsdlFile = RES_LIST_WSDL;
	    URL url = ServiceProvider.class.getResource (wsdlFile);
	    if (url == null)
		throw new SoaplabException ("WSDL not found when looking for '" + wsdlFile + "'");

	    Service svc =
		Service.create (url,
				new QName (SoaplabConstants.SOAPLAB_NAMESPACE,
					   SoaplabConstants.LIST_SERVICE_NAME));
	    AnalysisList port
		= svc.getPort (new QName (SoaplabConstants.SOAPLAB_NAMESPACE,
					  SoaplabConstants.LIST_SERVICE_PORT_NAME),
			       org.soaplab.share.jaxws.AnalysisList.class);
 	    ((BindingProvider)port)
 		.getRequestContext()
 		.put (BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
		      locator.getListEndpoint());

	    return port;	    
	    
        } catch (WebServiceException e) {
	    throw new SoaplabException ("Getting a proxy of a list service failed.", e);
        }
    }

    /**************************************************************************
     *
     *************************************************************************/
    public Analysis getService()
	throws SoaplabException {

        try {
	    String wsdlFile = RES_WSDL;
	    URL url = ServiceProvider.class.getResource (wsdlFile);
	    if (url == null)
		throw new SoaplabException ("WSDL not found when looking for '" + wsdlFile + "'");

	    Service svc =
		Service.create (url,
				new QName (SoaplabConstants.SOAPLAB_NAMESPACE,
					   SoaplabConstants.SERVICE_NAME));
	    Analysis port
		= svc.getPort (new QName (SoaplabConstants.SOAPLAB_NAMESPACE,
					  SoaplabConstants.SERVICE_PORT_NAME),
			       org.soaplab.share.jaxws.Analysis.class);
 	    ((BindingProvider)port)
 		.getRequestContext()
 		.put (BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
		      locator.getServiceEndpoint());

	    return port;	    
	    
        } catch (WebServiceException e) {
	    throw new SoaplabException ("Getting a proxy of an analysis service failed.", e);
        }
    }

}
