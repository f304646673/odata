package org.apache.olingo.compliance.api;

import java.nio.file.Path;
import java.util.List;

/**
 * Core compliance detection system interface.
 * Validates OData 4.0 XML files and directories through integrated repositories.
 */
public interface ComplianceDetectionSystem {
    
    /**
     * Validates a single OData XML file.
     *
     * @param filePath Path to the XML file
     * @return Compliance result with detailed information
     */
    ComplianceResult validateFile(Path filePath);
    
    /**
     * Validates all OData XML files in a directory.
     *
     * @param directoryPath Path to the directory
     * @param recursive Whether to scan subdirectories recursively
     * @return List of compliance results for each file
     */
    List<ComplianceResult> validateDirectory(Path directoryPath, boolean recursive);
    
    /**
     * Registers a compliant file's information into the system repositories.
     * This includes both file path repository and namespace schema extension repository.
     *
     * @param filePath Path to the compliant XML file
     * @return Registration result with repository update information
     */
    RegistrationResult registerCompliantFile(Path filePath);
    
    /**
     * Gets the file path repository for accessing stored file information.
     *
     * @return File path repository instance
     */
    FilePathRepository getFilePathRepository();
    
    /**
     * Gets the namespace schema extension repository for accessing merged schema information.
     *
     * @return Namespace schema extension repository instance
     */
    NamespaceSchemaRepository getNamespaceSchemaRepository();
    
    /**
     * Gets the dependency tree manager for analyzing element dependencies.
     *
     * @return Dependency tree manager instance
     */
    DependencyTreeManager getDependencyTreeManager();
}
