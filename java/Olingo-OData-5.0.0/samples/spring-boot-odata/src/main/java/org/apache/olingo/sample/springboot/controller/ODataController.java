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
import org.springframework.web.bind.annotation.RequestMethod;
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
    @RequestMapping(value = {"", "/", "/**"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public void handleODataRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String requestUri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullRequest = requestUri + (queryString != null ? "?" + queryString : "");
        
        LOG.info("Processing OData request: {} {} (Full URL: {})", 
            request.getMethod(), requestUri, fullRequest);
        LOG.debug("Request headers:");
        request.getHeaderNames().asIterator().forEachRemaining(header -> 
            LOG.debug("  {}: {}", header, request.getHeader(header)));
        
        // Special handling for $metadata requests
        if (requestUri.endsWith("$metadata") || requestUri.endsWith("$metadata/")) {
            LOG.info("Detected $metadata request - should return metadata document");
        }
        
        try {
            // Create a wrapper to provide correct servlet path and path info for OData framework
            HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request);
            
            // Delegate to the service layer (similar to HttpServlet pattern)
            odataService.processODataRequest(wrapper, response);
            
        } catch (Exception e) {
            LOG.error("Error processing OData request: {} {}", 
                request.getMethod(), requestUri, e);
            
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
    
    /**
     * HttpServletRequest wrapper to provide correct path information for OData framework
     */
    private static class HttpServletRequestWrapper extends jakarta.servlet.http.HttpServletRequestWrapper {
        
        public HttpServletRequestWrapper(HttpServletRequest request) {
            super(request);
        }
        
        @Override
        public String getServletPath() {
            // Return the base service path
            return "/cars.svc";
        }
        
        @Override
        public String getPathInfo() {
            String requestUri = getRequestURI();
            String contextPath = getContextPath();
            
            // Remove context path and servlet path to get the path info
            String basePath = contextPath + "/cars.svc";
            if (requestUri.startsWith(basePath)) {
                String pathInfo = requestUri.substring(basePath.length());
                if (pathInfo.isEmpty()) {
                    return null; // This represents the service document request
                }
                return pathInfo; // This should be "/$metadata" for metadata requests
            }
            return null;
        }
        
        @Override
        public String getRequestURI() {
            return super.getRequestURI();
        }
        
        @Override
        public StringBuffer getRequestURL() {
            return super.getRequestURL();
        }
    }
}
