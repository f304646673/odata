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
 * Test class for OData compliance errors using XmlFileComplianceValidator
 */
public class ODataComplianceErrorsTest {

    private XmlFileComplianceValidator validator;
    private static final String ODATA_COMPLIANCE_ERRORS_DIR = "src/test/resources/validator/06-odata-compliance-errors";

    @BeforeEach
    public void setUp() {
        validator = new OlingoXmlFileComplianceValidator();
    }

    @Test
    public void testInvalidEntityTypeInheritance() {
        testODataComplianceError("invalid-entity-type-inheritance.xml");
    }

    @Test
    public void testInvalidNavigationProperty() {
        testODataComplianceError("invalid-navigation-property.xml");
    }

    @Test
    public void testInvalidPropertyType() {
        testODataComplianceError("invalid-property-type.xml");
    }

    @Test
    public void testMissingKeyProperty() {
        testODataComplianceError("missing-key-property.xml");
    }

    @Test
    public void testInvalidComplexTypeReference() {
        testODataComplianceError("invalid-complex-type-reference.xml");
    }

    /**
     * Helper method to test a specific OData compliance error file
     */
    private void testODataComplianceError(String fileName) {
        Path testFilePath = Paths.get(ODATA_COMPLIANCE_ERRORS_DIR, fileName);
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

        // OData compliance error files should NOT be compliant
        assertFalse(result.isCompliant(), "OData compliance error file should not be compliant: " + fileName);
        assertTrue(result.hasErrors(), "OData compliance error file should have errors: " + fileName);
    }
}
