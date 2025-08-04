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

/**
 * Enumeration of result types for validation and merge operations
 * @deprecated Use OperationType instead
 */
@Deprecated
public enum ResultType {
    // File/IO related errors
    FILE_NOT_FOUND("File not found"),
    FILE_READ_ERROR("File read error"),
    FILE_WRITE_ERROR("File write error"),
    PERMISSION_DENIED("Permission denied"),
    
    // Schema structure errors
    SCHEMA_INVALID("Invalid schema structure"),
    SCHEMA_MALFORMED("Malformed schema"),
    SCHEMA_EMPTY("Empty schema"),
    SCHEMA_TOO_LARGE("Schema too large"),
    SCHEMA_NOT_FOUND("Schema file could not be found"),
    SCHEMA_LOADING_ERROR("Error loading schema"),
    
    // Parsing errors
    PARSING_ERROR("General parsing error"),
    
    // Validation specific errors
    VALIDATION_FAILED("Validation failed"),
    MISSING_REQUIRED_ELEMENT("Missing required element"),
    INVALID_ELEMENT_TYPE("Invalid element type"),
    INVALID_ATTRIBUTE("Invalid attribute"),
    DUPLICATE_ELEMENT("Duplicate element"),
    CIRCULAR_REFERENCE("Circular reference detected"),
    TYPE_MISMATCH("Type mismatch"),
    CONSTRAINT_VIOLATION("Constraint violation"),
    
    // Cross-file reference errors
    REFERENCE_NOT_FOUND("Reference not found"),
    REFERENCE_AMBIGUOUS("Ambiguous reference"),
    REFERENCE_CIRCULAR("Circular reference"),
    REFERENCE_INVALID("Invalid reference"),
    MISSING_TYPE_REFERENCE("Referenced type does not exist"),
    MISSING_ANNOTATION_TARGET("Annotation target does not exist"),
    UNRESOLVED_TYPE_REFERENCE("Type reference cannot be resolved"),
    
    // Merge specific errors
    MERGE_CONFLICT("Merge conflict"),
    MERGE_VALIDATION_FAILED("Pre-merge validation failed"),
    SCHEMA_INCOMPATIBLE("Schemas incompatible for merge"),
    SCHEMA_MERGE_CONFLICT("Conflict detected during schema merging"),
    VERSION_MISMATCH("Version mismatch"),
    NAMESPACE_CONFLICT("Namespace conflict"),
    
    // Network/dependency errors
    NETWORK_ERROR("Network error"),
    DEPENDENCY_NOT_FOUND("Dependency not found"),
    DEPENDENCY_RESOLUTION_FAILED("Dependency resolution failed"),
    DEPENDENCY_ANALYSIS_ERROR("Error analyzing schema dependencies"),
    SCHEMA_RESOLUTION_FAILED("Failed to resolve schema reference"),
    
    // Configuration errors
    CONFIGURATION_ERROR("Configuration error"),
    INVALID_PARAMETER("Invalid parameter"),
    
    // Warnings
    SCHEMA_WARNING("Schema warning"),
    TYPE_OVERRIDDEN("Type overridden"),
    DEPRECATED_FEATURE("Deprecated feature used"),
    PERFORMANCE_WARNING("Performance warning"),
    COMPATIBILITY_WARNING("Compatibility warning"),
    
    // Info/Success
    SUCCESS("Operation successful"),
    SCHEMA_LOADED("Schema loaded successfully"),
    VALIDATION_PASSED("Validation passed"),
    MERGE_COMPLETED("Merge completed"),
    CACHE_HIT("Cache hit"),
    CACHE_MISS("Cache miss");
    
    private final String description;
    
    ResultType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if this is an error type
     */
    public boolean isError() {
        return this.ordinal() <= INVALID_PARAMETER.ordinal();
    }
    
    /**
     * Check if this is a warning type
     */
    public boolean isWarning() {
        return this.ordinal() >= SCHEMA_WARNING.ordinal() && this.ordinal() <= COMPATIBILITY_WARNING.ordinal();
    }
    
    /**
     * Check if this is an info/success type
     */
    public boolean isInfo() {
        return this.ordinal() >= SUCCESS.ordinal();
    }
}
