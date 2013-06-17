// InputManyFiles.java
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.List;
import java.util.ArrayList;


/**
 * An input adaptor used for the "filelist" input. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: InputManyFiles.java,v 1.10 2011/04/06 13:21:17 mahmutuludag Exp $
 */

public class InputManyFiles
    extends AbstractDataAdaptor {

    private static org.apache.commons.logging.Log log =
       org.apache.commons.logging.LogFactory.getLog (InputManyFiles.class);

    /**************************************************************************
     * Does nothing.
     *************************************************************************/
    @Override public void process (String dataName,
				   InputStream source,
				   OutputStream result,
				   Job job)
	throws IOException, SoaplabException {
	log.warn ("This adaptor does not do anything in the process() method. Are you sure to use it?");
    }


    /**************************************************************************
     * The 'source' are input files, each of them as an byte
     * array. Store them in a temporary place (perhaps in SANDBOX?)
     * and return a list of their (fully qualified) names - as a
     * String containing new-lines between file names. <p>
     *
     * @return type String
     *************************************************************************/
    @Override public Object processFromList (String dataName,
					     List source,
					     Job job)
    throws IOException, SoaplabException {

        // keep here a list of names of the created files
        List<String> filenames = new ArrayList<String>();

        // create individual files
        for (Object obj: source)
        {
            if (obj==null)
                throw new SoaplabException (job.getMetadataAccessor().getServiceName()
                        + ": " + "Null file content, for input " +
                        dataName);

            File file = File.createTempFile ("soaplab.", null, job.getJobDir());

            if (obj instanceof byte[])
                FileUtils.writeByteArrayToFile (file, (byte[])obj);
            else
                FileUtils.writeStringToFile (file, obj.toString());

            filenames.add (file.getAbsolutePath());
        }
        
        boolean isEMBOSS = job.getClass().getName().equals(
                "org.soaplab.emboss.EmbossJob");

        // make something with the list of file names (separated do
        // the sub-classes can do something else with it)
        return formatFilenames (filenames, isEMBOSS);
    }

    /**************************************************************************
     * Given a list of filenames, return them as a new-line separated
     * or comma separated list in case of EMBOSS jobs.
     **************************************************************************/
    protected String formatFilenames (List<String> filenames, boolean isEMBOSS)
	throws IOException
	{
        char separator = isEMBOSS ? ',' : '\n';
        
        return StringUtils.join (filenames, separator);
    }

}
