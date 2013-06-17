// DefaultAnalysisInventory.java
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

import org.soaplab.services.metadata.AnalysisInstallation;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;

/**
 * A default implementation of the {@link AnalysisInventory}
 * interface, reading list of available analyses from an XML
 * configuration file. <p>
 *
 * The XML filename is given in the property {@link
 * Config#PROP_APPLIST_FILENAME}. The property can actually contain
 * more than one filename (or, the same property can be used - in the
 * Soaplab2 configuration file - several times, with several different
 * filenames). <p>
 *
 * It makes sure that the analysis list (or lists) is (are) reloaded
 * and read again if any of them was modified. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: DefaultAnalysisInventory.java,v 1.4 2007/10/15 14:07:23 marsenger Exp $
 */

public class DefaultAnalysisInventory
    implements AnalysisInventory {

    private static org.apache.commons.logging.Log log =
       org.apache.commons.logging.LogFactory.getLog (DefaultAnalysisInventory.class);

    protected XMLConfiguration[] configs;
    protected String lastUsedFileNames;
    protected boolean warningIssued;
    protected Hashtable<String,Vector<AnalysisInstallation>> appList;

    /**************************************************************************
     * The main constructor.
     *************************************************************************/
    public DefaultAnalysisInventory() {
	appList = new Hashtable<String,Vector<AnalysisInstallation>>();
	reloadConfig();
    }

    /**************************************************************************
     * Check if the property containing filename with an analysis list
     * (or filenames, actually) has changed since the last time we saw
     * it. If yes, reload analysis list(s) defined in this property.
     *************************************************************************/
    protected synchronized void reloadConfig() {

	// find the filename(s) with our analysis list(s)
	String[] fileNames = Config.get().getStringArray (Config.PROP_APPLIST_FILENAME);
	if (fileNames == null || fileNames.length == 0) {
	    if (! warningIssued) {
		log.warn ("Property '" + Config.PROP_APPLIST_FILENAME + "' not found.");
		warningIssued = true;
	    }
	    configs = null;
	    lastUsedFileNames = null;
	    appList = new Hashtable<String,Vector<AnalysisInstallation>>();
	    return;
	}

	// has PROP_APPLIST_FILENAME changed since last visit?
	String joined = StringUtils.join (fileNames, ",");
	if (! joined.equals (lastUsedFileNames)) {
	    warningIssued = false;
	    List<XMLConfiguration> configsList = new ArrayList<XMLConfiguration>();
	    for (int i = 0; i < fileNames.length; i++) {
		try {
		    XMLConfiguration config = new XMLConfiguration (fileNames[i]);
		    config.setReloadingStrategy (new FileChangedReloadingStrategy() {
			    public void reloadingPerformed() {
				super.reloadingPerformed();  // because of updateLastModified();
				readConfigs();
			    }
			});
		    configsList.add (config);
		    log.info ("Using analysis list file '" + fileNames[i] + "'");
		} catch (ConfigurationException e) {
		    log.error ("Loading analysis list from '" + fileNames[i] + "' failed: " +
			       e.getMessage());
		}
	    }
	    if (configsList.size() > 0) {
		configs = configsList.toArray (new XMLConfiguration[] {});
		readConfigs();
		lastUsedFileNames = joined;
	    } else {
		configs = null;
		lastUsedFileNames = null;
		appList = new Hashtable<String,Vector<AnalysisInstallation>>();
	    }
	}

	// try to trigger reloading of individual XML file names (if
	// they changed)
	if (configs != null) {
	    for (int i = 0; i < configs.length; i++)
		configs[i].isEmpty();
	}

    }

    /**************************************************************************
     * Load into the global variable 'appList' contents of XML config
     * files represented by another global variable 'configs'. <p>
     *
     * The format of any of these XML files with analysis list is
     * following:
     *
     * <whatever>
     *   <category name="classic">
     *     <app name="helloworld"
     *          xml="/path/.../helloworld_al.xml"
     *          desc="Classic greeting from the beginning of the UNIX epoch"
     *          module="sowa"
     *          />
     *     <app ..../>
     *   </category>
     *   <category ..../>
     * </whatever>
     *
     *************************************************************************/
    protected synchronized void readConfigs() {
	appList.clear();
	String category;
	for (Configuration config: configs) {
	    int categoryCount = 0;
	    while ( (category =
		     config.getString ("category(" + categoryCount + ")[@name]")) != null) {
		int appCount = 0;
		String appName;
		while ( (appName =
			 config.getString ("category(" + categoryCount +
					   ").app(" + appCount + ")[@name]")) != null) {
		    Properties props = new Properties();
		    props.put (AnalysisInstallation.CATEGORY, category);
		    props.put (AnalysisInstallation.APP_NAME, appName);
		    String value;
		    if ( (value =
			  config.getString ("category(" + categoryCount +
					    ").app(" + appCount + ")[@xml]")) != null)
			props.put (AnalysisInstallation.XML_DESC, value);
		    if ( (value =
			  config.getString ("category(" + categoryCount +
					    ").app(" + appCount + ")[@desc]")) != null)
			props.put (AnalysisInstallation.DESC, value);
		    if ( (value =
			  config.getString ("category(" + categoryCount +
					    ").app(" + appCount + ")[@module]")) != null)
			props.put (AnalysisInstallation.MODULE, value);
		    Vector<AnalysisInstallation> apps = appList.get (category);
		    if (apps == null) {
			apps = new Vector<AnalysisInstallation>();
			appList.put (category, apps);
		    }
		    apps.addElement (new AnalysisInstallation (props));
		    appCount++;
		}
		categoryCount++;
	    }
	}
	if (configs.length > 1)
	    log.info ("Analysis lists reloaded.");
	else
	    log.info ("Analysis list reloaded.");
    }

    // ------------------------------------------------------------------------
    //
    //  Methods implementing AnalysisInventory interface
    //
    // ------------------------------------------------------------------------

    /**************************************************************************
     *
     *************************************************************************/
    public Hashtable<String,Vector<AnalysisInstallation>> getList() {
 	reloadConfig();
	return appList;
    }

    /**************************************************************************
     * It returns an empty authority identifier.
     *************************************************************************/
    public String getAuthority() {
	return "";
    }

    // ------------------------------------------------------------------------
    //
    //  For testing...
    //
    // ------------------------------------------------------------------------

    public static void main (String[] args) {

	DefaultAnalysisInventory provider = new DefaultAnalysisInventory();
	System.out.println ("Analyses:");
	System.out.println (provider.getList());

	while (true) {
	    try { Thread.sleep (5000); } catch (Exception e) {}
	    System.out.println (provider.getList());
	}

    }

}
