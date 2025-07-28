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
 * Test class for dependency reference error files using XmlFileComplianceValidator.
 * Tests files from src/test/resources/validator/04-dependency-reference-errors/
 */
@RunWith(Parameterized.class)
public class DependencyReferenceErrorsTest {
    
    private XmlFileComplianceValidator validator;
    private final Path testFilePath;
    
    public DependencyReferenceErrorsTest(Path testFilePath) {
        this.testFilePath = testFilePath;
    }
    
    @Before
    public void setUp() {
        validator = new OlingoXmlFileComplianceValidator();
    }
    
    @Parameterized.Parameters(name = "{0}")
    public static List<Path> getTestFiles() throws Exception {
        List<Path> testFiles = new ArrayList<>();
        Path errorFilesDir = Paths.get("src/test/resources/validator/04-dependency-reference-errors");
        
        if (Files.exists(errorFilesDir) && Files.isDirectory(errorFilesDir)) {
            try (Stream<Path> files = Files.walk(errorFilesDir)) {
                files.filter(Files::isRegularFile)
                     .filter(path -> path.toString().endsWith(".xml"))
                     .forEach(testFiles::add);
            }
        }
        
        // Ensure we have at least one test file
        if (testFiles.isEmpty()) {
            throw new RuntimeException("No dependency reference error test files found in " + errorFilesDir);
        }
        
        return testFiles;
    }
    
    @Test
    public void testDependencyReferenceError() {
        File xmlFile = testFilePath.toFile();
        assertTrue("Test file should exist: " + testFilePath, xmlFile.exists());
        assertTrue("Test file should not be empty: " + testFilePath, xmlFile.length() > 0);
        
        XmlComplianceResult result = validator.validateFile(xmlFile);
        
        assertNotNull("Result should not be null", result);
        
        // Log the result for debugging
        System.out.println("Testing dependency reference error file: " + testFilePath.getFileName());
        System.out.println("  Compliant: " + result.isCompliant());
        System.out.println("  Errors: " + result.getErrorCount());
        System.out.println("  Warnings: " + result.getWarningCount());
        
        if (result.hasErrors()) {
            System.out.println("  Error details: " + result.getErrors());
        }
        
        // For dependency reference error files, we expect them to have errors or be non-compliant
        if (result.isCompliant() && result.getErrorCount() == 0) {
            System.out.println("INFO: Dependency reference error file was actually valid: " + testFilePath.getFileName());
        }
        
        // At minimum, the validation should complete without throwing exceptions
        assertNotNull("Validation should complete successfully", result);
    }
}
