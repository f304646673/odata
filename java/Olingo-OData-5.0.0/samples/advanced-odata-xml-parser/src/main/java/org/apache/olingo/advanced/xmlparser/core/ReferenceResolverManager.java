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

import java.io.InputStream;
import java.util.Set;
import org.apache.olingo.server.core.ReferenceResolver;

/**
 * Interface for resolving schema references
 */
public interface ReferenceResolverManager {
    
    /**
     * Add a reference resolver
     */
    void addReferenceResolver(ReferenceResolver resolver);
    
    /**
     * Resolve a reference to an input stream
     */
    InputStream resolveReference(String referencePath) throws Exception;
    
    /**
     * Extract references from XML file
     */
    Set<String> extractReferencesFromXml(String schemaPath) throws Exception;
}
