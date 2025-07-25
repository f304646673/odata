package org.apache.olingo.schemamanager.repository;

import java.io.InputStream;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.repository.impl.InMemorySchemaRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemorySchemaRepositoryTest_getSchema {
    
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
    void testGetSchema_ExistingNamespace() throws Exception {
        // Test getting schema by existing namespace
        CsdlSchema schema = loadSchemaFromResource("xml-schemas/repository/basic-repository.xml");
        repository.addSchema(schema, "basic-repository.xml");
        
        CsdlSchema result = repository.getSchema("TestService.Repository.Basic");
        
        assertNotNull(result);
        assertEquals("TestService.Repository.Basic", result.getNamespace());
    }
    
    @Test
    void testGetSchema_NonExistingNamespace() {
        // Test getting schema by non-existing namespace
        CsdlSchema result = repository.getSchema("NonExistent.Namespace");
        
        assertNull(result);
    }
    
    @Test
    void testGetSchema_NullNamespace() {
        // Test getting schema with null namespace
        CsdlSchema result = repository.getSchema(null);
        
        assertNull(result);
    }
    
    @Test
    void testGetSchema_EmptyNamespace() {
        // Test getting schema with empty namespace
        CsdlSchema result = repository.getSchema("");
        
        assertNull(result);
    }
    
    @Test
    void testGetSchema_AfterMultipleAdds() throws Exception {
        // Test getting specific schema after adding multiple
        CsdlSchema schema1 = loadSchemaFromResource("xml-schemas/repository/basic-repository.xml");
        CsdlSchema schema2 = loadSchemaFromResource("xml-schemas/repository/multi-schema.xml");
        
        repository.addSchema(schema1, "basic-repository.xml");
        repository.addSchema(schema2, "multi-schema.xml");
        
        CsdlSchema result1 = repository.getSchema("TestService.Repository.Basic");
        CsdlSchema result2 = repository.getSchema("TestService.Repository.Multi.Schema1");
        
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals("TestService.Repository.Basic", result1.getNamespace());
        assertEquals("TestService.Repository.Multi.Schema1", result2.getNamespace());
    }
}
