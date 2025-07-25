package org.apache.olingo.schemamanager.repository.impl;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for InMemorySchemaRepository.addSchema() method.
 * Covers various scenarios for adding schemas to the repository.
 */
class InMemorySchemaRepositoryTest_addSchema {

    private InMemorySchemaRepository repository;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        repository = new InMemorySchemaRepository();
        System.out.println("Setting up test: " + testInfo.getDisplayName());
    }

    @Test
    void testAddSchema_ValidSchema_ShouldAddSuccessfully() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        String filePath = "/test/path/complex-schema.xml";

        // Act
        repository.addSchema(schema, filePath);

        // Assert
        assertNotNull(repository.getSchema(schema.getNamespace()));
        assertEquals(schema, repository.getSchema(schema.getNamespace()));
        assertEquals(filePath, repository.getSchemaFilePath(schema.getNamespace()));
        assertTrue(repository.getAllNamespaces().contains(schema.getNamespace()));
    }

    @Test
    void testAddSchema_NullSchema_ShouldHandleGracefully() {
        // Arrange
        String filePath = "/test/path/null-schema.xml";

        // Act & Assert
        assertDoesNotThrow(() -> repository.addSchema(null, filePath));
        assertTrue(repository.getAllSchemas().isEmpty());
        assertTrue(repository.getAllNamespaces().isEmpty());
    }

    @Test
    void testAddSchema_SchemaWithNullNamespace_ShouldHandleGracefully() {
        // Arrange
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace(null); // null namespace
        String filePath = "/test/path/invalid-schema.xml";

        // Act & Assert
        assertDoesNotThrow(() -> repository.addSchema(schema, filePath));
        assertTrue(repository.getAllSchemas().isEmpty());
        assertTrue(repository.getAllNamespaces().isEmpty());
    }

    @Test
    void testAddSchema_MultipleSchemas_ShouldAddAll() {
        // Arrange
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        String filePath1 = "/test/path/simple-schema.xml";
        String filePath2 = "/test/path/complex-schema.xml";

        // Act
        repository.addSchema(schema1, filePath1);
        repository.addSchema(schema2, filePath2);

        // Assert
        assertEquals(2, repository.getAllSchemas().size());
        assertEquals(2, repository.getAllNamespaces().size());
        assertNotNull(repository.getSchema(schema1.getNamespace()));
        assertNotNull(repository.getSchema(schema2.getNamespace()));
        assertEquals(filePath1, repository.getSchemaFilePath(schema1.getNamespace()));
        assertEquals(filePath2, repository.getSchemaFilePath(schema2.getNamespace()));
    }

    @Test
    void testAddSchema_SameNamespaceTwice_ShouldOverwrite() {
        // Arrange
        CsdlSchema schema1 = XmlSchemaTestUtils.loadSchemaFromXml("simple-schema.xml");
        CsdlSchema schema2 = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        
        // Force same namespace for testing
        String commonNamespace = "test.common.namespace";
        schema1.setNamespace(commonNamespace);
        schema2.setNamespace(commonNamespace);
        
        String filePath1 = "/test/path/first.xml";
        String filePath2 = "/test/path/second.xml";

        // Act
        repository.addSchema(schema1, filePath1);
        repository.addSchema(schema2, filePath2);

        // Assert
        assertEquals(1, repository.getAllSchemas().size());
        assertEquals(schema2, repository.getSchema(commonNamespace)); // Last one wins
        assertEquals(filePath2, repository.getSchemaFilePath(commonNamespace));
    }

    @Test
    void testAddSchema_WithEntityTypes_ShouldIndexCorrectly() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        String filePath = "/test/path/complex-schema.xml";

        // Act
        repository.addSchema(schema, filePath);

        // Assert
        if (schema.getEntityTypes() != null && !schema.getEntityTypes().isEmpty()) {
            for (CsdlEntityType entityType : schema.getEntityTypes()) {
                String fullName = schema.getNamespace() + "." + entityType.getName();
                assertNotNull(repository.getEntityType(fullName));
                assertNotNull(repository.getEntityType(schema.getNamespace(), entityType.getName()));
            }
            assertFalse(repository.getEntityTypes(schema.getNamespace()).isEmpty());
        }
    }

    @Test
    void testAddSchema_WithComplexTypes_ShouldIndexCorrectly() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        String filePath = "/test/path/complex-schema.xml";

        // Act
        repository.addSchema(schema, filePath);

        // Assert
        if (schema.getComplexTypes() != null && !schema.getComplexTypes().isEmpty()) {
            for (CsdlComplexType complexType : schema.getComplexTypes()) {
                String fullName = schema.getNamespace() + "." + complexType.getName();
                assertNotNull(repository.getComplexType(fullName));
                assertNotNull(repository.getComplexType(schema.getNamespace(), complexType.getName()));
            }
            assertFalse(repository.getComplexTypes(schema.getNamespace()).isEmpty());
        }
    }

    @Test
    void testAddSchema_WithEnumTypes_ShouldIndexCorrectly() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        String filePath = "/test/path/complex-schema.xml";

        // Act
        repository.addSchema(schema, filePath);

        // Assert
        if (schema.getEnumTypes() != null && !schema.getEnumTypes().isEmpty()) {
            for (CsdlEnumType enumType : schema.getEnumTypes()) {
                String fullName = schema.getNamespace() + "." + enumType.getName();
                assertNotNull(repository.getEnumType(fullName));
                assertNotNull(repository.getEnumType(schema.getNamespace(), enumType.getName()));
            }
            assertFalse(repository.getEnumTypes(schema.getNamespace()).isEmpty());
        }
    }

    @Test
    void testAddSchema_WithActions_ShouldIndexCorrectly() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        String filePath = "/test/path/complex-schema.xml";

        // Act
        repository.addSchema(schema, filePath);

        // Assert
        if (schema.getActions() != null && !schema.getActions().isEmpty()) {
            for (CsdlAction action : schema.getActions()) {
                String fullName = schema.getNamespace() + "." + action.getName();
                assertNotNull(repository.getAction(fullName));
                assertNotNull(repository.getAction(schema.getNamespace(), action.getName()));
            }
            assertFalse(repository.getActions(schema.getNamespace()).isEmpty());
        }
    }

    @Test
    void testAddSchema_WithFunctions_ShouldIndexCorrectly() {
        // Arrange
        CsdlSchema schema = XmlSchemaTestUtils.loadSchemaFromXml("complex-schema.xml");
        String filePath = "/test/path/complex-schema.xml";

        // Act
        repository.addSchema(schema, filePath);

        // Assert
        if (schema.getFunctions() != null && !schema.getFunctions().isEmpty()) {
            for (CsdlFunction function : schema.getFunctions()) {
                String fullName = schema.getNamespace() + "." + function.getName();
                assertNotNull(repository.getFunction(fullName));
                assertNotNull(repository.getFunction(schema.getNamespace(), function.getName()));
            }
            assertFalse(repository.getFunctions(schema.getNamespace()).isEmpty());
        }
    }

    @Test
    void testAddSchema_EmptySchema_ShouldAddButNotIndex() {
        // Arrange
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("test.empty.namespace");
        String filePath = "/test/path/empty-schema.xml";

        // Act
        repository.addSchema(schema, filePath);

        // Assert
        assertNotNull(repository.getSchema(schema.getNamespace()));
        assertTrue(repository.getEntityTypes(schema.getNamespace()).isEmpty());
        assertTrue(repository.getComplexTypes(schema.getNamespace()).isEmpty());
        assertTrue(repository.getEnumTypes(schema.getNamespace()).isEmpty());
        assertTrue(repository.getActions(schema.getNamespace()).isEmpty());
        assertTrue(repository.getFunctions(schema.getNamespace()).isEmpty());
    }
}
