package org.apache.olingo.schema.processor.validation;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

/**
 * Test class for security vulnerabilities using XmlFileComplianceValidator
 */
@RunWith(Parameterized.class)
public class SecurityVulnerabilitiesTest {
    
    private final XmlFileComplianceValidator validator;
    private final Path testFilePath;
    
    public SecurityVulnerabilitiesTest(Path testFilePath) {
        this.validator = new OlingoXmlFileComplianceValidator();
        this.testFilePath = testFilePath;
    }
    
    @Before
    public void setUp() {
        // Additional setup if needed
    }
    
    @Parameterized.Parameters(name = "{0}")
    public static List<Path> getTestFiles() throws Exception {
        List<Path> testFiles = new ArrayList<>();
        Path errorFilesDir = Paths.get("src/test/resources/validator/08-security-vulnerabilities");
        
        if (Files.exists(errorFilesDir)) {
            try (Stream<Path> files = Files.list(errorFilesDir)) {
                files.filter(path -> path.toString().endsWith(".xml"))
                     .forEach(testFiles::add);
            }
        }
        
        return testFiles;
    }
    
    @Test
    public void testSecurityVulnerability() {
        File xmlFile = testFilePath.toFile();
        assertTrue("Test file should exist: " + testFilePath, xmlFile.exists());
        
        XmlComplianceResult result = validator.validateFile(xmlFile);
        
        assertNotNull("Result should not be null", result);
        // Security vulnerabilities should be caught during parsing
        assertFalse("Security vulnerability file should not be compliant: " + testFilePath.getFileName(), 
                   result.isCompliant());
        assertTrue("Security vulnerability file should have errors: " + testFilePath.getFileName(), 
                  result.getErrorCount() > 0);
        
        // Log the result for debugging
        System.out.println("Validated: " + testFilePath.getFileName() + 
                          " - Compliant: " + result.isCompliant() + 
                          " - Errors: " + result.getErrorCount() + 
                          " - Warnings: " + result.getWarningCount());
        
        if (result.hasErrors()) {
            System.out.println("  Errors: " + result.getErrors());
        }
    }
}
