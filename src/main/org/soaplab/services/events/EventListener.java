// EventListener.java
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
 * An interface for listening to the Soaplab events. It is an internal
 * interface, not visible as a web service. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: EventListener.java,v 1.2 2006/11/19 02:14:30 marsenger Exp $
 */

public interface EventListener {

    /**************************************************************************
     * This method is called when a general event is sent, or its
     * subclass that is not explicitly known to this interface. <p>
     *
     * @param event that was sent
     *************************************************************************/
    void handleAnalysisEvent (AnalysisEvent event);

    /**************************************************************************
     * This method is called when a "state-changed" event is sent. <p>
     *
     * @param event that was sent
     *************************************************************************/
    void handleStateChangeEvent (StateChangeEvent event);

    /**************************************************************************
     * This method is called when a "heart-beat" event is sent. <p>
     *
     * @param event that was sent
     *************************************************************************/
    void handleHeartbeatProgressEvent (HeartbeatProgressEvent event);

    /**************************************************************************
     * This method is called when a "percent progress" event is sent. <p>
     *
     * @param event that was sent
     *************************************************************************/
    void handlePercentProgressEvent (PercentProgressEvent event);

    /**************************************************************************
     * This method is called when a "step progress" event is sent. <p>
     *
     * @param event that was sent
     *************************************************************************/
    void handleStepProgressEvent (StepProgressEvent event);

    /**************************************************************************
     * This method is called when a "time progress" event is sent. <p>
     *
     * @param event that was sent
     *************************************************************************/
    void handleTimeProgressEvent (TimeProgressEvent event);

}
