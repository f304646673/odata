package org.apache.olingo.schema.processor.model.dependency;

import java.util.*;

/**
 * 依赖链追踪器 - 记录和分析元素间的依赖关系链
 */
public class DependencyChain {
    
    private final List<DependencyInfo> chain;
    private final String rootElement;
    
    public DependencyChain(String rootElement) {
        this.rootElement = rootElement;
        this.chain = new ArrayList<>();
    }
    
    private DependencyChain(String rootElement, List<DependencyInfo> chain) {
        this.rootElement = rootElement;
        this.chain = new ArrayList<>(chain);
    }
    
    /**
     * 添加依赖到链中
     */
    public void addDependency(DependencyInfo dependency) {
        if (dependency != null && !chain.contains(dependency)) {
            chain.add(dependency);
        }
    }
    
    /**
     * 创建扩展的依赖链
     */
    public DependencyChain extend(DependencyInfo dependency) {
        DependencyChain extended = new DependencyChain(rootElement, chain);
        extended.addDependency(dependency);
        return extended;
    }
    
    /**
     * 检查是否存在循环依赖
     */
    public boolean hasCyclicDependency() {
        Set<String> visited = new HashSet<>();
        visited.add(rootElement);
        
        for (DependencyInfo dep : chain) {
            String target = dep.getFullTargetName();
            if (visited.contains(target)) {
                return true;
            }
            visited.add(target);
        }
        
        return false;
    }
    
    /**
     * 获取链的深度
     */
    public int getDepth() {
        return chain.size();
    }
    
    /**
     * 获取链的所有节点
     */
    public List<String> getNodes() {
        List<String> nodes = new ArrayList<>();
        nodes.add(rootElement);
        
        for (DependencyInfo dep : chain) {
            nodes.add(dep.getFullTargetName());
        }
        
        return nodes;
    }
    
    /**
     * 获取链中的所有依赖信息
     */
    public List<DependencyInfo> getDependencies() {
        return new ArrayList<>(chain);
    }
    
    /**
     * 获取最终目标元素
     */
    public String getFinalTarget() {
        if (chain.isEmpty()) {
            return rootElement;
        }
        return chain.get(chain.size() - 1).getFullTargetName();
    }
    
    /**
     * 获取根元素
     */
    public String getRootElement() {
        return rootElement;
    }
    
    /**
     * 检查链是否为空
     */
    public boolean isEmpty() {
        return chain.isEmpty();
    }
    
    /**
     * 获取依赖链的字符串表示
     */
    @Override
    public String toString() {
        if (chain.isEmpty()) {
            return rootElement + " (no dependencies)";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(rootElement);
        
        for (DependencyInfo dep : chain) {
            sb.append(" -> ").append(dep.getFullTargetName())
              .append(" (").append(dep.getDependencyType())
              .append(":").append(dep.getPropertyName()).append(")");
        }
        
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        DependencyChain that = (DependencyChain) obj;
        return Objects.equals(rootElement, that.rootElement) &&
               Objects.equals(chain, that.chain);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(rootElement, chain);
    }
}
