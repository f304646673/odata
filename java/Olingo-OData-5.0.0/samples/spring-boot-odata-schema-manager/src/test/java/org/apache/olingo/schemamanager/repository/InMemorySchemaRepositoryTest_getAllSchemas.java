package org.apache.olingo.schemamanager.repository;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Arrays;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.repository.impl.InMemorySchemaRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemorySchemaRepositoryTest_getAllSchemas {
    
    private InMemorySchemaRepository repository;
    
    @BeforeEach
    void setUp() {
        repository = new InMemorySchemaRepository();
    }
    
    private CsdlSchema loadSchemaFromResource(String resourcePath, String namespace) throws Exception {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            
            CsdlSchema schema = new CsdlSchema();
            schema.setNamespace(namespace);
            return schema;
        }
    }
    
    @Test
    void testGetAllSchemas_EmptyRepository() {
        // Test getting all schemas from empty repository
        Map<String, CsdlSchema> schemas = repository.getAllSchemas();
        
        assertNotNull(schemas);
        assertTrue(schemas.isEmpty());
    }
    
    @Test
    void testGetAllSchemas_SingleSchema() throws Exception {
        // Test getting all schemas with single schema
        CsdlSchema schema = loadSchemaFromResource("xml-schemas/repository/basic-repository.xml", 
                                                  "TestService.Repository.Basic");
        repository.addSchema(schema, "basic-repository.xml");
        
        Map<String, CsdlSchema> schemas = repository.getAllSchemas();
        
        assertNotNull(schemas);
        assertEquals(1, schemas.size());
        assertTrue(schemas.containsKey("TestService.Repository.Basic"));
        assertEquals("TestService.Repository.Basic", schemas.get("TestService.Repository.Basic").getNamespace());
    }
    
    @Test
    void testGetAllSchemas_MultipleSchemas() throws Exception {
        // Test getting all schemas with multiple schemas from multi-schema.xml
        CsdlSchema schema1 = loadSchemaFromResource("xml-schemas/repository/basic-repository.xml", 
                                                   "TestService.Repository.Basic");
        CsdlSchema schema2 = loadSchemaFromResource("xml-schemas/repository/multi-schema.xml", 
                                                   "TestService.Repository.Multi.Schema1");
        CsdlSchema schema3 = loadSchemaFromResource("xml-schemas/repository/multi-schema.xml", 
                                                   "TestService.Repository.Multi.Schema2");
        
        repository.addSchema(schema1, "basic-repository.xml");
        repository.addSchema(schema2, "multi-schema.xml");
        repository.addSchema(schema3, "multi-schema.xml");
        
        Map<String, CsdlSchema> schemas = repository.getAllSchemas();
        
        assertNotNull(schemas);
        assertEquals(3, schemas.size());
        
        // Verify all namespaces are present
        Set<String> namespaces = schemas.keySet();
        
        assertTrue(namespaces.contains("TestService.Repository.Basic"));
        assertTrue(namespaces.contains("TestService.Repository.Multi.Schema1"));
        assertTrue(namespaces.contains("TestService.Repository.Multi.Schema2"));
    }
    
    @Test
    void testGetAllSchemas_ReturnsCopy() throws Exception {
        // Test that getAllSchemas returns a copy (not modifiable)
        CsdlSchema schema = loadSchemaFromResource("xml-schemas/repository/basic-repository.xml", 
                                                  "TestService.Repository.Basic");
        repository.addSchema(schema, "basic-repository.xml");
        
        Map<String, CsdlSchema> schemas1 = repository.getAllSchemas();
        Map<String, CsdlSchema> schemas2 = repository.getAllSchemas();
        
        assertNotNull(schemas1);
        assertNotNull(schemas2);
        assertEquals(schemas1.size(), schemas2.size());
        
        // Verify they are separate instances (if implementation provides copies)
        assertNotSame(schemas1, schemas2);
    }
}
