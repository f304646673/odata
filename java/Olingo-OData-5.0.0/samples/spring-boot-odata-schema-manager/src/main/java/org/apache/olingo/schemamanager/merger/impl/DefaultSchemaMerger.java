package org.apache.olingo.schemamanager.merger.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlSingleton;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;
import org.apache.olingo.schemamanager.merger.SchemaMerger;

/**
 * Default implementation of SchemaMerger
 * Merges schemas with the same namespace and provides comprehensive conflict detection
 */
public class DefaultSchemaMerger implements SchemaMerger {
    
    @Override
    public MergeResult mergeSchemas(List<CsdlSchema> schemas) {
        return mergeSchemas(schemas, ConflictResolution.THROW_ERROR);
    }
    
    @Override
    public MergeResult mergeSchemas(List<CsdlSchema> schemas, ConflictResolution resolution) {
        System.out.println("DEBUG: Starting merge with " + schemas.size() + " schemas");
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        List<ConflictInfo> conflicts = new ArrayList<>();
        
        // Validate input
        if (schemas == null || schemas.isEmpty()) {
            errors.add("Schema list cannot be null or empty");
            return new MergeResult(new ArrayList<>(), warnings, errors, conflicts, false);
        }
        
        // Check for null schemas in the list
        for (int i = 0; i < schemas.size(); i++) {
            if (schemas.get(i) == null) {
                errors.add("Schema at index " + i + " is null");
                return new MergeResult(new ArrayList<>(), warnings, errors, conflicts, false);
            }
        }
        
        try {
            System.out.println("DEBUG: Starting schema grouping by namespace");
            // Group schemas by namespace
            Map<String, List<CsdlSchema>> schemasByNamespace = groupSchemasByNamespace(schemas);
            System.out.println("DEBUG: Grouped schemas into " + schemasByNamespace.size() + " namespaces");
            for (Map.Entry<String, List<CsdlSchema>> entry : schemasByNamespace.entrySet()) {
                System.out.println("DEBUG: Namespace '" + entry.getKey() + "' has " + entry.getValue().size() + " schemas");
            }
            
            List<CsdlSchema> mergedSchemas = new ArrayList<>();
            
            // Merge schemas within each namespace
            for (Map.Entry<String, List<CsdlSchema>> entry : schemasByNamespace.entrySet()) {
                String namespace = entry.getKey();
                List<CsdlSchema> namespacedSchemas = entry.getValue();
                
                System.out.println("DEBUG: Processing namespace '" + namespace + "' with " + namespacedSchemas.size() + " schemas");
                
                if (namespacedSchemas.size() == 1) {
                    // Single schema, no merging needed
                    System.out.println("DEBUG: Single schema, no merging needed for namespace '" + namespace + "'");
                    mergedSchemas.add(namespacedSchemas.get(0));
                } else {
                    // Multiple schemas with same namespace, need to merge
                    System.out.println("DEBUG: Multiple schemas detected for namespace '" + namespace + "', starting merge");
                    CsdlSchema mergedSchema = performMerge(namespacedSchemas, resolution, warnings, errors, conflicts, namespace);
                    if (mergedSchema != null) {
                        mergedSchemas.add(mergedSchema);
                    }
                }
            }
            
            // For THROW_ERROR strategy, fail if there are conflicts
            if (resolution == ConflictResolution.THROW_ERROR && !conflicts.isEmpty()) {
                for (ConflictInfo conflict : conflicts) {
                    errors.add("Conflict detected in namespace '" + conflict.getNamespace() + "': " + conflict.getDescription());
                }
                return new MergeResult(new ArrayList<>(), warnings, errors, conflicts, false);
            }
            
            boolean success = errors.isEmpty();
            return new MergeResult(mergedSchemas, warnings, errors, conflicts, success);
        } catch (Exception e) {
            // Print detailed error information for debugging
            System.err.println("=== MERGE ERROR DETAILS ===");
            System.err.println("Exception type: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            System.err.println("Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "null"));
            System.err.println("Stack trace:");
            e.printStackTrace();
            System.err.println("=== END ERROR DETAILS ===");
            errors.add("Merge failed: " + e.getMessage());
            return new MergeResult(new ArrayList<>(), warnings, errors, conflicts, false);
        }
    }
    
    /**
     * Group schemas by their namespace
     */
    private Map<String, List<CsdlSchema>> groupSchemasByNamespace(List<CsdlSchema> schemas) {
        Map<String, List<CsdlSchema>> schemasByNamespace = new HashMap<>();
        
        for (CsdlSchema schema : schemas) {
            String namespace = schema.getNamespace();
            schemasByNamespace.computeIfAbsent(namespace, k -> new ArrayList<>()).add(schema);
        }
        
        return schemasByNamespace;
    }
    
    private CsdlSchema performMerge(List<CsdlSchema> schemas, ConflictResolution resolution, 
                                   List<String> warnings, List<String> errors, List<ConflictInfo> conflicts, String namespace) {
        System.out.println("DEBUG: performMerge started for namespace '" + namespace + "' with " + schemas.size() + " schemas");
        CsdlSchema result = new CsdlSchema();
        result.setNamespace(schemas.get(0).getNamespace());
        result.setAlias(schemas.get(0).getAlias());
        
        // Merge all schema elements
        System.out.println("DEBUG: Starting mergeEntityTypes");
        mergeEntityTypes(schemas, result, resolution, warnings, errors, conflicts, namespace);
        System.out.println("DEBUG: Starting mergeComplexTypes");
        mergeComplexTypes(schemas, result, resolution, warnings, errors, conflicts, namespace);
        System.out.println("DEBUG: Starting mergeEnumTypes");
        mergeEnumTypes(schemas, result, resolution, warnings, errors, conflicts, namespace);
        System.out.println("DEBUG: Starting mergeActions");
        mergeActions(schemas, result, resolution, warnings, errors, conflicts, namespace);
        mergeFunctions(schemas, result, resolution, warnings, errors, conflicts, namespace);
        mergeTerms(schemas, result, resolution, warnings, errors, conflicts, namespace);
        mergeTypeDefinitions(schemas, result, resolution, warnings, errors, conflicts, namespace);
        mergeEntityContainers(schemas, result, resolution, warnings, errors, conflicts, namespace);
        
        return result;
    }
    
    private void mergeEntityTypes(List<CsdlSchema> schemas, CsdlSchema result, ConflictResolution resolution,
                                 List<String> warnings, List<String> errors, List<ConflictInfo> conflicts, String namespace) {
        System.out.println("DEBUG: mergeEntityTypes started");
        Map<String, CsdlEntityType> entityTypes = new HashMap<>();
        
        for (int i = 0; i < schemas.size(); i++) {
            CsdlSchema schema = schemas.get(i);
            System.out.println("DEBUG: Processing schema " + i + " with namespace '" + schema.getNamespace() + "'");
            if (schema.getEntityTypes() != null) {
                System.out.println("DEBUG: Schema " + i + " has " + schema.getEntityTypes().size() + " entity types");
                for (int j = 0; j < schema.getEntityTypes().size(); j++) {
                    CsdlEntityType entityType = schema.getEntityTypes().get(j);
                    String name = entityType.getName();
                    System.out.println("DEBUG: Processing entity type '" + name + "' from schema " + i);
                    if (entityTypes.containsKey(name)) {
                        System.out.println("DEBUG: Conflict detected for entity type '" + name + "', calling handleEntityTypeConflict");
                        handleEntityTypeConflict(name, entityTypes.get(name), entityType, resolution,
                                               warnings, errors, conflicts, entityTypes, namespace);
                    } else {
                        System.out.println("DEBUG: Adding new entity type '" + name + "'");
                        entityTypes.put(name, entityType);
                    }
                }
            } else {
                System.out.println("DEBUG: Schema " + i + " has no entity types");
            }
        }
        
        result.setEntityTypes(new ArrayList<>(entityTypes.values()));
    }
    
    private void handleEntityTypeConflict(String name, CsdlEntityType existing, CsdlEntityType conflicting,
                                        ConflictResolution resolution, List<String> warnings, List<String> errors,
                                        List<ConflictInfo> conflicts, Map<String, CsdlEntityType> entityTypes, String namespace) {
        ConflictInfo conflict = new ConflictInfo(ConflictType.ENTITY_TYPE, name,
                "EntityType '" + name + "' is defined in multiple schemas", existing, conflicting, namespace);
        conflicts.add(conflict);
        
        switch (resolution) {
            case THROW_ERROR:
                errors.add("Conflicting EntityType: " + name);
                break;
            case KEEP_FIRST:
                warnings.add("Keeping first definition of EntityType: " + name);
                break;
            case KEEP_LAST:
                entityTypes.put(name, conflicting);
                warnings.add("Using last definition of EntityType: " + name);
                break;
            case AUTO_MERGE:
                if (areEntityTypesCompatible(existing, conflicting)) {
                    entityTypes.put(name, mergeEntityTypes(existing, conflicting));
                    warnings.add("Auto-merged EntityType: " + name);
                } else {
                    errors.add("Cannot auto-merge incompatible EntityType: " + name);
                }
                break;
            case SKIP_CONFLICTS:
                warnings.add("Skipping conflicting EntityType: " + name);
                entityTypes.remove(name);
                break;
        }
    }
    
    private boolean areEntityTypesCompatible(CsdlEntityType type1, CsdlEntityType type2) {
        try {
            System.out.println("DEBUG: Checking compatibility between EntityTypes");
            System.out.println("DEBUG: Type1 - Name: " + type1.getName());
            try {
                String baseType1 = type1.getBaseType();
                System.out.println("DEBUG: Type1 - BaseType: " + baseType1);
            } catch (Exception e) {
                System.out.println("DEBUG: Error getting Type1 BaseType: " + e.getMessage());
                e.printStackTrace();
            }
            try {
                FullQualifiedName baseTypeFQN1 = type1.getBaseTypeFQN();
                System.out.println("DEBUG: Type1 - BaseTypeFQN: " + baseTypeFQN1);
            } catch (Exception e) {
                System.out.println("DEBUG: Error getting Type1 BaseTypeFQN: " + e.getMessage());
                e.printStackTrace();
            }
            
            System.out.println("DEBUG: Type2 - Name: " + type2.getName());
            try {
                String baseType2 = type2.getBaseType();
                System.out.println("DEBUG: Type2 - BaseType: " + baseType2);
            } catch (Exception e) {
                System.out.println("DEBUG: Error getting Type2 BaseType: " + e.getMessage());
                e.printStackTrace();
            }
            try {
                FullQualifiedName baseTypeFQN2 = type2.getBaseTypeFQN();
                System.out.println("DEBUG: Type2 - BaseTypeFQN: " + baseTypeFQN2);
            } catch (Exception e) {
                System.out.println("DEBUG: Error getting Type2 BaseTypeFQN: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Check if base types match
            String baseType1 = null;
            String baseType2 = null;
            try {
                baseType1 = type1.getBaseType();
                baseType2 = type2.getBaseType();
            } catch (Exception e) {
                System.err.println("Error getting base types: " + e.getMessage());
                e.printStackTrace();
                // If we can't get base types, consider them unequal
                return false;
            }
            
            if (!objectsEqual(baseType1, baseType2)) {
                System.out.println("DEBUG: Base types are different: '" + baseType1 + "' vs '" + baseType2 + "'");
                return false;
            }
            
            // Check if abstract flags match
            if (type1.isAbstract() != type2.isAbstract()) {
                System.out.println("DEBUG: Abstract flags are different");
                return false;
            }
            
            // Check if open types match
            if (type1.isOpenType() != type2.isOpenType()) {
                System.out.println("DEBUG: Open type flags are different");
                return false;
            }
            
            System.out.println("DEBUG: EntityTypes are compatible");
            return true;
        } catch (Exception e) {
            System.err.println("ERROR in areEntityTypesCompatible: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    private CsdlEntityType mergeEntityTypes(CsdlEntityType type1, CsdlEntityType type2) {
        CsdlEntityType merged = new CsdlEntityType();
        merged.setName(type1.getName());
        
        // Handle base type safely - only set if not null
        if (type1.getBaseType() != null) {
            merged.setBaseType(type1.getBaseType());
        } else if (type2.getBaseType() != null) {
            merged.setBaseType(type2.getBaseType());
        }
        
        merged.setAbstract(type1.isAbstract());
        merged.setOpenType(type1.isOpenType());
        
        // Merge properties
        Map<String, CsdlProperty> properties = new HashMap<>();
        if (type1.getProperties() != null) {
            for (CsdlProperty prop : type1.getProperties()) {
                properties.put(prop.getName(), prop);
            }
        }
        if (type2.getProperties() != null) {
            for (CsdlProperty prop : type2.getProperties()) {
                if (!properties.containsKey(prop.getName())) {
                    properties.put(prop.getName(), prop);
                }
            }
        }
        merged.setProperties(new ArrayList<>(properties.values()));
        
        // Merge keys (use first non-null)
        if (type1.getKey() != null) {
            merged.setKey(type1.getKey());
        } else if (type2.getKey() != null) {
            merged.setKey(type2.getKey());
        }
        
        return merged;
    }
    
    private void mergeComplexTypes(List<CsdlSchema> schemas, CsdlSchema result, ConflictResolution resolution,
                                  List<String> warnings, List<String> errors, List<ConflictInfo> conflicts, String namespace) {
        Map<String, CsdlComplexType> complexTypes = new HashMap<>();
        
        for (CsdlSchema schema : schemas) {
            if (schema.getComplexTypes() != null) {
                for (CsdlComplexType complexType : schema.getComplexTypes()) {
                    String name = complexType.getName();
                    if (complexTypes.containsKey(name)) {
                        handleComplexTypeConflict(name, complexTypes.get(name), complexType, resolution,
                                                warnings, errors, conflicts, complexTypes, namespace);
                    } else {
                        complexTypes.put(name, complexType);
                    }
                }
            }
        }
        
        result.setComplexTypes(new ArrayList<>(complexTypes.values()));
    }
    
    private void handleComplexTypeConflict(String name, CsdlComplexType existing, CsdlComplexType conflicting,
                                         ConflictResolution resolution, List<String> warnings, List<String> errors,
                                         List<ConflictInfo> conflicts, Map<String, CsdlComplexType> complexTypes, String namespace) {
        ConflictInfo conflict = new ConflictInfo(ConflictType.COMPLEX_TYPE, name,
                "ComplexType '" + name + "' is defined in multiple schemas", existing, conflicting, namespace);
        conflicts.add(conflict);
        
        switch (resolution) {
            case THROW_ERROR:
                errors.add("Conflicting ComplexType: " + name);
                break;
            case KEEP_FIRST:
                warnings.add("Keeping first definition of ComplexType: " + name);
                break;
            case KEEP_LAST:
                complexTypes.put(name, conflicting);
                warnings.add("Using last definition of ComplexType: " + name);
                break;
            case AUTO_MERGE:
                if (areComplexTypesCompatible(existing, conflicting)) {
                    complexTypes.put(name, mergeComplexTypes(existing, conflicting));
                    warnings.add("Auto-merged ComplexType: " + name);
                } else {
                    errors.add("Cannot auto-merge incompatible ComplexType: " + name);
                }
                break;
            case SKIP_CONFLICTS:
                warnings.add("Skipping conflicting ComplexType: " + name);
                complexTypes.remove(name);
                break;
        }
    }
    
    private boolean areComplexTypesCompatible(CsdlComplexType type1, CsdlComplexType type2) {
        return objectsEqual(type1.getBaseType(), type2.getBaseType()) &&
               type1.isAbstract() == type2.isAbstract() &&
               type1.isOpenType() == type2.isOpenType();
    }
    
    private CsdlComplexType mergeComplexTypes(CsdlComplexType type1, CsdlComplexType type2) {
        CsdlComplexType merged = new CsdlComplexType();
        merged.setName(type1.getName());
        
        // Handle base type safely - only set if not null
        if (type1.getBaseType() != null) {
            merged.setBaseType(type1.getBaseType());
        } else if (type2.getBaseType() != null) {
            merged.setBaseType(type2.getBaseType());
        }
        
        merged.setAbstract(type1.isAbstract());
        merged.setOpenType(type1.isOpenType());
        
        // Merge properties
        Map<String, CsdlProperty> properties = new HashMap<>();
        if (type1.getProperties() != null) {
            for (CsdlProperty prop : type1.getProperties()) {
                properties.put(prop.getName(), prop);
            }
        }
        if (type2.getProperties() != null) {
            for (CsdlProperty prop : type2.getProperties()) {
                if (!properties.containsKey(prop.getName())) {
                    properties.put(prop.getName(), prop);
                }
            }
        }
        merged.setProperties(new ArrayList<>(properties.values()));
        
        return merged;
    }
    
    private void mergeEnumTypes(List<CsdlSchema> schemas, CsdlSchema result, ConflictResolution resolution,
                               List<String> warnings, List<String> errors, List<ConflictInfo> conflicts, String namespace) {
        Map<String, CsdlEnumType> enumTypes = new HashMap<>();
        
        for (CsdlSchema schema : schemas) {
            if (schema.getEnumTypes() != null) {
                for (CsdlEnumType enumType : schema.getEnumTypes()) {
                    String name = enumType.getName();
                    if (enumTypes.containsKey(name)) {
                        handleGenericConflict(ConflictType.ENUM_TYPE, name, enumTypes.get(name), enumType,
                                            resolution, warnings, errors, conflicts, namespace);
                        if (resolution == ConflictResolution.KEEP_LAST) {
                            enumTypes.put(name, enumType);
                        } else if (resolution == ConflictResolution.SKIP_CONFLICTS) {
                            enumTypes.remove(name);
                        }
                    } else {
                        enumTypes.put(name, enumType);
                    }
                }
            }
        }
        
        result.setEnumTypes(new ArrayList<>(enumTypes.values()));
    }
    
    private void mergeActions(List<CsdlSchema> schemas, CsdlSchema result, ConflictResolution resolution,
                             List<String> warnings, List<String> errors, List<ConflictInfo> conflicts, String namespace) {
        Map<String, List<CsdlAction>> actionsByName = new HashMap<>();
        
        for (CsdlSchema schema : schemas) {
            if (schema.getActions() != null) {
                for (CsdlAction action : schema.getActions()) {
                    String name = action.getName();
                    actionsByName.computeIfAbsent(name, k -> new ArrayList<>()).add(action);
                }
            }
        }
        
        List<CsdlAction> mergedActions = new ArrayList<>();
        for (Map.Entry<String, List<CsdlAction>> entry : actionsByName.entrySet()) {
            String name = entry.getKey();
            List<CsdlAction> actions = entry.getValue();
            
            if (actions.size() == 1) {
                mergedActions.add(actions.get(0));
            } else {
                // Check for conflicts based on parameters
                Map<String, CsdlAction> actionsBySignature = new HashMap<>();
                for (CsdlAction action : actions) {
                    String signature = getActionSignature(action);
                    if (actionsBySignature.containsKey(signature)) {
                        handleGenericConflict(ConflictType.ACTION, name + "(" + signature + ")",
                                            actionsBySignature.get(signature), action, resolution,
                                            warnings, errors, conflicts, namespace);
                        if (resolution == ConflictResolution.KEEP_LAST) {
                            actionsBySignature.put(signature, action);
                        } else if (resolution == ConflictResolution.SKIP_CONFLICTS) {
                            actionsBySignature.remove(signature);
                        }
                    } else {
                        actionsBySignature.put(signature, action);
                    }
                }
                mergedActions.addAll(actionsBySignature.values());
            }
        }
        
        result.setActions(mergedActions);
    }
    
    private void mergeFunctions(List<CsdlSchema> schemas, CsdlSchema result, ConflictResolution resolution,
                               List<String> warnings, List<String> errors, List<ConflictInfo> conflicts, String namespace) {
        Map<String, List<CsdlFunction>> functionsByName = new HashMap<>();
        
        for (CsdlSchema schema : schemas) {
            if (schema.getFunctions() != null) {
                for (CsdlFunction function : schema.getFunctions()) {
                    String name = function.getName();
                    functionsByName.computeIfAbsent(name, k -> new ArrayList<>()).add(function);
                }
            }
        }
        
        List<CsdlFunction> mergedFunctions = new ArrayList<>();
        for (Map.Entry<String, List<CsdlFunction>> entry : functionsByName.entrySet()) {
            String name = entry.getKey();
            List<CsdlFunction> functions = entry.getValue();
            
            if (functions.size() == 1) {
                mergedFunctions.add(functions.get(0));
            } else {
                // Check for conflicts based on parameters and return type
                Map<String, CsdlFunction> functionsBySignature = new HashMap<>();
                for (CsdlFunction function : functions) {
                    String signature = getFunctionSignature(function);
                    if (functionsBySignature.containsKey(signature)) {
                        handleGenericConflict(ConflictType.FUNCTION, name + "(" + signature + ")",
                                            functionsBySignature.get(signature), function, resolution,
                                            warnings, errors, conflicts, namespace);
                        if (resolution == ConflictResolution.KEEP_LAST) {
                            functionsBySignature.put(signature, function);
                        } else if (resolution == ConflictResolution.SKIP_CONFLICTS) {
                            functionsBySignature.remove(signature);
                        }
                    } else {
                        functionsBySignature.put(signature, function);
                    }
                }
                mergedFunctions.addAll(functionsBySignature.values());
            }
        }
        
        result.setFunctions(mergedFunctions);
    }
    
    private void mergeTerms(List<CsdlSchema> schemas, CsdlSchema result, ConflictResolution resolution,
                           List<String> warnings, List<String> errors, List<ConflictInfo> conflicts, String namespace) {
        Map<String, CsdlTerm> terms = new HashMap<>();
        
        for (CsdlSchema schema : schemas) {
            if (schema.getTerms() != null) {
                for (CsdlTerm term : schema.getTerms()) {
                    String name = term.getName();
                    if (terms.containsKey(name)) {
                        handleGenericConflict(ConflictType.TERM, name, terms.get(name), term,
                                            resolution, warnings, errors, conflicts, namespace);
                        if (resolution == ConflictResolution.KEEP_LAST) {
                            terms.put(name, term);
                        } else if (resolution == ConflictResolution.SKIP_CONFLICTS) {
                            terms.remove(name);
                        }
                    } else {
                        terms.put(name, term);
                    }
                }
            }
        }
        
        result.setTerms(new ArrayList<>(terms.values()));
    }
    
    private void mergeTypeDefinitions(List<CsdlSchema> schemas, CsdlSchema result, ConflictResolution resolution,
                                     List<String> warnings, List<String> errors, List<ConflictInfo> conflicts, String namespace) {
        Map<String, CsdlTypeDefinition> typeDefinitions = new HashMap<>();
        
        for (CsdlSchema schema : schemas) {
            if (schema.getTypeDefinitions() != null) {
                for (CsdlTypeDefinition typeDef : schema.getTypeDefinitions()) {
                    String name = typeDef.getName();
                    if (typeDefinitions.containsKey(name)) {
                        handleGenericConflict(ConflictType.TYPE_DEFINITION, name, typeDefinitions.get(name), typeDef,
                                            resolution, warnings, errors, conflicts, namespace);
                        if (resolution == ConflictResolution.KEEP_LAST) {
                            typeDefinitions.put(name, typeDef);
                        } else if (resolution == ConflictResolution.SKIP_CONFLICTS) {
                            typeDefinitions.remove(name);
                        }
                    } else {
                        typeDefinitions.put(name, typeDef);
                    }
                }
            }
        }
        
        result.setTypeDefinitions(new ArrayList<>(typeDefinitions.values()));
    }
    
    private void mergeEntityContainers(List<CsdlSchema> schemas, CsdlSchema result, ConflictResolution resolution,
                                      List<String> warnings, List<String> errors, List<ConflictInfo> conflicts, String namespace) {
        Map<String, CsdlEntityContainer> entityContainers = new HashMap<>();
        
        for (CsdlSchema schema : schemas) {
            if (schema.getEntityContainer() != null) {
                CsdlEntityContainer container = schema.getEntityContainer();
                String name = container.getName();
                if (entityContainers.containsKey(name)) {
                    handleEntityContainerConflict(name, entityContainers.get(name), container, resolution,
                                                 warnings, errors, conflicts, entityContainers, namespace);
                } else {
                    entityContainers.put(name, container);
                }
            }
        }
        
        // Set the first (or merged) entity container
        if (!entityContainers.isEmpty()) {
            result.setEntityContainer(entityContainers.values().iterator().next());
        }
    }
    
    private void handleEntityContainerConflict(String name, CsdlEntityContainer existing, CsdlEntityContainer conflicting,
                                             ConflictResolution resolution, List<String> warnings, List<String> errors,
                                             List<ConflictInfo> conflicts, Map<String, CsdlEntityContainer> containers, String namespace) {
        ConflictInfo conflict = new ConflictInfo(ConflictType.ENTITY_CONTAINER, name,
                "EntityContainer '" + name + "' is defined in multiple schemas", existing, conflicting, namespace);
        conflicts.add(conflict);
        
        switch (resolution) {
            case THROW_ERROR:
                errors.add("Conflicting EntityContainer: " + name);
                break;
            case KEEP_FIRST:
                warnings.add("Keeping first definition of EntityContainer: " + name);
                break;
            case KEEP_LAST:
                containers.put(name, conflicting);
                warnings.add("Using last definition of EntityContainer: " + name);
                break;
            case AUTO_MERGE:
                containers.put(name, mergeEntityContainers(existing, conflicting));
                warnings.add("Auto-merged EntityContainer: " + name);
                break;
            case SKIP_CONFLICTS:
                warnings.add("Skipping conflicting EntityContainer: " + name);
                containers.remove(name);
                break;
        }
    }
    
    private CsdlEntityContainer mergeEntityContainers(CsdlEntityContainer container1, CsdlEntityContainer container2) {
        CsdlEntityContainer merged = new CsdlEntityContainer();
        merged.setName(container1.getName());
        
        // Merge entity sets
        Map<String, CsdlEntitySet> entitySets = new HashMap<>();
        if (container1.getEntitySets() != null) {
            for (CsdlEntitySet entitySet : container1.getEntitySets()) {
                entitySets.put(entitySet.getName(), entitySet);
            }
        }
        if (container2.getEntitySets() != null) {
            for (CsdlEntitySet entitySet : container2.getEntitySets()) {
                if (!entitySets.containsKey(entitySet.getName())) {
                    entitySets.put(entitySet.getName(), entitySet);
                }
            }
        }
        merged.setEntitySets(new ArrayList<>(entitySets.values()));
        
        // Merge singletons
        Map<String, CsdlSingleton> singletons = new HashMap<>();
        if (container1.getSingletons() != null) {
            for (CsdlSingleton singleton : container1.getSingletons()) {
                singletons.put(singleton.getName(), singleton);
            }
        }
        if (container2.getSingletons() != null) {
            for (CsdlSingleton singleton : container2.getSingletons()) {
                if (!singletons.containsKey(singleton.getName())) {
                    singletons.put(singleton.getName(), singleton);
                }
            }
        }
        merged.setSingletons(new ArrayList<>(singletons.values()));
        
        // Merge action imports
        Map<String, CsdlActionImport> actionImports = new HashMap<>();
        if (container1.getActionImports() != null) {
            for (CsdlActionImport actionImport : container1.getActionImports()) {
                actionImports.put(actionImport.getName(), actionImport);
            }
        }
        if (container2.getActionImports() != null) {
            for (CsdlActionImport actionImport : container2.getActionImports()) {
                if (!actionImports.containsKey(actionImport.getName())) {
                    actionImports.put(actionImport.getName(), actionImport);
                }
            }
        }
        merged.setActionImports(new ArrayList<>(actionImports.values()));
        
        // Merge function imports
        Map<String, CsdlFunctionImport> functionImports = new HashMap<>();
        if (container1.getFunctionImports() != null) {
            for (CsdlFunctionImport functionImport : container1.getFunctionImports()) {
                functionImports.put(functionImport.getName(), functionImport);
            }
        }
        if (container2.getFunctionImports() != null) {
            for (CsdlFunctionImport functionImport : container2.getFunctionImports()) {
                if (!functionImports.containsKey(functionImport.getName())) {
                    functionImports.put(functionImport.getName(), functionImport);
                }
            }
        }
        merged.setFunctionImports(new ArrayList<>(functionImports.values()));
        
        return merged;
    }
    
    private void handleGenericConflict(ConflictType type, String name, Object existing, Object conflicting,
                                     ConflictResolution resolution, List<String> warnings, List<String> errors,
                                     List<ConflictInfo> conflicts, String namespace) {
        ConflictInfo conflict = new ConflictInfo(type, name,
                type.name() + " '" + name + "' is defined in multiple schemas", existing, conflicting, namespace);
        conflicts.add(conflict);
        
        switch (resolution) {
            case THROW_ERROR:
                errors.add("Conflicting " + type.name() + ": " + name);
                break;
            case KEEP_FIRST:
                warnings.add("Keeping first definition of " + type.name() + ": " + name);
                break;
            case KEEP_LAST:
                warnings.add("Using last definition of " + type.name() + ": " + name);
                break;
            case AUTO_MERGE:
                errors.add("Cannot auto-merge " + type.name() + ": " + name);
                break;
            case SKIP_CONFLICTS:
                warnings.add("Skipping conflicting " + type.name() + ": " + name);
                break;
        }
    }
    
    private String getActionSignature(CsdlAction action) {
        StringBuilder signature = new StringBuilder();
        if (action.getParameters() != null) {
            for (CsdlParameter param : action.getParameters()) {
                if (signature.length() > 0) {
                    signature.append(",");
                }
                signature.append(param.getType());
            }
        }
        return signature.toString();
    }
    
    private String getFunctionSignature(CsdlFunction function) {
        StringBuilder signature = new StringBuilder();
        if (function.getParameters() != null) {
            for (CsdlParameter param : function.getParameters()) {
                if (signature.length() > 0) {
                    signature.append(",");
                }
                signature.append(param.getType());
            }
        }
        
        // Add return type to signature for functions
        if (function.getReturnType() != null) {
            signature.append("->").append(function.getReturnType().getType());
        }
        
        return signature.toString();
    }
    
    private boolean objectsEqual(Object obj1, Object obj2) {
        try {
            if (obj1 == null && obj2 == null) {
                return true;
            }
            if (obj1 == null || obj2 == null) {
                return false;
            }
            System.out.println("DEBUG: Comparing objects: " + obj1.getClass().getSimpleName() + " vs " + obj2.getClass().getSimpleName());
            if (obj1 instanceof org.apache.olingo.commons.api.edm.FullQualifiedName) {
                System.out.println("DEBUG: obj1 is FullQualifiedName: " + obj1);
            }
            if (obj2 instanceof org.apache.olingo.commons.api.edm.FullQualifiedName) {
                System.out.println("DEBUG: obj2 is FullQualifiedName: " + obj2);
            }
            return obj1.equals(obj2);
        } catch (Exception e) {
            System.err.println("ERROR in objectsEqual: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
