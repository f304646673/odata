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
 * Test class for encoding and charset errors using XmlFileComplianceValidator
 */
public class EncodingCharsetErrorsTest {

    private XmlFileComplianceValidator validator;
    private static final String ENCODING_CHARSET_ERRORS_DIR = "src/test/resources/validator/07-encoding-charset-errors";

    @BeforeEach
    public void setUp() {
        validator = new OlingoXmlFileComplianceValidator();
    }

    @Test
    public void testUtf8EncodingError() {
        testEncodingCharsetError("utf8-encoding-error.xml");
    }

    @Test
    public void testUtf16EncodingError() {
        testEncodingCharsetError("utf16-encoding-error.xml");
    }

    @Test
    public void testInvalidCharacterEncoding() {
        testEncodingCharsetError("invalid-character-encoding.xml");
    }

    @Test
    public void testBomMismatch() {
        testEncodingCharsetError("bom-mismatch.xml");
    }

    /**
     * Helper method to test a specific encoding/charset error file
     */
    private void testEncodingCharsetError(String fileName) {
        Path testFilePath = Paths.get(ENCODING_CHARSET_ERRORS_DIR, fileName);
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

        // Encoding/charset error files should NOT be compliant
        assertFalse(result.isCompliant(), "Encoding charset error file should not be compliant: " + fileName);
        assertTrue(result.hasErrors(), "Encoding charset error file should have errors: " + fileName);
    }
}
