// StepProgressEvent.java
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

import org.jdom.Element;

/**
 * A class representing an event that reports the numnber of total and
 * completed steps for the underlying service/analysis/job. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: StepProgressEvent.java,v 1.2 2006/11/03 20:28:56 marsenger Exp $
 */
public class StepProgressEvent
    extends AnalysisEvent {

    protected int totalSteps = 0;
    protected int completedSteps = 0;

    /**************************************************************************
     * Basic constructor for sub-classes.
     **************************************************************************/
    protected StepProgressEvent() {
	super();
    }

    /**************************************************************************
     * The most general constructor, taking a message, number of total
     * and completed steps, and the event source.
     **************************************************************************/
    public StepProgressEvent (Object source, String message,
			      int totalSteps,
			      int completedSteps) {
	super (source, message);
        this.totalSteps = totalSteps;
        this.completedSteps = completedSteps;
    }

    /**************************************************************************
     * A constructor taking a general message and number of total and
     * completed steps. The event source is considered unknown.
     **************************************************************************/
    public StepProgressEvent (String message,
			      int totalSteps,
			      int completedSteps) {
	this (null, message, totalSteps, completedSteps);
    }

    /**************************************************************************
     * A usual constructor, taking just the number of total and
     * completed steps. There will be no accompanied message, nor the
     * event source.
     **************************************************************************/
    public StepProgressEvent (int totalSteps,
			      int completedSteps) {
	this (null, null, totalSteps, completedSteps);
    }

    /**************************************************************************
     * Get the number of total steps.
     **************************************************************************/
    public int getTotalSteps() {
	return totalSteps;
    }

    /**************************************************************************
     * Get the number of completed steps.
     **************************************************************************/
    public int getCompletedSteps() {
	return completedSteps;
    }

    /**************************************************************************
     * Create an XML element representing this event. <p>
     *
     * @return a <a
     * href="http://www.jdom.org/docs/apidocs/org/jdom/Element.html">jDom
     * element</a> that can be used for building the whole XML document
     *************************************************************************/
    public Element toXML() {
	Element parent = super.toXML();
	Element elem = new Element (STEP_PROGRESS);
	elem.setAttribute (TOTAL_STEPS, "" + getTotalSteps());
	elem.setAttribute (STEPS_COMPLETED, "" + getCompletedSteps());
	parent.addContent (elem);
	return parent;
    }

}
