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
package org.apache.olingo.sample.springboot.xmlnative.edm;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.core.MetadataParser;
import org.apache.olingo.server.core.SchemaBasedEdmProvider;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

/**
 * Native XML EDM Provider using Olingo's built-in XML parsing capabilities
 * 
 * This provider demonstrates how to use Olingo's native XML parsing capabilities
 * to load EDM directly from XML files without manual parsing or programmatic definition.
 * 
 * Key features:
 * - Uses Olingo's MetadataParser to parse XML directly
 * - Leverages SchemaBasedEdmProvider for XML-based EDM management
 * - No manual XML parsing or programmatic EDM definition required
 * - True "native" Olingo approach to XML metadata loading
 */
public class NativeXmlEdmProvider extends SchemaBasedEdmProvider {

    
    // XML file path
    private static final String XML_METADATA_FILE = "service-metadata.xml";
    
    // Initialize the provider by loading XML metadata
    public NativeXmlEdmProvider() {
        loadMetadataFromXml();
    }
    
    /**
     * Load EDM metadata from XML file using Olingo's native MetadataParser
     */
    private void loadMetadataFromXml() {
        try {
            // Load XML metadata file from classpath
            ClassPathResource resource = new ClassPathResource(XML_METADATA_FILE);
            
            if (!resource.exists()) {
                throw new IllegalStateException("XML metadata file not found: " + XML_METADATA_FILE);
            }
            
            
            // Create Olingo's MetadataParser
            MetadataParser parser = new MetadataParser();
            
            // Parse XML and build EDM provider
            try (Reader reader = new InputStreamReader(resource.getInputStream())) {
                SchemaBasedEdmProvider xmlProvider = parser.buildEdmProvider(reader);
                
                // Copy all schemas from XML-based provider to this provider
                List<CsdlSchema> schemas = xmlProvider.getSchemas();
                for (CsdlSchema schema : schemas) {
                    this.addSchema(schema);
                }
                
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize NativeXmlEdmProvider", e);
        }
    }
}
