// DefaultMetadataAccessorFactory.java
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

/**
 * A default implementation of the {@link MetadataAccessorFactory}
 * interface. It creates metadata accessors for individual services
 * that read metadata from XML files. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: DefaultMetadataAccessorFactory.java,v 1.1.1.1 2006/11/03 09:14:59 marsenger Exp $
 */
public class DefaultMetadataAccessorFactory
    implements MetadataAccessorFactory {

    /******************************************************************************
     *
     ******************************************************************************/
    public MetadataAccessor getInstance (String serviceName)
	throws SoaplabException {
	return new MetadataAccessorXML (serviceName);
    }

}
