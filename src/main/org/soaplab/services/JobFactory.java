// JobFactory.java
//
// Created: April 2007
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

package org.soaplab.services;

import org.soaplab.share.SoaplabException;
import org.soaplab.services.metadata.MetadataAccessor;
import java.util.Map;

/**
 * An interface defining a factory for creating {@link Job}
 * instances. This is used to ensure that all Job classes are fed by
 * the same parameter types. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: JobFactory.java,v 1.1 2007/04/02 14:35:55 marsenger Exp $
 */

public interface JobFactory {

    /**************************************************************************
     * Get a new Job.
     **************************************************************************/
    Job newInstance (String jobId,
		     MetadataAccessor metadataAccessor,
		     Reporter reporter,
		     Map<String,Object> sharedAttributes,
		     boolean jobRecreated)
	throws SoaplabException;

}
