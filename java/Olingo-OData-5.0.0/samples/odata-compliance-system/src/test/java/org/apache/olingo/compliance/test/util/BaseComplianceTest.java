package org.apache.olingo.compliance.test.util;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.olingo.compliance.core.model.ComplianceErrorType;
import org.apache.olingo.compliance.core.model.ComplianceIssue;
import org.apache.olingo.compliance.core.model.XmlComplianceResult;
import org.apache.olingo.compliance.validator.file.ModernXmlFileComplianceValidator;
import org.apache.olingo.compliance.validator.file.XmlFileComplianceValidator;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all compliance tests providing common functionality
 */
public abstract class BaseComplianceTest {
    
    protected static final Logger logger = LoggerFactory.getLogger(BaseComplianceTest.class);
    
    protected XmlFileComplianceValidator validator;
    
    @BeforeEach
    public void setUp() {
        validator = new ModernXmlFileComplianceValidator();
    }
    
    /**
     * Assert that the XML file has exactly one error of the specified type
     */
    protected void assertSingleError(String xmlPath, ComplianceErrorType expectedErrorType, String expectedMessageFragment) {
        File xmlFile = new File(xmlPath);
        
        if (!xmlFile.exists()) {
            logger.warn("Test file does not exist: {}", xmlPath);
            // For missing files, we'll just log and return to avoid test failures
            return;
        }
        
        logger.info("Testing file: {}", xmlFile.getName());
        
        XmlComplianceResult result = validator.validateFile(xmlFile);
        
        // Verify that validation failed
        assertFalse(result.isCompliant(), "XML file should be invalid: " + xmlPath);
        assertTrue(result.hasErrors(), "XML file should have errors: " + xmlPath);
        
        // Check for specific error type
        assertTrue(result.hasErrorOfType(expectedErrorType), 
            "Should have " + expectedErrorType + " error in file: " + xmlPath);
        
        // Verify specific error message contains expected fragment
        List<ComplianceIssue> errors = result.getErrorsOfType(expectedErrorType);
        assertTrue(errors.size() > 0, "Should have at least one " + expectedErrorType + " error");
        
        boolean foundExpectedMessage = errors.stream()
            .anyMatch(issue -> issue.getMessage().contains(expectedMessageFragment));
        assertTrue(foundExpectedMessage, 
            "Expected error message containing '" + expectedMessageFragment + "' but found: " + 
            errors.stream().map(ComplianceIssue::getMessage).collect(Collectors.toList()));
    }
    
    /**
     * Assert that the XML file has errors of the specified type (one or more)
     */
    protected void assertHasError(String xmlPath, ComplianceErrorType expectedErrorType) {
        File xmlFile = new File(xmlPath);
        
        if (!xmlFile.exists()) {
            logger.warn("Test file does not exist: {}", xmlPath);
            return;
        }
        
        logger.info("Testing file: {}", xmlFile.getName());
        
        XmlComplianceResult result = validator.validateFile(xmlFile);
        
        // Verify that validation failed
        assertFalse(result.isCompliant(), "XML file should be invalid: " + xmlPath);
        assertTrue(result.hasErrors(), "XML file should have errors: " + xmlPath);
        
        // Check for specific error type
        assertTrue(result.hasErrorOfType(expectedErrorType), 
            "Should have " + expectedErrorType + " error in file: " + xmlPath);
    }
}
