package org.apache.olingo.schema.repository.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlSingleton;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;

/**
 * Schema Repository的上下文信息
 * 管理所有Schema及其元素的索引和依赖关系
 */
public class SchemaRepositoryContext {
    
    // Schema缓存 - 线程安全
    private final Map<String, CsdlSchema> schemas = new ConcurrentHashMap<>();
    
    // 依赖关系图
    private final Map<FullQualifiedName, SchemaDependencyNode> dependencyNodes = new ConcurrentHashMap<>();
    
    // 元素索引 - 按类型分组，提高查找性能
    private final Map<String, Map<FullQualifiedName, CsdlEntityType>> entityTypes = new ConcurrentHashMap<>();
    private final Map<String, Map<FullQualifiedName, CsdlComplexType>> complexTypes = new ConcurrentHashMap<>();
    private final Map<String, Map<FullQualifiedName, CsdlAction>> actions = new ConcurrentHashMap<>();
    private final Map<String, Map<FullQualifiedName, CsdlFunction>> functions = new ConcurrentHashMap<>();
    private final Map<String, Map<FullQualifiedName, CsdlTypeDefinition>> typeDefinitions = new ConcurrentHashMap<>();
    private final Map<String, Map<FullQualifiedName, CsdlTerm>> terms = new ConcurrentHashMap<>();
    private final Map<String, CsdlEntityContainer> entityContainers = new ConcurrentHashMap<>();
    
    // 别名映射
    private final Map<String, String> aliasToNamespaceMap = new ConcurrentHashMap<>();
    private final Map<String, String> namespaceToAliasMap = new ConcurrentHashMap<>();
    
    // 统计信息
    private volatile long lastUpdateTime = System.currentTimeMillis();
    private volatile int totalElements = 0;
    
    /**
     * 添加Schema到context中
     */
    public synchronized void addSchema(CsdlSchema schema) {
        if (schema == null || schema.getNamespace() == null) {
            throw new IllegalArgumentException("Schema and namespace cannot be null");
        }
        
        String namespace = schema.getNamespace();
        schemas.put(namespace, schema);
        
        // 更新别名映射
        if (schema.getAlias() != null) {
            aliasToNamespaceMap.put(schema.getAlias(), namespace);
            namespaceToAliasMap.put(namespace, schema.getAlias());
        }
        
        // 建立索引
        indexSchemaElements(schema);
        
        lastUpdateTime = System.currentTimeMillis();
    }
    
    /**
     * 移除Schema
     */
    public synchronized boolean removeSchema(String namespace) {
        CsdlSchema removedSchema = schemas.remove(namespace);
        if (removedSchema != null) {
            // 清理索引
            removeSchemaFromIndexes(removedSchema);
            
            // 清理别名映射
            if (removedSchema.getAlias() != null) {
                aliasToNamespaceMap.remove(removedSchema.getAlias());
                namespaceToAliasMap.remove(namespace);
            }
            
            lastUpdateTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }
    
    /**
     * 建立Schema元素索引
     */
    private void indexSchemaElements(CsdlSchema schema) {
        String namespace = schema.getNamespace();
        
        // 索引EntityTypes
        if (schema.getEntityTypes() != null) {
            Map<FullQualifiedName, CsdlEntityType> namespaceEntityTypes = 
                entityTypes.computeIfAbsent(namespace, k -> new ConcurrentHashMap<>());
            for (CsdlEntityType entityType : schema.getEntityTypes()) {
                FullQualifiedName fqn = new FullQualifiedName(namespace, entityType.getName());
                namespaceEntityTypes.put(fqn, entityType);
                totalElements++;
            }
        }
        
        // 索引ComplexTypes
        if (schema.getComplexTypes() != null) {
            Map<FullQualifiedName, CsdlComplexType> namespaceComplexTypes = 
                complexTypes.computeIfAbsent(namespace, k -> new ConcurrentHashMap<>());
            for (CsdlComplexType complexType : schema.getComplexTypes()) {
                FullQualifiedName fqn = new FullQualifiedName(namespace, complexType.getName());
                namespaceComplexTypes.put(fqn, complexType);
                totalElements++;
            }
        }
        
        // 索引Actions
        if (schema.getActions() != null) {
            Map<FullQualifiedName, CsdlAction> namespaceActions = 
                actions.computeIfAbsent(namespace, k -> new ConcurrentHashMap<>());
            for (CsdlAction action : schema.getActions()) {
                FullQualifiedName fqn = new FullQualifiedName(namespace, action.getName());
                namespaceActions.put(fqn, action);
                totalElements++;
            }
        }
        
        // 索引Functions
        if (schema.getFunctions() != null) {
            Map<FullQualifiedName, CsdlFunction> namespaceFunctions = 
                functions.computeIfAbsent(namespace, k -> new ConcurrentHashMap<>());
            for (CsdlFunction function : schema.getFunctions()) {
                FullQualifiedName fqn = new FullQualifiedName(namespace, function.getName());
                namespaceFunctions.put(fqn, function);
                totalElements++;
            }
        }
        
        // 索引TypeDefinitions
        if (schema.getTypeDefinitions() != null) {
            Map<FullQualifiedName, CsdlTypeDefinition> namespaceTypeDefinitions = 
                typeDefinitions.computeIfAbsent(namespace, k -> new ConcurrentHashMap<>());
            for (CsdlTypeDefinition typeDefinition : schema.getTypeDefinitions()) {
                FullQualifiedName fqn = new FullQualifiedName(namespace, typeDefinition.getName());
                namespaceTypeDefinitions.put(fqn, typeDefinition);
                totalElements++;
            }
        }
        
        // 索引Terms
        if (schema.getTerms() != null) {
            Map<FullQualifiedName, CsdlTerm> namespaceTerms = 
                terms.computeIfAbsent(namespace, k -> new ConcurrentHashMap<>());
            for (CsdlTerm term : schema.getTerms()) {
                FullQualifiedName fqn = new FullQualifiedName(namespace, term.getName());
                namespaceTerms.put(fqn, term);
                totalElements++;
            }
        }
        
        // 索引EntityContainer
        if (schema.getEntityContainer() != null) {
            entityContainers.put(namespace, schema.getEntityContainer());
            totalElements++;
        }
    }
    
    /**
     * 从索引中移除Schema元素
     */
    private void removeSchemaFromIndexes(CsdlSchema schema) {
        String namespace = schema.getNamespace();
        
        // 移除各类型索引
        entityTypes.remove(namespace);
        complexTypes.remove(namespace);
        actions.remove(namespace);
        functions.remove(namespace);
        typeDefinitions.remove(namespace);
        terms.remove(namespace);
        entityContainers.remove(namespace);
        
        // 移除依赖节点
        dependencyNodes.entrySet().removeIf(entry -> 
            entry.getKey().getNamespace().equals(namespace));
    }
    
    /**
     * 获取EntityType
     */
    public CsdlEntityType getEntityType(FullQualifiedName fqn) {
        if (fqn == null) return null;
        
        Map<FullQualifiedName, CsdlEntityType> namespaceMap = entityTypes.get(fqn.getNamespace());
        return namespaceMap != null ? namespaceMap.get(fqn) : null;
    }
    
    /**
     * 获取ComplexType
     */
    public CsdlComplexType getComplexType(FullQualifiedName fqn) {
        if (fqn == null) return null;
        
        Map<FullQualifiedName, CsdlComplexType> namespaceMap = complexTypes.get(fqn.getNamespace());
        return namespaceMap != null ? namespaceMap.get(fqn) : null;
    }
    
    /**
     * 获取Action
     */
    public CsdlAction getAction(FullQualifiedName fqn) {
        if (fqn == null) return null;
        
        Map<FullQualifiedName, CsdlAction> namespaceMap = actions.get(fqn.getNamespace());
        return namespaceMap != null ? namespaceMap.get(fqn) : null;
    }
    
    /**
     * 获取Function
     */
    public CsdlFunction getFunction(FullQualifiedName fqn) {
        if (fqn == null) return null;
        
        Map<FullQualifiedName, CsdlFunction> namespaceMap = functions.get(fqn.getNamespace());
        return namespaceMap != null ? namespaceMap.get(fqn) : null;
    }
    
    /**
     * 获取TypeDefinition
     */
    public CsdlTypeDefinition getTypeDefinition(FullQualifiedName fqn) {
        if (fqn == null) return null;
        
        Map<FullQualifiedName, CsdlTypeDefinition> namespaceMap = typeDefinitions.get(fqn.getNamespace());
        return namespaceMap != null ? namespaceMap.get(fqn) : null;
    }
    
    /**
     * 获取Term
     */
    public CsdlTerm getTerm(FullQualifiedName fqn) {
        if (fqn == null) return null;
        
        Map<FullQualifiedName, CsdlTerm> namespaceMap = terms.get(fqn.getNamespace());
        return namespaceMap != null ? namespaceMap.get(fqn) : null;
    }
    
    /**
     * 获取EntityContainer
     */
    public CsdlEntityContainer getEntityContainer(String namespace) {
        return entityContainers.get(namespace);
    }
    
    /**
     * 根据别名解析namespace
     */
    public String resolveNamespace(String aliasOrNamespace) {
        if (aliasOrNamespace == null) return null;
        
        // 首先检查是否是别名
        String namespace = aliasToNamespaceMap.get(aliasOrNamespace);
        if (namespace != null) {
            return namespace;
        }
        
        // 如果不是别名，检查是否是有效的namespace
        if (schemas.containsKey(aliasOrNamespace)) {
            return aliasOrNamespace;
        }
        
        return null;
    }
    
    /**
     * 添加依赖节点
     */
    public void addDependencyNode(SchemaDependencyNode node) {
        if (node != null && node.getFullyQualifiedName() != null) {
            dependencyNodes.put(node.getFullyQualifiedName(), node);
        }
    }
    
    /**
     * 获取依赖节点
     */
    public SchemaDependencyNode getDependencyNode(FullQualifiedName fqn) {
        return dependencyNodes.get(fqn);
    }
    
    /**
     * 获取所有依赖节点
     */
    public Map<FullQualifiedName, SchemaDependencyNode> getAllDependencyNodes() {
        return new HashMap<>(dependencyNodes);
    }
    
    /**
     * 获取所有Schema
     */
    public Map<String, CsdlSchema> getAllSchemas() {
        return new HashMap<>(schemas);
    }
    
    /**
     * 获取Schema
     */
    public CsdlSchema getSchema(String namespace) {
        return schemas.get(namespace);
    }
    
    /**
     * 检查是否包含Schema
     */
    public boolean containsSchema(String namespace) {
        return schemas.containsKey(namespace);
    }
    
    /**
     * 获取所有namespace
     */
    public Set<String> getAllNamespaces() {
        return new HashSet<>(schemas.keySet());
    }
    
    /**
     * 获取别名到namespace的映射
     */
    public Map<String, String> getAliasToNamespaceMap() {
        return new HashMap<>(aliasToNamespaceMap);
    }
    
    /**
     * 获取namespace到别名的映射
     */
    public Map<String, String> getNamespaceToAliasMap() {
        return new HashMap<>(namespaceToAliasMap);
    }
    
    /**
     * 清空所有数据
     */
    public synchronized void clear() {
        schemas.clear();
        dependencyNodes.clear();
        entityTypes.clear();
        complexTypes.clear();
        actions.clear();
        functions.clear();
        typeDefinitions.clear();
        terms.clear();
        entityContainers.clear();
        aliasToNamespaceMap.clear();
        namespaceToAliasMap.clear();
        totalElements = 0;
        lastUpdateTime = System.currentTimeMillis();
    }
    
    /**
     * 获取统计信息
     */
    public RepositoryStatistics getStatistics() {
        return new RepositoryStatistics(
            schemas.size(),
            totalElements,
            dependencyNodes.size(),
            lastUpdateTime
        );
    }
    
    /**
     * Repository统计信息类
     */
    public static class RepositoryStatistics {
        private final int schemaCount;
        private final int totalElements;
        private final int dependencyNodeCount;
        private final long lastUpdateTime;
        
        public RepositoryStatistics(int schemaCount, int totalElements, 
                                  int dependencyNodeCount, long lastUpdateTime) {
            this.schemaCount = schemaCount;
            this.totalElements = totalElements;
            this.dependencyNodeCount = dependencyNodeCount;
            this.lastUpdateTime = lastUpdateTime;
        }
        
        public int getSchemaCount() { return schemaCount; }
        public int getTotalElements() { return totalElements; }
        public int getDependencyNodeCount() { return dependencyNodeCount; }
        public long getLastUpdateTime() { return lastUpdateTime; }
    }
}
