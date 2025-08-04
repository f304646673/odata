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
package org.apache.olingo.advanced.xmlparser.core;

import java.io.File;
import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlSingleton;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.core.SchemaBasedEdmProvider;

/**
 * Advanced Schema Provider that acts as a proxy for SchemaBasedEdmProvider
 * and provides additional validation and merge capabilities.
 * 
 * This class can be constructed from a file or directory path and provides
 * all the functionality of SchemaBasedEdmProvider while adding new features
 * for schema validation and merging.
 */
public class AdvancedSchemaProvider extends SchemaBasedEdmProvider {
    
    private final SchemaBasedEdmProvider delegateProvider;
    private final AdvancedMetadataParser parser;
    private final String originalPath;
    
    /**
     * Construct an AdvancedSchemaProvider from a file path.
     * 
     * @param schemaPath Path to an OData XML schema file
     * @throws Exception if the schema cannot be loaded
     */
    public AdvancedSchemaProvider(String schemaPath) throws Exception {
        this.originalPath = schemaPath;
        this.parser = new AdvancedMetadataParser();
        
        File schemaFile = new File(schemaPath);
        if (!schemaFile.exists()) {
            throw new IllegalArgumentException("Schema path does not exist: " + schemaPath);
        }
        
        if (schemaFile.isDirectory()) {
            this.delegateProvider = buildFromDirectory(schemaPath);
        } else {
            this.delegateProvider = parser.buildEdmProvider(schemaPath);
        }
        
        // Copy all schemas and references from delegate to this instance
        copyFromDelegate();
    }
    
    /**
     * Build provider from directory containing multiple schema files
     */
    private SchemaBasedEdmProvider buildFromDirectory(String directoryPath) throws Exception {
        File dir = new File(directoryPath);
        File[] xmlFiles = dir.listFiles((file, name) -> name.toLowerCase().endsWith(".xml"));
        
        if (xmlFiles == null || xmlFiles.length == 0) {
            throw new IllegalArgumentException("No XML files found in directory: " + directoryPath);
        }
        
        // Start with the first file
        SchemaBasedEdmProvider provider = parser.buildEdmProvider(xmlFiles[0].getAbsolutePath());
        
        // Merge remaining files
        for (int i = 1; i < xmlFiles.length; i++) {
            try {
                parser.mergeSchema(provider, xmlFiles[i].getAbsolutePath());
            } catch (Exception e) {
                throw new Exception("Failed to merge schema file: " + xmlFiles[i].getName() + 
                                  ". Error: " + e.getMessage(), e);
            }
        }
        
        return provider;
    }
    
    /**
     * Copy schemas and references from delegate provider
     */
    private void copyFromDelegate() throws Exception {
        // Copy schemas
        for (CsdlSchema schema : delegateProvider.getSchemas()) {
            try {
                java.lang.reflect.Method method = SchemaBasedEdmProvider.class.getDeclaredMethod("addSchema", CsdlSchema.class);
                method.setAccessible(true);
                method.invoke(this, schema);
            } catch (Exception e) {
                throw new RuntimeException("Failed to copy schema: " + schema.getNamespace(), e);
            }
        }
        
        // Copy references
        for (EdmxReference reference : delegateProvider.getReferences()) {
            try {
                java.lang.reflect.Method method = SchemaBasedEdmProvider.class.getDeclaredMethod("addReference", EdmxReference.class);
                method.setAccessible(true);
                method.invoke(this, reference);
            } catch (Exception e) {
                throw new RuntimeException("Failed to copy reference: " + reference.getUri(), e);
            }
        }
    }
    
    /**
     * Validate an OData 4 XML file against this provider.
     * This method does not modify this provider.
     * 
     * @param xmlSchemaPath Path to the OData 4 XML file to validate
     * @return OperationResult containing validation details and any conflicts
     * @throws Exception if validation fails
     */
    public OperationResult validateSchema(String xmlSchemaPath) throws Exception {
        // Use the parser's new OperationResult method directly
        return parser.validateSchema(delegateProvider, xmlSchemaPath);
    }

    /**
     * @deprecated Use validateSchema(String) instead
     */
    @Deprecated
    public OperationResult validateSchemaNew(String xmlSchemaPath) throws Exception {
        return validateSchema(xmlSchemaPath);
    }
    
    /**
     * Validate schemas in a directory against this provider.
     * This method does not modify this provider.
     * 
     * @param schemaDirectory Path to the directory containing OData 4 XML files
     * @return OperationResult containing validation details and any conflicts
     * @throws Exception if validation fails
     */
    public OperationResult validateSchemaDirectory(String schemaDirectory) throws Exception {
        // Use the parser's new OperationResult method directly
        return parser.validateSchemaDirectory(delegateProvider, schemaDirectory);
    }
    
    /**
     * Merge an OData 4 XML file into this provider.
     * This method modifies this provider by adding new schemas.
     * 
     * @param xmlSchemaPath Path to the OData 4 XML file to merge
     * @return OperationResult containing merge details and any conflicts
     * @throws Exception if merge fails
     */
    public OperationResult mergeSchema(String xmlSchemaPath) throws Exception {
        // Use the parser's new OperationResult method directly
        OperationResult result = parser.mergeSchema(delegateProvider, xmlSchemaPath);
        if (result.isSuccessful()) {
            // Update this instance with new schemas
            refreshFromDelegate();
        }
        return result;
    }
    
    /**
     * Merge schemas from a directory into this provider.
     * This method modifies this provider by adding new schemas.
     * 
     * @param schemaDirectory Path to the directory containing OData 4 XML files
     * @return OperationResult containing merge details and any conflicts
     * @throws Exception if merge fails
     */
    public OperationResult mergeSchemaDirectory(String schemaDirectory) throws Exception {
        // Use the parser's new OperationResult method directly
        OperationResult result = parser.mergeSchemaDirectory(delegateProvider, schemaDirectory);
        if (result.isSuccessful()) {
            // Update this instance with new schemas
            refreshFromDelegate();
        }
        return result;
    }

    /**
     * @deprecated Use mergeSchemaDirectory(String) instead
     */
    @Deprecated
    public OperationResult mergeSchemas(String schemaDirectory) throws Exception {
        return mergeSchemaDirectory(schemaDirectory);
    }

    /**
     * @deprecated Use mergeSchema(String) instead
     */
    @Deprecated
    public OperationResult mergeSchemaFile(String xmlSchemaPath) throws Exception {
        return mergeSchema(xmlSchemaPath);
    }

    /**
     * Legacy support method for tests - validates schema and returns OperationResult wrapper
     * @deprecated Use validateSchema(String) which returns OperationResult instead
     */
    @Deprecated
    public OperationResult validateSchemaLegacy(String xmlSchemaPath) throws Exception {
        return parser.validateSchemaInternal(delegateProvider, xmlSchemaPath, false);
    }

    /**
     * Legacy support method for tests - merges schemas and returns OperationResult wrapper
     * @deprecated Use mergeSchema(String) which returns OperationResult instead
     */
    @Deprecated
    public OperationResult mergeSchemasLegacy(String xmlSchemaPath) throws Exception {
        return parser.mergeSchemaInternal(delegateProvider, xmlSchemaPath);
    }
    
    /**
     * Refresh this instance from the delegate after modifications
     */
    private void refreshFromDelegate() throws Exception {
        // Clear current schemas
        getSchemas().clear();
        getReferences().clear();
        
        // Copy from delegate again
        copyFromDelegate();
    }
    
    /**
     * Get the original path used to construct this provider
     */
    public String getOriginalPath() {
        return originalPath;
    }
    
    /**
     * Get the underlying parser used by this provider
     */
    public AdvancedMetadataParser getParser() {
        return parser;
    }
    
    // Delegate all SchemaBasedEdmProvider methods to the delegate
    
    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) throws ODataException {
        return delegateProvider.getEntityType(entityTypeName);
    }
    
    @Override
    public CsdlComplexType getComplexType(FullQualifiedName complexTypeName) throws ODataException {
        return delegateProvider.getComplexType(complexTypeName);
    }
    
    @Override
    public CsdlEnumType getEnumType(FullQualifiedName enumTypeName) throws ODataException {
        return delegateProvider.getEnumType(enumTypeName);
    }
    
    @Override
    public CsdlTypeDefinition getTypeDefinition(FullQualifiedName typeDefinitionName) throws ODataException {
        return delegateProvider.getTypeDefinition(typeDefinitionName);
    }
    
    @Override
    public CsdlEntityContainer getEntityContainer() throws ODataException {
        return delegateProvider.getEntityContainer();
    }
    
    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) throws ODataException {
        return delegateProvider.getEntitySet(entityContainer, entitySetName);
    }
    
    @Override
    public CsdlSingleton getSingleton(FullQualifiedName entityContainer, String singletonName) throws ODataException {
        return delegateProvider.getSingleton(entityContainer, singletonName);
    }
    
    @Override
    public CsdlActionImport getActionImport(FullQualifiedName entityContainer, String actionImportName) throws ODataException {
        return delegateProvider.getActionImport(entityContainer, actionImportName);
    }
    
    @Override
    public CsdlFunctionImport getFunctionImport(FullQualifiedName entityContainer, String functionImportName) throws ODataException {
        return delegateProvider.getFunctionImport(entityContainer, functionImportName);
    }
    
    @Override
    public List<CsdlAction> getActions(FullQualifiedName actionName) throws ODataException {
        return delegateProvider.getActions(actionName);
    }
    
    @Override
    public List<CsdlFunction> getFunctions(FullQualifiedName functionName) throws ODataException {
        return delegateProvider.getFunctions(functionName);
    }
    
    @Override
    public CsdlTerm getTerm(FullQualifiedName termName) throws ODataException {
        return delegateProvider.getTerm(termName);
    }
}
