package org.apache.olingo.schema.processor.model.extended;

import java.util.Set;

/**
 * 依赖关系接口，用于追踪schema元素之间的依赖关系
 */
public interface DependencyTracker {
    
    /**
     * 获取直接依赖的类型全限定名集合
     * @return 依赖的类型全限定名集合
     */
    Set<String> getDirectDependencies();
    
    /**
     * 获取递归依赖的类型全限定名集合
     * @return 递归依赖的类型全限定名集合
     */
    Set<String> getRecursiveDependencies();
    
    /**
     * 添加依赖
     * @param fullyQualifiedTypeName 依赖的类型全限定名
     */
    void addDependency(String fullyQualifiedTypeName);
    
    /**
     * 移除依赖
     * @param fullyQualifiedTypeName 要移除的依赖类型全限定名
     */
    void removeDependency(String fullyQualifiedTypeName);
    
    /**
     * 清空所有依赖
     */
    void clearDependencies();
    
    /**
     * 检查是否有循环依赖
     * @return 如果存在循环依赖返回true
     */
    boolean hasCircularDependencies();
    
    /**
     * 获取当前元素的全限定名
     * @return 全限定名
     */
    String getFullyQualifiedName();
    
    /**
     * 设置当前元素的全限定名
     * @param fullyQualifiedName 全限定名
     */
    void setFullyQualifiedName(String fullyQualifiedName);
}
