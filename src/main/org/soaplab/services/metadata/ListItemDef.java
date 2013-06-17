// ListItemDef.java
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

import org.apache.commons.lang.StringUtils;

/**
 * A data structure defining an item of a static list. <p>
 *
 * @see ListDef
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: ListItemDef.java,v 1.2 2007/04/19 22:17:06 marsenger Exp $
 */
public class ListItemDef {

    //
    // List item types
    //
    public static final int REGULAR    = 0;
    public static final int NODE       = 1;
    public static final int SEPARATOR  = 2;

    // members
    public String value;
    public String shown_as;
    public int level = 0;
    public int type = REGULAR;

    /*********************************************************************
     * Constructor. Actually, no need... the members are public...
     *********************************************************************/
    public ListItemDef() {}

    /*********************************************************************
     * Convert string representation of a list item type to an integer.
     * Return -1 if unknown type is passed.
     ********************************************************************/
    private static Hashtable<String,Integer> itemTypes = new Hashtable<String,Integer>();
    static {
	itemTypes.put ("regular",   new Integer (REGULAR));
	itemTypes.put ("node",      new Integer (NODE));
	itemTypes.put ("separator", new Integer (SEPARATOR));
    }

    protected static int checkAndGetType (String type) {
	return GenUtils.checkAndGetStr2Int (type, itemTypes);
    }

    public String toString() { return format (0); }

    /*********************************************************************
     * As toString() but with indentations.
     *********************************************************************/
    public String format (int indent) {
        String tabs = GenUtils.indent (indent + level*indent);
	if (type == SEPARATOR)
	    return tabs + "---[separator]---";
	if (StringUtils.isBlank (shown_as))
	    return tabs + value;
	else
	    return tabs + shown_as + " (" + value + ")";
    }

}
