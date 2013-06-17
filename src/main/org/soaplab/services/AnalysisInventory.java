// AnalysisInventory.java
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

import org.soaplab.services.metadata.AnalysisInstallation;

import java.util.Hashtable;
import java.util.Vector;

/**
 * An interface that has to be implemented by classes providing lists
 * of available analyses. It is an internal interface, not visible as
 * a service (for that see {@link org.soaplab.share.AnalysisList
 * AnalysisList}). <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: AnalysisInventory.java,v 1.1.1.1 2006/11/03 09:14:57 marsenger Exp $
 */

public interface AnalysisInventory {

    /**************************************************************************
     * Return a list of available analysis, with basic properties. <p>
     *
     * @return a hashtable where keys are categories, and each
     * category has a vector of its analysis; each analysis is in a
     * container with its name and few other basic characteristics
     *************************************************************************/
    Hashtable<String,Vector<AnalysisInstallation>> getList();

    /**************************************************************************
     * Return an identifier of this list. It can be used to
     * distinguish between the same named applications provided by
     * different lists (providers). <p>
     *
     * @return a reasonably uniquely idntifying name, but also
     * reasonably human-readable because it could become part of the
     * web services name
     *************************************************************************/
    String getAuthority();

}
