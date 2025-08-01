package org.apache.olingo.xmlprocessor.core.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.xmlprocessor.core.dependency.CsdlDependencyNode;
import org.apache.olingo.xmlprocessor.core.dependency.GlobalDependencyManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Extended CSDL element interface for dependency tracking
 * Unified dependency management interface for all Extended* classes
 */
public interface ExtendedCsdlElement {
    
    /**
     * Get element ID
     * 
     * @return unique identifier of the element
     */
    String getElementId();
    
    /**
     * Get element's fully qualified name
     * 
     * @return fully qualified name of the element
     */
    FullQualifiedName getElementFullyQualifiedName();
    
    /**
     * Get element's dependency type
     * 
     * @return dependency type of the element
     */
    CsdlDependencyNode.DependencyType getElementDependencyType();
    
    /**
     * Get element's property name (if applicable)
     * 
     * @return property name or null
     */
    String getElementPropertyName();
    
    /**
     * Add dependency relationship with full parameters
     * 
     * @param propertyName property name that creates the dependency
     * @param targetFqn target fully qualified name
     * @param dependencyType dependency type
     */
    default void addDependency(String propertyName, FullQualifiedName targetFqn, CsdlDependencyNode.DependencyType dependencyType) {
        // Register the dependency node if needed and add dependency
        String targetId = targetFqn != null ? targetFqn.toString() : "unknown";
        GlobalDependencyManager.getInstance().addDependency(getElementId(), targetId);
    }
    
    /**
     * Remove dependency relationship by property name
     * 
     * @param propertyName property name
     * @return true if removed successfully
     */
    default boolean removeDependency(String propertyName) {
        // For now, we'll remove by property name (simplified)
        Set<CsdlDependencyNode> dependencies = getDirectDependencies();
        for (CsdlDependencyNode dep : dependencies) {
            if (propertyName.equals(dep.getElementId())) {
                GlobalDependencyManager.getInstance().removeDependency(getElementId(), dep.getElementId());
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get direct dependencies as nodes
     * 
     * @return set of directly dependent nodes
     */
    default Set<CsdlDependencyNode> getDirectDependencies() {
        return GlobalDependencyManager.getInstance().getDirectDependencies(getElementId());
    }
    
    /**
     * Get direct dependents as nodes
     * 
     * @return set of directly dependent nodes
     */
    default Set<CsdlDependencyNode> getDirectDependents() {
        return GlobalDependencyManager.getInstance().getDirectDependents(getElementId());
    }
    
    /**
     * Get all dependencies as nodes (recursive)
     * 
     * @return set of all dependency nodes
     */
    default Set<CsdlDependencyNode> getAllDependencies() {
        return GlobalDependencyManager.getInstance().getAllDependencies(getElementId());
    }
    
    /**
     * Get all dependents as nodes (recursive)
     * 
     * @return set of all dependent nodes
     */
    default Set<CsdlDependencyNode> getAllDependents() {
        return GlobalDependencyManager.getInstance().getAllDependents(getElementId());
    }
    
    /**
     * Get dependency path to target
     * 
     * @param targetId target element ID
     * @return dependency path, or null if not found
     */
    default List<CsdlDependencyNode> getDependencyPath(String targetId) {
        return GlobalDependencyManager.getInstance().getDependencyPath(getElementId(), targetId);
    }
    
    /**
     * Check if has circular dependency
     * 
     * @return true if has circular dependency
     */
    default boolean hasCircularDependency() {
        return GlobalDependencyManager.getInstance().hasCircularDependency(getElementId());
    }
    
    /**
     * Get self node
     * 
     * @return self dependency node
     */
    default CsdlDependencyNode getSelfNode() {
        return GlobalDependencyManager.getInstance().getElement(getElementId());
    }
    
    /**
     * Get dependencies by type
     * 
     * @param dependencyType dependency type
     * @return set of nodes with specified type
     */
    default Set<CsdlDependencyNode> getDependenciesByType(CsdlDependencyNode.DependencyType dependencyType) {
        return GlobalDependencyManager.getInstance().getElementsByType(dependencyType);
    }
    
    /**
     * Get dependencies by namespace
     * 
     * @param namespace namespace
     * @return set of nodes with specified namespace
     */
    default Set<CsdlDependencyNode> getDependenciesByNamespace(String namespace) {
        return GlobalDependencyManager.getInstance().getElementsByNamespace(namespace);
    }
    
    /**
     * Get dependency count
     * 
     * @return number of dependencies
     */
    default int getDependencyCount() {
        return getDirectDependencies().size();
    }
    
    /**
     * Get dependencies as nodes (alias for compatibility)
     * 
     * @return set of dependency nodes
     */
    default Set<CsdlDependencyNode> getDependencies() {
        return getDirectDependencies();
    }
    
    /**
     * Clear all dependency relationships
     */
    default void clearDependencies() {
        CsdlDependencyNode node = GlobalDependencyManager.getInstance().getElement(getElementId());
        if (node != null) {
            // Remove all outgoing dependencies
            Set<CsdlDependencyNode> dependencies = new HashSet<>(node.getDependencies());
            for (CsdlDependencyNode dependency : dependencies) {
                GlobalDependencyManager.getInstance().removeDependency(getElementId(), dependency.getElementId());
            }
        }
    }
    
    /**
     * Check if depends on specified element
     * 
     * @param elementId element ID to check
     * @return true if depends on specified element
     */
    default boolean dependsOn(String elementId) {
        return getAllDependencies().stream()
                .anyMatch(node -> elementId.equals(node.getElementId()));
    }
    
    /**
     * Check if is root node (depends on no other elements)
     * 
     * @return true if is root node
     */
    default boolean isRoot() {
        return getDirectDependencies().isEmpty();
    }
    
    /**
     * Check if is leaf node (no other elements depend on it)
     * 
     * @return true if is leaf node
     */
    default boolean isLeaf() {
        return getDirectDependents().isEmpty();
    }
    
    /**
     * Get detailed information of dependency node
     * 
     * @return dependency node object, null if not exists
     */
    default CsdlDependencyNode getDependencyNode() {
        return GlobalDependencyManager.getInstance().getElement(getElementId());
    }
    
    // === Additional methods for test compatibility ===
    
    /**
     * Check if has dependency (using dependsOn logic)
     * 
     * @param elementId element ID to check
     * @return true if depends on specified element
     */
    default boolean hasDependency(String elementId) {
        return dependsOn(elementId);
    }
    
    /**
     * Check if has dependency path to target
     * 
     * @param targetId target element ID
     * @return true if path exists
     */
    default boolean hasDependencyPath(String targetId) {
        return getDependencyPath(targetId) != null && !getDependencyPath(targetId).isEmpty();
    }
    
    /**
     * Get dependent count (number of elements that depend on this)
     * 
     * @return number of dependents
     */
    default int getDependentCount() {
        return getDirectDependents().size();
    }
    
    // === Legacy namespace support (placeholder for compatibility) ===
    
    /**
     * Set namespace (placeholder implementation for test compatibility)
     * 
     * @param namespace namespace to set
     * @return this element for chaining
     */
    default ExtendedCsdlElement setNamespace(String namespace) {
        // Since we're now using global management, this is a no-op
        // The namespace is derived from the element's context
        return this;
    }
    
    /**
     * Get namespace (placeholder implementation for test compatibility)
     * 
     * @return namespace or null
     */
    default String getNamespace() {
        // Try to extract namespace from dependency node
        CsdlDependencyNode node = getDependencyNode();
        if (node != null && node.getFullyQualifiedName() != null) {
            return node.getFullyQualifiedName().getNamespace();
        }
        return null;
    }
    
    /**
     * Register element with global dependency manager
     * 
     * @return this element for chaining
     */
    default ExtendedCsdlElement registerElement() {
        // Ensure the element is registered with the global manager
        // This creates a node if it doesn't exist using the element's specific dependency type
        GlobalDependencyManager.getInstance().registerElement(getElementId(), 
            getElementFullyQualifiedName(), 
            getElementDependencyType(), getElementPropertyName());
        return this;
    }
    
    /**
     * Set parent name (for parameter-like elements, placeholder for compatibility)
     * 
     * @param parentName parent name
     * @return this element for chaining  
     */
    default ExtendedCsdlElement setParentName(String parentName) {
        // This is a placeholder for test compatibility
        // The parent relationship is handled through dependency relationships
        return this;
    }
    
    /**
     * Get parent name (placeholder for compatibility)
     * 
     * @return parent name or null
     */
    default String getParentName() {
        // This is a placeholder for test compatibility
        return null;
    }
    
    /**
     * Add dependency by element ID (simplified version for tests)
     * 
     * @param elementId target element ID
     * @return true if added successfully
     */
    default boolean addDependency(String elementId) {
        return GlobalDependencyManager.getInstance().addDependency(getElementId(), elementId);
    }
}
