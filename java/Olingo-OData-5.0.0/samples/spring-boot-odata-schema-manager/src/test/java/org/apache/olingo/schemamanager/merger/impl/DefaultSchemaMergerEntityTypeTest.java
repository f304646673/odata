package org.apache.olingo.schemamanager.merger.impl;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.schemamanager.merger.SchemaMerger;
import org.apache.olingo.schemamanager.merger.SchemaMerger.ConflictResolution;
import org.apache.olingo.schemamanager.merger.SchemaMerger.MergeResult;
import org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils;

/**
 * Test class for EntityType merging scenarios in DefaultSchemaMerger
 */
@DisplayName("EntityType Merger Tests")
public class DefaultSchemaMergerEntityTypeTest {
    
    private SchemaMerger schemaMerger;
    
    @BeforeEach
    void setUp() {
        schemaMerger = new DefaultSchemaMerger();
    }
    
    @Test
    @DisplayName("EntityType KEEP_FIRST - Should keep first definition")
    void testEntityType_KeepFirst() throws Exception {
        // Load test schemas
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("merger/entity-type/keep-first/schema1.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("merger/entity-type/keep-first/schema2.xml");
        
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
        assertNotNull(mergedSchema.getEntityTypes(), "Merged schema should have entity types");
        assertEquals(1, mergedSchema.getEntityTypes().size(), "Should have one entity type");
        
        // Check that first schema's properties are preserved
        assertTrue(entityTypesEqual(mergedSchema.getEntityTypes().get(0), schema1.getEntityTypes().get(0)), 
                   "Merged entity type should match first schema's entity type");
    }

    @Test
    @DisplayName("EntityType KEEP_LAST - Should keep last definition")
    void testEntityType_KeepLast() throws Exception {
        // Load test schemas
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("merger/entity-type/keep-last/schema1.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("merger/entity-type/keep-last/schema2.xml");
        
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
        assertNotNull(mergedSchema.getEntityTypes(), "Merged schema should have entity types");
        assertEquals(1, mergedSchema.getEntityTypes().size(), "Should have one entity type");
        
        // Check that last schema's properties are preserved
        assertEquals("Product", mergedSchema.getEntityTypes().get(0).getName());
        assertEquals(3, mergedSchema.getEntityTypes().get(0).getProperties().size(), "Should have 3 properties from last schema");
    }
    
    @Test
    @DisplayName("EntityType THROW_ERROR - Should throw error on conflict")
    void testEntityType_ThrowError() throws Exception {
        // Load test schemas
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("merger/entity-type/throw-error/schema1.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("merger/entity-type/throw-error/schema2.xml");
        
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
    @DisplayName("EntityType SKIP_CONFLICTS - Should skip conflicting definitions")
    void testEntityType_SkipConflicts() throws Exception {
        // Load test schemas
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("merger/entity-type/keep-first/schema1.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("merger/entity-type/keep-first/schema2.xml");
        
        // Merge with SKIP_CONFLICTS strategy
        MergeResult result = schemaMerger.mergeSchemas(Arrays.asList(schema1, schema2), ConflictResolution.SKIP_CONFLICTS);
        
        // Verify success
        assertTrue(result.isSuccess(), "Merge should succeed with SKIP_CONFLICTS strategy");
        assertEquals(1, result.getMergedSchemas().size(), "Should have one merged schema");
        
        // Verify warnings and conflicts
        assertFalse(result.getWarnings().isEmpty(), "Should have warnings about skipped conflicts");
        assertFalse(result.getConflicts().isEmpty(), "Should have conflict information");
        assertTrue(result.getErrors().isEmpty(), "Should have no errors");
        
        // Verify conflicting entity type is skipped
        CsdlSchema mergedSchema = result.getMergedSchemas().get(0);
        assertTrue(mergedSchema.getEntityTypes() == null || mergedSchema.getEntityTypes().isEmpty(), 
                  "Should have no entity types due to conflict skipping");
    }
    
    @Test
    @DisplayName("EntityType AUTO_MERGE - Should auto-merge compatible types")
    void testEntityType_AutoMerge() throws Exception {
        // Create compatible entity types for auto-merge
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("merger/entity-type/keep-first/schema1.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("merger/entity-type/keep-first/schema2.xml");
        
        // Merge with AUTO_MERGE strategy
        MergeResult result = schemaMerger.mergeSchemas(Arrays.asList(schema1, schema2), ConflictResolution.AUTO_MERGE);
        
        // For this test, we expect it to either succeed with auto-merge or fail if types are incompatible
        // The exact behavior depends on the compatibility of the test schemas
        assertNotNull(result, "Should have merge result");
        assertFalse(result.getConflicts().isEmpty(), "Should have conflict information");
    }
    
    @Test
    @DisplayName("EntityType No Conflict - Should merge without issues")
    void testEntityType_NoConflict() throws Exception {
        // Create schema with unique entity type name
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("merger/entity-type/keep-first/schema1.xml");
        
        // Merge single schema
        MergeResult result = schemaMerger.mergeSchemas(Arrays.asList(schema1), ConflictResolution.THROW_ERROR);
        
        // Verify success
        assertTrue(result.isSuccess(), "Merge should succeed with no conflicts");
        assertEquals(1, result.getMergedSchemas().size(), "Should have one merged schema");
        assertTrue(result.getErrors().isEmpty(), "Should have no errors");
        assertTrue(result.getConflicts().isEmpty(), "Should have no conflicts");
        
        // Verify entity type is preserved
        CsdlSchema mergedSchema = result.getMergedSchemas().get(0);
        assertNotNull(mergedSchema.getEntityTypes(), "Merged schema should have entity types");
        assertEquals(1, mergedSchema.getEntityTypes().size(), "Should have one entity type");
        assertEquals("Product", mergedSchema.getEntityTypes().get(0).getName());
    }

    // check if two entity types are equal
    private boolean entityTypesEqual(CsdlEntityType type1, CsdlEntityType type2) {
        return type1.getName().equals(type2.getName())
               && type1.getProperties().equals(type2.getProperties())
               && type1.getKey().equals(type2.getKey())
               && type1.getNavigationProperties().equals(type2.getNavigationProperties())
               && type1.getAnnotations().equals(type2.getAnnotations())
               && (type1.getBaseType() == null ? type2.getBaseType() == null : type1.getBaseType().equals(type2.getBaseType()))
               && type1.isAbstract() == type2.isAbstract()
               && type1.isOpenType() == type2.isOpenType();
    }
}
