package org.apache.olingo.xmlprocessor.core.model.ExtendedCsdlElement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.xmlprocessor.core.dependency.model.CsdlDependencyNode;
import org.apache.olingo.xmlprocessor.core.dependency.DependencyManager;
import org.apache.olingo.xmlprocessor.parser.context.XmlParsingContext;

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
     * Set parsing context for this element
     *
     * @param context parsing context
     * @return this element for fluent interface
     */
    default ExtendedCsdlElement setParsingContext(XmlParsingContext context) {
        // Default implementation - each concrete class can override if needed
        return this;
    }

    /**
     * Get parsing context for this element
     *
     * @return parsing context, may be null if not set
     */
    default XmlParsingContext getParsingContext() {
        // Default implementation returns null - each concrete class should maintain its own context
        // This is a design choice to keep the interface simple while allowing flexibility
        return null;
    }

    /**
     * Add dependency relationship with full parameters
     * 
     * @param propertyName property name that creates the dependency
     * @param targetFqn target fully qualified name
     * @param dependencyType dependency type
     */
    default void addDependency(String propertyName, FullQualifiedName targetFqn, CsdlDependencyNode.DependencyType dependencyType) {
        XmlParsingContext context = getParsingContext();
        if (context != null) {
            DependencyManager manager = context.getDependencyManager();
            String targetId = targetFqn != null ? targetFqn.toString() : "unknown";
            manager.addDependency(getElementId(), targetId);
        }
    }
    
    /**
     * Remove dependency relationship by property name
     * 
     * @param propertyName property name
     * @return true if removed successfully
     */
    default boolean removeDependency(String propertyName) {
        XmlParsingContext context = getParsingContext();
        if (context != null) {
            DependencyManager manager = context.getDependencyManager();
            Set<CsdlDependencyNode> dependencies = getDirectDependencies();
            for (CsdlDependencyNode dep : dependencies) {
                if (propertyName.equals(dep.getElementId())) {
                    return manager.removeDependency(getElementId(), dep.getElementId());
                }
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
        XmlParsingContext context = getParsingContext();
        if (context != null) {
            return context.getDependencyManager().getDirectDependencies(getElementId());
        }
        return new HashSet<>();
    }
    
    /**
     * Get direct dependents as nodes
     * 
     * @return set of directly dependent nodes
     */
    default Set<CsdlDependencyNode> getDirectDependents() {
        XmlParsingContext context = getParsingContext();
        if (context != null) {
            return context.getDependencyManager().getDirectDependents(getElementId());
        }
        return new HashSet<>();
    }
    
    /**
     * Get all dependencies as nodes (recursive)
     * 
     * @return set of all dependency nodes
     */
    default Set<CsdlDependencyNode> getAllDependencies() {
        XmlParsingContext context = getParsingContext();
        if (context != null) {
            return context.getDependencyManager().getAllDependencies(getElementId());
        }
        return new HashSet<>();
    }
    
    /**
     * Get all dependents as nodes (recursive)
     * 
     * @return set of all dependent nodes
     */
    default Set<CsdlDependencyNode> getAllDependents() {
        XmlParsingContext context = getParsingContext();
        if (context != null) {
            return context.getDependencyManager().getAllDependents(getElementId());
        }
        return new HashSet<>();
    }
    
    /**
     * Get dependency path to target
     * 
     * @param targetId target element ID
     * @return dependency path, or null if not found
     */
    default List<CsdlDependencyNode> getDependencyPath(String targetId) {
        XmlParsingContext context = getParsingContext();
        if (context != null) {
            return context.getDependencyManager().getDependencyPath(getElementId(), targetId);
        }
        return null;
    }
    
    /**
     * Check if has circular dependency
     * 
     * @return true if has circular dependency
     */
    default boolean hasCircularDependency() {
        XmlParsingContext context = getParsingContext();
        if (context != null) {
            return context.getDependencyManager().hasCircularDependency(getElementId());
        }
        return false;
    }
    
    /**
     * Get self node
     * 
     * @return self dependency node
     */
    default CsdlDependencyNode getSelfNode() {
        XmlParsingContext context = getParsingContext();
        if (context != null) {
            return context.getDependencyManager().getElement(getElementId());
        }
        return null;
    }
    
    /**
     * Get dependencies by type
     * 
     * @param dependencyType dependency type
     * @return set of nodes with specified type
     */
    default Set<CsdlDependencyNode> getDependenciesByType(CsdlDependencyNode.DependencyType dependencyType) {
        XmlParsingContext context = getParsingContext();
        if (context != null) {
            return context.getDependencyManager().getElementsByType(dependencyType);
        }
        return new HashSet<>();
    }
    
    /**
     * Get dependencies by namespace
     * 
     * @param namespace namespace
     * @return set of nodes with specified namespace
     */
    default Set<CsdlDependencyNode> getDependenciesByNamespace(String namespace) {
        XmlParsingContext context = getParsingContext();
        if (context != null) {
            return context.getDependencyManager().getElementsByNamespace(namespace);
        }
        return new HashSet<>();
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
        XmlParsingContext context = getParsingContext();
        if (context != null) {
            DependencyManager manager = context.getDependencyManager();
            CsdlDependencyNode node = manager.getElement(getElementId());
            if (node != null) {
                Set<CsdlDependencyNode> dependencies = new HashSet<>(node.getDependencies());
                for (CsdlDependencyNode dependency : dependencies) {
                    manager.removeDependency(getElementId(), dependency.getElementId());
                }
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
        XmlParsingContext context = getParsingContext();
        if (context != null) {
            return context.getDependencyManager().getElement(getElementId());
        }
        return null;
    }

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
     * Set namespace for the element
     *
     * @param namespace namespace to set
     * @return this element for fluent interface
     */
    ExtendedCsdlElement setNamespace(String namespace);

    /**
     * Get namespace for the element
     *
     * @return namespace of the element
     */
    String getNamespace();

    /**
     * Register element in dependency manager
     *
     * @return this element for fluent interface
     */
    default ExtendedCsdlElement registerElement() {
        XmlParsingContext context = getParsingContext();
        if (context != null) {
            context.getDependencyManager().registerElement(
                getElementId(),
                getElementFullyQualifiedName(),
                getElementDependencyType(),
                getNamespace()
            );
        }
        return this;
    }
    
    /**
     * Add dependency by namespace (simplified)
     *
     * @param namespace namespace to add as dependency
     * @return true if added successfully
     */
    default boolean addDependency(String namespace) {
        if (namespace != null && !namespace.trim().isEmpty()) {
            XmlParsingContext context = getParsingContext();
            if (context != null) {
                return context.getDependencyManager().addDependency(getElementId(), namespace);
            }
        }
        return false;
    }

    /**
     * Set parent name for elements that support it
     *
     * @param parentName parent name to set
     * @return this element for fluent interface
     */
    default ExtendedCsdlElement setParentName(String parentName) {
        // Default implementation - do nothing for elements that don't support parent names
        return this;
    }
    
    /**
     * Get parent name for elements that support it
     *
     * @return parent name or null
     */
    default String getParentName() {
        // Default implementation - return null for elements that don't support parent names
        return null;
    }
}
