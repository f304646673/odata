package org.apache.olingo.compliance.engine.core.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.compliance.engine.core.SchemaRegistry;

/**
 * Default implementation of SchemaRegistry that provides OData schema management.
 */
public class DefaultSchemaRegistryImpl implements SchemaRegistry {

    public boolean isSchemasValid(List<SchemaDefinition> schemas) {
        return true;
    }

    // 命名空间到Schema信息的映射
    private final Map<String, SchemaDefinitionImpl> namespaceToSchema = new ConcurrentHashMap<>();
    
    // 类型全名到定义的映射 (namespace.typename -> TypeDefinition)
    private final Map<String, TypeDefinitionImpl> typeDefinitions = new ConcurrentHashMap<>();
    
    // 别名到命名空间的映射
    private final Map<String, String> aliasToNamespace = new ConcurrentHashMap<>();
    
    // 文件路径到Schema的映射
    private final Map<String, Set<SchemaDefinitionImpl>> fileToSchemas = new ConcurrentHashMap<>();
    
    @Override
    public void registerSchema(SchemaDefinition schema) {
        if (!(schema instanceof SchemaDefinitionImpl)) {
            // Convert to our implementation
            schema = new SchemaDefinitionImpl(schema.getNamespace(), schema.getAlias(), 
                    schema.getFilePath(), schema.getTypes());
        }
        
        SchemaDefinitionImpl impl = (SchemaDefinitionImpl) schema;
        namespaceToSchema.put(impl.getNamespace(), impl);
        
        // 注册别名映射
        if (impl.getAlias() != null && !impl.getAlias().isEmpty()) {
            aliasToNamespace.put(impl.getAlias(), impl.getNamespace());
        }
        
        // 注册类型定义
        for (TypeDefinition type : impl.getTypes()) {
            String fullTypeName = impl.getNamespace() + "." + type.getName();
            TypeDefinitionImpl typeImpl = (type instanceof TypeDefinitionImpl) ?
                    (TypeDefinitionImpl) type :
                    new TypeDefinitionImpl(type.getName(), type.getKind(), type.getBaseType());
            typeDefinitions.put(fullTypeName, typeImpl);
        }
        
        // 注册文件映射
        fileToSchemas.computeIfAbsent(impl.getFilePath(), k -> ConcurrentHashMap.newKeySet())
                    .add(impl);
    }
    
    @Override
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
    
    @Override
    public boolean isNamespaceDefined(String namespace) {
        return namespaceToSchema.containsKey(namespace);
    }
    
    @Override
    public boolean isTypeDefined(String namespace, String typeName) {
        String fullTypeName = namespace + "." + typeName;
        return typeDefinitions.containsKey(fullTypeName);
    }
    
    @Override
    public boolean isFileExists(String fileName) {
        return fileToSchemas.containsKey(fileName);
    }
    
    @Override
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
    
    @Override
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
    
    @Override
    public Set<String> getAllNamespaces() {
        return new HashSet<>(namespaceToSchema.keySet());
    }
    
    @Override
    public Set<String> getTypesInNamespace(String namespace) {
        Set<String> types = new HashSet<>();
        SchemaDefinitionImpl schema = namespaceToSchema.get(namespace);
        if (schema != null) {
            for (TypeDefinition type : schema.getTypes()) {
                types.add(type.getName());
            }
        }
        return types;
    }
    
    @Override
    public Set<String> getAllFileNames() {
        return new HashSet<>(fileToSchemas.keySet());
    }
    
    @Override
    public SchemaDefinition getSchema(String namespace) {
        return namespaceToSchema.get(namespace);
    }
    
    @Override
    public Set<SchemaDefinition> getSchemasForFile(String filePath) {
        Set<SchemaDefinitionImpl> implSet = fileToSchemas.getOrDefault(filePath, Collections.emptySet());
        return new HashSet<>(implSet);
    }
    
    @Override
    public SchemaDefinition getSchemaByNamespace(String namespace) {
        return namespaceToSchema.get(namespace);
    }
    
    @Override
    public void merge(SchemaRegistry other) {
        if (other == null) return;
        
        if (other instanceof DefaultSchemaRegistryImpl) {
            DefaultSchemaRegistryImpl defaultOther = (DefaultSchemaRegistryImpl) other;
            for (SchemaDefinitionImpl schema : defaultOther.namespaceToSchema.values()) {
                registerSchema(schema);
            }
        }
    }
    
    @Override
    public RegistryStatistics getStatistics() {
        int namespaceCount = namespaceToSchema.size();
        int totalTypes = typeDefinitions.size();
        
        int entityTypes = 0;
        int complexTypes = 0;
        
        for (TypeDefinitionImpl type : typeDefinitions.values()) {
            if ("EntityType".equals(type.getKind())) {
                entityTypes++;
            } else if ("ComplexType".equals(type.getKind())) {
                complexTypes++;
            }
        }
        
        int fileCount = fileToSchemas.size();
        
        return new RegistryStatisticsImpl(namespaceCount, totalTypes, entityTypes, complexTypes, fileCount);
    }
    
    @Override
    public void addSchemas(List<CsdlSchema> schemas) {
        if (schemas == null) return;
        
        for (CsdlSchema csdlSchema : schemas) {
            // Convert CsdlSchema to SchemaDefinition
            String namespace = csdlSchema.getNamespace();
            String alias = csdlSchema.getAlias();
            String filePath = "unknown"; // CsdlSchema doesn't have file path info
            
            List<TypeDefinition> types = new ArrayList<>();
            
            // Add EntityTypes
            if (csdlSchema.getEntityTypes() != null) {
                csdlSchema.getEntityTypes().forEach(et -> 
                    types.add(new TypeDefinitionImpl(et.getName(), "EntityType", et.getBaseType())));
            }
            
            // Add ComplexTypes
            if (csdlSchema.getComplexTypes() != null) {
                csdlSchema.getComplexTypes().forEach(ct -> 
                    types.add(new TypeDefinitionImpl(ct.getName(), "ComplexType", ct.getBaseType())));
            }
            
            // Add EnumTypes
            if (csdlSchema.getEnumTypes() != null) {
                csdlSchema.getEnumTypes().forEach(et -> 
                    types.add(new TypeDefinitionImpl(et.getName(), "EnumType", null)));
            }
            
            SchemaDefinition schemaDefinition = new SchemaDefinitionImpl(namespace, alias, filePath, types);
            registerSchema(schemaDefinition);
        }
    }
    
    @Override
    public boolean hasSchemaForFile(String filePath) {
        return fileToSchemas.containsKey(filePath) && !fileToSchemas.get(filePath).isEmpty();
    }
    
    @Override
    public boolean hasNamespace(String namespace) {
        return namespaceToSchema.containsKey(namespace);
    }
    
    @Override
    public void addSchema(SchemaDefinition schema) {
        registerSchema(schema);
    }
    
    @Override
    public void reset() {
        namespaceToSchema.clear();
        typeDefinitions.clear();
        aliasToNamespace.clear();
        fileToSchemas.clear();
    }
    
    // Implementation classes
    
    public static class SchemaDefinitionImpl implements SchemaDefinition {
        private final String namespace;
        private final String alias;
        private final String filePath;
        private final List<TypeDefinition> types;
        
        public SchemaDefinitionImpl(String namespace, String alias, String filePath, List<TypeDefinition> types) {
            this.namespace = namespace;
            this.alias = alias;
            this.filePath = filePath;
            this.types = types != null ? new ArrayList<>(types) : new ArrayList<>();
        }
        
        @Override
        public String getNamespace() { return namespace; }
        @Override
        public String getAlias() { return alias; }
        @Override
        public String getFilePath() { return filePath; }
        @Override
        public List<TypeDefinition> getTypes() { return new ArrayList<>(types); }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SchemaDefinitionImpl that = (SchemaDefinitionImpl) o;
            return Objects.equals(namespace, that.namespace) &&
                   Objects.equals(alias, that.alias) &&
                   Objects.equals(filePath, that.filePath);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(namespace, alias, filePath);
        }
        
        @Override
        public String toString() {
            return String.format("SchemaDefinition{namespace='%s', alias='%s', filePath='%s', types=%d}", 
                               namespace, alias, filePath, types.size());
        }
    }
    
    public static class TypeDefinitionImpl implements TypeDefinition {
        private final String name;
        private final String kind;
        private final String baseType;
        
        public TypeDefinitionImpl(String name, String kind, String baseType) {
            this.name = name;
            this.kind = kind;
            this.baseType = baseType;
        }
        
        @Override
        public String getName() { return name; }
        @Override
        public String getKind() { return kind; }
        @Override
        public String getBaseType() { return baseType; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TypeDefinitionImpl that = (TypeDefinitionImpl) o;
            return Objects.equals(name, that.name) &&
                   Objects.equals(kind, that.kind) &&
                   Objects.equals(baseType, that.baseType);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(name, kind, baseType);
        }
        
        @Override
        public String toString() {
            return String.format("TypeDefinition{name='%s', kind='%s', baseType='%s'}", name, kind, baseType);
        }
    }
    
    public static class RegistryStatisticsImpl implements RegistryStatistics {
        private final int namespaceCount;
        private final int totalTypes;
        private final int entityTypes;
        private final int complexTypes;
        private final int fileCount;
        
        public RegistryStatisticsImpl(int namespaceCount, int totalTypes, int entityTypes, 
                                     int complexTypes, int fileCount) {
            this.namespaceCount = namespaceCount;
            this.totalTypes = totalTypes;
            this.entityTypes = entityTypes;
            this.complexTypes = complexTypes;
            this.fileCount = fileCount;
        }
        
        @Override
        public int getNamespaceCount() { return namespaceCount; }
        @Override
        public int getTotalTypes() { return totalTypes; }
        @Override
        public int getEntityTypes() { return entityTypes; }
        @Override
        public int getComplexTypes() { return complexTypes; }
        @Override
        public int getFileCount() { return fileCount; }
        
        @Override
        public String toString() {
            return String.format("RegistryStatistics{namespaces=%d, totalTypes=%d, entityTypes=%d, complexTypes=%d, files=%d}",
                    namespaceCount, totalTypes, entityTypes, complexTypes, fileCount);
        }
    }
}
