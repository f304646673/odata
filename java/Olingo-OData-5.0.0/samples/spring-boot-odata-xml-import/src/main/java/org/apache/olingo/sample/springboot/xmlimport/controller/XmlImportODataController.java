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
package org.apache.olingo.sample.springboot.xmlimport.controller;

import java.util.ArrayList;

import org.apache.olingo.sample.springboot.xmlimport.edm.AdvancedXmlImportEdmProvider;
import org.apache.olingo.sample.springboot.xmlimport.processor.XmlImportEntityProcessor;
import org.apache.olingo.sample.springboot.xmlimport.processor.XmlImportMetadataProcessor;
import org.apache.olingo.sample.springboot.xmlimport.processor.XmlImportServiceDocumentProcessor;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/cars.svc")
public class XmlImportODataController {
    
    @Autowired
    private XmlImportEntityProcessor entityProcessor;
    
    @Autowired
    private XmlImportServiceDocumentProcessor serviceDocumentProcessor;
    
    @Autowired
    private XmlImportMetadataProcessor metadataProcessor;
    
    @Autowired
    private AdvancedXmlImportEdmProvider edmProvider;
    
    @RequestMapping(value = {"", "/", "/**"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public void processODataRequest(HttpServletRequest request, HttpServletResponse response) {
        try {
            OData odata = OData.newInstance();
            ServiceMetadata serviceMetadata = odata.createServiceMetadata(edmProvider, new ArrayList<>());
            
            ODataHttpHandler handler = odata.createHandler(serviceMetadata);
            
            handler.register(entityProcessor);
            handler.register(serviceDocumentProcessor);
            handler.register(metadataProcessor);
            
            HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request);
            
            handler.process(wrapper, response);
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Wrapper class to properly handle OData request paths
     */
    private static class HttpServletRequestWrapper extends jakarta.servlet.http.HttpServletRequestWrapper {
        
        public HttpServletRequestWrapper(HttpServletRequest request) {
            super(request);
        }
        
        @Override
        public String getServletPath() {
            return "/cars.svc";
        }
        
        @Override
        public String getPathInfo() {
            String requestUri = getRequestURI();
            String contextPath = getContextPath();
            
            String basePath = contextPath + "/cars.svc";
            if (requestUri.startsWith(basePath)) {
                String pathInfo = requestUri.substring(basePath.length());
                if (pathInfo.isEmpty()) {
                    return null;
                }
                return pathInfo;
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
