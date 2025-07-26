package org.apache.olingo.schemamanager.analyzer;

import java.util.List;
import java.util.Set;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;

/**
 * Analyzer for building and querying dependency trees of schema elements.
 * This analyzer creates a hierarchical dependency structure where each schema element
 * and its sub-components (properties, parameters, etc.) can be tracked for dependencies.
 */
public interface DependencyTreeAnalyzer {
    
    /**
     * Builds a dependency tree for all elements in the given schemas
     * @param schemas the schemas to analyze
     * @return the root dependency tree node containing all dependencies
     */
    DependencyTreeNode buildDependencyTree(List<CsdlSchema> schemas);
    
    /**
     * Gets all dependencies (direct and transitive) for a schema element
     * @param elementName the fully qualified name of the schema element
     * @return list of all dependency tree nodes that this element depends on
     */
    List<DependencyTreeNode> getAllDependencies(String elementName);
    
    /**
     * Gets all dependents (elements that depend on this element) 
     * @param elementName the fully qualified name of the schema element
     * @return list of all dependency tree nodes that depend on this element
     */
    List<DependencyTreeNode> getAllDependents(String elementName);
    
    /**
     * Gets the dependency tree node for a specific element
     * @param elementName the fully qualified name of the schema element
     * @return the dependency tree node, or null if not found
     */
    DependencyTreeNode getDependencyTreeNode(String elementName);
    
    /**
     * Checks if there are circular dependencies in the tree
     * @return list of circular dependency paths found
     */
    List<List<String>> detectCircularDependencies();
    
    /**
     * Gets the impact analysis when an element changes
     * @param elementName the element that changed
     * @return impact analysis result containing affected elements
     */
    ImpactAnalysis getImpactAnalysis(String elementName);
    
    /**
     * Finds the dependency path between two elements
     * @param fromElement source element
     * @param toElement target element
     * @return the dependency path, or empty list if no path exists
     */
    List<String> findDependencyPath(String fromElement, String toElement);
    
    /**
     * Gets all leaf dependencies (elements with no further dependencies)
     * @param elementName the element to analyze
     * @return set of leaf dependency element names
     */
    Set<String> getLeafDependencies(String elementName);
    
    /**
     * Gets dependency statistics for the entire tree
     * @return dependency statistics
     */
    DependencyStatistics getDependencyStatistics();
}
