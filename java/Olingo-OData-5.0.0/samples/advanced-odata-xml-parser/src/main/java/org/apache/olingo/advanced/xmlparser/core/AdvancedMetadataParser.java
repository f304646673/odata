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
package org.apache.olingo.advanced.xmlparser.core;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.olingo.advanced.xmlparser.statistics.ParseStatistics;
import org.apache.olingo.advanced.xmlparser.statistics.ErrorType;
import org.apache.olingo.advanced.xmlparser.cache.ICacheManager;
import org.apache.olingo.advanced.xmlparser.cache.CacheManager;
import org.apache.olingo.advanced.xmlparser.resolver.IReferenceResolverManager;
import org.apache.olingo.advanced.xmlparser.resolver.ReferenceResolverManager;
import org.apache.olingo.advanced.xmlparser.resolver.ClassPathReferenceResolver;
import org.apache.olingo.advanced.xmlparser.resolver.FileSystemReferenceResolver;
import org.apache.olingo.advanced.xmlparser.resolver.UrlReferenceResolver;
import org.apache.olingo.advanced.xmlparser.resolver.FileBasedReferenceResolver;
import org.apache.olingo.advanced.xmlparser.schema.SchemaComparator;
import org.apache.olingo.advanced.xmlparser.schema.ISchemaMerger;
import org.apache.olingo.advanced.xmlparser.schema.SchemaMerger;
import org.apache.olingo.advanced.xmlparser.schema.ISchemaValidator;
import org.apache.olingo.advanced.xmlparser.schema.SchemaValidator;
import org.apache.olingo.advanced.xmlparser.dependency.IDependencyGraphManager;
import org.apache.olingo.advanced.xmlparser.dependency.DependencyGraphManager;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.commons.api.edmx.EdmxReferenceInclude;
import org.apache.olingo.server.core.MetadataParser;
import org.apache.olingo.server.core.ReferenceResolver;
import org.apache.olingo.server.core.SchemaBasedEdmProvider;

/**
 * Modular Advanced Metadata Parser that uses composition of smaller modules
 * to handle complex OData schema parsing with dependency resolution.
 */
public class AdvancedMetadataParser {
    
    private boolean detectCircularDependencies = true;
    private boolean allowCircularDependencies = false;
    private boolean enableCaching = true;
    private int maxDependencyDepth = 10;
    
    private final Map<String, List<String>> errorReport = new ConcurrentHashMap<>();
    private final ParseStatistics statistics = new ParseStatistics();
    private final MetadataParser underlyingParser;
    
    // Modular components
    private final IDependencyGraphManager dependencyManager;
    private final SchemaComparator schemaComparator;
    private final ISchemaMerger schemaMerger;
    private final IReferenceResolverManager referenceManager;
    private final ISchemaValidator schemaValidator;
    private final ICacheManager cacheManager;
    
    /**
     * Constructor
     */
    public AdvancedMetadataParser() {
        this.underlyingParser = new MetadataParser();
        
        // Initialize modular components
        this.dependencyManager = new DependencyGraphManager(statistics, errorReport);
        this.schemaComparator = new SchemaComparator();
        this.schemaMerger = new SchemaMerger(schemaComparator, statistics, errorReport);
        this.referenceManager = new ReferenceResolverManager();
        this.schemaValidator = new SchemaValidator(statistics);
        this.cacheManager = new CacheManager(enableCaching);
        
        // Add default reference resolvers
        referenceManager.addReferenceResolver(new ClassPathReferenceResolver());
        referenceManager.addReferenceResolver(new FileSystemReferenceResolver());
        referenceManager.addReferenceResolver(new UrlReferenceResolver());
        
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
        this.cacheManager.setEnabled(enable);
        return this;
    }
    
    public AdvancedMetadataParser maxDependencyDepth(int depth) {
        this.maxDependencyDepth = depth;
        return this;
    }
    
    public AdvancedMetadataParser addReferenceResolver(ReferenceResolver resolver) {
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
            
            // Clear state for new parsing operation
            clearState();
            
            // Build dependency graph
            buildDependencyGraph(mainSchemaPath, 0);
            
            // Check for circular dependencies
            if (detectCircularDependencies) {
                List<List<String>> cycles = dependencyManager.detectCircularDependencies();
                if (!cycles.isEmpty()) {
                    statistics.incrementCircularDetected();
                    dependencyManager.handleCircularDependencies(cycles, allowCircularDependencies);
                }
            }
            
            // Resolve dependencies in topological order
            List<String> loadOrder = dependencyManager.calculateLoadOrder();
            
            // Load schemas in dependency order
            SchemaBasedEdmProvider result = loadSchemasInOrder(loadOrder, mainSchemaPath);
            
            // Validate references after all schemas are loaded
            schemaValidator.validateReferences(result);
            
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
        
        if (dependencyManager.containsSchema(schemaPath)) {
            return; // Already analyzed
        }
        
        if (dependencyManager.isCurrentlyLoading(schemaPath)) {
            // Circular dependency detected during parsing
            statistics.addError(ErrorType.CIRCULAR_DEPENDENCY,
                "Circular dependency detected during parsing: " + schemaPath,
                schemaPath);
            return;
        }
        
        dependencyManager.markLoading(schemaPath);
        
        try {
            // Extract references from XML using our own XML parsing
            // to avoid Olingo's deduplication by namespace
            Set<String> xmlReferences = referenceManager.extractReferencesFromXml(schemaPath);
            
            // Add all found references to dependencies
            for (String refPath : xmlReferences) {
                dependencyManager.addDependency(schemaPath, refPath);
                
                // Recursively analyze dependencies
                buildDependencyGraph(refPath, depth + 1);
            }
            
            statistics.incrementSchemasProcessed();
            
        } catch (Exception e) {
            statistics.addError(ErrorType.DEPENDENCY_ANALYSIS_ERROR, "Dependency analysis failed", schemaPath, e);
            errorReport.put(schemaPath, java.util.Arrays.asList("Dependency analysis failed: " + e.getMessage()));
            throw e;
        } finally {
            dependencyManager.markFinished(schemaPath);
        }
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
        String cacheKey = cacheManager.generateCacheKey(schemaPath);
        
        // Check cache first
        if (enableCaching && cacheManager.containsKey(cacheKey)) {
            SchemaBasedEdmProvider cachedProvider = cacheManager.getCachedProvider(cacheKey);
            schemaMerger.copySchemas(cachedProvider, targetProvider);
            statistics.incrementCachedReused();
            return;
        }

        // Check for recursive loading
        if (dependencyManager.isCurrentlyLoading(schemaPath)) {
            return;
        }

        dependencyManager.markLoading(schemaPath);

        try {
            // Resolve and load schema
            InputStream inputStream = referenceManager.resolveReference(schemaPath);
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
                cacheManager.cacheProvider(cacheKey, schemaProvider);
            }
            
            // Copy schemas to target provider
            schemaMerger.copySchemas(schemaProvider, targetProvider);
            
            // Copy references using reflection to access protected methods
            for (EdmxReference reference : schemaProvider.getReferences()) {
                addReferenceUsingReflection(targetProvider, reference);
            }
            
            statistics.incrementFilesProcessed();
            
        } catch (Exception e) {
            statistics.addError(ErrorType.SCHEMA_LOADING_ERROR, "Schema loading failed", schemaPath, e);
            errorReport.put(schemaPath, java.util.Arrays.asList("Schema loading failed: " + e.getMessage()));
            throw e;
        } finally {
            dependencyManager.markFinished(schemaPath);
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
    
    /**
     * Validate an OData 4 XML file against an existing SchemaBasedEdmProvider.
     * This method does not modify the existing provider.
     * 
     * @param existingProvider The existing SchemaBasedEdmProvider to validate against
     * @param xmlSchemaPath Path to the OData 4 XML file to validate
     * @return ValidationResult containing validation details and any conflicts
     * @throws Exception if validation fails
     */
    public ValidationResult validateSchema(SchemaBasedEdmProvider existingProvider, String xmlSchemaPath) throws Exception {
        return validateSchemaInternal(existingProvider, xmlSchemaPath, false);
    }
    
    /**
     * Validate schemas in a directory against an existing SchemaBasedEdmProvider.
     * This method does not modify the existing provider.
     * 
     * @param existingProvider The existing SchemaBasedEdmProvider to validate against
     * @param schemaDirectory Path to the directory containing OData 4 XML files
     * @return ValidationResult containing validation details and any conflicts
     * @throws Exception if validation fails
     */
    public ValidationResult validateSchemaDirectory(SchemaBasedEdmProvider existingProvider, String schemaDirectory) throws Exception {
        File dir = new File(schemaDirectory);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("Schema directory does not exist or is not a directory: " + schemaDirectory);
        }
        
        ValidationResult combinedResult = new ValidationResult();
        
        // Find all XML files in the directory
        File[] xmlFiles = dir.listFiles((file, name) -> name.toLowerCase().endsWith(".xml"));
        if (xmlFiles == null || xmlFiles.length == 0) {
            combinedResult.addMessage("No XML files found in directory: " + schemaDirectory);
            return combinedResult;
        }
        
        for (File xmlFile : xmlFiles) {
            try {
                ValidationResult fileResult = validateSchemaInternal(existingProvider, xmlFile.getAbsolutePath(), false);
                combinedResult.merge(fileResult);
            } catch (Exception e) {
                combinedResult.addError("Failed to validate file " + xmlFile.getName() + ": " + e.getMessage());
            }
        }
        
        return combinedResult;
    }
    
    /**
     * Merge an OData 4 XML file into an existing SchemaBasedEdmProvider.
     * This method modifies the existing provider by adding new schemas.
     * 
     * @param existingProvider The existing SchemaBasedEdmProvider to merge into (will be modified)
     * @param xmlSchemaPath Path to the OData 4 XML file to merge
     * @return MergeResult containing merge details and any conflicts
     * @throws Exception if merge fails
     */
    public MergeResult mergeSchema(SchemaBasedEdmProvider existingProvider, String xmlSchemaPath) throws Exception {
        return mergeSchemaInternal(existingProvider, xmlSchemaPath);
    }
    
    /**
     * Merge schemas from a directory into an existing SchemaBasedEdmProvider.
     * This method modifies the existing provider by adding new schemas.
     * 
     * @param existingProvider The existing SchemaBasedEdmProvider to merge into (will be modified)
     * @param schemaDirectory Path to the directory containing OData 4 XML files
     * @return MergeResult containing merge details and any conflicts
     * @throws Exception if merge fails
     */
    public MergeResult mergeSchemaDirectory(SchemaBasedEdmProvider existingProvider, String schemaDirectory) throws Exception {
        File dir = new File(schemaDirectory);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("Schema directory does not exist or is not a directory: " + schemaDirectory);
        }
        
        MergeResult combinedResult = new MergeResult();
        
        // Find all XML files in the directory
        File[] xmlFiles = dir.listFiles((file, name) -> name.toLowerCase().endsWith(".xml"));
        if (xmlFiles == null || xmlFiles.length == 0) {
            combinedResult.addMessage("No XML files found in directory: " + schemaDirectory);
            return combinedResult;
        }
        
        for (File xmlFile : xmlFiles) {
            try {
                MergeResult fileResult = mergeSchemaInternal(existingProvider, xmlFile.getAbsolutePath());
                combinedResult.merge(fileResult);
            } catch (Exception e) {
                combinedResult.addError("Failed to merge file " + xmlFile.getName() + ": " + e.getMessage());
            }
        }
        
        return combinedResult;
    }
    
    /**
     * Internal method for schema validation
     */
    private ValidationResult validateSchemaInternal(SchemaBasedEdmProvider existingProvider, String xmlSchemaPath, boolean allowMerge) throws Exception {
        ValidationResult result = new ValidationResult();
        
        // Parse the new schema
        SchemaBasedEdmProvider newProvider = buildEdmProvider(xmlSchemaPath);
        
        // Validate compatibility
        result = schemaValidator.validateCompatibility(existingProvider, newProvider);
        
        // Additional validation specific to the file
        File schemaFile = new File(xmlSchemaPath);
        if (!schemaFile.exists()) {
            result.addError("Schema file does not exist: " + xmlSchemaPath);
            return result;
        }
        
        result.addMessage("Validated schema file: " + xmlSchemaPath);
        result.addMessage("New schemas found: " + newProvider.getSchemas().size());
        
        return result;
    }
    
    /**
     * Internal method for schema merging
     */
    private MergeResult mergeSchemaInternal(SchemaBasedEdmProvider existingProvider, String xmlSchemaPath) throws Exception {
        MergeResult result = new MergeResult();
        
        // First validate the schema
        ValidationResult validationResult = validateSchemaInternal(existingProvider, xmlSchemaPath, true);
        result.setValidationResult(validationResult);
        
        if (validationResult.hasErrors()) {
            result.addError("Validation failed, merge aborted");
            return result;
        }
        
        // Parse the new schema
        SchemaBasedEdmProvider newProvider = buildEdmProvider(xmlSchemaPath);
        
        // Perform the merge
        int originalSchemaCount = existingProvider.getSchemas().size();
        schemaMerger.copySchemas(newProvider, existingProvider);
        
        // Copy references
        for (EdmxReference reference : newProvider.getReferences()) {
            addReferenceUsingReflection(existingProvider, reference);
        }
        
        int newSchemaCount = existingProvider.getSchemas().size();
        
        result.addMessage("Successfully merged schema from: " + xmlSchemaPath);
        result.addMessage("Original schema count: " + originalSchemaCount);
        result.addMessage("New schema count: " + newSchemaCount);
        result.addMessage("Schemas added: " + (newSchemaCount - originalSchemaCount));
        
        return result;
    }
}
