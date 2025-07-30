package org.apache.olingo.schema.processor.validation.file;

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
    private static final String ERROR_SCHEMAS_DIR = "src/test/resources/validator/file/05-annotation-errors";

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
        boolean foundTargetError = result.getErrors().stream().anyMatch(e -> e.contains("Annotation target does not exist"));
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
        boolean foundTargetError = result.getErrors().stream().anyMatch(e -> e.contains("Annotation target does not exist"));
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
        boolean foundTermError = result.getErrors().stream().anyMatch(e -> e.contains("Undefined annotation term"));
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
        boolean foundTermError = result.getErrors().stream().anyMatch(e -> e.contains("Invalid annotation term format"));
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
        boolean foundTermError = result.getErrors().stream().anyMatch(e -> e.contains("Undefined annotation term"));
        assertTrue(foundTermError, "应检测到注解术语相关错误: " + result.getErrors());
    }

    @Test
    public void testUndefinedAnnotationTermInvalidFormatProperty() {
        Path testFilePath = Paths.get(ERROR_SCHEMAS_DIR, "undefined-annotation-term-invalid-format-property.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "Annotation error file should have errors: undefined-annotation-term-invalid-format-property.xml");
        boolean foundTermError = result.getErrors().stream().anyMatch(e -> e.contains("Invalid annotation term format") || e.contains("Undefined annotation term") || e.contains("Invalid.Term.Format!"));
        assertTrue(foundTermError, "应检测到注解术语格式相关错误: " + result.getErrors());
    }

    @Test
    public void testUndefinedAnnotationTermInvalidFormatEntityType() {
        Path testFilePath = Paths.get(ERROR_SCHEMAS_DIR, "undefined-annotation-term-invalid-format-entitytype.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "Annotation error file should have errors: undefined-annotation-term-invalid-format-entitytype.xml");
        boolean foundTermError = result.getErrors().stream().anyMatch(e -> e.contains("Invalid annotation term format") || e.contains("Undefined annotation term") || e.contains("Invalid.Term.Format!"));
        assertTrue(foundTermError, "应检测到注解术语格式相关错误: " + result.getErrors());
    }

    @Test
    public void testUndefinedAnnotationTermInvalidFormatComplexType() {
        Path testFilePath = Paths.get(ERROR_SCHEMAS_DIR, "undefined-annotation-term-invalid-format-complextype.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "Annotation error file should have errors: undefined-annotation-term-invalid-format-complextype.xml");
        boolean foundTermError = result.getErrors().stream().anyMatch(e -> e.contains("Invalid annotation term format") || e.contains("Undefined annotation term") || e.contains("Invalid.Term.Format!"));
        assertTrue(foundTermError, "应检测到注解术语格式相关错误: " + result.getErrors());
    }

    @Test
    public void testUndefinedAnnotationTermInvalidFormatEnumType() {
        Path testFilePath = Paths.get(ERROR_SCHEMAS_DIR, "undefined-annotation-term-invalid-format-enumtype.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "Annotation error file should have errors: undefined-annotation-term-invalid-format-enumtype.xml");
        boolean foundTermError = result.getErrors().stream().anyMatch(e -> e.contains("Invalid annotation term format") || e.contains("Undefined annotation term") || e.contains("Invalid.Term.Format!"));
        assertTrue(foundTermError, "应检测到注解术语格式相关错误: " + result.getErrors());
    }

    @Test
    public void testUndefinedAnnotationTermInvalidFormatEntitySet() {
        Path testFilePath = Paths.get(ERROR_SCHEMAS_DIR, "undefined-annotation-term-invalid-format-entityset.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "Annotation error file should have errors: undefined-annotation-term-invalid-format-entityset.xml");
        boolean foundTermError = result.getErrors().stream().anyMatch(e -> e.contains("Invalid annotation term format") || e.contains("Undefined annotation term") || e.contains("Invalid.Term.Format!"));
        assertTrue(foundTermError, "应检测到注解术语格式相关错误: " + result.getErrors());
    }

    @Test
    public void testUndefinedAnnotationTermInvalidFormatNavigationProperty() {
        Path testFilePath = Paths.get(ERROR_SCHEMAS_DIR, "undefined-annotation-term-invalid-format-navigationproperty.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "Annotation error file should have errors: undefined-annotation-term-invalid-format-navigationproperty.xml");
        boolean foundTermError = result.getErrors().stream().anyMatch(e -> e.contains("Invalid annotation term format") || e.contains("Undefined annotation term") || e.contains("Invalid.Term.Format!"));
        assertTrue(foundTermError, "应检测到注解术语格式相关错误: " + result.getErrors());
    }

//    @Test
//    public void testUndefinedAnnotationTermInvalidFormatSingleton() {
//        Path testFilePath = Paths.get(ERROR_SCHEMAS_DIR, "undefined-annotation-term-invalid-format-singleton.xml");
//        File xmlFile = testFilePath.toFile();
//        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
//        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
//        XmlComplianceResult result = validator.validateFile(xmlFile);
//        assertNotNull(result, "Result should not be null");
//        assertTrue(result.hasErrors(), "Annotation error file should have errors: undefined-annotation-term-invalid-format-singleton.xml");
//        boolean foundTermError = result.getErrors().stream().anyMatch(e -> e.contains("Invalid annotation term format") || e.contains("Undefined annotation term") || e.contains("Invalid.Term.Format!"));
//        assertTrue(foundTermError, "应检测到注解术语格式相关错误: " + result.getErrors());
//    }

    @Test
    public void testUndefinedAnnotationTermInvalidFormatAction() {
        Path testFilePath = Paths.get(ERROR_SCHEMAS_DIR, "undefined-annotation-term-invalid-format-action.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "Annotation error file should have errors: undefined-annotation-term-invalid-format-action.xml");
        boolean foundTermError = result.getErrors().stream().anyMatch(e -> e.contains("Invalid annotation term format") || e.contains("Undefined annotation term") || e.contains("Invalid.Term.Format!"));
        assertTrue(foundTermError, "应检测到注解术语格式相关错误: " + result.getErrors());
    }

    @Test
    public void testUndefinedAnnotationTermInvalidFormatFunction() {
        Path testFilePath = Paths.get(ERROR_SCHEMAS_DIR, "undefined-annotation-term-invalid-format-function.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "Annotation error file should have errors: undefined-annotation-term-invalid-format-function.xml");
        boolean foundTermError = result.getErrors().stream().anyMatch(e -> e.contains("Invalid annotation term format") || e.contains("Undefined annotation term") || e.contains("Invalid.Term.Format!"));
        assertTrue(foundTermError, "应检测到注解术语格式相关错误: " + result.getErrors());
    }

//    @Test
//    public void testUndefinedAnnotationTermInvalidFormatActionImport() {
//        Path testFilePath = Paths.get(ERROR_SCHEMAS_DIR, "undefined-annotation-term-invalid-format-actionimport.xml");
//        File xmlFile = testFilePath.toFile();
//        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
//        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
//        XmlComplianceResult result = validator.validateFile(xmlFile);
//        assertNotNull(result, "Result should not be null");
//        assertTrue(result.hasErrors(), "Annotation error file should have errors: undefined-annotation-term-invalid-format-actionimport.xml");
//        boolean foundTermError = result.getErrors().stream().anyMatch(e -> e.contains("Invalid annotation term format") || e.contains("Undefined annotation term") || e.contains("Invalid.Term.Format!"));
//        assertTrue(foundTermError, "应检测到注解术语格式相关错误: " + result.getErrors());
//    }

//    @Test
//    public void testUndefinedAnnotationTermInvalidFormatFunctionImport() {
//        Path testFilePath = Paths.get(ERROR_SCHEMAS_DIR, "undefined-annotation-term-invalid-format-functionimport.xml");
//        File xmlFile = testFilePath.toFile();
//        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
//        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
//        XmlComplianceResult result = validator.validateFile(xmlFile);
//        assertNotNull(result, "Result should not be null");
//        assertTrue(result.hasErrors(), "Annotation error file should have errors: undefined-annotation-term-invalid-format-functionimport.xml");
//        boolean foundTermError = result.getErrors().stream().anyMatch(e -> e.contains("Invalid annotation term format") || e.contains("Undefined annotation term") || e.contains("Invalid.Term.Format!"));
//        assertTrue(foundTermError, "应检测到注解术语格式相关错误: " + result.getErrors());
//    }

}
