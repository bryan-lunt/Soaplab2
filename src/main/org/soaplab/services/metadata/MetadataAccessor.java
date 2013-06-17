// MetadataAccessor.java
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

package org.soaplab.services.metadata;

import org.soaplab.share.SoaplabException;
import org.soaplab.share.SoaplabConstants;

/**
 * An interface to get metadata describing behaviour, and possibly
 * even look-and-feel, for any analysis. An analysis is typically a
 * remote program, often, but not necessarily, a command-line
 * program. It can be also other remote resource, like a web page -
 * the result of such "analysis" is screen-scraped content of the
 * page. <p>
 *
 * This interface does not address the problem where to get metadata
 * from (for example by reading and parsing an XML file). It is up to
 * the implementation of this interface to solve it. For example, look
 * at {@link
 * org.soaplab.services.metadata.DefaultMetadataAccessorFactory}. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: MetadataAccessor.java,v 1.6 2007/11/04 11:53:56 marsenger Exp $
 */
public interface MetadataAccessor {

    /******************************************************************************
     * Return a general description of an analysis. <p>
     *
     * @return an analysis metadata, or null if such metadata does not
     * exist (but that would cause a havoc in Soaplab)
     ******************************************************************************/
    AnalysisDef getAnalysisDef();

    /******************************************************************************
     * Return description of all parameters for an analysis. A
     * <em>parameter</em> is any input or output. It contains the
     * usual input/output data and the command-line options. <p>
     *
     * @return an array of metadata, one element for each parameter;
     * if there are no parameters, it returns an empty array (not
     * null)
     ******************************************************************************/
    ParamDef[] getParamDefs();

    /******************************************************************************
     * Return a slightly simplified version of what is returned by
     * {@link #getParamDefs} - and only for input data and
     * options. <p>
     *
     * Some elements (parameters) returned by {@link #getParamDefs}
     * can be represented here by more elements. For example, this
     * typically happens for input data that can be sent to an
     * analysis either as direct data, or as a reference to data. Such
     * input is represented by only one {@link ParamDef} instance, but
     * by two elements returned by this <tt>getInputDefs</tt>. <p>
     *
     * The mapping between elements returned by {@link #getParamDefs}
     * and by this method is done by parameter name: the names used by
     * this method are either same as those returned by {@link
     * #getParamDefs}, or they have some additional suffixes. Soaplab
     * recognizes suffixes {@link SoaplabConstants#DIRECT_DATA_SUFFIX
     * SoaplabConstants.DIRECT_DATA_SUFFIX}, {@link SoaplabConstants#URL_SUFFIX
     * SoaplabConstants.URL_SUFFIX},{@link SoaplabConstants#USA_SUFFIX
     * SoaplabConstants.USA_SUFFIX}. <p>
     *
     * @return an array of metadata; if there are no input parameters,
     * it returns an empty array (not null)
     ******************************************************************************/

    InputPropertyDef[]  getInputDefs();
    /******************************************************************************
     * Return a slightly simplified version of what is returned by
     * {@link #getParamDefs} - and only for output data. <p>
     *
     * The mapping between elements returned by {@link #getParamDefs}
     * and by this method is done by parameter name: the names used by
     * this method are the same as those returned by {@link
     * #getParamDefs}. <p>
     *
     * @return an array of metadata; if there are no output data, it
     * returns an empty array (not null)
     ******************************************************************************/
    OutputPropertyDef[] getOutputDefs();

    /******************************************************************************
     * Return the whole metadata in the raw format. <p>
     *
     * @return an XML representation of the analysis metadata, using the
     * <a
     * href="http://soaplab.sourceforge.net/soaplab2/SoaplabAnalysis.dtd">SoaplabAnalysis.dtd</a>.
     *
     * @throws SoaplabException if metadata does not exist
     ******************************************************************************/
    String describe()
	throws SoaplabException;

    /******************************************************************************
     * Return a name of a service providing this analysis. <p>
     *
     * @return a name of a service - this may be different from the
     * analysis name returned within {@link #getAnalysisDef}
     ******************************************************************************/
    String getServiceName()
	throws SoaplabException;

}
