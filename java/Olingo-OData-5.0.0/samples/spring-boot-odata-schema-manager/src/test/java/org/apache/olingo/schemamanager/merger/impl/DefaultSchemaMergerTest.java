package org.apache.olingo.schemamanager.merger.impl;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.*;
import org.apache.olingo.schemamanager.merger.SchemaMerger;
import org.apache.olingo.schemamanager.merger.SchemaMerger.ConflictResolution;
import org.apache.olingo.schemamanager.merger.SchemaMerger.MergeResult;
import org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for DefaultSchemaMerger
 */
class DefaultSchemaMergerTest {

    private SchemaMerger schemaMerger;

    @BeforeEach
    void setUp() {
        schemaMerger = new DefaultSchemaMerger();
    }

    @Test
    void testMergeSchemas_Simple() {
        // Create schemas manually to avoid XML parsing issues
        CsdlSchema schema1 = new CsdlSchema();
        schema1.setNamespace("TestNamespace");  // Same namespace to trigger conflict resolution
        schema1.setAlias("TN");
        
        CsdlEntityType entityType1 = new CsdlEntityType();
        entityType1.setName("User");
        
        schema1.setEntityTypes(Arrays.asList(entityType1));
        
        // Create second schema - SAME namespace and entity name to trigger conflict
        CsdlSchema schema2 = new CsdlSchema();
        schema2.setNamespace("TestNamespace");  // Same namespace
        schema2.setAlias("TN");
        
        CsdlEntityType entityType2 = new CsdlEntityType();
        entityType2.setName("User");  // Same entity name
        
        schema2.setEntityTypes(Arrays.asList(entityType2));
        
        // Verify schemas created correctly
        assertNotNull(schema1, "Schema1 should not be null");
        assertNotNull(schema2, "Schema2 should not be null");
        assertNotNull(schema1.getNamespace(), "Schema1 namespace should not be null");
        assertNotNull(schema2.getNamespace(), "Schema2 namespace should not be null");
        
        System.out.println("Schema1: " + schema1.getNamespace() + ", EntityTypes: " + 
                          (schema1.getEntityTypes() != null ? schema1.getEntityTypes().size() : "null"));
        System.out.println("Schema2: " + schema2.getNamespace() + ", EntityTypes: " + 
                          (schema2.getEntityTypes() != null ? schema2.getEntityTypes().size() : "null"));
        
        // Debug entity type details
        if (schema1.getEntityTypes() != null && !schema1.getEntityTypes().isEmpty()) {
            CsdlEntityType et1 = schema1.getEntityTypes().get(0);
            System.out.println("EntityType1 - Name: '" + et1.getName() + "', BaseType: '" + et1.getBaseType() + "'");
        }
        if (schema2.getEntityTypes() != null && !schema2.getEntityTypes().isEmpty()) {
            CsdlEntityType et2 = schema2.getEntityTypes().get(0);
            System.out.println("EntityType2 - Name: '" + et2.getName() + "', BaseType: '" + et2.getBaseType() + "'");
        }
        
        try {
            System.out.println("TEST: About to call merge");
            // Merge schemas with AUTO_MERGE - should detect conflict and try to auto-merge
            MergeResult result = schemaMerger.mergeSchemas(Arrays.asList(schema1, schema2), ConflictResolution.AUTO_MERGE);
            System.out.println("TEST: Merge call completed");
            
            // Verify result
            assertNotNull(result);
            if (!result.isSuccess()) {
                System.out.println("Merge failed with errors: " + result.getErrors());
                System.out.println("Conflicts: " + result.getConflicts());
            }
            assertTrue(result.isSuccess(), "Merge should succeed but failed with errors: " + result.getErrors());
            assertEquals(1, result.getMergedSchemas().size(), "Should have 1 merged schema");
        } catch (Exception e) {
            System.err.println("=== EXCEPTION IN TEST ===");
            System.err.println("Exception type: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getClass().getName() + ": " + e.getCause().getMessage());
            }
            System.err.println("Stack trace:");
            e.printStackTrace();
            System.err.println("=== END EXCEPTION ===");
            fail("Exception during merge: " + e.getMessage());
        }
    }

    @Test
    void testMergeSchemas_SingleNamespace_ThrowError() {
        // Load test schemas
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("merger/schema1.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("merger/schema2.xml");
        
        // Merge schemas with THROW_ERROR to detect conflicts
        MergeResult result = schemaMerger.mergeSchemas(Arrays.asList(schema1, schema2), ConflictResolution.THROW_ERROR);
        
        // Verify result
        assertNotNull(result);
        assertFalse(result.isSuccess(), "Merge should fail with THROW_ERROR strategy due to conflicts");
        assertFalse(result.getErrors().isEmpty());
        assertFalse(result.getConflicts().isEmpty());
        
        // Verify conflicts are properly detected
        assertTrue(result.getConflicts().stream().anyMatch(c -> "User".equals(c.getElementName())));
        assertTrue(result.getConflicts().stream().anyMatch(c -> "Address".equals(c.getElementName())));
        assertTrue(result.getConflicts().stream().anyMatch(c -> "Status".equals(c.getElementName())));
        assertTrue(result.getConflicts().stream().anyMatch(c -> "Container".equals(c.getElementName())));
    }

    @Test
    void testMergeSchemas_MultipleNamespaces() {
        // Load test schemas with different namespaces
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("merger/schema1.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("merger/schema_different_namespace.xml");
        
        // Merge schemas
        MergeResult result = schemaMerger.mergeSchemas(Arrays.asList(schema1, schema2), ConflictResolution.THROW_ERROR);
        
        // Verify result
        assertNotNull(result);
        if (!result.isSuccess()) {
            System.out.println("Merge failed with errors: " + result.getErrors());
            System.out.println("Conflicts: " + result.getConflicts());
        }
        assertTrue(result.isSuccess(), "Merge should succeed but failed with errors: " + result.getErrors());
        assertTrue(result.getConflicts().isEmpty());
        assertEquals(2, result.getMergedSchemas().size());
    }

    @Test
    void testMergeSchemas_KeepFirst() {
        // Load test schemas
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("merger/schema1.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("merger/schema2.xml");
        
        // Merge schemas with KEEP_FIRST strategy
        MergeResult result = schemaMerger.mergeSchemas(Arrays.asList(schema1, schema2), ConflictResolution.KEEP_FIRST);
        
        // Verify result
        assertNotNull(result);
        assertTrue(result.isSuccess(), "Merge should succeed with KEEP_FIRST strategy");
        assertEquals(1, result.getMergedSchemas().size());
        assertFalse(result.getWarnings().isEmpty(), "Should have warnings about conflicts");
        assertFalse(result.getConflicts().isEmpty(), "Should detect conflicts");
    }

    @Test
    void testMergeSchemas_KeepLast() {
        // Load test schemas
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("merger/schema1.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("merger/schema2.xml");
        
        // Merge schemas with KEEP_LAST strategy
        MergeResult result = schemaMerger.mergeSchemas(Arrays.asList(schema1, schema2), ConflictResolution.KEEP_LAST);
        
        // Verify result
        assertNotNull(result);
        assertTrue(result.isSuccess(), "Merge should succeed with KEEP_LAST strategy");
        assertEquals(1, result.getMergedSchemas().size());
        assertFalse(result.getWarnings().isEmpty(), "Should have warnings about conflicts");
        assertFalse(result.getConflicts().isEmpty(), "Should detect conflicts");
    }

    @Test
    void testMergeSchemas_SkipConflicts() {
        // Load test schemas
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("merger/schema1.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("merger/schema2.xml");
        
        // Merge schemas with SKIP_CONFLICTS strategy
        MergeResult result = schemaMerger.mergeSchemas(Arrays.asList(schema1, schema2), ConflictResolution.SKIP_CONFLICTS);
        
        // Verify result
        assertNotNull(result);
        assertTrue(result.isSuccess(), "Merge should succeed with SKIP_CONFLICTS strategy");
        assertEquals(1, result.getMergedSchemas().size());
        assertFalse(result.getWarnings().isEmpty(), "Should have warnings about skipped conflicts");
        assertFalse(result.getConflicts().isEmpty(), "Should detect conflicts");
    }
}
