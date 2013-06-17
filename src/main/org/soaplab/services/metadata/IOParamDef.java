// IOParamDef.java
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

import org.soaplab.share.SoaplabConstants;
import org.soaplab.services.GenUtils;
import java.util.Hashtable;

/**
 * A data structure defining input/output types of an analysis tools.
 * For input data, it defines how to feed an application with the
 * input data, and for output, it specifies where to catch the output
 * data from an application. <p>
 *
 * Supported types are:
 *<pre>
 * input    an input data which are not eaten by the app's standard input
 * stdin    send data to app's standard input
 * output   an output data which are not produced by standard stream
 * stdout   catch data from app's standard output
 * stderr   catch data from app's standard error stream
 *</pre>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: IOParamDef.java,v 1.13 2007/12/04 16:26:57 marsenger Exp $
 */
public class IOParamDef
    extends StdParamDef {

    //
    // Constants for properties
    //

    /** An option name. Its value (of type String) is a class name of
     * an adaptor for pre-processing an input. */
    public static final String INPUT_ADAPTOR = "input_adaptor";

    /** An option name. Its value (of type String) is a class name of
     * an adaptor for post-processing an output (result). */
    public static final String OUTPUT_ADAPTOR = "output_adaptor";

    /** An option name. Its value (of type String) makes this output
     * special. Usual value is "url" which means that the output
     * should be made also available as an HTTP/GET target. */
    public static final String SPECIAL_TYPE = "special_type";

    /**
     * Input/output types.
     */
    public static final int TYPE_INPUT     = 0;
    public static final int TYPE_STDIN     = 1;
    public static final int TYPE_OUTPUT    = 2;
    public static final int TYPE_STDOUT    = 3;
    public static final int TYPE_STDERR    = 4;

    //
    // Because the value of this parameter is of type String
    // we need to remember what this string represents
    //
    public static final int FORMAT_DIRECT      = 0;
    public static final int FORMAT_URL         = 1;
    public static final int FORMAT_UNSPECIFIED = 2;  // allowing both direct and URL

    //
    // members
    //
    public int ioType = TYPE_INPUT;
    public int ioFormat = FORMAT_DIRECT;

    /*********************************************************************
     * A default constructor.
     ********************************************************************/
    public IOParamDef() {
    }

    /*********************************************************************
     * Convert string representation of an IO type to an integer.
     * Return -1 if unknown type is passed.
     ********************************************************************/
    private static Hashtable<String,Integer> ioTypes = new Hashtable<String,Integer>();
    static {
        ioTypes.put ("input",  new Integer (TYPE_INPUT));
        ioTypes.put ("stdin",  new Integer (TYPE_STDIN));
        ioTypes.put ("output", new Integer (TYPE_OUTPUT));
        ioTypes.put ("stdout", new Integer (TYPE_STDOUT));
        ioTypes.put ("stderr", new Integer (TYPE_STDERR));
    }

    protected static int checkAndGetIOType (String ioType) {
        return GenUtils.checkAndGetStr2Int (ioType, ioTypes);
    }

    public String getIOTypeAsString() {
	return GenUtils.inverseMapping (ioType, ioTypes);
    }

    /*********************************************************************
     * Convert string representation of an IO format to an integer.
     * Return -1 if unknown type is passed.
     ********************************************************************/
    private static Hashtable<String,Integer> ioFormats = new Hashtable<String,Integer>();
    static {
        ioFormats.put ("direct",      new Integer (FORMAT_DIRECT));
        ioFormats.put ("url",         new Integer (FORMAT_URL));
        ioFormats.put ("unspecified", new Integer (FORMAT_UNSPECIFIED));
    }

    protected static int checkAndGetIOFormat (String ioFormat) {
        return GenUtils.checkAndGetStr2Int (ioFormat, ioFormats);
    }

    public String getIOFormatAsString() {
	return GenUtils.inverseMapping (ioFormat, ioFormats);
    }

    /**************************************************************************
     * Return true if this definition represents an input (either a
     * regular or a standard input).
     **************************************************************************/
    public boolean isInput() {
	return (ioType == TYPE_INPUT || ioType == TYPE_STDIN);
    }

    /**************************************************************************
     * Return true if this definition represents an output (either a
     * regular or a standard stream).
     **************************************************************************/
    public boolean isOutput() {
	return (ioType == TYPE_OUTPUT || ioType == TYPE_STDOUT || ioType == TYPE_STDERR);
    }

    /**************************************************************************
     * Return true if this definition represents a regular input
     * (which excludes the standard input).
     **************************************************************************/
    public boolean isRegularInput() {
	return (ioType == TYPE_INPUT);
    }

    /**************************************************************************
     * Return true if this definition represents a standard output.
     **************************************************************************/
    public boolean isStdout() {
	return (ioType == TYPE_STDOUT);
    }

    /**************************************************************************
     * Return true if this definition represents a standard error.
     **************************************************************************/
    public boolean isStderr() {
	return (ioType == TYPE_STDERR);
    }

    /**************************************************************************
     * Return true if this definition represents a standard input.
     **************************************************************************/
    public boolean isStdin() {
	return (ioType == TYPE_STDIN);
    }

    /**************************************************************************
     * Return true if this definition represents a regular output
     * (which excludes the standard output and standard error output).
     **************************************************************************/
    public boolean isRegularOutput() {
	return (ioType == TYPE_OUTPUT);
    }

    /**************************************************************************
     * Return true if this definition represents any of the standard
     * streams.
     **************************************************************************/
    public boolean isStandardStream() {
	return (ioType == TYPE_STDIN || ioType == TYPE_STDOUT || ioType == TYPE_STDERR);
    }

    /**************************************************************************
     * Return true if this definition represents direct data (and not
     * reference nor unspecified data).
     **************************************************************************/
    public boolean isDirectData() {
	return (ioFormat == FORMAT_DIRECT);
    }

    /**************************************************************************
     * Return true if this definition represents reference data (and not
     * direct nor unspecified data).
     **************************************************************************/
    public boolean isReferenceData() {
	return (ioFormat == FORMAT_URL);
    }

    /**************************************************************************
     * Return the beginning of its own name, stripped of the endings
     * indicating the 'ioFormat' type.
     **************************************************************************/
    public String getStrippedName() {
	if (id.endsWith (SoaplabConstants.DIRECT_DATA_SUFFIX))
	    return id.substring (0, id.length()-SoaplabConstants.DIRECT_DATA_SUFFIX.length());
	if (id.endsWith (SoaplabConstants.URL_SUFFIX))
	    return id.substring (0, id.length()-SoaplabConstants.URL_SUFFIX.length());
	if (id.endsWith (SoaplabConstants.USA_SUFFIX))
	    return id.substring (0, id.length()-SoaplabConstants.USA_SUFFIX.length());
	return id;
    }

    /**************************************************************************
     *
     **************************************************************************/
    public String toString() { return format (0); }

    /**************************************************************************
     * As toString() but with indentations.
     **************************************************************************/
    public String format (int indent) {
	String tabs = GenUtils.indent (indent);
        StringBuilder buf = new StringBuilder();
        buf.append (super.format (indent));
	buf.append (tabs + "\tioType =   " + getIOTypeAsString() + "(" + ioType + ")\n");
	buf.append (tabs + "\tioFormat = " + getIOFormatAsString() + "(" + ioFormat + ")\n");
        return buf.toString();
    }
}

