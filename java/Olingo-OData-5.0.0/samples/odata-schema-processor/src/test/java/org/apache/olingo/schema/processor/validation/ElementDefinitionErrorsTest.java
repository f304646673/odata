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
 * Test class for element definition errors using XmlFileComplianceValidator
 */
public class ElementDefinitionErrorsTest {

    private XmlFileComplianceValidator validator;
    private static final String ELEMENT_DEFINITION_ERRORS_DIR = "src/test/resources/validator/03-element-definition-errors";

    @BeforeEach
    public void setUp() {
        validator = new OlingoXmlFileComplianceValidator();
    }

    @Test
    public void testDuplicateEntityType() {
        Path testFilePath = Paths.get(ELEMENT_DEFINITION_ERRORS_DIR, "duplicate-entity-type.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isCompliant(), "Element definition error file should not be compliant: duplicate-entity-type.xml");
        assertTrue(result.hasErrors(), "Element definition error file should have errors: duplicate-entity-type.xml");
        boolean foundDuplicateEntity = result.getErrors().stream().anyMatch(e -> e.contains("duplicate entity") || e.contains("already defined") || e.contains("实体类型重复"));
        assertTrue(foundDuplicateEntity, "应检测到实体类型重复相关错误: " + result.getErrors());
    }

    @Test
    public void testInvalidEntityContainer() {
        Path testFilePath = Paths.get(ELEMENT_DEFINITION_ERRORS_DIR, "invalid-entity-container.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isCompliant(), "Element definition error file should not be compliant: invalid-entity-container.xml");
        assertTrue(result.hasErrors(), "Element definition error file should have errors: invalid-entity-container.xml");
        boolean foundInvalidContainer = result.getErrors().stream().anyMatch(e -> e.contains("invalid entity container") || e.contains("EntityContainer") || e.contains("实体容器无效"));
        assertTrue(foundInvalidContainer, "应检测到实体容器相关错误: " + result.getErrors());
    }

    @Test
    public void testInvalidPropertyDefinition() {
        Path testFilePath = Paths.get(ELEMENT_DEFINITION_ERRORS_DIR, "invalid-property-definition.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isCompliant(), "Element definition error file should not be compliant: invalid-property-definition.xml");
        assertTrue(result.hasErrors(), "Element definition error file should have errors: invalid-property-definition.xml");
        boolean foundInvalidProperty = result.getErrors().stream().anyMatch(e -> e.contains("invalid property") || e.contains("Property definition") || e.contains("属性定义无效"));
        assertTrue(foundInvalidProperty, "应检测到属性定义相关错误: " + result.getErrors());
    }

    @Test
    public void testMissingEntitySet() {
        Path testFilePath = Paths.get(ELEMENT_DEFINITION_ERRORS_DIR, "missing-entity-set.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isCompliant(), "Element definition error file should not be compliant: missing-entity-set.xml");
        assertTrue(result.hasErrors(), "Element definition error file should have errors: missing-entity-set.xml");
        boolean foundMissingEntitySet = result.getErrors().stream().anyMatch(e -> e.contains("missing entity set") || e.contains("EntitySet is required") || e.contains("缺少实体集"));
        assertTrue(foundMissingEntitySet, "应检测到缺少实体集相关错误: " + result.getErrors());
    }

    @Test
    public void testInvalidComplexType() {
        Path testFilePath = Paths.get(ELEMENT_DEFINITION_ERRORS_DIR, "invalid-complex-type.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isCompliant(), "Element definition error file should not be compliant: invalid-complex-type.xml");
        assertTrue(result.hasErrors(), "Element definition error file should have errors: invalid-complex-type.xml");
        boolean foundInvalidComplexType = result.getErrors().stream().anyMatch(e -> e.contains("invalid complex type") || e.contains("ComplexType") || e.contains("复杂类型无效"));
        assertTrue(foundInvalidComplexType, "应检测到复杂类型相关错误: " + result.getErrors());
    }
}
