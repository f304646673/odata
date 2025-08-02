package org.apache.olingo.compliance.validator.directory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.olingo.compliance.core.model.ComplianceErrorType;
import org.apache.olingo.compliance.core.model.ComplianceIssue;
import org.apache.olingo.compliance.core.model.ValidationStatistics;
import org.apache.olingo.compliance.core.model.XmlComplianceResult;
import org.apache.olingo.compliance.engine.core.SchemaExtractor;
import org.apache.olingo.compliance.engine.core.SchemaRegistry;
import org.apache.olingo.compliance.validator.file.RegistryAwareXmlValidator;

/**
 * 目录验证管理器，负责验证多层目录结构中的OData XML文件
 * 基于Schema Registry架构，支持跨文件的类型依赖检查和继承关系验证
 */
public class DirectoryValidationManager {
    
    private final SchemaConflictDetector conflictDetector;
    private final RegistryAwareXmlValidator fileValidator;
    private final SchemaExtractor schemaExtractor;
    private final Map<String, Set<SchemaInfo>> namespaceToSchemas; // 保留用于冲突检测
    private final Map<String, SchemaInfo> fileToSchema; // 保留用于冲突检测
    
    public DirectoryValidationManager() {
        this.conflictDetector = new SchemaConflictDetector();
        this.fileValidator = new org.apache.olingo.compliance.validator.file.EnhancedRegistryAwareXmlValidator();
        this.schemaExtractor = new SchemaExtractor();
        this.namespaceToSchemas = new ConcurrentHashMap<>();
        this.fileToSchema = new ConcurrentHashMap<>();
    }
    
    /**
     * 验证整个目录结构
     * @param directoryPath 目录路径
     * @param systemRegistry 系统级Schema注册表（可选，包含外部依赖）
     */
    public DirectoryValidationResult validateDirectory(String directoryPath, SchemaRegistry systemRegistry) throws IOException {
        long startTime = System.currentTimeMillis();
        
        // 每次验证时重置冲突检测的数据结构
        namespaceToSchemas.clear();
        fileToSchema.clear();
        
        // 1. 收集所有XML文件
        List<File> xmlFiles = collectXmlFiles(directoryPath);
        
        // 2. 构建目录级Schema Registry
        SchemaRegistry directoryRegistry = buildDirectoryRegistry(xmlFiles);
        
        // 3. 合并系统级和目录级Registry
        SchemaRegistry combinedRegistry = new SchemaRegistry();
        if (systemRegistry != null) {
            combinedRegistry.merge(systemRegistry);
        }
        combinedRegistry.merge(directoryRegistry);
        
        // 4. 使用合并后的Registry验证每个文件
        Map<String, XmlComplianceResult> fileResults = new LinkedHashMap<>();
        List<ComplianceIssue> allIssues = new ArrayList<>();
        
        for (File xmlFile : xmlFiles) {
            try {
                XmlComplianceResult result = fileValidator.validateWithRegistry(xmlFile, combinedRegistry);
                fileResults.put(xmlFile.getAbsolutePath(), result);
                allIssues.addAll(result.getIssues());
            } catch (Exception e) {
                ComplianceIssue issue = new ComplianceIssue(
                    ComplianceErrorType.VALIDATION_ERROR,
                    "Failed to validate file: " + e.getMessage(),
                    null,
                    xmlFile.getAbsolutePath(),
                    ComplianceIssue.Severity.ERROR
                );
                allIssues.add(issue);
            }
        }
        
        // 5. 检测Schema冲突（保留原有逻辑用于向后兼容）
        List<ComplianceIssue> conflictIssues = conflictDetector.detectConflicts(namespaceToSchemas);
        allIssues.addAll(conflictIssues);
        
        long endTime = System.currentTimeMillis();
        
        return new DirectoryValidationResult(
            directoryPath,
            xmlFiles.size(),
            fileResults,
            conflictIssues,
            allIssues,
            createStatistics(combinedRegistry, xmlFiles.size(), endTime - startTime, fileResults),
            endTime - startTime
        );
    }
    
    /**
     * 验证整个目录结构（使用默认的空系统Registry）
     */
    public DirectoryValidationResult validateDirectory(String directoryPath) throws IOException {
        return validateDirectory(directoryPath, null);
    }
    
    /**
     * 收集目录中的所有XML文件
     */
    private List<File> collectXmlFiles(String directoryPath) throws IOException {
        List<File> xmlFiles = new ArrayList<>();
        
        try (Stream<Path> paths = Files.walk(Paths.get(directoryPath))) {
            paths.filter(Files::isRegularFile)
                 .filter(path -> path.toString().toLowerCase().endsWith(".xml"))
                 .sorted() // 确保处理顺序一致
                 .forEach(path -> xmlFiles.add(path.toFile()));
        }
        
        return xmlFiles;
    }
    
    /**
     * 只收集指定目录中的XML文件（不递归子目录）
     */
    private List<File> collectXmlFilesNonRecursive(String directoryPath) throws IOException {
        List<File> xmlFiles = new ArrayList<>();
        Path dirPath = Paths.get(directoryPath);
        
        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
            return xmlFiles;
        }
        
        try (Stream<Path> paths = Files.list(dirPath)) {
            paths.filter(Files::isRegularFile)
                 .filter(path -> path.toString().toLowerCase().endsWith(".xml"))
                 .sorted() // 确保处理顺序一致
                 .forEach(path -> xmlFiles.add(path.toFile()));
        }
        
        return xmlFiles;
    }
    
    /**
     * 验证单个目录（不递归子目录）
     * @param directoryPath 目录路径
     * @param systemRegistry 系统级Schema注册表（可选，包含外部依赖）
     */
    public DirectoryValidationResult validateSingleDirectory(String directoryPath, SchemaRegistry systemRegistry) throws IOException {
        long startTime = System.currentTimeMillis();
        
        // 每次验证时重置冲突检测的数据结构
        namespaceToSchemas.clear();
        fileToSchema.clear();
        
        // 1. 收集指定目录中的XML文件（不递归）
        List<File> xmlFiles = collectXmlFilesNonRecursive(directoryPath);
        
        // 2. 构建目录级Schema Registry
        SchemaRegistry directoryRegistry = buildDirectoryRegistry(xmlFiles);
        
        // 3. 合并系统级和目录级Registry
        SchemaRegistry combinedRegistry = new SchemaRegistry();
        if (systemRegistry != null) {
            combinedRegistry.merge(systemRegistry);
        }
        combinedRegistry.merge(directoryRegistry);
        
        // 4. 使用合并后的Registry验证每个文件
        Map<String, XmlComplianceResult> fileResults = new LinkedHashMap<>();
        List<ComplianceIssue> allIssues = new ArrayList<>();
        
        for (File xmlFile : xmlFiles) {
            try {
                XmlComplianceResult result = fileValidator.validateWithRegistry(xmlFile, combinedRegistry);
                fileResults.put(xmlFile.getAbsolutePath(), result);
                allIssues.addAll(result.getIssues());
            } catch (Exception e) {
                ComplianceIssue issue = new ComplianceIssue(
                    ComplianceErrorType.VALIDATION_ERROR,
                    "Failed to validate file: " + e.getMessage(),
                    null,
                    xmlFile.getAbsolutePath(),
                    ComplianceIssue.Severity.ERROR
                );
                allIssues.add(issue);
            }
        }
        
        // 5. 检测Schema冲突（仅针对当前目录）
        List<ComplianceIssue> conflictIssues = conflictDetector.detectConflicts(namespaceToSchemas);
        allIssues.addAll(conflictIssues);
        
        long endTime = System.currentTimeMillis();
        
        return new DirectoryValidationResult(
            directoryPath,
            xmlFiles.size(),
            fileResults,
            conflictIssues,
            allIssues,
            createStatistics(combinedRegistry, xmlFiles.size(), endTime - startTime, fileResults),
            endTime - startTime
        );
    }
    
    /**
     * 验证单个目录（不递归子目录，使用默认的空系统Registry）
     */
    public DirectoryValidationResult validateSingleDirectory(String directoryPath) throws IOException {
        return validateSingleDirectory(directoryPath, null);
    }
    
    /**
     * 构建目录级Schema Registry
     */
    private SchemaRegistry buildDirectoryRegistry(List<File> xmlFiles) {
        SchemaRegistry registry = new SchemaRegistry();
        
        for (File xmlFile : xmlFiles) {
            try {
                List<SchemaRegistry.SchemaDefinition> schemas = schemaExtractor.extractSchemas(xmlFile);
                for (SchemaRegistry.SchemaDefinition schema : schemas) {
                    registry.registerSchema(schema);
                    
                    // 同时维护旧的数据结构用于冲突检测
                    SchemaInfo oldSchema = new SchemaInfo(
                        schema.getNamespace(),
                        schema.getAlias(),
                        schema.getFilePath(),
                        convertTypesToElementNames(schema.getTypes())
                    );
                    
                    fileToSchema.put(xmlFile.getAbsolutePath(), oldSchema);
                    namespaceToSchemas.computeIfAbsent(schema.getNamespace(), k -> ConcurrentHashMap.newKeySet())
                                     .add(oldSchema);
                }
            } catch (Exception e) {
                System.err.println("Warning: Failed to extract schema from " + xmlFile.getName() + ": " + e.getMessage());
            }
        }
        
        return registry;
    }
    
    /**
     * 将TypeDefinition列表转换为元素名称集合（用于向后兼容）
     */
    private Set<String> convertTypesToElementNames(List<SchemaRegistry.TypeDefinition> types) {
        Set<String> elementNames = new HashSet<>();
        for (SchemaRegistry.TypeDefinition type : types) {
            elementNames.add(type.getKind() + ":" + type.getName());
        }
        return elementNames;
    }
    
    /**
     * 创建统计信息
     */
    private ValidationStatistics  createStatistics(SchemaRegistry registry, int totalFiles, 
                                                                   long validationTime, Map<String, XmlComplianceResult> fileResults) {
        SchemaRegistry.RegistryStatistics regStats = registry.getStatistics();
        long validFiles = fileResults.values().stream().mapToLong(r -> r.isCompliant() ? 1 : 0).sum();
        
        return new ValidationStatistics (
            regStats.getTotalTypes(),
            regStats.getEntityTypes(),
            regStats.getComplexTypes(),
            regStats.getNamespaceCount(),
            totalFiles,
            totalFiles,
            validationTime,
            validFiles
        );
    }
    
    
    /**
     * Schema信息结构（保留用于向后兼容）
     */
    public static class SchemaInfo {
        private final String namespace;
        private final String alias;
        private final String filePath;
        private final Set<String> elementNames;
        
        public SchemaInfo(String namespace, String alias, String filePath, Set<String> elementNames) {
            this.namespace = namespace;
            this.alias = alias;
            this.filePath = filePath;
            this.elementNames = Collections.unmodifiableSet(new HashSet<>(elementNames));
        }
        
        // Getters
        public String getNamespace() { return namespace; }
        public String getAlias() { return alias; }
        public String getFilePath() { return filePath; }
        public Set<String> getElementNames() { return elementNames; }
        
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
            return String.format("SchemaInfo{namespace='%s', alias='%s', filePath='%s', elements=%d}",
                               namespace, alias, filePath, elementNames.size());
        }
    }
    
    /**
     * 目录验证结果
     */
    public static class DirectoryValidationResult {
        private final String directoryPath;
        private final int totalFiles;
        private final Map<String, XmlComplianceResult> validationResults;
        private final List<ComplianceIssue> conflictIssues;
        private final List<ComplianceIssue> allIssues;
        private final ValidationStatistics  statistics;
        private final long validationTimeMs;
        
        public DirectoryValidationResult(String directoryPath, int totalFiles,
                                       Map<String, XmlComplianceResult> validationResults,
                                       List<ComplianceIssue> conflictIssues,
                                       List<ComplianceIssue> allIssues,
                                       ValidationStatistics  statistics,
                                       long validationTimeMs) {
            this.directoryPath = directoryPath;
            this.totalFiles = totalFiles;
            this.validationResults = Collections.unmodifiableMap(new LinkedHashMap<>(validationResults));
            this.conflictIssues = Collections.unmodifiableList(new ArrayList<>(conflictIssues));
            this.allIssues = Collections.unmodifiableList(new ArrayList<>(allIssues));
            this.statistics = statistics;
            this.validationTimeMs = validationTimeMs;
        }
        
        // Getters
        public String getDirectoryPath() { return directoryPath; }
        public int getTotalFiles() { return totalFiles; }
        public Map<String, XmlComplianceResult> getValidationResults() { return validationResults; }
        public List<ComplianceIssue> getConflictIssues() { return conflictIssues; }
        public List<ComplianceIssue> getAllIssues() { return allIssues; }
        public ValidationStatistics  getStatistics() { return statistics; }
        public long getValidationTimeMs() { return validationTimeMs; }
        
        public boolean isValid() {
            return conflictIssues.isEmpty() && 
                   allIssues.isEmpty() && 
                   validationResults.values().stream().allMatch(XmlComplianceResult::isCompliant);
        }
        
        public int getValidFileCount() {
            return (int) validationResults.values().stream()
                                         .mapToLong(r -> r.isCompliant() ? 1 : 0)
                                         .sum();
        }
        
        public int getTotalIssueCount() {
            return allIssues.size();
        }
        
        @Override
        public String toString() {
            return String.format(
                "DirectoryValidationResult{directoryPath='%s', totalFiles=%d, validFiles=%d, " +
                "conflictIssues=%d, totalIssues=%d, validationTimeMs=%d, isValid=%s}",
                directoryPath, totalFiles, getValidFileCount(), conflictIssues.size(), 
                getTotalIssueCount(), validationTimeMs, isValid()
            );
        }
    }
}
