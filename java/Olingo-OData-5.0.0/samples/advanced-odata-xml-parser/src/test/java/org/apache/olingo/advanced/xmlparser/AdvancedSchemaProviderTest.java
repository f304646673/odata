/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under 2.0 (the
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

import java.io.File;
import java.util.List;

import org.apache.olingo.advanced.xmlparser.core.AdvancedSchemaProvider;
import org.apache.olingo.advanced.xmlparser.core.OperationResult;
import org.apache.olingo.advanced.xmlparser.core.OperationType;
import org.apache.olingo.advanced.xmlparser.core.ResultType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test class for AdvancedSchemaProvider functionality
 * Updated to use OperationResult and ResultType for better error type checking
 */
@DisplayName("AdvancedSchemaProvider Tests")
public class AdvancedSchemaProviderTest {

    private String baseSchemaPath;
    private String extendedSchemaPath;
    private String additionalSchemaPath;
    private String conflictingSchemaPath;
    private String multiDirPath;
    private String nonExistentFilePath;
    private String malformedSchemaPath;
    private String emptySchemaPath;

    @BeforeEach
    public void setUp() {
        ClassLoader classLoader = getClass().getClassLoader();
        baseSchemaPath = new File(classLoader.getResource("schemas/provider-test/base-schema.xml").getFile()).getAbsolutePath();
        extendedSchemaPath = new File(classLoader.getResource("schemas/provider-test/extended-schema.xml").getFile()).getAbsolutePath();
        additionalSchemaPath = new File(classLoader.getResource("schemas/provider-test/additional-schema.xml").getFile()).getAbsolutePath();
        conflictingSchemaPath = new File(classLoader.getResource("schemas/provider-test/conflicting-schema.xml").getFile()).getAbsolutePath();
        multiDirPath = new File(classLoader.getResource("schemas/provider-test/multi-dir").getFile()).getAbsolutePath();
        
        // These files may not exist - we'll test error conditions
        nonExistentFilePath = "/tmp/non-existent-" + System.currentTimeMillis() + ".xml";
        malformedSchemaPath = new File(classLoader.getResource("schemas/provider-test/malformed-schema.xml").getFile()).getAbsolutePath();
        emptySchemaPath = new File(classLoader.getResource("schemas/provider-test/empty-schema.xml").getFile()).getAbsolutePath();
    }

    @Test
    @DisplayName("Should create provider from single file")
    public void testCreateProviderFromFile() throws Exception {
        AdvancedSchemaProvider provider = new AdvancedSchemaProvider(baseSchemaPath);
        
        assertNotNull(provider, "Provider should be created");
        assertEquals(baseSchemaPath, provider.getOriginalPath(), "Original path should match");
        
        // Verify schema is loaded
        List<CsdlSchema> schemas = provider.getSchemas();
        assertFalse(schemas.isEmpty(), "Should have at least one schema");
        
        // Check that we can find the expected schema
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
        
        OperationResult result = provider.validateSchemaNew(extendedSchemaPath);
        
        assertNotNull(result, "Validation result should not be null");
        assertTrue(result.isSuccessful(), "Validation should succeed for compatible schema");
        assertFalse(result.hasErrors(), "Should have no errors");
        
        // Check that result has validation passed type
        assertTrue(result.hasResultType(ResultType.VALIDATION_PASSED), 
                  "Should have VALIDATION_PASSED result type");
        
        // Check messages
        assertFalse(result.getItems().isEmpty(), "Should have validation result items");
    }

    @Test
    @DisplayName("Should validate directory successfully")
    public void testValidateDirectory() throws Exception {
        AdvancedSchemaProvider provider = new AdvancedSchemaProvider(baseSchemaPath);
        
        OperationResult result = provider.validateSchemaDirectory(multiDirPath);
        
        assertNotNull(result, "Validation result should not be null");
        assertTrue(result.isSuccessful(), "Directory validation should succeed");
        assertFalse(result.getItems().isEmpty(), "Should have validation result items");
    }

    @Test
    @DisplayName("Should detect file not found error")
    public void testValidateNonExistentFile() throws Exception {
        AdvancedSchemaProvider provider = new AdvancedSchemaProvider(baseSchemaPath);
        
        OperationResult result = provider.validateSchema(nonExistentFilePath);
        
        assertNotNull(result, "Validation result should not be null");
        assertFalse(result.isSuccessful(), "Validation should fail for non-existent file");
        assertTrue(result.hasErrors(), "Should have errors");
        
        // Check for specific error type
        assertTrue(result.hasResultType(ResultType.FILE_NOT_FOUND), 
                  "Should have FILE_NOT_FOUND error type");
    }

    @Test
    @DisplayName("Should detect malformed schema")
    public void testValidateMalformedSchema() throws Exception {
        AdvancedSchemaProvider provider = new AdvancedSchemaProvider(baseSchemaPath);
        
        OperationResult result = provider.validateSchema(malformedSchemaPath);
        
        assertNotNull(result, "Validation result should not be null");
        assertFalse(result.isSuccessful(), "Validation should fail for malformed schema");
        assertTrue(result.hasErrors(), "Should have errors");
        
        // Check for specific error type
        assertTrue(result.hasResultType(ResultType.SCHEMA_MALFORMED) || 
                  result.hasResultType(ResultType.SCHEMA_INVALID), 
                  "Should have SCHEMA_MALFORMED or SCHEMA_INVALID error type");
    }

    @Test
    @DisplayName("Should detect empty schema")
    public void testValidateEmptySchema() throws Exception {
        AdvancedSchemaProvider provider = new AdvancedSchemaProvider(baseSchemaPath);
        
        OperationResult result = provider.validateSchema(emptySchemaPath);
        
        assertNotNull(result, "Validation result should not be null");
        assertFalse(result.isSuccessful(), "Validation should fail for empty schema");
        assertTrue(result.hasErrors(), "Should have errors");
        
        // Check for specific error type
        assertTrue(result.hasResultType(ResultType.SCHEMA_EMPTY), 
                  "Should have SCHEMA_EMPTY error type");
    }

    @Test
    @DisplayName("Should detect schema conflicts during validation")
    public void testValidateConflictingSchema() throws Exception {
        AdvancedSchemaProvider provider = new AdvancedSchemaProvider(baseSchemaPath);
        
        OperationResult result = provider.validateSchema(conflictingSchemaPath);
        
        assertNotNull(result, "Validation result should not be null");
        
        // Conflicting schema should either fail validation or succeed with warnings
        if (!result.isSuccessful()) {
            assertTrue(result.hasErrors(), "Should have errors for conflicting schema");
            assertTrue(result.hasResultType(ResultType.TYPE_MISMATCH) || 
                      result.hasResultType(ResultType.DUPLICATE_ELEMENT) ||
                      result.hasResultType(ResultType.MERGE_CONFLICT), 
                      "Should have conflict-related error type");
        } else {
            // If validation succeeds, there should be warnings about conflicts
            assertTrue(result.hasWarnings(), "Should have warnings about conflicts");
            assertTrue(result.hasResultType(ResultType.TYPE_OVERRIDDEN) ||
                      result.hasResultType(ResultType.COMPATIBILITY_WARNING), 
                      "Should have warning about type conflicts");
        }
    }

    @Test
    @DisplayName("Should merge compatible schemas successfully")
    public void testMergeCompatibleSchemas() throws Exception {
        AdvancedSchemaProvider provider = new AdvancedSchemaProvider(baseSchemaPath);
        
        OperationResult mergeResult = provider.mergeSchemaFile(extendedSchemaPath);
        
        assertNotNull(mergeResult, "Merge result should not be null");
        assertTrue(mergeResult.isSuccessful(), "Merge should succeed for compatible schemas");
        
        // Check for merge completed result type
        assertTrue(mergeResult.hasResultType(ResultType.MERGE_COMPLETED), 
                  "Should have MERGE_COMPLETED result type");
        
        // Verify merged provider has schemas from both sources
        List<CsdlSchema> mergedSchemas = provider.getSchemas();
        assertTrue(mergedSchemas.size() >= 2, "Should have schemas from both sources");
    }

    @Test
    @DisplayName("Should detect merge conflicts")
    public void testMergeConflictingSchemas() throws Exception {
        AdvancedSchemaProvider provider = new AdvancedSchemaProvider(baseSchemaPath);
        
        OperationResult mergeResult = provider.mergeSchemaFile(conflictingSchemaPath);
        
        assertNotNull(mergeResult, "Merge result should not be null");
        
        // Conflicting schemas should either fail merge or succeed with warnings
        if (!mergeResult.isSuccessful()) {
            assertTrue(mergeResult.hasErrors(), "Should have errors for conflicting schemas");
            assertTrue(mergeResult.hasResultType(ResultType.MERGE_CONFLICT) ||
                      mergeResult.hasResultType(ResultType.SCHEMA_INCOMPATIBLE) ||
                      mergeResult.hasResultType(ResultType.TYPE_MISMATCH), 
                      "Should have merge conflict error type");
        } else {
            assertTrue(mergeResult.hasWarnings(), "Should have warnings about conflicts");
            assertTrue(mergeResult.hasResultType(ResultType.TYPE_OVERRIDDEN), 
                      "Should have type override warning");
        }
    }

    @Test
    @DisplayName("Should handle merge with validation failure")
    public void testMergeWithValidationFailure() throws Exception {
        AdvancedSchemaProvider provider = new AdvancedSchemaProvider(baseSchemaPath);
        
        OperationResult mergeResult = provider.mergeSchemas(malformedSchemaPath);
        
        assertNotNull(mergeResult, "Merge result should not be null");
        assertFalse(mergeResult.isSuccessful(), "Merge should fail for malformed schema");
        assertTrue(mergeResult.hasErrors(), "Should have errors");
        
        // Check for merge validation failed or schema malformed
        assertTrue(mergeResult.hasResultType(ResultType.MERGE_VALIDATION_FAILED) ||
                  mergeResult.hasResultType(ResultType.SCHEMA_MALFORMED) ||
                  mergeResult.hasResultType(ResultType.SCHEMA_INVALID), 
                  "Should have validation or schema error type");
    }

    @Test
    @DisplayName("Should handle circular references")
    public void testCircularReferenceDetection() throws Exception {
        // This test requires special schema files with circular references
        // For now, we'll test the error type detection capability
        AdvancedSchemaProvider provider = new AdvancedSchemaProvider(baseSchemaPath);
        
        // Create a result manually to test the error type checking
        OperationResult result = new OperationResult();
        result.addError(ResultType.CIRCULAR_REFERENCE, "Circular reference detected between Schema A and Schema B");
        
        assertTrue(result.hasResultType(ResultType.CIRCULAR_REFERENCE), 
                  "Should detect circular reference error type");
        assertFalse(result.isSuccessful(), "Should not be successful with circular reference");
    }

    @Test
    @DisplayName("Should handle dependency resolution failure")
    public void testDependencyResolutionFailure() throws Exception {
        AdvancedSchemaProvider provider = new AdvancedSchemaProvider(baseSchemaPath);
        
        // Create a result to test dependency resolution failure
        OperationResult result = new OperationResult();
        result.addError(ResultType.DEPENDENCY_NOT_FOUND, "Required dependency 'External.Schema' not found");
        result.addError(ResultType.REFERENCE_NOT_FOUND, "Reference to 'External.Type' could not be resolved");
        
        assertTrue(result.hasResultType(ResultType.DEPENDENCY_NOT_FOUND), 
                  "Should detect dependency not found error");
        assertTrue(result.hasResultType(ResultType.REFERENCE_NOT_FOUND), 
                  "Should detect reference not found error");
        assertFalse(result.isSuccessful(), "Should not be successful with dependency failures");
        assertEquals(2, result.getErrors().size(), "Should have two error items");
    }

    @Test
    @DisplayName("Should provide operation summary")
    public void testOperationSummary() throws Exception {
        AdvancedSchemaProvider provider = new AdvancedSchemaProvider(baseSchemaPath);
        
        OperationResult result = provider.validateSchema(extendedSchemaPath);
        
        String summary = result.getSummary();
        assertNotNull(summary, "Summary should not be null");
        assertFalse(summary.trim().isEmpty(), "Summary should not be empty");
        assertTrue(summary.contains("VALIDATION"), "Summary should mention operation type");
    }

    @Test
    @DisplayName("Should support legacy OperationResult wrapper")
    public void testLegacyOperationResultWrapper() throws Exception {
        // Test backward compatibility with deprecated OperationResult class
        AdvancedSchemaProvider provider = new AdvancedSchemaProvider(baseSchemaPath);
        
        OperationResult legacyResult = provider.validateSchemaLegacy(extendedSchemaPath);
        
        assertNotNull(legacyResult, "Legacy result should not be null");
        assertTrue(legacyResult.isSuccessful(), "Legacy validation should succeed");
        
        // Test legacy methods
        List<String> errorMessages = legacyResult.getErrorMessages();
        List<String> warningMessages = legacyResult.getWarningMessages();
        List<String> infoMessages = legacyResult.getMessages();
        
        assertNotNull(errorMessages, "Error messages list should not be null");
        assertNotNull(warningMessages, "Warning messages list should not be null");
        assertNotNull(infoMessages, "Info messages list should not be null");
    }

    @Test
    @DisplayName("Should support legacy OperationResult wrapper for merge")
    public void testLegacyMergeOperationResultWrapper() throws Exception {
        // Test backward compatibility with deprecated OperationResult class
        AdvancedSchemaProvider provider = new AdvancedSchemaProvider(baseSchemaPath);
        
        OperationResult legacyResult = provider.mergeSchemasLegacy(extendedSchemaPath);
        
        assertNotNull(legacyResult, "Legacy merge result should not be null");
        assertTrue(legacyResult.isSuccessful(), "Legacy merge should succeed");
        
        // Test legacy methods
        List<String> errorMessages = legacyResult.getErrorMessages();
        List<String> warningMessages = legacyResult.getWarningMessages();
        List<String> infoMessages = legacyResult.getMessages();
        
        assertNotNull(errorMessages, "Error messages list should not be null");
        assertNotNull(warningMessages, "Warning messages list should not be null");
        assertNotNull(infoMessages, "Info messages list should not be null");
    }

    @Test
    @DisplayName("Should validate with multiple error types")
    public void testMultipleResultTypes() throws Exception {
        // Test that we can have multiple different error types in one result
        OperationResult result = new OperationResult();
        
        // Add various error types
        result.addError(ResultType.FILE_NOT_FOUND, "Schema file not found");
        result.addError(ResultType.SCHEMA_MALFORMED, "Schema XML is malformed");
        result.addError(ResultType.MISSING_REQUIRED_ELEMENT, "Required element 'Name' is missing");
        result.addWarning(ResultType.DEPRECATED_FEATURE, "Using deprecated feature");
        result.addInfo("Processing completed with issues");
        
        // Verify all error types are detected
        assertTrue(result.hasResultType(ResultType.FILE_NOT_FOUND), "Should detect file not found");
        assertTrue(result.hasResultType(ResultType.SCHEMA_MALFORMED), "Should detect malformed schema");
        assertTrue(result.hasResultType(ResultType.MISSING_REQUIRED_ELEMENT), "Should detect missing element");
        assertTrue(result.hasResultType(ResultType.DEPRECATED_FEATURE), "Should detect deprecated feature");
        
        // Verify counts
        assertEquals(3, result.getErrors().size(), "Should have 3 errors");
        assertEquals(1, result.getWarnings().size(), "Should have 1 warning");
        assertEquals(1, result.getInfos().size(), "Should have 1 info item");
        
        assertFalse(result.isSuccessful(), "Should not be successful with errors");
        assertTrue(result.hasErrors(), "Should have errors");
        assertTrue(result.hasWarnings(), "Should have warnings");
    }

    @Test
    @DisplayName("Should handle constraint violation scenarios")
    public void testConstraintViolationScenarios() throws Exception {
        // Test various constraint violation scenarios
        OperationResult result = new OperationResult();
        
        // Add constraint violation errors
        result.addError(ResultType.CONSTRAINT_VIOLATION, "Property 'Age' violates minimum value constraint (must be >= 0)");
        result.addError(ResultType.TYPE_MISMATCH, "Property 'Name' expects String but got Integer");
        result.addError(ResultType.INVALID_ATTRIBUTE, "Invalid attribute 'MaxLenght' (should be 'MaxLength')");
        
        assertTrue(result.hasResultType(ResultType.CONSTRAINT_VIOLATION), "Should detect constraint violation");
        assertTrue(result.hasResultType(ResultType.TYPE_MISMATCH), "Should detect type mismatch");
        assertTrue(result.hasResultType(ResultType.INVALID_ATTRIBUTE), "Should detect invalid attribute");
        
        assertFalse(result.isSuccessful(), "Should not be successful with constraint violations");
    }

    @Test
    @DisplayName("Should provide detailed error context")
    public void testDetailedErrorContext() throws Exception {
        OperationResult result = new OperationResult();
        
        // Add errors with context information
        result.addError(ResultType.REFERENCE_NOT_FOUND, 
            "Reference to type 'External.Customer' could not be resolved", "Entity Type: Order, Property: Customer, Line: 45");
        
        result.addWarning(ResultType.PERFORMANCE_WARNING, 
            "Complex inheritance hierarchy may impact performance", "Entity Type: BaseEntity, Inheritance Depth: 5");
        
        // Verify context information is preserved
        List<OperationResult.ResultItem> errors = result.getErrors();
        assertEquals(1, errors.size(), "Should have one error");
        
        OperationResult.ResultItem firstError = errors.get(0);
        assertEquals(ResultType.REFERENCE_NOT_FOUND, firstError.getType(), "Should have correct error type");
        assertTrue(firstError.getContext().contains("Line: 45"), "Should have line number in context");
        
        List<OperationResult.ResultItem> warnings = result.getWarnings();
        assertEquals(1, warnings.size(), "Should have one warning");
        
        OperationResult.ResultItem firstWarning = warnings.get(0);
        assertEquals(ResultType.PERFORMANCE_WARNING, firstWarning.getType(), "Should have correct warning type");
        assertTrue(firstWarning.getContext().contains("Inheritance Depth: 5"), "Should have depth in context");
    }

    @Test
    @DisplayName("Should handle network and dependency failures")
    public void testNetworkAndDependencyFailures() {
        OperationResult result = new OperationResult(OperationType.MERGE);
        
        // Test network related failures
        result.addError(ResultType.NETWORK_ERROR, "Failed to download remote schema: Connection timeout");
        result.addError(ResultType.DEPENDENCY_NOT_FOUND, "Required dependency 'com.example.base:schema' not found in repository");
        result.addError(ResultType.DEPENDENCY_RESOLUTION_FAILED, "Conflicting dependency versions detected");
        
        assertFalse(result.isSuccessful(), "Should fail with dependency errors");
        assertEquals(3, result.getErrors().size(), "Should have 3 errors");
        
        assertTrue(result.hasResultType(ResultType.NETWORK_ERROR), "Should have network error");
        assertTrue(result.hasResultType(ResultType.DEPENDENCY_NOT_FOUND), "Should have dependency not found error");
        assertTrue(result.hasResultType(ResultType.DEPENDENCY_RESOLUTION_FAILED), "Should have dependency resolution error");
    }

    @Test
    @DisplayName("Should handle configuration and parameter errors")
    public void testConfigurationAndParameterErrors() {
        OperationResult result = new OperationResult(OperationType.VALIDATION);
        
        // Test configuration related failures
        result.addError(ResultType.CONFIGURATION_ERROR, "Invalid parser configuration: conflicting settings");
        result.addError(ResultType.INVALID_PARAMETER, "Schema path parameter cannot be null or empty");
        result.addError(ResultType.PERMISSION_DENIED, "Access denied to schema directory: /restricted/schemas/");
        
        assertFalse(result.isSuccessful(), "Should fail with configuration errors");
        assertEquals(3, result.getErrors().size(), "Should have 3 errors");
        
        assertTrue(result.hasResultType(ResultType.CONFIGURATION_ERROR), "Should have configuration error");
        assertTrue(result.hasResultType(ResultType.INVALID_PARAMETER), "Should have invalid parameter error");
        assertTrue(result.hasResultType(ResultType.PERMISSION_DENIED), "Should have permission denied error");
    }

    @Test
    @DisplayName("Should handle large schema and performance warnings")
    public void testLargeSchemaAndPerformanceWarnings() {
        OperationResult result = new OperationResult(OperationType.VALIDATION);
        
        // Add success but with performance warnings
        result.addInfo(ResultType.VALIDATION_PASSED, "Schema validation completed successfully");
        result.addWarning(ResultType.SCHEMA_WARNING, "Schema size exceeds recommended limit: 50MB");
        result.addWarning(ResultType.PERFORMANCE_WARNING, "Deep inheritance hierarchy detected (depth > 10)");
        result.addWarning(ResultType.COMPATIBILITY_WARNING, "Using deprecated OData v3 features");
        
        assertTrue(result.isSuccessful(), "Should succeed with warnings");
        assertTrue(result.hasWarnings(), "Should have warnings");
        assertEquals(3, result.getWarnings().size(), "Should have 3 warnings");
        
        assertTrue(result.hasResultType(ResultType.VALIDATION_PASSED), "Should have validation passed");
        assertTrue(result.hasResultType(ResultType.SCHEMA_WARNING), "Should have schema warning");
        assertTrue(result.hasResultType(ResultType.PERFORMANCE_WARNING), "Should have performance warning");
        assertTrue(result.hasResultType(ResultType.COMPATIBILITY_WARNING), "Should have compatibility warning");
    }

    @Test
    @DisplayName("Should handle complex merge conflict scenarios")
    public void testComplexMergeConflictScenarios() {
        OperationResult result = new OperationResult(OperationType.MERGE);
        
        // Multiple types of merge conflicts
        result.addError(ResultType.NAMESPACE_CONFLICT, "Namespace 'Test.Models' exists in both schemas with different versions");
        result.addError(ResultType.VERSION_MISMATCH, "Schema version mismatch: base=1.0, target=2.0");
        result.addError(ResultType.TYPE_MISMATCH, "Entity type 'Customer' has conflicting property types");
        result.addWarning(ResultType.TYPE_OVERRIDDEN, "Entity type 'Product' was overridden by target schema");
        
        assertFalse(result.isSuccessful(), "Should fail with merge conflicts");
        assertEquals(3, result.getErrors().size(), "Should have 3 errors");
        assertEquals(1, result.getWarnings().size(), "Should have 1 warning");
        
        assertTrue(result.hasResultType(ResultType.NAMESPACE_CONFLICT), "Should have namespace conflict");
        assertTrue(result.hasResultType(ResultType.VERSION_MISMATCH), "Should have version mismatch");
        assertTrue(result.hasResultType(ResultType.TYPE_MISMATCH), "Should have type mismatch");
        assertTrue(result.hasResultType(ResultType.TYPE_OVERRIDDEN), "Should have type override warning");
    }

    @Test
    @DisplayName("Should handle multiple validation error types")
    public void testMultipleValidationResultTypes() {
        OperationResult result = new OperationResult(OperationType.VALIDATION);
        
        // Various validation errors
        result.addError(ResultType.MISSING_REQUIRED_ELEMENT, "Required element 'Key' is missing in EntityType 'Customer'");
        result.addError(ResultType.INVALID_ELEMENT_TYPE, "Invalid element type 'Edm.UnknownType' in property 'CustomField'");
        result.addError(ResultType.INVALID_ATTRIBUTE, "Invalid attribute 'MaxLength' on navigation property");
        result.addError(ResultType.DUPLICATE_ELEMENT, "Duplicate entity type 'Order' found in namespace 'Test.Models'");
        result.addError(ResultType.CIRCULAR_REFERENCE, "Circular reference detected: A -> B -> C -> A");
        result.addError(ResultType.CONSTRAINT_VIOLATION, "Referential constraint violation: foreign key mismatch");
        
        assertFalse(result.isSuccessful(), "Should fail with validation errors");
        assertEquals(6, result.getErrors().size(), "Should have 6 different error types");
        
        assertTrue(result.hasResultType(ResultType.MISSING_REQUIRED_ELEMENT), "Should detect missing required element");
        assertTrue(result.hasResultType(ResultType.INVALID_ELEMENT_TYPE), "Should detect invalid element type");
        assertTrue(result.hasResultType(ResultType.INVALID_ATTRIBUTE), "Should detect invalid attribute");
        assertTrue(result.hasResultType(ResultType.DUPLICATE_ELEMENT), "Should detect duplicate element");
        assertTrue(result.hasResultType(ResultType.CIRCULAR_REFERENCE), "Should detect circular reference");
        assertTrue(result.hasResultType(ResultType.CONSTRAINT_VIOLATION), "Should detect constraint violation");
    }

    @Test
    @DisplayName("Should test result type categorization")
    public void testResultTypeCategorization() {
        // Test error types
        assertTrue(ResultType.FILE_NOT_FOUND.isError(), "FILE_NOT_FOUND should be error");
        assertTrue(ResultType.VALIDATION_FAILED.isError(), "VALIDATION_FAILED should be error");
        assertTrue(ResultType.MERGE_CONFLICT.isError(), "MERGE_CONFLICT should be error");
        
        // Test warning types
        assertTrue(ResultType.SCHEMA_WARNING.isWarning(), "SCHEMA_WARNING should be warning");
        assertTrue(ResultType.PERFORMANCE_WARNING.isWarning(), "PERFORMANCE_WARNING should be warning");
        assertTrue(ResultType.DEPRECATED_FEATURE.isWarning(), "DEPRECATED_FEATURE should be warning");
        
        // Test info types
        assertTrue(ResultType.SUCCESS.isInfo(), "SUCCESS should be info");
        assertTrue(ResultType.VALIDATION_PASSED.isInfo(), "VALIDATION_PASSED should be info");
        assertTrue(ResultType.MERGE_COMPLETED.isInfo(), "MERGE_COMPLETED should be info");
        
        // Test mutual exclusivity
        assertFalse(ResultType.FILE_NOT_FOUND.isWarning(), "Error should not be warning");
        assertFalse(ResultType.FILE_NOT_FOUND.isInfo(), "Error should not be info");
        assertFalse(ResultType.SCHEMA_WARNING.isError(), "Warning should not be error");
        assertFalse(ResultType.SCHEMA_WARNING.isInfo(), "Warning should not be info");
        assertFalse(ResultType.SUCCESS.isError(), "Info should not be error");
        assertFalse(ResultType.SUCCESS.isWarning(), "Info should not be warning");
    }
}
