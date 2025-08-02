package org.apache.olingo.compliance.core.api;

import java.io.FileDescriptor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unified validation result that can represent both file and directory validation outcomes.
 * This replaces the separate ComplianceResult and DirectoryValidationResult classes.
 */
public class ValidationResult {
    
    // Basic validation state
    private final boolean isCompliant;
    private final List<String> errors;
    private final List<String> warnings;
    private final Map<String, Object> metadata;
    private final long processingTimeMs;
    private final LocalDateTime validationTime;
    
    // File-specific data
    private final String fileName;
    private final long fileSizeBytes;
    
    // Directory-specific data
    private final String directoryPath;
    private final int totalFilesProcessed;
    private final int validFiles;
    private final int invalidFiles;
    private final Map<String, ValidationResult> fileResults;
    private final List<ConflictResult> conflicts;
    private final List<String> globalErrors;
    private final List<String> globalWarnings;

    public static FileDescriptor builder() {
        return new FileDescriptor();
    }

    // Builder pattern for flexible construction
    public static class Builder {
        private boolean isCompliant = true;
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        private Map<String, Object> metadata = new HashMap<>();
        private long processingTimeMs = 0;
        private LocalDateTime validationTime = LocalDateTime.now();
        
        // File-specific
        private String fileName;
        private long fileSizeBytes = 0;
        
        // Directory-specific
        private String directoryPath;
        private int totalFilesProcessed = 0;
        private int validFiles = 0;
        private int invalidFiles = 0;
        private Map<String, ValidationResult> fileResults = new HashMap<>();
        private List<ConflictResult> conflicts = new ArrayList<>();
        private List<String> globalErrors = new ArrayList<>();
        private List<String> globalWarnings = new ArrayList<>();
        
        public Builder compliant(boolean compliant) {
            this.isCompliant = compliant;
            return this;
        }
        
        public Builder addError(String error) {
            this.errors.add(error);
            this.isCompliant = false;
            return this;
        }
        
        public Builder addErrors(Collection<String> errors) {
            this.errors.addAll(errors);
            if (!errors.isEmpty()) {
                this.isCompliant = false;
            }
            return this;
        }
        
        public Builder addWarning(String warning) {
            this.warnings.add(warning);
            return this;
        }
        
        public Builder addWarnings(Collection<String> warnings) {
            this.warnings.addAll(warnings);
            return this;
        }
        
        public Builder addMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }
        
        public Builder processingTime(long timeMs) {
            this.processingTimeMs = timeMs;
            return this;
        }
        
        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }
        
        public Builder fileSize(long sizeBytes) {
            this.fileSizeBytes = sizeBytes;
            return this;
        }
        
        public Builder directoryPath(String path) {
            this.directoryPath = path;
            return this;
        }
        
        public Builder totalFiles(int total) {
            this.totalFilesProcessed = total;
            return this;
        }
        
        public Builder validFiles(int valid) {
            this.validFiles = valid;
            return this;
        }
        
        public Builder invalidFiles(int invalid) {
            this.invalidFiles = invalid;
            return this;
        }
        
        public Builder addFileResult(String fileName, ValidationResult result) {
            this.fileResults.put(fileName, result);
            return this;
        }
        
        public Builder addConflict(ConflictResult conflict) {
            this.conflicts.add(conflict);
            return this;
        }
        
        public Builder addGlobalError(String error) {
            this.globalErrors.add(error);
            this.isCompliant = false;
            return this;
        }
        
        public Builder addGlobalWarning(String warning) {
            this.globalWarnings.add(warning);
            return this;
        }
        
        public ValidationResult build() {
            return new ValidationResult(this);
        }
    }
    
    private ValidationResult(Builder builder) {
        this.isCompliant = builder.isCompliant;
        this.errors = Collections.unmodifiableList(new ArrayList<>(builder.errors));
        this.warnings = Collections.unmodifiableList(new ArrayList<>(builder.warnings));
        this.metadata = Collections.unmodifiableMap(new HashMap<>(builder.metadata));
        this.processingTimeMs = builder.processingTimeMs;
        this.validationTime = builder.validationTime;
        
        this.fileName = builder.fileName;
        this.fileSizeBytes = builder.fileSizeBytes;
        
        this.directoryPath = builder.directoryPath;
        this.totalFilesProcessed = builder.totalFilesProcessed;
        this.validFiles = builder.validFiles;
        this.invalidFiles = builder.invalidFiles;
        this.fileResults = Collections.unmodifiableMap(new HashMap<>(builder.fileResults));
        this.conflicts = Collections.unmodifiableList(new ArrayList<>(builder.conflicts));
        this.globalErrors = Collections.unmodifiableList(new ArrayList<>(builder.globalErrors));
        this.globalWarnings = Collections.unmodifiableList(new ArrayList<>(builder.globalWarnings));
    }
    
    // Static factory methods for common cases
    public static ValidationResult success(String fileName, long processingTime) {
        return new Builder()
                .compliant(true)
                .fileName(fileName)
                .processingTime(processingTime)
                .build();
    }
    
    public static ValidationResult error(String fileName, String error, long processingTime) {
        return new Builder()
                .compliant(false)
                .fileName(fileName)
                .addError(error)
                .processingTime(processingTime)
                .build();
    }
    
    public static ValidationResult directorySuccess(String directoryPath, int totalFiles, long processingTime) {
        return new Builder()
                .compliant(true)
                .directoryPath(directoryPath)
                .totalFiles(totalFiles)
                .validFiles(totalFiles)
                .processingTime(processingTime)
                .build();
    }
    
    // Getters
    public boolean isCompliant() { return isCompliant; }
    public List<String> getErrors() { return errors; }
    public List<String> getWarnings() { return warnings; }
    public boolean hasErrors() { return !errors.isEmpty(); }
    public boolean hasWarnings() { return !warnings.isEmpty(); }
    public int getErrorCount() { return errors.size(); }
    public int getWarningCount() { return warnings.size(); }
    public Map<String, Object> getMetadata() { return metadata; }
    public long getProcessingTimeMs() { return processingTimeMs; }
    public LocalDateTime getValidationTime() { return validationTime; }
    
    // File-specific getters
    public String getFileName() { return fileName; }
    public long getFileSizeBytes() { return fileSizeBytes; }
    public boolean isFileResult() { return fileName != null; }
    
    // Directory-specific getters
    public String getDirectoryPath() { return directoryPath; }
    public int getTotalFilesProcessed() { return totalFilesProcessed; }
    public int getValidFiles() { return validFiles; }
    public int getInvalidFiles() { return invalidFiles; }
    public Map<String, ValidationResult> getFileResults() { return fileResults; }
    public List<ConflictResult> getConflicts() { return conflicts; }
    public List<String> getGlobalErrors() { return globalErrors; }
    public List<String> getGlobalWarnings() { return globalWarnings; }
    public boolean isDirectoryResult() { return directoryPath != null; }
    public boolean hasConflicts() { return !conflicts.isEmpty(); }
    public boolean hasGlobalErrors() { return !globalErrors.isEmpty(); }
    public boolean hasGlobalWarnings() { return !globalWarnings.isEmpty(); }
    
    @Override
    public String toString() {
        if (isFileResult()) {
            return String.format("ValidationResult[file=%s, compliant=%s, errors=%d, warnings=%d, time=%dms]", 
                               fileName, isCompliant, errors.size(), warnings.size(), processingTimeMs);
        } else if (isDirectoryResult()) {
            return String.format("ValidationResult[directory=%s, compliant=%s, files=%d/%d, conflicts=%d, time=%dms]", 
                               directoryPath, isCompliant, validFiles, totalFilesProcessed, conflicts.size(), processingTimeMs);
        } else {
            return String.format("ValidationResult[compliant=%s, errors=%d, warnings=%d, time=%dms]", 
                               isCompliant, errors.size(), warnings.size(), processingTimeMs);
        }
    }
    
    /**
     * Represents a conflict detected during directory validation.
     */
    public static class ConflictResult {
        private final String conflictType;
        private final String description;
        private final List<String> affectedFiles;
        private final String severity;
        
        public ConflictResult(String conflictType, String description, List<String> affectedFiles, String severity) {
            this.conflictType = conflictType;
            this.description = description;
            this.affectedFiles = Collections.unmodifiableList(new ArrayList<>(affectedFiles));
            this.severity = severity;
        }
        
        public String getConflictType() { return conflictType; }
        public String getDescription() { return description; }
        public List<String> getAffectedFiles() { return affectedFiles; }
        public String getSeverity() { return severity; }
        
        @Override
        public String toString() {
            return String.format("ConflictResult[type=%s, severity=%s, files=%s]", 
                               conflictType, severity, affectedFiles);
        }
    }
}
