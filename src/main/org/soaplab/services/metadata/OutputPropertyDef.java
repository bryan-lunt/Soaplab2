// OutputPropertyDef.java
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

/**
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: OutputPropertyDef.java,v 1.2 2007/04/19 22:17:06 marsenger Exp $
 */
public class OutputPropertyDef {

    public String name;
    public String type;

    /**************************************************************************
     * Constructors
     **************************************************************************/
    public OutputPropertyDef() {}

    public OutputPropertyDef (String name, String type) {
	this.name = name;
	this.type = type;
    }

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
        buf.append (tabs + "[OutputPropertyDef]\n");
        buf.append (tabs + "\tname = "      + name      + "\n");
        buf.append (tabs + "\ttype = "      + type      + "\n");
        return buf.toString();
    }
}
