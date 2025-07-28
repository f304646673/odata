package org.apache.olingo.schema.processor.loader;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;
import org.apache.olingo.schema.processor.parser.ODataXmlParser;
import org.apache.olingo.schema.processor.repository.SchemaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Schema目录加载器
 * 严格按照OData 4.0规范验证和加载所有XML Schema文件
 * 
 * 工作流程：
 * 1. 发现所有XML文件
 * 2. 解析每个文件并进行OData 4.0规范验证
 * 3. 分析文件间的依赖关系
 * 4. 检查循环依赖
 * 5. 验证依赖完整性
 * 6. 只有所有验证通过后才保存Schema和文件信息
 */
public class SchemaDirectoryLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(SchemaDirectoryLoader.class);
    
    private final ODataXmlParser xmlParser;
    private final ODataValidator validator;
    
    public SchemaDirectoryLoader(ODataXmlParser xmlParser) {
        if (xmlParser == null) {
            throw new IllegalArgumentException("XML parser cannot be null");
        }
        this.xmlParser = xmlParser;
        this.validator = new ODataValidator();
    }
    
    /**
     * 完整的加载结果，包含Schema信息和文件信息
     */
    public static class LoadResult {
        private final boolean success;
        private final List<Path> loadedFiles;
        private final List<CsdlSchema> schemas;
        private final Map<Path, FileInfo> fileInfoMap;
        private final List<String> errors;
        private final List<String> warnings;
        
        public LoadResult(boolean success, List<Path> loadedFiles, List<CsdlSchema> schemas, 
                         Map<Path, FileInfo> fileInfoMap, List<String> errors, List<String> warnings) {
            this.success = success;
            this.loadedFiles = new ArrayList<>(loadedFiles);
            this.schemas = new ArrayList<>(schemas);
            this.fileInfoMap = new HashMap<>(fileInfoMap);
            this.errors = new ArrayList<>(errors);
            this.warnings = new ArrayList<>(warnings);
        }
        
        public boolean isSuccess() { return success; }
        public List<Path> getLoadedFiles() { return new ArrayList<>(loadedFiles); }
        public List<CsdlSchema> getSchemas() { return new ArrayList<>(schemas); }
        public Map<Path, FileInfo> getFileInfoMap() { return new HashMap<>(fileInfoMap); }
        public List<String> getErrors() { return new ArrayList<>(errors); }
        public List<String> getWarnings() { return new ArrayList<>(warnings); }
        
        public int getFileCount() { return loadedFiles.size(); }
        public int getSchemaCount() { return schemas.size(); }
        
        /**
         * 获取指定文件的信息
         */
        public FileInfo getFileInfo(Path filePath) {
            return fileInfoMap.get(filePath);
        }
        
        /**
         * 获取所有已定义的命名空间
         */
        public Set<String> getAllDefinedNamespaces() {
            Set<String> namespaces = new HashSet<>();
            for (CsdlSchema schema : schemas) {
                if (schema.getNamespace() != null) {
                    namespaces.add(schema.getNamespace());
                }
            }
            return namespaces;
        }
        
        /**
         * 检查是否所有依赖都得到满足
         */
        public boolean allDependenciesSatisfied() {
            Set<String> definedNamespaces = getAllDefinedNamespaces();
            for (FileInfo fileInfo : fileInfoMap.values()) {
                for (String dependency : fileInfo.getDependencies()) {
                    if (!definedNamespaces.contains(dependency)) {
                        return false;
                    }
                }
            }
            return true;
        }
        
        /**
         * 获取文件依赖关系图
         */
        public Map<Path, Set<Path>> getDependencyGraph() {
            Map<Path, Set<Path>> graph = new HashMap<>();
            for (Map.Entry<Path, FileInfo> entry : fileInfoMap.entrySet()) {
                Path file = entry.getKey();
                FileInfo fileInfo = entry.getValue();
                Set<Path> dependencies = resolveDependencyFiles(fileInfo, fileInfoMap);
                graph.put(file, dependencies);
            }
            return graph;
        }
        
        /**
         * 解析依赖的文件
         */
        private Set<Path> resolveDependencyFiles(FileInfo fileInfo, Map<Path, FileInfo> allFileInfos) {
            Set<Path> dependencyFiles = new HashSet<>();
            Set<String> dependencies = fileInfo.getDependencies();
            
            for (Map.Entry<Path, FileInfo> entry : allFileInfos.entrySet()) {
                Path otherFile = entry.getKey();
                FileInfo otherFileInfo = entry.getValue();
                
                for (CsdlSchema schema : otherFileInfo.getSchemas()) {
                    if (schema.getNamespace() != null && dependencies.contains(schema.getNamespace())) {
                        dependencyFiles.add(otherFile);
                        break;
                    }
                }
            }
            
            return dependencyFiles;
        }
    }
    
    /**
     * 验证并加载目录中的所有Schema文件
     * 这是主要的公共方法，外部使用者应该调用此方法
     * 
     * @param rootDirectory 要扫描的根目录
     * @return 加载结果，包含成功状态、文件列表、Schema列表和错误信息
     */
    public LoadResult validateAndLoadDirectory(Path rootDirectory) {
        logger.info("Starting comprehensive validation and loading of directory: {}", rootDirectory);
        
        List<Path> allFiles = new ArrayList<>();
        List<CsdlSchema> allSchemas = new ArrayList<>();
        Map<Path, FileInfo> fileInfoMap = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        try {
            // 第一阶段：发现和基本验证
            if (!performInitialValidation(rootDirectory, errors)) {
                return new LoadResult(false, allFiles, allSchemas, fileInfoMap, errors, warnings);
            }
            
            // 第二阶段：解析和OData规范验证
            List<Path> xmlFiles = discoverXmlFiles(rootDirectory);
            if (!parseAndValidateFiles(xmlFiles, fileInfoMap, errors, warnings)) {
                return new LoadResult(false, allFiles, allSchemas, fileInfoMap, errors, warnings);
            }
            
            // 第三阶段：依赖关系分析
            if (!analyzeDependencies(fileInfoMap, errors, warnings)) {
                return new LoadResult(false, allFiles, allSchemas, fileInfoMap, errors, warnings);
            }
            
            // 第四阶段：循环依赖检查
            if (!checkCircularDependencies(fileInfoMap, errors)) {
                return new LoadResult(false, allFiles, allSchemas, fileInfoMap, errors, warnings);
            }
            
            // 第五阶段：依赖完整性验证
            if (!validateDependencyCompleteness(fileInfoMap, errors, warnings)) {
                return new LoadResult(false, allFiles, allSchemas, fileInfoMap, errors, warnings);
            }
            
            // 第六阶段：文件路径存在性验证
            if (!validateFilePaths(fileInfoMap, errors, warnings)) {
                return new LoadResult(false, allFiles, allSchemas, fileInfoMap, errors, warnings);
            }
            
            // 第七阶段：收集最终结果
            collectFinalResults(fileInfoMap, allFiles, allSchemas);
            
            logger.info("Successfully validated and loaded {} files with {} schemas", 
                       allFiles.size(), allSchemas.size());
            
            return new LoadResult(true, allFiles, allSchemas, fileInfoMap, errors, warnings);
            
        } catch (Exception e) {
            String error = "Critical error during validation and loading: " + e.getMessage();
            errors.add(error);
            logger.error(error, e);
            return new LoadResult(false, allFiles, allSchemas, fileInfoMap, errors, warnings);
        }
    }
    
    /**
     * 第一阶段：执行初始验证
     */
    private boolean performInitialValidation(Path rootDirectory, List<String> errors) {
        if (!Files.exists(rootDirectory)) {
            errors.add("Directory does not exist: " + rootDirectory);
            return false;
        }
        
        if (!Files.isDirectory(rootDirectory)) {
            errors.add("Path is not a directory: " + rootDirectory);
            return false;
        }
        
        if (!Files.isReadable(rootDirectory)) {
            errors.add("Directory is not readable: " + rootDirectory);
            return false;
        }
        
        return true;
    }
    
    /**
     * 第二阶段：解析和验证文件
     */
    private boolean parseAndValidateFiles(List<Path> xmlFiles, Map<Path, FileInfo> fileInfoMap, 
                                        List<String> errors, List<String> warnings) {
        logger.info("Parsing and validating {} XML files", xmlFiles.size());
        
        boolean allValid = true;
        
        for (Path xmlFile : xmlFiles) {
            try {
                FileInfo fileInfo = parseAndValidateFile(xmlFile);
                fileInfoMap.put(xmlFile, fileInfo);
                
                if (!fileInfo.isValid()) {
                    allValid = false;
                    errors.addAll(fileInfo.getValidationErrors());
                }
                warnings.addAll(fileInfo.getValidationWarnings());
                
            } catch (Exception e) {
                allValid = false;
                String error = "Failed to process file " + xmlFile + ": " + e.getMessage();
                errors.add(error);
                logger.error(error, e);
            }
        }
        
        return allValid;
    }
    
    /**
     * 解析并验证单个文件
     */
    private FileInfo parseAndValidateFile(Path xmlFile) {
        logger.debug("Parsing and validating file: {}", xmlFile);
        
        FileInfo.Builder builder = new FileInfo.Builder(xmlFile);
        
        try {
            // 文件存在性和可读性检查
            if (!Files.exists(xmlFile)) {
                builder.addValidationError("File does not exist: " + xmlFile);
                return builder.build();
            }
            
            if (!Files.isReadable(xmlFile)) {
                builder.addValidationError("File is not readable: " + xmlFile);
                return builder.build();
            }
            
            if (Files.size(xmlFile) == 0) {
                builder.addValidationError("File is empty: " + xmlFile);
                return builder.build();
            }
            
            // 解析XML文件
            ODataXmlParser.ParseResult parseResult = xmlParser.parseSchemas(xmlFile);
            
            if (!parseResult.isSuccess()) {
                for (String error : parseResult.getErrors()) {
                    builder.addValidationError("Parse error: " + error);
                }
                return builder.build();
            }
            
            List<CsdlSchema> schemas = parseResult.getSchemas();
            if (schemas.isEmpty()) {
                builder.addValidationWarning("No schemas found in file");
                return builder.build();
            }
            
            builder.addSchemas(schemas);
            
            // 对每个Schema进行OData 4.0规范验证
            Set<String> allDependencies = new HashSet<>();
            for (CsdlSchema schema : schemas) {
                ODataValidator.ValidationResult validationResult = validator.validateSchema(schema);
                
                if (!validationResult.isValid()) {
                    for (String error : validationResult.getErrors()) {
                        builder.addValidationError("Schema validation error in '" + schema.getNamespace() + "': " + error);
                    }
                }
                
                for (String warning : validationResult.getWarnings()) {
                    builder.addValidationWarning("Schema validation warning in '" + schema.getNamespace() + "': " + warning);
                }
                
                allDependencies.addAll(validationResult.getDependencies());
            }
            
            builder.addDependencies(allDependencies);
            
            // 添加解析警告
            for (String warning : parseResult.getWarnings()) {
                builder.addValidationWarning("Parse warning: " + warning);
            }
            
        } catch (Exception e) {
            builder.addValidationError("Failed to parse file: " + e.getMessage());
        }
        
        return builder.build();
    }
    
    /**
     * 第三阶段：分析依赖关系
     */
    private boolean analyzeDependencies(Map<Path, FileInfo> fileInfoMap, 
                                      List<String> errors, List<String> warnings) {
        logger.info("Analyzing dependencies among {} files", fileInfoMap.size());
        
        // 构建定义的命名空间映射
        Map<String, Set<Path>> namespaceToFiles = new HashMap<>();
        for (Map.Entry<Path, FileInfo> entry : fileInfoMap.entrySet()) {
            Path file = entry.getKey();
            FileInfo fileInfo = entry.getValue();
            
            for (CsdlSchema schema : fileInfo.getSchemas()) {
                String namespace = schema.getNamespace();
                if (namespace != null && !namespace.trim().isEmpty()) {
                    namespaceToFiles.computeIfAbsent(namespace, k -> new HashSet<>()).add(file);
                }
            }
        }
        
        // 检查命名空间冲突
        for (Map.Entry<String, Set<Path>> entry : namespaceToFiles.entrySet()) {
            String namespace = entry.getKey();
            Set<Path> files = entry.getValue();
            if (files.size() > 1) {
                String conflictFiles = files.stream()
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.joining(", "));
                errors.add("Namespace conflict: '" + namespace + "' is defined in multiple files: " + conflictFiles);
            }
        }
        
        // 计算每个文件的被依赖关系
        for (Map.Entry<Path, FileInfo> entry : fileInfoMap.entrySet()) {
            Path file = entry.getKey();
            FileInfo fileInfo = entry.getValue();
            
            Set<Path> dependentFiles = new HashSet<>();
            
            // 查找依赖于此文件中任何命名空间的其他文件
            for (CsdlSchema schema : fileInfo.getSchemas()) {
                String namespace = schema.getNamespace();
                if (namespace != null) {
                    for (Map.Entry<Path, FileInfo> otherEntry : fileInfoMap.entrySet()) {
                        Path otherFile = otherEntry.getKey();
                        FileInfo otherFileInfo = otherEntry.getValue();
                        
                        if (!file.equals(otherFile) && otherFileInfo.getDependencies().contains(namespace)) {
                            dependentFiles.add(otherFile);
                        }
                    }
                }
            }
            
            // 更新FileInfo中的被依赖关系
            fileInfo.addDependents(dependentFiles);
        }
        
        // 检查孤立的依赖
        Set<String> allDefinedNamespaces = namespaceToFiles.keySet();
        for (Map.Entry<Path, FileInfo> entry : fileInfoMap.entrySet()) {
            Path file = entry.getKey();
            FileInfo fileInfo = entry.getValue();
            
            for (String dependency : fileInfo.getDependencies()) {
                if (!allDefinedNamespaces.contains(dependency)) {
                    warnings.add("File " + file.getFileName() + " depends on undefined namespace: " + dependency);
                }
            }
        }
        
        return true;
    }
    
    /**
     * 第四阶段：检查循环依赖
     */
    private boolean checkCircularDependencies(Map<Path, FileInfo> fileInfoMap, List<String> errors) {
        logger.info("Checking for circular dependencies");
        
        // 使用深度优先搜索检测循环
        Set<Path> visited = new HashSet<>();
        Set<Path> recursionStack = new HashSet<>();
        List<Path> currentPath = new ArrayList<>();
        
        for (Path file : fileInfoMap.keySet()) {
            if (!visited.contains(file)) {
                if (hasCircularDependency(file, fileInfoMap, visited, recursionStack, currentPath, errors)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * 递归检查循环依赖
     */
    private boolean hasCircularDependency(Path file, Map<Path, FileInfo> fileInfoMap,
                                        Set<Path> visited, Set<Path> recursionStack, 
                                        List<Path> currentPath, List<String> errors) {
        visited.add(file);
        recursionStack.add(file);
        currentPath.add(file);
        
        FileInfo fileInfo = fileInfoMap.get(file);
        if (fileInfo == null) {
            currentPath.remove(currentPath.size() - 1);
            recursionStack.remove(file);
            return false;
        }
        
        // 通过依赖的命名空间找到依赖的文件
        Set<Path> dependencyFiles = getDependencyFiles(fileInfo, fileInfoMap);
        
        for (Path dependencyFile : dependencyFiles) {
            if (!visited.contains(dependencyFile)) {
                if (hasCircularDependency(dependencyFile, fileInfoMap, visited, recursionStack, currentPath, errors)) {
                    return true;
                }
            } else if (recursionStack.contains(dependencyFile)) {
                // 构建循环依赖路径描述
                int cycleStart = currentPath.indexOf(dependencyFile);
                List<Path> cyclePath = new ArrayList<>(currentPath.subList(cycleStart, currentPath.size()));
                cyclePath.add(dependencyFile);
                
                String cycleDescription = cyclePath.stream()
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.joining(" -> "));
                
                String error = "Circular dependency detected: " + cycleDescription;
                errors.add(error);
                return true;
            }
        }
        
        currentPath.remove(currentPath.size() - 1);
        recursionStack.remove(file);
        return false;
    }
    
    /**
     * 获取文件的依赖文件
     */
    private Set<Path> getDependencyFiles(FileInfo fileInfo, Map<Path, FileInfo> fileInfoMap) {
        Set<Path> dependencyFiles = new HashSet<>();
        Set<String> dependencies = fileInfo.getDependencies();
        
        for (Map.Entry<Path, FileInfo> entry : fileInfoMap.entrySet()) {
            Path otherFile = entry.getKey();
            FileInfo otherFileInfo = entry.getValue();
            
            for (CsdlSchema schema : otherFileInfo.getSchemas()) {
                if (schema.getNamespace() != null && dependencies.contains(schema.getNamespace())) {
                    dependencyFiles.add(otherFile);
                    break;
                }
            }
        }
        
        return dependencyFiles;
    }
    
    /**
     * 第五阶段：验证依赖完整性
     */
    private boolean validateDependencyCompleteness(Map<Path, FileInfo> fileInfoMap, 
                                                  List<String> errors, List<String> warnings) {
        logger.info("Validating dependency completeness");
        
        // 收集所有定义的命名空间
        Set<String> definedNamespaces = new HashSet<>();
        for (FileInfo fileInfo : fileInfoMap.values()) {
            for (CsdlSchema schema : fileInfo.getSchemas()) {
                if (schema.getNamespace() != null && !schema.getNamespace().trim().isEmpty()) {
                    definedNamespaces.add(schema.getNamespace());
                }
            }
        }
        
        // 检查所有依赖是否都能满足
        boolean allComplete = true;
        Set<String> missingDependencies = new HashSet<>();
        
        for (Map.Entry<Path, FileInfo> entry : fileInfoMap.entrySet()) {
            Path file = entry.getKey();
            FileInfo fileInfo = entry.getValue();
            
            for (String dependency : fileInfo.getDependencies()) {
                if (!definedNamespaces.contains(dependency)) {
                    missingDependencies.add(dependency);
                    String error = "File " + file.getFileName() + 
                                 " has unsatisfied dependency: " + dependency;
                    errors.add(error);
                    allComplete = false;
                }
            }
        }
        
        // 生成依赖关系统计
        if (allComplete) {
            int totalDependencies = fileInfoMap.values().stream()
                .mapToInt(fi -> fi.getDependencies().size())
                .sum();
            warnings.add("Dependency analysis complete: " + totalDependencies + 
                        " total dependencies across " + fileInfoMap.size() + " files");
        } else {
            warnings.add("Missing dependencies: " + String.join(", ", missingDependencies));
        }
        
        return allComplete;
    }
    
    /**
     * 第六阶段：验证文件路径存在性
     */
    private boolean validateFilePaths(Map<Path, FileInfo> fileInfoMap, 
                                    List<String> errors, List<String> warnings) {
        logger.info("Validating file paths existence");
        
        boolean allValid = true;
        
        for (Map.Entry<Path, FileInfo> entry : fileInfoMap.entrySet()) {
            Path file = entry.getKey();
            
            // 验证文件路径是否有效
            if (!Files.exists(file)) {
                errors.add("Referenced file does not exist: " + file);
                allValid = false;
                continue;
            }
            
            if (!Files.isReadable(file)) {
                errors.add("Referenced file is not readable: " + file);
                allValid = false;
                continue;
            }
            
            // 验证文件格式
            String fileName = file.getFileName().toString();
            if (!fileName.toLowerCase().endsWith(".xml")) {
                warnings.add("File does not have .xml extension: " + fileName);
            }
            
            // 验证文件大小
            try {
                long fileSize = Files.size(file);
                if (fileSize == 0) {
                    errors.add("File is empty: " + file);
                    allValid = false;
                } else if (fileSize > 50 * 1024 * 1024) { // 50MB
                    warnings.add("File is very large (>50MB): " + file + " (" + fileSize + " bytes)");
                }
            } catch (IOException e) {
                errors.add("Cannot read file size: " + file + " - " + e.getMessage());
                allValid = false;
            }
        }
        
        return allValid;
    }
    
    /**
     * 第七阶段：收集最终结果
     */
    private void collectFinalResults(Map<Path, FileInfo> fileInfoMap, 
                                   List<Path> allFiles, List<CsdlSchema> allSchemas) {
        // 按文件名排序
        List<Path> sortedFiles = new ArrayList<>(fileInfoMap.keySet());
        sortedFiles.sort(Comparator.comparing(Path::getFileName));
        
        allFiles.addAll(sortedFiles);
        
        // 收集所有Schema，按命名空间排序
        List<CsdlSchema> collectedSchemas = new ArrayList<>();
        for (FileInfo fileInfo : fileInfoMap.values()) {
            collectedSchemas.addAll(fileInfo.getSchemas());
        }
        
        collectedSchemas.sort(Comparator.comparing(CsdlSchema::getNamespace, 
                                                 Comparator.nullsLast(Comparator.naturalOrder())));
        allSchemas.addAll(collectedSchemas);
    }
    
    /**
     * 发现目录中的所有XML文件
     */
    private List<Path> discoverXmlFiles(Path rootDirectory) throws IOException {
        List<Path> xmlFiles = new ArrayList<>();
        
        Files.walkFileTree(rootDirectory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().toLowerCase().endsWith(".xml") && Files.isRegularFile(file)) {
                    xmlFiles.add(file);
                    logger.debug("Found XML file: {}", file);
                }
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                logger.warn("Failed to visit file: {} - {}", file, exc.getMessage());
                return FileVisitResult.CONTINUE;
            }
        });
        
        xmlFiles.sort(Comparator.comparing(Path::getFileName));
        logger.info("Discovered {} XML files in directory: {}", xmlFiles.size(), rootDirectory);
        
        return xmlFiles;
    }
    
    /**
     * 兼容性方法：简化的目录加载
     * @deprecated 使用 {@link #validateAndLoadDirectory(Path)} 替代
     */
    @Deprecated
    public LoadResult loadDirectory(Path rootDirectory) {
        logger.warn("Using deprecated loadDirectory method. Consider using validateAndLoadDirectory for full validation.");
        return validateAndLoadDirectory(rootDirectory);
    }
    
    /**
     * 从指定目录路径加载
     */
    public LoadResult loadDirectory(String directoryPath) {
        if (directoryPath == null) {
            logger.error("Directory path cannot be null");
            return new LoadResult(false, Collections.emptyList(), Collections.emptyList(), 
                                Collections.emptyMap(), Arrays.asList("Directory path cannot be null"), 
                                Collections.emptyList());
        }
        return validateAndLoadDirectory(Paths.get(directoryPath));
    }
    
    /**
     * 从资源路径加载Schema文件
     */
    public LoadResult loadFromResources(String resourcePath) {
        if (resourcePath == null) {
            logger.error("Resource path cannot be null");
            return new LoadResult(false, Collections.emptyList(), Collections.emptyList(), 
                                Collections.emptyMap(), Arrays.asList("Resource path cannot be null"), 
                                Collections.emptyList());
        }
        
        List<String> errors = new ArrayList<>();
        
        try {
            // 获取资源目录的实际路径
            Path currentDir = Paths.get("").toAbsolutePath();
            Path resourceDir = currentDir.resolve("src/main/resources").resolve(resourcePath);
            
            if (!Files.exists(resourceDir)) {
                resourceDir = currentDir.resolve("resources").resolve(resourcePath);
            }
            
            if (!Files.exists(resourceDir)) {
                errors.add("Resource directory not found: " + resourcePath);
                return new LoadResult(false, new ArrayList<>(), new ArrayList<>(), 
                                    new HashMap<>(), errors, new ArrayList<>());
            }
            
            return validateAndLoadDirectory(resourceDir);
            
        } catch (Exception e) {
            errors.add("Failed to load from resources: " + e.getMessage());
            logger.error("Error loading from resources", e);
            return new LoadResult(false, new ArrayList<>(), new ArrayList<>(), 
                                new HashMap<>(), errors, new ArrayList<>());
        }
    }
    
    /**
     * 加载指定的Schema文件列表到仓库
     */
    public void loadSchemasToRepository(List<CsdlSchema> schemas, SchemaRepository repository) {
        if (schemas == null) {
            throw new IllegalArgumentException("Schemas list cannot be null");
        }
        if (repository == null) {
            throw new IllegalArgumentException("Repository cannot be null");
        }
        
        if (schemas.isEmpty()) {
            logger.warn("No schemas to load to repository");
            return;
        }
        
        logger.info("Loading {} schemas to repository", schemas.size());
        
        for (CsdlSchema schema : schemas) {
            try {
                repository.addSchema(schema);
                logger.debug("Loaded schema: {}", schema.getNamespace());
            } catch (Exception e) {
                logger.error("Failed to load schema {} to repository: {}", 
                           schema.getNamespace(), e.getMessage(), e);
            }
        }
        
        logger.info("Successfully loaded {} schemas to repository", schemas.size());
    }
    
    /**
     * 验证Schema的引用关系
     * 
     * 这个方法实现了完整的Schema引用验证，包括：
     * - 命名空间有效性检查
     * - 引用的Schema是否存在
     * - 引用的类型是否存在
     * - 循环引用检测
     * - 依赖路径验证
     * 
     * @param schemas 要验证的Schema列表
     * @return 验证结果，包含详细的错误和警告信息
     */
    public ValidationResult validateSchemaReferences(List<CsdlSchema> schemas) {
        ValidationResult result = new ValidationResult();
        
        if (schemas == null) {
            logger.warn("Schema list is null");
            result.addError("Schema list is null");
            return result;
        }
        
        if (schemas.isEmpty()) {
            logger.info("Schema list is empty - validation passes trivially");
            return result; // Empty list is valid - no schemas to validate
        }
        
        logger.info("Starting comprehensive schema reference validation for {} schemas", schemas.size());
        
        // 构建命名空间到Schema的映射
        Map<String, CsdlSchema> namespaceMap = new HashMap<>();
        Set<String> duplicateNamespaces = new HashSet<>();
        
        for (CsdlSchema schema : schemas) {
            String namespace = schema.getNamespace();
            
            // 验证命名空间
            if (namespace == null || namespace.trim().isEmpty()) {
                result.addError("Schema has null or empty namespace");
                continue;
            }
            
            // 验证命名空间格式
            if (!isValidNamespace(namespace)) {
                result.addError("Invalid namespace format: " + namespace);
                continue;
            }
            
            // 检查重复命名空间
            if (namespaceMap.containsKey(namespace)) {
                duplicateNamespaces.add(namespace);
                result.addError("Duplicate namespace found: " + namespace);
            } else {
                namespaceMap.put(namespace, schema);
            }
        }
        
        // 验证每个Schema的引用
        for (CsdlSchema schema : schemas) {
            if (schema.getNamespace() == null || duplicateNamespaces.contains(schema.getNamespace())) {
                continue; // 跳过无效或重复的Schema
            }
            
            validateSchemaReferences(schema, namespaceMap, result);
        }
        
        // 检查循环依赖
        checkSchemaCircularDependencies(schemas, namespaceMap, result);
        
        logger.info("Schema reference validation completed. Errors: {}, Warnings: {}", 
                   result.getErrors().size(), result.getWarnings().size());
        
        return result;
    }
    
    /**
     * 验证单个Schema的引用
     */
    private void validateSchemaReferences(CsdlSchema schema, Map<String, CsdlSchema> namespaceMap, 
                                        ValidationResult result) {
        String currentNamespace = schema.getNamespace();
        
        // 验证EntityType引用
        if (schema.getEntityTypes() != null) {
            schema.getEntityTypes().forEach(entityType -> {
                validateEntityTypeReferences(entityType, currentNamespace, namespaceMap, result);
            });
        }
        
        // 验证ComplexType引用
        if (schema.getComplexTypes() != null) {
            schema.getComplexTypes().forEach(complexType -> {
                validateComplexTypeReferences(complexType, currentNamespace, namespaceMap, result);
            });
        }
        
        // 验证EntityContainer引用
        if (schema.getEntityContainer() != null) {
            validateEntityContainerReferences(schema.getEntityContainer(), currentNamespace, namespaceMap, result);
        }
    }
    
    /**
     * 验证EntityType的引用
     */
    private void validateEntityTypeReferences(org.apache.olingo.commons.api.edm.provider.CsdlEntityType entityType, 
                                            String currentNamespace, Map<String, CsdlSchema> namespaceMap, 
                                            ValidationResult result) {
        // 验证基类型引用
        if (entityType.getBaseType() != null) {
            String baseType = entityType.getBaseType();
            if (!validateTypeReference(baseType, currentNamespace, namespaceMap)) {
                result.addError("EntityType '" + entityType.getName() + "' references undefined base type: " + baseType);
            }
        }
        
        // 验证属性类型引用
        if (entityType.getProperties() != null) {
            entityType.getProperties().forEach(property -> {
                String propertyType = property.getType();
                if (propertyType != null && !isBuiltInType(propertyType)) {
                    if (!validateTypeReference(propertyType, currentNamespace, namespaceMap)) {
                        result.addError("Property '" + property.getName() + "' in EntityType '" + 
                                      entityType.getName() + "' references undefined type: " + propertyType);
                    }
                }
            });
        }
        
        // 验证导航属性引用
        if (entityType.getNavigationProperties() != null) {
            entityType.getNavigationProperties().forEach(navProp -> {
                String navType = navProp.getType();
                if (navType != null && !validateTypeReference(navType, currentNamespace, namespaceMap)) {
                    result.addError("NavigationProperty '" + navProp.getName() + "' in EntityType '" + 
                                  entityType.getName() + "' references undefined type: " + navType);
                }
            });
        }
    }
    
    /**
     * 验证ComplexType的引用
     */
    private void validateComplexTypeReferences(org.apache.olingo.commons.api.edm.provider.CsdlComplexType complexType, 
                                             String currentNamespace, Map<String, CsdlSchema> namespaceMap, 
                                             ValidationResult result) {
        // 验证基类型引用
        if (complexType.getBaseType() != null) {
            String baseType = complexType.getBaseType();
            if (!validateTypeReference(baseType, currentNamespace, namespaceMap)) {
                result.addError("ComplexType '" + complexType.getName() + "' references undefined base type: " + baseType);
            }
        }
        
        // 验证属性类型引用
        if (complexType.getProperties() != null) {
            complexType.getProperties().forEach(property -> {
                String propertyType = property.getType();
                if (propertyType != null && !isBuiltInType(propertyType)) {
                    if (!validateTypeReference(propertyType, currentNamespace, namespaceMap)) {
                        result.addError("Property '" + property.getName() + "' in ComplexType '" + 
                                      complexType.getName() + "' references undefined type: " + propertyType);
                    }
                }
            });
        }
    }
    
    /**
     * 验证EntityContainer的引用
     */
    private void validateEntityContainerReferences(org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer container, 
                                                  String currentNamespace, Map<String, CsdlSchema> namespaceMap, 
                                                  ValidationResult result) {
        // 验证EntitySet引用
        if (container.getEntitySets() != null) {
            container.getEntitySets().forEach(entitySet -> {
                String entityType = entitySet.getType();
                if (entityType != null && !validateTypeReference(entityType, currentNamespace, namespaceMap)) {
                    result.addError("EntitySet '" + entitySet.getName() + "' references undefined EntityType: " + entityType);
                }
            });
        }
        
        // 验证Singleton引用
        if (container.getSingletons() != null) {
            container.getSingletons().forEach(singleton -> {
                String entityType = singleton.getType();
                if (entityType != null && !validateTypeReference(entityType, currentNamespace, namespaceMap)) {
                    result.addError("Singleton '" + singleton.getName() + "' references undefined EntityType: " + entityType);
                }
            });
        }
    }
    
    /**
     * 验证类型引用是否有效
     */
    private boolean validateTypeReference(String typeRef, String currentNamespace, Map<String, CsdlSchema> namespaceMap) {
        if (typeRef == null || typeRef.trim().isEmpty()) {
            return false;
        }
        
        // 处理集合类型
        String actualType = typeRef;
        if (typeRef.startsWith("Collection(") && typeRef.endsWith(")")) {
            actualType = typeRef.substring(11, typeRef.length() - 1);
        }
        
        // 检查是否为内置类型
        if (isBuiltInType(actualType)) {
            return true;
        }
        
        // 解析命名空间和类型名
        String namespace;
        String typeName;
        
        if (actualType.contains(".")) {
            int lastDot = actualType.lastIndexOf('.');
            namespace = actualType.substring(0, lastDot);
            typeName = actualType.substring(lastDot + 1);
        } else {
            namespace = currentNamespace;
            typeName = actualType;
        }
        
        // 检查命名空间是否存在
        CsdlSchema targetSchema = namespaceMap.get(namespace);
        if (targetSchema == null) {
            return false;
        }
        
        // 检查类型是否存在
        return schemaContainsType(targetSchema, typeName);
    }
    
    /**
     * 检查Schema中是否包含指定类型
     */
    private boolean schemaContainsType(CsdlSchema schema, String typeName) {
        // 检查EntityType
        if (schema.getEntityTypes() != null) {
            for (CsdlEntityType entityType : schema.getEntityTypes()) {
                if (typeName.equals(entityType.getName())) {
                    return true;
                }
            }
        }
        
        // 检查ComplexType
        if (schema.getComplexTypes() != null) {
            for (CsdlComplexType complexType : schema.getComplexTypes()) {
                if (typeName.equals(complexType.getName())) {
                    return true;
                }
            }
        }
        
        // 检查EnumType
        if (schema.getEnumTypes() != null) {
            for (CsdlEnumType enumType : schema.getEnumTypes()) {
                if (typeName.equals(enumType.getName())) {
                    return true;
                }
            }
        }
        
        // 检查TypeDefinition
        if (schema.getTypeDefinitions() != null) {
            for (CsdlTypeDefinition typeDef : schema.getTypeDefinitions()) {
                if (typeName.equals(typeDef.getName())) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * 检查Schema之间的循环依赖
     */
    private void checkSchemaCircularDependencies(List<CsdlSchema> schemas, Map<String, CsdlSchema> namespaceMap, 
                                               ValidationResult result) {
        Map<String, Set<String>> dependencyGraph = buildSchemaDependencyGraph(schemas, namespaceMap);
        
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();
        
        for (String namespace : dependencyGraph.keySet()) {
            if (!visited.contains(namespace)) {
                List<String> path = new ArrayList<>();
                if (hasCircularDependency(namespace, dependencyGraph, visited, recursionStack, path)) {
                    String cyclePath = String.join(" -> ", path);
                    result.addError("Circular dependency detected in schemas: " + cyclePath);
                }
            }
        }
    }
    
    /**
     * 构建Schema依赖图
     */
    private Map<String, Set<String>> buildSchemaDependencyGraph(List<CsdlSchema> schemas, 
                                                              Map<String, CsdlSchema> namespaceMap) {
        Map<String, Set<String>> graph = new HashMap<>();
        
        for (CsdlSchema schema : schemas) {
            String namespace = schema.getNamespace();
            if (namespace == null) continue;
            
            Set<String> dependencies = new HashSet<>();
            
            // 收集所有外部引用的命名空间
            collectDependencies(schema, namespace, dependencies);
            
            graph.put(namespace, dependencies);
        }
        
        return graph;
    }
    
    /**
     * 收集Schema的依赖
     */
    private void collectDependencies(CsdlSchema schema, String currentNamespace, Set<String> dependencies) {
        // 从EntityType收集依赖
        if (schema.getEntityTypes() != null) {
            schema.getEntityTypes().forEach(entityType -> {
                collectTypeReferences(entityType.getBaseType(), currentNamespace, dependencies);
                if (entityType.getProperties() != null) {
                    entityType.getProperties().forEach(prop -> 
                        collectTypeReferences(prop.getType(), currentNamespace, dependencies));
                }
                if (entityType.getNavigationProperties() != null) {
                    entityType.getNavigationProperties().forEach(navProp -> 
                        collectTypeReferences(navProp.getType(), currentNamespace, dependencies));
                }
            });
        }
        
        // 从ComplexType收集依赖
        if (schema.getComplexTypes() != null) {
            schema.getComplexTypes().forEach(complexType -> {
                collectTypeReferences(complexType.getBaseType(), currentNamespace, dependencies);
                if (complexType.getProperties() != null) {
                    complexType.getProperties().forEach(prop -> 
                        collectTypeReferences(prop.getType(), currentNamespace, dependencies));
                }
            });
        }
    }
    
    /**
     * 从类型引用中收集依赖的命名空间
     */
    private void collectTypeReferences(String typeRef, String currentNamespace, Set<String> dependencies) {
        if (typeRef == null || typeRef.trim().isEmpty()) {
            return;
        }
        
        // 处理集合类型
        String actualType = typeRef;
        if (typeRef.startsWith("Collection(") && typeRef.endsWith(")")) {
            actualType = typeRef.substring(11, typeRef.length() - 1);
        }
        
        // 跳过内置类型
        if (isBuiltInType(actualType)) {
            return;
        }
        
        // 提取命名空间
        if (actualType.contains(".")) {
            int lastDot = actualType.lastIndexOf('.');
            String namespace = actualType.substring(0, lastDot);
            if (!namespace.equals(currentNamespace)) {
                dependencies.add(namespace);
            }
        }
    }
    
    /**
     * 检查是否存在循环依赖
     */
    private boolean hasCircularDependency(String namespace, Map<String, Set<String>> graph, 
                                        Set<String> visited, Set<String> recursionStack, 
                                        List<String> path) {
        visited.add(namespace);
        recursionStack.add(namespace);
        path.add(namespace);
        
        Set<String> dependencies = graph.get(namespace);
        if (dependencies != null) {
            for (String dependency : dependencies) {
                if (!visited.contains(dependency)) {
                    if (hasCircularDependency(dependency, graph, visited, recursionStack, path)) {
                        return true;
                    }
                } else if (recursionStack.contains(dependency)) {
                    // 找到循环，添加回到循环起点
                    path.add(dependency);
                    return true;
                }
            }
        }
        
        path.remove(path.size() - 1);
        recursionStack.remove(namespace);
        return false;
    }
    
    /**
     * 验证命名空间格式是否有效
     */
    private boolean isValidNamespace(String namespace) {
        if (namespace == null || namespace.trim().isEmpty()) {
            return false;
        }
        
        // 基本格式检查：不能包含空格，应该是点分隔的标识符
        if (namespace.contains(" ") || namespace.contains("\t") || namespace.contains("\n")) {
            return false;
        }
        
        // 检查是否符合标识符规范
        String[] parts = namespace.split("\\.");
        for (String part : parts) {
            if (part.isEmpty() || !isValidIdentifier(part)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 检查是否为有效的标识符
     */
    private boolean isValidIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return false;
        }
        
        // 第一个字符必须是字母或下划线
        char first = identifier.charAt(0);
        if (!Character.isLetter(first) && first != '_') {
            return false;
        }
        
        // 其余字符必须是字母、数字或下划线
        for (int i = 1; i < identifier.length(); i++) {
            char c = identifier.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_') {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 检查是否为OData内置类型
     */
    private boolean isBuiltInType(String type) {
        if (type == null) return false;
        
        return type.startsWith("Edm.") || 
               type.equals("Binary") || type.equals("Boolean") || type.equals("Byte") ||
               type.equals("Date") || type.equals("DateTimeOffset") || type.equals("Decimal") ||
               type.equals("Double") || type.equals("Duration") || type.equals("Guid") ||
               type.equals("Int16") || type.equals("Int32") || type.equals("Int64") ||
               type.equals("SByte") || type.equals("Single") || type.equals("String") ||
               type.equals("TimeOfDay") || type.equals("Geography") || type.equals("Geometry");
    }
    
    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }
        
        public List<String> getWarnings() {
            return new ArrayList<>(warnings);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
    }
}
