package org.apache.olingo.schema.processor.model.extended;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 抽象依赖追踪器实现基类
 */
public abstract class AbstractDependencyTracker implements DependencyTracker {
    
    private final Set<String> directDependencies = new HashSet<>();
    private String fullyQualifiedName;
    
    @Override
    public Set<String> getDirectDependencies() {
        return Collections.unmodifiableSet(directDependencies);
    }
    
    @Override
    public Set<String> getRecursiveDependencies() {
        // 递归依赖需要通过依赖解析器来实现
        // 这里返回直接依赖，具体的递归逻辑在DependencyResolver中实现
        return Collections.unmodifiableSet(directDependencies);
    }
    
    @Override
    public void addDependency(String fullyQualifiedTypeName) {
        if (fullyQualifiedTypeName != null && !fullyQualifiedTypeName.trim().isEmpty()) {
            directDependencies.add(fullyQualifiedTypeName);
        }
    }
    
    @Override
    public void removeDependency(String fullyQualifiedTypeName) {
        directDependencies.remove(fullyQualifiedTypeName);
    }
    
    @Override
    public void clearDependencies() {
        directDependencies.clear();
    }
    
    @Override
    public boolean hasCircularDependencies() {
        // 简单的自引用检查
        return directDependencies.contains(fullyQualifiedName);
    }
    
    @Override
    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }
    
    @Override
    public void setFullyQualifiedName(String fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
    }
}
