package org.apache.olingo.compliance.api;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Result of registering a compliant file into the system repositories.
 */
public interface RegistrationResult {
    
    /**
     * Gets the file path that was registered.
     *
     * @return File path
     */
    String getFilePath();
    
    /**
     * Checks if the registration was successful.
     *
     * @return true if successful, false otherwise
     */
    boolean isSuccessful();
    
    /**
     * Gets the list of registration errors, if any.
     *
     * @return List of error messages
     */
    List<String> getErrors();
    
    /**
     * Gets the registration timestamp.
     *
     * @return Registration timestamp
     */
    LocalDateTime getRegistrationTime();
    
    /**
     * Checks if the file was registered in the file path repository.
     *
     * @return true if registered in file path repository
     */
    boolean isRegisteredInFilePathRepository();
    
    /**
     * Checks if the file was registered in the namespace schema repository.
     *
     * @return true if registered in namespace schema repository
     */
    boolean isRegisteredInNamespaceSchemaRepository();
    
    /**
     * Gets the schema namespace that was registered.
     *
     * @return Schema namespace
     */
    String getRegisteredNamespace();
    
    /**
     * Gets the number of elements that were registered.
     *
     * @return Number of registered elements
     */
    int getRegisteredElementCount();
}
