// ExploreConfig.java
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

package org.soaplab.admin;

import org.soaplab.services.Config;
import org.soaplab.clients.CmdLineHelper;

import org.tulsoft.tools.BaseCmdLine;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.SystemConfiguration;

import java.util.Properties;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A command-line tool that explores and tests Soaplab
 * configuration. Start it with <tt>-help</tt> to see available
 * options. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: ExploreConfig.java,v 1.6 2007/10/22 00:31:17 marsenger Exp $
 */
public class ExploreConfig
    extends CmdLineHelper {

    static ExploreConfig worker = new ExploreConfig();

    /*************************************************************************
     *
     *  An entry point
     *
     *************************************************************************/
    @SuppressWarnings("unchecked")
    public static void main (String[] args) {

	try {
	    BaseCmdLine cmd = getCmdLine (args, ExploreConfig.class);
	    String param = null;

	    // remember System properties before we initialize
	    // Soaplab's Config
	    Properties sysProps = System.getProperties();

	    // for testing: adding property files
	    if ((param = cmd.getParam ("-f")) != null) {
		title ("Adding config property files:");
		String[] files = param.split (",");
		for (String filename: files) {
		    if (Config.addConfigPropertyFile (filename))
			msgln ("Added:  " + filename);
		    else
			msgln ("Failed: " + filename);
		}
		msgln ("");
	    }
	    if ((param = cmd.getParam ("-xml")) != null) {
		title ("Adding config XML files:");
		String[] files = param.split (",");
		for (String filename: files) {
		    if (Config.addConfigXMLFile (filename))
			msgln ("Added:  " + filename);
		    else
			msgln ("Failed: " + filename);
		}
		msgln ("");
	    }

	    // list of configurations
	    if (cmd.hasOption ("-lf")) {
		title ("Using property resources (in this order):");
		int i = 0;
		while (true) {
		    Configuration cfg = null;
		    int count = 0;
		    try {
			cfg = Config.get().getConfiguration (i++);
			for (Iterator<String> it = cfg.getKeys(); it.hasNext(); count++)
			    it.next();
		    } catch (IndexOutOfBoundsException e) {
			break;
		    }
		    if (count == 0) msg (i + ": (empty) ");
		    else  	    msg (i + ": (" + String.format ("%1$5d", count) + ") ");
		    if (cfg instanceof FileConfiguration) {
			msgln ( ((FileConfiguration)cfg).getFile().getAbsolutePath() );
		    } else if (cfg instanceof SystemConfiguration) {
			msgln ("Java System Properties");
		    } else if (cfg instanceof BaseConfiguration) {
			msgln ("Directly added properties (no config file)");
		    } else {
			msgln (cfg.getClass().getName());
		    }
		}
		msgln ("");
	    }

	    // list of properties
	    boolean listProps = cmd.hasOption ("-l");
	    boolean listAllProps = cmd.hasOption ("-la");
	    if (listProps || listAllProps) {
		title ("Available properties" +
		       (listProps ? " (except System properties):" : ":"));
		SortedMap<String,String> allProps = new TreeMap<String,String>();
		int maxLen = 0;
		for (Iterator<String> it = Config.get().getKeys(); it.hasNext(); ) {
		    String key = it.next();
		    if (listAllProps || !sysProps.containsKey (key)) {
			if (key.length() > maxLen) maxLen = key.length();
			String[] values = Config.get().getStringArray (key);
			if (values.length != 0)
			    allProps.put (key, StringUtils.join (values, ","));
		    }
		}
		for (Iterator<Map.Entry<String,String>> it = allProps.entrySet().iterator();
		     it.hasNext(); ) {
		    Map.Entry<String,String> entry = it.next();
		    msgln (String.format ("%1$-" + maxLen + "s", entry.getKey()) +
			   " => " + entry.getValue());
		}
		msgln ("");
	    }

	    // get properties by prefix
	    if ((param = cmd.getParam ("-prefix")) != null) {
		String serviceName = cmd.getParam ("-service");
		if (serviceName == null)
		    title ("Properties matching prefix:");
		else
		    title ("Properties matching service name and prefix:");
		SortedSet<String> selectedKeys = new TreeSet<String>();
		int maxLen = 0;
		Properties props =
		    Config.getMatchingProperties (param, serviceName, null);
		for (Enumeration en = props.propertyNames() ; en.hasMoreElements() ;) {
		    String key = (String)en.nextElement();
		    if (key.length() > maxLen) maxLen = key.length();
		    selectedKeys.add (key);
		}
		for (Iterator<String> it = selectedKeys.iterator(); it.hasNext(); ) {
		    String key = it.next();
		    msgln (String.format ("%1$-" + maxLen + "s", key) +
			   " => " + props.getProperty (key));
		}
		msgln ("");
	    }

	    // show individual properties value
	    if (cmd.params.length > 0) {
		int maxLen = 0;
		for (int i = 0; i < cmd.params.length; i++) {
		    int len = cmd.params[i].length();
		    if (len > maxLen) maxLen = len;
		}
		title ("Selected properties:");
		for (int i = 0; i < cmd.params.length; i++) {
		    String value = Config.get().getString (cmd.params[i]);
		    msgln (String.format ("%1$-" + maxLen + "s", cmd.params[i]) +
			   " => " + (value == null ? "<null>" : value));
		}
	    }

	} catch (Throwable e) {
	    processErrorAndExit (e);
	}
    }

}
