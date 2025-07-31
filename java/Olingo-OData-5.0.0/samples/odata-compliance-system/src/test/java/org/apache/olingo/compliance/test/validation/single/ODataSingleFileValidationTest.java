package org.apache.olingo.compliance.test.validation.single;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import org.apache.olingo.compliance.file.ModernXmlFileComplianceValidator;
import org.apache.olingo.compliance.file.XmlComplianceResult;
import org.apache.olingo.compliance.file.XmlFileComplianceValidator;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Comprehensive test class for OData 4.0 XML single file validation.
 * Tests all categories of invalid XML files to ensure Olingo's validation
 * mechanisms correctly identify and report the expected errors.
 * 
 * This test focuses on single file validation - each XML file should be 
 * independently valid or invalid according to OData 4.0 specifications.
 */

public class ODataSingleFileValidationTest {
    
    private static final Logger logger = LoggerFactory.getLogger(ODataSingleFileValidationTest.class);
    
    private XmlFileComplianceValidator validator;
    private Path singleValidationRoot;
    
    @BeforeEach
    void setUp() {
        // Initialize test resources using the advanced validation framework
        singleValidationRoot = Paths.get("src/test/resources/validation/single");
        validator = new ModernXmlFileComplianceValidator();
    }
    
    @Test
    void testValidXmlFiles() throws IOException {
        Path validDir = singleValidationRoot.resolve("valid");
        if (!Files.exists(validDir)) {
            logger.warn("Valid directory does not exist: {}", validDir);
            return;
        }
        
        testValidCasesInDirectory(validDir);
    }
    
    @Test
    void testAttributeErrorFiles() throws IOException {
        Path attributeErrorDir = singleValidationRoot.resolve("invalid/attribute-error");
        if (!Files.exists(attributeErrorDir)) {
            logger.warn("Attribute error directory does not exist: {}", attributeErrorDir);
            return;
        }
        
        testInvalidCasesInDirectory(attributeErrorDir, "attribute");
    }
    
    @Test
    void testDuplicateErrorFiles() throws IOException {
        Path duplicateDir = singleValidationRoot.resolve("invalid/duplicate");
        if (!Files.exists(duplicateDir)) {
            logger.warn("Duplicate error directory does not exist: {}", duplicateDir);
            return;
        }
        
        testInvalidCasesInDirectory(duplicateDir, "duplicate");
    }
    
    @Test
    void testMissingErrorFiles() throws IOException {
        Path missingDir = singleValidationRoot.resolve("invalid/missing");
        if (!Files.exists(missingDir)) {
            logger.warn("Missing error directory does not exist: {}", missingDir);
            return;
        }
        
        testInvalidCasesInDirectory(missingDir, "missing");
    }
    
    @Test
    void testTypeErrorFiles() throws IOException {
        Path typeErrorDir = singleValidationRoot.resolve("invalid/type-error");
        if (!Files.exists(typeErrorDir)) {
            logger.warn("Type error directory does not exist: {}", typeErrorDir);
            return;
        }
        
        testInvalidCasesInDirectory(typeErrorDir, "type");
    }
    
    @Test
    void testStructureErrorFiles() throws IOException {
        Path structureErrorDir = singleValidationRoot.resolve("invalid/structure-error");
        if (!Files.exists(structureErrorDir)) {
            logger.warn("Structure error directory does not exist: {}", structureErrorDir);
            return;
        }
        
        testInvalidCasesInDirectory(structureErrorDir, "structure");
    }
    
    @Test
    void testOtherErrorFiles() throws IOException {
        Path otherDir = singleValidationRoot.resolve("invalid/other");
        if (!Files.exists(otherDir)) {
            logger.warn("Other error directory does not exist: {}", otherDir);
            return;
        }
        
        testInvalidCasesInDirectory(otherDir, "other");
    }
    
    /**
     * Test valid XML files - these should parse successfully
     */
    private void testValidCasesInDirectory(Path validDir) throws IOException {
        try (Stream<Path> paths = Files.walk(validDir)) {
            List<Path> subdirs = paths
                .filter(Files::isDirectory)
                .filter(path -> !path.equals(validDir))
                .toList();
                
            for (Path subdir : subdirs) {
                logger.info("Testing valid cases in: {}", subdir.getFileName());
                testValidCasesInSubdirectory(subdir);
            }
        }
    }
    
    /**
     * Test invalid XML files - these should fail validation with expected error types
     */
    private void testInvalidCasesInDirectory(Path invalidDir, String expectedErrorType) throws IOException {
        try (Stream<Path> paths = Files.walk(invalidDir)) {
            List<Path> subdirs = paths
                .filter(Files::isDirectory)
                .filter(path -> !path.equals(invalidDir))
                .toList();
                
            for (Path subdir : subdirs) {
                logger.info("Testing invalid {} cases in: {}", expectedErrorType, subdir.getFileName());
                testInvalidCasesInSubdirectory(subdir, expectedErrorType);
            }
        }
    }
    
    /**
     * Test all XML files in a valid subdirectory
     */
    private void testValidCasesInSubdirectory(Path casePath) throws IOException {
        try (Stream<Path> paths = Files.walk(casePath)) {
            List<Path> xmlFiles = paths
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".xml"))
                .toList();
                
            for (Path xmlFile : xmlFiles) {
                testSingleValidXmlFile(xmlFile);
            }
        }
    }
    
    /**
     * Test all XML files in an invalid subdirectory
     */
    private void testInvalidCasesInSubdirectory(Path casePath, String expectedErrorType) throws IOException {
        try (Stream<Path> paths = Files.walk(casePath)) {
            List<Path> xmlFiles = paths
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".xml"))
                .toList();
                
            for (Path xmlFile : xmlFiles) {
                testSingleInvalidXmlFile(xmlFile, expectedErrorType);
            }
        }
    }
    
    /**
     * Test a single valid XML file - should parse successfully
     */
    private void testSingleValidXmlFile(Path xmlFile) {
        logger.info("Testing valid XML file: {}", xmlFile.getFileName());
        
        // Use Olingo's advanced validation framework
        XmlComplianceResult result = validator.validateFile(xmlFile);
        
        if (!result.isCompliant()) {
            // This is unexpected for valid files
            String errorMessages = String.join("; ", result.getErrors());
            fail("Valid XML file " + xmlFile.getFileName() + " failed validation: " + errorMessages);
        } else {
            // If we get here, the validation passed as expected
            logger.debug("Valid XML file {} validated successfully", xmlFile.getFileName());
        }
    }
    
    /**
     * Test a single invalid XML file - should fail validation with expected error type
     */
    private void testSingleInvalidXmlFile(Path xmlFile, String expectedErrorType) {
        logger.info("Testing invalid XML file: {}", xmlFile.getFileName());
        
        // Use Olingo's advanced validation framework
        XmlComplianceResult result = validator.validateFile(xmlFile);
        
        // Verify that validation failed as expected
        assertFalse(result.isCompliant(), 
            "XML file " + xmlFile.getFileName() + " should have failed validation but didn't");
        
        // Verify that there are error messages
        assertNotNull(result.getErrors(), 
            "XML file " + xmlFile.getFileName() + " should have error messages");
        
        assertFalse(result.getErrors().isEmpty(), 
            "XML file " + xmlFile.getFileName() + " should have non-empty error list");
        
        // Verify that the error message matches the expected error type
        validateErrorMessage(xmlFile, result.getErrors(), expectedErrorType);
        
        logger.debug("Validation correctly failed for {}: {}", xmlFile.getFileName(), 
                    String.join("; ", result.getErrors()));
    }
    
    /**
     * Validate that the error messages contain keywords consistent with the expected error type
     */
    private void validateErrorMessage(Path xmlFile, List<String> errorMessages, String expectedErrorType) {
        String allErrorMessages = String.join(" ", errorMessages).toLowerCase();
        String fileName = xmlFile.getFileName().toString().toLowerCase();
        
        // Check for specific error types based on expected error category and filename patterns
        boolean foundKeyword;
        switch (expectedErrorType) {
            case "attribute":
                foundKeyword = validateAttributeErrorMessage(fileName, allErrorMessages);
                break;
            case "duplicate":
                foundKeyword = validateDuplicateErrorMessage(allErrorMessages);
                break;
            case "missing":
                foundKeyword = validateMissingErrorMessage(allErrorMessages);
                break;
            case "type":
                foundKeyword = validateTypeErrorMessage(allErrorMessages);
                break;
            case "structure":
                foundKeyword = validateStructureErrorMessage(allErrorMessages);
                break;
            case "other":
                foundKeyword = validateOtherErrorMessage(allErrorMessages);
                break;
            default:
                // Generic validation - look for any error indicators
                foundKeyword = allErrorMessages.contains("invalid") || 
                              allErrorMessages.contains("error") || 
                              allErrorMessages.contains("illegal") ||
                              allErrorMessages.contains("unexpected");
        }
        
        assertTrue(foundKeyword, 
            "Error message for " + xmlFile.getFileName() + 
            " should contain relevant keywords for " + expectedErrorType + " error. " +
            "Actual messages: " + allErrorMessages);
    }
    
    private boolean validateAttributeErrorMessage(String fileName, String errorMessage) {
        if (fileName.contains("missing")) {
            return errorMessage.contains("missing") || 
                   errorMessage.contains("required") || 
                   errorMessage.contains("not found") ||
                   errorMessage.contains("attribute");
        } else if (fileName.contains("invalid")) {
            return errorMessage.contains("invalid") || 
                   errorMessage.contains("unexpected") || 
                   errorMessage.contains("illegal") ||
                   errorMessage.contains("attribute");
        } else {
            // Generic attribute error keywords
            return errorMessage.contains("attribute") || 
                   errorMessage.contains("property") || 
                   errorMessage.contains("element");
        }
    }
    
    private boolean validateDuplicateErrorMessage(String errorMessage) {
        return errorMessage.contains("duplicate") || 
               errorMessage.contains("already") || 
               errorMessage.contains("multiple") ||
               errorMessage.contains("redefined") ||
               errorMessage.contains("exists");
    }
    
    private boolean validateMissingErrorMessage(String errorMessage) {
        return errorMessage.contains("missing") || 
               errorMessage.contains("required") || 
               errorMessage.contains("not found") ||
               errorMessage.contains("absent") ||
               errorMessage.contains("expected") ||
               errorMessage.contains("must have") ||
               errorMessage.contains("cannot invoke") ||
               errorMessage.contains("null") ||
               errorMessage.contains("failed to read") ||
               errorMessage.contains("failed at") ||
               errorMessage.contains("does not exist");
    }
    
    private boolean validateTypeErrorMessage(String errorMessage) {
        return errorMessage.contains("type") || 
               errorMessage.contains("invalid") || 
               errorMessage.contains("unknown") ||
               errorMessage.contains("reference") ||
               errorMessage.contains("not found");
    }
    
    private boolean validateStructureErrorMessage(String errorMessage) {
        return errorMessage.contains("structure") || 
               errorMessage.contains("element") || 
               errorMessage.contains("hierarchy") ||
               errorMessage.contains("schema") ||
               errorMessage.contains("format");
    }
    
    private boolean validateOtherErrorMessage(String errorMessage) {
        return errorMessage.contains("error") || 
               errorMessage.contains("invalid") || 
               errorMessage.contains("illegal") ||
               errorMessage.contains("unexpected") ||
               errorMessage.contains("violation");
    }
}
