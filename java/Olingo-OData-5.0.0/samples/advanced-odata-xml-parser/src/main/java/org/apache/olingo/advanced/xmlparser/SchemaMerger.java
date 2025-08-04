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
package org.apache.olingo.advanced.xmlparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.server.core.SchemaBasedEdmProvider;

/**
 * Handles merging of schemas and copying schemas between providers.
 */
public class SchemaMerger {
    private final SchemaComparator comparator;
    private final ParseStatistics statistics;
    private final Map<String, List<String>> errorReport;
    
    public SchemaMerger(SchemaComparator comparator, ParseStatistics statistics, Map<String, List<String>> errorReport) {
        this.comparator = comparator;
        this.statistics = statistics;
        this.errorReport = errorReport;
    }
    
    /**
     * Copy schemas from source provider to target provider using reflection
     * Merges schemas with the same namespace, detecting conflicts
     */
    public void copySchemas(SchemaBasedEdmProvider source, SchemaBasedEdmProvider target) throws Exception {
        // Create a map of existing schemas by namespace for efficient lookup
        Map<String, CsdlSchema> existingSchemas = new HashMap<>();
        for (CsdlSchema existingSchema : target.getSchemas()) {
            existingSchemas.put(existingSchema.getNamespace(), existingSchema);
        }
        
        // Process each schema from source
        for (CsdlSchema sourceSchema : source.getSchemas()) {
            String namespace = sourceSchema.getNamespace();
            
            if (existingSchemas.containsKey(namespace)) {
                CsdlSchema existingSchema = existingSchemas.get(namespace);
                
                // Check if schemas are identical
                boolean identical = comparator.areSchemasIdentical(existingSchema, sourceSchema);
                if (identical) {
                    // Schemas are identical, skip adding
                    continue;
                } else {
                    // Schemas have same namespace but different content - attempt to merge
                    try {
                        CsdlSchema mergedSchema = mergeSchemas(existingSchema, sourceSchema, namespace);
                        
                        // Remove the old schema and add the merged one
                        removeSchemaUsingReflection(target, existingSchema);
                        addSchemaUsingReflection(target, mergedSchema);
                        existingSchemas.put(namespace, mergedSchema);
                    } catch (Exception e) {
                        // If merge fails, it's a conflict
                        String error = String.format("Schema merge conflict in namespace '%s': %s", 
                            namespace, e.getMessage());
                        statistics.addError(ErrorType.SCHEMA_MERGE_CONFLICT, error, namespace);
                        errorReport.computeIfAbsent(namespace, k -> new ArrayList<>()).add(error);
                        throw e;
                    }
                }
            } else {
                // New namespace, add directly
                addSchemaUsingReflection(target, sourceSchema);
                existingSchemas.put(namespace, sourceSchema);
            }
        }
        
        // Copy references from source to target
        try {
            // Note: EdmxReference doesn't have accessible references in this API version
            // This is a placeholder for reference copying if needed
        } catch (Exception e) {
            // References might not be accessible, continue without them
        }
    }
    
    /**
     * Merge two schemas with the same namespace, detecting conflicts
     */
    public CsdlSchema mergeSchemas(CsdlSchema existing, CsdlSchema source, String namespace) throws Exception {
        // Create a new schema to hold merged content
        CsdlSchema merged = new CsdlSchema();
        merged.setNamespace(namespace);
        merged.setAlias(existing.getAlias() != null ? existing.getAlias() : source.getAlias());
        
        // Track element names to detect conflicts
        Set<String> entityTypeNames = new HashSet<>();
        Set<String> complexTypeNames = new HashSet<>();
        Set<String> enumTypeNames = new HashSet<>();
        Set<String> typeDefinitionNames = new HashSet<>();
        Set<String> actionNames = new HashSet<>();
        Set<String> functionNames = new HashSet<>();
        Set<String> containerNames = new HashSet<>();
        
        // Copy all elements from existing schema
        if (existing.getEntityTypes() != null) {
            for (CsdlEntityType entityType : existing.getEntityTypes()) {
                merged.getEntityTypes().add(entityType);
                entityTypeNames.add(entityType.getName());
            }
        }
        
        if (existing.getComplexTypes() != null) {
            for (CsdlComplexType complexType : existing.getComplexTypes()) {
                merged.getComplexTypes().add(complexType);
                complexTypeNames.add(complexType.getName());
            }
        }
        
        if (existing.getEnumTypes() != null) {
            for (CsdlEnumType enumType : existing.getEnumTypes()) {
                merged.getEnumTypes().add(enumType);
                enumTypeNames.add(enumType.getName());
            }
        }
        
        if (existing.getTypeDefinitions() != null) {
            for (CsdlTypeDefinition typeDef : existing.getTypeDefinitions()) {
                merged.getTypeDefinitions().add(typeDef);
                typeDefinitionNames.add(typeDef.getName());
            }
        }
        
        if (existing.getActions() != null) {
            for (CsdlAction action : existing.getActions()) {
                merged.getActions().add(action);
                actionNames.add(action.getName());
            }
        }
        
        if (existing.getFunctions() != null) {
            for (CsdlFunction function : existing.getFunctions()) {
                merged.getFunctions().add(function);
                functionNames.add(function.getName());
            }
        }
        
        if (existing.getEntityContainer() != null) {
            merged.setEntityContainer(existing.getEntityContainer());
            containerNames.add(existing.getEntityContainer().getName());
        }
        
        // Add elements from source schema, checking for conflicts
        if (source.getEntityTypes() != null) {
            for (CsdlEntityType entityType : source.getEntityTypes()) {
                if (entityTypeNames.contains(entityType.getName())) {
                    String error = String.format(
                        "Conflicting EntityType '%s' found in namespace '%s' during schema merge",
                        entityType.getName(), namespace);
                    statistics.addError(ErrorType.SCHEMA_MERGE_CONFLICT, error, namespace);
                    errorReport.computeIfAbsent(namespace, k -> new ArrayList<>()).add(error);
                    throw new IllegalArgumentException(error);
                }
                merged.getEntityTypes().add(entityType);
                entityTypeNames.add(entityType.getName());
            }
        }
        
        if (source.getComplexTypes() != null) {
            for (CsdlComplexType complexType : source.getComplexTypes()) {
                if (complexTypeNames.contains(complexType.getName())) {
                    String error = String.format(
                        "Conflicting ComplexType '%s' found in namespace '%s' during schema merge",
                        complexType.getName(), namespace);
                    statistics.addError(ErrorType.SCHEMA_MERGE_CONFLICT, error, namespace);
                    errorReport.computeIfAbsent(namespace, k -> new ArrayList<>()).add(error);
                    throw new IllegalArgumentException(error);
                }
                merged.getComplexTypes().add(complexType);
                complexTypeNames.add(complexType.getName());
            }
        }
        
        if (source.getEnumTypes() != null) {
            for (CsdlEnumType enumType : source.getEnumTypes()) {
                if (enumTypeNames.contains(enumType.getName())) {
                    String error = String.format(
                        "Conflicting EnumType '%s' found in namespace '%s' during schema merge",
                        enumType.getName(), namespace);
                    statistics.addError(ErrorType.SCHEMA_MERGE_CONFLICT, error, namespace);
                    errorReport.computeIfAbsent(namespace, k -> new ArrayList<>()).add(error);
                    throw new IllegalArgumentException(error);
                }
                merged.getEnumTypes().add(enumType);
                enumTypeNames.add(enumType.getName());
            }
        }
        
        if (source.getTypeDefinitions() != null) {
            for (CsdlTypeDefinition typeDef : source.getTypeDefinitions()) {
                if (typeDefinitionNames.contains(typeDef.getName())) {
                    String error = String.format(
                        "Conflicting TypeDefinition '%s' found in namespace '%s' during schema merge",
                        typeDef.getName(), namespace);
                    statistics.addError(ErrorType.SCHEMA_MERGE_CONFLICT, error, namespace);
                    errorReport.computeIfAbsent(namespace, k -> new ArrayList<>()).add(error);
                    throw new IllegalArgumentException(error);
                }
                merged.getTypeDefinitions().add(typeDef);
                typeDefinitionNames.add(typeDef.getName());
            }
        }
        
        if (source.getActions() != null) {
            for (CsdlAction action : source.getActions()) {
                if (actionNames.contains(action.getName())) {
                    String error = String.format(
                        "Conflicting Action '%s' found in namespace '%s' during schema merge",
                        action.getName(), namespace);
                    statistics.addError(ErrorType.SCHEMA_MERGE_CONFLICT, error, namespace);
                    errorReport.computeIfAbsent(namespace, k -> new ArrayList<>()).add(error);
                    throw new IllegalArgumentException(error);
                }
                merged.getActions().add(action);
                actionNames.add(action.getName());
            }
        }
        
        if (source.getFunctions() != null) {
            for (CsdlFunction function : source.getFunctions()) {
                if (functionNames.contains(function.getName())) {
                    String error = String.format(
                        "Conflicting Function '%s' found in namespace '%s' during schema merge",
                        function.getName(), namespace);
                    statistics.addError(ErrorType.SCHEMA_MERGE_CONFLICT, error, namespace);
                    errorReport.computeIfAbsent(namespace, k -> new ArrayList<>()).add(error);
                    throw new IllegalArgumentException(error);
                }
                merged.getFunctions().add(function);
                functionNames.add(function.getName());
            }
        }
        
        if (source.getEntityContainer() != null) {
            if (containerNames.contains(source.getEntityContainer().getName())) {
                String error = String.format(
                    "Conflicting EntityContainer '%s' found in namespace '%s' during schema merge",
                    source.getEntityContainer().getName(), namespace);
                statistics.addError(ErrorType.SCHEMA_MERGE_CONFLICT, error, namespace);
                errorReport.computeIfAbsent(namespace, k -> new ArrayList<>()).add(error);
                throw new IllegalArgumentException(error);
            }
            // If there's no existing container, set this one
            // If there is an existing container, we would need to merge them (complex scenario)
            if (merged.getEntityContainer() == null) {
                merged.setEntityContainer(source.getEntityContainer());
            }
        }
        
        return merged;
    }
    
    /**
     * Remove schema using reflection to access internal schema list
     */
    private void removeSchemaUsingReflection(SchemaBasedEdmProvider provider, CsdlSchema schema) 
            throws Exception {
        try {
            // Try different possible field names for the schemas list
            java.lang.reflect.Field schemasField = null;
            String[] possibleFieldNames = {"schemas", "schemaList", "csdlSchemas", "edmSchemas"};
            
            for (String fieldName : possibleFieldNames) {
                try {
                    schemasField = SchemaBasedEdmProvider.class.getDeclaredField(fieldName);
                    break;
                } catch (NoSuchFieldException e) {
                    // Try next field name
                }
            }
            
            if (schemasField == null) {
                // If we can't find the field, let's list all fields for debugging
                java.lang.reflect.Field[] allFields = SchemaBasedEdmProvider.class.getDeclaredFields();
                StringBuilder fieldNames = new StringBuilder();
                for (java.lang.reflect.Field field : allFields) {
                    if (fieldNames.length() > 0) fieldNames.append(", ");
                    fieldNames.append(field.getName()).append(":").append(field.getType().getSimpleName());
                }
                throw new IllegalStateException("Could not find schemas field. Available fields: " + fieldNames.toString());
            }
            
            schemasField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<CsdlSchema> schemas = (List<CsdlSchema>) schemasField.get(provider);
            schemas.remove(schema);
        } catch (Exception e) {
            // Fallback: create new provider without the schema (not ideal but works)
            throw new IllegalStateException("Could not remove schema during merge: " + e.getMessage(), e);
        }
    }
    
    /**
     * Add reference using reflection to access protected method
     */
    private void addReferenceUsingReflection(SchemaBasedEdmProvider provider, EdmxReference reference) 
            throws Exception {
        try {
            java.lang.reflect.Method method = SchemaBasedEdmProvider.class.getDeclaredMethod("addReference", EdmxReference.class);
            method.setAccessible(true);
            method.invoke(provider, reference);
        } catch (Exception e) {
            throw e;
        }
    }
    
    /**
     * Add schema using reflection to access protected method
     */
    private void addSchemaUsingReflection(SchemaBasedEdmProvider provider, CsdlSchema schema) 
            throws Exception {
        try {
            java.lang.reflect.Method method = SchemaBasedEdmProvider.class.getDeclaredMethod("addSchema", CsdlSchema.class);
            method.setAccessible(true);
            method.invoke(provider, schema);
        } catch (Exception e) {
            throw e;
        }
    }
}
