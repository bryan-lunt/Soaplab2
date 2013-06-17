// FileStorage.java
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

package org.soaplab.services.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.soaplab.services.AbstractJob;
import org.soaplab.services.AnalysisService;
import org.soaplab.services.Config;
import org.soaplab.services.IOData;
import org.soaplab.services.Job;
import org.soaplab.services.JobState;
import org.soaplab.services.Reporter;
import org.soaplab.services.events.AnalysisEvent;
import org.soaplab.services.events.DefaultDeserializer;
import org.soaplab.services.events.EventDeserializer;
import org.soaplab.services.metadata.OutputPropertyDef;
import org.soaplab.share.SoaplabConstants;
import org.soaplab.share.SoaplabException;

/**
 * A storage handler that stores analysis results and various job
 * characteristics in a local file system. <p>
 *
 * It creates the following directory and file structure, starting
 * from a directory defined by Soaplab property {@link
 * #org.soaplab.services.Config.PROP_RESULTS_DIR PROP_RESULTS_DIR}:
 *
 * <pre>
 * [&lt;property:PROP_RESULTS_DIR&gt;]
 *   [<jobId>]
 *    job.properties
 *       last.updated = ...
 *       state.major = ...
 *       state.minor = ...
 *       state.description = ...
 *       created = ...
 *       started = ...
 *       ended = ...
 *       elapsed = ...
 *       result.&lt;result-name-1&gt; = &lt;type-of-result-name-1&gt;
 *       ...
 *    last-event.xml
 *    [results]
 *       &lt;result-name-1&gt;     // for non-arrays
 *       [&lt;result-name-2&gt;]   // for arrays
 *          00001
 *          00002
 *          ...
 *</pre>
 *
 * In the overview above, the square brackets indicate directory,
 * arrow brackets indicate variable name, plain name is a file name,
 * equals signs indicate a line in a property file). <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: FileStorage.java,v 1.23 2010/08/05 12:11:32 mahmutuludag Exp $
 */

public class FileStorage
    implements PersistentStorage {

    private static org.apache.commons.logging.Log log =
       org.apache.commons.logging.LogFactory.getLog (FileStorage.class);

    // names of job properties
    protected static final String JOB_PROP_LAST_UPDATED = "last.updated";
    protected static final String JOB_PROP_CREATED = "created";
    protected static final String JOB_PROP_STARTED = "started";
    protected static final String JOB_PROP_ENDED = "ended";
    protected static final String JOB_PROP_ELAPSED = "elapsed";
    protected static final String JOB_PROP_STATE_MAJOR = "state.major";
    protected static final String JOB_PROP_STATE_MINOR = "state.minor";
    protected static final String JOB_PROP_STATE_DESC = "state.description";
    protected static final String JOB_PROP_RESULT_PREFIX = "result.";

    // used file names
    protected static final String FILE_JOB_PROPS = "job.properties";
    protected static final String FILE_LAST_EVENT = "last-event.xml";
    protected static final String DIR_RESULTS = "results";

    // where to store everything
    protected File startDir;

    // a worker for deserialization of the last event
    EventDeserializer deserializer;

    // map of locks for directories
    private static Map<String, ReentrantReadWriteLock> locks =  new LocksMap();

    /**************************************************************************
     * The main constructor. <p>
     *
     * @throws SoaplabException if cannot find place where to store files
     **************************************************************************/
    public FileStorage()
	throws SoaplabException {

	// establish the root of my storage
	String resultDir = Config.getString (Config.PROP_RESULTS_DIR,
					     null,   // no default value
					     null,   // no service name used
					     this);
	if (resultDir == null) {
	    startDir = createAndCheckTemporary();
	} else {
	    startDir = new File (resultDir);
	    if (! createAndCheck (startDir)) {
		startDir = createAndCheckTemporary();
	    }
	}
	if (startDir == null)
	    throw new SoaplabException ("Missing file storage system.");
	log.info ("Results will be stored in " + startDir.getAbsolutePath());

	// find event deserializer
	deserializer = DefaultDeserializer.getDeserializer();
    }

    private static class LocksMap extends LinkedHashMap<String, ReentrantReadWriteLock> {

        private static final int MAX_ENTRIES = 100;

        public LocksMap() {
            super(16, 0.75f);
        }

        protected boolean removeEldestEntry(Map.Entry<String, ReentrantReadWriteLock> eldest) {
            if (size() > MAX_ENTRIES){
                if (eldest.getValue().hasQueuedThreads())
                        return false;
                else
                    return true;
            }
            return false;
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
     * Create a directory for results in a temporary directory, and
     * returns a File representing it. If failed, log an error message
     * and return null.
     **************************************************************************/
    protected File createAndCheckTemporary() {
	File dir = new File (System.getProperty ("java.io.tmpdir"),
			     "soaplab.results");
	if (createAndCheck (dir))
	    return dir;
	log.error ("Problems with temporary directory '" + startDir.getAbsolutePath() +
		   "'. No file storage will be available!");
	return null;
    }

    /**************************************************************************
     * Create the given directory 'dir' (unless it already exists - in
     * which case check that it is realy a directory), an return
     * true. If failed, log an error and return false.
     **************************************************************************/
    protected boolean createAndCheck (File dir) {
	if (! dir.exists())
	    dir.mkdirs();
	if (dir.isDirectory() && dir.exists() && dir.canWrite())
	    return true;
	log.error ("Problems with directory '" + dir.getAbsolutePath() +
		   "'. Trying a temporary directory now.");
	return false;
    }

    /**************************************************************************
     * Find and return a directory representing given job. <p>
     *
     * @param jobId indicates whose directory is asked for
     * @throws SoaplabException if such directory does not exist
     **************************************************************************/
    protected File getJobDir (String jobId)
	throws SoaplabException {

	File jobDir = new File (startDir, jobId);
	if (! jobDir.exists())
	    throw new SoaplabException (SoaplabConstants.FAULT_NOT_FOUND);
	return jobDir;
    }

    
    /**************************************************************************
     * Create and return a new directory 'name' (unless it already
     * exists) in the directory 'parent'. If failed, log an error and
     * return null.
     **************************************************************************/
    protected File createDir (File parent, String name)
    {

        File dir = new File (parent, name);

        if (!dir.exists() && !dir.mkdirs())
        {
            log.error ("Cannot create " + dir.getAbsolutePath());
            return null;
        }

        if (!dir.canWrite())
        {
            log.error ("Cannot write to " + dir.getAbsolutePath());
            return null;
        }

        return dir;
    }

    
    /**************************************************************************
     * Store job's (time) properties, job state and the last event
     * from the given 'job' to the persistent storage - which may
     * already exist, in which case update them. <p>
     *
     * But be careful not to override a job state that is newer than
     * the one in the given 'job'. This can be checked for the jib
     * state and for the last event (that has a timestamp). This is
     * here to synchronize parallele accesses to the same job from
     * different JVMs.
     **************************************************************************/
    protected File _setJob (Job job)
	throws SoaplabException {

	// create a job directory (may already exist)
	File jobDir = createDir (startDir, job.getId());
	if (jobDir == null)
	    throw new SoaplabException ("Unable to create job directory.");

	// job properties
	Properties jobProps = loadJobProperties (jobDir);
	jobProps.put (JOB_PROP_LAST_UPDATED, "" + System.currentTimeMillis());
	Reporter reporter = job.getReporter();
	if (reporter == null)
	    throw new SoaplabException ("Missing job's reporter.");
	JobState state = reporter.getState();
	    JobState lastSavedState =
		JobState.checkAndGetState (jobProps.getProperty (JOB_PROP_STATE_MAJOR));
	    if (lastSavedState.isValidChange (state.getWithoutSync())) {
		jobProps.put (JOB_PROP_STATE_MAJOR, state.getAsStringWithoutSync());
	    }
	    jobProps.put (JOB_PROP_CREATED, "" + state.getCreatedWithoutSync());
	    jobProps.put (JOB_PROP_STARTED, "" + state.getStartedWithoutSync());
	    jobProps.put (JOB_PROP_ENDED,   "" + state.getEndedWithoutSync());
	    jobProps.put (JOB_PROP_ELAPSED, "" + state.getElapsedWithoutSync());
	    String minor = state.getDetailedWithoutSync();
	    if (minor != null)
		jobProps.put (JOB_PROP_STATE_MINOR, minor);
	    String desc = state.getDescriptionWithoutSync();
	    if (desc != null)
		jobProps.put (JOB_PROP_STATE_DESC, desc);
	    saveJobProperties (jobDir, jobProps);

	    // store last event (if newer than the stored one)
	    AnalysisEvent lastSavedEvent = getLastEvent (jobDir);
	    AnalysisEvent newEvent = state.getLastEventWithoutSync();
	    if (lastSavedEvent == null || 
		lastSavedEvent.isOlderThan (newEvent)) {
		// TBD?: this still does not stop to store an event
		// that came after a job was terminated; e.g. job was
		// terminated by request and then killed - the job
		// state will be "terminated by request" but the last
		// event will contain probably the "terminated by
		// error" as the last state change
	        setLastEvent(jobDir, newEvent);
	    }

	return jobDir;
    }

    /**************************************************************************
     *
     **************************************************************************/
    protected Properties loadJobProperties (File jobDir) {
	File propsFile = new File (jobDir, FILE_JOB_PROPS);
	Properties jobProps = new Properties();
	Lock readlock = getLock(jobDir.getName(), true);
    readlock.lock();
    try {
	    FileInputStream fis = new FileInputStream (propsFile);
	    jobProps.load (fis);
	    fis.close();
	} catch (IOException e) {
	    // ignore: the file may not even exist yet
	} catch (IllegalArgumentException e) {
	    log.error ("Malformed Unicode escape appears in file " +
		       propsFile.getAbsolutePath() +
		       ". File contents ignored.");
	} finally {
        readlock.unlock();
    }
	return jobProps;
    }

    /**************************************************************************
     *
     **************************************************************************/
    protected void saveJobProperties (File jobDir, Properties jobProps) {
	File propsFile = new File (jobDir, FILE_JOB_PROPS);
	Lock writelock = getLock(jobDir.getName(), false);
    writelock.lock();
    jobDir.mkdirs();
    try {
	    FileOutputStream fos = new FileOutputStream (propsFile);
	    jobProps.store (fos, null);
	    fos.close();
	} catch (IOException e) {
	    log.error ("Cannot store job properties into " +
		       propsFile.getAbsolutePath() + ": " +
		       e.toString());
	} finally {
        writelock.unlock();
    }
	
    }

    /**************************************************************************
     * Return a 'long' property from 'props', or -1.
     **************************************************************************/
    protected long getLong (Properties props, String key) {
	try {
	    return Long.valueOf (props.getProperty (key, "-1")).longValue();
	} catch (java.lang.NumberFormatException e) {
	    return -1;
	}
    }

    /**************************************************************************
     * Store 'result' into a file 'dir/fileName'. Store also (in the
     * job properies) the 'type' of this result. Also return the
     * stored type (wich will be mostly the same as given by 'type').
     **************************************************************************/
    protected String storeResult (File dir,
				  String fileName,
				  String type,
				  Object result)
	throws SoaplabException {
	File file = new File (dir, fileName);
	FileOutputStream fos = null;
	try {
	    if (result instanceof File) {
		FileUtils.copyFile ((File)result, file);
		return (type == null ?
			SoaplabConstants.TYPE_BINARY_SIMPLE : type);

	    } else if (result instanceof byte[]) {
		fos = new FileOutputStream (file);
		IOUtils.write ((byte[])result, fos);
		fos.close();
		return (type == null ?
			SoaplabConstants.TYPE_BINARY_SIMPLE : type);

	    } else {
		FileUtils.writeStringToFile (file,
					     result.toString(),
					     System.getProperty ("file.encoding"));
		return (type == null ?
			SoaplabConstants.TYPE_TEXT_SIMPLE : type);
	    }
	} catch (Exception e) {
	    throw new SoaplabException ("Storing result " + fileName + " failed: " +
					e.toString());
	} finally {
	    IOUtils.closeQuietly (fos);
	}
    }

    /**************************************************************************
     * Read a file to a String. Throw an internal error if it fails.
     **************************************************************************/
    protected String readTextFile (File file)
	throws SoaplabException {
	try {
	    return FileUtils.readFileToString (file,
					       System.getProperty ("file.encoding"));
	} catch (IOException e) {
	    internalError ("Cannot retrieve result: " +
			   file.getAbsolutePath() + ". " + e.getMessage());
	}
	return null;   // never comes here
    }

    /**************************************************************************
     * Read a file to a byte array. Throw an internal error if it fails.
     **************************************************************************/
    protected byte[] readBinaryFile (File file)
	throws SoaplabException {

	try {
	    return FileUtils.readFileToByteArray (file);
	} catch (IOException e) {
	    internalError ("Cannot retrieve result: " +
			   file.getAbsolutePath() + ". " + e.getMessage());
	}
	return null;   // never comes here
    }

    /**************************************************************************
     * Retrieve one result from given 'jobDir', converting its the
     * result file into its correct 'type'. <p>
     *
     * Return null if 'name' result does not exist. Otherwise throw an
     * exception.
     **************************************************************************/
    protected Object getResult (File jobDir, String name, String type)
	throws SoaplabException {

	File resultFile = new File (new File (jobDir, DIR_RESULTS),
				    IOData.cleanFileName (name));
	if (! resultFile.exists())
	    return null;

	if (SoaplabConstants.TYPE_BINARY_SIMPLE.equals (type)) {
	    // byte[]
	    return readBinaryFile (resultFile);

	    // TODO: the extra 'long' bit below was added after detailed_status
	    // defined as one of the normal outputs in typed interface (i think, mahmut)
	    // we should visit here when we start returning typed report outputs
	    // as discussed 
	} else if (SoaplabConstants.TYPE_TEXT_SIMPLE.equals (type) || type.equals("long")) {
	    // String
	    return readTextFile (resultFile);

	} else if (SoaplabConstants.TYPE_BINARY_ARRAY.equals (type)) {
	    // byte[][]
	    if (! resultFile.isDirectory())
		internalError (resultFile + " should be a directory (of binary results).");
	    String[] list = resultFile.list();
	    if (list == null)
		return new byte[][] {};   // an empty result
	    Arrays.sort (list);
	    ArrayList<byte[]> results = new ArrayList<byte[]>();
	    for (int i = 0; i < list.length; i++) {
		results.add (readBinaryFile (new File (resultFile, list[i])));
	    }
	    return results.toArray (new byte[][] {});

	} else if (SoaplabConstants.TYPE_TEXT_ARRAY.equals (type)) {
	    // String[]
	    if (! resultFile.isDirectory())
		internalError (resultFile + " should be a directory (of string results).");
	    String[] list = resultFile.list();
	    if (list == null)
		return new String[] {};   // an empty result
	    Arrays.sort (list);
	    ArrayList<String> results = new ArrayList<String>();
	    for (int i = 0; i < list.length; i++) {
		results.add (readTextFile (new File (resultFile, list[i])));
	    }
	    return results.toArray (new String[] {});

	} else {
	    internalError ("Unsupported result type '" + type + "',  result name is "+name+".");
	}
	return null;  // never comes here
    }

    /**************************************************************************
     *
     **************************************************************************/
    protected Map<String,String> getResultInfo (File jobDir,
						String resultName,
						String resultType)
	throws SoaplabException {

	File resultFile = new File (new File (jobDir, DIR_RESULTS),
				    IOData.cleanFileName (resultName));
	if (! resultFile.exists())
	    return null;

	Map<String,String> info = new HashMap<String,String>();
	info.put (SoaplabConstants.RESULT_NAME, resultName);
	info.put (SoaplabConstants.RESULT_TYPE, resultType);

	if (resultFile.isFile()) {
	    info.put (SoaplabConstants.RESULT_SIZE, "" + resultFile.length());
	} else {
	    File[] list = resultFile.listFiles();
	    if (list != null) {
		info.put (SoaplabConstants.RESULT_LENGTH, "" + list.length);
		long totalSize = 0;
		for (int i = 0; i < list.length; i++)
		    totalSize += list[i].length();
		info.put (SoaplabConstants.RESULT_SIZE, "" + totalSize);
	    }
	}
	return info;
    }

    /**************************************************************************
     * Find the type of the given result. Look for it in the metadata
     * definitions (they are accessible from the given 'job'). <p>
     *
     * @return found type, or null - if the type cannot be found
     **************************************************************************/
    protected String findResultType (Job job, String resultName)
	throws SoaplabException {
	if (resultName == null)
	    return null;  // could it happen?
	try {
	    if (job.getMetadataAccessor() == null)
		return null;
	    for (OutputPropertyDef def: job.getMetadataAccessor().getOutputDefs()) {
		if (resultName.equals (def.name))
		    return AnalysisService.convertType (def.type);
	    }
	} catch (Throwable e) {
	    log.warn ("Cannot find result type: " + e.toString());
	}
	return null;
    }

    /**************************************************************************
     *
     *         Methods implementing PersistentStorage interface
     *
     **************************************************************************/


    /**************************************************************************
     *
     **************************************************************************/
    public String getDescription() {
	return "Local file system storage: " + startDir.getAbsolutePath();
    }

    /**************************************************************************
     *
     **************************************************************************/
    public String[] listJobs() {
	String[] result = startDir.list();
	return (result == null ? new String[] {} : result);
    }

    /**************************************************************************
     *
     **************************************************************************/
    public Properties getJobProperties (String jobId)
	throws SoaplabException {
	File jobDir = getJobDir (jobId);
	return loadJobProperties (jobDir);
    }

    /**************************************************************************
     *
     **************************************************************************/
    public void setJobProperties (String jobId, Properties props)
	throws SoaplabException {
	File jobDir = getJobDir (jobId);
	saveJobProperties (jobDir, props);
    }

    /**************************************************************************
     *
     **************************************************************************/
    public JobState getJobState (String jobId)
	throws SoaplabException {

	File jobDir = getJobDir (jobId);
	Properties jobProps = loadJobProperties (jobDir);
	JobState state =
	    JobState.checkAndGetState (jobProps.getProperty (JOB_PROP_STATE_MAJOR));
	state.setDetailedWithoutSave (jobProps.getProperty (JOB_PROP_STATE_MINOR));
	state.setDescriptionWithoutSave (jobProps.getProperty (JOB_PROP_STATE_DESC));
	state.setCreatedWithoutSave (getLong (jobProps, JOB_PROP_CREATED));
	state.setStartedWithoutSave (getLong (jobProps, JOB_PROP_STARTED));
	state.setEndedWithoutSave (getLong (jobProps, JOB_PROP_ENDED));

	AnalysisEvent le = getLastEvent (jobDir);
	if (le != null)
	    state.setLastEventWithoutSave (le);

	return state;
    }
        
    
    /**************************************************************************
     * Sets either a read lock or write lock for the given directory
     * If the size of the locks in the map is more than 100 then locks
     * with no queued threads are removed from the map
     **************************************************************************/
    private synchronized Lock getLock(String jobDir, boolean read){
        ReentrantReadWriteLock lock = locks.get(jobDir);
        if (lock == null){
            lock = new ReentrantReadWriteLock();
            locks.put(jobDir, lock);
        }
        Lock retLock;
        if (read==true)
            retLock = lock.readLock();
        else
            retLock = lock.writeLock();
        return retLock;
    }
    
    
    /**************************************************************************
     * Returns currently saved last event (from the given 'jobDir').
     * May return null.
     **************************************************************************/
    protected AnalysisEvent getLastEvent (File jobDir) {
	if (deserializer != null) {
	    File lastEventFile = new File (jobDir, FILE_LAST_EVENT);
        Lock readlock = getLock(jobDir.getName(), true);
        readlock.lock();
        try {
		if (lastEventFile.exists())
		    return deserializer.deserialize (this, lastEventFile);
	    } catch (Exception e) {
		log.error ("Error by reading '" + lastEventFile + "': " + e.toString());
	    }
	    finally {
	        readlock.unlock();
	    }
	}
	return null;
    }
    
    
    /**************************************************************************
     * Saves last event
     **************************************************************************/
    protected void setLastEvent(File jobDir, AnalysisEvent newEvent) {
        File lastEventFile = new File(jobDir, FILE_LAST_EVENT);
        Lock writelock = getLock(jobDir.getName(), false);
        writelock.lock();
        try {
            FileUtils.writeStringToFile(lastEventFile, newEvent.toString(),
                    System.getProperty("file.encoding"));
        } catch (IOException e) {
            log.error("Cannot write to " + lastEventFile.getAbsolutePath()
                    + ": " + e.toString());
        } finally {
            writelock.unlock();
        }
    }


    /***************************************************************************
     * 
     **************************************************************************/
    public void setJob (Job job)
	throws SoaplabException {
	_setJob (job);
    }

    /**************************************************************************
     *
     **************************************************************************/
    public void removeJob (String jobId)
	throws SoaplabException {

	// precaution: it is related to how the JobManager creates job
	// IDs (it is here intentionally in the not-too-portable way)
	if (! jobId.startsWith ("[")) {
	    log.warn ("Trying to remove strangely named job: " + jobId);
	    return;
	}

	File jobDir = getJobDir (jobId);
	Lock writeLock = getLock(jobId, false);
	writeLock.lock();
	try {
	    FileUtils.deleteDirectory (jobDir);
	} catch (IOException e) {
	    throw new SoaplabException ("Error while deleting the job directory " +
	    		"of job "+jobId+": " + e.toString());
	}
	finally {
	    writeLock.unlock();
	}
    }

    /**************************************************************************
     *
     **************************************************************************/
    public void removeJobs (String jobIdPattern) {
	try {
	    String[] jobIds = listJobs();
	    for (int i = 0; i < jobIds.length; i++) {
		if (jobIds[i].contains (jobIdPattern))
		    removeJob (jobIds[i]);
	    }
	} catch (SoaplabException e) {
	    // ignore
	}
    }

    /**************************************************************************
     *
     **************************************************************************/
    public void removeJobs (Date fromDate, Date toDate) {
	if (startDir == null)
	    return;
	if (fromDate == null && toDate == null)
	    return;

	String[] jobIds = listJobs();
	for (int i = 0; i < jobIds.length; i++) {
	    try {
		File jobDir = new File (startDir, jobIds[i]);
		Properties jobProps = loadJobProperties (jobDir);
		long lastUpdated = getLong (jobProps, JOB_PROP_LAST_UPDATED);
		if (lastUpdated > 0) {
		    Date jobDate = new Date (lastUpdated);
		    if ( (fromDate != null && jobDate.after  (fromDate)) ||
			 (toDate   != null && jobDate.before (toDate)) ) {
			removeJob (jobIds[i]);
		    }
		}

	    } catch (SoaplabException e) {
		// ignore
	    }
	}
    }

    /**************************************************************************
     *
     **************************************************************************/
    public Map<String,Object> getResults (String jobId)
	throws SoaplabException {
	File jobDir = getJobDir (jobId);
	Properties jobProps = loadJobProperties (jobDir);
	int prefixLen = JOB_PROP_RESULT_PREFIX.length();
	Map<String,Object> results = new HashMap<String,Object>();
	for (Enumeration en = jobProps.keys(); en.hasMoreElements(); ) {
	    String key = (String)en.nextElement();
	    if (key.startsWith (JOB_PROP_RESULT_PREFIX)) {
		String type = jobProps.getProperty (key);
		String resultName = key.substring (prefixLen);
		Object result = getResult (jobDir, resultName, type);
		if (result != null)
		    results.put (resultName, result);
	    }
	}
	return results;
    }

    /**************************************************************************
     * Return null if 'name' result does not exist.
     **************************************************************************/
    public Object getResult (String jobId, String name)
	throws SoaplabException {
	File jobDir = getJobDir (jobId);
	Properties jobProps = loadJobProperties (jobDir);
	String type = jobProps.getProperty (JOB_PROP_RESULT_PREFIX + name);
	return getResult (jobDir, name, type);
    }

    /**************************************************************************
     *
     *************************************************************************/
    @SuppressWarnings("unchecked")
    public Map<String,String>[] getResultsInfo (String jobId)
	throws SoaplabException {

	File jobDir = getJobDir (jobId);
	Properties jobProps = loadJobProperties (jobDir);
	int prefixLen = JOB_PROP_RESULT_PREFIX.length();

	ArrayList<Map<String,String>> infos =
	    new ArrayList<Map<String,String>>();

	for (Enumeration en = jobProps.keys(); en.hasMoreElements(); ) {
	    String key = (String)en.nextElement();
	    if (key.startsWith (JOB_PROP_RESULT_PREFIX)) {
		Map<String,String> anInfo
		    = getResultInfo (jobDir,
				     key.substring (prefixLen),
				     jobProps.getProperty (key));
		if (anInfo != null)
		    infos.add (anInfo);
	    }
	}
	return infos.toArray (new Map[] {});
    }

    /**************************************************************************
     *
     **************************************************************************/
    public void setResult (Job job, String resultName, Object result)
	throws SoaplabException {

	// create an environment (it probably already exists)
	File jobDir = _setJob (job);

	// create a result directory (may already exist)
	File resultsDir = createDir (jobDir, DIR_RESULTS);
	if (resultsDir == null)
	    throw new SoaplabException ("Cannot store result " + resultName);

	String cleanName = IOData.cleanFileName (resultName);
	String type = findResultType (job, resultName);

	Class resultClass = result.getClass();
	if (resultClass.isArray() &&
	    ! resultClass.getComponentType().equals (byte.class)) {

	    // result is an array (but not just array of bytes)
	    Object[] elements = (Object[])result;
	    File thisResultDir = createDir (resultsDir, cleanName);
	    if (thisResultDir == null)
		throw new SoaplabException ("Cannot store result " + resultName);
	    for (int i = 0; i < elements.length; i++) {
		String fileName = String.format ("%1$05d", i+1);
		type = storeResult (thisResultDir, fileName, type, elements[i]);
	    }
	    if (SoaplabConstants.TYPE_BINARY_SIMPLE.equals (type))
		type = SoaplabConstants.TYPE_BINARY_ARRAY;
	    else if (SoaplabConstants.TYPE_TEXT_SIMPLE.equals (type))
		type = SoaplabConstants.TYPE_TEXT_ARRAY;

	} else {

	    // result is not an array (or just an array of bytes)
	    type = storeResult (resultsDir, cleanName, type, result);
	}

	if (type != null) {
	    Properties jobProps = loadJobProperties (jobDir);
	    jobProps.put (JOB_PROP_RESULT_PREFIX + resultName, type);
	    jobProps.put (JOB_PROP_LAST_UPDATED, "" + System.currentTimeMillis());
	    saveJobProperties (jobDir, jobProps);
	}
    }

    /**************************************************************************
     *
     **************************************************************************/
    public boolean isSink() {
	return false;
    }

}
