// DefaultEventListener.java
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

import org.soaplab.services.Config;
import org.apache.commons.logging.impl.SimpleLog;

/**
 * A default implementation of an {@link EventListener} interface. It
 * logs all events in a logging level as indicated by configuration
 * properties {@link org.soaplab.services.Config#PROP_EVENT_LOG_LEVEL
 * PROP_EVENT_LOG_LEVEL} and {@link
 * org.soaplab.services.Config#PROP_EVENT_IGNORE_HEARBEAT
 * PROP__EVENT_IGNORE_HEARBEAT}. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: DefaultEventListener.java,v 1.3 2006/11/20 01:49:35 marsenger Exp $
 */

public class DefaultEventListener
    implements EventListener {

    private static org.apache.commons.logging.Log log =
       org.apache.commons.logging.LogFactory.getLog (EventListener.class);

    /**************************************************************************
     *
     *************************************************************************/
    public DefaultEventListener() {
    }

    /**************************************************************************
     *
     *************************************************************************/
    public void handleAnalysisEvent (AnalysisEvent event) {
	logMessage (event.formatSource() + " " + event.toString());
    }

    /**************************************************************************
     *
     *************************************************************************/
    public void handleStateChangeEvent (StateChangeEvent event) {
	logMessage (event.formatSource() + " " +
		    event.getPreviousState() + " --> " + event.getNewState());
    }

    /**************************************************************************
     *
     *************************************************************************/
    public void handleHeartbeatProgressEvent (HeartbeatProgressEvent event) {
	boolean ignoreHeartbeatProgressEvents = false;
	try {
	    ignoreHeartbeatProgressEvents = 
		Config.isEnabled (Config.PROP_EVENT_IGNORE_HEARBEAT,
				  false,   // default value
				  null, this);
	} catch (org.apache.commons.configuration.ConversionException e) {
	}
	if (! ignoreHeartbeatProgressEvents) {
	    logMessage (event.formatSource() + " " +
			(event.getMessage() == null
			 ? "(hearbeat event)"
			 : event.getMessage()));
	}
    }

    /**************************************************************************
     *
     *************************************************************************/
    public void handlePercentProgressEvent (PercentProgressEvent event) {
	logMessage (event.formatSource() + " " +
		    event.getPercentage() + "%");
    }

    /**************************************************************************
     *
     *************************************************************************/
    public void handleStepProgressEvent (StepProgressEvent event) {
	logMessage (event.formatSource() + " " +
		    event.getCompletedSteps() + " of " +
		    event.getTotalSteps() + " completed");
    }

    /**************************************************************************
     *
     *************************************************************************/
    public void handleTimeProgressEvent (TimeProgressEvent event) {
	logMessage (event.formatSource() + " " +
		    event.getTimeRemaining() + " remaining");
    }

    /**************************************************************************
     *
     *************************************************************************/
    protected void logMessage (String msg) {
	switch (getLevel()) {
	case SimpleLog.LOG_LEVEL_TRACE:
	    log.trace (msg); break;
	case SimpleLog.LOG_LEVEL_DEBUG:
	    log.debug (msg); break;
	case SimpleLog.LOG_LEVEL_INFO:
	    log.info (msg); break;
	case SimpleLog.LOG_LEVEL_WARN:
	    log.warn (msg); break;
	case SimpleLog.LOG_LEVEL_ERROR:
	    log.error (msg); break;
	case SimpleLog.LOG_LEVEL_FATAL:
	    log.fatal (msg); break;
	case SimpleLog.LOG_LEVEL_OFF:
	    // ignore
	}
    }

    /**************************************************************************
     * Return wanted loogging level.
     *************************************************************************/
    protected int getLevel() {
	String strLevel = Config.getString (Config.PROP_EVENT_LOG_LEVEL,
					    "debug",
					    null, this);
        if ("trace".equalsIgnoreCase (strLevel)) {
            return SimpleLog.LOG_LEVEL_TRACE;
        } else if ("debug".equalsIgnoreCase (strLevel)) {
            return  SimpleLog.LOG_LEVEL_DEBUG;
        } else if ("info".equalsIgnoreCase (strLevel)) {
            return SimpleLog.LOG_LEVEL_INFO;
        } else if ("warn".equalsIgnoreCase (strLevel)) {
            return SimpleLog.LOG_LEVEL_WARN;
        } else if ("error".equalsIgnoreCase (strLevel)) {
	    return SimpleLog.LOG_LEVEL_ERROR;
        } else if ("fatal".equalsIgnoreCase (strLevel)) {
            return SimpleLog.LOG_LEVEL_FATAL;
        } else if ("off".equalsIgnoreCase (strLevel)) {
            return SimpleLog.LOG_LEVEL_OFF;
	} else {
	    log.warn ("Unknown value in property " +
		      Config.PROP_EVENT_LOG_LEVEL +
		      ". Setting to 'debug'.");
	    return SimpleLog.LOG_LEVEL_DEBUG;
	}
    }

}
