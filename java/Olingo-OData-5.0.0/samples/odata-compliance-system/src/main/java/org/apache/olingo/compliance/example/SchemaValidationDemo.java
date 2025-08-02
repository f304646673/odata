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
import org.apache.olingo.compliance.engine.core.impl.DefaultSchemaRegistryImpl;
import org.apache.olingo.compliance.validator.ComplianceValidator;
import org.apache.olingo.compliance.validator.impl.ComplianceValidatorImpl;
import org.apache.olingo.compliance.engine.core.SchemaRegistry;

/**
 * Demonstration class showing how to use the refactored OData schema validation code
 * to load and validate XML files from the test scenarios directory.
 */
public class SchemaValidationDemo {
    
    private final ComplianceValidator validator;
    private final SchemaRegistry schemaRegistry;

    public SchemaValidationDemo() {
        this.validator = new ComplianceValidatorImpl();
        this.schemaRegistry = new DefaultSchemaRegistryImpl();
    }

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
            
            for (String filePath : validFiles) {
                System.out.println("\nValidating: " + filePath);
                
                ComplianceResult result = validator.validateFile(new File(filePath), schemaRegistry);

                System.out.println("Is compliant: " + result.isCompliant());
                if (!result.getIssues().isEmpty()) {
                    System.out.println("Issues found:");
                    for (ComplianceIssue issue : result.getIssues()) {
                        System.out.println("  - " + issue.getSeverity() + ": " + issue.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error during valid scenarios validation: " + e.getMessage());
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
            
            for (String filePath : invalidFiles) {
                System.out.println("\nValidating: " + filePath);
                
                ComplianceResult result = validator.validateFile(new File(filePath), schemaRegistry);

                System.out.println("Is compliant: " + result.isCompliant());
                System.out.println("Issues found: " + result.getIssues().size());

                for (ComplianceIssue issue : result.getIssues()) {
                    System.out.println("  - " + issue.getSeverity() + ": " + issue.getErrorType() + " - " + issue.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error during invalid scenarios validation: " + e.getMessage());
        }
    }
    
    /**
     * Demonstrate cross-directory validation
     */
    public void demonstrateCrossDirectoryValidation() {
        System.out.println("\n\n3. Cross-Directory Validation:");
        System.out.println("================================");

        try {
            String testDirectory = getResourceDirectory("/test-scenarios/cross-reference");

            if (testDirectory != null) {
                System.out.println("Validating directory: " + testDirectory);

                ComplianceResult result = validator.validateDirectory(testDirectory, schemaRegistry, true);

                System.out.println("Directory validation completed:");
                System.out.println("Is compliant: " + result.isCompliant());
                System.out.println("Total issues: " + result.getIssues().size());
                System.out.println("Validation time: " + result.getValidationTimeMs() + "ms");

                // Print metadata
                System.out.println("\nValidation metadata:");
                result.getMetadata().forEach((key, value) -> {
                    System.out.println("  " + key + ": " + value);
                });

                // Print issues
                if (!result.getIssues().isEmpty()) {
                    System.out.println("\nIssues found:");
                    for (ComplianceIssue issue : result.getIssues()) {
                        System.out.println("  - " + issue.getSeverity() + ": " + issue.getErrorType());
                        System.out.println("    Message: " + issue.getMessage());
//                        System.out.println("    Source: " + issue.getSource());
                    }
                }
            } else {
                System.out.println("Cross-reference test directory not found");
            }
        } catch (Exception e) {
            System.err.println("Error during cross-directory validation: " + e.getMessage());
        }
    }
    
    /**
     * Get XML files from a resource directory
     */
    private List<String> getXmlFilesInResourceDirectory(String resourcePath) throws Exception {
        List<String> xmlFiles = new ArrayList<>();
        
        URL resourceUrl = getClass().getResource(resourcePath);
        if (resourceUrl != null) {
            Path resourceDir = Paths.get(resourceUrl.toURI());

            try (Stream<Path> files = Files.walk(resourceDir)) {
                files.filter(Files::isRegularFile)
                     .filter(path -> path.toString().toLowerCase().endsWith(".xml"))
                     .forEach(path -> xmlFiles.add(path.toString()));
            }
        }
        
        return xmlFiles;
    }

    /**
     * Get resource directory path
     */
    private String getResourceDirectory(String resourcePath) {
        try {
            URL resourceUrl = getClass().getResource(resourcePath);
            if (resourceUrl != null) {
                return Paths.get(resourceUrl.toURI()).toString();
            }
        } catch (Exception e) {
            System.err.println("Failed to locate resource directory: " + resourcePath);
        }
        return null;
    }
}
