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
package org.apache.olingo.sample.springboot.xml.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.apache.olingo.sample.springboot.xml.data.XmlBasedDataProvider;
import org.apache.olingo.sample.springboot.xml.edm.XmlBasedEdmProvider;
import org.apache.olingo.sample.springboot.xml.processor.XmlBasedEntityProcessor;
import org.apache.olingo.sample.springboot.xml.processor.XmlBasedMetadataProcessor;
import org.apache.olingo.sample.springboot.xml.processor.XmlBasedServiceDocumentProcessor;
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

/**
 * XML-based OData Controller for Spring Boot
 * 
 * This controller handles all OData requests and routes them to the appropriate
 * processors using the XML-based EDM provider and data provider.
 */
@RestController
@RequestMapping("/cars.svc")
public class XmlBasedODataController {

    private static final Logger LOG = LoggerFactory.getLogger(XmlBasedODataController.class);

    private final XmlBasedEdmProvider edmProvider;
    private final XmlBasedDataProvider dataProvider;
    private final ODataHttpHandler handler;

    /**
     * Constructor - initializes the OData handler with XML-based providers
     */
    public XmlBasedODataController() {
        LOG.info("Initializing XML-based OData Controller");

        try {
            // Initialize providers
            this.edmProvider = new XmlBasedEdmProvider("service-metadata.xml");
            this.dataProvider = new XmlBasedDataProvider();

            // Create OData instance
            OData odata = OData.newInstance();
            ServiceMetadata serviceMetadata = odata.createServiceMetadata(edmProvider, Collections.emptyList());

            // Create and configure handler
            this.handler = odata.createHandler(serviceMetadata);
            
            // Register processors
            XmlBasedEntityProcessor entityProcessor = new XmlBasedEntityProcessor(dataProvider);
            XmlBasedMetadataProcessor metadataProcessor = new XmlBasedMetadataProcessor();
            XmlBasedServiceDocumentProcessor serviceDocumentProcessor = new XmlBasedServiceDocumentProcessor();
            
            handler.register(entityProcessor);
            handler.register(metadataProcessor);
            handler.register(serviceDocumentProcessor);

            LOG.info("XML-based OData Controller initialized successfully");
            
        } catch (Exception e) {
            LOG.error("Failed to initialize XML-based OData Controller", e);
            throw new RuntimeException("Failed to initialize OData controller", e);
        }
    }

    /**
     * Handle all OData requests
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
            
            // Process the request using the OData handler
            handler.process(wrapper, response);
            
            LOG.debug("OData request processed successfully");
            
        } catch (Exception e) {
            LOG.error("Error processing OData request: {} {}", 
                request.getMethod(), requestUri, e);
            
            // Set error response
            response.setStatus(500); // HttpServletResponse.SC_INTERNAL_SERVER_ERROR
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
            "service", "XML-based OData Service",
            "edmProvider", edmProvider.getClass().getSimpleName(),
            "dataProvider", dataProvider.getClass().getSimpleName(),
            "dataStats", dataProvider.getDataStatistics()
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
            "service", "XML-based Spring Boot OData Service",
            "version", "1.0.0",
            "description", "OData service with EDM loaded from XML file",
            "features", Map.of(
                "edmSource", "XML file (service-metadata.xml)",
                "dataProvider", "In-memory with sample data",
                "supportedOperations", "GET, POST, PUT, DELETE",
                "supportedFormats", "JSON, XML"
            ),
            "endpoints", Map.of(
                "service", "/cars.svc/",
                "metadata", "/cars.svc/$metadata",
                "cars", "/cars.svc/Cars",
                "manufacturers", "/cars.svc/Manufacturers",
                "health", "/cars.svc/health",
                "info", "/cars.svc/info"
            )
        );
        
        LOG.debug("Info completed: {}", info);
        return info;
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
