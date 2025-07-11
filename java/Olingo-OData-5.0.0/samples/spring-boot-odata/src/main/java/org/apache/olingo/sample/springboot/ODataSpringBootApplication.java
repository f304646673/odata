package org.apache.olingo.sample.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Spring Boot OData Sample Application
 * 
 * This application demonstrates how to integrate Apache Olingo OData framework
 * with Spring Boot, providing a RESTful OData service similar to the traditional
 * HttpServlet implementation but with Spring Boot's modern architecture.
 */
@SpringBootApplication
public class ODataSpringBootApplication {

    public static void main(String[] args) {
        
        try {
            ConfigurableApplicationContext context = SpringApplication.run(ODataSpringBootApplication.class, args);
        } catch (Exception e) {
            System.err.println("Error starting Spring Boot application: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
