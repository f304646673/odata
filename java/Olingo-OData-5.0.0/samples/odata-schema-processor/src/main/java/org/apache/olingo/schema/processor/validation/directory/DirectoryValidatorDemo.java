package org.apache.olingo.schema.processor.validation.directory;

import org.apache.olingo.schema.processor.validation.OlingoXmlFileComplianceValidator;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Command line utility to demonstrate directory-level XML schema validation.
 * Usage: java DirectoryValidatorDemo <directory_path> [file_pattern]
 */
public class DirectoryValidatorDemo {
    
    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }
        
        String directoryPath = args[0];
        String filePattern = args.length > 1 ? args[1] : "*.xml";
        
        System.out.println("=== Directory Schema Validator Demo ===");
        System.out.println("Directory: " + directoryPath);
        System.out.println("Pattern: " + filePattern);
        System.out.println();
        
        DirectorySchemaValidator validator = new DirectorySchemaValidator(new OlingoXmlFileComplianceValidator());
        
        try {
            Path directory = Paths.get(directoryPath);
            DirectoryValidationResult result = validator.validateDirectory(directory, filePattern);
            
            printResults(result);
            
        } catch (Exception e) {
            System.err.println("Error during validation: " + e.getMessage());
            e.printStackTrace();
        } finally {
            validator.shutdown();
        }
    }
    
    private static void printUsage() {
        System.out.println("Directory Schema Validator Demo");
        System.out.println("Usage: java DirectoryValidatorDemo <directory_path> [file_pattern]");
        System.out.println();
        System.out.println("  directory_path  Path to directory containing XML files");
        System.out.println("  file_pattern    Optional glob pattern for file matching (default: *.xml)");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java DirectoryValidatorDemo /path/to/schemas");
        System.out.println("  java DirectoryValidatorDemo /path/to/schemas schema_*.xml");
    }
    
    private static void printResults(DirectoryValidationResult result) {
        System.out.println(result.getSummary());
        System.out.println();
        
        // Print file results
        System.out.println("=== Individual File Results ===");
        result.getFileResults().forEach((fileName, fileResult) -> {
            System.out.printf("%-30s %s%n", fileName, 
                            fileResult.isCompliant() ? "✓ VALID" : "✗ INVALID");
            if (!fileResult.isCompliant() && !fileResult.getErrors().isEmpty()) {
                fileResult.getErrors().forEach(error -> 
                    System.out.println("    Error: " + error));
            }
        });
        System.out.println();
        
        // Print conflicts
        if (result.hasConflicts()) {
            System.out.println("=== Schema Conflicts ===");
            result.getConflicts().forEach(conflict -> {
                System.out.println("Type: " + conflict.getType());
                System.out.println("Namespace: " + conflict.getNamespace());
                if (conflict.getElementName() != null) {
                    System.out.println("Element: " + conflict.getElementName());
                }
                System.out.println("Files: " + conflict.getConflictingFiles());
                System.out.println("Description: " + conflict.getDescription());
                System.out.println();
            });
        }
        
        // Print namespace mapping
        if (!result.getNamespaceToFiles().isEmpty()) {
            System.out.println("=== Namespace to Files Mapping ===");
            result.getNamespaceToFiles().forEach((namespace, files) -> {
                System.out.printf("%-40s %s%n", namespace, files);
            });
            System.out.println();
        }
        
        // Print global errors and warnings
        if (result.hasGlobalErrors()) {
            System.out.println("=== Global Errors ===");
            result.getGlobalErrors().forEach(error -> 
                System.out.println("• " + error));
            System.out.println();
        }
        
        if (!result.getGlobalWarnings().isEmpty()) {
            System.out.println("=== Global Warnings ===");
            result.getGlobalWarnings().forEach(warning -> 
                System.out.println("• " + warning));
            System.out.println();
        }
        
        // Final status
        System.out.println("=== Final Status ===");
        System.out.println("Overall Compliant: " + (result.isCompliant() ? "YES" : "NO"));
        System.out.println("Validation completed in " + result.getValidationTimeMs() + "ms");
    }
}
