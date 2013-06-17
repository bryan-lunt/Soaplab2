// CondDef.java
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

package org.soaplab.services.metadata;

import org.soaplab.services.GenUtils;
import java.util.Hashtable;

/**
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: CondDef.java,v 1.1.1.1 2006/11/03 09:15:01 marsenger Exp $
 */
public class CondDef {

    //
    // Constants for conditions
    //
    public static final String OWNER           = "owner";
    public static final String VERLEVEL        = "verlevel";
    public static final String EVALUATOR       = "evaluator";
    public static final String REGEXP          = "regexp";
    public static final String BOOLEXP         = "boolexp";
    public static final String REGEXP_ERROR    = "regexperror";
    public static final String BOOLEXP_ERROR   = "boolexperror";
    public static final String FOR_DIRECT_DATA = "direct";
    public static final String FOR_URL_DATA    = "filename";

    //
    // Who is primarily responsible for evaluating a condition.
    //
    public static final int COND_OWNER_N_A    = 0;   // not assigned, usually both client and server use given condition
    public static final int COND_OWNER_SERVER = 1;   // a server is supposed to evaluate given condition
    public static final int COND_OWNER_CLIENT = 2;   // a client is supposed to evaluate given condition

    //
    // Verification level expresses how strongly to apply verification rules
    //
    public static final int VERIF_LEVEL_N_A      = 0;   // not assigned
    public static final int VERIF_LEVEL_NONE     = 1;   // do not perform any verification rules
    public static final int VERIF_LEVEL_SILENT   = 2;   // verify, but without any messages, accept everything
    public static final int VERIF_LEVEL_WARNING  = 3;   // verify, warn if wrong, but accept everything
    public static final int VERIF_LEVEL_ABORT    = 4;   // verify, warn and abort if something goes wrong


    // members
    public PropertiesBag options;

    // some properties have default values
    String[] defaults = new String[] {
        OWNER,    "" + COND_OWNER_N_A,
        VERLEVEL, "" + VERIF_LEVEL_ABORT
    };

    /**************************************************************************
     * Constructors
     **************************************************************************/
    public CondDef() {
	options = new PropertiesBag (defaults);
    }

    /*********************************************************************
     * Convenient methods for accessing 'options'
     *********************************************************************/
    public boolean is (String key)     { return options.is (key); }
    public boolean exists (String key) { return options.exists (key); }
    public String get (String key)     { return options.getStr (key); }
    public int getInt (String key)     { return options.getInt (key); }
    
    /*********************************************************************
     * Convert a stringified verification level to an integer.  Return
     * -1 if unknown level is passed.
     ********************************************************************/
    private static Hashtable<String,Integer> verlevels = new Hashtable<String,Integer>();
    static {
        verlevels.put ("n_a",     new Integer (VERIF_LEVEL_N_A));
        verlevels.put ("none",    new Integer (VERIF_LEVEL_NONE));
        verlevels.put ("silent",  new Integer (VERIF_LEVEL_SILENT));
        verlevels.put ("warning", new Integer (VERIF_LEVEL_WARNING));
        verlevels.put ("abort",   new Integer (VERIF_LEVEL_ABORT));
    }

    protected static int checkAndGetLevel (String verlevel) {
        return GenUtils.checkAndGetStr2Int (verlevel, verlevels);
    }

    /*********************************************************************
     * Convert a stringified owner type to an integer.  Return -1 if
     * unknown type is passed.
     ********************************************************************/
    private static Hashtable<String,Integer> owners = new Hashtable<String,Integer>();
    static {
        owners.put ("n_a",     new Integer (COND_OWNER_N_A));
        owners.put ("server",  new Integer (COND_OWNER_SERVER));
        owners.put ("client",  new Integer (COND_OWNER_CLIENT));
    }

    protected static int checkAndGetOwner (String owner) {
        return GenUtils.checkAndGetStr2Int (owner, owners);
    }

    /*********************************************************************
     *
     **************************************************************************/
    public String toString() { return format (0); }

    /*********************************************************************
     * As toString() but with indentations.
     **************************************************************************/
    public String format (int indent) {
	String tabs = GenUtils.indent (indent);
        return tabs + "[CondDef]" +
                      " verlevel=" + GenUtils.inverseMapping (getInt (VERLEVEL), verlevels) +
                      " owner="    + GenUtils.inverseMapping (getInt (OWNER), owners) + "\n" +
	       options.format (indent + 1);
    }
    
}
