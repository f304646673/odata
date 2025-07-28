package org.apache.olingo.xml.validator;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;

/**
 * Repository for storing and managing OData schemas with dependency tracking.
 * Provides functionality to store schemas, resolve dependencies, and detect conflicts.
 */
public class SchemaRepository {
    
    // Schema storage by namespace
    private final Map<String, CsdlSchema> schemasByNamespace;
    
    // File path mappings
    private final Map<String, Path> namespaceToFilePath;
    private final Map<Path, String> filePathToNamespace;
    
    // Type registries
    private final Map<String, CsdlEntityType> entityTypes;       // Full qualified name -> EntityType
    private final Map<String, CsdlComplexType> complexTypes;     // Full qualified name -> ComplexType
    private final Map<String, CsdlEnumType> enumTypes;           // Full qualified name -> EnumType
    private final Map<String, CsdlTypeDefinition> typeDefinitions; // Full qualified name -> TypeDefinition
    private final Map<String, CsdlEntityContainer> entityContainers; // Full qualified name -> EntityContainer
    
    // Dependency tracking
    private final Map<String, Set<String>> dependencies;         // namespace -> set of dependent namespaces
    private final Map<String, Set<String>> dependents;           // namespace -> set of namespaces that depend on it
    
    /**
     * Creates a new empty schema repository.
     */
    public SchemaRepository() {
        this.schemasByNamespace = new HashMap<>();
        this.namespaceToFilePath = new HashMap<>();
        this.filePathToNamespace = new HashMap<>();
        this.entityTypes = new HashMap<>();
        this.complexTypes = new HashMap<>();
        this.enumTypes = new HashMap<>();
        this.typeDefinitions = new HashMap<>();
        this.entityContainers = new HashMap<>();
        this.dependencies = new HashMap<>();
        this.dependents = new HashMap<>();
    }
    
    /**
     * Adds a schema to the repository.
     * 
     * @param schema the schema to add
     * @param filePath the file path where the schema was loaded from
     * @throws IllegalArgumentException if schema is null or namespace already exists
     */
    public void addSchema(CsdlSchema schema, Path filePath) {
        if (schema == null) {
            throw new IllegalArgumentException("Schema cannot be null");
        }
        if (schema.getNamespace() == null || schema.getNamespace().trim().isEmpty()) {
            throw new IllegalArgumentException("Schema namespace cannot be null or empty");
        }
        
        String namespace = schema.getNamespace();
        
        // Check for namespace conflicts
        if (schemasByNamespace.containsKey(namespace)) {
            throw new IllegalArgumentException("Schema with namespace '" + namespace + "' already exists");
        }
        
        // Store schema
        schemasByNamespace.put(namespace, schema);
        namespaceToFilePath.put(namespace, filePath);
        filePathToNamespace.put(filePath, namespace);
        
        // Index all types from this schema
        indexSchemaTypes(schema);
    }
    
    /**
     * Indexes all types from a schema for quick lookup.
     */
    private void indexSchemaTypes(CsdlSchema schema) {
        String namespace = schema.getNamespace();
        
        // Index EntityTypes
        if (schema.getEntityTypes() != null) {
            for (CsdlEntityType entityType : schema.getEntityTypes()) {
                String fullName = namespace + "." + entityType.getName();
                entityTypes.put(fullName, entityType);
            }
        }
        
        // Index ComplexTypes
        if (schema.getComplexTypes() != null) {
            for (CsdlComplexType complexType : schema.getComplexTypes()) {
                String fullName = namespace + "." + complexType.getName();
                complexTypes.put(fullName, complexType);
            }
        }
        
        // Index EnumTypes
        if (schema.getEnumTypes() != null) {
            for (CsdlEnumType enumType : schema.getEnumTypes()) {
                String fullName = namespace + "." + enumType.getName();
                enumTypes.put(fullName, enumType);
            }
        }
        
        // Index TypeDefinitions
        if (schema.getTypeDefinitions() != null) {
            for (CsdlTypeDefinition typeDef : schema.getTypeDefinitions()) {
                String fullName = namespace + "." + typeDef.getName();
                typeDefinitions.put(fullName, typeDef);
            }
        }
        
        // Index EntityContainers
        if (schema.getEntityContainer() != null) {
            String fullName = namespace + "." + schema.getEntityContainer().getName();
            entityContainers.put(fullName, schema.getEntityContainer());
        }
    }
    
    /**
     * Adds a dependency relationship between two namespaces.
     * 
     * @param dependentNamespace the namespace that depends on another
     * @param dependencyNamespace the namespace being depended upon
     */
    public void addDependency(String dependentNamespace, String dependencyNamespace) {
        dependencies.computeIfAbsent(dependentNamespace, k -> new HashSet<>()).add(dependencyNamespace);
        dependents.computeIfAbsent(dependencyNamespace, k -> new HashSet<>()).add(dependentNamespace);
    }
    
    /**
     * Checks if a circular dependency exists.
     * 
     * @return true if circular dependencies are detected
     */
    public boolean hasCircularDependencies() {
        for (String namespace : schemasByNamespace.keySet()) {
            if (hasCircularDependency(namespace, new HashSet<>())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Recursively checks for circular dependencies starting from a namespace.
     */
    private boolean hasCircularDependency(String namespace, Set<String> visiting) {
        if (visiting.contains(namespace)) {
            return true; // Circular dependency detected
        }
        
        visiting.add(namespace);
        Set<String> deps = dependencies.get(namespace);
        if (deps != null) {
            for (String dep : deps) {
                if (hasCircularDependency(dep, visiting)) {
                    return true;
                }
            }
        }
        visiting.remove(namespace);
        return false;
    }
    
    /**
     * Gets all circular dependency chains.
     * 
     * @return list of circular dependency chains
     */
    public List<List<String>> getCircularDependencyChains() {
        List<List<String>> chains = new ArrayList<>();
        for (String namespace : schemasByNamespace.keySet()) {
            List<String> chain = findCircularDependencyChain(namespace, new ArrayList<>());
            if (chain != null && !chain.isEmpty()) {
                chains.add(chain);
            }
        }
        return chains;
    }
    
    /**
     * Finds a circular dependency chain starting from a namespace.
     */
    private List<String> findCircularDependencyChain(String namespace, List<String> path) {
        if (path.contains(namespace)) {
            // Found circular dependency, return the chain
            List<String> chain = new ArrayList<>();
            int startIndex = path.indexOf(namespace);
            for (int i = startIndex; i < path.size(); i++) {
                chain.add(path.get(i));
            }
            chain.add(namespace); // Complete the circle
            return chain;
        }
        
        path.add(namespace);
        Set<String> deps = dependencies.get(namespace);
        if (deps != null) {
            for (String dep : deps) {
                List<String> chain = findCircularDependencyChain(dep, new ArrayList<>(path));
                if (chain != null && !chain.isEmpty()) {
                    return chain;
                }
            }
        }
        return null;
    }
    
    // Getters and utility methods
    
    public CsdlSchema getSchema(String namespace) {
        return schemasByNamespace.get(namespace);
    }
    
    public Set<String> getAllNamespaces() {
        return Collections.unmodifiableSet(schemasByNamespace.keySet());
    }
    
    public Path getFilePath(String namespace) {
        return namespaceToFilePath.get(namespace);
    }
    
    public String getNamespace(Path filePath) {
        return filePathToNamespace.get(filePath);
    }
    
    public CsdlEntityType getEntityType(String fullQualifiedName) {
        return entityTypes.get(fullQualifiedName);
    }
    
    public CsdlComplexType getComplexType(String fullQualifiedName) {
        return complexTypes.get(fullQualifiedName);
    }
    
    public CsdlEnumType getEnumType(String fullQualifiedName) {
        return enumTypes.get(fullQualifiedName);
    }
    
    public CsdlTypeDefinition getTypeDefinition(String fullQualifiedName) {
        return typeDefinitions.get(fullQualifiedName);
    }
    
    public CsdlEntityContainer getEntityContainer(String fullQualifiedName) {
        return entityContainers.get(fullQualifiedName);
    }
    
    public Set<String> getDependencies(String namespace) {
        return dependencies.getOrDefault(namespace, Collections.emptySet());
    }
    
    public Set<String> getDependents(String namespace) {
        return dependents.getOrDefault(namespace, Collections.emptySet());
    }
    
    public boolean containsNamespace(String namespace) {
        return schemasByNamespace.containsKey(namespace);
    }
    
    public boolean isEmpty() {
        return schemasByNamespace.isEmpty();
    }
    
    public int size() {
        return schemasByNamespace.size();
    }
    
    /**
     * Checks if a type exists in the repository.
     * 
     * @param fullQualifiedName the full qualified type name
     * @return true if the type exists
     */
    public boolean typeExists(String fullQualifiedName) {
        return entityTypes.containsKey(fullQualifiedName) ||
               complexTypes.containsKey(fullQualifiedName) ||
               enumTypes.containsKey(fullQualifiedName) ||
               typeDefinitions.containsKey(fullQualifiedName);
    }
    
    @Override
    public String toString() {
        return "SchemaRepository{" +
                "schemas=" + schemasByNamespace.size() +
                ", entityTypes=" + entityTypes.size() +
                ", complexTypes=" + complexTypes.size() +
                ", enumTypes=" + enumTypes.size() +
                ", typeDefinitions=" + typeDefinitions.size() +
                ", entityContainers=" + entityContainers.size() +
                '}';
    }
}
