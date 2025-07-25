package org.apache.olingo.schemamanager.repository;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Arrays;

import java.io.InputStream;
import java.util.Map;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.repository.impl.InMemorySchemaRepository;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemorySchemaRepositoryTest_addSchema {
    
    private InMemorySchemaRepository repository;
    
    @BeforeEach
    void setUp() {
        repository = new InMemorySchemaRepository();
    }
    
    private CsdlSchema loadSchemaFromResource(String resourcePath) throws Exception {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            
            CsdlSchema schema = new CsdlSchema();
            if (resourcePath.contains("basic-repository")) {
                schema.setNamespace("TestService.Repository.Basic");
            } else if (resourcePath.contains("multi-schema")) {
                schema.setNamespace("TestService.Repository.Multi.Schema1");
            }
            return schema;
        }
    }
    
    @Test
    void testAddSchema_SingleSchema() throws Exception {
        // Test adding a single schema from basic-repository.xml
        CsdlSchema schema = loadSchemaFromResource("xml-schemas/repository/basic-repository.xml");
        
        repository.addSchema(schema, "basic-repository.xml");
        
        Map<String, CsdlSchema> storedSchemas = repository.getAllSchemas();
        assertEquals(1, storedSchemas.size());
        assertTrue(storedSchemas.containsKey("TestService.Repository.Basic"));
        assertEquals("TestService.Repository.Basic", storedSchemas.get("TestService.Repository.Basic").getNamespace());
    }
    
    @Test
    void testAddSchema_MultipleSchemas() throws Exception {
        // Test adding multiple schemas
        CsdlSchema schema1 = loadSchemaFromResource("xml-schemas/repository/basic-repository.xml");
        CsdlSchema schema2 = loadSchemaFromResource("xml-schemas/repository/multi-schema.xml");
        
        repository.addSchema(schema1, "basic-repository.xml");
        repository.addSchema(schema2, "multi-schema.xml");
        
        Map<String, CsdlSchema> storedSchemas = repository.getAllSchemas();
        assertEquals(2, storedSchemas.size());
        assertTrue(storedSchemas.containsKey("TestService.Repository.Basic"));
        assertTrue(storedSchemas.containsKey("TestService.Repository.Multi.Schema1"));
    }
    
    @Test
    void testAddSchema_DuplicateNamespace() throws Exception {
        // Test adding schema with duplicate namespace
        CsdlSchema schema1 = loadSchemaFromResource("xml-schemas/repository/basic-repository.xml");
        CsdlSchema schema2 = loadSchemaFromResource("xml-schemas/repository/basic-repository.xml");
        
        repository.addSchema(schema1, "basic-repository1.xml");
        
        // Should handle duplicate namespace appropriately (overwrites)
        assertDoesNotThrow(() -> repository.addSchema(schema2, "basic-repository2.xml"));
        
        Map<String, CsdlSchema> storedSchemas = repository.getAllSchemas();
        assertEquals(1, storedSchemas.size()); // Only one schema with that namespace
    }
    
    @Test
    void testAddSchema_NullSchema() {
        // Test adding null schema - should not throw but log warning
        assertDoesNotThrow(() -> repository.addSchema(null, "test.xml"));
        
        Map<String, CsdlSchema> storedSchemas = repository.getAllSchemas();
        assertTrue(storedSchemas.isEmpty());
    }
    
    @Test
    void testAddSchema_NullNamespace() throws Exception {
        // Test adding schema with null namespace - should not throw but log warning
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace(null);
        
        assertDoesNotThrow(() -> repository.addSchema(schema, "test.xml"));
        
        Map<String, CsdlSchema> storedSchemas = repository.getAllSchemas();
        assertTrue(storedSchemas.isEmpty());
    }
}
