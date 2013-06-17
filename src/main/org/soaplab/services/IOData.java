// IOData.java
//
// Created: December 2006
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

package org.soaplab.services;

import org.soaplab.share.SoaplabException;
import org.soaplab.share.SoaplabConstants;
import org.soaplab.services.metadata.IOParamDef;
import org.soaplab.services.Job;
import org.soaplab.services.AbstractJob;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * A class representing input or output data. It stores data just
 * before they are passed to the application, and the application
 * results first appear in these objects. <p>
 *
 * The class is also responsible for fetching data from remote sites
 * (if asked for it) before sending it to the analysis. <p>
 *
 * The main method for storing data into this container is {@link
 * #setData}. It can store either direct data or a reference (this
 * class knows which is which because it gets also an IOParamDef with
 * metadata in its {@link #IOData(IOParamDef,Job,String)
 * constructor}). Some references (e.g. URL with http protocol) are
 * resolved (data fetched and stored in the local data). <p>
 *
 * Retrieving stored data back is done by methods {@link #getData} and
 * {@link #getReference}. The {@link #getData} returns data that are
 * stored in a local file. It may be null if there are no local data
 * (either not yet - because an analysis has not yet created them, or
 * they will never be - which happens for inputs that will be resolved
 * by the analysis itself). The {@link #getReference} returns a
 * reference to a data - which may be a name of a local file, or an
 * unresolved reference, or null. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: IOData.java,v 1.15 2010/08/05 12:11:31 mahmutuludag Exp $
 */

public class IOData {

    private static final org.apache.commons.logging.Log log =
       org.apache.commons.logging.LogFactory.getLog (IOData.class);

    // metadata defining parameter that created this data container
    protected IOParamDef paramDef;

    // used if the parameter value is repetitive - in that case there
    // are more IOData objects related to the same parameter
    protected String id;

    // what job this data are created for/by
    protected Job job;

    // data contents (the "real" data)
    protected String reference;
    protected File localFile;

    // where to store files
    protected File jobDir;

    /**************************************************************************
     *
     **************************************************************************/
    protected IOData() {
    }

    /**************************************************************************
     * The main constructor. <p>
     *
     * @param paramDef is a definition (metadata) describing the
     * parameter who created this data container
     *
     * @param job is a job this data container is created for
     *
     * @param id is an ID identifying uniquely this data container
     * within the same parameter and job (this is used when one
     * parameter has repetitive values, each value represenated by one
     * data container)
     *
     * @throw SoaplabException if it failed to create a directory here
     * to store data locally
     **************************************************************************/
    public IOData (IOParamDef paramDef, Job job, String id)
	throws SoaplabException {
	this.paramDef = paramDef;
        this.job = job;
        this.id = id;

	// create (or reuse) a directory where to put my data
	if (job instanceof AbstractJob)
	    jobDir = ((AbstractJob)job).getJobDir();
	else
	    jobDir = new File (System.getProperty ("java.io.tmpdir"));

	// if this is about regular output data, create a reference (to
	// a local, but not yet existing, file) - this is the case
	// when there is a reference without calling first the method
	// setData()
 	if (paramDef.isOutput()) {
	    localFile = new File (jobDir, getFileName());
 	}
    }

    /**************************************************************************
     * Log and throw an internal error.
     **************************************************************************/
    protected void internalError (String msg)
	throws SoaplabException {
	log.error (AbstractJob.INTERNAL_ERROR + msg);
	throw new SoaplabException (SoaplabConstants.FAULT_INTERNAL_ERROR);
    }

    /**************************************************************************
     * Clean given 'filename' in order to be able to be used as a file
     * name in different platforms.
     **************************************************************************/
    public static String cleanFileName (String filename) {
	return filename.replace (':', '.').replace ('/', '_');
    }

    /**************************************************************************
     * Create a temporary file that will be removed on exit.
     *************************************************************************/
    public static File createTempFile()
	throws IOException {
	File tmp = File.createTempFile ("soaplab.", null);
	// Eclipse memory analyser when run on a heap dump suggests that
	// the following deleteOnExit() call results in memory leak
	tmp.deleteOnExit();
	return tmp;
    }

    /**************************************************************************
     * Create a temporary file in the specified job directory.
     *************************************************************************/
    public static File createTempFile(File jobDir)
    throws IOException {
        File tmp = File.createTempFile ("soaplab.", null, jobDir);
        return tmp;
    }

    /**************************************************************************
     * Create a local file name for this data container.
     **************************************************************************/
    protected String getFileName() {
        if (StringUtils.isNotBlank(paramDef.dflt) && !paramDef.dflt.startsWith(File.separator))
            return paramDef.dflt;
	StringBuilder buf = new StringBuilder (100);
	buf.append (paramDef.isInput() ? "i_" : "o_");
	if (StringUtils.isNotBlank (getId())) {
	    buf.append (getId());
	    buf.append ("_");
	}
	buf.append (cleanFileName (paramDef.id));
	if (paramDef.exists (SoaplabConstants.FILE_EXT))
	    buf.append (paramDef.get (SoaplabConstants.FILE_EXT));
	return buf.toString();
    }

    /******************************************************************************
     * Store given 'data' in a local file, and clear the 'reference'
     * (so the method getReference() will later return the name of
     * this local file). <p>
     *
     * @throws SoaplabException if the IO operation failed
     ******************************************************************************/
    protected void setDirectData (Object data)
	throws SoaplabException {

	// create a file name where to store 'data'
	localFile = new File (jobDir, getFileName());
	reference = null;

	// store 'data'
	FileOutputStream fos = null;
	try {
	    if (data instanceof byte[]) {
		fos = new FileOutputStream (localFile);
		IOUtils.write ((byte[])data, fos);
		fos.close();
	    } else if (data instanceof InputStream) {
		fos = new FileOutputStream (localFile);
		IOUtils.copy ((InputStream)data, fos);
		fos.close();
	    } else if (data instanceof Reader) {
		fos = new FileOutputStream (localFile);
		IOUtils.copy ((Reader)data, fos);
		fos.close();
	    } else if (data instanceof URL) {
		// this deserves a more specialized error message
		try {
			// TBD:
			// improvements can be made as Stian (taverna team) described below (04 Jun 09)
			// If the service is clever, it can even recognize that it's one
			// of it's 'own' URLs and just access the file directly without any
			// downloading.
		    FileUtils.copyURLToFile ((URL)data, localFile);
		} catch (IOException e) {
		    String msg =
			"Either cannot read from URL '" + data + "', or " +
			"it is an internal error. The former is more probable.";
		    log.error (msg);
		    throw new SoaplabException (SoaplabConstants.FAULT_NOT_ACCEPTABLE_INPUTS);
		}
	    } else if (data instanceof File) {
		FileUtils.copyFile ((File)data, localFile);
	    } else {
		FileUtils.writeStringToFile (localFile,
					     data.toString(),
					     System.getProperty ("file.encoding"));
	    }
	    
	} catch (IOException e) {
	    internalError ("Cannot write to " + localFile + ": " + e.getMessage());
	    
	} finally {
	    if (fos != null)
		IOUtils.closeQuietly (fos);
	}
    }

    /******************************************************************************
     * See setData() for details.
     ******************************************************************************/
    protected void setReferenceData (String dataRef)
	throws SoaplabException {

	if (! job.isDataReferenceSafe (dataRef, paramDef)) {
	    // unsafe: it points to a local file outside of SANDBOX
	    String msg = "Refusing to resolve the reference: " + dataRef;
	    log.warn (msg);
	    throw new SoaplabException (msg);
	}

	try {
	    // do we have a URL?
	    URL url = new URL (dataRef);
	    setDirectData (url);

	} catch (MalformedURLException e) {
	    // given reference is not a URL
	    reference = dataRef;
	}

    }

    //
    // public methods
    //

    /**************************************************************************
     *
     **************************************************************************/
    public String getId() {
	return id;
    }

    /******************************************************************************
     * The 'data' can be either direct data or a data reference
     * (e.g. a URL) - according what is in metadata. <p>
     *
     * <u>Treating direct data</u><p>
     *
     * Store given data in a local file.  It accepts data of type
     * byte[], File, URL, InputStream or Reader.  Everything else is
     * also accepted by converting to a String using its toString()
     * method. <p>
     *
     * <u>Treating as reference</u><p>
     *
     * The 'data' is first stringified. Then, if such reference can be
     * resolvable (e.g. it is a valid URL with http protocol) it is
     * resolved and the data it points to are stored in a local
     * file. The method {@link #getReference} then returns a name of
     * this local file (the original reference is forgotten). <p>
     *
     * This method does not, however, resolve references that point to
     * local files (i.e. URLs with the 'file:' protocol), unless they
     * point to the Soaplab 'sandbox'. This is because of
     * security. <p>
     *
     * If stringified 'data' cannot be resolved it is passed to the
     * application as it is (it is up to the application to do the
     * resolution), and the method {@link #getReference} returns the
     * unchanged 'data.toString()'. <p>
     *
     * @param data to be stored (direct data or reference)
     *
     * @throws SoaplabException if the IO operation failed, or if
     * 'data' is null, or if 'data' is a reference pointing to a local
     * file that is outside of the 'sanbox' (in the lasy case the
     * error message will contain text {@link
     * SoaplabConstants#FAULT_NOT_ACCEPTABLE_INPUTS})
     ******************************************************************************/
    public void setData (boolean isDirectData, Object data)
	throws SoaplabException {

	if (data == null)
	    internalError ("IO data are null. Strange...");

	if (isDirectData) {
	    setDirectData (data);
	} else {
	    setReferenceData (data.toString());
	}
    }

    /******************************************************************************
     * Gets back data stored in a local file. Returns null if there
     * are no data stored.
     ******************************************************************************/
    public File getData() {
	return localFile;
    }

    /******************************************************************************
     * Gets back a reference to the stored data (or to data stored
     * somewhere else). Returns null if no data has been set, and no
     * reference created in the constructor (the constructor creates a
     * reference for an output, but still empty, local file).
     ******************************************************************************/
    public String getReference() {
	if (reference != null)
	    return reference;
	if (localFile == null)
	    return null;
	return localFile.getAbsolutePath();
    }

    /******************************************************************************
     * Works similar to the getReference method except it returns
     * name of the localFile rather than full file name including its path 
     ******************************************************************************/
    public String getReferenceName() {
    	if (reference != null)
    	    return reference;
    	if (localFile == null)
    	    return null;
    	return localFile.getName();
    }

    /*************************************************************************
     * Return metadata of the parameter that created this data container.
     *************************************************************************/
    public IOParamDef getDefinition() {
	return paramDef;
    }

    /******************************************************************************
     *
     ******************************************************************************/
    public String toString() {
	StringBuilder buf = new StringBuilder();
	buf.append (String.format ("%-7s", paramDef.getIOTypeAsString()).toUpperCase());
	buf.append ("(");
	buf.append (paramDef.getIOFormatAsString());
	buf.append (") ");

	String separator = "";
	String path = null;
	if (localFile != null) {
	    path = localFile.getAbsolutePath();
	    buf.append ("File: ");
	    buf.append (path);
	    separator = ", ";
	}
	if (reference != null && ! reference.equals (path)) {
	    buf.append (separator);
	    buf.append ("Reference: ");
	    buf.append (reference);
	    separator = ", ";
	}

	return buf.toString();
    }

}
