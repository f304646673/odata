package org.apache.olingo.compliance.validation.core;

import org.apache.olingo.compliance.file.ComplianceErrorType;
import org.apache.olingo.compliance.file.ComplianceIssue;
import org.apache.olingo.compliance.file.ModernXmlFileComplianceValidator;
import org.apache.olingo.compliance.file.XmlComplianceResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * 合规性验证管理器，负责协调知识库构建、上下文管理和验证过程。
 * 这是整个合规性辅助判断系统的入口点。
 */
public class ComplianceValidationManager {
    
    private ComplianceKnowledgeBase globalKnowledgeBase;
    private final ComplianceKnowledgeBaseLoader loader;
    private final ValidationConfigurationManager configManager;
    
    public ComplianceValidationManager() {
        this.loader = new ComplianceKnowledgeBaseLoader();
        this.configManager = new ValidationConfigurationManager();
        this.globalKnowledgeBase = new ComplianceKnowledgeBase.Builder().build(); // 空的初始知识库
    }
    
    /**
     * 从指定目录加载已知的XML模式文件来构建全局知识库
     */
    public ComplianceValidationManager loadKnowledgeBase(String schemaDirectory) throws IOException {
        List<File> schemaFiles = findSchemaFiles(schemaDirectory);
        this.globalKnowledgeBase = loader.loadFromFiles(schemaFiles);
        return this;
    }
    
    /**
     * 从单个文件加载知识库
     */
    public ComplianceValidationManager loadKnowledgeBase(File schemaFile) throws IOException {
        this.globalKnowledgeBase = loader.loadFromFile(schemaFile);
        return this;
    }
    
    /**
     * 合并多个知识库
     */
    public ComplianceValidationManager mergeKnowledgeBase(ComplianceKnowledgeBase additionalKnowledgeBase) {
        this.globalKnowledgeBase = loader.merge(this.globalKnowledgeBase, additionalKnowledgeBase);
        return this;
    }
    
    /**
     * 创建新的验证上下文
     */
    public ComplianceContext createValidationContext() {
        return new ComplianceContext(globalKnowledgeBase);
    }
    
    /**
     * 验证单个XML文件
     */
    public ComplianceContext.ValidationResult validateFile(String filePath) throws IOException {
        ComplianceContext context = createValidationContext();
        return validateFile(filePath, context);
    }
    
    /**
     * 使用指定上下文验证单个XML文件
     */
    public ComplianceContext.ValidationResult validateFile(String filePath, ComplianceContext context) throws IOException {
        long startTime = System.currentTimeMillis();
        
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                throw new IOException("File not found: " + filePath);
            }
            
            // 使用现有的验证器进行验证
            ModernXmlFileComplianceValidator validator = new ModernXmlFileComplianceValidator();
            XmlComplianceResult validationResult = validator.validateFile(file);
            List<ComplianceIssue> issues = validationResult.getIssues();
            
            boolean isValid = issues.stream().noneMatch(issue -> 
                issue.getErrorType() == ComplianceErrorType.VALIDATION_ERROR ||
                issue.getErrorType() == ComplianceErrorType.INVALID_BASE_TYPE
            );
            
            Set<ComplianceIssue> issueSet = ConcurrentHashMap.newKeySet();
            issueSet.addAll(issues);
            
            long endTime = System.currentTimeMillis();
            
            ComplianceContext.ValidationResult result = new ComplianceContext.ValidationResult(
                filePath, isValid, issueSet, endTime - startTime
            );
            
            context.addValidationResult(result);
            context.markFileProcessed(filePath);
            
            // 如果验证通过，尝试从文件中提取类型信息并添加到临时状态
            if (isValid) {
                extractAndAddTypeInformation(file, context);
            }
            
            return result;
            
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            
            ComplianceIssue errorIssue = new ComplianceIssue(
                ComplianceErrorType.VALIDATION_ERROR,
                "Validation failed: " + e.getMessage(),
                null,
                filePath,
                ComplianceIssue.Severity.ERROR
            );
            
            Set<ComplianceIssue> errorSet = ConcurrentHashMap.newKeySet();
            errorSet.add(errorIssue);
            
            ComplianceContext.ValidationResult result = new ComplianceContext.ValidationResult(
                filePath, false, errorSet, endTime - startTime
            );
            
            context.addValidationResult(result);
            
            return result;
        }
    }
    
    /**
     * 批量验证多个文件
     */
    public ComplianceContext validateFiles(List<String> filePaths) throws IOException {
        ComplianceContext context = createValidationContext();
        
        for (String filePath : filePaths) {
            validateFile(filePath, context);
        }
        
        return context;
    }
    
    /**
     * 验证目录中的所有XML文件
     */
    public ComplianceContext validateDirectory(String directoryPath) throws IOException {
        List<String> xmlFiles = findXmlFiles(directoryPath);
        return validateFiles(xmlFiles);
    }
    
    /**
     * 获取全局知识库
     */
    public ComplianceKnowledgeBase getGlobalKnowledgeBase() {
        return globalKnowledgeBase;
    }
    
    /**
     * 获取配置管理器
     */
    public ValidationConfigurationManager getConfigurationManager() {
        return configManager;
    }
    
    /**
     * 从验证过的文件中提取类型信息
     */
    private void extractAndAddTypeInformation(File xmlFile, ComplianceContext context) {
        try {
            // 这里应该实现XML解析逻辑来提取类型信息
            // 为了示例，我们创建一个简单的占位符实现
            
            String fileName = xmlFile.getName();
            String namespace = extractNamespaceFromFile(xmlFile);
            
            if (namespace != null && !context.isNamespaceRegistered(namespace)) {
                // 添加新发现的命名空间
                context.addTemporaryNamespaceAlias(fileName.replace(".xml", ""), namespace);
                
                // 这里可以进一步解析XML来提取具体的类型定义
                // 由于这是一个框架示例，我们暂时跳过具体的XML解析实现
            }
            
        } catch (Exception e) {
            // 记录错误但不影响主要验证流程
            context.setProperty("extraction_error_" + xmlFile.getName(), e.getMessage());
        }
    }
    
    /**
     * 从XML文件中提取命名空间（简化实现）
     */
    private String extractNamespaceFromFile(File xmlFile) {
        try {
            String content = Files.readString(xmlFile.toPath());
            // 简化的命名空间提取逻辑
            if (content.contains("xmlns")) {
                // 这里应该使用更sophisticated的XML解析
                return "extracted.namespace"; // 占位符
            }
        } catch (IOException e) {
            // 忽略读取错误
        }
        return null;
    }
    
    /**
     * 查找目录中的XML文件
     */
    private List<String> findXmlFiles(String directoryPath) throws IOException {
        List<String> xmlFiles = new ArrayList<>();
        
        try (Stream<Path> paths = Files.walk(Paths.get(directoryPath))) {
            paths.filter(Files::isRegularFile)
                 .filter(path -> path.toString().toLowerCase().endsWith(".xml"))
                 .forEach(path -> xmlFiles.add(path.toString()));
        }
        
        return xmlFiles;
    }
    
    /**
     * 查找模式文件
     */
    private List<File> findSchemaFiles(String directoryPath) throws IOException {
        List<File> schemaFiles = new ArrayList<>();
        
        try (Stream<Path> paths = Files.walk(Paths.get(directoryPath))) {
            paths.filter(Files::isRegularFile)
                 .filter(path -> {
                     String fileName = path.getFileName().toString().toLowerCase();
                     return fileName.endsWith(".xml") || fileName.endsWith(".xsd");
                 })
                 .forEach(path -> schemaFiles.add(path.toFile()));
        }
        
        return schemaFiles;
    }
    
    /**
     * 配置管理器
     */
    public static class ValidationConfigurationManager {
        private boolean strictMode = true;
        private boolean enableCaching = true;
        private int maxCacheSize = 1000;
        private boolean enableStatistics = true;
        
        public boolean isStrictMode() { return strictMode; }
        public ValidationConfigurationManager setStrictMode(boolean strictMode) {
            this.strictMode = strictMode;
            return this;
        }
        
        public boolean isEnableCaching() { return enableCaching; }
        public ValidationConfigurationManager setEnableCaching(boolean enableCaching) {
            this.enableCaching = enableCaching;
            return this;
        }
        
        public int getMaxCacheSize() { return maxCacheSize; }
        public ValidationConfigurationManager setMaxCacheSize(int maxCacheSize) {
            this.maxCacheSize = maxCacheSize;
            return this;
        }
        
        public boolean isEnableStatistics() { return enableStatistics; }
        public ValidationConfigurationManager setEnableStatistics(boolean enableStatistics) {
            this.enableStatistics = enableStatistics;
            return this;
        }
    }
}
