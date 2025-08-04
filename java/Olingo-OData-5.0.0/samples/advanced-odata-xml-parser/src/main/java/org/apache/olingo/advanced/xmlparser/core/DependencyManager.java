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
import java.util.Map;
import java.util.Set;

/**
 * Interface for managing schema dependencies and load order
 */
public interface DependencyManager {
    
    /**
     * Add a dependency relationship
     */
    void addDependency(String schema, String dependency);
    
    /**
     * Check if schema is contained in the dependency graph
     */
    boolean containsSchema(String schemaPath);
    
    /**
     * Check if schema is currently being loaded
     */
    boolean isCurrentlyLoading(String schemaPath);
    
    /**
     * Mark schema as currently loading
     */
    void markLoading(String schemaPath);
    
    /**
     * Mark schema as finished loading
     */
    void markFinished(String schemaPath);
    
    /**
     * Detect circular dependencies
     */
    List<List<String>> detectCircularDependencies();
    
    /**
     * Handle circular dependencies based on policy
     */
    void handleCircularDependencies(List<List<String>> cycles, boolean allowCircular);
    
    /**
     * Calculate topological load order
     */
    List<String> calculateLoadOrder();
    
    /**
     * Get all dependencies
     */
    Map<String, Set<String>> getAllDependencies();
    
    /**
     * Clear internal state
     */
    void clearState();
}
