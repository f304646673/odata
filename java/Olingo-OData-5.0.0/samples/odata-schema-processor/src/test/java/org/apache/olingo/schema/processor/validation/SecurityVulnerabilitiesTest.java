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
    public void testXmlExternalEntityAttack() {
        Path testFilePath = Paths.get("src/test/resources/validator/08-security-vulnerabilities/xml-external-entity-attack.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "安全漏洞文件应有错误: xml-external-entity-attack.xml");
        boolean foundXxe = result.getErrors().stream().anyMatch(e -> e.toLowerCase().contains("external entity") || e.contains("外部实体") || e.contains("xxe"));
        assertTrue(foundXxe, "应检测到外部实体攻击相关错误: " + result.getErrors());
    }

    @Test
    public void testBillionLaughsAttack() {
        Path testFilePath = Paths.get("src/test/resources/validator/08-security-vulnerabilities/billion-laughs-attack.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "安全漏洞文件应有错误: billion-laughs-attack.xml");
        boolean foundBillionLaughs = result.getErrors().stream().anyMatch(e -> e.toLowerCase().contains("billion laughs") || e.contains("实体扩展限制") || e.contains("entity expansion"));
        assertTrue(foundBillionLaughs, "应检测到Billion Laughs攻击相关错误: " + result.getErrors());
    }

    @Test
    public void testDosEntityExpansion() {
        Path testFilePath = Paths.get("src/test/resources/validator/08-security-vulnerabilities/dos-entity-expansion.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "安全漏洞文件应有错误: dos-entity-expansion.xml");
        boolean foundDos = result.getErrors().stream().anyMatch(e -> e.toLowerCase().contains("entity expansion") || e.contains("实体扩展") || e.contains("dos"));
        assertTrue(foundDos, "应检测到实体扩展DoS相关错误: " + result.getErrors());
    }

    @Test
    public void testParameterEntityAttack() {
        Path testFilePath = Paths.get("src/test/resources/validator/08-security-vulnerabilities/parameter-entity-attack.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "安全漏洞文件应有错误: parameter-entity-attack.xml");
        boolean foundParamEntity = result.getErrors().stream().anyMatch(e -> e.toLowerCase().contains("parameter entity") || e.contains("参数实体") || e.contains("parameter-entity"));
        assertTrue(foundParamEntity, "应检测到参数实体攻击相关错误: " + result.getErrors());
    }
}
