package org.apache.olingo.schema.processor.validation.file;

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
    private static final String ELEMENT_DEFINITION_ERRORS_DIR = "src/test/resources/validator/file/03-element-definition-errors";

    @BeforeEach
    public void setUp() {
        validator = new OlingoXmlFileComplianceValidator();
    }

    @Test
    public void testDuplicateElementNames() {
        Path testFilePath = Paths.get(ELEMENT_DEFINITION_ERRORS_DIR, "duplicate-element-names.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isCompliant(), "Element definition error file should not be compliant: duplicate-element-names.xml");
        assertTrue(result.hasErrors(), "Element definition error file should have errors: duplicate-element-names.xml");

        boolean foundDuplicateElement = result.getErrors().stream().anyMatch(e ->
            e.contains("Conflicting") ||
            e.contains("duplicate") ||
            e.contains("already defined") ||
            e.contains("Invalid EntityType name") ||
            e.contains("Invalid Property name") ||
            e.contains("Invalid constraint definition"));
        assertTrue(foundDuplicateElement, "应检测到重复元素名称相关错误: " + result.getErrors());
    }

    @Test
    public void testEmptyElementNames() {
        Path testFilePath = Paths.get(ELEMENT_DEFINITION_ERRORS_DIR, "empty-element-names.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isCompliant(), "Element definition error file should not be compliant: empty-element-names.xml");
        assertTrue(result.hasErrors(), "Element definition error file should have errors: empty-element-names.xml");

        boolean foundEmptyName = result.getErrors().stream().anyMatch(e ->
            e.contains("must have a valid name") ||
            e.contains("empty name") ||
            e.contains("名称不能为空") ||
            e.contains("Invalid EntityType name") ||
            e.contains("Invalid Property name") ||
            e.contains("Invalid constraint definition"));
        assertTrue(foundEmptyName, "应检测到空元素名称相关错误: " + result.getErrors());
    }

    @Test
    public void testInvalidElementNames() {
        Path testFilePath = Paths.get(ELEMENT_DEFINITION_ERRORS_DIR, "invalid-element-names.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isCompliant(), "Element definition error file should not be compliant: invalid-element-names.xml");
        assertTrue(result.hasErrors(), "Element definition error file should have errors: invalid-element-names.xml");
        boolean foundInvalidName = result.getErrors().stream().anyMatch(e -> e.contains("Invalid") || e.contains("invalid name") || e.contains("名称无效"));
        assertTrue(foundInvalidName, "应检测到无效元素名称相关错误: " + result.getErrors());
    }

    @Test
    public void testMissingRequiredAttributes() {
        Path testFilePath = Paths.get(ELEMENT_DEFINITION_ERRORS_DIR, "missing-required-attributes.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isCompliant(), "Element definition error file should not be compliant: missing-required-attributes.xml");
        assertTrue(result.hasErrors(), "Element definition error file should have errors: missing-required-attributes.xml");
        boolean foundMissingAttr = result.getErrors().stream().anyMatch(e -> e.contains("\"namespaceAndName\" is null") || e.contains("missing") || e.contains("required") || e.contains("缺少必需属性"));
        assertTrue(foundMissingAttr, "应检测到缺少必需属性相关错误: " + result.getErrors());
    }
}
