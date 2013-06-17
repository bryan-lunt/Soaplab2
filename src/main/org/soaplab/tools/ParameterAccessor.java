// ParameterAccessor.java
//
// Created: March 1998, Refreshed: November 2006
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

/** 
 * An interface used primarily by the class {@link Substitutor}. It
 * defines how to get access to the parameter values and to the
 * parameter name. See the description of "parameter" in {@link
 * Substitutor}. <p>
 *
 * This interface should be implemented by objects using instances of
 * the Substitutor class. A Substitutor instance gets an access to the
 * parameter values using an implementation of this interface. <p>
 *
 * @see Substitutor
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: ParameterAccessor.java,v 1.1 2006/11/24 01:30:39 marsenger Exp $
 */
public interface ParameterAccessor {

    /******************************************************************************
     * 'id' in all methods identifies parameter whose values are asked for.
     ******************************************************************************/

    /******************************************************************************
     * Return an array of values for parameter 'id'. <p>
     *
     * @param id identifies parameter whose values are being asked for
     ******************************************************************************/
    String[] getValues (String id);

    /******************************************************************************
     * Return a value element indexed by 'idx'. Or an empty string if
     * the given index does not exist. <p>
     *
     * @param id identifies parameter whose value is being asked for
     * @param idx an index into the array of parameter values
     ******************************************************************************/
    String getValue (String id, int idx);

    /******************************************************************************
     * Return a parameter name. <P>
     *
     * @param id identifies parameter whose name is being asked for
     ******************************************************************************/
    String getQualifier (String id);

    /******************************************************************************
     * Return length of the array of all values of a given
     * parameter. <P>
     *
     * @param id identifies parameter whose value count is being asked for
     ******************************************************************************/
    int getCount (String id);

    /******************************************************************************
     * Return length of the array of all values of the current
     * parameter.  What <em>current</em> means is defined by the
     * implementation.
     ******************************************************************************/
    int getCount ();

}
