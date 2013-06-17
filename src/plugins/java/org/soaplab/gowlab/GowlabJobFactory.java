// GowlabJobFactory.java
//
// Created: April 2007
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

package org.soaplab.gowlab;

import org.soaplab.share.SoaplabException;
import org.soaplab.tools.ICreator;
import org.soaplab.services.Reporter;
import org.soaplab.services.metadata.MetadataAccessor;
import org.soaplab.services.metadata.MetadataUtils;
import org.soaplab.services.JobFactory;
import org.soaplab.services.Job;

import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * A factory for GowlabJob instances. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: GowlabJobFactory.java,v 1.1 2007/04/18 18:16:09 marsenger Exp $
 */

public class GowlabJobFactory
    implements JobFactory {

    /**************************************************************************
     * The default constructor.
     **************************************************************************/
    public GowlabJobFactory() {
    }

    /**************************************************************************
     * FACTORY: Get a new Job. <p>
     *
     * It uses service metadata to find what job clas to
     * instantiate. The 'launcher' attribute is used for it. Its full
     * syntax is:
     *
     *<pre>
     * launcher [GET | POST | HEAD | ...] [ &lt;class-name&gt; | &lt;scrip-name&gt; ]
     *</pre>
     *
     * For finding what job to instantiate, the first part of the
     * launcher definition (GET, ...) is not used. The second part is
     * interpreted this way:
     *
     * <ul>
     *
     * <li> If it is empty (or if the whole launcher is empty), a
     * default class {@link GowlabJob} is used.
     *
     * <li> If it ends with <tt>.xsl</tt>, a class {@link XSLTJob} is
     * used.
     *
     * <li> If it ends with <tt>.sh</tt> or <tt>.pl</tt> or
     * <tt>.external</tt>, a class {@link ExternalScriptJob} is used.
     *
     * <li> Otherwise it is considered a class name and the factory
     * tries to instantiate it.
     *
     * </ul>
     **************************************************************************/
    public Job newInstance (String jobId,
			    MetadataAccessor metadataAccessor,
			    Reporter reporter,
			    Map<String,Object> sharedAttributes,
			    boolean jobRecreated)
	throws SoaplabException {

	String launcher2 =
	    MetadataUtils. parseLauncher (metadataAccessor,
					  metadataAccessor.getServiceName(),
					  this) [1];

	if (StringUtils.isEmpty (launcher2))
	    return new GowlabJob (jobId, metadataAccessor, reporter,
				  sharedAttributes, jobRecreated);

// 	if (launcher2.endsWith (".xsl"))
// 	    return new XSLTJob (jobId, metadataAccessor, reporter,
// 				sharedAttributes, jobRecreated);

// 	if (launcher2.endsWith (".pl") ||
// 	    launcher2.endsWith (".sh") ||
// 	    launcher2.endsWith (".external"))
// 	    return new ExternalScriptJob (jobId, metadataAccessor, reporter,
// 					  sharedAttributes, jobRecreated);

	Class[] clss = new Class[] { String.class, MetadataAccessor.class,
				     Reporter.class, Map.class, boolean.class };
	Object[] objs = new Object[] { jobId, metadataAccessor, reporter,
				       sharedAttributes, jobRecreated };
	return (Job) ICreator.createInstance (launcher2, clss, objs);
    }

}
