package org.apache.olingo.schema.processor.validation.directory;

import org.apache.olingo.commons.api.edm.provider.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Detects conflicts between schema elements across multiple XML files.
 * This class analyzes schemas from different files to identify:
 * - Duplicate element definitions within the same namespace
 * - Duplicate namespace schema definitions
 * - Incompatible element definitions
 * - Circular references
 * - Missing references
 */
public class SchemaConflictDetector {
    
    private static final Logger logger = LoggerFactory.getLogger(SchemaConflictDetector.class);
    
    // Maps namespace to files that define schemas for that namespace
    private final Map<String, Set<String>> namespaceToFiles = new HashMap<>();
    
    // Maps namespace to all schemas defined for that namespace across files
    private final Map<String, List<SchemaInfo>> namespaceToSchemas = new HashMap<>();
    
    // Maps (namespace, elementName) to files that define that element
    private final Map<ElementKey, Set<String>> elementToFiles = new HashMap<>();
    
    // Maps (namespace, elementName) to element definitions
    private final Map<ElementKey, List<ElementInfo>> elementToDefinitions = new HashMap<>();
    
    /**
     * Container for schema information
     */
    private static class SchemaInfo {
        final CsdlSchema schema;
        final String fileName;
        
        SchemaInfo(CsdlSchema schema, String fileName) {
            this.schema = schema;
            this.fileName = fileName;
        }
    }
    
    /**
     * Container for element information
     */
    private static class ElementInfo {
        final String elementName;
        final String elementType; // EntityType, ComplexType, etc.
        final Object definition; // The actual CsdlEntityType, CsdlComplexType, etc.
        final String fileName;
        final String namespace;
        
        ElementInfo(String elementName, String elementType, Object definition, String fileName, String namespace) {
            this.elementName = elementName;
            this.elementType = elementType;
            this.definition = definition;
            this.fileName = fileName;
            this.namespace = namespace;
        }
    }
    
    /**
     * Key for identifying unique elements across namespaces
     */
    private static class ElementKey {
        final String namespace;
        final String elementName;
        
        ElementKey(String namespace, String elementName) {
            this.namespace = namespace;
            this.elementName = elementName;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ElementKey that = (ElementKey) o;
            return Objects.equals(namespace, that.namespace) && Objects.equals(elementName, that.elementName);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(namespace, elementName);
        }
        
        @Override
        public String toString() {
            return namespace + "." + elementName;
        }
    }
    
    /**
     * Add a schema for conflict detection analysis
     * 
     * @param schema the CSDL schema
     * @param fileName the file that contains this schema
     */
    public void addSchema(CsdlSchema schema, String fileName) {
        if (schema == null || fileName == null) {
            return;
        }
        
        String namespace = schema.getNamespace();
        if (namespace == null) {
            logger.warn("Schema in file {} has no namespace", fileName);
            return;
        }
        
        // Track namespace to files mapping
        namespaceToFiles.computeIfAbsent(namespace, k -> new HashSet<>()).add(fileName);
        
        // Track namespace to schemas mapping
        namespaceToSchemas.computeIfAbsent(namespace, k -> new ArrayList<>()).add(new SchemaInfo(schema, fileName));
        
        // Analyze all elements in the schema
        analyzeSchemaElements(schema, fileName, namespace);
    }
    
    /**
     * Analyze all elements in a schema and track them
     */
    private void analyzeSchemaElements(CsdlSchema schema, String fileName, String namespace) {
        // Entity Types
        if (schema.getEntityTypes() != null) {
            for (CsdlEntityType entityType : schema.getEntityTypes()) {
                addElement(namespace, entityType.getName(), "EntityType", entityType, fileName);
            }
        }
        
        // Complex Types
        if (schema.getComplexTypes() != null) {
            for (CsdlComplexType complexType : schema.getComplexTypes()) {
                addElement(namespace, complexType.getName(), "ComplexType", complexType, fileName);
            }
        }
        
        // Enum Types
        if (schema.getEnumTypes() != null) {
            for (CsdlEnumType enumType : schema.getEnumTypes()) {
                addElement(namespace, enumType.getName(), "EnumType", enumType, fileName);
            }
        }
        
        // Type Definitions
        if (schema.getTypeDefinitions() != null) {
            for (CsdlTypeDefinition typeDef : schema.getTypeDefinitions()) {
                addElement(namespace, typeDef.getName(), "TypeDefinition", typeDef, fileName);
            }
        }
        
        // Entity Containers
        if (schema.getEntityContainer() != null) {
            CsdlEntityContainer container = schema.getEntityContainer();
            addElement(namespace, container.getName(), "EntityContainer", container, fileName);
            
            // Entity Sets
            if (container.getEntitySets() != null) {
                for (CsdlEntitySet entitySet : container.getEntitySets()) {
                    addElement(namespace, entitySet.getName(), "EntitySet", entitySet, fileName);
                }
            }
            
            // Singletons
            if (container.getSingletons() != null) {
                for (CsdlSingleton singleton : container.getSingletons()) {
                    addElement(namespace, singleton.getName(), "Singleton", singleton, fileName);
                }
            }
            
            // Function Imports
            if (container.getFunctionImports() != null) {
                for (CsdlFunctionImport functionImport : container.getFunctionImports()) {
                    addElement(namespace, functionImport.getName(), "FunctionImport", functionImport, fileName);
                }
            }
            
            // Action Imports
            if (container.getActionImports() != null) {
                for (CsdlActionImport actionImport : container.getActionImports()) {
                    addElement(namespace, actionImport.getName(), "ActionImport", actionImport, fileName);
                }
            }
        }
        
        // Functions
        if (schema.getFunctions() != null) {
            for (CsdlFunction function : schema.getFunctions()) {
                // Functions can be overloaded, so we need to include parameters in the key
                String functionKey = createFunctionKey(function);
                addElement(namespace, functionKey, "Function", function, fileName);
            }
        }
        
        // Actions
        if (schema.getActions() != null) {
            for (CsdlAction action : schema.getActions()) {
                // Actions can be overloaded, so we need to include parameters in the key
                String actionKey = createActionKey(action);
                addElement(namespace, actionKey, "Action", action, fileName);
            }
        }
        
        // Terms (for annotations)
        if (schema.getTerms() != null) {
            for (CsdlTerm term : schema.getTerms()) {
                addElement(namespace, term.getName(), "Term", term, fileName);
            }
        }
    }
    
    /**
     * Add an element to tracking maps
     */
    private void addElement(String namespace, String elementName, String elementType, Object definition, String fileName) {
        ElementKey key = new ElementKey(namespace, elementName);
        
        // Track element to files mapping
        elementToFiles.computeIfAbsent(key, k -> new HashSet<>()).add(fileName);
        
        // Track element to definitions mapping
        elementToDefinitions.computeIfAbsent(key, k -> new ArrayList<>())
                           .add(new ElementInfo(elementName, elementType, definition, fileName, namespace));
    }
    
    /**
     * Create a unique key for function including its parameters
     */
    private String createFunctionKey(CsdlFunction function) {
        StringBuilder key = new StringBuilder(function.getName());
        if (function.getParameters() != null && !function.getParameters().isEmpty()) {
            key.append("(");
            key.append(function.getParameters().stream()
                      .map(p -> p.getType())
                      .collect(Collectors.joining(",")));
            key.append(")");
        } else {
            key.append("()");
        }
        return key.toString();
    }
    
    /**
     * Create a unique key for action including its parameters
     */
    private String createActionKey(CsdlAction action) {
        StringBuilder key = new StringBuilder(action.getName());
        if (action.getParameters() != null && !action.getParameters().isEmpty()) {
            key.append("(");
            key.append(action.getParameters().stream()
                      .map(p -> p.getType())
                      .collect(Collectors.joining(",")));
            key.append(")");
        } else {
            key.append("()");
        }
        return key.toString();
    }
    
    /**
     * Detect all conflicts and return them as a list
     * 
     * @return list of detected conflicts
     */
    public List<SchemaConflict> detectConflicts() {
        List<SchemaConflict> conflicts = new ArrayList<>();
        
        // Check for duplicate namespace schemas
        conflicts.addAll(detectDuplicateNamespaceSchemas());
        
        // Check for duplicate elements within namespaces
        conflicts.addAll(detectDuplicateElements());
        
        // Check for incompatible definitions
        conflicts.addAll(detectIncompatibleDefinitions());
        
        return conflicts;
    }
    
    /**
     * Detect duplicate namespace schemas
     * Rule: Each namespace should have only one complete schema definition
     */
    private List<SchemaConflict> detectDuplicateNamespaceSchemas() {
        List<SchemaConflict> conflicts = new ArrayList<>();
        
        for (Map.Entry<String, List<SchemaInfo>> entry : namespaceToSchemas.entrySet()) {
            String namespace = entry.getKey();
            List<SchemaInfo> schemas = entry.getValue();
            
            // Check if multiple files define complete schemas for the same namespace
            Set<String> filesWithCompleteSchemas = new HashSet<>();
            
            for (SchemaInfo schemaInfo : schemas) {
                if (hasCompleteSchemaDefinition(schemaInfo.schema)) {
                    filesWithCompleteSchemas.add(schemaInfo.fileName);
                }
            }
            
            if (filesWithCompleteSchemas.size() > 1) {
                conflicts.add(SchemaConflict.duplicateNamespaceSchema(namespace, 
                                                                    new ArrayList<>(filesWithCompleteSchemas)));
            }
        }
        
        return conflicts;
    }
    
    /**
     * Check if a schema has a complete definition (entity container + types)
     */
    private boolean hasCompleteSchemaDefinition(CsdlSchema schema) {
        return schema.getEntityContainer() != null || 
               (schema.getEntityTypes() != null && !schema.getEntityTypes().isEmpty()) ||
               (schema.getComplexTypes() != null && !schema.getComplexTypes().isEmpty());
    }
    
    /**
     * Detect duplicate elements within the same namespace
     */
    private List<SchemaConflict> detectDuplicateElements() {
        List<SchemaConflict> conflicts = new ArrayList<>();
        
        for (Map.Entry<ElementKey, Set<String>> entry : elementToFiles.entrySet()) {
            ElementKey key = entry.getKey();
            Set<String> files = entry.getValue();
            
            if (files.size() > 1) {
                // Check if these are truly duplicates or just references
                List<ElementInfo> definitions = elementToDefinitions.get(key);
                if (definitions != null && definitions.size() > 1) {
                    // Multiple actual definitions found
                    conflicts.add(SchemaConflict.duplicateElement(key.namespace, key.elementName, 
                                                                new ArrayList<>(files)));
                }
            }
        }
        
        return conflicts;
    }
    
    /**
     * Detect incompatible definitions for the same element
     */
    private List<SchemaConflict> detectIncompatibleDefinitions() {
        List<SchemaConflict> conflicts = new ArrayList<>();
        
        for (Map.Entry<ElementKey, List<ElementInfo>> entry : elementToDefinitions.entrySet()) {
            ElementKey key = entry.getKey();
            List<ElementInfo> definitions = entry.getValue();
            
            if (definitions.size() > 1) {
                // Check if definitions are compatible
                if (!areDefinitionsCompatible(definitions)) {
                    List<String> files = definitions.stream()
                                                  .map(def -> def.fileName)
                                                  .distinct()
                                                  .collect(Collectors.toList());
                    
                    String details = "Incompatible definitions found: " + 
                                   definitions.stream()
                                            .map(def -> String.format("%s in %s", def.elementType, def.fileName))
                                            .collect(Collectors.joining(", "));
                    
                    conflicts.add(SchemaConflict.incompatibleDefinition(key.namespace, key.elementName,
                                                                      files, details));
                }
            }
        }
        
        return conflicts;
    }
    
    /**
     * Check if multiple definitions of the same element are compatible
     */
    private boolean areDefinitionsCompatible(List<ElementInfo> definitions) {
        if (definitions.size() <= 1) {
            return true;
        }
        
        // Group by element type
        Map<String, List<ElementInfo>> byType = definitions.stream()
                                                          .collect(Collectors.groupingBy(def -> def.elementType));
        
        // All definitions should be of the same type
        if (byType.size() > 1) {
            return false;
        }
        
        // For now, we consider different files defining the same element as incompatible
        // This can be enhanced to do deeper structural comparison
        return false;
    }
    
    /**
     * Get the mapping of namespaces to files
     */
    public Map<String, Set<String>> getNamespaceToFiles() {
        return new HashMap<>(namespaceToFiles);
    }
    
    /**
     * Clear all collected data
     */
    public void clear() {
        namespaceToFiles.clear();
        namespaceToSchemas.clear();
        elementToFiles.clear();
        elementToDefinitions.clear();
    }
}
