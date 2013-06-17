// ServiceProviderImpl.java
//
// Created: October 2007
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

package org.soaplab.clients.axis1;

import org.soaplab.clients.ServiceProvider;
import org.soaplab.clients.ServiceLocator;
import org.soaplab.share.SoaplabException;
import org.soaplab.share.SoaplabConstants;
import org.soaplab.share.SoaplabMap;
import org.soaplab.share.MapEntry;
import org.soaplab.share.Analysis;
import org.soaplab.share.AnalysisList;

import org.tulsoft.tools.soap.axis.AxisCall;
import org.tulsoft.shared.GException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.ArrayUtils;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Iterator;
import java.util.List;

/**
 * A specific implementation of the {@link ServiceProvider} interface
 * for old Apache Axis-1 toolkit, using the old RPC style
 * protocol. <p>
 *
 * This implementation allows for backward compatibility with
 * Soaplab1. Which means it can connect either to the new Soaplab2
 * services (providing that they were deployed with the Apache Axis-1
 * protocol) or to the old Soaplab1 services (throwing exceptions - or
 * empty results - if asked for a non-existing method (such as
 * getResultInfo()). <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: ServiceProviderImpl.java,v 1.3 2008/11/20 13:56:21 mahmutuludag Exp $
 */
public class ServiceProviderImpl
    implements ServiceProvider {

    private static org.apache.commons.logging.Log log =
	org.apache.commons.logging.LogFactory.getLog (ServiceProviderImpl.class);

    protected ServiceLocator locator;
    private AxisCall callForList;
    private AxisCall callForAnalysis;

    /*************************************************************************
     * A non-args constructor. Just for sub-classes.
     *************************************************************************/
    protected ServiceProviderImpl() {
    }

    /*************************************************************************
     * The main constructor. <p>
     *
     * @param locator identifies (together with properties from a
     * Soaplab client configuration file) what service should be
     * called.
     *************************************************************************/
    public ServiceProviderImpl (ServiceLocator locator) {
	this.locator = locator;
    }


    /**************************************************************************
     *
     * Methods implementing ServiceProvider interface
     *
     *************************************************************************/

    /**************************************************************************
     *
     *************************************************************************/
    public AnalysisList getListService()
	throws SoaplabException {
  	return new AnalysisListImpl();
    }

    /**************************************************************************
     *
     *************************************************************************/
    public Analysis getService()
	throws SoaplabException {
	String serviceName = locator.getServiceName();
	if (StringUtils.isEmpty (serviceName))
	    throw new SoaplabException ("No service name given (for 'axis1' protocol).");
  	return new AnalysisImpl();
    }

    /**************************************************************************
     *
     * A class implementing calls to the Soaplab1-compatible
     * AnalysisList service
     *
     *************************************************************************/
    protected class AnalysisListImpl
	implements AnalysisList {

	/********************************************************************
	 * Constructor. Create an Axis worker that will be able to
	 * contact an AnalysisList service (using the list endpoint
	 * given in the service locator).
	 ********************************************************************/
	public AnalysisListImpl()
	    throws SoaplabException {

	    // create workers for Axis calls
	    try {
		String endpoint = locator.getListEndpoint();
		log.info ("Axis-1 LIST client uses " + endpoint);
		callForList = new AxisCall (new URL (endpoint));

	    } catch (MalformedURLException e) {
		throw new SoaplabException (e.getMessage());
	    } catch (GException e) {
		throw new SoaplabException (e.getMessage());
	    }
	}

	/********************************************************************
	 *
	 ********************************************************************/
	public String[] getAvailableCategories() {
	    try {
		return (String[])callForList.doCall ("getAvailableCategories",
						     new Object[] {});
	    } catch (Throwable e) {
		log.error (new SoaplabException (e.getMessage(), e));
		return ArrayUtils.EMPTY_STRING_ARRAY;
	    }
	}

	public String[] getAvailableAnalyses() {
	    try {
		return (String[])callForList.doCall ("getAvailableAnalyses",
						     new Object[] {});
	    } catch (Throwable e) {
		log.error (new SoaplabException (e.getMessage(), e));
		return ArrayUtils.EMPTY_STRING_ARRAY;
	    }
	}

	public SoaplabMap getAvailableAnalysesWithDescription() {
	    log.warn ("Method getAvailableAnalysesWithDescription() is not provided by Axis1 protocol.");
	    return new SoaplabMap();
	}
	
	public String[] getAvailableAnalysesInCategory (String categoryName) {
	    try {
		return (String[])callForList.doCall ("getAvailableAnalysesInCategory",
						     new Object[] { categoryName });
	    } catch (Throwable e) {
		log.error (new SoaplabException (e.getMessage(), e));
		return ArrayUtils.EMPTY_STRING_ARRAY;
	    }
	}

	public String getServiceLocation (String analysisName) {
	    try {
		return (String)callForList.doCall ("getServiceLocation",
						     new Object[] { analysisName });
	    } catch (Throwable e) {
		log.error (new SoaplabException (e.getMessage(), e));
		return "";
	    }
	}
    }

    /**************************************************************************
     *
     * A class implementing calls to the Soaplab1-compatible
     * Analysis service
     *
     *************************************************************************/
    protected class AnalysisImpl
	implements Analysis {

	/********************************************************************
	 * Constructor. Create an Axis worker that will be able to
	 * contact an Analysis service (using the endpoint given
	 * in the service locator).
	 ********************************************************************/
	public AnalysisImpl()
	    throws SoaplabException {

	    // create workers for Axis calls
	    try {
		String endpoint = locator.getServiceEndpoint();
		log.info ("Axis-1 SERVICE client uses " + endpoint);
		callForAnalysis = new AxisCall (new URL (endpoint));
		callForAnalysis.disableTimeout();

	    } catch (MalformedURLException e) {
		throw new SoaplabException (e.getMessage());
	    } catch (GException e) {
		throw new SoaplabException (e.getMessage());
	    }
	}

	/********************************************************************
	 *
	 ********************************************************************/
	protected Object doCall (String method, Object[] parameters)
	    throws SoaplabException {
	    try {
	        synchronized (callForAnalysis) {
	            return callForAnalysis.doCall (method, parameters);
	        }
	    } catch (Throwable e) {
		throw new SoaplabException (e.getMessage(), e);
	    }
	}

	/********************************************************************
	 *
	 ********************************************************************/
	public String describe()
	    throws SoaplabException {
	    return (String)doCall ("describe",
				   new Object[] {});
	}

	@SuppressWarnings("unchecked")
	public SoaplabMap[] getInputSpec()
	    throws SoaplabException {
	    Map[] result = (Map[])doCall ("getInputSpec",
					  new Object[] {});
	    return SoaplabMap.fromMaps (result);
	}

	@SuppressWarnings("unchecked")
	public SoaplabMap[] getResultSpec()
	    throws SoaplabException {
	    Map[] result = (Map[])doCall ("getResultSpec",
					  new Object[] {});
	    return SoaplabMap.fromMaps (result);
	}
	
	@SuppressWarnings("unchecked")
	public SoaplabMap getAnalysisType()
	    throws SoaplabException {
	    Map result = (Map)doCall ("getAnalysisType",
				      new Object[] {});
	    return SoaplabMap.fromMap (result);
	}
	
	public String createJob (SoaplabMap inputs)
	    throws SoaplabException {
	    try {
		return (String)doCall ("createJob",
				       new Object[] { SoaplabMap.toMap (inputs) });
	    } catch (IllegalArgumentException e) {
		throw new SoaplabException (e.getMessage());
	    }
	}
	
	public void run (String jobId)
	    throws SoaplabException {
	    doCall ("run",
		    new Object[] { jobId });
	}
	
	public void runNotifiable (String jobId, String notificationDescriptor)
	    throws SoaplabException {
	    doCall ("runNotifiable",
		    new Object[] { jobId, notificationDescriptor });
	}
	
	public String createAndRun (SoaplabMap inputs)
	    throws SoaplabException {
	    try {
		return (String)doCall ("createAndRun",
				       new Object[] { SoaplabMap.toMap (inputs) });
	    } catch (IllegalArgumentException e) {
		throw new SoaplabException (e.getMessage());
	    }
	}
	
	public String createAndRunNotifiable (SoaplabMap inputs,
					      String notificationDescriptor)
	    throws SoaplabException {
	    try {
		return (String)doCall ("createAndRunNotifiable",
				       new Object[] {
					   SoaplabMap.toMap (inputs),
					   notificationDescriptor });
	    } catch (IllegalArgumentException e) {
		throw new SoaplabException (e.getMessage());
	    }
	}
	
	public void waitFor (String jobId)
	    throws SoaplabException {
	    doCall ("waitFor",
		    new Object[] { jobId });
	}
	
	@SuppressWarnings("unchecked")
	public SoaplabMap runAndWaitFor (SoaplabMap inputs)
	    throws SoaplabException {
	    try {
		Map result = (Map)doCall ("runAndWaitFor",
					  new Object[] { SoaplabMap.toMap (inputs) });
		return SoaplabMap.fromMap (result);
	    } catch (IllegalArgumentException e) {
		throw new SoaplabException (e.getMessage());
	    }
	}
	
	public void terminate (String jobId)
	    throws SoaplabException {
	    doCall ("terminate",
		    new Object[] { jobId });
	}
	
	public String getStatus (String jobId)
	    throws SoaplabException {
	    return (String)doCall ("getStatus",
				   new Object[] { jobId });
	}
	
	@SuppressWarnings("unchecked")
	public SoaplabMap getCharacteristics (String jobId)
	    throws SoaplabException {
	    Map<String,Long> result = (Map<String,Long>)doCall ("getCharacteristics",
								new Object[] { jobId });
	    // output
	    SoaplabMap sm = new SoaplabMap();
	    List<MapEntry> smEntries = sm.getEntries();

	    // input
	    Iterator<Map.Entry<String,Long>> it = result.entrySet().iterator();
	    while (it.hasNext()) {
		Map.Entry<String,Long> entry = it.next();
		String key = entry.getKey();
		if (key.equals (SoaplabConstants.TIME_ELAPSED)) {
		    smEntries.add (new MapEntry (key, entry.getValue()+""));
		} else {
		    smEntries.add (new MapEntry (key,
						 ""+ (entry.getValue() * 1000)));
		}
	    }
	    return sm;
	}
	
	public String getLastEvent (String jobId)
	    throws SoaplabException {
	    return (String)doCall ("getLastEvent",
				   new Object[] { jobId });
	}
	
	public String getNotificationDescriptor (String jobId)
	    throws SoaplabException {
	    return (String)doCall ("getNotificationDescriptor",
				   new Object[] { jobId });
	}
	
	@SuppressWarnings("unchecked")
	public SoaplabMap getResults (String jobId)
	    throws SoaplabException {
	    Map result = (Map)doCall ("getResults",
				      new Object[] { jobId });
	    return SoaplabMap.fromMap (result);
	}
	
	@SuppressWarnings("unchecked")
	public SoaplabMap getSomeResults (String jobId, String[] resultNames)
	    throws SoaplabException {
	    Map result = (Map)doCall ("getSomeResults",
				      new Object[] { jobId, resultNames });
	    return SoaplabMap.fromMap (result);
	}
	
	public SoaplabMap[] getResultsInfo (String jobId)
	    throws SoaplabException {
	    throw new SoaplabException ("Method getResultsInfo() is not provided by Axis1 protocol.");
	}
	
	public void destroy (String jobId)
	    throws SoaplabException {
	    doCall ("destroy",
		    new Object[] { jobId });
	}
	
    }
}

