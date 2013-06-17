// ServiceLocator.java
//
// Created: June 2007
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

package org.soaplab.clients;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.StandardToStringStyle;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Set;
import java.util.HashSet;

/**
 * A container (with a bit of behaviour) that stores where is a
 * Soaplab service and how to access it. Everything what this
 * container does not know, it takes from the Soaplab client
 * configuration properties. <p>
 *
 * Each container defines just one Soaplab Analysis service, and it
 * may also define a Soaplab List service (there are no two separate
 * containers for the List service and the Analysis service). <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: ServiceLocator.java,v 1.10 2009/06/19 12:32:10 mahmutuludag Exp $
 */
public class ServiceLocator {

    protected static final String FIXED_URL_PATH = "/services/";
    protected static final String FIXED_URL_PATH_TYPED_INTERFACE = "/typed/services/";
    protected static final String DEFAULT_LIST_SERVICE_NAME_AXIS1 = "AnalysisFactory";
    protected static final String DEFAULT_LIST_SERVICE_NAME_JAXWS = "list";

    private String serviceName;
    private String listServiceName;
    private String host;
    private String port;
    private String context;
    private String listEndpoint;
    private String serviceEndpoint;
    private String protocol;
    private boolean useTypedInterace = false;

    public boolean isUseTypedInterace() {
		return useTypedInterace;
	}

	public void setUseTypedInterace(boolean useTypedInterace) {
		this.useTypedInterace = useTypedInterace;
	}

	// some styles for toString() methods
    protected static final StandardToStringStyle OUR_MULTI_LINE_STYLE =
	new StandardToStringStyle();
    static {
	OUR_MULTI_LINE_STYLE.setUseShortClassName (true);
	OUR_MULTI_LINE_STYLE.setContentStart ("[");
	OUR_MULTI_LINE_STYLE.setFieldSeparator (SystemUtils.LINE_SEPARATOR + "  ");
	OUR_MULTI_LINE_STYLE.setFieldSeparatorAtStart (true);
	OUR_MULTI_LINE_STYLE.setContentEnd (SystemUtils.LINE_SEPARATOR + "]");
    }

    // recognized protocols
    static protected Set<String> knownProtocols = new HashSet<String>();
    static {
	knownProtocols.add (ClientConfig.PROTOCOL_JAXWS);
	knownProtocols.add (ClientConfig.PROTOCOL_AXIS1);
	knownProtocols.add (ClientConfig.PROTOCOL_LOCAL);
    }

    /*************************************************************************
     * A constructor creates an empty locator. Which is useful either
     * when only a List service is being defined (because a List
     * service does not need any service name), or if various 'set'
     * methods will be used later to fill the locator sufficiently.
     *************************************************************************/
    public ServiceLocator() {
    }

    /*************************************************************************
     * A usual constructor. <p>
     *
     * @param serviceName is an Analysis service defined by this
     * locator. There may be other properties needed (such as a host
     * name and a port number where the service is operating from) but
     * they can be obtained from the Soaplab client configuration file
     * (however, there is no property there specifying a service
     * name).
     *************************************************************************/
    public ServiceLocator (String serviceName) {
	setServiceName (serviceName);
    }

    /**************************************************************************
     *
     *************************************************************************/
    public String getHost() {
	if (StringUtils.isNotEmpty (host))
	    return host;
	return ClientConfig.get().getString (ClientConfig.PROP_HOST,
					     ClientConfig.DEFAULT_HOST);
    }

    /**************************************************************************
     *
     *************************************************************************/
    public final void setHost (final String newHost) {
	this.host = newHost;
    }

    /**************************************************************************
     *
     *************************************************************************/
    public String getPort() {
	if (StringUtils.isNotEmpty (port))
	    return port;
	return ClientConfig.get().getString (ClientConfig.PROP_PORT,
					     ClientConfig.DEFAULT_PORT);
    }

    /**************************************************************************
     *
     * @throws IllegalArgumentException if the 'newPort' is
     * not null but cannot be converted into an integer
     *************************************************************************/
    public final void setPort (final String newPort) {
	if (StringUtils.isNotEmpty (newPort)) {
	    try {
		Integer.decode (newPort);
	    } catch (NumberFormatException e) {
		throw new IllegalArgumentException
		    ("A non-numeric port passed to a ServiceLocator: '" +
		     newPort + "'");
	    }
	}
	this.port = newPort;
    }

    /**************************************************************************
     *
     *************************************************************************/
    public String getContext() {
	if (StringUtils.isNotEmpty (context))
	    return context;
	return ClientConfig.get().getString (ClientConfig.PROP_CONTEXT_NAME,
					     ClientConfig.DEFAULT_CONTEXT_NAME);
    }

    /**************************************************************************
     *
     *************************************************************************/
    public boolean isContextSet() {
    	boolean cfgfileentry = ClientConfig.get().containsKey(ClientConfig.PROP_CONTEXT_NAME);
	return StringUtils.isNotEmpty (context) || cfgfileentry;
    }

    /**************************************************************************
     *
     *************************************************************************/
    public final void setContext (final String newContext) {
	this.context = newContext;
    }

    /**************************************************************************
     *
     *************************************************************************/
    public final String getListServiceName() {
	if (StringUtils.isEmpty (listServiceName)){
	    if (getProtocol().equals(ClientConfig.PROTOCOL_AXIS1))
	        return DEFAULT_LIST_SERVICE_NAME_AXIS1;
	    return DEFAULT_LIST_SERVICE_NAME_JAXWS;
	}
	else
	    return listServiceName;
    }

    /**************************************************************************
     *
     *************************************************************************/
    public final void setListServiceName (final String newListServiceName) {
	this.listServiceName = newListServiceName;
    }

    /**************************************************************************
     *
     *************************************************************************/
    public final String getServiceName() {
	return serviceName;
    }

    /**************************************************************************
     *
     *************************************************************************/
    public final void setServiceName (final String newServiceName) {
	this.serviceName = newServiceName;
    }

    /**************************************************************************
     * If an endpoint is not defined, we build it from other parts
     * on-the-fly.
     *************************************************************************/
    public String getListEndpoint() {
	if (StringUtils.isNotEmpty (listEndpoint))
	    return listEndpoint;
	return buildEndpoint (getListServiceName());
    }

    /**************************************************************************
     * Setting a non-empty list endpoint also sets the host, port,
     * context, and list service name (from the values defined in the
     * 'newListEndpoint'). <p>
     *
     * @throws IllegalArgumentException if the 'newListEndpoint' is
     * not null but cannot be converted into a URL
     *************************************************************************/
    public void setListEndpoint (final String newListEndpoint) {
	this.listEndpoint = newListEndpoint;
	if (StringUtils.isNotEmpty (newListEndpoint)) {
	    // parse given endpoint and set from it the other members
	    String path = cutEndpoint (newListEndpoint);
	    setListServiceName (StringUtils.substringAfterLast (path, "/"));
	}
    }

    /**************************************************************************
     * If an endpoint is not defined, we build it from other parts
     * on-the-fly. If the 'serviceName' is also missing, we do not
     * build it (it would be very probably a wrong endpoint).
     *************************************************************************/
    public String getServiceEndpoint() {
	if (StringUtils.isNotEmpty (serviceEndpoint))
	    return serviceEndpoint;
	if (StringUtils.isBlank (serviceName))
	    return "";
	return buildEndpoint
	    (StringUtils.isEmpty (serviceName) ? "" : serviceName);
    }

    /**************************************************************************
     * Setting a non-empty service endpoint also sets the host, port,
     * context, and service name (from the values defined in the
     * 'newListEndpoint'). <p>
     *
     * @throws IllegalArgumentException if the 'newListEndpoint' is
     * not null but cannot be converted into a URL
     *************************************************************************/
    public void setServiceEndpoint (final String newServiceEndpoint) {
	this.serviceEndpoint = newServiceEndpoint;
	if (StringUtils.isNotEmpty (newServiceEndpoint)) {
	    // parse given endpoint and set from it the other members
	    String path = cutEndpoint (newServiceEndpoint);
	    setServiceName (StringUtils.substringAfterLast (path, "/"));
	}
    }

    /**************************************************************************
     *
     *************************************************************************/
    public String getProtocol() {
	if (StringUtils.isNotEmpty (protocol))
	    return protocol;
	return ClientConfig.get().getString (ClientConfig.PROP_PROTOCOL,
					     ClientConfig.DEFAULT_PROTOCOL);
    }

    /**************************************************************************
     *
     * @throws IllegalArgumentException if the 'newProtocol' is
     * not null but it is not recognized
     *************************************************************************/
    public final void setProtocol (final String newProtocol) {
	if (StringUtils.isNotEmpty (newProtocol)) {
	    if (! knownProtocols.contains (newProtocol)) {
		throw new IllegalArgumentException
		    ("An unknown protocol passed to a ServiceLocator: '" +
		     newProtocol + "'");
	    }
	}
	this.protocol = newProtocol;
    }

    /**************************************************************************
     *
     *************************************************************************/
    protected String buildEndpoint (String lastName) {
	StringBuilder buf = new StringBuilder();
	buf.append ("http://");
	buf.append (getHost());
	buf.append (":");
	buf.append (getPort());
	buf.append ("/");
	buf.append (getContext());
	buf.append (useTypedInterace ? FIXED_URL_PATH_TYPED_INTERFACE : FIXED_URL_PATH);
	buf.append (lastName);
	return buf.toString();
    }

    /**************************************************************************
     * Cut the 'endpoint' into host, port and context. Return the path
     * (the part of the URL without the leading slash) - because it
     * may be useful for extracting a service name (elsewhere).
     *************************************************************************/
    protected String cutEndpoint (String endpoint) {
	try {
	    URL url = new URL (endpoint);
	    setHost (url.getHost());
	    int p = url.getPort();
	    if (p == -1)
		p = url.getDefaultPort();
	    if (p > -1)
		setPort (""+p);
	    String path = url.getPath();
	    if (path.startsWith ("/") && path.length() > 1)
		path = path.substring (1);
	    if (StringUtils.contains (path, FIXED_URL_PATH)) {
		setContext (StringUtils.substringBefore (path, FIXED_URL_PATH));
	    } else {
		setContext (StringUtils.substringBeforeLast (path, "/"));
	    }
	    return path;

	} catch (MalformedURLException e) {
	    throw new IllegalArgumentException
		("Not a valid URL passed to a ServiceLocator: '" +
		 endpoint + "'");
	}
    }

    /**************************************************************************
     *
     *************************************************************************/
    @Override public String toString() {
	return new ToStringBuilder (this, OUR_MULTI_LINE_STYLE).
	    append ("Protocol",          getProtocol()).
	    append ("Host",              getHost()).
	    append ("Port",              getPort()).
	    append ("Context",           getContext()).
	    append ("Service name",      getServiceName()).
	    append ("List Service name", getListServiceName()).
	    append ("Service endpoint",  getServiceEndpoint()).
	    append ("List endpoint",     getListEndpoint()).
	    toString();
    }

}
