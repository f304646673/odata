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
package org.apache.olingo.advanced.xmlparser.provider;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.commons.api.edmx.EdmxReferenceInclude;
import org.apache.olingo.server.core.MetadataParser;
import org.apache.olingo.server.core.SchemaBasedEdmProvider;

/**
 * Utility class for SchemaBasedEdmProvider operations
 */
public class ProviderUtils {
    
    /**
     * Add reference using reflection to access protected method
     */
    public static void addReferenceUsingReflection(SchemaBasedEdmProvider provider, EdmxReference reference) 
            throws Exception {
        try {
            java.lang.reflect.Method method = SchemaBasedEdmProvider.class.getDeclaredMethod("addReference", EdmxReference.class);
            method.setAccessible(true);
            method.invoke(provider, reference);
        } catch (Exception e) {
            throw new RuntimeException("Failed to add reference to provider", e);
        }
    }
    
    /**
     * Create a provider for a reference dependency
     */
    public static SchemaBasedEdmProvider createProviderForReference(String referencePath, MetadataParser parser) 
            throws Exception {
        try {
            // Try to resolve the reference as a resource
            InputStream inputStream = ProviderUtils.class.getClassLoader().getResourceAsStream("schemas/" + referencePath);
            if (inputStream == null) {
                return null;
            }
            
            try {
                return parser.buildEdmProvider(new InputStreamReader(inputStream));
            } finally {
                inputStream.close();
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Add references from dependency graph to provider
     */
    public static void addReferencesToProvider(SchemaBasedEdmProvider provider, 
                                              Map<String, Set<String>> allDependencies,
                                              MetadataParser parser) throws Exception {
        for (Map.Entry<String, Set<String>> entry : allDependencies.entrySet()) {
            Set<String> dependencies = entry.getValue();
            
            for (String dependencyPath : dependencies) {
                // Create a reference provider for the dependency
                SchemaBasedEdmProvider dependencyProvider = createProviderForReference(dependencyPath, parser);
                if (dependencyProvider != null) {
                    // Extract namespace from the dependency provider and create EdmxReference
                    List<CsdlSchema> schemas = dependencyProvider.getSchemas();
                    for (CsdlSchema schema : schemas) {
                        // Create EdmxReference with includes
                        EdmxReference reference = new EdmxReference(URI.create(dependencyPath));
                        EdmxReferenceInclude include = new EdmxReferenceInclude(schema.getNamespace());
                        reference.addInclude(include);
                        
                        // Use reflection to add reference 
                        addReferenceUsingReflection(provider, reference);
                    }
                }
            }
        }
    }
}
