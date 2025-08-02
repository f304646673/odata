package org.apache.olingo.compliance.engine.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;

/**
 * Default implementation of ValidationService that uses internal data structures
 * to provide validation operations. This implementation encapsulates all the
 * validation-related data structures and operations.
 */
public class DefaultValidationService implements ValidationService {
    
    // Core data structures
    private final SchemaRegistry schemaRegistry;
    private final Set<String> referencedNamespaces;
    private final Set<String> importedNamespaces;
    private final Set<String> currentSchemaNamespaces;
    private final Set<String> definedTargets;
    private final Map<String, String> typeKinds;
    private final Map<String, Object> metadata;
    private final Map<String, Object> cache;
    private final Map<String, Long> ruleExecutionTimes;
    
    // Schema data
    private List<CsdlSchema> allSchemas;
    
    // State
    private volatile boolean initialized;
    
    public DefaultValidationService() {
        this.schemaRegistry = new DefaultSchemaRegistry();
        this.referencedNamespaces = Collections.synchronizedSet(new HashSet<>());
        this.importedNamespaces = Collections.synchronizedSet(new HashSet<>());
        this.currentSchemaNamespaces = Collections.synchronizedSet(new HashSet<>());
        this.definedTargets = Collections.synchronizedSet(new HashSet<>());
        this.typeKinds = new ConcurrentHashMap<>();
        this.metadata = new ConcurrentHashMap<>();
        this.cache = new ConcurrentHashMap<>();
        this.ruleExecutionTimes = new ConcurrentHashMap<>();
        this.allSchemas = new ArrayList<>();
        this.initialized = false;
    }
    
    public DefaultValidationService(SchemaRegistry schemaRegistry) {
        this.schemaRegistry = schemaRegistry != null ? schemaRegistry : new DefaultSchemaRegistry();
        this.referencedNamespaces = Collections.synchronizedSet(new HashSet<>());
        this.importedNamespaces = Collections.synchronizedSet(new HashSet<>());
        this.currentSchemaNamespaces = Collections.synchronizedSet(new HashSet<>());
        this.definedTargets = Collections.synchronizedSet(new HashSet<>());
        this.typeKinds = new ConcurrentHashMap<>();
        this.metadata = new ConcurrentHashMap<>();
        this.cache = new ConcurrentHashMap<>();
        this.ruleExecutionTimes = new ConcurrentHashMap<>();
        this.allSchemas = new ArrayList<>();
        this.initialized = false;
    }
    
    @Override
    public boolean isNamespaceDefined(String namespace) {
        return schemaRegistry.isNamespaceDefined(namespace);
    }
    
    @Override
    public boolean isTypeDefined(String namespace, String typeName) {
        return schemaRegistry.isTypeDefined(namespace, typeName);
    }
    
    @Override
    public boolean isFileExists(String fileName) {
        return schemaRegistry.isFileExists(fileName);
    }
    
    @Override
    public Set<String> getAllNamespaces() {
        return schemaRegistry.getAllNamespaces();
    }
    
    @Override
    public Set<String> getTypesInNamespace(String namespace) {
        return schemaRegistry.getTypesInNamespace(namespace);
    }
    
    @Override
    public Set<String> getAllFileNames() {
        return schemaRegistry.getAllFileNames();
    }
    
    @Override
    public String getTypeKind(String fullTypeName) {
        return typeKinds.get(fullTypeName);
    }
    
    @Override
    public void setTypeKind(String fullTypeName, String kind) {
        typeKinds.put(fullTypeName, kind);
    }
    
    @Override
    public Map<String, String> getAllTypeKinds() {
        return Collections.unmodifiableMap(typeKinds);
    }
    
    @Override
    public void addReferencedNamespace(String namespace) {
        referencedNamespaces.add(namespace);
    }
    
    @Override
    public void addImportedNamespace(String namespace) {
        importedNamespaces.add(namespace);
    }
    
    @Override
    public void addCurrentSchemaNamespace(String namespace) {
        currentSchemaNamespaces.add(namespace);
    }
    
    @Override
    public Set<String> getReferencedNamespaces() {
        return Collections.unmodifiableSet(referencedNamespaces);
    }
    
    @Override
    public Set<String> getImportedNamespaces() {
        return Collections.unmodifiableSet(importedNamespaces);
    }
    
    @Override
    public Set<String> getCurrentSchemaNamespaces() {
        return Collections.unmodifiableSet(currentSchemaNamespaces);
    }
    
    @Override
    public void addDefinedTarget(String target) {
        definedTargets.add(target);
    }
    
    @Override
    public void addDefinedTargets(Set<String> targets) {
        definedTargets.addAll(targets);
    }
    
    @Override
    public Set<String> getDefinedTargets() {
        return Collections.unmodifiableSet(definedTargets);
    }
    
    @Override
    public boolean isTargetDefined(String target) {
        return definedTargets.contains(target);
    }
    
    @Override
    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
    }
    
    @Override
    public Object getMetadata(String key) {
        return metadata.get(key);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key, Class<T> type) {
        Object value = metadata.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return null;
    }
    
    @Override
    public Map<String, Object> getAllMetadata() {
        return Collections.unmodifiableMap(metadata);
    }
    
    @Override
    public void setCache(String key, Object value) {
        cache.put(key, value);
    }
    
    @Override
    public Object getCache(String key) {
        return cache.get(key);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCache(String key, Class<T> type) {
        Object value = cache.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return null;
    }
    
    @Override
    public List<CsdlSchema> getAllSchemas() {
        return allSchemas != null ? Collections.unmodifiableList(allSchemas) : Collections.emptyList();
    }
    
    @Override
    public void setAllSchemas(List<CsdlSchema> schemas) {
        this.allSchemas = schemas != null ? new ArrayList<>(schemas) : new ArrayList<>();
        if (this.allSchemas != null && schemaRegistry instanceof DefaultSchemaRegistry) {
            ((DefaultSchemaRegistry) schemaRegistry).addSchemas(this.allSchemas);
        }
    }
    
    @Override
    public List<String> validateCrossFileReferences() {
        List<String> issues = new ArrayList<>();
        
        // Validate all referenced namespaces are defined
        for (String referencedNs : referencedNamespaces) {
            if (!isNamespaceDefined(referencedNs)) {
                issues.add("Referenced namespace not found: " + referencedNs);
            }
        }
        
        // Validate all imported namespaces are available
        for (String importedNs : importedNamespaces) {
            if (!isNamespaceDefined(importedNs)) {
                issues.add("Imported namespace not found: " + importedNs);
            }
        }
        
        return issues;
    }
    
    @Override
    public List<String> detectConflicts() {
        List<String> conflicts = new ArrayList<>();
        
        // Check for namespace conflicts
        Set<String> namespaces = getAllNamespaces();
        for (String namespace : namespaces) {
            Set<String> types = getTypesInNamespace(namespace);
            Set<String> seenTypes = new HashSet<>();
            for (String type : types) {
                if (seenTypes.contains(type)) {
                    conflicts.add("Duplicate type definition: " + namespace + "." + type);
                } else {
                    seenTypes.add(type);
                }
            }
        }
        
        // Check for target conflicts
        Set<String> seenTargets = new HashSet<>();
        for (String target : definedTargets) {
            if (seenTargets.contains(target)) {
                conflicts.add("Duplicate target definition: " + target);
            } else {
                seenTargets.add(target);
            }
        }
        
        return conflicts;
    }
    
    @Override
    public void recordRuleExecution(String ruleName, long executionTime) {
        ruleExecutionTimes.put(ruleName, executionTime);
    }
    
    @Override
    public long getRuleExecutionTime(String ruleName) {
        return ruleExecutionTimes.getOrDefault(ruleName, 0L);
    }
    
    @Override
    public Map<String, Long> getAllRuleExecutionTimes() {
        return Collections.unmodifiableMap(ruleExecutionTimes);
    }
    
    @Override
    public boolean isReady() {
        return initialized && schemaRegistry != null;
    }
    
    @Override
    public void initialize(List<CsdlSchema> schemas) {
        setAllSchemas(schemas);
        this.initialized = true;
    }
    
    @Override
    public void reset() {
        referencedNamespaces.clear();
        importedNamespaces.clear();
        currentSchemaNamespaces.clear();
        definedTargets.clear();
        typeKinds.clear();
        metadata.clear();
        cache.clear();
        ruleExecutionTimes.clear();
        allSchemas.clear();
        this.initialized = false;
        
        if (schemaRegistry instanceof DefaultSchemaRegistry) {
            ((DefaultSchemaRegistry) schemaRegistry).reset();
        }
    }
    
    /**
     * Get the underlying schema registry (for advanced operations)
     */
    public SchemaRegistry getSchemaRegistry() {
        return schemaRegistry;
    }
}
