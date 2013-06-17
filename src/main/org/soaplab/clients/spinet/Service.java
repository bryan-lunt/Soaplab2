// Service.java
//
// Created: October 2007
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

package org.soaplab.clients.spinet;

import org.soaplab.share.SoaplabException;
import org.soaplab.share.SoaplabConstants;
import org.soaplab.share.SoaplabMap;
import org.soaplab.share.Analysis;
import org.soaplab.clients.ClientConfig;
import org.soaplab.clients.ServiceLocator;
import org.soaplab.clients.SoaplabBaseClient;

import org.tulsoft.tools.servlets.Html;
import static org.tulsoft.tools.servlets.HtmlConstants.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.WordUtils;

import java.io.IOException;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A representation of a Soaplab service on the client side. It has
 * two main purposes (I wonder if I should separate it in two classes
 * then): a) To invoke Soaplab services, and b) To create HTML
 * presentation for these services. <p>
 *
 * The former is done by using SoaplabBaseClient instances. The latter
 * constitutes the major contents of this class. <p>
 *
 * It is used from the JSP - that's why it also behaves as a bean
 * (set/get methods). <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: Service.java,v 1.22 2010/09/03 10:04:01 smoretti Exp $
 */

public class Service {

    private static org.apache.commons.logging.Log log =
       org.apache.commons.logging.LogFactory.getLog (Service.class);

    protected static final String DEFAULT_SELECT_TEXT = "--Use Default--";

    private static final String TMP_PROP_BOTH_INPUTS = "both_inputs";

    // suffixes to input names
    // (if changed, then change them also in services.js)
    protected static final char   S_S              = '$';
    protected static final String S_INPUT          = S_S + "INPUT";
    protected static final String S_INPUTDATA      = S_S + "INPUTDATA";
    protected static final String S_INPUTDATAALONE = S_S + "INPUTDATAALONE";
    protected static final String S_DEFAULTVALUE   = S_S + "DEFAULTVALUE";
    protected static final String S_UPLOAD         = S_S + "UPLOAD";
    protected static final String S_TOGGLE         = S_S + "TOGGLE";
    protected static final String S_MESSAGE        = S_S + "MESSAGE";

    // suffix to result names
    protected static final String S_OUTPUT   = "$OUTPUT";

    // EMBOSS-specific stuff
    protected static final String TMP_PROP_EMBOSS_INPUT = "for_emboss";
    protected static final String INPUT_SEQ_TYPE = "seq_type";
    protected static final String S_SHOWDB = S_S + "SHOWDB";
    protected static final String S_SHOWDBDESC = S_S + "SHOWDBDESC";
    // (if you change this, change it also in JavaScript)
    protected static final String DEFAULT_SHOWDB_TEXT = "--Select DB--";
    protected static String[] showdbNames;
    protected static String[] showdbComments;

    private static final String FILELIST_WARNING =
	"This input is of type FILELIST. Its purpose is to send more files to the Soaplab service, wrapped in file containing all their names. Unfortunately, such input is not supported by this interface. Therefore, it is very probable that this service will not work as one would expect. Sorry for that (try other clients that support this type of input).";

    private String name;
    private String description;
    private static Html h = new Html();

    /*************************************************************************
     * A default constructor.
     *************************************************************************/
    public Service (String name) {
	this.name = name;
    }

    /*************************************************************************
     *
     *                  The "bean" part is here
     *
     *      (but not only, some pieces of the presentation layer are also
     *       used as the bean calls)
     *
     *************************************************************************/

    /*************************************************************************
     * The service name can be set only in the constructor.
     *************************************************************************/
    public String getName() {
	return name;
    }

    /*************************************************************************
     * A display name is more "human-readable" (no underscores,
     * capitalization, etc.)
     *************************************************************************/
    public String getDisplayName() {
	String[] parts =
	    StringUtils.split (getName(),
			       SoaplabConstants.SOAPLAB_SERVICE_NAME_DELIMITER,
			       2);
	if (parts.length > 1)
	    return parts[1];
	else
	    return parts[0];
    }

    /*************************************************************************
     *
     *************************************************************************/
    public String getDescription() {
	return description;
    }

    /*************************************************************************
     *
     *************************************************************************/
    public void setDescription (String description) {
	this.description = description;
    }

    /*************************************************************************
     *
     *
     *                  The Soaplab calls are here
     *
     *
     *************************************************************************/

    /*************************************************************************
     *
     *************************************************************************/
    protected Analysis getClient()
	throws SoaplabException {
	return getClient (getName());
    }

    //
    protected Analysis getClient (String serviceName)
	throws SoaplabException {
	ServiceLocator locator = new ServiceLocator (serviceName);
	locator.setProtocol (ClientConfig.PROTOCOL_LOCAL);
	return new SoaplabBaseClient (locator);
    }

    /*************************************************************************
     *
     *************************************************************************/
    public String createAndRun (Map<String,Object> inputs)
	throws SoaplabException {
	return getClient().createAndRun (SoaplabMap.fromMap (inputs));
    }

    /*************************************************************************
     *
     *************************************************************************/
    public String getStatus (String jobId)
	throws SoaplabException {
	return getClient().getStatus (jobId);
    }

    /*************************************************************************
     *
     *************************************************************************/
    public Map<String,String>[] getResultsInfo (String jobId)
	throws SoaplabException {
	return SoaplabMap.toStringMaps (getClient().getResultsInfo (jobId));
    }

    /*************************************************************************
     *
     *************************************************************************/
    public Object getResult (String jobId, String resultName)
	throws SoaplabException {
	Map<String,Object> results =
	    SoaplabMap.toMap (getClient()
			      .getSomeResults (jobId, new String[] { resultName }));
	return results.get (resultName);
    }

    /*************************************************************************
     * Get metadata about one result. Return an empty map if the
     * result name does not exist.
     *************************************************************************/
    public Map<String,Object> getResultSpec (String resultName)
	throws SoaplabException {
	Map<String,Object>[] outputs =
	    SoaplabMap.toMaps (getClient().getResultSpec());
 	for (Map<String,Object> output: outputs) {
 	    String name =(String)output.get ("name");
	    if (resultName.equals (name))
		return output;
	}
	return new HashMap<String,Object>();
    }

    /*************************************************************************
     *
     *************************************************************************/
    public void terminate (String jobId)
	throws SoaplabException {
	getClient().terminate (jobId);
    }

    /*************************************************************************
     *
     *************************************************************************/
    public void destroy (String jobId)
	throws SoaplabException {
	getClient().destroy (jobId);
    }

    /*************************************************************************
     *
     *
     *                  The presentation layer is here
     *
     *
     *************************************************************************/


    /*************************************************************************
     * It returns a map of several parts of a service panel - which is
     * an HTML representation how this service can be controlled
     * (started, explored, etc.). These parts are included into an
     * HTML page using the JSP mechanism (that understands this
     * returned map).
     *************************************************************************/
    public Map<String,String> getPanelMap()
	throws SoaplabException {
	log.debug ("Panel requested for " + getName());

	try {
	    // get metadata
	    Analysis client = getClient();
	    Map<String,Object>[] inputs = SoaplabMap.toMaps (client.getInputSpec());
// 	    Map<String,Object>[] outputs = SoaplabMap.toMaps (client.getResultSpec());
	    Map<String,Object> app = SoaplabMap.toMap (client.getAnalysisType());
	    if (BooleanUtils.toBoolean
		((String)app.get (SoaplabConstants.ANALYSIS_EMBOSS))) {
		fetchDBNames();
	    }

 	    // pack everything into a resulting Map
	    Map<String,String> valuesMap = new HashMap<String,String>();
	    valuesMap.put ("service.name", getName());
	    valuesMap.put ("inputs", getInputsPanel (app, inputs));
// 	    valuesMap.put ("outputs", getOutputsPanel (app, outputs));
	    valuesMap.put ("apphelp", getHelpPanel (app));
	    return valuesMap;

	} catch (Throwable e) {
	    throw new SoaplabException (e.getMessage(), e);
	}
    }

    /*************************************************************************
     * Calling display.showdb service... (but only once)
     *************************************************************************/
    protected synchronized void fetchDBNames() {
	if (showdbNames != null)
	    return;
	try {
	    Analysis client = getClient ("display.showdb");
	    Map<String,Object> inputs = new HashMap<String,Object>();
	    inputs.put ("only",    "true");
	    inputs.put ("comment", "true");
	    Map<String,Object> results =
		SoaplabMap.toMap (client.runAndWaitFor (SoaplabMap.fromMap (inputs)));
	    String result = (String)results.get ("outfile");
	    if (result == null) {
		log.warn ("Fetching EMBOSS database names returned an empty result");
		return;
	    }
	    List<String> showdbNamesList = new ArrayList<String>();
	    showdbNamesList.add (DEFAULT_SHOWDB_TEXT);
	    List<String> showdbCommentsList = new ArrayList<String>();
	    showdbCommentsList.add ("");
	    for (String line: result.split ("\\n")) {
		if (StringUtils.isBlank (line))
		    continue;
		String[] parts = line.split ("\\s", 2);
		showdbNamesList.add (parts[0].trim());
		showdbCommentsList.add (parts.length > 1 ? parts[1].trim() : "");
	    }
	    showdbNames = showdbNamesList.toArray (new String[] {});
	    showdbComments = showdbCommentsList.toArray (new String[] {});

	} catch (Exception e) {
	    log.warn ("Cannot fetch EMBOSS database names: " + e.toString());
	}
    }

    /*************************************************************************
     *
     *************************************************************************/
    protected String getTemplate (String filename)
	throws IOException {
 	return FileUtils.readFileToString (new File (filename));
    }

    /*************************************************************************
     *
     *************************************************************************/
    protected void sort (Map<String,Object>[] data) {
	Arrays.sort (data, new Comparator<Map<String,Object>>() {
	    public int compare (Map<String,Object> a, Map<String,Object> b) {
		if (a == null) return -1;
		if (b == null) return +1;
		String name1 = (String)a.get ("name");
		String name2 = (String)b.get ("name");
		return name1.compareTo (name2);
	    }
	});
    }

    /*************************************************************************
     * Merge together elements that represent the same input
     * (_direct_data and _url|_usa). Mark the merged element with a
     * new property TMP_PROP_BOTH_INPUTS. Put null on the not-anymore
     * used element. <p>
     *
     * 
     *************************************************************************/
    protected void consolidate (Map<String,Object>[] data) {
	for (int i = 0; i < data.length; i++) {
	    if (data[i] == null) continue;
	    if (isInputFile (data [i], data)) {
		String name = (String)data[i].get (SoaplabConstants.INPUT_NAME);
		boolean isURL = name.endsWith (SoaplabConstants.URL_SUFFIX);
		boolean isUSA = name.endsWith (SoaplabConstants.USA_SUFFIX);
		if (name.endsWith (SoaplabConstants.DIRECT_DATA_SUFFIX)) {
		    String realName =
			name.substring (0,
					name.length()-SoaplabConstants.DIRECT_DATA_SUFFIX.length());
		    data[i].put (SoaplabConstants.INPUT_NAME, realName);
		    String lookForURL = realName + SoaplabConstants.URL_SUFFIX;
		    String lookForUSA = realName + SoaplabConstants.USA_SUFFIX;
		    for (int j = i + 1; j < data.length; j++) {
			if (data[j] == null) continue;
			String name2 = (String)data[j].get (SoaplabConstants.INPUT_NAME);
			if (lookForURL.equals (name2) || lookForUSA.equals (name2)) {
			    if (data[j].containsKey (SoaplabConstants.INPUT_DEFAULT_VALUE)) {
				data[i].put (SoaplabConstants.INPUT_DEFAULT_VALUE,
					     data[j].get (SoaplabConstants.INPUT_DEFAULT_VALUE) );
			    }
			    data[i].put (TMP_PROP_BOTH_INPUTS, "1");
			    data[j] = null;
			    if (lookForUSA.equals (name2)) {
				data[i].put (TMP_PROP_EMBOSS_INPUT, "1");
			    }
			}
		    }
		} else if (isURL || isUSA) {
		    String realName =
			( isURL ? 
			  name.substring (0, name.length()-SoaplabConstants.URL_SUFFIX.length()) :
			  name.substring (0, name.length()-SoaplabConstants.USA_SUFFIX.length()) );
		    data[i].put (SoaplabConstants.INPUT_NAME, realName);
		    String lookFor = realName + SoaplabConstants.DIRECT_DATA_SUFFIX;
		    for (int j = i + 1; j < data.length; j++) {
			if (data[j] == null) continue;
			String name2 = (String)data[j].get (SoaplabConstants.INPUT_NAME);
			if (lookFor.equals (name2)) {
			    if (data[j].containsKey (SoaplabConstants.INPUT_DEFAULT_VALUE)) {
				data[i].put (SoaplabConstants.INPUT_DEFAULT_VALUE,
					     data[j].get (SoaplabConstants.INPUT_DEFAULT_VALUE) );
			    }
			    data[i].put (TMP_PROP_BOTH_INPUTS, "1");
			    data[j] = null;
			    if (isUSA) {
				data[i].put (TMP_PROP_EMBOSS_INPUT, "1");
			    }
			}
		    }
		}
	    }
	}
    }

    /*************************************************************************
     * Return true if 'input' represents an input file (which is known
     * because there is a property DATA_TYPE with the value "input").
     *************************************************************************/
    protected boolean isInputFile (Map<String,Object> input) {
	return
	    "input".equals ((String)input.get (SoaplabConstants.DATA_TYPE)) ||
	    input.containsKey (INPUT_SEQ_TYPE);
    }

    /*************************************************************************
     * Return true if 'input' represents an input file - by looking
     * also at other 'inputs', trying to find known suffixes for
     * direct data and for refernece data. This is not too nice (and
     * can be broken if some application starts using these suffixes
     * for regular parameters) - but we have to wait for the better
     * ACD2XML converter that would correctly set DATA_TYPE attribute
     * for *all* inputs (those coming from EMBOSS are now not labelled
     * properly). <p>
     *
     * Because of the above, we also add here DATA_TYPE => "input" -
     * so the simpler isInputFile() method interprets it as an input.
     *************************************************************************/
    protected boolean isInputFile (Map<String,Object> input,
				   Map<String,Object>[] inputs) {

	// often the input can be confirmed without checking other inputs
	if (isInputFile (input))
	    return true;

	// if not, check the known suffixes
	String name = (String)input.get (SoaplabConstants.INPUT_NAME);
	if (name.endsWith (SoaplabConstants.DIRECT_DATA_SUFFIX)) {
	    String strippedName =
		StringUtils.removeEnd (name, SoaplabConstants.DIRECT_DATA_SUFFIX);
	    if (nameExists (strippedName + SoaplabConstants.URL_SUFFIX, inputs) ||
		nameExists (strippedName + SoaplabConstants.USA_SUFFIX, inputs)) {
		input.put (SoaplabConstants.DATA_TYPE, "input");
		return true;
	    }
	}
	if (name.endsWith (SoaplabConstants.URL_SUFFIX)) {
	    String strippedName =
		StringUtils.removeEnd (name, SoaplabConstants.URL_SUFFIX);
	    if (nameExists (strippedName + SoaplabConstants.DIRECT_DATA_SUFFIX, inputs)) {
		input.put (SoaplabConstants.DATA_TYPE, "input");
		return true;
	    }
	}
	if (name.endsWith (SoaplabConstants.USA_SUFFIX)) {
	    String strippedName =
		StringUtils.removeEnd (name, SoaplabConstants.USA_SUFFIX);
	    if (nameExists (strippedName + SoaplabConstants.DIRECT_DATA_SUFFIX, inputs)) {
		input.put (SoaplabConstants.DATA_TYPE, "input");
		return true;
	    }
	}
	return false;
    }

    /*************************************************************************
     * Return true if the 'name' exists in any of the elements of
     * 'data', under the key {@link SoaplabConstants#INPUT_NAME}.
     *************************************************************************/
    private boolean nameExists (String name, Map<String,Object>[] data) {
	for (int i = 0; i < data.length; i++) {
	    if (data[i] == null) continue;
	    if (name.equals ((String)data[i].get (SoaplabConstants.INPUT_NAME)))
		return true;
	}
	return false;
    }

    /*************************************************************************
     *
     *************************************************************************/
    protected String getInputsPanel (Map<String,Object> app,
				     Map<String,Object>[] inputs) {
	consolidate (inputs);
	sort (inputs);
	int indexOfFirstOptional = 0;
	List<String> names = new ArrayList<String>();
	List<String> classes = new ArrayList<String>();
	List<String> uis = new ArrayList<String>();
	StringBuilder tooltips = new StringBuilder();
	StringBuilder js = new StringBuilder();

	// init js (for input validation)
	js.append (h.gen (SCRIPT, TYPE, "text/javascript", null));
	js.append ("\nTESTS ['");
	js.append (getName());
	js.append ("']=[\n");

	// first add all mandatory inputs
	for (Map<String,Object> input: inputs) {
	    if (input == null)
		continue;
	    if (BooleanUtils.toBoolean
		((String)input.get (SoaplabConstants.INPUT_MANDATORY))) {
		createInput (app, input, names, classes, uis, tooltips, js);
		indexOfFirstOptional++;
	    }
	}
	if (indexOfFirstOptional == 0)
	    indexOfFirstOptional = -1;  // if no mandatory input

	// then add all remaining inputs
	for (Map<String,Object> input: inputs) {
	    if (input == null)
		continue;
	    if (! BooleanUtils.toBoolean
		((String)input.get (SoaplabConstants.INPUT_MANDATORY))) {
		createInput (app, input, names, classes, uis, tooltips, js);
	    }
	}

	// finish js
	js.append ("];\n");
	js.append (h.end (SCRIPT));
	js.append ("\n");

	return
	    createInputTable (names, classes, uis, indexOfFirstOptional) + "\n\n" +
	    tooltips.toString() + "\n\n" + js.toString();
    }

    /*************************************************************************
     *
     *************************************************************************/
    protected void createInput (Map<String,Object> app,
				Map<String,Object> input,
				List<String> names,
				List<String> classes,
				List<String> uis,
				StringBuilder tooltips,
				StringBuilder js) {

	boolean emboss = BooleanUtils.toBoolean
	    ((String)app.get (SoaplabConstants.ANALYSIS_EMBOSS));

	String name = (String)input.get (SoaplabConstants.INPUT_NAME);
	String type = (String)input.get (SoaplabConstants.INPUT_TYPE);
	String defaultValue = (String)input.get (SoaplabConstants.INPUT_DEFAULT_VALUE);
	String[] allowedValues = (String[])input.get (SoaplabConstants.INPUT_ALLOWED_VALUES);
	String format = (String)input.get (SoaplabConstants.INPUT_FORMAT);
	String datatype = (String)input.get (SoaplabConstants.DATA_TYPE);
	boolean mandatory = BooleanUtils.toBoolean
	    ((String)input.get (SoaplabConstants.INPUT_MANDATORY));

	// something only for EMBOSS
	if (emboss) {
	    if (name.startsWith ("send_") || name.startsWith ("sbegin_")) {
		format = "%d";
	    }
	}

	if (ArrayUtils.getLength (allowedValues) > 0) {
	    // allowed values => SELECT
	    names.add (Html.esc (name));
	    classes.add (createInputTooltipAndClass (name, input, tooltips));

	    // it's never used but initiates creation of a special select option
	    if (!mandatory && StringUtils.isEmpty (defaultValue)) {
		defaultValue = "something";
	    }

	    uis.add (createSelect (name + S_INPUT, defaultValue, allowedValues));

	} else if (type.equals (SoaplabConstants.TYPE_BOOLEAN_SIMPLE)) {
	    // boolean type => RADIO GROUP
	    names.add (Html.esc (name));
	    classes.add (createInputTooltipAndClass (name, input, tooltips));
	    uis.add (createRadioPair (name + S_INPUT, defaultValue));

	} else if (isInputFile (input)) {
	    // input file
	    names.add (Html.esc (name));
	    classes.add (createInputTooltipAndClass (name, input, tooltips));
	    uis.add (createDataInput (name, js, input));

	} else if (format != null) {
	    // formatted field (requiring some validation)
	    names.add (Html.esc (name));
	    classes.add (createInputTooltipAndClass (name, input, tooltips));
	    uis.add (createValidText (name + S_INPUT, input, mandatory, js, format, defaultValue));

	} else if ("filelist".equals (datatype)) {
	    // filelist is not supported in Spinet, inform about it
	    names.add (Html.esc (name));
	    classes.add (createInputTooltipAndClass (name, input, tooltips));
	    uis.add (createTextWithWarning (name + S_INPUT, mandatory, js,
					    FILELIST_WARNING,
					    VALUE, defaultValue,
					    CLASS, "input-text"));
 	} else {
	    // ...and the rest
	    names.add (Html.esc (name));
	    classes.add (createInputTooltipAndClass (name, input, tooltips));
	    uis.add (createText (name + S_INPUT, mandatory, js,
				 VALUE, defaultValue,
				 CLASS, "input-text"));
	}
    }

    // CSS class names for input and output names
    private static final String DEFAULT_INPUT_CLASS = "input-name";
    private static final String DEFAULT_OUTPUT_CLASS = "output-name";

    /*************************************************************************
     *
     *************************************************************************/
    protected String createInputTooltipAndClass (String name,
						 Map<String,Object>input,
						 StringBuilder tooltips) {
	return createTooltipAndClass (name, input, tooltips, DEFAULT_INPUT_CLASS);
    }

    /*************************************************************************
     *
     *************************************************************************/
    protected String createOutputTooltipAndClass (String name,
						  Map<String,Object>output,
						  StringBuilder tooltips) {
	return createTooltipAndClass (name, output, tooltips, DEFAULT_OUTPUT_CLASS);
    }

    /*************************************************************************
     * If the given 'name' has in its 'input' anything useful for
     * creating a tooltip, create it and add it into 'tooltips'. Then
     * return a (CSS-)class name that should be used for this
     * name. The returned class is either a default one (if no tooltip
     * was created), or a combinantion of a default one and a tooltip
     * class.
     *************************************************************************/
    protected String createTooltipAndClass (String name,
					    Map<String,Object>input,
					    StringBuilder tooltips,
					    String defaultCSSClass) {
	String prompt = (String)input.get (SoaplabConstants.DATA_PROMPT);
	String help = (String)input.get (SoaplabConstants.DATA_HELP);
	boolean promptAvailable = StringUtils.isNotBlank (prompt);
	boolean helpAvailable = StringUtils.isNotBlank (help);

	if (! promptAvailable && ! helpAvailable)
	    return defaultCSSClass;

	if (! promptAvailable)
	    prompt = name;

	// build a DIV that represents a tooltip (a note)
	String noteId = (getName() + name).replaceAll ("[^a-z0-9]", "X");

	String[] attrs = new String[] { ID,    "supernote-note-" + noteId,
					CLASS, "snp-mouseoffset pinnable tooltip" };
	tooltips.append ("\n");
	tooltips.append (h.gen (DIV, attrs,
				h.gen (H5, prompt) + (helpAvailable ? help : "")));
 	return defaultCSSClass + " supernote-hover-" + noteId;
    }

    /*************************************************************************
     *
     *************************************************************************/
    protected String createDataInput (String name,
				      StringBuilder js,
				      Map<String,Object> input) {

	if (input.containsKey (TMP_PROP_BOTH_INPUTS)) {

	    // for URL and direct data
	    String defaultValue = (String)input.get (SoaplabConstants.INPUT_DEFAULT_VALUE);
	    boolean defaultForDirect = BooleanUtils.toBoolean
		((String)input.get (SoaplabConstants.DEFAULT_FOR_DIRECT));
	    boolean checkedForDirect =
		(StringUtils.isEmpty (defaultValue) || defaultForDirect);

	    StringBuilder buf = new StringBuilder();
	    buf.append (h.gen (TABLE, new String[] { CELLPADDING, "0",
						     CLASS,       "data-input-box" }));
	    // ...this is a row for URL data
	    String refSuffix =
		(input.containsKey (TMP_PROP_EMBOSS_INPUT) ?
		 SoaplabConstants.USA_SUFFIX :
		 SoaplabConstants.URL_SUFFIX);

	    String elementName = name + refSuffix;
 	    String onFocus = "checkRadio(this.form, '" + name + S_TOGGLE + refSuffix + "')";
	    String togglePrompt = null;

	    if (refSuffix.equals (SoaplabConstants.USA_SUFFIX)) {
		togglePrompt = "as USA or<br>as URL";

		// add EMBOSS databases
		buf.append (h.gen (TR,
				   h.gen (TD, new String[] { COLSPAN, "3" },
					  createShowDB (elementName + S_INPUTDATA))));
		buf.append ("\n");
	    } else {
		togglePrompt = "as URL";
	    }

	    buf.append (h.gen (TR));
	    buf.append
		(h.gen (TD, createText (elementName + S_INPUTDATA,
					false,   // mandatorness is tested elsewhere
					js,      // but no need for JavaScript tests
					VALUE, (defaultForDirect ? null : defaultValue),
					SIZE, "30",
					ONFOCUS, onFocus,
					CLASS, "input-text")));
	    buf.append
		(h.gen (TD, createRadio (name + S_TOGGLE,
					 name + S_TOGGLE + refSuffix,
					 refSuffix,
					 !checkedForDirect)));
	    buf.append (h.gen (TD, new String[] { CLASS, "data-input-toggle" }, togglePrompt));
	    buf.append (h.end (TR));

	    // ...this is a row for direct data
	    buf.append (h.gen (TR));
	    buf.append (h.gen (TD,
			       createDirectDataInput (name, js, input) +
			       createErrorMessageField (name)));
	    buf.append
		(h.gen (TD, createRadio (name + S_TOGGLE,
					 name + S_TOGGLE + SoaplabConstants.DIRECT_DATA_SUFFIX,
					 SoaplabConstants.DIRECT_DATA_SUFFIX,
					 checkedForDirect)));
	    buf.append (h.gen (TD, new String[] { CLASS, "data-input-toggle" },
			       "direct data<br>or local file"));
	    buf.append (h.end (TR));

	    buf.append (h.end (TABLE));
	    return buf.toString();

	} else {

	    // only for direct data
	    return classWrap ("data-input-box",
			      createDirectDataInputAlone (name, js, input) +
			      createErrorMessageField (name));
	}
    }

    /*************************************************************************
     *
     *************************************************************************/
    protected String createDirectDataInput (String name,
					    StringBuilder js,
					    Map<String,Object> input) {
	boolean mandatory = BooleanUtils.toBoolean
	    ((String)input.get (SoaplabConstants.INPUT_MANDATORY));
	String defaultValue = (String)input.get (SoaplabConstants.INPUT_DEFAULT_VALUE);
	boolean defaultForDirect = BooleanUtils.toBoolean
	    ((String)input.get (SoaplabConstants.DEFAULT_FOR_DIRECT));
	StringBuilder buf = new StringBuilder();
	buf.append (h.gen (TABLE, new String[] { CLASS, "direct-input-box" }));

	String onFocus =
	    "checkRadio(this.form, '" + name + S_TOGGLE + SoaplabConstants.DIRECT_DATA_SUFFIX + "')";
	buf.append
	    (h.gen (TR, h.gen
		    (TD, createTextArea (name + SoaplabConstants.DIRECT_DATA_SUFFIX + S_INPUTDATA, 
					 VALUE, (defaultForDirect ? defaultValue : null),
					 ROWS, "3",
					 COLS, "30",
					 ONFOCUS, onFocus,
					 CLASS, "input-text"))));
	// hack: onFocus on type=file does not work in all browsers, but onClick does
	String onClick =
	    "checkRadio(this.form, '" + name + S_TOGGLE + SoaplabConstants.DIRECT_DATA_SUFFIX + "')";
	buf.append (h.gen (TR,
			   h.gen (TD, h.gen (INPUT, new String[]
			       { NAME, name + SoaplabConstants.DIRECT_DATA_SUFFIX + S_UPLOAD,
				 TYPE, "file",
				 ONCLICK, onClick,
				 CLASS, "input-upload" }, ""))));
	buf.append (h.end (TABLE));
	if (mandatory) {
	    js.append ("\"isInputMandatory (form,'" + name + "')\",\n");
	}
	return buf.toString();
    }

    /*************************************************************************
     * The same as createDirectDataInput - but for a standalone direct
     * data (not joined with a field for a URL data).
     *************************************************************************/
    protected String createDirectDataInputAlone (String name,
						 StringBuilder js,
						 Map<String,Object> input) {
	boolean mandatory = BooleanUtils.toBoolean
	    ((String)input.get (SoaplabConstants.INPUT_MANDATORY));
	String defaultValue = (String)input.get (SoaplabConstants.INPUT_DEFAULT_VALUE);
	boolean defaultForDirect = BooleanUtils.toBoolean
	    ((String)input.get (SoaplabConstants.DEFAULT_FOR_DIRECT));
	StringBuilder buf = new StringBuilder();
	buf.append (h.gen (TABLE, new String[] { CLASS, "direct-input-box" }));
	buf.append
	    (h.gen (TR, h.gen
		    (TD, createTextArea (name + S_INPUTDATAALONE,
					 VALUE, (defaultForDirect ? defaultValue : null),
					 ROWS, "3",
					 COLS, "30",
					 CLASS, "input-text"))));

	buf.append (h.gen (TR,
			   h.gen (TD, h.gen (INPUT, new String[]
			       { NAME, name + S_UPLOAD,
				 TYPE, "file",
				 CLASS, "input-upload" }, ""))));
	buf.append (h.end (TABLE));
	if (mandatory) {
	    js.append ("\"isInputMandatory (form,'" + name + "')\",\n");
	}
	return buf.toString();
    }

    /*************************************************************************
     *
     *************************************************************************/
    protected String createShowDB (String usaElementName) {
	if (showdbNames == null)
	    return "";
	StringBuilder buf = new StringBuilder();
	String strippedName = StringUtils.removeEnd
	    (usaElementName.substring (0, usaElementName.indexOf (S_S)),
	     SoaplabConstants.USA_SUFFIX);
	String descName = getName() + S_S + usaElementName + S_SHOWDBDESC;
	String onChange =
	    "injectDB(this.form,this,'" + usaElementName + "','" +
	    strippedName + "','" + descName + "')";
	buf.append (h.gen (TABLE, new String[] { CLASS, "showdb-box" }));
	buf.append (h.gen (TR,
			   h.gen (TD,
				  h.list (new String[] { NAME,     usaElementName + S_SHOWDB,
							 ONCHANGE, onChange,
							 CLASS,    "showdb-select" },
					  showdbComments,
					  showdbNames,
					  null)) +
			   h.gen (TD,
				  h.gen (SPAN, new String[] { ID,    descName,
							      CLASS, "showdb-comment" }, ""))));
	buf.append (h.end (TABLE));
	return buf.toString();
    }

    /*************************************************************************
     *
     *************************************************************************/
    protected String classWrap (String className, String contents) {
	return h.gen (P, new String[] { CLASS, className }, contents);
    }

    /*************************************************************************
     *
     *************************************************************************/
    protected String createSelect (String name,
				   String defaultValue,
				   String[] allowedValues) {
	StringBuilder buf = new StringBuilder();
	if (StringUtils.isNotEmpty (defaultValue)) {
	    buf.append (h.list (name,
				(String[])ArrayUtils.add (allowedValues, 0,
							  DEFAULT_SELECT_TEXT)));
	} else {
	    buf.append (h.list (name, allowedValues));
	}
	return buf.toString();
    }

    /*************************************************************************
     *
     *************************************************************************/
    protected String createRadioPair (String name,
				      String defaultValue) {
	StringBuilder buf = new StringBuilder();
	boolean checked = BooleanUtils.toBoolean (defaultValue);
	buf.append ("yes&nbsp;");
	buf.append (createRadio (name, null, "yes", checked));
	buf.append ("&nbsp;&nbsp;no&nbsp;");
	buf.append (createRadio (name, null, "no", !checked));

	String strippedName = name.substring (0, name.indexOf (S_S));
	if (checked) {
	    buf.append (h.hidden (strippedName + S_DEFAULTVALUE, "yes"));
	} else {
	    buf.append (h.hidden (strippedName + S_DEFAULTVALUE, "no"));
	}

	return buf.toString();
    }

    /*************************************************************************
     *
     *************************************************************************/
    protected String createRadio (String name,
				  String id,
				  String value,
				  boolean checked) {
	List<String> attrs = new ArrayList<String>();
	attrs.add (TYPE);
	attrs.add ("radio");
	attrs.add (NAME);
	attrs.add (name);
	if (StringUtils.isNotBlank (id)) {
	    attrs.add (ID);
	    attrs.add (id);
	}
	attrs.add (VALUE);
	attrs.add (value);
	if (checked) {
	    attrs.add (CHECKED);
	    attrs.add ("checked");
	}
	return (h.gen (INPUT, attrs.toArray (new String[] {}), ""));
    }

    /*************************************************************************
     * It expects an even number of parameters:
     *    attribute name, attribute value,...
     *************************************************************************/
    protected String createText (String name,
				 boolean mandatory,
				 StringBuilder js,
				 String... args) {

	StringBuilder buf = new StringBuilder();
	List<String> attrs = new ArrayList<String>();
	attrs.add (TYPE);
	attrs.add ("text");
	attrs.add (NAME);
	attrs.add (name);

	for (int i = 0; i < args.length - 1; i += 2) {
	    if (args[i].equals (VALUE)) {
		if (StringUtils.isEmpty (args[i+1]))
		    continue;
		String strippedName = name.substring (0, name.indexOf (S_S));
		buf.append (h.hidden (strippedName + S_DEFAULTVALUE, args[i+1]));
	    }
	    attrs.add (args[i]);
	    attrs.add (args[i+1]);
	}
	buf.append (h.gen (INPUT, attrs.toArray (new String[] {}), ""));
	buf.append (createErrorMessageField (name));
	if (mandatory) {
	    js.append ("\"isMandatory (form,'" + name + "')\",\n");
	}
	return buf.toString();
    }

    /*************************************************************************
     * It expects an even number of parameters:
     *    attribute name, attribute value,...
     *************************************************************************/
    protected String createTextWithWarning (String name,
					    boolean mandatory,
					    StringBuilder js,
					    String warningMsg,
					    String... args) {
	StringBuilder buf = new StringBuilder();
	buf.append (createText (name, mandatory, js, args));
	buf.append (h.gen (DIV, new String[] { CLASS, "fixmessage" }, warningMsg));
	return buf.toString();
    }

    /*************************************************************************
     * Return an element whose name is derived from 'name' and has css
     * class suitable for showing error messages.
     *************************************************************************/
    protected String createErrorMessageField (String name) {
	return h.gen (SPAN, new String[] { ID, getName() + S_S + name + S_MESSAGE,
					   CLASS, "smallmessage",
					   STYLE, "display:none" }, "");
    }

    /*************************************************************************
     * It expects an even number of parameters:
     *    attribute name, attribute value,...
     *************************************************************************/
    protected String createTextArea (String name, String... args) {

	List<String> attrs = new ArrayList<String>();
	attrs.add (TYPE);
	attrs.add ("textarea");
	attrs.add (NAME);
	attrs.add (name);

	String value = null;
	for (int i = 0; i < args.length - 1; i += 2) {
	    if (args[i].equals (VALUE)) {
		value = args[i+1];
		continue;
	    }
	    attrs.add (args[i]);
	    attrs.add (args[i+1]);
	}
	if (StringUtils.isEmpty (value)) {
	    return h.gen (TEXTAREA, attrs.toArray (new String[] {}), "");
	} else {
	    String strippedName = name.substring (0, name.indexOf (S_S));
	    StringBuilder buf = new StringBuilder();
	    buf.append (h.hidden (strippedName + S_DEFAULTVALUE, value));
	    buf.append (h.gen (TEXTAREA, attrs.toArray (new String[] {}), value));
	    return buf.toString();
	}
    }

    /*************************************************************************
     *
     *************************************************************************/
    protected String createInputTable (List<String> names,
				       List<String> classes,
				       List<String> uis,
				       int indexOfFirstOptional) {
	StringBuilder buf = new StringBuilder();
	buf.append (h.gen (TABLE, new String[] { CLASS,       "inputs",
						 CELLPADDING, "3" }));
	buf.append ("\n");
	for (int i = 0; i < names.size(); i++) {
	    if (i == indexOfFirstOptional) {
		buf.append (h.gen (TR,
				   h.gen (TD, new String[] { CLASS, "input-separator",
							     COLSPAN, "2" },
					  "mandatory<br><hr>optional")));
		buf.append ("\n");
	    }
	    buf.append (h.gen (TR));
 	    buf.append (h.gen (TD,
			       h.gen (SPAN, new String[] { CLASS, classes.get(i) },
				      names.get(i))));
	    buf.append (h.gen (TD, new String[] { CLASS, "input-ui" },
			       uis.get(i)));
	    buf.append (h.end (TR));
	    buf.append ("\n");
	}
	buf.append (h.end (TABLE));
	return buf.toString();
    }

    /*************************************************************************
     * Limited capability: only introduce checks for formats "%.*[f|d]".
     *************************************************************************/
    protected String createValidText (String name,
            Map<String,Object> input,
				      boolean mandatory,
				      StringBuilder js,
				      String format,
				      String defaultValue) {
	String cssClassName = null;
	Matcher m = Pattern.compile ("%.*([fd])").matcher (format);
	if (m.matches()) {
	    String conversion = m.group (1);
	    if ("f".equals (conversion)) {
		cssClassName = "input-float";
		js.append ("\"isFloat (form,'" + name + "')\",\n");
	    } else {
		cssClassName = "input-integer";
		js.append ("\"isInteger (form,'" + name + "')\",\n");
	    }
	    String min = (String)input.get("minimum");
	    String max = (String)input.get("maximum");
	    js.append ("\"isInTheRange (form,'" + name + "',"+min+","+max+")\",\n");
	} else {
	    // not (yet) supported format
	    cssClassName = "input-text";
	    //TBD: minlength and maxlength values are not yet transferred
	    //to the xml metadata files
	    //String minl = (String)input.get("minlength");
	    //String maxl = (String)input.get("maxlength");
	    //js.append ("\"isStringLengthInTheRange (form,'" + name + "',"
	    //        +minl+","+maxl+")\",\n");

	}
	return createText (name, mandatory, js,
			   VALUE, defaultValue,
			   CLASS, cssClassName);
    }

//     /*************************************************************************
//      *
//      *************************************************************************/
//     protected String getOutputsPanel (Map<String,Object> app,
// 				      Map<String,Object>[] outputs) {

// 	boolean emboss = BooleanUtils.toBoolean
// 	    ((String)app.get (SoaplabConstants.ANALYSIS_EMBOSS));

// 	sort (outputs);
// 	StringBuilder tooltips = new StringBuilder();
// 	StringBuilder buf = new StringBuilder();
// 	buf.append (h.gen (TABLE, new String[] { CLASS,       "outputs",
// 						 CELLPADDING, "3" }));
// 	buf.append ("\n");

// 	for (Map<String,Object> output: outputs) {
// 	    String name =(String)output.get ("name");

// 	    // ignore EMBOSS graphical outputs, except for one
// 	    if (emboss && name.matches ("^Graphics_in_.+")) {
// 		continue;
// 	    }

// 	    buf.append (h.gen (TR));
// 	    buf.append (h.gen (TD, createResultCheckbox (name + S_OUTPUT,
// 							 true,
// 							 null)));;

// 	    String cssClass = createOutputTooltipAndClass (name, output, tooltips);

// 	    buf.append (h.gen (TD, new String[] { CLASS, cssClass }, name));
// 	    buf.append (h.end (TR));
// 	    buf.append ("\n");
// 	}
// 	buf.append (h.gen (TR,
// 			   h.gen (TD, new String[] { CLASS, "output-separator",
// 						     COLSPAN, "2" }, "<hr>")));
// 	buf.append (h.end (TABLE));
// 	buf.append ("\n");
// 	buf.append (tooltips);

// 	return buf.toString();
//     }

//     /*************************************************************************
//      *
//      *************************************************************************/
//     protected String createResultCheckbox (String name,
// 					   boolean checked,
// 					   String onClick) {
// 	List<String> attrs = new ArrayList<String>();
// 	attrs.add (NAME);
// 	attrs.add (name);
// 	attrs.add (VALUE);
// 	attrs.add ("true");
// 	attrs.add (CLASS);
// 	attrs.add ("output-chbox");
// 	if (checked) {
// 	    attrs.add (CHECKED);
// 	    attrs.add ("checked");
// 	}
// 	if (onClick != null) {
// 	    attrs.add (ONCLICK);
// 	    attrs.add (onClick);
// 	}
// 	return (h.checkbox ("results", attrs.toArray (new String[] {})));
//     }

    /*************************************************************************
     *
     *************************************************************************/
    protected String getHelpPanel (Map<String,Object> app) {
	String name = (String)app.get (SoaplabConstants.ANALYSIS_NAME);
	String desc = (String)app.get (SoaplabConstants.ANALYSIS_DESCRIPTION);
	String helpURL = (String)app.get (SoaplabConstants.ANALYSIS_HELP_URL);

	StringBuilder buf = new StringBuilder();
	buf.append (h.gen (H5, "Service: " + name));
	if (StringUtils.isNotBlank (desc)) {
	    buf.append (h.gen (P, desc));
	}
	//	buf.append (h.gen (P, h.gen (B, "Properties")));
	buf.append (h.gen (CENTER, h.gen (H5, "Properties")));
	buf.append (h.gen (CENTER,
			   h.gen (DIV, new String[] { CLASS, "smallnote" },
				  "[ Some of these properties may be useful only for developers... ]")));
	buf.append (h.gen (TABLE));
	buf.append ("\n");
	for (Iterator<String> it = app.keySet().iterator(); it.hasNext(); ) {
	    String key = it.next();
	    if (key.equals (SoaplabConstants.ANALYSIS_NAME)) continue;
	    if (key.equals (SoaplabConstants.ANALYSIS_DESCRIPTION)) continue;
	    if (key.equals (SoaplabConstants.ANALYSIS_HELP_URL)) continue;
	    buf.append (h.gen (TR, new String[] { CLASS, "app-properties" }));
	    buf.append (h.gen (TH, new String[] { ALIGN, "left", VALIGN, "top" },
			       WordUtils.capitalize (key)));
	    buf.append (h.gen (TD, new String[] { ALIGN, "left" }, (String)app.get (key)));
	    buf.append (h.end (TR));
	    buf.append ("\n");
	}
	buf.append (h.end (TABLE));
	if (StringUtils.isNotBlank (helpURL)) {
	    buf.append (h.gen (SPAN, new String[] { CLASS, "note-link" },
			       h.a (helpURL, "_blank", "More...")));
	}
	return buf.toString();
    }


    // missing in HtmlConstants
    private static final String ID = "id";
    private static final String CENTER = "center";
    private static final String SPAN = "span";
    private static final String CLASS = "class";
    private static final String STYLE = "style";
    private static final String TEXTAREA = "TEXTAREA";
    private static final String ONFOCUS = "onFocus";
    private static final String ONCHANGE = "onChange";

}
