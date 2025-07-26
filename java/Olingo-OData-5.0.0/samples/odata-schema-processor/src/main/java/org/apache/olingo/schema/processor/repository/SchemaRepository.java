package org.apache.olingo.schema.processor.repository;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Schema仓库接口，用于管理和合并Schema
 */
public interface SchemaRepository {
    
    /**
     * 添加Schema到仓库
     * @param schema 要添加的Schema
     * @return 添加结果
     */
    AddResult addSchema(CsdlSchema schema);
    
    /**
     * 批量添加Schema
     * @param schemas 要添加的Schema列表
     * @return 添加结果
     */
    AddResult addSchemas(List<CsdlSchema> schemas);
    
    /**
     * 根据namespace获取Schema
     * @param namespace Schema的namespace
     * @return Schema，如果不存在返回null
     */
    CsdlSchema getSchema(String namespace);
    
    /**
     * 获取所有Schema
     * @return 所有Schema的列表
     */
    List<CsdlSchema> getAllSchemas();
    
    /**
     * 获取所有namespace
     * @return namespace集合
     */
    Set<String> getAllNamespaces();
    
    /**
     * 检查指定namespace是否存在
     * @param namespace 要检查的namespace
     * @return 如果存在返回true
     */
    boolean containsNamespace(String namespace);
    
    /**
     * 移除指定namespace的Schema
     * @param namespace 要移除的namespace
     * @return 如果移除成功返回true
     */
    boolean removeSchema(String namespace);
    
    /**
     * 清空所有Schema
     */
    void clear();
    
    /**
     * 合并同名namespace的Schema
     * @param conflictResolution 冲突解决策略
     * @return 合并结果
     */
    MergeResult mergeSchemas(ConflictResolution conflictResolution);
    
    /**
     * 验证所有Schema的一致性
     * @return 验证结果
     */
    ValidationResult validateAll();
    
    /**
     * 冲突解决策略
     */
    enum ConflictResolution {
        /** 保留第一个定义 */
        KEEP_FIRST,
        /** 保留最后一个定义 */
        KEEP_LAST,
        /** 抛出异常 */
        THROW_EXCEPTION,
        /** 尝试合并 */
        MERGE
    }
    
    /**
     * 添加结果
     */
    class AddResult {
        private final boolean success;
        private final List<String> errors;
        private final List<String> warnings;
        private final int addedCount;
        private final int conflictCount;
        
        public AddResult(boolean success, List<String> errors, List<String> warnings,
                        int addedCount, int conflictCount) {
            this.success = success;
            this.errors = errors;
            this.warnings = warnings;
            this.addedCount = addedCount;
            this.conflictCount = conflictCount;
        }
        
        public boolean isSuccess() { return success; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        public int getAddedCount() { return addedCount; }
        public int getConflictCount() { return conflictCount; }
    }
    
    /**
     * 合并结果
     */
    class MergeResult {
        private final boolean success;
        private final List<String> errors;
        private final List<String> warnings;
        private final int mergedCount;
        private final Map<String, List<String>> conflicts;
        
        public MergeResult(boolean success, List<String> errors, List<String> warnings,
                          int mergedCount, Map<String, List<String>> conflicts) {
            this.success = success;
            this.errors = errors;
            this.warnings = warnings;
            this.mergedCount = mergedCount;
            this.conflicts = conflicts;
        }
        
        public boolean isSuccess() { return success; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        public int getMergedCount() { return mergedCount; }
        public Map<String, List<String>> getConflicts() { return conflicts; }
    }
    
    /**
     * 验证结果
     */
    class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;
        
        public ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
            this.valid = valid;
            this.errors = errors;
            this.warnings = warnings;
        }
        
        public boolean isValid() { return valid; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
    }
}
