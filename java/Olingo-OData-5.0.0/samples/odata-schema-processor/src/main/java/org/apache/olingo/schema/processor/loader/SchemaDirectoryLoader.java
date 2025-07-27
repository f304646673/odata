package org.apache.olingo.schema.processor.loader;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schema.processor.parser.ODataXmlParser;
import org.apache.olingo.schema.processor.repository.SchemaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Schema目录加载器
 * 递归扫描目录，加载所有XML Schema文件到仓库中
 */
public class SchemaDirectoryLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(SchemaDirectoryLoader.class);
    
    private final ODataXmlParser xmlParser;
    
    public SchemaDirectoryLoader(ODataXmlParser xmlParser) {
        this.xmlParser = xmlParser;
    }
    
    /**
     * 加载结果
     */
    public static class LoadResult {
        private final boolean success;
        private final List<Path> loadedFiles;
        private final List<CsdlSchema> schemas;
        private final List<String> errors;
        private final List<String> warnings;
        
        public LoadResult(boolean success, List<Path> loadedFiles, List<CsdlSchema> schemas, 
                         List<String> errors, List<String> warnings) {
            this.success = success;
            this.loadedFiles = new ArrayList<>(loadedFiles);
            this.schemas = new ArrayList<>(schemas);
            this.errors = new ArrayList<>(errors);
            this.warnings = new ArrayList<>(warnings);
        }
        
        public boolean isSuccess() { return success; }
        public List<Path> getLoadedFiles() { return loadedFiles; }
        public List<CsdlSchema> getSchemas() { return schemas; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        
        public int getFileCount() { return loadedFiles.size(); }
        public int getSchemaCount() { return schemas.size(); }
    }
    
    /**
     * 递归加载目录中的所有Schema文件
     */
    public LoadResult loadDirectory(Path rootDirectory) {
        List<Path> loadedFiles = new ArrayList<>();
        List<CsdlSchema> schemas = new ArrayList<>(); 
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (!Files.exists(rootDirectory)) {
            errors.add("Directory does not exist: " + rootDirectory);
            return new LoadResult(false, loadedFiles, schemas, errors, warnings);
        }
        
        if (!Files.isDirectory(rootDirectory)) {
            errors.add("Path is not a directory: " + rootDirectory);
            return new LoadResult(false, loadedFiles, schemas, errors, warnings);
        }
        
        logger.info("Starting to load schemas from directory: {}", rootDirectory);
        
        try {
            // 第一步：发现所有XML文件
            List<Path> xmlFiles = discoverXmlFiles(rootDirectory);
            logger.info("Found {} XML files", xmlFiles.size());
            
            // 第二步：解析和验证每个文件
            for (Path xmlFile : xmlFiles) {
                try {
                    loadSingleFile(xmlFile, loadedFiles, schemas, errors, warnings);
                } catch (Exception e) {
                    String error = "Failed to load file " + xmlFile + ": " + e.getMessage();
                    errors.add(error);
                    logger.error(error, e);
                }
            }
            
            // 第三步：验证Schema之间的引用关系
            validateSchemaReferences(schemas, warnings);
            
            logger.info("Schema loading completed. Loaded {} files, {} schemas, {} errors, {} warnings", 
                       loadedFiles.size(), schemas.size(), errors.size(), warnings.size());
            
        } catch (IOException e) {
            errors.add("Failed to scan directory: " + e.getMessage());
            logger.error("Failed to scan directory: " + rootDirectory, e);
        }
        
        return new LoadResult(errors.isEmpty(), loadedFiles, schemas, errors, warnings);
    }
    
    /**
     * 发现目录中的所有XML文件
     */
    private List<Path> discoverXmlFiles(Path rootDirectory) throws IOException {
        List<Path> xmlFiles = new ArrayList<>();
        
        Files.walkFileTree(rootDirectory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (isXmlFile(file)) {
                    xmlFiles.add(file);
                    logger.debug("Found XML file: {}", file);
                }
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                logger.warn("Failed to visit file: {}", file, exc);
                return FileVisitResult.CONTINUE;
            }
        });
        
        // 按文件名排序，确保一致的加载顺序
        xmlFiles.sort(Comparator.comparing(Path::getFileName));
        
        return xmlFiles;
    }
    
    /**
     * 检查是否为XML文件
     */
    private boolean isXmlFile(Path file) {
        String fileName = file.getFileName().toString().toLowerCase();
        return fileName.endsWith(".xml") && Files.isRegularFile(file);
    }
    
    /**
     * 加载单个文件
     */
    private void loadSingleFile(Path xmlFile, List<Path> loadedFiles, List<CsdlSchema> schemas, 
                               List<String> errors, List<String> warnings) {
        logger.debug("Loading file: {}", xmlFile);
        
        try {
            // 解析XML文件
            ODataXmlParser.ParseResult parseResult = xmlParser.parseSchemas(xmlFile);
            
            if (!parseResult.isSuccess()) {
                for (String error : parseResult.getErrors()) {
                    errors.add("Parse error in " + xmlFile + ": " + error);
                }
                return;
            }
            
            List<CsdlSchema> fileSchemas = parseResult.getSchemas();
            
            if (fileSchemas.isEmpty()) {
                warnings.add("No schemas found in file: " + xmlFile);
                return;
            }
            
            // 添加警告信息
            for (String warning : parseResult.getWarnings()) {
                warnings.add("Parse warning in " + xmlFile + ": " + warning);
            }
            
            // 添加到结果
            loadedFiles.add(xmlFile);
            schemas.addAll(fileSchemas);
            
            logger.debug("Successfully loaded {} schemas from {}", fileSchemas.size(), xmlFile);
            
        } catch (Exception e) {
            String error = "Failed to parse XML file " + xmlFile + ": " + e.getMessage();
            errors.add(error);
            logger.error(error, e);
        }
    }
    
    /**
     * 验证Schema之间的引用关系
     */
    private void validateSchemaReferences(List<CsdlSchema> schemas, List<String> warnings) {
        Set<String> availableNamespaces = new HashSet<>();
        
        // 收集所有可用的namespace
        for (CsdlSchema schema : schemas) {
            availableNamespaces.add(schema.getNamespace());
        }
        
        // 这里可以添加更复杂的引用验证逻辑
        // 比如检查Schema之间的循环依赖等
        
        logger.debug("Cross-schema reference validation completed. Available namespaces: {}", 
                    availableNamespaces);
    }
    
    /**
     * 加载目录到Schema仓库
     */
    public LoadResult loadDirectoryToRepository(Path rootDirectory, SchemaRepository repository) {
        LoadResult result = loadDirectory(rootDirectory);
        
        if (result.isSuccess() && !result.getSchemas().isEmpty()) {
            try {
                SchemaRepository.AddResult addResult = repository.addSchemas(result.getSchemas());
                logger.info("Added {} schemas to repository. {} successful, {} conflicts", 
                           result.getSchemas().size(), 
                           addResult.getAddedCount(), 
                           addResult.getConflictCount());
                           
                if (!addResult.getErrors().isEmpty()) {
                    for (String error : addResult.getErrors()) {
                        logger.warn("Repository add error: {}", error);
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to add schemas to repository", e);
            }
        }
        
        return result;
    }
    
    /**
     * 从Resources目录加载Schema
     */
    public LoadResult loadFromResources(String resourcePath) {
        try {
            // 获取resources目录的路径
            Path resourceDir = getResourceDirectory(resourcePath);
            
            if (resourceDir != null) {
                return loadDirectory(resourceDir);
            } else {
                List<String> errors = new ArrayList<>();
                errors.add("Resource directory not found: " + resourcePath);
                return new LoadResult(false, new ArrayList<>(), new ArrayList<>(), errors, new ArrayList<>());
            }
            
        } catch (Exception e) {
            List<String> errors = new ArrayList<>();
            errors.add("Failed to load from resources: " + e.getMessage());
            return new LoadResult(false, new ArrayList<>(), new ArrayList<>(), errors, new ArrayList<>());
        }
    }
    
    /**
     * 获取资源目录路径
     */
    private Path getResourceDirectory(String resourcePath) {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            
            // 首先尝试作为文件系统路径
            java.net.URL resource = classLoader.getResource(resourcePath);
            if (resource != null) {
                if ("file".equals(resource.getProtocol())) {
                    return Paths.get(resource.toURI());
                }
            }
            
            // 如果在开发环境中，尝试相对路径
            Path currentDir = Paths.get("").toAbsolutePath();
            Path resourceDir = currentDir.resolve("src/main/resources").resolve(resourcePath);
            if (Files.exists(resourceDir)) {
                return resourceDir;
            }
            
        } catch (java.net.URISyntaxException | java.lang.SecurityException e) {
            logger.debug("Failed to resolve resource directory: {}", resourcePath, e);
        }
        
        return null;
    }
}
