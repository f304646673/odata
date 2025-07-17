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
package org.apache.olingo.sample.springboot.xmlnative.controller;

import org.apache.olingo.sample.springboot.xmlnative.data.NativeXmlDataProvider;
import org.apache.olingo.sample.springboot.xmlnative.edm.NativeXmlEdmProvider;
import org.apache.olingo.sample.springboot.xmlnative.processor.NativeXmlEntityProcessor;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * Native XML OData Controller for Spring Boot
 * 
 * This controller handles all OData requests using Olingo's native XML processing capabilities.
 * It demonstrates how to use Olingo's built-in APIs without manual XML parsing.
 */
@RestController
@RequestMapping("/cars.svc")
public class NativeXmlODataController {

    private static final Logger LOG = LoggerFactory.getLogger(NativeXmlODataController.class);

    private final NativeXmlEdmProvider edmProvider;
    private final NativeXmlDataProvider dataProvider;
    private final ODataHttpHandler handler;

    /**
     * Constructor - initializes the OData handler with native XML providers
     */
    public NativeXmlODataController() {
        LOG.info("Initializing Native XML OData Controller");

        try {
            // Initialize providers using Olingo's native approach
            this.edmProvider = new NativeXmlEdmProvider();
            this.dataProvider = new NativeXmlDataProvider();

            // Create OData instance using Olingo's native factory
            OData odata = OData.newInstance();
            ServiceMetadata serviceMetadata = odata.createServiceMetadata(edmProvider, Collections.emptyList());

            // Create and configure handler using Olingo's native API
            this.handler = odata.createHandler(serviceMetadata);
            
            // Register processors using Olingo's native registration
            NativeXmlEntityProcessor entityProcessor = new NativeXmlEntityProcessor(dataProvider);
            
            handler.register(entityProcessor);

            LOG.info("Native XML OData Controller initialized successfully");
            
        } catch (Exception e) {
            LOG.error("Failed to initialize Native XML OData Controller", e);
            throw new RuntimeException("Failed to initialize OData controller", e);
        }
    }

    /**
     * Handle all OData requests using Olingo's native request processing
     */
    @RequestMapping(value = {"", "/", "/**"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public void process(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestUri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullRequest = requestUri + (queryString != null ? "?" + queryString : "");
        
        LOG.debug("Processing OData request: {} {} (Full URL: {})", 
            request.getMethod(), requestUri, fullRequest);

        try {
            // Create a wrapper to provide correct servlet path and path info for OData framework
            HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request);
            
            // Process the request using Olingo's native handler
            handler.process(wrapper, response);
            
            LOG.debug("OData request processed successfully");
            
        } catch (Exception e) {
            LOG.error("Error processing OData request: {} {}", 
                request.getMethod(), requestUri, e);
            
            // Set error response
            response.setStatus(500);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Health check endpoint
     */
    @RequestMapping("/health")
    @ResponseBody
    public Map<String, Object> health() {
        LOG.debug("Health check requested");
        
        Map<String, Object> health = Map.of(
            "status", "UP",
            "service", "Native XML OData Service",
            "edmProvider", edmProvider.getClass().getSimpleName(),
            "dataProvider", dataProvider.getClass().getSimpleName(),
            "dataStats", dataProvider.getDataStatistics(),
            "approach", "Olingo Native XML Processing"
        );
        
        LOG.debug("Health check completed: {}", health);
        return health;
    }

    /**
     * Info endpoint
     */
    @RequestMapping("/info")
    @ResponseBody
    public Map<String, Object> info() {
        LOG.debug("Info requested");
        
        Map<String, Object> info = Map.of(
            "service", "Native XML Spring Boot OData Service",
            "version", "1.0.0",
            "description", "OData service using Olingo's native XML processing capabilities",
            "features", Map.of(
                "edmSource", "Olingo native EDM provider",
                "dataProvider", "Native XML data provider with Olingo APIs",
                "xmlProcessing", "Olingo built-in XML serialization/deserialization",
                "supportedOperations", "GET (read-only in this sample)",
                "supportedFormats", "JSON, XML"
            ),
            "endpoints", Map.of(
                "service", "/cars.svc/",
                "metadata", "/cars.svc/$metadata",
                "cars", "/cars.svc/Cars",
                "manufacturers", "/cars.svc/Manufacturers",
                "health", "/cars.svc/health",
                "info", "/cars.svc/info"
            ),
            "xmlDemo", "Use /cars.svc/xml-demo to see Olingo's native XML serialization"
        );
        
        LOG.debug("Info completed: {}", info);
        return info;
    }

    /**
     * Demonstrate Olingo's native XML serialization capabilities
     */
    @RequestMapping("/xml-demo")
    @ResponseBody
    public String xmlDemo() {
        LOG.debug("XML demo requested");
        
        try {
            // Use the EDM provider's native XML serialization
            String xmlOutput = edmProvider.serializeToXml();
            
            LOG.debug("XML demo completed");
            return xmlOutput;
        } catch (Exception e) {
            LOG.error("Error in XML demo", e);
            return "Error demonstrating XML serialization: " + e.getMessage();
        }
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
