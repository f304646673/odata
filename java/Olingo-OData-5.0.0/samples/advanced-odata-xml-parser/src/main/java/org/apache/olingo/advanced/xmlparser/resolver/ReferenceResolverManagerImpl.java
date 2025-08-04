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
package org.apache.olingo.advanced.xmlparser.resolver;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.olingo.advanced.xmlparser.core.ReferenceResolverManager;
import org.apache.olingo.server.core.ReferenceResolver;

/**
 * Implementation of reference resolver management using verified logic from AdvancedMetadataParser
 */
public class ReferenceResolverManagerImpl implements ReferenceResolverManager {
    
    private final ReferenceResolverManager delegateManager;
    private final Map<String, ReferenceResolver> resolvers = new ConcurrentHashMap<>();
    
    public ReferenceResolverManagerImpl() {
        // Initialize with the original manager for backward compatibility
        this.delegateManager = new org.apache.olingo.advanced.xmlparser.ReferenceResolverManager();
        
        // Register default resolvers
        registerResolver("classpath", new ClassPathReferenceResolver());
        registerResolver("file", new FileSystemReferenceResolver());
        registerResolver("http", new UrlReferenceResolver());
        registerResolver("https", new UrlReferenceResolver());
    }
    
    @Override
    public void registerResolver(String scheme, ReferenceResolver resolver) {
        resolvers.put(scheme, resolver);
    }
    
    @Override
    public ReferenceResolver getResolver(String scheme) {
        return resolvers.get(scheme);
    }
    
    @Override
    public InputStream resolveReference(URI uri) throws Exception {
        String scheme = uri.getScheme();
        if (scheme == null) {
            scheme = "file"; // Default to file scheme
        }
        
        ReferenceResolver resolver = getResolver(scheme);
        if (resolver != null) {
            return resolver.resolveReference(uri, null, null);
        }
        
        throw new IllegalArgumentException("No resolver found for scheme: " + scheme);
    }
    
    @Override
    public boolean canResolve(URI uri) {
        String scheme = uri.getScheme();
        if (scheme == null) {
            scheme = "file";
        }
        return resolvers.containsKey(scheme);
    }
}
