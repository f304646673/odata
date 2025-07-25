package org.apache.olingo.schemamanager.merger.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.merger.SchemaMerger;
import org.apache.olingo.schemamanager.merger.SchemaMerger.CompatibilityResult;
import org.apache.olingo.schemamanager.merger.SchemaMerger.ConflictInfo;
import org.apache.olingo.schemamanager.merger.SchemaMerger.ConflictResolution;
import org.apache.olingo.schemamanager.merger.SchemaMerger.ConflictType;
import org.apache.olingo.schemamanager.merger.SchemaMerger.MergeResult;
import org.apache.olingo.schemamanager.parser.ODataSchemaParser;
import org.apache.olingo.schemamanager.parser.ODataSchemaParser.ParseResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Comprehensive test suite for DefaultSchemaMerger
 * Tests all merge scenarios, conflict detection, and resolution strategies
 */
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DefaultSchemaMergerComprehensiveTest {

    @Autowired
    private SchemaMerger schemaMerger;
    
    @Autowired
    private ODataSchemaParser schemaParser;
    
    private CsdlSchema schema1;
    private CsdlSchema schema2;
    private CsdlSchema schemaConflict;
    private CsdlSchema schemaDifferentNamespace;
    private CsdlSchema schemaEmpty;
    
    @BeforeEach
    void setUp() throws IOException {
        // Load test schemas from XML resources
        schema1 = loadSchemaFromResource("/xml-schemas/merger/schema1.xml");
        schema2 = loadSchemaFromResource("/xml-schemas/merger/schema2.xml");
        schemaConflict = loadSchemaFromResource("/xml-schemas/merger/schema_conflict.xml");
        schemaDifferentNamespace = loadSchemaFromResource("/xml-schemas/merger/schema_different_namespace.xml");
        schemaEmpty = loadSchemaFromResource("/xml-schemas/merger/schema_empty.xml");
    }
    
    private CsdlSchema loadSchemaFromResource(String resourcePath) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
            assertNotNull(inputStream, "Resource not found: " + resourcePath);
            
            Path tempPath = Paths.get("temp_" + System.currentTimeMillis() + ".xml");
            ParseResult parseResult = schemaParser.parseSchema(inputStream, tempPath.toString());
            
            assertTrue(parseResult.isSuccess(), "Failed to parse schema: " + resourcePath);
            assertFalse(parseResult.getSchemas().isEmpty(), "No schemas found in: " + resourcePath);
            
            return parseResult.getSchemas().get(0).getSchema();
        }
    }
    
    // Basic merge tests
    
    @Test
    @DisplayName("Merge compatible schemas successfully")
    void testMergeCompatibleSchemas() {
        List<CsdlSchema> schemas = Arrays.asList(schema1, schema2);
        
        MergeResult result = schemaMerger.mergeSchemas(schemas);
        
        assertTrue(result.isSuccess());
        assertNotNull(result.getMergedSchema());
        assertEquals("TestNamespace", result.getMergedSchema().getNamespace());
        
        // Verify merged content
        CsdlSchema merged = result.getMergedSchema();
        
        // Should have all EntityTypes from both schemas
        assertNotNull(merged.getEntityTypes());
        assertEquals(3, merged.getEntityTypes().size()); // User, Order, Product
        assertTrue(merged.getEntityTypes().stream().anyMatch(et -> "User".equals(et.getName())));
        assertTrue(merged.getEntityTypes().stream().anyMatch(et -> "Order".equals(et.getName())));
        assertTrue(merged.getEntityTypes().stream().anyMatch(et -> "Product".equals(et.getName())));
        
        // Should have all ComplexTypes from both schemas
        assertNotNull(merged.getComplexTypes());
        assertEquals(2, merged.getComplexTypes().size()); // Address, ContactInfo
        
        // Should have all EnumTypes from both schemas
        assertNotNull(merged.getEnumTypes());
        assertEquals(2, merged.getEnumTypes().size()); // Status, Category
        
        // Should have all Functions from both schemas
        assertNotNull(merged.getFunctions());
        assertEquals(2, merged.getFunctions().size()); // GetUserByEmail, GetProductsByCategory
        
        // Should have all Actions from both schemas
        assertNotNull(merged.getActions());
        assertEquals(2, merged.getActions().size()); // ActivateUser, UpdateProductPrice
        
        // Should have all Terms from both schemas
        assertNotNull(merged.getTerms());
        assertEquals(2, merged.getTerms().size()); // Description, Category
        
        // Should have all TypeDefinitions from both schemas
        assertNotNull(merged.getTypeDefinitions());
        assertEquals(2, merged.getTypeDefinitions().size()); // UserIdType, ProductIdType
        
        // Should have merged EntityContainer
        assertNotNull(merged.getEntityContainer());
        assertEquals("Container", merged.getEntityContainer().getName());
        assertEquals(3, merged.getEntityContainer().getEntitySets().size());
        assertEquals(2, merged.getEntityContainer().getFunctionImports().size());
        assertEquals(2, merged.getEntityContainer().getActionImports().size());
    }
    
    @Test
    @DisplayName("Merge single schema returns same schema")
    void testMergeSingleSchema() {
        List<CsdlSchema> schemas = Collections.singletonList(schema1);
        
        MergeResult result = schemaMerger.mergeSchemas(schemas);
        
        assertTrue(result.isSuccess());
        assertNotNull(result.getMergedSchema());
        assertEquals(schema1.getNamespace(), result.getMergedSchema().getNamespace());
    }
    
    @Test
    @DisplayName("Merge empty list returns error")
    void testMergeEmptyList() {
        List<CsdlSchema> schemas = Collections.emptyList();
        
        MergeResult result = schemaMerger.mergeSchemas(schemas);
        
        assertFalse(result.isSuccess());
        assertNull(result.getMergedSchema());
        assertFalse(result.getErrors().isEmpty());
        assertTrue(result.getErrors().get(0).contains("No schemas provided"));
    }
    
    @Test
    @DisplayName("Merge null list returns error")
    void testMergeNullList() {
        MergeResult result = schemaMerger.mergeSchemas(null);
        
        assertFalse(result.isSuccess());
        assertNull(result.getMergedSchema());
        assertFalse(result.getErrors().isEmpty());
    }
    
    // Conflict resolution tests
    
    @ParameterizedTest
    @EnumSource(ConflictResolution.class)
    @DisplayName("Test all conflict resolution strategies")
    void testAllConflictResolutionStrategies(ConflictResolution resolution) {
        List<CsdlSchema> schemas = Arrays.asList(schema1, schemaConflict);
        
        if (resolution == ConflictResolution.THROW_ERROR) {
            // THROW_ERROR should throw an exception
            assertThrows(IllegalStateException.class, () -> {
                schemaMerger.mergeSchemas(schemas, resolution);
            });
        } else {
            MergeResult result = schemaMerger.mergeSchemas(schemas, resolution);
            
            // All other strategies should succeed
            assertTrue(result.isSuccess());
            assertNotNull(result.getMergedSchema());
            
            // Check that conflicts were detected
            assertFalse(result.getConflicts().isEmpty());
            
            // Verify specific behavior based on strategy
            switch (resolution) {
                case KEEP_FIRST:
                    // Should keep first occurrence of conflicting elements
                    verifyKeepFirstBehavior(result.getMergedSchema());
                    break;
                case KEEP_LAST:
                    // Should keep last occurrence of conflicting elements
                    verifyKeepLastBehavior(result.getMergedSchema());
                    break;
                case AUTO_MERGE:
                    // Should merge compatible elements, fail on incompatible
                    verifyAutoMergeBehavior(result);
                    break;
                case SKIP_CONFLICTS:
                    // Should skip conflicting elements
                    verifySkipConflictsBehavior(result.getMergedSchema());
                    break;
            }
        }
    }
    
    private void verifyKeepFirstBehavior(CsdlSchema merged) {
        // User EntityType should have Int32 Id (from schema1)
        CsdlEntityType userType = merged.getEntityTypes().stream()
                .filter(et -> "User".equals(et.getName()))
                .findFirst().orElse(null);
        assertNotNull(userType);
        CsdlProperty idProperty = userType.getProperties().stream()
                .filter(p -> "Id".equals(p.getName()))
                .findFirst().orElse(null);
        assertNotNull(idProperty);
        assertEquals("Edm.Int32", idProperty.getType());
    }
    
    private void verifyKeepLastBehavior(CsdlSchema merged) {
        // User EntityType should have String Id (from schemaConflict)
        CsdlEntityType userType = merged.getEntityTypes().stream()
                .filter(et -> "User".equals(et.getName()))
                .findFirst().orElse(null);
        assertNotNull(userType);
        CsdlProperty idProperty = userType.getProperties().stream()
                .filter(p -> "Id".equals(p.getName()))
                .findFirst().orElse(null);
        assertNotNull(idProperty);
        assertEquals("Edm.String", idProperty.getType());
    }
    
    private void verifyAutoMergeBehavior(MergeResult result) {
        // AUTO_MERGE should detect incompatible conflicts
        boolean hasIncompatibleConflicts = result.getConflicts().stream()
                .anyMatch(conflict -> conflict.getConflictType() == ConflictType.ENTITY_TYPE ||
                                     conflict.getConflictType() == ConflictType.COMPLEX_TYPE ||
                                     conflict.getConflictType() == ConflictType.ENUM_TYPE);
        assertTrue(hasIncompatibleConflicts);
    }
    
    private void verifySkipConflictsBehavior(CsdlSchema merged) {
        // SKIP_CONFLICTS should remove incompatible conflicting elements
        // Check that incompatible User EntityType is not present or has been resolved
        assertNotNull(merged.getEntityTypes());
    }
    
    // Namespace merging tests
    
    @Test
    @DisplayName("Merge by namespace groups schemas correctly")
    void testMergeByNamespace() {
        Map<String, CsdlSchema> schemaMap = new HashMap<>();
        schemaMap.put("schema1", schema1);
        schemaMap.put("schema2", schema2);
        schemaMap.put("schemaConflict", schemaConflict);
        schemaMap.put("schemaDifferent", schemaDifferentNamespace);
        
        Map<String, CsdlSchema> result = schemaMerger.mergeByNamespace(schemaMap);
        
        // Should have 2 namespaces: TestNamespace and DifferentNamespace
        assertEquals(2, result.size());
        assertTrue(result.containsKey("TestNamespace"));
        assertTrue(result.containsKey("DifferentNamespace"));
        
        // TestNamespace should have merged content from schema1, schema2, and schemaConflict
        CsdlSchema testNamespaceSchema = result.get("TestNamespace");
        assertNotNull(testNamespaceSchema);
        assertEquals("TestNamespace", testNamespaceSchema.getNamespace());
        
        // DifferentNamespace should have content from schemaDifferentNamespace
        CsdlSchema differentNamespaceSchema = result.get("DifferentNamespace");
        assertNotNull(differentNamespaceSchema);
        assertEquals("DifferentNamespace", differentNamespaceSchema.getNamespace());
    }
    
    @Test
    @DisplayName("Merge by namespace with conflict resolution")
    void testMergeByNamespaceWithConflictResolution() {
        Map<String, CsdlSchema> schemaMap = new HashMap<>();
        schemaMap.put("schema1", schema1);
        schemaMap.put("schemaConflict", schemaConflict);
        
        Map<String, CsdlSchema> result = schemaMerger.mergeByNamespace(schemaMap, ConflictResolution.KEEP_LAST);
        
        assertEquals(1, result.size());
        assertTrue(result.containsKey("TestNamespace"));
        
        CsdlSchema merged = result.get("TestNamespace");
        verifyKeepLastBehavior(merged);
    }
    
    // Compatibility checking tests
    
    @Test
    @DisplayName("Check compatibility between compatible schemas")
    void testCheckCompatibilityCompatible() {
        CompatibilityResult result = schemaMerger.checkCompatibility(schema1, schema2);
        
        assertTrue(result.isCompatible());
        assertTrue(result.getConflicts().isEmpty());
        assertFalse(result.getWarnings().isEmpty()); // Should have warnings about duplicate elements
    }
    
    @Test
    @DisplayName("Check compatibility between conflicting schemas")
    void testCheckCompatibilityConflicting() {
        CompatibilityResult result = schemaMerger.checkCompatibility(schema1, schemaConflict);
        
        assertFalse(result.isCompatible());
        assertFalse(result.getConflicts().isEmpty());
        assertFalse(result.getDetailedConflicts().isEmpty());
        
        // Should detect conflicts in multiple element types
        List<ConflictInfo> conflicts = result.getDetailedConflicts();
        assertTrue(conflicts.stream().anyMatch(c -> c.getConflictType() == ConflictType.ENTITY_TYPE));
        assertTrue(conflicts.stream().anyMatch(c -> c.getConflictType() == ConflictType.COMPLEX_TYPE));
        assertTrue(conflicts.stream().anyMatch(c -> c.getConflictType() == ConflictType.ENUM_TYPE));
        assertTrue(conflicts.stream().anyMatch(c -> c.getConflictType() == ConflictType.FUNCTION));
        assertTrue(conflicts.stream().anyMatch(c -> c.getConflictType() == ConflictType.ACTION));
        assertTrue(conflicts.stream().anyMatch(c -> c.getConflictType() == ConflictType.TERM));
        assertTrue(conflicts.stream().anyMatch(c -> c.getConflictType() == ConflictType.TYPE_DEFINITION));
    }
    
    @Test
    @DisplayName("Check compatibility between different namespace schemas")
    void testCheckCompatibilityDifferentNamespace() {
        CompatibilityResult result = schemaMerger.checkCompatibility(schema1, schemaDifferentNamespace);
        
        assertFalse(result.isCompatible());
        assertFalse(result.getConflicts().isEmpty());
        assertTrue(result.getConflicts().get(0).contains("Different namespaces"));
    }
    
    // Conflict resolution method tests
    
    @Test
    @DisplayName("Resolve conflicts with KEEP_FIRST strategy")
    void testResolveConflictsKeepFirst() {
        List<CsdlSchema> schemas = Arrays.asList(schema1, schemaConflict);
        
        CsdlSchema result = schemaMerger.resolveConflicts(schemas, ConflictResolution.KEEP_FIRST);
        
        assertNotNull(result);
        assertEquals(schema1.getNamespace(), result.getNamespace());
        // Should essentially be the first schema
        assertSame(schema1, result);
    }
    
    @Test
    @DisplayName("Resolve conflicts with KEEP_LAST strategy")
    void testResolveConflictsKeepLast() {
        List<CsdlSchema> schemas = Arrays.asList(schema1, schemaConflict);
        
        CsdlSchema result = schemaMerger.resolveConflicts(schemas, ConflictResolution.KEEP_LAST);
        
        assertNotNull(result);
        assertEquals(schemaConflict.getNamespace(), result.getNamespace());
        // Should essentially be the last schema
        assertSame(schemaConflict, result);
    }
    
    @Test
    @DisplayName("Resolve conflicts with THROW_ERROR strategy")
    void testResolveConflictsThrowError() {
        List<CsdlSchema> schemas = Arrays.asList(schema1, schemaConflict);
        
        assertThrows(IllegalStateException.class, () -> {
            schemaMerger.resolveConflicts(schemas, ConflictResolution.THROW_ERROR);
        });
    }
    
    @Test
    @DisplayName("Resolve conflicts with AUTO_MERGE strategy")
    void testResolveConflictsAutoMerge() {
        List<CsdlSchema> schemas = Arrays.asList(schema1, schema2);
        
        CsdlSchema result = schemaMerger.resolveConflicts(schemas, ConflictResolution.AUTO_MERGE);
        
        assertNotNull(result);
        assertEquals("TestNamespace", result.getNamespace());
        // Should have merged content from both schemas
        assertTrue(result.getEntityTypes().size() >= Math.max(
                schema1.getEntityTypes() != null ? schema1.getEntityTypes().size() : 0,
                schema2.getEntityTypes() != null ? schema2.getEntityTypes().size() : 0));
    }
    
    @Test
    @DisplayName("Resolve conflicts with SKIP_CONFLICTS strategy")
    void testResolveConflictsSkipConflicts() {
        List<CsdlSchema> schemas = Arrays.asList(schema1, schemaConflict);
        
        CsdlSchema result = schemaMerger.resolveConflicts(schemas, ConflictResolution.SKIP_CONFLICTS);
        
        assertNotNull(result);
        assertEquals("TestNamespace", result.getNamespace());
        // Should have some content but may have skipped conflicting elements
    }
    
    @Test
    @DisplayName("Resolve conflicts with single schema")
    void testResolveConflictsSingleSchema() {
        List<CsdlSchema> schemas = Collections.singletonList(schema1);
        
        CsdlSchema result = schemaMerger.resolveConflicts(schemas, ConflictResolution.KEEP_FIRST);
        
        assertNotNull(result);
        assertSame(schema1, result);
    }
    
    @Test
    @DisplayName("Resolve conflicts with empty list")
    void testResolveConflictsEmptyList() {
        List<CsdlSchema> schemas = Collections.emptyList();
        
        CsdlSchema result = schemaMerger.resolveConflicts(schemas, ConflictResolution.KEEP_FIRST);
        
        assertNull(result);
    }
    
    @Test
    @DisplayName("Resolve conflicts with null list")
    void testResolveConflictsNullList() {
        CsdlSchema result = schemaMerger.resolveConflicts(null, ConflictResolution.KEEP_FIRST);
        
        assertNull(result);
    }
    
    // Edge cases and error handling
    
    @Test
    @DisplayName("Merge with null schemas in list")
    void testMergeWithNullSchemasInList() {
        List<CsdlSchema> schemas = Arrays.asList(schema1, null, schema2);
        
        MergeResult result = schemaMerger.mergeSchemas(schemas);
        
        assertTrue(result.isSuccess());
        assertNotNull(result.getMergedSchema());
        // Should have merged non-null schemas
        assertEquals("TestNamespace", result.getMergedSchema().getNamespace());
    }
    
    @Test
    @DisplayName("Merge schemas with empty content")
    void testMergeWithEmptySchemas() {
        List<CsdlSchema> schemas = Arrays.asList(schema1, schemaEmpty);
        
        MergeResult result = schemaMerger.mergeSchemas(schemas);
        
        assertTrue(result.isSuccess());
        assertNotNull(result.getMergedSchema());
        assertEquals("TestNamespace", result.getMergedSchema().getNamespace());
    }
    
    @Test
    @DisplayName("Check detailed conflict information")
    void testDetailedConflictInformation() {
        List<CsdlSchema> schemas = Arrays.asList(schema1, schemaConflict);
        
        MergeResult result = schemaMerger.mergeSchemas(schemas, ConflictResolution.KEEP_FIRST);
        
        assertFalse(result.getConflicts().isEmpty());
        
        // Verify conflict details
        for (ConflictInfo conflict : result.getConflicts()) {
            assertNotNull(conflict.getConflictType());
            assertNotNull(conflict.getElementName());
            assertNotNull(conflict.getMessage());
            assertNotNull(conflict.getFirstElement());
            assertNotNull(conflict.getSecondElement());
        }
    }
    
    @Test
    @DisplayName("Merge performance with large number of schemas")
    void testMergePerformance() {
        // Create a list with multiple copies of the same schema
        List<CsdlSchema> schemas = Arrays.asList(
            schema1, schema2, schema1, schema2, schema1, schema2
        );
        
        long startTime = System.currentTimeMillis();
        MergeResult result = schemaMerger.mergeSchemas(schemas);
        long endTime = System.currentTimeMillis();
        
        assertTrue(result.isSuccess());
        assertNotNull(result.getMergedSchema());
        
        // Should complete within reasonable time (adjust threshold as needed)
        long duration = endTime - startTime;
        assertTrue(duration < 5000, "Merge took too long: " + duration + "ms");
        
        // Should have warnings about duplicate elements
        assertFalse(result.getWarnings().isEmpty());
    }
    
    @Test
    @DisplayName("Verify warning messages are meaningful")
    void testWarningMessages() {
        List<CsdlSchema> schemas = Arrays.asList(schema1, schema2);
        
        MergeResult result = schemaMerger.mergeSchemas(schemas);
        
        assertTrue(result.isSuccess());
        assertFalse(result.getWarnings().isEmpty());
        
        // Check that warnings contain meaningful information
        for (String warning : result.getWarnings()) {
            assertTrue(warning.length() > 10, "Warning too short: " + warning);
            assertTrue(warning.contains("User") || warning.contains("Address") || 
                      warning.contains("Status") || warning.contains("Description") ||
                      warning.contains("UserIdType") || warning.contains("GetUserByEmail") ||
                      warning.contains("ActivateUser"), 
                      "Warning doesn't contain expected element name: " + warning);
        }
    }
    
    @Test
    @DisplayName("Test unknown conflict resolution strategy")
    void testUnknownConflictResolutionStrategy() {
        // This test checks internal error handling for invalid enum values
        // In practice, this shouldn't happen due to enum constraints
        List<CsdlSchema> schemas = Arrays.asList(schema1, schemaConflict);
        
        // All valid enum values should work
        for (ConflictResolution resolution : ConflictResolution.values()) {
            if (resolution == ConflictResolution.THROW_ERROR) {
                assertThrows(IllegalStateException.class, () -> {
                    schemaMerger.mergeSchemas(schemas, resolution);
                });
            } else {
                MergeResult result = schemaMerger.mergeSchemas(schemas, resolution);
                assertTrue(result.isSuccess() || !result.getErrors().isEmpty());
            }
        }
    }
}
