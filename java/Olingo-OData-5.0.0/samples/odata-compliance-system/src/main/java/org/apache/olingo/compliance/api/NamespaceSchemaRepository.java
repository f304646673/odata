package org.apache.olingo.compliance.api;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;

/**
 * Repository for storing and retrieving merged OData schema information by namespace.
 * Merges schema elements from multiple files into consolidated namespace-based schemas.
 */
public interface NamespaceSchemaRepository {
    
    /**
     * Merges a schema into the namespace-based repository.
     * If the namespace already exists, elements are merged; otherwise, a new namespace entry is created.
     *
     * @param schema The schema to merge
     * @param sourceFilePath Source file path for tracking purposes
     */
    void mergeSchema(CsdlSchema schema, String sourceFilePath);
    
    /**
     * Retrieves the merged schema for a specific namespace.
     *
     * @param namespace The namespace to retrieve
     * @return Optional containing the merged schema if found
     */
    Optional<CsdlSchema> getSchemaByNamespace(String namespace);
    
    /**
     * Gets all registered namespaces.
     *
     * @return Set of all registered namespaces
     */
    Set<String> getAllNamespaces();
    
    /**
     * Gets all merged schemas from all namespaces.
     *
     * @return List of all merged schemas
     */
    List<CsdlSchema> getAllSchemas();
    
    /**
     * Checks if a namespace is registered.
     *
     * @param namespace Namespace to check
     * @return true if registered, false otherwise
     */
    boolean containsNamespace(String namespace);
    
    /**
     * Gets the source file paths that contributed to a namespace.
     *
     * @param namespace The namespace to query
     * @return List of source file paths
     */
    List<String> getSourceFilePaths(String namespace);
    
    /**
     * Removes a namespace and all its associated schema information.
     *
     * @param namespace Namespace to remove
     * @return true if removed, false if not found
     */
    boolean removeNamespace(String namespace);
    
    /**
     * Removes contribution from a specific source file path.
     * If this was the only contributor to a namespace, the namespace is removed entirely.
     *
     * @param sourceFilePath Source file path to remove
     * @return Set of namespaces that were affected
     */
    Set<String> removeSourceFilePath(String sourceFilePath);
    
    /**
     * Clears all stored schemas from the repository.
     */
    void clear();
    
    /**
     * Gets the total number of registered namespaces.
     *
     * @return Number of registered namespaces
     */
    int size();
}
