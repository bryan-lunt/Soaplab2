// AnalysisListService.java
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

import org.soaplab.share.AnalysisList;
import org.soaplab.share.SoaplabMap;

/**
 * The analysis list service is a service that produces a list of
 * available analysis services, and provides pointers to them. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: AnalysisListService.java,v 1.10 2007/10/19 08:53:42 marsenger Exp $
 */

public class AnalysisListService
    implements AnalysisList {

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
    public SoaplabMap getAvailableAnalysesWithDescription() {
	return AnalysisInventoryProvider.instance()
	    .getAvailableAnalysesWithDescription();
    }

    /**************************************************************************
     *
     *************************************************************************/
    public String[] getAvailableAnalysesInCategory (String categoryName) {
	return AnalysisInventoryProvider.instance().getAvailableAnalysesInCategory (categoryName);
    }

    /**************************************************************************
     * This implementation returns the parameter 'analysisName'
     * unchanged.
     *************************************************************************/
    public String getServiceLocation (String analysisName) {
	return analysisName;
    }

}
