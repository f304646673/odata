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
 * Test class for XML format errors using XmlFileComplianceValidator
 */
public class XmlFormatErrorsTest {

    private XmlFileComplianceValidator validator;
    private static final String XML_FORMAT_ERRORS_DIR = "src/test/resources/validator/01-xml-format-errors";

    @BeforeEach
    public void setUp() {
        validator = new OlingoXmlFileComplianceValidator();
    }

    @Test
    public void testEncodingMismatch() {
        testXmlFormatError("encoding-mismatch.xml");
    }

    @Test
    public void testInvalidCharacters() {
        testXmlFormatError("invalid-characters.xml");
    }

    @Test
    public void testMissingRootElement() {
        testXmlFormatError("missing-root-element.xml");
    }

    @Test
    public void testUnclosedTags() {
        testXmlFormatError("unclosed-tags.xml");
    }

    /**
     * Helper method to test a specific XML format error file
     */
    private void testXmlFormatError(String fileName) {
        Path testFilePath = Paths.get(XML_FORMAT_ERRORS_DIR, fileName);
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

        // XML format error files should NOT be compliant
        assertFalse(result.isCompliant(), "XML format error file should not be compliant: " + fileName);
        assertTrue(result.hasErrors(), "XML format error file should have errors: " + fileName);
    }
}
