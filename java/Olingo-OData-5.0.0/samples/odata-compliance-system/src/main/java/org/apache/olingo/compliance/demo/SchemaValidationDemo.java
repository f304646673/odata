package org.apache.olingo.compliance.demo;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.olingo.compliance.core.model.ComplianceIssue;
import org.apache.olingo.compliance.core.model.XmlComplianceResult;
import org.apache.olingo.compliance.validator.directory.DirectoryValidationManager;
import org.apache.olingo.compliance.validator.file.EnhancedRegistryAwareXmlValidator;

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
            
            EnhancedRegistryAwareXmlValidator validator = new EnhancedRegistryAwareXmlValidator();
            
            for (String filePath : validFiles) {
                System.out.println("\nValidating: " + filePath);
                
                File file = new File(filePath);
                if (file.exists()) {
                    XmlComplianceResult result = validator.validateFile(file);
                    
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
            
            EnhancedRegistryAwareXmlValidator validator = new EnhancedRegistryAwareXmlValidator();
            
            for (String filePath : invalidFiles) {
                System.out.println("\nValidating: " + filePath);
                
                File file = new File(filePath);
                if (file.exists()) {
                    XmlComplianceResult result = validator.validateFile(file);
                    
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
     * Demonstrate cross-directory validation using DirectoryValidationManager
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
                
                DirectoryValidationManager manager = new DirectoryValidationManager();
                
                // Validate valid directory
                System.out.println("\nValidating valid directory:");
                DirectoryValidationManager.DirectoryValidationResult validDirResult = 
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
                DirectoryValidationManager.DirectoryValidationResult invalidDirResult = 
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
            
//             for (String filePath : validFiles) {
//                 System.out.println("\nValidating: " + new File(filePath).getName());
                
//                 // Use SchemaExtractor to extract schema information
//                 SchemaExtractor extractor = new SchemaExtractor();
//                 SchemaExtractor.SchemaExtractionResult result = extractor.extractSchemas(filePath);
                
//                 if (result.isSuccess()) {
//                     System.out.println("  ✓ Schema extraction successful");
//                     System.out.println("  - Namespaces found: " + result.getNamespaces());
//                     System.out.println("  - Entity types: " + result.getEntityTypes().size());
//                     System.out.println("  - Complex types: " + result.getComplexTypes().size());
//                     System.out.println("  - Containers: " + result.getContainers().size());
//                     System.out.println("  - Annotations: " + result.getAnnotations().size());
//                 } else {
//                     System.out.println("  ✗ Schema extraction failed: " + result.getErrorMessage());
//                 }
//             }
            
//             // Validate directory as a whole
//             System.out.println("\nDirectory-level validation for valid scenarios:");
//             DirectoryValidationManager validator = new DirectoryValidationManager();
//             List<ComplianceIssue> issues = validator.validateDirectory(VALID_DIR);
            
//             if (issues.isEmpty()) {
//                 System.out.println("  ✓ No conflicts detected in valid directory");
//             } else {
//                 System.out.println("  ⚠ Unexpected issues found:");
//                 for (ComplianceIssue issue : issues) {
//                     System.out.println("    - " + issue.getMessage());
//                 }
//             }
            
//         } catch (Exception e) {
//             System.err.println("Error validating valid scenarios: " + e.getMessage());
//             e.printStackTrace();
//         }
//     }
    
//     /**
//      * Demonstrate validation of invalid XML scenarios
//      */
//     public void demonstrateInvalidScenarios() {
//         System.out.println("\n\n2. Validating INVALID XML scenarios:");
//         System.out.println("======================================");
        
//         try {
//             List<String> invalidFiles = getXmlFilesInDirectory(INVALID_DIR);
            
//             for (String filePath : invalidFiles) {
//                 System.out.println("\nValidating: " + new File(filePath).getName());
                
//                 // Use SchemaExtractor to extract schema information
//                 SchemaExtractor extractor = new SchemaExtractor();
//                 SchemaExtractor.SchemaExtractionResult result = extractor.extractSchemas(filePath);
                
//                 if (result.isSuccess()) {
//                     System.out.println("  ⚠ Schema extraction successful (but file should be invalid)");
//                     System.out.println("  - Namespaces found: " + result.getNamespaces());
//                     System.out.println("  - Entity types: " + result.getEntityTypes().size());
//                     System.out.println("  - Complex types: " + result.getComplexTypes().size());
//                 } else {
//                     System.out.println("  ✓ Schema extraction failed as expected: " + result.getErrorMessage());
//                 }
//             }
            
//             // Validate directory as a whole
//             System.out.println("\nDirectory-level validation for invalid scenarios:");
//             DirectoryValidationManager validator = new DirectoryValidationManager();
//             List<ComplianceIssue> issues = validator.validateDirectory(INVALID_DIR);
            
//             if (!issues.isEmpty()) {
//                 System.out.println("  ✓ Expected conflicts detected:");
//                 for (ComplianceIssue issue : issues) {
//                     System.out.println("    - " + issue.getErrorType() + ": " + issue.getMessage());
//                 }
//             } else {
//                 System.out.println("  ⚠ No conflicts detected (unexpected for invalid directory)");
//             }
            
//         } catch (Exception e) {
//             System.err.println("Error validating invalid scenarios: " + e.getMessage());
//             e.printStackTrace();
//         }
//     }
    
//     /**
//      * Demonstrate cross-directory validation (combining valid and invalid)
//      */
//     public void demonstrateCrossDirectoryValidation() {
//         System.out.println("\n\n3. Cross-directory validation:");
//         System.out.println("===============================");
        
//         try {
//             // Combine valid and invalid files for cross-validation
//             List<String> allFiles = new ArrayList<>();
//             allFiles.addAll(getXmlFilesInDirectory(VALID_DIR));
//             allFiles.addAll(getXmlFilesInDirectory(INVALID_DIR));
            
//             System.out.println("Validating " + allFiles.size() + " files across both directories...");
            
//             DirectoryValidationManager validator = new DirectoryValidationManager();
//             List<ComplianceIssue> allIssues = new ArrayList<>();
            
//             // Validate each file individually and collect issues
//             for (String filePath : allFiles) {
//                 List<ComplianceIssue> fileIssues = validator.validateSingleFile(filePath);
//                 allIssues.addAll(fileIssues);
//             }
            
//             // Also check for cross-file conflicts
//             String parentDir = new File(VALID_DIR).getParent();
//             List<ComplianceIssue> crossFileIssues = validator.validateDirectory(parentDir);
//             allIssues.addAll(crossFileIssues);
            
//             System.out.println("\nTotal issues found: " + allIssues.size());
            
//             // Group issues by type
//             long namespaceConflicts = allIssues.stream()
//                 .filter(issue -> issue.getErrorType().name().contains("NAMESPACE"))
//                 .count();
//             long elementConflicts = allIssues.stream()
//                 .filter(issue -> issue.getErrorType().name().contains("ELEMENT"))
//                 .count();
//             long annotationConflicts = allIssues.stream()
//                 .filter(issue -> issue.getErrorType().name().contains("ANNOTATION"))
//                 .count();
//             long dependencyConflicts = allIssues.stream()
//                 .filter(issue -> issue.getErrorType().name().contains("DEPENDENCY"))
//                 .count();
            
//             System.out.println("  - Namespace conflicts: " + namespaceConflicts);
//             System.out.println("  - Element conflicts: " + elementConflicts);
//             System.out.println("  - Annotation conflicts: " + annotationConflicts);
//             System.out.println("  - Dependency conflicts: " + dependencyConflicts);
            
//             // Show detailed issues
//             if (!allIssues.isEmpty()) {
//                 System.out.println("\nDetailed issues:");
//                 for (ComplianceIssue issue : allIssues) {
//                     System.out.println("  - [" + issue.getErrorType() + "] " + issue.getMessage());
//                     if (issue.getFileName() != null) {
//                         System.out.println("    File: " + issue.getFileName());
//                     }
//                 }
//             }
            
//         } catch (Exception e) {
//             System.err.println("Error in cross-directory validation: " + e.getMessage());
//             e.printStackTrace();
//         }
//     }
    
//     /**
//      * Get all XML files in a directory
//      */
//     private List<String> getXmlFilesInDirectory(String directoryPath) throws IOException {
//         List<String> xmlFiles = new ArrayList<>();
//         Path dir = Paths.get(directoryPath);
        
//         if (Files.exists(dir) && Files.isDirectory(dir)) {
//             try (Stream<Path> stream = Files.walk(dir)) {
//                 stream.filter(Files::isRegularFile)
//                       .filter(path -> path.toString().toLowerCase().endsWith(".xml"))
//                       .forEach(path -> xmlFiles.add(path.toString()));
//             }
//         }
        
//         return xmlFiles;
//     }
    
//     /**
//      * Utility method to demonstrate individual schema extraction
//      */
//     public static void demonstrateSchemaExtraction(String filePath) {
//         System.out.println("\n--- Schema Extraction Demo for: " + filePath + " ---");
        
//         try {
//             SchemaExtractor extractor = new SchemaExtractor();
//             SchemaExtractor.SchemaExtractionResult result = extractor.extractSchemas(filePath);
            
//             if (result.isSuccess()) {
//                 System.out.println("Extraction successful!");
                
//                 // Show extracted information
//                 System.out.println("Namespaces: " + result.getNamespaces());
                
//                 System.out.println("Entity Types:");
//                 result.getEntityTypes().forEach((namespace, types) -> {
//                     types.forEach(type -> {
//                         System.out.println("  - " + namespace + "." + type.getName());
//                     });
//                 });
                
//                 System.out.println("Complex Types:");
//                 result.getComplexTypes().forEach((namespace, types) -> {
//                     types.forEach(type -> {
//                         System.out.println("  - " + namespace + "." + type.getName());
//                     });
//                 });
                
//                 System.out.println("Containers:");
//                 result.getContainers().forEach((namespace, containers) -> {
//                     containers.forEach(container -> {
//                         System.out.println("  - " + namespace + "." + container.getName());
//                     });
//                 });
                
//                 System.out.println("Annotations:");
//                 result.getAnnotations().forEach(annotation -> {
//                     System.out.println("  - Target: " + annotation.getTarget() + 
//                                      ", Term: " + annotation.getTerm());
//                 });
                
//             } else {
//                 System.out.println("Extraction failed: " + result.getErrorMessage());
//             }
            
//         } catch (Exception e) {
//             System.err.println("Error during extraction: " + e.getMessage());
//             e.printStackTrace();
//         }
//     }
// }
