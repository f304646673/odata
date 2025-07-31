package org.apache.olingo.compliance.api;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;

/**
 * Repository for storing and retrieving OData schema information by file path.
 * Handles files that may contain multiple schemas with different namespaces.
 */
public interface FilePathRepository {
    
    /**
     * File entry containing multiple schemas and metadata.
     */
    interface FileEntry {
        Path getFilePath();
        List<CsdlSchema> getSchemas();
        LocalDateTime getValidationTime();
        long getFileSize();
        Set<String> getNamespaces();
    }
    
    /**
     * Stores multiple schemas from a single file.
     * A single XML file can contain multiple schemas with different namespaces.
     *
     * @param filePath Path to the schema file
     * @param schemas List of parsed CSDL schemas from the file
     * @param validationTime When the file was validated
     * @param fileSize Size of the file in bytes
     */
    void storeSchemas(Path filePath, List<CsdlSchema> schemas, LocalDateTime validationTime, long fileSize);
    
    /**
     * Retrieves all schemas from a file path.
     *
     * @param filePath Path to the schema file
     * @return List of schemas if found, empty list otherwise
     */
    List<CsdlSchema> getSchemas(Path filePath);
    
    /**
     * Retrieves a specific schema by namespace from a file.
     *
     * @param filePath Path to the schema file
     * @param namespace The namespace to retrieve
     * @return Optional containing the schema if found
     */
    Optional<CsdlSchema> getSchemaByNamespace(Path filePath, String namespace);
    
    /**
     * Gets complete file entry with all metadata.
     *
     * @param filePath Path to the schema file
     * @return Optional containing the file entry if found
     */
    Optional<FileEntry> getFileEntry(Path filePath);
    
    /**
     * Checks if a file path has been registered.
     *
     * @param filePath Path to check
     * @return true if registered, false otherwise
     */
    boolean contains(Path filePath);
    
    /**
     * Gets all registered file paths.
     *
     * @return List of all registered file paths
     */
    List<Path> getAllFilePaths();
    
    /**
     * Gets all file paths that contain a specific namespace.
     *
     * @param namespace The namespace to search for
     * @return List of file paths containing the namespace
     */
    List<Path> getFilePathsByNamespace(String namespace);
    
    /**
     * Gets all namespaces found across all files.
     *
     * @return Set of all namespaces
     */
    Set<String> getAllNamespaces();
    
    /**
     * Gets the validation time for a file path.
     *
     * @param filePath Path to the schema file
     * @return Optional containing the validation time if found
     */
    Optional<LocalDateTime> getValidationTime(Path filePath);
    
    /**
     * Gets the file size for a file path.
     *
     * @param filePath Path to the schema file
     * @return Optional containing the file size if found
     */
    Optional<Long> getFileSize(Path filePath);
    
    /**
     * Removes a file path and all its associated schemas from the repository.
     *
     * @param filePath Path to remove
     * @return Set of namespaces that were affected by the removal
     */
    Set<String> remove(Path filePath);
    
    /**
     * Clears all stored schemas from the repository.
     */
    void clear();
    
    /**
     * Gets the total number of registered files.
     *
     * @return Number of registered files
     */
    int size();
    
    /**
     * Gets the total number of schemas across all files.
     *
     * @return Total number of schemas
     */
    int getTotalSchemaCount();
}
