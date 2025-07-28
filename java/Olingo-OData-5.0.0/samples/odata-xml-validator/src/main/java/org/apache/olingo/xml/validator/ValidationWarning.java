package org.apache.olingo.xml.validator;

import java.nio.file.Path;

/**
 * Represents a validation warning found during XML or schema validation.
 * Warnings indicate potential issues that don't prevent validation from succeeding
 * but may cause problems in the future.
 */
public class ValidationWarning {
    
    /**
     * Warning types for categorizing validation warnings.
     */
    public enum WarningType {
        DEPRECATED_FEATURE,
        BEST_PRACTICE_VIOLATION,
        POTENTIAL_PERFORMANCE_ISSUE,
        MISSING_OPTIONAL_ELEMENT,
        NAMESPACE_CONVENTION_WARNING,
        FILE_NAMING_WARNING,
        ENCODING_WARNING,
        ANNOTATION_WARNING,
        COMPATIBILITY_WARNING,
        UNUSED_DEPENDENCY
    }
    
    private final WarningType type;
    private final String message;
    private final Path filePath;
    private final int lineNumber;
    private final int columnNumber;
    private final String elementName;
    private final String namespace;
    
    /**
     * Creates a new validation warning.
     * 
     * @param type the type of warning
     * @param message descriptive warning message
     * @param filePath path to the file where warning occurred
     * @param lineNumber line number in the file (optional, -1 if unknown)
     * @param columnNumber column number in the file (optional, -1 if unknown)
     * @param elementName name of the element causing the warning (optional)
     * @param namespace namespace of the element (optional)
     */
    public ValidationWarning(WarningType type, String message, Path filePath, 
                           int lineNumber, int columnNumber, String elementName, String namespace) {
        this.type = type;
        this.message = message;
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
        this.elementName = elementName;
        this.namespace = namespace;
    }
    
    /**
     * Creates a simple validation warning with just type, message and file path.
     */
    public static ValidationWarning of(WarningType type, String message, Path filePath) {
        return new ValidationWarning(type, message, filePath, -1, -1, null, null);
    }
    
    /**
     * Creates a validation warning with position information.
     */
    public static ValidationWarning at(WarningType type, String message, Path filePath, 
                                     int lineNumber, int columnNumber) {
        return new ValidationWarning(type, message, filePath, lineNumber, columnNumber, null, null);
    }
    
    /**
     * Creates a validation warning with element context.
     */
    public static ValidationWarning forElement(WarningType type, String message, Path filePath, 
                                             String elementName, String namespace) {
        return new ValidationWarning(type, message, filePath, -1, -1, elementName, namespace);
    }
    
    // Getters
    public WarningType getType() {
        return type;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Path getFilePath() {
        return filePath;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
    
    public int getColumnNumber() {
        return columnNumber;
    }
    
    public String getElementName() {
        return elementName;
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    public boolean hasPosition() {
        return lineNumber > 0 && columnNumber > 0;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ValidationWarning{");
        sb.append("type=").append(type);
        sb.append(", message='").append(message).append('\'');
        if (filePath != null) {
            sb.append(", file=").append(filePath.getFileName());
        }
        if (hasPosition()) {
            sb.append(", line=").append(lineNumber);
            sb.append(", column=").append(columnNumber);
        }
        if (elementName != null) {
            sb.append(", element=").append(elementName);
        }
        if (namespace != null) {
            sb.append(", namespace=").append(namespace);
        }
        sb.append('}');
        return sb.toString();
    }
}
