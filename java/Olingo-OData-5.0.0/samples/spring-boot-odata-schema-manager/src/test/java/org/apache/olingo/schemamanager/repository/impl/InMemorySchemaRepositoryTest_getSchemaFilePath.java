package org.apache.olingo.schemamanager.repository.impl;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for InMemorySchemaRepository.getSchemaFilePath() method.
 * Covers various scenarios for retrieving schema file paths from the repository.
 */
class InMemorySchemaRepositoryTest_getSchemaFilePath {

    private InMemorySchemaRepository repository;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        repository = new InMemorySchemaRepository();
        System.out.println("Setting up test: " + testInfo.getDisplayName());
    }

    @Test
    void testGetSchemaFilePath_ExistingNamespace_ShouldReturnFilePath() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        String expectedFilePath = "/test/path/simple-schema.xml";
        repository.addSchema(schema, expectedFilePath);

        // Act
        String result = repository.getSchemaFilePath(schema.getNamespace());

        // Assert
        assertNotNull(result);
        assertEquals(expectedFilePath, result);
    }

    @Test
    void testGetSchemaFilePath_NonExistingNamespace_ShouldReturnNull() {
        // Arrange
        String nonExistingNamespace = "non.existing.namespace";

        // Act
        String result = repository.getSchemaFilePath(nonExistingNamespace);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetSchemaFilePath_NullNamespace_ShouldReturnNull() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        repository.addSchema(schema, "/test/path/simple-schema.xml");

        // Act
        String result = repository.getSchemaFilePath(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetSchemaFilePath_EmptyNamespace_ShouldReturnNull() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        repository.addSchema(schema, "/test/path/simple-schema.xml");

        // Act
        String result = repository.getSchemaFilePath("");

        // Assert
        assertNull(result);
    }

    @Test
    void testGetSchemaFilePath_MultipleSchemas_ShouldReturnCorrectPath() {
        // Arrange
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        String filePath1 = "/test/path/simple-schema.xml";
        String filePath2 = "/test/path/complex-schema.xml";
        
        repository.addSchema(schema1, filePath1);
        repository.addSchema(schema2, filePath2);

        // Act
        String result1 = repository.getSchemaFilePath(schema1.getNamespace());
        String result2 = repository.getSchemaFilePath(schema2.getNamespace());

        // Assert
        assertEquals(filePath1, result1);
        assertEquals(filePath2, result2);
        assertNotEquals(result1, result2);
    }

    @Test
    void testGetSchemaFilePath_AfterClear_ShouldReturnNull() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        String filePath = "/test/path/simple-schema.xml";
        repository.addSchema(schema, filePath);
        
        // Verify it exists first
        assertNotNull(repository.getSchemaFilePath(schema.getNamespace()));

        // Act
        repository.clear();
        String result = repository.getSchemaFilePath(schema.getNamespace());

        // Assert
        assertNull(result);
    }

    @Test
    void testGetSchemaFilePath_WithSpecialCharactersInPath_ShouldWork() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        String specialFilePath = "/test/path with spaces/schema-file_with$special&chars.xml";
        repository.addSchema(schema, specialFilePath);

        // Act
        String result = repository.getSchemaFilePath(schema.getNamespace());

        // Assert
        assertNotNull(result);
        assertEquals(specialFilePath, result);
    }

    @Test
    void testGetSchemaFilePath_WithAbsolutePath_ShouldReturnExactPath() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        String absoluteFilePath = "C:\\Users\\Test\\Documents\\schemas\\test-schema.xml";
        repository.addSchema(schema, absoluteFilePath);

        // Act
        String result = repository.getSchemaFilePath(schema.getNamespace());

        // Assert
        assertEquals(absoluteFilePath, result);
    }

    @Test
    void testGetSchemaFilePath_WithRelativePath_ShouldReturnExactPath() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        String relativeFilePath = "./schemas/test-schema.xml";
        repository.addSchema(schema, relativeFilePath);

        // Act
        String result = repository.getSchemaFilePath(schema.getNamespace());

        // Assert
        assertEquals(relativeFilePath, result);
    }

    @Test
    void testGetSchemaFilePath_AfterSchemaOverwrite_ShouldReturnLatestPath() {
        // Arrange
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        
        // Force same namespace
        String commonNamespace = "test.overwrite.namespace";
        schema1.setNamespace(commonNamespace);
        schema2.setNamespace(commonNamespace);
        
        String firstPath = "/test/path/first.xml";
        String secondPath = "/test/path/second.xml";

        repository.addSchema(schema1, firstPath);
        repository.addSchema(schema2, secondPath);

        // Act
        String result = repository.getSchemaFilePath(commonNamespace);

        // Assert
        assertEquals(secondPath, result); // Latest path should be returned
        assertNotEquals(firstPath, result);
    }

    @Test
    void testGetSchemaFilePath_ConcurrentAccess_ShouldBeThreadSafe() throws InterruptedException {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        String expectedFilePath = "/test/path/simple-schema.xml";
        repository.addSchema(schema, expectedFilePath);

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
                    String result = repository.getSchemaFilePath(schema.getNamespace());
                    if (!expectedFilePath.equals(result)) {
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
            assertTrue(result, "One or more threads failed to get schema file path correctly");
        }
    }

    @Test
    void testGetSchemaFilePath_CaseSensitiveNamespace_ShouldReturnNullForWrongCase() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        String filePath = "/test/path/simple-schema.xml";
        repository.addSchema(schema, filePath);
        
        String originalNamespace = schema.getNamespace();
        String upperCaseNamespace = originalNamespace.toUpperCase();

        // Assume namespaces are case-sensitive
        if (!originalNamespace.equals(upperCaseNamespace)) {
            // Act
            String result = repository.getSchemaFilePath(upperCaseNamespace);

            // Assert
            assertNull(result);
        }
    }

    @Test
    void testGetSchemaFilePath_WithEmptyFilePath_ShouldReturnEmpty() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        String emptyFilePath = "";
        repository.addSchema(schema, emptyFilePath);

        // Act
        String result = repository.getSchemaFilePath(schema.getNamespace());

        // Assert
        assertEquals(emptyFilePath, result);
    }

    @Test
    void testGetSchemaFilePath_WithNullFilePath_ShouldReturnNull() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        repository.addSchema(schema, null);

        // Act
        String result = repository.getSchemaFilePath(schema.getNamespace());

        // Assert
        assertNull(result);
    }
}
