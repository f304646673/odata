package org.apache.olingo.schemamanager.repository.impl;

import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for InMemorySchemaRepository.getEntityTypes() method (by namespace).
 * Covers various scenarios for retrieving entity types from a specific namespace.
 */
class InMemorySchemaRepositoryTest_getEntityTypes {

    private InMemorySchemaRepository repository;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        repository = new InMemorySchemaRepository();
        System.out.println("Setting up test: " + testInfo.getDisplayName());
    }

    @Test
    void testGetEntityTypes_ExistingNamespace_ShouldReturnEntityTypes() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");

        // Act
        List<CsdlEntityType> result = repository.getEntityTypes(schema.getNamespace());

        // Assert
        assertNotNull(result);
        if (schema.getEntityTypes() != null && !schema.getEntityTypes().isEmpty()) {
            assertEquals(schema.getEntityTypes().size(), result.size());
            
            // Verify all entity types are included
            for (CsdlEntityType expectedEntityType : schema.getEntityTypes()) {
                assertTrue(result.stream().anyMatch(et -> et.getName().equals(expectedEntityType.getName())),
                          "Entity type should be found: " + expectedEntityType.getName());
            }
        } else {
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void testGetEntityTypes_NonExistingNamespace_ShouldReturnEmptyList() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");
        String nonExistingNamespace = "non.existing.namespace";

        // Act
        List<CsdlEntityType> result = repository.getEntityTypes(nonExistingNamespace);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetEntityTypes_NullNamespace_ShouldReturnEmptyList() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");

        // Act
        List<CsdlEntityType> result = repository.getEntityTypes(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetEntityTypes_EmptyNamespace_ShouldReturnEmptyList() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");

        // Act
        List<CsdlEntityType> result = repository.getEntityTypes("");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetEntityTypes_MultipleSchemas_ShouldReturnOnlyFromRequestedNamespace() {
        // Arrange
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema1, "/test/path/simple-schema.xml");
        repository.addSchema(schema2, "/test/path/complex-schema.xml");

        // Act
        List<CsdlEntityType> result1 = repository.getEntityTypes(schema1.getNamespace());
        List<CsdlEntityType> result2 = repository.getEntityTypes(schema2.getNamespace());

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        
        // Verify each result contains only types from the respective schema
        if (schema1.getEntityTypes() != null) {
            assertEquals(schema1.getEntityTypes().size(), result1.size());
        } else {
            assertTrue(result1.isEmpty());
        }
        
        if (schema2.getEntityTypes() != null) {
            assertEquals(schema2.getEntityTypes().size(), result2.size());
        } else {
            assertTrue(result2.isEmpty());
        }
    }

    @Test
    void testGetEntityTypes_EmptyRepository_ShouldReturnEmptyList() {
        // Act
        List<CsdlEntityType> result = repository.getEntityTypes("any.namespace");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetEntityTypes_SchemaWithoutEntityTypes_ShouldReturnEmptyList() {
        // Arrange
        CsdlSchema emptySchema = new CsdlSchema();
        emptySchema.setNamespace("test.empty.namespace");
        repository.addSchema(emptySchema, "/test/path/empty-schema.xml");

        // Act
        List<CsdlEntityType> result = repository.getEntityTypes(emptySchema.getNamespace());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetEntityTypes_AfterClear_ShouldReturnEmptyList() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");
        
        // Verify entity types exist before clear
        List<CsdlEntityType> beforeClear = repository.getEntityTypes(schema.getNamespace());
        if (schema.getEntityTypes() != null && !schema.getEntityTypes().isEmpty()) {
            assertFalse(beforeClear.isEmpty());
        }

        // Act
        repository.clear();
        List<CsdlEntityType> result = repository.getEntityTypes(schema.getNamespace());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetEntityTypes_ReturnedListIsIndependent_ShouldNotAffectRepository() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");

        // Act
        List<CsdlEntityType> result = repository.getEntityTypes(schema.getNamespace());
        result.clear(); // Modify the returned list

        // Assert - Repository should not be affected
        List<CsdlEntityType> repositoryEntityTypes = repository.getEntityTypes(schema.getNamespace());
        if (schema.getEntityTypes() != null && !schema.getEntityTypes().isEmpty()) {
            assertEquals(schema.getEntityTypes().size(), repositoryEntityTypes.size());
        } else {
            assertTrue(repositoryEntityTypes.isEmpty());
        }
    }

    @Test
    void testGetEntityTypes_ConcurrentAccess_ShouldBeThreadSafe() throws InterruptedException {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");
        
        final int expectedSize = schema.getEntityTypes() != null ? schema.getEntityTypes().size() : 0;
        final String namespace = schema.getNamespace();

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
                    List<CsdlEntityType> entityTypes = repository.getEntityTypes(namespace);
                    if (entityTypes == null || entityTypes.size() != expectedSize) {
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
            assertTrue(result, "One or more threads failed to get entity types correctly");
        }
    }

    @Test
    void testGetEntityTypes_ConsistentWithGetEntityType_ShouldReturnSameEntities() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");

        // Act
        List<CsdlEntityType> entityTypes = repository.getEntityTypes(schema.getNamespace());

        // Assert
        for (CsdlEntityType entityType : entityTypes) {
            String fullName = schema.getNamespace() + "." + entityType.getName();
            CsdlEntityType retrievedEntityType = repository.getEntityType(fullName);
            
            assertNotNull(retrievedEntityType, "Entity type should be accessible via getEntityType: " + fullName);
            assertEquals(entityType, retrievedEntityType);
        }
    }

    @Test
    void testGetEntityTypes_CaseSensitiveNamespace_ShouldReturnEmptyForWrongCase() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");
        
        String originalNamespace = schema.getNamespace();
        String upperCaseNamespace = originalNamespace.toUpperCase();

        // Assume namespaces are case-sensitive
        if (!originalNamespace.equals(upperCaseNamespace)) {
            // Act
            List<CsdlEntityType> result = repository.getEntityTypes(upperCaseNamespace);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void testGetEntityTypes_WithSpecialCharactersInNamespace_ShouldWork() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        String specialNamespace = "special.namespace-with_chars$123";
        schema.setNamespace(specialNamespace);
        repository.addSchema(schema, "/test/path/special-schema.xml");

        // Act
        List<CsdlEntityType> result = repository.getEntityTypes(specialNamespace);

        // Assert
        assertNotNull(result);
        if (schema.getEntityTypes() != null && !schema.getEntityTypes().isEmpty()) {
            assertEquals(schema.getEntityTypes().size(), result.size());
        } else {
            assertTrue(result.isEmpty());
        }
    }
}
