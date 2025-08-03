package org.apache.olingo.advanced.xmlparser;

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
    UNRESOLVED_TYPE_REFERENCE("unresolved_type_reference", "Type reference cannot be resolved"),
    MISSING_FUNCTION_REFERENCE("missing_function_reference", "Referenced function does not exist");
    
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
