// TestingJob.java
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

package org.soaplab.services.testing;

import org.soaplab.share.SoaplabException;
import org.soaplab.share.SoaplabConstants;
import org.soaplab.services.AbstractJob;

import org.apache.commons.lang.time.DateFormatUtils;
import java.util.Date;

/**
 * A testing Job's implementation that does not do much. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: TestingJob.java,v 1.2 2007/09/23 11:18:47 marsenger Exp $
 */

public class TestingJob
    extends AbstractJob {

    private static org.apache.commons.logging.Log log =
       org.apache.commons.logging.LogFactory.getLog (TestingJob.class);

    /**************************************************************************
     * Really nothing here...
     **************************************************************************/
    protected void realRun()
	throws SoaplabException {

	log.info ("Testing job run in progress.");
	reporter.setResult ("result", "Current time is " +
			    DateFormatUtils.format (new Date(),
						    SoaplabConstants.DT_FORMAT));
    }

}
