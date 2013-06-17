// RepeatableDef.java
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
 * Structure defining how a parameter can be repeated.
 * Some useful combinations are:
 * <pre>
 * minRep  maxRep
 * 0       0      </TT>item is not repeatable (default)<TT>
 * 0      -1      </TT>repeatable infinitely<TT>
 * m      -1      </TT>repeat min m-times<TT>
 * 0       n      </TT>repeat max n-times<TT>
 * 1       n      </TT>repeat max n-times<TT>
 * m       n      </TT>repeat min m-times and max n-times<TT>
 * </pre>
 *
 * Additional options (in 'options'), some of them are:
 * <pre>
 * KEY           TYPE    CONTENTS
 * repeatprompt  string  </TT>prompt describing what to be repeated (default: <em>More...</em>)<TT>
 * </pre>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: RepeatableDef.java,v 1.2 2007/04/19 22:17:06 marsenger Exp $
 */
public class RepeatableDef {

    //
    // Constants for repeatables
    //
    public static final String REPEAT_ERROR = "repeat_error";

    /** Minimum number of repetitions. */
    public int minRep = 0;
    /** Maximum number of repetitions. */
    public int maxRep = 0;
    public PropertiesBag options = new PropertiesBag();

    public RepeatableDef() {}

    /**************************************************************************
     * Convenient methods for accessing 'options'
     **************************************************************************/
    public String get (String key)     { return options.getStr (key); }

    public String toString() { return format (0); }

    /**************************************************************************
     * As toString() but with indentations.
     **************************************************************************/
    public String format (int indent) {
	String tabs = GenUtils.indent (indent);
        StringBuilder buf = new StringBuilder();
        buf.append (tabs + "[Repeatable]");
        buf.append (       " min=" + minRep);
        buf.append (       " max=" + maxRep + "\n");
        buf.append (options.format (indent + 1));
        return buf.toString();
    }

}
