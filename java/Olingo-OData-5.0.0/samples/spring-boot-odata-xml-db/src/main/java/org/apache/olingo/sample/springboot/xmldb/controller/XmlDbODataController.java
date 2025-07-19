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
package org.apache.olingo.sample.springboot.xmldb.controller;

import org.apache.olingo.sample.springboot.xmldb.edm.XmlDbEdmProvider;
import org.apache.olingo.sample.springboot.xmldb.processor.XmlDbEntityProcessor;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/xmldb-odata")
public class XmlDbODataController {

    @Autowired
    private XmlDbEdmProvider edmProvider;

    @Autowired
    private XmlDbEntityProcessor entityProcessor;

    @RequestMapping(value = "**")
    public void process(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Create OData handler
            OData odata = OData.newInstance();
            ServiceMetadata edm = odata.createServiceMetadata(edmProvider, java.util.Collections.emptyList());
            ODataHttpHandler handler = odata.createHandler(edm);

            // Register processors
            handler.register(entityProcessor);

            // Process request
            handler.process(request, response);
        } catch (RuntimeException e) {
            throw new RuntimeException("OData request failed.", e);
        }
    }
}
