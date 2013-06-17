// StdParamDef.java
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


/**
 * A data structure defining a standard parameter. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: StdParamDef.java,v 1.4 2007/10/17 15:49:38 mahmutuludag Exp $
 */
public class StdParamDef
    extends ParamDef {

    // members
    public RepeatableDef repeat = new RepeatableDef();
    public ListDef[] lists;

    public StdParamDef() {}

    /**************************************************************************
     * Return true if this parameter is allowed to be repeated.
     **************************************************************************/
    public boolean canBeRepeatable() {
	return (repeat != null &&
		(repeat.minRep != 0 || repeat.maxRep != 0));
    }

     public String toString() { return format (0); }

    /**************************************************************************
     * As toString() but with indentations.
     **************************************************************************/
    public String format (int indent) {
	    StringBuilder buf = new StringBuilder();
        buf.append (super.format (indent));
        if (canBeRepeatable())
	    buf.append (repeat.format (indent + 1));
        if (lists != null)
	    for (int i = 0; i < lists.length; i++)
		buf.append (lists[i].format (indent + 1));
        return buf.toString();
    }
}
