package org.apache.olingo.compliance.engine.core;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;

/**
 * Enhanced validation context that holds all data and state during validation.
 * This context is passed between validation rules and contains both input data and accumulated results.
 * Uses ValidationService interface for decoupled validation operations.
 */
public class ValidationContext {
    
    // Input data
    private final Object validationTarget;
    private final String fileName;
    private Path filePath;
    private String content;
    private InputStream inputStream;
    private final Map<String, Object> properties;
    
    // Validation service for decoupled operations
    private ValidationService validationService;
    
    // Validation state
    private final List<String> errors;
    private final List<String> warnings;
    private final List<String> infos;
    
    // Processing state
    private long startTime;
    private boolean processingComplete;
    
    public ValidationContext(Object validationTarget, String fileName) {
        this.validationTarget = validationTarget;
        this.fileName = fileName;
        this.filePath = null;
        this.content = null;
        this.inputStream = null;
        this.properties = new ConcurrentHashMap<>();
        
        // Initialize validation service
        this.validationService = new DefaultValidationService();
        
        // Initialize collections
        this.errors = Collections.synchronizedList(new ArrayList<>());
        this.warnings = Collections.synchronizedList(new ArrayList<>());
        this.infos = Collections.synchronizedList(new ArrayList<>());
        
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
        List<CsdlSchema> schemas = new ArrayList<>();
        schemas.add(schema);
        context.validationService.setAllSchemas(schemas);
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
    
    // Namespace management - delegate to validation service
    public void addReferencedNamespace(String namespace) {
        validationService.addReferencedNamespace(namespace);
    }
    
    public void addImportedNamespace(String namespace) {
        validationService.addImportedNamespace(namespace);
    }
    
    public void addCurrentSchemaNamespace(String namespace) {
        validationService.addCurrentSchemaNamespace(namespace);
    }
    
    public void addDefinedTarget(String target) {
        validationService.addDefinedTarget(target);
    }
    
    public void addDefinedTargets(java.util.Set<String> targets) {
        validationService.addDefinedTargets(targets);
    }
    
    // Type kinds management - delegate to validation service
    public void addTypeKind(String typeFullName, String kind) {
        validationService.setTypeKind(typeFullName, kind);
    }
    
    public String getTypeKind(String typeFullName) {
        return validationService.getTypeKind(typeFullName);
    }
    
    public Map<String, String> getTypeKinds() {
        return validationService.getAllTypeKinds();
    }
    
    // Metadata management - delegate to validation service
    public void addMetadata(String key, Object value) {
        validationService.setMetadata(key, value);
    }
    
    public Object getMetadata(String key) {
        return validationService.getMetadata(key);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key, Class<T> type) {
        return validationService.getMetadata(key, type);
    }
    
    // Cache management - delegate to validation service
    public void putCache(String key, Object value) {
        validationService.setCache(key, value);
    }
    
    public Object getCache(String key) {
        return validationService.getCache(key);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getCache(String key, Class<T> type) {
        return validationService.getCache(key, type);
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
    
    // Rule execution tracking - delegate to validation service
    public void recordRuleExecution(String ruleName, long executionTime) {
        validationService.recordRuleExecution(ruleName, executionTime);
    }
    
    public long getRuleExecutionTime(String ruleName) {
        return validationService.getRuleExecutionTime(ruleName);
    }
    
    public Map<String, Long> getAllRuleExecutionTimes() {
        return validationService.getAllRuleExecutionTimes();
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
    
    // Schema operations - delegate to validation service
    public List<CsdlSchema> getAllSchemas() { 
        return validationService.getAllSchemas();
    }
    
    public void setAllSchemas(List<CsdlSchema> schemas) { 
        validationService.setAllSchemas(schemas);
    }
    
    // ValidationService access
    public ValidationService getValidationService() { 
        return validationService; 
    }
    
    public void setValidationService(ValidationService validationService) { 
        this.validationService = validationService != null ? validationService : new DefaultValidationService();
    }
    
    // Legacy SchemaRegistry access (for backward compatibility)
    @Deprecated
    public SchemaRegistry getSchemaRegistry() { 
        if (validationService instanceof DefaultValidationService) {
            return ((DefaultValidationService) validationService).getSchemaRegistry();
        }
        return null;
    }
    
    @Deprecated
    public void setSchemaRegistry(SchemaRegistry schemaRegistry) { 
        if (schemaRegistry != null) {
            this.validationService = new DefaultValidationService(schemaRegistry);
        }
    }
    
    // Error state
    public List<String> getErrors() { return Collections.unmodifiableList(errors); }
    public List<String> getWarnings() { return Collections.unmodifiableList(warnings); }
    public List<String> getInfos() { return Collections.unmodifiableList(infos); }
    public boolean hasErrors() { return !errors.isEmpty(); }
    public boolean hasWarnings() { return !warnings.isEmpty(); }
    public int getErrorCount() { return errors.size(); }
    public int getWarningCount() { return warnings.size(); }
    
    // Namespace access - delegate to validation service
    public java.util.Set<String> getReferencedNamespaces() { 
        return validationService.getReferencedNamespaces();
    }
    
    public java.util.Set<String> getImportedNamespaces() { 
        return validationService.getImportedNamespaces();
    }
    
    public java.util.Set<String> getCurrentSchemaNamespaces() { 
        return validationService.getCurrentSchemaNamespaces();
    }
    
    public java.util.Set<String> getDefinedTargets() { 
        return validationService.getDefinedTargets();
    }
    
    public Map<String, Object> getAllMetadata() { 
        return validationService.getAllMetadata();
    }
    
    public long getStartTime() { return startTime; }
    public boolean isProcessingComplete() { return processingComplete; }
    
    @Override
    public String toString() {
        return String.format("ValidationContext[file=%s, errors=%d, warnings=%d, time=%dms, complete=%s]", 
                           fileName, errors.size(), warnings.size(), getProcessingTime(), processingComplete);
    }
}
