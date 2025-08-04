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
 * Enumeration of result types for validation and merge operations.
 * Organized by category with clear, non-overlapping semantics.
 * @deprecated Use OperationType instead
 */
@Deprecated
public enum ResultType {
    // ========================================
    // File/IO Errors
    // ========================================
    FILE_NOT_FOUND("File or schema not found"),
    FILE_READ_ERROR("File read error"),
    FILE_WRITE_ERROR("File write error"),
    PERMISSION_DENIED("Permission denied"),
    
    // ========================================
    // Schema Structure Errors
    // ========================================
    SCHEMA_INVALID("Invalid schema structure"),
    SCHEMA_MALFORMED("Malformed schema syntax"),
    SCHEMA_EMPTY("Empty schema content"),
    SCHEMA_TOO_LARGE("Schema exceeds size limits"),
    SCHEMA_LOADING_ERROR("Error loading schema"),
    
    // ========================================
    // Parsing Errors
    // ========================================
    PARSING_ERROR("General parsing error"),
    
    // ========================================
    // Validation Errors
    // ========================================
    VALIDATION_FAILED("Validation failed"),
    MISSING_REQUIRED_ELEMENT("Missing required element"),
    INVALID_ELEMENT_TYPE("Invalid element type"),
    INVALID_ATTRIBUTE("Invalid attribute"),
    DUPLICATE_ELEMENT("Duplicate element"),
    TYPE_MISMATCH("Type mismatch"),
    CONSTRAINT_VIOLATION("Constraint violation"),
    
    // ========================================
    // Reference/Dependency Errors
    // ========================================
    CIRCULAR_REFERENCE("Circular reference detected"),
    REFERENCE_NOT_FOUND("Reference target not found"),
    REFERENCE_AMBIGUOUS("Ambiguous reference"),
    REFERENCE_INVALID("Invalid reference"),
    DEPENDENCY_NOT_FOUND("Dependency not found"),
    DEPENDENCY_RESOLUTION_FAILED("Dependency resolution failed"),
    DEPENDENCY_ANALYSIS_ERROR("Error analyzing dependencies"),
    UNRESOLVED_TYPE_REFERENCE("Type reference cannot be resolved"),
    MISSING_ANNOTATION_TARGET("Annotation target does not exist"),
    
    // ========================================
    // Merge/Schema Compatibility Errors
    // ========================================
    SCHEMA_MERGE_CONFLICT("Schema merge conflict detected"),
    MERGE_VALIDATION_FAILED("Pre-merge validation failed"),
    SCHEMA_INCOMPATIBLE("Schemas incompatible for merge"),
    VERSION_MISMATCH("Version mismatch"),
    NAMESPACE_CONFLICT("Namespace conflict"),
    
    // ========================================
    // Network/System Errors
    // ========================================
    NETWORK_ERROR("Network error"),
    
    // ========================================
    // Configuration Errors
    // ========================================
    CONFIGURATION_ERROR("Configuration error"),
    INVALID_PARAMETER("Invalid parameter"),
    
    // ========================================
    // Warnings
    // ========================================
    SCHEMA_WARNING("Schema warning"),
    TYPE_OVERRIDDEN("Type overridden"),
    DEPRECATED_FEATURE("Deprecated feature used"),
    PERFORMANCE_WARNING("Performance warning"),
    COMPATIBILITY_WARNING("Compatibility warning"),
    
    // ========================================
    // Info/Success
    // ========================================
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
