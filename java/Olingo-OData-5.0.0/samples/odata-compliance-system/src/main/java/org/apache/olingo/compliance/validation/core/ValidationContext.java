package org.apache.olingo.compliance.validation.core;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;

/**
 * Enhanced validation context that holds all data and state during validation.
 * This context is passed between validation rules and contains both input data and accumulated results.
 */
public class ValidationContext {
    
    // Input data
    private final Object validationTarget;
    private final String fileName;
    private Path filePath;
    private String content;
    private InputStream inputStream;
    private final Map<String, Object> properties;
    
    // Parsed schema data
    private CsdlSchema schema;
    private List<CsdlSchema> allSchemas;
    
    // Validation state
    private final List<String> errors;
    private final List<String> warnings;
    private final List<String> infos;
    private final Set<String> referencedNamespaces;
    private final Set<String> importedNamespaces;
    private final Set<String> currentSchemaNamespaces;
    private final Set<String> definedTargets;
    private final Map<String, String> typeKinds;  // Maps type name to type kind (EntityType, ComplexType, etc.)
    private final Map<String, Object> metadata;
    private final Map<String, Object> cache;
    
    // Processing state
    private long startTime;
    private boolean processingComplete;
    private final Map<String, Long> ruleExecutionTimes;
    
    public ValidationContext(Object validationTarget, String fileName) {
        this.validationTarget = validationTarget;
        this.fileName = fileName;
        this.filePath = null;
        this.content = null;
        this.inputStream = null;
        this.properties = new ConcurrentHashMap<>();
        
        // Initialize collections
        this.errors = Collections.synchronizedList(new ArrayList<>());
        this.warnings = Collections.synchronizedList(new ArrayList<>());
        this.infos = Collections.synchronizedList(new ArrayList<>());
        this.referencedNamespaces = Collections.synchronizedSet(new HashSet<>());
        this.importedNamespaces = Collections.synchronizedSet(new HashSet<>());
        this.currentSchemaNamespaces = Collections.synchronizedSet(new HashSet<>());
        this.definedTargets = Collections.synchronizedSet(new HashSet<>());
        this.typeKinds = new ConcurrentHashMap<>();
        this.metadata = new ConcurrentHashMap<>();
        this.cache = new ConcurrentHashMap<>();
        this.ruleExecutionTimes = new ConcurrentHashMap<>();
        
        this.startTime = System.currentTimeMillis();
        this.processingComplete = false;
    }
    
    // Static factory methods for different types of validation
    public static ValidationContext forFile(Path filePath) {
        ValidationContext context = new ValidationContext(filePath, filePath.getFileName().toString());
        context.filePath = filePath;
        return context;
    }
    
    public static ValidationContext forContent(String content, String fileName) {
        ValidationContext context = new ValidationContext(content, fileName);
        context.content = content;
        return context;
    }
    
    public static ValidationContext forStream(InputStream stream, String fileName) {
        ValidationContext context = new ValidationContext(stream, fileName);
        context.inputStream = stream;
        return context;
    }
    
    public static ValidationContext forSchema(CsdlSchema schema, String fileName) {
        ValidationContext context = new ValidationContext(schema, fileName);
        context.schema = schema;
        return context;
    }
    
    // Error handling methods
    public void addError(String error) {
        errors.add(error);
    }
    
    public void addError(String ruleName, String error) {
        errors.add(String.format("[%s] %s", ruleName, error));
    }
    
    public void addWarning(String warning) {
        warnings.add(warning);
    }
    
    public void addWarning(String ruleName, String warning) {
        warnings.add(String.format("[%s] %s", ruleName, warning));
    }
    
    public void addInfo(String info) {
        infos.add(info);
    }
    
    public void addInfo(String ruleName, String info) {
        infos.add(String.format("[%s] %s", ruleName, info));
    }
    
    // Namespace management
    public void addReferencedNamespace(String namespace) {
        referencedNamespaces.add(namespace);
    }
    
    public void addImportedNamespace(String namespace) {
        importedNamespaces.add(namespace);
    }
    
    public void addCurrentSchemaNamespace(String namespace) {
        currentSchemaNamespaces.add(namespace);
    }
    
    public void addDefinedTarget(String target) {
        definedTargets.add(target);
    }
    
    public void addDefinedTargets(Set<String> targets) {
        definedTargets.addAll(targets);
    }
    
    // Type kinds management  
    public void addTypeKind(String typeFullName, String kind) {
        typeKinds.put(typeFullName, kind);
    }
    
    public String getTypeKind(String typeFullName) {
        return typeKinds.get(typeFullName);
    }
    
    public Map<String, String> getTypeKinds() {
        return Collections.unmodifiableMap(typeKinds);
    }
    
    // Metadata management
    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }
    
    public Object getMetadata(String key) {
        return metadata.get(key);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key, Class<T> type) {
        Object value = metadata.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return null;
    }
    
    // Cache management
    public void putCache(String key, Object value) {
        cache.put(key, value);
    }
    
    public Object getCache(String key) {
        return cache.get(key);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getCache(String key, Class<T> type) {
        Object value = cache.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return null;
    }
    
    // Property management
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }
    
    public Object getProperty(String key) {
        return properties.get(key);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, Class<T> type) {
        Object value = properties.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return null;
    }
    
    // Rule execution tracking
    public void recordRuleExecution(String ruleName, long executionTime) {
        ruleExecutionTimes.put(ruleName, executionTime);
    }
    
    public long getRuleExecutionTime(String ruleName) {
        return ruleExecutionTimes.getOrDefault(ruleName, 0L);
    }
    
    public Map<String, Long> getAllRuleExecutionTimes() {
        return Collections.unmodifiableMap(ruleExecutionTimes);
    }
    
    // Processing control
    public void markProcessingComplete() {
        this.processingComplete = true;
    }
    
    public long getProcessingTime() {
        return System.currentTimeMillis() - startTime;
    }
    
    // Getters
    public Object getValidationTarget() { return validationTarget; }
    public String getFileName() { return fileName; }
    public Path getFilePath() { return filePath; }
    public String getContent() { return content; }
    public InputStream getInputStream() { return inputStream; }
    public Map<String, Object> getProperties() { return Collections.unmodifiableMap(properties); }
    
    public CsdlSchema getSchema() { return schema; }
    public void setSchema(CsdlSchema schema) { this.schema = schema; }
    
    public List<CsdlSchema> getAllSchemas() { return allSchemas; }
    public void setAllSchemas(List<CsdlSchema> schemas) { this.allSchemas = schemas; }
    
    public List<String> getErrors() { return Collections.unmodifiableList(errors); }
    public List<String> getWarnings() { return Collections.unmodifiableList(warnings); }
    public List<String> getInfos() { return Collections.unmodifiableList(infos); }
    public boolean hasErrors() { return !errors.isEmpty(); }
    public boolean hasWarnings() { return !warnings.isEmpty(); }
    public int getErrorCount() { return errors.size(); }
    public int getWarningCount() { return warnings.size(); }
    
    public Set<String> getReferencedNamespaces() { return Collections.unmodifiableSet(referencedNamespaces); }
    public Set<String> getImportedNamespaces() { return Collections.unmodifiableSet(importedNamespaces); }
    public Set<String> getCurrentSchemaNamespaces() { return Collections.unmodifiableSet(currentSchemaNamespaces); }
    public Set<String> getDefinedTargets() { return Collections.unmodifiableSet(definedTargets); }
    public Map<String, Object> getMetadata() { return Collections.unmodifiableMap(metadata); }
    
    public long getStartTime() { return startTime; }
    public boolean isProcessingComplete() { return processingComplete; }
    
    @Override
    public String toString() {
        return String.format("ValidationContext[file=%s, errors=%d, warnings=%d, time=%dms, complete=%s]", 
                           fileName, errors.size(), warnings.size(), getProcessingTime(), processingComplete);
    }
}
