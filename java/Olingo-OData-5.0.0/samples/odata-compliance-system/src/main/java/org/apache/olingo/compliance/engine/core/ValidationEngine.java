package org.apache.olingo.compliance.engine.core;

import java.util.List;

import org.apache.olingo.compliance.engine.rule.ValidationRule;
import org.apache.olingo.compliance.core.api.ValidationConfig;
import org.apache.olingo.compliance.core.api.ValidationResult;

/**
 * Core validation engine interface.
 * This is the heart of the validation system, coordinating all validation rules and strategies.
 */
public interface ValidationEngine {
    
    /**
     * Executes validation using the provided context and configuration.
     * 
     * @param context the validation context containing data to validate
     * @param config the validation configuration
     * @return validation result
     */
    ValidationResult validate(ValidationContext context, ValidationConfig config);
    
    /**
     * Registers a validation rule with the engine.
     * 
     * @param rule the validation rule to register
     */
    void registerRule(ValidationRule rule);
    
    /**
     * Unregisters a validation rule from the engine.
     * 
     * @param ruleName the name of the rule to unregister
     */
    void unregisterRule(String ruleName);
    
    /**
     * Gets all registered validation rules.
     * 
     * @return list of registered rules
     */
    List<ValidationRule> getRegisteredRules();
    
    /**
     * Gets a validation rule by name.
     * 
     * @param ruleName the name of the rule
     * @return the validation rule, or null if not found
     */
    ValidationRule getRule(String ruleName);
    
    /**
     * Checks if a rule is registered.
     * 
     * @param ruleName the name of the rule
     * @return true if the rule is registered, false otherwise
     */
    boolean hasRule(String ruleName);
}
