package org.apache.olingo.advanced.xmlparser;

/**
 * Statistics and metrics
 */
public class ParseStatistics {
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
