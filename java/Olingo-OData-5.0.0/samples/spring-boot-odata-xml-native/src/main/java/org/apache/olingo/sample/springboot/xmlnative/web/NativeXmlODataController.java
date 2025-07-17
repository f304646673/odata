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
package org.apache.olingo.sample.springboot.xmlnative.web;

import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.sample.springboot.xmlnative.edm.NativeXmlEdmProvider;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

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
 * - Provides proper error handling and logging
 */
@RestController
@RequestMapping("/odata")
public class NativeXmlODataController {

    private static final Logger LOG = LoggerFactory.getLogger(NativeXmlODataController.class);
    
    private ODataHttpHandler oDataHttpHandler;
    
    /**
     * Initialize OData HTTP handler with native XML-based EDM provider
     */
    public NativeXmlODataController() {
        LOG.info("Initializing Native XML OData Controller");
        
        try {
            // Create OData instance
            OData odata = OData.newInstance();
            
            // Create EDM provider using native XML parsing
            NativeXmlEdmProvider edmProvider = new NativeXmlEdmProvider();
            
            // Create service metadata
            ServiceMetadata serviceMetadata = odata.createServiceMetadata(edmProvider, new ArrayList<>());
            
            // Create HTTP handler
            oDataHttpHandler = odata.createHandler(serviceMetadata);
            
            LOG.info("Native XML OData Controller initialized successfully");
            
        } catch (Exception e) {
            LOG.error("Failed to initialize Native XML OData Controller: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize OData controller", e);
        }
    }
    
    /**
     * Handle all OData requests
     */
    @RequestMapping(value = "/**")
    public void handleODataRequest(HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Processing OData request: {} {}", request.getMethod(), request.getRequestURL());
        
        try {
            oDataHttpHandler.process(request, response);
        } catch (Exception e) {
            LOG.error("Error processing OData request: {}", e.getMessage(), e);
            
            // Send error response
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Internal Server Error: " + e.getMessage());
            } catch (IOException ioException) {
                LOG.error("Error writing error response: {}", ioException.getMessage(), ioException);
            }
        }
    }
}
