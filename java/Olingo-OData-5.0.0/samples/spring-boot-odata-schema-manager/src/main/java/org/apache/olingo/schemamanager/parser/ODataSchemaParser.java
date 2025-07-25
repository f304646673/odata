package org.apache.olingo.schemamanager.parser;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;

/**
 * OData Schema解析器接口
 * 负责使用Olingo底层方法解析XML文件
 * OData 4 的一个 XML（CSDL/EDMX）可以包含多个 <Schema> 元素，每个 <Schema> 可以有自己的 namespace。
 * 但同一个 XML 文件中的不同 <Schema> 不允许有相同的 namespace。
 * OData 4.0 规范要求：每个 <Schema> 的 Namespace 属性必须唯一，不能重复。否则会导致解析冲突和元数据不一致。
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
     * 解析结果类 - 支持多个Schema
     */
    class ParseResult {
        private final List<SchemaWithDependencies> schemas;
        private final boolean success;
        private final String errorMessage;
        
        // 构造函数 - 单个Schema（向后兼容）
        public ParseResult(CsdlSchema schema, List<String> dependencies, boolean success, String errorMessage) {
            this.schemas = new ArrayList<>();
            if (schema != null) {
                this.schemas.add(new SchemaWithDependencies(schema, dependencies));
            }
            this.success = success;
            this.errorMessage = errorMessage;
        }
        
        // 构造函数 - 多个Schema
        public ParseResult(List<SchemaWithDependencies> schemas, boolean success, String errorMessage) {
            this.schemas = schemas != null ? new ArrayList<>(schemas) : new ArrayList<>();
            this.success = success;
            this.errorMessage = errorMessage;
        }
        
        // 静态工厂方法
        public static ParseResult success(List<SchemaWithDependencies> schemas) {
            return new ParseResult(schemas, true, null);
        }
        
        public static ParseResult failure(String errorMessage) {
            return new ParseResult(new ArrayList<>(), false, errorMessage);
        }
        
        // Getters
        public List<SchemaWithDependencies> getSchemas() { return new ArrayList<>(schemas); }
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        
        // 向后兼容方法
        @Deprecated
        public CsdlSchema getSchema() { 
            return schemas.isEmpty() ? null : schemas.get(0).getSchema(); 
        }
        
        @Deprecated
        public List<String> getDependencies() { 
            return schemas.isEmpty() ? new ArrayList<>() : schemas.get(0).getDependencies(); 
        }
        
        // 便利方法
        public boolean hasMultipleSchemas() {
            return schemas.size() > 1;
        }
        
        public int getSchemaCount() {
            return schemas.size();
        }
        
        public SchemaWithDependencies getSchemaByNamespace(String namespace) {
            return schemas.stream()
                .filter(s -> namespace.equals(s.getSchema().getNamespace()))
                .findFirst()
                .orElse(null);
        }
    }
    
    /**
     * Schema及其依赖信息
     */
    class SchemaWithDependencies {
        private final CsdlSchema schema;
        private final List<String> dependencies;
        
        public SchemaWithDependencies(CsdlSchema schema, List<String> dependencies) {
            this.schema = schema;
            this.dependencies = dependencies != null ? new ArrayList<>(dependencies) : new ArrayList<>();
        }
        
        public CsdlSchema getSchema() { return schema; }
        public List<String> getDependencies() { return new ArrayList<>(dependencies); }
        public String getNamespace() { return schema != null ? schema.getNamespace() : null; }
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
