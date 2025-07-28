package org.apache.olingo.schema.processor.loader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OData 4.0规范验证器
 * 验证Schema是否符合OData 4.0标准
 */
public class ODataValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(ODataValidator.class);
    
    // OData 4.0规范中的命名规则
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9_]*(?:\\.[A-Za-z][A-Za-z0-9_]*)*$");
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");
    
    // 内置类型
    private static final Set<String> PRIMITIVE_TYPES = Set.of(
        "Edm.String", "Edm.Int32", "Edm.Int64", "Edm.Boolean", "Edm.DateTime", 
        "Edm.DateTimeOffset", "Edm.Decimal", "Edm.Double", "Edm.Guid", 
        "Edm.Binary", "Edm.Byte", "Edm.Int16", "Edm.Single", "Edm.Time"
    );
    
    /**
     * 验证结果
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;
        private final Set<String> dependencies;
        
        public ValidationResult(boolean valid, List<String> errors, List<String> warnings, Set<String> dependencies) {
            this.valid = valid;
            this.errors = new ArrayList<>(errors);
            this.warnings = new ArrayList<>(warnings);
            this.dependencies = new HashSet<>(dependencies);
        }
        
        public boolean isValid() { return valid; }
        public List<String> getErrors() { return new ArrayList<>(errors); }
        public List<String> getWarnings() { return new ArrayList<>(warnings); }
        public Set<String> getDependencies() { return new HashSet<>(dependencies); }
    }
    
    /**
     * 验证单个Schema
     */
    public ValidationResult validateSchema(CsdlSchema schema) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Set<String> dependencies = new HashSet<>();
        
        if (schema == null) {
            errors.add("Schema cannot be null");
            return new ValidationResult(false, errors, warnings, dependencies);
        }
        
        // 验证namespace
        validateNamespace(schema.getNamespace(), errors);
        
        // 验证EntityTypes
        if (schema.getEntityTypes() != null) {
            for (CsdlEntityType entityType : schema.getEntityTypes()) {
                validateEntityType(entityType, schema.getNamespace(), errors, warnings, dependencies);
            }
        }
        
        // 验证ComplexTypes
        if (schema.getComplexTypes() != null) {
            for (CsdlComplexType complexType : schema.getComplexTypes()) {
                validateComplexType(complexType, schema.getNamespace(), errors, warnings, dependencies);
            }
        }
        
        // 验证EntityContainer
        if (schema.getEntityContainer() != null) {
            validateEntityContainer(schema.getEntityContainer(), schema.getNamespace(), errors, warnings, dependencies);
        }
        
        // 验证Actions
        if (schema.getActions() != null) {
            for (CsdlAction action : schema.getActions()) {
                validateAction(action, schema.getNamespace(), errors, warnings, dependencies);
            }
        }
        
        // 验证Functions
        if (schema.getFunctions() != null) {
            for (CsdlFunction function : schema.getFunctions()) {
                validateFunction(function, schema.getNamespace(), errors, warnings, dependencies);
            }
        }
        
        return new ValidationResult(errors.isEmpty(), errors, warnings, dependencies);
    }
    
    /**
     * 验证namespace格式
     */
    private void validateNamespace(String namespace, List<String> errors) {
        if (namespace == null || namespace.trim().isEmpty()) {
            errors.add("Namespace cannot be null or empty");
            return;
        }
        
        if (!NAMESPACE_PATTERN.matcher(namespace).matches()) {
            errors.add("Invalid namespace format: " + namespace);
        }
        
        if (namespace.length() > 512) {
            errors.add("Namespace too long (max 512 characters): " + namespace);
        }
    }
    
    /**
     * 验证EntityType
     */
    private void validateEntityType(CsdlEntityType entityType, String namespace, 
                                  List<String> errors, List<String> warnings, Set<String> dependencies) {
        if (entityType.getName() == null || entityType.getName().trim().isEmpty()) {
            errors.add("EntityType name cannot be null or empty");
            return;
        }
        
        // 验证名称格式
        if (!IDENTIFIER_PATTERN.matcher(entityType.getName()).matches()) {
            errors.add("Invalid EntityType name format: " + entityType.getName());
        }
        
        // 验证BaseType依赖
        if (entityType.getBaseType() != null) {
            String baseType = entityType.getBaseType();
            if (!baseType.startsWith(namespace + ".") && !PRIMITIVE_TYPES.contains(baseType)) {
                String baseNamespace = extractNamespace(baseType);
                if (baseNamespace != null && !baseNamespace.equals(namespace)) {
                    dependencies.add(baseNamespace);
                }
            }
        }
        
        // 验证Key
        if (entityType.getKey() != null && !entityType.getKey().isEmpty()) {
            Set<String> keyPropertyNames = new HashSet<>();
            for (CsdlPropertyRef keyRef : entityType.getKey()) {
                if (keyRef.getName() == null || keyRef.getName().trim().isEmpty()) {
                    errors.add("Key property name cannot be null or empty in EntityType: " + entityType.getName());
                } else {
                    if (keyPropertyNames.contains(keyRef.getName())) {
                        errors.add("Duplicate key property: " + keyRef.getName() + " in EntityType: " + entityType.getName());
                    }
                    keyPropertyNames.add(keyRef.getName());
                }
            }
            
            // 验证Key属性是否存在于Properties中
            if (entityType.getProperties() != null) {
                Set<String> propertyNames = new HashSet<>();
                for (CsdlProperty prop : entityType.getProperties()) {
                    propertyNames.add(prop.getName());
                }
                
                for (String keyName : keyPropertyNames) {
                    if (!propertyNames.contains(keyName)) {
                        errors.add("Key property '" + keyName + "' not found in properties of EntityType: " + entityType.getName());
                    }
                }
            }
        } else if (entityType.getBaseType() == null) {
            // 没有BaseType的EntityType必须有Key
            warnings.add("EntityType without BaseType should have a Key: " + entityType.getName());
        }
        
        // 验证Properties
        if (entityType.getProperties() != null) {
            Set<String> propertyNames = new HashSet<>();
            for (CsdlProperty property : entityType.getProperties()) {
                validateProperty(property, entityType.getName(), "EntityType", propertyNames, errors, warnings, dependencies, namespace);
            }
        }
    }
    
    /**
     * 验证ComplexType
     */
    private void validateComplexType(CsdlComplexType complexType, String namespace,
                                   List<String> errors, List<String> warnings, Set<String> dependencies) {
        if (complexType.getName() == null || complexType.getName().trim().isEmpty()) {
            errors.add("ComplexType name cannot be null or empty");
            return;
        }
        
        // 验证名称格式
        if (!IDENTIFIER_PATTERN.matcher(complexType.getName()).matches()) {
            errors.add("Invalid ComplexType name format: " + complexType.getName());
        }
        
        // 验证BaseType依赖
        if (complexType.getBaseType() != null) {
            String baseType = complexType.getBaseType();
            if (!baseType.startsWith(namespace + ".")) {
                String baseNamespace = extractNamespace(baseType);
                if (baseNamespace != null && !baseNamespace.equals(namespace)) {
                    dependencies.add(baseNamespace);
                }
            }
        }
        
        // 验证Properties
        if (complexType.getProperties() != null) {
            Set<String> propertyNames = new HashSet<>();
            for (CsdlProperty property : complexType.getProperties()) {
                validateProperty(property, complexType.getName(), "ComplexType", propertyNames, errors, warnings, dependencies, namespace);
            }
        }
    }
    
    /**
     * 验证EntityContainer
     */
    private void validateEntityContainer(CsdlEntityContainer container, String namespace,
                                       List<String> errors, List<String> warnings, Set<String> dependencies) {
        if (container.getName() == null || container.getName().trim().isEmpty()) {
            errors.add("EntityContainer name cannot be null or empty");
            return;
        }
        
        // 验证名称格式
        if (!IDENTIFIER_PATTERN.matcher(container.getName()).matches()) {
            errors.add("Invalid EntityContainer name format: " + container.getName());
        }
        
        // 验证EntitySets
        if (container.getEntitySets() != null) {
            Set<String> entitySetNames = new HashSet<>();
            for (CsdlEntitySet entitySet : container.getEntitySets()) {
                validateEntitySet(entitySet, entitySetNames, errors, warnings, dependencies, namespace);
            }
        }
    }
    
    /**
     * 验证EntitySet
     */
    private void validateEntitySet(CsdlEntitySet entitySet, Set<String> entitySetNames,
                                 List<String> errors, List<String> warnings, Set<String> dependencies, String namespace) {
        if (entitySet.getName() == null || entitySet.getName().trim().isEmpty()) {
            errors.add("EntitySet name cannot be null or empty");
            return;
        }
        
        // 检查重复名称
        if (entitySetNames.contains(entitySet.getName())) {
            errors.add("Duplicate EntitySet name: " + entitySet.getName());
        }
        entitySetNames.add(entitySet.getName());
        
        // 验证名称格式
        if (!IDENTIFIER_PATTERN.matcher(entitySet.getName()).matches()) {
            errors.add("Invalid EntitySet name format: " + entitySet.getName());
        }
        
        // 验证EntityType引用
        if (entitySet.getType() != null) {
            String entityType = entitySet.getType();
            if (!entityType.startsWith(namespace + ".")) {
                String typeNamespace = extractNamespace(entityType);
                if (typeNamespace != null && !typeNamespace.equals(namespace)) {
                    dependencies.add(typeNamespace);
                }
            }
        } else {
            errors.add("EntitySet must have a Type: " + entitySet.getName());
        }
    }
    
    /**
     * 验证Action
     */
    private void validateAction(CsdlAction action, String namespace,
                              List<String> errors, List<String> warnings, Set<String> dependencies) {
        if (action.getName() == null || action.getName().trim().isEmpty()) {
            errors.add("Action name cannot be null or empty");
            return;
        }
        
        // 验证名称格式
        if (!IDENTIFIER_PATTERN.matcher(action.getName()).matches()) {
            errors.add("Invalid Action name format: " + action.getName());
        }
        
        // 验证参数类型依赖
        if (action.getParameters() != null) {
            action.getParameters().forEach(param -> {
                if (param.getType() != null) {
                    validateTypeDependency(param.getType(), namespace, dependencies);
                }
            });
        }
        
        // 验证返回类型依赖
        if (action.getReturnType() != null && action.getReturnType().getType() != null) {
            validateTypeDependency(action.getReturnType().getType(), namespace, dependencies);
        }
    }
    
    /**
     * 验证Function
     */
    private void validateFunction(CsdlFunction function, String namespace,
                                List<String> errors, List<String> warnings, Set<String> dependencies) {
        if (function.getName() == null || function.getName().trim().isEmpty()) {
            errors.add("Function name cannot be null or empty");
            return;
        }
        
        // 验证名称格式
        if (!IDENTIFIER_PATTERN.matcher(function.getName()).matches()) {
            errors.add("Invalid Function name format: " + function.getName());
        }
        
        // 验证参数类型依赖
        if (function.getParameters() != null) {
            function.getParameters().forEach(param -> {
                if (param.getType() != null) {
                    validateTypeDependency(param.getType(), namespace, dependencies);
                }
            });
        }
        
        // 验证返回类型依赖
        if (function.getReturnType() != null && function.getReturnType().getType() != null) {
            validateTypeDependency(function.getReturnType().getType(), namespace, dependencies);
        }
    }
    
    /**
     * 验证Property
     */
    private void validateProperty(CsdlProperty property, String parentName, String parentType,
                                Set<String> propertyNames, List<String> errors, List<String> warnings,
                                Set<String> dependencies, String namespace) {
        if (property.getName() == null || property.getName().trim().isEmpty()) {
            errors.add("Property name cannot be null or empty in " + parentType + ": " + parentName);
            return;
        }
        
        // 检查重复属性名
        if (propertyNames.contains(property.getName())) {
            errors.add("Duplicate property name: " + property.getName() + " in " + parentType + ": " + parentName);
        }
        propertyNames.add(property.getName());
        
        // 验证名称格式
        if (!IDENTIFIER_PATTERN.matcher(property.getName()).matches()) {
            errors.add("Invalid property name format: " + property.getName() + " in " + parentType + ": " + parentName);
        }
        
        // 验证类型依赖
        if (property.getType() != null) {
            validateTypeDependency(property.getType(), namespace, dependencies);
        } else {
            errors.add("Property must have a Type: " + property.getName() + " in " + parentType + ": " + parentName);
        }
    }
    
    /**
     * 验证类型依赖并添加到依赖集合
     */
    private void validateTypeDependency(String type, String namespace, Set<String> dependencies) {
        if (type == null || type.trim().isEmpty()) {
            return;
        }
        
        // 处理Collection类型
        String actualType = type;
        if (type.startsWith("Collection(") && type.endsWith(")")) {
            actualType = type.substring(11, type.length() - 1);
        }
        
        // 如果不是基础类型且不属于当前namespace，则添加依赖
        if (!PRIMITIVE_TYPES.contains(actualType) && !actualType.startsWith(namespace + ".")) {
            String typeNamespace = extractNamespace(actualType);
            if (typeNamespace != null && !typeNamespace.equals(namespace)) {
                dependencies.add(typeNamespace);
            }
        }
    }
    
    /**
     * 从完全限定类型名中提取namespace
     */
    private String extractNamespace(String fullyQualifiedTypeName) {
        if (fullyQualifiedTypeName == null || fullyQualifiedTypeName.trim().isEmpty()) {
            return null;
        }
        
        int lastDotIndex = fullyQualifiedTypeName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fullyQualifiedTypeName.substring(0, lastDotIndex);
        }
        
        return null;
    }
}
