package org.apache.olingo.schema.processor.repository;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * XML文件仓库接口，用于管理和加载XML文件
 */
public interface XmlFileRepository {
    
    /**
     * 从指定目录加载所有XML文件
     * @param rootPath 根目录路径
     * @return 加载结果
     */
    LoadResult loadFromDirectory(Path rootPath);
    
    /**
     * 获取所有已加载的XML文件路径
     * @return XML文件路径集合
     */
    Set<Path> getAllXmlFiles();
    
    /**
     * 获取指定XML文件的内容
     * @param filePath 文件路径
     * @return 文件内容，如果文件不存在返回null
     */
    String getXmlContent(Path filePath);
    
    /**
     * 获取XML文件到Schema namespace的映射
     * @return 文件路径到Schema namespace集合的映射
     */
    Map<Path, Set<String>> getFileToNamespaceMapping();
    
    /**
     * 获取Schema namespace到XML文件的映射
     * @return namespace到文件路径集合的映射
     */
    Map<String, Set<Path>> getNamespaceToFileMapping();
    
    /**
     * 检查指定namespace是否在任何XML文件中定义
     * @param namespace 要检查的namespace
     * @return 如果存在返回true
     */
    boolean isNamespaceDefined(String namespace);
    
    /**
     * 获取定义了指定namespace的所有XML文件
     * @param namespace 要查找的namespace
     * @return 定义了该namespace的文件路径集合
     */
    Set<Path> getFilesDefiningNamespace(String namespace);
    
    /**
     * 加载结果
     */
    class LoadResult {
        private final boolean success;
        private final List<String> errors;
        private final List<String> warnings;
        private final int totalFiles;
        private final int successfulFiles;
        
        public LoadResult(boolean success, List<String> errors, List<String> warnings, 
                         int totalFiles, int successfulFiles) {
            this.success = success;
            this.errors = errors;
            this.warnings = warnings;
            this.totalFiles = totalFiles;
            this.successfulFiles = successfulFiles;
        }
        
        public boolean isSuccess() { return success; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        public int getTotalFiles() { return totalFiles; }
        public int getSuccessfulFiles() { return successfulFiles; }
    }
}
