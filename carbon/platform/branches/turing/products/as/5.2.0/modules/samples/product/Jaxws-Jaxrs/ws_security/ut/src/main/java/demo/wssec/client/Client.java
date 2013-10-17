/*
 * Copyright 2011-2012 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package demo.wssec.client;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.net.URL;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.hello_world_soap_http.Greeter;
import org.apache.cxf.hello_world_soap_http.GreeterService;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;




public final class Client {

    private static final String USER_NAME = System.getProperty("user.name");

    private Client() {
    }

    public static void main(String args[]) throws Exception {
        try {

            SpringBusFactory bf = new SpringBusFactory();
            URL busFile = Client.class.getResource("wssec.xml");
            Bus bus = bf.createBus(busFile.toString());
            BusFactory.setDefaultBus(bus);

            Map<String, Object> outProps = new HashMap<String, Object>();
            outProps.put("action", "UsernameToken");

            outProps.put("passwordType", "PasswordText");
            outProps.put("user", "abcd");
            outProps.put("passwordCallbackClass", "demo.wssec.client.UTPasswordCallback");

            bus.getOutInterceptors().add(new WSS4JOutInterceptor(outProps));

//            Map<String, Object> inProps = new HashMap<String, Object>();
//            inProps.put("action", "UsernameToken Timestamp");
//            inProps.put("passwordType", "PasswordText");
//            inProps.put("passwordCallbackClass", "demo.wssec.client.UTPasswordCallback");

//            bus.getInInterceptors().add(new WSS4JInInterceptor(inProps));

            GreeterService service = null;
	    if (args.length != 0 && args[0].length() != 0) {
		    File wsdlFile = new File(args[0]);
		    URL wsdlURL;
            if (wsdlFile.exists()) {
            	    wsdlURL = wsdlFile.toURL();
            } else {
                    wsdlURL = new URL(args[0]);
            }
                    // Create the service client with specified wsdlurl
                    service = new GreeterService(wsdlURL);
            } else {
                    // Create the service client with its default wsdlurl
                    service = new GreeterService();
            }
            Greeter port = service.getGreeterPort();

            String[] names = new String[] {"Anne", "Bill", "Chris", "Scott"};
            // make a sequence of 4 invocations
            for (int i = 0; i < 4; i++) {
                System.out.println("Invoking greetMe...");
                String response = port.greetMe(names[i]);
                System.out.println("response: " + response + "\n");
            }

            bus.shutdown(true);

        } catch (UndeclaredThrowableException ex) {
            ex.getUndeclaredThrowable().printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
}
