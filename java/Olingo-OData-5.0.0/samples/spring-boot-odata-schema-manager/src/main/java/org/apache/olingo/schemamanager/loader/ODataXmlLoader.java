package org.apache.olingo.schemamanager.loader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * OData XML加载器接口
 * 负责从不同来源加载和解析OData XML文件
 */
public interface ODataXmlLoader {

    /**
     * 从指定资源目录递归加载所有OData XML文件
     * @param resourceDir 资源目录路径
     * @return 加载结果
     */
    LoadResult loadFromResourceDirectory(String resourceDir);
    
    /**
     * 从classpath目录递归加载XML文件
     * @param classpathDirectory classpath目录路径
     * @return 加载结果
     */
    LoadResult loadSingleFileFromResource(String classpathDirectory);
    
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
     * XML文件信息类 - 支持多个Schema
     */
    static class XmlFileInfo {
        private final String filePath;
        private final List<SchemaInfo> schemas;
        private final boolean loadSuccess;
        private final String errorMessage;
        
        // 构造函数 - 单个Schema（向后兼容）
        public XmlFileInfo(String filePath, String namespace, List<String> dependencies, boolean loadSuccess, String errorMessage) {
            this.filePath = filePath;
            this.schemas = new ArrayList<>();
            if (namespace != null && loadSuccess) {
                this.schemas.add(new SchemaInfo(namespace, dependencies));
            }
            this.loadSuccess = loadSuccess;
            this.errorMessage = errorMessage;
        }
        
        // 构造函数 - 多个Schema
        public XmlFileInfo(String filePath, List<SchemaInfo> schemas, boolean loadSuccess, String errorMessage) {
            this.filePath = filePath;
            this.schemas = schemas != null ? new ArrayList<>(schemas) : new ArrayList<>();
            this.loadSuccess = loadSuccess;
            this.errorMessage = errorMessage;
        }
        
        // 静态工厂方法
        public static XmlFileInfo success(String filePath, List<SchemaInfo> schemas) {
            return new XmlFileInfo(filePath, schemas, true, null);
        }
        
        public static XmlFileInfo failure(String filePath, String errorMessage) {
            return new XmlFileInfo(filePath, new ArrayList<>(), false, errorMessage);
        }
        
        // Getters
        public String getFilePath() { return filePath; }
        public List<SchemaInfo> getSchemas() { return new ArrayList<>(schemas); }
        public boolean isLoadSuccess() { return loadSuccess; }
        public String getErrorMessage() { return errorMessage; }
        
        // 向后兼容方法
        @Deprecated
        public String getNamespace() { 
            return schemas.isEmpty() ? null : schemas.get(0).getNamespace(); 
        }
        
        @Deprecated
        public List<String> getDependencies() { 
            return schemas.isEmpty() ? new ArrayList<>() : schemas.get(0).getDependencies(); 
        }
        
        // 便利方法
        public boolean hasMultipleSchemas() {
            return schemas.size() > 1;
        }
        
        public int getSchemaCount() {
            return schemas.size();
        }
        
        public SchemaInfo getSchemaByNamespace(String namespace) {
            return schemas.stream()
                .filter(s -> namespace.equals(s.getNamespace()))
                .findFirst()
                .orElse(null);
        }
        
        public List<String> getAllNamespaces() {
            List<String> namespaces = new ArrayList<>();
            for (SchemaInfo schema : schemas) {
                namespaces.add(schema.getNamespace());
            }
            return namespaces;
        }
    }
    
    /**
     * Schema信息类
     */
    static class SchemaInfo {
        private final String namespace;
        private final List<String> dependencies;
        
        public SchemaInfo(String namespace, List<String> dependencies) {
            this.namespace = namespace;
            this.dependencies = dependencies != null ? new ArrayList<>(dependencies) : new ArrayList<>();
        }
        
        public String getNamespace() { return namespace; }
        public List<String> getDependencies() { return new ArrayList<>(dependencies); }
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
