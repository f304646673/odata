package org.apache.olingo.compliance.api;

import java.nio.file.Path;
import java.util.List;

/**
 * Validator for OData compliance checking.
 */
public interface ComplianceValidator {
    
    /**
     * Validates a single file for OData compliance.
     *
     * @param filePath Path to the file to validate
     * @return Compliance result
     */
    ComplianceResult validateFile(Path filePath);
    
    /**
     * Validates all files in a directory for OData compliance.
     *
     * @param directoryPath Path to the directory to validate
     * @param recursive Whether to scan subdirectories recursively
     * @return List of compliance results
     */
    List<ComplianceResult> validateDirectory(Path directoryPath, boolean recursive);
}
