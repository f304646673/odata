package org.apache.olingo.sample.springboot.xml;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for OData XML sample.
 * This sample demonstrates how to create an OData service using XML-based EDM metadata.
 * 
 * @author Apache Olingo
 * @version 5.0.0
 */
@SpringBootApplication
public class ODataXmlSpringBootApplication {

    /**
     * Main method to start the Spring Boot OData XML application
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ODataXmlSpringBootApplication.class, args);
    }
}
