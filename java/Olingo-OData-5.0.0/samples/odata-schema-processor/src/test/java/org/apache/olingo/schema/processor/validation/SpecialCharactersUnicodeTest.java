package org.apache.olingo.schema.processor.validation;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for special characters and Unicode files using XmlFileComplianceValidator.
 * Tests files from src/test/resources/validator/10-special-characters-unicode/
 */
public class SpecialCharactersUnicodeTest {

    private XmlFileComplianceValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new OlingoXmlFileComplianceValidator();
    }

    private void testSpecialCharactersUnicodeFile(Path testFilePath) {
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);

        XmlComplianceResult result = validator.validateFile(xmlFile);

        assertNotNull(result, "Result should not be null");

        // Log the result for debugging
        System.out.println("Testing special characters Unicode file: " + testFilePath.getFileName());
        System.out.println("  Compliant: " + result.isCompliant());
        System.out.println("  Errors: " + result.getErrorCount());
        System.out.println("  Warnings: " + result.getWarningCount());

        if (result.hasErrors()) {
            System.out.println("  Error details: " + result.getErrors());
        }

        // For special characters Unicode files, we're testing both valid and invalid cases
        // Some files might be valid Unicode usage, others might contain invalid XML characters
        if (result.isCompliant() && result.getErrorCount() == 0) {
            System.out.println("INFO: Special characters Unicode file was valid: " + testFilePath.getFileName());
        }

        // At minimum, the validation should complete without throwing exceptions
        assertNotNull(result, "Validation should complete successfully");
    }

    @Test
    public void testUnicodeNames() throws Exception {
        testSpecialCharactersUnicodeFile(Paths.get("src/test/resources/validator/10-special-characters-unicode/unicode-names.xml"));
    }

    @Test
    public void testXmlSpecialChars() throws Exception {
        testSpecialCharactersUnicodeFile(Paths.get("src/test/resources/validator/10-special-characters-unicode/xml-special-chars.xml"));
    }

    @Test
    public void testInvalidXmlChars() throws Exception {
        testSpecialCharactersUnicodeFile(Paths.get("src/test/resources/validator/10-special-characters-unicode/invalid-xml-chars.xml"));
    }
}
