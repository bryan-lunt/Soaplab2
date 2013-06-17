// DefaultFilter.java
//
// Created: October 2010
//
// Copyright 2010 Martin Senger
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

package org.soaplab.clients.spinet.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 * This class filters all static pages by adding them "UTF-8"
 * character encoding and, when not known from the static file
 * extension, the MIME type "text/plain". Without a non-empty MIME
 * type, there would be no sense to add charater encoding - and we
 * need it for non-western characters. <p/>
 *
 * This filter will forward all static content calls to
 * DefaultServlet. <p/>
 *
 * The idea was found <a
 * href="http://www.kuligowski.pl/java/rest-style-urls-and-url-mapping-for-static-content-apache-tomcat,5">here</a>. <p/>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @author Sebastian Kuligowski
 * @version $Id: DefaultFilter.java,v 1.4 2011/04/06 12:02:26 mahmutuludag Exp $
 */

public class DefaultFilter
    implements Filter {

    private static org.apache.commons.logging.Log log =
    	org.apache.commons.logging.LogFactory.getLog (DefaultFilter.class);

    private RequestDispatcher defaultRequestDispatcher;
    private ServletContext servletContext;

    /*************************************************************************
     *
     *************************************************************************/
    public void destroy() {}

    /*************************************************************************
     *
     *************************************************************************/
    public void doFilter (ServletRequest request, ServletResponse response, FilterChain chain)
	throws IOException, ServletException {

	String servletPath = ((HttpServletRequest)request).getServletPath();
	// log.info ("ServletPath: " + servletPath);
	// log.info ("RealPath:    " + servletContext.getRealPath (servletPath));
	// log.info ("MimeType:    " + servletContext.getMimeType (servletContext.getRealPath (servletPath)));
	String mimeType = servletContext.getMimeType (servletContext.getRealPath (servletPath));
	if (mimeType == null) {
	    response.setContentType ("text/plain");
	}
	response.setCharacterEncoding ("UTF-8");
        defaultRequestDispatcher.forward (request, response);
    }
    
    /*************************************************************************
     *
     *************************************************************************/
    public void init (FilterConfig filterConfig)
	throws ServletException {
        this.servletContext = filterConfig.getServletContext();
        this.defaultRequestDispatcher = this.servletContext.getNamedDispatcher ("default");
    }
}
