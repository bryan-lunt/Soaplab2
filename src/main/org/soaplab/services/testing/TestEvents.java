// TestEvents.java
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

package org.soaplab.services.testing;

import org.soaplab.share.SoaplabException;
import org.soaplab.services.JobState;
import org.soaplab.services.events.AnalysisEvent;
import org.soaplab.services.events.StateChangeEvent;
import org.soaplab.services.events.TimeProgressEvent;
import org.soaplab.services.events.PercentProgressEvent;
import org.soaplab.services.events.StepProgressEvent;
import org.soaplab.services.events.HeartbeatProgressEvent;
import org.soaplab.services.events.EventDeserializer;
import org.soaplab.services.events.DefaultDeserializer;

/**
 * A class showing some events. Just for testing. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: TestEvents.java,v 1.2 2006/11/03 20:28:57 marsenger Exp $
 */
public class TestEvents {

    static EventDeserializer deserializer = new DefaultDeserializer();

    static void title (String title) {
	System.out.println (title);
	for (int i = 0; i < title.length(); i++)
	    System.out.print ("-");
	System.out.println();
    }

    public static void main (String[] args) {

	String xml = null;

	// state change event
	title ("StateChangeEvent:");
	JobState state = new JobState (JobState.COMPLETED);
	state.setDetailed ("200");
	state.setDescription ("Mirr\"or\ned");
	StateChangeEvent event1 =
	    new StateChangeEvent (null, "this is a message",
				  new JobState (JobState.CREATED).toString(),
				  state.toString());
	System.out.println (xml = event1.toString());
	System.out.println (deserialize (xml).toString());

	// time progress event
	title ("TimeProgressEvent:");
	TimeProgressEvent event2 =
	    new TimeProgressEvent (null, "this is a message",
				   123456789);
	System.out.println (xml = event2.toString());
	System.out.println (deserialize (xml).toString());

	// percent progress event
	title ("PercentProgressEvent:");
	PercentProgressEvent event3 =
	    new PercentProgressEvent (null, "this is a message",
				      33);
 	System.out.println (xml = event3.toString());
	System.out.println (deserialize (xml).toString());

	// step progress event
	title ("StepProgressEvent:");
	StepProgressEvent event4 =
	    new StepProgressEvent (10, 6);
	System.out.println (xml = event4.toString());
	System.out.println (deserialize (xml).toString());

	// heartbeat progress event
	title ("HeartbeatProgressEvent:");
	HeartbeatProgressEvent event5 =
	    new HeartbeatProgressEvent();
	System.out.println (xml = event5.toString());
	System.out.println (deserialize (xml).toString());

    }

    static AnalysisEvent deserialize (String xml) {
	try {
	    return deserializer.deserialize (null, xml);
	} catch (SoaplabException e) {
	    System.err.println (e.getMessage());
	    return new AnalysisEvent (null);
	}
    }

}
