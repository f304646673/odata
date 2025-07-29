package org.apache.olingo.schema.processor.validation;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.server.core.MetadataParser;
import org.apache.olingo.server.core.SchemaBasedEdmProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OData Schema依赖验证器
 * 
 * 使用Olingo原生方法验证XML Schema文件中的类型引用是否有对应的edmx:Reference导入
 */
public class SchemaReferenceValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(SchemaReferenceValidator.class);
    
    // 匹配namespace.TypeName格式的正则表达式
    private static final Pattern NAMESPACE_TYPE_PATTERN = Pattern.compile("^([A-Za-z0-9_\\.]+)\\.([A-Za-z0-9_]+)$");
    
    /**
     * 验证结果
     */
    public static class ValidationResult {
        private final boolean isValid;
        private final List<String> errors;
        private final Set<String> referencedNamespaces;
        private final Set<String> declaredReferences;
        private final Set<String> includedNamespaces;
        
        public ValidationResult(boolean isValid, List<String> errors, 
                              Set<String> referencedNamespaces, Set<String> declaredReferences,
                              Set<String> includedNamespaces) {
            this.isValid = isValid;
            this.errors = new ArrayList<>(errors);
            this.referencedNamespaces = new HashSet<>(referencedNamespaces);
            this.declaredReferences = new HashSet<>(declaredReferences);
            this.includedNamespaces = new HashSet<>(includedNamespaces);
        }
        
        public boolean isValid() { return isValid; }
        public List<String> getErrors() { return errors; }
        public Set<String> getReferencedNamespaces() { return referencedNamespaces; }
        public Set<String> getDeclaredReferences() { return declaredReferences; }
        public Set<String> getIncludedNamespaces() { return includedNamespaces; }
        public Set<String> getMissingReferences() {
            Set<String> missing = new HashSet<>(referencedNamespaces);
            missing.removeAll(includedNamespaces);
            return missing;
        }
    }
    
    /**
     * 使用Olingo原生方法验证XML文件的依赖引用
     */
    public ValidationResult validateSchemaReferences(Path xmlFile) throws Exception {
        List<String> errors = new ArrayList<>();
        Set<String> referencedNamespaces = new HashSet<>();
        Set<String> includedNamespaces = new HashSet<>();
        Set<String> declaredReferences = new HashSet<>();
        
        try (FileInputStream fis = new FileInputStream(xmlFile.toFile())) {
            // 使用Olingo的MetadataParser解析CSDL文档
            MetadataParser parser = new MetadataParser();
            java.io.InputStreamReader reader = new java.io.InputStreamReader(fis, "UTF-8");
            SchemaBasedEdmProvider edmProvider = parser.buildEdmProvider(reader);
            
            // 获取引用的命名空间
            List<EdmxReference> references = edmProvider.getReferences();
            if (references != null) {
                for (EdmxReference ref : references) {
                    declaredReferences.add(ref.getUri().toString());
                    logger.debug("Found reference: {}", ref.getUri());
                    if (ref.getIncludes() != null) {
                        ref.getIncludes().forEach(include -> {
                            includedNamespaces.add(include.getNamespace());
                            logger.debug("Found included namespace: {}", include.getNamespace());
                        });
                    }
                }
            }
            
            // 获取Schema并检查类型引用
            List<CsdlSchema> schemas = edmProvider.getSchemas();
            if (schemas != null) {
                for (CsdlSchema schema : schemas) {
                    String currentNamespace = schema.getNamespace();
                    logger.debug("Current schema namespace: {}", currentNamespace);
                    
                    // 检查EntityType的类型引用
                    if (schema.getEntityTypes() != null) {
                        for (CsdlEntityType entityType : schema.getEntityTypes()) {
                            checkEntityTypeReferences(entityType, currentNamespace, referencedNamespaces);
                        }
                    }
                    
                    // 检查ComplexType的类型引用
                    if (schema.getComplexTypes() != null) {
                        for (CsdlComplexType complexType : schema.getComplexTypes()) {
                            checkComplexTypeReferences(complexType, currentNamespace, referencedNamespaces);
                        }
                    }
                }
            }
            
            // 检查是否有未声明的引用
            for (String referencedNs : referencedNamespaces) {
                if (!includedNamespaces.contains(referencedNs)) {
                    errors.add("Missing edmx:Reference for namespace: " + referencedNs + 
                              ". Referenced namespaces must be explicitly imported via edmx:Reference.");
                }
            }
            
        } catch (javax.xml.stream.XMLStreamException e) {
            logger.error("Failed to parse XML", e);
            errors.add("XML parsing error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Validation failed", e);
            errors.add("Validation error: " + e.getMessage());
        }
        
        boolean isValid = errors.isEmpty();
        return new ValidationResult(isValid, errors, referencedNamespaces, declaredReferences, includedNamespaces);
    }
    
    /**
     * 检查EntityType的类型引用
     */
    private void checkEntityTypeReferences(CsdlEntityType entityType, String currentNamespace, Set<String> referencedNamespaces) {
        // 检查BaseType引用
        if (entityType.getBaseType() != null) {
            checkTypeReference(entityType.getBaseType(), currentNamespace, referencedNamespaces);
        }
        
        // 检查Property类型引用
        if (entityType.getProperties() != null) {
            for (CsdlProperty property : entityType.getProperties()) {
                if (property.getType() != null) {
                    String actualType = extractActualType(property.getType());
                    checkTypeReference(actualType, currentNamespace, referencedNamespaces);
                }
            }
        }
        
        // 检查NavigationProperty类型引用
        if (entityType.getNavigationProperties() != null) {
            for (CsdlNavigationProperty navProp : entityType.getNavigationProperties()) {
                if (navProp.getType() != null) {
                    String actualType = extractActualType(navProp.getType());
                    checkTypeReference(actualType, currentNamespace, referencedNamespaces);
                }
            }
        }
    }
    
    /**
     * 检查ComplexType的类型引用
     */
    private void checkComplexTypeReferences(CsdlComplexType complexType, String currentNamespace, Set<String> referencedNamespaces) {
        // 检查BaseType引用
        if (complexType.getBaseType() != null) {
            checkTypeReference(complexType.getBaseType(), currentNamespace, referencedNamespaces);
        }
        
        // 检查Property类型引用
        if (complexType.getProperties() != null) {
            for (CsdlProperty property : complexType.getProperties()) {
                if (property.getType() != null) {
                    String actualType = extractActualType(property.getType());
                    checkTypeReference(actualType, currentNamespace, referencedNamespaces);
                }
            }
        }
    }
    
    /**
     * EDM内置类型判断，使用Olingo官方定义
     */
    private static boolean isEdmBuiltInType(String typeName) {
        if (typeName == null) return false;
        String actualType = typeName;
        if (typeName.startsWith("Collection(") && typeName.endsWith(")")) {
            actualType = typeName.substring(11, typeName.length() - 1);
        }
        try {
            EdmPrimitiveTypeKind.valueOfFQN(actualType);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 检查类型引用
     */
    private void checkTypeReference(String typeReference, String currentNamespace, 
                                   Set<String> referencedNamespaces) {
        if (typeReference == null || typeReference.trim().isEmpty()) {
            return;
        }
        // 跳过EDM内置类型（使用Olingo官方判断）
        if (isEdmBuiltInType(typeReference)) {
            return;
        }
        
        // 解析namespace
        Matcher matcher = NAMESPACE_TYPE_PATTERN.matcher(typeReference);
        if (matcher.matches()) {
            String namespace = matcher.group(1);
            String typeName = matcher.group(2);
            
            // 跳过当前namespace
            if (!namespace.equals(currentNamespace)) {
                referencedNamespaces.add(namespace);
                logger.debug("Found external type reference: {} in namespace: {}", typeName, namespace);
            }
        }
    }
    
    /**
     * 提取实际类型（处理Collection(Type)格式）
     */
    private String extractActualType(String type) {
        if (type.startsWith("Collection(") && type.endsWith(")")) {
            return type.substring(11, type.length() - 1);
        }
        return type;
    }
}
