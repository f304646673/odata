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
 * Test class for EnumType merging scenarios in DefaultSchemaMerger
 */
@DisplayName("EnumType Merger Tests")
public class DefaultSchemaMergerEnumTypeTest {
    
    private SchemaMerger schemaMerger;
    
    @BeforeEach
    void setUp() {
        schemaMerger = new DefaultSchemaMerger();
    }
    
    @Test
    @DisplayName("EnumType KEEP_FIRST - Should keep first definition")
    void testEnumType_KeepFirst() throws Exception {
        // Load test schemas
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("merger/enum-type/keep-first/schema1.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("merger/enum-type/keep-first/schema2.xml");
        
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
        assertNotNull(mergedSchema.getEnumTypes(), "Merged schema should have enum types");
        assertEquals(1, mergedSchema.getEnumTypes().size(), "Should have one enum type");
        
        // Check that first schema's definition is preserved
        assertEquals("Color", mergedSchema.getEnumTypes().get(0).getName());
        assertEquals(3, mergedSchema.getEnumTypes().get(0).getMembers().size(), "Should have 3 members from first schema");
    }
    
    @Test
    @DisplayName("EnumType KEEP_LAST - Should keep last definition")
    void testEnumType_KeepLast() throws Exception {
        // Load test schemas
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("merger/enum-type/keep-first/schema1.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("merger/enum-type/keep-first/schema2.xml");
        
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
        assertNotNull(mergedSchema.getEnumTypes(), "Merged schema should have enum types");
        assertEquals(1, mergedSchema.getEnumTypes().size(), "Should have one enum type");
        
        // Check that last schema's definition is preserved
        assertEquals("Color", mergedSchema.getEnumTypes().get(0).getName());
        assertEquals(3, mergedSchema.getEnumTypes().get(0).getMembers().size(), "Should have 3 members from last schema");
    }
    
    @Test
    @DisplayName("EnumType THROW_ERROR - Should throw error on conflict")
    void testEnumType_ThrowError() throws Exception {
        // Load test schemas
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("merger/enum-type/keep-first/schema1.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("merger/enum-type/keep-first/schema2.xml");
        
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
    @DisplayName("EnumType SKIP_CONFLICTS - Should skip conflicting definitions")
    void testEnumType_SkipConflicts() throws Exception {
        // Load test schemas
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("merger/enum-type/keep-first/schema1.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("merger/enum-type/keep-first/schema2.xml");
        
        // Merge with SKIP_CONFLICTS strategy
        MergeResult result = schemaMerger.mergeSchemas(Arrays.asList(schema1, schema2), ConflictResolution.SKIP_CONFLICTS);
        
        // Verify success
        assertTrue(result.isSuccess(), "Merge should succeed with SKIP_CONFLICTS strategy");
        assertEquals(1, result.getMergedSchemas().size(), "Should have one merged schema");
        
        // Verify warnings and conflicts
        assertFalse(result.getWarnings().isEmpty(), "Should have warnings about skipped conflicts");
        assertFalse(result.getConflicts().isEmpty(), "Should have conflict information");
        assertTrue(result.getErrors().isEmpty(), "Should have no errors");
        
        // Verify conflicting enum type is skipped
        CsdlSchema mergedSchema = result.getMergedSchemas().get(0);
        assertTrue(mergedSchema.getEnumTypes() == null || mergedSchema.getEnumTypes().isEmpty(), 
                  "Should have no enum types due to conflict skipping");
    }
    
    @Test
    @DisplayName("EnumType AUTO_MERGE - Should report cannot auto-merge")
    void testEnumType_AutoMerge() throws Exception {
        // Load test schemas
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("merger/enum-type/keep-first/schema1.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("merger/enum-type/keep-first/schema2.xml");
        
        // Merge with AUTO_MERGE strategy
        MergeResult result = schemaMerger.mergeSchemas(Arrays.asList(schema1, schema2), ConflictResolution.AUTO_MERGE);
        
        // Verify failure (enum types cannot be auto-merged)
        assertFalse(result.isSuccess(), "Merge should fail as enum types cannot be auto-merged");
        assertFalse(result.getErrors().isEmpty(), "Should have errors about inability to auto-merge");
        assertFalse(result.getConflicts().isEmpty(), "Should have conflict information");
    }
    
    @Test
    @DisplayName("EnumType No Conflict - Should merge without issues")
    void testEnumType_NoConflict() throws Exception {
        // Create schema with unique enum type name
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("merger/enum-type/keep-first/schema1.xml");
        
        // Merge single schema
        MergeResult result = schemaMerger.mergeSchemas(Arrays.asList(schema1), ConflictResolution.THROW_ERROR);
        
        // Verify success
        assertTrue(result.isSuccess(), "Merge should succeed with no conflicts");
        assertEquals(1, result.getMergedSchemas().size(), "Should have one merged schema");
        assertTrue(result.getErrors().isEmpty(), "Should have no errors");
        assertTrue(result.getConflicts().isEmpty(), "Should have no conflicts");
        
        // Verify enum type is preserved
        CsdlSchema mergedSchema = result.getMergedSchemas().get(0);
        assertNotNull(mergedSchema.getEnumTypes(), "Merged schema should have enum types");
        assertEquals(1, mergedSchema.getEnumTypes().size(), "Should have one enum type");
        assertEquals("Color", mergedSchema.getEnumTypes().get(0).getName());
    }
}
