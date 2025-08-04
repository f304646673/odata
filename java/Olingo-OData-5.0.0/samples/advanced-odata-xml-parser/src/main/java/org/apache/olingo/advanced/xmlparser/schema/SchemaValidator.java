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

import java.util.HashMap;
import java.util.Map;

import org.apache.olingo.advanced.xmlparser.core.OperationResult;
import org.apache.olingo.advanced.xmlparser.core.OperationType;
import org.apache.olingo.advanced.xmlparser.core.ResultType;
import org.apache.olingo.advanced.xmlparser.statistics.ParseStatistics;
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotations;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlSingleton;
import org.apache.olingo.server.core.SchemaBasedEdmProvider;

/**
 * Validates schema references and types to ensure all dependencies are satisfied.
 */
public class SchemaValidator implements ISchemaValidator {
    private final ParseStatistics statistics;
    
    public SchemaValidator(ParseStatistics statistics) {
        this.statistics = statistics;
    }
    
    /**
     * Validate all references in the loaded schemas
     */
    public void validateReferences(SchemaBasedEdmProvider provider) {
        try {
            if (provider == null || provider.getSchemas() == null) {
                return;
            }
            
            // Build a registry of all available types across all schemas
            TypeRegistry typeRegistry = new TypeRegistry(provider.getSchemas());
            
            // Validate each schema
            for (CsdlSchema schema : provider.getSchemas()) {
                validateSchemaReferences(schema, typeRegistry);
            }
        } catch (Exception e) {
            statistics.addError(ResultType.PARSING_ERROR,
                "Error during reference validation: " + e.getMessage(),
                "validateReferences");
        }
    }
    
    /**
     * Validate references within a single schema
     */
    private void validateSchemaReferences(CsdlSchema schema, TypeRegistry typeRegistry) {
        String namespace = schema.getNamespace();
        
        // Validate entity types
        if (schema.getEntityTypes() != null) {
            for (CsdlEntityType entityType : schema.getEntityTypes()) {
                validateEntityTypeReferences(entityType, namespace, typeRegistry);
            }
        }
        
        // Validate complex types
        if (schema.getComplexTypes() != null) {
            for (CsdlComplexType complexType : schema.getComplexTypes()) {
                validateComplexTypeReferences(complexType, namespace, typeRegistry);
            }
        }
        
        // Validate function references
        if (schema.getFunctions() != null) {
            for (CsdlFunction function : schema.getFunctions()) {
                validateFunctionReferences(function, namespace, typeRegistry);
            }
        }
        
        // Validate action references
        if (schema.getActions() != null) {
            for (CsdlAction action : schema.getActions()) {
                validateActionReferences(action, namespace, typeRegistry);
            }
        }
        
        // Validate entity container references
        if (schema.getEntityContainer() != null) {
            validateEntityContainerReferences(schema.getEntityContainer(), namespace, typeRegistry);
        }
        
        // Validate annotation targets
        if (schema.getAnnotationGroups() != null) {
            for (CsdlAnnotations annotations : schema.getAnnotationGroups()) {
                validateAnnotationTargets(annotations, namespace, typeRegistry);
            }
        }
    }
    
    private void validateEntityTypeReferences(CsdlEntityType entityType, String namespace, TypeRegistry typeRegistry) {
        String typeName = namespace + "." + entityType.getName();
        
        // Validate base type
        if (entityType.getBaseType() != null) {
            if (!typeRegistry.hasEntityType(entityType.getBaseType())) {
                statistics.addError(ResultType.MISSING_TYPE_REFERENCE, 
                    "Entity type base type not found: " + entityType.getBaseType(), 
                    typeName);
            }
        }
        
        // Validate property types
        if (entityType.getProperties() != null) {
            for (CsdlProperty property : entityType.getProperties()) {
                validatePropertyType(property, typeName, typeRegistry);
            }
        }
        
        // Validate navigation property types
        if (entityType.getNavigationProperties() != null) {
            for (CsdlNavigationProperty navProp : entityType.getNavigationProperties()) {
                if (!typeRegistry.hasEntityType(navProp.getType())) {
                    statistics.addError(ResultType.MISSING_TYPE_REFERENCE,
                        "Navigation property type not found: " + navProp.getType(),
                        typeName + "." + navProp.getName());
                }
            }
        }
    }
    
    private void validateComplexTypeReferences(CsdlComplexType complexType, String namespace, TypeRegistry typeRegistry) {
        String typeName = namespace + "." + complexType.getName();
        
        // Validate base type
        if (complexType.getBaseType() != null) {
            if (!typeRegistry.hasComplexType(complexType.getBaseType())) {
                statistics.addError(ResultType.MISSING_TYPE_REFERENCE,
                    "Complex type base type not found: " + complexType.getBaseType(),
                    typeName);
            }
        }
        
        // Validate property types
        if (complexType.getProperties() != null) {
            for (CsdlProperty property : complexType.getProperties()) {
                validatePropertyType(property, typeName, typeRegistry);
            }
        }
    }
    
    private void validatePropertyType(CsdlProperty property, String ownerTypeName, TypeRegistry typeRegistry) {
        String propertyType = property.getType();
        
        // Skip primitive types (start with Edm.)
        if (propertyType.startsWith("Edm.")) {
            return;
        }
        
        // Check if the type exists
        if (!typeRegistry.hasType(propertyType)) {
            statistics.addError(ResultType.MISSING_TYPE_REFERENCE,
                "Property type not found: " + propertyType,
                ownerTypeName + "." + property.getName());
        }
    }
    
    private void validateFunctionReferences(CsdlFunction function, String namespace, TypeRegistry typeRegistry) {
        String functionName = namespace + "." + function.getName();
        
        // Validate return type
        if (function.getReturnType() != null && function.getReturnType().getType() != null) {
            String returnType = function.getReturnType().getType();
            if (!returnType.startsWith("Edm.") && !typeRegistry.hasType(returnType)) {
                statistics.addError(ResultType.MISSING_TYPE_REFERENCE,
                    "Function return type not found: " + returnType,
                    functionName);
            }
        }
        
        // Validate parameter types
        if (function.getParameters() != null) {
            for (CsdlParameter parameter : function.getParameters()) {
                String paramType = parameter.getType();
                if (!paramType.startsWith("Edm.") && !typeRegistry.hasType(paramType)) {
                    statistics.addError(ResultType.MISSING_TYPE_REFERENCE,
                        "Function parameter type not found: " + paramType,
                        functionName + "." + parameter.getName());
                }
            }
        }
    }
    
    private void validateActionReferences(CsdlAction action, String namespace, TypeRegistry typeRegistry) {
        String actionName = namespace + "." + action.getName();
        
        // Validate parameter types
        if (action.getParameters() != null) {
            for (CsdlParameter parameter : action.getParameters()) {
                String paramType = parameter.getType();
                if (!paramType.startsWith("Edm.") && !typeRegistry.hasType(paramType)) {
                    statistics.addError(ResultType.MISSING_TYPE_REFERENCE,
                        "Action parameter type not found: " + paramType,
                        actionName + "." + parameter.getName());
                }
            }
        }
    }
    
    private void validateEntityContainerReferences(CsdlEntityContainer container, String namespace, TypeRegistry typeRegistry) {
        String containerName = namespace + "." + container.getName();
        
        // Validate entity sets
        if (container.getEntitySets() != null) {
            for (CsdlEntitySet entitySet : container.getEntitySets()) {
                if (!typeRegistry.hasEntityType(entitySet.getType())) {
                    statistics.addError(ResultType.MISSING_TYPE_REFERENCE,
                        "Entity set type not found: " + entitySet.getType(),
                        containerName + "." + entitySet.getName());
                }
            }
        }
        
        // Validate singletons
        if (container.getSingletons() != null) {
            for (CsdlSingleton singleton : container.getSingletons()) {
                if (!typeRegistry.hasEntityType(singleton.getType())) {
                    statistics.addError(ResultType.MISSING_TYPE_REFERENCE,
                        "Singleton type not found: " + singleton.getType(),
                        containerName + "." + singleton.getName());
                }
            }
        }
        
        // Validate function imports
        if (container.getFunctionImports() != null) {
            for (CsdlFunctionImport functionImport : container.getFunctionImports()) {
                if (!typeRegistry.hasFunction(functionImport.getFunction())) {
                    statistics.addError(ResultType.MISSING_TYPE_REFERENCE,
                        "Function import function not found: " + functionImport.getFunction(),
                        containerName + "." + functionImport.getName());
                }
            }
        }
        
        // Validate action imports
        if (container.getActionImports() != null) {
            for (CsdlActionImport actionImport : container.getActionImports()) {
                if (!typeRegistry.hasAction(actionImport.getAction())) {
                    statistics.addError(ResultType.MISSING_TYPE_REFERENCE,
                        "Action import action not found: " + actionImport.getAction(),
                        containerName + "." + actionImport.getName());
                }
            }
        }
    }
    
    private void validateAnnotationTargets(CsdlAnnotations annotations, String namespace, TypeRegistry typeRegistry) {
        String target = annotations.getTarget();
        
        // Parse and validate annotation target
        if (target != null && !target.isEmpty()) {
            if (!typeRegistry.hasTarget(target)) {
                statistics.addError(ResultType.MISSING_ANNOTATION_TARGET,
                    "Annotation target not found: " + target,
                    namespace);
            }
        }
    }
    
    /**
     * Validate compatibility between existing and new schema providers
     */
    @Override
    public OperationResult validateCompatibility(SchemaBasedEdmProvider existingProvider, SchemaBasedEdmProvider newProvider) {
        OperationResult result = new OperationResult(OperationType.VALIDATION);
        
        try {
            // Build maps of existing schemas by namespace
            Map<String, CsdlSchema> existingSchemas = new HashMap<>();
            for (CsdlSchema schema : existingProvider.getSchemas()) {
                existingSchemas.put(schema.getNamespace(), schema);
            }
            
            // Validate each new schema against existing ones
            for (CsdlSchema newSchema : newProvider.getSchemas()) {
                String namespace = newSchema.getNamespace();
                
                if (existingSchemas.containsKey(namespace)) {
                    // Schema with same namespace exists, check for conflicts
                    CsdlSchema existingSchema = existingSchemas.get(namespace);
                    validateSchemaCompatibility(existingSchema, newSchema, result);
                } else {
                    // New schema namespace, validate internal consistency
                    result.addInfo(ResultType.SUCCESS, "New schema namespace found: " + namespace);
                    validateNewSchemaConsistency(newSchema, result);
                }
            }
            
        } catch (Exception e) {
            result.addError(ResultType.VALIDATION_FAILED, "Compatibility validation failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Validate compatibility between two schemas with the same namespace
     */
    private void validateSchemaCompatibility(CsdlSchema existing, CsdlSchema newSchema, OperationResult result) {
        String namespace = existing.getNamespace();
        boolean hasConflicts = false;
        
        // Check entity types
        if (newSchema.getEntityTypes() != null) {
            for (CsdlEntityType newEntityType : newSchema.getEntityTypes()) {
                if (hasEntityType(existing, newEntityType.getName())) {
                    CsdlEntityType existingType = getEntityType(existing, newEntityType.getName());
                    if (!areEntityTypesCompatible(existingType, newEntityType)) {
                        result.addError(ResultType.TYPE_MISMATCH, "Entity type '" + newEntityType.getName() + "' has incompatible definition in namespace " + namespace);
                        hasConflicts = true;
                    } else {
                        result.addWarning(ResultType.TYPE_OVERRIDDEN, "Entity type '" + newEntityType.getName() + "' already exists in namespace " + namespace);
                    }
                }
            }
        }
        
        // Check complex types
        if (newSchema.getComplexTypes() != null) {
            for (CsdlComplexType newComplexType : newSchema.getComplexTypes()) {
                if (hasComplexType(existing, newComplexType.getName())) {
                    CsdlComplexType existingType = getComplexType(existing, newComplexType.getName());
                    if (!areComplexTypesCompatible(existingType, newComplexType)) {
                        result.addError(ResultType.TYPE_MISMATCH, "Complex type '" + newComplexType.getName() + "' has incompatible definition in namespace " + namespace);
                        hasConflicts = true;
                    } else {
                        result.addWarning(ResultType.TYPE_OVERRIDDEN, "Complex type '" + newComplexType.getName() + "' already exists in namespace " + namespace);
                    }
                }
            }
        }
        
        // Check enum types
        if (newSchema.getEnumTypes() != null) {
            for (org.apache.olingo.commons.api.edm.provider.CsdlEnumType newEnumType : newSchema.getEnumTypes()) {
                if (hasEnumType(existing, newEnumType.getName())) {
                    result.addWarning(ResultType.DUPLICATE_ELEMENT, "Enum type '" + newEnumType.getName() + "' already exists in namespace " + namespace);
                }
            }
        }
        
        // Check actions
        if (newSchema.getActions() != null) {
            for (CsdlAction newAction : newSchema.getActions()) {
                if (hasAction(existing, newAction.getName())) {
                    result.addWarning(ResultType.DUPLICATE_ELEMENT, "Action '" + newAction.getName() + "' already exists in namespace " + namespace);
                }
            }
        }
        
        // Add overall compatibility warning if conflicts were found
        if (hasConflicts) {
            result.addError(ResultType.MERGE_CONFLICT, "Schema merge conflict detected in namespace " + namespace);
        } else if (result.hasWarnings()) {
            result.addWarning(ResultType.COMPATIBILITY_WARNING, "Schema compatibility warnings found in namespace " + namespace);
        }
        
        // Check functions
        if (newSchema.getFunctions() != null) {
            for (CsdlFunction newFunction : newSchema.getFunctions()) {
                if (hasFunction(existing, newFunction.getName())) {
                    result.addWarning("Function '" + newFunction.getName() + "' already exists in namespace " + namespace);
                }
            }
        }
        
        // Check entity container
        if (newSchema.getEntityContainer() != null) {
            if (existing.getEntityContainer() != null) {
                result.addError(ResultType.VALIDATION_FAILED, "Entity container already exists in namespace " + namespace + ". Only one entity container per namespace is allowed.");
            }
        }
    }
    
    /**
     * Validate internal consistency of a new schema
     */
    private void validateNewSchemaConsistency(CsdlSchema schema, OperationResult result) {
        String namespace = schema.getNamespace();
        
        // Check if schema is empty
        boolean isEmpty = (schema.getEntityTypes() == null || schema.getEntityTypes().isEmpty()) &&
                         (schema.getComplexTypes() == null || schema.getComplexTypes().isEmpty()) &&
                         (schema.getEnumTypes() == null || schema.getEnumTypes().isEmpty()) &&
                         (schema.getActions() == null || schema.getActions().isEmpty()) &&
                         (schema.getFunctions() == null || schema.getFunctions().isEmpty()) &&
                         (schema.getEntityContainer() == null);
        
        if (isEmpty) {
            result.addError(ResultType.SCHEMA_EMPTY, "Schema " + namespace + " is empty and contains no types or operations");
            return;
        }
        
        // Use existing type registry validation logic
        try {
            TypeRegistry typeRegistry = new TypeRegistry(java.util.Arrays.asList(schema));
            validateSchemaReferences(schema, typeRegistry);
            result.addInfo(ResultType.SUCCESS, "Schema " + namespace + " passed internal consistency validation");
        } catch (Exception e) {
            result.addError(ResultType.VALIDATION_FAILED, "Schema " + namespace + " failed internal consistency validation: " + e.getMessage());
        }
    }
    
    // Helper methods to check existence of types
    private boolean hasEntityType(CsdlSchema schema, String name) {
        return schema.getEntityTypes() != null && 
               schema.getEntityTypes().stream().anyMatch(et -> et.getName().equals(name));
    }
    
    private boolean hasComplexType(CsdlSchema schema, String name) {
        return schema.getComplexTypes() != null && 
               schema.getComplexTypes().stream().anyMatch(ct -> ct.getName().equals(name));
    }
    
    private boolean hasEnumType(CsdlSchema schema, String name) {
        return schema.getEnumTypes() != null && 
               schema.getEnumTypes().stream().anyMatch(et -> et.getName().equals(name));
    }
    
    private boolean hasAction(CsdlSchema schema, String name) {
        return schema.getActions() != null && 
               schema.getActions().stream().anyMatch(a -> a.getName().equals(name));
    }
    
    private boolean hasFunction(CsdlSchema schema, String name) {
        return schema.getFunctions() != null && 
               schema.getFunctions().stream().anyMatch(f -> f.getName().equals(name));
    }
    
    // Helper methods to get types by name
    private CsdlEntityType getEntityType(CsdlSchema schema, String name) {
        if (schema.getEntityTypes() == null) return null;
        return schema.getEntityTypes().stream()
                    .filter(et -> et.getName().equals(name))
                    .findFirst()
                    .orElse(null);
    }
    
    private CsdlComplexType getComplexType(CsdlSchema schema, String name) {
        if (schema.getComplexTypes() == null) return null;
        return schema.getComplexTypes().stream()
                    .filter(ct -> ct.getName().equals(name))
                    .findFirst()
                    .orElse(null);
    }
    
    // Helper methods to check type compatibility
    private boolean areEntityTypesCompatible(CsdlEntityType existing, CsdlEntityType newType) {
        // Simple compatibility check - just check if both have keys or both don't
        if (existing.getKey() == null && newType.getKey() == null) return true;
        if (existing.getKey() == null || newType.getKey() == null) return false;
        
        // For simplicity, consider them compatible if both have keys
        // A more sophisticated check would compare property names, types, etc.
        return true;
    }
    
    private boolean areComplexTypesCompatible(CsdlComplexType existing, CsdlComplexType newType) {
        // Simple compatibility check - just check if they have the same base type
        if (existing.getBaseType() == null && newType.getBaseType() == null) return true;
        if (existing.getBaseType() == null || newType.getBaseType() == null) return false;
        
        return existing.getBaseType().equals(newType.getBaseType());
    }
}
