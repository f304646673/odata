package org.apache.olingo.compliance.engine.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OData Schema注册表，保存和管理Schema信息，支持类型查询和依赖检查
 * 替代原来的ComplianceKnowledgeBase，提供更清晰的Schema管理能力
 */
public class SchemaRegistry {
    
    // 命名空间到Schema信息的映射
    private final Map<String, SchemaDefinition> namespaceToSchema = new ConcurrentHashMap<>();
    
    // 类型全名到定义的映射 (namespace.typename -> TypeDefinition)
    private final Map<String, TypeDefinition> typeDefinitions = new ConcurrentHashMap<>();
    
    // 别名到命名空间的映射
    private final Map<String, String> aliasToNamespace = new ConcurrentHashMap<>();
    
    // 文件路径到Schema的映射
    private final Map<String, Set<SchemaDefinition>> fileToSchemas = new ConcurrentHashMap<>();
    
    /**
     * 注册一个Schema定义
     */
    public void registerSchema(SchemaDefinition schema) {
        namespaceToSchema.put(schema.getNamespace(), schema);
        
        // 注册别名映射
        if (schema.getAlias() != null && !schema.getAlias().isEmpty()) {
            aliasToNamespace.put(schema.getAlias(), schema.getNamespace());
        }
        
        // 注册类型定义
        for (TypeDefinition type : schema.getTypes()) {
            String fullTypeName = schema.getNamespace() + "." + type.getName();
            typeDefinitions.put(fullTypeName, type);
        }
        
        // 注册文件映射
        fileToSchemas.computeIfAbsent(schema.getFilePath(), k -> ConcurrentHashMap.newKeySet())
                    .add(schema);
    }
    
    /**
     * 检查类型是否存在
     * @param typeName 类型名，可以是 "namespace.TypeName" 或 "alias.TypeName"
     */
    public boolean isTypeExists(String typeName) {
        if (typeName == null || typeName.isEmpty()) {
            return false;
        }
        
        // 直接检查完整类型名
        if (typeDefinitions.containsKey(typeName)) {
            return true;
        }
        
        // 检查是否使用了别名
        int dotIndex = typeName.indexOf('.');
        if (dotIndex > 0) {
            String prefix = typeName.substring(0, dotIndex);
            String localName = typeName.substring(dotIndex + 1);
            
            // 检查prefix是否是别名
            String namespace = aliasToNamespace.get(prefix);
            if (namespace != null) {
                String fullTypeName = namespace + "." + localName;
                return typeDefinitions.containsKey(fullTypeName);
            }
        }
        
        return false;
    }
    
    /**
     * 获取类型定义
     */
    public TypeDefinition getTypeDefinition(String typeName) {
        if (typeName == null || typeName.isEmpty()) {
            return null;
        }
        
        // 直接查找
        TypeDefinition type = typeDefinitions.get(typeName);
        if (type != null) {
            return type;
        }
        
        // 通过别名查找
        int dotIndex = typeName.indexOf('.');
        if (dotIndex > 0) {
            String prefix = typeName.substring(0, dotIndex);
            String localName = typeName.substring(dotIndex + 1);
            
            String namespace = aliasToNamespace.get(prefix);
            if (namespace != null) {
                String fullTypeName = namespace + "." + localName;
                return typeDefinitions.get(fullTypeName);
            }
        }
        
        return null;
    }
    
    /**
     * 检查基类型是否有效
     * @param typeName 类型名
     * @param baseTypeName 基类型名
     */
    public boolean isValidBaseType(String typeName, String baseTypeName) {
        TypeDefinition type = getTypeDefinition(typeName);
        TypeDefinition baseType = getTypeDefinition(baseTypeName);
        
        if (type == null || baseType == null) {
            return false;
        }
        
        // ComplexType不能继承EntityType
        if ("ComplexType".equals(type.getKind()) && "EntityType".equals(baseType.getKind())) {
            return false;
        }
        
        // EntityType不能继承ComplexType  
        if ("EntityType".equals(type.getKind()) && "ComplexType".equals(baseType.getKind())) {
            return false;
        }
        
        // 同类型之间可以继承
        return type.getKind().equals(baseType.getKind());
    }
    
    /**
     * 获取所有命名空间
     */
    public Set<String> getAllNamespaces() {
        return new HashSet<>(namespaceToSchema.keySet());
    }
    
    /**
     * 获取指定命名空间的Schema
     */
    public SchemaDefinition getSchema(String namespace) {
        return namespaceToSchema.get(namespace);
    }
    
    /**
     * 合并另一个Registry的内容
     */
    public void merge(SchemaRegistry other) {
        if (other == null) return;
        
        for (SchemaDefinition schema : other.namespaceToSchema.values()) {
            registerSchema(schema);
        }
    }
    
    /**
     * 清空所有内容
     */
    public void clear() {
        namespaceToSchema.clear();
        typeDefinitions.clear();
        aliasToNamespace.clear();
        fileToSchemas.clear();
    }
    
    /**
     * 获取统计信息
     */
    public RegistryStatistics getStatistics() {
        int totalTypes = typeDefinitions.size();
        int entityTypes = (int) typeDefinitions.values().stream()
                .filter(t -> "EntityType".equals(t.getKind())).count();
        int complexTypes = (int) typeDefinitions.values().stream()
                .filter(t -> "ComplexType".equals(t.getKind())).count();
        
        return new RegistryStatistics(
            namespaceToSchema.size(),
            totalTypes,
            entityTypes,
            complexTypes,
            fileToSchemas.size()
        );
    }
    
    /**
     * Schema定义
     */
    public static class SchemaDefinition {
        private final String namespace;
        private final String alias;
        private final String filePath;
        private final List<TypeDefinition> types;
        
        public SchemaDefinition(String namespace, String alias, String filePath, List<TypeDefinition> types) {
            this.namespace = namespace;
            this.alias = alias;
            this.filePath = filePath;
            this.types = types != null ? new ArrayList<>(types) : new ArrayList<>();
        }
        
        public String getNamespace() { return namespace; }
        public String getAlias() { return alias; }
        public String getFilePath() { return filePath; }
        public List<TypeDefinition> getTypes() { return new ArrayList<>(types); }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SchemaDefinition that = (SchemaDefinition) o;
            return Objects.equals(namespace, that.namespace) &&
                   Objects.equals(filePath, that.filePath);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(namespace, filePath);
        }
    }
    
    /**
     * 类型定义
     */
    public static class TypeDefinition {
        private final String name;
        private final String kind; // EntityType, ComplexType, EnumType, etc.
        private final String baseType; // 基类型，可为null
        
        public TypeDefinition(String name, String kind, String baseType) {
            this.name = name;
            this.kind = kind;
            this.baseType = baseType;
        }
        
        public String getName() { return name; }
        public String getKind() { return kind; }
        public String getBaseType() { return baseType; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TypeDefinition that = (TypeDefinition) o;
            return Objects.equals(name, that.name) && Objects.equals(kind, that.kind);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(name, kind);
        }
    }
    
    /**
     * Registry统计信息
     */
    public static class RegistryStatistics {
        private final int namespaceCount;
        private final int totalTypes;
        private final int entityTypes;
        private final int complexTypes;
        private final int fileCount;
        
        public RegistryStatistics(int namespaceCount, int totalTypes, int entityTypes, 
                                int complexTypes, int fileCount) {
            this.namespaceCount = namespaceCount;
            this.totalTypes = totalTypes;
            this.entityTypes = entityTypes;
            this.complexTypes = complexTypes;
            this.fileCount = fileCount;
        }
        
        public int getNamespaceCount() { return namespaceCount; }
        public int getTotalTypes() { return totalTypes; }
        public int getEntityTypes() { return entityTypes; }
        public int getComplexTypes() { return complexTypes; }
        public int getFileCount() { return fileCount; }
        
        @Override
        public String toString() {
            return String.format("RegistryStatistics{namespaces=%d, totalTypes=%d, entityTypes=%d, complexTypes=%d, files=%d}",
                    namespaceCount, totalTypes, entityTypes, complexTypes, fileCount);
        }
    }
}
