package org.apache.olingo.schema.processor.validation;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for dependency reference error files using XmlFileComplianceValidator.
 * Tests files from src/test/resources/validator/04-dependency-reference-errors/
 */
public class DependencyReferenceErrorsTest {

    private XmlFileComplianceValidator validator;
    private static final String ERROR_SCHEMAS_DIR = "src/test/resources/validator/04-dependency-reference-errors";

    @BeforeEach
    public void setUp() {
        validator = new OlingoXmlFileComplianceValidator();
    }

    @Test
    public void testMissingExternalNamespace() {
        Path testFilePath = Paths.get(ERROR_SCHEMAS_DIR, "missing-external-namespace.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "Dependency reference error file should have errors: missing-external-namespace.xml");
        boolean foundNamespaceError = result.getErrors().stream().anyMatch(e -> e.contains("is referenced but not imported in the schema"));
        assertTrue(foundNamespaceError, "应检测到缺少外部命名空间相关错误: " + result.getErrors());
    }

    @Test
    public void testMissingFileReference() {
        Path testFilePath = Paths.get(ERROR_SCHEMAS_DIR, "missing-file-reference.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "Dependency reference error file should have errors: missing-file-reference.xml");
        boolean foundFileRefError = result.getErrors().stream().anyMatch(e -> e.contains("Validation error: Failed to read complete metadata file. Failed at EntityType"));
        assertTrue(foundFileRefError, "应检测到缺少文件引用相关错误: " + result.getErrors());
    }

    @Test
    public void testUndefinedTypeReferences() {
        Path testFilePath = Paths.get(ERROR_SCHEMAS_DIR, "undefined-type-references.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "Dependency reference error file should have errors: undefined-type-references.xml");
        boolean foundTypeRefError = result.getErrors().stream().anyMatch(e -> e.contains("is referenced but not imported in the schema"));
        assertTrue(foundTypeRefError, "应检测到未定义类型引用相关错误: " + result.getErrors());
    }

}
