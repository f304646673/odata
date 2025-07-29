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
        Path testFilePath = Paths.get(XML_FORMAT_ERRORS_DIR, "encoding-mismatch.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isCompliant(), "XML format error file should not be compliant: encoding-mismatch.xml");
        assertTrue(result.hasErrors(), "XML format error file should have errors: encoding-mismatch.xml");
        // 针对性断言（根据实际错误内容调整）
        boolean foundEntityOrPropertyError = result.getErrors().stream().anyMatch(e -> e.contains("Invalid EntityType name") || e.contains("Invalid Property name"));
        assertTrue(foundEntityOrPropertyError, "应检测到实体名或属性名相关错误: " + result.getErrors());
    }

    @Test
    public void testInvalidCharacters() {
        Path testFilePath = Paths.get(XML_FORMAT_ERRORS_DIR, "invalid-characters.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isCompliant(), "XML format error file should not be compliant: invalid-characters.xml");
        assertTrue(result.hasErrors(), "XML format error file should have errors: invalid-characters.xml");
        // 针对性断言（根据实际错误内容调整）
        boolean foundReadFailed = result.getErrors().stream().anyMatch(e -> e.contains("Failed to read complete metadata file") || e.contains("Failed at Documentation"));
        assertTrue(foundReadFailed, "应检测到读取元数据文件失败相关错误: " + result.getErrors());
    }

    @Test
    public void testMissingRootElement() {
        Path testFilePath = Paths.get(XML_FORMAT_ERRORS_DIR, "missing-root-element.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isCompliant(), "XML format error file should not be compliant: missing-root-element.xml");
        assertTrue(result.hasErrors(), "XML format error file should have errors: missing-root-element.xml");
        // 针对性断言（根据实际错误内容调整）
        boolean foundRootError = result.getErrors().stream().anyMatch(e -> e.contains("Failed to read complete metadata file") || e.contains("Failed at EntityType"));
        assertTrue(foundRootError, "应检测到缺少根元素相关错误: " + result.getErrors());
    }

    @Test
    public void testUnclosedTags() {
        Path testFilePath = Paths.get(XML_FORMAT_ERRORS_DIR, "unclosed-tags.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isCompliant(), "XML format error file should not be compliant: unclosed-tags.xml");
        assertTrue(result.hasErrors(), "XML format error file should have errors: unclosed-tags.xml");
        // 针对性断言（根据实际错误内容调整）
        boolean foundUnclosedTag = result.getErrors().stream().anyMatch(e -> e.contains("ParseError") || e.contains("结束标记") || e.contains("终止"));
        assertTrue(foundUnclosedTag, "应检测到未闭合标签相关错误: " + result.getErrors());
    }
}
