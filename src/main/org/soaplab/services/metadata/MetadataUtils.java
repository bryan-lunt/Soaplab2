// MetadataUtils.java
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

import org.soaplab.share.SoaplabException;
import org.soaplab.share.SoaplabConstants;
import org.soaplab.services.Config;

import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * Some static pieces of code for dealing with analysis metadata. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: MetadataUtils.java,v 1.7 2007/10/24 14:35:36 marsenger Exp $
 */
public abstract class MetadataUtils {

    // an (almost) undocumented property
    private static final String PROP_SHOW_ALL_PROPS = "show.all.properties";

    /**************************************************************************
     * Convert a set of InputPropertyDef's into a Map (as defined and
     * used, for example, by <tt>Analysis</tt> in Soaplab. The input
     * spec is available from the given 'metadataAccessor'.
     **************************************************************************/
    @SuppressWarnings("unchecked")
    public static Map<String,Object>[] inputSpec2Map (MetadataAccessor metadataAccessor)
	throws SoaplabException {

	InputPropertyDef[] ipd = metadataAccessor.getInputDefs();
	Map<String,ParamDef> paramByNames = new HashMap<String,ParamDef>();  // just for better access
	ParamDef[] paramDefs = metadataAccessor.getParamDefs();
	for (int i = 0; i < paramDefs.length; i++)
	    paramByNames.put (paramDefs[i].id, paramDefs[i]);
	
	Map<String,Object>[] inputSpec = new HashMap [ipd.length];
	for (int i = 0; i < ipd.length; i++) {
	    inputSpec[i] = new HashMap<String,Object>();
	    String name = ipd[i].name;
	    inputSpec[i].put (SoaplabConstants.INPUT_NAME, name);
	    inputSpec[i].put (SoaplabConstants.INPUT_TYPE, ipd[i].type);
	    inputSpec[i].put (SoaplabConstants.INPUT_MANDATORY, ipd[i].mandatory + "");
	    String dflt = ipd[i].defaultValue;
	    if (StringUtils.isNotBlank (dflt))
		inputSpec[i].put (SoaplabConstants.INPUT_DEFAULT_VALUE, dflt);
	    if (ipd[i].possibleValues != null && ipd[i].possibleValues.length > 0)
		inputSpec[i].put (SoaplabConstants.INPUT_ALLOWED_VALUES, ipd[i].possibleValues);
	    
	    // use additional metadata:
	    // ...some names can share the same 'paramDef'
	    String realName;
	    if (name.endsWith ("_usa") || name.endsWith ("_url"))
		realName = name.substring (0, name.length()-4);
	    else if (name.endsWith ("_direct_data"))
		realName = name.substring (0, name.length()-12);
	    else
		realName = name;
	    
	    ParamDef param = (ParamDef)paramByNames.get (realName);
	    if (param != null) {
		for (Enumeration<String> en = param.options.keys(); en.hasMoreElements(); ) {
		    String key = (String)en.nextElement();
		    inputSpec[i].put (key, param.get (key));
		}
	    }
	}
	return inputSpec;
    }

    /**************************************************************************
     * Convert a set of OutputPropertyDef's into a Map (as defined and
     * used, for example, by <tt>Analysis</tt> in Soaplab). The output
     * spec is available from the given 'metadataAccessor'. <p>
     *
     * Filter out some properties that are not useful for the
     * end-users. But keep there a backdoor: when a property
     * 'show.all.properties' is on, do not filter them.
     **************************************************************************/
    @SuppressWarnings("unchecked")
    public static Map<String,Object>[] outputSpec2Map (MetadataAccessor metadataAccessor)
	throws SoaplabException {

	boolean noFilter = 
	    Config.isEnabled (PROP_SHOW_ALL_PROPS, false, null, null);

	OutputPropertyDef[] opd = metadataAccessor.getOutputDefs();
	Map<String,ParamDef> paramByNames = new HashMap<String,ParamDef>();  // just for better access
	ParamDef[] paramDefs = metadataAccessor.getParamDefs();
	for (int i = 0; i < paramDefs.length; i++)
	    paramByNames.put (paramDefs[i].id, paramDefs[i]);
	
	Map<String,Object>[] resultSpec = new HashMap [opd.length];
	for (int i = 0; i < opd.length; i++) {
	    resultSpec[i] = new HashMap<String,Object>();
	    String name = opd[i].name;
	    resultSpec[i].put (SoaplabConstants.RESULT_NAME, name);
	    resultSpec[i].put (SoaplabConstants.RESULT_TYPE, opd[i].type);
	    
	    // use additional metadata:
	    // ...some names can share the same 'paramDef'
	    String realName;
	    if (name.endsWith ("_url"))
		realName = name.substring (0, name.length()-4);
	    else
		realName = name;
	    
	    ParamDef param = (ParamDef)paramByNames.get (realName);
	    if (param != null) {
		for (Enumeration<String> en = param.options.keys(); en.hasMoreElements(); ) {
		    String key = (String)en.nextElement();
		    if ( noFilter ||
			 (! key.equals (ParamDef.NODISPLAY) &&
			  ! key.equals (IOParamDef.SPECIAL_TYPE)) ) {
			resultSpec[i].put (key, param.get (key));
		    }
		}
	    }
	}
	return resultSpec;
    }

    /**************************************************************************
     * Convert a set of Analysis attributes into a Map (as defined and
     * used, for example, by <tt>Analysis</tt> in Soaplab. The
     * attributes are available from the given 'metadataAccessor'. <p>
     *
     * Filter out some properties that are not useful for the
     * end-users. But keep there a backdoor: when a property
     * 'show.all.properties' is on, do not filter them.
     **************************************************************************/
    public static Map<String,Object> analysisSpec2Map (MetadataAccessor metadataAccessor)
	throws SoaplabException {

	boolean noFilter = 
	    Config.isEnabled (PROP_SHOW_ALL_PROPS, false, null, null);

	AnalysisDef def = metadataAccessor.getAnalysisDef();
	Map<String,Object> analysisSpec = new Hashtable<String,Object>();
	analysisSpec.put (SoaplabConstants.ANALYSIS_TYPE, def.type);
	analysisSpec.put (SoaplabConstants.ANALYSIS_NAME, def.appName);
	if (noFilter)
	    analysisSpec.put (SoaplabConstants.ANALYSIS_MODULE, def.module);

	for (Enumeration<String> en = def.options.keys(); en.hasMoreElements(); ) {
	    String name = en.nextElement();
	    if ( noFilter ||
		 ! name.equals ("class")) {
		analysisSpec.put (name, def.get (name));
	    }
	}
	return analysisSpec;
    }

    /**************************************************************************
     * Find the 'launcher' directive in the given metadata, or in the
     * configuration (using given 'serviceName' and the caller), and
     * parse it into a list of two elements.
     **************************************************************************/
    public static String[] parseLauncher (MetadataAccessor metadataAccessor,
					  String serviceName,
					  Object owner)
	throws SoaplabException {

	String launcher =
	    Config.getString (Config.PROP_LAUNCHER,
			      metadataAccessor.getAnalysisDef().launcher,
			      serviceName,
			      owner);
	if (StringUtils.isEmpty (launcher))
	    return new String[] { null, null };

	String[] parts = StringUtils.split (launcher, " ", 2);
	if (parts.length == 1) {
	    if (parts[0].toUpperCase().matches ("GET|POST|HEAD"))
		return new String[] { parts[0], null };
	    else
		return new String[] { null, parts[0] };
	}
	return parts;
    }
}
