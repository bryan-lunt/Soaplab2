// Analysis1.java
//
// Re-created: October 2007
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

package org.soaplab.services.protocol.axis1;

import org.soaplab.share.SoaplabException;

import java.util.Map;


/**
 * An old interface to Analysis service, as it was used in
 * Soaplab1. It is here for backward compatible implementtaion using
 * the old Apache Axis-1 RPC protocol). <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: Analysis1.java,v 1.2 2007/10/06 18:32:14 marsenger Exp $
 */

public interface Analysis1 {

    String describe()
	throws SoaplabException;

    Map[] getInputSpec()
	throws SoaplabException;

    Map[] getResultSpec()
	throws SoaplabException;

    Map getAnalysisType()
	throws SoaplabException;

    String createJob (Map inputs)
	throws SoaplabException;

    void run (String jobId)
	throws SoaplabException;

    void runNotifiable (String jobId, String notificationDescriptor)
	throws SoaplabException;

    String createAndRun (Map inputs)
	throws SoaplabException;

    String createAndRunNotifiable (Map inputs, String notificationDescriptor)
	throws SoaplabException;

    void waitFor (String jobId)
	throws SoaplabException;

    Map runAndWaitFor (Map inputs)
	throws SoaplabException;

    void terminate (String jobId)
	throws SoaplabException;

    String getLastEvent (String jobId)
	throws SoaplabException;

    String getNotificationDescriptor (String jobId)
	throws SoaplabException;

    String getStatus (String jobId)
	throws SoaplabException;

    long getCreated (String jobId)
	throws SoaplabException;

    long getStarted (String jobId)
	throws SoaplabException;

    long getEnded (String jobId)
	throws SoaplabException;

    long getElapsed (String jobId)
	throws SoaplabException;

    Map getCharacteristics (String jobId)
	throws SoaplabException;

    Map getResults (String jobId)
	throws SoaplabException;

    Map getSomeResults (String jobId, String[] resultNames)
	throws SoaplabException;

    void destroy (String jobId)
	throws SoaplabException;

}
