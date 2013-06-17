// MetadataAccessorXML.java
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

import org.soaplab.services.metadata.deprecated.EventDef;
import org.soaplab.services.metadata.deprecated.ActionDef;
import org.soaplab.services.metadata.deprecated.ResultDef;

import org.soaplab.services.AnalysisInventoryProvider;
import org.soaplab.services.Config;
import org.soaplab.share.SoaplabException;
import org.soaplab.share.SoaplabConstants;

import org.tulsoft.shared.UUtils;
import org.tulsoft.shared.GException;
import org.tulsoft.tools.xml.XMLUtils2;
import org.tulsoft.tools.xml.XMLErrorHandler;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.io.FileUtils;

import org.xml.sax.SAXException;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;

import java.util.Iterator;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Stack;
import java.util.Set;
import java.util.HashSet;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import java.io.File;

/**
 * A default implementation of MetadataAccessor interface that reads
 * service metadata from XML files. <p>
 *
 * How does it find metadata for its service?<p>
 *
 * It uses singleton {@link org.soaplab.services.AnalysisInventoryProvider
 * AnalysisInventoryProvider} to get list of all available analyses
 * (possibly from more than one provider, i.e. possibly from more
 * analysis lists). It looks there for its analysis (whose name was
 * given in the {@link #MetadataAccessorXML constructor}). An analysis
 * info (a container class {@link AnalysisInstallation} contains also
 * a poiner to an XML metadata file describing this analysis in all
 * details. <p>
 *
 * The pointer to an XML metadata file can be a URL, an absolute path,
 * or a relative path. In the last case, it is expanded to an absolute
 * path using Soaplab property {@link
 * org.soaplab.services.Config#PROP_METADATA_DIR
 * Config.PROP_METADATA_DIR}, or even {@link
 * org.soaplab.services.Config#PROP_METADATA_FILE
 * Config.PROP_METADATA_FILE}. In any case, if the path is still not
 * absolute, it is tried to locate the file as a class resource. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: MetadataAccessorXML.java,v 1.23 2008/10/07 10:14:31 mahmutuludag Exp $
 */
public class MetadataAccessorXML
    extends XMLErrorHandler
    implements MetadataAccessor {

    private static org.apache.commons.logging.Log log =
	org.apache.commons.logging.LogFactory.getLog (MetadataAccessorXML.class);

    // metadata as parsed data structures
    protected AnalysisDef _app;     // general description of the analysis
    protected ParamDef[]  _params;  // definitions of all parameters
    protected EventDef    _event;   // what and how to execute, etc.
    protected InputPropertyDef[]  _inputs;
    protected OutputPropertyDef[] _outputs;

    // given in constructor
    protected String serviceName;
    protected String module;   // TBD perhaps otherwise

    // where are metadata (full path, please)
    protected String metadataFile;

    /******************************************************************************
     * A real constructor. It loads metadata for the given service.
     ******************************************************************************/
    public MetadataAccessorXML (String serviceName)
	throws SoaplabException {

	this.serviceName = serviceName;

	AnalysisInstallation app =
	    AnalysisInventoryProvider.instance().getServiceInstallation (serviceName);
	if (app == null)
	    throw new SoaplabException ("Unknown service '" + serviceName + "'");
	module = app.getModule();
	metadataFile = locateMetadata (app);
	if (metadataFile == null)
	    throw new SoaplabException ("Missing metadata for service " + serviceName);
	log.info ("Service metadata file: " + metadataFile);
        load (metadataFile);
    }

    /******************************************************************************
     *
     ******************************************************************************/
    protected String locateMetadata (AnalysisInstallation app) {

	String filename = null;
	String appName = app.getAppName();
	String catName = app.getCategory();

	// direct property has precedence
	filename = Config.get().getString (serviceName + Config.PROP_METADATA_FILE);
	if (filename == null)
	    filename = Config.get().getString (catName + "." + appName +
					       Config.PROP_METADATA_FILE);

	// then, take it from the analysis list
	if (filename == null) {
	    filename = app.getXMLDesc();

	    // finally, just guess it
	    if (filename == null) {
		filename = catName + System.getProperty ("file.separator") +
		    appName + "_al.xml";
	    }
	}

	// having a filename, let's check whether it is a URL
	try {
	    URL aURL = new URL (filename);
	    return aURL.toExternalForm();
	} catch (MalformedURLException e2) {
	}

	// having a filename, we need a path to it
	File file = new File (filename);
	if (file.isAbsolute())
	    return filename;
	String[] dirs = Config.get().getStringArray (Config.PROP_METADATA_DIR);
	for (int i = 0; i < dirs.length; i++) {
	    file = new File (dirs[i], filename);
	    if (log.isDebugEnabled())
		log.debug ("Trying " + file.getAbsolutePath());
	    if (file.exists())
		return file.getAbsolutePath();

	    // last resort: try to find it as this class resource
	    URL url = this.getClass().getClassLoader().getResource (filename);
	    if (url == null) {
		// try the next metadata directory
		continue;
	    }
	    return url.toExternalForm();
	}

	return null;
    }


    /******************************************************************************
     * 
     * Methods of AnalysisMetadataAccessor interface
     *
     * Most of these public methods are synchronized in order to
     * prevent returning uncomplete data if the parsing of metadata is
     * in progress.
     *
     ******************************************************************************/
    public synchronized AnalysisDef getAnalysisDef() {
	if (_app == null)
	    return null;
	if (StringUtils.isBlank (_app.module))
	    _app.module = this.module;
	return _app;
    }
    public synchronized ParamDef[]  getParamDefs() {
        if (_params == null)
	    return new ParamDef[] {};
	return _params;
    }

    public synchronized InputPropertyDef[] getInputDefs() {
	if (_inputs == null)
	    return new InputPropertyDef[] {};
	return _inputs;
    }

    public synchronized OutputPropertyDef[] getOutputDefs() {
	if (_outputs == null)
	    return new OutputPropertyDef[] {};
	return _outputs;
    }

    public String describe()
	throws SoaplabException {
	if (metadataFile == null)
	    return ""; // should not happen, should it? :-)
	try {
	    try {
		File mFile = new File (metadataFile);
		return FileUtils.readFileToString (mFile);
	    } catch (java.io.FileNotFoundException e2) {
		// try URL
		File tmpFile = File.createTempFile (getServiceName(),
						    ".xml");
		FileUtils.copyURLToFile (new URL (metadataFile), tmpFile);
		String mData = FileUtils.readFileToString (tmpFile);
		tmpFile.delete();
		return mData;
	    }
	} catch (Exception e) {
	    throw new SoaplabException ("Error by re-reading XML input: " + e.toString());
	}
    }

    public String getServiceName()
	throws SoaplabException {
	return serviceName;
    }

    /**************************************************************************
     * Check if loaded metadata contains all necessary tags.
     **************************************************************************/
    public void checkMetadata()
	throws SoaplabException {

	Vector<String> errors = new Vector<String>();

        if (analysis == null) {
	    errors.addElement ("No 'analysis' tag found.");
	} else {
	    if (analysis.type == null)
		errors.addElement ("An analysis must have a type defined.");
            if (analysis.category == null)
		errors.addElement ("An analysis must have a category defined.");
            if (analysis.appName == null)
		errors.addElement ("An analysis must have a name defined.");
	}

        if (event == null) {
	    errors.addElement ("No 'event' tag found.");
	} else {
            if (event.actions == null || event.actions.length == 0)
		errors.addElement ("An event must have at least one action.");
	}

	// any errors found?
	if (errors.size() > 0) {
	    StringBuilder buf = new StringBuilder();
	    for (Enumeration en = errors.elements(); en.hasMoreElements(); )
		buf.append ("\n\t" + (String)en.nextElement());
	    throw new SoaplabException ("Errors in XML input:" + buf.toString());
	}
    }

    /**************************************************************************
     * parsing an XML input
     **************************************************************************/
    private synchronized void load (String xmlFilename)
	throws SoaplabException {

	InputSource xmlSource = new InputSource (xmlFilename);

	try {
	    // Create the parser and register handlers
	    XMLReader parser = XMLUtils2.makeXMLParser (this);

	    // Parse it!
	    parser.parse (xmlSource);

	} catch (GException e) {
	    throw new SoaplabException ("Error in creating XML parser " + e.getMessage());
	} catch (SAXException e) {
	    throw new SoaplabException ("Error in the XML input.\n" +
					 XMLUtils2.getFormattedError (e));
	} catch (IOException e) {
	    throw new SoaplabException ("Error by reading XML input: " + e.getMessage());

	} catch (Error e) {
	    throw new SoaplabException ("Serious or unexpected error!\n" + e.toString(), e);
	}
    }


    /*********************************************************************
     * setDocumentLocator()
     ********************************************************************/
    Locator locator;

    public void setDocumentLocator (Locator l) {
        this.locator = l;
    }

    /*********************************************************************
     * Globals used during XML parsing - it is thread safe because they
     *    all are used from Praser.parse() method which is called only
     *    from a synchronized method load(), and the Parser itself does
     *    not use any threads (at least I hope :-)).
     ********************************************************************/

    // here we build the resulting data structures
    // (we will move them to _* variables at the end of parsed document)
    AnalysisDef analysis;
    Vector<ParamDef> params;
    EventDef event;

    Stack<Vector<ParamDef>> paramStack;
    ParamDef currParam;
    RepeatableDef currRepeat;

    Vector<ActionDef> actions;
    ActionDef currAction;

    Vector<ResultDef> results;
    ResultDef currResult;

    Vector<ListDef> lists;
    ListDef currList;
    Vector<ListItemDef> listItems;

    Vector<CondDef> conditions;
    CondDef currCondition;

    Stack<PropertiesBag> optionStack;

    Stack<StringBuffer> pcdataStack;

    Vector<InputPropertyDef> inputs;
    Vector<String> allowed;
    InputPropertyDef currInputPropertyDef;
    Vector<OutputPropertyDef> outputs;
    OutputPropertyDef currOutputPropertyDef;
    String lastType;

    // keep track where are we at the moment
    TagTracer tt;

    /*********************************************************************
     * Recognized tag paths (used by TagTracer)
     *    Note that paths are not used for 'condition' and 'option' -
     *    they are on too many places.
     ********************************************************************/
    private static final String[] ANALYSIS_PATH        = new String[] { "DsLSRAnalysis", "analysis" };
    private static final String[] EXT_PATH             = AA (ANALYSIS_PATH,    new String[] { "analysis_extension" });
    private static final String[] APP_INFO_PATH        = AA (EXT_PATH,         new String[] { "app_info" });
    private static final String[] EVENT_PATH           = AA (EXT_PATH,         new String[] { "event" });
    private static final String[] ACTION_PATH          = AA (EVENT_PATH,       new String[] { "action" });
    private static final String[] PARAMETER_PATH       = AA (EXT_PATH,         new String[] { "parameter" });
    private static final String[] BASE_PARAM_PATH      = AA (PARAMETER_PATH,   new String[] { "base" });
    private static final String[] STANDARD_PATH        = AA (PARAMETER_PATH,   new String[] { "standard" });
    private static final String[] RANGE_PATH           = AA (PARAMETER_PATH,   new String[] { "range" });
    private static final String[] DATA_PATH            = AA (PARAMETER_PATH,   new String[] { "data" });
    private static final String[] RESULT_PATH          = AA (DATA_PATH,        new String[] { "result" });
    private static final String[] CHOICE_PATH          = AA (PARAMETER_PATH,   new String[] { "choice" });
    private static final String[] CHOICE_LIST_PATH     = AA (PARAMETER_PATH,   new String[] { "choice_list" });
    private static final String[] SUB_CHOICE_PATH      = AA (CHOICE_PATH,      new String[] { "base" });
    private static final String[] SUB_CHOICE_LIST_PATH = AA (CHOICE_LIST_PATH, new String[] { "base" });
    private static final String[] INPUT_PATH           = AA (ANALYSIS_PATH,    new String[] { "input" });
//    private static final String[] ALLOWED_PATH         = AA (INPUT_PATH,       new String[] { "allowed" });
    private static final String[] OUTPUT_PATH          = AA (ANALYSIS_PATH,    new String[] { "output" });

    private static String[] AA (String[] arr1, String[] arr2) {
	return (String[])ArrayUtils.addAll (arr1, arr2);
    }

    /*********************************************************************
     * startDocument()
     ********************************************************************/
    public void startDocument() {
	analysis = null;
	params = new Vector<ParamDef>();
	event = null;
        tt = new TagTracer();
        optionStack = new Stack<PropertiesBag>();
        paramStack = new Stack<Vector<ParamDef>>();
        pcdataStack = new Stack<StringBuffer>();

	// this should not be necessary, but because we do not use 'tag pathes'
	// for 'conditions' (and 'options') we need to take extra precaution
        conditions = new Vector<CondDef>();
    }

    /*********************************************************************
     * endDocument()
     ********************************************************************/
    public void endDocument() {

	// parser created all options with String type but for some we need
	// different types - so change it now
        if (analysis != null)
	    updatePropertyTypes (analysis.options);
        if (event != null) {
	    updatePropertyTypes (event.options);
	    if (event.actions != null)
		for (int i = 0; i < event.actions.length; i++)
		    updatePropertyTypes (event.actions[i].options);
	}
        for (Enumeration<ParamDef> en = params.elements(); en.hasMoreElements(); ) {
	    ParamDef pd = en.nextElement();
            updatePropertyTypes (pd.options);
	}

	// move created data structure to their right place
        try {
	    checkMetadata();
            _app = analysis;
	    _event = event;
	    _params = new ParamDef [params.size()];
	    params.copyInto (_params);

	    // deprecated EventDef and ActionDef
	    // (copying their fileds directly to AnalysisDef)
	    _app.conds = _event.conds;
	    ActionDef actionDef = _event.getMainActionDef();
	    if (actionDef != null) {
		_app.method = actionDef.method;
		_app.launcher = actionDef.launcher;
		_app.file = actionDef.file;
		_app.options.addProperties (actionDef.options);
	    }

	    // propagate (some) options defined for the whole analysis
	    // to the individual parameters
	    for (Iterator it = _app.options.keySet().iterator(); it.hasNext(); ) {
		String key = (String)it.next();
		if (sharedAttrs.contains (key)) {
		    Object value = _app.options.get (key);
		    for (ParamDef param: _params) {
			if (! param.options.containsKey (key))
			    param.options.put (key, value);
		    }
		}
	    }

	} catch (SoaplabException e) {
	}
        _inputs = new InputPropertyDef [inputs.size()];
        inputs.copyInto (_inputs);
        _outputs = new OutputPropertyDef [outputs.size()];
        outputs.copyInto (_outputs);
    }

    private static Set<String> sharedAttrs = new HashSet<String>();
    static {
        sharedAttrs.add (ParamDef.TAGSEPAR);
        sharedAttrs.add (ParamDef.USE_DEFAULTS);
    }

    /*********************************************************************
     * startElement()
     ********************************************************************/
    public void startElement (String namespaceURI, String name, String qName,
			      Attributes attrs) {
        tt.push (name);

	if (tt.is (ANALYSIS_PATH)) {
            analysis = new AnalysisDef();
            inputs = new Vector<InputPropertyDef>();
            outputs = new Vector<OutputPropertyDef>();
	    for (int i = 0; i < attrs.getLength(); i++) {
                String key = attrs.getLocalName(i);
		String value = attrs.getValue(i);
                if      (key.equals ("type")) analysis.type = value;
                else if (key.equals ("name")) analysis.appName = value;
                else                          analysis.options.put (key, value);
	    }

	} else if (tt.is (INPUT_PATH)) {
            lastType = "";   // will be used by sub-tags 'default' and 'allowed'
	    currInputPropertyDef = new InputPropertyDef();
	    for (int i = 0; i < attrs.getLength(); i++) {
		String key = attrs.getLocalName(i);
		String value = attrs.getValue(i);
		if      (key.equals ("name"))      currInputPropertyDef.name = value;
		else if (key.equals ("type")) {

		    // the type - if it is not a pure IDL-based type - may content
		    // a semantic prefix folowed by a slash - and that should be
		    // removed otherwise TCUtils will not understand the type
		    if (!value.startsWith ("IDL:")) {
			int pos = value.lastIndexOf ("/");
			if (pos > -1 && pos < value.length() - 1)
			    value = value.substring (pos + 1);
		    }

		    lastType = value;
		    currInputPropertyDef.type = value;
		} else if (key.equals ("mandatory"))
		    currInputPropertyDef.mandatory = UUtils.is (value);

	    }
            allowed = new Vector<String>();

	} else if (tt.is (OUTPUT_PATH)) {
            lastType = "";
	    currOutputPropertyDef = new OutputPropertyDef();
	    for (int i = 0; i < attrs.getLength(); i++) {
		String key = attrs.getLocalName(i);
		String value = attrs.getValue(i);
		if      (key.equals ("name"))      currOutputPropertyDef.name = value;
		else if (key.equals ("type")) {
		    lastType = value;
		    currOutputPropertyDef.type = value;
		}
	    }

	} else if (tt.is (EXT_PATH)) {
	    paramStack.push (new Vector<ParamDef>());
	    optionStack.push (new PropertiesBag());

	} else if (tt.is (APP_INFO_PATH)) {
	    for (int i = 0; i < attrs.getLength(); i++) {
		String key = attrs.getLocalName(i);
		String value = attrs.getValue(i);
		if (key.equals ("category")) analysis.category = value;
		else                         analysis.options.put (key, value);
	    }

	} else if (tt.is (EVENT_PATH)) {
	    event = new EventDef();
            copyToOptions (attrs, event.options);
            actions = new Vector<ActionDef>();
            conditions = new Vector<CondDef>();

	} else if (tt.is (ACTION_PATH)) {
	    currAction = new ActionDef();
	    for (int i = 0; i < attrs.getLength(); i++) {
		String key = attrs.getLocalName(i);
		String value = attrs.getValue(i);
		if      (key.equals ("type"))     currAction.type = ActionDef.checkAndGetType (value);
		else if (key.equals ("launcher")) currAction.launcher = value;
		else if (key.equals ("file"))     currAction.file = value;
		else if (key.equals ("method"))   currAction.method = value;
		else                              currAction.options.put (key, value);
	    }
            optionStack.push (new PropertiesBag());

	} else if (name.equals ("condition")) {
            if (currCondition == null) {
		currCondition = new CondDef();
		for (int i = 0; i < attrs.getLength(); i++) {
		    String key = attrs.getLocalName(i);
		    String value = attrs.getValue(i);
		    if (key.equals (CondDef.OWNER))
			currCondition.options.put (key, new Integer (CondDef.checkAndGetOwner (value)));
		    else if (key.equals (CondDef.VERLEVEL))
			currCondition.options.put (key, new Integer (CondDef.checkAndGetLevel (value)));
		    else
			currCondition.options.put (key, value);
		}
		optionStack.push (new PropertiesBag());
	    }

	} else if (name.equals ("option")) {
            if (! optionStack.empty()) {
		PropertiesBag currOptions = optionStack.peek();
                String key = attrs.getValue ("name");
		if (StringUtils.isNotBlank (key)) {
		    String value = attrs.getValue ("value");
// 		    if (StringUtils.isBlank (value)) value = "true";
		    if (StringUtils.isBlank (value)) value = "";
		    currOptions.put (key, value);
		}
	    }

	} else if (name.equals ("repeatable")) {
	    if (currRepeat != null) {
		for (int i = 0; i < attrs.getLength(); i++) {
		    String key = attrs.getLocalName(i);
		    String value = attrs.getValue(i);
		    if      (key.equals ("min")) currRepeat.minRep = NumberUtils.toInt (value);
		    else if (key.equals ("max")) currRepeat.maxRep = NumberUtils.toInt (value);
		    else currRepeat.options.put (key, value);
		}
                optionStack.push (new PropertiesBag());
	    }

	} else if (name.equals ("list")) {
	    if (currList == null) {
                currList = new ListDef();
		for (int i = 0; i < attrs.getLength(); i++) {
		    String key = attrs.getLocalName(i);
		    String value = attrs.getValue(i);
		    if      (key.equals ("name"))  currList.name = value;
		    else if (key.equals ("type"))  currList.type = ListDef.checkAndGetType (value);
		    else if (key.equals ("slice")) currList.slice = Math.abs (NumberUtils.toInt (value));
		}
                listItems = new Vector<ListItemDef>();
	    }

	} else if (name.equals ("list_item")) {
	    if (listItems != null) {
                ListItemDef currItem = new ListItemDef();
		for (int i = 0; i < attrs.getLength(); i++) {
		    String key = attrs.getLocalName(i);
		    String value = attrs.getValue(i);
		    if      (key.equals ("value"))    currItem.value = value;
		    else if (key.equals ("shown_as")) currItem.shown_as = value;
		    else if (key.equals ("type"))     currItem.type = ListItemDef.checkAndGetType (value);
		    else if (key.equals ("level"))    currItem.level = Math.abs (NumberUtils.toInt (value));
		}
                listItems.addElement (currItem);
	    }

	} else if (tt.is (PARAMETER_PATH)) {
	    paramStack.push (new Vector<ParamDef>());    // a space for the 'base' part of this parameter

	} else if (tt.is (STANDARD_PATH)) {
            currParam = new StdParamDef();
            currParam.createdFor = ParamDef.STANDARD_PARAMETER;
            copyToOptions (attrs, currParam.options);
            currRepeat = new RepeatableDef();
            lists = new Vector<ListDef>();

	} else if (tt.is (RANGE_PATH)) {
            currParam = new StdParamDef();
            currParam.createdFor = ParamDef.RANGE_PARAMETER;
            copyToOptions (attrs, currParam.options);
            currRepeat = new RepeatableDef();
            lists = new Vector<ListDef>();

	} else if (tt.is (DATA_PATH)) {
            currParam = new IOParamDef();
            currParam.createdFor = ParamDef.IO_PARAMETER;
	    for (int i = 0; i < attrs.getLength(); i++) {
		String key = attrs.getLocalName(i);
		String value = attrs.getValue(i);
		if      (key.equals ("iotype"))   ((IOParamDef)currParam).ioType = IOParamDef.checkAndGetIOType (value);
		else if (key.equals ("ioformat")) ((IOParamDef)currParam).ioFormat = IOParamDef.checkAndGetIOFormat (value);
		else                              currParam.options.put (key, value);
	    }
            currRepeat = new RepeatableDef();
            results = new Vector<ResultDef>();
            lists = new Vector<ListDef>();

	} else if (tt.is (RESULT_PATH)) {
	    currResult = new ResultDef();
	    for (int i = 0; i < attrs.getLength(); i++) {
		String key = attrs.getLocalName(i);
		String value = attrs.getValue(i);
		if      (key.equals ("type"))         currResult.type = value;
		else if (key.equals ("name"))         currResult.name = value;
		else if (key.equals ("special_type")) currResult.specialType = ResultDef.checkAndGetType (value);
		else                                  currResult.options.put (key, value);
	    }
            optionStack.push (new PropertiesBag());

	} else if (tt.is (CHOICE_PATH) || tt.is (CHOICE_LIST_PATH)) {
            currParam = new ChoiceParamDef();
            currParam.createdFor = (name.equals ("choice") ?
				    ParamDef.CHOICE_PARAMETER :
                                    ParamDef.CHOICE_LIST_PARAMETER);
	    for (int i = 0; i < attrs.getLength(); i++) {
		String key = attrs.getLocalName(i);
		String value = attrs.getValue(i);
		if (key.equals ("radiotype") || key.equals ("grouptype"))
                    ((ChoiceParamDef)currParam).choiceType = ChoiceParamDef.checkAndGetType (value);
		else currParam.options.put (key, value);
	    }
	    paramStack.push (new Vector<ParamDef>());    // and create a place for sub-parameters

	} else if (tt.is (BASE_PARAM_PATH) || tt.is (SUB_CHOICE_PATH) || tt.is (SUB_CHOICE_LIST_PATH)) {
            ParamDef p = new ParamDef();
	    for (int i = 0; i < attrs.getLength(); i++) {
		String key = attrs.getLocalName(i);
		String value = attrs.getValue(i);
		if      (key.equals ("name")) p.id = value;
		else if (key.equals ("type")) p.type = value;
		else                          p.options.put (key, value);
	    }
	    Vector<ParamDef> v = paramStack.peek();
            v.addElement (p);
            optionStack.push (new PropertiesBag());   // be prepared for parameter's options
            conditions = new Vector<CondDef>();       // and for parameter's conditions

	} else if (name.equals ("default")     ||
                   name.equals ("description") ||
                   name.equals ("allowed")     ||
                   name.equals ("prompt")      ||
                   name.equals ("help")) {
	    pcdataStack.push (new StringBuffer());
	}

    }

    /*********************************************************************
     * endElement()
     ********************************************************************/
    public void endElement (String namespaceURI, String name, String qName) {

	if (tt.is (INPUT_PATH)) {

	    // 'possible_values' are always dealt with only here  because
	    // we had to wait for all of them first
	    currInputPropertyDef.possibleValues = new String [allowed.size()];
            allowed.copyInto (currInputPropertyDef.possibleValues);
            allowed = null;

	    inputs.addElement (currInputPropertyDef);
	    currInputPropertyDef = null;

	} else if (tt.is (OUTPUT_PATH)) {
	    outputs.addElement (currOutputPropertyDef);
	    currOutputPropertyDef = null;

	} else if (tt.is (EXT_PATH)) {
	    analysis.add (optionStack.pop());

	} else if (tt.is (EVENT_PATH)) {
            event.actions = new ActionDef [actions.size()];
            actions.copyInto (event.actions);
            actions.removeAllElements();

            event.conds = new CondDef [conditions.size()];
            conditions.copyInto (event.conds);
            conditions.removeAllElements();

	} else if (tt.is (ACTION_PATH)) {
	    currAction.add (optionStack.pop());
	    actions.addElement (currAction);
	    currAction = null;

	} else if (tt.is (PARAMETER_PATH)) {
	    Vector<ParamDef> base = paramStack.pop();   // should be always with 1 element
            ParamDef baseParam = base.elementAt(0);
            params.addElement (mergeDef (baseParam, currParam));
            currParam = null;

	} else if (tt.is (STANDARD_PATH) || tt.is (RANGE_PATH)) {
            ((StdParamDef)currParam).repeat = currRepeat;
            currRepeat = null;
            ((StdParamDef)currParam).lists = new ListDef [lists.size()];
            lists.copyInto (((StdParamDef)currParam).lists);
            lists = null;

	} else if (tt.is (DATA_PATH)) {
            ((IOParamDef)currParam).repeat = currRepeat;
            currRepeat = null;
            ((StdParamDef)currParam).lists = new ListDef [lists.size()];
            lists.copyInto (((StdParamDef)currParam).lists);
            lists = null;

	    // ResultDef is deprecated - therefore:
	    // - copy results options to the current IOPramDef
	    for (Enumeration<ResultDef> en = results.elements(); en.hasMoreElements(); ) {
		ResultDef rd = en.nextElement();
		currParam.options.addProperties (rd.options);
		if (rd.specialType == ResultDef.URL)
		    currParam.options.put (IOParamDef.SPECIAL_TYPE, "url");
	    }
            results.removeAllElements();

	} else if (tt.is (RESULT_PATH)) {
	    currResult.add (optionStack.pop());
	    results.addElement (currResult);
	    currResult = null;

	} else if (tt.is (CHOICE_PATH) || tt.is (CHOICE_LIST_PATH)) {
	    Vector<ParamDef> bools = paramStack.pop();
            ((ChoiceParamDef)currParam).bools = new ParamDef [bools.size()];
            bools.copyInto (((ChoiceParamDef)currParam).bools);

	} else if (tt.is (BASE_PARAM_PATH) || tt.is (SUB_CHOICE_PATH) || tt.is (SUB_CHOICE_LIST_PATH)) {
	    Vector<ParamDef> v = paramStack.peek();
            ParamDef p = v.lastElement();

            p.conds = new CondDef [conditions.size()];
            conditions.copyInto (p.conds);
            conditions.removeAllElements();

	    p.add (optionStack.pop());

	} else if (name.equals ("condition")) {
            if (currCondition != null) {
	        currCondition.options.addProperties (optionStack.pop());
		conditions.addElement (currCondition);
		currCondition = null;
	    }

	} else if (name.equals ("repeatable")) {
            if (currRepeat != null) {
                currRepeat.options.addProperties (optionStack.pop());
	    }

	} else if (name.equals ("list")) {
            if (currList != null) {
		currList.items = new ListItemDef [listItems.size()];
                listItems.copyInto (currList.items);
		lists.addElement (currList);
		currList = null;
                listItems = null;
	    }
               
	} else if (name.equals ("default")) {
	    StringBuffer buf = pcdataStack.pop();

	    // we can be inside AppLab extensions...
            if (! paramStack.empty()) {
                Vector<ParamDef> v = paramStack.peek();
                if (v.size() > 0) {
		    ParamDef p = v.lastElement();
		    p.dflt = new String (buf);
		}

	    // ...or we are in the BSA standard part
	    } else if (currInputPropertyDef != null) {
		currInputPropertyDef.defaultValue = new String (buf);
  	    }

	} else if (name.equals ("description")) {
	    StringBuffer buf = pcdataStack.pop();
	    analysis.options.put (SoaplabConstants.ANALYSIS_DESCRIPTION, new String (buf));

	} else if (name.equals ("allowed")) {
	    StringBuffer buf = pcdataStack.pop();
            if (allowed != null)
		allowed.addElement (new String (buf));

	} else if (name.equals ("prompt")) {
	    StringBuffer buf = pcdataStack.pop();
            if (! paramStack.empty()) {
                Vector<ParamDef> v = paramStack.peek();
                if (v.size() > 0) {
		    ParamDef p = v.lastElement();
                    p.options.put (SoaplabConstants.DATA_PROMPT, new String (buf));
		}
	    }

	} else if (name.equals ("help")) {
	    StringBuffer buf = pcdataStack.pop();
            if (! paramStack.empty()) {
                Vector<ParamDef> v = paramStack.peek();
                if (v.size() > 0) {
		    ParamDef p = v.lastElement();
                    p.options.put (SoaplabConstants.DATA_HELP, new String (buf));
		}
	    }
	}

        tt.pop();
    }

    /*********************************************************************
     * characters()
     ********************************************************************/
    public void characters (char[] ch, int start, int length) {
        if (pcdataStack.empty()) return;
        StringBuffer buf = pcdataStack.peek();
        buf.append (ch, start, length);
    }

    /*********************************************************************
     * copyToOptions()
     ********************************************************************/
    protected static void copyToOptions (Attributes attrs, PropertiesBag options) {
        for (int i = 0; i < attrs.getLength(); i++) {
	    String key = attrs.getLocalName(i);
	    String value = attrs.getValue(i);
	    options.put (key, value);
	}
    }

    /*********************************************************************
     * mergeDef()
     *    Each parameter was created in two parts - a base and a specific
     *    one. This method merges the basic part into the specific one,
     *    and returns the (enriched) part - which is now a full parameter.
     ********************************************************************/
    private static ParamDef mergeDef (ParamDef base, ParamDef specific) {
	if (specific == null)
	    return base;
	else {
	    specific.add (base.options);
	    specific.conds = base.conds;
	    specific.id = base.id;
	    specific.type = base.type;
	    specific.dflt = base.dflt;
	    return specific;
	}
    }

    /*********************************************************************
     * updatePropertyTypes()
     *    Check all properties 'props' if they should be of a different
     *    type than String (they all are now stored as Strings). If yes,
     *    change the value to a proper type.
     *
     *    The expected types for individual property names are available
     *    in 'propTypes'. This hashtable should be updated whenever a new
     *    property name with a non-string type is added.
     ********************************************************************/
    private static final int BOOLEAN_TYPE = 1;
    private static final int INTEGER_TYPE = 2;
    private static final int LONG_TYPE    = 3;

    private static Hashtable<String,Integer> propTypes = new Hashtable<String,Integer>();
    static {
        propTypes.put (SoaplabConstants.INPUT_MANDATORY,    new Integer (BOOLEAN_TYPE));
        propTypes.put (ParamDef.NODISPLAY,                  new Integer (BOOLEAN_TYPE));
        propTypes.put (ParamDef.READONLY,                   new Integer (BOOLEAN_TYPE));
        propTypes.put (ParamDef.USE_DEFAULTS,               new Integer (BOOLEAN_TYPE));
        propTypes.put (SoaplabConstants.DEFAULT_FOR_DIRECT, new Integer (BOOLEAN_TYPE));
        propTypes.put (SoaplabConstants.ANALYSIS_EMBOSS,    new Integer (BOOLEAN_TYPE));
        propTypes.put (ParamDef.ORDERING,                   new Integer (INTEGER_TYPE));
    }

    protected void updatePropertyTypes (PropertiesBag props) {
	String key, value;
        for (Enumeration en = props.keys(); en.hasMoreElements(); ) {
	    key = (String)en.nextElement();
	    if (propTypes.containsKey (key)) {
		value = (String)props.get (key);
		switch (((Integer)propTypes.get (key)).intValue()) {
		case BOOLEAN_TYPE:
                    props.put (key, new Boolean (UUtils.is (value)));
		    break;
		case INTEGER_TYPE:
                    props.put (key, new Integer (NumberUtils.toInt (value)));
		    break;
		case LONG_TYPE:
                    props.put (key, new Long (toLong (value)));
		    break;
		}
	    }
	}
    }

    /*****************************************************************************
     * Return a long number created from a string, or zero if string is wrong.
     *
     * @param str a string containing digits (and "digit usual" characters)
     * @return a converted integer or zero if string was not so numeric
     *****************************************************************************/
    long toLong (String str) {
	long i;
	try {
	    if (str == null) i = 0;
	    else             i = Long.valueOf (str).longValue();
	} catch (java.lang.NumberFormatException e) {
	    i = 0;
	}
	return i;
    }

}
