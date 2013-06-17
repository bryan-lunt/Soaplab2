package org.soaplab.ebi.fasta;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.soaplab.clients.ClientForJUnit;
import org.soaplab.share.SoaplabException;
import org.soaplab.clients.ClientConfig;

public class WSJobTest {

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
	cl = new ClientForJUnit("ebi_similarity_search.fasta_protein_xml");
    }
	
	private String startFastaJob() throws SoaplabException {
		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("sequence", "swissprot:p12345");
		inputs.put("program", "fasta3");
		inputs.put("db_swissprot", "true");
		String jobId = cl.createAndRun(inputs);
		return jobId;

	}

	@Test
	public final void testRunAndWaitFor() {
		try {
			String jobId = startFastaJob();
			cl.waitFor(jobId);
		} catch (SoaplabException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("problem: " + e);
		}
	}

	@Test
	public final void testRunAndTerminate() throws Exception {
		try {
			String jobId = startFastaJob();
			// wait a bit before terminating the job
			Thread.sleep(50);
			cl.terminate(jobId);
			String newStatus = cl.getStatus(jobId);
			assertEquals(newStatus.startsWith("TERMINATED"), true);
		} catch (SoaplabException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("problem: " + e);
		}
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
