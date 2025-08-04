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
package org.apache.olingo.advanced.xmlparser.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.olingo.advanced.xmlparser.core.SchemaProcessor;
import org.apache.olingo.advanced.xmlparser.statistics.ParseStatistics;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.server.core.SchemaBasedEdmProvider;

/**
 * Implementation that combines schema merger and comparator functionality
 */
public class SchemaProcessorImpl implements SchemaProcessor {
    private final SchemaMerger schemaMerger;
    private final SchemaComparator schemaComparator;
    
    public SchemaProcessorImpl(ParseStatistics statistics, Map<String, List<String>> errorReport) {
        this.schemaComparator = new SchemaComparator();
        this.schemaMerger = new SchemaMerger(schemaComparator, statistics, errorReport);
    }
    
    @Override
    public void copySchemas(SchemaBasedEdmProvider source, SchemaBasedEdmProvider target) throws Exception {
        schemaMerger.copySchemas(source, target);
    }
    
    @Override
    public CsdlSchema mergeSchemas(CsdlSchema existing, CsdlSchema source, String namespace) throws Exception {
        return schemaMerger.mergeSchemas(existing, source, namespace);
    }
    
    @Override
    public boolean areSchemasIdentical(CsdlSchema schema1, CsdlSchema schema2) {
        return schemaComparator.areSchemasIdentical(schema1, schema2);
    }
}
