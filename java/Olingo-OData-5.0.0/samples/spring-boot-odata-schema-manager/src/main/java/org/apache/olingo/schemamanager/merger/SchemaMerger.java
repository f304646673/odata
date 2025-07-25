package org.apache.olingo.schemamanager.merger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;

/**
 * Schema merger interface
 * Responsible for merging Schema information with the same namespace
 * Supports comprehensive schema element merging including EntityTypes, ComplexTypes, 
 * EnumTypes, Actions, Functions, Terms, TypeDefinitions, and EntityContainers
 */
public interface SchemaMerger {
    
    /**
     * Merge multiple schemas with the same namespace
     * @param schemas List of schemas to merge
     * @return Merge result with detailed information
     */
    MergeResult mergeSchemas(List<CsdlSchema> schemas);
    
    /**
     * Merge multiple schemas with the same namespace using specified conflict resolution
     * @param schemas List of schemas to merge
     * @param resolution Conflict resolution strategy
     * @return Merge result with detailed information
     */
    MergeResult mergeSchemas(List<CsdlSchema> schemas, ConflictResolution resolution);
    
    /**
     * Merge all schemas grouped by namespace
     * @param schemaMap Schema mapping (filePath -> schema)
     * @return Merged schemas grouped by namespace
     */
    Map<String, CsdlSchema> mergeByNamespace(Map<String, CsdlSchema> schemaMap);
    
    /**
     * Merge all schemas grouped by namespace with specified conflict resolution
     * @param schemaMap Schema mapping (filePath -> schema)
     * @param resolution Conflict resolution strategy
     * @return Merged schemas grouped by namespace
     */
    Map<String, CsdlSchema> mergeByNamespace(Map<String, CsdlSchema> schemaMap, ConflictResolution resolution);
    
    /**
     * Check compatibility between two schemas
     * @param existingSchema Existing schema
     * @param newSchema New schema to merge
     * @return Compatibility check result
     */
    CompatibilityResult checkCompatibility(CsdlSchema existingSchema, CsdlSchema newSchema);
    
    /**
     * Resolve conflicts between schemas
     * @param conflictingSchemas List of conflicting schemas
     * @param resolution Conflict resolution strategy
     * @return Resolved schema
     */
    CsdlSchema resolveConflicts(List<CsdlSchema> conflictingSchemas, ConflictResolution resolution);
    
    
    /**
     * Merge result class containing detailed merge information
     */
    class MergeResult {
        private final CsdlSchema mergedSchema;
        private final List<String> warnings;
        private final List<String> errors;
        private final List<ConflictInfo> conflicts;
        private final boolean success;
        
        public MergeResult(CsdlSchema mergedSchema, List<String> warnings, List<String> errors, 
                          List<ConflictInfo> conflicts, boolean success) {
            this.mergedSchema = mergedSchema;
            this.warnings = warnings;
            this.errors = errors;
            this.conflicts = conflicts;
            this.success = success;
        }
        
        // Backward compatibility constructor
        public MergeResult(CsdlSchema mergedSchema, List<String> warnings, List<String> errors, boolean success) {
            this(mergedSchema, warnings, errors, new ArrayList<>(), success);
        }
        
        // Getters
        public CsdlSchema getMergedSchema() { return mergedSchema; }
        public List<String> getWarnings() { return warnings; }
        public List<String> getErrors() { return errors; }
        public List<ConflictInfo> getConflicts() { return conflicts; }
        public boolean isSuccess() { return success; }
    }
    
    /**
     * Compatibility check result class
     */
    class CompatibilityResult {
        private final boolean compatible;
        private final List<String> conflicts;
        private final List<String> warnings;
        private final List<ConflictInfo> detailedConflicts;
        
        public CompatibilityResult(boolean compatible, List<String> conflicts, List<String> warnings, 
                                 List<ConflictInfo> detailedConflicts) {
            this.compatible = compatible;
            this.conflicts = conflicts;
            this.warnings = warnings;
            this.detailedConflicts = detailedConflicts;
        }
        
        // Backward compatibility constructor
        public CompatibilityResult(boolean compatible, List<String> conflicts, List<String> warnings) {
            this(compatible, conflicts, warnings, new ArrayList<>());
        }
        
        // Getters
        public boolean isCompatible() { return compatible; }
        public List<String> getConflicts() { return conflicts; }
        public List<String> getWarnings() { return warnings; }
        public List<ConflictInfo> getDetailedConflicts() { return detailedConflicts; }
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
        
        public ConflictInfo(ConflictType type, String elementName, String description, 
                           Object existingElement, Object conflictingElement) {
            this.type = type;
            this.elementName = elementName;
            this.description = description;
            this.existingElement = existingElement;
            this.conflictingElement = conflictingElement;
        }
        
        // Getters
        public ConflictType getConflictType() { return type; }
        public ConflictType getType() { return type; }
        public String getElementName() { return elementName; }
        public String getDescription() { return description; }
        public String getMessage() { return description; }
        public Object getExistingElement() { return existingElement; }
        public Object getFirstElement() { return existingElement; }
        public Object getConflictingElement() { return conflictingElement; }
        public Object getSecondElement() { return conflictingElement; }
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
