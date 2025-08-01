package org.apache.olingo.schema.repository.loader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.edm.xml.XMLMetadata;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.schema.repository.model.SchemaRepositoryContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Schema Repository的加载器
 * 支持从多种数据源加载Schema
 */
public class SchemaRepositoryLoader {
    
    private static final Logger LOG = LoggerFactory.getLogger(SchemaRepositoryLoader.class);
    
    private final SchemaRepositoryContext context;
    private final ExecutorService executorService;
    private final SchemaValidator validator;
    private final ODataClient oDataClient;
    
    /**
     * 构造函数
     */
    public SchemaRepositoryLoader(SchemaRepositoryContext context) {
        this.context = context;
        this.executorService = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors());
        this.validator = new DefaultSchemaValidator();
        this.oDataClient = ODataClientFactory.getClient();
    }
    
    /**
     * 自定义验证器的构造函数
     */
    public SchemaRepositoryLoader(SchemaRepositoryContext context, SchemaValidator validator) {
        this.context = context;
        this.executorService = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors());
        this.validator = validator != null ? validator : new DefaultSchemaValidator();
        this.oDataClient = ODataClientFactory.getClient();
    }
    
    /**
     * 从XML文件加载Schema
     */
    public void loadFromFile(File file) throws SchemaLoadException {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new SchemaLoadException("Invalid file: " + file);
        }
        
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            loadFromInputStream(inputStream, file.getName());
        } catch (IOException e) {
            throw new SchemaLoadException("Failed to read file: " + file.getAbsolutePath(), e);
        }
    }
    
    /**
     * 从XML文件路径加载Schema
     */
    public void loadFromFile(String filePath) throws SchemaLoadException {
        loadFromFile(new File(filePath));
    }
    
    /**
     * 从Path加载Schema
     */
    public void loadFromPath(Path path) throws SchemaLoadException {
        if (path == null || !Files.exists(path) || !Files.isRegularFile(path)) {
            throw new SchemaLoadException("Invalid path: " + path);
        }
        
        try (InputStream inputStream = Files.newInputStream(path)) {
            loadFromInputStream(inputStream, path.getFileName().toString());
        } catch (IOException e) {
            throw new SchemaLoadException("Failed to read path: " + path.toString(), e);
        }
    }
    
    /**
     * 从InputStream加载Schema
     */
    public void loadFromInputStream(InputStream inputStream, String sourceName) throws SchemaLoadException {
        if (inputStream == null) {
            throw new SchemaLoadException("InputStream cannot be null");
        }
        
        try {
            LOG.info("Loading schema from: {}", sourceName != null ? sourceName : "InputStream");
            
            // 使用OData客户端解析XML metadata
            XMLMetadata xmlMetadata = oDataClient.getDeserializer(ContentType.APPLICATION_XML)
                .toMetadata(inputStream);
            
            List<CsdlSchema> schemas = xmlMetadata.getSchemas();
            
            if (schemas == null || schemas.isEmpty()) {
                throw new SchemaLoadException("No schemas found in: " + sourceName);
            }
            
            // 验证和加载每个Schema
            for (CsdlSchema schema : schemas) {
                loadSchema(schema, sourceName);
            }
            
            LOG.info("Successfully loaded {} schemas from: {}", schemas.size(), sourceName);
            
        } catch (Exception e) {
            throw new SchemaLoadException("Failed to parse schema from: " + sourceName, e);
        }
    }
    
    /**
     * 加载单个Schema
     */
    public void loadSchema(CsdlSchema schema, String sourceName) throws SchemaLoadException {
        if (schema == null) {
            throw new SchemaLoadException("Schema cannot be null");
        }
        
        if (schema.getNamespace() == null || schema.getNamespace().trim().isEmpty()) {
            throw new SchemaLoadException("Schema namespace cannot be null or empty in: " + sourceName);
        }
        
        try {
            // 验证Schema
            validator.validate(schema);
            
            // 检查是否已存在相同namespace的Schema
            if (context.containsSchema(schema.getNamespace())) {
                LOG.warn("Schema with namespace '{}' already exists, it will be replaced", schema.getNamespace());
            }
            
            // 添加到context
            context.addSchema(schema);
            
            LOG.debug("Loaded schema: {} from: {}", schema.getNamespace(), sourceName);
            
        } catch (SchemaValidationException e) {
            throw new SchemaLoadException("Schema validation failed for: " + sourceName, e);
        }
    }
    
    /**
     * 批量从文件目录加载Schema
     */
    public void loadFromDirectory(Path directory, boolean recursive) throws SchemaLoadException {
        if (directory == null || !Files.exists(directory) || !Files.isDirectory(directory)) {
            throw new SchemaLoadException("Invalid directory: " + directory);
        }
        
        List<Path> xmlFiles = new ArrayList<>();
        try {
            Files.walk(directory, recursive ? Integer.MAX_VALUE : 1)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().toLowerCase().endsWith(".xml"))
                .forEach(xmlFiles::add);
        } catch (IOException e) {
            throw new SchemaLoadException("Failed to scan directory: " + directory, e);
        }
        
        if (xmlFiles.isEmpty()) {
            LOG.warn("No XML files found in directory: {}", directory);
            return;
        }
        
        LOG.info("Found {} XML files in directory: {}", xmlFiles.size(), directory);
        
        // 并行加载文件
        loadFilesInParallel(xmlFiles);
    }
    
    /**
     * 并行加载多个文件
     */
    public void loadFilesInParallel(List<Path> files) throws SchemaLoadException {
        if (files == null || files.isEmpty()) {
            return;
        }
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        List<SchemaLoadException> exceptions = new ArrayList<>();
        
        for (Path file : files) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    loadFromPath(file);
                } catch (SchemaLoadException e) {
                    synchronized (exceptions) {
                        exceptions.add(e);
                    }
                }
            }, executorService);
            
            futures.add(future);
        }
        
        // 等待所有任务完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0]));
        
        try {
            allFutures.get(30, TimeUnit.SECONDS); // 30秒超时
        } catch (Exception e) {
            throw new SchemaLoadException("Failed to load files in parallel", e);
        }
        
        // 检查是否有异常
        if (!exceptions.isEmpty()) {
            SchemaLoadException firstException = exceptions.get(0);
            for (int i = 1; i < exceptions.size(); i++) {
                firstException.addSuppressed(exceptions.get(i));
            }
            throw firstException;
        }
    }
    
    /**
     * 重新加载所有Schema（清空后重新加载）
     */
    public void reloadAll(List<Path> files) throws SchemaLoadException {
        context.clear();
        loadFilesInParallel(files);
    }
    
    /**
     * 获取Context
     */
    public SchemaRepositoryContext getContext() {
        return context;
    }
    
    /**
     * 关闭资源
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Schema验证器接口
     */
    public interface SchemaValidator {
        void validate(CsdlSchema schema) throws SchemaValidationException;
    }
    
    /**
     * 默认Schema验证器实现
     */
    public static class DefaultSchemaValidator implements SchemaValidator {
        
        @Override
        public void validate(CsdlSchema schema) throws SchemaValidationException {
            if (schema == null) {
                throw new SchemaValidationException("Schema cannot be null");
            }
            
            if (schema.getNamespace() == null || schema.getNamespace().trim().isEmpty()) {
                throw new SchemaValidationException("Schema namespace cannot be null or empty");
            }
            
            // 基本验证通过
            LOG.debug("Schema validation passed for namespace: {}", schema.getNamespace());
        }
    }
    
    /**
     * Schema加载异常
     */
    public static class SchemaLoadException extends Exception {
        private static final long serialVersionUID = 1L;
        
        public SchemaLoadException(String message) {
            super(message);
        }
        
        public SchemaLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * Schema验证异常
     */
    public static class SchemaValidationException extends Exception {
        private static final long serialVersionUID = 1L;
        
        public SchemaValidationException(String message) {
            super(message);
        }
        
        public SchemaValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
