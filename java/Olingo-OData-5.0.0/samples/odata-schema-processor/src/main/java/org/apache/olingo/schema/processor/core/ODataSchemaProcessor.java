package org.apache.olingo.schema.processor.core;

import org.apache.olingo.schema.processor.repository.XmlFileRepository;
import org.apache.olingo.schema.processor.repository.SchemaRepository;
import org.apache.olingo.schema.processor.parser.ODataXmlParser;
import org.apache.olingo.schema.processor.validator.ODataValidator;
import org.apache.olingo.schema.processor.analyzer.DependencyAnalyzer;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * OData Schema处理器主接口
 * 整合XML文件管理、Schema解析、验证和依赖分析功能
 */
public interface ODataSchemaProcessor {
    
    /**
     * 从目录加载并处理所有OData XML文件
     * @param rootPath 根目录路径
     * @return 处理结果
     */
    ProcessingResult processDirectory(Path rootPath);
    
    /**
     * 获取XML文件仓库
     * @return XML文件仓库
     */
    XmlFileRepository getXmlFileRepository();
    
    /**
     * 获取Schema仓库
     * @return Schema仓库
     */
    SchemaRepository getSchemaRepository();
    
    /**
     * 获取XML解析器
     * @return XML解析器
     */
    ODataXmlParser getXmlParser();
    
    /**
     * 获取验证器
     * @return 验证器
     */
    ODataValidator getValidator();
    
    /**
     * 获取依赖分析器
     * @return 依赖分析器
     */
    DependencyAnalyzer getDependencyAnalyzer();
    
    /**
     * 验证所有已加载的Schema
     * @return 验证结果
     */
    ValidationResult validateAllSchemas();
    
    /**
     * 分析指定元素的依赖关系
     * @param fullyQualifiedName 元素的全限定名
     * @return 依赖分析结果
     */
    DependencyResult analyzeDependencies(String fullyQualifiedName);
    
    /**
     * 检测循环依赖
     * @return 循环依赖检测结果
     */
    CircularDependencyResult detectCircularDependencies();
    
    /**
     * 获取处理统计信息
     * @return 统计信息
     */
    ProcessingStatistics getStatistics();
    
    /**
     * 处理结果
     */
    class ProcessingResult {
        private final boolean success;
        private final List<String> errors;
        private final List<String> warnings;
        private final int totalFiles;
        private final int processedFiles;
        private final int totalSchemas;
        private final Set<String> namespaces;
        
        public ProcessingResult(boolean success, List<String> errors, List<String> warnings,
                               int totalFiles, int processedFiles, int totalSchemas, Set<String> namespaces) {
            this.success = success;
            this.errors = errors;
            this.warnings = warnings;
            this.totalFiles = totalFiles;
            this.processedFiles = processedFiles;
            this.totalSchemas = totalSchemas;
            this.namespaces = namespaces;
        }
        
        public boolean isSuccess() { return success; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        public int getTotalFiles() { return totalFiles; }
        public int getProcessedFiles() { return processedFiles; }
        public int getTotalSchemas() { return totalSchemas; }
        public Set<String> getNamespaces() { return namespaces; }
    }
    
    /**
     * 验证结果
     */
    class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;
        private final List<String> missingDependencies;
        
        public ValidationResult(boolean valid, List<String> errors, List<String> warnings,
                               List<String> missingDependencies) {
            this.valid = valid;
            this.errors = errors;
            this.warnings = warnings;
            this.missingDependencies = missingDependencies;
        }
        
        public boolean isValid() { return valid; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        public List<String> getMissingDependencies() { return missingDependencies; }
    }
    
    /**
     * 依赖分析结果
     */
    class DependencyResult {
        private final String targetElement;
        private final Set<String> directDependencies;
        private final Set<String> recursiveDependencies;
        private final Set<String> reverseDependencies;
        private final int dependencyDepth;
        
        public DependencyResult(String targetElement, Set<String> directDependencies,
                               Set<String> recursiveDependencies, Set<String> reverseDependencies,
                               int dependencyDepth) {
            this.targetElement = targetElement;
            this.directDependencies = directDependencies;
            this.recursiveDependencies = recursiveDependencies;
            this.reverseDependencies = reverseDependencies;
            this.dependencyDepth = dependencyDepth;
        }
        
        public String getTargetElement() { return targetElement; }
        public Set<String> getDirectDependencies() { return directDependencies; }
        public Set<String> getRecursiveDependencies() { return recursiveDependencies; }
        public Set<String> getReverseDependencies() { return reverseDependencies; }
        public int getDependencyDepth() { return dependencyDepth; }
    }
    
    /**
     * 循环依赖检测结果
     */
    class CircularDependencyResult {
        private final boolean hasCircularDependencies;
        private final List<List<String>> cycles;
        private final List<String> affectedElements;
        
        public CircularDependencyResult(boolean hasCircularDependencies, List<List<String>> cycles,
                                       List<String> affectedElements) {
            this.hasCircularDependencies = hasCircularDependencies;
            this.cycles = cycles;
            this.affectedElements = affectedElements;
        }
        
        public boolean hasCircularDependencies() { return hasCircularDependencies; }
        public List<List<String>> getCycles() { return cycles; }
        public List<String> getAffectedElements() { return affectedElements; }
    }
    
    /**
     * 处理统计信息
     */
    class ProcessingStatistics {
        private final int totalXmlFiles;
        private final int totalSchemas;
        private final int totalEntityTypes;
        private final int totalComplexTypes;
        private final int totalEnumTypes;
        private final int totalActions;
        private final int totalFunctions;
        private final Set<String> namespaces;
        private final long processingTimeMs;
        
        public ProcessingStatistics(int totalXmlFiles, int totalSchemas, int totalEntityTypes,
                                   int totalComplexTypes, int totalEnumTypes, int totalActions,
                                   int totalFunctions, Set<String> namespaces, long processingTimeMs) {
            this.totalXmlFiles = totalXmlFiles;
            this.totalSchemas = totalSchemas;
            this.totalEntityTypes = totalEntityTypes;
            this.totalComplexTypes = totalComplexTypes;
            this.totalEnumTypes = totalEnumTypes;
            this.totalActions = totalActions;
            this.totalFunctions = totalFunctions;
            this.namespaces = namespaces;
            this.processingTimeMs = processingTimeMs;
        }
        
        public int getTotalXmlFiles() { return totalXmlFiles; }
        public int getTotalSchemas() { return totalSchemas; }
        public int getTotalEntityTypes() { return totalEntityTypes; }
        public int getTotalComplexTypes() { return totalComplexTypes; }
        public int getTotalEnumTypes() { return totalEnumTypes; }
        public int getTotalActions() { return totalActions; }
        public int getTotalFunctions() { return totalFunctions; }
        public Set<String> getNamespaces() { return namespaces; }
        public long getProcessingTimeMs() { return processingTimeMs; }
    }
}
