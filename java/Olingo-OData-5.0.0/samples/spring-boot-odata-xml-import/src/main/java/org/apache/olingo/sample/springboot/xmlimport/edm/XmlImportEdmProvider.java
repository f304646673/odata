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
package org.apache.olingo.sample.springboot.xmlimport.edm;

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

public class XmlImportEdmProvider extends SchemaBasedEdmProvider {
    
    private static final String MAIN_XML_METADATA_FILE = "main-schema.xml";
    private static final String ADDRESS_XML_METADATA_FILE = "address-schema.xml";
    
    public XmlImportEdmProvider() {
        loadMetadataWithImports();
    }
    
    private void loadMetadataWithImports() {
        try {
            MetadataParser parser = new MetadataParser();
            
            ClassPathResource mainResource = new ClassPathResource(MAIN_XML_METADATA_FILE);
            if (!mainResource.exists()) {
                throw new IllegalStateException("Main XML metadata file not found: " + MAIN_XML_METADATA_FILE);
            }
            
            try (Reader mainReader = new InputStreamReader(mainResource.getInputStream())) {
                SchemaBasedEdmProvider mainProvider = parser.buildEdmProvider(mainReader);
                
                List<CsdlSchema> mainSchemas = mainProvider.getSchemas();
                for (CsdlSchema schema : mainSchemas) {
                    this.addSchema(schema);
                }
                
                if (mainProvider.getReferences() != null) {
                    for (var reference : mainProvider.getReferences()) {
                        this.addReference(reference);
                    }
                }
            }
            
            ClassPathResource addressResource = new ClassPathResource(ADDRESS_XML_METADATA_FILE);
            if (addressResource.exists()) {
                try (Reader addressReader = new InputStreamReader(addressResource.getInputStream())) {
                    SchemaBasedEdmProvider addressProvider = parser.buildEdmProvider(addressReader);
                    
                    List<CsdlSchema> addressSchemas = addressProvider.getSchemas();
                    for (CsdlSchema schema : addressSchemas) {
                        this.addSchema(schema);
                    }
                }
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize XmlImportEdmProvider with XML imports", e);
        }
    }
    
    @Override
    public List<CsdlSchema> getSchemas() {
        try {
            return super.getSchemas();
        } catch (ODataException e) {
            throw new RuntimeException("Failed to get schemas", e);
        }
    }
    
    @Override
    public CsdlEntityContainer getEntityContainer() throws ODataException {
        return super.getEntityContainer();
    }
    
    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) throws ODataException {
        return super.getEntityContainerInfo(entityContainerName);
    }
    
    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) throws ODataException {
        return super.getEntitySet(entityContainer, entitySetName);
    }
}
