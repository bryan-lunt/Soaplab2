// EventDef.java
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

package org.soaplab.services.metadata.deprecated;

import org.soaplab.services.metadata.PropertiesBag;
import org.soaplab.services.metadata.CondDef;
import org.soaplab.services.GenUtils;

/**
 * A structure defining an Event. An event specifies what to do when a
 * client request comes to a server (typical event would be to execute
 * an analysis). <p>
 *
 * However, for simple cases (like starting just one analysis) there
 * is only one event, doing only one action. But it is prepared for
 * the server-side "pipelining" from analysis to analysis. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: EventDef.java,v 1.1 2007/10/23 08:20:54 marsenger Exp $
 */
public class EventDef {

    // members
    public ActionDef[] actions;    // what to do on the server side
    public CondDef[] conds;        // inter-parameter verification rules
    public PropertiesBag options = new PropertiesBag();

    public EventDef() {}

    /**************************************************************************
     * Convenient methods for accessing 'options'
     **************************************************************************/
    public boolean is (String key)     { return options.is (key); }
    public boolean exists (String key) { return options.exists (key); }
    public String get (String key)     { return options.getStr (key); }
    public int getInt (String key)     { return options.getInt (key); }
    public void add (PropertiesBag newOptions) {
        options.addProperties (newOptions);
    }

    /**************************************************************************
     * Find and return the main action - the first action of type EXE.
     **************************************************************************/
    public ActionDef getMainActionDef() {
	if (actions == null) return null;
	for (ActionDef action: actions) {
	    if (action.type == ActionDef.EXE)
		return action;
	}
	return null;   // not found any EXE type
    }

    public String toString() { return format (0); }

    /**************************************************************************
     * As toString() but with indentations.
     **************************************************************************/
    public String format (int indent) {
	String tabs = GenUtils.indent (indent);
        StringBuilder buf = new StringBuilder();

        buf.append (tabs + "[EventDef]\n" + options.format (indent + 1));
        if (actions != null)
	    for (int i = 0; i < actions.length; i++)
		buf.append (actions[i].format (indent + 1) + "\n");
        if (conds != null)
	    for (int i = 0; i < conds.length; i++)
		buf.append (conds[i].format (indent + 1) + "\n");
        return buf.toString();
    }
}
