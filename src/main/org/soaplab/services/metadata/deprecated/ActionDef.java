// ActionDef.java
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
import org.soaplab.services.GenUtils;
import java.util.Hashtable;

/**
 * A structure defining an Action. An action is something (usually a
 * program) executed on the server-side. <p>
 *
 * Currently, there are two types of actions supported:
 * <ul>
 *   <li> 'exe' is a normal action - an analysis to be started
 *   <li> 'monitor' is an optional action serving to monitor how
 *        an 'exe' action is running
 * </ul>
 * <p>
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: ActionDef.java,v 1.1 2007/10/23 08:20:54 marsenger Exp $
 */
public class ActionDef {

    //
    // Action types
    //
    public static final int EXE           = 0;   // a program should be executed
    public static final int MONITOR       = 1;
    public static final int DAEMON        = 2;
    public static final int DUMMY         = 9;

    //
    // Monitor types
    //
    public static final int MSG_PROGRESS     = 0;
    public static final int PERCENT_PROGRESS = 1;
    public static final int TIME_PROGRESS    = 2;
    public static final int STEP_PROGRESS    = 3;

    // members
    public int type = EXE;         // what to do
    public String method = "";     // rules how to create an analysis input, or
                                   // type of monitoring for 'monitor' action
    public String launcher = "";   // how to execute (wrapper around an execution)
    public String file = "";       // what to execute (program...)
    public PropertiesBag options = new PropertiesBag();

    public ActionDef() {}

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

    /*********************************************************************
     * Convert string representation of an action type to an
     * integer. <p>
     *
     * Returns -1 if unknown type is passed.
     ********************************************************************/
    private static Hashtable<String,Integer> actionTypes = new Hashtable<String,Integer>();
    static {
        actionTypes.put ("exe",     new Integer (EXE));
        actionTypes.put ("monitor", new Integer (MONITOR));
        actionTypes.put ("daemon",  new Integer (DAEMON));
        actionTypes.put ("dummy",   new Integer (DUMMY));
    }

    public static int checkAndGetType (String actionType) {
        return GenUtils.checkAndGetStr2Int (actionType, actionTypes);
    }

    /*********************************************************************
     * Convert string representation of a monitor type to an
     * integer. <p>
     *
     * Return MSG_PROGRESS if unknown type is passed.
     ********************************************************************/
    private static Hashtable<String,Integer> monitorTypes = new Hashtable<String,Integer>();
    static {
        monitorTypes.put ("percentprogress", new Integer (PERCENT_PROGRESS));
        monitorTypes.put ("timeprogress",    new Integer (TIME_PROGRESS));
        monitorTypes.put ("stepprogress",    new Integer (STEP_PROGRESS));
    }

    public static int checkAndGetMonitorType (String monitorType) {
        int _type = GenUtils.checkAndGetStr2Int (monitorType, monitorTypes);
        return (_type == -1 ? MSG_PROGRESS : _type);
    }

    public String toString() { return format (0); }

    /**************************************************************************
     * As toString() but with indentations.
     **************************************************************************/
    public String format (int indent) {
	String tabs = GenUtils.indent (indent);
        StringBuilder buf = new StringBuilder();
        buf.append (tabs + "[ActionDef]\n");
        buf.append (tabs + "\ttype = "     + GenUtils.inverseMapping (type, actionTypes) + "(" + type + ")\n");
        buf.append (tabs + "\tfile = "     + file + "\n");
        buf.append (tabs + "\tlauncher = " + launcher + "\n");
	buf.append (tabs + "\tmethod = "   + method + "\n");
	buf.append (options.format (indent + 1));
        return buf.toString();
    }
}
