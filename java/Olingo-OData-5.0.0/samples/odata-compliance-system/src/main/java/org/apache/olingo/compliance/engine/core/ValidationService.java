package org.apache.olingo.compliance.engine.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;

/**
 * Validation service interface that provides all validation-related operations.
 * This interface encapsulates various validation checks and data queries,
 * allowing for better decoupling between validation context and specific data structures.
 */
public interface ValidationService {
    
    // Schema and namespace operations
    /**
     * Check if a namespace is defined in the validation scope
     */
    boolean isNamespaceDefined(String namespace);
    
    /**
     * Check if a type is defined in the given namespace
     */
    boolean isTypeDefined(String namespace, String typeName);
    
    /**
     * Check if a file with the given name exists in the validation scope
     */
    boolean isFileExists(String fileName);
    
    /**
     * Get all available namespaces
     */
    Set<String> getAllNamespaces();
    
    /**
     * Get all types defined in a specific namespace
     */
    Set<String> getTypesInNamespace(String namespace);
    
    /**
     * Get all file names in the validation scope
     */
    Set<String> getAllFileNames();
    
    // Type system operations
    /**
     * Get the kind of a type (EntityType, ComplexType, etc.)
     */
    String getTypeKind(String fullTypeName);
    
    /**
     * Set the kind of a type
     */
    void setTypeKind(String fullTypeName, String kind);
    
    /**
     * Get all type kinds
     */
    Map<String, String> getAllTypeKinds();
    
    // Reference tracking operations
    /**
     * Add a referenced namespace
     */
    void addReferencedNamespace(String namespace);
    
    /**
     * Add an imported namespace
     */
    void addImportedNamespace(String namespace);
    
    /**
     * Add a current schema namespace
     */
    void addCurrentSchemaNamespace(String namespace);
    
    /**
     * Get all referenced namespaces
     */
    Set<String> getReferencedNamespaces();
    
    /**
     * Get all imported namespaces
     */
    Set<String> getImportedNamespaces();
    
    /**
     * Get all current schema namespaces
     */
    Set<String> getCurrentSchemaNamespaces();
    
    // Target definition operations
    /**
     * Add a defined target
     */
    void addDefinedTarget(String target);
    
    /**
     * Add multiple defined targets
     */
    void addDefinedTargets(Set<String> targets);
    
    /**
     * Get all defined targets
     */
    Set<String> getDefinedTargets();
    
    /**
     * Check if a target is defined
     */
    boolean isTargetDefined(String target);
    
    // Metadata operations
    /**
     * Store metadata value
     */
    void setMetadata(String key, Object value);
    
    /**
     * Get metadata value
     */
    Object getMetadata(String key);
    
    /**
     * Get metadata value with type check
     */
    <T> T getMetadata(String key, Class<T> type);
    
    /**
     * Get all metadata
     */
    Map<String, Object> getAllMetadata();
    
    // Cache operations
    /**
     * Store cache value
     */
    void setCache(String key, Object value);
    
    /**
     * Get cache value
     */
    Object getCache(String key);
    
    /**
     * Get cache value with type check
     */
    <T> T getCache(String key, Class<T> type);
    
    // Schema operations
    /**
     * Get all schemas in the validation scope
     */
    List<CsdlSchema> getAllSchemas();
    
    /**
     * Set all schemas
     */
    void setAllSchemas(List<CsdlSchema> schemas);
    
    // Cross-file reference validation
    /**
     * Validate cross-file references for the current context
     */
    List<String> validateCrossFileReferences();
    
    // Conflict detection
    /**
     * Detect conflicts in the validation scope
     */
    List<String> detectConflicts();
    
    // Rule execution tracking
    /**
     * Record rule execution time
     */
    void recordRuleExecution(String ruleName, long executionTime);
    
    /**
     * Get rule execution time
     */
    long getRuleExecutionTime(String ruleName);
    
    /**
     * Get all rule execution times
     */
    Map<String, Long> getAllRuleExecutionTimes();
    
    // Validation state
    /**
     * Check if validation service is ready
     */
    boolean isReady();
    
    /**
     * Initialize the validation service with schemas
     */
    void initialize(List<CsdlSchema> schemas);
    
    /**
     * Reset the validation service state
     */
    void reset();
}
