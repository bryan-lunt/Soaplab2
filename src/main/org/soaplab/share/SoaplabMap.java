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

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * A container for input and output data to/from Soaplab analysis
 * services. It is a replacement for a usual Java
 * Map&lt;String,Object>, allowing only certain value types. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: SoaplabMap.java,v 1.8 2011/04/06 14:12:46 mahmutuludag Exp $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SoaplabMap", propOrder = {
    "entries"
})
public class SoaplabMap {

    protected List<MapEntry> entries;

    /**************************************************************************
     * Gets the value of all entries.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the entries property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEntries().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MapEntry }
     *************************************************************************/
    public List<MapEntry> getEntries() {
        if (entries == null) {
            entries = new ArrayList<MapEntry>();
        }
        return this.entries;
    }

    /**************************************************************************
     *
     *************************************************************************/
    public Object get (String key) {
	for (Iterator<MapEntry> it = getEntries().iterator(); it.hasNext(); ) {
	    MapEntry map = it.next();
	    if (map.getKey().equals (key))
		return map.getObjectValue();
	}
	return null;
    }

    
    public void put(String key, Object value)
    {
        
        getEntries().add(new MapEntry(key, value));
        
        return;
    }
    
    /**************************************************************************
     *
     * @throws IllegalArgumentException if the input 'map' contains a
     * value which is not one of the four types allowed by the SoaplabMap
     *************************************************************************/
    public static SoaplabMap fromMap (Map<String,Object> map) {

	// output
	SoaplabMap sm = new SoaplabMap();
	List<MapEntry> smEntries = sm.getEntries();

	// input
	Iterator<Map.Entry<String,Object>> it = map.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry<String,Object> entry = it.next();
	    smEntries.add (new MapEntry (entry.getKey(), entry.getValue()));
	}
	return sm;
    }

    public static SoaplabMap fromStringMap (Map<String,String> map) {

	// output
	SoaplabMap sm = new SoaplabMap();
	List<MapEntry> smEntries = sm.getEntries();

	// input
	Iterator<Map.Entry<String,String>> it = map.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry<String,String> entry = it.next();
	    smEntries.add (new MapEntry (entry.getKey(), entry.getValue()));
	}
	return sm;
    }

    public static Map<String,String> toStringMap (SoaplabMap map) {
	Map<String,String> result = new HashMap<String,String>();
	for (MapEntry entry: map.getEntries()) {
	    try {
		result.put (entry.getKey(), (String)entry.getObjectValue());
	    } catch (java.lang.ClassCastException e) {
		throw new IllegalArgumentException ("Expected type String, got type " +
						    entry.getObjectValue().getClass().getName());
	    }
	}
	return result;
    }
    @SuppressWarnings("unchecked")
	public static Map<String,String>[] toStringMaps (SoaplabMap[] maps) {
	synchronized (maps) {
	    Map<String,String>[] result = new HashMap[maps.length];
	    for (int i = 0; i < maps.length; i++) {
		result[i] = SoaplabMap.toStringMap (maps [i]);
	    }
	    return result;
	}
    }





    public static SoaplabMap[] fromMaps (Map<String,Object>[] maps) {
	synchronized (maps) {
	    SoaplabMap[] result = new SoaplabMap [maps.length];
	    for (int i = 0; i < maps.length; i++) {
		result[i] = SoaplabMap.fromMap (maps [i]);
	    }
	    return result;
	}
    }

    /**************************************************************************
     *
     *************************************************************************/
    @SuppressWarnings("unchecked")
    public static Map<String,Object> toMap (SoaplabMap map) {
	Map<String,Object> result = new HashMap<String,Object>();
	for (MapEntry entry: map.getEntries()) {
	    Object value = entry.getObjectValue();
	    if (value instanceof ArrayList) {
		ArrayList listValue = (ArrayList)value;
		int listSize = listValue.size();
		if (listSize == 0) {
		    result.put (entry.getKey(), listValue.toArray());
		} else {
		    Object listElement = listValue.get (0);
		    if (listElement instanceof String) {
			result.put (entry.getKey(), listValue.toArray (new String[ listSize ]));
		    } else if (listElement instanceof byte[]) {
			result.put (entry.getKey(), listValue.toArray (new byte[ listSize ][]));
		    } else {
			result.put (entry.getKey(), listValue.toArray ());
		    }
		}
	    } else {
		result.put (entry.getKey(), value);
	    }
	}
	return result;
    }

    @SuppressWarnings("unchecked")
    public static Map<String,Object>[] toMaps (SoaplabMap[] maps) {
	synchronized (maps) {
	    Map<String,Object>[] result = new HashMap[maps.length];
	    for (int i = 0; i < maps.length; i++) {
		result[i] = SoaplabMap.toMap (maps [i]);
	    }
	    return result;
	}
    }


}
