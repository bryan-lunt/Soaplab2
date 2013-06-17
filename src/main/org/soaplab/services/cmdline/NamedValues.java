// NamedValues.java
//
// Created: April 2007
//
// Copyright 2007 Martin Senger
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

import java.util.List;
import java.util.ArrayList;

/**
 * A container for parameter name, parameter tag and its value, or
 * values. <p>
 *
 * The contents of this container is somewhere in between user inputs
 * and ready-to-use arguments for the command-line. It is used for job
 * classes that do not create the full command-line (such as Gowlab
 * jobs) but they still need an easy access to the user inputs (such
 * as replacing missing inputs with their default values, checking if
 * repetitivness (multiple values for the same parameter) is allowed,
 * etc.). <p>
 *
 * One container represents one parameter. The {@link #getId parameter
 * internal ID} should be always present (but see about an exception
 * below). The {@link #getTag tag} may be empty - this is the case
 * when no tag (qualifier) is defined in the parameter metadata, and
 * also if the parameter METHOD template is defined/used. The tag here
 * does not have any prefix (such as minus sign). <p>
 *
 * The {@link #getValues values} come from the user input data. There
 * may be multiple values in these cases: (a) a parameter can be
 * repetitive, or (b) it is a multi-choice list parameter. <p>
 *
 * The <em>boolean</em> parameter is present only if it has a true
 * value (from the user or by default). Its value is actually its tag
 * - that's why for these parameters the same value is both in the
 * {@link #getTag tag} and in the {@link #getValues values}. <p>

 * Be aware that for repetitive parameters, this container is
 * different from the way how it is expressed on a command-line. Here,
 * the tag is present only once. On the command-line, it is repeated
 * for each individual value (unless a parameter METHOD template
 * defines it otherwise). For example, the following parameter ( as
 * define din an ACD file):
 *
 *<pre>
 *string: str  [
 * information: "str: A ordinary string with a tag 'strtag'"
 * additional: "Y"
 * qualifier: strtag
 * default: "Ciao mundi"
 *]
 *</pre>
 *
 * and if a user sends two values ('a' and 'b'), will look on the
 * command line:
 *
 *<pre>
 *-strtag a -strtag b
 *</pre>
 *
 * but this container will contain:
 *
 *<pre>
 *Param ID                 Param Tag       Value(s)
 *-------------------------------------------------
 *str                      strtag          [a, b]
 *</pre>
 *
 * If service metadata specify a global METHOD template then this
 * container have empty parameter ID (because the role of individual
 * parameters is hidden/lost inside the global template). Also the
 * parameter tag is empty. Only values have contents. <p>
 *
 * Currently, the IO parameters do not use this structure. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: NamedValues.java,v 1.2 2007/04/19 22:17:05 marsenger Exp $
 */

public class NamedValues {

    private String paramId = "";
    private String paramTag = "";
    private List<String> values = new ArrayList<String>();

    /**************************************************************************
     * Constructor - a parameter ID is needed (in most cases).
     **************************************************************************/
    public NamedValues (String paramId) {
	this.paramId = paramId;
    }

    /**************************************************************************
     * Constructor - a container without parameter ID (used when a
     * service metadata have a global METHOD template).
     **************************************************************************/
    public NamedValues() {
    }


    /**************************************************************************
     * Return an ID of a parameter whose data this container keeps.
     **************************************************************************/
    public String getId() {
	return paramId;
    }


    /**************************************************************************
     * Set a tag (qualifier).
     **************************************************************************/
    public void setTag (String paramTag) {
	this.paramTag = paramTag;
    }

    /**************************************************************************
     * Return a tag (qualifier) for this parameter (without any
     * prefixes, such as minus sign or slash - even if such character
     * is define din the parameter metadata).
     **************************************************************************/
    public String getTag() {
	return paramTag;
    }


    /**************************************************************************
     * Return a list of data values representing user inputs. It can
     * return an empty list but never be a null.
     **************************************************************************/
    public List<String> getValues() {
	return values;
    }

    /**************************************************************************
     * Add a single value to the data representing user inputs.
     **************************************************************************/
    public void addValue (String value) {
	values.add (value);
    }

    /**************************************************************************
     * Set all values representing user inputs.
     **************************************************************************/
    public void setValues (List<String> values) {
	this.values = values;
    }

}
