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

package org.soaplab.services.protocol.jaxws;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.lang.StringUtils;
import org.soaplab.share.SoaplabConstants;
import org.soaplab.share.SoaplabException;
import org.soaplab.share.SoaplabMap;

/**
 * This class is a JAX-WS Web Service implementation of Soaplab
 * services. It contains only 'Java Web Services' specific stuff - the
 * business logic is in the super-class. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: AnalysisService.java,v 1.3 2011/04/06 13:24:05 mahmutuludag Exp $
 */
@WebService
    (name            = "AnalysisService",
     serviceName     = SoaplabConstants.SERVICE_NAME,
     targetNamespace = SoaplabConstants.SOAPLAB_NAMESPACE,
     portName        = SoaplabConstants.SERVICE_PORT_NAME)
    public class AnalysisService
    extends org.soaplab.services.AnalysisService {

	/**************************************************************************
	 * The main constructor.
	 *************************************************************************/
	public AnalysisService()
	    throws SoaplabException {
	}

	/**************************************************************************
	 *
	 *************************************************************************/
	public AnalysisService (String serviceName)
	    throws SoaplabException {
	    super (serviceName);
	}

	// ------------------------------------------------------------------------
	//
	//  Methods implementing ProtocolToolkit interface
	//
	// ------------------------------------------------------------------------

	/**************************************************************************
	 * Return a name under which this service was called. This
	 * name is "hidden" in the URL invoking this web service. <p>
	 *
	 * @throw SoaplabException if the service name cannot be
	 * obtained; or if the web service context is not known. The
	 * latter may happen if this class is not used within a
	 * servlet container (such as Tomcat) properly - because it is
	 * the conainer who injects code into otherwise empty web
	 * service context instance.
	 *
	 *************************************************************************/
	@WebMethod(exclude=true)
	public String getServiceName()
	    throws SoaplabException {

	    // do we have a Web Service Context?
	    if (wsc == null)
		throw new SoaplabException ("Internal error: Undefined Web Service Context.");

	    MessageContext mcx = null;
	    try {
		mcx = wsc.getMessageContext();
		if (mcx == null)
		    throw new SoaplabException ("Internal error: Undefined Message Context.");
	    } catch (java.lang.IllegalStateException e) {
		throw new SoaplabException
		    ("Internal error: The method is called while no request is being serviced");
	    }
	    HttpServletRequest req =
		(HttpServletRequest)mcx.get (MessageContext.SERVLET_REQUEST);
	    if (req == null)
		throw new SoaplabException ("Internal error: Undefined HttpServletRequest.");
	    String requestURI = req.getRequestURI();
	    String serviceName = StringUtils.substringAfterLast (requestURI, "/");
	    if (StringUtils.isBlank (serviceName)) {
		if (StringUtils.isNotBlank (requestURI)) {
		    // request URI does not have any slash (is it possible?)
		    return requestURI;
		} else {
		    throw new SoaplabException
			("Internal error: Cannot recognize what service was called.");
		}
	    }
	    return serviceName;
	}

	// this is filled by an "injection" (by Tomcat when a request comes)
	@Resource private WebServiceContext wsc;

	
	@WebMethod(exclude=true)
	public void getClientInfo(SoaplabMap map)
	throws SoaplabException
	{

	    // do we have a Web Service Context?
	    if (wsc == null)
	        throw new SoaplabException ("Internal error: Undefined Web Service Context.");

	    MessageContext mcx = null;
	    
	    try {
	        mcx = wsc.getMessageContext();
	        if (mcx == null)
	            throw new SoaplabException ("Internal error: Undefined Message Context.");
	    } catch (java.lang.IllegalStateException e) {
	        throw new SoaplabException
	        ("Internal error: The method is called while no request is being serviced");
	    }
	    HttpServletRequest httpReq =
	        (HttpServletRequest)mcx.get (MessageContext.SERVLET_REQUEST);
	    if (httpReq == null)
	        throw new SoaplabException ("Internal error: Undefined HttpServletRequest.");


	    // Client IP address...
	    String remoteIP = null;
	    if(httpReq.getRemoteAddr() != null)
	        remoteIP = httpReq.getRemoteAddr();
	    // Handle proxy or load balancer client IP.
	    if(httpReq.getHeader("X-Cluster-Client-Ip") != null) {
	        remoteIP =  httpReq.getHeader("X-Cluster-Client-Ip");
	    }
	    // Default value in case of problems
	    if(remoteIP == null)
	        remoteIP = "unknown";

	    map.put(SoaplabConstants.REMOTE_IP, remoteIP);
	    map.put(SoaplabConstants.INTERFACE, "jaxws");

	    return;
	}

	// ------------------------------------------------------------------------
	//
	//  Methods implementing Analysis interface
	//
	// ------------------------------------------------------------------------

	/**************************************************************************
	 *
	 *************************************************************************/
	@WebMethod
	    @RequestWrapper  (className = "org.soaplab.share.jaxws.Describe")
	    @ResponseWrapper (className = "org.soaplab.share.jaxws.DescribeResponse")
	    @Override public String describe()
	    throws SoaplabException {
	    return super.describe();
	}

	/**************************************************************************
	 *
	 *************************************************************************/
	@WebMethod
	    @RequestWrapper  (className = "org.soaplab.share.jaxws.GetInputSpec")
	    @ResponseWrapper (className = "org.soaplab.share.jaxws.GetInputSpecResponse")
	    @Override public SoaplabMap[] getInputSpec()
	    throws SoaplabException {
	    return super.getInputSpec();
	}

	/**************************************************************************
	 *
	 *************************************************************************/
    	@WebMethod
	    @RequestWrapper  (className = "org.soaplab.share.jaxws.GetResultSpec")
	    @ResponseWrapper (className = "org.soaplab.share.jaxws.GetResultSpecResponse")
	    @Override public SoaplabMap[] getResultSpec()
	    throws SoaplabException {
	    return super.getResultSpec();
	}

	/**************************************************************************
	 *
	 *************************************************************************/
    	@WebMethod
	    @RequestWrapper  (className = "org.soaplab.share.jaxws.GetAnalysisType")
	    @ResponseWrapper (className = "org.soaplab.share.jaxws.GetAnalysisTypeResponse")
	    @Override public SoaplabMap getAnalysisType()
	    throws SoaplabException {
	    return super.getAnalysisType();
	}


	// 
	// Methods controlling an execution of this analysis
	//

	/**************************************************************************
	 *
	 *************************************************************************/
    	@WebMethod
	    @RequestWrapper  (className = "org.soaplab.share.jaxws.CreateJob")
	    @ResponseWrapper (className = "org.soaplab.share.jaxws.CreateJobResponse")
	    @Override public String createJob (SoaplabMap inputs)
	    throws SoaplabException {
    	    getClientInfo(inputs);
	    return super.createJob (inputs);
	}

	/**************************************************************************
	 *
	 *************************************************************************/
    	@WebMethod
	    @RequestWrapper  (className = "org.soaplab.share.jaxws.Run")
	    @ResponseWrapper (className = "org.soaplab.share.jaxws.RunResponse")
	    @Override public void run (String jobID)
	    throws SoaplabException {
	    super.run (jobID);
	}

	/**************************************************************************
	 *
	 *************************************************************************/
    	@WebMethod
	    @RequestWrapper  (className = "org.soaplab.share.jaxws.RunNotifiable")
	    @ResponseWrapper (className = "org.soaplab.share.jaxws.RunNotifiableResponse")
	    @Override public void runNotifiable (String jobID, String notificationDescriptor)
	    throws SoaplabException {
	    super.runNotifiable (jobID, notificationDescriptor);
	}

	/**************************************************************************
	 *
	 *************************************************************************/
    	@WebMethod
	    @RequestWrapper  (className = "org.soaplab.share.jaxws.CreateAndRun")
	    @ResponseWrapper (className = "org.soaplab.share.jaxws.CreateAndRunResponse")
	    @Override public String createAndRun (SoaplabMap inputs)
	    throws SoaplabException {
    	    getClientInfo(inputs);
	    return super.createAndRun (inputs);
	}

	/**************************************************************************
	 *
	 *************************************************************************/
    	@WebMethod
	    @RequestWrapper  (className = "org.soaplab.share.jaxws.CreateAndRunNotifiable")
	    @ResponseWrapper (className = "org.soaplab.share.jaxws.CreateAndRunNotifiableResponse")
	    @Override public String createAndRunNotifiable (SoaplabMap inputs,
							    String notificationDescriptor)
	    throws SoaplabException {
	    return super.createAndRunNotifiable (inputs, notificationDescriptor);
	}

	/**************************************************************************
	 *
	 *************************************************************************/
    	@WebMethod
	    @RequestWrapper  (className = "org.soaplab.share.jaxws.WaitFor")
	    @ResponseWrapper (className = "org.soaplab.share.jaxws.WaitForResponse")
	    @Override public void waitFor (String jobID)
	    throws SoaplabException {
	    super.waitFor (jobID);
	}

	/**************************************************************************
	 *
	 *************************************************************************/
    	@WebMethod
	    @RequestWrapper  (className = "org.soaplab.share.jaxws.RunAndWaitFor")
	    @ResponseWrapper (className = "org.soaplab.share.jaxws.RunAndWaitForResponse")
	    @Override public SoaplabMap runAndWaitFor (SoaplabMap inputs)
	    throws SoaplabException {
	    return super.runAndWaitFor (inputs);
	}

	/**************************************************************************
	 *
	 *************************************************************************/
    	@WebMethod
	    @RequestWrapper  (className = "org.soaplab.share.jaxws.Terminate")
	    @ResponseWrapper (className = "org.soaplab.share.jaxws.TerminateResponse")
	    @Override public void terminate (String jobID)
	    throws SoaplabException {
	    super.terminate (jobID);
	}

	// 
	// Methods giving analysis status and results
	//

	/**************************************************************************
	 *
	 *************************************************************************/
    	@WebMethod
	    @RequestWrapper  (className = "org.soaplab.share.jaxws.GetLastEvent")
	    @ResponseWrapper (className = "org.soaplab.share.jaxws.GetLastEventResponse")
	    @Override public String getLastEvent (String jobID)
	    throws SoaplabException {
	    return super.getLastEvent (jobID);
	}

	/**************************************************************************
	 *
	 *************************************************************************/
    	@WebMethod
	    @RequestWrapper  (className = "org.soaplab.share.jaxws.getNotificationDescriptor")
	    @ResponseWrapper (className = "org.soaplab.share.jaxws.getNotificationDescriptorResponse")
	    @Override public String getNotificationDescriptor (String jobID)
	    throws SoaplabException {
	    return super.getNotificationDescriptor (jobID);
	}

	/**************************************************************************
	 *
	 *************************************************************************/
    	@WebMethod
	    @RequestWrapper  (className = "org.soaplab.share.jaxws.GetStatus")
	    @ResponseWrapper (className = "org.soaplab.share.jaxws.GetStatusResponse")
	    @Override public String getStatus (String jobID)
	    throws SoaplabException {
	    return super.getStatus (jobID);
	}

	/**************************************************************************
	 *
	 *************************************************************************/
    	@WebMethod
	    @RequestWrapper  (className = "org.soaplab.share.jaxws.GetCharacteristics")
	    @ResponseWrapper (className = "org.soaplab.share.jaxws.GetCharacteristicsResponse")
	    @Override public SoaplabMap getCharacteristics (String jobID)
	    throws SoaplabException {
	    return super.getCharacteristics (jobID);
	}

	/**************************************************************************
	 *
	 *************************************************************************/
    	@WebMethod
	    @RequestWrapper  (className = "org.soaplab.share.jaxws.GetResults")
	    @ResponseWrapper (className = "org.soaplab.share.jaxws.GetResultsResponse")
	    @Override public SoaplabMap getResults (String jobID)
	    throws SoaplabException {
	    return super.getResults (jobID);
	}

	/**************************************************************************
	 *
	 *************************************************************************/
    	@WebMethod
	    @RequestWrapper  (className = "org.soaplab.share.jaxws.GetSomeResults")
	    @ResponseWrapper (className = "org.soaplab.share.jaxws.GetSomeResultsResponse")
	    @Override public SoaplabMap getSomeResults (String jobID, String[] resultNames)
	    throws SoaplabException {
	    return super.getSomeResults (jobID, resultNames);
	}

	/**************************************************************************
	 *
	 *************************************************************************/
    	@WebMethod
	    @RequestWrapper  (className = "org.soaplab.share.jaxws.GetResultsInfo")
	    @ResponseWrapper (className = "org.soaplab.share.jaxws.GetResultsInfoResponse")
	    @Override public SoaplabMap[] getResultsInfo (String jobID)
	    throws SoaplabException {
	    return super.getResultsInfo (jobID);
	}


	//
	// Methods for releasing resources
	//

	/**************************************************************************
	 *
	 *************************************************************************/
    	@WebMethod
	    @RequestWrapper  (className = "org.soaplab.share.jaxws.Destroy")
	    @ResponseWrapper (className = "org.soaplab.share.jaxws.DestroyResponse")
	    @Override public void destroy (String jobID)
	    throws SoaplabException {
	    super.destroy (jobID);
	}

}
