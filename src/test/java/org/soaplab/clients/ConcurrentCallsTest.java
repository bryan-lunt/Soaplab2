package org.soaplab.clients;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.soaplab.share.SoaplabException;
import org.soaplab.share.SoaplabMap;

public class ConcurrentCallsTest {

    private static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(ConcurrentCallsTest.class);

    public static final String AXIS_ENDPOINT = "http://localhost:8080/soaplab2-axis/services/";
    public static final String JAXWS_ENDPOINT = "http://localhost:8080/soaplab2/services/";
    public static final String TEST_SERVICE = "classic.helloworld";
    public static int NUMBER_OF_THREADS = 100;

    @BeforeClass
    public static void setUp() throws Exception {
        log
                .info("This test assumes you have deployed default Soaplab services on "
                        + "your local tomcat server using both axis1 and jaxws options\n"
                        + "  "
                        + AXIS_ENDPOINT
                        + "/\n"
                        + "  "
                        + JAXWS_ENDPOINT
                        + "/\n");
    }

    ServiceLocator locator = new ServiceLocator();

    static int nFailedThreads = 0;

    public class CallThread implements Runnable {
        SoaplabBaseClient client;

        public CallThread() {
        }

        void setClient(SoaplabBaseClient client) {
            this.client = client;
        }

        public void run() {
            try {
                SoaplabMap[] m = client.getInputSpec();
                assert (m.length == 1);
            } catch (Exception e) {
                e.printStackTrace();
                nFailedThreads++;
            }
        }
    }

    @Test
    public void axisCalls() {
        log.info("Axis-1 calls...");
        locator.setProtocol(ClientConfig.PROTOCOL_AXIS1);
        locator.setServiceEndpoint(AXIS_ENDPOINT + TEST_SERVICE);
        actualCalls();
        assertEquals(nFailedThreads, 0);
    }

    private void actualCalls() {
        nFailedThreads = 0;
        CallThread r = null;
        try {
            final SoaplabBaseClient client = new SoaplabBaseClient(locator);
            r = new CallThread();
            r.setClient(client);
        } catch (SoaplabException e) {
            fail("Problem while initializing the test: " + e);
        }
        List<Thread> threadList = new ArrayList<Thread>(NUMBER_OF_THREADS);
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            Thread t = new Thread(r);
            t.start();
            threadList.add(t);
        }
        for (Thread t : threadList)
            try {
                t.join();
            } catch (InterruptedException e) {
                fail("Problem while waiting concurrent calls finished: " + e);
            }
        log.info("Number of calls: "+NUMBER_OF_THREADS+", failed: " + nFailedThreads);
    }

    @Test
    public void jaxwsCalls() {
        log.info("JAX-WS calls...");
        locator.setProtocol(ClientConfig.PROTOCOL_JAXWS);
        locator.setServiceEndpoint(JAXWS_ENDPOINT + TEST_SERVICE);
        actualCalls();
        assertEquals(nFailedThreads, 0);
    }

}
