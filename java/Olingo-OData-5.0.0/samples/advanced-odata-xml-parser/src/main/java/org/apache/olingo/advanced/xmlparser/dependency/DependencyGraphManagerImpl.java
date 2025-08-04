/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.olingo.advanced.xmlparser.dependency;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.olingo.advanced.xmlparser.core.DependencyManager;
import org.apache.olingo.advanced.xmlparser.statistics.ErrorInfo;
import org.apache.olingo.advanced.xmlparser.statistics.ErrorType;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.olingo.advanced.xmlparser.core.DependencyManager;
import org.apache.olingo.advanced.xmlparser.statistics.ParseStatistics;

/**
 * Implementation of dependency graph management using verified logic from AdvancedMetadataParser
 */
public class DependencyGraphManagerImpl implements DependencyManager {
    private final Map<String, Set<String>> dependencyGraph = new ConcurrentHashMap<>();
    private final Set<String> currentlyLoading = ConcurrentHashMap.newKeySet();
    private final Set<String> processedSchemas = ConcurrentHashMap.newKeySet();
    private final ParseStatistics statistics;
    private final Map<String, List<String>> errorReport;
    
    public DependencyGraphManagerImpl(ParseStatistics statistics, Map<String, List<String>> errorReport) {
        this.statistics = statistics;
        this.errorReport = errorReport;
    }
    
    @Override
    public void addDependency(String source, String target) {
        dependencyGraph.computeIfAbsent(source, k -> new HashSet<>()).add(target);
    }
    
    @Override
    public boolean containsSchema(String schemaPath) {
        return dependencyGraph.containsKey(schemaPath);
    }
    
    @Override
    public boolean isCurrentlyLoading(String schemaPath) {
        return currentlyLoading.contains(schemaPath);
    }
    
    @Override
    public void markLoading(String schemaPath) {
        currentlyLoading.add(schemaPath);
    }
    
    @Override
    public void markFinished(String schemaPath) {
        currentlyLoading.remove(schemaPath);
        processedSchemas.add(schemaPath);
    }
    
    @Override
    public List<List<String>> detectCircularDependencies() {
        List<List<String>> cycles = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();
        
        for (String node : dependencyGraph.keySet()) {
            if (!visited.contains(node)) {
                List<String> currentPath = new ArrayList<>();
                if (dfsDetectCycle(node, visited, recursionStack, currentPath, cycles)) {
                    // Cycle detected
                }
            }
        }
        
        return cycles;
    }
    
    /**
     * DFS helper for cycle detection (verified logic from AdvancedMetadataParser)
     */
    private boolean dfsDetectCycle(String node, Set<String> visited, Set<String> recursionStack, 
                                   List<String> currentPath, List<List<String>> cycles) {
        visited.add(node);
        recursionStack.add(node);
        currentPath.add(node);
        
        Set<String> dependencies = dependencyGraph.get(node);
        if (dependencies != null) {
            for (String dependency : dependencies) {
                if (!visited.contains(dependency)) {
                    if (dfsDetectCycle(dependency, visited, recursionStack, currentPath, cycles)) {
                        return true;
                    }
                } else if (recursionStack.contains(dependency)) {
                    // Cycle detected
                    int cycleStart = currentPath.indexOf(dependency);
                    if (cycleStart >= 0) {
                        List<String> cycle = new ArrayList<>(currentPath.subList(cycleStart, currentPath.size()));
                        cycle.add(dependency); // Complete the cycle
                        cycles.add(cycle);
                    }
                    return true;
                }
            }
        }
        
        recursionStack.remove(node);
        currentPath.remove(currentPath.size() - 1);
        return false;
    }
    
    @Override
    public void handleCircularDependencies(List<List<String>> cycles, boolean allowCircularDependencies) {
        for (List<String> cycle : cycles) {
            errorReport.put("circular_dependency", cycle);
        }
        
        if (!allowCircularDependencies) {
            throw new IllegalStateException("Circular dependencies detected and not allowed. Cycles: " + cycles);
        }
    }
    
    @Override
    public List<String> calculateLoadOrder() {
        List<String> loadOrder = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> temporaryMark = new HashSet<>();
        
        for (String node : dependencyGraph.keySet()) {
            if (!visited.contains(node)) {
                topologicalSort(node, visited, temporaryMark, loadOrder);
            }
        }
        
        java.util.Collections.reverse(loadOrder); // Reverse to get correct dependency order
        return loadOrder;
    }
    
    /**
     * Topological sort helper (verified logic from AdvancedMetadataParser)
     */
    private void topologicalSort(String node, Set<String> visited, Set<String> temporaryMark, List<String> loadOrder) {
        if (temporaryMark.contains(node)) {
            // This indicates a cycle, but we'll handle it gracefully
            return;
        }
        
        if (visited.contains(node)) {
            return;
        }
        
        temporaryMark.add(node);
        
        Set<String> dependencies = dependencyGraph.get(node);
        if (dependencies != null) {
            for (String dependency : dependencies) {
                topologicalSort(dependency, visited, temporaryMark, loadOrder);
            }
        }
        
        temporaryMark.remove(node);
        visited.add(node);
        loadOrder.add(node);
    }
    
    @Override
    public Map<String, Set<String>> getAllDependencies() {
        return new java.util.HashMap<>(dependencyGraph);
    }
    
    @Override
    public void clearState() {
        dependencyGraph.clear();
        currentlyLoading.clear();
        processedSchemas.clear();
    }
}
