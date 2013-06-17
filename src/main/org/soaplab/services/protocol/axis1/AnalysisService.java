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

package org.soaplab.services.protocol.axis1;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.axis.MessageContext;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.commons.lang.StringUtils;
import org.soaplab.share.SoaplabConstants;
import org.soaplab.share.SoaplabException;
import org.soaplab.share.SoaplabMap;

/**
 * This class is an Axis-1 (the old Apache Axis) Web Service
 * implementation of Soaplab services. It contains only Axis 1
 * specific stuff (such as necessary conversions between java.util.Map
 * and SoaplabMap) - the business logic is delegated to the
 * org.soaplab.services.AnalysisService class (a simple inheritance
 * would not work here because of overwriting methods different only
 * by their return types). <p>
 *
 * Note that the old Axis (Axis 1) is really not much supported by
 * Soaplab2 - it exists mainly because of the backward
 * compatibility. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: AnalysisService.java,v 1.6 2011/04/06 14:09:02 mahmutuludag Exp $
 */
public class AnalysisService
    implements Analysis1 {

    private org.apache.commons.logging.Log defaultLog =
       org.apache.commons.logging.LogFactory.getLog (AnalysisService.class);

    protected DelegateAnalysisService delegate;

    /**************************************************************************
     * The main constructor.
     *************************************************************************/
    public AnalysisService()
	throws SoaplabException {
	delegate = new DelegateAnalysisService();
    }

    //
    private long checkTime (Object value) {
	if (value == null)
	    return -1;
	try {
	    return ((Long)value).longValue();
	} catch (ClassCastException e) {
	    getLog().warn ("An unexpected type returned by getCharacteristics(): " +
			   value.getClass().getName());
	    return -1;
	}
    }

    //
    private org.apache.commons.logging.Log getLog() {
	try {
	    return org.apache.commons.logging.LogFactory.getLog
		(delegate.getServiceName().replace ('.', '_'));
	} catch (SoaplabException e) {
	    return defaultLog;
	}
    }

    /**************************************************************************
     *
     * Implementing Analysis1 interface
     *
     *************************************************************************/
    public String describe()
	throws SoaplabException {
	return delegate.describe();
    }

    public Map[] getInputSpec()
	throws SoaplabException {
	return SoaplabMap.toMaps (delegate.getInputSpec());
    }

    public Map[] getResultSpec()
	throws SoaplabException {
	return SoaplabMap.toMaps (delegate.getResultSpec());
    }

    public Map getAnalysisType()
	throws SoaplabException {
	return SoaplabMap.toMap (delegate.getAnalysisType());
    }

    @SuppressWarnings("unchecked")
    public String createJob (Map inputs)
	throws SoaplabException {
        delegate.getClientInfo(inputs);
	return delegate.createJob (SoaplabMap.fromMap (inputs));
    }

    public void run (String jobId)
	throws SoaplabException {
	delegate.run (jobId);
    }

    public void runNotifiable (String jobId, String notificationDescriptor)
	throws SoaplabException {
	delegate.runNotifiable (jobId, notificationDescriptor);
    }

    @SuppressWarnings("unchecked")
    public String createAndRun (Map inputs)
	throws SoaplabException {
        delegate.getClientInfo(inputs);
	return delegate.createAndRun (SoaplabMap.fromMap (inputs));
    }

    @SuppressWarnings("unchecked")
    public String createAndRunNotifiable (Map inputs, String notificationDescriptor)
	throws SoaplabException {
        delegate.getClientInfo(inputs);
	return delegate.createAndRunNotifiable (SoaplabMap.fromMap (inputs),
						notificationDescriptor);
    }

    public void waitFor (String jobId)
	throws SoaplabException {
	delegate.waitFor (jobId);
    }

    @SuppressWarnings("unchecked")
    public Map runAndWaitFor (Map inputs)
	throws SoaplabException {
	return SoaplabMap.toMap (delegate.runAndWaitFor (SoaplabMap.fromMap (inputs)));
    }

    public void terminate (String jobId)
	throws SoaplabException {
	delegate.terminate (jobId);
    }

    public String getLastEvent (String jobId)
	throws SoaplabException {
	return delegate.getLastEvent (jobId);
    }

    public String getNotificationDescriptor (String jobId)
	throws SoaplabException {
	return delegate.getNotificationDescriptor (jobId);
    }

    public String getStatus (String jobId)
	throws SoaplabException {
	return delegate.getStatus (jobId);
    }

    public long getCreated (String jobId)
	throws SoaplabException {
	return checkTime (getCharacteristics (jobId).get (SoaplabConstants.TIME_CREATED));
    }

    public long getStarted (String jobId)
	throws SoaplabException {
	return checkTime (getCharacteristics (jobId).get (SoaplabConstants.TIME_STARTED));
    }

    public long getEnded (String jobId)
	throws SoaplabException {
	return checkTime (getCharacteristics (jobId).get (SoaplabConstants.TIME_ENDED));
    }

    public long getElapsed (String jobId)
	throws SoaplabException {
	return checkTime (getCharacteristics (jobId).get (SoaplabConstants.TIME_ELAPSED));
    }

    public Map getCharacteristics (String jobId)
	throws SoaplabException {

	// output
	Map<String,Long> result = new HashMap<String,Long>();

	// input
	Map<String,String> fromDelegate =
	    SoaplabMap.toStringMap (delegate.getCharacteristics (jobId));

	Iterator<Map.Entry<String,String>> it = fromDelegate.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry<String,String> entry = it.next();
	    String key = entry.getKey();
	    try {
		Long value = new Long (entry.getValue());
		if (key.equals (SoaplabConstants.TIME_ELAPSED)) {
		    result.put (key, value);
		} else {
		    result.put (key, new Long (value.longValue() / 1000));
		}
	    } catch (NumberFormatException e) {
		getLog().warn ("A non-numeric value returned by getCharacteristics(): " +
			       entry.getValue());
	    }
	}
	return result;
    }

    public Map getResults (String jobId)
	throws SoaplabException {
	return SoaplabMap.toMap (delegate.getResults (jobId));
    }

    public Map getSomeResults (String jobId, String[] resultNames)
	throws SoaplabException {
	return SoaplabMap.toMap (delegate.getSomeResults (jobId, resultNames));
    }

    public void destroy (String jobId)
	throws SoaplabException {
	delegate.destroy (jobId);
    }

    /**************************************************************************
     *
     * The real worker (inheriting from the general,
     * protocol-independent AnalysisService). It is used by
     * delegation.
     *
     *************************************************************************/
    protected class DelegateAnalysisService
	extends org.soaplab.services.AnalysisService {

	/*********************************************************************
	 * The main constructor.
	 ********************************************************************/
	public DelegateAnalysisService()
	    throws SoaplabException {
	}

	/*********************************************************************
	 *
	 ********************************************************************/
	public DelegateAnalysisService (String serviceName)
	    throws SoaplabException {
	    super (serviceName);
	}

	/*********************************************************************
	 *
	 ********************************************************************/
	@Override public String getServiceName()
	    throws SoaplabException {
	    MessageContext ctx = MessageContext.getCurrentContext();
	    if (ctx == null)
		throw new SoaplabException
		    ("Internal error (in Axis-1): Undefined Message Context.");
	    String targetService = ctx.getTargetService();
	    if (StringUtils.isEmpty (targetService))
		throw new SoaplabException
		    ("Internal error (in Axis-1): Unknown target service.");
	    return targetService;
	}

	/*********************************************************************
	 *
	 ********************************************************************/
	@SuppressWarnings("unchecked")
    void getClientInfo(Map map)
	throws SoaplabException
	{
	    MessageContext msgContext = MessageContext.getCurrentContext();
	    if (msgContext == null)
	        throw new SoaplabException
	        ("Internal error (in Axis-1): Undefined Message Context.");
	    
	    HttpServletRequest httpReq = 
            (HttpServletRequest)msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
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
        
        defaultLog.debug("remote IP: "+remoteIP);
        
        map.put(SoaplabConstants.REMOTE_IP,remoteIP);
        map.put(SoaplabConstants.INTERFACE,"axis1");
        
        return;
	}

    }

}
