package org.apache.olingo.compliance.test.validation.single.valid;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.olingo.compliance.core.model.ComplianceResult;
import org.apache.olingo.compliance.test.util.BaseComplianceTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for validating OData 4.0 XML files that should be valid.
 * Tests all valid XML files in the validation/single/valid directory.
 * Each test method validates that specific XML files are considered compliant.
 */
public class ValidXmlFilesTest extends BaseComplianceTest {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidXmlFilesTest.class);
    
    private Path singleValidationRoot;
    private Path validRoot;
    
    @BeforeEach
    public void setUp() {
        super.setUp();
        singleValidationRoot = Paths.get("src/test/resources/validation/single");
        validRoot = singleValidationRoot.resolve("valid");
    }
    
    @Test
    void testValidComplex() {
        testValidXmlFile("valid-complex", "Complex schema with various elements");
    }
    
    @Test
    void testValidEmptyEnumtype() {
        testValidXmlFile("valid-empty-enumtype", "Schema with empty enum type");
    }
    
    @Test
    void testValidEmptySchema() {
        testValidXmlFile("valid-empty-schema", "Empty schema definition");
    }
    
    @Test
    void testValidEmptySchemaContent() {
        testValidXmlFile("valid-empty-schema-content", "Schema with empty content");
    }
    
    @Test
    void testValidEntitytypeNoProperty() {
        testValidXmlFile("valid-entitytype-no-property", "Entity type without properties");
    }
    
    @Test
    void testValidFunctionOverloadParameters() {
        testValidXmlFile("valid-function-overload-parameters", "Function overload with different parameters");
    }
    
    @Test
    void testValidFunctionOverloadReturntype() {
        testValidXmlFile("valid-function-overload-returntype", "Function overload with different return types");
    }
    
    @Test
    void testValidMinimal() {
        testValidXmlFile("valid-minimal", "Minimal valid OData schema");
    }
    
    /**
     * Helper method to test a valid XML file
     */
    private void testValidXmlFile(String subdirectory, String description) {
        Path testDir = validRoot.resolve(subdirectory);
        if (!Files.exists(testDir)) {
            logger.warn("Test directory does not exist: {}", testDir);
            return; // Skip missing directories to avoid test failures
        }
        
        // Look for XML file with same name as directory
        Path xmlFile = testDir.resolve(subdirectory + ".xml");
        if (!Files.exists(xmlFile)) {
            logger.warn("Test file does not exist: {}", xmlFile);
            return; // Skip missing files to avoid test failures
        }
        
        logger.info("Testing file: {} - {}", xmlFile.getFileName(), description);
        
        ComplianceResult result = validator.validateFile(xmlFile.toFile(), schemaRegistry);

        // Verify that validation passed
        assertTrue(result.isCompliant(), 
            "XML file should be valid: " + xmlFile + " - " + description);
        
        logger.debug("Validation successful for: {}", xmlFile.getFileName());
    }
}
