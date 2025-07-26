package org.apache.olingo.schema.processor.analyzer.impl;

import org.apache.olingo.schema.processor.analyzer.DependencyAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 依赖分析器默认实现
 * 提供OData模式间依赖关系的分析功能
 */
public class DefaultDependencyAnalyzer implements DependencyAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultDependencyAnalyzer.class);
    
    // 依赖关系图：key为元素全限定名，value为其依赖的元素集合
    private final Map<String, Set<String>> dependencyGraph = new ConcurrentHashMap<>();
    
    // 反向依赖关系图：key为元素全限定名，value为依赖它的元素集合
    private final Map<String, Set<String>> reverseDependencyGraph = new ConcurrentHashMap<>();
    
    /**
     * 添加依赖关系
     */
    public void addDependency(String element, String dependency) {
        dependencyGraph.computeIfAbsent(element, k -> new HashSet<>()).add(dependency);
        reverseDependencyGraph.computeIfAbsent(dependency, k -> new HashSet<>()).add(element);
    }
    
    /**
     * 移除依赖关系
     */
    public void removeDependency(String element, String dependency) {
        Set<String> deps = dependencyGraph.get(element);
        if (deps != null) {
            deps.remove(dependency);
            if (deps.isEmpty()) {
                dependencyGraph.remove(element);
            }
        }
        
        Set<String> reverseDeps = reverseDependencyGraph.get(dependency);
        if (reverseDeps != null) {
            reverseDeps.remove(element);
            if (reverseDeps.isEmpty()) {
                reverseDependencyGraph.remove(dependency);
            }
        }
    }
    
    @Override
    public Set<String> getDirectDependencies(String fullyQualifiedName) {
        Set<String> dependencies = dependencyGraph.get(fullyQualifiedName);
        return dependencies != null ? new HashSet<>(dependencies) : new HashSet<>();
    }
    
    @Override
    public Set<String> getRecursiveDependencies(String fullyQualifiedName) {
        Set<String> visited = new HashSet<>();
        Set<String> result = new HashSet<>();
        getRecursiveDependenciesHelper(fullyQualifiedName, visited, result);
        return result;
    }
    
    private void getRecursiveDependenciesHelper(String element, Set<String> visited, Set<String> result) {
        if (visited.contains(element)) {
            return; // 避免循环依赖造成的无限递归
        }
        
        visited.add(element);
        Set<String> directDeps = dependencyGraph.get(element);
        if (directDeps != null) {
            for (String dep : directDeps) {
                result.add(dep);
                getRecursiveDependenciesHelper(dep, visited, result);
            }
        }
    }
    
    @Override
    public Set<String> getReverseDependencies(String fullyQualifiedName) {
        Set<String> dependencies = reverseDependencyGraph.get(fullyQualifiedName);
        return dependencies != null ? new HashSet<>(dependencies) : new HashSet<>();
    }
    
    @Override
    public List<DependencyCycle> detectCircularDependencies() {
        List<DependencyCycle> cycles = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();
        
        for (String element : dependencyGraph.keySet()) {
            if (!visited.contains(element)) {
                List<String> currentPath = new ArrayList<>();
                detectCycleDFS(element, visited, recursionStack, currentPath, cycles);
            }
        }
        
        return cycles;
    }
    
    private void detectCycleDFS(String element, Set<String> visited, Set<String> recursionStack,
                               List<String> currentPath, List<DependencyCycle> cycles) {
        
        visited.add(element);
        recursionStack.add(element);
        currentPath.add(element);
        
        Set<String> deps = dependencyGraph.get(element);
        if (deps != null) {
            for (String dependency : deps) {
                if (!visited.contains(dependency)) {
                    detectCycleDFS(dependency, visited, recursionStack, currentPath, cycles);
                } else if (recursionStack.contains(dependency)) {
                    // 发现环形依赖
                    int cycleStart = currentPath.indexOf(dependency);
                    List<String> cycle = new ArrayList<>(currentPath.subList(cycleStart, currentPath.size()));
                    cycle.add(dependency); // 完成环
                    String description = "Circular dependency detected: " + String.join(" -> ", cycle);
                    cycles.add(new DependencyCycle(cycle, description));
                }
            }
        }
        
        recursionStack.remove(element);
        currentPath.remove(currentPath.size() - 1);
    }
    
    @Override
    public boolean hasCircularDependency(String fullyQualifiedName) {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();
        return hasCircularDependencyDFS(fullyQualifiedName, visited, recursionStack);
    }
    
    private boolean hasCircularDependencyDFS(String element, Set<String> visited, Set<String> recursionStack) {
        if (recursionStack.contains(element)) {
            return true; // 发现环
        }
        if (visited.contains(element)) {
            return false; // 已经访问过且没有环
        }
        
        visited.add(element);
        recursionStack.add(element);
        
        Set<String> deps = dependencyGraph.get(element);
        if (deps != null) {
            for (String dependency : deps) {
                if (hasCircularDependencyDFS(dependency, visited, recursionStack)) {
                    return true;
                }
            }
        }
        
        recursionStack.remove(element);
        return false;
    }
    
    @Override
    public Map<String, Set<String>> getDependencyGraph() {
        Map<String, Set<String>> result = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : dependencyGraph.entrySet()) {
            result.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return result;
    }
    
    @Override
    public Map<String, Set<String>> getReverseDependencyGraph() {
        Map<String, Set<String>> result = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : reverseDependencyGraph.entrySet()) {
            result.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return result;
    }
    
    @Override
    public List<Set<String>> getDependencyLayers() {
        List<Set<String>> layers = new ArrayList<>();
        Set<String> allElements = new HashSet<>(dependencyGraph.keySet());
        allElements.addAll(reverseDependencyGraph.keySet());
        Set<String> processed = new HashSet<>();
        
        while (processed.size() < allElements.size()) {
            Set<String> currentLayer = new HashSet<>();
            
            for (String element : allElements) {
                if (!processed.contains(element)) {
                    Set<String> deps = dependencyGraph.get(element);
                    if (deps == null || deps.isEmpty() || processed.containsAll(deps)) {
                        currentLayer.add(element);
                    }
                }
            }
            
            if (currentLayer.isEmpty()) {
                // 存在循环依赖，无法继续分层
                Set<String> remaining = new HashSet<>(allElements);
                remaining.removeAll(processed);
                layers.add(remaining);
                break;
            }
            
            layers.add(currentLayer);
            processed.addAll(currentLayer);
        }
        
        return layers;
    }
    
    @Override
    public DependencyStatistics getDependencyStatistics() {
        Set<String> allElements = new HashSet<>(dependencyGraph.keySet());
        allElements.addAll(reverseDependencyGraph.keySet());
        
        int totalElements = allElements.size();
        int elementsWithDependencies = (int) allElements.stream()
                .filter(element -> {
                    Set<String> deps = dependencyGraph.get(element);
                    return deps != null && !deps.isEmpty();
                })
                .count();
        int elementsWithoutDependencies = totalElements - elementsWithDependencies;
        
        int maxDepth = calculateMaxDependencyDepth();
        double avgDeps = calculateAverageDependencies();
        Map<String, Integer> depCountByNamespace = calculateDependencyCountByNamespace();
        
        return new DependencyStatistics(totalElements, elementsWithDependencies,
                elementsWithoutDependencies, maxDepth, avgDeps, depCountByNamespace);
    }
    
    private int calculateMaxDependencyDepth() {
        int maxDepth = 0;
        for (String element : dependencyGraph.keySet()) {
            Set<String> visited = new HashSet<>();
            int depth = calculateDepthDFS(element, visited);
            maxDepth = Math.max(maxDepth, depth);
        }
        return maxDepth;
    }
    
    private int calculateDepthDFS(String element, Set<String> visited) {
        if (visited.contains(element)) {
            return 0; // 避免循环依赖造成的无限递归
        }
        
        visited.add(element);
        Set<String> deps = dependencyGraph.get(element);
        if (deps == null || deps.isEmpty()) {
            return 0;
        }
        
        int maxChildDepth = 0;
        for (String dep : deps) {
            int childDepth = calculateDepthDFS(dep, new HashSet<>(visited));
            maxChildDepth = Math.max(maxChildDepth, childDepth);
        }
        
        return 1 + maxChildDepth;
    }
    
    private double calculateAverageDependencies() {
        if (dependencyGraph.isEmpty()) {
            return 0.0;
        }
        
        int totalDeps = dependencyGraph.values().stream()
                .mapToInt(Set::size)
                .sum();
        
        return (double) totalDeps / dependencyGraph.size();
    }
    
    private Map<String, Integer> calculateDependencyCountByNamespace() {
        Map<String, Integer> result = new HashMap<>();
        
        for (Map.Entry<String, Set<String>> entry : dependencyGraph.entrySet()) {
            String elementName = entry.getKey();
            int lastDotIndex = elementName.lastIndexOf('.');
            String namespace = lastDotIndex > 0 ? elementName.substring(0, lastDotIndex) : "default";
            
            result.merge(namespace, entry.getValue().size(), Integer::sum);
        }
        
        return result;
    }
    
    @Override
    public ImpactAnalysis analyzeImpact(String fullyQualifiedName) {
        Set<String> directlyAffected = getReverseDependencies(fullyQualifiedName);
        Set<String> indirectlyAffected = new HashSet<>();
        
        // 计算间接影响
        Queue<String> queue = new LinkedList<>(directlyAffected);
        Set<String> visited = new HashSet<>(directlyAffected);
        
        while (!queue.isEmpty()) {
            String current = queue.poll();
            Set<String> reverseDeps = reverseDependencyGraph.get(current);
            if (reverseDeps != null) {
                for (String dep : reverseDeps) {
                    if (!visited.contains(dep)) {
                        indirectlyAffected.add(dep);
                        visited.add(dep);
                        queue.offer(dep);
                    }
                }
            }
        }
        
        int totalAffected = directlyAffected.size() + indirectlyAffected.size();
        Map<String, Integer> affectedByNamespace = calculateAffectedByNamespace(directlyAffected, indirectlyAffected);
        
        return new ImpactAnalysis(fullyQualifiedName, directlyAffected, indirectlyAffected,
                totalAffected, affectedByNamespace);
    }
    
    private Map<String, Integer> calculateAffectedByNamespace(Set<String> directlyAffected, Set<String> indirectlyAffected) {
        Map<String, Integer> result = new HashMap<>();
        
        Set<String> allAffected = new HashSet<>(directlyAffected);
        allAffected.addAll(indirectlyAffected);
        
        for (String element : allAffected) {
            int lastDotIndex = element.lastIndexOf('.');
            String namespace = lastDotIndex > 0 ? element.substring(0, lastDotIndex) : "default";
            result.merge(namespace, 1, Integer::sum);
        }
        
        return result;
    }
}
