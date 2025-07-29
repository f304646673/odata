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
    public void testInvalidAnnotationTargets() {
        Path testFilePath = Paths.get(ERROR_SCHEMAS_DIR, "invalid-annotation-targets.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "Annotation error file should have errors: invalid-annotation-targets.xml");
        boolean foundTargetError = result.getErrors().stream().anyMatch(e -> e.contains("invalid target") || e.contains("Annotation target") || e.contains("目标无效"));
        assertTrue(foundTargetError, "应检测到注解目标相关错误: " + result.getErrors());
    }

    @Test
    public void testUndefinedAnnotationTerms() {
        Path testFilePath = Paths.get(ERROR_SCHEMAS_DIR, "undefined-annotation-terms.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "Annotation error file should have errors: undefined-annotation-terms.xml");
        boolean foundTermError = result.getErrors().stream().anyMatch(e -> e.contains("undefined term") || e.contains("未定义的术语") || e.contains("Annotation term"));
        assertTrue(foundTermError, "应检测到注解术语相关错误: " + result.getErrors());
    }
}
