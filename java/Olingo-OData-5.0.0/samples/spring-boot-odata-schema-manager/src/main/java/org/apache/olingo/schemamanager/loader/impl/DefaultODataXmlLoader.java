package org.apache.olingo.schemamanager.loader.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.olingo.schemamanager.loader.ODataXmlLoader;
import org.apache.olingo.schemamanager.parser.ODataSchemaParser;
import org.apache.olingo.schemamanager.repository.SchemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
  
    /**
     * 从classpath resource目录递归加载所有XML文件
     * @param resourceDir 资源目录（如 "loader/valid"）
     * @return 加载结果
     */
    public LoadResult loadFromResourceDirectory(String resourceDir) {
        List<String> errorMessages = new ArrayList<>();
        Map<String, XmlFileInfo> currentLoaded = new HashMap<>();
        int total = 0, successful = 0, failed = 0;
        try {
            List<String> xmlResourcePaths = listXmlResourcesRecursively(resourceDir);
            total = xmlResourcePaths.size();
            for (String path : xmlResourcePaths) {
                try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
                    if (is == null) {
                        failed++;
                        errorMessages.add("Resource not found: " + path);
                        continue;
                    }
                    LoadResult singleResult = loadFromInputStream(is, path);
                    if (singleResult.getSuccessfulFiles() > 0) {
                        successful++;
                        currentLoaded.putAll(singleResult.getLoadedFiles());
                    } else {
                        failed++;
                        errorMessages.addAll(singleResult.getErrorMessages());
                    }
                } catch (IOException | RuntimeException e) {
                    failed++;
                    errorMessages.add("Error loading resource " + path + ": " + e.getMessage());
                }
            }
        } catch (IOException | RuntimeException e) {
            errorMessages.add("Resource directory scan error: " + e.getMessage());
            failed = total == 0 ? 1 : total;
        }
        return new LoadResult(total, successful, failed, errorMessages, currentLoaded);
    }
    
    @Override
    public LoadResult loadSingleFileFromResource(String relativePath) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(relativePath);
        if (inputStream == null) {
            throw new IllegalArgumentException("Resource not found: " + relativePath);
        }
        try {
            return loadFromInputStream(inputStream, relativePath);
        } finally {
            try {
                inputStream.close();
            } catch (java.io.IOException | RuntimeException e) {
                return new LoadResult(1, 0, 1, 
                        Arrays.asList("Error closing input stream: " + e.getMessage()), new HashMap<>());
            }
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

    /**
     * 递归列出resource目录下所有xml文件的路径（相对classpath）
     */
    private List<String> listXmlResourcesRecursively(String resourceDir) throws IOException {
        List<String> result = new ArrayList<>();
        java.net.URL dirUrl = getClass().getClassLoader().getResource(resourceDir);
        if (dirUrl == null) return result;
        if (dirUrl.getProtocol().equals("file")) {
            File dir = new File(dirUrl.getPath());
            if (dir.exists() && dir.isDirectory()) {
                listXmlFilesFromFileSystem(dir, resourceDir, result);
            }
        } else if (dirUrl.getProtocol().equals("jar")) {
            String jarPath = dirUrl.getPath().substring(5, dirUrl.getPath().indexOf("!"));
            try (java.util.jar.JarFile jar = new java.util.jar.JarFile(jarPath)) {
                java.util.Enumeration<java.util.jar.JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    java.util.jar.JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (name.startsWith(resourceDir) && name.toLowerCase().endsWith(".xml") && !entry.isDirectory()) {
                        result.add(name);
                    }
                }
            }
        }
        return result;
    }

    private void listXmlFilesFromFileSystem(File dir, String resourceDir, List<String> result) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    listXmlFilesFromFileSystem(file, resourceDir + "/" + file.getName(), result);
                } else if (file.getName().toLowerCase().endsWith(".xml")) {
                    String relPath = resourceDir + "/" + file.getName();
                    result.add(relPath.replace("\\", "/"));
                }
            }
        }
    }
}
