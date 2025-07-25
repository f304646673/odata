package org.apache.olingo.schemamanager.repository.impl;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Arrays;

import java.util.Map;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * Tests for InMemorySchemaRepository.getAllSchemas() method.
 * Covers various scenarios for retrieving all schemas from the repository.
 */
class InMemorySchemaRepositoryTest_getAllSchemas {

    private InMemorySchemaRepository repository;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        repository = new InMemorySchemaRepository();
        System.out.println("Setting up test: " + testInfo.getDisplayName());
    }

    @Test
    void testGetAllSchemas_EmptyRepository_ShouldReturnEmptyMap() {
        // Act
        Map<String, CsdlSchema> result = repository.getAllSchemas();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    @Test
    void testGetAllSchemas_SingleSchema_ShouldReturnMapWithOneEntry() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        repository.addSchema(schema, "/test/path/simple-schema.xml");

        // Act
        Map<String, CsdlSchema> result = repository.getAllSchemas();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey(schema.getNamespace()));
        assertEquals(schema, result.get(schema.getNamespace()));
    }

    @Test
    void testGetAllSchemas_MultipleSchemas_ShouldReturnAllSchemas() {
        // Arrange
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema1, "/test/path/simple-schema.xml");
        repository.addSchema(schema2, "/test/path/complex-schema.xml");

        // Act
        Map<String, CsdlSchema> result = repository.getAllSchemas();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(schema1.getNamespace()));
        assertTrue(result.containsKey(schema2.getNamespace()));
        assertEquals(schema1, result.get(schema1.getNamespace()));
        assertEquals(schema2, result.get(schema2.getNamespace()));
    }

    @Test
    void testGetAllSchemas_ReturnedMapIsIndependent_ShouldNotAffectRepository() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        repository.addSchema(schema, "/test/path/simple-schema.xml");

        // Act
        Map<String, CsdlSchema> result = repository.getAllSchemas();
        result.clear(); // Modify the returned map

        // Assert - Repository should not be affected
        Map<String, CsdlSchema> repositorySchemas = repository.getAllSchemas();
        assertEquals(1, repositorySchemas.size());
        assertTrue(repositorySchemas.containsKey(schema.getNamespace()));
    }

    @Test
    void testGetAllSchemas_AfterClear_ShouldReturnEmptyMap() {
        // Arrange
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema1, "/test/path/simple-schema.xml");
        repository.addSchema(schema2, "/test/path/complex-schema.xml");

        // Verify schemas exist
        assertEquals(2, repository.getAllSchemas().size());

        // Act
        repository.clear();
        Map<String, CsdlSchema> result = repository.getAllSchemas();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    @Test
    void testGetAllSchemas_AfterOverwrite_ShouldContainLatestVersion() {
        // Arrange
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        
        // Force same namespace
        String commonNamespace = "test.overwrite.namespace";
        schema1.setNamespace(commonNamespace);
        schema2.setNamespace(commonNamespace);

        repository.addSchema(schema1, "/test/path/first.xml");
        repository.addSchema(schema2, "/test/path/second.xml");

        // Act
        Map<String, CsdlSchema> result = repository.getAllSchemas();

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.containsKey(commonNamespace));
        assertEquals(schema2, result.get(commonNamespace)); // Latest version
        assertNotEquals(schema1, result.get(commonNamespace));
    }

    @Test
    void testGetAllSchemas_WithComplexContent_ShouldIncludeAllElements() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");

        // Act
        Map<String, CsdlSchema> result = repository.getAllSchemas();

        // Assert
        assertEquals(1, result.size());
        CsdlSchema retrievedSchema = result.get(schema.getNamespace());
        assertNotNull(retrievedSchema);
        
        // Verify all content is preserved
        assertEquals(schema.getNamespace(), retrievedSchema.getNamespace());
        
        if (schema.getEntityTypes() != null) {
            assertEquals(schema.getEntityTypes().size(), 
                        retrievedSchema.getEntityTypes() != null ? retrievedSchema.getEntityTypes().size() : 0);
        }
        
        if (schema.getComplexTypes() != null) {
            assertEquals(schema.getComplexTypes().size(), 
                        retrievedSchema.getComplexTypes() != null ? retrievedSchema.getComplexTypes().size() : 0);
        }
        
        if (schema.getEnumTypes() != null) {
            assertEquals(schema.getEnumTypes().size(), 
                        retrievedSchema.getEnumTypes() != null ? retrievedSchema.getEnumTypes().size() : 0);
        }
    }

    @Test
    void testGetAllSchemas_ConcurrentAccess_ShouldBeThreadSafe() throws InterruptedException {
        // Arrange
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema1, "/test/path/simple-schema.xml");
        repository.addSchema(schema2, "/test/path/complex-schema.xml");

        final int numberOfThreads = 10;
        final int operationsPerThread = 50;
        Thread[] threads = new Thread[numberOfThreads];
        final boolean[] results = new boolean[numberOfThreads];

        // Act
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                boolean allSuccessful = true;
                for (int j = 0; j < operationsPerThread; j++) {
                    Map<String, CsdlSchema> allSchemas = repository.getAllSchemas();
                    if (allSchemas.size() != 2 || 
                        !allSchemas.containsKey(schema1.getNamespace()) ||
                        !allSchemas.containsKey(schema2.getNamespace())) {
                        allSuccessful = false;
                        break;
                    }
                }
                results[threadIndex] = allSuccessful;
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Assert
        for (boolean result : results) {
            assertTrue(result, "One or more threads failed to get all schemas correctly");
        }
    }

    @Test
    void testGetAllSchemas_WithDifferentNamespaces_ShouldPreserveAllNamespaces() {
        // Arrange
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        
        // Ensure different namespaces
        schema1.setNamespace("namespace.one");
        schema2.setNamespace("namespace.two");
        
        repository.addSchema(schema1, "/test/path/schema1.xml");
        repository.addSchema(schema2, "/test/path/schema2.xml");

        // Act
        Map<String, CsdlSchema> result = repository.getAllSchemas();

        // Assert
        assertEquals(2, result.size());
        assertEquals(schema1, result.get("namespace.one"));
        assertEquals(schema2, result.get("namespace.two"));
        assertNotEquals(result.get("namespace.one"), result.get("namespace.two"));
    }

    @Test
    void testGetAllSchemas_ConsistentWithGetSchema_ShouldReturnSameSchemas() {
        // Arrange
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema1, "/test/path/simple-schema.xml");
        repository.addSchema(schema2, "/test/path/complex-schema.xml");

        // Act
        Map<String, CsdlSchema> allSchemas = repository.getAllSchemas();

        // Assert
        for (Map.Entry<String, CsdlSchema> entry : allSchemas.entrySet()) {
            String namespace = entry.getKey();
            CsdlSchema schemaFromAll = entry.getValue();
            CsdlSchema schemaFromGet = repository.getSchema(namespace);
            
            assertEquals(schemaFromAll, schemaFromGet);
        }
    }
}
