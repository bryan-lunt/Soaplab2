// ResultDef.java
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
 * A structure defining a data result. Each IO parameter can contain
 * zero, one, or more such structures. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: ResultDef.java,v 1.1 2007/10/23 22:10:30 marsenger Exp $
 */
public class ResultDef {

    /** Property name. Its value (of type String) is a class name of
     * an adaptor for post-processing results. */
    public static final String OUTPUT_ADAPTOR = "output_adaptor";

    //
    // Result special types
    //
    public static final int NORMAL = 0;   // usual output
    public static final int INFO   = 1;   // only info about output file
    public static final int URL    = 2;   // about external viewers for these data

    // members
    public String type;                 // BSA compliant type (mandatory)
    public String name;                 // mandatory result name
    public int    specialType = NORMAL; // (different 'types' may require
                                        // some addition care)

    public PropertiesBag options = new PropertiesBag();
                                                          // (e.g. OUTPUT_ADAPTOR)

    public ResultDef() {}

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
     * Convert string representation of a special type to an integer.
     * Return -1 if unknown type is passed.
     ********************************************************************/
    private static Hashtable<String,Integer> specialTypes = new Hashtable<String,Integer>();
    static {
        specialTypes.put ("normal",  new Integer (NORMAL));
        specialTypes.put ("info",    new Integer (INFO));
        specialTypes.put ("url",     new Integer (URL));
    }

    public static int checkAndGetType (String type) {
        return GenUtils.checkAndGetStr2Int (type, specialTypes);
    }

    public String toString() { return format (0); }

    /**************************************************************************
     * As toString() but with indentations.
     **************************************************************************/
    public String format (int indent) {
	String tabs = GenUtils.indent (indent);
        StringBuilder buf = new StringBuilder();
        buf.append (tabs + "[ResultDef]");
        buf.append (       " specialType = " + GenUtils.inverseMapping (specialType, specialTypes) + "(" + specialType + ")\n");
        buf.append (tabs + "\tname = "     + name + "\n");
        buf.append (tabs + "\ttype = " + type + "\n");
	buf.append (options.format (indent + 1));
        return buf.toString();
    }
}
