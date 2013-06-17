// HeartbeatProgressEvent.java
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
 * A class representing an event that indicates that the underlying
 * service/analysis/job is still in progress. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: HeartbeatProgressEvent.java,v 1.2 2006/11/03 20:28:56 marsenger Exp $
 */
public class HeartbeatProgressEvent
    extends AnalysisEvent {

    /**************************************************************************
     * A usual constructor, taking nothing. There will be no
     * accompanied message, nor the event source.
     **************************************************************************/
    public HeartbeatProgressEvent() {
	this (null, null);
    }

    /**************************************************************************
     * The most general constructor, taking a message, and the event
     * source.
     **************************************************************************/
    public HeartbeatProgressEvent (Object source, String message) {
	super (source, message);
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
	Element elem = new Element (HEARTBEAT_PROGRESS);
	parent.addContent (elem);
	return parent;
    }

}
