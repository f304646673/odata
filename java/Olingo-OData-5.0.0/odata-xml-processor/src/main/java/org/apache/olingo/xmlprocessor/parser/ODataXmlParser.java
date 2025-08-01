package org.apache.olingo.xmlprocessor.parser;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Collections;

/**
 * OData XML解析器接口
 */
public interface ODataXmlParser {
    
    /**
     * 从输入流解析Schema
     * @param inputStream 输入流
     * @param sourceName 源名称（用于错误报告）
     * @return 解析结果
     */
    ParseResult parseSchemas(InputStream inputStream, String sourceName);
    
    /**
     * 从文件解析Schema
     * @param filePath 文件路径
     * @return 解析结果
     */
    ParseResult parseSchemas(Path filePath);
    
    /**
     * 解析XML内容
     * @param xmlContent XML内容
     * @param sourceName 源名称
     * @return 解析结果
     */
    ParseResult parseSchemas(String xmlContent, String sourceName);
    
    /**
     * 验证XML格式是否正确
     * @param xmlContent XML内容
     * @return 验证结果
     */
    ValidationResult validateXmlFormat(String xmlContent);
    
    /**
     * 解析结果
     */
    class ParseResult {
        private final boolean success;
        private final List<CsdlSchema> schemas;
        private final List<String> errors;
        private final List<String> warnings;
        private final String sourceName;
        
        public ParseResult(boolean success, List<CsdlSchema> schemas, List<String> errors,
                          List<String> warnings, String sourceName) {
            this.success = success;
            this.schemas = schemas;
            this.errors = errors;
            this.warnings = warnings;
            this.sourceName = sourceName;
        }
        
        public boolean isSuccess() { return success; }
        public List<CsdlSchema> getSchemas() { return schemas; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        public String getSourceName() { return sourceName; }
        
        public static ParseResult success(List<CsdlSchema> schemas, String sourceName) {
            return new ParseResult(true, schemas, Collections.emptyList(), Collections.emptyList(), sourceName);
        }
        
        public static ParseResult failure(List<String> errors, String sourceName) {
            return new ParseResult(false, Collections.emptyList(), errors, Collections.emptyList(), sourceName);
        }
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
