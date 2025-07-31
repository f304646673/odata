package org.apache.olingo.compliance.examples;

import org.apache.olingo.compliance.api.ComplianceDetectionSystem;
import org.apache.olingo.compliance.api.ComplianceResult;
import org.apache.olingo.compliance.api.DependencyTreeManager;
import org.apache.olingo.compliance.api.RegistrationResult;
import org.apache.olingo.compliance.impl.DefaultComplianceDetectionSystem;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Example demonstrating the OData Compliance Detection System.
 * This class shows how to use the system to validate OData XML files and register compliant files.
 */
public class ComplianceSystemExample {
    
    public static void main(String[] args) {
        System.out.println("=== OData 4.0 Compliance Detection System Example ===");
        
        // Create the compliance detection system
        ComplianceDetectionSystem system = new DefaultComplianceDetectionSystem();
        
        // Example 1: Validate a single file
        System.out.println("\n1. Validating a single file:");
        Path sampleFile = Paths.get("src/test/resources/samples/basic-schema.xml");
        ComplianceResult result = system.validateFile(sampleFile);
        
        System.out.println("File: " + result.getFilePath());
        System.out.println("Compliant: " + result.isCompliant());
        System.out.println("Errors: " + result.getErrors().size());
        System.out.println("Processing time: " + result.getProcessingTimeMs() + "ms");
        
        // Example 2: Register a compliant file
        if (result.isCompliant()) {
            System.out.println("\n2. Registering compliant file:");
            RegistrationResult regResult = system.registerCompliantFile(sampleFile);
            System.out.println("Registration successful: " + regResult.isSuccessful());
            if (regResult.isSuccessful()) {
                System.out.println("Registered namespace: " + regResult.getRegisteredNamespace());
                System.out.println("Element count: " + regResult.getRegisteredElementCount());
            }
        }
        
        // Example 3: Validate a directory
        System.out.println("\n3. Validating directory:");
        Path sampleDir = Paths.get("src/test/resources/samples");
        List<ComplianceResult> results = system.validateDirectory(sampleDir, false);
        
        System.out.println("Total files validated: " + results.size());
        long compliantCount = results.stream().mapToLong(r -> r.isCompliant() ? 1 : 0).sum();
        System.out.println("Compliant files: " + compliantCount);
        
        // Example 4: Repository statistics
        System.out.println("\n4. Repository statistics:");
        System.out.println("File path repository size: " + system.getFilePathRepository().size());
        System.out.println("Namespace repository size: " + system.getNamespaceSchemaRepository().size());
        System.out.println("Registered namespaces: " + system.getNamespaceSchemaRepository().getAllNamespaces());
        
        // Example 5: Dependency analysis
        System.out.println("\n5. Dependency analysis:");
        DependencyTreeManager.DependencyStatistics depStats = system.getDependencyTreeManager().getStatistics();
        System.out.println("Total elements: " + depStats.getTotalElements());
        System.out.println("Elements with dependencies: " + depStats.getElementsWithDependencies());
        System.out.println("Circular dependencies: " + depStats.getCircularDependencyCount());
        
        System.out.println("\n=== Example completed ===");
    }
}
