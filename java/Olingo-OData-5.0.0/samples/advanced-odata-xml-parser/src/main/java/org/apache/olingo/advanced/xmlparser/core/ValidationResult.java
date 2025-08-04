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
 * Result of schema validation operation
 */
public class ValidationResult {
    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    private final List<String> messages = new ArrayList<>();
    private boolean isValid = true;
    
    /**
     * Add an error message
     */
    public void addError(String error) {
        errors.add(error);
        isValid = false;
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
     * Check if validation has errors
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    /**
     * Check if validation has warnings
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    /**
     * Check if validation is successful
     */
    public boolean isValid() {
        return isValid && errors.isEmpty();
    }
    
    /**
     * Get all error messages
     */
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }
    
    /**
     * Get all warning messages
     */
    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }
    
    /**
     * Get all informational messages
     */
    public List<String> getMessages() {
        return new ArrayList<>(messages);
    }
    
    /**
     * Merge another validation result into this one
     */
    public void merge(ValidationResult other) {
        this.errors.addAll(other.errors);
        this.warnings.addAll(other.warnings);
        this.messages.addAll(other.messages);
        this.isValid = this.isValid && other.isValid;
    }
    
    /**
     * Get a summary of the validation result
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Validation Result: ").append(isValid() ? "SUCCESS" : "FAILED").append("\n");
        sb.append("Errors: ").append(errors.size()).append("\n");
        sb.append("Warnings: ").append(warnings.size()).append("\n");
        sb.append("Messages: ").append(messages.size()).append("\n");
        
        if (!errors.isEmpty()) {
            sb.append("\nErrors:\n");
            for (String error : errors) {
                sb.append("  - ").append(error).append("\n");
            }
        }
        
        if (!warnings.isEmpty()) {
            sb.append("\nWarnings:\n");
            for (String warning : warnings) {
                sb.append("  - ").append(warning).append("\n");
            }
        }
        
        if (!messages.isEmpty()) {
            sb.append("\nMessages:\n");
            for (String message : messages) {
                sb.append("  - ").append(message).append("\n");
            }
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return getSummary();
    }
}
