// HelloWorldJobFactory.java
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

package org.soaplab.samples;

import org.soaplab.share.SoaplabException;
import org.soaplab.services.Reporter;
import org.soaplab.services.metadata.MetadataAccessor;
import org.soaplab.services.JobFactory;
import org.soaplab.services.Job;

import java.util.Map;

/**
 * A factory for HelloWorldJob instances. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: HelloWorldJobFactory.java,v 1.1 2007/04/02 15:10:19 marsenger Exp $
 */

public class HelloWorldJobFactory
    implements JobFactory {

    /**************************************************************************
     * The default constructor.
     **************************************************************************/
    public HelloWorldJobFactory() {
    }

    /**************************************************************************
     * FACTORY: Get a new Job.
     **************************************************************************/
    public Job newInstance (String jobId,
			    MetadataAccessor metadataAccessor,
			    Reporter reporter,
			    Map<String,Object> sharedAttributes,
			    boolean jobRecreated)
	throws SoaplabException {
	return new HelloWorldJob (jobId, metadataAccessor, reporter,
				  sharedAttributes, jobRecreated);
    }

}
