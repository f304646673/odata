package org.apache.olingo.compliance.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Result of compliance validation for an OData XML file.
 */
public interface ComplianceResult {
    
    /**
     * Gets the file path that was validated.
     *
     * @return File path
     */
    String getFilePath();
    
    /**
     * Checks if the file is compliant with OData 4.0 standards.
     *
     * @return true if compliant, false otherwise
     */
    boolean isCompliant();
    
    /**
     * Gets the list of validation errors found.
     *
     * @return List of error messages
     */
    List<String> getErrors();
    
    /**
     * Gets the list of validation warnings found.
     *
     * @return List of warning messages
     */
    List<String> getWarnings();
    
    /**
     * Gets the validation timestamp.
     *
     * @return Validation timestamp
     */
    LocalDateTime getValidationTime();
    
    /**
     * Gets the processing time in milliseconds.
     *
     * @return Processing time
     */
    long getProcessingTimeMs();
    
    /**
     * Gets additional metadata about the validation.
     *
     * @return Metadata map
     */
    Map<String, Object> getMetadata();
    
    /**
     * Gets the detected schema namespace.
     *
     * @return Schema namespace, if detected
     */
    String getSchemaNamespace();
    
    /**
     * Gets the file size in bytes.
     *
     * @return File size
     */
    long getFileSize();
}
