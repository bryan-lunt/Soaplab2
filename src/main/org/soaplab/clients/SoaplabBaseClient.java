// SoaplabBaseClient.java
//
// Created: June 2007
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

package org.soaplab.clients;

import org.soaplab.share.SoaplabException;
import org.soaplab.share.Analysis;
import org.soaplab.share.SoaplabMap;
import org.soaplab.share.AnalysisList;

import org.apache.commons.lang.StringUtils;

/**
 * A general client that can call all methods of all Soaplab
 * services (both list services and analysis services). <p>
 *
 * It uses a given Service locator and a client configuration file to
 * decide how to access Soaplab services. The services can be accessed
 * as Web Services (using perhaps several different protocols/styles),
 * or by loading just local classes. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: SoaplabBaseClient.java,v 1.8 2007/11/21 14:50:43 mahmutuludag Exp $
 */
public class SoaplabBaseClient
    implements Analysis, AnalysisList {

//     private org.apache.commons.logging.Log log =
//        org.apache.commons.logging.LogFactory.getLog (SoaplabBaseClient.class);

    Analysis service;
    AnalysisList listService;
    ServiceLocator locator;

    /*************************************************************************
     * Constructor for sub-classes.
     *************************************************************************/
    protected SoaplabBaseClient() {
    }

    /*************************************************************************
     * The main constructor. <p>
     *
     * @param locator identifies (together with properties from a
     * Soaplab client configuration file) what service should be
     * called (and which protocol to use for it)
     *************************************************************************/
    public SoaplabBaseClient (ServiceLocator locator)
	throws SoaplabException {
	setLocator (locator);
    }

    /**************************************************************************
     *
     *************************************************************************/
    protected void init()
	throws SoaplabException {
	ServiceProvider provider =
	    new ServiceProviderFactory (locator)
	    .getServiceProvider();
	listService = provider.getListService();
	if (StringUtils.isNotEmpty (locator.getServiceName()))
	    service = provider.getService();
    }

    /**************************************************************************
     * Throw an exception if the 'service' is not defined. We do not
     * throw it in init() because in that time we do not know if the
     * 'service' will be used all all (it may be that only list
     * service methods will be used).
     *************************************************************************/
    protected void checkIfServiceDefined()
	throws SoaplabException {
	if (service == null)
	    throw new SoaplabException
		("No service name given. Sorry, I cannot do much without it...");
    }

    /**************************************************************************
     *
     *************************************************************************/
    public ServiceLocator getLocator() {
	return locator;
    }

    /**************************************************************************
     *
     *************************************************************************/
    public void setLocator (ServiceLocator locator) throws SoaplabException{
	this.locator = locator;
	init();
    }

    /**************************************************************************
     * Delegated methods implementing AnalysisList interface
     *************************************************************************/

    public String[] getAvailableCategories() {
	return listService.getAvailableCategories();
    }

    public String[] getAvailableAnalyses() {
	return listService.getAvailableAnalyses();
    }

    public SoaplabMap getAvailableAnalysesWithDescription() {
	return listService.getAvailableAnalysesWithDescription();
    }

    public String[] getAvailableAnalysesInCategory (String categoryName) {
	return listService.getAvailableAnalysesInCategory (categoryName);
    }

    public String getServiceLocation (String analysisName) {
	return listService.getServiceLocation (analysisName);
    }

    /**************************************************************************
     * Delegated methods implementing Analysis interface
     *************************************************************************/

    public String describe()
	throws SoaplabException {
	checkIfServiceDefined();
	return service.describe();
    }

    public SoaplabMap[] getInputSpec()
	throws SoaplabException {
	checkIfServiceDefined();
	return service.getInputSpec();
    }

    public SoaplabMap[] getResultSpec()
	throws SoaplabException {
	checkIfServiceDefined();
	return service.getResultSpec();
    }

    public SoaplabMap getAnalysisType()
	throws SoaplabException {
	checkIfServiceDefined();
	return service.getAnalysisType();
    }

    public String createJob (SoaplabMap inputs)
	throws SoaplabException {
	checkIfServiceDefined();
	return service.createJob (inputs);
    }

    public void run (String jobId)
	throws SoaplabException {
	checkIfServiceDefined();
	service.run (jobId);
    }

    public void runNotifiable (String jobId, String notificationDescriptor)
 	throws SoaplabException {
	checkIfServiceDefined();
	service.runNotifiable (jobId, notificationDescriptor);
    }

    public String createAndRun (SoaplabMap inputs)
	throws SoaplabException {
	checkIfServiceDefined();
	return service.createAndRun (inputs);
    }

    public String createAndRunNotifiable (SoaplabMap inputs,
					  String notificationDescriptor)
	throws SoaplabException {
	checkIfServiceDefined();
	return service.createAndRunNotifiable (inputs, notificationDescriptor);
    }

    public void waitFor (String jobId)
	throws SoaplabException {
	checkIfServiceDefined();
	service.waitFor (jobId);
    }

    public SoaplabMap runAndWaitFor (SoaplabMap inputs)
	throws SoaplabException {
	return service.runAndWaitFor (inputs);
    }

    public void terminate (String jobId)
	throws SoaplabException {
	checkIfServiceDefined();
	service.terminate (jobId);
    }

    public String getStatus (String jobId)
	throws SoaplabException {
	checkIfServiceDefined();
	return service.getStatus (jobId);
    }

    public SoaplabMap getCharacteristics (String jobId)
	throws SoaplabException {
	checkIfServiceDefined();
	return service.getCharacteristics (jobId);
    }

    public String getLastEvent (String jobId)
	throws SoaplabException {
	checkIfServiceDefined();
	return service.getLastEvent (jobId);
    }

     public String getNotificationDescriptor (String jobId)
 	throws SoaplabException {
	checkIfServiceDefined();
 	return service.getNotificationDescriptor (jobId);
     }

    public SoaplabMap getResults (String jobId)
	throws SoaplabException {
	checkIfServiceDefined();
	return service.getResults (jobId);
    }

    public SoaplabMap getSomeResults (String jobId, String[] resultNames)
	throws SoaplabException {
	checkIfServiceDefined();
	return service.getSomeResults (jobId, resultNames);
    }

    public SoaplabMap[] getResultsInfo (String jobId)
	throws SoaplabException {
	checkIfServiceDefined();
	return service.getResultsInfo (jobId);
    }

    public void destroy (String jobId)
	throws SoaplabException {
	checkIfServiceDefined();
	service.destroy (jobId);
    }

}
