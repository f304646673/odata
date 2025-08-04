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

import org.apache.olingo.advanced.xmlparser.statistics.ParseStatistics;
import org.apache.olingo.advanced.xmlparser.statistics.ErrorType;
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
            statistics.addError(ErrorType.PARSING_ERROR,
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
                statistics.addError(ErrorType.MISSING_TYPE_REFERENCE, 
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
                    statistics.addError(ErrorType.MISSING_TYPE_REFERENCE,
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
                statistics.addError(ErrorType.MISSING_TYPE_REFERENCE,
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
            statistics.addError(ErrorType.MISSING_TYPE_REFERENCE,
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
                statistics.addError(ErrorType.MISSING_TYPE_REFERENCE,
                    "Function return type not found: " + returnType,
                    functionName);
            }
        }
        
        // Validate parameter types
        if (function.getParameters() != null) {
            for (CsdlParameter parameter : function.getParameters()) {
                String paramType = parameter.getType();
                if (!paramType.startsWith("Edm.") && !typeRegistry.hasType(paramType)) {
                    statistics.addError(ErrorType.MISSING_TYPE_REFERENCE,
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
                    statistics.addError(ErrorType.MISSING_TYPE_REFERENCE,
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
                    statistics.addError(ErrorType.MISSING_TYPE_REFERENCE,
                        "Entity set type not found: " + entitySet.getType(),
                        containerName + "." + entitySet.getName());
                }
            }
        }
        
        // Validate singletons
        if (container.getSingletons() != null) {
            for (CsdlSingleton singleton : container.getSingletons()) {
                if (!typeRegistry.hasEntityType(singleton.getType())) {
                    statistics.addError(ErrorType.MISSING_TYPE_REFERENCE,
                        "Singleton type not found: " + singleton.getType(),
                        containerName + "." + singleton.getName());
                }
            }
        }
        
        // Validate function imports
        if (container.getFunctionImports() != null) {
            for (CsdlFunctionImport functionImport : container.getFunctionImports()) {
                if (!typeRegistry.hasFunction(functionImport.getFunction())) {
                    statistics.addError(ErrorType.MISSING_TYPE_REFERENCE,
                        "Function import function not found: " + functionImport.getFunction(),
                        containerName + "." + functionImport.getName());
                }
            }
        }
        
        // Validate action imports
        if (container.getActionImports() != null) {
            for (CsdlActionImport actionImport : container.getActionImports()) {
                if (!typeRegistry.hasAction(actionImport.getAction())) {
                    statistics.addError(ErrorType.MISSING_TYPE_REFERENCE,
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
                statistics.addError(ErrorType.MISSING_ANNOTATION_TARGET,
                    "Annotation target not found: " + target,
                    namespace);
            }
        }
    }
}
