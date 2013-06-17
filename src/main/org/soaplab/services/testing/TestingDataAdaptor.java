// TestingDataAdaptor.java
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

package org.soaplab.services.testing;

import org.soaplab.services.adaptor.AbstractDataAdaptor;
import org.soaplab.share.SoaplabException;
import org.soaplab.services.Job;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * An example of an adaptor adapting a service result. It just accepts
 * text input and cut it into words. <p>
 *
 * In order to use, it must be specified in an ACD file. For an
 * example, see results.acd file in the Soaplab distribution. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: TestingDataAdaptor.java,v 1.2 2007/10/26 16:40:52 marsenger Exp $
 */

public class TestingDataAdaptor
    extends AbstractDataAdaptor {

    /**************************************************************************
     *
     *************************************************************************/
    @Override public void process (String dataName,
				   InputStream source,
				   OutputStream result,
				   Job job)
	throws IOException, SoaplabException {

	try {
	    List lines = IOUtils.readLines (source);
	    for (Object line: lines) {
		String done = line.toString().replaceAll ("\\s+", "\\\n");
		IOUtils.write (done, result);
	    }
	} finally {
	    IOUtils.closeQuietly (source);
	}

    }

    /**************************************************************************
     *
     *************************************************************************/
    @Override public Object processFromList (String dataName,
					     List source,
					     Job job)
	throws IOException, SoaplabException {
	return null;
    }

}
