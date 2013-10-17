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

package org.example.customerservice.client;

import java.io.File;
import java.net.URL;

import org.example.customerservice.CustomerService;
import org.example.customerservice.CustomerServiceService;

public class CustomerServiceClient {
    protected CustomerServiceClient() {
    }
    
    public static void main(String args[]) throws Exception {
        CustomerServiceService customerServiceService;
        if (args.length != 0 && args[0].length() != 0) {
            File wsdlFile = new File(args[0]);
            URL wsdlURL;
            if (wsdlFile.exists()) {
                wsdlURL = wsdlFile.toURL();
            } else {
                wsdlURL = new URL(args[0]);
            }
            // Create the service client with specified wsdlurl
            customerServiceService = new CustomerServiceService(wsdlURL);
        } else {
            // Create the service client with its default wsdlurl
            customerServiceService = new CustomerServiceService();
        }

        CustomerService customerService = customerServiceService.getCustomerServicePort();
        
        // Initialize the test class and call the tests
        CustomerServiceTester client = new CustomerServiceTester();
        client.setCustomerService(customerService);
        client.testCustomerService();
        System.exit(0); 
    }
}
