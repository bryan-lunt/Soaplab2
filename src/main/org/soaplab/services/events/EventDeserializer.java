// EventDeserializer.java
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

import java.io.File;

/**
 * An interface whose implementations can re-create Soaplab events
 * (Java objects) from their XML representation. <p>
 *
 * The reason why it is an interface and not a class is that Soaplab
 * can use the SPI mechanism to find an implementation that can deal
 * also with non-default events (the default events can be
 * deserialized by the default implementation). <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: EventDeserializer.java,v 1.2 2006/11/19 02:14:30 marsenger Exp $
 */
public interface EventDeserializer {

    /**************************************************************************
     * Create an object representing an event from 'xmlEvent'. <p>
     *
     * @param source is a source of this event; can be null if the
     * source is not known (this is here because event serialization,
     * the XML string, does not contain any event source)
     *
     * @param xmlEvent is a serialized event
     *
     * @return an event created from input parameters
     * @throws SoaplabException if something prevents to create an event
     **************************************************************************/
    AnalysisEvent deserialize (Object source, String xmlEvent)
	throws SoaplabException;

    /**************************************************************************
     * Create an object representing an event from XML stored in given
     * file. <p>
     *
     * Otherwise the same as the {@link #deserialize(Object,String)}
     * method.
     *
     **************************************************************************/
    AnalysisEvent deserialize (Object source, File xmlEventFile)
	throws SoaplabException;


}
