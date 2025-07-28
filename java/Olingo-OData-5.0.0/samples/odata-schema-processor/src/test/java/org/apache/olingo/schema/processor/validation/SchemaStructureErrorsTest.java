package org.apache.olingo.schema.processor.validation;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for schema structure errors using XmlFileComplianceValidator
 */
public class SchemaStructureErrorsTest {

    private XmlFileComplianceValidator validator;
    private static final String SCHEMA_STRUCTURE_ERRORS_DIR = "src/test/resources/validator/02-schema-structure-errors";

    @BeforeEach
    public void setUp() {
        validator = new OlingoXmlFileComplianceValidator();
    }

    @Test
    public void testConflictingNamespace1() {
        testSchemaStructureError("conflicting-namespace/conflicting-namespace-1.xml");
    }

    @Test
    public void testConflictingNamespace2() {
        testSchemaStructureError("conflicting-namespace/conflicting-namespace-2.xml");
    }

    @Test
    public void testInvalidNamespaceFormat() {
        testSchemaStructureError("invalid-namespace-format.xml");
    }

    @Test
    public void testMissingEdmxRoot() {
        testSchemaStructureError("missing-edmx-root.xml");
    }

    @Test
    public void testMissingNamespace() {
        testSchemaStructureError("missing-namespace.xml");
    }

    @Test
    public void testMissingSchemaElement() {
        testSchemaStructureError("missing-schema-element.xml");
    }

    /**
     * Helper method to test a specific schema structure error file
     */
    private void testSchemaStructureError(String fileName) {
        Path testFilePath = Paths.get(SCHEMA_STRUCTURE_ERRORS_DIR, fileName);
        File xmlFile = testFilePath.toFile();

        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);

        XmlComplianceResult result = validator.validateFile(xmlFile);

        assertNotNull(result, "Result should not be null");

        // Log the result for debugging
        System.out.println("Validated: " + fileName + " - Compliant: " + result.isCompliant() +
                          " - Errors: " + result.getErrorCount() + " - Warnings: " + result.getWarningCount());
        if (!result.getErrors().isEmpty()) {
            System.out.println("  Errors: " + result.getErrors());
        }

        // Schema structure error files should NOT be compliant
        assertFalse(result.isCompliant(), "Schema structure error file should not be compliant: " + fileName);
        assertTrue(result.hasErrors(), "Schema structure error file should have errors: " + fileName);
    }
}
