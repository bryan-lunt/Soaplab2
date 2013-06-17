package org.soaplab.clients;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.soaplab.share.SoaplabException;

public class RepeatabilityTests {

    ClientForJUnit cl = null;

    @BeforeClass
    public static void init() {
	ClientConfig.get().setProperty (ClientConfig.PROP_PROTOCOL,
					ClientConfig.PROTOCOL_LOCAL);
    }

    /**************************************************************************
     *
     *                              Tests
     *
     **************************************************************************/

    @Before
	public void setUp() throws Exception {
	cl = new ClientForJUnit("gowlab.echo");
    }

	@Test
	public void testCheckRepeatability() {
		Map<String, Object> inputs = new HashMap<String, Object>();
		String[] names = { "name1", "name2", "name3" };
		inputs.put("name", names);

		try {
			cl.createAndRun(inputs);
		} catch (SoaplabException e) {
			if (e.getMessage().contains("NotValidInputs"))
				return; // means success
			e.printStackTrace();
		}
		fail("Expected to fail");
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
