package org.apache.olingo.compliance.engine.core;

import java.util.List;
import java.util.Set;

/**
 * Schema Registry interface for OData schema management and validation.
 * Provides schema registration, type lookup, and validation operations.
 */
public interface SchemaRegistry {
    
    /**
     * Register a schema definition
     */
    void registerSchema(SchemaDefinition schema);
    
    /**
     * Check if a type exists
     * @param typeName type name, can be "namespace.TypeName" or "alias.TypeName"
     */
    boolean isTypeExists(String typeName);
    
    /**
     * Check if a namespace is defined
     */
    boolean isNamespaceDefined(String namespace);
    
    /**
     * Check if a type is defined in the given namespace
     */
    boolean isTypeDefined(String namespace, String typeName);
    
    /**
     * Check if a file exists
     */
    boolean isFileExists(String fileName);
    
    /**
     * Get type definition
     */
    TypeDefinition getTypeDefinition(String typeName);
    
    /**
     * Check if base type is valid
     */
    boolean isValidBaseType(String typeName, String baseTypeName);
    
    /**
     * Get all namespaces
     */
    Set<String> getAllNamespaces();
    
    /**
     * Get all types in a namespace
     */
    Set<String> getTypesInNamespace(String namespace);
    
    /**
     * Get all file names
     */
    Set<String> getAllFileNames();
    
    /**
     * Get schema definition by namespace
     */
    SchemaDefinition getSchema(String namespace);
    
    /**
     * Get schemas for a file
     */
    Set<SchemaDefinition> getSchemasForFile(String filePath);
    
    /**
     * Get schema by namespace
     */
    SchemaDefinition getSchemaByNamespace(String namespace);
    
    /**
     * Check if schema exists for a file
     */
    boolean hasSchemaForFile(String filePath);
    
    /**
     * Check if namespace exists
     */
    boolean hasNamespace(String namespace);
    
    /**
     * Add a single schema
     */
    void addSchema(SchemaDefinition schema);
    
    /**
     * Merge another registry
     */
    void merge(SchemaRegistry other);
    
    /**
     * Get registry statistics
     */
    RegistryStatistics getStatistics();
    
    /**
     * Add schemas from a list
     */
    void addSchemas(List<org.apache.olingo.commons.api.edm.provider.CsdlSchema> schemas);
    
    /**
     * Reset the registry
     */
    void reset();
    
    /**
     * Schema definition interface
     */
    interface SchemaDefinition {
        String getNamespace();
        String getAlias();
        String getFilePath();
        List<TypeDefinition> getTypes();
    }
    
    /**
     * Type definition interface
     */
    interface TypeDefinition {
        String getName();
        String getKind();
        String getBaseType();
    }
    
    /**
     * Registry statistics interface
     */
    interface RegistryStatistics {
        int getNamespaceCount();
        int getTotalTypes();
        int getEntityTypes();
        int getComplexTypes();
        int getFileCount();
    }
}