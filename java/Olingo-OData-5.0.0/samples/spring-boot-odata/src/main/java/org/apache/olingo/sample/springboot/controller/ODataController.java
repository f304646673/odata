package org.apache.olingo.sample.springboot.controller;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.olingo.sample.springboot.service.ODataSpringBootService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring Boot OData Controller
 * 
 * This controller exposes OData endpoints using Spring Boot's REST controller pattern.
 * It delegates the actual OData processing to the ODataSpringBootService, which
 * follows the same pattern as the traditional HttpServlet implementation.
 */
@RestController
@RequestMapping("/cars.svc")
public class ODataController {

    private static final Logger LOG = LoggerFactory.getLogger(ODataController.class);

    @Autowired
    private ODataSpringBootService odataService;

    /**
     * Handle all OData requests
     * 
     * This method catches all HTTP requests under /cars.svc/* and delegates
     * them to the ODataSpringBootService, which processes them similar to
     * how the traditional CarsServlet.service() method works.
     */
    @RequestMapping("/**")
    public void handleODataRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        LOG.debug("Processing OData request: {} {}", request.getMethod(), request.getRequestURI());
        
        try {
            // Delegate to the service layer (similar to HttpServlet pattern)
            odataService.processODataRequest(request, response);
            
        } catch (Exception e) {
            LOG.error("Error processing OData request: {} {}", 
                request.getMethod(), request.getRequestURI(), e);
            
            // Let Spring Boot handle the error response
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Internal Server Error: " + e.getMessage());
        }
    }

    /**
     * Health check endpoint
     */
    @RequestMapping("/health")
    public String health() {
        return "Spring Boot OData Service is running";
    }
}
