package org.apache.olingo.compliance.validation.rules.structural;

import org.apache.olingo.compliance.validation.api.ValidationConfig;
import org.apache.olingo.compliance.validation.core.ValidationContext;
import org.apache.olingo.compliance.validation.core.ValidationRule;

/**
 * Abstract base class for structural validation rules.
 * Structural rules validate the basic structure and format of schema elements.
 */
public abstract class AbstractStructuralRule implements ValidationRule {
    
    private final String name;
    private final String description;
    private final String severity;
    
    protected AbstractStructuralRule(String name, String description, String severity) {
        this.name = name;
        this.description = description;
        this.severity = severity;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public String getCategory() {
        return "structural";
    }
    
    @Override
    public String getSeverity() {
        return severity;
    }
    
    @Override
    public boolean isApplicable(ValidationContext context, ValidationConfig config) {
        return config.isStructuralValidationEnabled() && 
               config.isRuleEnabled(getName()) &&
               isStructurallyApplicable(context, config);
    }
    
    /**
     * Subclasses should override this to determine if the rule applies to the specific context.
     */
    protected abstract boolean isStructurallyApplicable(ValidationContext context, ValidationConfig config);
    
    @Override
    public long getEstimatedExecutionTime() {
        return 100; // Default 100ms for structural rules
    }
}
