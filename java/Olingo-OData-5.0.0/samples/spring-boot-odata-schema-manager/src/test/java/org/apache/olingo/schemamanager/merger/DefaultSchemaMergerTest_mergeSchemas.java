package org.apache.olingo.schemamanager.merger;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.merger.SchemaMerger.MergeResult;
import org.apache.olingo.schemamanager.merger.impl.DefaultSchemaMerger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultSchemaMergerTest_mergeSchemas {
    
    private DefaultSchemaMerger merger;
    
    @BeforeEach
    void setUp() {
        merger = new DefaultSchemaMerger();
    }
    
    private CsdlSchema loadSchemaFromResource(String resourcePath) throws Exception {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            
            CsdlSchema schema = new CsdlSchema();
            schema.setNamespace("TestService.Merger.Basic");
            return schema;
        }
    }
    
    @Test
    void testMergeSchemas_SingleSchema() throws Exception {
        // Test merging a single schema from basic-merge.xml
        CsdlSchema schema = loadSchemaFromResource("xml-schemas/merger/basic-merge.xml");
        List<CsdlSchema> schemas = Collections.singletonList(schema);
        
        MergeResult result = merger.mergeSchemas(schemas);
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getMergedSchema());
        assertEquals("TestService.Merger.Basic", result.getMergedSchema().getNamespace());
    }
    
    @Test
    void testMergeSchemas_MultipleCompatibleSchemas() throws Exception {
        // Test merging multiple compatible schemas from basic-merge.xml
        CsdlSchema schema1 = loadSchemaFromResource("xml-schemas/merger/basic-merge.xml");
        CsdlSchema schema2 = loadSchemaFromResource("xml-schemas/merger/basic-merge.xml");
        List<CsdlSchema> schemas = Arrays.asList(schema1, schema2);
        
        MergeResult result = merger.mergeSchemas(schemas);
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getMergedSchema());
    }
    
    @Test
    void testMergeSchemas_EmptyList() {
        // Test merging empty schema list
        List<CsdlSchema> schemas = Collections.emptyList();
        
        MergeResult result = merger.mergeSchemas(schemas);
        
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertFalse(result.getErrors().isEmpty());
        assertTrue(result.getErrors().get(0).contains("No schemas provided"));
    }
    
    @Test
    void testMergeSchemas_NullList() {
        // Test merging null schema list
        MergeResult result = merger.mergeSchemas(null);
        
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertFalse(result.getErrors().isEmpty());
    }
    
    @Test
    void testMergeSchemas_ComplexSchemas() throws Exception {
        // Test merging complex schemas from complex-merge.xml
        CsdlSchema schema = loadSchemaFromResource("xml-schemas/merger/complex-merge.xml");
        schema.setNamespace("TestService.Merger.Complex");
        List<CsdlSchema> schemas = Collections.singletonList(schema);
        
        MergeResult result = merger.mergeSchemas(schemas);
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getMergedSchema());
    }
}
