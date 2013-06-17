// AnalysisList.java
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

package org.soaplab.share.jaxws;

import org.soaplab.share.SoaplabConstants;
import org.soaplab.share.SoaplabMap;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * A JAX-WS specific flavour of a general interface to a Web Service
 * that produces a list of available analyses, and provides pointers
 * to them. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: AnalysisList.java,v 1.2 2007/10/19 08:53:42 marsenger Exp $
 */
@WebService (targetNamespace = SoaplabConstants.SOAPLAB_NAMESPACE)
public interface AnalysisList
    extends org.soaplab.share.AnalysisList {

    @WebMethod
	@RequestWrapper  (className = "org.soaplab.share.jaxws.GetAvailableCategories")
	@ResponseWrapper (className = "org.soaplab.share.jaxws.GetAvailableCategoriesResponse")
	String[] getAvailableCategories();

    @WebMethod
	@RequestWrapper  (className = "org.soaplab.share.jaxws.GetAvailableAnalyses")
	@ResponseWrapper (className = "org.soaplab.share.jaxws.GetAvailableAnalysesResponse")
	String[] getAvailableAnalyses();

    @WebMethod
	@RequestWrapper  (className = "org.soaplab.share.jaxws.GetAvailableAnalysesWithDescription")
	@ResponseWrapper (className = "org.soaplab.share.jaxws.GetAvailableAnalysesWithDescriptionResponse")
	SoaplabMap getAvailableAnalysesWithDescription();

    @WebMethod
	@RequestWrapper  (className = "org.soaplab.share.jaxws.GetAvailableAnalysesInCategory")
	@ResponseWrapper (className = "org.soaplab.share.jaxws.GetAvailableAnalysesInCategoryResponse")
	String[] getAvailableAnalysesInCategory (String categoryName);

    @WebMethod
	@RequestWrapper  (className = "org.soaplab.share.jaxws.GetServiceLocation")
	@ResponseWrapper (className = "org.soaplab.share.jaxws.GetServiceLocationResponse")
	String getServiceLocation (String analysisName);

}
