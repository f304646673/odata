package org.apache.olingo.schema.processor.repository.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schema.processor.repository.SchemaRepository;

/**
 * 内存中的Schema仓库实现
 */
public class InMemorySchemaRepository implements SchemaRepository {
    
    private final Map<String, CsdlSchema> schemas = new ConcurrentHashMap<>();
    
    @Override
    public AddResult addSchema(CsdlSchema schema) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (schema == null) {
            errors.add("Schema cannot be null");
            return new AddResult(false, errors, warnings, 0, 0);
        }
        
        if (schema.getNamespace() == null) {
            errors.add("Schema namespace cannot be null");
            return new AddResult(false, errors, warnings, 0, 0);
        }
        
        int conflictCount = 0;
        if (schemas.containsKey(schema.getNamespace())) {
            conflictCount = 1;
            warnings.add("Schema with namespace " + schema.getNamespace() + " already exists, replacing");
        }
        
        schemas.put(schema.getNamespace(), schema);
        
        return new AddResult(true, errors, warnings, 1, conflictCount);
    }
    
    @Override
    public AddResult addSchemas(List<CsdlSchema> schemaList) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        int addedCount = 0;
        int conflictCount = 0;
        
        for (CsdlSchema schema : schemaList) {
            AddResult result = addSchema(schema);
            if (result.isSuccess()) {
                addedCount += result.getAddedCount();
                conflictCount += result.getConflictCount();
            }
            errors.addAll(result.getErrors());
            warnings.addAll(result.getWarnings());
        }
        
        return new AddResult(errors.isEmpty(), errors, warnings, addedCount, conflictCount);
    }
    
    @Override
    public CsdlSchema getSchema(String namespace) {
        return schemas.get(namespace);
    }
    
    @Override
    public List<CsdlSchema> getAllSchemas() {
        return new ArrayList<>(schemas.values());
    }
    
    @Override
    public Set<String> getAllNamespaces() {
        return new HashSet<>(schemas.keySet());
    }
    
    @Override
    public boolean containsNamespace(String namespace) {
        return schemas.containsKey(namespace);
    }
    
    @Override
    public boolean removeSchema(String namespace) {
        return schemas.remove(namespace) != null;
    }
    
    @Override
    public void clear() {
        schemas.clear();
    }
    
    @Override
    public MergeResult mergeSchemas(ConflictResolution conflictResolution) {
        // 基本实现 - 没有实际合并逻辑
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Map<String, List<String>> conflicts = new HashMap<>();
        
        return new MergeResult(true, errors, warnings, 0, conflicts);
    }
    
    @Override
    public ValidationResult validateAll() {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // 基本验证 - 检查所有Schema是否有有效的namespace
        for (CsdlSchema schema : schemas.values()) {
            if (schema.getNamespace() == null || schema.getNamespace().trim().isEmpty()) {
                errors.add("Schema has empty or null namespace");
            }
        }
        
        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }
}
