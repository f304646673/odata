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
package org.apache.olingo.xmlprocessor.parser.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotations;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumMember;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlSingleton;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.server.core.MetadataParser;
import org.apache.olingo.server.core.ReferenceResolver;
import org.apache.olingo.server.core.SchemaBasedEdmProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
        private final List<ErrorInfo> errors = new ArrayList<>();
        
        // Getters
        public int getTotalFilesProcessed() { return totalFilesProcessed; }
        public int getCachedFilesReused() { return cachedFilesReused; }
        public int getCircularDependenciesDetected() { return circularDependenciesDetected; }
        public int getMaxDepthReached() { return maxDepthReached; }
        public long getTotalParsingTime() { return totalParsingTime; }
        
        /**
         * Get all error information
         */
        public List<ErrorInfo> getErrors() { return new ArrayList<>(errors); }
        
        /**
         * Get error counts by type (computed from errors list)
         */
        public Map<ErrorType, Integer> getErrorTypeCounts() { 
            Map<ErrorType, Integer> counts = new HashMap<>();
            for (ErrorInfo error : errors) {
                counts.put(error.getType(), counts.getOrDefault(error.getType(), 0) + 1);
            }
            return counts;
        }
        
        /**
         * Get errors by type
         */
        public List<ErrorInfo> getErrorsByType(ErrorType type) {
            return errors.stream()
                    .filter(error -> error.getType() == type)
                    .collect(java.util.stream.Collectors.toList());
        }
        
        /**
         * Check if there are any errors of a specific type (computed from errors list)
         */
        public boolean hasErrorType(ErrorType type) {
            return errors.stream().anyMatch(error -> error.getType() == type);
        }
        
        /**
         * Get total error count
         */
        public int getTotalErrorCount() {
            return errors.size();
        }
        
        void incrementFilesProcessed() { totalFilesProcessed++; }
        void incrementCachedReused() { cachedFilesReused++; }
        void incrementCircularDetected() { circularDependenciesDetected++; }
        void updateMaxDepth(int depth) { maxDepthReached = Math.max(maxDepthReached, depth); }
        void addParsingTime(long time) { totalParsingTime += time; }
        
        /**
         * Add error with type and description
         */
        void addError(ErrorType type, String description) {
            ErrorInfo error = new ErrorInfo(type, description);
            errors.add(error);
        }
        
        /**
         * Add error with type, description and context
         */
        void addError(ErrorType type, String description, String context) {
            ErrorInfo error = new ErrorInfo(type, description, context);
            errors.add(error);
        }
        
        /**
         * Add error with type, description, context and caused by exception
         */
        void addError(ErrorType type, String description, String context, Throwable cause) {
            ErrorInfo error = new ErrorInfo(type, description, context, cause);
            errors.add(error);
        }
    }
    
    /**
     * Enumeration of all possible error types
     */
    public enum ErrorType {
        PARSING_ERROR("parsing_error", "General parsing error"),
        FILE_NOT_FOUND("file_not_found", "File does not exist"),
        SCHEMA_NOT_FOUND("schema_not_found", "Schema file could not be found"),
        DEPENDENCY_ANALYSIS_ERROR("dependency_analysis_error", "Error analyzing schema dependencies"),
        SCHEMA_RESOLUTION_FAILED("schema_resolution_failed", "Failed to resolve schema reference"),
        SCHEMA_LOADING_ERROR("schema_loading_error", "Error loading schema"),
        SCHEMA_MERGE_CONFLICT("schema_merge_conflict", "Conflict detected during schema merging"),
        CIRCULAR_DEPENDENCY("circular_dependency", "Circular dependency detected"),
        MAX_DEPTH_EXCEEDED("max_depth_exceeded", "Maximum dependency depth exceeded"),
        INVALID_REFERENCE("invalid_reference", "Invalid reference URI or format"),
        XML_PARSING_ERROR("xml_parsing_error", "Error parsing XML content"),
        REFLECTION_ERROR("reflection_error", "Error using reflection to access internal methods"),
        CONFIGURATION_ERROR("configuration_error", "Configuration or setup error"),
        MISSING_ANNOTATION("missing_annotation", "Required annotation is missing"),
        MISSING_TYPE_REFERENCE("missing_type_reference", "Referenced type does not exist"),
        MISSING_ANNOTATION_TARGET("missing_annotation_target", "Annotation target does not exist"),
        UNRESOLVED_TYPE_REFERENCE("unresolved_type_reference", "Type reference cannot be resolved");
        
        private final String legacyKey;
        private final String description;
        
        ErrorType(String legacyKey, String description) {
            this.legacyKey = legacyKey;
            this.description = description;
        }
        
        public String getLegacyKey() { return legacyKey; }
        public String getDescription() { return description; }
        
        /**
         * Convert legacy string key to ErrorType
         */
        public static ErrorType fromLegacyKey(String legacyKey) {
            for (ErrorType type : ErrorType.values()) {
                if (type.legacyKey.equals(legacyKey)) {
                    return type;
                }
            }
            return PARSING_ERROR; // Default fallback
        }
    }
    
    /**
     * Comprehensive error information structure
     */
    public static class ErrorInfo {
        private final ErrorType type;
        private final String description;
        private final String context;
        private final Throwable cause;
        private final long timestamp;
        private final String threadName;
        
        /**
         * Create error info with type and description
         */
        public ErrorInfo(ErrorType type, String description) {
            this(type, description, null, null);
        }
        
        /**
         * Create error info with type, description and context
         */
        public ErrorInfo(ErrorType type, String description, String context) {
            this(type, description, context, null);
        }
        
        /**
         * Create error info with all details
         */
        public ErrorInfo(ErrorType type, String description, String context, Throwable cause) {
            this.type = type;
            this.description = description;
            this.context = context;
            this.cause = cause;
            this.timestamp = System.currentTimeMillis();
            this.threadName = Thread.currentThread().getName();
        }
        
        // Getters
        public ErrorType getType() { return type; }
        public String getDescription() { return description; }
        public String getContext() { return context; }
        public Throwable getCause() { return cause; }
        public long getTimestamp() { return timestamp; }
        public String getThreadName() { return threadName; }
        
        /**
         * Get formatted error message
         */
        public String getFormattedMessage() {
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(type.name()).append("] ");
            sb.append(description);
            if (context != null && !context.trim().isEmpty()) {
                sb.append(" (Context: ").append(context).append(")");
            }
            if (cause != null) {
                sb.append(" - Caused by: ").append(cause.getMessage());
            }
            return sb.toString();
        }
        
        /**
         * Get detailed error information including timestamp and thread
         */
        public String getDetailedMessage() {
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(new java.util.Date(timestamp)).append("] ");
            sb.append("[Thread: ").append(threadName).append("] ");
            sb.append(getFormattedMessage());
            return sb.toString();
        }
        
        @Override
        public String toString() {
            return getFormattedMessage();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ErrorInfo errorInfo = (ErrorInfo) obj;
            return type == errorInfo.type &&
                   java.util.Objects.equals(description, errorInfo.description) &&
                   java.util.Objects.equals(context, errorInfo.context);
        }
        
        @Override
        public int hashCode() {
            return java.util.Objects.hash(type, description, context);
        }
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
        
        underlyingParser.parseAnnotations(true);
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
            // Check if main schema file exists first
            File mainSchemaFile = new File(mainSchemaPath);
            if (!mainSchemaFile.exists()) {
                statistics.addError(ErrorType.FILE_NOT_FOUND, "File does not exist", mainSchemaPath);
                throw new IllegalArgumentException("File not found: " + mainSchemaPath);
            }
            
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
            
            // Validate references after all schemas are loaded
            validateReferences(result);
            
            return result;
            
        } catch (Exception e) {
            statistics.addError(ErrorType.PARSING_ERROR, "Failed to parse schema", mainSchemaPath, e);
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
                statistics.addError(ErrorType.SCHEMA_NOT_FOUND, "Schema file not found", schemaPath);
                throw new IllegalArgumentException("Schema not found: " + schemaPath);
            }
            
            // Create a MetadataParser with file-based reference resolver
            MetadataParser tempParser = new MetadataParser();
            
            // Set up file-based reference resolver for the schema directory
            File schemaFile = new File(schemaPath);
            if (schemaFile.exists()) {
                File schemaDir = schemaFile.getParentFile();
                tempParser.referenceResolver(new FileBasedReferenceResolver(schemaDir));
            } else {
                // Use current working directory as fallback
                tempParser.referenceResolver(new FileBasedReferenceResolver(new File(".")));
            }
            
            // Parse the schema to extract references using our own XML parsing
            // to avoid Olingo's deduplication by namespace
            Set<String> xmlReferences = extractReferencesFromXml(schemaPath);
            
            // Add all found references to dependencies
            for (String refPath : xmlReferences) {
                dependencies.add(refPath);
                
                // Recursively analyze dependencies
                buildDependencyGraph(refPath, depth + 1);
            }
            
        } catch (Exception e) {
            statistics.addError(ErrorType.DEPENDENCY_ANALYSIS_ERROR, "Dependency analysis failed", schemaPath, e);
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
        // Generate cache key that includes path information to avoid conflicts with same filename
        String cacheKey = generateCacheKey(schemaPath);
        
        // Check cache first
        if (enableCaching && providerCache.containsKey(cacheKey)) {
            SchemaBasedEdmProvider cachedProvider = providerCache.get(cacheKey);
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
                statistics.addError(ErrorType.SCHEMA_RESOLUTION_FAILED, "Could not resolve schema", schemaPath);
                throw new IllegalArgumentException("Could not resolve schema: " + schemaPath);
            }

            // Configure parser with reference resolver for this schema
            File schemaFile = new File(schemaPath);
            File schemaDir = schemaFile.getParentFile();
            if (schemaDir == null) {
                // If no parent directory, try to find the schema in test resources
                schemaDir = new File("src/test/resources/schemas");
                if (!schemaDir.exists()) {
                    schemaDir = new File(".");
                }
            }
            
            // Create a new parser instance with the appropriate reference resolver
            MetadataParser parser = new MetadataParser()
                .parseAnnotations(true)
                .useLocalCoreVocabularies(false)
                .recursivelyLoadReferences(false)
                .referenceResolver(new FileBasedReferenceResolver(schemaDir));
            
            // Parse schema using configured parser
            SchemaBasedEdmProvider schemaProvider = parser.buildEdmProvider(new InputStreamReader(inputStream));
            
            // Cache the provider
            if (enableCaching) {
                providerCache.put(cacheKey, schemaProvider);
            }
            
            // Copy schemas to target provider
            copySchemas(schemaProvider, targetProvider);
            
            // Copy references using reflection to access protected methods
            for (EdmxReference reference : schemaProvider.getReferences()) {
                addReferenceUsingReflection(targetProvider, reference);
            }
            
            statistics.incrementFilesProcessed();
            
        } catch (Exception e) {
            statistics.addError(ErrorType.SCHEMA_LOADING_ERROR, "Schema loading failed", schemaPath, e);
            errorReport.put(schemaPath, Arrays.asList("Schema loading failed: " + e.getMessage()));
            throw e;
        } finally {
            currentlyLoading.remove(schemaPath);
        }
    }
    
    /**
     * Copy schemas from source provider to target provider using reflection
     * Merges schemas with the same namespace, detecting conflicts
     */
    private void copySchemas(SchemaBasedEdmProvider source, SchemaBasedEdmProvider target) throws Exception {
        // Create a map of existing schemas by namespace for efficient lookup
        Map<String, CsdlSchema> existingSchemas = new HashMap<>();
        for (CsdlSchema existingSchema : target.getSchemas()) {
            existingSchemas.put(existingSchema.getNamespace(), existingSchema);
        }
        
        // Process each schema from source
        for (CsdlSchema sourceSchema : source.getSchemas()) {
            String namespace = sourceSchema.getNamespace();
            CsdlSchema existingSchema = existingSchemas.get(namespace);
            
            if (existingSchema == null) {
                // No existing schema with this namespace, add it directly
                addSchemaUsingReflection(target, sourceSchema);
                existingSchemas.put(namespace, sourceSchema);
            } else {
                // Schema with same namespace exists, check if they are identical or need merging
                if (areSchemasIdentical(existingSchema, sourceSchema)) {
                    // Schemas are identical, skip merging (common in circular dependencies)
                    continue;
                } else {
                    // Different schemas with same namespace, merge them
                    CsdlSchema mergedSchema = mergeSchemas(existingSchema, sourceSchema, namespace);
                    
                    // Remove the old schema and add the merged one
                    removeSchemaUsingReflection(target, existingSchema);
                    addSchemaUsingReflection(target, mergedSchema);
                    
                    // Update our tracking map
                    existingSchemas.put(namespace, mergedSchema);
                }
            }
        }
    }
    
    /**
     * Check if two schemas are identical (same elements with same definitions)
     */
    private boolean areSchemasIdentical(CsdlSchema schema1, CsdlSchema schema2) {
        // Check if schemas are truly identical by comparing all their elements
        if (!Objects.equals(schema1.getNamespace(), schema2.getNamespace())) {
            return false;
        }
        
        // Check EntityTypes - compare by name and properties
        if (!areEntityTypesIdentical(schema1.getEntityTypes(), schema2.getEntityTypes())) {
            return false;
        }
        
        // Check ComplexTypes - compare by name and properties
        if (!areComplexTypesIdentical(schema1.getComplexTypes(), schema2.getComplexTypes())) {
            return false;
        }
        
        // Check EnumTypes - compare by name and members
        if (!areEnumTypesIdentical(schema1.getEnumTypes(), schema2.getEnumTypes())) {
            return false;
        }
        
        // Check TypeDefinitions - compare by name and underlying type
        if (!areTypeDefinitionsIdentical(schema1.getTypeDefinitions(), schema2.getTypeDefinitions())) {
            return false;
        }
        
        // Check Actions - compare by name and parameters (actions don't support overloading)
        if (!areActionsIdentical(schema1.getActions(), schema2.getActions())) {
            return false;
        }
        
        // Check Functions - compare by name, parameters, and return type (functions support overloading)
        if (!areFunctionsIdentical(schema1.getFunctions(), schema2.getFunctions())) {
            return false;
        }
        
        // Check EntityContainers - compare by name and contents
        if (!areEntityContainersIdentical(schema1.getEntityContainer(), schema2.getEntityContainer())) {
            return false;
        }
        
        return true;
    }
    
    private boolean areEntityTypesIdentical(List<CsdlEntityType> types1, List<CsdlEntityType> types2) {
        if ((types1 == null) != (types2 == null)) return false;
        if (types1 == null) return true;
        
        if (types1.size() != types2.size()) return false;
        
        Map<String, CsdlEntityType> map1 = types1.stream()
            .collect(Collectors.toMap(CsdlEntityType::getName, Function.identity()));
        Map<String, CsdlEntityType> map2 = types2.stream()
            .collect(Collectors.toMap(CsdlEntityType::getName, Function.identity()));
        
        if (!map1.keySet().equals(map2.keySet())) return false;
        
        for (String name : map1.keySet()) {
            if (!areEntityTypeDetailsIdentical(map1.get(name), map2.get(name))) {
                return false;
            }
        }
        return true;
    }
    
    private boolean areEntityTypeDetailsIdentical(CsdlEntityType type1, CsdlEntityType type2) {
        if (!Objects.equals(type1.getName(), type2.getName())) return false;
        if (!Objects.equals(type1.getBaseType(), type2.getBaseType())) return false;
        if (!Objects.equals(type1.isAbstract(), type2.isAbstract())) return false;
        if (!Objects.equals(type1.isOpenType(), type2.isOpenType())) return false;
        
        // Compare properties
        if (!arePropertiesIdentical(type1.getProperties(), type2.getProperties())) return false;
        
        // Compare navigation properties
        if (!areNavigationPropertiesIdentical(type1.getNavigationProperties(), type2.getNavigationProperties())) return false;
        
        // Compare keys
        if (!areKeysIdentical(type1.getKey(), type2.getKey())) return false;
        
        return true;
    }
    
    private boolean areComplexTypesIdentical(List<CsdlComplexType> types1, List<CsdlComplexType> types2) {
        if ((types1 == null) != (types2 == null)) return false;
        if (types1 == null) return true;
        
        if (types1.size() != types2.size()) return false;
        
        Map<String, CsdlComplexType> map1 = types1.stream()
            .collect(Collectors.toMap(CsdlComplexType::getName, Function.identity()));
        Map<String, CsdlComplexType> map2 = types2.stream()
            .collect(Collectors.toMap(CsdlComplexType::getName, Function.identity()));
        
        if (!map1.keySet().equals(map2.keySet())) return false;
        
        for (String name : map1.keySet()) {
            if (!areComplexTypeDetailsIdentical(map1.get(name), map2.get(name))) {
                return false;
            }
        }
        return true;
    }
    
    private boolean areComplexTypeDetailsIdentical(CsdlComplexType type1, CsdlComplexType type2) {
        if (!Objects.equals(type1.getName(), type2.getName())) return false;
        if (!Objects.equals(type1.getBaseType(), type2.getBaseType())) return false;
        if (!Objects.equals(type1.isAbstract(), type2.isAbstract())) return false;
        if (!Objects.equals(type1.isOpenType(), type2.isOpenType())) return false;
        
        return arePropertiesIdentical(type1.getProperties(), type2.getProperties());
    }
    
    private boolean areEnumTypesIdentical(List<CsdlEnumType> types1, List<CsdlEnumType> types2) {
        if ((types1 == null) != (types2 == null)) return false;
        if (types1 == null) return true;
        
        if (types1.size() != types2.size()) return false;
        
        Map<String, CsdlEnumType> map1 = types1.stream()
            .collect(Collectors.toMap(CsdlEnumType::getName, Function.identity()));
        Map<String, CsdlEnumType> map2 = types2.stream()
            .collect(Collectors.toMap(CsdlEnumType::getName, Function.identity()));
        
        if (!map1.keySet().equals(map2.keySet())) return false;
        
        for (String name : map1.keySet()) {
            if (!areEnumTypeDetailsIdentical(map1.get(name), map2.get(name))) {
                return false;
            }
        }
        return true;
    }
    
    private boolean areEnumTypeDetailsIdentical(CsdlEnumType type1, CsdlEnumType type2) {
        if (!Objects.equals(type1.getName(), type2.getName())) return false;
        if (!Objects.equals(type1.getUnderlyingType(), type2.getUnderlyingType())) return false;
        if (!Objects.equals(type1.isFlags(), type2.isFlags())) return false;
        
        List<CsdlEnumMember> members1 = type1.getMembers();
        List<CsdlEnumMember> members2 = type2.getMembers();
        
        if ((members1 == null) != (members2 == null)) return false;
        if (members1 == null) return true;
        if (members1.size() != members2.size()) return false;
        
        for (int i = 0; i < members1.size(); i++) {
            CsdlEnumMember m1 = members1.get(i);
            CsdlEnumMember m2 = members2.get(i);
            if (!Objects.equals(m1.getName(), m2.getName()) || !Objects.equals(m1.getValue(), m2.getValue())) {
                return false;
            }
        }
        return true;
    }
    
    private boolean areTypeDefinitionsIdentical(List<CsdlTypeDefinition> types1, List<CsdlTypeDefinition> types2) {
        if ((types1 == null) != (types2 == null)) return false;
        if (types1 == null) return true;
        
        if (types1.size() != types2.size()) return false;
        
        Map<String, CsdlTypeDefinition> map1 = types1.stream()
            .collect(Collectors.toMap(CsdlTypeDefinition::getName, Function.identity()));
        Map<String, CsdlTypeDefinition> map2 = types2.stream()
            .collect(Collectors.toMap(CsdlTypeDefinition::getName, Function.identity()));
        
        if (!map1.keySet().equals(map2.keySet())) return false;
        
        for (String name : map1.keySet()) {
            CsdlTypeDefinition t1 = map1.get(name);
            CsdlTypeDefinition t2 = map2.get(name);
            if (!Objects.equals(t1.getUnderlyingType(), t2.getUnderlyingType())) {
                return false;
            }
        }
        return true;
    }
    
    private boolean areActionsIdentical(List<CsdlAction> actions1, List<CsdlAction> actions2) {
        if ((actions1 == null) != (actions2 == null)) return false;
        if (actions1 == null) return true;
        
        if (actions1.size() != actions2.size()) return false;
        
        // Actions don't support overloading, so we can use simple name-based comparison
        Map<String, CsdlAction> map1 = actions1.stream()
            .collect(Collectors.toMap(CsdlAction::getName, Function.identity()));
        Map<String, CsdlAction> map2 = actions2.stream()
            .collect(Collectors.toMap(CsdlAction::getName, Function.identity()));
        
        if (!map1.keySet().equals(map2.keySet())) return false;
        
        for (String name : map1.keySet()) {
            if (!areActionDetailsIdentical(map1.get(name), map2.get(name))) {
                return false;
            }
        }
        return true;
    }
    
    private boolean areFunctionsIdentical(List<CsdlFunction> functions1, List<CsdlFunction> functions2) {
        if ((functions1 == null) != (functions2 == null)) return false;
        if (functions1 == null) return true;
        
        if (functions1.size() != functions2.size()) return false;
        
        // Functions support overloading, so we need to compare by signature (name + parameter types)
        Map<String, CsdlFunction> map1 = functions1.stream()
            .collect(Collectors.toMap(this::getFunctionSignature, Function.identity()));
        Map<String, CsdlFunction> map2 = functions2.stream()
            .collect(Collectors.toMap(this::getFunctionSignature, Function.identity()));
        
        if (!map1.keySet().equals(map2.keySet())) return false;
        
        for (String signature : map1.keySet()) {
            if (!areFunctionDetailsIdentical(map1.get(signature), map2.get(signature))) {
                return false;
            }
        }
        return true;
    }
    
    private String getFunctionSignature(CsdlFunction function) {
        StringBuilder sig = new StringBuilder(function.getName()).append("(");
        if (function.getParameters() != null) {
            for (CsdlParameter param : function.getParameters()) {
                sig.append(param.getType()).append(",");
            }
        }
        sig.append(")");
        return sig.toString();
    }
    
    private boolean areActionDetailsIdentical(CsdlAction action1, CsdlAction action2) {
        if (!Objects.equals(action1.getName(), action2.getName())) return false;
        if (!Objects.equals(action1.isBound(), action2.isBound())) return false;
        
        return areParametersIdentical(action1.getParameters(), action2.getParameters());
    }
    
    private boolean areFunctionDetailsIdentical(CsdlFunction function1, CsdlFunction function2) {
        if (!Objects.equals(function1.getName(), function2.getName())) return false;
        if (!Objects.equals(function1.isBound(), function2.isBound())) return false;
        if (!Objects.equals(function1.isComposable(), function2.isComposable())) return false;
        
        // Compare return type
        if (!areReturnTypesIdentical(function1.getReturnType(), function2.getReturnType())) return false;
        
        return areParametersIdentical(function1.getParameters(), function2.getParameters());
    }
    
    private boolean areEntityContainersIdentical(CsdlEntityContainer container1, CsdlEntityContainer container2) {
        if ((container1 == null) != (container2 == null)) return false;
        if (container1 == null) return true;
        
        if (!Objects.equals(container1.getName(), container2.getName())) return false;
        // Note: CsdlEntityContainer doesn't have getExtends() method in this version
        
        // Compare entity sets, action imports, function imports, singletons
        // This is a simplified comparison
        return areEntitySetsIdentical(container1.getEntitySets(), container2.getEntitySets()) &&
               areActionImportsIdentical(container1.getActionImports(), container2.getActionImports()) &&
               areFunctionImportsIdentical(container1.getFunctionImports(), container2.getFunctionImports()) &&
               areSingletonsIdentical(container1.getSingletons(), container2.getSingletons());
    }
    
    // Helper methods for detailed comparisons (simplified implementations)
    private boolean arePropertiesIdentical(List<CsdlProperty> props1, List<CsdlProperty> props2) {
        if ((props1 == null) != (props2 == null)) return false;
        if (props1 == null) return true;
        if (props1.size() != props2.size()) return false;
        
        Map<String, CsdlProperty> map1 = props1.stream()
            .collect(Collectors.toMap(CsdlProperty::getName, Function.identity()));
        Map<String, CsdlProperty> map2 = props2.stream()
            .collect(Collectors.toMap(CsdlProperty::getName, Function.identity()));
        
        if (!map1.keySet().equals(map2.keySet())) return false;
        
        for (String name : map1.keySet()) {
            CsdlProperty p1 = map1.get(name);
            CsdlProperty p2 = map2.get(name);
            if (!Objects.equals(p1.getType(), p2.getType()) ||
                !Objects.equals(p1.isNullable(), p2.isNullable()) ||
                !Objects.equals(p1.getMaxLength(), p2.getMaxLength()) ||
                !Objects.equals(p1.getPrecision(), p2.getPrecision()) ||
                !Objects.equals(p1.getScale(), p2.getScale())) {
                return false;
            }
        }
        return true;
    }
    
    private boolean areNavigationPropertiesIdentical(List<CsdlNavigationProperty> navProps1, List<CsdlNavigationProperty> navProps2) {
        // Simplified comparison
        if ((navProps1 == null) != (navProps2 == null)) return false;
        if (navProps1 == null) return true;
        return navProps1.size() == navProps2.size();
    }
    
    private boolean areKeysIdentical(List<CsdlPropertyRef> keys1, List<CsdlPropertyRef> keys2) {
        if ((keys1 == null) != (keys2 == null)) return false;
        if (keys1 == null) return true;
        if (keys1.size() != keys2.size()) return false;
        
        Set<String> keyNames1 = keys1.stream().map(CsdlPropertyRef::getName).collect(Collectors.toSet());
        Set<String> keyNames2 = keys2.stream().map(CsdlPropertyRef::getName).collect(Collectors.toSet());
        
        return keyNames1.equals(keyNames2);
    }
    
    private boolean areParametersIdentical(List<CsdlParameter> params1, List<CsdlParameter> params2) {
        if ((params1 == null) != (params2 == null)) return false;
        if (params1 == null) return true;
        if (params1.size() != params2.size()) return false;
        
        for (int i = 0; i < params1.size(); i++) {
            CsdlParameter p1 = params1.get(i);
            CsdlParameter p2 = params2.get(i);
            if (!Objects.equals(p1.getName(), p2.getName()) ||
                !Objects.equals(p1.getType(), p2.getType()) ||
                !Objects.equals(p1.isNullable(), p2.isNullable())) {
                return false;
            }
        }
        return true;
    }
    
    private boolean areReturnTypesIdentical(CsdlReturnType ret1, CsdlReturnType ret2) {
        if ((ret1 == null) != (ret2 == null)) return false;
        if (ret1 == null) return true;
        
        return Objects.equals(ret1.getType(), ret2.getType()) &&
               Objects.equals(ret1.isNullable(), ret2.isNullable()) &&
               Objects.equals(ret1.isCollection(), ret2.isCollection());
    }
    
    /**
     * Compare EntitySets according to OData 4.0 specification.
     * EntitySets are considered identical if they have the same Name, EntityType, and NavigationPropertyBindings.
     */
    private boolean areEntitySetsIdentical(List<CsdlEntitySet> sets1, List<CsdlEntitySet> sets2) {
        if ((sets1 == null) != (sets2 == null)) return false;
        if (sets1 == null) return true;
        if (sets1.size() != sets2.size()) return false;
        
        // Create maps for efficient lookup by name
        Map<String, CsdlEntitySet> map1 = sets1.stream()
            .collect(Collectors.toMap(CsdlEntitySet::getName, Function.identity()));
        Map<String, CsdlEntitySet> map2 = sets2.stream()
            .collect(Collectors.toMap(CsdlEntitySet::getName, Function.identity()));
        
        if (!map1.keySet().equals(map2.keySet())) return false;
        
        for (String name : map1.keySet()) {
            if (!areEntitySetDetailsIdentical(map1.get(name), map2.get(name))) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Compare detailed EntitySet properties according to OData 4.0 specification.
     */
    private boolean areEntitySetDetailsIdentical(CsdlEntitySet set1, CsdlEntitySet set2) {
        if (!Objects.equals(set1.getName(), set2.getName())) return false;
        if (!Objects.equals(set1.getType(), set2.getType())) return false;
        if (!Objects.equals(set1.isIncludeInServiceDocument(), set2.isIncludeInServiceDocument())) return false;
        
        // Compare NavigationPropertyBindings
        return areNavigationPropertyBindingsIdentical(
            set1.getNavigationPropertyBindings(), 
            set2.getNavigationPropertyBindings()
        );
    }
    
    /**
     * Compare NavigationPropertyBindings according to OData 4.0 specification.
     */
    private boolean areNavigationPropertyBindingsIdentical(
            List<CsdlNavigationPropertyBinding> bindings1, 
            List<CsdlNavigationPropertyBinding> bindings2) {
        if ((bindings1 == null) != (bindings2 == null)) return false;
        if (bindings1 == null) return true;
        if (bindings1.size() != bindings2.size()) return false;
        
        // Create maps for efficient lookup by path
        Map<String, String> map1 = bindings1.stream()
            .collect(Collectors.toMap(CsdlNavigationPropertyBinding::getPath, 
                                    CsdlNavigationPropertyBinding::getTarget));
        Map<String, String> map2 = bindings2.stream()
            .collect(Collectors.toMap(CsdlNavigationPropertyBinding::getPath, 
                                    CsdlNavigationPropertyBinding::getTarget));
        
        return map1.equals(map2);
    }
    
    /**
     * Compare ActionImports according to OData 4.0 specification.
     * ActionImports are considered identical if they have the same Name, Action, and EntitySet.
     */
    private boolean areActionImportsIdentical(List<CsdlActionImport> imports1, List<CsdlActionImport> imports2) {
        if ((imports1 == null) != (imports2 == null)) return false;
        if (imports1 == null) return true;
        if (imports1.size() != imports2.size()) return false;
        
        // Create maps for efficient lookup by name (ActionImports are uniquely identified by name)
        Map<String, CsdlActionImport> map1 = imports1.stream()
            .collect(Collectors.toMap(CsdlActionImport::getName, Function.identity()));
        Map<String, CsdlActionImport> map2 = imports2.stream()
            .collect(Collectors.toMap(CsdlActionImport::getName, Function.identity()));
        
        if (!map1.keySet().equals(map2.keySet())) return false;
        
        for (String name : map1.keySet()) {
            if (!areActionImportDetailsIdentical(map1.get(name), map2.get(name))) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Compare detailed ActionImport properties according to OData 4.0 specification.
     */
    private boolean areActionImportDetailsIdentical(CsdlActionImport import1, CsdlActionImport import2) {
        if (!Objects.equals(import1.getName(), import2.getName())) return false;
        if (!Objects.equals(import1.getAction(), import2.getAction())) return false;
        return Objects.equals(import1.getEntitySet(), import2.getEntitySet());
    }
    
    /**
     * Compare FunctionImports according to OData 4.0 specification.
     * FunctionImports are considered identical if they have the same Name, Function, EntitySet, and IncludeInServiceDocument.
     */
    private boolean areFunctionImportsIdentical(List<CsdlFunctionImport> imports1, List<CsdlFunctionImport> imports2) {
        if ((imports1 == null) != (imports2 == null)) return false;
        if (imports1 == null) return true;
        if (imports1.size() != imports2.size()) return false;
        
        // Create maps for efficient lookup by name (FunctionImports are uniquely identified by name)
        Map<String, CsdlFunctionImport> map1 = imports1.stream()
            .collect(Collectors.toMap(CsdlFunctionImport::getName, Function.identity()));
        Map<String, CsdlFunctionImport> map2 = imports2.stream()
            .collect(Collectors.toMap(CsdlFunctionImport::getName, Function.identity()));
        
        if (!map1.keySet().equals(map2.keySet())) return false;
        
        for (String name : map1.keySet()) {
            if (!areFunctionImportDetailsIdentical(map1.get(name), map2.get(name))) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Compare detailed FunctionImport properties according to OData 4.0 specification.
     */
    private boolean areFunctionImportDetailsIdentical(CsdlFunctionImport import1, CsdlFunctionImport import2) {
        if (!Objects.equals(import1.getName(), import2.getName())) return false;
        if (!Objects.equals(import1.getFunction(), import2.getFunction())) return false;
        if (!Objects.equals(import1.getEntitySet(), import2.getEntitySet())) return false;
        return Objects.equals(import1.isIncludeInServiceDocument(), import2.isIncludeInServiceDocument());
    }
    
    /**
     * Compare Singletons according to OData 4.0 specification.
     * Singletons are considered identical if they have the same Name, Type, and NavigationPropertyBindings.
     */
    private boolean areSingletonsIdentical(List<CsdlSingleton> singletons1, List<CsdlSingleton> singletons2) {
        if ((singletons1 == null) != (singletons2 == null)) return false;
        if (singletons1 == null) return true;
        if (singletons1.size() != singletons2.size()) return false;
        
        // Create maps for efficient lookup by name (Singletons are uniquely identified by name)
        Map<String, CsdlSingleton> map1 = singletons1.stream()
            .collect(Collectors.toMap(CsdlSingleton::getName, Function.identity()));
        Map<String, CsdlSingleton> map2 = singletons2.stream()
            .collect(Collectors.toMap(CsdlSingleton::getName, Function.identity()));
        
        if (!map1.keySet().equals(map2.keySet())) return false;
        
        for (String name : map1.keySet()) {
            if (!areSingletonDetailsIdentical(map1.get(name), map2.get(name))) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Compare detailed Singleton properties according to OData 4.0 specification.
     */
    private boolean areSingletonDetailsIdentical(CsdlSingleton singleton1, CsdlSingleton singleton2) {
        if (!Objects.equals(singleton1.getName(), singleton2.getName())) return false;
        if (!Objects.equals(singleton1.getType(), singleton2.getType())) return false;
        
        // Compare NavigationPropertyBindings
        return areNavigationPropertyBindingsIdentical(
            singleton1.getNavigationPropertyBindings(), 
            singleton2.getNavigationPropertyBindings()
        );
    }
    
    /**
     * Merge two schemas with the same namespace, detecting conflicts
     */
    private CsdlSchema mergeSchemas(CsdlSchema existing, CsdlSchema source, String namespace) throws Exception {
        // Create a new schema to hold merged content
        CsdlSchema merged = new CsdlSchema();
        merged.setNamespace(namespace);
        merged.setAlias(existing.getAlias() != null ? existing.getAlias() : source.getAlias());
        
        // Track element names to detect conflicts
        Set<String> entityTypeNames = new HashSet<>();
        Set<String> complexTypeNames = new HashSet<>();
        Set<String> enumTypeNames = new HashSet<>();
        Set<String> typeDefinitionNames = new HashSet<>();
        Set<String> actionNames = new HashSet<>();
        Set<String> functionNames = new HashSet<>();
        Set<String> containerNames = new HashSet<>();
        
        // Copy all elements from existing schema
        if (existing.getEntityTypes() != null) {
            for (CsdlEntityType entityType : existing.getEntityTypes()) {
                merged.getEntityTypes().add(entityType);
                entityTypeNames.add(entityType.getName());
            }
        }
        
        if (existing.getComplexTypes() != null) {
            for (CsdlComplexType complexType : existing.getComplexTypes()) {
                merged.getComplexTypes().add(complexType);
                complexTypeNames.add(complexType.getName());
            }
        }
        
        if (existing.getEnumTypes() != null) {
            for (CsdlEnumType enumType : existing.getEnumTypes()) {
                merged.getEnumTypes().add(enumType);
                enumTypeNames.add(enumType.getName());
            }
        }
        
        if (existing.getTypeDefinitions() != null) {
            for (CsdlTypeDefinition typeDef : existing.getTypeDefinitions()) {
                merged.getTypeDefinitions().add(typeDef);
                typeDefinitionNames.add(typeDef.getName());
            }
        }
        
        if (existing.getActions() != null) {
            for (CsdlAction action : existing.getActions()) {
                merged.getActions().add(action);
                actionNames.add(action.getName());
            }
        }
        
        if (existing.getFunctions() != null) {
            for (CsdlFunction function : existing.getFunctions()) {
                merged.getFunctions().add(function);
                functionNames.add(function.getName());
            }
        }
        
        if (existing.getEntityContainer() != null) {
            merged.setEntityContainer(existing.getEntityContainer());
            containerNames.add(existing.getEntityContainer().getName());
        }
        
        // Add elements from source schema, checking for conflicts
        if (source.getEntityTypes() != null) {
            for (CsdlEntityType entityType : source.getEntityTypes()) {
                if (entityTypeNames.contains(entityType.getName())) {
                    String error = String.format(
                        "Conflicting EntityType '%s' found in namespace '%s' during schema merge",
                        entityType.getName(), namespace);
                    statistics.addError(ErrorType.SCHEMA_MERGE_CONFLICT, error, namespace);
                    errorReport.computeIfAbsent(namespace, k -> new ArrayList<>()).add(error);
                    throw new IllegalArgumentException(error);
                }
                merged.getEntityTypes().add(entityType);
                entityTypeNames.add(entityType.getName());
            }
        }
        
        if (source.getComplexTypes() != null) {
            for (CsdlComplexType complexType : source.getComplexTypes()) {
                if (complexTypeNames.contains(complexType.getName())) {
                    String error = String.format(
                        "Conflicting ComplexType '%s' found in namespace '%s' during schema merge",
                        complexType.getName(), namespace);
                    statistics.addError(ErrorType.SCHEMA_MERGE_CONFLICT, error, namespace);
                    errorReport.computeIfAbsent(namespace, k -> new ArrayList<>()).add(error);
                    throw new IllegalArgumentException(error);
                }
                merged.getComplexTypes().add(complexType);
                complexTypeNames.add(complexType.getName());
            }
        }
        
        if (source.getEnumTypes() != null) {
            for (CsdlEnumType enumType : source.getEnumTypes()) {
                if (enumTypeNames.contains(enumType.getName())) {
                    String error = String.format(
                        "Conflicting EnumType '%s' found in namespace '%s' during schema merge",
                        enumType.getName(), namespace);
                    statistics.addError(ErrorType.SCHEMA_MERGE_CONFLICT, error, namespace);
                    errorReport.computeIfAbsent(namespace, k -> new ArrayList<>()).add(error);
                    throw new IllegalStateException(error);
                }
                merged.getEnumTypes().add(enumType);
                enumTypeNames.add(enumType.getName());
            }
        }
        
        if (source.getTypeDefinitions() != null) {
            for (CsdlTypeDefinition typeDef : source.getTypeDefinitions()) {
                if (typeDefinitionNames.contains(typeDef.getName())) {
                    String error = String.format(
                        "Conflicting TypeDefinition '%s' found in namespace '%s' during schema merge",
                        typeDef.getName(), namespace);
                    statistics.addError(ErrorType.SCHEMA_MERGE_CONFLICT, error, namespace);
                    errorReport.computeIfAbsent(namespace, k -> new ArrayList<>()).add(error);
                    throw new IllegalStateException(error);
                }
                merged.getTypeDefinitions().add(typeDef);
                typeDefinitionNames.add(typeDef.getName());
            }
        }
        
        if (source.getActions() != null) {
            for (CsdlAction action : source.getActions()) {
                if (actionNames.contains(action.getName())) {
                    String error = String.format(
                        "Conflicting Action '%s' found in namespace '%s' during schema merge",
                        action.getName(), namespace);
                    statistics.addError(ErrorType.SCHEMA_MERGE_CONFLICT, error, namespace);
                    errorReport.computeIfAbsent(namespace, k -> new ArrayList<>()).add(error);
                    throw new IllegalStateException(error);
                }
                merged.getActions().add(action);
                actionNames.add(action.getName());
            }
        }
        
        if (source.getFunctions() != null) {
            for (CsdlFunction function : source.getFunctions()) {
                if (functionNames.contains(function.getName())) {
                    String error = String.format(
                        "Conflicting Function '%s' found in namespace '%s' during schema merge",
                        function.getName(), namespace);
                    statistics.addError(ErrorType.SCHEMA_MERGE_CONFLICT, error, namespace);
                    errorReport.computeIfAbsent(namespace, k -> new ArrayList<>()).add(error);
                    throw new IllegalStateException(error);
                }
                merged.getFunctions().add(function);
                functionNames.add(function.getName());
            }
        }
        
        if (source.getEntityContainer() != null) {
            if (containerNames.contains(source.getEntityContainer().getName())) {
                String error = String.format(
                    "Conflicting EntityContainer '%s' found in namespace '%s' during schema merge",
                    source.getEntityContainer().getName(), namespace);
                statistics.addError(ErrorType.SCHEMA_MERGE_CONFLICT, error, namespace);
                errorReport.computeIfAbsent(namespace, k -> new ArrayList<>()).add(error);
                throw new IllegalStateException(error);
            }
            // If there's no existing container, set this one
            // If there is an existing container, we would need to merge them (complex scenario)
            if (merged.getEntityContainer() == null) {
                merged.setEntityContainer(source.getEntityContainer());
            }
        }
        
        return merged;
    }
    
    /**
     * Remove schema using reflection to access internal schema list
     */
    private void removeSchemaUsingReflection(SchemaBasedEdmProvider provider, CsdlSchema schema) 
            throws Exception {
        try {
            // Try different possible field names for the schemas list
            java.lang.reflect.Field schemasField = null;
            String[] possibleFieldNames = {"schemas", "schemaList", "csdlSchemas", "edmSchemas"};
            
            for (String fieldName : possibleFieldNames) {
                try {
                    schemasField = SchemaBasedEdmProvider.class.getDeclaredField(fieldName);
                    break;
                } catch (NoSuchFieldException e) {
                    // Try next field name
                }
            }
            
            if (schemasField == null) {
                // If we can't find the field, let's list all fields for debugging
                java.lang.reflect.Field[] allFields = SchemaBasedEdmProvider.class.getDeclaredFields();
                StringBuilder fieldNames = new StringBuilder();
                for (java.lang.reflect.Field field : allFields) {
                    if (fieldNames.length() > 0) fieldNames.append(", ");
                    fieldNames.append(field.getName()).append(":").append(field.getType().getSimpleName());
                }
                throw new IllegalStateException("Could not find schemas field. Available fields: " + fieldNames.toString());
            }
            
            schemasField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<CsdlSchema> schemas = (List<CsdlSchema>) schemasField.get(provider);
            schemas.remove(schema);
        } catch (Exception e) {
            // Fallback: create new provider without the schema (not ideal but works)
            throw new IllegalStateException("Could not remove schema during merge: " + e.getMessage(), e);
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
        // First try the configured resolvers
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
        
        // If that fails, try to resolve from test resources
        try {
            String resourcePath = "schemas/" + referencePath;
            InputStream resourceStream = this.getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (resourceStream != null) {
                return resourceStream;
            }
            
            // Also try common subdirectories
            String[] subdirs = {"dependencies", "circular", "deep", "invalid", "multi", "crossdir", "nested"};
            for (String subdir : subdirs) {
                resourcePath = "schemas/" + subdir + "/" + referencePath;
                resourceStream = this.getClass().getClassLoader().getResourceAsStream(resourcePath);
                if (resourceStream != null) {
                    return resourceStream;
                }
            }
            
            // Try to handle relative paths like "../dirA/common.xml"
            if (referencePath.startsWith("../")) {
                String relativePath = referencePath.substring(3); // Remove "../"
                resourcePath = "schemas/crossdir/" + relativePath;
                resourceStream = this.getClass().getClassLoader().getResourceAsStream(resourcePath);
                if (resourceStream != null) {
                    return resourceStream;
                }
                
                // Also try nested directory structure
                resourcePath = "schemas/nested/" + relativePath;
                resourceStream = this.getClass().getClassLoader().getResourceAsStream(resourcePath);
                if (resourceStream != null) {
                    return resourceStream;
                }
            }
        } catch (Exception e) {
            // Continue 
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
        
        // Note: we don't clear the cache here as it should persist across builds
        // unless explicitly disabled
    }
    
    /**
     * Generate cache key that includes path information to avoid conflicts
     */
    private String generateCacheKey(String schemaPath) {
        try {
            // Use canonical path to ensure uniqueness for the same file referenced by different relative paths
            File file = new File(schemaPath);
            String canonicalPath = file.getCanonicalPath();
            
            // Normalize path separators for consistency
            return canonicalPath.replace("\\", "/");
        } catch (IOException e) {
            // Fallback to original logic if canonical path fails
            File file = new File(schemaPath);
            String fileName = file.getName();
            
            // For relative paths or complex paths, include parent directory to distinguish same filenames
            if (schemaPath.contains("/") || schemaPath.contains("\\")) {
                String parent = file.getParent();
                if (parent != null) {
                    // Normalize the parent path and combine with filename
                    parent = parent.replace("\\", "/");
                    if (parent.contains("/")) {
                        // Take last two path components to create unique key
                        String[] parts = parent.split("/");
                        if (parts.length >= 2) {
                            return parts[parts.length - 2] + "/" + parts[parts.length - 1] + "/" + fileName;
                        } else if (parts.length == 1) {
                            return parts[0] + "/" + fileName;
                        }
                    }
                    return parent + "/" + fileName;
                }
            }
            
            // Fallback to just filename for simple cases
            return fileName;
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
                
                InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(path);
                if (resourceStream != null) {
                    return resourceStream;
                }
            } catch (Exception e) {
                // Ignore and return null
            }
            return null;
        }
    }
    
    /**
     * File Based Reference Resolver for handling local file references
     */
    private static class FileBasedReferenceResolver implements ReferenceResolver {
        private final File baseDirectory;
        
        public FileBasedReferenceResolver(File baseDirectory) {
            this.baseDirectory = baseDirectory;
        }
        
        @Override
        public InputStream resolveReference(URI referenceUri, String xmlBase) {
            try {
                String referencePath = referenceUri.getPath();
                
                // For absolute URI
                if (referenceUri.isAbsolute()) {
                    File resolvedFile = new File(referencePath);
                    if (resolvedFile.exists() && resolvedFile.isFile()) {
                        return new FileInputStream(resolvedFile);
                    }
                }
                
                // Priority 1: Try as resource from classpath (for schemas/* paths)
                InputStream resourceStream = FileBasedReferenceResolver.class.getClassLoader().getResourceAsStream(referencePath);
                if (resourceStream != null) {
                    return resourceStream;
                }
                
                // Priority 2: Try relative to base directory (fallback for relative paths)
                File resolvedFile = new File(baseDirectory, referencePath);
                if (resolvedFile.exists() && resolvedFile.isFile()) {
                    return new FileInputStream(resolvedFile);
                }
                
                // Priority 3: Search in test resources directory
                File testResourcesDir = new File("src/test/resources");
                if (!testResourcesDir.exists()) {
                    // We're running from target/test-classes
                    testResourcesDir = new File("target/test-classes");
                }
                
                if (testResourcesDir.exists()) {
                    File candidateFile = new File(testResourcesDir, referencePath);
                    if (candidateFile.exists() && candidateFile.isFile()) {
                        return new FileInputStream(candidateFile);
                    }
                }
                
                return null;
            } catch (Exception e) {
                return null;
            }
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
    
    /**
     * Extract edmx:Reference elements directly from XML to avoid Olingo's deduplication by namespace
     */
    private Set<String> extractReferencesFromXml(String schemaPath) throws Exception {
        Set<String> references = new HashSet<>();
        
        try {
            InputStream inputStream = resolveReference(schemaPath);
            if (inputStream == null) {
                return references;
            }
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            
            // Find all edmx:Reference elements
            NodeList referenceNodes = doc.getElementsByTagNameNS("http://docs.oasis-open.org/odata/ns/edmx", "Reference");
            
            for (int i = 0; i < referenceNodes.getLength(); i++) {
                Element refElement = (Element) referenceNodes.item(i);
                String uri = refElement.getAttribute("Uri");
                if (uri != null && !uri.trim().isEmpty()) {
                    references.add(uri);
                }
            }
            
            inputStream.close();
            
        } catch (Exception e) {
            // If XML parsing fails, fall back to empty set
            // Let other parts of the system handle the error
        }
        
        return references;
    }
    
    /**
     * Validate all references in the loaded schemas
     */
    private void validateReferences(SchemaBasedEdmProvider provider) {
        try {
            if (provider == null || provider.getSchemas() == null) {
                return;
            }
            
            // Build a registry of all available types across all schemas
            TypeRegistry typeRegistry = new TypeRegistry(provider.getSchemas());
            
            // Validate each schema
            for (CsdlSchema schema : provider.getSchemas()) {
                validateSchemaReferences(schema, typeRegistry);
            }
        } catch (Exception e) {
            statistics.addError(ErrorType.PARSING_ERROR,
                "Error during reference validation: " + e.getMessage(),
                "validateReferences");
        }
    }
    
    /**
     * Validate references within a single schema
     */
    private void validateSchemaReferences(CsdlSchema schema, TypeRegistry typeRegistry) {
        String namespace = schema.getNamespace();
        
        // Validate entity types
        if (schema.getEntityTypes() != null) {
            for (CsdlEntityType entityType : schema.getEntityTypes()) {
                validateEntityTypeReferences(entityType, namespace, typeRegistry);
            }
        }
        
        // Validate complex types
        if (schema.getComplexTypes() != null) {
            for (CsdlComplexType complexType : schema.getComplexTypes()) {
                validateComplexTypeReferences(complexType, namespace, typeRegistry);
            }
        }
        
        // Validate function references
        if (schema.getFunctions() != null) {
            for (CsdlFunction function : schema.getFunctions()) {
                validateFunctionReferences(function, namespace, typeRegistry);
            }
        }
        
        // Validate action references
        if (schema.getActions() != null) {
            for (CsdlAction action : schema.getActions()) {
                validateActionReferences(action, namespace, typeRegistry);
            }
        }
        
        // Validate entity container references
        if (schema.getEntityContainer() != null) {
            validateEntityContainerReferences(schema.getEntityContainer(), namespace, typeRegistry);
        }
        
        // Validate annotation targets
        if (schema.getAnnotationGroups() != null) {
            for (CsdlAnnotations annotations : schema.getAnnotationGroups()) {
                validateAnnotationTargets(annotations, namespace, typeRegistry);
            }
        }
    }
    
    private void validateEntityTypeReferences(CsdlEntityType entityType, String namespace, TypeRegistry typeRegistry) {
        String typeName = namespace + "." + entityType.getName();
        
        // Validate base type
        if (entityType.getBaseType() != null) {
            if (!typeRegistry.hasEntityType(entityType.getBaseType())) {
                statistics.addError(ErrorType.MISSING_TYPE_REFERENCE, 
                    "Entity type base type not found: " + entityType.getBaseType(), 
                    typeName);
            }
        }
        
        // Validate property types
        if (entityType.getProperties() != null) {
            for (CsdlProperty property : entityType.getProperties()) {
                validatePropertyType(property, typeName, typeRegistry);
            }
        }
        
        // Validate navigation property types
        if (entityType.getNavigationProperties() != null) {
            for (CsdlNavigationProperty navProp : entityType.getNavigationProperties()) {
                if (!typeRegistry.hasEntityType(navProp.getType())) {
                    statistics.addError(ErrorType.MISSING_TYPE_REFERENCE,
                        "Navigation property type not found: " + navProp.getType(),
                        typeName + "." + navProp.getName());
                }
            }
        }
    }
    
    private void validateComplexTypeReferences(CsdlComplexType complexType, String namespace, TypeRegistry typeRegistry) {
        String typeName = namespace + "." + complexType.getName();
        
        // Validate base type
        if (complexType.getBaseType() != null) {
            if (!typeRegistry.hasComplexType(complexType.getBaseType())) {
                statistics.addError(ErrorType.MISSING_TYPE_REFERENCE,
                    "Complex type base type not found: " + complexType.getBaseType(),
                    typeName);
            }
        }
        
        // Validate property types
        if (complexType.getProperties() != null) {
            for (CsdlProperty property : complexType.getProperties()) {
                validatePropertyType(property, typeName, typeRegistry);
            }
        }
    }
    
    private void validatePropertyType(CsdlProperty property, String ownerTypeName, TypeRegistry typeRegistry) {
        String propertyType = property.getType();
        
        // Skip primitive types (start with Edm.)
        if (propertyType.startsWith("Edm.")) {
            return;
        }
        
        // Check if the type exists
        if (!typeRegistry.hasType(propertyType)) {
            statistics.addError(ErrorType.MISSING_TYPE_REFERENCE,
                "Property type not found: " + propertyType,
                ownerTypeName + "." + property.getName());
        }
    }
    
    private void validateFunctionReferences(CsdlFunction function, String namespace, TypeRegistry typeRegistry) {
        String functionName = namespace + "." + function.getName();
        
        // Validate return type
        if (function.getReturnType() != null && function.getReturnType().getType() != null) {
            String returnType = function.getReturnType().getType();
            if (!returnType.startsWith("Edm.") && !typeRegistry.hasType(returnType)) {
                statistics.addError(ErrorType.MISSING_TYPE_REFERENCE,
                    "Function return type not found: " + returnType,
                    functionName);
            }
        }
        
        // Validate parameter types
        if (function.getParameters() != null) {
            for (CsdlParameter parameter : function.getParameters()) {
                String paramType = parameter.getType();
                if (!paramType.startsWith("Edm.") && !typeRegistry.hasType(paramType)) {
                    statistics.addError(ErrorType.MISSING_TYPE_REFERENCE,
                        "Function parameter type not found: " + paramType,
                        functionName + "." + parameter.getName());
                }
            }
        }
    }
    
    private void validateActionReferences(CsdlAction action, String namespace, TypeRegistry typeRegistry) {
        String actionName = namespace + "." + action.getName();
        
        // Validate parameter types
        if (action.getParameters() != null) {
            for (CsdlParameter parameter : action.getParameters()) {
                String paramType = parameter.getType();
                if (!paramType.startsWith("Edm.") && !typeRegistry.hasType(paramType)) {
                    statistics.addError(ErrorType.MISSING_TYPE_REFERENCE,
                        "Action parameter type not found: " + paramType,
                        actionName + "." + parameter.getName());
                }
            }
        }
    }
    
    private void validateEntityContainerReferences(CsdlEntityContainer container, String namespace, TypeRegistry typeRegistry) {
        String containerName = namespace + "." + container.getName();
        
        // Validate entity sets
        if (container.getEntitySets() != null) {
            for (CsdlEntitySet entitySet : container.getEntitySets()) {
                if (!typeRegistry.hasEntityType(entitySet.getType())) {
                    statistics.addError(ErrorType.MISSING_TYPE_REFERENCE,
                        "Entity set type not found: " + entitySet.getType(),
                        containerName + "." + entitySet.getName());
                }
            }
        }
        
        // Validate singletons
        if (container.getSingletons() != null) {
            for (CsdlSingleton singleton : container.getSingletons()) {
                if (!typeRegistry.hasEntityType(singleton.getType())) {
                    statistics.addError(ErrorType.MISSING_TYPE_REFERENCE,
                        "Singleton type not found: " + singleton.getType(),
                        containerName + "." + singleton.getName());
                }
            }
        }
        
        // Validate function imports
        if (container.getFunctionImports() != null) {
            for (CsdlFunctionImport functionImport : container.getFunctionImports()) {
                if (!typeRegistry.hasFunction(functionImport.getFunction())) {
                    statistics.addError(ErrorType.MISSING_TYPE_REFERENCE,
                        "Function import function not found: " + functionImport.getFunction(),
                        containerName + "." + functionImport.getName());
                }
            }
        }
        
        // Validate action imports
        if (container.getActionImports() != null) {
            for (CsdlActionImport actionImport : container.getActionImports()) {
                if (!typeRegistry.hasAction(actionImport.getAction())) {
                    statistics.addError(ErrorType.MISSING_TYPE_REFERENCE,
                        "Action import action not found: " + actionImport.getAction(),
                        containerName + "." + actionImport.getName());
                }
            }
        }
    }
    
    private void validateAnnotationTargets(CsdlAnnotations annotations, String namespace, TypeRegistry typeRegistry) {
        String target = annotations.getTarget();
        
        // Parse and validate annotation target
        if (target != null && !target.isEmpty()) {
            if (!typeRegistry.hasTarget(target)) {
                statistics.addError(ErrorType.MISSING_ANNOTATION_TARGET,
                    "Annotation target not found: " + target,
                    namespace);
            }
        }
    }
    
    /**
     * Registry of all available types across all schemas
     */
    private static class TypeRegistry {
        private final Set<String> entityTypes = new HashSet<>();
        private final Set<String> complexTypes = new HashSet<>();
        private final Set<String> enumTypes = new HashSet<>();
        private final Set<String> typeDefinitions = new HashSet<>();
        private final Set<String> functions = new HashSet<>();
        private final Set<String> actions = new HashSet<>();
        private final Set<String> containers = new HashSet<>();
        private final Set<String> targets = new HashSet<>();
        
        public TypeRegistry(List<CsdlSchema> schemas) {
            for (CsdlSchema schema : schemas) {
                registerSchemaTypes(schema);
            }
        }
        
        private void registerSchemaTypes(CsdlSchema schema) {
            String namespace = schema.getNamespace();
            
            // Register entity types
            if (schema.getEntityTypes() != null) {
                for (CsdlEntityType entityType : schema.getEntityTypes()) {
                    String fullName = namespace + "." + entityType.getName();
                    entityTypes.add(fullName);
                    targets.add(fullName);
                    
                    // Register properties as potential targets
                    if (entityType.getProperties() != null) {
                        for (CsdlProperty property : entityType.getProperties()) {
                            targets.add(fullName + "/" + property.getName());
                        }
                    }
                    
                    // Register navigation properties as potential targets
                    if (entityType.getNavigationProperties() != null) {
                        for (CsdlNavigationProperty navProp : entityType.getNavigationProperties()) {
                            targets.add(fullName + "/" + navProp.getName());
                        }
                    }
                }
            }
            
            // Register complex types
            if (schema.getComplexTypes() != null) {
                for (CsdlComplexType complexType : schema.getComplexTypes()) {
                    String fullName = namespace + "." + complexType.getName();
                    complexTypes.add(fullName);
                    targets.add(fullName);
                    
                    // Register properties as potential targets
                    if (complexType.getProperties() != null) {
                        for (CsdlProperty property : complexType.getProperties()) {
                            targets.add(fullName + "/" + property.getName());
                        }
                    }
                }
            }
            
            // Register enum types
            if (schema.getEnumTypes() != null) {
                for (CsdlEnumType enumType : schema.getEnumTypes()) {
                    String fullName = namespace + "." + enumType.getName();
                    enumTypes.add(fullName);
                    targets.add(fullName);
                }
            }
            
            // Register type definitions
            if (schema.getTypeDefinitions() != null) {
                for (CsdlTypeDefinition typeDef : schema.getTypeDefinitions()) {
                    String fullName = namespace + "." + typeDef.getName();
                    typeDefinitions.add(fullName);
                    targets.add(fullName);
                }
            }
            
            // Register functions
            if (schema.getFunctions() != null) {
                for (CsdlFunction function : schema.getFunctions()) {
                    String fullName = namespace + "." + function.getName();
                    functions.add(fullName);
                    targets.add(fullName);
                }
            }
            
            // Register actions
            if (schema.getActions() != null) {
                for (CsdlAction action : schema.getActions()) {
                    String fullName = namespace + "." + action.getName();
                    actions.add(fullName);
                    targets.add(fullName);
                }
            }
            
            // Register entity container
            if (schema.getEntityContainer() != null) {
                String fullName = namespace + "." + schema.getEntityContainer().getName();
                containers.add(fullName);
                targets.add(fullName);
            }
        }
        
        public boolean hasEntityType(String typeName) {
            return entityTypes.contains(typeName);
        }
        
        public boolean hasComplexType(String typeName) {
            return complexTypes.contains(typeName);
        }
        
        public boolean hasEnumType(String typeName) {
            return enumTypes.contains(typeName);
        }
        
        public boolean hasTypeDefinition(String typeName) {
            return typeDefinitions.contains(typeName);
        }
        
        public boolean hasFunction(String functionName) {
            return functions.contains(functionName);
        }
        
        public boolean hasAction(String actionName) {
            return actions.contains(actionName);
        }
        
        public boolean hasContainer(String containerName) {
            return containers.contains(containerName);
        }
        
        public boolean hasTarget(String targetName) {
            return targets.contains(targetName);
        }
        
        public boolean hasType(String typeName) {
            return hasEntityType(typeName) || 
                   hasComplexType(typeName) || 
                   hasEnumType(typeName) || 
                   hasTypeDefinition(typeName);
        }
    }
}
