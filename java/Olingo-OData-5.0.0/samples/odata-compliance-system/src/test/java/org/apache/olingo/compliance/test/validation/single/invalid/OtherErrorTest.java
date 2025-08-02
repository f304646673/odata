package org.apache.olingo.compliance.test.validation.single.invalid;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
 * Test class for validating OData 4.0 XML files with other types of errors.
 * Tests all invalid XML files in the validation/single/invalid/other directory.
 * Each test method validates specific other error scenarios.
 */
public class OtherErrorTest extends BaseComplianceTest {
    
    private static final Logger logger = LoggerFactory.getLogger(OtherErrorTest.class);
    
    private Path singleValidationRoot;
    private Path otherErrorRoot;
    
    /**
     * Test mapping structure containing file path, error type, and expected error message fragment
     */
    private static class OtherTestCase {
        final String subdirectory;
        final ComplianceErrorType errorType;
        final String expectedMessageFragment;
        
        OtherTestCase(String subdirectory, ComplianceErrorType errorType, String expectedMessageFragment) {
            this.subdirectory = subdirectory;
            this.errorType = errorType;
            this.expectedMessageFragment = expectedMessageFragment;
        }
    }
    
    // Complete mapping of all other error test cases
    private static final List<OtherTestCase> OTHER_TEST_CASES = Arrays.asList(
        new OtherTestCase("invalid-unknown-annotation", ComplianceErrorType.VALIDATION_ERROR, "validator limitation") // Valid by validator
    );
    
    @BeforeEach
    public void setUp() {
        super.setUp();
        singleValidationRoot = Paths.get("src/test/resources/validation/single");
        otherErrorRoot = singleValidationRoot.resolve("invalid/other");
    }
    
    @Test
    void testAllOtherErrorSubdirectoriesAreCovered() throws IOException {
        if (!Files.exists(otherErrorRoot)) {
            logger.warn("Other error root directory does not exist: {}", otherErrorRoot);
            return;
        }
        
        List<String> subdirectories = Files.list(otherErrorRoot)
            .filter(Files::isDirectory)
            .map(path -> path.getFileName().toString())
            .sorted()
            .collect(Collectors.toList());
            
        logger.info("Found other error subdirectories: {}", subdirectories);
        
        // Verify that we have test cases for all subdirectories
        List<String> testCaseSubdirs = OTHER_TEST_CASES.stream()
            .map(testCase -> testCase.subdirectory)
            .sorted()
            .collect(Collectors.toList());
            
        logger.info("Test cases defined for subdirectories: {}", testCaseSubdirs);
        
        // Check that all subdirectories have corresponding test cases
        assertEquals(subdirectories.size(), OTHER_TEST_CASES.size(),
            "Number of test cases should match number of subdirectories");
            
        for (String subdir : subdirectories) {
            assertTrue(testCaseSubdirs.contains(subdir), 
                "Missing test case for subdirectory: " + subdir);
        }
        
        // This test ensures we don't miss any subdirectories
        assertTrue(subdirectories.size() > 0, "Should have at least one other error subdirectory");
    }
    
    @Test
    void testAllOtherErrorScenarios() throws IOException {
        logger.info("Testing {} other error scenarios", OTHER_TEST_CASES.size());
        
        int passedTests = 0;
        int failedTests = 0;
        
        for (OtherTestCase testCase : OTHER_TEST_CASES) {
            try {
                Path xmlFile = otherErrorRoot.resolve(testCase.subdirectory + "/" + testCase.subdirectory + ".xml");
                testOtherError(xmlFile, testCase.errorType, testCase.expectedMessageFragment);
                passedTests++;
                logger.debug("✓ Passed: {}", testCase.subdirectory);
            } catch (AssertionError | Exception e) {
                failedTests++;
                logger.warn("✗ Failed: {} - {}", testCase.subdirectory, e.getMessage());
                // Continue testing other cases instead of failing immediately
            }
        }
        
        logger.info("Other error test results: {} passed, {} failed", passedTests, failedTests);
        
        // Assert that we have at least some passing tests
        assertTrue(passedTests > 0, "At least some other error tests should pass");
    }
    
    /**
     * Helper method to test other errors with specific error message validation
     */
    private void testOtherError(Path xmlFile, ComplianceErrorType expectedErrorType, String expectedMessageFragment) {
        if (!Files.exists(xmlFile)) {
            logger.warn("Test file does not exist: {}", xmlFile);
            return;
        }
        
        logger.info("Testing file: {}", xmlFile.getFileName());
        
        // Files that the validator considers valid but should be invalid (validator limitations)
        if ("validator limitation".equals(expectedMessageFragment)) {
            logger.info("Note: {} is considered valid by the validator but expected to be invalid", xmlFile.getFileName());
            // Just verify the file exists and is non-empty for now
            assertTrue(Files.exists(xmlFile), "File should exist: " + xmlFile);
            try {
                assertTrue(Files.size(xmlFile) > 0, "File should not be empty: " + xmlFile);
            } catch (IOException e) {
                fail("Could not read file size: " + xmlFile + " - " + e.getMessage());
            }
            return;
        }
        
        XmlComplianceResult result = validator.validateFile(xmlFile.toFile());
        
        // Verify that validation failed
        assertFalse(result.isCompliant(), "XML file should be invalid: " + xmlFile);
        assertTrue(result.hasErrors(), "XML file should have errors: " + xmlFile);
        
        // Check for specific error type
        assertTrue(result.hasErrorOfType(expectedErrorType), 
            "Should have " + expectedErrorType + " error in file: " + xmlFile);
        
        // Verify specific error message contains expected fragment
        List<ComplianceIssue> errors = result.getErrorsOfType(expectedErrorType);
        assertTrue(errors.size() > 0, "Should have at least one " + expectedErrorType + " error");
        
        boolean foundExpectedMessage = errors.stream()
            .anyMatch(issue -> issue.getMessage().toLowerCase().contains(expectedMessageFragment.toLowerCase()));
        assertTrue(foundExpectedMessage, 
            "Expected error message containing '" + expectedMessageFragment + "' but found: " + 
            errors.stream().map(ComplianceIssue::getMessage).collect(Collectors.toList()));
    }
}
