// EventConstants.java
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

/**
 * A collection of constants used Soaplab events in XML format. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: EventConstants.java,v 1.1 2006/11/03 20:28:56 marsenger Exp $
 */
public interface EventConstants {

    static final String ANALYSIS_EVENT = "analysis_event";
    static final String MESSAGE = "message";
    static final String TIMESTAMP = "timestamp";

    static final String STATE_CHANGED = "state_changed";
    static final String PREVIOUS_STATE = "previous_state";
    static final String NEW_STATE = "new_state";

    static final String TIME_PROGRESS = "time_progress";
    static final String REMAINING = "remaining";

    static final String PERCENT_PROGRESS = "percent_progress";
    static final String PERCENTAGE = "percentage";

    static final String STEP_PROGRESS = "step_progress";
    static final String TOTAL_STEPS = "total_steps";
    static final String STEPS_COMPLETED = "steps_completed";

    static final String HEARTBEAT_PROGRESS = "heartbeat_progress";

    static final String TIMESTAMP_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy";
}
