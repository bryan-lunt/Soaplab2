// ListDef.java
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
 * A data structure defining a static list of string values, possibly
 * hierarchically nested (like a 'menu' structure'). The values are
 * used as the 'allowed' values for the parameter where his structure
 * is used. <p>
 *
 * The list is "static" which means that there are no methods for
 * moving or removing values once they are here. <p>
 *
 *<pre>
 * Explanation of the individual members
 * (remember that some features are useful only for clients and some only
 *  for servers, but they all live here together)
 *
 * name
 *    the list name is usually used as a title for a client-side menu
 *    displaying the list
 * type
 *    FULL  ... values of such list cover (express) the whole value of
 *              the related parameter (this is the usual list type)
 *    SLICE ... values of such list cover only part of a parameter
 *              value (the parts are called 'slices')
 *    An example:
 *       A parameter value should be like this (a date): 1999/12/31.
 *       This parameter can have attached a FULL list with values
 *       such as: 1999/12/31
 *                2000/12/31
 *                2001/12/31,
 *       or it can have attached one or more SLICE lists, such as:
 *                12
 *                11
 *                ...  (a list covering months)
 *       and
 *                1999
 *                2000
 *                ...  (a list covering available years)
 *
 *       (btw, a parameter can have both FULL and SLICE lists).
 * slice
 *    a one-based serial number of a slice covered by this list
 *    (it makes sense only for list of type SLICE)
 * items (defined in ListItemDef.java)
 *    An array of list values. They are in the order how they should
 *    be presented by client - for nested items (which are anyway stored
 *    here flatted but with attribute) the order is top-to-down and
 *    left-to-right - see example in 'level' below.
 *
 *    Each item has this attributes:
 *       value     ... the real value
 *       shown_as  ... a string used on the client-side to display the value
 *                     (an example: value = 12
 *                                  shown_as = December)
 *       type      ... an item can be a real one (REGULAR),
 *                     or a node in a 'menu' (NODE),
 *                     or just a menu separator (SEPARATOR)
 *       level     ... zero-based index of item nesting (defaul is 0)
 *                     An example: The following (client-side) menu:
 *                        Europe
 *                           UK
 *                              Scotland
 *                           France
 *                              Provence
 *                        America
 *                           North
 *                              USA
 *                              Canada
 *                           South
 *                        Asia
 *                           Japan
 *
 *                     would be flattened with the following 'levels':
 *
 *                        0 Europe
 *                        1 UK
 *                        2 Scotland
 *                        1 France
 *                        2 Provence
 *                        0 America
 *                        1 North
 *                        2 USA
 *                        2 Canada
 *                        1 South
 *                        0 Asia
 *                        1 Japan
 *
 *</pre>
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: ListDef.java,v 1.2 2007/04/19 22:17:06 marsenger Exp $
 */
public class ListDef {

    //
    // List types
    //
    public static final int FULL       = 0;
    public static final int SLICE      = 1;

    // members (explained in details above)
    public String      name = "";
    public int         type = FULL;
    public int         slice = 1;
    public ListItemDef items[];

    /**************************************************************************
     * Constructor. Actually, no need... the members are public...
     **************************************************************************/
    public ListDef() {}

    /*********************************************************************
     * Convert string representation of a list type to an integer.
     * Return -1 if unknown type is passed.
     ********************************************************************/
    private static Hashtable<String,Integer> listTypes = new Hashtable<String,Integer>();
    static {
        listTypes.put ("full",  new Integer (FULL));
        listTypes.put ("slice", new Integer (SLICE));
    }

    protected static int checkAndGetType (String type) {
        return GenUtils.checkAndGetStr2Int (type, listTypes);
    }

    public String toString() { return format (0); }

    /**************************************************************************
     * As toString() but with indentations.
     **************************************************************************/
    public String format (int indent) {
	String tabs = GenUtils.indent (indent);
        StringBuilder buf = new StringBuilder();
        String sliceStr = "";
	if (type == SLICE && slice > 0)
	    sliceStr = " (" + slice + ")";
        buf.append (tabs + "[ListDef]");
        buf.append (       " name=" + name);
        buf.append (       " type=" + GenUtils.inverseMapping (type, listTypes) + sliceStr + "\n");
	if (items != null)
	    for (int i = 0; i < items.length; i++)
		buf.append (items[i].format (indent + 1) + "\n");
        return buf.toString();
    }
}
