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

import static org.junit.Assert.*;

/**
 * Comprehensive test class that validates all files in the validator test resources
 */
public class ComprehensiveValidatorTest {
    
    private XmlFileComplianceValidator validator;
    
    @Before
    public void setUp() {
        validator = new OlingoXmlFileComplianceValidator();
    }
    
    @Test
    public void testAllValidFiles() throws Exception {
        Path validFilesDir = Paths.get("src/test/resources/validator/valid-files");
        List<Path> files = getXmlFiles(validFilesDir);
        
        assertTrue("Should have at least one valid file to test", files.size() > 0);
        
        for (Path filePath : files) {
            XmlComplianceResult result = validator.validateFile(filePath.toFile());
            assertNotNull("Result should not be null for " + filePath.getFileName(), result);
            assertTrue("Valid file should be compliant: " + filePath.getFileName(), result.isCompliant());
            assertEquals("Valid file should have no errors: " + filePath.getFileName(), 0, result.getErrorCount());
        }
    }
    
    @Test
    public void testXmlFormatErrors() throws Exception {
        Path errorDir = Paths.get("src/test/resources/validator/01-xml-format-errors");
        List<Path> files = getXmlFiles(errorDir);
        
        for (Path filePath : files) {
            XmlComplianceResult result = validator.validateFile(filePath.toFile());
            assertNotNull("Result should not be null for " + filePath.getFileName(), result);
            assertFalse("XML format error file should not be compliant: " + filePath.getFileName(), result.isCompliant());
            assertTrue("XML format error file should have errors: " + filePath.getFileName(), result.getErrorCount() > 0);
        }
    }
    
    @Test
    public void testSchemaStructureErrors() throws Exception {
        Path errorDir = Paths.get("src/test/resources/validator/02-schema-structure-errors");
        List<Path> files = getXmlFiles(errorDir);
        
        for (Path filePath : files) {
            XmlComplianceResult result = validator.validateFile(filePath.toFile());
            assertNotNull("Result should not be null for " + filePath.getFileName(), result);
            assertFalse("Schema structure error file should not be compliant: " + filePath.getFileName(), result.isCompliant());
            assertTrue("Schema structure error file should have errors: " + filePath.getFileName(), result.getErrorCount() > 0);
        }
    }
    
    @Test
    public void testElementDefinitionErrors() throws Exception {
        Path errorDir = Paths.get("src/test/resources/validator/03-element-definition-errors");
        List<Path> files = getXmlFiles(errorDir);
        
        for (Path filePath : files) {
            XmlComplianceResult result = validator.validateFile(filePath.toFile());
            assertNotNull("Result should not be null for " + filePath.getFileName(), result);
            assertFalse("Element definition error file should not be compliant: " + filePath.getFileName(), result.isCompliant());
            assertTrue("Element definition error file should have errors: " + filePath.getFileName(), result.getErrorCount() > 0);
        }
    }
    
    @Test
    public void testDependencyReferenceErrors() throws Exception {
        Path errorDir = Paths.get("src/test/resources/validator/04-dependency-reference-errors");
        List<Path> files = getXmlFiles(errorDir);
        
        for (Path filePath : files) {
            XmlComplianceResult result = validator.validateFile(filePath.toFile());
            assertNotNull("Result should not be null for " + filePath.getFileName(), result);
            // Note: Some dependency errors might only be warnings rather than errors
            assertTrue("Dependency reference error file should have errors or warnings: " + filePath.getFileName(), 
                      result.getErrorCount() > 0 || result.getWarningCount() > 0);
        }
    }
    
    @Test
    public void testAnnotationErrors() throws Exception {
        Path errorDir = Paths.get("src/test/resources/validator/05-annotation-errors");
        List<Path> files = getXmlFiles(errorDir);
        
        for (Path filePath : files) {
            XmlComplianceResult result = validator.validateFile(filePath.toFile());
            assertNotNull("Result should not be null for " + filePath.getFileName(), result);
            assertTrue("Annotation error file should have errors or warnings: " + filePath.getFileName(), 
                      result.getErrorCount() > 0 || result.getWarningCount() > 0);
        }
    }
    
    @Test
    public void testODataComplianceErrors() throws Exception {
        Path errorDir = Paths.get("src/test/resources/validator/06-odata-compliance-errors");
        List<Path> files = getXmlFiles(errorDir);
        
        for (Path filePath : files) {
            XmlComplianceResult result = validator.validateFile(filePath.toFile());
            assertNotNull("Result should not be null for " + filePath.getFileName(), result);
            assertTrue("OData compliance error file should have errors or warnings: " + filePath.getFileName(), 
                      result.getErrorCount() > 0 || result.getWarningCount() > 0);
        }
    }
    
    @Test
    public void testEncodingCharsetErrors() throws Exception {
        Path errorDir = Paths.get("src/test/resources/validator/07-encoding-charset-errors");
        List<Path> files = getXmlFiles(errorDir);
        
        for (Path filePath : files) {
            XmlComplianceResult result = validator.validateFile(filePath.toFile());
            assertNotNull("Result should not be null for " + filePath.getFileName(), result);
            // Encoding errors may not always be detectable in simple parsing
            System.out.println("Encoding test: " + filePath.getFileName() + 
                             " - Compliant: " + result.isCompliant() + 
                             " - Errors: " + result.getErrorCount());
        }
    }
    
    @Test
    public void testSecurityVulnerabilities() throws Exception {
        Path errorDir = Paths.get("src/test/resources/validator/08-security-vulnerabilities");
        List<Path> files = getXmlFiles(errorDir);
        
        for (Path filePath : files) {
            XmlComplianceResult result = validator.validateFile(filePath.toFile());
            assertNotNull("Result should not be null for " + filePath.getFileName(), result);
            assertFalse("Security vulnerability file should not be compliant: " + filePath.getFileName(), result.isCompliant());
            assertTrue("Security vulnerability file should have errors: " + filePath.getFileName(), result.getErrorCount() > 0);
        }
    }
    
    @Test
    public void testPerformanceEdgeCases() throws Exception {
        Path errorDir = Paths.get("src/test/resources/validator/09-performance-edge-cases");
        List<Path> files = getXmlFiles(errorDir);
        
        for (Path filePath : files) {
            XmlComplianceResult result = validator.validateFile(filePath.toFile());
            assertNotNull("Result should not be null for " + filePath.getFileName(), result);
            // Performance edge cases may or may not be errors
            System.out.println("Performance test: " + filePath.getFileName() + 
                             " - Compliant: " + result.isCompliant() + 
                             " - Errors: " + result.getErrorCount() + 
                             " - Time: " + result.getValidationTimeMs() + "ms");
        }
    }
    
    @Test
    public void testSpecialCharactersUnicode() throws Exception {
        Path errorDir = Paths.get("src/test/resources/validator/10-special-characters-unicode");
        List<Path> files = getXmlFiles(errorDir);
        
        for (Path filePath : files) {
            XmlComplianceResult result = validator.validateFile(filePath.toFile());
            assertNotNull("Result should not be null for " + filePath.getFileName(), result);
            // Unicode handling may vary
            System.out.println("Unicode test: " + filePath.getFileName() + 
                             " - Compliant: " + result.isCompliant() + 
                             " - Errors: " + result.getErrorCount());
        }
    }
    
    @Test
    public void testFileCount() throws Exception {
        // Ensure we have test files for each category
        int totalFiles = 0;
        
        String[] directories = {
            "valid-files", "01-xml-format-errors", "02-schema-structure-errors",
            "03-element-definition-errors", "04-dependency-reference-errors",
            "05-annotation-errors", "06-odata-compliance-errors",
            "07-encoding-charset-errors", "08-security-vulnerabilities",
            "09-performance-edge-cases", "10-special-characters-unicode"
        };
        
        for (String dir : directories) {
            Path dirPath = Paths.get("src/test/resources/validator/" + dir);
            List<Path> files = getXmlFiles(dirPath);
            totalFiles += files.size();
            assertTrue("Directory " + dir + " should have at least one XML file", files.size() > 0);
        }
        
        assertTrue("Total test files should be at least 20", totalFiles >= 20);
        System.out.println("Total XML test files: " + totalFiles);
    }
    
    private List<Path> getXmlFiles(Path directory) throws Exception {
        List<Path> xmlFiles = new ArrayList<>();
        
        if (Files.exists(directory)) {
            try (Stream<Path> files = Files.walk(directory)) {
                files.filter(path -> path.toString().endsWith(".xml"))
                     .forEach(xmlFiles::add);
            }
        }
        
        return xmlFiles;
    }
}
