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

import java.util.List;
import java.util.stream.Collectors;

/**
 * Result of schema merge operation
 * @deprecated Use OperationResult instead
 */
@Deprecated
public class MergeResult extends OperationResult {
    
    private ValidationResult validationResult;
    
    public MergeResult() {
        super(OperationType.MERGE);
    }
    
    /**
     * Add an error message
     * @deprecated Use addError(ResultType, String) instead
     */
    @Deprecated
    public void addError(String error) {
        addError(ResultType.MERGE_CONFLICT, error);
    }
    
    /**
     * Add a warning message
     * @deprecated Use addWarning(ResultType, String) instead
     */
    @Deprecated
    public void addWarning(String warning) {
        addWarning(ResultType.TYPE_OVERRIDDEN, warning);
    }
    
    /**
     * Add an informational message
     * @deprecated Use addInfo(String) instead
     */
    @Deprecated
    public void addMessage(String message) {
        addInfo(message);
    }
    
    /**
     * Set the validation result that was performed before merge
     */
    public void setValidationResult(ValidationResult validationResult) {
        this.validationResult = validationResult;
        if (validationResult != null && validationResult.hasErrors()) {
            addError(ResultType.MERGE_VALIDATION_FAILED, "Pre-merge validation failed");
        }
    }
    
    /**
     * Get the validation result
     */
    public ValidationResult getValidationResult() {
        return validationResult;
    }
    
    /**
     * Check if merge is successful
     * @deprecated Use isSuccessful() instead
     */
    @Deprecated
    public boolean isSuccessful() {
        return super.isSuccessful() && (validationResult == null || validationResult.isSuccessful());
    }
    
    /**
     * Get error messages as strings
     * @deprecated Use getErrors() and access ResultItem objects instead
     */
    @Deprecated
    public List<String> getErrorMessages() {
        List<String> allErrors = super.getErrors().stream()
                .map(item -> item.getMessage())
                .collect(Collectors.toList());
        
        if (validationResult != null) {
            allErrors.addAll(validationResult.getErrorMessages());
        }
        return allErrors;
    }
    
    /**
     * Get warning messages as strings
     * @deprecated Use getWarnings() and access ResultItem objects instead
     */
    @Deprecated
    public List<String> getWarningMessages() {
        List<String> allWarnings = super.getWarnings().stream()
                .map(item -> item.getMessage())
                .collect(Collectors.toList());
        
        if (validationResult != null) {
            allWarnings.addAll(validationResult.getWarningMessages());
        }
        return allWarnings;
    }
    
    /**
     * Get informational messages as strings
     * @deprecated Use getItems() and filter for non-error/non-warning items instead
     */
    @Deprecated
    public List<String> getMessages() {
        List<String> allMessages = getItems().stream()
                .filter(item -> !item.isError() && !item.isWarning())
                .map(item -> item.getMessage())
                .collect(Collectors.toList());
        
        if (validationResult != null) {
            allMessages.addAll(validationResult.getMessages());
        }
        return allMessages;
    }
    
    /**
     * Merge another merge result into this one
     * @deprecated Use merge(OperationResult) instead
     */
    @Deprecated
    public void merge(MergeResult other) {
        super.merge(other);
        
        // Merge validation results if they exist
        if (other.validationResult != null) {
            if (this.validationResult == null) {
                this.validationResult = new ValidationResult();
            }
            this.validationResult.merge(other.validationResult);
        }
    }
}
