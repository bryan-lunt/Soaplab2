// AbstractDataAdaptor.java
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

package org.soaplab.services.adaptor;

import org.soaplab.share.SoaplabException;
import org.soaplab.services.Job;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


/**
 * A nothing doing input/output adaptor. It exists mainly to prevent
 * adaptor's creators to suffer from changes in the DataAdaptor
 * interface in the future. [ Yes, it may happen. I assume that one
 * coming change will deal with the situation that an adaptor does not
 * only change (adapt) input or output data but also change them from
 * direct data to data reference or vice-versa. ] <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: AbstractDataAdaptor.java,v 1.1 2007/10/26 16:40:51 marsenger Exp $
 */

public abstract class AbstractDataAdaptor
    implements DataAdaptor {

    /**************************************************************************
     * Does nothing.
     *************************************************************************/
    public void process (String dataName,
			 InputStream source,
			 OutputStream result,
			 Job job)
	throws IOException, SoaplabException {
    }


    /**************************************************************************
     * Return 'source' unchanged.
     *************************************************************************/
    public Object processFromList (String dataName,
				   List source,
				   Job job)
	throws IOException, SoaplabException {
	return source;
    }
}
