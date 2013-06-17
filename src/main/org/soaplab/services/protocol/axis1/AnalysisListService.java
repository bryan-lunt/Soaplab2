// AnalysisListService.java
//
// Created: September 2007
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

package org.soaplab.services.protocol.axis1;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.axis.MessageContext;
import org.apache.axis.transport.http.HTTPConstants;
import org.soaplab.services.AnalysisInventoryProvider;
import org.soaplab.share.SoaplabMap;

/**
 * This analysis list is an Axis-1 (the old Apache Axis) Web Service
 * implementation of a service that produces a list of available
 * analysis services, and provides pointers to them. It should contain
 * only 'Axis-1' specific stuff - the business logic is in the
 * super-class - but it is empty because there is nothing Axis-1
 * specific for this kind of service. <p>
 *
 * Note that the old Axis (Axis 1) is really not much supported by
 * Soaplab2 - it exists mainly because of backward compatibility. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: AnalysisListService.java,v 1.4 2009/06/19 12:32:10 mahmutuludag Exp $
 */

public class AnalysisListService {

	public String getServiceLocation(String analysisName) {
		
		String[] analyses = getAvailableAnalyses();
		boolean ok = false;
		for (String a:analyses){
			if (a.equalsIgnoreCase(analysisName)){
				ok = true;
				break;
			}			
		}
		if (!ok)
			return "";

		MessageContext ctx = MessageContext.getCurrentContext();
		if (ctx==null)
			return analysisName;
		HttpServletRequest req = (HttpServletRequest) ctx
				.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
		if (req ==null)
			return analysisName;
		String url = new String(req.getRequestURL());
		int pos = url.lastIndexOf("/");
		if (pos > -1)
			return url.substring(0, pos + 1) + analysisName;
		else
			return url + "/" + analysisName;
	}
	

    /**************************************************************************
     *
     *************************************************************************/
    public String[] getAvailableCategories() {
	return AnalysisInventoryProvider.instance().getAvailableCategories();
    }

    /**************************************************************************
     *
     *************************************************************************/
    public String[] getAvailableAnalyses() {
	return AnalysisInventoryProvider.instance().getAvailableAnalyses();
    }

    /******************************************************************************
     *
     ******************************************************************************/
    public Map getAvailableAnalysesWithDescription() {
	return SoaplabMap.toMap(AnalysisInventoryProvider.instance()
	    .getAvailableAnalysesWithDescription());
    }

    /**************************************************************************
     *
     *************************************************************************/
    public String[] getAvailableAnalysesInCategory (String categoryName) {
	return AnalysisInventoryProvider.instance().getAvailableAnalysesInCategory (categoryName);
    }



}
