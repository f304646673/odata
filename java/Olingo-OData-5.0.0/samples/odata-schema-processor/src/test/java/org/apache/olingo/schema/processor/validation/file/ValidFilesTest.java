package org.apache.olingo.schema.processor.validation.file;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for valid XML files using XmlFileComplianceValidator.
 * Tests files from src/test/resources/validator/00-valid-schemas/
 */
public class ValidFilesTest {

    private XmlFileComplianceValidator validator;
    private static final String VALID_SCHEMAS_DIR = "src/test/resources/validator/file/00-valid-schemas";

    @BeforeEach
    public void setUp() {
        validator = new OlingoXmlFileComplianceValidator();
    }

    @Test
    public void testCompleteValidSchema() {
        testValidXmlFile("complete-valid-schema.xml");
    }

    @Test
    public void testComplexTypeSchema() {
        testValidXmlFile("complex-type-schema.xml");
    }

    @Test
    public void testMinimalValidSchema() {
        testValidXmlFile("minimal-valid-schema.xml");
    }

    @Test
    public void testCrossNamespaceReference() {
        testValidXmlFile("cross-namespace-reference/common/CommonTypes.xml");
        testValidXmlFile("cross-namespace-reference/business/BusinessEntities.xml");
    }

    @Test
    public void testCircularReference() {
        testValidXmlFile("circular-reference/a/A.xml");
        testValidXmlFile("circular-reference/b/B.xml");
    }

    @Test
    public void testAnnotationInclude() {
        testValidXmlFile("annotation-include/Annotations.xml");
        testValidXmlFile("annotation-include/MainWithAnnotations.xml");
    }

    @Test
    public void testCrossFileInheritance() {
        testValidXmlFile("cross-file-inheritance/Base.xml");
        testValidXmlFile("cross-file-inheritance/Derived.xml");
    }

    @Test
    public void testSharedTypeReference() {
        testValidXmlFile("shared-type-reference/shared/SharedTypes.xml");
        testValidXmlFile("shared-type-reference/Main.xml");
    }


    @Test
    public void testCircularDependencyA() {
        testValidXmlFile("circular-dependency/circular-dependency-a.xml");
        testValidXmlFile("circular-dependency/circular-dependency-b.xml");
        testValidXmlFile("circular-dependency/circular-dependency-c.xml");
    }

    /**
     * Helper method to test a specific valid XML file
     */
    private void testValidXmlFile(String fileName) {
        Path testFilePath = Paths.get(VALID_SCHEMAS_DIR, fileName);
        File xmlFile = testFilePath.toFile();

        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);

        XmlComplianceResult result = validator.validateFile(xmlFile);

        assertNotNull(result, "Result should not be null");

        // Log the result for debugging
        System.out.println("Testing valid file: " + fileName);
        System.out.println("  Compliant: " + result.isCompliant());
        System.out.println("  Errors: " + result.getErrorCount());
        System.out.println("  Warnings: " + result.getWarningCount());

        if (result.hasErrors()) {
            System.out.println("  Error details: " + result.getErrors());
        }

        // For valid files, we expect them to be compliant
        assertTrue(result.isCompliant(), "Valid file should be compliant: " + fileName);
        assertTrue(!result.hasErrors(), "Valid file should have no errors: " + fileName);
    }
}
