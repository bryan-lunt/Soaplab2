// StateChangeEvent.java
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
 * A class representing an event that characterizes a change of state of
 * the underlying service/analysis/job. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: StateChangeEvent.java,v 1.2 2006/11/03 20:28:56 marsenger Exp $
 */
public class StateChangeEvent
    extends AnalysisEvent {

    protected String previousState;
    protected String newState;

    /**************************************************************************
     * Basic constructor for sub-classes.
     **************************************************************************/
    protected StateChangeEvent() {
	super();
    }

    /**************************************************************************
     * The most general constructor, taking a message, the two states,
     * the old and new one, and an event source.
     **************************************************************************/
    public StateChangeEvent (Object source, String message,
			     String previousState,
			     String newState) {
	super (source, message);
        this.previousState = previousState;
	this.newState = newState;
    }

    /**************************************************************************
     * A constructor taking a general message and two states, the old
     * and new one. The event source is considered unknown.
     **************************************************************************/
    public StateChangeEvent (String message,
			     String previousState,
			     String newState) {
	this (null, message, previousState, newState);
    }

    /**************************************************************************
     * A usual constructor, taking just two states, the old and new
     * one. There will be no accompanied message, nor the event
     * source.
     **************************************************************************/
    public StateChangeEvent (String previousState,
			     String newState) {
	this (null, null, previousState, newState);
    }

    /**************************************************************************
     * Get the previous state as reported by this event.
     **************************************************************************/
    public String getPreviousState() {
	return previousState;
    }

    /**************************************************************************
     * Get the new state as reported by this event.
     **************************************************************************/
    public String getNewState() {
	return newState;
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
	Element elem = new Element (STATE_CHANGED);
	elem.setAttribute (PREVIOUS_STATE, getPreviousState());
	elem.setAttribute (NEW_STATE, getNewState());
	parent.addContent (elem);
	return parent;
    }

}
