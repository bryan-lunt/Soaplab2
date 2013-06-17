// ClientForJUnit.java
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

package org.soaplab.clients;

import java.util.Map;

import org.soaplab.share.SoaplabException;
import org.soaplab.share.SoaplabMap;

public class ClientForJUnit extends SoaplabBaseClient {

    public ClientForJUnit(String name) throws SoaplabException {
        ServiceLocator locator = new ServiceLocator();
        locator.setServiceName(name);
        setLocator(locator);
        try {
            init();
        } catch (SoaplabException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String createAndRun(Map<String, Object> inputs)
            throws SoaplabException {
        return createAndRun(SoaplabMap.fromMap(inputs));
    }

    // gets the list of service categories of the EBI Soaplab server 
    public static void main(String[] arg) {
        ServiceLocator locator = new ServiceLocator();
        locator.setProtocol(ClientConfig.PROTOCOL_AXIS1);
        locator.setServiceEndpoint("http://www.ebi.ac.uk/soaplab/services");
        try {
            SoaplabBaseClient client = new SoaplabBaseClient(locator);
            String[] catgs = client.getAvailableCategories();
            System.out.println("# of catgs: " + catgs.length);
        } catch (SoaplabException e) {
            e.printStackTrace();
        }
    }

}
