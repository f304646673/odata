package org.apache.olingo.compliance.impl;

import org.apache.olingo.compliance.api.NamespaceSchemaRepository;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Default implementation of NamespaceSchemaRepository using in-memory storage.
 */
public class DefaultNamespaceSchemaRepository implements NamespaceSchemaRepository {
    
    private final ConcurrentHashMap<String, NamespaceEntry> namespaceMap = new ConcurrentHashMap<>();
    
    private static class NamespaceEntry {
        final CsdlSchema mergedSchema;
        final List<String> sourceFilePaths;
        
        NamespaceEntry(CsdlSchema mergedSchema) {
            this.mergedSchema = mergedSchema;
            this.sourceFilePaths = new CopyOnWriteArrayList<>();
        }
    }
    
    @Override
    public void mergeSchema(CsdlSchema schema, String sourceFilePath) {
        String namespace = schema.getNamespace();
        if (namespace == null) {
            return;
        }
        
        namespaceMap.compute(namespace, (key, existing) -> {
            if (existing == null) {
                NamespaceEntry entry = new NamespaceEntry(schema);
                entry.sourceFilePaths.add(sourceFilePath);
                return entry;
            } else {
                // Perform actual schema merging
                CsdlSchema mergedSchema = mergeSchemas(existing.mergedSchema, schema);
                NamespaceEntry newEntry = new NamespaceEntry(mergedSchema);
                newEntry.sourceFilePaths.addAll(existing.sourceFilePaths);
                newEntry.sourceFilePaths.add(sourceFilePath);
                return newEntry;
            }
        });
    }
    
    /**
     * Merges two schemas with the same namespace.
     * Combines entity types, complex types, enums, actions, functions, etc.
     */
    private CsdlSchema mergeSchemas(CsdlSchema existing, CsdlSchema newSchema) {
        CsdlSchema merged = new CsdlSchema();
        merged.setNamespace(existing.getNamespace());
        merged.setAlias(existing.getAlias() != null ? existing.getAlias() : newSchema.getAlias());
        
        // Merge entity types
        merged.setEntityTypes(mergeEntityTypes(existing.getEntityTypes(), newSchema.getEntityTypes()));
        
        // Merge complex types
        merged.setComplexTypes(mergeComplexTypes(existing.getComplexTypes(), newSchema.getComplexTypes()));
        
        // Merge enumerations
        merged.setEnumTypes(mergeEnumTypes(existing.getEnumTypes(), newSchema.getEnumTypes()));
        
        // Merge type definitions
        merged.setTypeDefinitions(mergeTypeDefinitions(existing.getTypeDefinitions(), newSchema.getTypeDefinitions()));
        
        // Merge actions
        merged.setActions(mergeActions(existing.getActions(), newSchema.getActions()));
        
        // Merge functions  
        merged.setFunctions(mergeFunctions(existing.getFunctions(), newSchema.getFunctions()));
        
        // Merge terms
        merged.setTerms(mergeTerms(existing.getTerms(), newSchema.getTerms()));
        
        // Merge entity container (should be unique per namespace)
        if (existing.getEntityContainer() != null) {
            merged.setEntityContainer(existing.getEntityContainer());
        } else if (newSchema.getEntityContainer() != null) {
            merged.setEntityContainer(newSchema.getEntityContainer());
        }
        
        return merged;
    }
    
    private java.util.List<org.apache.olingo.commons.api.edm.provider.CsdlEntityType> mergeEntityTypes(
            java.util.List<org.apache.olingo.commons.api.edm.provider.CsdlEntityType> existing,
            java.util.List<org.apache.olingo.commons.api.edm.provider.CsdlEntityType> newTypes) {
        
        java.util.List<org.apache.olingo.commons.api.edm.provider.CsdlEntityType> merged = new ArrayList<>();
        java.util.Map<String, org.apache.olingo.commons.api.edm.provider.CsdlEntityType> typeMap = new HashMap<>();
        
        // Add existing types
        if (existing != null) {
            for (org.apache.olingo.commons.api.edm.provider.CsdlEntityType type : existing) {
                typeMap.put(type.getName(), type);
            }
        }
        
        // Add new types (overwrite if same name)
        if (newTypes != null) {
            for (org.apache.olingo.commons.api.edm.provider.CsdlEntityType type : newTypes) {
                typeMap.put(type.getName(), type);
            }
        }
        
        merged.addAll(typeMap.values());
        return merged;
    }
    
    private java.util.List<org.apache.olingo.commons.api.edm.provider.CsdlComplexType> mergeComplexTypes(
            java.util.List<org.apache.olingo.commons.api.edm.provider.CsdlComplexType> existing,
            java.util.List<org.apache.olingo.commons.api.edm.provider.CsdlComplexType> newTypes) {
        
        java.util.List<org.apache.olingo.commons.api.edm.provider.CsdlComplexType> merged = new ArrayList<>();
        java.util.Map<String, org.apache.olingo.commons.api.edm.provider.CsdlComplexType> typeMap = new HashMap<>();
        
        if (existing != null) {
            for (org.apache.olingo.commons.api.edm.provider.CsdlComplexType type : existing) {
                typeMap.put(type.getName(), type);
            }
        }
        
        if (newTypes != null) {
            for (org.apache.olingo.commons.api.edm.provider.CsdlComplexType type : newTypes) {
                typeMap.put(type.getName(), type);
            }
        }
        
        merged.addAll(typeMap.values());
        return merged;
    }
    
    private java.util.List<org.apache.olingo.commons.api.edm.provider.CsdlEnumType> mergeEnumTypes(
            java.util.List<org.apache.olingo.commons.api.edm.provider.CsdlEnumType> existing,
            java.util.List<org.apache.olingo.commons.api.edm.provider.CsdlEnumType> newTypes) {
        
        java.util.List<org.apache.olingo.commons.api.edm.provider.CsdlEnumType> merged = new ArrayList<>();
        java.util.Map<String, org.apache.olingo.commons.api.edm.provider.CsdlEnumType> typeMap = new HashMap<>();
        
        if (existing != null) {
            for (org.apache.olingo.commons.api.edm.provider.CsdlEnumType type : existing) {
                typeMap.put(type.getName(), type);
            }
        }
        
        if (newTypes != null) {
            for (org.apache.olingo.commons.api.edm.provider.CsdlEnumType type : newTypes) {
                typeMap.put(type.getName(), type);
            }
        }
        
        merged.addAll(typeMap.values());
        return merged;
    }
    
    private java.util.List<org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition> mergeTypeDefinitions(
            java.util.List<org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition> existing,
            java.util.List<org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition> newTypes) {
        
        java.util.List<org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition> merged = new ArrayList<>();
        java.util.Map<String, org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition> typeMap = new HashMap<>();
        
        if (existing != null) {
            for (org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition type : existing) {
                typeMap.put(type.getName(), type);
            }
        }
        
        if (newTypes != null) {
            for (org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition type : newTypes) {
                typeMap.put(type.getName(), type);
            }
        }
        
        merged.addAll(typeMap.values());
        return merged;
    }
    
    private java.util.List<org.apache.olingo.commons.api.edm.provider.CsdlAction> mergeActions(
            java.util.List<org.apache.olingo.commons.api.edm.provider.CsdlAction> existing,
            java.util.List<org.apache.olingo.commons.api.edm.provider.CsdlAction> newActions) {
        
        java.util.List<org.apache.olingo.commons.api.edm.provider.CsdlAction> merged = new ArrayList<>();
        
        if (existing != null) {
            merged.addAll(existing);
        }
        
        if (newActions != null) {
            merged.addAll(newActions);
        }
        
        return merged;
    }
    
    private java.util.List<org.apache.olingo.commons.api.edm.provider.CsdlFunction> mergeFunctions(
            java.util.List<org.apache.olingo.commons.api.edm.provider.CsdlFunction> existing,
            java.util.List<org.apache.olingo.commons.api.edm.provider.CsdlFunction> newFunctions) {
        
        java.util.List<org.apache.olingo.commons.api.edm.provider.CsdlFunction> merged = new ArrayList<>();
        
        if (existing != null) {
            merged.addAll(existing);
        }
        
        if (newFunctions != null) {
            merged.addAll(newFunctions);
        }
        
        return merged;
    }
    
    private java.util.List<org.apache.olingo.commons.api.edm.provider.CsdlTerm> mergeTerms(
            java.util.List<org.apache.olingo.commons.api.edm.provider.CsdlTerm> existing,
            java.util.List<org.apache.olingo.commons.api.edm.provider.CsdlTerm> newTerms) {
        
        java.util.List<org.apache.olingo.commons.api.edm.provider.CsdlTerm> merged = new ArrayList<>();
        java.util.Map<String, org.apache.olingo.commons.api.edm.provider.CsdlTerm> termMap = new HashMap<>();
        
        if (existing != null) {
            for (org.apache.olingo.commons.api.edm.provider.CsdlTerm term : existing) {
                termMap.put(term.getName(), term);
            }
        }
        
        if (newTerms != null) {
            for (org.apache.olingo.commons.api.edm.provider.CsdlTerm term : newTerms) {
                termMap.put(term.getName(), term);
            }
        }
        
        merged.addAll(termMap.values());
        return merged;
    }
    
    public Optional<CsdlSchema> getSchemaByNamespace(String namespace) {
        NamespaceEntry entry = namespaceMap.get(namespace);
        return entry != null ? Optional.of(entry.mergedSchema) : Optional.empty();
    }
    
    public Set<String> getAllNamespaces() {
        return new HashSet<>(namespaceMap.keySet());
    }
    
    public List<CsdlSchema> getAllSchemas() {
        List<CsdlSchema> schemas = new ArrayList<>();
        for (NamespaceEntry entry : namespaceMap.values()) {
            schemas.add(entry.mergedSchema);
        }
        return schemas;
    }
    
    public boolean containsNamespace(String namespace) {
        return namespaceMap.containsKey(namespace);
    }
    
    public List<String> getSourceFilePaths(String namespace) {
        NamespaceEntry entry = namespaceMap.get(namespace);
        return entry != null ? new ArrayList<>(entry.sourceFilePaths) : Collections.emptyList();
    }
    
    public boolean removeNamespace(String namespace) {
        return namespaceMap.remove(namespace) != null;
    }
    
    public Set<String> removeSourceFilePath(String sourceFilePath) {
        Set<String> affectedNamespaces = new HashSet<>();
        Iterator<Map.Entry<String, NamespaceEntry>> iterator = namespaceMap.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<String, NamespaceEntry> entry = iterator.next();
            String namespace = entry.getKey();
            NamespaceEntry namespaceEntry = entry.getValue();
            
            if (namespaceEntry.sourceFilePaths.remove(sourceFilePath)) {
                affectedNamespaces.add(namespace);
                // If no more source files, remove the namespace entirely
                if (namespaceEntry.sourceFilePaths.isEmpty()) {
                    iterator.remove();
                }
            }
        }
        
        return affectedNamespaces;
    }
    
    public void clear() {
        namespaceMap.clear();
    }
    
    public int size() {
        return namespaceMap.size();
    }
}
