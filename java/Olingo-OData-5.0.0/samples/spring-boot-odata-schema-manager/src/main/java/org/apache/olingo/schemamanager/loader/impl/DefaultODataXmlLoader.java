package org.apache.olingo.schemamanager.loader.impl;

import org.apache.olingo.schemamanager.loader.ODataXmlLoader;
import org.apache.olingo.schemamanager.parser.ODataSchemaParser;
import org.apache.olingo.schemamanager.repository.SchemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认的OData XML加载器实现
 */
@Component
public class DefaultODataXmlLoader implements ODataXmlLoader {
    
    @Autowired
    private ODataSchemaParser parser;
    
    @Autowired 
    private SchemaRepository repository;
    
    private final Map<String, XmlFileInfo> loadedFiles = new ConcurrentHashMap<>();
    
    @Override
    public LoadResult loadFromDirectory(String directoryPath) {
        List<String> errorMessages = new ArrayList<>();
        Map<String, XmlFileInfo> currentLoaded = new HashMap<>();
        
        try {
            File dir = new File(directoryPath);
            if (!dir.exists() || !dir.isDirectory()) {
                errorMessages.add("Directory not found: " + directoryPath);
                return new LoadResult(0, 0, 1, errorMessages, currentLoaded);
            }
            
            List<File> xmlFiles = findXmlFiles(dir);
            int total = xmlFiles.size();
            int successful = 0;
            int failed = 0;
            
            for (File file : xmlFiles) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    LoadResult singleResult = loadFromInputStream(fis, file.getAbsolutePath());
                    if (singleResult.getSuccessfulFiles() > 0) {
                        successful++;
                        currentLoaded.putAll(singleResult.getLoadedFiles());
                    } else {
                        failed++;
                        errorMessages.addAll(singleResult.getErrorMessages());
                    }
                } catch (Exception e) {
                    failed++;
                    errorMessages.add("Error loading file " + file.getAbsolutePath() + ": " + e.getMessage());
                }
            }
            
            return new LoadResult(total, successful, failed, errorMessages, currentLoaded);
            
        } catch (Exception e) {
            errorMessages.add("Directory scan error: " + e.getMessage());
            return new LoadResult(0, 0, 1, errorMessages, currentLoaded);
        }
    }
    
    @Override
    public LoadResult loadFromClasspathDirectory(String classpathDirectory) {
        // 简化实现 - 可以后续扩展
        List<String> errorMessages = Arrays.asList("Classpath directory loading not yet implemented");
        return new LoadResult(0, 0, 1, errorMessages, new HashMap<>());
    }
    
    @Override
    public LoadResult loadSingleFile(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            return loadFromInputStream(fis, filePath);
        } catch (Exception e) {
            List<String> errorMessages = Arrays.asList("Error loading file " + filePath + ": " + e.getMessage());
            return new LoadResult(1, 0, 1, errorMessages, new HashMap<>());
        }
    }
    
    @Override
    public LoadResult loadFromInputStream(InputStream inputStream, String sourceName) {
        List<String> errorMessages = new ArrayList<>();
        Map<String, XmlFileInfo> currentLoaded = new HashMap<>();
        
        try {
            ODataSchemaParser.ParseResult parseResult = parser.parseSchema(inputStream, sourceName);
            
            if (!parseResult.isSuccess()) {
                String error = parseResult.getErrorMessage() != null ? 
                    parseResult.getErrorMessage() : "Unknown parse error";
                errorMessages.add(error);
                
                XmlFileInfo fileInfo = new XmlFileInfo(sourceName, null, new ArrayList<>(), false, error);
                currentLoaded.put(sourceName, fileInfo);
                loadedFiles.put(sourceName, fileInfo);
                
                return new LoadResult(1, 0, 1, errorMessages, currentLoaded);
            }
            
            // 添加到repository
            repository.addSchema(parseResult.getSchema(), sourceName);
            
            // 创建文件信息
            XmlFileInfo fileInfo = new XmlFileInfo(
                sourceName, 
                parseResult.getSchema().getNamespace(),
                parseResult.getDependencies(),
                true, 
                null
            );
            
            currentLoaded.put(sourceName, fileInfo);
            loadedFiles.put(sourceName, fileInfo);
            
            return new LoadResult(1, 1, 0, errorMessages, currentLoaded);
                
        } catch (Exception e) {
            errorMessages.add("Parse error: " + e.getMessage());
            XmlFileInfo fileInfo = new XmlFileInfo(sourceName, null, new ArrayList<>(), false, e.getMessage());
            currentLoaded.put(sourceName, fileInfo);
            loadedFiles.put(sourceName, fileInfo);
            
            return new LoadResult(1, 0, 1, errorMessages, currentLoaded);
        }
    }
    
    @Override
    public Map<String, XmlFileInfo> getLoadedFiles() {
        return new HashMap<>(loadedFiles);
    }
    
    @Override
    public void clear() {
        loadedFiles.clear();
        repository.clear();
    }
    
    private List<File> findXmlFiles(File directory) {
        List<File> xmlFiles = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    xmlFiles.addAll(findXmlFiles(file));
                } else if (file.getName().toLowerCase().endsWith(".xml")) {
                    xmlFiles.add(file);
                }
            }
        }
        return xmlFiles;
    }
}
