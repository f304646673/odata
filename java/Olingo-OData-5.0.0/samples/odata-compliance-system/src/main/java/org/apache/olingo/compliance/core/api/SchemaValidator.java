package org.apache.olingo.compliance.core.api;

import java.io.File;
import java.nio.file.Path;

/**
 * Main entry point for schema validation.
 * This interface provides a unified API for validating both individual files and directories.
 */
public interface SchemaValidator {
    
    /**
     * Validates a single XML schema file.
     * 
     * @param xmlFile the XML file to validate
     * @return validation result containing errors, warnings, and metadata
     */
    ValidationResult validateFile(File xmlFile);
    
    /**
     * Validates a single XML schema file.
     * 
     * @param xmlPath path to the XML file to validate
     * @return validation result containing errors, warnings, and metadata
     */
    ValidationResult validateFile(Path xmlPath);
    
    /**
     * Validates XML content from string.
     * 
     * @param xmlContent the XML content as string
     * @param fileName optional file name for error reporting
     * @return validation result containing errors, warnings, and metadata
     */
    ValidationResult validateContent(String xmlContent, String fileName);
    
    /**
     * Validates all XML schema files in a directory.
     * 
     * @param directoryPath path to directory containing XML files
     * @return validation result for the entire directory
     */
    ValidationResult validateDirectory(Path directoryPath);
    
    /**
     * Validates XML files in a directory matching a specific pattern.
     * 
     * @param directoryPath path to directory containing XML files
     * @param filePattern glob pattern for file matching (e.g., "*.xml")
     * @return validation result for the directory
     */
    ValidationResult validateDirectory(Path directoryPath, String filePattern);
}
