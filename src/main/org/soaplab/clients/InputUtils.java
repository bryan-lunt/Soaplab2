// InputUtils.java
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

package org.soaplab.clients;

import org.soaplab.share.SoaplabConstants;

import org.tulsoft.tools.BaseCmdLine;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.commons.lang.text.StrMatcher;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.io.IOException;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.File;
import java.io.FileInputStream;


/**
 * Few utilities handling analysis inputs, such as collecting them
 * from the command-line, or reading them from the local files. It
 * also helps to display metadata describing inputs. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: InputUtils.java,v 1.8 2010/11/08 12:38:47 marsenger Exp $
 */

public abstract class InputUtils {

    /*************************************************************************
     * Collect inputs from the command line, return them together with
     * their names. Each named input that occurs on the given
     * command-line is there either as two pieces:
     *
     *<pre>
     *   &lt;input-name&gt; &lt;input-value&gt;
     *</pre>
     *
     * or, if the name is prefixed by a minus sign, as a 'boolean'
     * value represented just by the input name:
     *
     * <pre>
     *   -&lt;input-name&gt;
     *</pre>
     *
     * The former case has more features:
     *<ul>
     *
     * <li> If the <em>input-value</em> starts with a colon (':'), it
     * is considered a local file name. The file is read and its
     * contents is used as an input. <p>
     *
     * <li> If the <em>input-value</em> contains one or more bar
     * characters ('|'), the value is considered a <em>repeatable
     * value</em>, it is split into an array (on places indicated by
     * the bars) which is than consider a single (array) input. <p>
     *
     * If a bar character should be a part of input data (and not a
     * separator) it must be quoted. <p>
     *
     * <li> The two features above can be combined: each element of
     * the repeatable value can start with a colon to indicate a file
     * name. <p>
     *
     * <li> Finally, the <em>input-value</em> can start with two
     * colons ('::'). It means that the value after the two colons is
     * a file name whose contents are line-separated file names. These
     * files are read and their contents is sent as an array as an
     * input. Note that this feature cannot be combined with a
     * repeatable value. <p>
     *
     *</ul>
     *
     * @param cmd represent the command-line
     * @param attrs are names (and perhaps other information) of
     * possible inputs
     *
     * @throws IOException if an input is read from a file and it failes
     *************************************************************************/
    public static Map<String,Object> collectInputs (BaseCmdLine cmd,
						    Map<String,Object>[] attrs)
	throws IOException {

	Map<String,Object> inputs = new HashMap<String,Object>();
	String key, value, type;
	for (int i = 0; i < attrs.length; i++) {
	    key = (String)attrs[i].get (SoaplabConstants.INPUT_NAME);
	    value = cmd.getParam (key);
	    if (value == null) {
		// for boolean type, try a short notation
		type = (String)attrs[i].get (SoaplabConstants.INPUT_TYPE);
		if (SoaplabConstants.TYPE_BOOLEAN_SIMPLE.equals (type)) {
		    if (cmd.hasOption ("-" + key))
			value = "true";
		    else
			continue;
		} else {
		    continue;
		}
	    }

	    if (value.startsWith ("::") && value.length() > 2) {
		inputs.put (key, list2Files (value.substring (2)));
		continue;
	    }

	    String[] split = splitMultiValue (value);
	    Object[] processedSplit = new Object [split.length]; // some input files may be binary
	    for (int j = 0; j < split.length; j++) {
		// the value can be actually a file name
		if (split[j].startsWith (":") && split[j].length() > 1) {
		    if (split[j].length() > 4 && split[j].startsWith (":[b]")) {
			// :[b] means a binary file (do not convert it to a string contents)
			processedSplit[j] = IOUtils.toByteArray (new FileInputStream (split[j].substring (4)));
		    } else {
			processedSplit[j] = IOUtils.toString (new FileReader (split[j].substring (1)));
		    }
		} else {
		    processedSplit[j] = split[j];  // no processing needed
		}
	    }
	    if (split.length > 1) {
		inputs.put (key, processedSplit);
	    } else if (split.length == 1) {
		inputs.put (key, processedSplit[0]);
	    }
	}
	return inputs;
    }

    //
    static String[] splitMultiValue (String value) {
	StrTokenizer tokenizer = new StrTokenizer (value,
						   StrMatcher.charMatcher ('|'),
						   StrMatcher.quoteMatcher());
	return tokenizer.getTokenArray();
    }

    //
    // the value can actually be a file name than contains more file
    // names; it comes her
    //
    static byte[][] list2Files (String value)
	throws IOException {

	ArrayList<byte[]> result = new ArrayList<byte[]>();
	File listFile = new File (value);
	String fullPath =
	    FilenameUtils.getFullPath (listFile.getAbsolutePath());
	LineIterator it = FileUtils.lineIterator (listFile);
	try {
	    while (it.hasNext()) {
		String line = it.nextLine().trim();
		if (StringUtils.isEmpty (line)) continue; // ignore blank lines
		if (line.startsWith ("#")) continue;      // ignore comments
		File aFile = new File (line);
		try {
		    result.add (FileUtils.readFileToByteArray (aFile));
		} catch (IOException e) {
		    // try to add path of the list file
		    if (aFile.isAbsolute()) {
			throw new IOException ("Error when reading [" + line + "]: " + e.getMessage());
		    } else {
			File bFile = new File (fullPath, line);
			result.add (FileUtils.readFileToByteArray (bFile));
		    }
		}
	    }
	} finally {
	    LineIterator.closeQuietly (it);
	}
	return result.toArray (new byte[][] {});
    }


    /*************************************************************************
     * Formats metadata into given stream. <p>
     *
     * @param attrs is an array of metadata describing inputs
     * @param out where to output
     * @param inFull if true all details about metadata are printed
     * @param inVerbose if true decoration (title) is printed, as well 
     *************************************************************************/
    public static void formatInputMetadata (Map<String,Object>[] attrs,
					    PrintStream out,
					    boolean inFull,
					    boolean inVerbose) {
	formatMetadata (attrs, out, inFull, inVerbose,
			SoaplabConstants.INPUT_NAME,
			"Input parameter names:",
			"Input parameter attributes:");
    }

    /*************************************************************************
     * Formats metadata into given stream. <p>
     *
     * @param attrs is an array of metadata describing outputs
     * @param out where to output
     * @param inFull if true all details about metadata are printed
     * @param inVerbose if true decoration (title) is printed, as well 
     *************************************************************************/
    public static void formatOutputMetadata (Map<String,Object>[] attrs,
					     PrintStream out,
					     boolean inFull,
					     boolean inVerbose) {
	formatMetadata (attrs, out, inFull, inVerbose,
			SoaplabConstants.RESULT_NAME,
			"Result names:",
			"Result attributes:");
    }

    /*************************************************************************
     *
     *************************************************************************/
    static void formatMetadata (Map<String,Object>[] attrs,
				PrintStream out,
				boolean inFull,
				boolean inVerbose,
				final String keyName,
				String titleShort,
				String titleForFull) {
	if (attrs != null) {
	    Arrays.sort (attrs, new Comparator<Map<String,Object>>() {
		public int compare (Map<String,Object> a, Map<String,Object> b) {
		    String name1 = (String)a.get (keyName);
		    String name2 = (String)b.get (keyName);
		    return name1.compareTo (name2);
		}
	    });
	    if (inFull) {
		title (out, inVerbose, titleForFull);
		for (int i = 0; i < attrs.length; i++) {
		    out.println (attrs[i].get (keyName).toString());
		    for (Iterator<String> it = attrs[i].keySet().iterator(); it.hasNext(); ) {
			String key = it.next();
			if (key.equals (keyName)) continue;
			out.println ("\t" + formatAttr (key, attrs[i].get (key), 20));
		    }
		}
	    } else {
		title (out, inVerbose, titleShort);
		for (int i = 0; i < attrs.length; i++)
		    out.println (attrs[i].get (keyName).toString());
	    }
	    out.println ("");
	}
    }

    /*************************************************************************
     * Pretty format an (input | result) attribute (defined as a
     * name and value, and a preferred length of the formatted name).
     *************************************************************************/
    static String formatAttr (String name, Object value, int preferredLength) {
	StringBuilder buf = new StringBuilder (name + ": ");
	for (int i = buf.length(); i < preferredLength; i++)
	    buf.append (" ");
	if (value instanceof String[])
	    buf. append (StringUtils.join ((String[])value, ", "));
	else
	    buf.append (value.toString());
	return buf.toString();
    }

    /*************************************************************************
     * Print and underline message 'title'.
     *************************************************************************/
    static void title (PrintStream out, boolean inVerbose, String title) {
	if (! inVerbose)
	    return;
	out.println (title);
	for (int i = 0; i < title.length(); i++)
	    out.print ("-");
	out.println ("");
    }


    /*************************************************************************
     * Read all relevant arguments from the 'cmd' (representing a
     * command-line) and create from them an instance of a
     * ServiceLocator. <p>
     *
     * What are the relevant arguments is explained in the help pages
     * for CmdLineClient.
     *************************************************************************/
    public static ServiceLocator getServiceLocator (BaseCmdLine cmd) {

	    ServiceLocator locator = new ServiceLocator();
	    locator.setServiceName (cmd.getParam ("-name"));
	    locator.setListServiceName (cmd.getParam ("-lname"));
	    locator.setHost (cmd.getParam ("-host"));
	    locator.setPort (cmd.getParam ("-port"));
	    locator.setContext (cmd.getParam ("-ctx"));
	    locator.setServiceEndpoint (cmd.getParam ("-e"));
	    locator.setListEndpoint (cmd.getParam ("-el"));
	    locator.setProtocol (cmd.getParam ("-protocol"));

	    // make it a bit easier for Axis-1 protocol
	    if ( ClientConfig.PROTOCOL_AXIS1.equals (locator.getProtocol()) ) {
		if (! locator.isContextSet() &&
		    StringUtils.isEmpty (cmd.getParam ("-ctx")) ) {
		    locator.setContext
			(new StringBuilder()
			 .append (ClientConfig.DEFAULT_CONTEXT_NAME)
			 .append ("-axis")
			 .toString());
		}
	    }

	    return locator;
    }

}
