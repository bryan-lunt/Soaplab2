// ServiceProvider.java
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

import org.soaplab.share.SoaplabException;
import org.soaplab.share.Analysis;
import org.soaplab.share.AnalysisList;

/**
 * An interface whose implementations provide access to Soaplab
 * services. Different implementations support different access
 * protocols to the Soaplab services. An advanced client may use
 * {@link ServiceProviderFactory} that can create different
 * implementation classes based on the Soaplab client configuration
 * properties. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: ServiceProvider.java,v 1.1 2007/06/19 21:39:26 marsenger Exp $
 */
public interface ServiceProvider {

    /**************************************************************************
     * Return an instance that represents a Soaplab List service. <p>
     *
     * @throws SoaplabException if there are problems to find or
     * create such instance
     *************************************************************************/
    AnalysisList getListService()
	throws SoaplabException;

    /**************************************************************************
     * Return an instance that represents a Soaplab Analysis service. <p>
     *
     * @throws SoaplabException if there are problems to find or
     * create such instance
     *************************************************************************/
    Analysis getService()
	throws SoaplabException;

}
