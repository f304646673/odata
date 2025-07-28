package org.apache.olingo.xml.validator;

import java.nio.file.Path;

/**
 * Represents a validation error found during XML or schema validation.
 */
public class ValidationError {
    
    /**
     * Error types for categorizing validation errors.
     */
    public enum ErrorType {
        XML_FORMAT_ERROR,
        XML_ENCODING_ERROR,
        SCHEMA_STRUCTURE_ERROR,
        MISSING_NAMESPACE,
        NAMESPACE_CONFLICT,
        ELEMENT_DEFINITION_ERROR,
        MISSING_REQUIRED_ATTRIBUTE,
        DEPENDENCY_ERROR,
        CIRCULAR_DEPENDENCY,
        ANNOTATION_ERROR,
        ODATA_COMPLIANCE_ERROR,
        FILE_ACCESS_ERROR,
        TYPE_REFERENCE_ERROR,
        DUPLICATE_ELEMENT_ERROR
    }
    
    private final ErrorType type;
    private final String message;
    private final Path filePath;
    private final int lineNumber;
    private final int columnNumber;
    private final String elementName;
    private final String namespace;
    private final Throwable cause;
    
    /**
     * Creates a new validation error.
     * 
     * @param type the type of error
     * @param message descriptive error message
     * @param filePath path to the file where error occurred
     * @param lineNumber line number in the file (optional, -1 if unknown)
     * @param columnNumber column number in the file (optional, -1 if unknown)
     * @param elementName name of the element causing the error (optional)
     * @param namespace namespace of the element (optional)
     * @param cause underlying exception that caused this error (optional)
     */
    public ValidationError(ErrorType type, String message, Path filePath, 
                         int lineNumber, int columnNumber, String elementName, 
                         String namespace, Throwable cause) {
        this.type = type;
        this.message = message;
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
        this.elementName = elementName;
        this.namespace = namespace;
        this.cause = cause;
    }
    
    /**
     * Creates a simple validation error with just type, message and file path.
     */
    public static ValidationError of(ErrorType type, String message, Path filePath) {
        return new ValidationError(type, message, filePath, -1, -1, null, null, null);
    }
    
    /**
     * Creates a validation error with position information.
     */
    public static ValidationError at(ErrorType type, String message, Path filePath, 
                                   int lineNumber, int columnNumber) {
        return new ValidationError(type, message, filePath, lineNumber, columnNumber, null, null, null);
    }
    
    /**
     * Creates a validation error with element context.
     */
    public static ValidationError forElement(ErrorType type, String message, Path filePath, 
                                           String elementName, String namespace) {
        return new ValidationError(type, message, filePath, -1, -1, elementName, namespace, null);
    }
    
    /**
     * Creates a validation error with underlying exception.
     */
    public static ValidationError withCause(ErrorType type, String message, Path filePath, Throwable cause) {
        return new ValidationError(type, message, filePath, -1, -1, null, null, cause);
    }
    
    // Getters
    public ErrorType getType() {
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
    
    public Throwable getCause() {
        return cause;
    }
    
    public boolean hasPosition() {
        return lineNumber > 0 && columnNumber > 0;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ValidationError{");
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
