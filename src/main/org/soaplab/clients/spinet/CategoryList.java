// CategoryList.java
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

import org.soaplab.services.Config;
import org.soaplab.share.SoaplabException;
import org.soaplab.share.SoaplabMap;
import org.soaplab.clients.ClientConfig;
import org.soaplab.clients.ServiceLocator;
import org.soaplab.clients.SoaplabBaseClient;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * A read-only bean (used usually from a JSP page) representing a list
 * of all service categories provided by a Soaplab2 server. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: CategoryList.java,v 1.2 2009/06/19 12:32:10 mahmutuludag Exp $
 */

public class CategoryList {

    private static org.apache.commons.logging.Log log =
       org.apache.commons.logging.LogFactory.getLog (CategoryList.class);

    /*************************************************************************
     * Return a list of available service categories. <p>
     *
     * @return list of categories which may be empty but never null
     *
     * @throws SoaplabException if a list is not accessible
     *************************************************************************/
    public Category[] getCategories()
	throws SoaplabException {

	ServiceLocator locator = new ServiceLocator();
	locator.setProtocol (ClientConfig.PROTOCOL_LOCAL);

	SoaplabBaseClient client = new SoaplabBaseClient (locator);

	// fetch service descriptions
	Map<String,String> services =
	    SoaplabMap.toStringMap (client.getAvailableAnalysesWithDescription());
	log.debug ("Found " + services.size() + " services");

	   String services_disabled = Config.getString ("spinet.services.disabled", "",
               null, this);
	   
	// fetch categories
	List<Category> categories = new ArrayList<Category>();
	for (String categoryName: client.getAvailableCategories()) {
	    Category category = new Category (categoryName);
	    for (String serviceName: client.getAvailableAnalysesInCategory (categoryName)) {
	        if(services_disabled.contains(serviceName.substring(categoryName.length()+1)))
	            continue;
		Service service = new Service (serviceName);
		service.setDescription (services.get (serviceName));
		category.addService (service);
	    }
	    if (category.getServices().length>0)
	    categories.add (category);
	}
	log.debug ("Found " + categories.size() + " categories");

	return categories.toArray (new Category[] {});
    }

}
