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
        testPerformanceEdgeCase("large-schema.xml");
    }

    @Test
    public void testDeepNesting() {
        testPerformanceEdgeCase("deep-nesting.xml");
    }

    @Test
    public void testManyReferences() {
        testPerformanceEdgeCase("many-references.xml");
    }

    @Test
    public void testComplexInheritance() {
        testPerformanceEdgeCase("complex-inheritance.xml");
    }

    /**
     * Helper method to test a specific performance edge case file
     */
    private void testPerformanceEdgeCase(String fileName) {
        Path testFilePath = Paths.get(PERFORMANCE_EDGE_CASES_DIR, fileName);
        File xmlFile = testFilePath.toFile();

        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);

        // Measure validation time for performance analysis
        long startTime = System.currentTimeMillis();

        XmlComplianceResult result = validator.validateFile(xmlFile);

        long validationTime = System.currentTimeMillis() - startTime;

        assertNotNull(result, "Result should not be null");

        // Log the result for debugging
        System.out.println("Validated: " + fileName + " - Compliant: " + result.isCompliant() +
                          " - Errors: " + result.getErrorCount() + " - Warnings: " + result.getWarningCount() +
                          " - Time: " + validationTime + "ms");
        if (!result.getErrors().isEmpty()) {
            System.out.println("  Errors: " + result.getErrors());
        }

        // For performance edge case files, we're mainly testing that validation completes
        // within reasonable time bounds
        assertTrue(validationTime < 30000, "Validation should complete within reasonable time (< 30 seconds)");

        // At minimum, the validation should complete without throwing exceptions
        assertNotNull(result, "Validation should complete successfully");
    }
}
