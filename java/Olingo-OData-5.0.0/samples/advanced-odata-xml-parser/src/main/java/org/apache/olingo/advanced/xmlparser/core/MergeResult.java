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

import java.util.ArrayList;
import java.util.List;

/**
 * Result of schema merge operation
 */
public class MergeResult {
    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    private final List<String> messages = new ArrayList<>();
    private ValidationResult validationResult;
    private boolean isSuccessful = true;
    
    /**
     * Add an error message
     */
    public void addError(String error) {
        errors.add(error);
        isSuccessful = false;
    }
    
    /**
     * Add a warning message
     */
    public void addWarning(String warning) {
        warnings.add(warning);
    }
    
    /**
     * Add an informational message
     */
    public void addMessage(String message) {
        messages.add(message);
    }
    
    /**
     * Set the validation result that was performed before merge
     */
    public void setValidationResult(ValidationResult validationResult) {
        this.validationResult = validationResult;
        if (validationResult != null && validationResult.hasErrors()) {
            this.isSuccessful = false;
        }
    }
    
    /**
     * Get the validation result
     */
    public ValidationResult getValidationResult() {
        return validationResult;
    }
    
    /**
     * Check if merge has errors
     */
    public boolean hasErrors() {
        return !errors.isEmpty() || (validationResult != null && validationResult.hasErrors());
    }
    
    /**
     * Check if merge has warnings
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty() || (validationResult != null && validationResult.hasWarnings());
    }
    
    /**
     * Check if merge is successful
     */
    public boolean isSuccessful() {
        return isSuccessful && !hasErrors();
    }
    
    /**
     * Get all error messages (including validation errors)
     */
    public List<String> getErrors() {
        List<String> allErrors = new ArrayList<>(errors);
        if (validationResult != null) {
            allErrors.addAll(validationResult.getErrors());
        }
        return allErrors;
    }
    
    /**
     * Get all warning messages (including validation warnings)
     */
    public List<String> getWarnings() {
        List<String> allWarnings = new ArrayList<>(warnings);
        if (validationResult != null) {
            allWarnings.addAll(validationResult.getWarnings());
        }
        return allWarnings;
    }
    
    /**
     * Get all informational messages (including validation messages)
     */
    public List<String> getMessages() {
        List<String> allMessages = new ArrayList<>(messages);
        if (validationResult != null) {
            allMessages.addAll(validationResult.getMessages());
        }
        return allMessages;
    }
    
    /**
     * Merge another merge result into this one
     */
    public void merge(MergeResult other) {
        this.errors.addAll(other.errors);
        this.warnings.addAll(other.warnings);
        this.messages.addAll(other.messages);
        this.isSuccessful = this.isSuccessful && other.isSuccessful;
        
        // Merge validation results if they exist
        if (other.validationResult != null) {
            if (this.validationResult == null) {
                this.validationResult = new ValidationResult();
            }
            this.validationResult.merge(other.validationResult);
        }
    }
    
    /**
     * Get a summary of the merge result
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Merge Result: ").append(isSuccessful() ? "SUCCESS" : "FAILED").append("\n");
        sb.append("Errors: ").append(getErrors().size()).append("\n");
        sb.append("Warnings: ").append(getWarnings().size()).append("\n");
        sb.append("Messages: ").append(getMessages().size()).append("\n");
        
        List<String> allErrors = getErrors();
        if (!allErrors.isEmpty()) {
            sb.append("\nErrors:\n");
            for (String error : allErrors) {
                sb.append("  - ").append(error).append("\n");
            }
        }
        
        List<String> allWarnings = getWarnings();
        if (!allWarnings.isEmpty()) {
            sb.append("\nWarnings:\n");
            for (String warning : allWarnings) {
                sb.append("  - ").append(warning).append("\n");
            }
        }
        
        List<String> allMessages = getMessages();
        if (!allMessages.isEmpty()) {
            sb.append("\nMessages:\n");
            for (String message : allMessages) {
                sb.append("  - ").append(message).append("\n");
            }
        }
        
        if (validationResult != null) {
            sb.append("\nValidation Details:\n");
            sb.append(validationResult.getSummary());
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return getSummary();
    }
}
