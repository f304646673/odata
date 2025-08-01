package org.apache.olingo.xmlprocessor.core.dependency;

import java.util.*;

/**
 * 增强的依赖跟踪器接口
 */
public interface EnhancedDependencyTracker {
    
    /**
     * 添加详细的依赖信息
     */
    void addDetailedDependency(DependencyInfo dependency);
    
    /**
     * 获取所有详细依赖信息
     */
    Set<DependencyInfo> getDetailedDependencies();
    
    /**
     * 获取指定类型的依赖
     */
    Set<DependencyInfo> getDependenciesByType(DependencyInfo.DependencyType type);
    
    /**
     * 获取指定命名空间的依赖
     */
    Set<DependencyInfo> getDependenciesByNamespace(String namespace);
    
    /**
     * 获取依赖链
     */
    List<DependencyChain> getDependencyChains();
    
    /**
     * 构建递归依赖链
     */
    void buildDependencyChains();
    
    /**
     * 检查是否存在循环依赖
     */
    boolean hasCircularDependencies();
    
    /**
     * 获取所有被依赖的元素
     */
    Set<String> getAllDependentElements();
    
    /**
     * 清除所有依赖信息
     */
    void clearDetailedDependencies();
    
    /**
     * 分析并构建依赖关系
     */
    void analyzeDependencies();
}
