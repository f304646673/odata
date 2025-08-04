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
package org.apache.olingo.advanced.xmlparser.dependency;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for dependency graph management operations
 */
public interface IDependencyGraphManager {
    
    /**
     * Add a dependency between two schemas
     */
    void addDependency(String source, String target);
    
    /**
     * Check if a schema is currently being loaded
     */
    boolean isCurrentlyLoading(String schemaPath);
    
    /**
     * Mark a schema as currently loading
     */
    void markLoading(String schemaPath);
    
    /**
     * Mark a schema as finished loading
     */
    void markFinished(String schemaPath);
    
    /**
     * Check if dependency graph contains a schema
     */
    boolean containsSchema(String schemaPath);
    
    /**
     * Get all dependencies
     */
    Map<String, Set<String>> getAllDependencies();
    
    /**
     * Detect circular dependencies
     */
    List<List<String>> detectCircularDependencies();
    
    /**
     * Handle circular dependencies
     */
    void handleCircularDependencies(List<List<String>> cycles, boolean allowCircularDependencies) throws Exception;
    
    /**
     * Calculate load order
     */
    List<String> calculateLoadOrder();
    
    /**
     * Clear state
     */
    void clearState();
}
