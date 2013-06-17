// InputPropertyDef.java
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
import org.apache.commons.lang.ArrayUtils;

/**
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: InputPropertyDef.java,v 1.3 2007/10/22 00:31:17 marsenger Exp $
 */
public class InputPropertyDef {

    public String name;
    public String type;
    public boolean mandatory;
    public String defaultValue;
    public String[] possibleValues;

    /**************************************************************************
     * Constructor
     **************************************************************************/
    public InputPropertyDef() {}

    /**************************************************************************
     *
     **************************************************************************/
    public String toString() { return format (0); }

    /**************************************************************************
     * As toString() but with indentations.
     **************************************************************************/
    public String format (int indent) {
	String tabs = GenUtils.indent (indent);
        StringBuilder buf = new StringBuilder();
        buf.append (tabs + "[InputPropertyDef]\n");
        buf.append (tabs + "\tname = "      + name      + "\n");
        buf.append (tabs + "\ttype = "      + type      + "\n");
        buf.append (tabs + "\tmandatory = " + mandatory + "\n");
        buf.append (tabs + "\tdefault value = "   + defaultValue   + "\n");
        buf.append (tabs + "\tallowed values = "   + ArrayUtils.toString (possibleValues) + "\n");
        return buf.toString();
    }
}
