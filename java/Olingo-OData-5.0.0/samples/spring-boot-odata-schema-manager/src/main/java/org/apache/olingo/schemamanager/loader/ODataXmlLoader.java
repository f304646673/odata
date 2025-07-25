package org.apache.olingo.schemamanager.loader;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * OData XML加载器接口
 * 负责从不同来源加载和解析OData XML文件
 */
public interface ODataXmlLoader {
    
    /**
     * 从指定目录递归加载所有OData XML文件
     * @param directoryPath 目录路径
     * @return 加载结果
     */
    LoadResult loadFromDirectory(String directoryPath);
    
    /**
     * 从classpath目录递归加载XML文件
     * @param classpathDirectory classpath目录路径
     * @return 加载结果
     */
    LoadResult loadSingleFileFromResource(String classpathDirectory);
    
    /**
     * 加载单个XML文件
     * @param filePath 文件路径
     * @return 加载结果
     */
    LoadResult loadSingleFile(String filePath);
    
    /**
     * 从输入流加载XML文件
     * @param inputStream 输入流
     * @param sourceName 源名称（用于识别）
     * @return 加载结果
     */
    LoadResult loadFromInputStream(InputStream inputStream, String sourceName);
    
    /**
     * 获取所有已加载的文件信息
     * @return 文件信息映射
     */
    Map<String, XmlFileInfo> getLoadedFiles();
    
    /**
     * 清除所有已加载的数据
     */
    void clear();
    
    /**
     * XML文件信息类
     */
    class XmlFileInfo {
        private final String filePath;
        private final String namespace;
        private final List<String> dependencies;
        private final boolean loadSuccess;
        private final String errorMessage;
        
        public XmlFileInfo(String filePath, String namespace, List<String> dependencies, boolean loadSuccess, String errorMessage) {
            this.filePath = filePath;
            this.namespace = namespace;
            this.dependencies = dependencies;
            this.loadSuccess = loadSuccess;
            this.errorMessage = errorMessage;
        }
        
        // Getters
        public String getFilePath() { return filePath; }
        public String getNamespace() { return namespace; }
        public List<String> getDependencies() { return dependencies; }
        public boolean isLoadSuccess() { return loadSuccess; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    /**
     * 加载结果类
     */
    class LoadResult {
        private final int totalFiles;
        private final int successfulFiles;
        private final int failedFiles;
        private final List<String> errorMessages;
        private final Map<String, XmlFileInfo> loadedFiles;
        
        public LoadResult(int totalFiles, int successfulFiles, int failedFiles, 
                         List<String> errorMessages, Map<String, XmlFileInfo> loadedFiles) {
            this.totalFiles = totalFiles;
            this.successfulFiles = successfulFiles;
            this.failedFiles = failedFiles;
            this.errorMessages = errorMessages;
            this.loadedFiles = loadedFiles;
        }
        
        // Getters
        public int getTotalFiles() { return totalFiles; }
        public int getSuccessfulFiles() { return successfulFiles; }
        public int getFailedFiles() { return failedFiles; }
        public List<String> getErrorMessages() { return errorMessages; }
        public Map<String, XmlFileInfo> getLoadedFiles() { return loadedFiles; }
    }
}
