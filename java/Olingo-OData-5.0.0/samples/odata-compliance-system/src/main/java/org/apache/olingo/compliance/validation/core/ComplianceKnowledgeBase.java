package org.apache.olingo.compliance.validation.core;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 只读的合规性辅助判断结构体，用于在验证过程中提供已知的类型和引用信息。
 * 这个结构体包含了已经加载到系统中的XML模式的元数据信息，
 * 可以被新的验证过程引用以进行跨模式的合规性检查。
 */
public class ComplianceKnowledgeBase {
    
    // 类型定义相关
    private final Map<String, TypeDefinition> definedTypes;
    private final Map<String, String> typeHierarchy; // childType -> parentType
    private final Map<String, Set<String>> namespaceDependencies; // namespace -> dependent namespaces
    
    // 实体和复杂类型的分类
    private final Set<String> entityTypes;
    private final Set<String> complexTypes;
    private final Set<String> primitiveTypes;
    private final Set<String> enumTypes;
    private final Set<String> typeDefinitions;
    
    // 服务和容器信息
    private final Map<String, ServiceDefinition> serviceDefinitions;
    private final Map<String, Set<String>> containerEntitySets; // container -> entity set names
    
    // 命名空间和引用
    private final Set<String> registeredNamespaces;
    private final Map<String, String> namespaceAliases; // alias -> namespace
    
    /**
     * 私有构造函数，只能通过Builder创建
     */
    private ComplianceKnowledgeBase(Builder builder) {
        this.definedTypes = Collections.unmodifiableMap(new ConcurrentHashMap<>(builder.definedTypes));
        this.typeHierarchy = Collections.unmodifiableMap(new ConcurrentHashMap<>(builder.typeHierarchy));
        this.namespaceDependencies = Collections.unmodifiableMap(new ConcurrentHashMap<>(builder.namespaceDependencies));
        
        this.entityTypes = Collections.unmodifiableSet(builder.entityTypes);
        this.complexTypes = Collections.unmodifiableSet(builder.complexTypes);
        this.primitiveTypes = Collections.unmodifiableSet(builder.primitiveTypes);
        this.enumTypes = Collections.unmodifiableSet(builder.enumTypes);
        this.typeDefinitions = Collections.unmodifiableSet(builder.typeDefinitions);
        
        this.serviceDefinitions = Collections.unmodifiableMap(new ConcurrentHashMap<>(builder.serviceDefinitions));
        this.containerEntitySets = Collections.unmodifiableMap(new ConcurrentHashMap<>(builder.containerEntitySets));
        
        this.registeredNamespaces = Collections.unmodifiableSet(builder.registeredNamespaces);
        this.namespaceAliases = Collections.unmodifiableMap(new ConcurrentHashMap<>(builder.namespaceAliases));
    }
    
    /**
     * 类型定义结构
     */
    public static class TypeDefinition {
        private final String fullName;
        private final String namespace;
        private final String localName;
        private final TypeKind kind;
        private final String baseType;
        private final Map<String, Object> properties;
        
        public TypeDefinition(String fullName, String namespace, String localName, 
                            TypeKind kind, String baseType, Map<String, Object> properties) {
            this.fullName = fullName;
            this.namespace = namespace;
            this.localName = localName;
            this.kind = kind;
            this.baseType = baseType;
            this.properties = properties != null ? 
                Collections.unmodifiableMap(new ConcurrentHashMap<>(properties)) : 
                Collections.emptyMap();
        }
        
        // Getters
        public String getFullName() { return fullName; }
        public String getNamespace() { return namespace; }
        public String getLocalName() { return localName; }
        public TypeKind getKind() { return kind; }
        public String getBaseType() { return baseType; }
        public Map<String, Object> getProperties() { return properties; }
        
        public boolean hasBaseType() { return baseType != null && !baseType.isEmpty(); }
    }
    
    /**
     * 类型种类枚举
     */
    public enum TypeKind {
        ENTITY_TYPE,
        COMPLEX_TYPE,
        PRIMITIVE_TYPE,
        ENUM_TYPE,
        TYPE_DEFINITION,
        ACTION,
        FUNCTION,
        UNKNOWN
    }
    
    /**
     * 服务定义结构
     */
    public static class ServiceDefinition {
        private final String namespace;
        private final String containerName;
        private final Set<String> entitySets;
        private final Set<String> singletons;
        private final Set<String> actionImports;
        private final Set<String> functionImports;
        
        public ServiceDefinition(String namespace, String containerName,
                               Set<String> entitySets, Set<String> singletons,
                               Set<String> actionImports, Set<String> functionImports) {
            this.namespace = namespace;
            this.containerName = containerName;
            this.entitySets = entitySets != null ? Collections.unmodifiableSet(entitySets) : Collections.emptySet();
            this.singletons = singletons != null ? Collections.unmodifiableSet(singletons) : Collections.emptySet();
            this.actionImports = actionImports != null ? Collections.unmodifiableSet(actionImports) : Collections.emptySet();
            this.functionImports = functionImports != null ? Collections.unmodifiableSet(functionImports) : Collections.emptySet();
        }
        
        // Getters
        public String getNamespace() { return namespace; }
        public String getContainerName() { return containerName; }
        public Set<String> getEntitySets() { return entitySets; }
        public Set<String> getSingletons() { return singletons; }
        public Set<String> getActionImports() { return actionImports; }
        public Set<String> getFunctionImports() { return functionImports; }
    }
    
    // 查询方法
    
    /**
     * 检查类型是否存在
     */
    public boolean isTypeDefined(String fullTypeName) {
        return definedTypes.containsKey(fullTypeName);
    }
    
    /**
     * 获取类型定义
     */
    public TypeDefinition getTypeDefinition(String fullTypeName) {
        return definedTypes.get(fullTypeName);
    }
    
    /**
     * 检查类型是否为实体类型
     */
    public boolean isEntityType(String fullTypeName) {
        return entityTypes.contains(fullTypeName);
    }
    
    /**
     * 检查类型是否为复杂类型
     */
    public boolean isComplexType(String fullTypeName) {
        return complexTypes.contains(fullTypeName);
    }
    
    /**
     * 检查类型是否为基本类型
     */
    public boolean isPrimitiveType(String fullTypeName) {
        return primitiveTypes.contains(fullTypeName);
    }
    
    /**
     * 检查继承关系是否有效
     * @param childType 子类型的全名
     * @param parentType 父类型的全名
     * @return 如果继承关系有效返回true
     */
    public boolean isValidInheritance(String childType, String parentType) {
        TypeDefinition childDef = getTypeDefinition(childType);
        TypeDefinition parentDef = getTypeDefinition(parentType);
        
        if (childDef == null || parentDef == null) {
            return false;
        }
        
        // 复杂类型只能继承自复杂类型
        if (childDef.getKind() == TypeKind.COMPLEX_TYPE && 
            parentDef.getKind() != TypeKind.COMPLEX_TYPE) {
            return false;
        }
        
        // 实体类型只能继承自实体类型
        if (childDef.getKind() == TypeKind.ENTITY_TYPE && 
            parentDef.getKind() != TypeKind.ENTITY_TYPE) {
            return false;
        }
        
        // 检查循环继承
        return !hasCircularInheritance(childType, parentType);
    }
    
    /**
     * 检查是否存在循环继承
     */
    public boolean hasCircularInheritance(String childType, String parentType) {
        Set<String> visited = ConcurrentHashMap.newKeySet();
        return hasCircularInheritanceRecursive(childType, parentType, visited);
    }
    
    private boolean hasCircularInheritanceRecursive(String childType, String targetType, Set<String> visited) {
        if (childType.equals(targetType)) {
            return true;
        }
        
        if (visited.contains(childType)) {
            return true; // 循环检测到
        }
        
        visited.add(childType);
        String parentType = typeHierarchy.get(childType);
        
        if (parentType != null) {
            return hasCircularInheritanceRecursive(parentType, targetType, visited);
        }
        
        return false;
    }
    
    /**
     * 检查命名空间是否已注册
     */
    public boolean isNamespaceRegistered(String namespace) {
        return registeredNamespaces.contains(namespace);
    }
    
    /**
     * 解析命名空间别名
     */
    public String resolveNamespaceAlias(String aliasOrNamespace) {
        return namespaceAliases.getOrDefault(aliasOrNamespace, aliasOrNamespace);
    }
    
    /**
     * 获取命名空间依赖
     */
    public Set<String> getNamespaceDependencies(String namespace) {
        return namespaceDependencies.getOrDefault(namespace, Collections.emptySet());
    }
    
    /**
     * 获取服务定义
     */
    public ServiceDefinition getServiceDefinition(String namespace) {
        return serviceDefinitions.get(namespace);
    }
    
    // 只读访问器
    public Set<String> getAllDefinedTypes() { return definedTypes.keySet(); }
    public Set<String> getAllEntityTypes() { return entityTypes; }
    public Set<String> getAllComplexTypes() { return complexTypes; }
    public Set<String> getAllRegisteredNamespaces() { return registeredNamespaces; }
    
    /**
     * Builder类用于构建ComplianceKnowledgeBase
     */
    public static class Builder {
        private final Map<String, TypeDefinition> definedTypes = new ConcurrentHashMap<>();
        private final Map<String, String> typeHierarchy = new ConcurrentHashMap<>();
        private final Map<String, Set<String>> namespaceDependencies = new ConcurrentHashMap<>();
        
        private final Set<String> entityTypes = ConcurrentHashMap.newKeySet();
        private final Set<String> complexTypes = ConcurrentHashMap.newKeySet();
        private final Set<String> primitiveTypes = ConcurrentHashMap.newKeySet();
        private final Set<String> enumTypes = ConcurrentHashMap.newKeySet();
        private final Set<String> typeDefinitions = ConcurrentHashMap.newKeySet();
        
        private final Map<String, ServiceDefinition> serviceDefinitions = new ConcurrentHashMap<>();
        private final Map<String, Set<String>> containerEntitySets = new ConcurrentHashMap<>();
        
        private final Set<String> registeredNamespaces = ConcurrentHashMap.newKeySet();
        private final Map<String, String> namespaceAliases = new ConcurrentHashMap<>();
        
        public Builder addTypeDefinition(TypeDefinition typeDefinition) {
            definedTypes.put(typeDefinition.getFullName(), typeDefinition);
            
            // 根据类型种类添加到相应的集合
            switch (typeDefinition.getKind()) {
                case ENTITY_TYPE:
                    entityTypes.add(typeDefinition.getFullName());
                    break;
                case COMPLEX_TYPE:
                    complexTypes.add(typeDefinition.getFullName());
                    break;
                case PRIMITIVE_TYPE:
                    primitiveTypes.add(typeDefinition.getFullName());
                    break;
                case ENUM_TYPE:
                    enumTypes.add(typeDefinition.getFullName());
                    break;
                case TYPE_DEFINITION:
                    typeDefinitions.add(typeDefinition.getFullName());
                    break;
            }
            
            // 添加继承关系
            if (typeDefinition.hasBaseType()) {
                typeHierarchy.put(typeDefinition.getFullName(), typeDefinition.getBaseType());
            }
            
            // 注册命名空间
            registeredNamespaces.add(typeDefinition.getNamespace());
            
            return this;
        }
        
        public Builder addNamespaceDependency(String namespace, String dependentNamespace) {
            namespaceDependencies.computeIfAbsent(namespace, k -> ConcurrentHashMap.newKeySet())
                                .add(dependentNamespace);
            return this;
        }
        
        public Builder addServiceDefinition(ServiceDefinition serviceDefinition) {
            serviceDefinitions.put(serviceDefinition.getNamespace(), serviceDefinition);
            registeredNamespaces.add(serviceDefinition.getNamespace());
            
            // 添加容器实体集映射
            containerEntitySets.put(serviceDefinition.getContainerName(), serviceDefinition.getEntitySets());
            
            return this;
        }
        
        public Builder addNamespaceAlias(String alias, String namespace) {
            namespaceAliases.put(alias, namespace);
            return this;
        }
        
        public Builder addNamespace(String namespace) {
            registeredNamespaces.add(namespace);
            return this;
        }
        
        public ComplianceKnowledgeBase build() {
            return new ComplianceKnowledgeBase(this);
        }
    }
}
