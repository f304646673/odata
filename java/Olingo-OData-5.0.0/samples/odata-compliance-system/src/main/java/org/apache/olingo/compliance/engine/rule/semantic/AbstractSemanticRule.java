package org.apache.olingo.compliance.engine.rules.semantic;

import org.apache.olingo.compliance.engine.rule.RuleResult;
import org.apache.olingo.compliance.core.api.ValidationConfig;
import org.apache.olingo.compliance.engine.core.ValidationContext;
import org.apache.olingo.compliance.engine.rule.ValidationRule;

/**
 * Abstract base class for semantic validation rules.
 * Semantic rules validate the meaning and relationships of schema elements.
 */
public abstract class AbstractSemanticRule implements ValidationRule {
    
    private final String name;
    private final String description;
    private final String severity;
    
    protected AbstractSemanticRule(String name, String description, String severity) {
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
        return "semantic";
    }
    
    @Override
    public String getSeverity() {
        return severity;
    }
    
    @Override
    public boolean isApplicable(ValidationContext context, ValidationConfig config) {
        return config.isSemanticValidationEnabled() && 
               config.isRuleEnabled(getName()) &&
               isSemanticApplicable(context, config);
    }
    
    /**
     * Subclasses should override this to determine if the rule applies to the specific context.
     */
    protected abstract boolean isSemanticApplicable(ValidationContext context, ValidationConfig config);
    
    @Override
    public long getEstimatedExecutionTime() {
        return 150; // Default 150ms for semantic rules
    }
}
