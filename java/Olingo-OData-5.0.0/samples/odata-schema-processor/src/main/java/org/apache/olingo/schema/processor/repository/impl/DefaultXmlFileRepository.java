package org.apache.olingo.schema.processor.repository.impl;

import org.apache.olingo.schema.processor.repository.XmlFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * XML文件仓库默认实现
 */
public class DefaultXmlFileRepository implements XmlFileRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultXmlFileRepository.class);
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("<Schema[^>]*Namespace\\s*=\\s*[\"']([^\"']+)[\"'][^>]*>", Pattern.CASE_INSENSITIVE);
    
    private final Map<Path, String> fileContents = new ConcurrentHashMap<>();
    private final Map<Path, Set<String>> fileToNamespaces = new ConcurrentHashMap<>();
    private final Map<String, Set<Path>> namespaceToFiles = new ConcurrentHashMap<>();
    
    @Override
    public LoadResult loadFromDirectory(Path rootPath) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        int totalFiles = 0;
        int successfulFiles = 0;
        
        try {
            if (!Files.exists(rootPath)) {
                errors.add("Root path does not exist: " + rootPath);
                return new LoadResult(false, errors, warnings, 0, 0);
            }
            
            if (!Files.isDirectory(rootPath)) {
                errors.add("Root path is not a directory: " + rootPath);
                return new LoadResult(false, errors, warnings, 0, 0);
            }
            
            // 递归查找所有XML文件
            List<Path> xmlFiles = Files.walk(rootPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".xml"))
                    .collect(Collectors.toList());
            
            totalFiles = xmlFiles.size();
            logger.info("Found {} XML files in directory: {}", totalFiles, rootPath);
            
            for (Path xmlFile : xmlFiles) {
                try {
                    loadSingleFile(xmlFile);
                    successfulFiles++;
                    logger.debug("Successfully loaded XML file: {}", xmlFile);
                } catch (Exception e) {
                    String error = "Failed to load XML file " + xmlFile + ": " + e.getMessage();
                    errors.add(error);
                    logger.error(error, e);
                }
            }
            
            if (successfulFiles == 0 && totalFiles > 0) {
                warnings.add("No XML files were successfully loaded");
            }
            
            logger.info("Loaded {} out of {} XML files successfully", successfulFiles, totalFiles);
            return new LoadResult(errors.isEmpty(), errors, warnings, totalFiles, successfulFiles);
            
        } catch (IOException e) {
            String error = "Failed to scan directory " + rootPath + ": " + e.getMessage();
            errors.add(error);
            logger.error(error, e);
            return new LoadResult(false, errors, warnings, totalFiles, successfulFiles);
        }
    }
    
    private void loadSingleFile(Path filePath) throws IOException {
        String content = new String(Files.readAllBytes(filePath), "UTF-8");
        fileContents.put(filePath, content);
        
        // 提取namespace信息
        Set<String> namespaces = extractNamespaces(content);
        fileToNamespaces.put(filePath, namespaces);
        
        // 更新反向映射
        for (String namespace : namespaces) {
            namespaceToFiles.computeIfAbsent(namespace, k -> new HashSet<>()).add(filePath);
        }
    }
    
    private Set<String> extractNamespaces(String xmlContent) {
        Set<String> namespaces = new HashSet<>();
        Matcher matcher = NAMESPACE_PATTERN.matcher(xmlContent);
        
        while (matcher.find()) {
            String namespace = matcher.group(1);
            if (namespace != null && !namespace.trim().isEmpty()) {
                namespaces.add(namespace.trim());
            }
        }
        
        return namespaces;
    }
    
    @Override
    public Set<Path> getAllXmlFiles() {
        return new HashSet<>(fileContents.keySet());
    }
    
    @Override
    public String getXmlContent(Path filePath) {
        return fileContents.get(filePath);
    }
    
    @Override
    public Map<Path, Set<String>> getFileToNamespaceMapping() {
        Map<Path, Set<String>> result = new HashMap<>();
        for (Map.Entry<Path, Set<String>> entry : fileToNamespaces.entrySet()) {
            result.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return result;
    }
    
    @Override
    public Map<String, Set<Path>> getNamespaceToFileMapping() {
        Map<String, Set<Path>> result = new HashMap<>();
        for (Map.Entry<String, Set<Path>> entry : namespaceToFiles.entrySet()) {
            result.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return result;
    }
    
    @Override
    public boolean isNamespaceDefined(String namespace) {
        return namespaceToFiles.containsKey(namespace);
    }
    
    @Override
    public Set<Path> getFilesDefiningNamespace(String namespace) {
        Set<Path> files = namespaceToFiles.get(namespace);
        return files != null ? new HashSet<>(files) : new HashSet<>();
    }
}
