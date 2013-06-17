// Value.java
//
// Created: May 2007
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

package org.soaplab.share;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * A container for a value used in {@link MapEntry MapEntry} (which is
 * further used in {@link SoaplabMap SoaplabMap}). It behaves like a
 * union: it keeps a value just in one of its member fields. If a new
 * value is set for a field, the remaining fields are nullified. <p>
 *
 * Note that for the fields represented by <tt>List</tt>s, there is no
 * way to keep an empty list as a real value (TBD?). <p>
 * 
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: Value.java,v 1.5 2009/06/19 12:32:10 mahmutuludag Exp $
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Value", propOrder = {
    "singleString",
    "stringArray",
    "singleBinary",
    "binaryArray"
}) public class Value {

    protected String singleString;
    protected List<String> stringArray;
    protected byte[] singleBinary;
    protected List<byte[]> binaryArray;

    /**************************************************************************
     *
     *************************************************************************/
    public Value() {
    }

    /**************************************************************************
     *
     * @throws IllegalArgumentException if the input 'value' is not of
     * the types allowed for this class
     *************************************************************************/
    public Value (Object value) {
    if (value instanceof Integer) {
        setSingleString (((Integer)value).toString());
    } else  if (value instanceof Float) {
        setSingleString (((Float)value).toString());
    } else  if (value instanceof Double) {
        setSingleString (((Double)value).toString());
    } else  if (value instanceof Boolean) {
        setSingleString (((Boolean)value).toString());
    } else	if (value instanceof String) {
	    setSingleString ((String)value);
	} else if (value instanceof byte[]) {
	    setSingleBinary ((byte[])value);
	} else {
	    Class valueClass = value.getClass();
	    if (! valueClass.isArray())
		throw new IllegalArgumentException
		    ("Conversion to SoaplabMap failed: Found an unsupported type '" +
		     valueClass.getName() + "'.");
	    Class componentClass = valueClass.getComponentType();
	    if (componentClass.equals (String.class)) {
		for (String element: (String[])value)
		    getStringArray().add (element);
	    } else if (componentClass.equals (byte[].class)) {
		for (byte[] element: (byte[][])value)
		    getBinaryArray().add (element);
	    } else {
		for (Object element: (Object[])value)
		    getStringArray().add (element.toString());
	    }
	}
    }

    /**************************************************************************
     * Get a value kept in this instance. It is taken from the
     * currently filled field.
     *************************************************************************/
    public Object getValue() {
	if (singleString != null)
	    return singleString;
	if (singleBinary != null)
	    return singleBinary;
	if (stringArray != null)
	    return stringArray;
	if (binaryArray != null)
	    return binaryArray;
	// TBD: not sure about this - but I do not want to have a
	// completely null Value (it would cause problems when
	// converting it to a java.util.Map
	return "";
    }

    /**************************************************************************
     *
     *************************************************************************/
    public String getSingleString() {
        return singleString;
    }

    /**************************************************************************
     *
     *************************************************************************/
    public void setSingleString (String value) {
        this.singleString = value;
    }

    /**
     * Gets the value of the stringArray property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the stringArray property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStringArray().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getStringArray() {
        if (stringArray == null) {
            stringArray = new ArrayList<String>();
        }
        return this.stringArray;
    }

    /**
     * Gets the value of the singleBinary property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getSingleBinary() {
        return singleBinary;
    }

    /**
     * Sets the value of the singleBinary property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setSingleBinary(byte[] value) {
        this.singleBinary = ((byte[]) value);
    }

    /**
     * Gets the value of the binaryArray property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the binaryArray property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBinaryArray().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * byte[]
     * 
     */
    public List<byte[]> getBinaryArray() {
        if (binaryArray == null) {
            binaryArray = new ArrayList<byte[]>();
        }
        return this.binaryArray;
    }

}
