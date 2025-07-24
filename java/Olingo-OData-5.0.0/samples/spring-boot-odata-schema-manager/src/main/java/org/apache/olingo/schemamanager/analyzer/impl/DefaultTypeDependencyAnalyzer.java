package org.apache.olingo.schemamanager.analyzer.impl;

import org.apache.olingo.commons.api.edm.provider.*;
import org.apache.olingo.schemamanager.analyzer.TypeDependencyAnalyzer;
import org.apache.olingo.schemamanager.repository.SchemaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Default implementation of TypeDependencyAnalyzer
 */
@Component
public class DefaultTypeDependencyAnalyzer implements TypeDependencyAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultTypeDependencyAnalyzer.class);
    
    @Autowired
    private SchemaRepository repository;
    
    @Override
    public List<TypeReference> getDirectDependencies(CsdlEntityType entityType) {
        List<TypeReference> dependencies = new ArrayList<>();
        
        // Check base type
        if (entityType.getBaseType() != null) {
            dependencies.add(new TypeReference(
                entityType.getBaseType(), 
                TypeKind.ENTITY_TYPE, 
                null, 
                false
            ));
        }
        
        // Check property types
        if (entityType.getProperties() != null) {
            for (CsdlProperty property : entityType.getProperties()) {
                addPropertyDependencies(property, dependencies);
            }
        }
        
        // Check navigation properties
        if (entityType.getNavigationProperties() != null) {
            for (CsdlNavigationProperty navProp : entityType.getNavigationProperties()) {
                addNavigationPropertyDependencies(navProp, dependencies);
            }
        }
        
        return dependencies;
    }
    
    @Override
    public List<TypeReference> getDirectDependencies(CsdlComplexType complexType) {
        List<TypeReference> dependencies = new ArrayList<>();
        
        // Check base type
        if (complexType.getBaseType() != null) {
            dependencies.add(new TypeReference(
                complexType.getBaseType(), 
                TypeKind.COMPLEX_TYPE, 
                null, 
                false
            ));
        }
        
        // Check property types
        if (complexType.getProperties() != null) {
            for (CsdlProperty property : complexType.getProperties()) {
                addPropertyDependencies(property, dependencies);
            }
        }
        
        return dependencies;
    }
    
    @Override
    public List<TypeReference> getAllDependencies(CsdlEntityType entityType) {
        Set<TypeReference> allDeps = new HashSet<>();
        Set<String> visited = new HashSet<>();
        
        String typeName = getFullTypeName(entityType);
        collectAllDependenciesRecursive(typeName, allDeps, visited);
        
        return new ArrayList<>(allDeps);
    }
    
    @Override
    public List<TypeReference> getAllDependencies(CsdlComplexType complexType) {
        Set<TypeReference> allDeps = new HashSet<>();
        Set<String> visited = new HashSet<>();
        
        String typeName = getFullTypeName(complexType);
        collectAllDependenciesRecursive(typeName, allDeps, visited);
        
        return new ArrayList<>(allDeps);
    }
    
    @Override
    public List<TypeReference> getDependents(String fullQualifiedName) {
        List<TypeReference> dependents = new ArrayList<>();
        
        // Check all schemas for types that depend on the given type
        repository.getAllSchemas().values().forEach(schema -> {
            String namespace = schema.getNamespace();
            
            // Check EntityTypes
            if (schema.getEntityTypes() != null) {
                for (CsdlEntityType entityType : schema.getEntityTypes()) {
                    List<TypeReference> deps = getDirectDependencies(entityType);
                    for (TypeReference dep : deps) {
                        if (dep.getFullQualifiedName().equals(fullQualifiedName)) {
                            dependents.add(new TypeReference(
                                namespace + "." + entityType.getName(),
                                TypeKind.ENTITY_TYPE,
                                dep.getPropertyName(),
                                false
                            ));
                        }
                    }
                }
            }
            
            // Check ComplexTypes
            if (schema.getComplexTypes() != null) {
                for (CsdlComplexType complexType : schema.getComplexTypes()) {
                    List<TypeReference> deps = getDirectDependencies(complexType);
                    for (TypeReference dep : deps) {
                        if (dep.getFullQualifiedName().equals(fullQualifiedName)) {
                            dependents.add(new TypeReference(
                                namespace + "." + complexType.getName(),
                                TypeKind.COMPLEX_TYPE,
                                dep.getPropertyName(),
                                false
                            ));
                        }
                    }
                }
            }
        });
        
        return dependents;
    }
    
    @Override
    public boolean hasDependency(String sourceType, String targetType) {
        CsdlEntityType entityType = repository.getEntityType(sourceType);
        if (entityType != null) {
            return getAllDependencies(entityType).stream()
                    .anyMatch(dep -> dep.getFullQualifiedName().equals(targetType));
        }
        
        CsdlComplexType complexType = repository.getComplexType(sourceType);
        if (complexType != null) {
            return getAllDependencies(complexType).stream()
                    .anyMatch(dep -> dep.getFullQualifiedName().equals(targetType));
        }
        
        return false;
    }
    
    @Override
    public List<String> getDependencyPath(String sourceType, String targetType) {
        List<String> path = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        
        if (findDependencyPath(sourceType, targetType, path, visited)) {
            return path;
        }
        
        return Collections.emptyList();
    }
    
    @Override
    public List<CircularDependency> detectCircularDependencies() {
        List<CircularDependency> cycles = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        
        // Check all types for circular dependencies
        repository.getAllSchemas().values().forEach(schema -> {
            String namespace = schema.getNamespace();
            
            // Check EntityTypes
            if (schema.getEntityTypes() != null) {
                for (CsdlEntityType entityType : schema.getEntityTypes()) {
                    String fullName = namespace + "." + entityType.getName();
                    if (!visited.contains(fullName)) {
                        List<String> currentPath = new ArrayList<>();
                        Set<String> recursionStack = new HashSet<>();
                        findCircularDependenciesRecursive(fullName, visited, recursionStack, currentPath, cycles);
                    }
                }
            }
            
            // Check ComplexTypes
            if (schema.getComplexTypes() != null) {
                for (CsdlComplexType complexType : schema.getComplexTypes()) {
                    String fullName = namespace + "." + complexType.getName();
                    if (!visited.contains(fullName)) {
                        List<String> currentPath = new ArrayList<>();
                        Set<String> recursionStack = new HashSet<>();
                        findCircularDependenciesRecursive(fullName, visited, recursionStack, currentPath, cycles);
                    }
                }
            }
        });
        
        return cycles;
    }
    
    @Override
    public DependencyGraph buildDependencyGraph(CsdlEntityContainer entityContainer) {
        Set<String> allTypes = new HashSet<>();
        List<TypeReference> allDependencies = new ArrayList<>();
        
        if (entityContainer.getEntitySets() != null) {
            for (CsdlEntitySet entitySet : entityContainer.getEntitySets()) {
                String entityTypeName = entitySet.getType();
                allTypes.add(entityTypeName);
                
                // Get entity type and its dependencies
                CsdlEntityType entityType = repository.getEntityType(entityTypeName);
                if (entityType != null) {
                    List<TypeReference> deps = getAllDependencies(entityType);
                    allDependencies.addAll(deps);
                    
                    // Add the dependencies to types set
                    deps.forEach(dep -> allTypes.add(dep.getFullQualifiedName()));
                }
            }
        }
        
        return new DependencyGraph(allTypes, allDependencies, entityContainer);
    }
    
    @Override
    public DependencyGraph buildCustomDependencyGraph(List<EntitySetDefinition> entitySetDefinitions) {
        // Create a custom EntityContainer
        CsdlEntityContainer customContainer = new CsdlEntityContainer();
        customContainer.setName("CustomContainer");
        
        List<CsdlEntitySet> entitySets = new ArrayList<>();
        Set<String> allTypes = new HashSet<>();
        List<TypeReference> allDependencies = new ArrayList<>();
        
        for (EntitySetDefinition def : entitySetDefinitions) {
            CsdlEntitySet entitySet = new CsdlEntitySet();
            entitySet.setName(def.getEntitySetName());
            entitySet.setType(def.getEntityTypeName());
            entitySets.add(entitySet);
            
            allTypes.add(def.getEntityTypeName());
            
            // Get entity type and its dependencies
            CsdlEntityType entityType = repository.getEntityType(def.getEntityTypeName());
            if (entityType != null) {
                List<TypeReference> deps = getAllDependencies(entityType);
                allDependencies.addAll(deps);
                
                // Add the dependencies to types set
                deps.forEach(dep -> allTypes.add(dep.getFullQualifiedName()));
            }
        }
        
        customContainer.setEntitySets(entitySets);
        
        return new DependencyGraph(allTypes, allDependencies, customContainer);
    }
    
    // Private helper methods
    
    private void addPropertyDependencies(CsdlProperty property, List<TypeReference> dependencies) {
        String propType = property.getType();
        if (isCustomType(propType)) {
            String actualType = extractTypeFromCollection(propType);
            TypeKind typeKind = determineTypeKind(actualType);
            boolean isCollection = propType.startsWith("Collection(");
            
            dependencies.add(new TypeReference(
                actualType,
                typeKind,
                property.getName(),
                isCollection
            ));
        }
    }
    
    private void addNavigationPropertyDependencies(CsdlNavigationProperty navProp, List<TypeReference> dependencies) {
        String navType = navProp.getType();
        if (isCustomType(navType)) {
            String actualType = extractTypeFromCollection(navType);
            boolean isCollection = navType.startsWith("Collection(");
            
            dependencies.add(new TypeReference(
                actualType,
                TypeKind.ENTITY_TYPE,
                navProp.getName(),
                isCollection
            ));
        }
    }
    
    private void collectAllDependenciesRecursive(String typeName, Set<TypeReference> allDeps, Set<String> visited) {
        if (visited.contains(typeName)) {
            return; // Avoid infinite recursion
        }
        
        visited.add(typeName);
        
        // Get direct dependencies
        List<TypeReference> directDeps = getDirectDependenciesForType(typeName);
        allDeps.addAll(directDeps);
        
        // Recursively collect dependencies
        for (TypeReference dep : directDeps) {
            collectAllDependenciesRecursive(dep.getFullQualifiedName(), allDeps, visited);
        }
    }
    
    private List<TypeReference> getDirectDependenciesForType(String typeName) {
        CsdlEntityType entityType = repository.getEntityType(typeName);
        if (entityType != null) {
            return getDirectDependencies(entityType);
        }
        
        CsdlComplexType complexType = repository.getComplexType(typeName);
        if (complexType != null) {
            return getDirectDependencies(complexType);
        }
        
        return Collections.emptyList();
    }
    
    private boolean findDependencyPath(String sourceType, String targetType, List<String> path, Set<String> visited) {
        if (visited.contains(sourceType)) {
            return false;
        }
        
        visited.add(sourceType);
        path.add(sourceType);
        
        if (sourceType.equals(targetType)) {
            return true;
        }
        
        List<TypeReference> dependencies = getDirectDependenciesForType(sourceType);
        for (TypeReference dep : dependencies) {
            if (findDependencyPath(dep.getFullQualifiedName(), targetType, path, visited)) {
                return true;
            }
        }
        
        path.remove(path.size() - 1);
        return false;
    }
    
    private void findCircularDependenciesRecursive(String typeName, Set<String> visited, 
                                                 Set<String> recursionStack, List<String> currentPath, 
                                                 List<CircularDependency> cycles) {
        if (recursionStack.contains(typeName)) {
            // Found a cycle
            int cycleStart = currentPath.indexOf(typeName);
            List<String> cycle = new ArrayList<>(currentPath.subList(cycleStart, currentPath.size()));
            cycle.add(typeName); // Complete the cycle
            cycles.add(new CircularDependency(cycle));
            return;
        }
        
        if (visited.contains(typeName)) {
            return;
        }
        
        visited.add(typeName);
        recursionStack.add(typeName);
        currentPath.add(typeName);
        
        List<TypeReference> dependencies = getDirectDependenciesForType(typeName);
        for (TypeReference dep : dependencies) {
            findCircularDependenciesRecursive(dep.getFullQualifiedName(), visited, recursionStack, currentPath, cycles);
        }
        
        recursionStack.remove(typeName);
        currentPath.remove(currentPath.size() - 1);
    }
    
    private String getFullTypeName(CsdlEntityType entityType) {
        // Find the namespace for this entity type
        for (CsdlSchema schema : repository.getAllSchemas().values()) {
            if (schema.getEntityTypes() != null) {
                for (CsdlEntityType et : schema.getEntityTypes()) {
                    if (et == entityType) {
                        return schema.getNamespace() + "." + entityType.getName();
                    }
                }
            }
        }
        return entityType.getName(); // fallback
    }
    
    private String getFullTypeName(CsdlComplexType complexType) {
        // Find the namespace for this complex type
        for (CsdlSchema schema : repository.getAllSchemas().values()) {
            if (schema.getComplexTypes() != null) {
                for (CsdlComplexType ct : schema.getComplexTypes()) {
                    if (ct == complexType) {
                        return schema.getNamespace() + "." + complexType.getName();
                    }
                }
            }
        }
        return complexType.getName(); // fallback
    }
    
    private boolean isCustomType(String typeName) {
        // Check if it's not a primitive type
        return typeName != null && 
               !typeName.startsWith("Edm.") && 
               !typeName.startsWith("Collection(Edm.");
    }
    
    private String extractTypeFromCollection(String typeName) {
        if (typeName.startsWith("Collection(") && typeName.endsWith(")")) {
            return typeName.substring(11, typeName.length() - 1);
        }
        return typeName;
    }
    
    private TypeKind determineTypeKind(String typeName) {
        if (repository.getEntityType(typeName) != null) {
            return TypeKind.ENTITY_TYPE;
        } else if (repository.getComplexType(typeName) != null) {
            return TypeKind.COMPLEX_TYPE;
        } else if (repository.getEnumType(typeName) != null) {
            return TypeKind.ENUM_TYPE;
        } else {
            return TypeKind.PRIMITIVE_TYPE;
        }
    }
}
