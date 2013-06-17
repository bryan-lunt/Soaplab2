// AnalysisListService.java
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

package org.soaplab.services.protocol.jaxws;

import org.soaplab.share.SoaplabConstants;
import org.soaplab.share.SoaplabMap;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.WebServiceContext;
import javax.servlet.http.HttpServletRequest;
import javax.annotation.Resource;

/**
 * The analysis list is a JAX-WS Web Service implementation of a
 * service that produces a list of available analysis services, and
 * provides pointers to them. It contains only 'Java Web Services'
 * specific stuff - the business logic is in the super-class. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: AnalysisListService.java,v 1.4 2007/10/19 08:53:42 marsenger Exp $
 */

@WebService
    (name            = "AnalysisListService",
     serviceName     = SoaplabConstants.LIST_SERVICE_NAME,
     targetNamespace = SoaplabConstants.SOAPLAB_NAMESPACE,
     portName        = SoaplabConstants.LIST_SERVICE_PORT_NAME)
    public class AnalysisListService
    extends org.soaplab.services.AnalysisListService {

    /**************************************************************************
     * The main constructor.
     *************************************************************************/
    public AnalysisListService() {
    }


    // ------------------------------------------------------------------------
    //
    //  Methods implementing AnalysisList interface
    //
    // ------------------------------------------------------------------------


    /**************************************************************************
     *
     *************************************************************************/
    @WebMethod
	@RequestWrapper  (className = "org.soaplab.share.jaxws.GetAvailableCategories")
	@ResponseWrapper (className = "org.soaplab.share.jaxws.GetAvailableCategoriesResponse")
	@Override public String[] getAvailableCategories() {
	return super.getAvailableCategories();
    }

    /**************************************************************************
     *
     *************************************************************************/
    @WebMethod
	@RequestWrapper  (className = "org.soaplab.share.jaxws.GetAvailableAnalyses")
	@ResponseWrapper (className = "org.soaplab.share.jaxws.GetAvailableAnalysesResponse")
	@Override public String[] getAvailableAnalyses() {
	return super.getAvailableAnalyses();
    }

    /**************************************************************************
     *
     *************************************************************************/
    @WebMethod
	@RequestWrapper  (className = "org.soaplab.share.jaxws.GetAvailableAnalysesWithDescription")
	@ResponseWrapper (className = "org.soaplab.share.jaxws.GetAvailableAnalysesWithDescriptionResponse")
	@Override public SoaplabMap getAvailableAnalysesWithDescription() {
	return super.getAvailableAnalysesWithDescription();
    }

    /**************************************************************************
     *
     *************************************************************************/
    @WebMethod
	@RequestWrapper  (className = "org.soaplab.share.jaxws.GetAvailableAnalysesInCategory")
	@ResponseWrapper (className = "org.soaplab.share.jaxws.GetAvailableAnalysesInCategoryResponse")
	@Override public String[] getAvailableAnalysesInCategory (String categoryName) {
	return super.getAvailableAnalysesInCategory (categoryName);
    }

    /**************************************************************************
     *
     *************************************************************************/
    @WebMethod
	@RequestWrapper  (className = "org.soaplab.share.jaxws.GetServiceLocation")
	@ResponseWrapper (className = "org.soaplab.share.jaxws.GetServiceLocationResponse")
	@Override public String getServiceLocation (String analysisName) {

	// precaution
	if (analysisName == null)
	    analysisName = "";

	// do we have a Web Service Context?
	if (wsc == null)
	    return analysisName;

	MessageContext mcx = null;
	try {
	    mcx = wsc.getMessageContext();
	    if (mcx == null)
		return analysisName;
	} catch (java.lang.IllegalStateException e) {
	    return analysisName;
	}
	HttpServletRequest req =
	    (HttpServletRequest)mcx.get (MessageContext.SERVLET_REQUEST);
	if (req == null)
	    return analysisName;
	StringBuffer requestURL = req.getRequestURL();

	// replace the name of this list service by the given analysis
	// name in this request URL
	while (requestURL.length() > 0) {
	    int lastChar = requestURL.length() - 1;
	    if (requestURL.charAt (lastChar) == '/') {
		requestURL.append (analysisName);
		return requestURL.toString();
	    }
	    requestURL.deleteCharAt (lastChar);
	}
	return analysisName;
    }
	
    // this is filled by an "injection" (by Tomcat when a request comes)
    @Resource private WebServiceContext wsc;
}
