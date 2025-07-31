package org.apache.olingo.compliance.validation.core;

import org.apache.olingo.compliance.validation.api.ValidationConfig;
import org.apache.olingo.compliance.validation.api.ValidationResult;

/**
 * Strategy interface for different types of validation (file vs directory).
 * This allows the system to handle different validation scenarios uniformly.
 */
public interface ValidationStrategy {
    
    /**
     * Gets the name of this validation strategy.
     * 
     * @return strategy name
     */
    String getName();
    
    /**
     * Checks if this strategy can handle the given validation context.
     * 
     * @param context the validation context
     * @return true if this strategy can handle the context, false otherwise
     */
    boolean canHandle(ValidationContext context);
    
    /**
     * Executes validation using this strategy.
     * 
     * @param context the validation context
     * @param config the validation configuration
     * @param engine the validation engine to use for rule execution
     * @return validation result
     */
    ValidationResult execute(ValidationContext context, ValidationConfig config, ValidationEngine engine);
    
    /**
     * Gets the estimated execution time for this strategy.
     * 
     * @param context the validation context
     * @return estimated execution time in milliseconds
     */
    long getEstimatedExecutionTime(ValidationContext context);
}
