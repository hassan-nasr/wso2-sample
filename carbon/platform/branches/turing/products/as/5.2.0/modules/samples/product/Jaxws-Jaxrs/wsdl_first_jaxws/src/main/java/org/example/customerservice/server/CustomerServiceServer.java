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

package org.example.customerservice.server;

import javax.xml.ws.Endpoint;

import org.example.customerservice.CustomerService;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.EndpointImpl;

public class CustomerServiceServer {

    protected CustomerServiceServer() throws Exception {
        System.out.println("Starting Server");
        CustomerService implementor = new CustomerServiceImpl();
        EndpointImpl ep = (EndpointImpl)Endpoint.publish("http://localhost:9090/CustomerServicePort",
                                                         implementor);

        // Adding logging for incoming and outgoing messages
        ep.getServer().getEndpoint().getInInterceptors().add(new LoggingInInterceptor());
        ep.getServer().getEndpoint().getOutInterceptors().add(new LoggingOutInterceptor());
    }

    public static void main(String args[]) throws Exception {
        new CustomerServiceServer();
        System.out.println("Server ready...");
        Thread.sleep(5 * 60 * 1000);
        System.out.println("Server exiting");
        System.exit(0);
    }
}
