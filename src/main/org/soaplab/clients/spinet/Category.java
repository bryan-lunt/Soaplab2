// Category.java
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import java.util.List;
import java.util.ArrayList;

/**
 * A read-only bean (used usually from a JSP page) representing a list
 * of all service provided by a Soaplab2 server in a single
 * category. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: Category.java,v 1.3 2007/11/15 00:36:28 marsenger Exp $
 */

public class Category {

    private String name;
    private List<Service> services;

    /*************************************************************************
     * A default constructor. <p>
     *************************************************************************/
    public Category (String name) {
	this.name = name;
	this.services = new ArrayList<Service>();
    }

    /*************************************************************************
     * Add a service to this category. <p>
     *
     * @param service to be added
     *************************************************************************/
    public void addService (Service service) {
	services.add (service);
    }

    /*************************************************************************
     * Return the name of this category.
     *************************************************************************/
    public String getName() {
	return name;
    }

    /*************************************************************************
     * Return the name of this category in a more "human-readable" form.
     *************************************************************************/
    public String getDisplayName() {
	return WordUtils.capitalizeFully
	    (StringUtils.replace (getName(), "_", "&nbsp;"));
    }

    /*************************************************************************
     * Return a list of available services from this category.
     *************************************************************************/
    public Service[] getServices() {
	return services.toArray (new Service[] {});
    }

}
