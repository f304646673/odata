package org.apache.olingo.schemamanager.analyzer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a node in the dependency tree structure.
 * Each node can represent a schema element or a sub-component of a schema element.
 */
public class DependencyTreeNode {
    
    public enum ElementType {
        ENTITY_TYPE,
        COMPLEX_TYPE,
        ENUM_TYPE,
        ACTION,
        FUNCTION,
        PROPERTY,
        NAVIGATION_PROPERTY,
        PARAMETER,
        RETURN_TYPE,
        BASE_TYPE,
        KEY
    }
    
    private final String elementName;
    private final String fullQualifiedName;
    private final ElementType elementType;
    private final String parentElementName;
    private final List<DependencyTreeNode> dependencies;
    private final List<DependencyTreeNode> dependents;
    private final Set<String> tags;
    private final Object metadata;
    
    public DependencyTreeNode(String elementName, String fullQualifiedName, 
                             ElementType elementType, String parentElementName) {
        this.elementName = elementName;
        this.fullQualifiedName = fullQualifiedName;
        this.elementType = elementType;
        this.parentElementName = parentElementName;
        this.dependencies = new ArrayList<>();
        this.dependents = new ArrayList<>();
        this.tags = new HashSet<>();
        this.metadata = null;
    }
    
    public DependencyTreeNode(String elementName, String fullQualifiedName, 
                             ElementType elementType, String parentElementName, Object metadata) {
        this.elementName = elementName;
        this.fullQualifiedName = fullQualifiedName;
        this.elementType = elementType;
        this.parentElementName = parentElementName;
        this.dependencies = new ArrayList<>();
        this.dependents = new ArrayList<>();
        this.tags = new HashSet<>();
        this.metadata = metadata;
    }
    
    /**
     * Adds a dependency to this node
     */
    public void addDependency(DependencyTreeNode dependency) {
        if (dependency != null && !dependencies.contains(dependency)) {
            dependencies.add(dependency);
            dependency.addDependent(this);
        }
    }
    
    /**
     * Adds a dependent to this node (internal method)
     */
    private void addDependent(DependencyTreeNode dependent) {
        if (dependent != null && !dependents.contains(dependent)) {
            dependents.add(dependent);
        }
    }
    
    /**
     * Removes a dependency from this node
     */
    public void removeDependency(DependencyTreeNode dependency) {
        if (dependency != null) {
            dependencies.remove(dependency);
            dependency.removeDependent(this);
        }
    }
    
    /**
     * Removes a dependent from this node (internal method)
     */
    private void removeDependent(DependencyTreeNode dependent) {
        if (dependent != null) {
            dependents.remove(dependent);
        }
    }
    
    /**
     * Gets all dependencies (direct only)
     */
    public List<DependencyTreeNode> getDependencies() {
        return new ArrayList<>(dependencies);
    }
    
    /**
     * Gets all dependents (direct only)
     */
    public List<DependencyTreeNode> getDependents() {
        return new ArrayList<>(dependents);
    }
    
    /**
     * Gets all transitive dependencies
     */
    public List<DependencyTreeNode> getAllDependencies() {
        List<DependencyTreeNode> allDeps = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        collectAllDependencies(allDeps, visited);
        return allDeps;
    }
    
    /**
     * Gets all transitive dependents
     */
    public List<DependencyTreeNode> getAllDependents() {
        List<DependencyTreeNode> allDeps = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        collectAllDependents(allDeps, visited);
        return allDeps;
    }
    
    private void collectAllDependencies(List<DependencyTreeNode> allDeps, Set<String> visited) {
        if (visited.contains(this.fullQualifiedName)) {
            return; // Avoid circular dependencies
        }
        visited.add(this.fullQualifiedName);
        
        for (DependencyTreeNode dep : dependencies) {
            if (!allDeps.contains(dep)) {
                allDeps.add(dep);
            }
            dep.collectAllDependencies(allDeps, visited);
        }
    }
    
    private void collectAllDependents(List<DependencyTreeNode> allDeps, Set<String> visited) {
        if (visited.contains(this.fullQualifiedName)) {
            return; // Avoid circular dependencies
        }
        visited.add(this.fullQualifiedName);
        
        for (DependencyTreeNode dep : dependents) {
            if (!allDeps.contains(dep)) {
                allDeps.add(dep);
            }
            dep.collectAllDependents(allDeps, visited);
        }
    }
    
    /**
     * Checks if this node has any dependencies
     */
    public boolean hasDependencies() {
        return !dependencies.isEmpty();
    }
    
    /**
     * Checks if this node has any dependents
     */
    public boolean hasDependents() {
        return !dependents.isEmpty();
    }
    
    /**
     * Checks if this node is a leaf (no dependencies)
     */
    public boolean isLeaf() {
        return dependencies.isEmpty();
    }
    
    /**
     * Checks if this node is a root (no dependents)
     */
    public boolean isRoot() {
        return dependents.isEmpty();
    }
    
    /**
     * Adds a tag to this node
     */
    public void addTag(String tag) {
        if (tag != null) {
            tags.add(tag);
        }
    }
    
    /**
     * Removes a tag from this node
     */
    public void removeTag(String tag) {
        tags.remove(tag);
    }
    
    /**
     * Checks if this node has a specific tag
     */
    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }
    
    /**
     * Gets the depth of this node in the dependency tree
     */
    public int getDepth() {
        if (dependencies.isEmpty()) {
            return 0;
        }
        
        int maxDepth = 0;
        Set<String> visited = new HashSet<>();
        return calculateDepth(visited);
    }
    
    private int calculateDepth(Set<String> visited) {
        if (visited.contains(this.fullQualifiedName)) {
            return 0; // Avoid infinite recursion in circular dependencies
        }
        visited.add(this.fullQualifiedName);
        
        if (dependencies.isEmpty()) {
            return 0;
        }
        
        int maxDepth = 0;
        for (DependencyTreeNode dep : dependencies) {
            int depth = dep.calculateDepth(new HashSet<>(visited));
            maxDepth = Math.max(maxDepth, depth + 1);
        }
        
        return maxDepth;
    }
    
    // Getters
    public String getElementName() {
        return elementName;
    }
    
    public String getFullQualifiedName() {
        return fullQualifiedName;
    }
    
    public ElementType getElementType() {
        return elementType;
    }
    
    public String getParentElementName() {
        return parentElementName;
    }
    
    public Set<String> getTags() {
        return new HashSet<>(tags);
    }
    
    public Object getMetadata() {
        return metadata;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DependencyTreeNode that = (DependencyTreeNode) obj;
        return Objects.equals(fullQualifiedName, that.fullQualifiedName) &&
               elementType == that.elementType;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(fullQualifiedName, elementType);
    }
    
    @Override
    public String toString() {
        return String.format("DependencyTreeNode{name='%s', type=%s, deps=%d, dependents=%d}", 
                           fullQualifiedName, elementType, dependencies.size(), dependents.size());
    }
}
