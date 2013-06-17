// AnalysisInstallation.java
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

import org.apache.commons.lang.StringUtils;
import java.util.Properties;


/**
 * A container holding data about an analysis. Rather than holding
 * data about the analysis itself (for that see {@link AnalysisDef}
 * and the other classes) it contains data about the environment this
 * analysis is used within (such as file location of the internal
 * analysis metadata). <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: AnalysisInstallation.java,v 1.3 2007/11/14 15:07:40 marsenger Exp $
 */
public class AnalysisInstallation {

    // names of some properties available from this object
    // (the same names are used as tag and attributes names in XML files
    //  containing a list of available analysis - therefore these names
    //  should correspond to SoaplabList.dtd)
    public static final String CATEGORY = "category"; // category name
    public static final String APP_NAME = "name";     // analysis name
    public static final String DESC     = "desc";     // one-line description
    public static final String XML_DESC = "xml";      // file with XML description of this analysis
    public static final String MODULE   = "module";   // job type, or class name

    // misc
    public static final String DEFAULT_CATEGORY = "Other";
    public static final String DEFAULT_MODULE = "sowa";

    // members/attributes
    private String category = DEFAULT_CATEGORY;
    private String appName = "";
    private String xmlFile;
    private String desc = "";
    private String module = DEFAULT_MODULE;

    private String delim = SoaplabConstants.SOAPLAB_SERVICE_NAME_DELIMITER;

    /**************************************************************************
     * Constructors
     **************************************************************************/
    public AnalysisInstallation() {}

    public AnalysisInstallation (Properties props) {
        setProperties (props);
    }

    public AnalysisInstallation (String category, String appName) {
        setId (category, appName);
    }

    /**************************************************************************
     * Set a delimiter that is used to separate category and the real
     * name of stored analysis.
     **************************************************************************/
    public void setDelimiter (String categoryNameDelimiter) {
	delim = categoryNameDelimiter;
    }

    /**************************************************************************
     * Return a name consisting from both category and application
     * name.
     **************************************************************************/
    public String getFullName() {
        return category + delim + appName;
    }
  
    /**************************************************************************
     * A static utility method: Split category and application name. If
     * there is no divider in the given name, the first element of the
     * returned two-elemens array will be an empty string.
     **************************************************************************/
    public static String[] splitName (String analysisName,
				      String categoryNameDelimiter) {
	int pos = analysisName.indexOf (categoryNameDelimiter);
	if (pos > -1)
	    return new String[] { analysisName.substring (0, pos),
				  analysisName.substring (pos + categoryNameDelimiter.length()) };
	else
	    return new String[] { "", analysisName };
    }

    /**************************************************************************
     * Set properties. The known properties are taken from 'props' and
     * stored.
     **************************************************************************/
    public void setProperties (Properties props) {
        String value = null;
        if (StringUtils.isNotBlank (value = props.getProperty (CATEGORY))) category = value;
        if (StringUtils.isNotBlank (value = props.getProperty (APP_NAME))) appName = value;
        if (StringUtils.isNotBlank (value = props.getProperty (DESC)))     desc = value;
        if (StringUtils.isNotBlank (value = props.getProperty (XML_DESC))) xmlFile = value;
        if (StringUtils.isNotBlank (value = props.getProperty (MODULE)))   module = value;
    }

    private void setId (String category, String appName) {
        this.category = category;
        this.appName = appName;
    }

    /**************************************************************************
     * Access methods
     **************************************************************************/
    public String getCategory()  { return category; }
    public String getAppName()   { return appName; }
    public String getDesc()      { return desc; }
    public String getXMLDesc()   { return xmlFile; }
    public String getModule()    { return module; }

    /**************************************************************************
     * equals()
     **************************************************************************/
    public boolean equals (Object anotherAnalysis) {
        if (! (anotherAnalysis instanceof AnalysisInstallation))
	    return super.equals (anotherAnalysis);
        return (category.equals (((AnalysisInstallation)anotherAnalysis).getCategory()) &&
                appName.equals (((AnalysisInstallation)anotherAnalysis).getAppName()));
    }

    /**************************************************************************
     * toString()
     **************************************************************************/
    public String toString() {
        return "[" + getFullName() + "] " + xmlFile;
    }

}
