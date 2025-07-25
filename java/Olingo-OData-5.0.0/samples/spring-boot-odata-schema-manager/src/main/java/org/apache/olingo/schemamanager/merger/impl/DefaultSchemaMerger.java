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
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlSingleton;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;
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
        return mergeSchemas(schemas, ConflictResolution.KEEP_FIRST);
    }
    
    @Override
    public MergeResult mergeSchemas(List<CsdlSchema> schemas, ConflictResolution resolution) {
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        List<ConflictInfo> conflicts = new ArrayList<>();
        
        if (schemas == null || schemas.isEmpty()) {
            errors.add("No schemas provided for merging");
            return new MergeResult(null, warnings, errors, conflicts, false);
        }
        
        if (schemas.size() == 1) {
            return new MergeResult(schemas.get(0), warnings, errors, conflicts, true);
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
                return new MergeResult(null, warnings, errors, conflicts, false);
            }
            
            mergedSchema.setNamespace(firstSchema.getNamespace());
            mergedSchema.setAlias(firstSchema.getAlias());
            
            // Merge all elements with conflict detection
            List<CsdlEntityType> allEntityTypes = new ArrayList<>();
            List<CsdlComplexType> allComplexTypes = new ArrayList<>();
            List<CsdlEnumType> allEnumTypes = new ArrayList<>();
            List<CsdlAction> allActions = new ArrayList<>();
            List<CsdlFunction> allFunctions = new ArrayList<>();
            List<CsdlTerm> allTerms = new ArrayList<>();
            List<CsdlTypeDefinition> allTypeDefinitions = new ArrayList<>();
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
                
                // Collect all elements
                if (schema.getEntityTypes() != null) {
                    allEntityTypes.addAll(schema.getEntityTypes());
                }
                if (schema.getComplexTypes() != null) {
                    allComplexTypes.addAll(schema.getComplexTypes());
                }
                if (schema.getEnumTypes() != null) {
                    allEnumTypes.addAll(schema.getEnumTypes());
                }
                if (schema.getActions() != null) {
                    allActions.addAll(schema.getActions());
                }
                if (schema.getFunctions() != null) {
                    allFunctions.addAll(schema.getFunctions());
                }
                if (schema.getTerms() != null) {
                    allTerms.addAll(schema.getTerms());
                }
                if (schema.getTypeDefinitions() != null) {
                    allTypeDefinitions.addAll(schema.getTypeDefinitions());
                }
                if (schema.getEntityContainer() != null) {
                    allContainers.add(schema.getEntityContainer());
                }
            }
            
            // Process elements with conflict resolution
            mergedSchema.setEntityTypes(processEntityTypes(allEntityTypes, resolution, conflicts, warnings));
            mergedSchema.setComplexTypes(processComplexTypes(allComplexTypes, resolution, conflicts, warnings));
            mergedSchema.setEnumTypes(processEnumTypes(allEnumTypes, resolution, conflicts, warnings));
            mergedSchema.setActions(processActions(allActions, resolution, conflicts, warnings));
            mergedSchema.setFunctions(processFunctions(allFunctions, resolution, conflicts, warnings));
            mergedSchema.setTerms(processTerms(allTerms, resolution, conflicts, warnings));
            mergedSchema.setTypeDefinitions(processTypeDefinitions(allTypeDefinitions, resolution, conflicts, warnings));
            mergedSchema.setEntityContainer(processEntityContainers(allContainers, resolution, conflicts, warnings));
            
            // Handle conflicts based on resolution strategy
            if (!conflicts.isEmpty() && resolution == ConflictResolution.THROW_ERROR) {
                StringBuilder errorMsg = new StringBuilder("Conflicts detected during merge:");
                for (ConflictInfo conflict : conflicts) {
                    errorMsg.append("\n- ").append(conflict.getDescription());
                }
                errors.add(errorMsg.toString());
                return new MergeResult(null, warnings, errors, conflicts, false);
            }
            
            logger.info("Merged {} schemas into single schema with namespace: {}", schemas.size(), mergedSchema.getNamespace());
            
            return new MergeResult(mergedSchema, warnings, errors, conflicts, true);
            
        } catch (Exception e) {
            errors.add("Error during merge: " + e.getMessage());
            logger.error("Error merging schemas", e);
            return new MergeResult(null, warnings, errors, conflicts, false);
        }
    }
    
    @Override
    public Map<String, CsdlSchema> mergeByNamespace(Map<String, CsdlSchema> schemaMap) {
        return mergeByNamespace(schemaMap, ConflictResolution.KEEP_FIRST);
    }
    
    @Override
    public Map<String, CsdlSchema> mergeByNamespace(Map<String, CsdlSchema> schemaMap, ConflictResolution resolution) {
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
            
            MergeResult result = mergeSchemas(namespacedSchemas, resolution);
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
        List<ConflictInfo> detailedConflicts = new ArrayList<>();
        
        if (!Objects.equals(existingSchema.getNamespace(), newSchema.getNamespace())) {
            conflicts.add("Different namespaces: " + existingSchema.getNamespace() + " vs " + newSchema.getNamespace());
        }
        
        // Check all types of conflicts with detailed information
        checkEntityTypeCompatibility(existingSchema, newSchema, conflicts, warnings, detailedConflicts);
        checkComplexTypeCompatibility(existingSchema, newSchema, conflicts, warnings, detailedConflicts);
        checkEnumTypeCompatibility(existingSchema, newSchema, conflicts, warnings, detailedConflicts);
        checkActionCompatibility(existingSchema, newSchema, conflicts, warnings, detailedConflicts);
        checkFunctionCompatibility(existingSchema, newSchema, conflicts, warnings, detailedConflicts);
        checkTermCompatibility(existingSchema, newSchema, conflicts, warnings, detailedConflicts);
        checkTypeDefinitionCompatibility(existingSchema, newSchema, conflicts, warnings, detailedConflicts);
        checkEntityContainerCompatibility(existingSchema, newSchema, conflicts, warnings, detailedConflicts);
        
        boolean isCompatible = conflicts.isEmpty();
        
        return new CompatibilityResult(isCompatible, conflicts, warnings, detailedConflicts);
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
    
    // Comprehensive element processing methods with conflict detection
    private List<CsdlEntityType> processEntityTypes(List<CsdlEntityType> entityTypes, ConflictResolution resolution, 
                                                   List<ConflictInfo> conflicts, List<String> warnings) {
        Map<String, List<CsdlEntityType>> nameGroups = new LinkedHashMap<>();
        
        for (CsdlEntityType entityType : entityTypes) {
            if (entityType != null && entityType.getName() != null) {
                nameGroups.computeIfAbsent(entityType.getName(), k -> new ArrayList<>()).add(entityType);
            }
        }
        
        List<CsdlEntityType> result = new ArrayList<>();
        
        for (Map.Entry<String, List<CsdlEntityType>> entry : nameGroups.entrySet()) {
            String name = entry.getKey();
            List<CsdlEntityType> types = entry.getValue();
            
            if (types.size() == 1) {
                result.add(types.get(0));
            } else {
                // Detect conflicts
                if (!areEntityTypesCompatible(types)) {
                    ConflictInfo conflict = new ConflictInfo(ConflictType.ENTITY_TYPE, name, 
                        "Incompatible EntityType definitions for: " + name, types.get(0), types.get(1));
                    conflicts.add(conflict);
                    
                    if (resolution == ConflictResolution.SKIP_CONFLICTS) {
                        warnings.add("Skipping conflicting EntityType: " + name);
                        continue;
                    }
                }
                
                // Apply resolution strategy
                result.add(resolveEntityTypeConflict(types, resolution, warnings));
            }
        }
        
        return result;
    }
    
    private List<CsdlComplexType> processComplexTypes(List<CsdlComplexType> complexTypes, ConflictResolution resolution, 
                                                     List<ConflictInfo> conflicts, List<String> warnings) {
        Map<String, List<CsdlComplexType>> nameGroups = new LinkedHashMap<>();
        
        for (CsdlComplexType complexType : complexTypes) {
            if (complexType != null && complexType.getName() != null) {
                nameGroups.computeIfAbsent(complexType.getName(), k -> new ArrayList<>()).add(complexType);
            }
        }
        
        List<CsdlComplexType> result = new ArrayList<>();
        
        for (Map.Entry<String, List<CsdlComplexType>> entry : nameGroups.entrySet()) {
            String name = entry.getKey();
            List<CsdlComplexType> types = entry.getValue();
            
            if (types.size() == 1) {
                result.add(types.get(0));
            } else {
                if (!areComplexTypesCompatible(types)) {
                    ConflictInfo conflict = new ConflictInfo(ConflictType.COMPLEX_TYPE, name, 
                        "Incompatible ComplexType definitions for: " + name, types.get(0), types.get(1));
                    conflicts.add(conflict);
                    
                    if (resolution == ConflictResolution.SKIP_CONFLICTS) {
                        warnings.add("Skipping conflicting ComplexType: " + name);
                        continue;
                    }
                }
                
                result.add(resolveComplexTypeConflict(types, resolution, warnings));
            }
        }
        
        return result;
    }
    
    private List<CsdlEnumType> processEnumTypes(List<CsdlEnumType> enumTypes, ConflictResolution resolution, 
                                               List<ConflictInfo> conflicts, List<String> warnings) {
        Map<String, List<CsdlEnumType>> nameGroups = new LinkedHashMap<>();
        
        for (CsdlEnumType enumType : enumTypes) {
            if (enumType != null && enumType.getName() != null) {
                nameGroups.computeIfAbsent(enumType.getName(), k -> new ArrayList<>()).add(enumType);
            }
        }
        
        List<CsdlEnumType> result = new ArrayList<>();
        
        for (Map.Entry<String, List<CsdlEnumType>> entry : nameGroups.entrySet()) {
            String name = entry.getKey();
            List<CsdlEnumType> types = entry.getValue();
            
            if (types.size() == 1) {
                result.add(types.get(0));
            } else {
                if (!areEnumTypesCompatible(types)) {
                    ConflictInfo conflict = new ConflictInfo(ConflictType.ENUM_TYPE, name, 
                        "Incompatible EnumType definitions for: " + name, types.get(0), types.get(1));
                    conflicts.add(conflict);
                    
                    if (resolution == ConflictResolution.SKIP_CONFLICTS) {
                        warnings.add("Skipping conflicting EnumType: " + name);
                        continue;
                    }
                }
                
                result.add(resolveEnumTypeConflict(types, resolution, warnings));
            }
        }
        
        return result;
    }
    
    private List<CsdlAction> processActions(List<CsdlAction> actions, ConflictResolution resolution, 
                                           List<ConflictInfo> conflicts, List<String> warnings) {
        Map<String, List<CsdlAction>> nameGroups = new LinkedHashMap<>();
        
        for (CsdlAction action : actions) {
            if (action != null && action.getName() != null) {
                String signature = getActionSignature(action);
                nameGroups.computeIfAbsent(signature, k -> new ArrayList<>()).add(action);
            }
        }
        
        List<CsdlAction> result = new ArrayList<>();
        
        for (Map.Entry<String, List<CsdlAction>> entry : nameGroups.entrySet()) {
            String signature = entry.getKey();
            List<CsdlAction> actionList = entry.getValue();
            
            if (actionList.size() == 1) {
                result.add(actionList.get(0));
            } else {
                ConflictInfo conflict = new ConflictInfo(ConflictType.ACTION, actionList.get(0).getName(), 
                    "Duplicate Action with signature: " + signature, actionList.get(0), actionList.get(1));
                conflicts.add(conflict);
                
                if (resolution == ConflictResolution.SKIP_CONFLICTS) {
                    warnings.add("Skipping conflicting Action: " + signature);
                    continue;
                }
                
                result.add(resolveActionConflict(actionList, resolution, warnings));
            }
        }
        
        return result;
    }
    
    private List<CsdlFunction> processFunctions(List<CsdlFunction> functions, ConflictResolution resolution, 
                                               List<ConflictInfo> conflicts, List<String> warnings) {
        Map<String, List<CsdlFunction>> nameGroups = new LinkedHashMap<>();
        
        for (CsdlFunction function : functions) {
            if (function != null && function.getName() != null) {
                String signature = getFunctionSignature(function);
                nameGroups.computeIfAbsent(signature, k -> new ArrayList<>()).add(function);
            }
        }
        
        List<CsdlFunction> result = new ArrayList<>();
        
        for (Map.Entry<String, List<CsdlFunction>> entry : nameGroups.entrySet()) {
            String signature = entry.getKey();
            List<CsdlFunction> functionList = entry.getValue();
            
            if (functionList.size() == 1) {
                result.add(functionList.get(0));
            } else {
                ConflictInfo conflict = new ConflictInfo(ConflictType.FUNCTION, functionList.get(0).getName(), 
                    "Duplicate Function with signature: " + signature, functionList.get(0), functionList.get(1));
                conflicts.add(conflict);
                
                if (resolution == ConflictResolution.SKIP_CONFLICTS) {
                    warnings.add("Skipping conflicting Function: " + signature);
                    continue;
                }
                
                result.add(resolveFunctionConflict(functionList, resolution, warnings));
            }
        }
        
        return result;
    }
    
    private List<CsdlTerm> processTerms(List<CsdlTerm> terms, ConflictResolution resolution, 
                                       List<ConflictInfo> conflicts, List<String> warnings) {
        Map<String, List<CsdlTerm>> nameGroups = new LinkedHashMap<>();
        
        for (CsdlTerm term : terms) {
            if (term != null && term.getName() != null) {
                nameGroups.computeIfAbsent(term.getName(), k -> new ArrayList<>()).add(term);
            }
        }
        
        List<CsdlTerm> result = new ArrayList<>();
        
        for (Map.Entry<String, List<CsdlTerm>> entry : nameGroups.entrySet()) {
            String name = entry.getKey();
            List<CsdlTerm> termList = entry.getValue();
            
            if (termList.size() == 1) {
                result.add(termList.get(0));
            } else {
                if (!areTermsCompatible(termList)) {
                    ConflictInfo conflict = new ConflictInfo(ConflictType.TERM, name, 
                        "Incompatible Term definitions for: " + name, termList.get(0), termList.get(1));
                    conflicts.add(conflict);
                    
                    if (resolution == ConflictResolution.SKIP_CONFLICTS) {
                        warnings.add("Skipping conflicting Term: " + name);
                        continue;
                    }
                }
                
                result.add(resolveTermConflict(termList, resolution, warnings));
            }
        }
        
        return result;
    }
    
    private List<CsdlTypeDefinition> processTypeDefinitions(List<CsdlTypeDefinition> typeDefinitions, ConflictResolution resolution, 
                                                           List<ConflictInfo> conflicts, List<String> warnings) {
        Map<String, List<CsdlTypeDefinition>> nameGroups = new LinkedHashMap<>();
        
        for (CsdlTypeDefinition typeDef : typeDefinitions) {
            if (typeDef != null && typeDef.getName() != null) {
                nameGroups.computeIfAbsent(typeDef.getName(), k -> new ArrayList<>()).add(typeDef);
            }
        }
        
        List<CsdlTypeDefinition> result = new ArrayList<>();
        
        for (Map.Entry<String, List<CsdlTypeDefinition>> entry : nameGroups.entrySet()) {
            String name = entry.getKey();
            List<CsdlTypeDefinition> typeDefList = entry.getValue();
            
            if (typeDefList.size() == 1) {
                result.add(typeDefList.get(0));
            } else {
                if (!areTypeDefinitionsCompatible(typeDefList)) {
                    ConflictInfo conflict = new ConflictInfo(ConflictType.TYPE_DEFINITION, name, 
                        "Incompatible TypeDefinition definitions for: " + name, typeDefList.get(0), typeDefList.get(1));
                    conflicts.add(conflict);
                    
                    if (resolution == ConflictResolution.SKIP_CONFLICTS) {
                        warnings.add("Skipping conflicting TypeDefinition: " + name);
                        continue;
                    }
                }
                
                result.add(resolveTypeDefinitionConflict(typeDefList, resolution, warnings));
            }
        }
        
        return result;
    }
    
    private CsdlEntityContainer processEntityContainers(List<CsdlEntityContainer> containers, ConflictResolution resolution, 
                                                       List<ConflictInfo> conflicts, List<String> warnings) {
        if (containers == null || containers.isEmpty()) {
            return null;
        }
        
        if (containers.size() == 1) {
            return containers.get(0);
        }
        
        // Merge all containers
        CsdlEntityContainer merged = new CsdlEntityContainer();
        CsdlEntityContainer first = containers.get(0);
        
        merged.setName(first.getName());
        if (first.getExtendsContainer() != null) {
            merged.setExtendsContainer(first.getExtendsContainer());
        }
        
        // Check container name conflicts
        for (CsdlEntityContainer container : containers) {
            if (!Objects.equals(container.getName(), first.getName())) {
                ConflictInfo conflict = new ConflictInfo(ConflictType.ENTITY_CONTAINER, container.getName(), 
                    "EntityContainer name conflict: " + first.getName() + " vs " + container.getName(), 
                    first, container);
                conflicts.add(conflict);
                warnings.add("Merging EntityContainers with different names, using: " + first.getName());
            }
        }
        
        // Process container elements
        List<CsdlEntitySet> allEntitySets = new ArrayList<>();
        List<CsdlSingleton> allSingletons = new ArrayList<>();
        List<CsdlActionImport> allActionImports = new ArrayList<>();
        List<CsdlFunctionImport> allFunctionImports = new ArrayList<>();
        
        for (CsdlEntityContainer container : containers) {
            if (container.getEntitySets() != null) {
                allEntitySets.addAll(container.getEntitySets());
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
        
        merged.setEntitySets(processEntitySets(allEntitySets, resolution, conflicts, warnings));
        merged.setSingletons(processSingletons(allSingletons, resolution, conflicts, warnings));
        merged.setActionImports(processActionImports(allActionImports, resolution, conflicts, warnings));
        merged.setFunctionImports(processFunctionImports(allFunctionImports, resolution, conflicts, warnings));
        
        return merged;
    }
    
    // Container element processing methods
    
    private List<CsdlEntitySet> processEntitySets(List<CsdlEntitySet> entitySets, ConflictResolution resolution, 
                                                 List<ConflictInfo> conflicts, List<String> warnings) {
        Map<String, List<CsdlEntitySet>> nameGroups = new LinkedHashMap<>();
        
        for (CsdlEntitySet entitySet : entitySets) {
            if (entitySet != null && entitySet.getName() != null) {
                nameGroups.computeIfAbsent(entitySet.getName(), k -> new ArrayList<>()).add(entitySet);
            }
        }
        
        List<CsdlEntitySet> result = new ArrayList<>();
        
        for (Map.Entry<String, List<CsdlEntitySet>> entry : nameGroups.entrySet()) {
            String name = entry.getKey();
            List<CsdlEntitySet> entitySetList = entry.getValue();
            
            if (entitySetList.size() == 1) {
                result.add(entitySetList.get(0));
            } else {
                if (!areEntitySetsCompatible(entitySetList)) {
                    ConflictInfo conflict = new ConflictInfo(ConflictType.ENTITY_SET, name, 
                        "Incompatible EntitySet definitions for: " + name, entitySetList.get(0), entitySetList.get(1));
                    conflicts.add(conflict);
                    
                    if (resolution == ConflictResolution.SKIP_CONFLICTS) {
                        warnings.add("Skipping conflicting EntitySet: " + name);
                        continue;
                    }
                }
                
                result.add(resolveEntitySetConflict(entitySetList, resolution, warnings));
            }
        }
        
        return result;
    }
    
    private List<CsdlSingleton> processSingletons(List<CsdlSingleton> singletons, ConflictResolution resolution, 
                                                 List<ConflictInfo> conflicts, List<String> warnings) {
        Map<String, List<CsdlSingleton>> nameGroups = new LinkedHashMap<>();
        
        for (CsdlSingleton singleton : singletons) {
            if (singleton != null && singleton.getName() != null) {
                nameGroups.computeIfAbsent(singleton.getName(), k -> new ArrayList<>()).add(singleton);
            }
        }
        
        List<CsdlSingleton> result = new ArrayList<>();
        
        for (Map.Entry<String, List<CsdlSingleton>> entry : nameGroups.entrySet()) {
            String name = entry.getKey();
            List<CsdlSingleton> singletonList = entry.getValue();
            
            if (singletonList.size() == 1) {
                result.add(singletonList.get(0));
            } else {
                ConflictInfo conflict = new ConflictInfo(ConflictType.SINGLETON, name, 
                    "Duplicate Singleton: " + name, singletonList.get(0), singletonList.get(1));
                conflicts.add(conflict);
                
                if (resolution == ConflictResolution.SKIP_CONFLICTS) {
                    warnings.add("Skipping conflicting Singleton: " + name);
                    continue;
                }
                
                result.add(resolveSingletonConflict(singletonList, resolution, warnings));
            }
        }
        
        return result;
    }
    
    private List<CsdlActionImport> processActionImports(List<CsdlActionImport> actionImports, ConflictResolution resolution, 
                                                       List<ConflictInfo> conflicts, List<String> warnings) {
        Map<String, List<CsdlActionImport>> nameGroups = new LinkedHashMap<>();
        
        for (CsdlActionImport actionImport : actionImports) {
            if (actionImport != null && actionImport.getName() != null) {
                nameGroups.computeIfAbsent(actionImport.getName(), k -> new ArrayList<>()).add(actionImport);
            }
        }
        
        List<CsdlActionImport> result = new ArrayList<>();
        
        for (Map.Entry<String, List<CsdlActionImport>> entry : nameGroups.entrySet()) {
            String name = entry.getKey();
            List<CsdlActionImport> actionImportList = entry.getValue();
            
            if (actionImportList.size() == 1) {
                result.add(actionImportList.get(0));
            } else {
                ConflictInfo conflict = new ConflictInfo(ConflictType.ACTION_IMPORT, name, 
                    "Duplicate ActionImport: " + name, actionImportList.get(0), actionImportList.get(1));
                conflicts.add(conflict);
                
                if (resolution == ConflictResolution.SKIP_CONFLICTS) {
                    warnings.add("Skipping conflicting ActionImport: " + name);
                    continue;
                }
                
                result.add(resolveActionImportConflict(actionImportList, resolution, warnings));
            }
        }
        
        return result;
    }
    
    private List<CsdlFunctionImport> processFunctionImports(List<CsdlFunctionImport> functionImports, ConflictResolution resolution, 
                                                           List<ConflictInfo> conflicts, List<String> warnings) {
        Map<String, List<CsdlFunctionImport>> nameGroups = new LinkedHashMap<>();
        
        for (CsdlFunctionImport functionImport : functionImports) {
            if (functionImport != null && functionImport.getName() != null) {
                nameGroups.computeIfAbsent(functionImport.getName(), k -> new ArrayList<>()).add(functionImport);
            }
        }
        
        List<CsdlFunctionImport> result = new ArrayList<>();
        
        for (Map.Entry<String, List<CsdlFunctionImport>> entry : nameGroups.entrySet()) {
            String name = entry.getKey();
            List<CsdlFunctionImport> functionImportList = entry.getValue();
            
            if (functionImportList.size() == 1) {
                result.add(functionImportList.get(0));
            } else {
                ConflictInfo conflict = new ConflictInfo(ConflictType.FUNCTION_IMPORT, name, 
                    "Duplicate FunctionImport: " + name, functionImportList.get(0), functionImportList.get(1));
                conflicts.add(conflict);
                
                if (resolution == ConflictResolution.SKIP_CONFLICTS) {
                    warnings.add("Skipping conflicting FunctionImport: " + name);
                    continue;
                }
                
                result.add(resolveFunctionImportConflict(functionImportList, resolution, warnings));
            }
        }
        
        return result;
    }

    // Compatibility and signature checking methods
    
    private boolean areEntityTypesCompatible(List<CsdlEntityType> types) {
        if (types.size() <= 1) return true;
        
        CsdlEntityType first = types.get(0);
        for (int i = 1; i < types.size(); i++) {
            if (!areEntityTypesCompatible(first, types.get(i))) {
                return false;
            }
        }
        return true;
    }
    
    private boolean areComplexTypesCompatible(List<CsdlComplexType> types) {
        if (types.size() <= 1) return true;
        
        CsdlComplexType first = types.get(0);
        for (int i = 1; i < types.size(); i++) {
            if (!areComplexTypesCompatible(first, types.get(i))) {
                return false;
            }
        }
        return true;
    }
    
    private boolean areEnumTypesCompatible(List<CsdlEnumType> types) {
        if (types.size() <= 1) return true;
        
        CsdlEnumType first = types.get(0);
        for (int i = 1; i < types.size(); i++) {
            if (!areEnumTypesCompatible(first, types.get(i))) {
                return false;
            }
        }
        return true;
    }
    
    private boolean areTermsCompatible(List<CsdlTerm> terms) {
        if (terms.size() <= 1) return true;
        
        CsdlTerm first = terms.get(0);
        for (int i = 1; i < terms.size(); i++) {
            CsdlTerm other = terms.get(i);
            if (!Objects.equals(first.getType(), other.getType()) ||
                !Objects.equals(first.getBaseTerm(), other.getBaseTerm())) {
                return false;
            }
        }
        return true;
    }
    
    private boolean areTypeDefinitionsCompatible(List<CsdlTypeDefinition> typeDefs) {
        if (typeDefs.size() <= 1) return true;
        
        CsdlTypeDefinition first = typeDefs.get(0);
        for (int i = 1; i < typeDefs.size(); i++) {
            CsdlTypeDefinition other = typeDefs.get(i);
            if (!Objects.equals(first.getUnderlyingType(), other.getUnderlyingType())) {
                return false;
            }
        }
        return true;
    }
    
    private boolean areEntitySetsCompatible(List<CsdlEntitySet> entitySets) {
        if (entitySets.size() <= 1) return true;
        
        CsdlEntitySet first = entitySets.get(0);
        for (int i = 1; i < entitySets.size(); i++) {
            CsdlEntitySet other = entitySets.get(i);
            if (!Objects.equals(first.getType(), other.getType())) {
                return false;
            }
        }
        return true;
    }
    
    private String getActionSignature(CsdlAction action) {
        StringBuilder sig = new StringBuilder(action.getName());
        sig.append("(");
        if (action.getParameters() != null) {
            sig.append(action.getParameters().stream()
                .map(p -> p.getType())
                .collect(Collectors.joining(",")));
        }
        sig.append(")");
        return sig.toString();
    }
    
    private String getFunctionSignature(CsdlFunction function) {
        StringBuilder sig = new StringBuilder(function.getName());
        sig.append("(");
        if (function.getParameters() != null) {
            sig.append(function.getParameters().stream()
                .map(p -> p.getType())
                .collect(Collectors.joining(",")));
        }
        sig.append(")");
        if (function.getReturnType() != null) {
            sig.append(":").append(function.getReturnType().getType());
        }
        return sig.toString();
    }
    
    // Conflict resolution methods
    
    private CsdlEntityType resolveEntityTypeConflict(List<CsdlEntityType> types, ConflictResolution resolution, List<String> warnings) {
        switch (resolution) {
            case KEEP_FIRST:
                warnings.add("EntityType conflict resolved by keeping first: " + types.get(0).getName());
                return types.get(0);
            case KEEP_LAST:
                warnings.add("EntityType conflict resolved by keeping last: " + types.get(types.size() - 1).getName());
                return types.get(types.size() - 1);
            case AUTO_MERGE:
                warnings.add("EntityType conflict resolved by auto-merge: " + types.get(0).getName());
                return mergeEntityTypes(types);
            default:
                return types.get(0);
        }
    }
    
    private CsdlComplexType resolveComplexTypeConflict(List<CsdlComplexType> types, ConflictResolution resolution, List<String> warnings) {
        switch (resolution) {
            case KEEP_FIRST:
                warnings.add("ComplexType conflict resolved by keeping first: " + types.get(0).getName());
                return types.get(0);
            case KEEP_LAST:
                warnings.add("ComplexType conflict resolved by keeping last: " + types.get(types.size() - 1).getName());
                return types.get(types.size() - 1);
            case AUTO_MERGE:
                warnings.add("ComplexType conflict resolved by auto-merge: " + types.get(0).getName());
                return mergeComplexTypes(types);
            default:
                return types.get(0);
        }
    }
    
    private CsdlEnumType resolveEnumTypeConflict(List<CsdlEnumType> types, ConflictResolution resolution, List<String> warnings) {
        switch (resolution) {
            case KEEP_FIRST:
                warnings.add("EnumType conflict resolved by keeping first: " + types.get(0).getName());
                return types.get(0);
            case KEEP_LAST:
                warnings.add("EnumType conflict resolved by keeping last: " + types.get(types.size() - 1).getName());
                return types.get(types.size() - 1);
            case AUTO_MERGE:
                warnings.add("EnumType conflict resolved by auto-merge: " + types.get(0).getName());
                return mergeEnumTypes(types);
            default:
                return types.get(0);
        }
    }
    
    private CsdlAction resolveActionConflict(List<CsdlAction> actions, ConflictResolution resolution, List<String> warnings) {
        switch (resolution) {
            case KEEP_FIRST:
                warnings.add("Action conflict resolved by keeping first: " + actions.get(0).getName());
                return actions.get(0);
            case KEEP_LAST:
                warnings.add("Action conflict resolved by keeping last: " + actions.get(actions.size() - 1).getName());
                return actions.get(actions.size() - 1);
            default:
                return actions.get(0);
        }
    }
    
    private CsdlFunction resolveFunctionConflict(List<CsdlFunction> functions, ConflictResolution resolution, List<String> warnings) {
        switch (resolution) {
            case KEEP_FIRST:
                warnings.add("Function conflict resolved by keeping first: " + functions.get(0).getName());
                return functions.get(0);
            case KEEP_LAST:
                warnings.add("Function conflict resolved by keeping last: " + functions.get(functions.size() - 1).getName());
                return functions.get(functions.size() - 1);
            default:
                return functions.get(0);
        }
    }
    
    private CsdlTerm resolveTermConflict(List<CsdlTerm> terms, ConflictResolution resolution, List<String> warnings) {
        switch (resolution) {
            case KEEP_FIRST:
                warnings.add("Term conflict resolved by keeping first: " + terms.get(0).getName());
                return terms.get(0);
            case KEEP_LAST:
                warnings.add("Term conflict resolved by keeping last: " + terms.get(terms.size() - 1).getName());
                return terms.get(terms.size() - 1);
            default:
                return terms.get(0);
        }
    }
    
    private CsdlTypeDefinition resolveTypeDefinitionConflict(List<CsdlTypeDefinition> typeDefs, ConflictResolution resolution, List<String> warnings) {
        switch (resolution) {
            case KEEP_FIRST:
                warnings.add("TypeDefinition conflict resolved by keeping first: " + typeDefs.get(0).getName());
                return typeDefs.get(0);
            case KEEP_LAST:
                warnings.add("TypeDefinition conflict resolved by keeping last: " + typeDefs.get(typeDefs.size() - 1).getName());
                return typeDefs.get(typeDefs.size() - 1);
            default:
                return typeDefs.get(0);
        }
    }
    
    private CsdlEntitySet resolveEntitySetConflict(List<CsdlEntitySet> entitySets, ConflictResolution resolution, List<String> warnings) {
        switch (resolution) {
            case KEEP_FIRST:
                warnings.add("EntitySet conflict resolved by keeping first: " + entitySets.get(0).getName());
                return entitySets.get(0);
            case KEEP_LAST:
                warnings.add("EntitySet conflict resolved by keeping last: " + entitySets.get(entitySets.size() - 1).getName());
                return entitySets.get(entitySets.size() - 1);
            default:
                return entitySets.get(0);
        }
    }
    
    private CsdlSingleton resolveSingletonConflict(List<CsdlSingleton> singletons, ConflictResolution resolution, List<String> warnings) {
        switch (resolution) {
            case KEEP_FIRST:
                warnings.add("Singleton conflict resolved by keeping first: " + singletons.get(0).getName());
                return singletons.get(0);
            case KEEP_LAST:
                warnings.add("Singleton conflict resolved by keeping last: " + singletons.get(singletons.size() - 1).getName());
                return singletons.get(singletons.size() - 1);
            default:
                return singletons.get(0);
        }
    }
    
    private CsdlActionImport resolveActionImportConflict(List<CsdlActionImport> actionImports, ConflictResolution resolution, List<String> warnings) {
        switch (resolution) {
            case KEEP_FIRST:
                warnings.add("ActionImport conflict resolved by keeping first: " + actionImports.get(0).getName());
                return actionImports.get(0);
            case KEEP_LAST:
                warnings.add("ActionImport conflict resolved by keeping last: " + actionImports.get(actionImports.size() - 1).getName());
                return actionImports.get(actionImports.size() - 1);
            default:
                return actionImports.get(0);
        }
    }
    
    private CsdlFunctionImport resolveFunctionImportConflict(List<CsdlFunctionImport> functionImports, ConflictResolution resolution, List<String> warnings) {
        switch (resolution) {
            case KEEP_FIRST:
                warnings.add("FunctionImport conflict resolved by keeping first: " + functionImports.get(0).getName());
                return functionImports.get(0);
            case KEEP_LAST:
                warnings.add("FunctionImport conflict resolved by keeping last: " + functionImports.get(functionImports.size() - 1).getName());
                return functionImports.get(functionImports.size() - 1);
            default:
                return functionImports.get(0);
        }
    }
    
    // Auto-merge methods for compatible types
    
    private CsdlEntityType mergeEntityTypes(List<CsdlEntityType> types) {
        // Simple merge - take first and merge properties
        CsdlEntityType merged = new CsdlEntityType();
        CsdlEntityType first = types.get(0);
        
        merged.setName(first.getName());
        merged.setBaseType(first.getBaseType());
        merged.setAbstract(first.isAbstract());
        merged.setHasStream(first.hasStream());
        
        // Merge properties from all types
        List<CsdlProperty> allProperties = new ArrayList<>();
        for (CsdlEntityType type : types) {
            if (type.getProperties() != null) {
                allProperties.addAll(type.getProperties());
            }
        }
        
        // Remove duplicate properties by name
        Map<String, CsdlProperty> uniqueProps = new LinkedHashMap<>();
        for (CsdlProperty prop : allProperties) {
            uniqueProps.putIfAbsent(prop.getName(), prop);
        }
        
        merged.setProperties(new ArrayList<>(uniqueProps.values()));
        return merged;
    }
    
    private CsdlComplexType mergeComplexTypes(List<CsdlComplexType> types) {
        CsdlComplexType merged = new CsdlComplexType();
        CsdlComplexType first = types.get(0);
        
        merged.setName(first.getName());
        merged.setBaseType(first.getBaseType());
        merged.setAbstract(first.isAbstract());
        
        // Merge properties from all types
        List<CsdlProperty> allProperties = new ArrayList<>();
        for (CsdlComplexType type : types) {
            if (type.getProperties() != null) {
                allProperties.addAll(type.getProperties());
            }
        }
        
        // Remove duplicate properties by name
        Map<String, CsdlProperty> uniqueProps = new LinkedHashMap<>();
        for (CsdlProperty prop : allProperties) {
            uniqueProps.putIfAbsent(prop.getName(), prop);
        }
        
        merged.setProperties(new ArrayList<>(uniqueProps.values()));
        return merged;
    }
    
    private CsdlEnumType mergeEnumTypes(List<CsdlEnumType> types) {
        // For enum types, just take the first one as they should be compatible
        return types.get(0);
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
    
    // Updated compatibility check methods with detailed conflict information
    
    private void checkEntityTypeCompatibility(CsdlSchema schema1, CsdlSchema schema2, List<String> conflicts, 
                                            List<String> warnings, List<ConflictInfo> detailedConflicts) {
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
                    detailedConflicts.add(new ConflictInfo(ConflictType.ENTITY_TYPE, typeName, 
                        "Incompatible EntityType definitions", type1, type2));
                } else {
                    warnings.add("EntityType " + typeName + " exists in both schemas");
                }
            }
        }
    }
    
    private void checkComplexTypeCompatibility(CsdlSchema schema1, CsdlSchema schema2, List<String> conflicts, 
                                             List<String> warnings, List<ConflictInfo> detailedConflicts) {
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
                    detailedConflicts.add(new ConflictInfo(ConflictType.COMPLEX_TYPE, typeName, 
                        "Incompatible ComplexType definitions", type1, type2));
                } else {
                    warnings.add("ComplexType " + typeName + " exists in both schemas");
                }
            }
        }
    }
    
    private void checkEnumTypeCompatibility(CsdlSchema schema1, CsdlSchema schema2, List<String> conflicts, 
                                          List<String> warnings, List<ConflictInfo> detailedConflicts) {
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
                    detailedConflicts.add(new ConflictInfo(ConflictType.ENUM_TYPE, typeName, 
                        "Incompatible EnumType definitions", type1, type2));
                } else {
                    warnings.add("EnumType " + typeName + " exists in both schemas");
                }
            }
        }
    }
    
    private void checkActionCompatibility(CsdlSchema schema1, CsdlSchema schema2, List<String> conflicts, 
                                        List<String> warnings, List<ConflictInfo> detailedConflicts) {
        if (schema1.getActions() == null || schema2.getActions() == null) {
            return;
        }
        
        Map<String, CsdlAction> actions1 = schema1.getActions().stream()
                .collect(Collectors.toMap(a -> getActionSignature(a), a -> a));
        Map<String, CsdlAction> actions2 = schema2.getActions().stream()
                .collect(Collectors.toMap(a -> getActionSignature(a), a -> a));
        
        for (String signature : actions1.keySet()) {
            if (actions2.containsKey(signature)) {
                CsdlAction action1 = actions1.get(signature);
                CsdlAction action2 = actions2.get(signature);
                
                conflicts.add("Duplicate Action: " + signature);
                detailedConflicts.add(new ConflictInfo(ConflictType.ACTION, action1.getName(), 
                    "Duplicate Action with signature: " + signature, action1, action2));
                warnings.add("Action " + action1.getName() + " exists in both schemas");
            }
        }
    }
    
    private void checkFunctionCompatibility(CsdlSchema schema1, CsdlSchema schema2, List<String> conflicts, 
                                          List<String> warnings, List<ConflictInfo> detailedConflicts) {
        if (schema1.getFunctions() == null || schema2.getFunctions() == null) {
            return;
        }
        
        Map<String, CsdlFunction> functions1 = schema1.getFunctions().stream()
                .collect(Collectors.toMap(f -> getFunctionSignature(f), f -> f));
        Map<String, CsdlFunction> functions2 = schema2.getFunctions().stream()
                .collect(Collectors.toMap(f -> getFunctionSignature(f), f -> f));
        
        for (String signature : functions1.keySet()) {
            if (functions2.containsKey(signature)) {
                CsdlFunction function1 = functions1.get(signature);
                CsdlFunction function2 = functions2.get(signature);
                
                conflicts.add("Duplicate Function: " + signature);
                detailedConflicts.add(new ConflictInfo(ConflictType.FUNCTION, function1.getName(), 
                    "Duplicate Function with signature: " + signature, function1, function2));
                warnings.add("Function " + function1.getName() + " exists in both schemas");
            }
        }
    }
    
    private void checkTermCompatibility(CsdlSchema schema1, CsdlSchema schema2, List<String> conflicts, 
                                      List<String> warnings, List<ConflictInfo> detailedConflicts) {
        if (schema1.getTerms() == null || schema2.getTerms() == null) {
            return;
        }
        
        Map<String, CsdlTerm> terms1 = schema1.getTerms().stream()
                .collect(Collectors.toMap(CsdlTerm::getName, t -> t));
        Map<String, CsdlTerm> terms2 = schema2.getTerms().stream()
                .collect(Collectors.toMap(CsdlTerm::getName, t -> t));
        
        for (String termName : terms1.keySet()) {
            if (terms2.containsKey(termName)) {
                CsdlTerm term1 = terms1.get(termName);
                CsdlTerm term2 = terms2.get(termName);
                
                if (!Objects.equals(term1.getType(), term2.getType()) || 
                    !Objects.equals(term1.getBaseTerm(), term2.getBaseTerm())) {
                    conflicts.add("Incompatible Term: " + termName);
                    detailedConflicts.add(new ConflictInfo(ConflictType.TERM, termName, 
                        "Incompatible Term definitions", term1, term2));
                } else {
                    warnings.add("Term " + termName + " exists in both schemas");
                }
            }
        }
    }
    
    private void checkTypeDefinitionCompatibility(CsdlSchema schema1, CsdlSchema schema2, List<String> conflicts, 
                                                List<String> warnings, List<ConflictInfo> detailedConflicts) {
        if (schema1.getTypeDefinitions() == null || schema2.getTypeDefinitions() == null) {
            return;
        }
        
        Map<String, CsdlTypeDefinition> typeDefs1 = schema1.getTypeDefinitions().stream()
                .collect(Collectors.toMap(CsdlTypeDefinition::getName, t -> t));
        Map<String, CsdlTypeDefinition> typeDefs2 = schema2.getTypeDefinitions().stream()
                .collect(Collectors.toMap(CsdlTypeDefinition::getName, t -> t));
        
        for (String typeDefName : typeDefs1.keySet()) {
            if (typeDefs2.containsKey(typeDefName)) {
                CsdlTypeDefinition typeDef1 = typeDefs1.get(typeDefName);
                CsdlTypeDefinition typeDef2 = typeDefs2.get(typeDefName);
                
                if (!Objects.equals(typeDef1.getUnderlyingType(), typeDef2.getUnderlyingType())) {
                    conflicts.add("Incompatible TypeDefinition: " + typeDefName);
                    detailedConflicts.add(new ConflictInfo(ConflictType.TYPE_DEFINITION, typeDefName, 
                        "Incompatible TypeDefinition definitions", typeDef1, typeDef2));
                } else {
                    warnings.add("TypeDefinition " + typeDefName + " exists in both schemas");
                }
            }
        }
    }
    
    private void checkEntityContainerCompatibility(CsdlSchema schema1, CsdlSchema schema2, List<String> conflicts, 
                                                  List<String> warnings, List<ConflictInfo> detailedConflicts) {
        if (schema1.getEntityContainer() == null || schema2.getEntityContainer() == null) {
            return;
        }
        
        CsdlEntityContainer container1 = schema1.getEntityContainer();
        CsdlEntityContainer container2 = schema2.getEntityContainer();
        
        if (!Objects.equals(container1.getName(), container2.getName())) {
            warnings.add("Different EntityContainer names: " + container1.getName() + " vs " + container2.getName());
            detailedConflicts.add(new ConflictInfo(ConflictType.ENTITY_CONTAINER, container1.getName(), 
                "EntityContainer name conflict", container1, container2));
        } else {
            warnings.add("Both schemas have EntityContainer: " + container1.getName());
        }
    }
    
    // Existing entity types compatibility check method (maintained for internal use)
    
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
