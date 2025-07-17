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

import java.io.IOException;
import java.util.ArrayList;

import org.apache.olingo.sample.springboot.xmlnative.data.NativeXmlDataProvider;
import org.apache.olingo.sample.springboot.xmlnative.edm.NativeXmlEdmProvider;
import org.apache.olingo.sample.springboot.xmlnative.processor.NativeXmlEntityProcessor;
import org.apache.olingo.sample.springboot.xmlnative.processor.NativeXmlServiceDocumentProcessor;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * OData Controller for Native XML Processing
 * 
 * This controller demonstrates how to use Olingo's native XML parsing capabilities
 * for loading EDM from XML files and processing OData requests.
 * 
 * Key features:
 * - Uses MetadataParser-based EDM provider
 * - Handles standard OData endpoints ($metadata, entity collections)
 * - Integrates with Spring Boot seamlessly
 */
@RestController
@RequestMapping("/cars.svc")
public class NativeXmlODataController {
    
    private final NativeXmlDataProvider dataProvider;
    private final NativeXmlEdmProvider edmProvider;
    
    /**
     * Initialize OData controller with native XML-based EDM provider
     */
    public NativeXmlODataController() {
        // Create EDM provider using native XML parsing
        edmProvider = new NativeXmlEdmProvider();
        
        // Create data provider
        dataProvider = new NativeXmlDataProvider();
    }
    
    /**
     * Handle all OData requests - similar to the original Spring Boot sample
     */
    @RequestMapping(value = "/**")
    public void handleODataRequest(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Create OData framework components for each request (like original sample)
            OData odata = OData.newInstance();
            ServiceMetadata serviceMetadata = odata.createServiceMetadata(edmProvider, new ArrayList<>());
            
            // Create HTTP handler for this request
            ODataHttpHandler handler = odata.createHandler(serviceMetadata);
            
            // Register service document processor
            handler.register(new NativeXmlServiceDocumentProcessor());
            
            // Register entity processor
            NativeXmlEntityProcessor entityProcessor = new NativeXmlEntityProcessor(dataProvider);
            handler.register(entityProcessor);
            
            // Create wrapper to provide correct servlet path and path info for OData framework
            HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request);
            
            // Process the request
            handler.process(wrapper, response);
            
        } catch (Exception e) {
            // Send error response
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Internal Server Error: " + e.getMessage());
            } catch (IOException ioException) {
                // Handle silently
            }
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
