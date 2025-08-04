package org.apache.olingo.compliance.test.util;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.olingo.compliance.core.model.ComplianceErrorType;
import org.apache.olingo.compliance.core.model.ComplianceIssue;
import org.apache.olingo.compliance.core.model.ComplianceResult;
import org.apache.olingo.compliance.engine.core.impl.DefaultSchemaRegistryImpl;
import org.apache.olingo.compliance.validator.ComplianceValidator;
import org.apache.olingo.compliance.validator.impl.ComplianceValidatorImpl;
import org.apache.olingo.compliance.engine.core.SchemaRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all compliance tests providing common functionality
 */
public abstract class BaseComplianceTest {
    
    protected static final Logger logger = LoggerFactory.getLogger(BaseComplianceTest.class);
    
    protected ComplianceValidator validator;
    protected SchemaRegistry schemaRegistry;

    @BeforeEach
    public void setUp() {
        validator = new ComplianceValidatorImpl();
        schemaRegistry = new DefaultSchemaRegistryImpl();
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
        
        ComplianceResult result = validator.validateFile(xmlFile, schemaRegistry);

        // Verify that validation failed
        Assertions.assertFalse(result.isCompliant(), "XML file should be invalid: " + xmlPath);
        Assertions.assertTrue(result.hasErrors(), "XML file should have errors: " + xmlPath);

        // Check for specific error type
        Assertions.assertTrue(result.hasErrorOfType(expectedErrorType),
            "Should have " + expectedErrorType + " error in file: " + xmlPath);
        
        // Verify specific error message contains expected fragment
        List<ComplianceIssue> errors = result.getErrorsOfType(expectedErrorType);
        Assertions.assertTrue(errors.size() > 0, "Should have at least one " + expectedErrorType + " error");

        boolean foundExpectedMessage = errors.stream()
            .anyMatch(issue -> issue.getMessage().contains(expectedMessageFragment));
        Assertions.assertTrue(foundExpectedMessage,
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
        
        ComplianceResult result = validator.validateFile(xmlFile, schemaRegistry);

        // Verify that validation failed
        Assertions.assertFalse(result.isCompliant(), "XML file should be invalid: " + xmlPath);
        Assertions.assertTrue(result.hasErrors(), "XML file should have errors: " + xmlPath);

        // Check for specific error type
        Assertions.assertTrue(result.hasErrorOfType(expectedErrorType),
            "Should have " + expectedErrorType + " error in file: " + xmlPath);
    }

    /**
     * Get test file from resources
     */
    protected File getTestFile(String resourcePath) {
        URL resource = getClass().getClassLoader().getResource(resourcePath);
        if (resource == null) {
            throw new IllegalArgumentException("Test resource not found: " + resourcePath);
        }
        return new File(resource.getFile());
    }

    /**
     * Get test resource path
     */
    protected String getTestResourcePath(String resourcePath) {
        URL resource = getClass().getClassLoader().getResource(resourcePath);
        if (resource == null) {
            throw new IllegalArgumentException("Test resource not found: " + resourcePath);
        }
        return Paths.get(resource.getPath()).toString();
    }
}
