package org.apache.olingo.advanced.xmlparser.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Statistics and metrics
 */
public class ParseStatistics {
    private int totalFilesProcessed = 0;
    private int cachedFilesReused = 0;
    private int circularDependenciesDetected = 0;
    private int maxDepthReached = 0;
    private long totalParsingTime = 0;
    private long startTime = 0;
    private long endTime = 0;
    private int schemasProcessed = 0;
    private int schemasLoaded = 0;
    private long totalTime = 0;
    private List<String> loadOrder = new ArrayList<>();
    private final List<ErrorInfo> errors = new ArrayList<>();
    
    // Getters
    public int getTotalFilesProcessed() { return totalFilesProcessed; }
    public int getCachedFilesReused() { return cachedFilesReused; }
    public int getCircularDependenciesDetected() { return circularDependenciesDetected; }
    public int getMaxDepthReached() { return maxDepthReached; }
    public long getTotalParsingTime() { return totalParsingTime; }
    public int getSchemasProcessed() { return schemasProcessed; }
    public int getSchemasLoaded() { return schemasLoaded; }
    public long getTotalTime() { return totalTime; }
    public List<String> getLoadOrder() { return new ArrayList<>(loadOrder); }
    
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
    
    public void incrementFilesProcessed() { totalFilesProcessed++; }
    public void incrementCachedReused() { cachedFilesReused++; }
    public void incrementCircularDetected() { circularDependenciesDetected++; }
    public void updateMaxDepth(int depth) { 
        maxDepthReached = Math.max(maxDepthReached, depth); 
    }
    public void addParsingTime(long time) { totalParsingTime += time; }
    
    // Additional methods for AdvancedMetadataParser
    public void recordStart() { startTime = System.currentTimeMillis(); }
    public void recordEnd() { endTime = System.currentTimeMillis(); }
    public void incrementSchemasProcessed() { schemasProcessed++; }
    public void incrementSchemasLoaded() { schemasLoaded++; }
    public void setTotalTime(long time) { totalTime = time; }
    public void setLoadOrder(List<String> order) { loadOrder = new ArrayList<>(order); }
    
    /**
     * Add error with type and description
     */
    public void addError(ErrorType type, String description) {
        ErrorInfo error = new ErrorInfo(type, description);
        errors.add(error);
    }
    
    /**
     * Add error with type, description and context
     */
    public void addError(ErrorType type, String description, String context) {
        ErrorInfo error = new ErrorInfo(type, description, context);
        errors.add(error);
    }
    
    /**
     * Add error with type, description, context and caused by exception
     */
    public void addError(ErrorType type, String description, String context, Throwable cause) {
        ErrorInfo error = new ErrorInfo(type, description, context, cause);
        errors.add(error);
    }
}
