package org.apache.olingo.schema.processor.exporter.builder;

import org.apache.olingo.commons.api.edm.provider.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 动态容器构建器
 * 用于动态创建OData容器
 */
public class DynamicContainerBuilder {
    
    private String containerName;
    private String namespace;
    private String alias;
    
    private final List<CsdlEntitySet> entitySets = new ArrayList<>();
    private final List<CsdlSingleton> singletons = new ArrayList<>();
    private final List<CsdlActionImport> actionImports = new ArrayList<>();
    private final List<CsdlFunctionImport> functionImports = new ArrayList<>();
    
    private final List<CsdlEntityType> entityTypes = new ArrayList<>();
    private final List<CsdlComplexType> complexTypes = new ArrayList<>();
    private final List<CsdlAction> actions = new ArrayList<>();
    private final List<CsdlFunction> functions = new ArrayList<>();
    private final List<CsdlEnumType> enumTypes = new ArrayList<>();
    private final List<CsdlTypeDefinition> typeDefinitions = new ArrayList<>();

    /**
     * 构建结果类
     */
    public static class ContainerBuildResult {
        private final CsdlEntityContainer container;
        private final CsdlSchema schema;
        private final String namespace;
        
        public ContainerBuildResult(CsdlEntityContainer container, CsdlSchema schema, String namespace) {
            this.container = container;
            this.schema = schema;
            this.namespace = namespace;
        }
        
        public CsdlEntityContainer getContainer() { return container; }
        public CsdlSchema getSchema() { return schema; }
        public String getNamespace() { return namespace; }
    }
    
    /**
     * 设置容器基本信息
     */
    public DynamicContainerBuilder setContainerInfo(String containerName, String namespace, String alias) {
        this.containerName = containerName;
        this.namespace = namespace;
        this.alias = alias;
        return this;
    }
    
    /**
     * 添加EntitySet
     */
    public DynamicContainerBuilder addEntitySet(String name, String entityType) {
        CsdlEntitySet entitySet = new CsdlEntitySet();
        entitySet.setName(name);
        entitySet.setType(entityType);
        entitySets.add(entitySet);
        return this;
    }
    
    /**
     * 添加带导航属性绑定的EntitySet
     */
    public DynamicContainerBuilder addEntitySetWithNavigationBindings(String name, String entityType, 
                                                                      List<CsdlNavigationPropertyBinding> navigationBindings) {
        CsdlEntitySet entitySet = new CsdlEntitySet();
        entitySet.setName(name);
        entitySet.setType(entityType);
        if (navigationBindings != null && !navigationBindings.isEmpty()) {
            entitySet.setNavigationPropertyBindings(navigationBindings);
        }
        entitySets.add(entitySet);
        return this;
    }
    
    /**
     * 添加Singleton
     */
    public DynamicContainerBuilder addSingleton(String name, String entityType) {
        CsdlSingleton singleton = new CsdlSingleton();
        singleton.setName(name);
        singleton.setType(entityType);
        singletons.add(singleton);
        return this;
    }
    
    /**
     * 添加带导航属性绑定的Singleton
     */
    public DynamicContainerBuilder addSingletonWithNavigationBindings(String name, String entityType,
                                                                      List<CsdlNavigationPropertyBinding> navigationBindings) {
        CsdlSingleton singleton = new CsdlSingleton();
        singleton.setName(name);
        singleton.setType(entityType);
        if (navigationBindings != null && !navigationBindings.isEmpty()) {
            singleton.setNavigationPropertyBindings(navigationBindings);
        }
        singletons.add(singleton);
        return this;
    }
    
    /**
     * 添加ActionImport
     */
    public DynamicContainerBuilder addActionImport(String name, String action) {
        return addActionImport(name, action, null);
    }
    
    /**
     * 添加带EntitySet的ActionImport
     */
    public DynamicContainerBuilder addActionImport(String name, String action, String entitySet) {
        CsdlActionImport actionImport = new CsdlActionImport();
        actionImport.setName(name);
        actionImport.setAction(action);
        if (entitySet != null) {
            actionImport.setEntitySet(entitySet);
        }
        actionImports.add(actionImport);
        return this;
    }
    
    /**
     * 添加FunctionImport
     */
    public DynamicContainerBuilder addFunctionImport(String name, String function) {
        return addFunctionImport(name, function, null, false);
    }
    
    /**
     * 添加带详细配置的FunctionImport
     */
    public DynamicContainerBuilder addFunctionImport(String name, String function, String entitySet, boolean includeInServiceDocument) {
        CsdlFunctionImport functionImport = new CsdlFunctionImport();
        functionImport.setName(name);
        functionImport.setFunction(function);
        if (entitySet != null) {
            functionImport.setEntitySet(entitySet);
        }
        functionImport.setIncludeInServiceDocument(includeInServiceDocument);
        functionImports.add(functionImport);
        return this;
    }
    
    /**
     * 添加EntityType
     */
    public DynamicContainerBuilder addEntityType(String name, List<CsdlProperty> properties, List<CsdlPropertyRef> key) {
        return addEntityType(name, null, properties, key, null);
    }
    
    /**
     * 添加带完整配置的EntityType
     */
    public DynamicContainerBuilder addEntityType(String name, String baseType, List<CsdlProperty> properties, 
                                                 List<CsdlPropertyRef> key, List<CsdlNavigationProperty> navigationProperties) {
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName(name);
        if (baseType != null) {
            entityType.setBaseType(baseType);
        }
        if (properties != null) {
            entityType.setProperties(properties);
        }
        if (key != null) {
            entityType.setKey(key);
        }
        if (navigationProperties != null) {
            entityType.setNavigationProperties(navigationProperties);
        }
        entityTypes.add(entityType);
        return this;
    }
    
    /**
     * 添加ComplexType
     */
    public DynamicContainerBuilder addComplexType(String name, List<CsdlProperty> properties) {
        return addComplexType(name, null, properties, null);
    }
    
    /**
     * 添加带完整配置的ComplexType
     */
    public DynamicContainerBuilder addComplexType(String name, String baseType, List<CsdlProperty> properties,
                                                  List<CsdlNavigationProperty> navigationProperties) {
        CsdlComplexType complexType = new CsdlComplexType();
        complexType.setName(name);
        if (baseType != null) {
            complexType.setBaseType(baseType);
        }
        if (properties != null) {
            complexType.setProperties(properties);
        }
        if (navigationProperties != null) {
            complexType.setNavigationProperties(navigationProperties);
        }
        complexTypes.add(complexType);
        return this;
    }
    
    /**
     * 添加Action
     */
    public DynamicContainerBuilder addAction(String name, List<CsdlParameter> parameters, CsdlReturnType returnType, boolean isBound) {
        CsdlAction action = new CsdlAction();
        action.setName(name);
        if (parameters != null) {
            action.setParameters(parameters);
        }
        if (returnType != null) {
            action.setReturnType(returnType);
        }
        action.setBound(isBound);
        actions.add(action);
        return this;
    }
    
    /**
     * 添加Function
     */
    public DynamicContainerBuilder addFunction(String name, List<CsdlParameter> parameters, CsdlReturnType returnType, boolean isBound) {
        return addFunction(name, parameters, returnType, isBound, false);
    }
    
    /**
     * 添加带完整配置的Function
     */
    public DynamicContainerBuilder addFunction(String name, List<CsdlParameter> parameters, CsdlReturnType returnType, 
                                              boolean isBound, boolean isComposable) {
        CsdlFunction function = new CsdlFunction();
        function.setName(name);
        if (parameters != null) {
            function.setParameters(parameters);
        }
        if (returnType != null) {
            function.setReturnType(returnType);
        }
        function.setBound(isBound);
        function.setComposable(isComposable);
        functions.add(function);
        return this;
    }
    
    /**
     * 添加EnumType
     */
    public DynamicContainerBuilder addEnumType(String name, List<CsdlEnumMember> members, String underlyingType) {
        CsdlEnumType enumType = new CsdlEnumType();
        enumType.setName(name);
        if (members != null) {
            enumType.setMembers(members);
        }
        if (underlyingType != null) {
            enumType.setUnderlyingType(underlyingType);
        }
        enumTypes.add(enumType);
        return this;
    }
    
    /**
     * 添加TypeDefinition
     */
    public DynamicContainerBuilder addTypeDefinition(String name, String underlyingType) {
        CsdlTypeDefinition typeDefinition = new CsdlTypeDefinition();
        typeDefinition.setName(name);
        typeDefinition.setUnderlyingType(underlyingType);
        typeDefinitions.add(typeDefinition);
        return this;
    }
    
    /**
     * 创建简单的属性
     */
    public static CsdlProperty createProperty(String name, String type, boolean nullable) {
        CsdlProperty property = new CsdlProperty();
        property.setName(name);
        property.setType(type);
        property.setNullable(nullable);
        return property;
    }
    
    /**
     * 创建属性引用（用于Key）
     */
    public static CsdlPropertyRef createPropertyRef(String name) {
        CsdlPropertyRef propertyRef = new CsdlPropertyRef();
        propertyRef.setName(name);
        return propertyRef;
    }
    
    /**
     * 创建导航属性
     */
    public static CsdlNavigationProperty createNavigationProperty(String name, String type, boolean nullable) {
        CsdlNavigationProperty navigationProperty = new CsdlNavigationProperty();
        navigationProperty.setName(name);
        navigationProperty.setType(type);
        navigationProperty.setNullable(nullable);
        return navigationProperty;
    }
    
    /**
     * 创建导航属性绑定
     */
    public static CsdlNavigationPropertyBinding createNavigationPropertyBinding(String path, String target) {
        CsdlNavigationPropertyBinding binding = new CsdlNavigationPropertyBinding();
        binding.setPath(path);
        binding.setTarget(target);
        return binding;
    }
    
    /**
     * 创建参数
     */
    public static CsdlParameter createParameter(String name, String type, boolean nullable) {
        CsdlParameter parameter = new CsdlParameter();
        parameter.setName(name);
        parameter.setType(type);
        parameter.setNullable(nullable);
        return parameter;
    }
    
    /**
     * 创建返回类型
     */
    public static CsdlReturnType createReturnType(String type, boolean nullable) {
        CsdlReturnType returnType = new CsdlReturnType();
        returnType.setType(type);
        returnType.setNullable(nullable);
        return returnType;
    }
    
    /**
     * 创建枚举成员
     */
    public static CsdlEnumMember createEnumMember(String name, String value) {
        CsdlEnumMember member = new CsdlEnumMember();
        member.setName(name);
        member.setValue(value);
        return member;
    }
    
    /**
     * 添加实体类型
     */
    public DynamicContainerBuilder addEntityType(String name, EntityTypeBuilder builder) {
        EntityTypeBuilderContextImpl context = new EntityTypeBuilderContextImpl();
        builder.build(context);
        
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName(name);
        entityType.setProperties(context.getProperties());
        entityType.setKey(context.getKeys());
        
        entityTypes.add(entityType);
        return this;
    }
    
    /**
     * 实体类型构建器上下文实现
     */
    private static class EntityTypeBuilderContextImpl implements EntityTypeBuilder.EntityTypeBuilderContext {
        private final List<CsdlProperty> properties = new ArrayList<>();
        private final List<CsdlPropertyRef> keys = new ArrayList<>();
        
        @Override
        public void addProperty(String name, String type, Boolean nullable, Integer maxLength) {
            CsdlProperty property = new CsdlProperty();
            property.setName(name);
            property.setType(type);
            if (nullable != null) {
                property.setNullable(nullable);
            }
            if (maxLength != null) {
                property.setMaxLength(maxLength);
            }
            properties.add(property);
        }
        
        @Override
        public void setKey(String... keyNames) {
            keys.clear();
            for (String keyName : keyNames) {
                keys.add(new CsdlPropertyRef().setName(keyName));
            }
        }
        
        @Override
        public List<CsdlProperty> getProperties() {
            return properties;
        }
        
        @Override
        public List<CsdlPropertyRef> getKeys() {
            return keys;
        }
    }

    /**
     * 构建Schema和Container
     */
    public ContainerBuildResult build() {
        return build(namespace != null ? namespace : "DefaultNamespace", 
                    containerName != null ? containerName : "DefaultContainer");
    }
    
    /**
     * 构建Schema和Container（重载方法）
     */
    public ContainerBuildResult build(String namespace, String containerName) {
        if (namespace == null || namespace.trim().isEmpty()) {
            throw new IllegalArgumentException("Namespace cannot be null or empty");
        }
        if (containerName == null || containerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Container name cannot be null or empty");
        }
        
        // 创建容器
        CsdlEntityContainer container = new CsdlEntityContainer();
        container.setName(containerName);
        container.setEntitySets(entitySets);
        container.setSingletons(singletons);
        container.setActionImports(actionImports);
        container.setFunctionImports(functionImports);
        
        // 创建Schema
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace(namespace);
        if (alias != null) {
            schema.setAlias(alias);
        }
        schema.setEntityContainer(container);
        
        // 添加类型定义
        if (!entityTypes.isEmpty()) {
            schema.setEntityTypes(entityTypes);
        }
        if (!complexTypes.isEmpty()) {
            schema.setComplexTypes(complexTypes);
        }
        if (!actions.isEmpty()) {
            schema.setActions(actions);
        }
        if (!functions.isEmpty()) {
            schema.setFunctions(functions);
        }
        if (!enumTypes.isEmpty()) {
            schema.setEnumTypes(enumTypes);
        }
        if (!typeDefinitions.isEmpty()) {
            schema.setTypeDefinitions(typeDefinitions);
        }
        
        return new ContainerBuildResult(container, schema, schema.getNamespace());
    }
    
    /**
     * 清除所有构建的内容
     */
    public DynamicContainerBuilder clear() {
        containerName = null;
        namespace = null;
        alias = null;
        
        entitySets.clear();
        singletons.clear();
        actionImports.clear();
        functionImports.clear();
        
        entityTypes.clear();
        complexTypes.clear();
        actions.clear();
        functions.clear();
        enumTypes.clear();
        typeDefinitions.clear();
        
        return this;
    }
    
    /**
     * 获取构建状态信息
     */
    public String getBuildStatus() {
        return String.format("Container: %s, Namespace: %s, EntitySets: %d, Singletons: %d, " +
                           "ActionImports: %d, FunctionImports: %d, EntityTypes: %d, ComplexTypes: %d, " +
                           "Actions: %d, Functions: %d, EnumTypes: %d, TypeDefinitions: %d",
                           containerName, namespace, entitySets.size(), singletons.size(),
                           actionImports.size(), functionImports.size(), entityTypes.size(), complexTypes.size(),
                           actions.size(), functions.size(), enumTypes.size(), typeDefinitions.size());
    }
}
