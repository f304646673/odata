package org.apache.olingo.advanced.xmlparser.schema;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;

/**
 * Registry of all available types across all schemas
 */
public class TypeRegistry {
    private final Set<String> entityTypes = new HashSet<>();
    private final Set<String> complexTypes = new HashSet<>();
    private final Set<String> enumTypes = new HashSet<>();
    private final Set<String> typeDefinitions = new HashSet<>();
    private final Set<String> functions = new HashSet<>();
    private final Set<String> actions = new HashSet<>();
    private final Set<String> containers = new HashSet<>();
    private final Set<String> targets = new HashSet<>();
    
    public TypeRegistry(List<CsdlSchema> schemas) {
        for (CsdlSchema schema : schemas) {
            registerSchemaTypes(schema);
        }
    }
    
    private void registerSchemaTypes(CsdlSchema schema) {
        String namespace = schema.getNamespace();
        
        // Register entity types
        if (schema.getEntityTypes() != null) {
            for (CsdlEntityType entityType : schema.getEntityTypes()) {
                String fullName = namespace + "." + entityType.getName();
                entityTypes.add(fullName);
                targets.add(fullName);
                
                // Register properties as potential targets
                if (entityType.getProperties() != null) {
                    for (CsdlProperty property : entityType.getProperties()) {
                        targets.add(fullName + "/" + property.getName());
                    }
                }
                
                // Register navigation properties as potential targets
                if (entityType.getNavigationProperties() != null) {
                    for (CsdlNavigationProperty navProp : entityType.getNavigationProperties()) {
                        targets.add(fullName + "/" + navProp.getName());
                    }
                }
            }
        }
        
        // Register complex types
        if (schema.getComplexTypes() != null) {
            for (CsdlComplexType complexType : schema.getComplexTypes()) {
                String fullName = namespace + "." + complexType.getName();
                complexTypes.add(fullName);
                targets.add(fullName);
                
                // Register properties as potential targets
                if (complexType.getProperties() != null) {
                    for (CsdlProperty property : complexType.getProperties()) {
                        targets.add(fullName + "/" + property.getName());
                    }
                }
            }
        }
        
        // Register enum types
        if (schema.getEnumTypes() != null) {
            for (CsdlEnumType enumType : schema.getEnumTypes()) {
                String fullName = namespace + "." + enumType.getName();
                enumTypes.add(fullName);
                targets.add(fullName);
            }
        }
        
        // Register type definitions
        if (schema.getTypeDefinitions() != null) {
            for (CsdlTypeDefinition typeDef : schema.getTypeDefinitions()) {
                String fullName = namespace + "." + typeDef.getName();
                typeDefinitions.add(fullName);
                targets.add(fullName);
            }
        }
        
        // Register functions
        if (schema.getFunctions() != null) {
            for (CsdlFunction function : schema.getFunctions()) {
                String fullName = namespace + "." + function.getName();
                functions.add(fullName);
                targets.add(fullName);
            }
        }
        
        // Register actions
        if (schema.getActions() != null) {
            for (CsdlAction action : schema.getActions()) {
                String fullName = namespace + "." + action.getName();
                actions.add(fullName);
                targets.add(fullName);
            }
        }
        
        // Register entity container
        if (schema.getEntityContainer() != null) {
            String fullName = namespace + "." + schema.getEntityContainer().getName();
            containers.add(fullName);
            targets.add(fullName);
        }
    }
    
    public boolean hasEntityType(String typeName) {
        return entityTypes.contains(typeName);
    }
    
    public boolean hasComplexType(String typeName) {
        return complexTypes.contains(typeName);
    }
    
    public boolean hasEnumType(String typeName) {
        return enumTypes.contains(typeName);
    }
    
    public boolean hasTypeDefinition(String typeName) {
        return typeDefinitions.contains(typeName);
    }
    
    public boolean hasFunction(String functionName) {
        return functions.contains(functionName);
    }
    
    public boolean hasAction(String actionName) {
        return actions.contains(actionName);
    }
    
    public boolean hasContainer(String containerName) {
        return containers.contains(containerName);
    }
    
    public boolean hasTarget(String targetName) {
        return targets.contains(targetName);
    }
    
    public boolean hasType(String typeName) {
        return hasEntityType(typeName) || 
                hasComplexType(typeName) || 
                hasEnumType(typeName) || 
                hasTypeDefinition(typeName);
    }
}