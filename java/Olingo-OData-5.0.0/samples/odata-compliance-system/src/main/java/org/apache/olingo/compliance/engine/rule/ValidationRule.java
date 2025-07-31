package org.apache.olingo.compliance.engine.rule;

import org.apache.olingo.compliance.engine.core.ValidationContext;
import org.apache.olingo.compliance.core.api.ValidationConfig;

/**
 * Interface for individual validation rules.
 * Each rule represents a specific validation check that can be applied to schema elements.
 */
public interface ValidationRule {
    
    /**
     * Gets the unique name of this validation rule.
     * 
     * @return the rule name
     */
    String getName();
    
    /**
     * Gets a description of what this rule validates.
     * 
     * @return the rule description
     */
    String getDescription();
    
    /**
     * Gets the category of this validation rule.
     * 
     * @return the rule category (e.g., "structural", "semantic", "security")
     */
    String getCategory();
    
    /**
     * Gets the severity level of violations for this rule.
     * 
     * @return the severity level ("error", "warning", "info")
     */
    String getSeverity();
    
    /**
     * Checks if this rule is applicable given the current validation context and configuration.
     * 
     * @param context the validation context
     * @param config the validation configuration
     * @return true if the rule should be applied, false otherwise
     */
    boolean isApplicable(ValidationContext context, ValidationConfig config);
    
    /**
     * Executes the validation rule against the provided context.
     * 
     * @param context the validation context to validate
     * @param config the validation configuration
     * @return the validation result for this rule
     */
    RuleResult validate(ValidationContext context, ValidationConfig config);
    
    /**
     * Gets the estimated execution time for this rule in milliseconds.
     * This helps with optimization and timeout handling.
     * 
     * @return estimated execution time in milliseconds
     */
    long getEstimatedExecutionTime();
    
    /**
     * Represents the result of applying a single validation rule.
     */
    class RuleResult {
        private final String ruleName;
        private final boolean passed;
        private final String message;
        private final String details;
        private final long executionTime;
        
        public RuleResult(String ruleName, boolean passed, String message, String details, long executionTime) {
            this.ruleName = ruleName;
            this.passed = passed;
            this.message = message;
            this.details = details;
            this.executionTime = executionTime;
        }
        
        public static RuleResult pass(String ruleName, long executionTime) {
            return new RuleResult(ruleName, true, null, null, executionTime);
        }
        
        public static RuleResult fail(String ruleName, String message, long executionTime) {
            return new RuleResult(ruleName, false, message, null, executionTime);
        }
        
        public static RuleResult fail(String ruleName, String message, String details, long executionTime) {
            return new RuleResult(ruleName, false, message, details, executionTime);
        }
        
        public String getRuleName() { return ruleName; }
        public boolean isPassed() { return passed; }
        public boolean isFailed() { return !passed; }
        public String getMessage() { return message; }
        public String getDetails() { return details; }
        public long getExecutionTime() { return executionTime; }
        
        @Override
        public String toString() {
            return String.format("RuleResult[rule=%s, passed=%s, message=%s, time=%dms]", 
                               ruleName, passed, message, executionTime);
        }
    }
}
