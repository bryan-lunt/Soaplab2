// AnalysisService.java
//
// Created: September 2007
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

package org.soaplab.share.jaxws;

import org.soaplab.share.SoaplabException;
import org.soaplab.share.SoaplabConstants;
import org.soaplab.share.SoaplabMap;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * A JAX-WS specific flavour of a general interface to a Soaplab
 * analysis service. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: Analysis.java,v 1.1 2007/09/22 13:44:50 marsenger Exp $
 */
@WebService (targetNamespace = SoaplabConstants.SOAPLAB_NAMESPACE)
public interface Analysis
    extends org.soaplab.share.Analysis {

    @WebMethod
	@RequestWrapper  (className = "org.soaplab.share.jaxws.Describe")
	@ResponseWrapper (className = "org.soaplab.share.jaxws.DescribeResponse")
	String describe()
	throws SoaplabException;

    @WebMethod
	@RequestWrapper  (className = "org.soaplab.share.jaxws.GetInputSpec")
	@ResponseWrapper (className = "org.soaplab.share.jaxws.GetInputSpecResponse")
	SoaplabMap[] getInputSpec()
	throws SoaplabException;

    @WebMethod
	@RequestWrapper  (className = "org.soaplab.share.jaxws.GetResultSpec")
	@ResponseWrapper (className = "org.soaplab.share.jaxws.GetResultSpecResponse")
	SoaplabMap[] getResultSpec()
	throws SoaplabException;

    @WebMethod
	@RequestWrapper  (className = "org.soaplab.share.jaxws.GetAnalysisType")
	@ResponseWrapper (className = "org.soaplab.share.jaxws.GetAnalysisTypeResponse")
	SoaplabMap getAnalysisType()
	throws SoaplabException;

    @WebMethod
	@RequestWrapper  (className = "org.soaplab.share.jaxws.CreateJob")
	@ResponseWrapper (className = "org.soaplab.share.jaxws.CreateJobResponse")
	String createJob (SoaplabMap inputs)
	throws SoaplabException;

    @WebMethod
	@RequestWrapper  (className = "org.soaplab.share.jaxws.Run")
	@ResponseWrapper (className = "org.soaplab.share.jaxws.RunResponse")
	void run (String jobID)
	throws SoaplabException;

    @WebMethod
	@RequestWrapper  (className = "org.soaplab.share.jaxws.RunNotifiable")
	@ResponseWrapper (className = "org.soaplab.share.jaxws.RunNotifiableResponse")
	void runNotifiable (String jobID, String notificationDescriptor)
	throws SoaplabException;

    @WebMethod
	@RequestWrapper  (className = "org.soaplab.share.jaxws.CreateAndRun")
	@ResponseWrapper (className = "org.soaplab.share.jaxws.CreateAndRunResponse")
	String createAndRun (SoaplabMap inputs)
	throws SoaplabException;

    @WebMethod
	@RequestWrapper  (className = "org.soaplab.share.jaxws.CreateAndRunNotifiable")
	@ResponseWrapper (className = "org.soaplab.share.jaxws.CreateAndRunNotifiableResponse")
	String createAndRunNotifiable (SoaplabMap inputs,
				       String notificationDescriptor)
	throws SoaplabException;

    @WebMethod
	@RequestWrapper  (className = "org.soaplab.share.jaxws.WaitFor")
	@ResponseWrapper (className = "org.soaplab.share.jaxws.WaitForResponse")
	void waitFor (String jobID)
	throws SoaplabException;

    @WebMethod
	@RequestWrapper  (className = "org.soaplab.share.jaxws.RunAndWaitFor")
	@ResponseWrapper (className = "org.soaplab.share.jaxws.RunAndWaitForResponse")
	SoaplabMap runAndWaitFor (SoaplabMap inputs)
	throws SoaplabException;

    @WebMethod
	@RequestWrapper  (className = "org.soaplab.share.jaxws.Terminate")
	@ResponseWrapper (className = "org.soaplab.share.jaxws.TerminateResponse")
	void terminate (String jobID)
	throws SoaplabException;

    @WebMethod
	@RequestWrapper  (className = "org.soaplab.share.jaxws.GetLastEvent")
	@ResponseWrapper (className = "org.soaplab.share.jaxws.GetLastEventResponse")
	String getLastEvent (String jobID)
	throws SoaplabException;

    @WebMethod
	@RequestWrapper  (className = "org.soaplab.share.jaxws.getNotificationDescriptor")
	@ResponseWrapper (className = "org.soaplab.share.jaxws.getNotificationDescriptorResponse")
	String getNotificationDescriptor (String jobID)
	throws SoaplabException;

    @WebMethod
	@RequestWrapper  (className = "org.soaplab.share.jaxws.GetStatus")
	@ResponseWrapper (className = "org.soaplab.share.jaxws.GetStatusResponse")
	String getStatus (String jobID)
	throws SoaplabException;

    @WebMethod
	@RequestWrapper  (className = "org.soaplab.share.jaxws.GetCharacteristics")
	@ResponseWrapper (className = "org.soaplab.share.jaxws.GetCharacteristicsResponse")
	SoaplabMap getCharacteristics (String jobID)
	throws SoaplabException;

    @WebMethod
	@RequestWrapper  (className = "org.soaplab.share.jaxws.GetResults")
	@ResponseWrapper (className = "org.soaplab.share.jaxws.GetResultsResponse")
	SoaplabMap getResults (String jobID)
	throws SoaplabException;

    @WebMethod
	@RequestWrapper  (className = "org.soaplab.share.jaxws.GetSomeResults")
	@ResponseWrapper (className = "org.soaplab.share.jaxws.GetSomeResultsResponse")
	SoaplabMap getSomeResults (String jobID, String[] resultNames)
	throws SoaplabException;

    @WebMethod
	@RequestWrapper  (className = "org.soaplab.share.jaxws.GetResultsInfo")
	@ResponseWrapper (className = "org.soaplab.share.jaxws.GetResultsInfoResponse")
	SoaplabMap[] getResultsInfo (String jobID)
	throws SoaplabException;

    @WebMethod
	@RequestWrapper  (className = "org.soaplab.share.jaxws.Destroy")
	@ResponseWrapper (className = "org.soaplab.share.jaxws.DestroyResponse")
	void destroy (String jobID)
	throws SoaplabException;

}
