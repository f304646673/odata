package org.apache.olingo.compliance.example;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.olingo.compliance.core.model.ComplianceIssue;
import org.apache.olingo.compliance.core.model.ComplianceResult;
import org.apache.olingo.compliance.validator.directory.DirectoryValidation;
import org.apache.olingo.compliance.validator.file.impl.FileValidatorImpl;

/**
 * Demonstration class showing how to use the refactored OData schema validation code
 * to load and validate XML files from the test scenarios directory.
 */
public class SchemaValidationDemo {
    
    public static void main(String[] args) {
        SchemaValidationDemo demo = new SchemaValidationDemo();
        
        System.out.println("=== OData Schema Validation Demo ===\n");
        
        // Demonstrate validation of valid scenarios
        demo.demonstrateValidScenarios();
        
        // Demonstrate validation of invalid scenarios
        demo.demonstrateInvalidScenarios();
        
        // Demonstrate cross-directory validation
        demo.demonstrateCrossDirectoryValidation();
    }
    
    /**
     * Demonstrate validation of valid XML scenarios
     */
    public void demonstrateValidScenarios() {
        System.out.println("1. Validating VALID XML scenarios:");
        System.out.println("=====================================");
        
        try {
            List<String> validFiles = getXmlFilesInResourceDirectory("/test-scenarios/valid");
            
            FileValidatorImpl validator = new FileValidatorImpl();
            
            for (String filePath : validFiles) {
                System.out.println("\nValidating: " + filePath);
                
                File file = new File(filePath);
                if (file.exists()) {
                    ComplianceResult result = validator.validateFile(file, null);
                    
                    if (result.isCompliant()) {
                        System.out.println("  ✓ No issues found - file is valid");
                    } else {
                        System.out.println("  ! Issues found:");
                        for (ComplianceIssue issue : result.getIssues()) {
                            System.out.printf("    - %s: %s%n", 
                                issue.getSeverity(), issue.getMessage());
                        }
                    }
                } else {
                    System.out.println("  ✗ File not found: " + filePath);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error during valid scenario demonstration: " + e.getMessage());
        }
    }
    
    /**
     * Demonstrate validation of invalid XML scenarios
     */
    public void demonstrateInvalidScenarios() {
        System.out.println("\n\n2. Validating INVALID XML scenarios:");
        System.out.println("======================================");
        
        try {
            List<String> invalidFiles = getXmlFilesInResourceDirectory("/test-scenarios/invalid");
            
            FileValidatorImpl validator = new FileValidatorImpl();
            
            for (String filePath : invalidFiles) {
                System.out.println("\nValidating: " + filePath);
                
                File file = new File(filePath);
                if (file.exists()) {
                    ComplianceResult result = validator.validateFile(file, null);
                    
                    if (result.isCompliant()) {
                        System.out.println("  ! Expected errors but file appears valid");
                    } else {
                        System.out.println("  ✓ Found expected issues:");
                        for (ComplianceIssue issue : result.getIssues()) {
                            System.out.printf("    - %s: %s%n", 
                                issue.getSeverity(), issue.getMessage());
                        }
                    }
                } else {
                    System.out.println("  ✗ File not found: " + filePath);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error during invalid scenario demonstration: " + e.getMessage());
        }
    }
    
    /**
     * Demonstrate cross-directory validation using DirectoryValidation
     */
    public void demonstrateCrossDirectoryValidation() {
        System.out.println("\n\n3. Cross-Directory Validation:");
        System.out.println("===============================");
        
        try {
            // Get resource directory paths
            URL validResourceUrl = SchemaValidationDemo.class.getResource("/test-scenarios/valid");
            URL invalidResourceUrl = SchemaValidationDemo.class.getResource("/test-scenarios/invalid");
            
            if (validResourceUrl != null && invalidResourceUrl != null) {
                Path validDir = Paths.get(validResourceUrl.toURI());
                Path invalidDir = Paths.get(invalidResourceUrl.toURI());
                
                DirectoryValidation manager = new DirectoryValidation();
                
                // Validate valid directory
                System.out.println("\nValidating valid directory:");
                DirectoryValidation.DirectoryValidationResult validDirResult =
                    manager.validateDirectory(validDir.toString());
                
                if (validDirResult.isValid()) {
                    System.out.println("  ✓ No cross-file issues in valid directory");
                } else {
                    System.out.println("  ! Cross-file issues in valid directory:");
                    for (ComplianceIssue issue : validDirResult.getAllIssues()) {
                        System.out.printf("    - %s: %s%n", 
                            issue.getSeverity(), issue.getMessage());
                    }
                }
                
                // Validate invalid directory
                System.out.println("\nValidating invalid directory:");
                DirectoryValidation.DirectoryValidationResult invalidDirResult =
                    manager.validateDirectory(invalidDir.toString());
                
                if (invalidDirResult.isValid()) {
                    System.out.println("  ! Expected cross-file issues but found none");
                } else {
                    System.out.println("  ✓ Found expected cross-file issues:");
                    for (ComplianceIssue issue : invalidDirResult.getAllIssues()) {
                        System.out.printf("    - %s: %s%n", 
                            issue.getSeverity(), issue.getMessage());
                    }
                }
            } else {
                System.out.println("  ✗ Could not locate test scenarios in resources");
            }
            
        } catch (Exception e) {
            System.err.println("Error during cross-directory validation: " + e.getMessage());
        }
    }
    
    /**
     * Get XML files in a resource directory
     */
    private List<String> getXmlFilesInResourceDirectory(String resourcePath) throws Exception {
        List<String> xmlFiles = new ArrayList<>();
        
        URL resourceUrl = SchemaValidationDemo.class.getResource(resourcePath);
        if (resourceUrl != null) {
            Path dir = Paths.get(resourceUrl.toURI());
            
            try (Stream<Path> paths = Files.walk(dir)) {
                paths.filter(Files::isRegularFile)
                     .filter(path -> path.toString().toLowerCase().endsWith(".xml"))
                     .forEach(path -> xmlFiles.add(path.toString()));
            }
        }
        
        return xmlFiles;
    }
}
