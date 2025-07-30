package org.apache.olingo.schema.processor.validation.file;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

    private void assertVulnerabilityError(Path testFilePath, String errorHint) {
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "安全漏洞文件应有错误: " + testFilePath.getFileName());
        boolean found = result.getErrors().stream().anyMatch(e -> e.contains(errorHint));
        assertTrue(found, "应检测到相关安全漏洞错误: " + result.getErrors());
    }

    @Test
    public void testXmlExternalEntityAttack() {
        assertVulnerabilityError(Paths.get("src/test/resources/validator/file/08-security-vulnerabilities/xml-external-entity-attack.xml"), "引用了实体");
    }

    @Test
    public void testBillionLaughs() {
        assertVulnerabilityError(Paths.get("src/test/resources/validator/file/08-security-vulnerabilities/billion-laughs.xml"), "引用了实体");
    }

    @Test
    public void testDosEntityExpansion() {
        assertVulnerabilityError(Paths.get("src/test/resources/validator/file/08-security-vulnerabilities/dos-entity-expansion.xml"), "引用了实体");
    }

    @Test
    public void testParameterEntityAttack() {
        assertVulnerabilityError(Paths.get("src/test/resources/validator/file/08-security-vulnerabilities/parameter-entity-attack.xml"), "引用了实体");
    }

    @Test
    public void testLargeFileDos() {
        Path testFilePath = Paths.get("src/test/resources/validator/file/08-security-vulnerabilities/large-file-dos.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.hasErrors(), "大文件应没有错误: large-file-dos.xml");
    }

    @Test
    public void testXxeAttack() {
        assertVulnerabilityError(Paths.get("src/test/resources/validator/file/08-security-vulnerabilities/xxe-attack.xml"), "引用了实体");
    }
}
