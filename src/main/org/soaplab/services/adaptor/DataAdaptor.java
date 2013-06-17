// DataAdaptor.java
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

package org.soaplab.services.adaptor;

import org.soaplab.share.SoaplabException;
import org.soaplab.services.Job;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


/**
 * An interface for adapting (changing, filtering) analysis input and
 * output data. <p>
 *
 * The implementation classes will be loaded dynamically - therefore
 * they should have a non-arg constructor. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: DataAdaptor.java,v 1.3 2007/10/26 16:40:51 marsenger Exp $
 */

public interface DataAdaptor {

    /**************************************************************************
     * Read 'source' data, adapt them, and write them back into
     * 'result'. The adapting process can benefit from knowing what
     * input or output element is adapted ('dataName') and from the
     * full metadata of the given service ('metadataAccessor'). <p>
     *
     * @param dataName identifies what data are being adapted; this is
     * usually a parameter or result name (as defined in the service
     * metadata). <p>
     *
     * @param source data to be adapted (read, processed). The adaptor
     * is supposed to <b>close</b> this source stream after reading
     * it.  <p>
     *
     * @param result data to be produced. The adaptor does not need to
     * close this result stream before returning back - but it
     * can do it. <p>
     *
     * @param job - the adapting process can benefit from knowing what
     * jobs calls it (e.g. because of access to the service metadata)
     * <p>
     *
     * @throws IOException if there were 'physical' problems with
     * reading or writing data <p>
     *
     * @throws SoaplabException if there were 'logical' problems (for
     * example, an unexpected source format, or missing pieces in the
     * source data)
     *************************************************************************/
    void process (String dataName,
		  InputStream source,
		  OutputStream result,
		  Job job)
	throws IOException, SoaplabException;

    /**************************************************************************
     * Read 'source' data, adapt them, and return them back. This
     * method can be used to combine a list of inputs into another
     * list or array, or to a merged object. <p>
     *
     * @param dataName identifies what data are being adapted; this is
     * usually a parameter or result name (as defined in the service
     * metadata). <p>
     *
     * @param source data to be adapted (read, processed). <p>
     *
     * @param job - the adapting process can benefit from knowing what
     * jobs calls it (e.g. because of access to the service metadata)
     * <p>
     *
     * @return adapted data (never null); it can be again an array or
     * list, or just a merged object <p>
     *
     * @throws IOException if there were 'physical' problems with
     * reading or writing data <p>
     *
     * @throws SoaplabException if there were 'logical' problems (for
     * example, an unexpected source format, or missing pieces in the
     * source data)
     *************************************************************************/
    Object processFromList (String dataName,
			    List source,
			    Job job)
	throws IOException, SoaplabException;

}
