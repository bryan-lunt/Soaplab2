// PropertiesBag.java
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
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * A simple extension of java.util.Hashtable bringing more methods for
 * checking and retrieving properties. It is also the place where we
 * can later add case-insensitive handling of keys. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: PropertiesBag.java,v 1.3 2007/04/19 22:17:06 marsenger Exp $
 */
public class PropertiesBag
    extends Hashtable<String,Object> {

    /**************************************************************************
     * An empty constructor.
     **************************************************************************/
    public PropertiesBag() {
    }

    /**************************************************************************
     * Create a bag initialized with 'props'.
     **************************************************************************/
//     public PropertiesBag (Object[] props) {
    public PropertiesBag (String[] props) {
        for (int i = 0; i < props.length - 1; i += 2)
            put (props[i], props[i+1]);
    }

    /**************************************************************************
     * A convenient constructor for creating a bag with exactly one property.
     **************************************************************************/
    public PropertiesBag (String propName, String propValue) {
        put (propName, propValue);
    }

    /**************************************************************************
     * Add new properties to this bag.
     **************************************************************************/
    public void addProperties (PropertiesBag options) {
        for (Enumeration en = options.keys(); en.hasMoreElements(); ) {
            String key = (String)en.nextElement();
            this.put (key, options.get (key));
	}
    }

    /**************************************************************************
     * Returns true if an option defined by a 'key' is present and has
     * a true value. Used for elements of type String and Boolean.
     **************************************************************************/
    public boolean is (String key) {
        if (containsKey (key)) {
            Object value = get (key);
            if (value instanceof Boolean)
                return ((Boolean)value).booleanValue();
            return BooleanUtils.toBoolean ((String)value);
	}
	return false;
    }

    /**************************************************************************
     * Returns true if an option defined by a 'key' is present and has
     * a true value. Used for elements of type String and Boolean.
     * The search key is extended by 'serNo' at its end (but only if
     * serNo > 0).  If the extended key was not found, a normal
     * (plain) key is tried. <p>
     *
     * For example, you can look for 'debug1' by calling is ('debug', 1).
     **************************************************************************/
    public boolean is (String key, int serNo) {
	if (serNo <= 0) return is (key);
        if (containsKey (key + serNo)) return is (key + serNo);
	return is (key);
    }

    /**************************************************************************
     * Returns true if an option defined by a 'key' is present and has
     * a non-empty value. It makes mostly sense for type String.
     **************************************************************************/
    public boolean exists (String key) {
        if (containsKey (key)) {
            Object obj = get (key);
            return (obj instanceof String && StringUtils.isNotBlank ((String)obj));
	}
	return false;
    }

    /**************************************************************************
     * Returns true if an option indicated by 'key' is found within
     * this bag AND is equal to a given value. Used for element of
     * type String.
     **************************************************************************/
    public boolean found (String key, String value) {
        if (containsKey (key)) {
            Object obj = get (key);
            if (obj instanceof String && value.equalsIgnoreCase ((String)obj))
                return true;
	}
	return false;
    }

    /**************************************************************************
     * Return a string value of the option indicated by 'key'. Return
     * an empty string if 'key' was not found. Used for element of
     * type Integer and String.
     **************************************************************************/
    public String getStr (String key) {
        Object obj = get (key);
        if (obj == null) return "";
        if (obj instanceof String) return (String)obj;
        if (obj instanceof Integer) return ((Integer)obj).toString();
        if (obj instanceof Boolean) return ((Boolean)obj).toString();
        return obj.toString();
    }

    /**************************************************************************
     * Return an integer value of the option indicated by 'key'.
     * Return -1 if 'key' was not found.  Return 0 if the element
     * contains non-numeric value. Used for element of type Integer
     * and String.
     **************************************************************************/
    public int getInt (String key) {
        Object obj = get (key);
        if (obj == null) return -1;
        if (obj instanceof Integer) return ((Integer)obj).intValue();
        if (obj instanceof String) return NumberUtils.toInt ((String)obj);
        return NumberUtils.toInt (obj.toString());
    }

//      public String toString() { return format (0); }

    /**************************************************************************
     * As toString() but with indentations.
     **************************************************************************/
    public String format (int indent) {
	String tabs = GenUtils.indent (indent);
	StringBuilder result = new StringBuilder();
        for (Enumeration en = keys(); en.hasMoreElements(); ) {
            String key = (String)en.nextElement();
	    result.append (tabs + key + " = " + formatElement (get (key)) + "\n");
	}
	return result.toString();
    }

    protected String formatElement (Object obj) {
        if (obj instanceof String) return (String)obj;
        else if (obj instanceof String[]) {
            StringBuilder buf = new StringBuilder();
            String[] strs = (String[])obj;
            for (int i = 0; i < strs.length; i++) {
                buf.append (strs[i]);
                if (i < strs.length - 1) buf.append (", ");
	    }
            return buf.toString();
//          } else if (obj instanceof Boolean) {
//              return obj.toString() + " [Boolean]";
	} else {
            return obj.toString();
	}
    }

}
