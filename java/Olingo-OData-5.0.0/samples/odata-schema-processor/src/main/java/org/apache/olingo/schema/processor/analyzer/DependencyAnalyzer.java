package org.apache.olingo.schema.processor.analyzer;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 依赖分析器接口，用于分析Schema元素之间的依赖关系
 */
public interface DependencyAnalyzer {
    
    /**
     * 分析指定元素的直接依赖
     * @param fullyQualifiedName 元素的全限定名
     * @return 直接依赖的元素全限定名集合
     */
    Set<String> getDirectDependencies(String fullyQualifiedName);
    
    /**
     * 分析指定元素的递归依赖（包括间接依赖）
     * @param fullyQualifiedName 元素的全限定名
     * @return 递归依赖的元素全限定名集合
     */
    Set<String> getRecursiveDependencies(String fullyQualifiedName);
    
    /**
     * 分析指定元素的反向依赖（哪些元素依赖于它）
     * @param fullyQualifiedName 元素的全限定名
     * @return 依赖于该元素的其他元素全限定名集合
     */
    Set<String> getReverseDependencies(String fullyQualifiedName);
    
    /**
     * 检测所有循环依赖
     * @return 循环依赖链的列表
     */
    List<DependencyCycle> detectCircularDependencies();
    
    /**
     * 检测指定元素是否存在循环依赖
     * @param fullyQualifiedName 元素的全限定名
     * @return 如果存在循环依赖返回true
     */
    boolean hasCircularDependency(String fullyQualifiedName);
    
    /**
     * 获取依赖关系图
     * @return 依赖关系图，key为元素全限定名，value为其依赖的元素集合
     */
    Map<String, Set<String>> getDependencyGraph();
    
    /**
     * 获取反向依赖关系图
     * @return 反向依赖关系图，key为元素全限定名，value为依赖它的元素集合
     */
    Map<String, Set<String>> getReverseDependencyGraph();
    
    /**
     * 获取依赖层级结构
     * @return 按依赖层级排序的元素列表，第一层没有依赖，后续层级依次包含依赖前面层级的元素
     */
    List<Set<String>> getDependencyLayers();
    
    /**
     * 计算依赖统计信息
     * @return 依赖统计信息
     */
    DependencyStatistics getDependencyStatistics();
    
    /**
     * 分析影响范围
     * @param fullyQualifiedName 要分析的元素全限定名
     * @return 影响分析结果
     */
    ImpactAnalysis analyzeImpact(String fullyQualifiedName);
    
    /**
     * 循环依赖信息
     */
    class DependencyCycle {
        private final List<String> cycle;
        private final String description;
        
        public DependencyCycle(List<String> cycle, String description) {
            this.cycle = cycle;
            this.description = description;
        }
        
        public List<String> getCycle() { return cycle; }
        public String getDescription() { return description; }
    }
    
    /**
     * 依赖统计信息
     */
    class DependencyStatistics {
        private final int totalElements;
        private final int elementsWithDependencies;
        private final int elementsWithoutDependencies;
        private final int maxDependencyDepth;
        private final double averageDependencies;
        private final Map<String, Integer> dependencyCountByNamespace;
        
        public DependencyStatistics(int totalElements, int elementsWithDependencies,
                                   int elementsWithoutDependencies, int maxDependencyDepth,
                                   double averageDependencies, Map<String, Integer> dependencyCountByNamespace) {
            this.totalElements = totalElements;
            this.elementsWithDependencies = elementsWithDependencies;
            this.elementsWithoutDependencies = elementsWithoutDependencies;
            this.maxDependencyDepth = maxDependencyDepth;
            this.averageDependencies = averageDependencies;
            this.dependencyCountByNamespace = dependencyCountByNamespace;
        }
        
        public int getTotalElements() { return totalElements; }
        public int getElementsWithDependencies() { return elementsWithDependencies; }
        public int getElementsWithoutDependencies() { return elementsWithoutDependencies; }
        public int getMaxDependencyDepth() { return maxDependencyDepth; }
        public double getAverageDependencies() { return averageDependencies; }
        public Map<String, Integer> getDependencyCountByNamespace() { return dependencyCountByNamespace; }
    }
    
    /**
     * 影响分析结果
     */
    class ImpactAnalysis {
        private final String targetElement;
        private final Set<String> directlyAffected;
        private final Set<String> indirectlyAffected;
        private final int totalAffected;
        private final Map<String, Integer> affectedByNamespace;
        
        public ImpactAnalysis(String targetElement, Set<String> directlyAffected,
                             Set<String> indirectlyAffected, int totalAffected,
                             Map<String, Integer> affectedByNamespace) {
            this.targetElement = targetElement;
            this.directlyAffected = directlyAffected;
            this.indirectlyAffected = indirectlyAffected;
            this.totalAffected = totalAffected;
            this.affectedByNamespace = affectedByNamespace;
        }
        
        public String getTargetElement() { return targetElement; }
        public Set<String> getDirectlyAffected() { return directlyAffected; }
        public Set<String> getIndirectlyAffected() { return indirectlyAffected; }
        public int getTotalAffected() { return totalAffected; }
        public Map<String, Integer> getAffectedByNamespace() { return affectedByNamespace; }
    }
}
