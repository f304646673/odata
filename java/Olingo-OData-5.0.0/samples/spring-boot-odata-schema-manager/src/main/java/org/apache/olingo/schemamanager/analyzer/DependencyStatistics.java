package org.apache.olingo.schemamanager.analyzer;

import java.util.Map;

/**
 * Statistics about dependencies in the schema
 */
public class DependencyStatistics {
    
    private final int totalElements;
    private final int totalDependencies;
    private final int circularDependencies;
    private final int leafElements;
    private final int rootElements;
    private final int maxDepth;
    private final double averageDepth;
    private final Map<DependencyTreeNode.ElementType, Integer> elementTypeCount;
    private final Map<String, Integer> dependencyCountByElement;
    
    public DependencyStatistics(int totalElements,
                               int totalDependencies,
                               int circularDependencies,
                               int leafElements,
                               int rootElements,
                               int maxDepth,
                               double averageDepth,
                               Map<DependencyTreeNode.ElementType, Integer> elementTypeCount,
                               Map<String, Integer> dependencyCountByElement) {
        this.totalElements = totalElements;
        this.totalDependencies = totalDependencies;
        this.circularDependencies = circularDependencies;
        this.leafElements = leafElements;
        this.rootElements = rootElements;
        this.maxDepth = maxDepth;
        this.averageDepth = averageDepth;
        this.elementTypeCount = elementTypeCount;
        this.dependencyCountByElement = dependencyCountByElement;
    }
    
    public int getTotalElements() {
        return totalElements;
    }
    
    public int getTotalDependencies() {
        return totalDependencies;
    }
    
    public int getCircularDependencies() {
        return circularDependencies;
    }
    
    public int getLeafElements() {
        return leafElements;
    }
    
    public int getRootElements() {
        return rootElements;
    }
    
    public int getMaxDepth() {
        return maxDepth;
    }
    
    public double getAverageDepth() {
        return averageDepth;
    }
    
    public Map<DependencyTreeNode.ElementType, Integer> getElementTypeCount() {
        return elementTypeCount;
    }
    
    public Map<String, Integer> getDependencyCountByElement() {
        return dependencyCountByElement;
    }
    
    @Override
    public String toString() {
        return String.format("DependencyStatistics{totalElements=%d, totalDependencies=%d, " +
                           "circularDependencies=%d, leafElements=%d, rootElements=%d, " +
                           "maxDepth=%d, averageDepth=%.2f}", 
                           totalElements, totalDependencies, circularDependencies, 
                           leafElements, rootElements, maxDepth, averageDepth);
    }
}
