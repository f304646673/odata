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
package org.apache.olingo.advanced.xmlparser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages dependency graphs for schema loading and circular dependency detection.
 */
public class DependencyGraphManager {
    private final Map<String, Set<String>> dependencyGraph = new ConcurrentHashMap<>();
    private final Set<String> currentlyLoading = ConcurrentHashMap.newKeySet();
    private final Set<String> processedSchemas = ConcurrentHashMap.newKeySet();
    private final ParseStatistics statistics;
    private final Map<String, List<String>> errorReport;
    
    public DependencyGraphManager(ParseStatistics statistics, Map<String, List<String>> errorReport) {
        this.statistics = statistics;
        this.errorReport = errorReport;
    }
    
    /**
     * Add dependency to the graph
     */
    public void addDependency(String source, String target) {
        dependencyGraph.computeIfAbsent(source, k -> new HashSet<>()).add(target);
    }
    
    /**
     * Check if schema is currently being loaded (to detect circular dependencies)
     */
    public boolean isCurrentlyLoading(String schemaPath) {
        return currentlyLoading.contains(schemaPath);
    }
    
    /**
     * Mark schema as currently loading
     */
    public void markLoading(String schemaPath) {
        currentlyLoading.add(schemaPath);
    }
    
    /**
     * Mark schema as finished loading
     */
    public void markFinished(String schemaPath) {
        currentlyLoading.remove(schemaPath);
        processedSchemas.add(schemaPath);
    }
    
    /**
     * Get dependencies for a schema
     */
    public Set<String> getDependencies(String schemaPath) {
        return dependencyGraph.getOrDefault(schemaPath, new HashSet<>());
    }
    
    /**
     * Check if dependency graph contains a schema (has been processed)
     */
    public boolean containsSchema(String schemaPath) {
        return processedSchemas.contains(schemaPath);
    }
    
    /**
     * Get all schemas in the dependency graph
     */
    public Set<String> getAllSchemas() {
        Set<String> allSchemas = new HashSet<>(dependencyGraph.keySet());
        for (Set<String> deps : dependencyGraph.values()) {
            allSchemas.addAll(deps);
        }
        return allSchemas;
    }
    
    /**
     * Detect circular dependencies using DFS
     */
    public List<List<String>> detectCircularDependencies() {
        List<List<String>> cycles = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();
        
        for (String node : dependencyGraph.keySet()) {
            if (!visited.contains(node)) {
                List<String> currentPath = new ArrayList<>();
                dfsDetectCycle(node, visited, recursionStack, currentPath, cycles);
            }
        }
        
        return cycles;
    }
    
    /**
     * DFS helper for cycle detection
     */
    private boolean dfsDetectCycle(String node, Set<String> visited, Set<String> recursionStack, 
                                   List<String> currentPath, List<List<String>> cycles) {
        visited.add(node);
        recursionStack.add(node);
        currentPath.add(node);
        
        Set<String> neighbors = dependencyGraph.get(node);
        if (neighbors != null) {
            for (String neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    if (dfsDetectCycle(neighbor, visited, recursionStack, currentPath, cycles)) {
                        return true;
                    }
                } else if (recursionStack.contains(neighbor)) {
                    // Found a cycle, extract the cycle path
                    int cycleStart = currentPath.indexOf(neighbor);
                    List<String> cycle = new ArrayList<>(currentPath.subList(cycleStart, currentPath.size()));
                    cycle.add(neighbor); // Close the cycle
                    cycles.add(cycle);
                    return true;
                }
            }
        }
        
        recursionStack.remove(node);
        currentPath.remove(currentPath.size() - 1);
        return false;
    }
    
    /**
     * Handle circular dependencies based on configuration
     */
    public void handleCircularDependencies(List<List<String>> cycles, boolean allowCircularDependencies) throws Exception {
        if (!cycles.isEmpty()) {
            for (List<String> cycle : cycles) {
                String cycleDescription = String.join(" -> ", cycle);
                statistics.addError(ErrorType.CIRCULAR_DEPENDENCY, 
                    "Circular dependency detected: " + cycleDescription, 
                    cycle.get(0));
                errorReport.computeIfAbsent("CIRCULAR_DEPENDENCIES", k -> new ArrayList<>())
                    .add(cycleDescription);
            }
            
            if (!allowCircularDependencies) {
                throw new IllegalStateException("Circular dependencies detected. Use allowCircularDependencies(true) to proceed anyway.");
            }
        }
    }
    
    /**
     * Calculate load order using topological sorting
     */
    public List<String> calculateLoadOrder() {
        List<String> loadOrder = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> temporaryMark = new HashSet<>();
        
        for (String node : dependencyGraph.keySet()) {
            if (!visited.contains(node)) {
                topologicalSort(node, visited, temporaryMark, loadOrder);
            }
        }
        
        return loadOrder;
    }
    
    /**
     * Topological sort helper
     */
    private void topologicalSort(String node, Set<String> visited, Set<String> temporaryMark, List<String> loadOrder) {
        if (temporaryMark.contains(node)) {
            // This indicates a cycle, but we'll let the cycle detection handle it
            return;
        }
        
        if (visited.contains(node)) {
            return;
        }
        
        temporaryMark.add(node);
        
        Set<String> neighbors = dependencyGraph.get(node);
        if (neighbors != null) {
            for (String neighbor : neighbors) {
                topologicalSort(neighbor, visited, temporaryMark, loadOrder);
            }
        }
        
        temporaryMark.remove(node);
        visited.add(node);
        loadOrder.add(0, node); // Add to beginning for reverse topological order
    }
    
    /**
     * Get all dependencies in the graph
     */
    public Map<String, Set<String>> getAllDependencies() {
        return new ConcurrentHashMap<>(dependencyGraph);
    }
    
    /**
     * Clear internal state
     */
    public void clearState() {
        dependencyGraph.clear();
        currentlyLoading.clear();
        processedSchemas.clear();
    }
}
