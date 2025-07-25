package org.apache.olingo.schemamanager.repository.impl;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * Tests for InMemorySchemaRepository.getSchema() method.
 * Covers various scenarios for retrieving schemas from the repository.
 */
class InMemorySchemaRepositoryTest_getSchema {

    private InMemorySchemaRepository repository;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        repository = new InMemorySchemaRepository();
        System.out.println("Setting up test: " + testInfo.getDisplayName());
    }

    @Test
    void testGetSchema_ExistingNamespace_ShouldReturnSchema() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        String filePath = "/test/path/simple-schema.xml";
        repository.addSchema(schema, filePath);

        // Act
        CsdlSchema result = repository.getSchema(schema.getNamespace());

        // Assert
        assertNotNull(result);
        assertEquals(schema, result);
        assertEquals(schema.getNamespace(), result.getNamespace());
    }

    @Test
    void testGetSchema_NonExistingNamespace_ShouldReturnNull() {
        // Arrange
        String nonExistingNamespace = "non.existing.namespace";

        // Act
        CsdlSchema result = repository.getSchema(nonExistingNamespace);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetSchema_NullNamespace_ShouldReturnNull() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        repository.addSchema(schema, "/test/path/simple-schema.xml");

        // Act
        CsdlSchema result = repository.getSchema(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetSchema_EmptyNamespace_ShouldReturnNull() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        repository.addSchema(schema, "/test/path/simple-schema.xml");

        // Act
        CsdlSchema result = repository.getSchema("");

        // Assert
        assertNull(result);
    }

    @Test
    void testGetSchema_MultipleSchemas_ShouldReturnCorrectOne() {
        // Arrange
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema1, "/test/path/simple-schema.xml");
        repository.addSchema(schema2, "/test/path/complex-schema.xml");

        // Act
        CsdlSchema result1 = repository.getSchema(schema1.getNamespace());
        CsdlSchema result2 = repository.getSchema(schema2.getNamespace());

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(schema1, result1);
        assertEquals(schema2, result2);
        assertNotEquals(result1, result2);
    }

    @Test
    void testGetSchema_AfterClear_ShouldReturnNull() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        repository.addSchema(schema, "/test/path/simple-schema.xml");
        
        // Verify it exists first
        assertNotNull(repository.getSchema(schema.getNamespace()));

        // Act
        repository.clear();
        CsdlSchema result = repository.getSchema(schema.getNamespace());

        // Assert
        assertNull(result);
    }

    @Test
    void testGetSchema_CaseSensitiveNamespace_ShouldReturnNull() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        repository.addSchema(schema, "/test/path/simple-schema.xml");
        String originalNamespace = schema.getNamespace();
        String upperCaseNamespace = originalNamespace.toUpperCase();

        // Assume namespaces are case-sensitive
        if (!originalNamespace.equals(upperCaseNamespace)) {
            // Act
            CsdlSchema result = repository.getSchema(upperCaseNamespace);

            // Assert
            assertNull(result);
        }
    }

    @Test
    void testGetSchema_WithSpecialCharacters_ShouldWork() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        String specialNamespace = "test.namespace-with_special.chars$123";
        schema.setNamespace(specialNamespace);
        repository.addSchema(schema, "/test/path/special-schema.xml");

        // Act
        CsdlSchema result = repository.getSchema(specialNamespace);

        // Assert
        assertNotNull(result);
        assertEquals(schema, result);
        assertEquals(specialNamespace, result.getNamespace());
    }

    @Test
    void testGetSchema_ConcurrentAccess_ShouldBeThreadSafe() throws InterruptedException {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        repository.addSchema(schema, "/test/path/simple-schema.xml");
        
        final int numberOfThreads = 10;
        final int operationsPerThread = 100;
        Thread[] threads = new Thread[numberOfThreads];
        final boolean[] results = new boolean[numberOfThreads];

        // Act
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                boolean allSuccessful = true;
                for (int j = 0; j < operationsPerThread; j++) {
                    CsdlSchema result = repository.getSchema(schema.getNamespace());
                    if (result == null || !result.equals(schema)) {
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
            assertTrue(result, "One or more threads failed to get schema correctly");
        }
    }

    @Test
    void testGetSchema_AfterOverwrite_ShouldReturnLatest() {
        // Arrange
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        
        // Force same namespace
        String commonNamespace = "test.overwrite.namespace";
        schema1.setNamespace(commonNamespace);
        schema2.setNamespace(commonNamespace);
        
        repository.addSchema(schema1, "/test/path/first.xml");

        // Act
        repository.addSchema(schema2, "/test/path/second.xml");
        CsdlSchema result = repository.getSchema(commonNamespace);

        // Assert
        assertNotNull(result);
        assertEquals(schema2, result);
        assertNotEquals(schema1, result);
    }
}
