package org.apache.olingo.compliance.engine.rules.security;

import org.apache.olingo.compliance.engine.rule.RuleResult;
import org.apache.olingo.compliance.core.api.ValidationConfig;
import org.apache.olingo.compliance.engine.core.ValidationContext;
import org.apache.olingo.compliance.engine.rule.ValidationRule;

/**
 * Abstract base class for security validation rules.
 * Security rules validate potential security vulnerabilities in XML schemas.
 */
public abstract class AbstractSecurityRule implements ValidationRule {
    
    private final String name;
    private final String description;
    private final String severity;
    
    protected AbstractSecurityRule(String name, String description, String severity) {
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
        return "security";
    }
    
    @Override
    public String getSeverity() {
        return severity;
    }
    
    @Override
    public boolean isApplicable(ValidationContext context, ValidationConfig config) {
        return config.isSecurityValidationEnabled() && 
               config.isRuleEnabled(getName()) &&
               isSecurityApplicable(context, config);
    }
    
    /**
     * Subclasses should override this to determine if the security rule applies to the specific context.
     */
    protected abstract boolean isSecurityApplicable(ValidationContext context, ValidationConfig config);
    
    @Override
    public long getEstimatedExecutionTime() {
        return 200; // Default 200ms for security rules (potentially more expensive)
    }
}
