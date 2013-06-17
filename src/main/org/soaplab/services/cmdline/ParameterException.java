// ParameterException.java
//
// Created: November 2006
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

package org.soaplab.services.cmdline;


/** 
 * It indicates an invalid user input sent to a service. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: ParameterException.java,v 1.2 2007/10/22 00:31:17 marsenger Exp $
 */
public class ParameterException
    extends Exception {

    private String paramName;

    /******************************************************************************
     * A constructor specifying a reason for this exception. <p>
     *
     * @param name of the invalid parameter
     * @param s message/reason
     ******************************************************************************/
    public ParameterException (String paramName, String s) {
	super ("[" + paramName + "] " + s);
	this.paramName = paramName;
    }

    /******************************************************************************
     * Get the name of an invalid parameter.
     ******************************************************************************/
    public String getParameterName() {
	return paramName;
    }

}
