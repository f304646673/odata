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
package org.apache.olingo.advanced.xmlparser;

import org.apache.olingo.advanced.xmlparser.core.AdvancedSchemaProvider;
import org.apache.olingo.advanced.xmlparser.core.ValidationResult;
import org.apache.olingo.advanced.xmlparser.core.MergeResult;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for AdvancedSchemaProvider functionality
 */
@DisplayName("AdvancedSchemaProvider Tests")
public class AdvancedSchemaProviderTest {

    private String baseSchemaPath;
    private String extendedSchemaPath;
    private String additionalSchemaPath;
    private String conflictingSchemaPath;
    private String multiDirPath;

    @BeforeEach
    public void setUp() {
        ClassLoader classLoader = getClass().getClassLoader();
        baseSchemaPath = new File(classLoader.getResource("schemas/provider-test/base-schema.xml").getFile()).getAbsolutePath();
        extendedSchemaPath = new File(classLoader.getResource("schemas/provider-test/extended-schema.xml").getFile()).getAbsolutePath();
        additionalSchemaPath = new File(classLoader.getResource("schemas/provider-test/additional-schema.xml").getFile()).getAbsolutePath();
        conflictingSchemaPath = new File(classLoader.getResource("schemas/provider-test/conflicting-schema.xml").getFile()).getAbsolutePath();
        multiDirPath = new File(classLoader.getResource("schemas/provider-test/multi-dir").getFile()).getAbsolutePath();
    }

    @Test
    @DisplayName("Should create provider from single file")
    public void testCreateProviderFromSingleFile() throws Exception {
        AdvancedSchemaProvider provider = new AdvancedSchemaProvider(baseSchemaPath);
        
        assertNotNull(provider, "Provider should be created");
        assertEquals(baseSchemaPath, provider.getOriginalPath(), "Original path should match");
        assertNotNull(provider.getParser(), "Parser should be available");
        
        // Verify schemas are loaded
        List<CsdlSchema> schemas = provider.getSchemas();
        assertFalse(schemas.isEmpty(), "Should have at least one schema");
        
        // Check specific schema content
        boolean foundBaseSchema = false;
        for (CsdlSchema schema : schemas) {
            if ("Test.Base".equals(schema.getNamespace())) {
                foundBaseSchema = true;
                break;
            }
        }
        assertTrue(foundBaseSchema, "Should contain Test.Base schema");
    }

    @Test
    @DisplayName("Should create provider from directory")
    public void testCreateProviderFromDirectory() throws Exception {
        AdvancedSchemaProvider provider = new AdvancedSchemaProvider(multiDirPath);
        
        assertNotNull(provider, "Provider should be created");
        assertEquals(multiDirPath, provider.getOriginalPath(), "Original path should match");
        
        // Verify multiple schemas are loaded
        List<CsdlSchema> schemas = provider.getSchemas();
        assertTrue(schemas.size() >= 2, "Should have multiple schemas from directory");
        
        // Check for both expected namespaces
        boolean foundCore = false, foundService = false;
        for (CsdlSchema schema : schemas) {
            if ("Test.MultiDir.Core".equals(schema.getNamespace())) {
                foundCore = true;
            } else if ("Test.MultiDir.Service".equals(schema.getNamespace())) {
                foundService = true;
            }
        }
        assertTrue(foundCore, "Should contain Test.MultiDir.Core schema");
        assertTrue(foundService, "Should contain Test.MultiDir.Service schema");
    }

    @Test
    @DisplayName("Should validate compatible schema successfully")
    public void testValidateCompatibleSchema() throws Exception {
        AdvancedSchemaProvider provider = new AdvancedSchemaProvider(baseSchemaPath);
        
        ValidationResult result = provider.validateSchema(extendedSchemaPath);
        
        assertNotNull(result, "Validation result should not be null");
        assertTrue(result.isValid(), "Validation should succeed for compatible schema");
        assertTrue(result.getErrors().isEmpty(), "Should have no errors");
        
        // Check messages
        assertFalse(result.getMessages().isEmpty(), "Should have validation messages");
    }

    @Test
    @DisplayName("Should validate directory successfully")
    public void testValidateDirectory() throws Exception {
        AdvancedSchemaProvider provider = new AdvancedSchemaProvider(baseSchemaPath);
        
        ValidationResult result = provider.validateSchemaDirectory(multiDirPath);
        
        assertNotNull(result, "Validation result should not be null");
        assertTrue(result.isValid(), "Directory validation should succeed");
        assertFalse(result.getMessages().isEmpty(), "Should have validation messages");
    }

    @Test
    @DisplayName("Should detect schema conflicts during validation")
    public void testValidateConflictingSchema() throws Exception {
        AdvancedSchemaProvider provider = new AdvancedSchemaProvider(baseSchemaPath);
        
        ValidationResult result = provider.validateSchema(conflictingSchemaPath);
        
        assertNotNull(result, "Validation result should not be null");
        // Depending on implementation, this might succeed with warnings or fail
        if (!result.isValid()) {
            assertFalse(result.getErrors().isEmpty(), "Should have errors for conflicting schema");
        } else {
            // If validation succeeds, there should be warnings about conflicts
            assertFalse(result.getWarnings().isEmpty() || result.getMessages().isEmpty(), 
                       "Should have warnings or messages about conflicts");
        }
    }

    @Test
    @DisplayName("Should merge compatible schema successfully")
    public void testMergeCompatibleSchema() throws Exception {
        AdvancedSchemaProvider provider = new AdvancedSchemaProvider(baseSchemaPath);
        int originalSchemaCount = provider.getSchemas().size();
        
        MergeResult result = provider.mergeSchema(additionalSchemaPath);
        
        assertNotNull(result, "Merge result should not be null");
        assertTrue(result.isSuccessful(), "Merge should succeed");
        assertTrue(result.getErrors().isEmpty(), "Should have no errors");
        
        // Verify schema was added
        int newSchemaCount = provider.getSchemas().size();
        assertTrue(newSchemaCount >= originalSchemaCount, "Should have equal or more schemas after merge");
        
        // Check for the new schema namespace
        boolean foundAdditionalSchema = false;
        for (CsdlSchema schema : provider.getSchemas()) {
            if ("Test.Additional".equals(schema.getNamespace())) {
                foundAdditionalSchema = true;
                break;
            }
        }
        assertTrue(foundAdditionalSchema, "Should contain merged Test.Additional schema");
    }

    @Test
    @DisplayName("Should merge directory successfully")
    public void testMergeDirectory() throws Exception {
        AdvancedSchemaProvider provider = new AdvancedSchemaProvider(baseSchemaPath);
        int originalSchemaCount = provider.getSchemas().size();
        
        MergeResult result = provider.mergeSchemaDirectory(multiDirPath);
        
        assertNotNull(result, "Merge result should not be null");
        assertTrue(result.isSuccessful(), "Directory merge should succeed");
        
        // Verify schemas were added
        int newSchemaCount = provider.getSchemas().size();
        assertTrue(newSchemaCount > originalSchemaCount, "Should have more schemas after directory merge");
        
        // Check messages
        assertFalse(result.getMessages().isEmpty(), "Should have merge messages");
    }

    @Test
    @DisplayName("Should provide EdmProvider functionality")
    public void testEdmProviderFunctionality() throws Exception {
        AdvancedSchemaProvider provider = new AdvancedSchemaProvider(baseSchemaPath);
        
        // Test entity type access
        CsdlEntityType baseEntity = provider.getEntityType(new FullQualifiedName("Test.Base", "BaseEntity"));
        assertNotNull(baseEntity, "Should be able to access BaseEntity");
        assertEquals("BaseEntity", baseEntity.getName(), "Entity name should match");
        
        // Test complex type access
        CsdlComplexType address = provider.getComplexType(new FullQualifiedName("Test.Base", "Address"));
        assertNotNull(address, "Should be able to access Address complex type");
        assertEquals("Address", address.getName(), "Complex type name should match");
        
        // Test enum type access
        CsdlEnumType status = provider.getEnumType(new FullQualifiedName("Test.Base", "Status"));
        assertNotNull(status, "Should be able to access Status enum type");
        assertEquals("Status", status.getName(), "Enum type name should match");
        
        // Test entity container access
        assertNotNull(provider.getEntityContainer(), "Should have entity container");
    }

    @Test
    @DisplayName("Should handle non-existent file gracefully")
    public void testNonExistentFile() {
        String nonExistentPath = "/non/existent/path/schema.xml";
        
        assertThrows(IllegalArgumentException.class, () -> {
            new AdvancedSchemaProvider(nonExistentPath);
        }, "Should throw exception for non-existent file");
    }

    @Test
    @DisplayName("Should handle empty directory gracefully")
    public void testEmptyDirectory() throws Exception {
        // Create a temporary empty directory for testing
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "empty-test-dir-" + System.currentTimeMillis());
        tempDir.mkdirs();
        
        try {
            assertThrows(IllegalArgumentException.class, () -> {
                new AdvancedSchemaProvider(tempDir.getAbsolutePath());
            }, "Should throw exception for empty directory");
        } finally {
            tempDir.delete();
        }
    }

    @Test
    @DisplayName("Should maintain state consistency after operations")
    public void testStateConsistency() throws Exception {
        AdvancedSchemaProvider provider = new AdvancedSchemaProvider(baseSchemaPath);
        
        // Record initial state
        int initialSchemaCount = provider.getSchemas().size();
        String initialPath = provider.getOriginalPath();
        
        // Perform validation (should not change state)
        ValidationResult validationResult = provider.validateSchema(additionalSchemaPath);
        assertTrue(validationResult.isValid(), "Validation should succeed");
        assertEquals(initialSchemaCount, provider.getSchemas().size(), 
                    "Schema count should not change after validation");
        
        // Perform merge (should change state)
        MergeResult mergeResult = provider.mergeSchema(additionalSchemaPath);
        assertTrue(mergeResult.isSuccessful(), "Merge should succeed");
        assertTrue(provider.getSchemas().size() >= initialSchemaCount, 
                  "Schema count should increase or stay same after merge");
        
        // Original path should remain unchanged
        assertEquals(initialPath, provider.getOriginalPath(), 
                    "Original path should remain unchanged");
    }
}
