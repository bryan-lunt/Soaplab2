// SoaplabMap.java
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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * A container for a key-value pair used in {@link SoaplabMap
 * SoaplabMap}. <p>
 * 
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: MapEntry.java,v 1.4 2007/10/06 18:32:14 marsenger Exp $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MapEntry", propOrder = {
    "key",
    "value"
})
public class MapEntry {

    @XmlElement(required = true)
    protected String key;
    @XmlElement(required = true)
    protected Value value;


    /**************************************************************************
     *
     *************************************************************************/
    public MapEntry() {
    }

    /**************************************************************************
     *
     *************************************************************************/
    public MapEntry (String key, Object value) {
	setKey (key);
	setValue (new Value (value));
    }

    /**************************************************************************
     *
     *************************************************************************/
    public Object getObjectValue() {
	return getValue().getValue();
    }

    /**
     * Gets the value of the key property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the value of the key property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKey(String value) {
        this.key = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link Value }
     *     
     */
    public Value getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link Value }
     *     
     */
    public void setValue(Value value) {
        this.value = value;
    }

}
