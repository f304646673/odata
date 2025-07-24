package org.apache.olingo.schemamanager.repository.impl;

import org.apache.olingo.commons.api.edm.provider.*;
import org.apache.olingo.schemamanager.repository.SchemaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of SchemaRepository using thread-safe data structures.
 */
@Component
public class InMemorySchemaRepository implements SchemaRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(InMemorySchemaRepository.class);
    
    // Thread-safe storage for schemas and types
    private final Map<String, CsdlSchema> schemas = new ConcurrentHashMap<>();
    private final Map<String, String> schemaFilePaths = new ConcurrentHashMap<>();
    
    // Index for types
    private final Map<String, CsdlEntityType> entityTypes = new ConcurrentHashMap<>();
    private final Map<String, CsdlComplexType> complexTypes = new ConcurrentHashMap<>();
    private final Map<String, CsdlEnumType> enumTypes = new ConcurrentHashMap<>();
    private final Map<String, CsdlEntityContainer> containers = new ConcurrentHashMap<>();
    
    @Override
    public void addSchema(CsdlSchema schema, String filePath) {
        if (schema == null) {
            logger.warn("Attempted to add null schema");
            return;
        }
        
        String namespace = schema.getNamespace();
        if (namespace == null) {
            logger.warn("Schema has null namespace, skipping");
            return;
        }
        
        logger.debug("Adding schema with namespace: {} from file: {}", namespace, filePath);
        schemas.put(namespace, schema);
        schemaFilePaths.put(namespace, filePath);
        
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
        
        // Index EntityContainer
        if (schema.getEntityContainer() != null) {
            String fullName = namespace + "." + schema.getEntityContainer().getName();
            containers.put(fullName, schema.getEntityContainer());
        }
        
        logger.debug("Successfully indexed schema: {} with {} entity types, {} complex types, {} enum types", 
                    namespace, 
                    schema.getEntityTypes() != null ? schema.getEntityTypes().size() : 0,
                    schema.getComplexTypes() != null ? schema.getComplexTypes().size() : 0,
                    schema.getEnumTypes() != null ? schema.getEnumTypes().size() : 0);
    }

    @Override
    public CsdlSchema getSchema(String namespace) {
        return schemas.get(namespace);
    }

    @Override
    public Map<String, CsdlSchema> getAllSchemas() {
        return new HashMap<>(schemas);
    }

    @Override
    public CsdlEntityType getEntityType(String fullQualifiedName) {
        return entityTypes.get(fullQualifiedName);
    }

    @Override
    public CsdlComplexType getComplexType(String fullQualifiedName) {
        return complexTypes.get(fullQualifiedName);
    }

    @Override
    public CsdlEnumType getEnumType(String fullQualifiedName) {
        return enumTypes.get(fullQualifiedName);
    }

    @Override
    public CsdlEntityType getEntityType(String namespace, String typeName) {
        return getEntityType(namespace + "." + typeName);
    }

    @Override
    public CsdlComplexType getComplexType(String namespace, String typeName) {
        return getComplexType(namespace + "." + typeName);
    }

    @Override
    public CsdlEnumType getEnumType(String namespace, String typeName) {
        return getEnumType(namespace + "." + typeName);
    }

    @Override
    public List<CsdlEntityType> getEntityTypes(String namespace) {
        return entityTypes.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(namespace + "."))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public List<CsdlComplexType> getComplexTypes(String namespace) {
        return complexTypes.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(namespace + "."))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public List<CsdlEnumType> getEnumTypes(String namespace) {
        return enumTypes.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(namespace + "."))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public Set<String> getAllNamespaces() {
        return new HashSet<>(schemas.keySet());
    }

    @Override
    public String getSchemaFilePath(String namespace) {
        return schemaFilePaths.get(namespace);
    }

    @Override
    public void clear() {
        logger.info("Clearing repository");
        schemas.clear();
        schemaFilePaths.clear();
        entityTypes.clear();
        complexTypes.clear();
        enumTypes.clear();
        containers.clear();
    }

    @Override
    public RepositoryStatistics getStatistics() {
        return new RepositoryStatistics(
                schemas.size(),
                entityTypes.size(),
                complexTypes.size(),
                enumTypes.size(),
                containers.size()
        );
    }
}
