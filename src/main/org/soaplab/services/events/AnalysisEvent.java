// AnalysisEvent.java
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
import org.jdom.Document;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format;

import org.apache.commons.lang.StringUtils;

import java.util.Date;

/**
 * A base (immutable) class for all events coming from a running
 * analysis. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: AnalysisEvent.java,v 1.4 2007/10/18 15:52:05 marsenger Exp $
 */
public class AnalysisEvent
    implements EventConstants {

    protected Object source;
    protected Date timestamp;
    protected String message;

    /**************************************************************************
     * Basic constructor for sub-classes.
     **************************************************************************/
    protected AnalysisEvent() {
	timestamp = new Date();
    }

    /**************************************************************************
     * A constructor taking a general message carried on by this
     * event, and the source of this event.
     **************************************************************************/
    public AnalysisEvent (Object source, String message) {
	this();
	this.message = message;
	this.source = source;
    }

    /**************************************************************************
     * A constructor taking a general message carried on by this
     * event. The event source is unknown.
     **************************************************************************/
    public AnalysisEvent (String message) {
	this (null, message);
    }

    /**************************************************************************
     * Get the main contents of this event. Use {@link #toString} to
     * get an XML representation of this event. <p>
     *
     * @return the event message. The returned message is never null -
     * just may be empty.
     **************************************************************************/
    public String getMessage() {
	return (message == null ? "" : message);
    }

    /**************************************************************************
     * Get the event source (which may be null).
     **************************************************************************/
    public Object getSource() {
	return source;
    }

    /**************************************************************************
     * Get the event timestamp. <p>
     *
     * @return the time when the event was created
     **************************************************************************/
    public Date getTimestamp() {
	return timestamp;
    }

    /**************************************************************************
     * Set the event timestamp. <p>
     *
     * @param timestamp is time assigned to the event
     **************************************************************************/
    public void setTimestamp (Date timestamp) {
	this.timestamp = timestamp;
    }

    /**************************************************************************
     * Return a stringified version of this event's source. This is
     * here as a separate method (and part of the {@link #toString}
     * method) because the event source is not part of the event XML
     * serialization. <p>
     *
     * @return a formatted event source
     *************************************************************************/
    public String formatSource() {
	if (getSource() == null) return "";
	return getSource().toString();
    }

    /**************************************************************************
     * Create an XML element representing this event. Note that
     * currently the event source is not part of the XML
     * representation. <p>
     *
     * @return a <a
     * href="http://www.jdom.org/docs/apidocs/org/jdom/Element.html">jDom
     * element</a> that can be used for building the whole XML document
     *************************************************************************/
    public Element toXML() {
	Element elem = new Element (ANALYSIS_EVENT);
	elem.setAttribute (TIMESTAMP, getTimestamp().toString());
	if (StringUtils.isNotBlank (getMessage()))
	    elem.addContent (new Element (MESSAGE).addContent (getMessage()));
	return elem;
    }

    /**************************************************************************
     * Return a stringified version of this event - in an XML format.
     **************************************************************************/
    public String toString() {
	Document doc = new Document (toXML());
	XMLOutputter xo = new XMLOutputter();
	xo.setFormat (Format.getPrettyFormat());
	return xo.outputString (doc);
    }

    /**************************************************************************
     * Compare this event with the 'newEvent'. <p>
     *
     * @param newEvent is compared with this instance
     * @return true if the 'newEvent' is newer (according to its
     * timestamp) than this one
     **************************************************************************/
    public boolean isOlderThan (AnalysisEvent newEvent) {
	return this.getTimestamp().before (newEvent.getTimestamp());
    }

}
