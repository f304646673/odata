package org.apache.olingo.schemamanager.merger;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import java.util.List;
import java.util.Map;

/**
 * Schema合并器接口
 * 负责将相同namespace的Schema信息进行合并
 */
public interface SchemaMerger {
    
    /**
     * 合并相同namespace的多个Schema
     * @param schemas 待合并的Schema列表
     * @return 合并结果
     */
    MergeResult mergeSchemas(List<CsdlSchema> schemas);
    
    /**
     * 合并所有Schema按namespace分组
     * @param schemaMap Schema映射 (filePath -> schema)
     * @return 按namespace分组的合并结果
     */
    Map<String, CsdlSchema> mergeByNamespace(Map<String, CsdlSchema> schemaMap);
    
    /**
     * 验证Schema合并的兼容性
     * @param existingSchema 现有Schema
     * @param newSchema 新Schema
     * @return 兼容性检查结果
     */
    CompatibilityResult checkCompatibility(CsdlSchema existingSchema, CsdlSchema newSchema);
    
    /**
     * 解决Schema合并冲突
     * @param conflictingSchemas 冲突的Schema列表
     * @param resolution 冲突解决策略
     * @return 解决后的Schema
     */
    CsdlSchema resolveConflicts(List<CsdlSchema> conflictingSchemas, ConflictResolution resolution);
    
    /**
     * 合并结果类
     */
    class MergeResult {
        private final CsdlSchema mergedSchema;
        private final List<String> warnings;
        private final List<String> errors;
        private final boolean success;
        
        public MergeResult(CsdlSchema mergedSchema, List<String> warnings, List<String> errors, boolean success) {
            this.mergedSchema = mergedSchema;
            this.warnings = warnings;
            this.errors = errors;
            this.success = success;
        }
        
        // Getters
        public CsdlSchema getMergedSchema() { return mergedSchema; }
        public List<String> getWarnings() { return warnings; }
        public List<String> getErrors() { return errors; }
        public boolean isSuccess() { return success; }
    }
    
    /**
     * 兼容性检查结果类
     */
    class CompatibilityResult {
        private final boolean compatible;
        private final List<String> conflicts;
        private final List<String> warnings;
        
        public CompatibilityResult(boolean compatible, List<String> conflicts, List<String> warnings) {
            this.compatible = compatible;
            this.conflicts = conflicts;
            this.warnings = warnings;
        }
        
        // Getters
        public boolean isCompatible() { return compatible; }
        public List<String> getConflicts() { return conflicts; }
        public List<String> getWarnings() { return warnings; }
    }
    
    /**
     * 冲突解决策略枚举
     */
    enum ConflictResolution {
        /** 保留第一个遇到的定义 */
        KEEP_FIRST,
        /** 保留最后一个遇到的定义 */
        KEEP_LAST,
        /** 抛出异常 */
        THROW_ERROR,
        /** 尝试自动合并 */
        AUTO_MERGE
    }
}
