// AnalysisList.java
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

package org.soaplab.share;

/**
 * An interface to a Web Service that produces a list of available
 * analyses, and provides pointers to them. <p>
 *
 * The interface uses two similar but not the same entities:
 * an <b>analysis</b> and a <b>service</b>.
 * An <em>analysis</em> is a program that has to be executed and
 * controlled (such as <tt>Blast</tt>), while <em>service</em> is
 * a Web Service that allows to execute and to controll the analysis.
 * A service is a wrapper around an analysis. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: AnalysisList.java,v 1.7 2007/10/19 08:53:42 marsenger Exp $
 */
// @WebService (targetNamespace = SoaplabConstants.SOAPLAB_NAMESPACE)
public interface AnalysisList {

    /**
     * Return an array of names of all available categories.
     * A category is a group of analyses, usually of a similar purpose.
     * Any analysis can belong to more than one category. <p>
     */
    String[] getAvailableCategories();

    /**
     * Return an array of names of all available analyses, regardless
     * which category they belong to. <p>
     *
     * <a name="analyses_names">
     * <h5>Naming convention: How to name analyses</h5>
     * This service recognizes analyses divided into categories.  A
     * full name of an analysis consists of the category name and an
     * analysis name, separated by {@link
     * SoaplabConstants#SOAPLAB_SERVICE_NAME_DELIMITER}. For example:
     * <pre>
     *   Classic.HelloWorld
     *   Reformatting.SeqRet
     *   Coding_Regions.PlotORF
     * </pre>
     * However, an implementation may recognize also names without
     * any category name. It may mean that the given analysis name
     * is looked for in all categories until a match is found.
     */
    String[] getAvailableAnalyses();

    /**
     * Return an array of names of all available analyses from the
     * given category. The returned names contain also the category
     * name (see <a href="#analyses_names">naming
     * conventions</a>). <p>
     *
     * @param categoryName whose analyses are wanted
     * @return a list of analyses names, or an empoty list if given
     * category does not exist
     */
    String[] getAvailableAnalysesInCategory (String categoryName);

    /**
     * Return a location of a web service representing given analysis.
     * The <tt>analysisName</tt> should comply with the
     * <a href="#analyses_names">naming conventions</a>.
     * The returned value is a URL containing a location of a service
     * that complies with the interface {@link Analysis}. <p>
     *
     * @param analysisName that is wanted
     * @return an endpoint of a service, or an empty string if such
     * analysis does not exist
     */
    String getServiceLocation (String analysisName);

    /******************************************************************************
     * Return the same list as by method {@link #getAvailableAnalyses}
     * but now enriched by short description for each analysis. <p>
     *
     * @return a map where keys are analysis names, and values (of
     * type String) are their descriptions
     ******************************************************************************/
    SoaplabMap getAvailableAnalysesWithDescription();


}
