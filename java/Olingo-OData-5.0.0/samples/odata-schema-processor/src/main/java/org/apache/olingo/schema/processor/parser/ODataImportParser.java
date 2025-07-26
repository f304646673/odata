package org.apache.olingo.schema.processor.parser;

import java.util.List;
import java.util.Set;

/**
 * OData XML导入解析器接口
 * 用于解析和管理OData 4.0 XML文件中的导入依赖关系
 */
public interface ODataImportParser {
    
    /**
     * 从XML内容中提取导入信息
     * @param xmlContent XML内容
     * @param sourceFile 源文件路径（用于错误报告）
     * @return 导入解析结果
     */
    ImportParseResult parseImports(String xmlContent, String sourceFile);
    
    /**
     * 从Schema中提取外部引用
     * @param xmlContent XML内容
     * @param declaredNamespaces 已声明的namespace集合
     * @return 外部引用列表
     */
    List<ExternalReference> extractExternalReferences(String xmlContent, Set<String> declaredNamespaces);
    
    /**
     * 验证导入的完整性
     * @param imports 导入列表
     * @param externalReferences 外部引用列表
     * @return 验证结果
     */
    ImportValidationResult validateImports(List<ODataImport> imports, List<ExternalReference> externalReferences);
    
    /**
     * 导入解析结果
     */
    class ImportParseResult {
        private final List<ODataImport> imports;
        private final List<ExternalReference> externalReferences;
        private final boolean success;
        private final List<String> errors;
        private final List<String> warnings;
        
        public ImportParseResult(List<ODataImport> imports, List<ExternalReference> externalReferences, 
                               boolean success, List<String> errors, List<String> warnings) {
            this.imports = imports;
            this.externalReferences = externalReferences;
            this.success = success;
            this.errors = errors;
            this.warnings = warnings;
        }
        
        public List<ODataImport> getImports() { return imports; }
        public List<ExternalReference> getExternalReferences() { return externalReferences; }
        public boolean isSuccess() { return success; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        
        @Override
        public String toString() {
            return "ImportParseResult{" +
                "imports=" + imports.size() +
                ", externalReferences=" + externalReferences.size() +
                ", success=" + success +
                ", errors=" + errors.size() +
                ", warnings=" + warnings.size() +
                '}';
        }
    }
    
    /**
     * OData导入信息
     */
    class ODataImport {
        private final String namespace;
        private final String alias;
        private final String include;
        private final String includeAnnotations;
        
        public ODataImport(String namespace, String alias, String include, String includeAnnotations) {
            this.namespace = namespace;
            this.alias = alias;
            this.include = include;
            this.includeAnnotations = includeAnnotations;
        }
        
        public String getNamespace() { return namespace; }
        public String getAlias() { return alias; }
        public String getInclude() { return include; }
        public String getIncludeAnnotations() { return includeAnnotations; }
        
        @Override
        public String toString() {
            return "ODataImport{" +
                "namespace='" + namespace + '\'' +
                ", alias='" + alias + '\'' +
                ", include='" + include + '\'' +
                ", includeAnnotations='" + includeAnnotations + '\'' +
                '}';
        }
    }
    
    /**
     * 外部引用信息
     */
    class ExternalReference {
        private final String fullyQualifiedName;
        private final String namespace;
        private final String localName;
        private final ReferenceType type;
        private final String location; // 在XML中的位置信息
        
        public ExternalReference(String fullyQualifiedName, ReferenceType type, String location) {
            this.fullyQualifiedName = fullyQualifiedName;
            this.type = type;
            this.location = location;
            
            // 解析namespace和localName
            int lastDotIndex = fullyQualifiedName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                this.namespace = fullyQualifiedName.substring(0, lastDotIndex);
                this.localName = fullyQualifiedName.substring(lastDotIndex + 1);
            } else {
                this.namespace = "";
                this.localName = fullyQualifiedName;
            }
        }
        
        public String getFullyQualifiedName() { return fullyQualifiedName; }
        public String getNamespace() { return namespace; }
        public String getLocalName() { return localName; }
        public ReferenceType getType() { return type; }
        public String getLocation() { return location; }
        
        @Override
        public String toString() {
            return "ExternalReference{" +
                "fullyQualifiedName='" + fullyQualifiedName + '\'' +
                ", type=" + type +
                ", location='" + location + '\'' +
                '}';
        }
    }
    
    /**
     * 引用类型枚举
     */
    enum ReferenceType {
        ENTITY_TYPE,
        COMPLEX_TYPE,
        ENUM_TYPE,
        TYPE_DEFINITION,
        ACTION,
        FUNCTION,
        ENTITY_SET,
        SINGLETON,
        TERM,
        ANNOTATION
    }
    
    /**
     * 导入验证结果
     */
    class ImportValidationResult {
        private final boolean valid;
        private final List<String> missingImports;
        private final List<String> unusedImports;
        private final List<String> errors;
        private final List<String> warnings;
        
        public ImportValidationResult(boolean valid, List<String> missingImports, 
                                    List<String> unusedImports, List<String> errors, List<String> warnings) {
            this.valid = valid;
            this.missingImports = missingImports;
            this.unusedImports = unusedImports;
            this.errors = errors;
            this.warnings = warnings;
        }
        
        public boolean isValid() { return valid; }
        public List<String> getMissingImports() { return missingImports; }
        public List<String> getUnusedImports() { return unusedImports; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        
        @Override
        public String toString() {
            return "ImportValidationResult{" +
                "valid=" + valid +
                ", missingImports=" + missingImports.size() +
                ", unusedImports=" + unusedImports.size() +
                ", errors=" + errors.size() +
                ", warnings=" + warnings.size() +
                '}';
        }
    }
}
