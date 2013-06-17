package org.soaplab.clients;

import org.soaplab.share.SoaplabException;
import org.tulsoft.tools.BaseCmdLine;

public class TestServiceClient
    extends SoaplabBaseClient {

    protected TestServiceClient (ServiceLocator locator)
	throws SoaplabException {
	super (locator);
    }

    public static void main (String[] args) {
	BaseCmdLine cmd = new BaseCmdLine (args, true);
        try {
	    ServiceLocator locator = new ServiceLocator();
	    locator.setServiceName (cmd.getParam ("-name"));
	    locator.setProtocol
		( cmd.hasOption ("-ws") ?
		  ClientConfig.PROTOCOL_JAXWS :
		  ClientConfig.PROTOCOL_LOCAL );
	    TestServiceClient client = new TestServiceClient (locator);

	    System.out.println (client.describe());
	    System.exit (0);

        } catch (Exception e) {
            e.printStackTrace();
	    System.exit (1);
	}

    }
}
