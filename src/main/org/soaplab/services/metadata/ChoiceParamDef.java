// ChoiceParamDef.java
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
 * A data structure defining a 'boolean' or 'radio-group'
 * parameter. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: ChoiceParamDef.java,v 1.2 2007/04/19 22:17:06 marsenger Exp $
 */
public class ChoiceParamDef
    extends ParamDef {

/**
 * Types of radiogroups (grouping together several boolean options).
 * It says how many options can be visible in the same time.
 */
    public static final int JUST_ONE     = 0;  // for ChoiceParamDef
    public static final int ZERO_OR_ONE  = 1;  // for ChoiceParamDef

    public static final int ZERO_OR_MORE = 2;  // for ChoiceListParamDef
    public static final int ONE_OR_MORE  = 3;  // for ChoiceListParamDef

    // members
    public int choiceType = JUST_ONE;
    public ParamDef[] bools = new ParamDef[0];

    public ChoiceParamDef() {}

    /*********************************************************************
     * Convert string representation of a choice type to an integer.
     * Return -1 if unknown type is passed.
     ********************************************************************/
    private static Hashtable<String,Integer> choiceTypes = new Hashtable<String,Integer>();
    static {
        choiceTypes.put ("just_one",     new Integer (JUST_ONE));
        choiceTypes.put ("zero_or_one",  new Integer (ZERO_OR_ONE));
        choiceTypes.put ("zero_or_more", new Integer (ZERO_OR_MORE));
        choiceTypes.put ("one_or_more",  new Integer (ONE_OR_MORE));
    }

    protected static int checkAndGetType (String choiceType) {
        return GenUtils.checkAndGetStr2Int (choiceType, choiceTypes);
    }

    public String toString() { return format (0); }

    /**************************************************************************
     * As toString() with but indentations.
     **************************************************************************/
    public String format (int indent) {
	String tabs = GenUtils.indent (indent);
        StringBuilder buf = new StringBuilder();
        buf.append (super.format (indent));
        buf.append (tabs + "\tchoiceType = " + GenUtils.inverseMapping (choiceType, choiceTypes) + "(" + choiceType + ")\n");
        if (bools != null)
	    for (int i = 0; i < bools.length; i++)
		buf.append (bools[i].format (indent + 1));
        return buf.toString();
    }
}
