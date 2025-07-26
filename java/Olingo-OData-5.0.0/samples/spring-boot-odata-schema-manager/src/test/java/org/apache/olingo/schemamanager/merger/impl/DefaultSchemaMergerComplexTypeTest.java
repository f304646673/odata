package org.apache.olingo.schemamanager.merger.impl;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.merger.SchemaMerger;
import org.apache.olingo.schemamanager.merger.SchemaMerger.ConflictResolution;
import org.apache.olingo.schemamanager.merger.SchemaMerger.MergeResult;
import org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils;

/**
 * Test class for ComplexType merging scenarios in DefaultSchemaMerger
 */
@DisplayName("ComplexType Merger Tests")
public class DefaultSchemaMergerComplexTypeTest {
    
    private SchemaMerger schemaMerger;
    
    @BeforeEach
    void setUp() {
        schemaMerger = new DefaultSchemaMerger();
    }
    
    @Test
    @DisplayName("ComplexType KEEP_FIRST - Should keep first definition")
    void testComplexType_KeepFirst() throws Exception {
        // Load test schemas
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("merger/complex-type/keep-first/schema1.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("merger/complex-type/keep-first/schema2.xml");
        
        // Merge with KEEP_FIRST strategy
        MergeResult result = schemaMerger.mergeSchemas(Arrays.asList(schema1, schema2), ConflictResolution.KEEP_FIRST);
        
        // Verify success
        assertTrue(result.isSuccess(), "Merge should succeed with KEEP_FIRST strategy");
        assertEquals(1, result.getMergedSchemas().size(), "Should have one merged schema");
        
        // Verify warnings and conflicts
        assertFalse(result.getWarnings().isEmpty(), "Should have warnings about conflicts");
        assertFalse(result.getConflicts().isEmpty(), "Should have conflict information");
        assertTrue(result.getErrors().isEmpty(), "Should have no errors");
        
        // Verify the first definition is kept
        CsdlSchema mergedSchema = result.getMergedSchemas().get(0);
        assertNotNull(mergedSchema.getComplexTypes(), "Merged schema should have complex types");
        assertEquals(1, mergedSchema.getComplexTypes().size(), "Should have one complex type");
        
        // Check that first schema's properties are preserved
        assertEquals("Address", mergedSchema.getComplexTypes().get(0).getName());
        assertEquals(3, mergedSchema.getComplexTypes().get(0).getProperties().size(), "Should have 3 properties from first schema");
    }
    
    @Test
    @DisplayName("ComplexType KEEP_LAST - Should keep last definition")
    void testComplexType_KeepLast() throws Exception {
        // Load test schemas
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("merger/complex-type/keep-first/schema1.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("merger/complex-type/keep-first/schema2.xml");
        
        // Merge with KEEP_LAST strategy
        MergeResult result = schemaMerger.mergeSchemas(Arrays.asList(schema1, schema2), ConflictResolution.KEEP_LAST);
        
        // Verify success
        assertTrue(result.isSuccess(), "Merge should succeed with KEEP_LAST strategy");
        assertEquals(1, result.getMergedSchemas().size(), "Should have one merged schema");
        
        // Verify warnings and conflicts
        assertFalse(result.getWarnings().isEmpty(), "Should have warnings about conflicts");
        assertFalse(result.getConflicts().isEmpty(), "Should have conflict information");
        assertTrue(result.getErrors().isEmpty(), "Should have no errors");
        
        // Verify the last definition is kept
        CsdlSchema mergedSchema = result.getMergedSchemas().get(0);
        assertNotNull(mergedSchema.getComplexTypes(), "Merged schema should have complex types");
        assertEquals(1, mergedSchema.getComplexTypes().size(), "Should have one complex type");
        
        // Check that last schema's properties are preserved
        assertEquals("Address", mergedSchema.getComplexTypes().get(0).getName());
        assertEquals(2, mergedSchema.getComplexTypes().get(0).getProperties().size(), "Should have 2 properties from last schema");
    }
    
    @Test
    @DisplayName("ComplexType THROW_ERROR - Should throw error on conflict")
    void testComplexType_ThrowError() throws Exception {
        // Load test schemas
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("merger/complex-type/throw-error/schema1.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("merger/complex-type/throw-error/schema2.xml");
        
        // Merge with THROW_ERROR strategy
        MergeResult result = schemaMerger.mergeSchemas(Arrays.asList(schema1, schema2), ConflictResolution.THROW_ERROR);
        
        // Verify failure
        assertFalse(result.isSuccess(), "Merge should fail with THROW_ERROR strategy");
        assertFalse(result.getErrors().isEmpty(), "Should have errors");
        assertFalse(result.getConflicts().isEmpty(), "Should have conflict information");
        
        // Verify no merged schemas
        assertTrue(result.getMergedSchemas().isEmpty(), "Should have no merged schemas on error");
    }
    
    @Test
    @DisplayName("ComplexType SKIP_CONFLICTS - Should skip conflicting definitions")
    void testComplexType_SkipConflicts() throws Exception {
        // Load test schemas
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("merger/complex-type/keep-first/schema1.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("merger/complex-type/keep-first/schema2.xml");
        
        // Merge with SKIP_CONFLICTS strategy
        MergeResult result = schemaMerger.mergeSchemas(Arrays.asList(schema1, schema2), ConflictResolution.SKIP_CONFLICTS);
        
        // Verify success
        assertTrue(result.isSuccess(), "Merge should succeed with SKIP_CONFLICTS strategy");
        assertEquals(1, result.getMergedSchemas().size(), "Should have one merged schema");
        
        // Verify warnings and conflicts
        assertFalse(result.getWarnings().isEmpty(), "Should have warnings about skipped conflicts");
        assertFalse(result.getConflicts().isEmpty(), "Should have conflict information");
        assertTrue(result.getErrors().isEmpty(), "Should have no errors");
        
        // Verify conflicting complex type is skipped
        CsdlSchema mergedSchema = result.getMergedSchemas().get(0);
        assertTrue(mergedSchema.getComplexTypes() == null || mergedSchema.getComplexTypes().isEmpty(), 
                  "Should have no complex types due to conflict skipping");
    }
    
    @Test
    @DisplayName("ComplexType AUTO_MERGE - Should auto-merge compatible types")
    void testComplexType_AutoMerge() throws Exception {
        // Load test schemas
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("merger/complex-type/keep-first/schema1.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("merger/complex-type/keep-first/schema2.xml");
        
        // Merge with AUTO_MERGE strategy
        MergeResult result = schemaMerger.mergeSchemas(Arrays.asList(schema1, schema2), ConflictResolution.AUTO_MERGE);
        
        // For this test, we expect it to either succeed with auto-merge or fail if types are incompatible
        assertNotNull(result, "Should have merge result");
        assertFalse(result.getConflicts().isEmpty(), "Should have conflict information");
    }
    
    @Test
    @DisplayName("ComplexType No Conflict - Should merge without issues")
    void testComplexType_NoConflict() throws Exception {
        // Create schema with unique complex type name
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("merger/complex-type/keep-first/schema1.xml");
        
        // Merge single schema
        MergeResult result = schemaMerger.mergeSchemas(Arrays.asList(schema1), ConflictResolution.THROW_ERROR);
        
        // Verify success
        assertTrue(result.isSuccess(), "Merge should succeed with no conflicts");
        assertEquals(1, result.getMergedSchemas().size(), "Should have one merged schema");
        assertTrue(result.getErrors().isEmpty(), "Should have no errors");
        assertTrue(result.getConflicts().isEmpty(), "Should have no conflicts");
        
        // Verify complex type is preserved
        CsdlSchema mergedSchema = result.getMergedSchemas().get(0);
        assertNotNull(mergedSchema.getComplexTypes(), "Merged schema should have complex types");
        assertEquals(1, mergedSchema.getComplexTypes().size(), "Should have one complex type");
        assertEquals("Address", mergedSchema.getComplexTypes().get(0).getName());
    }
}
