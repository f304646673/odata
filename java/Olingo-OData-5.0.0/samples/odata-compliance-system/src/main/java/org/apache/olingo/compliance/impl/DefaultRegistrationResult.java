package org.apache.olingo.compliance.impl;

import org.apache.olingo.compliance.api.RegistrationResult;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of RegistrationResult.
 */
public class DefaultRegistrationResult implements RegistrationResult {
    
    private final String filePath;
    private final boolean successful;
    private final List<String> errors;
    private final LocalDateTime registrationTime;
    private final boolean registeredInFilePathRepository;
    private final boolean registeredInNamespaceSchemaRepository;
    private final String registeredNamespace;
    private final int registeredElementCount;
    
    private DefaultRegistrationResult(String filePath, boolean successful, List<String> errors,
                                    boolean registeredInFilePathRepository,
                                    boolean registeredInNamespaceSchemaRepository,
                                    String registeredNamespace, int registeredElementCount) {
        this.filePath = filePath;
        this.successful = successful;
        this.errors = errors;
        this.registrationTime = LocalDateTime.now();
        this.registeredInFilePathRepository = registeredInFilePathRepository;
        this.registeredInNamespaceSchemaRepository = registeredInNamespaceSchemaRepository;
        this.registeredNamespace = registeredNamespace;
        this.registeredElementCount = registeredElementCount;
    }
    
    public static DefaultRegistrationResult success(String filePath, String namespace, int elementCount) {
        return new DefaultRegistrationResult(filePath, true, Collections.emptyList(),
                true, true, namespace, elementCount);
    }
    
    public static DefaultRegistrationResult failed(String filePath, String error) {
        return new DefaultRegistrationResult(filePath, false, Collections.singletonList(error),
                false, false, null, 0);
    }
    
    public static DefaultRegistrationResult failed(String filePath, List<String> errors) {
        return new DefaultRegistrationResult(filePath, false, errors,
                false, false, null, 0);
    }
    
    @Override
    public String getFilePath() {
        return filePath;
    }
    
    @Override
    public boolean isSuccessful() {
        return successful;
    }
    
    @Override
    public List<String> getErrors() {
        return errors;
    }
    
    @Override
    public LocalDateTime getRegistrationTime() {
        return registrationTime;
    }
    
    @Override
    public boolean isRegisteredInFilePathRepository() {
        return registeredInFilePathRepository;
    }
    
    @Override
    public boolean isRegisteredInNamespaceSchemaRepository() {
        return registeredInNamespaceSchemaRepository;
    }
    
    @Override
    public String getRegisteredNamespace() {
        return registeredNamespace;
    }
    
    @Override
    public int getRegisteredElementCount() {
        return registeredElementCount;
    }
}
