// JobManagerTest.java
//
// Created: May 2008
//
// Copyright 2008 Martin Senger
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

import org.junit.Test;
import static org.junit.Assert.*;

public class JobManagerTest {

    /**************************************************************************
     *
     *                              Tests
     *
     **************************************************************************/

    @Test
    public void createJobId() {
	JobManager jm = new JobManager();
	String name = "my.service.name";
	assertFalse (jm.createJobId (name).contains (":"));
	assertFalse (jm.createJobId (name).contains ("-"));
	assertTrue (jm.createJobId (name).contains ("_"));
    }

    @Test
    public void serviceNameFromJobId() {
	String name = "my.service.name";
	JobManager jm = new JobManager();
	String jobId = jm.createJobId (name);
	assertEquals (name, jm.serviceNameFromJobId (jobId));
	assertEquals (null, jm.serviceNameFromJobId ("blahblah"));
	assertEquals (null, jm.serviceNameFromJobId ("[blahblah"));
	assertEquals (null, jm.serviceNameFromJobId ("blahblah]"));
	assertEquals (null, jm.serviceNameFromJobId ("blah][blah"));
    }

    /**************************************************************************
     *
     *                            Optional parts
     *
     **************************************************************************/

    /**************************************************************************
     * This is to be able to run this JUnit 4 tests with a JUnit 3.x runner.
     **************************************************************************/
    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter (getThisClass());
    }

    /**************************************************************************
     * Run tests from the command line.
     **************************************************************************/
    public static void main (String args[]) {
	org.junit.runner.JUnitCore.main (getThisClassName());
    }

    /**************************************************************************
     * Get the class (name) of this class (note that this is a static
     * method). This madness is here just because I do not want to
     * change the class name in the optional methods above when I copy
     * and paste this into a new test file.
     **************************************************************************/
    private static String getThisClassName() {
	Exception e = new Exception();
	StackTraceElement[] sTrace = e.getStackTrace();
	// sTrace[0] will be always there
	return sTrace[0].getClassName();
    }

    private static Class getThisClass() {
	try {
	    return org.apache.commons.lang.ClassUtils.getClass (getThisClassName());
	} catch (ClassNotFoundException e) {
	    System.err.println ("Cannot get class name.");
	    return java.lang.Object.class;
	}
    }

}
