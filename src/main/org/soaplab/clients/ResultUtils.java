// ResultUtils.java
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

package org.soaplab.clients;

import org.soaplab.share.SoaplabException;
import org.soaplab.share.SoaplabConstants;
import org.tulsoft.tools.BaseCmdLine;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.io.IOException;
import java.io.PrintStream;
import java.io.FileOutputStream;

/**
 * Few utilities handling analysis results, such as displaying them on
 * terminal or saving them to local files. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: ResultUtils.java,v 1.8 2009/02/05 14:13:00 mahmutuludag Exp $
 */

public abstract class ResultUtils {

    /*************************************************************************
     * It helps to display and store 'result': <p>
     *
     * (1) If the 'result' is not binary and 'out' is not null, it
     * prints it to 'out' (and returns null). <p>
     *
     * (2) If the result is binary, it stores it in one or more
     * files whose names are then returned back. The names are created
     * from the 'resultName' and a unique identifier 'resultId'
     * (often, a job's id is used here by the caller). <p>
     *
     * (3) Undocumented feature: If the result is binary, but the
     * 'resultId' is null, it still will be printed to 'out'. This is
     * meant for debugging - in order to see results faster. <p>
     * 
     * (4) saveResults option has precedence over the printBinary option
     *
     * @return a list of files with the result - if it was created, or
     * null
     * @throws SoaplabException if a file should be created but could
     * not (usually because of an IO Exception); also when 'result' is
     * null
     *************************************************************************/
    public static String[] handleResult (Object result,
					 String resultName,
					 String resultId,
					 boolean saveResults,
					 PrintStream out)
    throws SoaplabException {
        boolean printBinary = resultId == null;
        if (result == null)
            throw new SoaplabException ("An empty result '" + resultName +
            "'. Correct name?");
        try {
            if (result instanceof byte[]) {
                if (out != null && printBinary==true && saveResults==false) {
                    out.println (new String ((byte[])result));
                    return null;
                } else {
                    return new String[] {
                            createResultFile (resultName, resultId, result)
                    };
                }

	    } else if (result.getClass().isArray()) {
                Object[] objs = (Object[]) result;            
                List<String> files = new ArrayList<String>();
                for (int i = 0; i < objs.length; i++) {
                    Object obj = objs[i];
                    if (saveResults || (printBinary == false && obj instanceof byte[])) {
                        files.add
			    (createResultFile (resultName + "_" +
					       String.format("%1$03d", i + 1),
					       resultId,
					       obj));
                    } else {
			if (out != null) {
			    if (obj instanceof byte[])
				out.println (new String ((byte[])obj));
			    else
				out.println(obj.toString());
			}
                    }
                }
                return files.toArray(new String[] {});

            } else {
                if (saveResults && !resultName.equals (SoaplabConstants.RESULT_REPORT)){
                    return new String[] {
			createResultFile (resultName, resultId, result)
                    };
                }
                if (out != null) {
                    out.println (result.toString());
                }
                return null;
            }
        } catch (Exception e) {
            throw new SoaplabException (e.toString(), e);
        }
    }

    /**************************************************************************
     * Clean given 'filename' in order to be able to be used as a file
     * name.
     **************************************************************************/
    static String cleanFileName (String filename) {
	return filename.replace (':', '.').replace ('/', '_');
    }

    /**************************************************************************
     * Returns a name of the created file.
     **************************************************************************/
    static String createResultFile (String resultName,
				    String resultId,
				    Object result)
	throws IOException {
	String filename = cleanFileName (resultName);
	if (StringUtils.isNotBlank (resultId)) {
	    filename += "___" + cleanFileName (resultId);
	}
	FileOutputStream fos = null;
	try {
	    fos = new FileOutputStream (filename);
	    if (result instanceof byte[])
	    IOUtils.write ((byte[])result, fos);
	    else
            IOUtils.write (result.toString(), fos);	    
	    return filename;
	} finally {
	    IOUtils.closeQuietly (fos);
	}
    }

    /**************************************************************************
     * Format info about results into 'out'.
     **************************************************************************/
    public static void formatResultInfo (Map<String,String>[] infos,
					 PrintStream out) {
	for (int i = 0; i < infos.length; i++) {
	    out.println (infos[i].get (SoaplabConstants.RESULT_NAME));
	    out.println ("\t" + "type: " +
			 infos[i].get (SoaplabConstants.RESULT_TYPE));
	    out.println ("\t" + "size: " +
			 infos[i].get (SoaplabConstants.RESULT_SIZE));
	    if (infos[i].containsKey (SoaplabConstants.RESULT_LENGTH))
		out.println ("\t" + "# array elements: " +
			     infos[i].get (SoaplabConstants.RESULT_LENGTH));
	}
    }

    /**************************************************************************
     * Extract result names (as defined in 'attrs') from the given
     * command-line (accessible using 'cmd'). If none found, return an
     * empty array, not null.
     **************************************************************************/
    public static String[] collectResultNames (BaseCmdLine cmd,
					       Map<String,Object>[] attrs)
	throws IOException {

	List<String> list = new ArrayList<String>();
	String key;
	for (int i = 0; i < attrs.length; i++) {
	    key = (String)attrs[i].get (SoaplabConstants.RESULT_NAME);
	    if (cmd.hasOption (key))
		list.add (key);
	}
	return list.toArray ( new String[] {} );
    }

    /**************************************************************************
     * Extract parameters of type '-save.&lt;result-name&gt; (check if
     * the 'result-name' exists in 'attrs') from the given
     * command-line (accessible using 'cmd'). Return extracted
     * 'result-names'. If none found, return an empty array, not null.
     **************************************************************************/
    public static String[] collectNamesToSave (BaseCmdLine cmd,
					       Map<String,Object>[] attrs)
	throws IOException {

	List<String> list = new ArrayList<String>();
	String key;
	for (int i = 0; i < attrs.length; i++) {
	    key = (String)attrs[i].get (SoaplabConstants.RESULT_NAME);
	    if (cmd.hasOption (key))
		list.add (key);
	}
	return list.toArray ( new String[] {} );
    }

}
