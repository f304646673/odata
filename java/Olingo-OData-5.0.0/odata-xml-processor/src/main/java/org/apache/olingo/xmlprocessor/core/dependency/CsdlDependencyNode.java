package org.apache.olingo.xmlprocessor.core.dependency;

import org.apache.olingo.commons.api.edm.FullQualifiedName;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * CSDL依赖节点，用于表示元素间的依赖关系
 */
public class CsdlDependencyNode {

    /**
     * 依赖类型枚举
     */
    public enum DependencyType {
        TYPE_REFERENCE,
        ENTITY_TYPE,
        COMPLEX_TYPE,
        ENUM_TYPE,
        TYPE_DEFINITION,
        ACTION_REFERENCE,
        FUNCTION_REFERENCE,
        TERM_REFERENCE,
        ENTITY_SET_REFERENCE,
        SINGLETON_REFERENCE,
        ACTION_IMPORT_REFERENCE,
        FUNCTION_IMPORT_REFERENCE,
        PROPERTY_REFERENCE,
        NAVIGATION_PROPERTY_REFERENCE,
        PARAMETER_REFERENCE,
        RETURN_TYPE_REFERENCE,
        ANNOTATION_REFERENCE,
        PROPERTY,  // 添加缺失的PROPERTY常量
        NAVIGATION_PROPERTY  // 添加缺失的NAVIGATION_PROPERTY常量
    }

    private final String elementId;
    private final FullQualifiedName fullyQualifiedName;
    private final DependencyType dependencyType;
    private final String namespace;
    private final String propertyName;

    private final Set<CsdlDependencyNode> dependencies = new HashSet<>();
    private final Set<CsdlDependencyNode> dependents = new HashSet<>();

    /**
     * 构造函数
     */
    public CsdlDependencyNode(String elementId, FullQualifiedName fullyQualifiedName,
                              DependencyType dependencyType, String namespace, String propertyName) {
        this.elementId = elementId;
        this.fullyQualifiedName = fullyQualifiedName;
        this.dependencyType = dependencyType;
        this.namespace = namespace;
        this.propertyName = propertyName;
    }

    /**
     * 简化构造函数
     */
    public CsdlDependencyNode(String elementId, DependencyType dependencyType) {
        this(elementId, null, dependencyType, null, null);
    }

    // Getters
    public String getElementId() {
        return elementId;
    }

    public FullQualifiedName getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    public DependencyType getDependencyType() {
        return dependencyType;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Set<CsdlDependencyNode> getDependencies() {
        return new HashSet<>(dependencies);
    }

    public Set<CsdlDependencyNode> getDependents() {
        return new HashSet<>(dependents);
    }

    // 依赖关系管理
    public boolean addDependency(CsdlDependencyNode dependency) {
        if (dependency != null && !dependency.equals(this)) {
            boolean added = dependencies.add(dependency);
            if (added) {
                dependency.dependents.add(this);
            }
            return added;
        }
        return false;
    }

    public boolean removeDependency(CsdlDependencyNode dependency) {
        if (dependency != null) {
            boolean removed = dependencies.remove(dependency);
            if (removed) {
                dependency.dependents.remove(this);
            }
            return removed;
        }
        return false;
    }

    public boolean dependsOn(CsdlDependencyNode node) {
        return dependencies.contains(node);
    }

    public boolean isDependentOf(CsdlDependencyNode node) {
        return dependents.contains(node);
    }

    // 循环依赖检测
    public boolean hasCircularDependency() {
        return hasCircularDependency(new HashSet<>());
    }

    private boolean hasCircularDependency(Set<CsdlDependencyNode> visited) {
        if (visited.contains(this)) {
            return true;
        }

        visited.add(this);
        for (CsdlDependencyNode dependency : dependencies) {
            if (dependency.hasCircularDependency(visited)) {
                return true;
            }
        }
        visited.remove(this);
        return false;
    }

    /**
     * 获取所有依赖（递归）
     */
    public Set<CsdlDependencyNode> getAllDependencies() {
        Set<CsdlDependencyNode> allDeps = new HashSet<>();
        getAllDependencies(allDeps, new HashSet<>());
        return allDeps;
    }

    private void getAllDependencies(Set<CsdlDependencyNode> result, Set<CsdlDependencyNode> visited) {
        if (visited.contains(this)) {
            return; // 避免循环依赖导致的无限递归
        }
        visited.add(this);

        for (CsdlDependencyNode dependency : dependencies) {
            result.add(dependency);
            dependency.getAllDependencies(result, visited);
        }

        visited.remove(this);
    }

    /**
     * 获取所有依赖者（递归）
     */
    public Set<CsdlDependencyNode> getAllDependents() {
        Set<CsdlDependencyNode> allDeps = new HashSet<>();
        getAllDependents(allDeps, new HashSet<>());
        return allDeps;
    }

    private void getAllDependents(Set<CsdlDependencyNode> result, Set<CsdlDependencyNode> visited) {
        if (visited.contains(this)) {
            return; // 避免循环依赖导致的无限递归
        }
        visited.add(this);

        for (CsdlDependencyNode dependent : dependents) {
            result.add(dependent);
            dependent.getAllDependents(result, visited);
        }

        visited.remove(this);
    }

    /**
     * 获取到目标节点的依赖路径
     */
    public java.util.List<CsdlDependencyNode> getDependencyPath(CsdlDependencyNode target) {
        java.util.List<CsdlDependencyNode> path = new java.util.ArrayList<>();
        if (findDependencyPath(target, path, new HashSet<>())) {
            return path;
        }
        return null; // 没有找到路径
    }

    private boolean findDependencyPath(CsdlDependencyNode target, java.util.List<CsdlDependencyNode> path, Set<CsdlDependencyNode> visited) {
        if (visited.contains(this)) {
            return false; // 避免循环
        }

        path.add(this);
        visited.add(this);

        if (this.equals(target)) {
            return true; // 找到目标
        }

        for (CsdlDependencyNode dependency : dependencies) {
            if (dependency.findDependencyPath(target, path, visited)) {
                return true;
            }
        }

        path.remove(path.size() - 1); // 回溯
        visited.remove(this);
        return false;
    }
}
