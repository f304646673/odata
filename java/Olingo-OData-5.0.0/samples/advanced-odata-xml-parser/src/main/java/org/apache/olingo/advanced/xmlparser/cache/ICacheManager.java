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
package org.apache.olingo.advanced.xmlparser.cache;

import org.apache.olingo.server.core.SchemaBasedEdmProvider;

/**
 * Interface for cache management operations
 */
public interface ICacheManager {
    
    /**
     * Get cached provider for a file
     */
    SchemaBasedEdmProvider getCachedProvider(String filePath);
    
    /**
     * Cache a provider for a file
     */
    void cacheProvider(String filePath, SchemaBasedEdmProvider provider);
    
    /**
     * Check if a file is cached
     */
    boolean isCached(String filePath);
    
    /**
     * Clear the cache
     */
    void clearCache();
    
    /**
     * Get cache size
     */
    int getCacheSize();
    
    /**
     * Set whether caching is enabled
     */
    void setEnabled(boolean enabled);
    
    /**
     * Check if caching is enabled
     */
    boolean isEnabled();
    
    /**
     * Generate cache key for a file path
     */
    String generateCacheKey(String filePath);
    
    /**
     * Check if cache contains a specific cache key
     */
    boolean containsKey(String cacheKey);
}
