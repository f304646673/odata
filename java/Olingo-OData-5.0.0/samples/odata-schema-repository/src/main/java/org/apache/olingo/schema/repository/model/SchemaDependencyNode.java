package org.apache.olingo.schema.repository.model;

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
 * 表示Schema Element依赖关系的节点
 * 支持树状结构来表示dependencies和dependents
 */
public class SchemaDependencyNode extends CsdlAbstractEdmItem implements CsdlNamed, CsdlAnnotatable {
    
    private String elementId;  // 元素唯一标识
    private String name;
    private FullQualifiedName fullyQualifiedName;
    private DependencyType dependencyType;
    private String propertyName; // 产生依赖的属性名
    
    // 树状结构：当前节点依赖的其他节点
    private final Set<SchemaDependencyNode> dependencies = new HashSet<>();
    
    // 树状结构：依赖当前节点的其他节点
    private final Set<SchemaDependencyNode> dependents = new HashSet<>();
    
    // 注解支持
    private List<CsdlAnnotation> annotations = new ArrayList<>();
    
    public enum DependencyType {
        TYPE_REFERENCE,      // 类型引用
        BASE_TYPE,          // 基类型
        ENTITY_SET,         // EntitySet引用
        ENTITY_TYPE,        // EntityType引用
        COMPLEX_TYPE,       // ComplexType引用
        ACTION,             // Action定义
        ACTION_IMPORT,      // ActionImport引用
        ACTION_REFERENCE,   // Action引用
        FUNCTION,           // Function定义
        FUNCTION_IMPORT,    // FunctionImport引用
        FUNCTION_REFERENCE, // Function引用
        NAVIGATION_PROPERTY,// NavigationProperty
        NAVIGATION_TARGET,  // 导航目标
        PARAMETER,          // Parameter
        PARAMETER_TYPE,     // 参数类型
        PROPERTY,           // Property
        RETURN_TYPE,        // 返回类型
        SINGLETON,          // Singleton
        TYPE_DEFINITION     // TypeDefinition
    }
    
    /**
     * 构造函数
     */
    public SchemaDependencyNode(String elementId, FullQualifiedName fullyQualifiedName, 
                             DependencyType dependencyType, String propertyName) {
        this.elementId = elementId;
        this.fullyQualifiedName = fullyQualifiedName;
        this.name = (fullyQualifiedName != null && fullyQualifiedName.getName() != null) ? 
                    fullyQualifiedName.getName() : elementId;
        this.dependencyType = dependencyType;
        this.propertyName = propertyName;
    }
    
    /**
     * 简化构造函数
     */
    public SchemaDependencyNode(FullQualifiedName fullyQualifiedName, DependencyType dependencyType) {
        this(fullyQualifiedName != null ? fullyQualifiedName.toString() : null, fullyQualifiedName, dependencyType, null);
    }
    
    /**
     * 添加依赖节点
     * @param dependency 被依赖的节点
     */
    public void addDependency(SchemaDependencyNode dependency) {
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
    public boolean removeDependency(SchemaDependencyNode dependency) {
        boolean removed = dependencies.remove(dependency);
        if (removed && dependency != null) {
            dependency.dependents.remove(this);
        }
        return removed;
    }
    
    /**
     * 检查是否形成循环依赖
     * @param visitedNodes 已访问的节点集合，用于检测循环
     * @return 是否存在循环依赖
     */
    public boolean hasCircularDependency(Set<SchemaDependencyNode> visitedNodes) {
        if (visitedNodes.contains(this)) {
            return true; // 发现循环
        }
        
        visitedNodes.add(this);
        for (SchemaDependencyNode dependency : dependencies) {
            if (dependency.hasCircularDependency(new HashSet<>(visitedNodes))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查是否存在循环依赖（入口方法）
     */
    public boolean hasCircularDependency() {
        return hasCircularDependency(new HashSet<>());
    }
    
    /**
     * 获取所有直接依赖项
     */
    public Set<SchemaDependencyNode> getDependencies() {
        return new HashSet<>(dependencies);
    }
    
    /**
     * 获取所有依赖当前节点的节点
     */
    public Set<SchemaDependencyNode> getDependents() {
        return new HashSet<>(dependents);
    }
    
    /**
     * 获取依赖深度（递归计算）
     */
    public int getDependencyDepth() {
        return getDependencyDepth(new HashSet<>());
    }
    
    private int getDependencyDepth(Set<SchemaDependencyNode> visited) {
        if (visited.contains(this)) {
            return 0; // 防止循环依赖导致无限递归
        }
        
        visited.add(this);
        int maxDepth = 0;
        for (SchemaDependencyNode dependency : dependencies) {
            maxDepth = Math.max(maxDepth, dependency.getDependencyDepth(visited));
        }
        return maxDepth + 1;
    }
    
    // Getters and setters
    public String getElementId() {
        return elementId;
    }
    
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public FullQualifiedName getFullyQualifiedName() {
        return fullyQualifiedName;
    }
    
    public void setFullyQualifiedName(FullQualifiedName fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
        if (fullyQualifiedName != null && fullyQualifiedName.getName() != null) {
            this.name = fullyQualifiedName.getName();
        }
    }
    
    public DependencyType getDependencyType() {
        return dependencyType;
    }
    
    public void setDependencyType(DependencyType dependencyType) {
        this.dependencyType = dependencyType;
    }
    
    public String getPropertyName() {
        return propertyName;
    }
    
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
    
    @Override
    public List<CsdlAnnotation> getAnnotations() {
        return annotations;
    }
    
    public void setAnnotations(List<CsdlAnnotation> annotations) {
        this.annotations = annotations != null ? annotations : new ArrayList<>();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchemaDependencyNode that = (SchemaDependencyNode) o;
        return Objects.equals(elementId, that.elementId) &&
               Objects.equals(fullyQualifiedName, that.fullyQualifiedName) &&
               dependencyType == that.dependencyType;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(elementId, fullyQualifiedName, dependencyType);
    }
    
    @Override
    public String toString() {
        return String.format("SchemaDependencyNode{elementId='%s', fullyQualifiedName=%s, dependencyType=%s, propertyName='%s'}",
                           elementId, fullyQualifiedName, dependencyType, propertyName);
    }
}
