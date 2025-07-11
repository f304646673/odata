package org.apache.olingo.sample.springboot.service;

import java.io.IOException;
import java.util.ArrayList;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.sample.springboot.data.SpringBootDataProvider;
import org.apache.olingo.sample.springboot.edm.SpringBootEdmProvider;
import org.apache.olingo.sample.springboot.processor.SpringBootCarsProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Spring Boot OData Service
 * 
 * This service is inspired by the traditional HttpServlet implementation (CarsServlet)
 * but adapted for Spring Boot architecture. It provides the same OData functionality
 * while leveraging Spring's dependency injection and configuration capabilities.
 */
@Service
public class ODataSpringBootService {

    private static final Logger LOG = LoggerFactory.getLogger(ODataSpringBootService.class);

    /**
     * Process OData HTTP request - similar to HttpServlet.service() method
     * 
     * This method mimics the behavior of the original CarsServlet.service() method:
     * 1. Manage session-based data provider
     * 2. Initialize OData framework components
     * 3. Process the request through OData handler
     */
    public void processODataRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // Session management - similar to CarsServlet
            HttpSession session = request.getSession(true);
            SpringBootDataProvider dataProvider = (SpringBootDataProvider) session.getAttribute(
                SpringBootDataProvider.class.getName());
            
            if (dataProvider == null) {
                dataProvider = new SpringBootDataProvider();
                session.setAttribute(SpringBootDataProvider.class.getName(), dataProvider);
                LOG.info("Created new Spring Boot data provider for session: {}", session.getId());
            }

            // OData framework initialization - same pattern as CarsServlet
            OData odata = OData.newInstance();
            ServiceMetadata serviceMetadata = odata.createServiceMetadata(
                new SpringBootEdmProvider(), 
                new ArrayList<>()
            );

            // Create and configure OData HTTP handler
            ODataHttpHandler handler = odata.createHandler(serviceMetadata);
            handler.register(new SpringBootCarsProcessor(dataProvider));

            // Process the request - delegate to OData framework
            handler.process(request, response);
            
            LOG.debug("Successfully processed OData request: {} {}", 
                request.getMethod(), request.getRequestURI());
                
        } catch (RuntimeException e) {
            LOG.error("Error processing OData request: {} {}", 
                request.getMethod(), request.getRequestURI(), e);
            throw new ServletException("OData processing failed", e);
        }
    }

    /**
     * Get service metadata - for debugging and introspection
     */
    public ServiceMetadata getServiceMetadata() {
        OData odata = OData.newInstance();
        return odata.createServiceMetadata(
            new SpringBootEdmProvider(), 
            new ArrayList<>()
        );
    }
}
