// GenUtils.java
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

package org.soaplab.services;

import java.util.Hashtable;
import java.util.Enumeration;

import org.apache.commons.io.IOUtils;
import org.apache.commons.discovery.tools.Service;

import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;


/**
 * Few utilities. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: GenUtils.java,v 1.4 2007/10/17 15:42:59 mahmutuludag Exp $
 */
public abstract class GenUtils {

    static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /*********************************************************************
     * Convert string representation to an integer according mapping
     * provided in a hashtable 'mapping' (elements of the hastable are
     * expected to be of type Integer). <p>
     *
     * @return -1 if an empty or unknown string is passed.
     ********************************************************************/
    public static int checkAndGetStr2Int (String str,
					  Hashtable<String,Integer> mapping) {
        if (str == null) return -1;
	Integer i = mapping.get (str);
        if (i == null) return -1;
        return i.intValue();
    }

    /*********************************************************************
     * Convert given integer into a string using 'mapping' that
     * contains given integer as a value (of type Integer) whose
     * corresponding key is returned. <p>
     *
     * @return "unknown" if 'value' is not found.
     ********************************************************************/
    public static String inverseMapping (int value, Hashtable mapping) {
        String key;
        for (Enumeration en = mapping.keys(); en.hasMoreElements(); ) {
            key = (String)en.nextElement();
            if ( ((Integer)mapping.get (key)).intValue() == value)
		return key;
	}
	return "unknown";
    }

    private static final String tabs = "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t";
    private static final int tabsLen = tabs.length();

    /*********************************************************************
     * Return some indentation.
     ********************************************************************/
    public static String indent (int indent) {
        if (indent < 1 || indent > tabsLen) return "";
	return tabs.substring (0, indent);
    }

    /*************************************************************************
     * Find the resource with the given 'filename', read it and return
     * it. A resource is some data (images, audio, text, etc) that can
     * be accessed by class code in a way that is independent of the
     * location of the code, typically such resource file sits
     * anywhere on the CLASSPATH. <p>
     *
     * @param filename of a resource is a '/'-separated path name that
     * identifies the resource
     *
     * @param resourceOwner is a class whose class loader is used
     * to find and get the resource; typically one would put here
     * "this.getClass()" when calling this method
     *
     * @return contents of the resource
     *
     * @throws IOException if resource was found but an error occured
     * during its reading (IO problem, memory problem etc.)
     *
     * @throws FileNotFoundException if resource could not be found
     *************************************************************************/
    public static String readResource (String filename, Class resourceOwner)
	throws IOException {

	// load it as a resource of the given class
	InputStream ins =
	    resourceOwner.getClassLoader().getResourceAsStream
	    (filename);

	if (ins == null) {
	    // ...or extend the path by the package name of the given
	    // class
	    String className = resourceOwner.getName();
	    int pkgEndIndex = className.lastIndexOf ('.');
	    if (pkgEndIndex > 0) {
		String packageName = className.substring (0, pkgEndIndex);

		// TBD: platform-dependent file separator?
		String newPath = packageName.replace ('.', '/') + "/" + filename;
		ins = resourceOwner.getClassLoader().getResourceAsStream (newPath);
	    }
	}
	if (ins == null)
	    throw new FileNotFoundException (filename);

	try {
	    return IOUtils.toString (ins);
	} catch (Error e) {     // be prepare for "out-of-memory" error
	    throw new IOException ("Problem when reading resource '" + filename +
				   "'. " + e.toString());
	} finally {
	    try {
		if (ins != null)
		    ins.close();
	    } catch (IOException e) {
	    }
	}
    }

    /*********************************************************************
     * Return the location of the specified resource by searching the
     * current directory, user home directory, the current classpath
     * and the system classpath. <p>
     *
     * The code was inspired by Apache Commons class
     * <tt>ConfigurationUtils</tt> (authors Herve Quiroz, Oliver
     * Heger, and Emmanuel Bourg). <p>
     *
     * @param name the name of the resource - usually a file name
     * (absolute or relative)
     *
     * @return a URL of the resource location
     *
     * @throw FileNotFoundException if the resource cannot be found;
     * or if the 'name' is null or otherwise invalid
     ********************************************************************/
    public static URL locateFile (String name)
	throws FileNotFoundException {

        if (name == null)
            throw new FileNotFoundException ("Null file name.");

        // attempt to create a URL directly
        try {
	    return new URL (name);
        } catch (IOException e) {
	    // let's make another attempt
        }

        // attempt to load from an absolute path, or from the current directory
	File file = new File (name);
	if (file.isAbsolute() || file.exists()) {
	    try {
		return file.toURI().toURL();
	    } catch (IOException e) {
		throw new FileNotFoundException ("Malformed path: " + name);
	    }
	}

        // attempt to load from the user home directory
	try {
            StringBuilder fName = new StringBuilder();
            fName.append (System.getProperty ("user.home"));
	    fName.append (File.separator);
	    fName.append (name);
            file = new File (fName.toString());
	    if (file.exists())
		return file.toURI().toURL();
	} catch (IOException e) {
	    // let's make another attempt
	}

        // attempt to load from the context classpath
	ClassLoader loader = Thread.currentThread().getContextClassLoader();
	URL url = loader.getResource (name);
	if (url != null)
	    return url;

        // attempt to load from the system classpath
	url = ClassLoader.getSystemResource (name);
	if (url != null)
	    return url;

	throw new FileNotFoundException (name);
    }


    /*************************************************************************
     * strDate can be a number - in that case is treated as a number
     * of millisecond and is always returned as negative number back; <p>
     *
     * a number can have a unit as its last character: 's', 'm', 'h',
     * 'd', 'w', or 'M'; <p>
     *
     * or 'strDate' can be of the given date format (or a default one
     * if 'dateFormat' is null); <p>
     *
     * if 'strDate' is null, or if any problems occurs, a zero is
     * returned; <p>
     *************************************************************************/
    static public long str2Date (String strDate, String dateFormat) {

	// an empty date
	if (strDate == null)
	    return 0;

	// a relative date can have units (at the end)
	boolean inSeconds = false, inMinutes = false, inHours = false, inDays = false, inWeeks = false, inMonths = false;
	if (strDate.endsWith ("s")) {
	    inSeconds = true;
	    strDate = strDate.substring (0, strDate.length() - 1);
	} else if (strDate.endsWith ("m")) {
	    inMinutes = true;
	    strDate = strDate.substring (0, strDate.length() - 1);
	} else if (strDate.endsWith ("h")) {
	    inHours = true;
	    strDate = strDate.substring (0, strDate.length() - 1);
	} else if (strDate.endsWith ("d")) {
	    inDays = true;
	    strDate = strDate.substring (0, strDate.length() - 1);
	} else if (strDate.endsWith ("w")) {
	    inWeeks = true;
	    strDate = strDate.substring (0, strDate.length() - 1);
	} else if (strDate.endsWith ("M")) {
	    inMonths = true;
	    strDate = strDate.substring (0, strDate.length() - 1);
	}

	// a relative past date from now
	try {
	    long number = Long.valueOf (strDate).longValue();
	    if (inSeconds)      number *= 1000;
	    else if (inMinutes) number = number * 60 * 1000;
	    else if (inHours)   number = number * 60 * 60 * 1000;
	    else if (inDays)    number = number * 24 * 60 * 60 * 1000;
	    else if (inWeeks)   number = number * 7 * 24 * 60 * 60 * 1000;
	    else if (inMonths)  number = number * 31 * 24 * 60 * 60 * 1000;
	    return (number < 0 ? number : 0 - number);
	} catch (NumberFormatException e) {};

	// use date format
	if (dateFormat == null)
	    dateFormat = DEFAULT_DATE_FORMAT;

	SimpleDateFormat df = null;
	try {
	    df = new SimpleDateFormat (dateFormat);
	} catch (IllegalArgumentException e) {
	    return 0;
// 	    throw new SoaplabException ("Illegal characters in given date format: " + dateFormat);
	}

	try {
	    return df.parse (strDate).getTime();
	} catch (java.text.ParseException e) {
	    return 0;
//  	    throw new SoaplabException ("Wrong date: " + e.toString() +
// 					". Using date format: " + dateFormat);
	}
    }

    /*********************************************************************
     * Return an enumeration of instances of given class, as found by
     * the SPI mechanism (as described in
     * sun/jdk1.3.1/docs/guide/jar/jar.html#Service Provider). <p>
     *
     * @param svc SPI to look for and load
     * @return instances of 'svc' (implementing it or extending it)
     ********************************************************************/
    @SuppressWarnings("unchecked")
    public static <SVC> Enumeration<SVC> spiProviders (Class<SVC> svc) {
	return Service.providers (svc);
    }

}
