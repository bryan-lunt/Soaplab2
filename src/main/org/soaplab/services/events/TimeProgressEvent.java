// TimeProgressEvent.java
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
 * A class representing an event that reports the remaining time of
 * the underlying service/analysis/job. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: TimeProgressEvent.java,v 1.2 2006/11/03 20:28:56 marsenger Exp $
 */
public class TimeProgressEvent
    extends AnalysisEvent {

    protected long timeRemaining = -1;

    /**************************************************************************
     * Basic constructor for sub-classes.
     **************************************************************************/
    protected TimeProgressEvent() {
	super();
    }

    /**************************************************************************
     * The most general constructor, taking a message, remaining time
     * (in 100 nanoseconds), and the event source.
     **************************************************************************/
    public TimeProgressEvent (Object source, String message,
			      long timeRemaining) {
	super (source, message);
        this.timeRemaining = timeRemaining;
    }

    /**************************************************************************
     * A constructor taking a general message and , remaining time
     * (in 100 nanoseconds). The event source is considered unknown.
     **************************************************************************/
    public TimeProgressEvent (String message,
			      long timeRemaining) {
	this (null, message, timeRemaining);
    }

    /**************************************************************************
     * A usual constructor, taking just the remaining time (in 100
     * nanoseconds). There will be no accompanied message, nor the
     * event source.
     **************************************************************************/
    public TimeProgressEvent (long timeRemaining) {
	this (null, null, timeRemaining);
    }

    /**************************************************************************
     * Get the remaining time (in 100 nanoseconds).
     **************************************************************************/
    public long getTimeRemaining() {
	return timeRemaining;
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
	Element elem = new Element (TIME_PROGRESS);
	elem.setAttribute (REMAINING, "" + new Float (getTimeRemaining())/10000000);
	parent.addContent (elem);
	return parent;
    }

}
