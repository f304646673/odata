package org.apache.olingo.compliance.api;

import java.util.List;
import java.util.Set;

/**
 * Manager for analyzing and maintaining dependency trees between OData schema elements.
 * Provides both dependency (what this element depends on) and reverse dependency 
 * (what depends on this element) information using references for automatic updates.
 */
public interface DependencyTreeManager {
    
    /**
     * Represents a schema element with its namespace and name.
     */
    interface ElementReference {
        String getNamespace();
        String getName();
        String getFullyQualifiedName();
        ElementType getType();
    }
    
    /**
     * Types of schema elements that can have dependencies.
     */
    enum ElementType {
        ENTITY_TYPE,
        COMPLEX_TYPE,
        ENUM_TYPE,
        TYPE_DEFINITION,
        ENTITY_SET,
        SINGLETON,
        ACTION,
        FUNCTION,
        TERM
    }
    
    /**
     * Represents a dependency tree node with references to related elements.
     */
    interface DependencyNode {
        ElementReference getElement();
        List<DependencyNode> getDependencies();
        List<DependencyNode> getReverseDependencies();
        int getDepth();
        boolean hasCyclicDependency();
    }
    
    /**
     * Builds dependency trees for all elements in a namespace.
     *
     * @param namespace The namespace to analyze
     */
    void buildDependencyTrees(String namespace);
    
    /**
     * Gets the dependency tree for a specific element.
     * Returns what this element depends on.
     *
     * @param namespace Element namespace
     * @param elementName Element name
     * @return Dependency tree node, or null if element not found
     */
    DependencyNode getDependencyTree(String namespace, String elementName);
    
    /**
     * Gets the reverse dependency tree for a specific element.
     * Returns what depends on this element.
     *
     * @param namespace Element namespace  
     * @param elementName Element name
     * @return Reverse dependency tree node, or null if element not found
     */
    DependencyNode getReverseDependencyTree(String namespace, String elementName);
    
    /**
     * Gets all elements that have dependencies.
     *
     * @return Set of element references with dependencies
     */
    Set<ElementReference> getAllElementsWithDependencies();
    
    /**
     * Detects circular dependencies in the dependency trees.
     *
     * @return List of element reference chains that form cycles
     */
    List<List<ElementReference>> detectCircularDependencies();
    
    /**
     * Refreshes dependency trees after schema changes.
     * This method is called automatically when schemas are updated through references.
     *
     * @param affectedNamespaces Namespaces that were modified
     */
    void refreshDependencyTrees(Set<String> affectedNamespaces);
    
    /**
     * Clears all dependency information.
     */
    void clear();
    
    /**
     * Gets dependency statistics.
     *
     * @return Dependency statistics information
     */
    DependencyStatistics getStatistics();
    
    /**
     * Statistics about dependency analysis.
     */
    interface DependencyStatistics {
        int getTotalElements();
        int getElementsWithDependencies();
        int getElementsWithReverseDependencies();
        int getCircularDependencyCount();
        int getMaxDependencyDepth();
        double getAverageDependencyDepth();
    }
}
