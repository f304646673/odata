package org.apache.olingo.compliance.validator.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.compliance.core.api.ValidationConfig;
import org.apache.olingo.compliance.core.model.ComplianceErrorType;
import org.apache.olingo.compliance.core.model.ComplianceIssue;
import org.apache.olingo.compliance.core.model.ComplianceResult;
import org.apache.olingo.compliance.engine.core.SchemaExtractor;
import org.apache.olingo.compliance.engine.core.SchemaRegistry;
import org.apache.olingo.compliance.engine.core.ValidationContext;
import org.apache.olingo.compliance.engine.core.ValidationEngine;
import org.apache.olingo.compliance.engine.core.impl.DefaultValidationEngineImpl;
import org.apache.olingo.compliance.validator.ComplianceValidator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * 统一的合规性验证器实现，支持文件和目录的验证
 * 
 * 该实现提供了以下功能：
 * 1. 单个文件验证 - 返回单文件验证结果
 * 2. 目录验证 - 扫描目录中所有XML文件并聚合验证结果
 * 3. 跨文件引用验证 - 使用SchemaRegistry进行类型依赖检查
 * 4. 统一的返回结果格式 - 所有验证都返回ComplianceResult
 */
public class ComplianceValidatorImpl implements ComplianceValidator {
    
    private final ValidationEngine engine;
    private final SchemaExtractor schemaExtractor;
    
    // 用于目录验证时的命名空间和Schema管理
    private final Map<String, Set<SchemaInfo>> namespaceToSchemas;
    private final Map<String, SchemaInfo> fileToSchema;
    
    public ComplianceValidatorImpl() {
        this.engine = createBasicValidationEngine();
        this.schemaExtractor = new SchemaExtractor();
        this.namespaceToSchemas = new ConcurrentHashMap<>();
        this.fileToSchema = new ConcurrentHashMap<>();
    }
    
    public ComplianceValidatorImpl(ValidationEngine customEngine) {
        this.engine = customEngine != null ? customEngine : createBasicValidationEngine();
        this.schemaExtractor = new SchemaExtractor();
        this.namespaceToSchemas = new ConcurrentHashMap<>();
        this.fileToSchema = new ConcurrentHashMap<>();
    }
    
    // ==================== 文件验证方法 ====================
    
    @Override
    public ComplianceResult validateFile(File xmlFile, SchemaRegistry registry) {
        if (xmlFile == null || !xmlFile.exists()) {
            return createErrorResult("File does not exist or is null", 
                                   xmlFile != null ? xmlFile.getAbsolutePath() : "null");
        }
        
        return validateSingleFile(xmlFile, registry);
    }
    
    @Override
    public ComplianceResult validateFile(Path xmlPath, SchemaRegistry registry) {
        if (xmlPath == null || !Files.exists(xmlPath)) {
            return createErrorResult("Path does not exist or is null", 
                                   xmlPath != null ? xmlPath.toString() : "null");
        }
        
        return validateFile(xmlPath.toFile(), registry);
    }
    
    @Override
    public ComplianceResult validateContent(String xmlContent, String fileName, SchemaRegistry registry) {
        if (xmlContent == null || xmlContent.trim().isEmpty()) {
            return createErrorResult("XML content is null or empty", fileName != null ? fileName : "unknown");
        }
        
        try {
            // 创建临时文件来处理字符串内容
            File tempFile = File.createTempFile("temp_validation_", ".xml");
            tempFile.deleteOnExit();
            
            // 写入内容到临时文件
            Files.write(tempFile.toPath(), xmlContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
            // 验证文件
            ComplianceResult result = validateSingleFile(tempFile, registry);
            
            // 更新结果中的源信息为提供的文件名
            return createResultWithCustomSource(result, fileName != null ? fileName : "content");

        } catch (IOException e) {
            return createErrorResult("Failed to process XML content: " + e.getMessage(), 
                                   fileName != null ? fileName : "unknown");
        }
    }
    
    // ==================== 目录验证方法 ====================
    
    @Override
    public ComplianceResult validateDirectory(String directoryPath, SchemaRegistry registry) {
        return validateDirectory(directoryPath, registry, true);
    }
    
    @Override
    public ComplianceResult validateDirectory(Path directoryPath, SchemaRegistry registry) {
        return validateDirectory(directoryPath.toString(), registry, true);
    }
    
    @Override
    public ComplianceResult validateDirectory(String directoryPath, SchemaRegistry registry, boolean enableCrossFileValidation) {
        if (directoryPath == null || directoryPath.trim().isEmpty()) {
            return createErrorResult("Directory path is null or empty", directoryPath);
        }
        
        Path dirPath = Paths.get(directoryPath);
        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
            return createErrorResult("Directory does not exist or is not a directory", directoryPath);
        }
        
        return validateDirectoryInternal(dirPath, registry, enableCrossFileValidation);
    }
    
    @Override
    public ComplianceResult validateDirectory(Path directoryPath, SchemaRegistry registry, boolean enableCrossFileValidation) {
        if (directoryPath == null) {
            return createErrorResult("Directory path is null", "null");
        }
        
        return validateDirectory(directoryPath.toString(), registry, enableCrossFileValidation);
    }
    
    // ==================== 私有实现方法 ====================
    
    /**
     * 验证单个文件的核心逻辑
     */
    private ComplianceResult validateSingleFile(File xmlFile, SchemaRegistry registry) {
        long startTime = System.currentTimeMillis();
        List<ComplianceIssue> allIssues = new ArrayList<>();
        Set<String> referencedNamespaces = new HashSet<>();
        Map<String, Object> metadata = new HashMap<>();
        
        try {
            // 1. 基础的Registry验证
            allIssues.addAll(validateTypeReferences(xmlFile, registry));
            allIssues.addAll(validateInheritanceRelations(xmlFile, registry));
            
            // 2. 增强的ValidationEngine验证
            ValidationContext context = ValidationContext.forFile(xmlFile.toPath());
            
            // 解析Schema（如果需要）
            List<CsdlSchema> schemas = parseSchemas(xmlFile);
            if (schemas != null && !schemas.isEmpty()) {
                context.setAllSchemas(schemas);
                
                // 收集命名空间信息
                for (CsdlSchema schema : schemas) {
                    if (schema.getNamespace() != null) {
                        context.addCurrentSchemaNamespace(schema.getNamespace());
                        referencedNamespaces.add(schema.getNamespace());
                    }
                }
            }
            
            // 使用ValidationEngine进行验证
            ValidationConfig config = ValidationConfig.standard();
            org.apache.olingo.compliance.core.api.ValidationResult result = engine.validate(context, config);
            
            // 添加ValidationEngine的验证结果
            for (String error : result.getErrors()) {
                allIssues.add(createComplianceIssue(error, xmlFile.getAbsolutePath(), ComplianceIssue.Severity.ERROR));
            }
            
            for (String warning : result.getWarnings()) {
                allIssues.add(createComplianceIssue(warning, xmlFile.getAbsolutePath(), ComplianceIssue.Severity.WARNING));
            }
            
            // 添加元数据
            metadata.put("fileSize", xmlFile.length());
            metadata.put("fileName", xmlFile.getName());
            metadata.put("validationType", "single-file");
            
        } catch (Exception e) {
            allIssues.add(createComplianceIssue(
                "Failed to validate file: " + e.getMessage(), 
                xmlFile.getAbsolutePath(), 
                ComplianceIssue.Severity.ERROR
            ));
        }
        
        // 计算验证时间
        long validationTime = System.currentTimeMillis() - startTime;
        metadata.put("validationTimeMs", validationTime);
        
        // 确定合规性状态
        boolean isCompliant = allIssues.isEmpty() || 
                             allIssues.stream().noneMatch(issue -> issue.getSeverity() == ComplianceIssue.Severity.ERROR);
        
        return new ComplianceResult(
            isCompliant,
            allIssues,
            referencedNamespaces,
            metadata,
            xmlFile.getAbsolutePath(),
            validationTime
        );
    }
    
    /**
     * 验证目录的核心逻辑
     */
    private ComplianceResult validateDirectoryInternal(Path directoryPath, SchemaRegistry registry, boolean enableCrossFileValidation) {
        long startTime = System.currentTimeMillis();
        List<ComplianceIssue> allIssues = new ArrayList<>();
        Set<String> allNamespaces = new HashSet<>();
        Map<String, Object> metadata = new HashMap<>();
        
        // 清理之前的状态
        namespaceToSchemas.clear();
        fileToSchema.clear();
        
        List<File> xmlFiles = new ArrayList<>();
        int processedFiles = 0;

        try {
            // 1. 扫描所有XML文件
            try (Stream<Path> files = Files.walk(directoryPath)) {
                files.filter(Files::isRegularFile)
                     .filter(path -> path.toString().toLowerCase().endsWith(".xml"))
                     .forEach(path -> xmlFiles.add(path.toFile()));
            }
            
            int totalFiles = xmlFiles.size();
            metadata.put("totalFilesFound", totalFiles);
            
            if (xmlFiles.isEmpty()) {
                metadata.put("validationType", "directory-empty");
                metadata.put("directoryPath", directoryPath.toString());
                metadata.put("processedFiles", 0);
                
                return new ComplianceResult(
                    true, // 空目录视为合规
                    allIssues,
                    allNamespaces,
                    metadata,
                    directoryPath.toString(),
                    System.currentTimeMillis() - startTime
                );
            }
            
            // 2. 如果启用跨文件验证，先构建Schema Registry
            if (enableCrossFileValidation) {
                buildSchemaRegistry(xmlFiles, registry, allIssues);
            }
            
            // 3. 验证每个文件
            for (File xmlFile : xmlFiles) {
                try {
                    ComplianceResult fileResult = validateSingleFile(xmlFile, registry);
                    
                    // 聚合结果
                    allIssues.addAll(fileResult.getIssues());
                    allNamespaces.addAll(fileResult.getReferencedNamespaces());
                    
                    processedFiles++;
                    
                } catch (Exception e) {
                    allIssues.add(createComplianceIssue(
                        "Failed to validate file: " + e.getMessage(),
                        xmlFile.getAbsolutePath(),
                        ComplianceIssue.Severity.ERROR
                    ));
                }
            }
            
            // 4. 如果启用跨文件验证，检查命名空间冲突
            if (enableCrossFileValidation) {
                allIssues.addAll(detectNamespaceConflicts());
            }
            
        } catch (Exception e) {
            allIssues.add(createComplianceIssue(
                "Failed to process directory: " + e.getMessage(),
                directoryPath.toString(),
                ComplianceIssue.Severity.ERROR
            ));
        }
        
        // 设置元数据
        long validationTime = System.currentTimeMillis() - startTime;
        metadata.put("validationType", "directory");
        metadata.put("directoryPath", directoryPath.toString());
        metadata.put("processedFiles", processedFiles);
        metadata.put("crossFileValidationEnabled", enableCrossFileValidation);
        metadata.put("validationTimeMs", validationTime);
        
        // 确定合规性状态
        boolean isCompliant = allIssues.isEmpty() || 
                             allIssues.stream().noneMatch(issue -> issue.getSeverity() == ComplianceIssue.Severity.ERROR);
        
        return new ComplianceResult(
            isCompliant,
            allIssues,
            allNamespaces,
            metadata,
            directoryPath.toString(),
            validationTime
        );
    }
    
    /**
     * 构建Schema Registry用于跨文件验证
     */
    private void buildSchemaRegistry(List<File> xmlFiles, SchemaRegistry registry, List<ComplianceIssue> issues) {
        for (File xmlFile : xmlFiles) {
            try {
                // 简化的Schema信息提取 - 基于XML解析
                SchemaInfo schemaInfo = extractSimpleSchemaInfo(xmlFile);
                if (schemaInfo != null && schemaInfo.getNamespace() != null) {
                    // 注册到SchemaRegistry
                    for (String typeName : schemaInfo.getTypes()) {
//                        registry.registerType(schemaInfo.getNamespace() + "." + typeName);
                    }

                    // 维护本地映射用于冲突检测
                    namespaceToSchemas.computeIfAbsent(schemaInfo.getNamespace(), k -> new HashSet<>()).add(schemaInfo);
                    fileToSchema.put(xmlFile.getAbsolutePath(), schemaInfo);
                }
            } catch (Exception e) {
                issues.add(createComplianceIssue(
                    "Failed to extract schema from file: " + e.getMessage(),
                    xmlFile.getAbsolutePath(),
                    ComplianceIssue.Severity.WARNING
                ));
            }
        }
    }
    
    /**
     * 简化的Schema信息提取
     */
    private SchemaInfo extractSimpleSchemaInfo(File xmlFile) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            // 安全配置
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);

            // 获取Schema命名空间
            NodeList schemas = document.getElementsByTagNameNS("*", "Schema");
            if (schemas.getLength() > 0) {
                Element schema = (Element) schemas.item(0);
                String namespace = schema.getAttribute("Namespace");

                if (namespace != null && !namespace.isEmpty()) {
                    Set<String> types = new HashSet<>();

                    // 收集EntityType
                    NodeList entityTypes = document.getElementsByTagNameNS("*", "EntityType");
                    for (int i = 0; i < entityTypes.getLength(); i++) {
                        Element entityType = (Element) entityTypes.item(i);
                        String typeName = entityType.getAttribute("Name");
                        if (typeName != null && !typeName.isEmpty()) {
                            types.add(typeName);
                        }
                    }

                    // 收集ComplexType
                    NodeList complexTypes = document.getElementsByTagNameNS("*", "ComplexType");
                    for (int i = 0; i < complexTypes.getLength(); i++) {
                        Element complexType = (Element) complexTypes.item(i);
                        String typeName = complexType.getAttribute("Name");
                        if (typeName != null && !typeName.isEmpty()) {
                            types.add(typeName);
                        }
                    }

                    return new SchemaInfo(namespace, xmlFile.getAbsolutePath(), types);
                }
            }

            return null;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检测命名空间冲突
     */
    private List<ComplianceIssue> detectNamespaceConflicts() {
        List<ComplianceIssue> conflicts = new ArrayList<>();
        
        for (Map.Entry<String, Set<SchemaInfo>> entry : namespaceToSchemas.entrySet()) {
            String namespace = entry.getKey();
            Set<SchemaInfo> schemas = entry.getValue();
            
            if (schemas.size() > 1) {
                // 存在命名空间冲突
                StringBuilder conflictFiles = new StringBuilder();
                for (SchemaInfo schema : schemas) {
                    if (conflictFiles.length() > 0) {
                        conflictFiles.append(", ");
                    }
                    conflictFiles.append(schema.getFilePath());
                }
                
                conflicts.add(new ComplianceIssue(
                    ComplianceErrorType.NAMESPACE_CONFLICT,
                    "Namespace '" + namespace + "' is defined in multiple files: " + conflictFiles,
                    null,
                    namespace,
                    ComplianceIssue.Severity.ERROR
                ));
            }
        }
        
        return conflicts;
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 验证类型引用（复用自原有实现）
     */
    private List<ComplianceIssue> validateTypeReferences(File xmlFile, SchemaRegistry registry) {
        List<ComplianceIssue> issues = new ArrayList<>();
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            // 安全配置
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            
            // 检查Property元素的Type属性
            NodeList properties = document.getElementsByTagNameNS("*", "Property");
            for (int i = 0; i < properties.getLength(); i++) {
                Element property = (Element) properties.item(i);
                String typeName = property.getAttribute("Type");
                
                if (typeName != null && !typeName.isEmpty() && !isBuiltInType(typeName)) {
                    if (!registry.isTypeExists(typeName)) {
                        issues.add(new ComplianceIssue(
                            ComplianceErrorType.TYPE_NOT_EXIST,
                            "Type '" + typeName + "' is not defined in any schema",
                            null,
                            xmlFile.getAbsolutePath(),
                            ComplianceIssue.Severity.ERROR
                        ));
                    }
                }
            }
            
            // 检查NavigationProperty元素的Type属性
            NodeList navProperties = document.getElementsByTagNameNS("*", "NavigationProperty");
            for (int i = 0; i < navProperties.getLength(); i++) {
                Element navProperty = (Element) navProperties.item(i);
                String typeName = navProperty.getAttribute("Type");
                
                if (typeName != null && !typeName.isEmpty()) {
                    String actualTypeName = extractTypeFromCollection(typeName);
                    if (!isBuiltInType(actualTypeName) && !registry.isTypeExists(actualTypeName)) {
                        issues.add(new ComplianceIssue(
                            ComplianceErrorType.TYPE_NOT_EXIST,
                            "Type '" + actualTypeName + "' is not defined in any schema",
                            null,
                            xmlFile.getAbsolutePath(),
                            ComplianceIssue.Severity.ERROR
                        ));
                    }
                }
            }
            
        } catch (Exception e) {
            issues.add(createComplianceIssue(
                "Failed to validate type references: " + e.getMessage(),
                xmlFile.getAbsolutePath(),
                ComplianceIssue.Severity.ERROR
            ));
        }
        
        return issues;
    }
    
    /**
     * 验证继承关系（复用自原有实现）
     */
    private List<ComplianceIssue> validateInheritanceRelations(File xmlFile, SchemaRegistry registry) {
        List<ComplianceIssue> issues = new ArrayList<>();
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            // 安全配置
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            
            // 获取当前文件的Schema命名空间
            NodeList schemas = document.getElementsByTagNameNS("*", "Schema");
            String currentNamespace = "";
            if (schemas.getLength() > 0) {
                Element schema = (Element) schemas.item(0);
                currentNamespace = schema.getAttribute("Namespace");
            }
            
            // 检查EntityType的继承关系
            NodeList entityTypes = document.getElementsByTagNameNS("*", "EntityType");
            for (int i = 0; i < entityTypes.getLength(); i++) {
                Element entityType = (Element) entityTypes.item(i);
                String typeName = entityType.getAttribute("Name");
                String baseType = entityType.getAttribute("BaseType");
                
                if (baseType != null && !baseType.isEmpty()) {
                    String fullTypeName = currentNamespace + "." + typeName;
                    if (!registry.isValidBaseType(fullTypeName, baseType)) {
                        ComplianceErrorType errorType = determineInheritanceErrorType(baseType, registry);
                        String message = determineInheritanceErrorMessage(typeName, baseType, errorType);
                        
                        issues.add(new ComplianceIssue(
                            errorType,
                            message,
                            null,
                            xmlFile.getAbsolutePath(),
                            ComplianceIssue.Severity.ERROR
                        ));
                    }
                }
            }
            
            // 检查ComplexType的继承关系
            NodeList complexTypes = document.getElementsByTagNameNS("*", "ComplexType");
            for (int i = 0; i < complexTypes.getLength(); i++) {
                Element complexType = (Element) complexTypes.item(i);
                String typeName = complexType.getAttribute("Name");
                String baseType = complexType.getAttribute("BaseType");
                
                if (baseType != null && !baseType.isEmpty()) {
                    String fullTypeName = currentNamespace + "." + typeName;
                    if (!registry.isValidBaseType(fullTypeName, baseType)) {
                        ComplianceErrorType errorType = determineInheritanceErrorType(baseType, registry);
                        String message = determineInheritanceErrorMessage(typeName, baseType, errorType);
                        
                        issues.add(new ComplianceIssue(
                            errorType,
                            message,
                            null,
                            xmlFile.getAbsolutePath(),
                            ComplianceIssue.Severity.ERROR
                        ));
                    }
                }
            }
            
        } catch (Exception e) {
            issues.add(createComplianceIssue(
                "Failed to validate inheritance relations: " + e.getMessage(),
                xmlFile.getAbsolutePath(),
                ComplianceIssue.Severity.ERROR
            ));
        }
        
        return issues;
    }
    
    /**
     * 创建基础的ValidationEngine
     */
    private ValidationEngine createBasicValidationEngine() {
        // 返回一个简单的ValidationEngine实现，避免依赖不存在的类
        return new DefaultValidationEngineImpl();
    }
    
    /**
     * 解析XML文件中的Schema
     */
    @SuppressWarnings("unused")
    private List<CsdlSchema> parseSchemas(File xmlFile) {
        // 简化实现，返回null让现有逻辑处理
        return null;
    }
    
    /**
     * 检查是否是内置类型
     */
    private boolean isBuiltInType(String typeName) {
        return typeName.startsWith("Edm.") || 
               typeName.equals("String") || 
               typeName.equals("Int32") ||
               typeName.equals("Boolean") ||
               typeName.equals("DateTimeOffset") ||
               typeName.equals("Guid") ||
               typeName.equals("Decimal") ||
               typeName.equals("Double") ||
               typeName.equals("Single") ||
               typeName.equals("Byte") ||
               typeName.equals("SByte") ||
               typeName.equals("Int16") ||
               typeName.equals("Int64") ||
               typeName.equals("Binary") ||
               typeName.equals("Date") ||
               typeName.equals("TimeOfDay") ||
               typeName.equals("Duration");
    }
    
    /**
     * 从Collection(TypeName)格式中提取类型名
     */
    private String extractTypeFromCollection(String typeName) {
        if (typeName.startsWith("Collection(") && typeName.endsWith(")")) {
            return typeName.substring(11, typeName.length() - 1);
        }
        return typeName;
    }
    
    /**
     * 根据基类型确定继承错误的类型
     */
    private ComplianceErrorType determineInheritanceErrorType(String baseType, SchemaRegistry registry) {
        if (!registry.isTypeExists(baseType)) {
            return ComplianceErrorType.SCHEMA_DEPENDENCY_ERROR;
        }
        return ComplianceErrorType.INVALID_INHERITANCE_HIERARCHY;
    }
    
    /**
     * 根据错误类型生成适当的错误消息
     */
    private String determineInheritanceErrorMessage(String typeName, String baseType, ComplianceErrorType errorType) {
        if (errorType == ComplianceErrorType.SCHEMA_DEPENDENCY_ERROR) {
            return "Schema dependency error: Type '" + typeName + "' references non-existent base type '" + baseType + "'";
        } else {
            return "Invalid inheritance hierarchy: Type '" + typeName + "' cannot inherit from '" + baseType + "'";
        }
    }
    
    /**
     * 创建ComplianceIssue
     */
    private ComplianceIssue createComplianceIssue(String message, String source, ComplianceIssue.Severity severity) {
        return new ComplianceIssue(
            ComplianceErrorType.VALIDATION_ERROR,
            message,
            null,
            source,
            severity
        );
    }
    
    /**
     * 创建错误结果
     */
    private ComplianceResult createErrorResult(String message, String source) {
        List<ComplianceIssue> issues = new ArrayList<>();
        issues.add(createComplianceIssue(message, source, ComplianceIssue.Severity.ERROR));
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("validationTimeMs", 0L);
        metadata.put("errorType", "system-error");
        
        return new ComplianceResult(
            false,
            issues,
            new HashSet<>(),
            metadata,
            source,
            0L
        );
    }
    
    /**
     * 使用自定义源信息创建结果
     */
    private ComplianceResult createResultWithCustomSource(ComplianceResult original, String newSource) {
        Map<String, Object> newMetadata = new HashMap<>(original.getMetadata());
//        newMetadata.put("originalSource", original.getSourcePath());
        newMetadata.put("customSource", newSource);

        return new ComplianceResult(
            original.isCompliant(),
            original.getIssues(),
            original.getReferencedNamespaces(),
            newMetadata,
            newSource,
            original.getValidationTimeMs()
        );
    }
    
    // ==================== 内部类 ====================

    /**
     * Schema信息类 - 用于命名空间管理
     */
    private static class SchemaInfo {
        private final String namespace;
        private final String filePath;
        private final Set<String> types;
        
        public SchemaInfo(String namespace, String filePath, Set<String> types) {
            this.namespace = namespace;
            this.filePath = filePath;
            this.types = types != null ? types : new HashSet<>();
        }
        
        public String getNamespace() { return namespace; }
        public String getFilePath() { return filePath; }
        public Set<String> getTypes() { return types; }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof SchemaInfo)) return false;
            SchemaInfo other = (SchemaInfo) obj;
            return namespace.equals(other.namespace) && filePath.equals(other.filePath);
        }
        
        @Override
        public int hashCode() {
            return namespace.hashCode() * 31 + filePath.hashCode();
        }
    }
}
