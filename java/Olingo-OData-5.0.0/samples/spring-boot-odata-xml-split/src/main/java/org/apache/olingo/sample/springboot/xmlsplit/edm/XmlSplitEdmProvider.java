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
package org.apache.olingo.sample.springboot.xmlsplit.edm;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.core.MetadataParser;
import org.apache.olingo.server.core.SchemaBasedEdmProvider;
import org.springframework.core.io.ClassPathResource;

/**
 * XML Split EDM Provider using Olingo's native XML parsing for multiple files
 * 
 * This provider demonstrates how to use Olingo's native XML parsing capabilities
 * to load EDM from multiple separate XML files and merge them into a single EDM model.
 * 
 * Key features:
 * - Loads separate XML files sequentially (address-schema.xml, main-schema.xml)
 * - Merges schemas from multiple files using SchemaBasedEdmProvider
 * - Handles cross-namespace type references (OData.Demo.Common.Address)
 * - True "split XML" approach with separate files for different concerns
 * - Fully native Olingo approach without manual XML parsing
 */
public class XmlSplitEdmProvider extends SchemaBasedEdmProvider {

    // Split XML file paths
    private static final String MAIN_XML_METADATA_FILE = "main-schema.xml";
    private static final String ADDRESS_XML_METADATA_FILE = "address-schema.xml";
    
    // Initialize the provider by loading XML metadata from split files
    public XmlSplitEdmProvider() {
        loadMetadataFromSplitXml();
    }
    
    /**
     * Load EDM metadata from split XML files using Olingo's native approach
     * This demonstrates loading multiple XML files sequentially to build a complete EDM
     */
    private void loadMetadataFromSplitXml() {
        try {
            // Create Olingo's MetadataParser
            MetadataParser parser = new MetadataParser();
            
            // First, load the address schema (contains shared complex types)
            ClassPathResource addressResource = new ClassPathResource(ADDRESS_XML_METADATA_FILE);
            if (addressResource.exists()) {
                try (Reader addressReader = new InputStreamReader(addressResource.getInputStream())) {
                    SchemaBasedEdmProvider addressProvider = parser.buildEdmProvider(addressReader);
                    
                    // Copy schemas from address provider
                    List<CsdlSchema> addressSchemas = addressProvider.getSchemas();
                    for (CsdlSchema schema : addressSchemas) {
                        this.addSchema(schema);
                    }
                }
            }
            
            // Then, load the main schema (contains entity types and container)
            ClassPathResource mainResource = new ClassPathResource(MAIN_XML_METADATA_FILE);
            if (!mainResource.exists()) {
                throw new IllegalStateException("Main XML metadata file not found: " + MAIN_XML_METADATA_FILE);
            }
            
            try (Reader mainReader = new InputStreamReader(mainResource.getInputStream())) {
                SchemaBasedEdmProvider mainProvider = parser.buildEdmProvider(mainReader);
                
                // Copy all schemas from main provider
                List<CsdlSchema> schemas = mainProvider.getSchemas();
                for (CsdlSchema schema : schemas) {
                    this.addSchema(schema);
                }
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize XmlSplitEdmProvider with split XML files", e);
        }
    }
    
    /**
     * Get entity container - ensure service document generation works correctly
     */
    @Override
    public CsdlEntityContainer getEntityContainer() throws ODataException {
        // Get the container from parent class
        CsdlEntityContainer container = super.getEntityContainer();
        
        if (container != null && container.getEntitySets() != null) {
            // Force all entity sets to be included in service document
            for (CsdlEntitySet entitySet : container.getEntitySets()) {
                entitySet.setIncludeInServiceDocument(true);
            }
        }
        
        return container;
    }
    
    /**
     * Get entity container info - this is crucial for service document generation
     */
    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) throws ODataException {
        // Let the parent class handle this using the loaded schemas
        return super.getEntityContainerInfo(entityContainerName);
    }
    
    /**
     * Get entity set - override to ensure IncludeInServiceDocument is true
     */
    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) throws ODataException {
        CsdlEntitySet entitySet = super.getEntitySet(entityContainer, entitySetName);
        if (entitySet != null) {
            // Force IncludeInServiceDocument to true
            entitySet.setIncludeInServiceDocument(true);
        }
        return entitySet;
    }
}
