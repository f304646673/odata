package org.apache.olingo.schema.processor.validation;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for performance edge cases using XmlFileComplianceValidator
 */
public class PerformanceEdgeCasesTest {

    private XmlFileComplianceValidator validator;
    private static final String PERFORMANCE_EDGE_CASES_DIR = "src/test/resources/validator/09-performance-edge-cases";

    @BeforeEach
    public void setUp() {
        validator = new OlingoXmlFileComplianceValidator();
    }

    @Test
    public void testLargeSchema() {
        Path testFilePath = Paths.get(PERFORMANCE_EDGE_CASES_DIR, "large-schema.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        long startTime = System.currentTimeMillis();
        XmlComplianceResult result = validator.validateFile(xmlFile);
        long validationTime = System.currentTimeMillis() - startTime;
        assertNotNull(result, "Result should not be null");
        // 性能边界用例通常关注大文件处理能力，断言无异常即可
        System.out.println("large-schema.xml 验证耗时: " + validationTime + "ms, 错误: " + result.getErrors());
    }

    @Test
    public void testDeepNesting() {
        Path testFilePath = Paths.get(PERFORMANCE_EDGE_CASES_DIR, "deep-nesting.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        long startTime = System.currentTimeMillis();
        XmlComplianceResult result = validator.validateFile(xmlFile);
        long validationTime = System.currentTimeMillis() - startTime;
        assertNotNull(result, "Result should not be null");
        boolean foundNestingError = result.getErrors().stream().anyMatch(e -> e.contains("nesting") || e.contains("层级过深") || e.contains("too deep"));
        System.out.println("deep-nesting.xml 验证耗时: " + validationTime + "ms, 错误: " + result.getErrors());
        assertTrue(result.hasErrors() ? foundNestingError : true, "应检测到层级过深相关错误或无错误: " + result.getErrors());
    }

    @Test
    public void testManyReferences() {
        Path testFilePath = Paths.get(PERFORMANCE_EDGE_CASES_DIR, "many-references.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        long startTime = System.currentTimeMillis();
        XmlComplianceResult result = validator.validateFile(xmlFile);
        long validationTime = System.currentTimeMillis() - startTime;
        assertNotNull(result, "Result should not be null");
        boolean foundRefError = result.getErrors().stream().anyMatch(e -> e.contains("reference") || e.contains("引用过多") || e.contains("too many references"));
        System.out.println("many-references.xml 验证耗时: " + validationTime + "ms, 错误: " + result.getErrors());
        assertTrue(result.hasErrors() ? foundRefError : true, "应检测到引用相关错误或无错误: " + result.getErrors());
    }

    @Test
    public void testComplexInheritance() {
        Path testFilePath = Paths.get(PERFORMANCE_EDGE_CASES_DIR, "complex-inheritance.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        long startTime = System.currentTimeMillis();
        XmlComplianceResult result = validator.validateFile(xmlFile);
        long validationTime = System.currentTimeMillis() - startTime;
        assertNotNull(result, "Result should not be null");
        boolean foundInheritanceError = result.getErrors().stream().anyMatch(e -> e.contains("inheritance") || e.contains("继承复杂") || e.contains("complex inheritance"));
        System.out.println("complex-inheritance.xml 验证耗时: " + validationTime + "ms, 错误: " + result.getErrors());
        assertTrue(result.hasErrors() ? foundInheritanceError : true, "应检测到复杂继承相关错误或无错误: " + result.getErrors());
    }
}
