package org.apache.olingo.compliance.validation.directory;

/**
 * 验证统计信息
 * 替代原来的 ComplianceContext.ComplianceStatistics
 */
public class ValidationStatistics {
    private final int totalTypes;
    private final int entityTypes;
    private final int complexTypes;
    private final int namespaceCount;
    private final int totalFiles;
    private final int processedFiles;
    private final long validationTimeMs;
    private final int validFiles;
    
    public ValidationStatistics(int totalTypes, int entityTypes, int complexTypes, 
                               int namespaceCount, int totalFiles, int processedFiles,
                               long validationTimeMs, int validFiles) {
        this.totalTypes = totalTypes;
        this.entityTypes = entityTypes;
        this.complexTypes = complexTypes;
        this.namespaceCount = namespaceCount;
        this.totalFiles = totalFiles;
        this.processedFiles = processedFiles;
        this.validationTimeMs = validationTimeMs;
        this.validFiles = validFiles;
    }
    
    public int getTotalTypes() { return totalTypes; }
    public int getEntityTypes() { return entityTypes; }
    public int getComplexTypes() { return complexTypes; }
    public int getNamespaceCount() { return namespaceCount; }
    public int getTotalFiles() { return totalFiles; }
    public int getProcessedFiles() { return processedFiles; }
    public long getValidationTimeMs() { return validationTimeMs; }
    public int getValidFiles() { return validFiles; }
    
    @Override
    public String toString() {
        return String.format(
            "ValidationStatistics{totalTypes=%d, entityTypes=%d, complexTypes=%d, " +
            "namespaceCount=%d, totalFiles=%d, processedFiles=%d, validationTimeMs=%d, validFiles=%d}",
            totalTypes, entityTypes, complexTypes, namespaceCount, 
            totalFiles, processedFiles, validationTimeMs, validFiles
        );
    }
}
