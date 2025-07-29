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
 * Test class for OData compliance errors using XmlFileComplianceValidator
 */
public class ODataComplianceErrorsTest {

    private XmlFileComplianceValidator validator;
    private static final String ODATA_COMPLIANCE_ERRORS_DIR = "src/test/resources/validator/06-odata-compliance-errors";

    @BeforeEach
    public void setUp() {
        validator = new OlingoXmlFileComplianceValidator();
    }

    @Test
    public void testInvalidEntityTypeInheritance() {
        Path testFilePath = Paths.get(ODATA_COMPLIANCE_ERRORS_DIR, "invalid-entity-type-inheritance.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isCompliant(), "OData compliance error file should not be compliant: invalid-entity-type-inheritance.xml");
        assertTrue(result.hasErrors(), "OData compliance error file should have errors: invalid-entity-type-inheritance.xml");
        boolean foundInheritanceError = result.getErrors().stream().anyMatch(e -> e.contains("inheritance") || e.contains("EntityType inheritance") || e.contains("继承关系错误"));
        assertTrue(foundInheritanceError, "应检测到实体类型继承相关错误: " + result.getErrors());
    }

    @Test
    public void testInvalidNavigationProperty() {
        Path testFilePath = Paths.get(ODATA_COMPLIANCE_ERRORS_DIR, "invalid-navigation-property.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isCompliant(), "OData compliance error file should not be compliant: invalid-navigation-property.xml");
        assertTrue(result.hasErrors(), "OData compliance error file should have errors: invalid-navigation-property.xml");
        boolean foundNavPropError = result.getErrors().stream().anyMatch(e -> e.contains("navigation property") || e.contains("NavigationProperty") || e.contains("导航属性错误"));
        assertTrue(foundNavPropError, "应检测到导航属性相关错误: " + result.getErrors());
    }

    @Test
    public void testInvalidPropertyType() {
        Path testFilePath = Paths.get(ODATA_COMPLIANCE_ERRORS_DIR, "invalid-property-type.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isCompliant(), "OData compliance error file should not be compliant: invalid-property-type.xml");
        assertTrue(result.hasErrors(), "OData compliance error file should have errors: invalid-property-type.xml");
        boolean foundPropTypeError = result.getErrors().stream().anyMatch(e -> e.contains("invalid property type") || e.contains("Property type") || e.contains("属性类型错误"));
        assertTrue(foundPropTypeError, "应检测到属性类型相关错误: " + result.getErrors());
    }

    @Test
    public void testMissingKeyProperty() {
        Path testFilePath = Paths.get(ODATA_COMPLIANCE_ERRORS_DIR, "missing-key-property.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isCompliant(), "OData compliance error file should not be compliant: missing-key-property.xml");
        assertTrue(result.hasErrors(), "OData compliance error file should have errors: missing-key-property.xml");
        boolean foundKeyError = result.getErrors().stream().anyMatch(e -> e.contains("missing key property") || e.contains("Key property") || e.contains("缺少主键属性"));
        assertTrue(foundKeyError, "应检测到缺少主键属性相关错误: " + result.getErrors());
    }

    @Test
    public void testInvalidComplexTypeReference() {
        Path testFilePath = Paths.get(ODATA_COMPLIANCE_ERRORS_DIR, "invalid-complex-type-reference.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isCompliant(), "OData compliance error file should not be compliant: invalid-complex-type-reference.xml");
        assertTrue(result.hasErrors(), "OData compliance error file should have errors: invalid-complex-type-reference.xml");
        boolean foundComplexTypeError = result.getErrors().stream().anyMatch(e -> e.contains("invalid complex type reference") || e.contains("ComplexType reference") || e.contains("复杂类型引用错误"));
        assertTrue(foundComplexTypeError, "应检测到复杂类型引用相关错误: " + result.getErrors());
    }

    @Test
    public void testInvalidConstraints() {
        Path testFilePath = Paths.get(ODATA_COMPLIANCE_ERRORS_DIR, "invalid-constraints.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isCompliant(), "OData compliance error file should not be compliant: invalid-constraints.xml");
        assertTrue(result.hasErrors(), "OData compliance error file should have errors: invalid-constraints.xml");
        boolean foundConstraintError = result.getErrors().stream().anyMatch(e -> e.contains("constraint") || e.contains("约束") || e.contains("invalid"));
        assertTrue(foundConstraintError, "应检测到约束相关错误: " + result.getErrors());
    }

    @Test
    public void testInvalidTypes() {
        Path testFilePath = Paths.get(ODATA_COMPLIANCE_ERRORS_DIR, "invalid-types.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isCompliant(), "OData compliance error file should not be compliant: invalid-types.xml");
        assertTrue(result.hasErrors(), "OData compliance error file should have errors: invalid-types.xml");
        boolean foundTypeError = result.getErrors().stream().anyMatch(e -> e.contains("Invalid") || e.contains("type") || e.contains("类型"));
        assertTrue(foundTypeError, "应检测到类型相关错误: " + result.getErrors());
    }

    @Test
    public void testUnsupportedFeatures() {
        Path testFilePath = Paths.get(ODATA_COMPLIANCE_ERRORS_DIR, "unsupported-features.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isCompliant(), "OData compliance error file should not be compliant: unsupported-features.xml");
        assertTrue(result.hasErrors(), "OData compliance error file should have errors: unsupported-features.xml");
        boolean foundUnsupportedError = result.getErrors().stream().anyMatch(e -> e.contains("unsupported") || e.contains("不支持") || e.contains("feature"));
        assertTrue(foundUnsupportedError, "应检测到不支持特性相关错误: " + result.getErrors());
    }

    @Test
    public void testVersionIncompatible() {
        Path testFilePath = Paths.get(ODATA_COMPLIANCE_ERRORS_DIR, "version-incompatible.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isCompliant(), "OData compliance error file should not be compliant: version-incompatible.xml");
        assertTrue(result.hasErrors(), "OData compliance error file should have errors: version-incompatible.xml");
        boolean foundVersionError = result.getErrors().stream().anyMatch(e -> e.contains("version") || e.contains("incompatible") || e.contains("版本不兼容"));
        assertTrue(foundVersionError, "应检测到版本不兼容相关错误: " + result.getErrors());
    }
}
