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
 * Test class for element definition errors using XmlFileComplianceValidator
 */
public class ElementDefinitionErrorsTest {

    private XmlFileComplianceValidator validator;
    private static final String ELEMENT_DEFINITION_ERRORS_DIR = "src/test/resources/validator/03-element-definition-errors";

    @BeforeEach
    public void setUp() {
        validator = new OlingoXmlFileComplianceValidator();
    }

    @Test
    public void testDuplicateEntityType() {
        testElementDefinitionError("duplicate-entity-type.xml");
    }

    @Test
    public void testInvalidEntityContainer() {
        testElementDefinitionError("invalid-entity-container.xml");
    }

    @Test
    public void testInvalidPropertyDefinition() {
        testElementDefinitionError("invalid-property-definition.xml");
    }

    @Test
    public void testMissingEntitySet() {
        testElementDefinitionError("missing-entity-set.xml");
    }

    @Test
    public void testInvalidComplexType() {
        testElementDefinitionError("invalid-complex-type.xml");
    }

    /**
     * Helper method to test a specific element definition error file
     */
    private void testElementDefinitionError(String fileName) {
        Path testFilePath = Paths.get(ELEMENT_DEFINITION_ERRORS_DIR, fileName);
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

        // Element definition error files should NOT be compliant
        assertFalse(result.isCompliant(), "Element definition error file should not be compliant: " + fileName);
        assertTrue(result.hasErrors(), "Element definition error file should have errors: " + fileName);
    }
}
