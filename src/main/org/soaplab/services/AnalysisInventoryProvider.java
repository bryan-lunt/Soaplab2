// AnalysisInventoryProvider.java
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
import org.soaplab.share.SoaplabConstants;
import org.soaplab.share.SoaplabMap;
import org.soaplab.services.DefaultAnalysisInventory;

import java.util.Vector;
import java.util.Set;
import java.util.TreeSet;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Map;
import java.util.HashMap;

/**
 * The analysis inventory provider is a singleton class that provides a
 * list of all available analyses list providers. It uses the SPI
 * mechanism to find all classes implementing the {@link AnalysisInventory}
 * interface. <p>
 *
 * This singleton class is used both by an {@link AnalysisListService}
 * service and by individual {@link AnalysisService} services (the
 * latter needs the list in order to find its service metadata, and to
 * check whether the service is still provided). <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: AnalysisInventoryProvider.java,v 1.7 2008/02/01 15:31:47 marsenger Exp $
 */

public class AnalysisInventoryProvider {

    private static org.apache.commons.logging.Log log =
       org.apache.commons.logging.LogFactory.getLog (AnalysisInventoryProvider.class);

    private static AnalysisInventoryProvider instance;
    private AnalysisInventory[] listProviders;

    /**************************************************************************
     * The only way how to create an AnalysisInventoryProvider instance
     * (because there are no public constructors in this class). <p>
     *
     * @return a singleton instance of an AnalysisInventoryProvider that is
     * aware of all available analyses
     **************************************************************************/
    public static synchronized AnalysisInventoryProvider instance() {
        if (instance == null) {
            instance = new AnalysisInventoryProvider();
        }
        return instance;
    }

    /**************************************************************************
     * A protected constructor, used by sub-classes and by the instance()
     * method.
     **************************************************************************/
    protected AnalysisInventoryProvider() {

	// loads all classes providing lists of analysis
	Enumeration<AnalysisInventory> spe = GenUtils.spiProviders (AnalysisInventory.class);
	Vector<AnalysisInventory> v = new Vector<AnalysisInventory>();
	while (spe.hasMoreElements()) {
	    v.addElement (spe.nextElement());
	}

	// if no classes loaded, make an instance of a default class
	if (v.size() == 0) {
	    v.addElement (new DefaultAnalysisInventory());
	}

	int size = v.size();
	listProviders = new AnalysisInventory [size];
	v.copyInto (listProviders);
	if (size == 0) {
	    log.error ("No analysis list providers was found. A configuration error?");
	} else {
	    log.info (size + " analysis list providers found");
	}
    }

    /**************************************************************************
     *
     *************************************************************************/
    private String getAuthorityAsPrefix (AnalysisInventory provider) {
	String authority = provider.getAuthority().trim();
	if (! "".equals (authority))
	    authority += SoaplabConstants.SOAPLAB_SERVICE_NAME_DELIMITER;
	return authority;
    }

    /**************************************************************************
     * Return a list of all providers of analyses lists.
     **************************************************************************/
    public AnalysisInventory[] getListProviders() {
	return listProviders;
    }

    /**************************************************************************
     *
     *************************************************************************/
    public String[] getAvailableCategories() {
	Set<String> result = new TreeSet<String>();
	for (int i = 0; i < listProviders.length; i++) {
	    String authority = getAuthorityAsPrefix (listProviders[i]);
	    Hashtable<String,Vector<AnalysisInstallation>> list = listProviders[i].getList();
	    for (Enumeration<String> en = list.keys(); en.hasMoreElements(); ) {
		result.add (authority + en.nextElement());
	    }
	}
	return result.toArray (new String[] {});
    }

    /**************************************************************************
     *
     *************************************************************************/
    public String[] getAvailableAnalyses() {

	Set<String> result = new TreeSet<String>();
	for (int i = 0; i < listProviders.length; i++) {
	    String authority = getAuthorityAsPrefix (listProviders[i]);
	    Hashtable<String,Vector<AnalysisInstallation>> list = listProviders[i].getList();
	    for (Enumeration<String> en = list.keys(); en.hasMoreElements(); ) {
		String key = en.nextElement();
		String prefix = authority + key.trim();
		if (! "".equals (prefix))
		    prefix += SoaplabConstants.SOAPLAB_SERVICE_NAME_DELIMITER;
		Vector<AnalysisInstallation> apps = list.get (key);
		for (Enumeration<AnalysisInstallation> en2 = apps.elements(); en2.hasMoreElements(); ) {
		    AnalysisInstallation app = en2.nextElement();
		    result.add (prefix + app.getAppName());
		}
	    }
	}
	return result.toArray (new String[] {});
    }

    /******************************************************************************
     *
     ******************************************************************************/
    public SoaplabMap getAvailableAnalysesWithDescription() {

	Map<String,String> result = new HashMap<String,String>();
	for (int i = 0; i < listProviders.length; i++) {
	    String authority = getAuthorityAsPrefix (listProviders[i]);
	    Hashtable<String,Vector<AnalysisInstallation>> list = listProviders[i].getList();
	    for (Enumeration<String> en = list.keys(); en.hasMoreElements(); ) {
		String key = en.nextElement();
		String prefix = authority + key.trim();
		if (! "".equals (prefix))
		    prefix += SoaplabConstants.SOAPLAB_SERVICE_NAME_DELIMITER;
		Vector<AnalysisInstallation> apps = list.get (key);
		for (Enumeration<AnalysisInstallation> en2 = apps.elements(); en2.hasMoreElements(); ) {
		    AnalysisInstallation app = en2.nextElement();
		    result.put (prefix + app.getAppName(), app.getDesc());
		}
	    }
	}
	return SoaplabMap.fromStringMap (result);
    }

    /**************************************************************************
     *
     *************************************************************************/
    public String[] getAvailableAnalysesInCategory (String categoryName) {

	Set<String> result = new TreeSet<String>();
	for (int i = 0; i < listProviders.length; i++) {
	    Hashtable<String,Vector<AnalysisInstallation>> list = listProviders[i].getList();
	    Vector<AnalysisInstallation> apps = list.get (categoryName);
	    if (apps == null)
		continue;
	    String authority = getAuthorityAsPrefix (listProviders[i]);
	    String prefix = authority + categoryName.trim();
	    if (! "".equals (prefix))
		prefix += SoaplabConstants.SOAPLAB_SERVICE_NAME_DELIMITER;
	    for (Enumeration<AnalysisInstallation> en2 = apps.elements(); en2.hasMoreElements(); ) {
		AnalysisInstallation app = en2.nextElement();
		result.add (prefix + app.getAppName());
	    }
	}
	return result.toArray (new String[] {});
    }

    /**************************************************************************
     * Return a container with information about an analysis
     * represented by the given service. <p>
     *
     * @return an information about a service, or null
     *************************************************************************/
    public AnalysisInstallation getServiceInstallation (String serviceName) {
	if (serviceName == null)
	    return null;

	for (int i = 0; i < listProviders.length; i++) {
	    String authority = getAuthorityAsPrefix (listProviders[i]);
	    Hashtable<String,Vector<AnalysisInstallation>> list = listProviders[i].getList();
	    for (Enumeration<String> en = list.keys(); en.hasMoreElements(); ) {
		String key = en.nextElement();
		String prefix = authority + key.trim();
		if (! "".equals (prefix))
		    prefix += SoaplabConstants.SOAPLAB_SERVICE_NAME_DELIMITER;
		Vector<AnalysisInstallation> apps = list.get (key);
		for (Enumeration<AnalysisInstallation> en2 = apps.elements(); en2.hasMoreElements(); ) {
		    AnalysisInstallation app = en2.nextElement();
		    if (serviceName.equals (prefix + app.getAppName()))
			return app;
		}
	    }
	}
	return null;
    }

    // ------------------------------------------------------------------------
    //
    //  For testing...
    //
    // ------------------------------------------------------------------------

    public static void main (String[] args) {

	AnalysisInventoryProvider service = AnalysisInventoryProvider.instance();

	System.out.println ("Available categories:");
	String[] categories = service.getAvailableCategories();
	for (int i = 0; i < categories.length; i++)
	    System.out.println ("\t" + categories[i]);

	System.out.println ("Available analyses:");
	String[] analyses = service.getAvailableAnalyses();
	for (int i = 0; i < analyses.length; i++)
	    System.out.println ("\t" + analyses[i]);

	System.out.println ("And once again:");
	for (int i = 0; i < categories.length; i++) {
	    System.out.println ("\t" + categories[i]);
	    analyses = service.getAvailableAnalysesInCategory (categories[i]);
	    for (int j = 0; j < analyses.length; j++)
		System.out.println ("\t\t" + analyses[j]);
	}
    }

}
