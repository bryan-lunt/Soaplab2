// ProtocolToolkit.java
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

package org.soaplab.services.protocol;

import org.soaplab.share.SoaplabException;

/**
 * An interface providing basic features in distributed architecture
 * environment that are dependent on the used protocol. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.org">Martin Senger</A>
 * @version $Id: ProtocolToolkit.java,v 1.3 2007/10/04 09:49:58 marsenger Exp $
 */

public interface ProtocolToolkit {

    /**************************************************************************
     * Return name of service which was called by the current request. <p>
     *
     * @throws SoaplabException if it cannot be done
     *************************************************************************/
    String getServiceName()
	throws SoaplabException;

}
