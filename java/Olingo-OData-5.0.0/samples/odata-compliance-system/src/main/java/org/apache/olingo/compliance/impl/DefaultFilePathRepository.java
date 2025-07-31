package org.apache.olingo.compliance.impl;

import org.apache.olingo.compliance.api.FilePathRepository;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Default implementation of FilePathRepository using in-memory storage.
 * Handles files that may contain multiple schemas with different namespaces.
 */
public class DefaultFilePathRepository implements FilePathRepository {
    
    private final ConcurrentHashMap<Path, DefaultFileEntry> fileMap = new ConcurrentHashMap<>();
    
    /**
     * Default implementation of FileEntry.
     */
    public static class DefaultFileEntry implements FileEntry {
        private final Path filePath;
        private final List<CsdlSchema> schemas;
        private final LocalDateTime validationTime;
        private final long fileSize;
        private final Set<String> namespaces;
        
        public DefaultFileEntry(Path filePath, List<CsdlSchema> schemas, 
                              LocalDateTime validationTime, long fileSize) {
            this.filePath = filePath;
            this.schemas = new ArrayList<>(schemas);
            this.validationTime = validationTime;
            this.fileSize = fileSize;
            this.namespaces = schemas.stream()
                .map(CsdlSchema::getNamespace)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        }
        
        @Override
        public Path getFilePath() {
            return filePath;
        }
        
        @Override
        public List<CsdlSchema> getSchemas() {
            return Collections.unmodifiableList(schemas);
        }
        
        @Override
        public LocalDateTime getValidationTime() {
            return validationTime;
        }
        
        @Override
        public long getFileSize() {
            return fileSize;
        }
        
        @Override
        public Set<String> getNamespaces() {
            return Collections.unmodifiableSet(namespaces);
        }
    }
    
    @Override
    public void storeSchemas(Path filePath, List<CsdlSchema> schemas, 
                           LocalDateTime validationTime, long fileSize) {
        fileMap.put(filePath, new DefaultFileEntry(filePath, schemas, validationTime, fileSize));
    }
    
    @Override
    public List<CsdlSchema> getSchemas(Path filePath) {
        DefaultFileEntry entry = fileMap.get(filePath);
        return entry != null ? entry.getSchemas() : Collections.emptyList();
    }
    
    @Override
    public Optional<CsdlSchema> getSchemaByNamespace(Path filePath, String namespace) {
        DefaultFileEntry entry = fileMap.get(filePath);
        if (entry != null) {
            return entry.getSchemas().stream()
                .filter(schema -> namespace.equals(schema.getNamespace()))
                .findFirst();
        }
        return Optional.empty();
    }
    
    @Override
    public Optional<FileEntry> getFileEntry(Path filePath) {
        DefaultFileEntry entry = fileMap.get(filePath);
        return entry != null ? Optional.of(entry) : Optional.empty();
    }
    
    @Override
    public boolean contains(Path filePath) {
        return fileMap.containsKey(filePath);
    }
    
    @Override
    public List<Path> getAllFilePaths() {
        return new ArrayList<>(fileMap.keySet());
    }
    
    @Override
    public List<Path> getFilePathsByNamespace(String namespace) {
        return fileMap.entrySet().stream()
            .filter(entry -> entry.getValue().getNamespaces().contains(namespace))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    @Override
    public Set<String> getAllNamespaces() {
        return fileMap.values().stream()
            .flatMap(entry -> entry.getNamespaces().stream())
            .collect(Collectors.toSet());
    }
    
    @Override
    public Optional<LocalDateTime> getValidationTime(Path filePath) {
        DefaultFileEntry entry = fileMap.get(filePath);
        return entry != null ? Optional.of(entry.getValidationTime()) : Optional.empty();
    }
    
    @Override
    public Optional<Long> getFileSize(Path filePath) {
        DefaultFileEntry entry = fileMap.get(filePath);
        return entry != null ? Optional.of(entry.getFileSize()) : Optional.empty();
    }
    
    @Override
    public Set<String> remove(Path filePath) {
        DefaultFileEntry entry = fileMap.remove(filePath);
        return entry != null ? entry.getNamespaces() : Collections.emptySet();
    }
    
    @Override
    public void clear() {
        fileMap.clear();
    }
    
    @Override
    public int size() {
        return fileMap.size();
    }
    
    @Override
    public int getTotalSchemaCount() {
        return fileMap.values().stream()
            .mapToInt(entry -> entry.getSchemas().size())
            .sum();
    }
}
