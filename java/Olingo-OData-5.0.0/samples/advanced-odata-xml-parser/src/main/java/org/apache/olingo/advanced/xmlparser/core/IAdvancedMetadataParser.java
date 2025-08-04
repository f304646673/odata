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

import org.apache.olingo.advanced.xmlparser.statistics.ParseStatistics;
import org.apache.olingo.server.core.SchemaBasedEdmProvider;
import org.apache.olingo.server.core.ReferenceResolver;
import java.util.List;
import java.util.Map;

/**
 * Interface for advanced metadata parsing with dependency resolution
 */
public interface IAdvancedMetadataParser {
    
    /**
     * Configure circular dependency detection
     */
    IAdvancedMetadataParser detectCircularDependencies(boolean detect);
    
    /**
     * Configure whether to allow circular dependencies
     */
    IAdvancedMetadataParser allowCircularDependencies(boolean allow);
    
    /**
     * Configure caching
     */
    IAdvancedMetadataParser enableCaching(boolean enable);
    
    /**
     * Configure maximum dependency depth
     */
    IAdvancedMetadataParser maxDependencyDepth(int depth);
    
    /**
     * Add a reference resolver
     */
    IAdvancedMetadataParser addReferenceResolver(ReferenceResolver resolver);
    
    /**
     * Build EDM provider from schema
     */
    SchemaBasedEdmProvider buildEdmProvider(String mainSchemaPath) throws Exception;
    
    /**
     * Get parsing statistics
     */
    ParseStatistics getStatistics();
    
    /**
     * Get error report
     */
    Map<String, List<String>> getErrorReport();
    
    /**
     * Clear cache
     */
    void clearCache();
}
