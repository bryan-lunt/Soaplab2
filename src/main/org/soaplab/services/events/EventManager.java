// EventManager.java
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

package org.soaplab.services.events;

import org.soaplab.services.GenUtils;

import java.util.Vector;
import java.util.Enumeration;

/**
 * An inter-mediator between individual jobs and all event
 * listeners. There is usually just one instance of this class for a
 * particular service - and it is shared by all service's jobs. <p>
 *
 * When created, it first finds all available event listeners (using
 * the SPI mechanism). Then, it passes all events received from
 * service invocations ("jobs") to all of them. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: EventManager.java,v 1.2 2006/11/19 02:14:30 marsenger Exp $
 */

public class EventManager
    implements EventListener {

    private static org.apache.commons.logging.Log log =
       org.apache.commons.logging.LogFactory.getLog (EventManager.class);

    // available event listeners
    protected static EventListener[] eventListeners;

    /**************************************************************************
     *
     *************************************************************************/
    public EventManager() {

	// loads all classes interested in event listening
	if (eventListeners == null) {
	    Enumeration<EventListener> spe =
		GenUtils.spiProviders (EventListener.class);
	    Vector<EventListener> v = new Vector<EventListener>();
	    while (spe.hasMoreElements()) {
		v.addElement (spe.nextElement());
	    }
	    int size = v.size();
	    eventListeners = new EventListener [size];
	    v.copyInto (eventListeners);
	    log.info ("Registered " + size + " event listener(s)");
	}
    }

    /**************************************************************************
     * Broadcast an event to all event listeners.
     **************************************************************************/
    public void handleEvent (AnalysisEvent event) {
	if (event instanceof StateChangeEvent)
	    handleStateChangeEvent ((StateChangeEvent)event);
	else if (event instanceof HeartbeatProgressEvent)
	    handleHeartbeatProgressEvent ((HeartbeatProgressEvent)event);
	else if (event instanceof PercentProgressEvent)
	    handlePercentProgressEvent ((PercentProgressEvent)event);
	else if (event instanceof TimeProgressEvent)
	    handleTimeProgressEvent ((TimeProgressEvent)event);
	else if (event instanceof StepProgressEvent)
	    handleStepProgressEvent ((StepProgressEvent)event);
	else 
	    handleAnalysisEvent (event);
    }

    /**************************************************************************
     *
     *           Methods implementing EventListener interface
     *
     *************************************************************************/

    /**************************************************************************
     *
     *************************************************************************/
    public void handleAnalysisEvent (AnalysisEvent event) {
	for (int i = 0; i < eventListeners.length; i++)
	    eventListeners[i].handleAnalysisEvent (event);
    }

    /**************************************************************************
     *
     *************************************************************************/
    public void handleStateChangeEvent (StateChangeEvent event) {
	for (int i = 0; i < eventListeners.length; i++)
	    eventListeners[i].handleStateChangeEvent (event);
    }

    /**************************************************************************
     *
     *************************************************************************/
    public void handleHeartbeatProgressEvent (HeartbeatProgressEvent event) {
	for (int i = 0; i < eventListeners.length; i++)
	    eventListeners[i].handleHeartbeatProgressEvent (event);
    }

    /**************************************************************************
     *
     *************************************************************************/
    public void handlePercentProgressEvent (PercentProgressEvent event) {
	for (int i = 0; i < eventListeners.length; i++)
	    eventListeners[i].handlePercentProgressEvent (event);
    }

    /**************************************************************************
     *
     *************************************************************************/
    public void handleStepProgressEvent (StepProgressEvent event) {
	for (int i = 0; i < eventListeners.length; i++)
	    eventListeners[i].handleStepProgressEvent (event);
    }

    /**************************************************************************
     *
     *************************************************************************/
    public void handleTimeProgressEvent (TimeProgressEvent event) {
	for (int i = 0; i < eventListeners.length; i++)
	    eventListeners[i].handleTimeProgressEvent (event);
    }

}
