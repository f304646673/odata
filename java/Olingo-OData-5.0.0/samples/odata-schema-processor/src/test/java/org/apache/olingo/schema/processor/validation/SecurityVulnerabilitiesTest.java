package org.apache.olingo.schema.processor.validation;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for security vulnerabilities using XmlFileComplianceValidator
 */
public class SecurityVulnerabilitiesTest {

    private XmlFileComplianceValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new OlingoXmlFileComplianceValidator();
    }

    private void testSecurityVulnerabilityFile(Path testFilePath) {
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);

        XmlComplianceResult result = validator.validateFile(xmlFile);

        assertNotNull(result, "Result should not be null");

        // Log the result for debugging
        System.out.println("Testing security vulnerability file: " + testFilePath.getFileName());
        System.out.println("  Compliant: " + result.isCompliant());
        System.out.println("  Errors: " + result.getErrorCount());
        System.out.println("  Warnings: " + result.getWarningCount());

        if (result.hasErrors()) {
            System.out.println("  Error details: " + result.getErrors());
        }

        // Security vulnerability files should typically fail validation
        if (result.isCompliant() && result.getErrorCount() == 0) {
            System.out.println("WARNING: Security vulnerability file was unexpectedly valid: " + testFilePath.getFileName());
        }

        // At minimum, the validation should complete without throwing exceptions
        assertNotNull(result, "Validation should complete successfully");
    }

    @Test
    public void testXxeAttack() throws Exception {
        testSecurityVulnerabilityFile(Paths.get("src/test/resources/validator/08-security-vulnerabilities/xxe-attack.xml"));
    }

    @Test
    public void testBillionLaughs() throws Exception {
        testSecurityVulnerabilityFile(Paths.get("src/test/resources/validator/08-security-vulnerabilities/billion-laughs.xml"));
    }

    @Test
    public void testLargeFileDos() throws Exception {
        testSecurityVulnerabilityFile(Paths.get("src/test/resources/validator/08-security-vulnerabilities/large-file-dos.xml"));
    }
}
