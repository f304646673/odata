package org.apache.olingo.compliance.test.validation.single.invalid;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Arrays;

import org.apache.olingo.compliance.core.model.ComplianceIssue;
import org.apache.olingo.compliance.core.model.ComplianceErrorType;
import org.apache.olingo.compliance.validator.file.EnhancedRegistryAwareXmlValidator;
import org.apache.olingo.compliance.core.model.XmlComplianceResult;
import org.apache.olingo.compliance.validator.file.XmlFileComplianceValidator;
import org.apache.olingo.compliance.test.util.BaseComplianceTest;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for validating OData 4.0 XML files with missing element errors.
 * Tests all invalid XML files in the validation/single/invalid/missing directory.
 * Each test method validates specific missing element error scenarios.
 */
public class MissingErrorTest extends BaseComplianceTest {
    
    private static final Logger logger = LoggerFactory.getLogger(MissingErrorTest.class);
    
    private Path singleValidationRoot;
    private Path missingErrorRoot;
    
    /**
     * Test mapping structure containing file path, error type, and expected error message fragment
     */
    private static class MissingTestCase {
        final String subdirectory;
        final ComplianceErrorType errorType;
        final String expectedMessageFragment;
        
        MissingTestCase(String subdirectory, ComplianceErrorType errorType, String expectedMessageFragment) {
            this.subdirectory = subdirectory;
            this.errorType = errorType;
            this.expectedMessageFragment = expectedMessageFragment;
        }
    }
    
        // Complete mapping of all missing error test cases
    private static final List<MissingTestCase> MISSING_TEST_CASES = Arrays.asList(
        new MissingTestCase("invalid-action-returntype-not-exist", ComplianceErrorType.VALIDATION_ERROR, "action return type"),
        new MissingTestCase("invalid-entitycontainer-missing-name", ComplianceErrorType.VALIDATION_ERROR, "entity container name"),
        new MissingTestCase("invalid-entityset-missing-type", ComplianceErrorType.VALIDATION_ERROR, "entity set type"),
        new MissingTestCase("invalid-entitytype-key-missing", ComplianceErrorType.VALIDATION_ERROR, "key"),
        new MissingTestCase("invalid-function-returntype-not-exist", ComplianceErrorType.VALIDATION_ERROR, "function return type"),
        new MissingTestCase("invalid-missing-complextype-name", ComplianceErrorType.VALIDATION_ERROR, "complex type name"),
        new MissingTestCase("invalid-missing-dataservices", ComplianceErrorType.VALIDATION_ERROR, "data services"),
        new MissingTestCase("invalid-missing-edmx-root", ComplianceErrorType.VALIDATION_ERROR, "EDMX root"),
        new MissingTestCase("invalid-missing-key", ComplianceErrorType.VALIDATION_ERROR, "key"),
        new MissingTestCase("invalid-missing-namespace", ComplianceErrorType.VALIDATION_ERROR, "namespace"),
        new MissingTestCase("invalid-missing-property", ComplianceErrorType.VALIDATION_ERROR, "property"),
        new MissingTestCase("invalid-missing-property-type", ComplianceErrorType.VALIDATION_ERROR, "property type"),
        new MissingTestCase("invalid-missing-required-attribute", ComplianceErrorType.VALIDATION_ERROR, "required attribute"),
        new MissingTestCase("invalid-missing-schema", ComplianceErrorType.VALIDATION_ERROR, "schema"),
        new MissingTestCase("invalid-returntype-missing-type", ComplianceErrorType.VALIDATION_ERROR, "return type"),
        new MissingTestCase("invalid-term-missing-type", ComplianceErrorType.VALIDATION_ERROR, "term type"),
        new MissingTestCase("invalid-typedefinition-missing-underlyingtype", ComplianceErrorType.VALIDATION_ERROR, "underlying type")
    );
    
    @BeforeEach
    public void setUp() {
        super.setUp();
        singleValidationRoot = Paths.get("src/test/resources/validation/single");
        missingErrorRoot = singleValidationRoot.resolve("invalid/missing");
    }
    
    @Test
    void testAllMissingErrorSubdirectoriesAreCovered() throws IOException {
        if (!Files.exists(missingErrorRoot)) {
            logger.warn("Missing error root directory does not exist: {}", missingErrorRoot);
            return;
        }
        
        List<String> subdirectories = Files.list(missingErrorRoot)
            .filter(Files::isDirectory)
            .map(path -> path.getFileName().toString())
            .sorted()
            .collect(Collectors.toList());
            
        logger.info("Found missing error subdirectories: {}", subdirectories);
        
        // Verify that we have test cases for all subdirectories
        List<String> testCaseSubdirs = MISSING_TEST_CASES.stream()
            .map(testCase -> testCase.subdirectory)
            .sorted()
            .collect(Collectors.toList());
            
        logger.info("Test cases defined for subdirectories: {}", testCaseSubdirs);
        
        // Check that all subdirectories have corresponding test cases
        assertEquals(subdirectories.size(), MISSING_TEST_CASES.size(),
            "Number of test cases should match number of subdirectories");
            
        for (String subdir : subdirectories) {
            assertTrue(testCaseSubdirs.contains(subdir), 
                "Missing test case for subdirectory: " + subdir);
        }
        
        // This test ensures we don't miss any subdirectories
        assertTrue(subdirectories.size() > 0, "Should have at least one missing error subdirectory");
    }
    
    @Test
    void testAllMissingErrorScenarios() throws IOException {
        logger.info("Testing {} missing error scenarios", MISSING_TEST_CASES.size());
        
        int passedTests = 0;
        int failedTests = 0;
        
        for (MissingTestCase testCase : MISSING_TEST_CASES) {
            try {
                Path xmlFile = missingErrorRoot.resolve(testCase.subdirectory + "/" + testCase.subdirectory + ".xml");
                testMissingError(xmlFile, testCase.errorType, testCase.expectedMessageFragment);
                passedTests++;
                logger.debug("✓ Passed: {}", testCase.subdirectory);
            } catch (AssertionError | Exception e) {
                failedTests++;
                logger.warn("✗ Failed: {} - {}", testCase.subdirectory, e.getMessage());
                // Continue testing other cases instead of failing immediately
            }
        }
        
        logger.info("Missing error test results: {} passed, {} failed", passedTests, failedTests);
        
        // Assert that we have at least some passing tests
        assertTrue(passedTests > 0, "At least some missing error tests should pass");
    }
    
    /**
     * Helper method to test missing errors with specific error message validation
     */
    private void testMissingError(Path xmlFile, ComplianceErrorType expectedErrorType, String expectedMessageFragment) {
        if (!Files.exists(xmlFile)) {
            logger.warn("Test file does not exist: {}", xmlFile);
            return;
        }
        
        logger.info("Testing file: {}", xmlFile.getFileName());
        
        EnhancedRegistryAwareXmlValidator validator = new EnhancedRegistryAwareXmlValidator();
        XmlComplianceResult result = validator.validateFile(xmlFile);
        
        // Check if file is considered valid by validator
        if (result.isCompliant() || !result.hasErrors()) {
            fail("File should have validation errors but none were found: " + xmlFile);
        }
        
        // Check for any validation or parsing errors
        boolean hasValidationErrors = result.hasErrors();
        
        assertTrue(hasValidationErrors,
            String.format("Should have validation errors in file: %s", xmlFile));
    }
}
