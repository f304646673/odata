package org.apache.olingo.compliance.validation.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 知识库加载器，负责从XML模式文件中加载类型信息构建ComplianceKnowledgeBase。
 * 这个类提供了多种加载策略和合并功能。
 */
public class ComplianceKnowledgeBaseLoader {
    
    private final Map<String, String> loadedFileCache = new ConcurrentHashMap<>();
    
    /**
     * 从单个文件加载知识库
     */
    public ComplianceKnowledgeBase loadFromFile(File schemaFile) throws IOException {
        ComplianceKnowledgeBase.Builder builder = new ComplianceKnowledgeBase.Builder();
        
        if (schemaFile.exists() && schemaFile.isFile()) {
            processSchemaFile(schemaFile, builder);
        }
        
        return builder.build();
    }
    
    /**
     * 从多个文件加载知识库
     */
    public ComplianceKnowledgeBase loadFromFiles(List<File> schemaFiles) throws IOException {
        ComplianceKnowledgeBase.Builder builder = new ComplianceKnowledgeBase.Builder();
        
        for (File schemaFile : schemaFiles) {
            if (schemaFile.exists() && schemaFile.isFile()) {
                processSchemaFile(schemaFile, builder);
            }
        }
        
        return builder.build();
    }
    
    /**
     * 合并两个知识库
     */
    public ComplianceKnowledgeBase merge(ComplianceKnowledgeBase base, ComplianceKnowledgeBase additional) {
        ComplianceKnowledgeBase.Builder builder = new ComplianceKnowledgeBase.Builder();
        
        // 添加基础知识库的内容
        addKnowledgeBaseToBuilder(base, builder);
        
        // 添加额外知识库的内容
        addKnowledgeBaseToBuilder(additional, builder);
        
        return builder.build();
    }
    
    /**
     * 将知识库内容添加到构建器
     */
    private void addKnowledgeBaseToBuilder(ComplianceKnowledgeBase kb, ComplianceKnowledgeBase.Builder builder) {
        // 添加所有类型定义
        for (String typeName : kb.getAllDefinedTypes()) {
            ComplianceKnowledgeBase.TypeDefinition typeDef = kb.getTypeDefinition(typeName);
            if (typeDef != null) {
                builder.addTypeDefinition(typeDef);
            }
        }
        
        // 添加所有命名空间
        for (String namespace : kb.getAllRegisteredNamespaces()) {
            builder.addNamespace(namespace);
            
            // 添加服务定义
            ComplianceKnowledgeBase.ServiceDefinition serviceDef = kb.getServiceDefinition(namespace);
            if (serviceDef != null) {
                builder.addServiceDefinition(serviceDef);
            }
        }
    }
    
    /**
     * 处理单个模式文件
     */
    private void processSchemaFile(File schemaFile, ComplianceKnowledgeBase.Builder builder) throws IOException {
        String absolutePath = schemaFile.getAbsolutePath();
        
        // 检查是否已经处理过这个文件
        if (loadedFileCache.containsKey(absolutePath)) {
            return;
        }
        
        try {
            // 这里应该实现真正的XML解析逻辑
            // 为了演示目的，我们创建一些示例类型定义
            
            String fileName = schemaFile.getName();
            String namespace = extractNamespaceFromFileName(fileName);
            
            // 添加一些标准的OData类型
            addStandardODataTypes(builder, namespace);
            
            // 根据文件名推断可能的类型定义
            addInferredTypesFromFileName(builder, namespace, fileName);
            
            // 标记文件已处理
            loadedFileCache.put(absolutePath, namespace);
            
        } catch (Exception e) {
            throw new IOException("Failed to process schema file: " + absolutePath, e);
        }
    }
    
    /**
     * 从文件名提取命名空间
     */
    private String extractNamespaceFromFileName(String fileName) {
        // 简化的命名空间提取逻辑
        String baseName = fileName.replaceAll("\\.(xml|xsd)$", "");
        
        // 特殊处理一些已知的模式文件
        if (baseName.toLowerCase().contains("odata")) {
            return "com.odata.core";
        } else if (baseName.toLowerCase().contains("edm")) {
            return "com.odata.edm";
        } else if (baseName.toLowerCase().contains("service")) {
            return "com.service." + baseName.toLowerCase();
        } else {
            return "com.schema." + baseName.toLowerCase();
        }
    }
    
    /**
     * 添加标准OData类型
     */
    private void addStandardODataTypes(ComplianceKnowledgeBase.Builder builder, String namespace) {
        // 添加基本的EDM类型
        String edmNamespace = "Edm";
        
        addPrimitiveType(builder, edmNamespace, "String");
        addPrimitiveType(builder, edmNamespace, "Int32");
        addPrimitiveType(builder, edmNamespace, "Int64");
        addPrimitiveType(builder, edmNamespace, "Boolean");
        addPrimitiveType(builder, edmNamespace, "DateTime");
        addPrimitiveType(builder, edmNamespace, "DateTimeOffset");
        addPrimitiveType(builder, edmNamespace, "Decimal");
        addPrimitiveType(builder, edmNamespace, "Double");
        addPrimitiveType(builder, edmNamespace, "Single");
        addPrimitiveType(builder, edmNamespace, "Guid");
        addPrimitiveType(builder, edmNamespace, "Binary");
        addPrimitiveType(builder, edmNamespace, "Stream");
        
        // 添加命名空间
        builder.addNamespace(edmNamespace);
        builder.addNamespace(namespace);
    }
    
    /**
     * 添加基本类型定义
     */
    private void addPrimitiveType(ComplianceKnowledgeBase.Builder builder, String namespace, String typeName) {
        String fullName = namespace + "." + typeName;
        
        ComplianceKnowledgeBase.TypeDefinition typeDef = new ComplianceKnowledgeBase.TypeDefinition(
            fullName,
            namespace,
            typeName,
            ComplianceKnowledgeBase.TypeKind.PRIMITIVE_TYPE,
            null, // 基本类型没有父类型
            new ConcurrentHashMap<>()
        );
        
        builder.addTypeDefinition(typeDef);
    }
    
    /**
     * 根据文件名推断类型定义
     */
    private void addInferredTypesFromFileName(ComplianceKnowledgeBase.Builder builder, 
                                            String namespace, String fileName) {
        String baseName = fileName.replaceAll("\\.(xml|xsd)$", "");
        
        // 如果文件名包含某些关键词，创建相应的类型定义
        if (baseName.toLowerCase().contains("customer")) {
            addSampleEntityType(builder, namespace, "Customer", null);
        }
        
        if (baseName.toLowerCase().contains("order")) {
            addSampleEntityType(builder, namespace, "Order", null);
        }
        
        if (baseName.toLowerCase().contains("product")) {
            addSampleEntityType(builder, namespace, "Product", null);
        }
        
        if (baseName.toLowerCase().contains("address")) {
            addSampleComplexType(builder, namespace, "Address", null);
        }
        
        // 添加一个示例服务定义
        if (baseName.toLowerCase().contains("service")) {
            addSampleServiceDefinition(builder, namespace);
        }
    }
    
    /**
     * 添加示例实体类型
     */
    private void addSampleEntityType(ComplianceKnowledgeBase.Builder builder, 
                                   String namespace, String typeName, String baseType) {
        String fullName = namespace + "." + typeName;
        
        Map<String, Object> properties = new ConcurrentHashMap<>();
        properties.put("hasKey", true);
        properties.put("isAbstract", false);
        
        ComplianceKnowledgeBase.TypeDefinition typeDef = new ComplianceKnowledgeBase.TypeDefinition(
            fullName,
            namespace,
            typeName,
            ComplianceKnowledgeBase.TypeKind.ENTITY_TYPE,
            baseType,
            properties
        );
        
        builder.addTypeDefinition(typeDef);
    }
    
    /**
     * 添加示例复杂类型
     */
    private void addSampleComplexType(ComplianceKnowledgeBase.Builder builder, 
                                    String namespace, String typeName, String baseType) {
        String fullName = namespace + "." + typeName;
        
        Map<String, Object> properties = new ConcurrentHashMap<>();
        properties.put("isAbstract", false);
        
        ComplianceKnowledgeBase.TypeDefinition typeDef = new ComplianceKnowledgeBase.TypeDefinition(
            fullName,
            namespace,
            typeName,
            ComplianceKnowledgeBase.TypeKind.COMPLEX_TYPE,
            baseType,
            properties
        );
        
        builder.addTypeDefinition(typeDef);
    }
    
    /**
     * 添加示例服务定义
     */
    private void addSampleServiceDefinition(ComplianceKnowledgeBase.Builder builder, String namespace) {
        String containerName = namespace + ".Container";
        
        List<String> entitySetNames = new ArrayList<>();
        entitySetNames.add("Customers");
        entitySetNames.add("Orders");
        entitySetNames.add("Products");
        
        // 创建包含实体集名称的Set
        Set<String> entitySetSet = ConcurrentHashMap.newKeySet();
        entitySetSet.addAll(entitySetNames);
        
        ComplianceKnowledgeBase.ServiceDefinition serviceDef = new ComplianceKnowledgeBase.ServiceDefinition(
            namespace,
            containerName,
            entitySetSet, // entity sets
            ConcurrentHashMap.newKeySet(), // singletons
            ConcurrentHashMap.newKeySet(), // action imports
            ConcurrentHashMap.newKeySet()  // function imports
        );
        
        builder.addServiceDefinition(serviceDef);
    }
    
    /**
     * 清除加载缓存
     */
    public void clearCache() {
        loadedFileCache.clear();
    }
    
    /**
     * 获取已加载文件的信息
     */
    public Map<String, String> getLoadedFiles() {
        return new ConcurrentHashMap<>(loadedFileCache);
    }
}
