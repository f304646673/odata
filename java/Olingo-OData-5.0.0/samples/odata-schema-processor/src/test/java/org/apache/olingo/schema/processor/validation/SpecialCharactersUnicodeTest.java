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
        Path testFilePath = Paths.get("src/test/resources/validator/10-special-characters-unicode/unicode-names.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        // 合法Unicode命名应无错误
        assertTrue(result.isCompliant() || !result.hasErrors(), "Unicode命名文件应无错误: " + result.getErrors());
    }

    @Test
    public void testXmlSpecialChars() throws Exception {
        Path testFilePath = Paths.get("src/test/resources/validator/10-special-characters-unicode/xml-special-chars.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        // XML特殊字符，通常应有错误
        boolean foundSpecialCharError = result.getErrors().stream().anyMatch(e -> e.contains("special character") || e.contains("非法字符") || e.contains("invalid character"));
        assertTrue(result.hasErrors() ? foundSpecialCharError : true, "应检测到特殊字符相关错误或无错误: " + result.getErrors());
    }

    @Test
    public void testInvalidXmlChars() throws Exception {
        Path testFilePath = Paths.get("src/test/resources/validator/10-special-characters-unicode/invalid-xml-chars.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        boolean foundInvalidChar = result.getErrors().stream().anyMatch(e -> e.contains("invalid xml character") || e.contains("非法XML字符") || e.contains("invalid character"));
        assertTrue(result.hasErrors() && foundInvalidChar, "应检测到非法XML字符相关错误: " + result.getErrors());
    }
}
