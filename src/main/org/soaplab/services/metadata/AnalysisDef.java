// AnalysisDef.java
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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * A structure with general description of an analysis (details about
 * all analysis input/output data are not here, but in {@link ParamDef}
 * structures). <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: AnalysisDef.java,v 1.3 2007/10/23 08:20:53 marsenger Exp $
 */
public class AnalysisDef {

    // members
    public String type;
    public String category;
    public String appName;
    public String module;
    public PropertiesBag options = new PropertiesBag();

    // came from EventDef and ActionDef after their deprecation
    public CondDef[] conds;        // inter-parameter verification rules
    public String method = "";     // rules how to create an analysis input, or
                                   // type of monitoring for 'monitor' action
    public String launcher = "";   // how to execute (wrapper around an execution)
    public String file = "";       // what to execute (program...)

    public AnalysisDef() {}

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

    /**************************************************************************
     *
     **************************************************************************/
    public String toString() {
 	return ToStringBuilder.reflectionToString (this,
						   ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
