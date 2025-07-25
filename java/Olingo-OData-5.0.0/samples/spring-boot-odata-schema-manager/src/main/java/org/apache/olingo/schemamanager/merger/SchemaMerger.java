package org.apache.olingo.schemamanager.merger;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;

/**
 * Schema merger interface
 * Responsible for merging schemas grouped by namespace
 * Supports comprehensive schema element merging including EntityTypes, ComplexTypes, 
 * EnumTypes, Actions, Functions, Terms, TypeDefinitions, and EntityContainers
 */
public interface SchemaMerger {
    
    /**
     * Merge multiple schemas, grouping by namespace and merging schemas with the same namespace
     * Uses default conflict resolution (THROW_ERROR)
     * @param schemas List of schemas to merge (can have different namespaces)
     * @return Merge result with detailed information containing one merged schema per namespace
     */
    MergeResult mergeSchemas(List<CsdlSchema> schemas);
    
    /**
     * Merge multiple schemas, grouping by namespace and merging schemas with the same namespace
     * @param schemas List of schemas to merge (can have different namespaces)
     * @param resolution Conflict resolution strategy
     * @return Merge result with detailed information containing one merged schema per namespace
     */
    MergeResult mergeSchemas(List<CsdlSchema> schemas, ConflictResolution resolution);
    
    
    /**
     * Merge result class containing detailed merge information for multiple namespaces
     */
    class MergeResult {
        private final List<CsdlSchema> mergedSchemas;
        private final List<String> warnings;
        private final List<String> errors;
        private final List<ConflictInfo> conflicts;
        private final boolean success;
        
        public MergeResult(List<CsdlSchema> mergedSchemas, List<String> warnings, List<String> errors, 
                          List<ConflictInfo> conflicts, boolean success) {
            this.mergedSchemas = mergedSchemas != null ? new ArrayList<>(mergedSchemas) : new ArrayList<>();
            this.warnings = warnings != null ? warnings : new ArrayList<>();
            this.errors = errors != null ? errors : new ArrayList<>();
            this.conflicts = conflicts != null ? conflicts : new ArrayList<>();
            this.success = success;
        }
        
        // Getters
        public List<CsdlSchema> getMergedSchemas() { return new ArrayList<>(mergedSchemas); }
        public List<String> getWarnings() { return warnings; }
        public List<String> getErrors() { return errors; }
        public List<ConflictInfo> getConflicts() { return conflicts; }
        public boolean isSuccess() { return success; }
        
        /**
         * Get merged schema for a specific namespace
         * @param namespace The namespace to search for
         * @return The merged schema for the namespace, or null if not found
         */
        public CsdlSchema getSchemaByNamespace(String namespace) {
            for (CsdlSchema schema : mergedSchemas) {
                if ((namespace == null && schema.getNamespace() == null) ||
                    (namespace != null && namespace.equals(schema.getNamespace()))) {
                    return schema;
                }
            }
            return null;
        }
        
        /**
         * Get all namespaces in the merge result
         * @return List of namespaces
         */
        public List<String> getNamespaces() {
            List<String> namespaces = new ArrayList<>();
            for (CsdlSchema schema : mergedSchemas) {
                namespaces.add(schema.getNamespace());
            }
            return namespaces;
        }
    }
    
    /**
     * Detailed conflict information
     */
    class ConflictInfo {
        private final ConflictType type;
        private final String elementName;
        private final String description;
        private final Object existingElement;
        private final Object conflictingElement;
        private final String namespace;
        
        public ConflictInfo(ConflictType type, String elementName, String description, 
                           Object existingElement, Object conflictingElement, String namespace) {
            this.type = type;
            this.elementName = elementName;
            this.description = description;
            this.existingElement = existingElement;
            this.conflictingElement = conflictingElement;
            this.namespace = namespace;
        }
        
        // Backward compatibility constructor
        public ConflictInfo(ConflictType type, String elementName, String description, 
                           Object existingElement, Object conflictingElement) {
            this(type, elementName, description, existingElement, conflictingElement, null);
        }
        
        // Getters
        public ConflictType getType() { return type; }
        public String getElementName() { return elementName; }
        public String getDescription() { return description; }
        public Object getExistingElement() { return existingElement; }
        public Object getConflictingElement() { return conflictingElement; }
        public String getNamespace() { return namespace; }
    }
    
    /**
     * Types of conflicts that can occur during schema merging
     */
    enum ConflictType {
        ENTITY_TYPE,
        COMPLEX_TYPE, 
        ENUM_TYPE,
        ACTION,
        FUNCTION,
        TERM,
        TYPE_DEFINITION,
        ENTITY_CONTAINER,
        ENTITY_SET,
        SINGLETON,
        ACTION_IMPORT,
        FUNCTION_IMPORT,
        PROPERTY,
        NAVIGATION_PROPERTY,
        PARAMETER,
        RETURN_TYPE
    }
    
    /**
     * Conflict resolution strategies
     */
    enum ConflictResolution {
        /** Keep the first encountered definition */
        KEEP_FIRST,
        /** Keep the last encountered definition */
        KEEP_LAST,
        /** Throw error when conflicts are detected */
        THROW_ERROR,
        /** Attempt automatic merge when possible */
        AUTO_MERGE,
        /** Skip conflicting elements */
        SKIP_CONFLICTS
    }
}
