package org.apache.olingo.schemamanager.repository.impl;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.repository.SchemaRepository;
import org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for InMemorySchemaRepository.getStatistics() method.
 * Covers various scenarios for retrieving repository statistics.
 */
class InMemorySchemaRepositoryTest_getStatistics {

    private InMemorySchemaRepository repository;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        repository = new InMemorySchemaRepository();
        System.out.println("Setting up test: " + testInfo.getDisplayName());
    }

    @Test
    void testGetStatistics_EmptyRepository_ShouldReturnZeroStatistics() {
        // Act
        SchemaRepository.RepositoryStatistics stats = repository.getStatistics();

        // Assert
        assertNotNull(stats);
        assertEquals(0, stats.getTotalSchemas());
        assertEquals(0, stats.getTotalEntityTypes());
        assertEquals(0, stats.getTotalComplexTypes());
        assertEquals(0, stats.getTotalEnumTypes());
        assertEquals(0, stats.getTotalEntityContainers());
        assertEquals(0, stats.getTotalActions());
        assertEquals(0, stats.getTotalFunctions());
    }

    @Test
    void testGetStatistics_SingleSchemaWithContent_ShouldReturnCorrectCounts() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");

        // Act
        SchemaRepository.RepositoryStatistics stats = repository.getStatistics();

        // Assert
        assertNotNull(stats);
        assertEquals(1, stats.getTotalSchemas());
        
        // Verify counts match schema content
        int expectedEntityTypes = schema.getEntityTypes() != null ? schema.getEntityTypes().size() : 0;
        int expectedComplexTypes = schema.getComplexTypes() != null ? schema.getComplexTypes().size() : 0;
        int expectedEnumTypes = schema.getEnumTypes() != null ? schema.getEnumTypes().size() : 0;
        int expectedContainers = schema.getEntityContainer() != null ? 1 : 0;
        int expectedActions = schema.getActions() != null ? schema.getActions().size() : 0;
        int expectedFunctions = schema.getFunctions() != null ? schema.getFunctions().size() : 0;

        assertEquals(expectedEntityTypes, stats.getTotalEntityTypes());
        assertEquals(expectedComplexTypes, stats.getTotalComplexTypes());
        assertEquals(expectedEnumTypes, stats.getTotalEnumTypes());
        assertEquals(expectedContainers, stats.getTotalEntityContainers());
        assertEquals(expectedActions, stats.getTotalActions());
        assertEquals(expectedFunctions, stats.getTotalFunctions());
    }

    @Test
    void testGetStatistics_MultipleSchemas_ShouldAggregateCorrectly() {
        // Arrange
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema1, "/test/path/simple-schema.xml");
        repository.addSchema(schema2, "/test/path/complex-schema.xml");

        // Act
        SchemaRepository.RepositoryStatistics stats = repository.getStatistics();

        // Assert
        assertNotNull(stats);
        assertEquals(2, stats.getTotalSchemas());
        
        // Calculate expected totals
        int expectedEntityTypes = 0;
        int expectedComplexTypes = 0;
        int expectedEnumTypes = 0;
        int expectedContainers = 0;
        int expectedActions = 0;
        int expectedFunctions = 0;
        
        for (CsdlSchema schema : new CsdlSchema[]{schema1, schema2}) {
            expectedEntityTypes += schema.getEntityTypes() != null ? schema.getEntityTypes().size() : 0;
            expectedComplexTypes += schema.getComplexTypes() != null ? schema.getComplexTypes().size() : 0;
            expectedEnumTypes += schema.getEnumTypes() != null ? schema.getEnumTypes().size() : 0;
            expectedContainers += schema.getEntityContainer() != null ? 1 : 0;
            expectedActions += schema.getActions() != null ? schema.getActions().size() : 0;
            expectedFunctions += schema.getFunctions() != null ? schema.getFunctions().size() : 0;
        }

        assertEquals(expectedEntityTypes, stats.getTotalEntityTypes());
        assertEquals(expectedComplexTypes, stats.getTotalComplexTypes());
        assertEquals(expectedEnumTypes, stats.getTotalEnumTypes());
        assertEquals(expectedContainers, stats.getTotalEntityContainers());
        assertEquals(expectedActions, stats.getTotalActions());
        assertEquals(expectedFunctions, stats.getTotalFunctions());
    }

    @Test
    void testGetStatistics_AfterClear_ShouldReturnZeroStatistics() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");
        
        // Verify non-zero statistics before clear
        SchemaRepository.RepositoryStatistics statsBefore = repository.getStatistics();
        assertTrue(statsBefore.getTotalSchemas() > 0);

        // Act
        repository.clear();
        SchemaRepository.RepositoryStatistics statsAfter = repository.getStatistics();

        // Assert
        assertEquals(0, statsAfter.getTotalSchemas());
        assertEquals(0, statsAfter.getTotalEntityTypes());
        assertEquals(0, statsAfter.getTotalComplexTypes());
        assertEquals(0, statsAfter.getTotalEnumTypes());
        assertEquals(0, statsAfter.getTotalEntityContainers());
        assertEquals(0, statsAfter.getTotalActions());
        assertEquals(0, statsAfter.getTotalFunctions());
    }

    @Test
    void testGetStatistics_AfterSchemaOverwrite_ShouldReflectLatestState() {
        // Arrange
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        
        // Force same namespace to test overwrite
        String commonNamespace = "test.overwrite.namespace";
        schema1.setNamespace(commonNamespace);
        schema2.setNamespace(commonNamespace);

        repository.addSchema(schema1, "/test/path/first.xml");
        repository.addSchema(schema2, "/test/path/second.xml");

        // Act
        SchemaRepository.RepositoryStatistics stats = repository.getStatistics();

        // Assert
        assertEquals(1, stats.getTotalSchemas()); // Only one schema (overwritten)
        
        // Should reflect schema2 content only
        int expectedEntityTypes = schema2.getEntityTypes() != null ? schema2.getEntityTypes().size() : 0;
        int expectedComplexTypes = schema2.getComplexTypes() != null ? schema2.getComplexTypes().size() : 0;
        
        assertEquals(expectedEntityTypes, stats.getTotalEntityTypes());
        assertEquals(expectedComplexTypes, stats.getTotalComplexTypes());
    }

    @Test
    void testGetStatistics_WithEmptySchema_ShouldCountSchemaButNotTypes() {
        // Arrange
        CsdlSchema emptySchema = new CsdlSchema();
        emptySchema.setNamespace("test.empty.namespace");
        repository.addSchema(emptySchema, "/test/path/empty-schema.xml");

        // Act
        SchemaRepository.RepositoryStatistics stats = repository.getStatistics();

        // Assert
        assertEquals(1, stats.getTotalSchemas());
        assertEquals(0, stats.getTotalEntityTypes());
        assertEquals(0, stats.getTotalComplexTypes());
        assertEquals(0, stats.getTotalEnumTypes());
        assertEquals(0, stats.getTotalEntityContainers());
        assertEquals(0, stats.getTotalActions());
        assertEquals(0, stats.getTotalFunctions());
    }

    @Test
    void testGetStatistics_ConcurrentAccess_ShouldBeThreadSafe() throws InterruptedException {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");

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
                    SchemaRepository.RepositoryStatistics stats = repository.getStatistics();
                    if (stats == null || stats.getTotalSchemas() != 1) {
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
            assertTrue(result, "One or more threads failed to get statistics correctly");
        }
    }

    @Test
    void testGetStatistics_ConsistentWithRepositoryState_ShouldMatchActualCounts() {
        // Arrange
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema1, "/test/path/simple-schema.xml");
        repository.addSchema(schema2, "/test/path/complex-schema.xml");

        // Act
        SchemaRepository.RepositoryStatistics stats = repository.getStatistics();

        // Assert - Statistics should match actual repository state
        assertEquals(repository.getAllSchemas().size(), stats.getTotalSchemas());
        assertEquals(repository.getAllNamespaces().size(), stats.getTotalSchemas());
        
        // Verify entity types count
        int actualEntityTypeCount = 0;
        for (String namespace : repository.getAllNamespaces()) {
            actualEntityTypeCount += repository.getEntityTypes(namespace).size();
        }
        assertEquals(actualEntityTypeCount, stats.getTotalEntityTypes());

        // Verify complex types count
        int actualComplexTypeCount = 0;
        for (String namespace : repository.getAllNamespaces()) {
            actualComplexTypeCount += repository.getComplexTypes(namespace).size();
        }
        assertEquals(actualComplexTypeCount, stats.getTotalComplexTypes());
    }

    @Test
    void testGetStatistics_ReturnedObjectIsImmutable_ShouldNotAffectRepository() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");

        // Act
        SchemaRepository.RepositoryStatistics stats1 = repository.getStatistics();
        SchemaRepository.RepositoryStatistics stats2 = repository.getStatistics();

        // Assert - Should get consistent results
        assertEquals(stats1.getTotalSchemas(), stats2.getTotalSchemas());
        assertEquals(stats1.getTotalEntityTypes(), stats2.getTotalEntityTypes());
        assertEquals(stats1.getTotalComplexTypes(), stats2.getTotalComplexTypes());
        assertEquals(stats1.getTotalEnumTypes(), stats2.getTotalEnumTypes());
        assertEquals(stats1.getTotalEntityContainers(), stats2.getTotalEntityContainers());
        assertEquals(stats1.getTotalActions(), stats2.getTotalActions());
        assertEquals(stats1.getTotalFunctions(), stats2.getTotalFunctions());
    }
}
