package org.apache.olingo.schemamanager.exception;

/**
 * Schema验证异常
 */
public class SchemaValidationException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    public SchemaValidationException(String message) {
        super(message);
    }
    
    public SchemaValidationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public SchemaValidationException(Throwable cause) {
        super(cause);
    }
}
