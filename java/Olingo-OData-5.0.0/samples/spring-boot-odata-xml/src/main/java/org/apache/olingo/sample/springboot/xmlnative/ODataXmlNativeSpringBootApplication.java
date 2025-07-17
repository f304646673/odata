/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.olingo.sample.springboot.xmlnative;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot OData XML Native Sample Application
 * 
 * This application demonstrates how to use Olingo's native XML parsing capabilities
 * to load EDM metadata from XML files without manual XML parsing.
 */
@SpringBootApplication
public class ODataXmlNativeSpringBootApplication {

    private static final Logger LOG = LoggerFactory.getLogger(ODataXmlNativeSpringBootApplication.class);

    public static void main(String[] args) {
        LOG.info("Starting Spring Boot OData XML Native Sample Application");
        LOG.debug("Running with Spring Boot v{}, Spring v{}", 
            SpringApplication.class.getPackage().getImplementationVersion(),
            org.springframework.core.SpringVersion.getVersion());
        
        SpringApplication.run(ODataXmlNativeSpringBootApplication.class, args);
        
        LOG.info("");
        LOG.info("----------------------------------------------------------");
        LOG.info("Application 'Spring Boot OData XML Native Sample' is running! Access URLs:");
        LOG.info("Local:          http://localhost:8080/");
        LOG.info("External:       http://{}:8080/", getExternalAddress());
        LOG.info("OData Service:  http://localhost:8080/cars.svc");
        LOG.info("OData Metadata: http://localhost:8080/cars.svc/$metadata");
        LOG.info("Cars Collection: http://localhost:8080/cars.svc/Cars");
        LOG.info("Manufacturers Collection: http://localhost:8080/cars.svc/Manufacturers");
        LOG.info("Profile(s):     {}", System.getProperty("spring.profiles.active", "default"));
        LOG.info("EDM Source:     XML file with Olingo native parsing");
        LOG.info("----------------------------------------------------------");
    }

    private static String getExternalAddress() {
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "localhost";
        }
    }
}
