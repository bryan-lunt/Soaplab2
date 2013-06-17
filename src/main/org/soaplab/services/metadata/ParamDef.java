// ParamDef.java
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
import org.soaplab.share.SoaplabConstants;

import org.apache.commons.lang.StringUtils;

import java.util.Hashtable;

/**
 * A general class defining a parameter for an analysis tool. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: ParamDef.java,v 1.8 2007/11/12 04:29:14 marsenger Exp $
 */
public class ParamDef {

    //
    // Constants for parameter properties
    //
    public static final String TAG            = "qualifier";
    public static final String METHOD         = "method";
    public static final String MANDATORY      = SoaplabConstants.INPUT_MANDATORY;    // Boolean
    public static final String TAGSEPAR       = "tagsepar";
    public static final String USE_DEFAULTS  = "use_defaults";// Boolean
    public static final String NODISPLAY      = "nodisplay";    // Boolean
    public static final String READONLY       = "readonly";     // Boolean
    public static final String SERVER_CLASS   = "server_class";
    public static final String ENVAR          = "envar";        // Boolean
    public static final String SEPARATOR      = "separator";
    public static final String ORDERING       = "ordering";     // Integer

    // for boolean parameter
    public static final String TRUEVALUE      = "truevalue";
    public static final String FALSEVALUE     = "falsevalue";

    // Available parameter types
    public static final int BASE_PARAMETER         = 0;
    public static final int STANDARD_PARAMETER     = 1;
    public static final int RANGE_PARAMETER        = 2;
    public static final int IO_PARAMETER           = 3;
    public static final int CHOICE_PARAMETER       = 4;
    public static final int CHOICE_LIST_PARAMETER  = 5;

    // members
    public String id;
    public String type;
    public String dflt = "";
    public CondDef[] conds = new CondDef[0];
    public PropertiesBag options = new PropertiesBag();

    // to link with an appropriate parameter type
    // (we need this because just a type of this object is not sufficient
    // because the same type of XxxxDef can be used for different
    // parameter types)
    public int createdFor = BASE_PARAMETER;

    /**************************************************************************
     * Constructor
     **************************************************************************/
    public ParamDef() {}

    /**************************************************************************
     * Convenient methods for accessing 'options'
     **************************************************************************/
    public boolean is (String key)            { return options.is (key); }
    public boolean is (String key, int serNo) { return options.is (key, serNo); }
    public boolean exists (String key)        { return options.exists (key); }
    public String get (String key)            { return options.getStr (key); }
    public int getInt (String key)            { return options.getInt (key); }

    public void add (PropertiesBag newOptions) {
        options.addProperties (newOptions);
    }

    /******************************************************************************
     * Several extensions to the convenience methods accessing
     * 'options'.  The search key is extended by 'serNo' at its end
     * (but only if serNo > 0).  If the extended key was not found, a
     * normal (plain) key is tried. <p>
     *
     * For example, you can look for 'size1' by calling get ('size', 1).
     ******************************************************************************/
    public String get (String key, int serNo) {
	if (serNo <= 0) return get (key);
	String value = get (key + serNo);
	if (StringUtils.isBlank (value)) return get (key);
	return value;
    }

    public int getInt (String key, int serNo) {
	if (serNo <= 0) return getInt (key);
	int value = getInt (key + serNo);
	if (value == -1) return getInt (key);
	return value;
    }

    /**************************************************************************
     * This method does nothing much here but it's supposed to be
     * overritten.
     **************************************************************************/
    public boolean canBeRepeatable() {
        return false;
    }

    private static Hashtable<String,Integer> paramTypes = new Hashtable<String,Integer>();
    static {
        paramTypes.put ("basic",       new Integer (BASE_PARAMETER));
        paramTypes.put ("standard",    new Integer (STANDARD_PARAMETER));
        paramTypes.put ("range",       new Integer (RANGE_PARAMETER));
        paramTypes.put ("data",        new Integer (IO_PARAMETER));
        paramTypes.put ("choice",      new Integer (CHOICE_PARAMETER));
        paramTypes.put ("choice_list", new Integer (CHOICE_LIST_PARAMETER));
    }

     public String toString() { return format (0); }

    /**************************************************************************
     * As toString() but with indentations.
     **************************************************************************/
    public String format (int indent) {
	String tabs = GenUtils.indent (indent);
        StringBuilder buf = new StringBuilder();
        buf.append (tabs + "[ParamDef - " + GenUtils.inverseMapping (createdFor, paramTypes) + "]");
        buf.append (       " id=" + id + " type=" + type + "\n");
        if (StringUtils.isNotBlank (dflt))
	    buf.append (tabs + "\tdefault = " + dflt + "\n");
        if (conds != null)
	    for (int i = 0; i < conds.length; i++)
		buf.append (conds[i].format (indent + 1) + "\n");
        buf.append (options.format (indent + 1));
	return buf.toString();
    }
}
