// PersistenceManager.java
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

import org.soaplab.services.GenUtils;
import org.soaplab.services.Job;
import org.soaplab.services.JobState;
import org.soaplab.share.SoaplabException;
import org.soaplab.share.SoaplabConstants;

import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Vector;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Date;

/**
 * A class that can handle all types of storage handlers (for storing
 * analysis results and other job-related chracteristics) - by passing
 * them requests defined by the {@link PersistentStorage}
 * interface. There is usually one instance of this class per one web
 * service (so it serves to all "jobs" of a service). <p>
 *
 * It finds all available storage handlers using the SPI mechanism. If
 * there are no handlers found, a default storage, {@link
 * FileStorage}, is instantiated. It passes requests to the first
 * handler in a blocking way (waiting for the request to complete),
 * and in a separate thread to other handlers. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: PersistenceManager.java,v 1.8 2010/08/05 12:11:32 mahmutuludag Exp $
 */

public class PersistenceManager
    implements PersistentStorage {

    private static org.apache.commons.logging.Log log =
       org.apache.commons.logging.LogFactory.getLog (PersistenceManager.class);

    // available storage elements
    protected static PersistentStorage[] storages;

    /**************************************************************************
     * A constructor. It creates instances of all classes providing
     * persistent storage. If no one is found, it instantiates the {@link
     * FileStorage} class.
     **************************************************************************/
    public PersistenceManager() {
    }

	// loads all classes providing persistent storage
	static {
	    Enumeration<PersistentStorage> spe =
		GenUtils.spiProviders (PersistentStorage.class);
	    Vector<PersistentStorage> v = new Vector<PersistentStorage>();
	    while (spe.hasMoreElements()) {
		v.addElement (spe.nextElement());
	    }
	    int size = v.size();
	    if (size == 0) {
		try {
		    v.addElement (new FileStorage());
		    size++;
		} catch (SoaplabException e) {
		    // it's reported later - see few lines below
		}
	    }
	    storages = new PersistentStorage [size];
	    v.copyInto (storages);
	    if (size > 0)
		log.info (size + " storage handler(s) registered");
	    else
		log.error ("No storage handlers found. No way to store results...");
	}

    /**************************************************************************
     *
     *        Methods implementing PersistentStorage interface
     *
     **************************************************************************/

    /**************************************************************************
     * It brings together descriptions from <em>all</em> currently
     * loaded storage elements.
     **************************************************************************/
    public String getDescription() {
	StringBuilder buf = new StringBuilder (500);
	for (int i = 0; i < storages.length; i++) {
	    buf.append (storages[i].getClass().getName());
	    if (storages[i].isSink())
		buf.append (" (a sink)");
	    String desc = storages[i].getDescription();
	    if (StringUtils.isNotBlank (desc)) {
		buf.append ("\n\t");
		buf.append (desc);
	    }
	}
	return buf.toString();
    }

    /**************************************************************************
     * It merges together lists from those storage handlers that are
     * not "sinks" (see {@link #isSink}).
     **************************************************************************/
    public String[] listJobs() {
	Set<String> jobs = new HashSet<String>();
	for (int i = 0; i < storages.length; i++) {
	    if (storages[i].isSink())
		continue;
	    String[] list = storages[i].listJobs();
	    for (int j = 0; j < list.length; j++)
		jobs.add (list[j]);
	}
	if (jobs.size() == 0)
	    return new String[] {};
 	return jobs.toArray (new String[] {});
    }

    /**************************************************************************
     * It returns the status of the given job. It takes it from the
     * first storage handler that knows this job.
     **************************************************************************/
    public JobState getJobState (String jobId)
	throws SoaplabException {

	for (int i = 0; i < storages.length; i++) {
	    if (storages[i].isSink())
		continue;
	    try {
		return storages[i].getJobState (jobId);
	    } catch (SoaplabException e) {
		// ignore
	    }
	}
	log.warn ("No storage handler has job " + jobId);
	throw new SoaplabException (SoaplabConstants.FAULT_NOT_FOUND);
    }

    /**************************************************************************
     * It returns properties of the given job. It takes them from the
     * first storage handler that knows this job.
     **************************************************************************/
    public Properties getJobProperties (String jobId)
	throws SoaplabException {

	for (int i = 0; i < storages.length; i++) {
	    if (storages[i].isSink())
		continue;
	    try {
		return storages[i].getJobProperties (jobId);
	    } catch (SoaplabException e) {
		// ignore
	    }
	}
	log.warn ("No storage handler has job " + jobId);
	throw new SoaplabException (SoaplabConstants.FAULT_NOT_FOUND);
    }

    /**************************************************************************
     * It passes the request to the first storage handler in a
     * blocking way (waiting for the request to complete), and then to
     * the other handlesr in a separate thread.
     **************************************************************************/
    public void setJobProperties (final String jobId,
				  final Properties props)
	throws SoaplabException {
	boolean atLeastOneDone = false;
	for (int i = 0; i < storages.length; i++) {
	    if (atLeastOneDone) {
		final PersistentStorage storage = storages[i];
		new Thread() {
		    public void run() {
			try {
			    storage.setJobProperties (jobId, props);
			} catch (SoaplabException e2) {
			    // ignore
			}
		    }
		}.start();
	    } else {
		try {
		    storages[i].setJobProperties (jobId, props);
		    atLeastOneDone = true;
		} catch (SoaplabException e) {
		    // ignore
		}
	    }
	}
	if (! atLeastOneDone) {
	    log.error ("No storage handler is able to store properties for " + jobId);
	    throw new SoaplabException (SoaplabConstants.FAULT_INTERNAL_ERROR);
	}
    }

    /**************************************************************************
     * It passes the request to the first storage handler in a
     * blocking way (waiting for the request to complete), and then to
     * the other handlers in separate threads.
     **************************************************************************/
    public void setJob (final Job job)
	throws SoaplabException {
	boolean atLeastOneDone = false;
	for (int i = 0; i < storages.length; i++) {
        final PersistentStorage storage = storages[i];
	    if (atLeastOneDone) {
		new Thread() {
		    public void run() {
			try {
			    storage.setJob (job);
			} catch (SoaplabException e2) {
			    // ignore
			}
		    }
		}.start();
	    } else {
		try {
		    storage.setJob (job);
		    atLeastOneDone = true;
		} catch (SoaplabException e) {
		    // ignore
		}
	    }
	}
	if (! atLeastOneDone) {
	    log.error ("No storage handler is able to store " + job.getId());
	    throw new SoaplabException (SoaplabConstants.FAULT_INTERNAL_ERROR);
	}
    }

    /**************************************************************************
     * Remove given job from all currently loaded storage handlers. It
     * does not throw any exceptions.
     **************************************************************************/
    public void removeJob (String jobId)
	throws SoaplabException {
	for (int i = 0; i < storages.length; i++) {
	    try {
		storages[i].removeJob (jobId);
	    } catch (SoaplabException e) {
		// ignored
	    }
	}
    }

    /**************************************************************************
     * Remove given job from all currently loaded storage handlers.
     **************************************************************************/
    public void removeJobs (String jobIdPattern) {
	for (int i = 0; i < storages.length; i++) {
	    storages[i].removeJobs (jobIdPattern);
	}
    }

    /**************************************************************************
     * Remove given job from all currently loaded storage handlers.
     **************************************************************************/
    public void removeJobs (Date fromDate, Date toDate) {
	for (int i = 0; i < storages.length; i++) {
	    storages[i].removeJobs (fromDate, toDate);
	}
    }

    /**************************************************************************
     * It returns results from the first storage handler that gives
     * them without errors. <p>
     *
     * NOTE: Which may not be the best strategy (they may be still
     * other results stored by other storage handlers. I may revisit
     * this if it causes problems. <p>
     *
     * No exception is thrown: if there are no result, an empty Map is
     * returned.
     **************************************************************************/
    public Map<String,Object> getResults (String jobId)
	throws SoaplabException {
	for (int i = 0; i < storages.length; i++) {
	    if (storages[i].isSink())
		continue;
	    try {
		return storages[i].getResults (jobId);
	    } catch (SoaplabException e) {
	    	SoaplabException.formatAndLog(e, log);	    	
	    }
	}
	log.warn ("No storage handler has results for " + jobId);
	return new HashMap<String,Object>();
    }

    /**************************************************************************
     * Return null if 'name' result does not exist.
     **************************************************************************/
    public Object getResult (String jobId, String name)
	throws SoaplabException {
	for (int i = 0; i < storages.length; i++) {
	    if (storages[i].isSink())
		continue;
	    try {
		return storages[i].getResult (jobId, name);
	    } catch (SoaplabException e) {
		// ignore
	    }
	}
	log.warn ("No storage handler has results for " + jobId);
	return new HashMap<String,Object>();
    }

    /**************************************************************************
     * It collects information about results in all available storage
     * handlers. It never throws any exception.
     *************************************************************************/
    @SuppressWarnings("unchecked")
    public Map<String,String>[] getResultsInfo (String jobId)
	throws SoaplabException {

	// collecting results - and keeping track what results I
	// already collected
	Map<String,Map<String,String>> infos =
	    new HashMap<String,Map<String,String>>();

	for (int i = 0; i < storages.length; i++) {
	    if (storages[i].isSink())
		continue;
	    try {
		Map<String,String>[] storageInfos =
		    storages[i].getResultsInfo (jobId);
		for (int j = 0; j < storageInfos.length; j++) {
		    Map<String,String> info = storageInfos[j];
		    String resultName = info.get (SoaplabConstants.RESULT_NAME);

		    // about each result, remember only its first
		    // occurrence (which is also a probable candidate to
		    // provide full result later)
		    if (! info.containsKey (resultName))
			infos.put (resultName, info);
		}
	    } catch (SoaplabException e) {
		// ignore
	    }
	}
	return infos.values().toArray (new HashMap [infos.size()]);
    }

    /**************************************************************************
     * It passes the request to the first storage handler in a
     * blocking way (waiting for the request to complete), and then to
     * the other handlesr in a separate thread.
     **************************************************************************/
    public void setResult (final Job job,
					final String name,
					final Object result)
	throws SoaplabException {
	boolean atLeastOneDone = false;
    if (storages==null){//means web application reloaded
        System.err.printf(Thread.currentThread().getName()+ " thread: "+
                "null storages, web application was probably reloaded\n");
        return;
    }
	for (int i = 0; i < storages.length; i++) {
        final PersistentStorage storage = storages[i];
	    if (atLeastOneDone) {
		new Thread() {
		    public void run() {
			try {
			    storage.setResult (job, name, result);
			} catch (SoaplabException e2) {
			    // ignore
			}
		    }
		}.start();
	    } else {
		try {
		    storage.setResult (job, name, result);
		    atLeastOneDone = true;
		} catch (SoaplabException e) {
		    // ignore
		}
	    }
	}
	if (! atLeastOneDone) {
	    log.error ("No storage handler is able to store " + job.getId());
	    throw new SoaplabException (SoaplabConstants.FAULT_INTERNAL_ERROR);
	}
    }

    /**************************************************************************
     * It always returns <tt>false</tt>.
     **************************************************************************/
    public boolean isSink() {
	return false;
    }
}
