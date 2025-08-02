package org.apache.olingo.compliance.engine.rule.crossfile;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.compliance.core.api.ValidationConfig;
import org.apache.olingo.compliance.engine.core.SchemaRegistry;
import org.apache.olingo.compliance.engine.core.ValidationContext;
import org.apache.olingo.compliance.engine.rule.ValidationRule;
import org.apache.olingo.compliance.engine.rule.structural.AbstractStructuralRule;

/**
 * 跨文件引用验证规则
 * 
 * 这个规则实现了您设想的"辅助检测接口"机制，通过SchemaRegistry提供跨文件引用验证：
 * 
 * 1. 引入的文件是否存在 - 通过检查Reference的Uri并验证对应的Schema是否在Registry中注册
 * 2. 引入的文件是否有对应namespace的schema - 验证Include的Namespace是否在Registry中存在
 * 3. 引用其他文件中的EntityType、ComplexType等是否存在 - 验证类型引用是否在Registry中注册
 * 
 * 这个规则依赖ValidationContext中的SchemaRegistry，实现了解耦设计：
 * - 在验证前，所有XML文件先被解析并注册到SchemaRegistry中
 * - 验证时，通过Registry检查跨文件引用的正确性
 * - 验证后，可以导出Registry数据供后续验证使用
 */
public class CrossFileReferenceValidationRule extends AbstractStructuralRule {
    
    // 匹配edmx:Reference元素的正则表达式
    private static final Pattern REFERENCE_PATTERN = Pattern.compile(
        "<edmx:Reference\\s+Uri\\s*=\\s*[\"']([^\"']+)[\"'][^>]*>", 
        Pattern.CASE_INSENSITIVE
    );
    
    // 匹配edmx:Include元素的正则表达式
    private static final Pattern INCLUDE_PATTERN = Pattern.compile(
        "<edmx:Include\\s+Namespace\\s*=\\s*[\"']([^\"']+)[\"'][^>]*>", 
        Pattern.CASE_INSENSITIVE
    );
    
    // EDM内置类型的命名空间
    private static final Set<String> BUILTIN_NAMESPACES = java.util.Collections.unmodifiableSet(
            new java.util.HashSet<>(java.util.Arrays.asList(
                    "Edm",
                    "System",
                    "http://docs.oasis-open.org/odata/ns/edm"
            ))
    );
    
    public CrossFileReferenceValidationRule() {
        super("cross-file-reference-validation",
              "Validates cross-file type references using Schema Registry",
              "error");
    }
    
    @Override
    protected boolean isStructurallyApplicable(ValidationContext context, ValidationConfig config) {
        // 需要XML内容或已解析的Schema，以及SchemaRegistry
        return (context.getContent() != null || 
                (context.getAllSchemas() != null && !context.getAllSchemas().isEmpty())) &&
               context.getSchemaRegistry() != null;
    }
    
    @Override
    public ValidationRule.RuleResult validate(ValidationContext context, ValidationConfig config) {
        long startTime = System.currentTimeMillis();
        
        SchemaRegistry registry = context.getSchemaRegistry();
        if (registry == null) {
            return ValidationRule.RuleResult.fail(getName(), 
                "SchemaRegistry is required for cross-file reference validation",
                System.currentTimeMillis() - startTime);
        }
        
        // 1. 验证edmx:Reference声明的URI对应的Schema是否在Registry中存在
        String referenceError = validateReferences(context, registry);
        if (referenceError != null) {
            return ValidationRule.RuleResult.fail(getName(), referenceError, System.currentTimeMillis() - startTime);
        }
        
        // 2. 验证edmx:Include声明的Namespace是否在Registry中存在
        String includeError = validateIncludes(context, registry);
        if (includeError != null) {
            return ValidationRule.RuleResult.fail(getName(), includeError, System.currentTimeMillis() - startTime);
        }
        
        // 3. 验证Schema中的类型引用是否在Registry中存在
        String typeReferenceError = validateTypeReferences(context, registry);
        if (typeReferenceError != null) {
            return ValidationRule.RuleResult.fail(getName(), typeReferenceError, System.currentTimeMillis() - startTime);
        }
        
        return ValidationRule.RuleResult.pass(getName(), System.currentTimeMillis() - startTime);
    }
    
    /**
     * 验证edmx:Reference声明的URI对应的Schema是否在Registry中存在
     */
    private String validateReferences(ValidationContext context, SchemaRegistry registry) {
        String xmlContent = getXmlContent(context);
        if (xmlContent == null) {
            return null; // 没有XML内容，跳过验证
        }
        
        Matcher matcher = REFERENCE_PATTERN.matcher(xmlContent);
        
        while (matcher.find()) {
            String uri = matcher.group(1);
            
            // 跳过HTTP/HTTPS引用，这些是外部引用
            if (uri.startsWith("http://") || uri.startsWith("https://")) {
                continue;
            }
            
            // 检查本地文件引用是否在Registry中有对应的Schema
            if (!registry.hasSchemaForFile(uri)) {
                return String.format("Referenced file '%s' is not registered in Schema Registry. " +
                    "The file may not exist or may have parsing errors.", uri);
            }
        }
        
        return null;
    }
    
    /**
     * 验证edmx:Include声明的Namespace是否在Registry中存在
     */
    private String validateIncludes(ValidationContext context, SchemaRegistry registry) {
        String xmlContent = getXmlContent(context);
        if (xmlContent == null) {
            return null;
        }
        
        Matcher matcher = INCLUDE_PATTERN.matcher(xmlContent);
        
        while (matcher.find()) {
            String namespace = matcher.group(1);
            
            // 跳过EDM内置命名空间
            if (BUILTIN_NAMESPACES.contains(namespace)) {
                continue;
            }
            
            // 检查命名空间是否在Registry中存在
            if (!registry.hasNamespace(namespace)) {
                return String.format("Included namespace '%s' is not found in Schema Registry. " +
                    "The referenced schema may not exist or may have parsing errors.", namespace);
            }
        }
        
        return null;
    }
    
    /**
     * 验证Schema中的类型引用是否在Registry中存在
     */
    private String validateTypeReferences(ValidationContext context, SchemaRegistry registry) {
        List<CsdlSchema> schemas = context.getAllSchemas();
        if (schemas == null || schemas.isEmpty()) {
            return null; // 没有解析的Schema，跳过验证
        }
        
        for (CsdlSchema schema : schemas) {
            String currentNamespace = schema.getNamespace();
            
            // 验证EntityType的类型引用
            if (schema.getEntityTypes() != null) {
                for (CsdlEntityType entityType : schema.getEntityTypes()) {
                    String error = validateEntityTypeReferences(entityType, currentNamespace, registry);
                    if (error != null) return error;
                }
            }
            
            // 验证ComplexType的类型引用
            if (schema.getComplexTypes() != null) {
                for (CsdlComplexType complexType : schema.getComplexTypes()) {
                    String error = validateComplexTypeReferences(complexType, currentNamespace, registry);
                    if (error != null) return error;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 验证EntityType中的类型引用
     */
    private String validateEntityTypeReferences(CsdlEntityType entityType, String currentNamespace, SchemaRegistry registry) {
        // 验证BaseType引用
        if (entityType.getBaseType() != null) {
            String error = validateTypeReference(entityType.getBaseType(), currentNamespace, registry, 
                "EntityType", entityType.getName());
            if (error != null) return error;
        }
        
        // 验证Property类型引用
        if (entityType.getProperties() != null) {
            for (CsdlProperty property : entityType.getProperties()) {
                if (property.getType() != null) {
                    String actualType = extractActualType(property.getType());
                    String error = validateTypeReference(actualType, currentNamespace, registry,
                        "Property", property.getName() + " in EntityType " + entityType.getName());
                    if (error != null) return error;
                }
            }
        }
        
        // 验证NavigationProperty类型引用
        if (entityType.getNavigationProperties() != null) {
            for (CsdlNavigationProperty navProp : entityType.getNavigationProperties()) {
                if (navProp.getType() != null) {
                    String actualType = extractActualType(navProp.getType());
                    String error = validateTypeReference(actualType, currentNamespace, registry,
                        "NavigationProperty", navProp.getName() + " in EntityType " + entityType.getName());
                    if (error != null) return error;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 验证ComplexType中的类型引用
     */
    private String validateComplexTypeReferences(CsdlComplexType complexType, String currentNamespace, SchemaRegistry registry) {
        // 验证BaseType引用
        if (complexType.getBaseType() != null) {
            String error = validateTypeReference(complexType.getBaseType(), currentNamespace, registry,
                "ComplexType", complexType.getName());
            if (error != null) return error;
        }
        
        // 验证Property类型引用
        if (complexType.getProperties() != null) {
            for (CsdlProperty property : complexType.getProperties()) {
                if (property.getType() != null) {
                    String actualType = extractActualType(property.getType());
                    String error = validateTypeReference(actualType, currentNamespace, registry,
                        "Property", property.getName() + " in ComplexType " + complexType.getName());
                    if (error != null) return error;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 验证单个类型引用
     */
    private String validateTypeReference(String typeRef, String currentNamespace, SchemaRegistry registry, 
                                       String elementKind, String elementName) {
        if (typeRef == null || typeRef.trim().isEmpty()) {
            return null;
        }
        
        // 提取命名空间
        int dotIndex = typeRef.lastIndexOf('.');
        if (dotIndex <= 0) {
            return null; // 没有命名空间，可能是简单类型
        }
        
        String namespace = typeRef.substring(0, dotIndex);
        
        // 跳过EDM内置类型
        if (BUILTIN_NAMESPACES.contains(namespace)) {
            return null;
        }
        
        // 跳过当前命名空间的类型（假设当前Schema中的类型已经存在）
        if (namespace.equals(currentNamespace)) {
            return null;
        }
        
        // 检查类型是否在Registry中存在
        if (!registry.isTypeExists(typeRef)) {
            return String.format("%s '%s' references undefined type '%s'. " +
                "The type may not exist in the referenced schema or the schema may not be properly imported.",
                elementKind, elementName, typeRef);
        }
        
        return null;
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
    
    /**
     * 获取XML内容
     */
    private String getXmlContent(ValidationContext context) {
        if (context.getContent() != null) {
            return context.getContent();
        }
        
        if (context.getFilePath() != null) {
            try {
                return new String(java.nio.file.Files.readAllBytes(context.getFilePath()), java.nio.charset.StandardCharsets.UTF_8);
            } catch (java.io.IOException e) {
                // 忽略错误，返回null
            }
        }
        
        return null;
    }
}
