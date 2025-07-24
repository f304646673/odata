package org.apache.olingo.schemamanager.parser;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import java.io.InputStream;
import java.util.List;

/**
 * OData Schema解析器接口
 * 负责使用Olingo底层方法解析XML文件
 */
public interface ODataSchemaParser {
    
    /**
     * 解析输入流中的OData XML
     * @param inputStream XML输入流
     * @param sourceName 源名称
     * @return 解析结果
     */
    ParseResult parseSchema(InputStream inputStream, String sourceName);
    
    /**
     * 获取Schema中的依赖关系
     * @param schema CSDL Schema
     * @return 依赖的namespace列表
     */
    List<String> extractDependencies(CsdlSchema schema);
    
    /**
     * 验证Schema的完整性
     * @param schema CSDL Schema
     * @return 验证结果
     */
    ValidationResult validateSchema(CsdlSchema schema);
    
    /**
     * 解析结果类
     */
    class ParseResult {
        private final CsdlSchema schema;
        private final List<String> dependencies;
        private final boolean success;
        private final String errorMessage;
        
        public ParseResult(CsdlSchema schema, List<String> dependencies, boolean success, String errorMessage) {
            this.schema = schema;
            this.dependencies = dependencies;
            this.success = success;
            this.errorMessage = errorMessage;
        }
        
        // Getters
        public CsdlSchema getSchema() { return schema; }
        public List<String> getDependencies() { return dependencies; }
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    /**
     * 验证结果类
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
        
        // Getters
        public boolean isValid() { return valid; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
    }
}
