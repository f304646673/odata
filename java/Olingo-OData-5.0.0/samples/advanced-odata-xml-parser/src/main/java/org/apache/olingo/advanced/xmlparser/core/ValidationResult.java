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
 * Result of schema validation operation
 * @deprecated Use OperationResult instead
 */
@Deprecated
public class ValidationResult extends OperationResult {
    
    public ValidationResult() {
        super(OperationType.VALIDATION);
    }
    
    /**
     * Add an error message
     * @deprecated Use addError(ResultType, String) instead
     */
    @Deprecated
    public void addError(String error) {
        addError(ResultType.SCHEMA_INVALID, error);
    }
    
    /**
     * Add a warning message
     * @deprecated Use addWarning(ResultType, String) instead
     */
    @Deprecated
    public void addWarning(String warning) {
        addWarning(ResultType.SCHEMA_WARNING, warning);
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
     * Get error messages as strings
     * @deprecated Use getErrors() and access ResultItem objects instead
     */
    @Deprecated
    public List<String> getErrorMessages() {
        return getErrors().stream()
                .map(item -> item.getMessage())
                .collect(Collectors.toList());
    }
    
    /**
     * Get warning messages as strings
     * @deprecated Use getWarnings() and access ResultItem objects instead
     */
    @Deprecated
    public List<String> getWarningMessages() {
        return getWarnings().stream()
                .map(item -> item.getMessage())
                .collect(Collectors.toList());
    }
    
    /**
     * Get informational messages as strings
     * @deprecated Use getItems() and filter for non-error/non-warning items instead
     */
    @Deprecated
    public List<String> getMessages() {
        return getItems().stream()
                .filter(item -> !item.isError() && !item.isWarning())
                .map(item -> item.getMessage())
                .collect(Collectors.toList());
    }
    
    /**
     * Merge another validation result into this one
     * @deprecated Use merge(OperationResult) instead
     */
    @Deprecated
    public void merge(ValidationResult other) {
        super.merge(other);
    }
}
