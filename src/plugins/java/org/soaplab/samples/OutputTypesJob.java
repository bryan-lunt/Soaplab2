//OutputTypesJob.java
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

package org.soaplab.samples;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.soaplab.services.IOData;
import org.soaplab.services.JobState;
import org.soaplab.services.Reporter;
import org.soaplab.services.cmdline.CmdLineJob;
import org.soaplab.services.cmdline.NamedValues;
import org.soaplab.services.metadata.MetadataAccessor;
import org.soaplab.share.SoaplabException;

/**
 * A simple plug-in job implementing the four possible output types.
 * <p>
 * 
 * @version $Id: OutputTypesJob.java,v 1.3 2007/12/06 11:08:16 mahmutuludag Exp $
 */

public class OutputTypesJob extends CmdLineJob {

    /***************************************************************************
     * The main constructor.
     **************************************************************************/
    public OutputTypesJob(String jobId, MetadataAccessor metadataAccessor,
            Reporter reporter, Map<String, Object> sharedAttributes,
            boolean jobRecreated) throws SoaplabException {
        super(jobId, metadataAccessor, reporter, sharedAttributes, jobRecreated);
    }

    /***************************************************************************
     **************************************************************************/
    public void run() throws SoaplabException {
        IOData iodata = null;
        String input;
        if (!runAllowed())
            return;
        checkInputs();
        reporter.getState().set(JobState.RUNNING);
        NamedValues[] nvs = createNamedArgs();
        int count = Integer.parseInt((String) nvs[0].getValues().get(0));
        if (count < 0)
            throw new SoaplabException(
                    "The option 'count' has a negative value");
        if (count >100)
            throw new SoaplabException(
                    "Value of the option 'count' should be less than 100");
        IOData[] iodata_ = createIO();
        for (IOData i : iodata_) {
            if (i.getDefinition().id.equals("input")) {
                iodata = i;
                break;
            }
        }
        if (iodata == null) {
            internalError("Requried input object cannot be found");
        }

        try {
            // input
            input = FileUtils.readFileToString(iodata.getData());
        } catch (IOException e) {
            error("Error when reading input data: " + e.toString());
            reporter.getState().set(JobState.TERMINATED_BY_ERROR);
            return;
        }
        // simple_text_output
        reporter.setResult("simple_text_output", input);
        // simple_binary_output
        byte[] binary = input.getBytes();
        reporter.setResult("simple_binary_output", binary);
        // array_text_output
        String[] array_text = new String[count];
        for (int i = 0; i < count; i++) {
            array_text[i] = input;
        }
        reporter.setResult("array_text_output", array_text);
        // array_binary_output
        byte[][] array_binary = new byte[count][];
        for (int i = 0; i < count; i++) {
            array_binary[i] = input.getBytes();
        }
        reporter.setResult("array_binary_output", array_binary);

        reporter.getState().set(JobState.COMPLETED);
        reporter.setDetailedStatusResult();
        reporter.setReportResult();
        // inform other threads waiting for the termination that
        // it has been done
        synchronized (reporter) {
            reporter.notifyAll();
        }
    }
}
