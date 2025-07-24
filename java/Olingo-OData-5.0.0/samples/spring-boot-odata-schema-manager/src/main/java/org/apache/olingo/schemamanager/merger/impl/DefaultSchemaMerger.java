package org.apache.olingo.schemamanager.merger.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlSingleton;
import org.apache.olingo.schemamanager.merger.SchemaMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Default implementation of SchemaMerger
 */
@Component
public class DefaultSchemaMerger implements SchemaMerger {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultSchemaMerger.class);
    
    @Override
    public MergeResult mergeSchemas(List<CsdlSchema> schemas) {
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        if (schemas == null || schemas.isEmpty()) {
            errors.add("No schemas provided for merging");
            return new MergeResult(null, warnings, errors, false);
        }
        
        if (schemas.size() == 1) {
            return new MergeResult(schemas.get(0), warnings, errors, true);
        }
        
        try {
            // Find the first non-null schema as base
            CsdlSchema mergedSchema = new CsdlSchema();
            CsdlSchema firstSchema = null;
            for (CsdlSchema schema : schemas) {
                if (schema != null) {
                    firstSchema = schema;
                    break;
                }
            }
            
            if (firstSchema == null) {
                errors.add("All schemas are null");
                return new MergeResult(null, warnings, errors, false);
            }
            
            mergedSchema.setNamespace(firstSchema.getNamespace());
            mergedSchema.setAlias(firstSchema.getAlias());
            
            // Merge all elements
            List<CsdlEntityType> allEntityTypes = new ArrayList<>();
            List<CsdlComplexType> allComplexTypes = new ArrayList<>();
            List<CsdlEnumType> allEnumTypes = new ArrayList<>();
            List<CsdlAction> allActions = new ArrayList<>();
            List<CsdlFunction> allFunctions = new ArrayList<>();
            List<CsdlEntityContainer> allContainers = new ArrayList<>();
            
            for (CsdlSchema schema : schemas) {
                if (schema == null) {
                    continue; // Skip null schemas
                }
                
                // Check namespace consistency
                if (schema.getNamespace() != null && !schema.getNamespace().equals(firstSchema.getNamespace())) {
                    warnings.add("Schema with different namespace found: " + schema.getNamespace() + 
                                 " (expected: " + firstSchema.getNamespace() + ")");
                }
                
                // Merge EntityTypes
                if (schema.getEntityTypes() != null) {
                    allEntityTypes.addAll(schema.getEntityTypes());
                }
                
                // Merge ComplexTypes
                if (schema.getComplexTypes() != null) {
                    allComplexTypes.addAll(schema.getComplexTypes());
                }
                
                // Merge EnumTypes
                if (schema.getEnumTypes() != null) {
                    allEnumTypes.addAll(schema.getEnumTypes());
                }
                
                // Merge Actions
                if (schema.getActions() != null) {
                    allActions.addAll(schema.getActions());
                }
                
                // Merge Functions
                if (schema.getFunctions() != null) {
                    allFunctions.addAll(schema.getFunctions());
                }
                
                // Collect EntityContainers
                if (schema.getEntityContainer() != null) {
                    allContainers.add(schema.getEntityContainer());
                }
            }
            
            // Merge EntityContainers
            CsdlEntityContainer mergedContainer = null;
            if (!allContainers.isEmpty()) {
                mergedContainer = mergeEntityContainers(allContainers, warnings);
            }
            
            // Remove duplicates and set to merged schema
            mergedSchema.setEntityTypes(removeDuplicateEntityTypes(allEntityTypes, warnings));
            mergedSchema.setComplexTypes(removeDuplicateComplexTypes(allComplexTypes, warnings));
            mergedSchema.setEnumTypes(removeDuplicateEnumTypes(allEnumTypes, warnings));
            mergedSchema.setActions(removeDuplicateActions(allActions, warnings));
            mergedSchema.setFunctions(removeDuplicateFunctions(allFunctions, warnings));
            mergedSchema.setEntityContainer(mergedContainer);
            
            logger.info("Merged {} schemas into single schema with namespace: {}", schemas.size(), mergedSchema.getNamespace());
            
            return new MergeResult(mergedSchema, warnings, errors, true);
            
        } catch (Exception e) {
            errors.add("Error during merge: " + e.getMessage());
            logger.error("Error merging schemas", e);
            return new MergeResult(null, warnings, errors, false);
        }
    }
    
    @Override
    public Map<String, CsdlSchema> mergeByNamespace(Map<String, CsdlSchema> schemaMap) {
        if (schemaMap == null || schemaMap.isEmpty()) {
            return new HashMap<>();
        }
        
        // Group schemas by namespace
        Map<String, List<CsdlSchema>> groupedSchemas = new HashMap<>();
        
        for (CsdlSchema schema : schemaMap.values()) {
            if (schema.getNamespace() != null) {
                groupedSchemas.computeIfAbsent(schema.getNamespace(), k -> new ArrayList<>()).add(schema);
            }
        }
        
        Map<String, CsdlSchema> mergedSchemas = new HashMap<>();
        
        for (Map.Entry<String, List<CsdlSchema>> entry : groupedSchemas.entrySet()) {
            String namespace = entry.getKey();
            List<CsdlSchema> namespacedSchemas = entry.getValue();
            
            MergeResult result = mergeSchemas(namespacedSchemas);
            if (result.isSuccess() && result.getMergedSchema() != null) {
                mergedSchemas.put(namespace, result.getMergedSchema());
            }
        }
        
        logger.info("Merged {} schemas into {} namespaces", schemaMap.size(), mergedSchemas.size());
        
        return mergedSchemas;
    }
    
    @Override
    public CompatibilityResult checkCompatibility(CsdlSchema existingSchema, CsdlSchema newSchema) {
        List<String> conflicts = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (!Objects.equals(existingSchema.getNamespace(), newSchema.getNamespace())) {
            conflicts.add("Different namespaces: " + existingSchema.getNamespace() + " vs " + newSchema.getNamespace());
        }
        
        // Check EntityType conflicts
        checkEntityTypeCompatibility(existingSchema, newSchema, conflicts, warnings);
        
        // Check ComplexType conflicts
        checkComplexTypeCompatibility(existingSchema, newSchema, conflicts, warnings);
        
        // Check EnumType conflicts
        checkEnumTypeCompatibility(existingSchema, newSchema, conflicts, warnings);
        
        // Check EntityContainer conflicts
        checkEntityContainerCompatibility(existingSchema, newSchema, conflicts, warnings);
        
        boolean isCompatible = conflicts.isEmpty();
        
        return new CompatibilityResult(isCompatible, conflicts, warnings);
    }
    
    @Override
    public CsdlSchema resolveConflicts(List<CsdlSchema> conflictingSchemas, ConflictResolution resolution) {
        if (conflictingSchemas == null || conflictingSchemas.isEmpty()) {
            return null;
        }
        
        if (conflictingSchemas.size() == 1) {
            return conflictingSchemas.get(0);
        }
        
        switch (resolution) {
            case KEEP_FIRST:
                return resolveKeepFirst(conflictingSchemas);
            case KEEP_LAST:
                return resolveKeepLast(conflictingSchemas);
            case AUTO_MERGE:
                MergeResult result = mergeSchemas(conflictingSchemas);
                return result.isSuccess() ? result.getMergedSchema() : null;
            case THROW_ERROR:
                throw new IllegalStateException("Conflicts detected and THROW_ERROR strategy specified");
            default:
                throw new IllegalArgumentException("Unknown conflict resolution strategy: " + resolution);
        }
    }
    
    // Private helper methods
    
    private List<CsdlEntityType> removeDuplicateEntityTypes(List<CsdlEntityType> entityTypes, List<String> warnings) {
        Map<String, CsdlEntityType> uniqueTypes = new LinkedHashMap<>();
        
        for (CsdlEntityType entityType : entityTypes) {
            String name = entityType.getName();
            if (uniqueTypes.containsKey(name)) {
                warnings.add("Duplicate EntityType found: " + name + ", keeping first occurrence");
            } else {
                uniqueTypes.put(name, entityType);
            }
        }
        
        return new ArrayList<>(uniqueTypes.values());
    }
    
    private List<CsdlComplexType> removeDuplicateComplexTypes(List<CsdlComplexType> complexTypes, List<String> warnings) {
        Map<String, CsdlComplexType> uniqueTypes = new LinkedHashMap<>();
        
        for (CsdlComplexType complexType : complexTypes) {
            String name = complexType.getName();
            if (uniqueTypes.containsKey(name)) {
                warnings.add("Duplicate ComplexType found: " + name + ", keeping first occurrence");
            } else {
                uniqueTypes.put(name, complexType);
            }
        }
        
        return new ArrayList<>(uniqueTypes.values());
    }
    
    private List<CsdlEnumType> removeDuplicateEnumTypes(List<CsdlEnumType> enumTypes, List<String> warnings) {
        Map<String, CsdlEnumType> uniqueTypes = new LinkedHashMap<>();
        
        for (CsdlEnumType enumType : enumTypes) {
            String name = enumType.getName();
            if (uniqueTypes.containsKey(name)) {
                warnings.add("Duplicate EnumType found: " + name + ", keeping first occurrence");
            } else {
                uniqueTypes.put(name, enumType);
            }
        }
        
        return new ArrayList<>(uniqueTypes.values());
    }
    
    private List<CsdlAction> removeDuplicateActions(List<CsdlAction> actions, List<String> warnings) {
        Map<String, CsdlAction> uniqueActions = new LinkedHashMap<>();
        
        for (CsdlAction action : actions) {
            String name = action.getName();
            if (uniqueActions.containsKey(name)) {
                warnings.add("Duplicate Action found: " + name + ", keeping first occurrence");
            } else {
                uniqueActions.put(name, action);
            }
        }
        
        return new ArrayList<>(uniqueActions.values());
    }
    
    private List<CsdlFunction> removeDuplicateFunctions(List<CsdlFunction> functions, List<String> warnings) {
        Map<String, CsdlFunction> uniqueFunctions = new LinkedHashMap<>();
        
        for (CsdlFunction function : functions) {
            String name = function.getName();
            if (uniqueFunctions.containsKey(name)) {
                warnings.add("Duplicate Function found: " + name + ", keeping first occurrence");
            } else {
                uniqueFunctions.put(name, function);
            }
        }
        
        return new ArrayList<>(uniqueFunctions.values());
    }
    
    private CsdlEntityContainer mergeEntityContainers(List<CsdlEntityContainer> containers, List<String> warnings) {
        if (containers == null || containers.isEmpty()) {
            return null;
        }
        
        if (containers.size() == 1) {
            return containers.get(0);
        }
        
        CsdlEntityContainer merged = new CsdlEntityContainer();
        CsdlEntityContainer first = containers.get(0);
        
        merged.setName(first.getName());
        // Only set extends container if it's not null
        if (first.getExtendsContainer() != null) {
            merged.setExtendsContainer(first.getExtendsContainer());
        }
        
        // Check if all containers have the same name, if not add warning
        for (CsdlEntityContainer container : containers) {
            if (!Objects.equals(container.getName(), first.getName())) {
                warnings.add("Merging EntityContainers with different names: " + 
                           first.getName() + " and " + container.getName() + 
                           ", using name: " + first.getName());
            }
        }
        
        List<CsdlEntitySet> allEntitySets = new ArrayList<>();
        List<CsdlSingleton> allSingletons = new ArrayList<>();
        List<CsdlActionImport> allActionImports = new ArrayList<>();
        List<CsdlFunctionImport> allFunctionImports = new ArrayList<>();
        
        for (CsdlEntityContainer container : containers) {
            if (container.getEntitySets() != null) {
                // Add null checking for EntitySets
                for (CsdlEntitySet entitySet : container.getEntitySets()) {
                    if (entitySet != null) {
                        if (entitySet.getName() == null) {
                            warnings.add("Found EntitySet with null name, skipping");
                            continue;
                        }
                        if (entitySet.getType() == null) {
                            warnings.add("Found EntitySet '" + entitySet.getName() + "' with null type, skipping");
                            continue;
                        }
                        allEntitySets.add(entitySet);
                    } else {
                        warnings.add("Found null EntitySet in container " + container.getName());
                    }
                }
            }
            if (container.getSingletons() != null) {
                allSingletons.addAll(container.getSingletons());
            }
            if (container.getActionImports() != null) {
                allActionImports.addAll(container.getActionImports());
            }
            if (container.getFunctionImports() != null) {
                allFunctionImports.addAll(container.getFunctionImports());
            }
        }
        
        merged.setEntitySets(removeDuplicateEntitySets(allEntitySets, warnings));
        merged.setSingletons(removeDuplicateSingletons(allSingletons, warnings));
        merged.setActionImports(removeDuplicateActionImports(allActionImports, warnings));
        merged.setFunctionImports(removeDuplicateFunctionImports(allFunctionImports, warnings));
        
        return merged;
    }
    
    private List<CsdlEntitySet> removeDuplicateEntitySets(List<CsdlEntitySet> entitySets, List<String> warnings) {
        Map<String, CsdlEntitySet> uniqueSets = new LinkedHashMap<>();
        
        for (CsdlEntitySet entitySet : entitySets) {
            if (entitySet == null) {
                warnings.add("Found null EntitySet, skipping");
                continue;
            }
            
            String name = entitySet.getName();
            if (name == null) {
                warnings.add("Found EntitySet with null name, skipping");
                continue;
            }
            
            if (uniqueSets.containsKey(name)) {
                warnings.add("Duplicate EntitySet found: " + name + ", keeping first occurrence");
            } else {
                uniqueSets.put(name, entitySet);
            }
        }
        
        return new ArrayList<>(uniqueSets.values());
    }
    
    private List<CsdlSingleton> removeDuplicateSingletons(List<CsdlSingleton> singletons, List<String> warnings) {
        Map<String, CsdlSingleton> uniqueSingletons = new LinkedHashMap<>();
        
        for (CsdlSingleton singleton : singletons) {
            String name = singleton.getName();
            if (uniqueSingletons.containsKey(name)) {
                warnings.add("Duplicate Singleton found: " + name + ", keeping first occurrence");
            } else {
                uniqueSingletons.put(name, singleton);
            }
        }
        
        return new ArrayList<>(uniqueSingletons.values());
    }
    
    private List<CsdlActionImport> removeDuplicateActionImports(List<CsdlActionImport> actionImports, List<String> warnings) {
        Map<String, CsdlActionImport> uniqueImports = new LinkedHashMap<>();
        
        for (CsdlActionImport actionImport : actionImports) {
            String name = actionImport.getName();
            if (uniqueImports.containsKey(name)) {
                warnings.add("Duplicate ActionImport found: " + name + ", keeping first occurrence");
            } else {
                uniqueImports.put(name, actionImport);
            }
        }
        
        return new ArrayList<>(uniqueImports.values());
    }
    
    private List<CsdlFunctionImport> removeDuplicateFunctionImports(List<CsdlFunctionImport> functionImports, List<String> warnings) {
        Map<String, CsdlFunctionImport> uniqueImports = new LinkedHashMap<>();
        
        for (CsdlFunctionImport functionImport : functionImports) {
            String name = functionImport.getName();
            if (uniqueImports.containsKey(name)) {
                warnings.add("Duplicate FunctionImport found: " + name + ", keeping first occurrence");
            } else {
                uniqueImports.put(name, functionImport);
            }
        }
        
        return new ArrayList<>(uniqueImports.values());
    }
    
    private void checkEntityTypeCompatibility(CsdlSchema schema1, CsdlSchema schema2, List<String> conflicts, List<String> warnings) {
        if (schema1.getEntityTypes() == null || schema2.getEntityTypes() == null) {
            return;
        }
        
        Map<String, CsdlEntityType> types1 = schema1.getEntityTypes().stream()
                .collect(Collectors.toMap(CsdlEntityType::getName, t -> t));
        Map<String, CsdlEntityType> types2 = schema2.getEntityTypes().stream()
                .collect(Collectors.toMap(CsdlEntityType::getName, t -> t));
        
        for (String typeName : types1.keySet()) {
            if (types2.containsKey(typeName)) {
                CsdlEntityType type1 = types1.get(typeName);
                CsdlEntityType type2 = types2.get(typeName);
                
                if (!areEntityTypesCompatible(type1, type2)) {
                    conflicts.add("Incompatible EntityType: " + typeName);
                } else {
                    warnings.add("EntityType " + typeName + " exists in both schemas");
                }
            }
        }
    }
    
    private void checkComplexTypeCompatibility(CsdlSchema schema1, CsdlSchema schema2, List<String> conflicts, List<String> warnings) {
        if (schema1.getComplexTypes() == null || schema2.getComplexTypes() == null) {
            return;
        }
        
        Map<String, CsdlComplexType> types1 = schema1.getComplexTypes().stream()
                .collect(Collectors.toMap(CsdlComplexType::getName, t -> t));
        Map<String, CsdlComplexType> types2 = schema2.getComplexTypes().stream()
                .collect(Collectors.toMap(CsdlComplexType::getName, t -> t));
        
        for (String typeName : types1.keySet()) {
            if (types2.containsKey(typeName)) {
                CsdlComplexType type1 = types1.get(typeName);
                CsdlComplexType type2 = types2.get(typeName);
                
                if (!areComplexTypesCompatible(type1, type2)) {
                    conflicts.add("Incompatible ComplexType: " + typeName);
                } else {
                    warnings.add("ComplexType " + typeName + " exists in both schemas");
                }
            }
        }
    }
    
    private void checkEnumTypeCompatibility(CsdlSchema schema1, CsdlSchema schema2, List<String> conflicts, List<String> warnings) {
        if (schema1.getEnumTypes() == null || schema2.getEnumTypes() == null) {
            return;
        }
        
        Map<String, CsdlEnumType> types1 = schema1.getEnumTypes().stream()
                .collect(Collectors.toMap(CsdlEnumType::getName, t -> t));
        Map<String, CsdlEnumType> types2 = schema2.getEnumTypes().stream()
                .collect(Collectors.toMap(CsdlEnumType::getName, t -> t));
        
        for (String typeName : types1.keySet()) {
            if (types2.containsKey(typeName)) {
                CsdlEnumType type1 = types1.get(typeName);
                CsdlEnumType type2 = types2.get(typeName);
                
                if (!areEnumTypesCompatible(type1, type2)) {
                    conflicts.add("Incompatible EnumType: " + typeName);
                } else {
                    warnings.add("EnumType " + typeName + " exists in both schemas");
                }
            }
        }
    }
    
    private void checkEntityContainerCompatibility(CsdlSchema schema1, CsdlSchema schema2, List<String> conflicts, List<String> warnings) {
        if (schema1.getEntityContainer() == null || schema2.getEntityContainer() == null) {
            return;
        }
        
        CsdlEntityContainer container1 = schema1.getEntityContainer();
        CsdlEntityContainer container2 = schema2.getEntityContainer();
        
        if (!Objects.equals(container1.getName(), container2.getName())) {
            warnings.add("Different EntityContainer names: " + container1.getName() + " vs " + container2.getName());
        } else {
            warnings.add("Both schemas have EntityContainer: " + container1.getName());
        }
    }
    
    private boolean areEntityTypesCompatible(CsdlEntityType type1, CsdlEntityType type2) {
        // Check basic compatibility
        if (!Objects.equals(type1.getBaseType(), type2.getBaseType()) ||
            !Objects.equals(type1.isAbstract(), type2.isAbstract())) {
            return false;
        }
        
        // Check properties compatibility
        List<CsdlProperty> props1 = type1.getProperties() != null ? type1.getProperties() : new ArrayList<>();
        List<CsdlProperty> props2 = type2.getProperties() != null ? type2.getProperties() : new ArrayList<>();
        
        // Convert to maps for easier comparison
        Map<String, CsdlProperty> propMap1 = props1.stream()
                .collect(Collectors.toMap(CsdlProperty::getName, p -> p));
        Map<String, CsdlProperty> propMap2 = props2.stream()
                .collect(Collectors.toMap(CsdlProperty::getName, p -> p));
        
        // If properties differ, types are incompatible
        if (!propMap1.keySet().equals(propMap2.keySet())) {
            return false;
        }
        
        // Check each property is compatible
        for (String propName : propMap1.keySet()) {
            CsdlProperty prop1 = propMap1.get(propName);
            CsdlProperty prop2 = propMap2.get(propName);
            
            if (!Objects.equals(prop1.getType(), prop2.getType()) ||
                !Objects.equals(prop1.isNullable(), prop2.isNullable())) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean areComplexTypesCompatible(CsdlComplexType type1, CsdlComplexType type2) {
        // Simple compatibility check - can be enhanced
        return Objects.equals(type1.getBaseType(), type2.getBaseType()) &&
               Objects.equals(type1.isAbstract(), type2.isAbstract());
    }
    
    private boolean areEnumTypesCompatible(CsdlEnumType type1, CsdlEnumType type2) {
        // Simple compatibility check - can be enhanced
        return Objects.equals(type1.getUnderlyingType(), type2.getUnderlyingType()) &&
               Objects.equals(type1.isFlags(), type2.isFlags());
    }
    
    private CsdlSchema resolveKeepFirst(List<CsdlSchema> schemas) {
        return schemas.get(0);
    }
    
    private CsdlSchema resolveKeepLast(List<CsdlSchema> schemas) {
        return schemas.get(schemas.size() - 1);
    }
}
