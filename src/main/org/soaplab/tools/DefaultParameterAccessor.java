// DefaultParameterAccessor.java
//
// Created: March 2000, Refreshed: November 2006
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

package org.soaplab.tools;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Properties;

/** 
 * A default implementation of ParameterAccessor interface used by
 * {@link Substitutor} class. <P>
 *
 * Instances of this class give access to the values of parameters, as
 * requested by the {@link Substitutor} engine (requests are done
 * through the interface {@link ParameterAccessor}). <P>
 *
 * Each instance can deal with one or more parameters. Each parameter
 * is represented by a list of string values (each element for one
 * repetition), by a unique ID, and by a parameter name (which is
 * called also tag or qualifier). <P>
 *
 * The parameter values can be given either in constructor or, if we
 * deal with more parameters, by method add(). <p>
 *
 * @see Substitutor
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: DefaultParameterAccessor.java,v 1.1 2006/11/24 01:30:39 marsenger Exp $
 */
public class DefaultParameterAccessor
    implements ParameterAccessor {

    Hashtable<String,String[]> remembered = new Hashtable<String,String[]>();
    Properties tags = new Properties();

    /******************************************************************************
     * No-arg constructor. Use method {@link #add} to define
     * parameters that should be accessed by this instance.
     ******************************************************************************/
    public DefaultParameterAccessor() {
    }

    /******************************************************************************
     * A constructor adding one parameter. Others may be added using
     * the {@link #add} method. <p>
     *
     * @param id identification of this parameter
     * @param tag name of this parameter
     * @param values values of this parameter
     ******************************************************************************/
    public DefaultParameterAccessor (String id, String tag, String[] values) {
        add (id, tag, values);
    }

    /******************************************************************************
     * Add a parameter. <P>
     *
     * @param id identification of this parameter
     * @param tag name of this parameter
     * @param values values of this parameter
     ******************************************************************************/
    public void add (String id, String tag, String[] values) {
        remembered.put (id, values);
        tags.put (id, tag);
    }


    /******************************************************************************
     *
     * Methods implementing ParameterAccessor interface
     *
     ******************************************************************************/


    /******************************************************************************
     *
     ******************************************************************************/
    public String[] getValues (String id) {
	String[] args = remembered.get (id);
	if (args == null || args.length == 0)
	    return new String[] {};
	return args;
    }

    /******************************************************************************
     *
     ******************************************************************************/
    public String getValue (String id, int idx) {
	String[] args = getValues (id);
	return (args.length > idx ? args [idx] : "");
    }

    /******************************************************************************
     *
     ******************************************************************************/
    public int getCount (String id) {
	return (getValues (id)).length;
    }

    /******************************************************************************
     *
     ******************************************************************************/
    public int getCount () {
	// because we do not have any 'id' let's return the count of
	// the id containing the greatest number of values
	int maxCount = 0;
	for (Enumeration<String[]> e = remembered.elements(); e.hasMoreElements(); )
	    maxCount = Math.max (maxCount, (e.nextElement().length));
	return maxCount;
    }

    /******************************************************************************
     *
     ******************************************************************************/
    public String getQualifier (String id) {
        String tag = tags.getProperty (id);
        if (tag == null) return "";
        return tag;
    }

}
