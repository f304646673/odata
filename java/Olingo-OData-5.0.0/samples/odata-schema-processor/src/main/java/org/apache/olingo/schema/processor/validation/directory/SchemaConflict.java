package org.apache.olingo.schema.processor.validation.directory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a conflict between schema elements across different XML files.
 * This can occur when multiple files define the same element (EntityType, ComplexType, etc.)
 * within the same namespace, or when there are incompatible definitions.
 */
public class SchemaConflict {
    
    public enum ConflictType {
        /** Same element name defined in multiple files within the same namespace */
        DUPLICATE_ELEMENT,
        /** Same namespace defined in multiple files with different schemas */
        DUPLICATE_NAMESPACE_SCHEMA,
        /** Incompatible element definitions across files */
        INCOMPATIBLE_DEFINITION,
        /** Circular reference between schemas */
        CIRCULAR_REFERENCE,
        /** Missing required reference */
        MISSING_REFERENCE,
        /** Conflicting annotation values on the same element */
        ANNOTATION_CONFLICT
    }
    
    private final ConflictType type;
    private final String namespace;
    private final String elementName;
    private final List<String> conflictingFiles;
    private final String description;
    private final String details;
    
    /**
     * Constructor for SchemaConflict
     * 
     * @param type the type of conflict
     * @param namespace the namespace where the conflict occurs
     * @param elementName the name of the conflicting element (can be null for namespace-level conflicts)
     * @param conflictingFiles list of files involved in the conflict
     * @param description brief description of the conflict
     * @param details detailed explanation of the conflict
     */
    public SchemaConflict(ConflictType type, String namespace, String elementName, 
                         List<String> conflictingFiles, String description, String details) {
        this.type = Objects.requireNonNull(type, "Conflict type cannot be null");
        this.namespace = Objects.requireNonNull(namespace, "Namespace cannot be null");
        this.elementName = elementName;
        this.conflictingFiles = Collections.unmodifiableList(conflictingFiles);
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        this.details = details;
    }
    
    /**
     * @return the type of conflict
     */
    public ConflictType getType() {
        return type;
    }
    
    /**
     * @return the namespace where the conflict occurs
     */
    public String getNamespace() {
        return namespace;
    }
    
    /**
     * @return the name of the conflicting element, or null for namespace-level conflicts
     */
    public String getElementName() {
        return elementName;
    }
    
    /**
     * @return list of files involved in the conflict
     */
    public List<String> getConflictingFiles() {
        return conflictingFiles;
    }
    
    /**
     * @return brief description of the conflict
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * @return detailed explanation of the conflict
     */
    public String getDetails() {
        return details;
    }
    
    /**
     * @return a formatted string representation of the conflict
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SchemaConflict{");
        sb.append("type=").append(type);
        sb.append(", namespace='").append(namespace).append('\'');
        if (elementName != null) {
            sb.append(", element='").append(elementName).append('\'');
        }
        sb.append(", files=").append(conflictingFiles);
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchemaConflict that = (SchemaConflict) o;
        return type == that.type &&
               Objects.equals(namespace, that.namespace) &&
               Objects.equals(elementName, that.elementName) &&
               Objects.equals(conflictingFiles, that.conflictingFiles);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, namespace, elementName, conflictingFiles);
    }
    
    /**
     * Create a duplicate element conflict
     */
    public static SchemaConflict duplicateElement(String namespace, String elementName, 
                                                 List<String> conflictingFiles) {
        String description = String.format("Element '%s' is defined multiple times in namespace '%s'", 
                                         elementName, namespace);
        String details = String.format("The element '%s' in namespace '%s' is defined in files: %s. " +
                                      "Each element should only be defined once per namespace.",
                                      elementName, namespace, conflictingFiles);
        return new SchemaConflict(ConflictType.DUPLICATE_ELEMENT, namespace, elementName, 
                                conflictingFiles, description, details);
    }
    
    /**
     * Create a duplicate namespace schema conflict
     */
    public static SchemaConflict duplicateNamespaceSchema(String namespace, List<String> conflictingFiles) {
        String description = String.format("Namespace '%s' schema is defined in multiple files", namespace);
        String details = String.format("The namespace '%s' has complete schema definitions in files: %s. " +
                                      "A namespace should have only one complete schema definition, though " +
                                      "different namespaces can be defined across multiple files.",
                                      namespace, conflictingFiles);
        return new SchemaConflict(ConflictType.DUPLICATE_NAMESPACE_SCHEMA, namespace, null, 
                                conflictingFiles, description, details);
    }
    
    /**
     * Create an incompatible definition conflict
     */
    public static SchemaConflict incompatibleDefinition(String namespace, String elementName,
                                                       List<String> conflictingFiles, String details) {
        String description = String.format("Incompatible definitions for element '%s' in namespace '%s'", 
                                         elementName, namespace);
        return new SchemaConflict(ConflictType.INCOMPATIBLE_DEFINITION, namespace, elementName,
                                conflictingFiles, description, details);
    }
    
    /**
     * Create a circular reference conflict
     */
    public static SchemaConflict circularReference(String namespace, List<String> conflictingFiles, String details) {
        String description = String.format("Circular reference detected in namespace '%s'", namespace);
        return new SchemaConflict(ConflictType.CIRCULAR_REFERENCE, namespace, null,
                                conflictingFiles, description, details);
    }
    
    /**
     * Create a missing reference conflict
     */
    public static SchemaConflict missingReference(String namespace, String elementName,
                                                List<String> affectedFiles, String details) {
        String description = String.format("Missing reference for element '%s' in namespace '%s'", 
                                         elementName, namespace);
        return new SchemaConflict(ConflictType.MISSING_REFERENCE, namespace, elementName,
                                affectedFiles, description, details);
    }

    /**
     * Create an annotation conflict
     */
    public static SchemaConflict annotationConflict(String namespace, String elementName, String termName,
                                                   List<String> conflictingFiles, String details) {
        String description = String.format("Conflicting annotation values for term '%s' on element '%s' in namespace '%s'",
                                         termName, elementName, namespace);
        return new SchemaConflict(ConflictType.ANNOTATION_CONFLICT, namespace, elementName,
                                conflictingFiles, description, details);
    }
}
