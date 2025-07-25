package org.apache.olingo.schemamanager.repository.impl;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * Tests for InMemorySchemaRepository.getEntityType() method (with full qualified name).
 * Covers various scenarios for retrieving entity types from the repository.
 */
class InMemorySchemaRepositoryTest_getEntityType {

    private InMemorySchemaRepository repository;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        repository = new InMemorySchemaRepository();
        System.out.println("Setting up test: " + testInfo.getDisplayName());
    }

    @Test
    void testGetEntityType_ExistingFullQualifiedName_ShouldReturnEntityType() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");
        
        if (schema.getEntityTypes() != null && !schema.getEntityTypes().isEmpty()) {
            CsdlEntityType expectedEntityType = schema.getEntityTypes().get(0);
            String fullQualifiedName = schema.getNamespace() + "." + expectedEntityType.getName();

            // Act
            CsdlEntityType result = repository.getEntityType(fullQualifiedName);

            // Assert
            assertNotNull(result);
            assertEquals(expectedEntityType, result);
            assertEquals(expectedEntityType.getName(), result.getName());
        }
    }

    @Test
    void testGetEntityType_NonExistingFullQualifiedName_ShouldReturnNull() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");
        String nonExistingName = "non.existing.namespace.NonExistingType";

        // Act
        CsdlEntityType result = repository.getEntityType(nonExistingName);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetEntityType_NullFullQualifiedName_ShouldReturnNull() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");

        // Act
        CsdlEntityType result = repository.getEntityType((String) null);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetEntityType_EmptyFullQualifiedName_ShouldReturnNull() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");

        // Act
        CsdlEntityType result = repository.getEntityType("");

        // Assert
        assertNull(result);
    }

    @Test
    void testGetEntityType_InvalidFormat_ShouldReturnNull() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");

        // Act & Assert
        assertNull(repository.getEntityType("InvalidFormat"));
        assertNull(repository.getEntityType("namespace."));
        assertNull(repository.getEntityType(".TypeName"));
        assertNull(repository.getEntityType("namespace..TypeName"));
    }

    @Test
    void testGetEntityType_CaseSensitive_ShouldReturnNullForWrongCase() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");
        
        if (schema.getEntityTypes() != null && !schema.getEntityTypes().isEmpty()) {
            CsdlEntityType entityType = schema.getEntityTypes().get(0);
            String correctName = schema.getNamespace() + "." + entityType.getName();
            String wrongCaseName = correctName.toUpperCase();

            // Assume case-sensitive
            if (!correctName.equals(wrongCaseName)) {
                // Act
                CsdlEntityType result = repository.getEntityType(wrongCaseName);

                // Assert
                assertNull(result);
            }
        }
    }

    @Test
    void testGetEntityType_MultipleSchemas_ShouldReturnCorrectEntityType() {
        // Arrange
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema1, "/test/path/simple-schema.xml");
        repository.addSchema(schema2, "/test/path/complex-schema.xml");

        // Act & Assert for each schema
        if (schema1.getEntityTypes() != null && !schema1.getEntityTypes().isEmpty()) {
            CsdlEntityType entityType1 = schema1.getEntityTypes().get(0);
            String fullName1 = schema1.getNamespace() + "." + entityType1.getName();
            assertEquals(entityType1, repository.getEntityType(fullName1));
        }

        if (schema2.getEntityTypes() != null && !schema2.getEntityTypes().isEmpty()) {
            CsdlEntityType entityType2 = schema2.getEntityTypes().get(0);
            String fullName2 = schema2.getNamespace() + "." + entityType2.getName();
            assertEquals(entityType2, repository.getEntityType(fullName2));
        }
    }

    @Test
    void testGetEntityType_AfterClear_ShouldReturnNull() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");
        
        if (schema.getEntityTypes() != null && !schema.getEntityTypes().isEmpty()) {
            CsdlEntityType entityType = schema.getEntityTypes().get(0);
            String fullName = schema.getNamespace() + "." + entityType.getName();
            
            // Verify it exists first
            assertNotNull(repository.getEntityType(fullName));

            // Act
            repository.clear();
            CsdlEntityType result = repository.getEntityType(fullName);

            // Assert
            assertNull(result);
        }
    }

    @Test
    void testGetEntityType_WithSpecialCharacters_ShouldWork() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");

        if (schema.getEntityTypes() != null && !schema.getEntityTypes().isEmpty()) {
            CsdlEntityType entityType = schema.getEntityTypes().get(0);
            String specialNamespace = "special.namespace-with_chars$123";
            schema.setNamespace(specialNamespace);
            
            // Re-add with special namespace
            repository.clear();
            repository.addSchema(schema, "/test/path/special-schema.xml");
            
            String fullName = specialNamespace + "." + entityType.getName();

            // Act
            CsdlEntityType result = repository.getEntityType(fullName);

            // Assert
            assertNotNull(result);
            assertEquals(entityType, result);
        }
    }

    @Test
    void testGetEntityType_ConcurrentAccess_ShouldBeThreadSafe() throws InterruptedException {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");
        
        if (schema.getEntityTypes() != null && !schema.getEntityTypes().isEmpty()) {
            CsdlEntityType expectedEntityType = schema.getEntityTypes().get(0);
            String fullName = schema.getNamespace() + "." + expectedEntityType.getName();

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
                        CsdlEntityType result = repository.getEntityType(fullName);
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
    void testGetEntityType_AfterSchemaOverwrite_ShouldReturnLatest() {
        // Arrange
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        
        // Force same namespace
        String commonNamespace = "test.overwrite.namespace";
        schema1.setNamespace(commonNamespace);
        schema2.setNamespace(commonNamespace);

        repository.addSchema(schema1, "/test/path/first.xml");
        repository.addSchema(schema2, "/test/path/second.xml");

        // Act & Assert
        if (schema2.getEntityTypes() != null && !schema2.getEntityTypes().isEmpty()) {
            CsdlEntityType expectedEntityType = schema2.getEntityTypes().get(0);
            String fullName = commonNamespace + "." + expectedEntityType.getName();
            CsdlEntityType result = repository.getEntityType(fullName);
            
            assertNotNull(result);
            assertEquals(expectedEntityType, result);
        }
    }

    @Test
    void testGetEntityType_AllEntityTypesFromSchema_ShouldBeAccessible() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");

        // Act & Assert
        if (schema.getEntityTypes() != null) {
            for (CsdlEntityType entityType : schema.getEntityTypes()) {
                String fullName = schema.getNamespace() + "." + entityType.getName();
                CsdlEntityType result = repository.getEntityType(fullName);
                
                assertNotNull(result, "Entity type should be accessible: " + fullName);
                assertEquals(entityType, result);
                assertEquals(entityType.getName(), result.getName());
            }
        }
    }
}
