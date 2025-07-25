package org.apache.olingo.schemamanager.repository.impl;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
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
 * Tests for InMemorySchemaRepository.getComplexType() method (with full qualified name).
 * Covers various scenarios for retrieving complex types from the repository.
 */
class InMemorySchemaRepositoryTest_getComplexType {

    private InMemorySchemaRepository repository;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        repository = new InMemorySchemaRepository();
        System.out.println("Setting up test: " + testInfo.getDisplayName());
    }

    @Test
    void testGetComplexType_ExistingFullQualifiedName_ShouldReturnComplexType() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");
        
        if (schema.getComplexTypes() != null && !schema.getComplexTypes().isEmpty()) {
            CsdlComplexType expectedComplexType = schema.getComplexTypes().get(0);
            String fullQualifiedName = schema.getNamespace() + "." + expectedComplexType.getName();

            // Act
            CsdlComplexType result = repository.getComplexType(fullQualifiedName);

            // Assert
            assertNotNull(result);
            assertEquals(expectedComplexType, result);
            assertEquals(expectedComplexType.getName(), result.getName());
        }
    }

    @Test
    void testGetComplexType_NonExistingFullQualifiedName_ShouldReturnNull() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");
        String nonExistingName = "non.existing.namespace.NonExistingType";

        // Act
        CsdlComplexType result = repository.getComplexType(nonExistingName);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetComplexType_NullFullQualifiedName_ShouldReturnNull() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");

        // Act
        CsdlComplexType result = repository.getComplexType((String) null);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetComplexType_EmptyFullQualifiedName_ShouldReturnNull() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");

        // Act
        CsdlComplexType result = repository.getComplexType("");

        // Assert
        assertNull(result);
    }

    @Test
    void testGetComplexType_MultipleSchemas_ShouldReturnCorrectComplexType() {
        // Arrange
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema1, "/test/path/simple-schema.xml");
        repository.addSchema(schema2, "/test/path/complex-schema.xml");

        // Act & Assert for each schema
        if (schema1.getComplexTypes() != null && !schema1.getComplexTypes().isEmpty()) {
            CsdlComplexType complexType1 = schema1.getComplexTypes().get(0);
            String fullName1 = schema1.getNamespace() + "." + complexType1.getName();
            assertEquals(complexType1, repository.getComplexType(fullName1));
        }

        if (schema2.getComplexTypes() != null && !schema2.getComplexTypes().isEmpty()) {
            CsdlComplexType complexType2 = schema2.getComplexTypes().get(0);
            String fullName2 = schema2.getNamespace() + "." + complexType2.getName();
            assertEquals(complexType2, repository.getComplexType(fullName2));
        }
    }

    @Test
    void testGetComplexType_AfterClear_ShouldReturnNull() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");
        
        if (schema.getComplexTypes() != null && !schema.getComplexTypes().isEmpty()) {
            CsdlComplexType complexType = schema.getComplexTypes().get(0);
            String fullName = schema.getNamespace() + "." + complexType.getName();
            
            // Verify it exists first
            assertNotNull(repository.getComplexType(fullName));

            // Act
            repository.clear();
            CsdlComplexType result = repository.getComplexType(fullName);

            // Assert
            assertNull(result);
        }
    }

    @Test
    void testGetComplexType_ConcurrentAccess_ShouldBeThreadSafe() throws InterruptedException {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");
        
        if (schema.getComplexTypes() != null && !schema.getComplexTypes().isEmpty()) {
            CsdlComplexType expectedComplexType = schema.getComplexTypes().get(0);
            String fullName = schema.getNamespace() + "." + expectedComplexType.getName();

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
                        CsdlComplexType result = repository.getComplexType(fullName);
                        if (result == null || !result.equals(expectedComplexType)) {
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
                assertTrue(result, "One or more threads failed to get complex type correctly");
            }
        }
    }

    @Test
    void testGetComplexType_AllComplexTypesFromSchema_ShouldBeAccessible() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        repository.addSchema(schema, "/test/path/complex-schema.xml");

        // Act & Assert
        if (schema.getComplexTypes() != null) {
            for (CsdlComplexType complexType : schema.getComplexTypes()) {
                String fullName = schema.getNamespace() + "." + complexType.getName();
                CsdlComplexType result = repository.getComplexType(fullName);
                
                assertNotNull(result, "Complex type should be accessible: " + fullName);
                assertEquals(complexType, result);
                assertEquals(complexType.getName(), result.getName());
            }
        }
    }
}
