package org.apache.olingo.schemamanager.repository.impl;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for InMemorySchemaRepository.getEntityType() method (with namespace and typeName).
 * Covers various scenarios for retrieving entity types by namespace and type name.
 */
class InMemorySchemaRepositoryTest_getEntityType_ByNamespace {

    private InMemorySchemaRepository repository;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        repository = new InMemorySchemaRepository();
        System.out.println("Setting up test: " + testInfo.getDisplayName());
    }

    @Test
    void testGetEntityType_ExistingNamespaceAndTypeName_ShouldReturnEntityType() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");
        
        if (schema.getEntityTypes() != null && !schema.getEntityTypes().isEmpty()) {
            CsdlEntityType expectedEntityType = schema.getEntityTypes().get(0);

            // Act
            CsdlEntityType result = repository.getEntityType(schema.getNamespace(), expectedEntityType.getName());

            // Assert
            assertNotNull(result);
            assertEquals(expectedEntityType, result);
            assertEquals(expectedEntityType.getName(), result.getName());
        }
    }

    @Test
    void testGetEntityType_NonExistingNamespace_ShouldReturnNull() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");
        
        if (schema.getEntityTypes() != null && !schema.getEntityTypes().isEmpty()) {
            CsdlEntityType entityType = schema.getEntityTypes().get(0);

            // Act
            CsdlEntityType result = repository.getEntityType("non.existing.namespace", entityType.getName());

            // Assert
            assertNull(result);
        }
    }

    @Test
    void testGetEntityType_NonExistingTypeName_ShouldReturnNull() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");

        // Act
        CsdlEntityType result = repository.getEntityType(schema.getNamespace(), "NonExistingTypeName");

        // Assert
        assertNull(result);
    }

    @Test
    void testGetEntityType_NullNamespace_ShouldReturnNull() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");
        
        if (schema.getEntityTypes() != null && !schema.getEntityTypes().isEmpty()) {
            CsdlEntityType entityType = schema.getEntityTypes().get(0);

            // Act
            CsdlEntityType result = repository.getEntityType(null, entityType.getName());

            // Assert
            assertNull(result);
        }
    }

    @Test
    void testGetEntityType_NullTypeName_ShouldReturnNull() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");

        // Act
        CsdlEntityType result = repository.getEntityType(schema.getNamespace(), null);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetEntityType_EmptyNamespace_ShouldReturnNull() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");
        
        if (schema.getEntityTypes() != null && !schema.getEntityTypes().isEmpty()) {
            CsdlEntityType entityType = schema.getEntityTypes().get(0);

            // Act
            CsdlEntityType result = repository.getEntityType("", entityType.getName());

            // Assert
            assertNull(result);
        }
    }

    @Test
    void testGetEntityType_EmptyTypeName_ShouldReturnNull() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");

        // Act
        CsdlEntityType result = repository.getEntityType(schema.getNamespace(), "");

        // Assert
        assertNull(result);
    }

    @Test
    void testGetEntityType_ConsistentWithFullQualifiedName_ShouldReturnSameResult() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");
        
        if (schema.getEntityTypes() != null && !schema.getEntityTypes().isEmpty()) {
            CsdlEntityType entityType = schema.getEntityTypes().get(0);
            String namespace = schema.getNamespace();
            String typeName = entityType.getName();
            String fullQualifiedName = namespace + "." + typeName;

            // Act
            CsdlEntityType resultByParts = repository.getEntityType(namespace, typeName);
            CsdlEntityType resultByFullName = repository.getEntityType(fullQualifiedName);

            // Assert
            assertEquals(resultByParts, resultByFullName);
            if (resultByParts != null) {
                assertEquals(entityType, resultByParts);
            }
        }
    }

    @Test
    void testGetEntityType_CaseSensitive_ShouldReturnNullForWrongCase() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");
        
        if (schema.getEntityTypes() != null && !schema.getEntityTypes().isEmpty()) {
            CsdlEntityType entityType = schema.getEntityTypes().get(0);
            String originalNamespace = schema.getNamespace();
            String originalTypeName = entityType.getName();
            
            String wrongCaseNamespace = originalNamespace.toUpperCase();
            String wrongCaseTypeName = originalTypeName.toUpperCase();

            // Assume case-sensitive
            if (!originalNamespace.equals(wrongCaseNamespace) || !originalTypeName.equals(wrongCaseTypeName)) {
                // Act
                CsdlEntityType result1 = repository.getEntityType(wrongCaseNamespace, originalTypeName);
                CsdlEntityType result2 = repository.getEntityType(originalNamespace, wrongCaseTypeName);

                // Assert
                assertNull(result1);
                assertNull(result2);
            }
        }
    }

    @Test
    void testGetEntityType_AfterClear_ShouldReturnNull() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");
        
        if (schema.getEntityTypes() != null && !schema.getEntityTypes().isEmpty()) {
            CsdlEntityType entityType = schema.getEntityTypes().get(0);
            String namespace = schema.getNamespace();
            String typeName = entityType.getName();
            
            // Verify it exists first
            assertNotNull(repository.getEntityType(namespace, typeName));

            // Act
            repository.clear();
            CsdlEntityType result = repository.getEntityType(namespace, typeName);

            // Assert
            assertNull(result);
        }
    }

    @Test
    void testGetEntityType_ConcurrentAccess_ShouldBeThreadSafe() throws InterruptedException {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");
        
        if (schema.getEntityTypes() != null && !schema.getEntityTypes().isEmpty()) {
            CsdlEntityType expectedEntityType = schema.getEntityTypes().get(0);
            String namespace = schema.getNamespace();
            String typeName = expectedEntityType.getName();

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
                        CsdlEntityType result = repository.getEntityType(namespace, typeName);
                        if (result == null || !result.equals(expectedEntityType)) {
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
                assertTrue(result, "One or more threads failed to get entity type correctly");
            }
        }
    }

    @Test
    void testGetEntityType_MultipleEntityTypesInSchema_ShouldReturnCorrectOne() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");

        // Act & Assert
        if (schema.getEntityTypes() != null && schema.getEntityTypes().size() > 1) {
            for (CsdlEntityType entityType : schema.getEntityTypes()) {
                CsdlEntityType result = repository.getEntityType(schema.getNamespace(), entityType.getName());
                
                assertNotNull(result, "Entity type should be found: " + entityType.getName());
                assertEquals(entityType, result);
                assertEquals(entityType.getName(), result.getName());
            }
        }
    }
}
