// SoaplabException.java
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

package org.soaplab.share;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.ws.WebFault;

/** 
 * A general exception which can be used as a wrapper around other
 * exceptions. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: SoaplabException.java,v 1.8 2009/06/26 13:49:29 mahmutuludag Exp $
 */

@WebFault
    (name = "SoaplabException",
     targetNamespace = SoaplabConstants.SOAPLAB_NAMESPACE,
     faultBean = "SoaplabExceptionBean")
public class SoaplabException extends Exception {

    private static org.apache.commons.logging.Log log =
	org.apache.commons.logging.LogFactory.getLog (SoaplabException.class);
    
    
    /******************************************************************************
     * A constructor specifying a reason for this exception.
     * @param s message/reason
     ******************************************************************************/
    public SoaplabException (String s) {
	super (s);
	faultInfo = new SoaplabExceptionBean();
	faultInfo.setMessage(s);
    }

    /******************************************************************************
     * A constructor allowing to chain exceptions. <p>
     *
     * @param s message (or reason of)
     * @param e original cause of this exception
     ******************************************************************************/
    public SoaplabException (String s, Throwable e) {
	super (s, e);
	if (e != null) {
 	    formatAndLog (e, log);
 	}
	faultInfo = new SoaplabExceptionBean();
	faultInfo.setMessage(s);
    }

    /******************************************************************************
     * Format given exception 'e' depending on how serious it it. In
     * same cases add stack trace. Log the result in the given
     * 'log'. <p>
     *
     * @param e an exception to be formatted and logged
     * @param log where to log it
     *
     ******************************************************************************/
    public static void formatAndLog (Throwable e,
				     org.apache.commons.logging.Log log) {
	boolean seriousError =
	    (e instanceof java.lang.RuntimeException);
	if (seriousError || log.isDebugEnabled()) {
	    StringWriter sw = new StringWriter (500);
	    e.printStackTrace (new PrintWriter (sw));
	    if (seriousError)
		log.error (sw.toString());
	    else
		log.debug (sw.toString());
	} else {
	    log.error (e.getMessage());
	}
    }
    
    /**
     * Java type that goes as soapenv:Fault detail element.
     * 
     */
    private SoaplabExceptionBean faultInfo;

    /**
     * 
     * @param faultInfo
     * @param message
     */
    public SoaplabException(String message, SoaplabExceptionBean faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @param faultInfo
     * @param message
     * @param cause
     */
    public SoaplabException(String message, SoaplabExceptionBean faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @return
     *     returns fault bean: org.soaplab.client.jaxws.SoaplabException
     */
    public SoaplabExceptionBean getFaultInfo() {
        return faultInfo;
    }

}
