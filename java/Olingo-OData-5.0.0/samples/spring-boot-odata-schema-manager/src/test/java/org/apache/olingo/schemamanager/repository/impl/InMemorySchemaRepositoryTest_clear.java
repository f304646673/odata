package org.apache.olingo.schemamanager.repository.impl;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for InMemorySchemaRepository.clear() method.
 * Covers various scenarios for clearing the repository.
 */
class InMemorySchemaRepositoryTest_clear {

    private InMemorySchemaRepository repository;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        repository = new InMemorySchemaRepository();
        System.out.println("Setting up test: " + testInfo.getDisplayName());
    }

    @Test
    void testClear_EmptyRepository_ShouldHandleGracefully() {
        // Act
        assertDoesNotThrow(() -> repository.clear());

        // Assert
        assertTrue(repository.getAllSchemas().isEmpty());
        assertTrue(repository.getAllNamespaces().isEmpty());
        
        // Verify statistics
        InMemorySchemaRepository.RepositoryStatistics stats = repository.getStatistics();
        assertEquals(0, stats.getTotalSchemas());
        assertEquals(0, stats.getTotalEntityTypes());
        assertEquals(0, stats.getTotalComplexTypes());
        assertEquals(0, stats.getTotalEnumTypes());
    }

    @Test
    void testClear_SingleSchema_ShouldRemoveEverything() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");
        
        // Verify schema was added
        assertNotNull(repository.getSchema(schema.getNamespace()));
        assertFalse(repository.getAllSchemas().isEmpty());

        // Act
        repository.clear();

        // Assert
        assertNull(repository.getSchema(schema.getNamespace()));
        assertTrue(repository.getAllSchemas().isEmpty());
        assertTrue(repository.getAllNamespaces().isEmpty());
        assertNull(repository.getSchemaFilePath(schema.getNamespace()));
        
        // Verify all indexed types are cleared
        if (schema.getEntityTypes() != null) {
            for (CsdlEntityType entityType : schema.getEntityTypes()) {
                String fullName = schema.getNamespace() + "." + entityType.getName();
                assertNull(repository.getEntityType(fullName));
            }
        }
        
        if (schema.getComplexTypes() != null) {
            for (CsdlComplexType complexType : schema.getComplexTypes()) {
                String fullName = schema.getNamespace() + "." + complexType.getName();
                assertNull(repository.getComplexType(fullName));
            }
        }
    }

    @Test
    void testClear_MultipleSchemas_ShouldRemoveAll() {
        // Arrange
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema1, "/test/path/simple-schema.xml");
        repository.addSchema(schema2, "/test/path/complex-schema.xml");
        
        // Verify schemas were added
        assertEquals(2, repository.getAllSchemas().size());
        assertEquals(2, repository.getAllNamespaces().size());

        // Act
        repository.clear();

        // Assert
        assertTrue(repository.getAllSchemas().isEmpty());
        assertTrue(repository.getAllNamespaces().isEmpty());
        assertNull(repository.getSchema(schema1.getNamespace()));
        assertNull(repository.getSchema(schema2.getNamespace()));
        assertNull(repository.getSchemaFilePath(schema1.getNamespace()));
        assertNull(repository.getSchemaFilePath(schema2.getNamespace()));
    }

    @Test
    void testClear_WithComplexContent_ShouldClearAllIndexes() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");
        
        // Store references before clear
        String namespace = schema.getNamespace();

        // Act
        repository.clear();

        // Assert - All type indexes should be cleared
        assertTrue(repository.getEntityTypes(namespace).isEmpty());
        assertTrue(repository.getComplexTypes(namespace).isEmpty());
        assertTrue(repository.getEnumTypes(namespace).isEmpty());
        assertTrue(repository.getActions(namespace).isEmpty());
        assertTrue(repository.getFunctions(namespace).isEmpty());
        
        // Verify statistics are all zero
        InMemorySchemaRepository.RepositoryStatistics stats = repository.getStatistics();
        assertEquals(0, stats.getTotalSchemas());
        assertEquals(0, stats.getTotalEntityTypes());
        assertEquals(0, stats.getTotalComplexTypes());
        assertEquals(0, stats.getTotalEnumTypes());
        assertEquals(0, stats.getTotalEntityContainers());
        assertEquals(0, stats.getTotalActions());
        assertEquals(0, stats.getTotalFunctions());
    }

    @Test
    void testClear_AfterClear_ShouldAllowNewSchemas() {
        // Arrange
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        repository.addSchema(schema1, "/test/path/simple-schema.xml");
        repository.clear();

        // Act - Add new schema after clear
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema2, "/test/path/complex-schema.xml");

        // Assert
        assertEquals(1, repository.getAllSchemas().size());
        assertEquals(1, repository.getAllNamespaces().size());
        assertNotNull(repository.getSchema(schema2.getNamespace()));
        assertNull(repository.getSchema(schema1.getNamespace())); // Old schema should not exist
    }

    @Test
    void testClear_ConcurrentAccess_ShouldBeThreadSafe() throws InterruptedException {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");

        final int numberOfThreads = 10;
        Thread[] threads = new Thread[numberOfThreads];
        final boolean[] results = new boolean[numberOfThreads];

        // Act - Multiple threads calling clear simultaneously
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                try {
                    repository.clear();
                    results[threadIndex] = true;
                } catch (Exception e) {
                    results[threadIndex] = false;
                }
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
            assertTrue(result, "Clear operation should complete successfully in all threads");
        }
        
        // Repository should be empty after all clears
        assertTrue(repository.getAllSchemas().isEmpty());
        assertTrue(repository.getAllNamespaces().isEmpty());
    }

    @Test
    void testClear_MultipleClearCalls_ShouldBeIdempotent() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");

        // Act - Multiple clear calls
        repository.clear();
        repository.clear();
        repository.clear();

        // Assert - Should still be empty
        assertTrue(repository.getAllSchemas().isEmpty());
        assertTrue(repository.getAllNamespaces().isEmpty());
        
        InMemorySchemaRepository.RepositoryStatistics stats = repository.getStatistics();
        assertEquals(0, stats.getTotalSchemas());
    }

    @Test
    void testClear_WithStatistics_ShouldResetCounters() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");
        
        // Verify non-zero statistics before clear
        InMemorySchemaRepository.RepositoryStatistics statsBefore = repository.getStatistics();
        assertTrue(statsBefore.getTotalSchemas() > 0);

        // Act
        repository.clear();

        // Assert
        InMemorySchemaRepository.RepositoryStatistics statsAfter = repository.getStatistics();
        assertEquals(0, statsAfter.getTotalSchemas());
        assertEquals(0, statsAfter.getTotalEntityTypes());
        assertEquals(0, statsAfter.getTotalComplexTypes());
        assertEquals(0, statsAfter.getTotalEnumTypes());
        assertEquals(0, statsAfter.getTotalEntityContainers());
        assertEquals(0, statsAfter.getTotalActions());
        assertEquals(0, statsAfter.getTotalFunctions());
    }

    @Test
    void testClear_ThenAddSameSchema_ShouldWorkCorrectly() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        String filePath = "/test/path/complex-schema.xml";
        
        repository.addSchema(schema, filePath);
        String originalNamespace = schema.getNamespace();

        // Act
        repository.clear();
        repository.addSchema(schema, filePath);

        // Assert
        assertNotNull(repository.getSchema(originalNamespace));
        assertEquals(schema, repository.getSchema(originalNamespace));
        assertEquals(filePath, repository.getSchemaFilePath(originalNamespace));
        assertEquals(1, repository.getAllSchemas().size());
        assertEquals(1, repository.getAllNamespaces().size());
    }
}
