package org.apache.olingo.schema.validation.engine;

import java.util.List;
import java.util.Collections;

/**
 * 集成处理结果
 */
public class IntegrationResult {
    
    public enum Status {
        SUCCESS,              // 成功完成所有步骤
        VALIDATION_FAILURE,   // 验证失败
        CONFLICT_DETECTED,    // 检测到冲突
        ERROR                 // 处理错误
    }
    
    private final Status status;
    private final String message;
    private final List<String> validationErrors;
    private final List<String> conflicts;
    private final List<String> processedFiles;
    private long processingTime;
    private int totalFiles;
    private int validFiles;
    private int invalidFiles;
    
    private IntegrationResult(Status status, String message, 
                             List<String> validationErrors,
                             List<String> conflicts,
                             List<String> processedFiles) {
        this.status = status;
        this.message = message;
        this.validationErrors = validationErrors != null ? validationErrors : Collections.emptyList();
        this.conflicts = conflicts != null ? conflicts : Collections.emptyList();
        this.processedFiles = processedFiles != null ? processedFiles : Collections.emptyList();
        this.processingTime = 0;
        this.totalFiles = 0;
        this.validFiles = 0;
        this.invalidFiles = 0;
    }
    
    // 静态工厂方法
    
    public static IntegrationResult success(List<String> validationErrors,
                                          List<String> conflicts,
                                          List<String> processedFiles) {
        return new IntegrationResult(Status.SUCCESS, "Integration completed successfully",
                                   validationErrors, conflicts, processedFiles);
    }
    
    public static IntegrationResult validationFailure(List<String> validationErrors) {
        return new IntegrationResult(Status.VALIDATION_FAILURE, "Validation failed",
                                   validationErrors, null, null);
    }
    
    public static IntegrationResult conflictDetected(List<String> validationErrors,
                                                   List<String> conflicts,
                                                   List<String> processedFiles) {
        return new IntegrationResult(Status.CONFLICT_DETECTED, "Schema conflicts detected",
                                   validationErrors, conflicts, processedFiles);
    }
    
    public static IntegrationResult failure(String errorMessage) {
        return new IntegrationResult(Status.ERROR, errorMessage, null, null, null);
    }
    
    // Getter 方法
    
    public Status getStatus() {
        return status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public List<String> getValidationErrors() {
        return validationErrors;
    }
    
    public List<String> getConflicts() {
        return conflicts;
    }
    
    public List<String> getProcessedFiles() {
        return processedFiles;
    }
    
    public long getProcessingTime() {
        return processingTime;
    }
    
    public int getTotalFiles() {
        return totalFiles;
    }
    
    public int getValidFiles() {
        return validFiles;
    }
    
    public int getInvalidFiles() {
        return invalidFiles;
    }
    
    // 状态判断方法
    
    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }
    
    public boolean isValidationFailure() {
        return status == Status.VALIDATION_FAILURE;
    }
    
    public boolean hasConflicts() {
        return status == Status.CONFLICT_DETECTED;
    }
    
    public boolean isError() {
        return status == Status.ERROR;
    }
    
    // 流式方法
    
    public IntegrationResult withProcessingTime(long processingTime) {
        this.processingTime = processingTime;
        return this;
    }
    
    public IntegrationResult withFileStats(int totalFiles, int validFiles, int invalidFiles) {
        this.totalFiles = totalFiles;
        this.validFiles = validFiles;
        this.invalidFiles = invalidFiles;
        return this;
    }
    
    @Override
    public String toString() {
        return String.format("IntegrationResult{status=%s, message='%s', processingTime=%dms, " +
                           "files=%d/%d/%d (total/valid/invalid)}", 
                           status, message, processingTime, totalFiles, validFiles, invalidFiles);
    }
}
