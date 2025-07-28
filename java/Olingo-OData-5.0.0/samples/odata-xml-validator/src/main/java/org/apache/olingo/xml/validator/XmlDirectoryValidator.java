package org.apache.olingo.xml.validator;

import java.nio.file.Path;
import java.util.List;

/**
 * Interface for validating OData 4.0 XML directory structures.
 * Provides comprehensive validation including XML format, schema structure,
 * dependencies, and OData compliance.
 */
public interface XmlDirectoryValidator {
    
    /**
     * Validates all XML files in the specified directory and its subdirectories.
     * 
     * @param directoryPath the root directory path containing XML files
     * @return validation result containing errors, warnings, and metadata
     */
    ValidationResult validateDirectory(Path directoryPath);
    
    /**
     * Validates a single XML file.
     * 
     * @param xmlFilePath path to the XML file
     * @return validation result for the specific file
     */
    ValidationResult validateXmlFile(Path xmlFilePath);
    
    /**
     * Builds a schema repository from the validated XML directory.
     * This repository can be used for future validations and dependency resolution.
     * 
     * @param directoryPath the root directory path
     * @return schema repository containing all loaded schemas
     */
    SchemaRepository buildSchemaRepository(Path directoryPath);
    
    /**
     * Validates an XML file against an existing schema repository.
     * Useful for checking new files against previously loaded schemas.
     * 
     * @param xmlFilePath path to the XML file
     * @param repository existing schema repository
     * @return validation result with dependency checking
     */
    ValidationResult validateWithRepository(Path xmlFilePath, SchemaRepository repository);
}
