package org.soaplab.services.cmdline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.soaplab.services.metadata.ParamDef;

public class BaseParameterTest {

    /**************************************************************************
     *
     *                              Tests
     *
     **************************************************************************/

    @Test
    public void testIgnoreArray() {
	ParamDef def = new ParamDef();
	def.id = "dummy";
	BaseParameter bp = new BaseParameter (def);

	assertTrue (bp.ignoreArray (null) == null);
	assertEquals ("abc", bp.ignoreArray ("abc"));
	assertEquals ("true", bp.ignoreArray (new String[] { "true" }));
	assertEquals ("x", bp.ignoreArray (new String[] { "x", "y", "z" }));
	assertEquals ("false", bp.ignoreArray ("false".getBytes()));
	assertEquals ("x", bp.ignoreArray (new byte[][] { "x".getBytes() }));
	assertEquals ("x", bp.ignoreArray (new byte[][] { "x".getBytes(),
							 "y".getBytes(),
							 "z".getBytes() }));
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
