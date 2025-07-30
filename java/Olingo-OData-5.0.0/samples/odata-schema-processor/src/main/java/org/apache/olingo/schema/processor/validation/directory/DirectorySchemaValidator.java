package org.apache.olingo.schema.processor.validation.directory;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schema.processor.validation.file.XmlComplianceResult;
import org.apache.olingo.schema.processor.validation.file.XmlFileComplianceValidator;
import org.apache.olingo.server.core.MetadataParser;
import org.apache.olingo.server.core.SchemaBasedEdmProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Validates XML schema files at the directory level.
 * This validator processes all XML files in a directory and:
 * 1. Validates each file individually using existing validators
 * 2. Detects conflicts between schemas across different files
 * 3. Ensures namespace consistency across the directory
 * 4. Provides comprehensive reporting of all issues
 */
public class DirectorySchemaValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(DirectorySchemaValidator.class);
    
    private final XmlFileComplianceValidator fileValidator;
    private final SchemaConflictDetector conflictDetector;
    private final ExecutorService executorService;
    private final int maxConcurrentValidations;
    
    /**
     * Constructor with default settings
     */
    public DirectorySchemaValidator(XmlFileComplianceValidator fileValidator) {
        this(fileValidator, 4); // Default to 4 concurrent validations
    }
    
    /**
     * Constructor with custom concurrency setting
     */
    public DirectorySchemaValidator(XmlFileComplianceValidator fileValidator, int maxConcurrentValidations) {
        this.fileValidator = Objects.requireNonNull(fileValidator, "File validator cannot be null");
        this.conflictDetector = new SchemaConflictDetector();
        this.maxConcurrentValidations = Math.max(1, maxConcurrentValidations);
        this.executorService = Executors.newFixedThreadPool(this.maxConcurrentValidations);
    }
    
    /**
     * Validate all XML files in a directory
     * 
     * @param directoryPath path to the directory containing XML files
     * @return comprehensive validation result for the directory
     */
    public DirectoryValidationResult validateDirectory(Path directoryPath) {
        return validateDirectory(directoryPath, "*.xml");
    }
    
    /**
     * Validate XML files in a directory matching a specific pattern
     * 
     * @param directoryPath path to the directory containing XML files
     * @param filePattern glob pattern for file matching (e.g., "*.xml", "schema_*.xml")
     * @return comprehensive validation result for the directory
     */
    public DirectoryValidationResult validateDirectory(Path directoryPath, String filePattern) {
        long startTime = System.currentTimeMillis();
        
        if (directoryPath == null || !Files.exists(directoryPath) || !Files.isDirectory(directoryPath)) {
            return createErrorResult("Directory does not exist or is not a directory", 
                                   directoryPath != null ? directoryPath.toString() : "null", startTime);
        }
        
        try {
            // Find all XML files matching the pattern
            List<Path> xmlFiles = findXmlFiles(directoryPath, filePattern);
            
            if (xmlFiles.isEmpty()) {
                return createEmptyResult(directoryPath.toString(), startTime);
            }
            
            // Validate individual files
            Map<String, XmlComplianceResult> fileResults = validateFiles(xmlFiles);
            
            // Extract schemas from valid files for conflict detection
            extractAndAnalyzeSchemas(xmlFiles, fileResults);
            
            // Detect conflicts across files
            List<SchemaConflict> conflicts = conflictDetector.detectConflicts();
            
            // Generate global warnings and errors
            List<String> globalErrors = new ArrayList<>();
            List<String> globalWarnings = new ArrayList<>();
            generateGlobalIssues(fileResults, conflicts, globalErrors, globalWarnings);
            
            // Determine overall compliance
            boolean compliant = isDirectoryCompliant(fileResults, conflicts, globalErrors);
            
            // Get namespace mapping
            Map<String, Set<String>> namespaceToFiles = conflictDetector.getNamespaceToFiles();
            
            return new DirectoryValidationResult(
                compliant,
                fileResults,
                conflicts,
                globalErrors,
                globalWarnings,
                namespaceToFiles,
                directoryPath.toString(),
                System.currentTimeMillis() - startTime
            );
            
        } catch (Exception e) {
            logger.error("Failed to validate directory: {}", directoryPath, e);
            return createErrorResult("Directory validation failed: " + e.getMessage(),
                                   directoryPath.toString(), startTime);
        } finally {
            conflictDetector.clear();
        }
    }
    
    /**
     * Find all XML files in the directory matching the pattern (recursively)
     */
    private List<Path> findXmlFiles(Path directoryPath, String pattern) throws IOException {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        
        try (Stream<Path> stream = Files.walk(directoryPath)) {
            return stream
                .filter(Files::isRegularFile)
                .filter(path -> matcher.matches(path.getFileName()))
                .collect(Collectors.toList());
        }
    }
    
    /**
     * Validate all files, optionally using parallel processing
     */
    private Map<String, XmlComplianceResult> validateFiles(List<Path> xmlFiles) {
        Map<String, XmlComplianceResult> results = new HashMap<>();
        
        if (xmlFiles.size() <= 1 || maxConcurrentValidations <= 1) {
            // Sequential validation for small numbers of files
            for (Path file : xmlFiles) {
                String fileName = file.getFileName().toString();
                try {
                    XmlComplianceResult result = fileValidator.validateFile(file);
                    results.put(fileName, result);
                } catch (Exception e) {
                    logger.error("Failed to validate file: {}", file, e);
                    results.put(fileName, createFileErrorResult(fileName, e.getMessage()));
                }
            }
        } else {
            // Parallel validation for larger numbers of files
            List<CompletableFuture<Void>> futures = xmlFiles.stream()
                .map(file -> CompletableFuture.runAsync(() -> {
                    String fileName = file.getFileName().toString();
                    try {
                        XmlComplianceResult result = fileValidator.validateFile(file);
                        synchronized (results) {
                            results.put(fileName, result);
                        }
                    } catch (Exception e) {
                        logger.error("Failed to validate file: {}", file, e);
                        synchronized (results) {
                            results.put(fileName, createFileErrorResult(fileName, e.getMessage()));
                        }
                    }
                }, executorService))
                .collect(Collectors.toList());
            
            // Wait for all validations to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
        
        return results;
    }
    
    /**
     * Extract schemas from XML files and add them to conflict detector
     */
    private void extractAndAnalyzeSchemas(List<Path> xmlFiles, Map<String, XmlComplianceResult> fileResults) {
        for (Path xmlFile : xmlFiles) {
            String fileName = xmlFile.getFileName().toString();
            XmlComplianceResult result = fileResults.get(fileName);
            
            // Only analyze schemas from files that parsed successfully
            if (result != null && result.isCompliant()) {
                try {
                    List<CsdlSchema> schemas = extractSchemasFromFile(xmlFile);
                    for (CsdlSchema schema : schemas) {
                        conflictDetector.addSchema(schema, fileName);
                    }
                } catch (Exception e) {
                    logger.warn("Failed to extract schemas from file {} for conflict analysis: {}", fileName, e.getMessage());
                }
            }
        }
    }
    
    /**
     * Extract CSDL schemas from an XML file
     */
    private List<CsdlSchema> extractSchemasFromFile(Path xmlFile) throws Exception {
        MetadataParser parser = new MetadataParser();
        parser.recursivelyLoadReferences(false); // Don't follow references for conflict detection
        parser.parseAnnotations(true);
        
        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(xmlFile), StandardCharsets.UTF_8)) {
            SchemaBasedEdmProvider edmProvider = parser.buildEdmProvider(reader);
            return new ArrayList<>(edmProvider.getSchemas());
        }
    }
    
    /**
     * Generate global errors and warnings based on file results and conflicts
     */
    private void generateGlobalIssues(Map<String, XmlComplianceResult> fileResults,
                                    List<SchemaConflict> conflicts,
                                    List<String> globalErrors,
                                    List<String> globalWarnings) {
        
        // Count invalid files
        long invalidFileCount = fileResults.values().stream()
                                          .filter(result -> !result.isCompliant())
                                          .count();
        
        if (invalidFileCount > 0) {
            globalWarnings.add(String.format("%d out of %d files failed individual validation", 
                                            invalidFileCount, fileResults.size()));
        }
        
        // Add conflict-based errors
        for (SchemaConflict conflict : conflicts) {
            switch (conflict.getType()) {
                case DUPLICATE_ELEMENT:
                case DUPLICATE_NAMESPACE_SCHEMA:
                case INCOMPATIBLE_DEFINITION:
                    globalErrors.add(conflict.getDescription());
                    break;
                case CIRCULAR_REFERENCE:
                case MISSING_REFERENCE:
                    globalWarnings.add(conflict.getDescription());
                    break;
            }
        }
        
        // Check for isolated files (files with namespaces not referenced by others)
        Set<String> allNamespaces = conflictDetector.getNamespaceToFiles().keySet();
        if (allNamespaces.size() > fileResults.size()) {
            globalWarnings.add("Some namespaces appear to be isolated (not referenced by other files)");
        }
    }
    
    /**
     * Determine if the directory is overall compliant
     */
    private boolean isDirectoryCompliant(Map<String, XmlComplianceResult> fileResults,
                                       List<SchemaConflict> conflicts,
                                       List<String> globalErrors) {
        
        // All files must be individually compliant
        boolean allFilesCompliant = fileResults.values().stream()
                                              .allMatch(XmlComplianceResult::isCompliant);
        
        // No conflicts should exist
        boolean noConflicts = conflicts.isEmpty();
        
        // No global errors
        boolean noGlobalErrors = globalErrors.isEmpty();
        
        return allFilesCompliant && noConflicts && noGlobalErrors;
    }
    
    /**
     * Create an error result for the directory validation
     */
    private DirectoryValidationResult createErrorResult(String error, String directoryPath, long startTime) {
        return new DirectoryValidationResult(
            false,
            Collections.emptyMap(),
            Collections.emptyList(),
            Collections.singletonList(error),
            Collections.emptyList(),
            Collections.emptyMap(),
            directoryPath,
            System.currentTimeMillis() - startTime
        );
    }
    
    /**
     * Create an empty result (no files found)
     */
    private DirectoryValidationResult createEmptyResult(String directoryPath, long startTime) {
        return new DirectoryValidationResult(
            true,
            Collections.emptyMap(),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.singletonList("No XML files found matching the pattern"),
            Collections.emptyMap(),
            directoryPath,
            System.currentTimeMillis() - startTime
        );
    }
    
    /**
     * Create an error result for individual file validation
     */
    private XmlComplianceResult createFileErrorResult(String fileName, String error) {
        return new XmlComplianceResult(
            false,
            Collections.singletonList(error),
            Collections.emptyList(),
            Collections.emptySet(),
            Collections.emptyMap(),
            fileName,
            0
        );
    }
    
    /**
     * Shutdown the executor service
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
