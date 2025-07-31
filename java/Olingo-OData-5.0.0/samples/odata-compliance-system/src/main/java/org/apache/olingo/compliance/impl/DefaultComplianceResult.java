package org.apache.olingo.compliance.impl;

import org.apache.olingo.compliance.api.ComplianceResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of ComplianceResult.
 */
public class DefaultComplianceResult implements ComplianceResult {
    
    private final String filePath;
    private final boolean compliant;
    private final List<String> errors;
    private final List<String> warnings;
    private final LocalDateTime validationTime;
    private final long processingTimeMs;
    private final Map<String, Object> metadata;
    private final String schemaNamespace;
    private final long fileSize;
    
    public DefaultComplianceResult(String filePath, boolean compliant, List<String> errors,
                                 List<String> warnings, LocalDateTime validationTime,
                                 long processingTimeMs, Map<String, Object> metadata,
                                 String schemaNamespace, long fileSize) {
        this.filePath = filePath;
        this.compliant = compliant;
        this.errors = errors;
        this.warnings = warnings;
        this.validationTime = validationTime;
        this.processingTimeMs = processingTimeMs;
        this.metadata = metadata;
        this.schemaNamespace = schemaNamespace;
        this.fileSize = fileSize;
    }
    
    @Override
    public String getFilePath() {
        return filePath;
    }
    
    @Override
    public boolean isCompliant() {
        return compliant;
    }
    
    @Override
    public List<String> getErrors() {
        return errors;
    }
    
    @Override
    public List<String> getWarnings() {
        return warnings;
    }
    
    @Override
    public LocalDateTime getValidationTime() {
        return validationTime;
    }
    
    @Override
    public long getProcessingTimeMs() {
        return processingTimeMs;
    }
    
    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    @Override
    public String getSchemaNamespace() {
        return schemaNamespace;
    }
    
    @Override
    public long getFileSize() {
        return fileSize;
    }
}
