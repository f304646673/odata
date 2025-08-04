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

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.olingo.server.core.SchemaBasedEdmProvider;

/**
 * Manages caching of parsed schema providers with unique key generation.
 * Uses verified business logic from AdvancedMetadataParser.
 */
public class CacheManager implements ICacheManager {
    private final ConcurrentHashMap<String, SchemaBasedEdmProvider> providerCache = new ConcurrentHashMap<>();
    private final boolean enableCaching;
    
    public CacheManager(boolean enableCaching) {
        this.enableCaching = enableCaching;
    }
    
    /**
     * Get cached provider if exists and caching is enabled
     */
    public SchemaBasedEdmProvider getCachedProvider(String schemaPath) {
        if (!enableCaching) {
            return null;
        }
        String cacheKey = generateCacheKey(schemaPath);
        return providerCache.get(cacheKey);
    }
    
    /**
     * Cache provider if caching is enabled
     */
    public void cacheProvider(String schemaPath, SchemaBasedEdmProvider provider) {
        if (enableCaching && provider != null) {
            String cacheKey = generateCacheKey(schemaPath);
            providerCache.put(cacheKey, provider);
        }
    }
    
    /**
     * Clear cache
     */
    @Override
    public void clearCache() {
        providerCache.clear();
    }
    
    /**
     * Get cache size
     */
    @Override
    public int getCacheSize() {
        return providerCache.size();
    }
    
    /**
     * Check if caching is enabled
     */
    public boolean isCachingEnabled() {
        return enableCaching;
    }
    
    /**
     * Set caching enabled/disabled (for configuration changes)
     * Note: This is for compatibility, but actual enableCaching is final
     */
    @Override
    public void setEnabled(boolean enabled) {
        // This method exists for API compatibility but the actual
        // enableCaching field is final and set in constructor
        if (enabled != enableCaching) {
            // Log a warning that caching setting cannot be changed after construction
            System.err.println("Warning: Cache setting cannot be changed after CacheManager construction");
        }
    }
    
    /**
     * Generate cache key that includes path information to avoid conflicts (verified logic from AdvancedMetadataParser)
     */
    @Override
    public String generateCacheKey(String schemaPath) {
        try {
            // Use canonical path to ensure uniqueness for the same file referenced by different relative paths
            File file = new File(schemaPath);
            String canonicalPath = file.getCanonicalPath();
            
            // Normalize path separators for consistency
            return canonicalPath.replace("\\", "/");
        } catch (IOException e) {
            // Fallback to original logic if canonical path fails
            File file = new File(schemaPath);
            String fileName = file.getName();
            
            // For relative paths or complex paths, include parent directory to distinguish same filenames
            if (schemaPath.contains("/") || schemaPath.contains("\\")) {
                String parent = file.getParent();
                if (parent != null) {
                    // Normalize the parent path and combine with filename
                    parent = parent.replace("\\", "/");
                    if (parent.contains("/")) {
                        // Take last two path components to create unique key
                        String[] parts = parent.split("/");
                        if (parts.length >= 2) {
                            return parts[parts.length - 2] + "/" + parts[parts.length - 1] + "/" + fileName;
                        } else if (parts.length == 1) {
                            return parts[0] + "/" + fileName;
                        }
                    }
                    return parent + "/" + fileName;
                }
            }
            
            // Fallback to just filename for simple cases
            return fileName;
        }
    }
    
    /**
     * Check if cache contains key
     */
    @Override
    public boolean containsKey(String cacheKey) {
        return enableCaching && providerCache.containsKey(cacheKey);
    }
    
    @Override
    public boolean isCached(String filePath) {
        return containsKey(generateCacheKey(filePath));
    }
    
    @Override
    public boolean isEnabled() {
        return enableCaching;
    }
}
