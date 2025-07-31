package org.apache.olingo.compliance.validation.core;

import org.apache.olingo.compliance.file.ComplianceIssue;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 可更新的合规性上下文，用于在验证新XML时动态更新合规性知识库。
 * 这个类提供了对ComplianceKnowledgeBase的可写访问，
 * 同时维护了验证过程中的临时状态和错误信息。
 */
public class ComplianceContext {
    
    // 只读的知识库
    private final ComplianceKnowledgeBase knowledgeBase;
    
    // 可更新的临时状态
    private final ComplianceKnowledgeBase.Builder temporaryBuilder;
    private final Map<String, Object> contextProperties;
    
    // 验证状态
    private final Set<String> processedFiles;
    private final Map<String, ValidationResult> validationResults;
    
    // 缓存和性能优化
    private final Map<String, Boolean> inheritanceCache;
    private final Map<String, String> resolvedTypeCache;
    
    public ComplianceContext(ComplianceKnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
        this.temporaryBuilder = new ComplianceKnowledgeBase.Builder();
        this.contextProperties = new ConcurrentHashMap<>();
        this.processedFiles = ConcurrentHashMap.newKeySet();
        this.validationResults = new ConcurrentHashMap<>();
        this.inheritanceCache = new ConcurrentHashMap<>();
        this.resolvedTypeCache = new ConcurrentHashMap<>();
    }
    
    /**
     * 验证结果结构
     */
    public static class ValidationResult {
        private final String filePath;
        private final boolean isValid;
        private final Set<ComplianceIssue> issues;
        private final long validationTimeMs;
        
        public ValidationResult(String filePath, boolean isValid, 
                              Set<ComplianceIssue> issues, long validationTimeMs) {
            this.filePath = filePath;
            this.isValid = isValid;
            this.issues = issues;
            this.validationTimeMs = validationTimeMs;
        }
        
        // Getters
        public String getFilePath() { return filePath; }
        public boolean isValid() { return isValid; }
        public Set<ComplianceIssue> getIssues() { return issues; }
        public long getValidationTimeMs() { return validationTimeMs; }
    }
    
    // 只读访问知识库
    
    /**
     * 检查类型是否存在（包括临时添加的类型）
     */
    public boolean isTypeDefined(String fullTypeName) {
        // 首先检查知识库
        if (knowledgeBase.isTypeDefined(fullTypeName)) {
            return true;
        }
        
        // 然后检查临时构建器中的类型
        ComplianceKnowledgeBase tempKb = temporaryBuilder.build();
        return tempKb.isTypeDefined(fullTypeName);
    }
    
    /**
     * 获取类型定义（优先从知识库，然后从临时状态）
     */
    public ComplianceKnowledgeBase.TypeDefinition getTypeDefinition(String fullTypeName) {
        ComplianceKnowledgeBase.TypeDefinition definition = knowledgeBase.getTypeDefinition(fullTypeName);
        if (definition != null) {
            return definition;
        }
        
        ComplianceKnowledgeBase tempKb = temporaryBuilder.build();
        return tempKb.getTypeDefinition(fullTypeName);
    }
    
    /**
     * 检查继承关系是否有效（带缓存）
     */
    public boolean isValidInheritance(String childType, String parentType) {
        String cacheKey = childType + "->" + parentType;
        
        return inheritanceCache.computeIfAbsent(cacheKey, k -> {
            // 首先检查知识库
            if (knowledgeBase.isValidInheritance(childType, parentType)) {
                return true;
            }
            
            // 然后检查临时状态
            ComplianceKnowledgeBase tempKb = temporaryBuilder.build();
            return tempKb.isValidInheritance(childType, parentType);
        });
    }
    
    /**
     * 检查类型是否为实体类型
     */
    public boolean isEntityType(String fullTypeName) {
        if (knowledgeBase.isEntityType(fullTypeName)) {
            return true;
        }
        
        ComplianceKnowledgeBase tempKb = temporaryBuilder.build();
        return tempKb.isEntityType(fullTypeName);
    }
    
    /**
     * 检查类型是否为复杂类型
     */
    public boolean isComplexType(String fullTypeName) {
        if (knowledgeBase.isComplexType(fullTypeName)) {
            return true;
        }
        
        ComplianceKnowledgeBase tempKb = temporaryBuilder.build();
        return tempKb.isComplexType(fullTypeName);
    }
    
    /**
     * 检查命名空间是否已注册
     */
    public boolean isNamespaceRegistered(String namespace) {
        if (knowledgeBase.isNamespaceRegistered(namespace)) {
            return true;
        }
        
        ComplianceKnowledgeBase tempKb = temporaryBuilder.build();
        return tempKb.isNamespaceRegistered(namespace);
    }
    
    // 更新操作
    
    /**
     * 添加新的类型定义到临时状态
     */
    public ComplianceContext addTemporaryTypeDefinition(ComplianceKnowledgeBase.TypeDefinition typeDefinition) {
        temporaryBuilder.addTypeDefinition(typeDefinition);
        
        // 清除相关缓存
        inheritanceCache.clear();
        resolvedTypeCache.clear();
        
        return this;
    }
    
    /**
     * 添加命名空间依赖到临时状态
     */
    public ComplianceContext addTemporaryNamespaceDependency(String namespace, String dependentNamespace) {
        temporaryBuilder.addNamespaceDependency(namespace, dependentNamespace);
        return this;
    }
    
    /**
     * 添加命名空间别名到临时状态
     */
    public ComplianceContext addTemporaryNamespaceAlias(String alias, String namespace) {
        temporaryBuilder.addNamespaceAlias(alias, namespace);
        resolvedTypeCache.clear(); // 清除解析缓存
        return this;
    }
    
    /**
     * 添加服务定义到临时状态
     */
    public ComplianceContext addTemporaryServiceDefinition(ComplianceKnowledgeBase.ServiceDefinition serviceDefinition) {
        temporaryBuilder.addServiceDefinition(serviceDefinition);
        return this;
    }
    
    /**
     * 记录文件处理状态
     */
    public ComplianceContext markFileProcessed(String filePath) {
        processedFiles.add(filePath);
        return this;
    }
    
    /**
     * 添加验证结果
     */
    public ComplianceContext addValidationResult(ValidationResult result) {
        validationResults.put(result.getFilePath(), result);
        return this;
    }
    
    /**
     * 设置上下文属性
     */
    public ComplianceContext setProperty(String key, Object value) {
        contextProperties.put(key, value);
        return this;
    }
    
    /**
     * 获取上下文属性
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, Class<T> type) {
        Object value = contextProperties.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * 检查文件是否已处理
     */
    public boolean isFileProcessed(String filePath) {
        return processedFiles.contains(filePath);
    }
    
    /**
     * 获取验证结果
     */
    public ValidationResult getValidationResult(String filePath) {
        return validationResults.get(filePath);
    }
    
    /**
     * 获取所有验证结果
     */
    public Map<String, ValidationResult> getAllValidationResults() {
        return new ConcurrentHashMap<>(validationResults);
    }
    
    /**
     * 构建包含所有临时状态的完整知识库
     */
    public ComplianceKnowledgeBase buildCompleteKnowledgeBase() {
        // 创建新的构建器，包含原知识库的所有内容
        ComplianceKnowledgeBase.Builder completeBuilder = new ComplianceKnowledgeBase.Builder();
        
        // 添加原知识库的内容
        for (String typeName : knowledgeBase.getAllDefinedTypes()) {
            ComplianceKnowledgeBase.TypeDefinition typeDef = knowledgeBase.getTypeDefinition(typeName);
            if (typeDef != null) {
                completeBuilder.addTypeDefinition(typeDef);
            }
        }
        
        for (String namespace : knowledgeBase.getAllRegisteredNamespaces()) {
            completeBuilder.addNamespace(namespace);
            
            ComplianceKnowledgeBase.ServiceDefinition serviceDef = knowledgeBase.getServiceDefinition(namespace);
            if (serviceDef != null) {
                completeBuilder.addServiceDefinition(serviceDef);
            }
        }
        
        // 合并临时状态
        ComplianceKnowledgeBase tempKb = temporaryBuilder.build();
        for (String typeName : tempKb.getAllDefinedTypes()) {
            ComplianceKnowledgeBase.TypeDefinition typeDef = tempKb.getTypeDefinition(typeName);
            if (typeDef != null) {
                completeBuilder.addTypeDefinition(typeDef);
            }
        }
        
        return completeBuilder.build();
    }
    
    /**
     * 清除临时状态
     */
    public ComplianceContext clearTemporaryState() {
        // 重新创建临时构建器
        ComplianceKnowledgeBase.Builder newBuilder = new ComplianceKnowledgeBase.Builder();
        
        // 清除缓存
        inheritanceCache.clear();
        resolvedTypeCache.clear();
        
        return this;
    }
    
    /**
     * 获取统计信息
     */
    public ComplianceStatistics getStatistics() {
        ComplianceKnowledgeBase completeKb = buildCompleteKnowledgeBase();
        
        int totalTypes = completeKb.getAllDefinedTypes().size();
        int entityTypes = completeKb.getAllEntityTypes().size();
        int complexTypes = completeKb.getAllComplexTypes().size();
        int namespaces = completeKb.getAllRegisteredNamespaces().size();
        int processedFileCount = processedFiles.size();
        int validationResultCount = validationResults.size();
        
        long totalValidationTime = validationResults.values().stream()
                                                   .mapToLong(ValidationResult::getValidationTimeMs)
                                                   .sum();
        
        long validFiles = validationResults.values().stream()
                                          .mapToLong(r -> r.isValid() ? 1 : 0)
                                          .sum();
        
        return new ComplianceStatistics(
            totalTypes, entityTypes, complexTypes, namespaces,
            processedFileCount, validationResultCount, totalValidationTime, validFiles
        );
    }
    
    /**
     * 统计信息结构
     */
    public static class ComplianceStatistics {
        private final int totalTypes;
        private final int entityTypes;
        private final int complexTypes;
        private final int namespaces;
        private final int processedFiles;
        private final int validationResults;
        private final long totalValidationTimeMs;
        private final long validFiles;
        
        public ComplianceStatistics(int totalTypes, int entityTypes, int complexTypes, int namespaces,
                                  int processedFiles, int validationResults, long totalValidationTimeMs, long validFiles) {
            this.totalTypes = totalTypes;
            this.entityTypes = entityTypes;
            this.complexTypes = complexTypes;
            this.namespaces = namespaces;
            this.processedFiles = processedFiles;
            this.validationResults = validationResults;
            this.totalValidationTimeMs = totalValidationTimeMs;
            this.validFiles = validFiles;
        }
        
        // Getters
        public int getTotalTypes() { return totalTypes; }
        public int getEntityTypes() { return entityTypes; }
        public int getComplexTypes() { return complexTypes; }
        public int getNamespaces() { return namespaces; }
        public int getProcessedFiles() { return processedFiles; }
        public int getValidationResults() { return validationResults; }
        public long getTotalValidationTimeMs() { return totalValidationTimeMs; }
        public long getValidFiles() { return validFiles; }
        public double getValidationSuccessRate() {
            return validationResults > 0 ? (double) validFiles / validationResults : 0.0;
        }
        
        @Override
        public String toString() {
            return String.format(
                "ComplianceStatistics{totalTypes=%d, entityTypes=%d, complexTypes=%d, " +
                "namespaces=%d, processedFiles=%d, validationResults=%d, " +
                "totalValidationTimeMs=%d, validFiles=%d, successRate=%.2f%%}",
                totalTypes, entityTypes, complexTypes, namespaces, processedFiles,
                validationResults, totalValidationTimeMs, validFiles, getValidationSuccessRate() * 100
            );
        }
    }
}
