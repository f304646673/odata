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

import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.sample.springboot.xmlimport.parser.AdvancedMetadataParser;
import org.apache.olingo.server.core.SchemaBasedEdmProvider;

public class AdvancedXmlImportEdmProvider extends SchemaBasedEdmProvider {
    
    private static final String MAIN_XML_METADATA_FILE = "main-schema.xml";
    
    private final AdvancedMetadataParser advancedParser;
    private final AdvancedMetadataParser.ParseStatistics statistics;
    
    public AdvancedXmlImportEdmProvider() {
        this.advancedParser = new AdvancedMetadataParser();
        
        loadMetadataWithAdvancedImportResolution();
        
        this.statistics = advancedParser.getStatistics();
    }
    
    private void loadMetadataWithAdvancedImportResolution() {
        try {
            SchemaBasedEdmProvider parsedProvider = advancedParser.buildEdmProvider(MAIN_XML_METADATA_FILE);
            
            List<CsdlSchema> schemas = parsedProvider.getSchemas();
            for (CsdlSchema schema : schemas) {
                this.addSchema(schema);
            }
            
            if (parsedProvider.getReferences() != null) {
                for (EdmxReference reference : parsedProvider.getReferences()) {
                    this.addReference(reference);
                }
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize AdvancedXmlImportEdmProvider", e);
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
    
    public AdvancedMetadataParser.ParseStatistics getParsingStatistics() {
        return statistics;
    }
    
    public Map<String, List<String>> getErrorReport() {
        return advancedParser.getErrorReport();
    }
    
    public void clearCache() {
        advancedParser.clearCache();
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
