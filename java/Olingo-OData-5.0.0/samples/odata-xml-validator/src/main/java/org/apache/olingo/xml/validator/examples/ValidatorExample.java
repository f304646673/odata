package org.apache.olingo.xml.validator.examples;

import org.apache.olingo.xml.validator.OlingoXmlDirectoryValidator;
import org.apache.olingo.xml.validator.ValidationResult;
import org.apache.olingo.xml.validator.ValidationError;
import org.apache.olingo.xml.validator.ValidationWarning;
import org.apache.olingo.xml.validator.SchemaRepository;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Example program demonstrating OData XML directory validation
 * 
 * This example shows how to use the OlingoXmlDirectoryValidator to validate
 * OData 4.0 XML schema files in a directory structure.
 */
public class ValidatorExample {

    public static void main(String[] args) {
        // Default to test-schemas directory if no arguments provided
        String directoryPath = args.length > 0 ? args[0] : "src/test/resources/test-schemas";
        
        System.out.println("OData XML Directory Validator Example");
        System.out.println("=====================================");
        System.out.println("Validating directory: " + directoryPath);
        System.out.println();

        try {
            // Create validator instance
            OlingoXmlDirectoryValidator validator = new OlingoXmlDirectoryValidator();
            
            // Validate the directory
            Path path = Paths.get(directoryPath);
            ValidationResult result = validator.validateDirectory(path);
            
            // Print results
            printValidationResult(result);
            
            // Print schema repository information
            printSchemaRepositoryInfo(validator);
            
        } catch (Exception e) {
            System.err.println("Error during validation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printValidationResult(ValidationResult result) {
        System.out.println("Validation Result:");
        System.out.println("------------------");
        System.out.println("Valid: " + result.isValid());
        System.out.println("Errors: " + result.getErrors().size());
        System.out.println("Warnings: " + result.getWarnings().size());
        System.out.println();

        if (!result.getErrors().isEmpty()) {
            System.out.println("Errors:");
            for (ValidationError error : result.getErrors()) {
                System.out.println("  - " + error.getMessage());
                if (error.getFilePath() != null) {
                    System.out.println("    File: " + error.getFilePath());
                }
                if (error.getLineNumber() > 0) {
                    System.out.println("    Line: " + error.getLineNumber());
                }
                System.out.println("    Type: " + error.getType());
                System.out.println();
            }
        }

        if (!result.getWarnings().isEmpty()) {
            System.out.println("Warnings:");
            for (ValidationWarning warning : result.getWarnings()) {
                System.out.println("  - " + warning.getMessage());
                if (warning.getFilePath() != null) {
                    System.out.println("    File: " + warning.getFilePath());
                }
                if (warning.getLineNumber() > 0) {
                    System.out.println("    Line: " + warning.getLineNumber());
                }
                System.out.println();
            }
        }
    }

    private static void printSchemaRepositoryInfo(OlingoXmlDirectoryValidator validator) {
        System.out.println("Schema Repository Information:");
        System.out.println("------------------------------");
        
        SchemaRepository repository = validator.getSchemaRepository();
        java.util.Set<String> namespaces = repository.getAllNamespaces();
        
        System.out.println("Total schemas loaded: " + namespaces.size());
        
        if (!namespaces.isEmpty()) {
            System.out.println("Namespaces:");
            for (String namespace : namespaces) {
                System.out.println("  - " + namespace);
                java.nio.file.Path schemaPath = repository.getFilePath(namespace);
                if (schemaPath != null) {
                    System.out.println("    Path: " + schemaPath);
                }
            }
        }
        System.out.println();
    }
}
