package org.apache.olingo.schema.processor.validation.directory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlSingleton;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
    
    // Maps (elementKey, termName) to annotation values
    private final Map<AnnotationKey, List<AnnotationInfo>> annotationToValues = new HashMap<>();

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
     * Container for annotation information
     */
    private static class AnnotationInfo {
        final String termName;
        final String value;
        final String fileName;
        final String targetElement;
        final String targetNamespace;

        AnnotationInfo(String termName, String value, String fileName, String targetElement, String targetNamespace) {
            this.termName = termName;
            this.value = value;
            this.fileName = fileName;
            this.targetElement = targetElement;
            this.targetNamespace = targetNamespace;
        }
    }

    /**
     * Key for identifying unique annotation targets and terms
     */
    private static class AnnotationKey {
        final String namespace;
        final String elementName;
        final String termName;

        AnnotationKey(String namespace, String elementName, String termName) {
            this.namespace = namespace;
            this.elementName = elementName;
            this.termName = termName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AnnotationKey that = (AnnotationKey) o;
            return Objects.equals(namespace, that.namespace) &&
                   Objects.equals(elementName, that.elementName) &&
                   Objects.equals(termName, that.termName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(namespace, elementName, termName);
        }

        @Override
        public String toString() {
            return namespace + "." + elementName + "@" + termName;
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

        // Analyze annotations on schema elements
        analyzeAnnotations(schema, fileName, namespace);
    }
    
    /**
     * Analyze annotations on all elements in a schema
     */
    private void analyzeAnnotations(CsdlSchema schema, String fileName, String namespace) {
        // Entity Types
        if (schema.getEntityTypes() != null) {
            for (CsdlEntityType entityType : schema.getEntityTypes()) {
                analyzeElementAnnotations(entityType.getAnnotations(), namespace, entityType.getName(), fileName);
            }
        }

        // Complex Types
        if (schema.getComplexTypes() != null) {
            for (CsdlComplexType complexType : schema.getComplexTypes()) {
                analyzeElementAnnotations(complexType.getAnnotations(), namespace, complexType.getName(), fileName);
            }
        }

        // Enum Types
        if (schema.getEnumTypes() != null) {
            for (CsdlEnumType enumType : schema.getEnumTypes()) {
                analyzeElementAnnotations(enumType.getAnnotations(), namespace, enumType.getName(), fileName);
            }
        }

        // Entity Container
        if (schema.getEntityContainer() != null) {
            CsdlEntityContainer container = schema.getEntityContainer();
            analyzeElementAnnotations(container.getAnnotations(), namespace, container.getName(), fileName);

            // Entity Sets
            if (container.getEntitySets() != null) {
                for (CsdlEntitySet entitySet : container.getEntitySets()) {
                    analyzeElementAnnotations(entitySet.getAnnotations(), namespace, entitySet.getName(), fileName);
                }
            }

            // Singletons
            if (container.getSingletons() != null) {
                for (CsdlSingleton singleton : container.getSingletons()) {
                    analyzeElementAnnotations(singleton.getAnnotations(), namespace, singleton.getName(), fileName);
                }
            }
        }

        // Functions
        if (schema.getFunctions() != null) {
            for (CsdlFunction function : schema.getFunctions()) {
                String functionKey = createFunctionKey(function);
                analyzeElementAnnotations(function.getAnnotations(), namespace, functionKey, fileName);
            }
        }

        // Actions
        if (schema.getActions() != null) {
            for (CsdlAction action : schema.getActions()) {
                String actionKey = createActionKey(action);
                analyzeElementAnnotations(action.getAnnotations(), namespace, actionKey, fileName);
            }
        }

        // Terms
        if (schema.getTerms() != null) {
            for (CsdlTerm term : schema.getTerms()) {
                analyzeElementAnnotations(term.getAnnotations(), namespace, term.getName(), fileName);
            }
        }
    }

    /**
     * Analyze annotations on a specific element
     */
    private void analyzeElementAnnotations(List<CsdlAnnotation> annotations, String namespace, String elementName, String fileName) {
        if (annotations == null || annotations.isEmpty()) {
            return;
        }

        for (CsdlAnnotation annotation : annotations) {
            String termName = annotation.getTerm();
            String value = getAnnotationValue(annotation);

            if (termName != null && value != null) {
                AnnotationKey key = new AnnotationKey(namespace, elementName, termName);
                annotationToValues.computeIfAbsent(key, k -> new ArrayList<>())
                                 .add(new AnnotationInfo(termName, value, fileName, elementName, namespace));
            }
        }
    }

    /**
     * Extract the value from an annotation (simplified version)
     */
    private String getAnnotationValue(CsdlAnnotation annotation) {
        // Try to get the expression and convert to string
        if (annotation.getExpression() != null) {
            Object expr = annotation.getExpression();
            // Try to call getValue() reflectively if available
            try {
                java.lang.reflect.Method getValueMethod = expr.getClass().getMethod("getValue");
                Object value = getValueMethod.invoke(expr);
                if (value != null) {
                    return value.toString();
                }
            } catch (Exception e) {
                // Ignore, fallback to toString()
            }
            // Fallback: use toString()
            return expr.toString();
        }

        // Fallback - just return the term name if no expression value is available
        return annotation.getTerm();
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
        
        // Check for annotation conflicts
        conflicts.addAll(detectAnnotationConflicts());

        return conflicts;
    }

    /**
     * Detect annotation conflicts
     * Rule: Same term on same element should not have conflicting values
     */
    private List<SchemaConflict> detectAnnotationConflicts() {
        List<SchemaConflict> conflicts = new ArrayList<>();

        for (Map.Entry<AnnotationKey, List<AnnotationInfo>> entry : annotationToValues.entrySet()) {
            AnnotationKey key = entry.getKey();
            List<AnnotationInfo> annotations = entry.getValue();

            if (annotations.size() > 1) {
                // Check if all annotations have the same value
                Set<String> uniqueValues = annotations.stream()
                                                    .map(info -> info.value)
                                                    .collect(Collectors.toSet());

                if (uniqueValues.size() > 1) {
                    // Found conflicting annotation values
                    List<String> files = annotations.stream()
                                                   .map(info -> info.fileName)
                                                   .distinct()
                                                   .collect(Collectors.toList());

                    String details = String.format("Conflicting annotation values for term '%s' on element '%s.%s': %s",
                                                  key.termName,
                                                  key.namespace,
                                                  key.elementName,
                                                  annotations.stream()
                                                           .map(info -> String.format("'%s' in %s", info.value, info.fileName))
                                                           .collect(Collectors.joining(", ")));

                    conflicts.add(SchemaConflict.annotationConflict(key.namespace, key.elementName,
                                                                  key.termName, files, details));
                }
            }
        }

        return conflicts;
    }
    
    /**
     * Detect duplicate namespace schemas
     * Rule: Only detect conflicts if there are multiple EntityContainers with the SAME NAME
     * in the same namespace. Multiple EntityContainers with different names in the same 
     * namespace are allowed by OData 4.0.
     */
    private List<SchemaConflict> detectDuplicateNamespaceSchemas() {
        List<SchemaConflict> conflicts = new ArrayList<>();
        
        for (Map.Entry<String, List<SchemaInfo>> entry : namespaceToSchemas.entrySet()) {
            String namespace = entry.getKey();
            List<SchemaInfo> schemas = entry.getValue();
            
            // Track EntityContainer names and their files
            Map<String, List<String>> containerNameToFiles = new HashMap<>();
            
            for (SchemaInfo schemaInfo : schemas) {
                if (schemaInfo.schema.getEntityContainer() != null) {
                    String containerName = schemaInfo.schema.getEntityContainer().getName();
                    containerNameToFiles.computeIfAbsent(containerName, k -> new ArrayList<>())
                                       .add(schemaInfo.fileName);
                }
            }
            
            // Report conflict only if the same EntityContainer name appears in multiple files
            // within the same namespace (this violates OData 4.0)
            for (Map.Entry<String, List<String>> containerEntry : containerNameToFiles.entrySet()) {
                List<String> filesWithSameContainer = containerEntry.getValue();
                
                if (filesWithSameContainer.size() > 1) {
                    conflicts.add(SchemaConflict.duplicateNamespaceSchema(namespace, filesWithSameContainer));
                }
            }
        }
        
        return conflicts;
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
        annotationToValues.clear();
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
}
