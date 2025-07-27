package org.apache.olingo.schema.processor.model.dependency;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmItem;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotatable;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlNamed;

/**
 * 表示Schema Element依赖关系的节点，继承自Olingo的CsdlAbstractEdmItem
 * 支持树状结构来表示dependencies和dependents
 */
public class CsdlDependencyNode extends CsdlAbstractEdmItem implements CsdlNamed, CsdlAnnotatable {
    
    private String name;
    private FullQualifiedName fullyQualifiedName;
    private DependencyType dependencyType;
    private String propertyName; // 产生依赖的属性名
    
    // 树状结构：当前节点依赖的其他节点
    private final Set<CsdlDependencyNode> dependencies = new HashSet<>();
    
    // 树状结构：依赖当前节点的其他节点
    private final Set<CsdlDependencyNode> dependents = new HashSet<>();
    
    // 注解支持
    private List<CsdlAnnotation> annotations = new ArrayList<>();
    
    public enum DependencyType {
        TYPE_REFERENCE,      // 类型引用
        BASE_TYPE,          // 基类型
        ENTITY_SET,         // EntitySet引用
        ENTITY_TYPE,        // EntityType引用
        ACTION_REFERENCE,   // Action引用
        FUNCTION_REFERENCE, // Function引用
        NAVIGATION_TARGET,  // 导航目标
        PARAMETER_TYPE,     // 参数类型
        RETURN_TYPE         // 返回类型
    }
    
    /**
     * 构造函数
     */
    public CsdlDependencyNode(String name, FullQualifiedName fullyQualifiedName, 
                             DependencyType dependencyType, String propertyName) {
        this.fullyQualifiedName = fullyQualifiedName;
        this.name = (fullyQualifiedName != null && fullyQualifiedName.getName() != null) ? 
                    fullyQualifiedName.getName() : name;
        this.dependencyType = dependencyType;
        this.propertyName = propertyName;
    }
    
    /**
     * 简化构造函数
     */
    public CsdlDependencyNode(FullQualifiedName fullyQualifiedName, DependencyType dependencyType) {
        this(null, fullyQualifiedName, dependencyType, null);
    }
    
    /**
     * 添加依赖节点
     * @param dependency 被依赖的节点
     */
    public void addDependency(CsdlDependencyNode dependency) {
        if (dependency != null && !dependency.equals(this)) {
            dependencies.add(dependency);
            dependency.dependents.add(this);
        }
    }
    
    /**
     * 移除依赖节点
     * @param dependency 要移除的依赖节点
     * @return 是否成功移除
     */
    public boolean removeDependency(CsdlDependencyNode dependency) {
        boolean removed = dependencies.remove(dependency);
        if (removed && dependency != null) {
            dependency.dependents.remove(this);
        }
        return removed;
    }
    
    /**
     * 添加被依赖关系（由其他节点调用）
     * @param dependent 依赖当前节点的节点
     */
    public void addDependent(CsdlDependencyNode dependent) {
        if (dependent != null && !dependent.equals(this)) {
            dependents.add(dependent);
            dependent.dependencies.add(this);
        }
    }
    
    /**
     * 移除被依赖关系
     * @param dependent 要移除的依赖者节点
     * @return 是否成功移除
     */
    public boolean removeDependent(CsdlDependencyNode dependent) {
        boolean removed = dependents.remove(dependent);
        if (removed && dependent != null) {
            dependent.dependencies.remove(this);
        }
        return removed;
    }
    
    /**
     * 递归获取所有依赖节点（深度优先）
     * @return 所有直接和间接依赖的节点
     */
    public Set<CsdlDependencyNode> getAllDependencies() {
        Set<CsdlDependencyNode> result = new HashSet<>();
        Set<CsdlDependencyNode> visited = new HashSet<>();
        collectDependencies(this, result, visited);
        result.remove(this); // 移除自身
        return result;
    }
    
    /**
     * 递归获取所有被依赖节点（深度优先）
     * @return 所有直接和间接依赖当前节点的节点
     */
    public Set<CsdlDependencyNode> getAllDependents() {
        Set<CsdlDependencyNode> result = new HashSet<>();
        Set<CsdlDependencyNode> visited = new HashSet<>();
        collectDependents(this, result, visited);
        result.remove(this); // 移除自身
        return result;
    }
    
    /**
     * 递归收集依赖节点
     */
    private void collectDependencies(CsdlDependencyNode node, Set<CsdlDependencyNode> result, Set<CsdlDependencyNode> visited) {
        if (visited.contains(node)) {
            return; // 避免循环依赖
        }
        visited.add(node);
        result.add(node);
        
        for (CsdlDependencyNode dependency : node.dependencies) {
            collectDependencies(dependency, result, visited);
        }
    }
    
    /**
     * 递归收集被依赖节点
     */
    private void collectDependents(CsdlDependencyNode node, Set<CsdlDependencyNode> result, Set<CsdlDependencyNode> visited) {
        if (visited.contains(node)) {
            return; // 避免循环依赖
        }
        visited.add(node);
        result.add(node);
        
        for (CsdlDependencyNode dependent : node.dependents) {
            collectDependents(dependent, result, visited);
        }
    }
    
    /**
     * 获取依赖路径（从当前节点到目标节点）
     * @param target 目标节点
     * @return 依赖路径，如果不存在则返回null
     */
    public List<CsdlDependencyNode> getDependencyPath(CsdlDependencyNode target) {
        List<CsdlDependencyNode> path = new ArrayList<>();
        Set<CsdlDependencyNode> visited = new HashSet<>();
        if (findDependencyPath(this, target, path, visited)) {
            return path;
        }
        return null;
    }
    
    /**
     * 查找依赖路径的递归实现
     */
    private boolean findDependencyPath(CsdlDependencyNode current, CsdlDependencyNode target, 
                                     List<CsdlDependencyNode> path, Set<CsdlDependencyNode> visited) {
        if (visited.contains(current)) {
            return false; // 避免循环
        }
        
        visited.add(current);
        path.add(current);
        
        if (current.equals(target)) {
            return true; // 找到目标
        }
        
        for (CsdlDependencyNode dependency : current.dependencies) {
            if (findDependencyPath(dependency, target, path, visited)) {
                return true;
            }
        }
        
        // 回溯
        path.remove(path.size() - 1);
        return false;
    }
    
    /**
     * 检查是否存在循环依赖
     * @return 如果存在循环依赖则返回true
     */
    public boolean hasCircularDependency() {
        Set<CsdlDependencyNode> visited = new HashSet<>();
        Set<CsdlDependencyNode> recursionStack = new HashSet<>();
        return hasCircularDependencyHelper(this, visited, recursionStack);
    }
    
    /**
     * 循环依赖检查的递归实现
     */
    private boolean hasCircularDependencyHelper(CsdlDependencyNode node, 
                                              Set<CsdlDependencyNode> visited,
                                              Set<CsdlDependencyNode> recursionStack) {
        if (recursionStack.contains(node)) {
            return true; // 发现循环
        }
        
        if (visited.contains(node)) {
            return false; // 已经访问过且无循环
        }
        
        visited.add(node);
        recursionStack.add(node);
        
        for (CsdlDependencyNode dependency : node.dependencies) {
            if (hasCircularDependencyHelper(dependency, visited, recursionStack)) {
                return true;
            }
        }
        
        recursionStack.remove(node);
        return false;
    }
    
    // === Getters and Setters ===
    
    @Override
    public String getName() {
        return name;
    }
    
    public CsdlDependencyNode setName(String name) {
        this.name = name;
        return this;
    }
    
    public FullQualifiedName getFullyQualifiedName() {
        return fullyQualifiedName;
    }
    
    public CsdlDependencyNode setFullyQualifiedName(FullQualifiedName fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
        if (fullyQualifiedName != null) {
            this.name = fullyQualifiedName.getName();
        }
        return this;
    }
    
    public DependencyType getDependencyType() {
        return dependencyType;
    }
    
    public CsdlDependencyNode setDependencyType(DependencyType dependencyType) {
        this.dependencyType = dependencyType;
        return this;
    }
    
    public String getPropertyName() {
        return propertyName;
    }
    
    public CsdlDependencyNode setPropertyName(String propertyName) {
        this.propertyName = propertyName;
        return this;
    }
    
    /**
     * 获取直接依赖节点（只读）
     */
    public Set<CsdlDependencyNode> getDependencies() {
        return new HashSet<>(dependencies);
    }
    
    /**
     * 获取直接被依赖节点（只读）
     */
    public Set<CsdlDependencyNode> getDependents() {
        return new HashSet<>(dependents);
    }
    
    @Override
    public List<CsdlAnnotation> getAnnotations() {
        return annotations;
    }
    
    public CsdlDependencyNode setAnnotations(List<CsdlAnnotation> annotations) {
        this.annotations = annotations != null ? annotations : new ArrayList<>();
        return this;
    }
    
    // === Object methods ===
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        CsdlDependencyNode that = (CsdlDependencyNode) obj;
        return Objects.equals(fullyQualifiedName, that.fullyQualifiedName) &&
               dependencyType == that.dependencyType &&
               Objects.equals(propertyName, that.propertyName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(fullyQualifiedName, dependencyType, propertyName);
    }
    
    @Override
    public String toString() {
        String fqn = fullyQualifiedName != null ? fullyQualifiedName.toString() : name;
        return String.format("CsdlDependencyNode{name='%s', type=%s, property='%s', deps=%d, dependents=%d}", 
            fqn, dependencyType, propertyName, dependencies.size(), dependents.size());
    }
}
