// DefaultDeserializer.java
//
// Created: November 2006
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

import org.soaplab.share.SoaplabException;
import org.soaplab.services.GenUtils;

import org.jdom.Element;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import org.xml.sax.InputSource;
import java.io.StringReader;
import java.io.File;
import java.util.Iterator;
import java.util.Enumeration;

import java.text.SimpleDateFormat;

/**
 * A default implementation of the {@link EventDeserializer} that
 * recognizes standard Soaplab events. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: DefaultDeserializer.java,v 1.2 2007/04/19 22:17:05 marsenger Exp $
 */
public class DefaultDeserializer
    implements EventConstants, EventDeserializer {

    /**************************************************************************
     * Find available deserializer, create its instance and return
     * it. If not found return an instance of self.
     **************************************************************************/
    public static EventDeserializer getDeserializer() {
	// only the first class interests us
	Enumeration<EventDeserializer> spe =
	    GenUtils.spiProviders (EventDeserializer.class);
	while (spe.hasMoreElements()) {
	    return (EventDeserializer)spe.nextElement();
	}
	return new DefaultDeserializer();
    }

    /**************************************************************************
     *
     **************************************************************************/
    public AnalysisEvent deserialize (Object source, String xmlEvent)
	throws SoaplabException {

	if (xmlEvent == null) return null;
	Document doc = null;
	try {
	    doc = new SAXBuilder()
		.build (new InputSource (new StringReader (xmlEvent)));
	} catch (Exception e) {
	    throw new SoaplabException ("Parsing Soaplab event failed.", e);
	}
	return createEventFromDocument (source, doc);
    }

    /**************************************************************************
     *
     **************************************************************************/
    public AnalysisEvent deserialize (Object source, File xmlEventFile)
	throws SoaplabException {

	if (xmlEventFile == null) return null;
	Document doc = null;
	try {
	    SAXBuilder saxBuilder = new SAXBuilder();
	    doc = saxBuilder.build (xmlEventFile);
	} catch (Exception e) {
	    throw new SoaplabException ("Parsing Soaplab event failed.", e);
	}
	return createEventFromDocument (source, doc);
    }

    /**************************************************************************
     * Create an event from the given DOM document.
     **************************************************************************/
    @SuppressWarnings("unchecked")
    protected AnalysisEvent createEventFromDocument (Object source,
						     Document doc)
	throws SoaplabException {

	Element analysisEvent = doc.getRootElement();
	if (! ANALYSIS_EVENT.equals (analysisEvent.getName()))
	    throw new SoaplabException
		("Deserializing Soaplab event failed: Document root element is not " + ANALYSIS_EVENT);

	AnalysisEvent event = null;
	String timestamp = analysisEvent.getAttributeValue (TIMESTAMP);
	String message = analysisEvent.getChildText (MESSAGE);
	for (Iterator<Element> it = analysisEvent.getChildren().iterator(); it.hasNext(); ) {
	    Element child = it.next();
	    String childName = child.getName();
	    if (STATE_CHANGED.equals (childName)) {
		event =
		    new StateChangeEvent (source, message,
					  child.getAttributeValue (PREVIOUS_STATE),
					  child.getAttributeValue (NEW_STATE));

	    } else if (HEARTBEAT_PROGRESS.equals (childName)) {
		event =
		    new HeartbeatProgressEvent (source, message);

	    } else if (PERCENT_PROGRESS.equals (childName)) {
		event =
		    new PercentProgressEvent (source, message,
					      NumberUtils.toFloat (child.getAttributeValue (PERCENTAGE)));

	    } else if (TIME_PROGRESS.equals (childName)) {
		float remaining = NumberUtils.toFloat(child.getAttributeValue (REMAINING));
		event =
		    new TimeProgressEvent (source, message,
					   (long)(remaining * 10000000));

	    } else if (STEP_PROGRESS.equals (childName)) {
		event =
		    new StepProgressEvent (source, message,
					   NumberUtils.toInt (child.getAttributeValue (TOTAL_STEPS)),
					   NumberUtils.toInt (child.getAttributeValue (STEPS_COMPLETED)));

	    } else {
		event = createDefaultEvent (child, source, message);

	    }
 	    if (StringUtils.isNotBlank (timestamp)) {
		try {
		    event.setTimestamp (new SimpleDateFormat (TIMESTAMP_FORMAT).parse (timestamp));
		} catch (java.text.ParseException e) {
		    // ignore
		}
	    }
	}
	return event;
    }

    /**************************************************************************
     * Create a default event. This implementation creates a simple
     * {@link AnalysisEvent} but it is separated here in order to give
     * a chance to sub-classes to further parse the XML (given in
     * 'child') and to create a specific event. <p>
     *
     * @param child contains everything (except MESSAGE) below the
     * ANALYSIS_EVENT tag
     * @param source what was passed to the {@link #deserialize} method
     * @param message what was extracted from the ANALYSIS_EVENT tag
     **************************************************************************/
    protected AnalysisEvent createDefaultEvent (Element child,
						Object source,
						String message)
	throws SoaplabException {
	return new AnalysisEvent (source, message);
    }

//     public static void main (String[] args) {
// 	try {
// 	    Date  date =
// 		new SimpleDateFormat (TIMESTAMP_FORMAT)
// 		.parse ("Fri Nov 03 15:22:28 GMT 2006");
// 	    System.out.println (date.toString());
// 	} catch (java.text.ParseException e) {
// 	    System.err.println ("ERROR: " + e.getMessage());
// 	}
//     }

}
