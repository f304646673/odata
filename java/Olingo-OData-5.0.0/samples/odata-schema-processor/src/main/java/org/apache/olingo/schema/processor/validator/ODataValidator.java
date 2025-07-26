package org.apache.olingo.schema.processor.validator;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * OData 4.0规范验证器接口
 */
public interface ODataValidator {
    
    /**
     * 验证单个Schema是否符合OData 4.0规范
     * @param schema 要验证的Schema
     * @return 验证结果
     */
    ValidationResult validateSchema(CsdlSchema schema);
    
    /**
     * 验证多个Schema的一致性
     * @param schemas Schema列表
     * @return 验证结果
     */
    ValidationResult validateSchemas(List<CsdlSchema> schemas);
    
    /**
     * 验证Schema之间的依赖关系
     * @param schemas Schema列表
     * @param availableNamespaces 可用的namespace集合
     * @return 验证结果
     */
    ValidationResult validateDependencies(List<CsdlSchema> schemas, Set<String> availableNamespaces);
    
    /**
     * 验证引用的完整性
     * @param schemas Schema列表
     * @return 验证结果
     */
    ValidationResult validateReferenceIntegrity(List<CsdlSchema> schemas);
    
    /**
     * 验证namespace的唯一性
     * @param schemas Schema列表
     * @return 验证结果
     */
    ValidationResult validateNamespaceUniqueness(List<CsdlSchema> schemas);
    
    /**
     * 验证类型定义的完整性
     * @param schema 要验证的Schema
     * @return 验证结果
     */
    ValidationResult validateTypeDefinitions(CsdlSchema schema);
    
    /**
     * 验证导入声明的正确性
     * @param schema Schema
     * @param availableNamespaces 可用的namespace集合
     * @return 验证结果
     */
    ValidationResult validateImports(CsdlSchema schema, Set<String> availableNamespaces);
    
    /**
     * 验证结果
     */
    class ValidationResult {
        private final boolean valid;
        private final List<ValidationError> errors;
        private final List<ValidationWarning> warnings;
        private final Map<String, Object> metadata;
        
        public ValidationResult(boolean valid, List<ValidationError> errors,
                               List<ValidationWarning> warnings, Map<String, Object> metadata) {
            this.valid = valid;
            this.errors = errors;
            this.warnings = warnings;
            this.metadata = metadata;
        }
        
        public boolean isValid() { return valid; }
        public List<ValidationError> getErrors() { return errors; }
        public List<ValidationWarning> getWarnings() { return warnings; }
        public Map<String, Object> getMetadata() { return metadata; }
    }
    
    /**
     * 验证错误
     */
    class ValidationError {
        private final ErrorType type;
        private final String message;
        private final String element;
        private final String location;
        private final String suggestion;
        
        public ValidationError(ErrorType type, String message, String element,
                              String location, String suggestion) {
            this.type = type;
            this.message = message;
            this.element = element;
            this.location = location;
            this.suggestion = suggestion;
        }
        
        public ErrorType getType() { return type; }
        public String getMessage() { return message; }
        public String getElement() { return element; }
        public String getLocation() { return location; }
        public String getSuggestion() { return suggestion; }
    }
    
    /**
     * 验证警告
     */
    class ValidationWarning {
        private final WarningType type;
        private final String message;
        private final String element;
        private final String location;
        private final String recommendation;
        
        public ValidationWarning(WarningType type, String message, String element,
                                String location, String recommendation) {
            this.type = type;
            this.message = message;
            this.element = element;
            this.location = location;
            this.recommendation = recommendation;
        }
        
        public WarningType getType() { return type; }
        public String getMessage() { return message; }
        public String getElement() { return element; }
        public String getLocation() { return location; }
        public String getRecommendation() { return recommendation; }
    }
    
    /**
     * 错误类型
     */
    enum ErrorType {
        MISSING_NAMESPACE,
        INVALID_NAMESPACE_FORMAT,
        DUPLICATE_ELEMENT,
        MISSING_REQUIRED_ELEMENT,
        INVALID_TYPE_REFERENCE,
        MISSING_IMPORT,
        CIRCULAR_DEPENDENCY,
        INVALID_CARDINALITY,
        SCHEMA_VERSION_MISMATCH,
        CONSTRAINT_VIOLATION
    }
    
    /**
     * 警告类型
     */
    enum WarningType {
        UNUSED_IMPORT,
        DEPRECATED_FEATURE,
        PERFORMANCE_CONCERN,
        NAMING_CONVENTION,
        BEST_PRACTICE_VIOLATION,
        COMPATIBILITY_ISSUE
    }
}
