// HelloWorldJob.java
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

package org.soaplab.samples;

import org.soaplab.share.SoaplabException;
import org.soaplab.services.AbstractJob;
import org.soaplab.services.Reporter;
import org.soaplab.services.metadata.MetadataAccessor;

import java.util.Map;

/**
 * A simple plug-in job implementing the obvious. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: HelloWorldJob.java,v 1.7 2007/04/19 22:17:07 marsenger Exp $
 */

public class HelloWorldJob
    extends AbstractJob {

    /**************************************************************************
     * The main constructor.
     **************************************************************************/
    public HelloWorldJob (String jobId,
			  MetadataAccessor metadataAccessor,
			  Reporter reporter,
			  Map<String,Object> sharedAttributes,
			  boolean jobRecreated)
	throws SoaplabException {
	super (jobId, metadataAccessor, reporter,
	       sharedAttributes, jobRecreated);
    }

    /**************************************************************************
     * Really nothing much here...
     **************************************************************************/
    protected void realRun()
	throws SoaplabException {
	reporter.setResult ("output", "Hello, world!");
    }

}
