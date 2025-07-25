package org.apache.olingo.schemamanager.repository.impl;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

import java.util.Set;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * Tests for InMemorySchemaRepository.getAllNamespaces() method.
 * Covers various scenarios for retrieving all namespaces from the repository.
 */
class InMemorySchemaRepositoryTest_getAllNamespaces {

    private InMemorySchemaRepository repository;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        repository = new InMemorySchemaRepository();
        System.out.println("Setting up test: " + testInfo.getDisplayName());
    }

    @Test
    void testGetAllNamespaces_EmptyRepository_ShouldReturnEmptySet() {
        // Act
        Set<String> result = repository.getAllNamespaces();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    @Test
    void testGetAllNamespaces_SingleSchema_ShouldReturnSetWithOneNamespace() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        repository.addSchema(schema, "/test/path/simple-schema.xml");

        // Act
        Set<String> result = repository.getAllNamespaces();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(schema.getNamespace()));
    }

    @Test
    void testGetAllNamespaces_MultipleSchemas_ShouldReturnAllNamespaces() {
        // Arrange
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema1, "/test/path/simple-schema.xml");
        repository.addSchema(schema2, "/test/path/complex-schema.xml");

        // Act
        Set<String> result = repository.getAllNamespaces();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(schema1.getNamespace()));
        assertTrue(result.contains(schema2.getNamespace()));
    }

    @Test
    void testGetAllNamespaces_ReturnedSetIsIndependent_ShouldNotAffectRepository() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        repository.addSchema(schema, "/test/path/simple-schema.xml");

        // Act
        Set<String> result = repository.getAllNamespaces();
        result.clear(); // Modify the returned set

        // Assert - Repository should not be affected
        Set<String> repositoryNamespaces = repository.getAllNamespaces();
        assertEquals(1, repositoryNamespaces.size());
        assertTrue(repositoryNamespaces.contains(schema.getNamespace()));
    }

    @Test
    void testGetAllNamespaces_AfterClear_ShouldReturnEmptySet() {
        // Arrange
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema1, "/test/path/simple-schema.xml");
        repository.addSchema(schema2, "/test/path/complex-schema.xml");

        // Verify namespaces exist
        assertEquals(2, repository.getAllNamespaces().size());

        // Act
        repository.clear();
        Set<String> result = repository.getAllNamespaces();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    @Test
    void testGetAllNamespaces_AfterOverwrite_ShouldContainUniqueNamespaces() {
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
        Set<String> result = repository.getAllNamespaces();

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.contains(commonNamespace));
    }

    @Test
    void testGetAllNamespaces_WithSpecialCharacters_ShouldIncludeAllNamespaces() {
        // Arrange
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        
        String specialNamespace1 = "special.namespace-with_chars$123";
        String specialNamespace2 = "another.namespace@with#symbols%456";
        
        schema1.setNamespace(specialNamespace1);
        schema2.setNamespace(specialNamespace2);
        
        repository.addSchema(schema1, "/test/path/special1.xml");
        repository.addSchema(schema2, "/test/path/special2.xml");

        // Act
        Set<String> result = repository.getAllNamespaces();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(specialNamespace1));
        assertTrue(result.contains(specialNamespace2));
    }

    @Test
    void testGetAllNamespaces_ConcurrentAccess_ShouldBeThreadSafe() throws InterruptedException {
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
                    Set<String> namespaces = repository.getAllNamespaces();
                    if (namespaces.size() != 2 || 
                        !namespaces.contains(schema1.getNamespace()) ||
                        !namespaces.contains(schema2.getNamespace())) {
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
            assertTrue(result, "One or more threads failed to get all namespaces correctly");
        }
    }

    @Test
    void testGetAllNamespaces_ConsistentWithGetSchema_ShouldReturnAccessibleNamespaces() {
        // Arrange
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema1, "/test/path/simple-schema.xml");
        repository.addSchema(schema2, "/test/path/complex-schema.xml");

        // Act
        Set<String> allNamespaces = repository.getAllNamespaces();

        // Assert
        for (String namespace : allNamespaces) {
            CsdlSchema schema = repository.getSchema(namespace);
            assertNotNull(schema, "Schema should be accessible for namespace: " + namespace);
        }
    }

    @Test
    void testGetAllNamespaces_WithEmptyNamespaceSchemas_ShouldHandleGracefully() {
        // Note: This test assumes that schemas with null namespaces are not added
        // based on the addSchema implementation
        
        // Arrange
        CsdlSchema validSchema = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        CsdlSchema nullNamespaceSchema = new CsdlSchema();
        nullNamespaceSchema.setNamespace(null);
        
        repository.addSchema(validSchema, "/test/path/valid-schema.xml");
        repository.addSchema(nullNamespaceSchema, "/test/path/null-namespace-schema.xml");

        // Act
        Set<String> result = repository.getAllNamespaces();

        // Assert
        assertEquals(1, result.size()); // Only valid schema should be included
        assertTrue(result.contains(validSchema.getNamespace()));
        assertFalse(result.contains(null));
    }

    @Test
    void testGetAllNamespaces_OrderIndependent_ShouldContainAllNamespaces() {
        // Arrange
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        
        schema1.setNamespace("a.first.namespace");
        schema2.setNamespace("z.last.namespace");

        // Add in different orders
        repository.addSchema(schema2, "/test/path/last.xml");
        repository.addSchema(schema1, "/test/path/first.xml");

        // Act
        Set<String> result = repository.getAllNamespaces();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains("a.first.namespace"));
        assertTrue(result.contains("z.last.namespace"));
    }
}
