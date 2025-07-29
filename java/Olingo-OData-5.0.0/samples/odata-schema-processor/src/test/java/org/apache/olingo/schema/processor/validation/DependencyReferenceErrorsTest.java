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
        boolean foundNamespaceError = result.getErrors().stream().anyMatch(e -> e.contains("missing external namespace") || e.contains("未找到外部命名空间") || e.contains("external namespace"));
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
        boolean foundFileRefError = result.getErrors().stream().anyMatch(e -> e.contains("missing file reference") || e.contains("未找到文件引用") || e.contains("file reference"));
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
        boolean foundTypeRefError = result.getErrors().stream().anyMatch(e -> e.contains("undefined type") || e.contains("未定义类型") || e.contains("type reference"));
        assertTrue(foundTypeRefError, "应检测到未定义类型引用相关错误: " + result.getErrors());
    }

    @Test
    public void testCircularDependencyA() {
        Path testFilePath = Paths.get(ERROR_SCHEMAS_DIR, "circular-dependency/circular-dependency-a.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "Dependency reference error file should have errors: circular-dependency-a.xml");
        boolean foundCircularError = result.getErrors().stream().anyMatch(e -> e.contains("circular dependency") || e.contains("循环依赖") || e.contains("dependency loop"));
        assertTrue(foundCircularError, "应检测到循环依赖相关错误: " + result.getErrors());
    }

    @Test
    public void testCircularDependencyB() {
        Path testFilePath = Paths.get(ERROR_SCHEMAS_DIR, "circular-dependency/circular-dependency-b.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "Dependency reference error file should have errors: circular-dependency-b.xml");
        boolean foundCircularError = result.getErrors().stream().anyMatch(e -> e.contains("circular dependency") || e.contains("循环依赖") || e.contains("dependency loop"));
        assertTrue(foundCircularError, "应检测到循环依赖相关错误: " + result.getErrors());
    }

    @Test
    public void testCircularDependencyC() {
        Path testFilePath = Paths.get(ERROR_SCHEMAS_DIR, "circular-dependency/circular-dependency-c.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "Dependency reference error file should have errors: circular-dependency-c.xml");
        boolean foundCircularError = result.getErrors().stream().anyMatch(e -> e.contains("circular dependency") || e.contains("循环依赖") || e.contains("dependency loop"));
        assertTrue(foundCircularError, "应检测到循环依赖相关错误: " + result.getErrors());
    }
}
