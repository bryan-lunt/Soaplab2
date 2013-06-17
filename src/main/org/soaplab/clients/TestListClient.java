package org.soaplab.clients;

import org.soaplab.share.SoaplabException;

public class TestListClient
    extends SoaplabBaseClient {

    protected TestListClient (ServiceLocator locator)
	throws SoaplabException {
	super (locator);
    }

    public static void main (String[] args) {
        try {
	    ServiceLocator locator = new ServiceLocator();
	    locator.setProtocol
		( args.length > 0 && args[0].equals ("-ws") ?
		  ClientConfig.PROTOCOL_JAXWS :
		  ClientConfig.PROTOCOL_LOCAL );
	    TestListClient client = new TestListClient (locator);

	    for (String category: client.getAvailableCategories()) {
		System.out.println (category);
		for (String analysis: client.getAvailableAnalysesInCategory (category)) {
		    System.out.println ("\t" + analysis);
		}
	    }

	    System.out.println ("\n" +
				client.getServiceLocation ("dummy"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
