package org.apache.olingo.compliance.validator.directory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.olingo.compliance.core.model.ComplianceErrorType;
import org.apache.olingo.compliance.core.model.ComplianceIssue;
import org.apache.olingo.compliance.core.model.XmlComplianceResult;
import org.apache.olingo.compliance.engine.core.SchemaExtractor;
import org.apache.olingo.compliance.engine.core.SchemaRegistry;

/**
 * 目录验证管理器，负责验证多层目录结构中的OData XML文件
 * 基于Schema Registry架构，支持跨文件的类型依赖检查和继承关系验证
 */
public class DirectoryValidationManager {
    
    private final Map<String, Set<SchemaInfo>> namespaceToSchemas; // 保留用于冲突检测
    private final Map<String, SchemaInfo> fileToSchema; // 保留用于冲突检测
    
    public DirectoryValidationManager() {
        this.namespaceToSchemas = new ConcurrentHashMap<>();
        this.fileToSchema = new ConcurrentHashMap<>();
    }
    
    /**
     * 验证指定目录及其子目录中的所有XML文件
     * @param directoryPath 目录路径
     * @return 验证结果
     */
    public DirectoryValidationResult validateDirectory(String directoryPath) {
        return validateDirectory(directoryPath, false);
    }
    
    /**
     * 验证指定目录及其子目录中的所有XML文件
     * @param directoryPath 目录路径
     * @param enableCrossFileValidation 是否启用跨文件验证
     * @return 验证结果
     */
    public DirectoryValidationResult validateDirectory(String directoryPath, boolean enableCrossFileValidation) {
        Objects.requireNonNull(directoryPath, "Directory path cannot be null");
        
        long startTime = System.currentTimeMillis();
        List<ComplianceIssue> allIssues = new ArrayList<>();
        
        try {
            // 清理之前的状态
            clearState();
            
            // 1. 收集所有XML文件（递归）
            List<File> xmlFiles = collectXmlFiles(directoryPath);
            
            if (xmlFiles.isEmpty()) {
                return createEmptyResult(directoryPath, startTime);
            }
            
            // 简化验证 - 仅检查文件是否存在
            for (File xmlFile : xmlFiles) {
                if (!xmlFile.exists()) {
                    ComplianceIssue issue = new ComplianceIssue(
                            ComplianceErrorType.MISSING_REQUIRED_ELEMENT,
                            "File does not exist: " + xmlFile.getName(),
                            ComplianceIssue.Severity.ERROR
                    );
                    allIssues.add(issue);
                }
            }
            
            // 增强验证 - 如果启用跨文件验证，则进行Schema冲突检测
            if (enableCrossFileValidation && xmlFiles.size() > 1) {
                allIssues.addAll(performDetailedConflictDetection(xmlFiles));
            } else if (xmlFiles.size() > 1) {
                // 默认情况下也启用跨文件验证
                allIssues.addAll(performDetailedConflictDetection(xmlFiles));
            } else if (xmlFiles.size() == 1) {
                // 对单文件也执行依赖冲突检测，因为依赖冲突可能是引用外部不存在的类型
                allIssues.addAll(performDetailedConflictDetection(xmlFiles));
            }
            
            long endTime = System.currentTimeMillis();
            
            XmlComplianceResult xmlResult = new XmlComplianceResult(
                    allIssues.isEmpty(),
                    allIssues,
                    Collections.emptySet(), // referencedNamespaces
                    new HashMap<>(), // metadata
                    directoryPath,
                    endTime - startTime
            );
            
            return new DirectoryValidationResult(xmlResult);
            
        } catch (Exception e) {
            // 验证过程中发生异常
            ComplianceIssue issue = new ComplianceIssue(
                    ComplianceErrorType.VALIDATION_ERROR,
                    "Directory validation failed: " + e.getMessage(),
                    ComplianceIssue.Severity.ERROR
            );
            allIssues.add(issue);
            
            long endTime = System.currentTimeMillis();
            
            XmlComplianceResult xmlResult = new XmlComplianceResult(
                    false, 
                    allIssues, 
                    Collections.emptySet(),
                    new HashMap<>(),
                    directoryPath,
                    endTime - startTime
            );
            
            return new DirectoryValidationResult(xmlResult);
        }
    }
    
    // 为了向后兼容，添加旧的方法名
    /**
     * @deprecated Use validateDirectory instead
     */
    @Deprecated
    public DirectoryValidationResult validateSingleDirectory(String directoryPath) {
        return validateDirectory(directoryPath);
    }
    
    // 为了向后兼容，添加旧的类型别名
    /**
     * @deprecated Use XmlComplianceResult instead
     */
    @Deprecated
    public static class DirectoryValidationResult {
        private final XmlComplianceResult xmlResult;
        
        public DirectoryValidationResult(XmlComplianceResult xmlResult) {
            this.xmlResult = xmlResult;
        }
        
        public DirectoryValidationResult(boolean isValid, List<ComplianceIssue> issues, 
                                       Set<String> referencedNamespaces, Map<String, Object> metadata,
                                       String source, long validationTimeMs) {
            this.xmlResult = new XmlComplianceResult(isValid, issues, referencedNamespaces, metadata, source, validationTimeMs);
        }
        
        // 旧API方法
        public boolean isValid() {
            return xmlResult.isCompliant();
        }
        
        public List<ComplianceIssue> getAllIssues() {
            return xmlResult.getIssues();
        }
        
        public int getTotalIssueCount() {
            return xmlResult.getIssues().size();
        }
        
        public int getTotalFiles() {
            // 简化实现 - 假设验证了1个"目录"
            return 1;
        }
        
        // 委托其他方法到XmlComplianceResult
        public List<ComplianceIssue> getIssues() {
            return xmlResult.getIssues();
        }
        
        public Set<String> getReferencedNamespaces() {
            return xmlResult.getReferencedNamespaces();
        }
        
        public Map<String, Object> getMetadata() {
            return xmlResult.getMetadata();
        }
        
        public String getSource() {
            return xmlResult.getFileName(); // 使用fileName作为source
        }
        
        public long getValidationTimeMs() {
            return xmlResult.getValidationTimeMs();
        }
    }
    
    /**
     * 执行详细的冲突检测
     */
    private List<ComplianceIssue> performDetailedConflictDetection(List<File> xmlFiles) {
        List<ComplianceIssue> issues = new ArrayList<>();
        
        // 构建详细的Schema信息映射
        Map<String, List<DetailedSchemaInfo>> namespaceToSchemas = new HashMap<>();
        Map<String, Set<String>> aliasToNamespaces = new HashMap<>();
        SchemaExtractor extractor = new SchemaExtractor();
        
        // 第一阶段：解析所有Schema并构建映射
        for (File xmlFile : xmlFiles) {
            try {
                List<SchemaRegistry.SchemaDefinition> schemas = extractor.extractSchemas(xmlFile);
                for (SchemaRegistry.SchemaDefinition schema : schemas) {
                    // 构建详细Schema信息
                    DetailedSchemaInfo detailedInfo = new DetailedSchemaInfo(
                            schema.getNamespace(),
                            schema.getAlias(),
                            xmlFile.getAbsolutePath(),
                            schema.getTypes()
                    );
                    
                    // 添加到namespace映射
                    namespaceToSchemas.computeIfAbsent(schema.getNamespace(), k -> new ArrayList<>())
                                     .add(detailedInfo);
                    
                    // 构建alias映射用于检测alias冲突
                    if (schema.getAlias() != null && !schema.getAlias().isEmpty()) {
                        aliasToNamespaces.computeIfAbsent(schema.getAlias(), k -> new HashSet<>())
                                        .add(schema.getNamespace());
                    }
                }
            } catch (Exception e) {
                // 解析失败，添加错误
                ComplianceIssue issue = new ComplianceIssue(
                        ComplianceErrorType.VALIDATION_ERROR,
                        "Failed to parse schema from " + xmlFile.getName() + ": " + e.getMessage(),
                        ComplianceIssue.Severity.ERROR
                );
                issues.add(issue);
            }
        }
        
        // 第二阶段：检测各种类型的冲突
        
        // 1. 检测EntityType、ComplexType、EnumType等元素冲突
        issues.addAll(detectElementConflicts(namespaceToSchemas));
        
        // 2. 检测Alias冲突
        issues.addAll(detectAliasConflicts(aliasToNamespaces));
        
        // 3. 检测容器级别冲突（EntityContainer、EntitySet等）
        issues.addAll(detectContainerConflicts(namespaceToSchemas));
        
        // 4. 检测实体级别冲突（NavigationProperty、ReferentialConstraint等）
        issues.addAll(detectEntityLevelConflicts(namespaceToSchemas));
        
        // 5. 检测继承层次冲突
        issues.addAll(detectInheritanceConflicts(namespaceToSchemas));

        // 6. 检测注解冲突
        issues.addAll(detectAnnotationConflicts(namespaceToSchemas));

        // 7. 检测无效继承层次结构
        issues.addAll(detectInvalidInheritanceHierarchy(namespaceToSchemas));

        // 8. 检测模式依赖冲突
        issues.addAll(detectSchemaDependencyConflicts(namespaceToSchemas));
        
        return issues;
    }
    
    /**
     * 检测元素冲突（EntityType、ComplexType、EnumType等）
     */
    private List<ComplianceIssue> detectElementConflicts(Map<String, List<DetailedSchemaInfo>> namespaceToSchemas) {
        List<ComplianceIssue> issues = new ArrayList<>();
        
        for (Map.Entry<String, List<DetailedSchemaInfo>> entry : namespaceToSchemas.entrySet()) {
            String namespace = entry.getKey();
            List<DetailedSchemaInfo> schemas = entry.getValue();
            
            if (schemas.size() <= 1) {
                continue; // 只有一个schema，不会有冲突
            }
            
            // 检测各种类型的元素冲突
            String[] elementTypes = {"EntityType", "ComplexType", "EnumType", "TypeDefinition", "Function", "Action", "Term", "EntityContainer"};
            
            for (String elementType : elementTypes) {
                Map<String, List<String>> elementNameToFiles = new HashMap<>();
                
                // 收集同一namespace下相同类型的所有元素
                for (DetailedSchemaInfo schema : schemas) {
                    for (SchemaRegistry.TypeDefinition type : schema.getTypes()) {
                        if (elementType.equals(type.getKind())) {
                            elementNameToFiles.computeIfAbsent(type.getName(), k -> new ArrayList<>())
                                             .add(schema.getFilePath());
                        }
                    }
                }
                
                // 检测重复定义
                for (Map.Entry<String, List<String>> elementEntry : elementNameToFiles.entrySet()) {
                    String elementName = elementEntry.getKey();
                    List<String> files = elementEntry.getValue();
                    
                    if (files.size() > 1) {
                        ComplianceIssue issue = new ComplianceIssue(
                                ComplianceErrorType.ELEMENT_CONFLICT,
                                String.format("%s '%s' is defined in multiple files within namespace '%s': %s", 
                                            elementType, elementName, namespace, 
                                            files.stream().map(f -> new File(f).getName()).collect(ArrayList::new, (list, file) -> list.add(file), (list1, list2) -> list1.addAll(list2))),
                                ComplianceIssue.Severity.ERROR
                        );
                        issues.add(issue);
                    }
                }
            }
        }
        
        return issues;
    }
    
    /**
     * 检测Alias冲突
     */
    private List<ComplianceIssue> detectAliasConflicts(Map<String, Set<String>> aliasToNamespaces) {
        List<ComplianceIssue> issues = new ArrayList<>();
        
        for (Map.Entry<String, Set<String>> entry : aliasToNamespaces.entrySet()) {
            String alias = entry.getKey();
            Set<String> namespaces = entry.getValue();
            
            if (namespaces.size() > 1) {
                ComplianceIssue issue = new ComplianceIssue(
                        ComplianceErrorType.ALIAS_CONFLICT,
                        String.format("Alias '%s' is used by multiple namespaces: %s", 
                                    alias, String.join(", ", namespaces)),
                        ComplianceIssue.Severity.ERROR
                );
                issues.add(issue);
            }
        }
        
        return issues;
    }
    
    /**
     * 检测容器级别冲突（EntitySet、Singleton、FunctionImport、ActionImport等）
     */
    private List<ComplianceIssue> detectContainerConflicts(Map<String, List<DetailedSchemaInfo>> namespaceToSchemas) {
        List<ComplianceIssue> issues = new ArrayList<>();
        
        for (Map.Entry<String, List<DetailedSchemaInfo>> entry : namespaceToSchemas.entrySet()) {
            String namespace = entry.getKey();
            List<DetailedSchemaInfo> schemas = entry.getValue();
            
            if (schemas.size() <= 1) {
                continue;
            }
            
            // 按 EntityContainer 分组检测容器内元素冲突
            Map<String, Map<String, List<String>>> containerToElementTypeToFiles = new HashMap<>();
            
            for (DetailedSchemaInfo schema : schemas) {
                for (SchemaRegistry.TypeDefinition type : schema.getTypes()) {
                    // 检测容器级别元素
                    if ("EntitySet".equals(type.getKind()) || "Singleton".equals(type.getKind()) ||
                        "FunctionImport".equals(type.getKind()) || "ActionImport".equals(type.getKind())) {
                        
                        // 需要从type中获取容器名称，这需要特殊处理
                        String containerName = extractContainerNameFromType(type, schema);
                        if (containerName != null) {
                            String elementKey = type.getKind() + ":" + type.getName();
                            
                            containerToElementTypeToFiles
                                .computeIfAbsent(containerName, k -> new HashMap<>())
                                .computeIfAbsent(elementKey, k -> new ArrayList<>())
                                .add(schema.getFilePath());
                        }
                    }
                    
                    // 检测 EntityContainer 冲突
                    else if ("EntityContainer".equals(type.getKind())) {
                        String containerKey = "EntityContainer:" + type.getName();
                        containerToElementTypeToFiles
                            .computeIfAbsent("__global__", k -> new HashMap<>())
                            .computeIfAbsent(containerKey, k -> new ArrayList<>())
                            .add(schema.getFilePath());
                    }
                }
            }
            
            // 检测冲突
            for (Map.Entry<String, Map<String, List<String>>> containerEntry : containerToElementTypeToFiles.entrySet()) {
                String containerName = containerEntry.getKey();
                Map<String, List<String>> elementToFiles = containerEntry.getValue();
                
                for (Map.Entry<String, List<String>> elementEntry : elementToFiles.entrySet()) {
                    String elementKey = elementEntry.getKey();
                    List<String> files = elementEntry.getValue();
                    
                    if (files.size() > 1) {
                        String[] parts = elementKey.split(":", 2);
                        String elementType = parts[0];
                        String elementName = parts[1];
                        
                        String message;
                        if ("__global__".equals(containerName)) {
                            message = String.format("%s '%s' is defined in multiple files within namespace '%s': %s", 
                                                  elementType, elementName, namespace,
                                                  files.stream().map(f -> new File(f).getName()).collect(ArrayList::new, (list, file) -> list.add(file), (list1, list2) -> list1.addAll(list2)));
                        } else {
                            message = String.format("%s '%s' is defined in multiple files within container '%s' (namespace '%s'): %s", 
                                                  elementType, elementName, containerName, namespace,
                                                  files.stream().map(f -> new File(f).getName()).collect(ArrayList::new, (list, file) -> list.add(file), (list1, list2) -> list1.addAll(list2)));
                        }
                        
                        ComplianceIssue issue = new ComplianceIssue(
                                ComplianceErrorType.ELEMENT_CONFLICT,
                                message,
                                ComplianceIssue.Severity.ERROR
                        );
                        issues.add(issue);
                    }
                }
            }
        }
        
        return issues;
    }
    
    /**
     * 从类型定义中提取容器名称
     */
    private String extractContainerNameFromType(SchemaRegistry.TypeDefinition type, DetailedSchemaInfo schema) {
        // 由于我们现在还没有在TypeDefinition接口中添加容器名称字段，
        // 这里使用简化的方法：假设同一个schema中的EntityContainer元素是相关的
        
        // 查找同一schema中的EntityContainer
        for (SchemaRegistry.TypeDefinition containerType : schema.getTypes()) {
            if ("EntityContainer".equals(containerType.getKind())) {
                return containerType.getName();
            }
        }
        
        return null; // 如果没有找到容器，返回null
    }
    
    /**
     * 检测实体级别冲突（NavigationProperty、ReferentialConstraint等）
     */
    private List<ComplianceIssue> detectEntityLevelConflicts(Map<String, List<DetailedSchemaInfo>> namespaceToSchemas) {
        List<ComplianceIssue> issues = new ArrayList<>();
        
        for (Map.Entry<String, List<DetailedSchemaInfo>> entry : namespaceToSchemas.entrySet()) {
            String namespace = entry.getKey();
            List<DetailedSchemaInfo> schemas = entry.getValue();
            
            if (schemas.size() <= 1) {
                continue;
            }
            
            // 按实体分组检测实体内元素冲突
            Map<String, Map<String, List<String>>> entityToElementTypeToFiles = new HashMap<>();
            
            for (DetailedSchemaInfo schema : schemas) {
                for (SchemaRegistry.TypeDefinition type : schema.getTypes()) {
                    // 检测实体级别元素
                    if ("NavigationProperty".equals(type.getKind()) || "ReferentialConstraint".equals(type.getKind())) {
                        // 需要从type中获取实体名称，这需要特殊处理
                        String entityName = extractEntityNameFromType(type, schema);
                        if (entityName != null) {
                            String elementKey = type.getKind() + ":" + type.getName();
                            
                            entityToElementTypeToFiles
                                .computeIfAbsent(entityName, k -> new HashMap<>())
                                .computeIfAbsent(elementKey, k -> new ArrayList<>())
                                .add(schema.getFilePath());
                        }
                    }
                }
            }
            
            // 检测冲突
            for (Map.Entry<String, Map<String, List<String>>> entityEntry : entityToElementTypeToFiles.entrySet()) {
                String entityName = entityEntry.getKey();
                Map<String, List<String>> elementToFiles = entityEntry.getValue();
                
                for (Map.Entry<String, List<String>> elementEntry : elementToFiles.entrySet()) {
                    String elementKey = elementEntry.getKey();
                    List<String> files = elementEntry.getValue();
                    
                    if (files.size() > 1) {
                        String[] parts = elementKey.split(":", 2);
                        String elementType = parts[0];
                        String elementName = parts[1];
                        
                        String message = String.format("%s '%s' is defined in multiple files within entity '%s' (namespace '%s'): %s", 
                                                      elementType, elementName, entityName, namespace,
                                                      files.stream().map(f -> new File(f).getName()).collect(ArrayList::new, (list, file) -> list.add(file), (list1, list2) -> list1.addAll(list2)));
                        
                        ComplianceIssue issue = new ComplianceIssue(
                                ComplianceErrorType.ELEMENT_CONFLICT,
                                message,
                                ComplianceIssue.Severity.ERROR
                        );
                        issues.add(issue);
                    }
                }
            }
        }
        
        return issues;
    }
    
    /**
     * 从类型定义中提取实体名称
     */
    private String extractEntityNameFromType(SchemaRegistry.TypeDefinition type, DetailedSchemaInfo schema) {
        // 简化的方法：由于当前 TypeDefinition 接口不支持获取父实体信息，
        // 这里使用 baseType 字段来存储相关信息
        return type.getBaseType(); // 对于实体元素，baseType可能包含相关信息
    }
    
    /**
     * 检测继承层次冲突
     */
    private List<ComplianceIssue> detectInheritanceConflicts(Map<String, List<DetailedSchemaInfo>> namespaceToSchemas) {
        List<ComplianceIssue> issues = new ArrayList<>();
        
        for (Map.Entry<String, List<DetailedSchemaInfo>> entry : namespaceToSchemas.entrySet()) {
            String namespace = entry.getKey();
            List<DetailedSchemaInfo> schemas = entry.getValue();
            
            // 收集所有带有继承关系的类型
            Map<String, String> typeToBaseType = new HashMap<>();
            
            for (DetailedSchemaInfo schema : schemas) {
                for (SchemaRegistry.TypeDefinition type : schema.getTypes()) {
                    if (("EntityType".equals(type.getKind()) || "ComplexType".equals(type.getKind())) 
                        && type.getBaseType() != null && !type.getBaseType().isEmpty()) {
                        typeToBaseType.put(type.getName(), type.getBaseType());
                    }
                }
            }
            
            // 检测循环继承
            for (Map.Entry<String, String> typeEntry : typeToBaseType.entrySet()) {
                String typeName = typeEntry.getKey();
                
                // 检测循环继承：A -> B -> C -> A
                Set<String> visitedTypes = new HashSet<>();
                String currentType = typeName;
                
                while (currentType != null && typeToBaseType.containsKey(currentType)) {
                    if (visitedTypes.contains(currentType)) {
                        // 发现循环继承
                        ComplianceIssue issue = new ComplianceIssue(
                                ComplianceErrorType.INVALID_INHERITANCE_HIERARCHY,
                                String.format("Circular inheritance detected in namespace '%s': %s", 
                                            namespace, String.join(" -> ", visitedTypes) + " -> " + currentType),
                                ComplianceIssue.Severity.ERROR
                        );
                        issues.add(issue);
                        break;
                    }
                    
                    visitedTypes.add(currentType);
                    currentType = typeToBaseType.get(currentType);
                    
                    // 限制检查深度，避免无限循环
                    if (visitedTypes.size() > 10) {
                        ComplianceIssue issue = new ComplianceIssue(
                                ComplianceErrorType.INVALID_INHERITANCE_HIERARCHY,
                                String.format("Inheritance chain too deep in namespace '%s' starting from type '%s'", 
                                            namespace, typeName),
                                ComplianceIssue.Severity.ERROR
                        );
                        issues.add(issue);
                        break;
                    }
                }
            }
        }
        
        return issues;
    }

    /**
     * 检测注解冲突 - 同一个Target有多个相同Term的Annotation
     */
    private List<ComplianceIssue> detectAnnotationConflicts(Map<String, List<DetailedSchemaInfo>> namespaceToSchemas) {
        List<ComplianceIssue> issues = new ArrayList<>();
        
        for (Map.Entry<String, List<DetailedSchemaInfo>> entry : namespaceToSchemas.entrySet()) {
            List<DetailedSchemaInfo> schemas = entry.getValue();
            
            // 收集所有注解：Target -> Term -> Files
            Map<String, Map<String, List<String>>> targetToTermToFiles = new HashMap<>();
            
            for (DetailedSchemaInfo schema : schemas) {
                for (SchemaRegistry.TypeDefinition type : schema.getTypes()) {
                    if ("Annotation".equals(type.getKind()) && type instanceof SchemaExtractor.OlingoAnnotationDefinition) {
                        SchemaExtractor.OlingoAnnotationDefinition annotation = (SchemaExtractor.OlingoAnnotationDefinition) type;
                        String target = annotation.getTarget();
                        String term = annotation.getTerm();
                        
                        targetToTermToFiles.computeIfAbsent(target, k -> new HashMap<>())
                                         .computeIfAbsent(term, k -> new ArrayList<>())
                                         .add(schema.getFilePath());
                    }
                }
            }
            
            // 检测冲突
            for (Map.Entry<String, Map<String, List<String>>> targetEntry : targetToTermToFiles.entrySet()) {
                String target = targetEntry.getKey();
                for (Map.Entry<String, List<String>> termEntry : targetEntry.getValue().entrySet()) {
                    String term = termEntry.getKey();
                    List<String> files = termEntry.getValue();
                    
                    if (files.size() > 1) {
                        ComplianceIssue issue = new ComplianceIssue(
                                ComplianceErrorType.ELEMENT_CONFLICT,
                                String.format("Annotation term '%s' for target '%s' is defined in multiple files within namespace '%s': %s",
                                            term, target, entry.getKey(),
                                            files.stream().map(f -> new File(f).getName()).collect(ArrayList::new, (list, file) -> list.add(file), (list1, list2) -> list1.addAll(list2))),
                                ComplianceIssue.Severity.ERROR
                        );
                        issues.add(issue);
                    }
                }
            }
        }
        
        return issues;
    }

    /**
     * 检测无效继承层次结构 - ComplexType试图继承EntityType等不兼容的继承关系
     */
    private List<ComplianceIssue> detectInvalidInheritanceHierarchy(Map<String, List<DetailedSchemaInfo>> namespaceToSchemas) {
        List<ComplianceIssue> issues = new ArrayList<>();
        
        // 收集所有类型的定义：name -> kind
        Map<String, String> typeNameToKind = new HashMap<>();
        
        for (List<DetailedSchemaInfo> schemas : namespaceToSchemas.values()) {
            for (DetailedSchemaInfo schema : schemas) {
                for (SchemaRegistry.TypeDefinition type : schema.getTypes()) {
                    String fullName = schema.getNamespace() + "." + type.getName();
                    typeNameToKind.put(fullName, type.getKind());
                }
            }
        }
        
        // 检测不兼容的继承关系
        for (List<DetailedSchemaInfo> schemas : namespaceToSchemas.values()) {
            for (DetailedSchemaInfo schema : schemas) {
                for (SchemaRegistry.TypeDefinition type : schema.getTypes()) {
                    String baseType = type.getBaseType();
                    if (baseType != null && !baseType.isEmpty()) {
                        String currentKind = type.getKind();
                        String baseKind = typeNameToKind.get(baseType);
                        
                        if (baseKind != null && !isValidInheritance(currentKind, baseKind)) {
                            ComplianceIssue issue = new ComplianceIssue(
                                    ComplianceErrorType.INVALID_INHERITANCE_HIERARCHY,
                                    String.format("Invalid inheritance: %s '%s' cannot inherit from %s '%s'",
                                                currentKind, type.getName(), baseKind, baseType),
                                    ComplianceIssue.Severity.ERROR
                            );
                            issues.add(issue);
                        }
                    }
                }
            }
        }
        
        return issues;
    }

    /**
     * 检测模式依赖冲突 - 引用了不存在的类型
     */
    private List<ComplianceIssue> detectSchemaDependencyConflicts(Map<String, List<DetailedSchemaInfo>> namespaceToSchemas) {
        List<ComplianceIssue> issues = new ArrayList<>();
        
        // 收集所有已定义的类型
        Set<String> definedTypes = new HashSet<>();
        for (List<DetailedSchemaInfo> schemas : namespaceToSchemas.values()) {
            for (DetailedSchemaInfo schema : schemas) {
                for (SchemaRegistry.TypeDefinition type : schema.getTypes()) {
                    String fullName = schema.getNamespace() + "." + type.getName();
                    definedTypes.add(fullName);
                }
            }
        }
        
        // 检测未定义的依赖
        for (Map.Entry<String, List<DetailedSchemaInfo>> entry : namespaceToSchemas.entrySet()) {
            List<DetailedSchemaInfo> schemas = entry.getValue();
            
            for (DetailedSchemaInfo schema : schemas) {
                for (SchemaRegistry.TypeDefinition type : schema.getTypes()) {
                    String baseType = type.getBaseType();
                    
                    if (baseType != null && !baseType.isEmpty() && 
                        !baseType.startsWith("Edm.") && // 忽略内置类型
                        !definedTypes.contains(baseType)) {
                        
                        ComplianceIssue issue = new ComplianceIssue(
                                ComplianceErrorType.SCHEMA_DEPENDENCY_ERROR,
                                String.format("Schema dependency error: Type '%s' references undefined base type '%s'", 
                                            type.getName(), baseType),
                                ComplianceIssue.Severity.ERROR
                        );
                        issues.add(issue);
                    }
                }
            }
        }
        
        return issues;
    }

    /**
     * 检查继承关系是否有效
     */
    private boolean isValidInheritance(String currentKind, String baseKind) {
        // EntityType 只能继承 EntityType
        if ("EntityType".equals(currentKind)) {
            return "EntityType".equals(baseKind);
        }
        
        // ComplexType 只能继承 ComplexType
        if ("ComplexType".equals(currentKind)) {
            return "ComplexType".equals(baseKind);
        }
        
        return true; // 其他类型暂时允许
    }
    
    /**
     * 详细的Schema信息类
     */
    private static class DetailedSchemaInfo {
        private final String namespace;
        private final String alias;
        private final String filePath;
        private final List<SchemaRegistry.TypeDefinition> types;
        
        public DetailedSchemaInfo(String namespace, String alias, String filePath, List<SchemaRegistry.TypeDefinition> types) {
            this.namespace = namespace;
            this.alias = alias;
            this.filePath = filePath;
            this.types = new ArrayList<>(types);
        }
        
        public String getNamespace() { return namespace; }
        public String getAlias() { return alias; }
        public String getFilePath() { return filePath; }
        public List<SchemaRegistry.TypeDefinition> getTypes() { return types; }
    }
    private List<File> collectXmlFiles(String directoryPath) throws IOException {
        List<File> xmlFiles = new ArrayList<>();
        Path rootPath = Paths.get(directoryPath);
        
        if (!Files.exists(rootPath)) {
            throw new IllegalArgumentException("Directory does not exist: " + directoryPath);
        }
        
        if (!Files.isDirectory(rootPath)) {
            throw new IllegalArgumentException("Path is not a directory: " + directoryPath);
        }
        
        try (Stream<Path> paths = Files.walk(rootPath)) {
            paths.filter(Files::isRegularFile)
                 .filter(path -> path.toString().toLowerCase().endsWith(".xml"))
                 .map(Path::toFile)
                 .forEach(xmlFiles::add);
        }
        
        return xmlFiles;
    }
    
    /**
     * 清理之前的验证状态
     */
    private void clearState() {
        namespaceToSchemas.clear();
        fileToSchema.clear();
    }
    
    /**
     * 创建空结果（没有找到XML文件时）
     */
    private DirectoryValidationResult createEmptyResult(String directoryPath, long startTime) {
        long endTime = System.currentTimeMillis();
        
        XmlComplianceResult xmlResult = new XmlComplianceResult(
                true,
                Collections.emptyList(),
                Collections.emptySet(),
                new HashMap<>(),
                directoryPath,
                endTime - startTime
        );
        
        return new DirectoryValidationResult(xmlResult);
    }
    
    /**
     * 获取目录验证统计信息
     */
    public DirectoryValidationStatistics getStatistics() {
        return new DirectoryValidationStatistics(
                namespaceToSchemas.size(),
                fileToSchema.size(),
                namespaceToSchemas.values().stream()
                        .mapToInt(Set::size)
                        .sum()
        );
    }
    
    /**
     * Schema信息类（用于冲突检测）
     */
    public static class SchemaInfo {
        private final String namespace;
        private final String alias;
        private final String filePath;
        private final int typeCount;
        
        public SchemaInfo(String namespace, String alias, String filePath, int typeCount) {
            this.namespace = namespace;
            this.alias = alias;
            this.filePath = filePath;
            this.typeCount = typeCount;
        }
        
        public String getNamespace() { return namespace; }
        public String getAlias() { return alias; }
        public String getFilePath() { return filePath; }
        public int getTypeCount() { return typeCount; }
        
        // 添加SchemaConflictDetector需要的方法
        public List<String> getElementNames() {
            return Collections.emptyList(); // 简化实现
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SchemaInfo that = (SchemaInfo) o;
            return Objects.equals(namespace, that.namespace) &&
                   Objects.equals(filePath, that.filePath);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(namespace, filePath);
        }
        
        @Override
        public String toString() {
            return String.format("Schema[namespace=%s, alias=%s, file=%s, types=%d]",
                    namespace, alias, filePath, typeCount);
        }
    }
    
    /**
     * 目录验证统计信息
     */
    public static class DirectoryValidationStatistics {
        private final int namespaceCount;
        private final int fileCount;
        private final int schemaCount;
        
        public DirectoryValidationStatistics(int namespaceCount, int fileCount, int schemaCount) {
            this.namespaceCount = namespaceCount;
            this.fileCount = fileCount;
            this.schemaCount = schemaCount;
        }
        
        public int getNamespaceCount() { return namespaceCount; }
        public int getFileCount() { return fileCount; }
        public int getSchemaCount() { return schemaCount; }
        
        @Override
        public String toString() {
            return String.format("DirectoryStats[namespaces=%d, files=%d, schemas=%d]",
                    namespaceCount, fileCount, schemaCount);
        }
    }
}
