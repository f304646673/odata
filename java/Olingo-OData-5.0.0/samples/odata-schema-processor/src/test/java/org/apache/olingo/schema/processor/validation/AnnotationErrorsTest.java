package org.apache.olingo.schema.processor.validation;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for annotation error files using XmlFileComplianceValidator.
 * Tests files from src/test/resources/validator/05-annotation-errors/
 */
public class AnnotationErrorsTest {

    private XmlFileComplianceValidator validator;
    private static final String ERROR_SCHEMAS_DIR = "src/test/resources/validator/05-annotation-errors";

    @BeforeEach
    public void setUp() {
        validator = new OlingoXmlFileComplianceValidator();
    }

    @Test
    public void testInvalidAnnotationTargetNonexistentEntity() {
        Path testFilePath = Paths.get(ERROR_SCHEMAS_DIR, "invalid-annotation-target-nonexistent-entity.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "Annotation error file should have errors: invalid-annotation-target-nonexistent-entity.xml");
        boolean foundTargetError = result.getErrors().stream().anyMatch(e -> e.contains("Annotation target references non-existent type"));
        assertTrue(foundTargetError, "应检测到注解目标相关错误: " + result.getErrors());
    }

    @Test
    public void testInvalidAnnotationTargetNonexistentProperty() {
        Path testFilePath = Paths.get(ERROR_SCHEMAS_DIR, "invalid-annotation-target-nonexistent-property.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "Annotation error file should have errors: invalid-annotation-target-nonexistent-property.xml");
        boolean foundTargetError = result.getErrors().stream().anyMatch(e -> e.contains("Invalid annotation target format"));
        assertTrue(foundTargetError, "应检测到注解目标相关错误: " + result.getErrors());
    }

    @Test
    public void testInvalidAnnotationTargetFormat() {
        Path testFilePath = Paths.get(ERROR_SCHEMAS_DIR, "invalid-annotation-target-format.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "Annotation error file should have errors: invalid-annotation-target-format.xml");
        boolean foundTargetError = result.getErrors().stream().anyMatch(e -> e.contains("Invalid annotation target format"));
        assertTrue(foundTargetError, "应检测到注解目标相关错误: " + result.getErrors());
    }

    @Test
    public void testInvalidAnnotationTargetWrongNamespace() {
        Path testFilePath = Paths.get(ERROR_SCHEMAS_DIR, "invalid-annotation-target-wrong-namespace.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "Annotation error file should have errors: invalid-annotation-target-wrong-namespace.xml");
        boolean foundTargetError = result.getErrors().stream().anyMatch(e -> e.contains("Annotation target references non-existent type"));
        assertTrue(foundTargetError, "应检测到注解目标相关错误: " + result.getErrors());
    }

    @Test
    public void testUndefinedAnnotationTermNonexistent() {
        Path testFilePath = Paths.get(ERROR_SCHEMAS_DIR, "undefined-annotation-term-nonexistent.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "Annotation error file should have errors: undefined-annotation-term-nonexistent.xml");
        boolean foundTermError = result.getErrors().stream().anyMatch(e -> e.contains("undefined term") || e.contains("未定义的术语") || e.contains("Annotation term"));
        assertTrue(foundTermError, "应检测到注解术语相关错误: " + result.getErrors());
    }

    @Test
    public void testUndefinedAnnotationTermInvalidFormat() {
        Path testFilePath = Paths.get(ERROR_SCHEMAS_DIR, "undefined-annotation-term-invalid-format-entitytype.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "Annotation error file should have errors: undefined-annotation-term-invalid-format.xml");
        boolean foundTermError = result.getErrors().stream().anyMatch(e -> e.contains("undefined term") || e.contains("未定义的术语") || e.contains("Annotation term"));
        assertTrue(foundTermError, "应检测到注解术语相关错误: " + result.getErrors());
    }

    @Test
    public void testUndefinedAnnotationTermMissingCoreDescription() {
        Path testFilePath = Paths.get(ERROR_SCHEMAS_DIR, "undefined-annotation-term-missing-core-description.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "Annotation error file should have errors: undefined-annotation-term-missing-core-description.xml");
        boolean foundTermError = result.getErrors().stream().anyMatch(e -> e.contains("undefined term") || e.contains("未定义的术语") || e.contains("Annotation term"));
        assertTrue(foundTermError, "应检测到注解术语相关错误: " + result.getErrors());
    }
}
