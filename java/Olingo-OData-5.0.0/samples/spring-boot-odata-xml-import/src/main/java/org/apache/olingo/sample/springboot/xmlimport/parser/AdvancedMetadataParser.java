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
package org.apache.olingo.sample.springboot.xmlimport.parser;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.server.core.MetadataParser;
import org.apache.olingo.server.core.ReferenceResolver;
import org.apache.olingo.server.core.SchemaBasedEdmProvider;
import org.springframework.core.io.ClassPathResource;

public class AdvancedMetadataParser {
    
    private boolean detectCircularDependencies = true;
    private boolean allowCircularDependencies = false;
    private boolean enableCaching = true;
    private int maxDependencyDepth = 10;
    
    private final Map<String, SchemaBasedEdmProvider> providerCache = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> dependencyGraph = new ConcurrentHashMap<>();
    private final Set<String> currentlyLoading = ConcurrentHashMap.newKeySet();
    private final Map<String, List<String>> errorReport = new ConcurrentHashMap<>();
    
    private final List<ReferenceResolver> referenceResolvers = new ArrayList<>();
    private final MetadataParser underlyingParser;
    
    /**
     * Statistics and metrics
     */
    public static class ParseStatistics {
        private int totalFilesProcessed = 0;
        private int cachedFilesReused = 0;
        private int circularDependenciesDetected = 0;
        private int maxDepthReached = 0;
        private long totalParsingTime = 0;
        private final Map<String, Integer> errorCounts = new HashMap<>();
        
        // Getters
        public int getTotalFilesProcessed() { return totalFilesProcessed; }
        public int getCachedFilesReused() { return cachedFilesReused; }
        public int getCircularDependenciesDetected() { return circularDependenciesDetected; }
        public int getMaxDepthReached() { return maxDepthReached; }
        public long getTotalParsingTime() { return totalParsingTime; }
        public Map<String, Integer> getErrorCounts() { return errorCounts; }
        
        void incrementFilesProcessed() { totalFilesProcessed++; }
        void incrementCachedReused() { cachedFilesReused++; }
        void incrementCircularDetected() { circularDependenciesDetected++; }
        void updateMaxDepth(int depth) { maxDepthReached = Math.max(maxDepthReached, depth); }
        void addParsingTime(long time) { totalParsingTime += time; }
        void incrementError(String errorType) { errorCounts.put(errorType, errorCounts.getOrDefault(errorType, 0) + 1); }
    }
    
    private final ParseStatistics statistics = new ParseStatistics();
    
    /**
     * Constructor
     */
    public AdvancedMetadataParser() {
        this.underlyingParser = new MetadataParser();
        
        // Add default reference resolvers
        addReferenceResolver(new ClassPathReferenceResolver());
        addReferenceResolver(new FileSystemReferenceResolver());
        addReferenceResolver(new UrlReferenceResolver());
        
        // Configure underlying parser
        underlyingParser.recursivelyLoadReferences(false); // We handle this ourselves
        underlyingParser.useLocalCoreVocabularies(true);
        underlyingParser.implicitlyLoadCoreVocabularies(true);
    }
    
    /**
     * Configuration methods
     */
    public AdvancedMetadataParser detectCircularDependencies(boolean detect) {
        this.detectCircularDependencies = detect;
        return this;
    }
    
    public AdvancedMetadataParser allowCircularDependencies(boolean allow) {
        this.allowCircularDependencies = allow;
        return this;
    }
    
    public AdvancedMetadataParser enableCaching(boolean enable) {
        this.enableCaching = enable;
        return this;
    }
    
    public AdvancedMetadataParser maxDependencyDepth(int depth) {
        this.maxDependencyDepth = depth;
        return this;
    }
    
    public AdvancedMetadataParser addReferenceResolver(ReferenceResolver resolver) {
        this.referenceResolvers.add(resolver);
        return this;
    }
    
    /**
     * Main parsing method with advanced dependency resolution
     */
    public SchemaBasedEdmProvider buildEdmProvider(String mainSchemaPath) throws Exception {
        long startTime = System.currentTimeMillis();
        
        try {
            // Clear state for new parsing operation
            clearState();
            
            // Build dependency graph
            buildDependencyGraph(mainSchemaPath, 0);
            
            // Check for circular dependencies
            if (detectCircularDependencies) {
                List<List<String>> cycles = detectCircularDependencies();
                if (!cycles.isEmpty()) {
                    statistics.incrementCircularDetected();
                    handleCircularDependencies(cycles);
                }
            }
            
            // Resolve dependencies in topological order
            List<String> loadOrder = calculateLoadOrder();
            
            // Load schemas in dependency order
            SchemaBasedEdmProvider result = loadSchemasInOrder(loadOrder, mainSchemaPath);
            
            return result;
            
        } catch (Exception e) {
            statistics.incrementError("parsing_error");
            throw e;
        } finally {
            statistics.addParsingTime(System.currentTimeMillis() - startTime);
        }
    }
    
    /**
     * Build dependency graph by analyzing all references
     */
    private void buildDependencyGraph(String schemaPath, int depth) throws Exception {
        if (depth > maxDependencyDepth) {
            throw new IllegalStateException("Maximum dependency depth exceeded: " + maxDependencyDepth);
        }
        
        statistics.updateMaxDepth(depth);
        
        if (dependencyGraph.containsKey(schemaPath)) {
            return; // Already analyzed
        }
        
        Set<String> dependencies = new HashSet<>();
        dependencyGraph.put(schemaPath, dependencies);
        
        try {
            // Load the schema to analyze its references
            InputStream inputStream = resolveReference(schemaPath);
            if (inputStream == null) {
                statistics.incrementError("schema_not_found");
                throw new IllegalArgumentException("Schema not found: " + schemaPath);
            }
            
            // Parse just the references (not the full schema)
            SchemaBasedEdmProvider tempProvider = underlyingParser.buildEdmProvider(new InputStreamReader(inputStream));
            
            // Extract references
            for (EdmxReference reference : tempProvider.getReferences()) {
                String refPath = reference.getUri().toString();
                dependencies.add(refPath);
                
                // Recursively analyze dependencies
                buildDependencyGraph(refPath, depth + 1);
            }
            
        } catch (Exception e) {
            statistics.incrementError("dependency_analysis_error");
            errorReport.put(schemaPath, Arrays.asList("Dependency analysis failed: " + e.getMessage()));
            throw e;
        }
    }
    
    /**
     * Detect circular dependencies using DFS
     */
    private List<List<String>> detectCircularDependencies() {
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
     * DFS helper for cycle detection
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
                    List<String> cycle = new ArrayList<>(currentPath.subList(cycleStart, currentPath.size()));
                    cycle.add(dependency); // Complete the cycle
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
    private void handleCircularDependencies(List<List<String>> cycles) throws Exception {
        for (List<String> cycle : cycles) {
            errorReport.put("circular_dependency", cycle);
        }
        
        if (!allowCircularDependencies) {
            throw new IllegalStateException("Circular dependencies detected and not allowed. Cycles: " + cycles);
        }
    }
    
    /**
     * Calculate load order using topological sorting
     */
    private List<String> calculateLoadOrder() {
        List<String> loadOrder = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> temporaryMark = new HashSet<>();
        
        for (String node : dependencyGraph.keySet()) {
            if (!visited.contains(node)) {
                topologicalSort(node, visited, temporaryMark, loadOrder);
            }
        }
        
        Collections.reverse(loadOrder); // Reverse to get correct dependency order
        return loadOrder;
    }
    
    /**
     * Topological sort helper
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
    
    /**
     * Load schemas in the calculated order
     */
    private SchemaBasedEdmProvider loadSchemasInOrder(List<String> loadOrder, String mainSchemaPath) throws Exception {
        SchemaBasedEdmProvider result = new SchemaBasedEdmProvider();
        
        // Load dependencies first
        for (String schemaPath : loadOrder) {
            if (!schemaPath.equals(mainSchemaPath)) {
                loadSchema(schemaPath, result);
            }
        }
        
        // Load main schema last
        loadSchema(mainSchemaPath, result);
        
        return result;
    }
    
    /**
     * Load a single schema with caching
     */
    private void loadSchema(String schemaPath, SchemaBasedEdmProvider targetProvider) throws Exception {
        // Check cache first
        if (enableCaching && providerCache.containsKey(schemaPath)) {
            SchemaBasedEdmProvider cachedProvider = providerCache.get(schemaPath);
            copySchemas(cachedProvider, targetProvider);
            statistics.incrementCachedReused();
            return;
        }
        
        // Check for recursive loading
        if (currentlyLoading.contains(schemaPath)) {
            return;
        }
        
        currentlyLoading.add(schemaPath);
        
        try {
            // Resolve and load schema
            InputStream inputStream = resolveReference(schemaPath);
            if (inputStream == null) {
                statistics.incrementError("schema_resolution_failed");
                throw new IllegalArgumentException("Could not resolve schema: " + schemaPath);
            }
            
            // Parse schema using underlying parser
            SchemaBasedEdmProvider schemaProvider = underlyingParser.buildEdmProvider(new InputStreamReader(inputStream));
            
            // Cache the provider
            if (enableCaching) {
                providerCache.put(schemaPath, schemaProvider);
            }
            
            // Copy schemas to target provider
            copySchemas(schemaProvider, targetProvider);
            
            // Copy references using reflection to access protected methods
            for (EdmxReference reference : schemaProvider.getReferences()) {
                addReferenceUsingReflection(targetProvider, reference);
            }
            
            statistics.incrementFilesProcessed();
            
        } catch (Exception e) {
            statistics.incrementError("schema_loading_error");
            errorReport.put(schemaPath, Arrays.asList("Schema loading failed: " + e.getMessage()));
            throw e;
        } finally {
            currentlyLoading.remove(schemaPath);
        }
    }
    
    /**
     * Copy schemas from source provider to target provider using reflection
     */
    private void copySchemas(SchemaBasedEdmProvider source, SchemaBasedEdmProvider target) throws Exception {
        for (CsdlSchema schema : source.getSchemas()) {
            addSchemaUsingReflection(target, schema);
        }
    }
    
    /**
     * Add reference using reflection to access protected method
     */
    private void addReferenceUsingReflection(SchemaBasedEdmProvider provider, EdmxReference reference) 
            throws Exception {
        try {
            java.lang.reflect.Method method = SchemaBasedEdmProvider.class.getDeclaredMethod("addReference", EdmxReference.class);
            method.setAccessible(true);
            method.invoke(provider, reference);
        } catch (Exception e) {
            throw e;
        }
    }
    
    /**
     * Add schema using reflection to access protected method
     */
    private void addSchemaUsingReflection(SchemaBasedEdmProvider provider, CsdlSchema schema) 
            throws Exception {
        try {
            java.lang.reflect.Method method = SchemaBasedEdmProvider.class.getDeclaredMethod("addSchema", CsdlSchema.class);
            method.setAccessible(true);
            method.invoke(provider, schema);
        } catch (Exception e) {
            throw e;
        }
    }
    
    /**
     * Resolve reference using multiple strategies
     */
    private InputStream resolveReference(String referencePath) {
        for (ReferenceResolver resolver : referenceResolvers) {
            try {
                URI uri = URI.create(referencePath);
                InputStream inputStream = resolver.resolveReference(uri, null);
                if (inputStream != null) {
                    return inputStream;
                }
            } catch (Exception e) {
                // Continue to next resolver
            }
        }
        
        return null;
    }
    
    /**
     * Clear internal state
     */
    private void clearState() {
        dependencyGraph.clear();
        currentlyLoading.clear();
        errorReport.clear();
        
        if (!enableCaching) {
            providerCache.clear();
        }
    }
    
    /**
     * Get parsing statistics
     */
    public ParseStatistics getStatistics() {
        return statistics;
    }
    
    /**
     * Get error report
     */
    public Map<String, List<String>> getErrorReport() {
        return new HashMap<>(errorReport);
    }
    
    /**
     * Clear cache
     */
    public void clearCache() {
        providerCache.clear();
    }
    
    /**
     * ClassPath Reference Resolver
     */
    private static class ClassPathReferenceResolver implements ReferenceResolver {
        @Override
        public InputStream resolveReference(URI referenceUri, String xmlBase) {
            try {
                String path = referenceUri.getPath();
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                
                ClassPathResource resource = new ClassPathResource(path);
                if (resource.exists()) {
                    return resource.getInputStream();
                }
            } catch (Exception e) {
                // Ignore and return null
            }
            return null;
        }
    }
    
    /**
     * File System Reference Resolver
     */
    private static class FileSystemReferenceResolver implements ReferenceResolver {
        @Override
        public InputStream resolveReference(URI referenceUri, String xmlBase) {
            try {
                File file = new File(referenceUri.getPath());
                if (file.exists() && file.isFile()) {
                    return new FileInputStream(file);
                }
            } catch (Exception e) {
                // Ignore and return null
            }
            return null;
        }
    }
    
    /**
     * URL Reference Resolver
     */
    private static class UrlReferenceResolver implements ReferenceResolver {
        @Override
        public InputStream resolveReference(URI referenceUri, String xmlBase) {
            try {
                if (referenceUri.isAbsolute()) {
                    return referenceUri.toURL().openStream();
                } else if (xmlBase != null) {
                    URI baseUri = URI.create(xmlBase);
                    URI resolvedUri = baseUri.resolve(referenceUri);
                    return resolvedUri.toURL().openStream();
                }
            } catch (Exception e) {
                // Ignore and return null
            }
            return null;
        }
    }
}
