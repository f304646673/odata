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

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.olingo.server.core.MetadataParser;
import org.apache.olingo.server.core.ReferenceResolver;
import org.apache.olingo.server.core.SchemaBasedEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.commons.api.edmx.EdmxReferenceInclude;
import java.net.URI;

/**
 * Modular Advanced Metadata Parser that uses composition of smaller modules
 * to handle complex OData schema parsing with dependency resolution.
 */
public class ModularAdvancedMetadataParser {
    
    private boolean detectCircularDependencies = true;
    private boolean allowCircularDependencies = false;
    private boolean enableCaching = true;
    private int maxDependencyDepth = 10;
    
    private final Map<String, List<String>> errorReport = new ConcurrentHashMap<>();
    private final ParseStatistics statistics = new ParseStatistics();
    private final MetadataParser underlyingParser;
    
    // Modular components
    private final DependencyGraphManager dependencyManager;
    private final SchemaComparator schemaComparator;
    private final SchemaMerger schemaMerger;
    private final ReferenceResolverManager referenceManager;
    private final SchemaValidator schemaValidator;
    private final CacheManager cacheManager;
    
    /**
     * Constructor
     */
    public ModularAdvancedMetadataParser() {
        this.underlyingParser = new MetadataParser();
        
        // Initialize modular components
        this.dependencyManager = new DependencyGraphManager(statistics, errorReport);
        this.schemaComparator = new SchemaComparator();
        this.schemaMerger = new SchemaMerger(schemaComparator, statistics, errorReport);
        this.referenceManager = new ReferenceResolverManager();
        this.schemaValidator = new SchemaValidator(statistics);
        this.cacheManager = new CacheManager(enableCaching);
        
        // Add default reference resolvers
        addReferenceResolver(new ClassPathReferenceResolver());
        addReferenceResolver(new FileSystemReferenceResolver());
        addReferenceResolver(new UrlReferenceResolver());
        
        // Configure underlying parser
        underlyingParser.recursivelyLoadReferences(false); // We handle this ourselves
        underlyingParser.useLocalCoreVocabularies(true);
        underlyingParser.implicitlyLoadCoreVocabularies(true);
        underlyingParser.parseAnnotations(true);
    }
    
    /**
     * Configuration methods
     */
    public ModularAdvancedMetadataParser detectCircularDependencies(boolean detect) {
        this.detectCircularDependencies = detect;
        return this;
    }
    
    public ModularAdvancedMetadataParser allowCircularDependencies(boolean allow) {
        this.allowCircularDependencies = allow;
        return this;
    }
    
    public ModularAdvancedMetadataParser enableCaching(boolean enable) {
        this.enableCaching = enable;
        this.cacheManager.setEnabled(enable);
        return this;
    }
    
    public ModularAdvancedMetadataParser maxDependencyDepth(int depth) {
        this.maxDependencyDepth = depth;
        return this;
    }
    
    public ModularAdvancedMetadataParser addReferenceResolver(ReferenceResolver resolver) {
        this.referenceManager.addReferenceResolver(resolver);
        return this;
    }
    
    /**
     * Main parsing method with advanced dependency resolution
     */
    public SchemaBasedEdmProvider buildEdmProvider(String mainSchemaPath) throws Exception {
        long startTime = System.currentTimeMillis();
        
        try {
            // Check if main schema file exists first
            File mainSchemaFile = new File(mainSchemaPath);
            if (!mainSchemaFile.exists()) {
                statistics.addError(ErrorType.FILE_NOT_FOUND, "File does not exist", mainSchemaPath);
                throw new IllegalArgumentException("File not found: " + mainSchemaPath);
            }
            
            // Clear state for new parsing session
            clearState();
            
            statistics.recordStart();
            
            // Build dependency graph
            buildDependencyGraph(mainSchemaPath, 0);
            
            // Detect circular dependencies if enabled
            if (detectCircularDependencies) {
                List<List<String>> cycles = dependencyManager.detectCircularDependencies();
                dependencyManager.handleCircularDependencies(cycles, allowCircularDependencies);
            }
            
            // Calculate load order
            List<String> loadOrder = dependencyManager.calculateLoadOrder();
            
            // Load schemas in the calculated order
            SchemaBasedEdmProvider provider = loadSchemasInOrder(loadOrder, mainSchemaPath);
            
            // Add references from dependency graph to provider
            addReferencesToProvider(provider);
            
            // Validate references
            schemaValidator.validateReferences(provider);
            
            statistics.recordEnd();
            statistics.setLoadOrder(loadOrder);
            
            return provider;
            
        } catch (Exception e) {
            statistics.recordEnd();
            statistics.addError(ErrorType.PARSING_ERROR, e.getMessage(), mainSchemaPath);
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            statistics.setTotalTime(endTime - startTime);
            statistics.addParsingTime(endTime - startTime);
        }
    }
    
    /**
     * Build dependency graph by recursively parsing schema references
     */
    private void buildDependencyGraph(String schemaPath, int depth) throws Exception {
        if (depth > maxDependencyDepth) {
            String error = String.format("Maximum dependency depth (%d) exceeded for schema: %s", 
                maxDependencyDepth, schemaPath);
            statistics.addError(ErrorType.DEPENDENCY_DEPTH_EXCEEDED, error, schemaPath);
            throw new IllegalStateException(error);
        }
        
        if (dependencyManager.containsSchema(schemaPath)) {
            return; // Already processed
        }
        
        if (dependencyManager.isCurrentlyLoading(schemaPath)) {
            // Circular dependency detected during parsing
            statistics.addError(ErrorType.CIRCULAR_DEPENDENCY,
                "Circular dependency detected during parsing: " + schemaPath,
                schemaPath);
            return;
        }
        
        // Update max depth tracking - only for schemas that are actually processed
        statistics.updateMaxDepth(depth);
        
        dependencyManager.markLoading(schemaPath);
        
        try {
            // Extract references from XML
            Set<String> references = referenceManager.extractReferencesFromXml(schemaPath);
            
            // Add dependencies to graph
            for (String reference : references) {
                dependencyManager.addDependency(schemaPath, reference);
                
                // Recursively build dependency graph for each reference
                buildDependencyGraph(reference, depth + 1);
            }
            
            statistics.incrementSchemasProcessed();
            
        } finally {
            dependencyManager.markFinished(schemaPath);
        }
    }
    
    /**
     * Load schemas in the calculated order
     */
    private SchemaBasedEdmProvider loadSchemasInOrder(List<String> loadOrder, String mainSchemaPath) throws Exception {
        SchemaBasedEdmProvider targetProvider = new SchemaBasedEdmProvider();
        
        for (String schemaPath : loadOrder) {
            loadSchema(schemaPath, targetProvider);
        }
        
        // Always make sure the main schema is loaded
        if (!loadOrder.contains(mainSchemaPath)) {
            loadSchema(mainSchemaPath, targetProvider);
        }
        
        return targetProvider;
    }
    
    /**
     * Load a single schema with caching
     */
    private void loadSchema(String schemaPath, SchemaBasedEdmProvider targetProvider) throws Exception {
        try {
            // Check cache first
            SchemaBasedEdmProvider cachedProvider = cacheManager.getCachedProvider(schemaPath);
            if (cachedProvider != null) {
                schemaMerger.copySchemas(cachedProvider, targetProvider);
                return;
            }
            
            // Load from file
            InputStream inputStream = referenceManager.resolveReference(schemaPath);
            if (inputStream == null) {
                String error = "Cannot resolve reference: " + schemaPath;
                statistics.addError(ErrorType.REFERENCE_NOT_FOUND, error, schemaPath);
                throw new IllegalArgumentException(error);
            }
            
            try {
                SchemaBasedEdmProvider sourceProvider = underlyingParser.buildEdmProvider(new InputStreamReader(inputStream));
                
                // Cache the provider if caching is enabled
                cacheManager.cacheProvider(schemaPath, sourceProvider);
                
                // Copy schemas from source to target
                schemaMerger.copySchemas(sourceProvider, targetProvider);
                
                statistics.incrementFilesProcessed();
                
            } finally {
                inputStream.close();
            }
            
        } catch (Exception e) {
            String error = String.format("Error loading schema '%s': %s", schemaPath, e.getMessage());
            statistics.addError(ErrorType.PARSING_ERROR, error, schemaPath);
            errorReport.computeIfAbsent(schemaPath, k -> new ArrayList<>()).add(error);
            throw e;
        } finally {
            statistics.incrementSchemasLoaded();
        }
    }
    
    /**
     * Add references from dependency graph to provider
     */
    private void addReferencesToProvider(SchemaBasedEdmProvider provider) throws Exception {
        // Get all dependency relationships from the dependency manager
        Map<String, Set<String>> allDependencies = dependencyManager.getAllDependencies();
        
        for (Map.Entry<String, Set<String>> entry : allDependencies.entrySet()) {
            String schemaPath = entry.getKey();
            Set<String> dependencies = entry.getValue();
            
            for (String dependencyPath : dependencies) {
                // Create a reference provider for the dependency
                SchemaBasedEdmProvider dependencyProvider = createProviderForReference(dependencyPath);
                if (dependencyProvider != null) {
                    // Extract namespace from the dependency provider and create EdmxReference
                    List<CsdlSchema> schemas = dependencyProvider.getSchemas();
                    for (CsdlSchema schema : schemas) {
                        // Create EdmxReference with includes
                        EdmxReference reference = new EdmxReference(URI.create(dependencyPath));
                        EdmxReferenceInclude include = new EdmxReferenceInclude(schema.getNamespace());
                        reference.addInclude(include);
                        
                        // Use reflection to add reference 
                        addReferenceUsingReflection(provider, reference);
                    }
                }
            }
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
     * Create a provider for a reference dependency
     */
    private SchemaBasedEdmProvider createProviderForReference(String referencePath) throws Exception {
        try {
            InputStream inputStream = referenceManager.resolveReference(referencePath);
            if (inputStream == null) {
                return null;
            }
            
            try {
                return underlyingParser.buildEdmProvider(new InputStreamReader(inputStream));
            } finally {
                inputStream.close();
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Clear internal state
     */
    private void clearState() {
        dependencyManager.clearState();
        errorReport.clear();
        
        // Note: we don't clear the cache here as it should persist across builds
        // unless explicitly disabled
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
        cacheManager.clearCache();
    }
}
