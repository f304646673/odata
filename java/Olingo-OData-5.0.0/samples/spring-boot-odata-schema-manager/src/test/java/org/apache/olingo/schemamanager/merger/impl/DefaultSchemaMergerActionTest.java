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
 * Test class for Action merging scenarios in DefaultSchemaMerger
 */
@DisplayName("Action Merger Tests")
public class DefaultSchemaMergerActionTest {
    
    private SchemaMerger schemaMerger;
    
    @BeforeEach
    void setUp() {
        schemaMerger = new DefaultSchemaMerger();
    }
    
    @Test
    @DisplayName("Action KEEP_FIRST - Should keep first definition")
    void testAction_KeepFirst() throws Exception {
        // Load test schemas
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("merger/action/keep-first/schema1.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("merger/action/keep-first/schema2.xml");
        
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
        assertNotNull(mergedSchema.getActions(), "Merged schema should have actions");
        assertEquals(1, mergedSchema.getActions().size(), "Should have one action");
        
        // Check that first schema's action is preserved
        assertEquals("ProcessOrder", mergedSchema.getActions().get(0).getName());
        assertEquals(2, mergedSchema.getActions().get(0).getParameters().size(), "Should have 2 parameters from first schema");
    }
    
    @Test
    @DisplayName("Action KEEP_LAST - Should keep last definition")
    void testAction_KeepLast() throws Exception {
        // Load test schemas
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("merger/action/keep-last/schema1.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("merger/action/keep-last/schema2.xml");
        
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
        assertNotNull(mergedSchema.getActions(), "Merged schema should have actions");
        assertEquals(1, mergedSchema.getActions().size(), "Should have one action");
        
        // Check that last schema's action is preserved
        assertEquals("ProcessOrder", mergedSchema.getActions().get(0).getName());
        assertEquals(2, mergedSchema.getActions().get(0).getParameters().size(), "Should have 2 parameters from last schema");
    }
    
    @Test
    @DisplayName("Action THROW_ERROR - Should throw error on conflict")
    void testAction_ThrowError() throws Exception {
        // Load test schemas
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("merger/action/keep-first/schema1.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("merger/action/keep-first/schema2.xml");
        
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
    @DisplayName("Action SKIP_CONFLICTS - Should skip conflicting definitions")
    void testAction_SkipConflicts() throws Exception {
        // Load test schemas
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("merger/action/keep-first/schema1.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("merger/action/keep-first/schema2.xml");
        
        // Merge with SKIP_CONFLICTS strategy
        MergeResult result = schemaMerger.mergeSchemas(Arrays.asList(schema1, schema2), ConflictResolution.SKIP_CONFLICTS);
        
        // Verify success
        assertTrue(result.isSuccess(), "Merge should succeed with SKIP_CONFLICTS strategy");
        assertEquals(1, result.getMergedSchemas().size(), "Should have one merged schema");
        
        // Verify warnings and conflicts
        assertFalse(result.getWarnings().isEmpty(), "Should have warnings about skipped conflicts");
        assertFalse(result.getConflicts().isEmpty(), "Should have conflict information");
        assertTrue(result.getErrors().isEmpty(), "Should have no errors");
        
        // Verify conflicting action is skipped
        CsdlSchema mergedSchema = result.getMergedSchemas().get(0);
        assertTrue(mergedSchema.getActions() == null || mergedSchema.getActions().isEmpty(), 
                  "Should have no actions due to conflict skipping");
    }
    
    @Test
    @DisplayName("Action AUTO_MERGE - Should report cannot auto-merge")
    void testAction_AutoMerge() throws Exception {
        // Load test schemas
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("merger/action/keep-first/schema1.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("merger/action/keep-first/schema2.xml");
        
        // Merge with AUTO_MERGE strategy
        MergeResult result = schemaMerger.mergeSchemas(Arrays.asList(schema1, schema2), ConflictResolution.AUTO_MERGE);
        
        // Verify failure (actions cannot be auto-merged)
        assertFalse(result.isSuccess(), "Merge should fail as actions cannot be auto-merged");
        assertFalse(result.getErrors().isEmpty(), "Should have errors about inability to auto-merge");
        assertFalse(result.getConflicts().isEmpty(), "Should have conflict information");
    }
    
    @Test
    @DisplayName("Action No Conflict - Should merge without issues")
    void testAction_NoConflict() throws Exception {
        // Create schema with unique action name
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("merger/action/keep-first/schema1.xml");
        
        // Merge single schema
        MergeResult result = schemaMerger.mergeSchemas(Arrays.asList(schema1), ConflictResolution.THROW_ERROR);
        
        // Verify success
        assertTrue(result.isSuccess(), "Merge should succeed with no conflicts");
        assertEquals(1, result.getMergedSchemas().size(), "Should have one merged schema");
        assertTrue(result.getErrors().isEmpty(), "Should have no errors");
        assertTrue(result.getConflicts().isEmpty(), "Should have no conflicts");
        
        // Verify action is preserved
        CsdlSchema mergedSchema = result.getMergedSchemas().get(0);
        assertNotNull(mergedSchema.getActions(), "Merged schema should have actions");
        assertEquals(1, mergedSchema.getActions().size(), "Should have one action");
        assertEquals("ProcessOrder", mergedSchema.getActions().get(0).getName());
    }
}
