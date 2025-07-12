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
package org.apache.olingo.sample.springboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Spring Boot OData Sample Application
 * 
 * This application demonstrates how to integrate Apache Olingo OData framework
 * with Spring Boot, providing a RESTful OData service similar to the traditional
 * HttpServlet implementation but with Spring Boot's modern architecture.
 * 
 * @author Apache Olingo
 * @version 5.0.0
 */
@SpringBootApplication
public class ODataSpringBootApplication {

    private static final Logger log = LoggerFactory.getLogger(ODataSpringBootApplication.class);

    /**
     * Main method to start the Spring Boot OData application
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            // Start Spring Boot application
            SpringApplication app = new SpringApplication(ODataSpringBootApplication.class);
            ConfigurableApplicationContext context = app.run(args);
            
            // Log application startup information
            logApplicationStartup(context.getEnvironment());
            
        } catch (Exception e) {
            log.error("Failed to start Spring Boot OData application: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    /**
     * Log application startup information including URLs and configuration
     * 
     * @param env Spring environment
     */
    private static void logApplicationStartup(Environment env) {
        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        
        String serverPort = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        String hostAddress = "localhost";
        
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("Could not determine host address", e);
        }
        
        String appName = env.getProperty("spring.application.name", "Spring Boot OData Sample");
        String[] profiles = env.getActiveProfiles().length == 0 ? 
            env.getDefaultProfiles() : env.getActiveProfiles();
        
        log.info("\n" +
            "----------------------------------------------------------\n" +
            "Application '{}' is running! Access URLs:\n" +
            "Local:      {}://localhost:{}{}\n" +
            "External:   {}://{}:{}{}\n" +
            "OData Service: {}://localhost:{}/cars.svc\n" +
            "OData Metadata: {}://localhost:{}/cars.svc/$metadata\n" +
            "Profile(s): {}\n" +
            "----------------------------------------------------------",
            appName,
            protocol, serverPort, contextPath,
            protocol, hostAddress, serverPort, contextPath,
            protocol, serverPort,
            protocol, serverPort,
            String.join(", ", profiles)
        );
    }
}
