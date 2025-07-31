package org.apache.olingo.compliance.validation.api;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration for schema validation behavior.
 * This allows users to customize validation rules and behavior.
 */
public class ValidationConfig {
    
    // Validation levels
    public enum ValidationLevel {
        STRICT,     // All rules enforced
        STANDARD,   // Standard OData compliance
        LENIENT     // Relaxed validation
    }
    
    public enum SecurityLevel {
        HIGH,       // Strict security checks
        MEDIUM,     // Standard security checks
        LOW         // Basic security checks
    }
    
    private final ValidationLevel validationLevel;
    private final SecurityLevel securityLevel;
    private final boolean enableStructuralValidation;
    private final boolean enableSemanticValidation;
    private final boolean enableSecurityValidation;
    private final boolean enableComplianceValidation;
    private final boolean enableCrossFileValidation;
    private final boolean enableParallelProcessing;
    private final int maxConcurrentValidations;
    private final long maxFileSize;
    private final long maxProcessingTime;
    private final Set<String> disabledRules;
    private final Set<String> enabledRules;
    
    private ValidationConfig(Builder builder) {
        this.validationLevel = builder.validationLevel;
        this.securityLevel = builder.securityLevel;
        this.enableStructuralValidation = builder.enableStructuralValidation;
        this.enableSemanticValidation = builder.enableSemanticValidation;
        this.enableSecurityValidation = builder.enableSecurityValidation;
        this.enableComplianceValidation = builder.enableComplianceValidation;
        this.enableCrossFileValidation = builder.enableCrossFileValidation;
        this.enableParallelProcessing = builder.enableParallelProcessing;
        this.maxConcurrentValidations = builder.maxConcurrentValidations;
        this.maxFileSize = builder.maxFileSize;
        this.maxProcessingTime = builder.maxProcessingTime;
        this.disabledRules = Collections.unmodifiableSet(new HashSet<>(builder.disabledRules));
        this.enabledRules = Collections.unmodifiableSet(new HashSet<>(builder.enabledRules));
    }
    
    public static class Builder {
        private ValidationLevel validationLevel = ValidationLevel.STANDARD;
        private SecurityLevel securityLevel = SecurityLevel.MEDIUM;
        private boolean enableStructuralValidation = true;
        private boolean enableSemanticValidation = true;
        private boolean enableSecurityValidation = true;
        private boolean enableComplianceValidation = true;
        private boolean enableCrossFileValidation = true;
        private boolean enableParallelProcessing = true;
        private int maxConcurrentValidations = 4;
        private long maxFileSize = 10 * 1024 * 1024; // 10MB
        private long maxProcessingTime = 300000; // 30 seconds
        private Set<String> disabledRules = new HashSet<>();
        private Set<String> enabledRules = new HashSet<>();
        
        public Builder validationLevel(ValidationLevel level) {
            this.validationLevel = level;
            return this;
        }
        
        public Builder securityLevel(SecurityLevel level) {
            this.securityLevel = level;
            return this;
        }
        
        public Builder enableStructuralValidation(boolean enable) {
            this.enableStructuralValidation = enable;
            return this;
        }
        
        public Builder enableSemanticValidation(boolean enable) {
            this.enableSemanticValidation = enable;
            return this;
        }
        
        public Builder enableSecurityValidation(boolean enable) {
            this.enableSecurityValidation = enable;
            return this;
        }
        
        public Builder enableComplianceValidation(boolean enable) {
            this.enableComplianceValidation = enable;
            return this;
        }
        
        public Builder enableCrossFileValidation(boolean enable) {
            this.enableCrossFileValidation = enable;
            return this;
        }
        
        public Builder enableParallelProcessing(boolean enable) {
            this.enableParallelProcessing = enable;
            return this;
        }
        
        public Builder maxConcurrentValidations(int max) {
            this.maxConcurrentValidations = Math.max(1, max);
            return this;
        }
        
        public Builder maxFileSize(long maxSize) {
            this.maxFileSize = Math.max(1024, maxSize);
            return this;
        }
        
        public Builder maxProcessingTime(long maxTime) {
            this.maxProcessingTime = Math.max(1000, maxTime);
            return this;
        }
        
        public Builder disableRules(Set<String> rules) {
            this.disabledRules = new HashSet<>(rules);
            return this;
        }
        
        public Builder enableOnlyRules(Set<String> rules) {
            this.enabledRules = new HashSet<>(rules);
            return this;
        }
        
        public ValidationConfig build() {
            return new ValidationConfig(this);
        }
    }
    
    // Predefined configurations
    public static ValidationConfig standard() {
        return new Builder().build();
    }
    
    public static ValidationConfig strict() {
        return new Builder()
                .validationLevel(ValidationLevel.STRICT)
                .securityLevel(SecurityLevel.HIGH)
                .build();
    }
    
    public static ValidationConfig lenient() {
        return new Builder()
                .validationLevel(ValidationLevel.LENIENT)
                .securityLevel(SecurityLevel.LOW)
                .build();
    }
    
    public static ValidationConfig performanceOptimized() {
        return new Builder()
                .enableParallelProcessing(true)
                .maxConcurrentValidations(8)
                .enableSemanticValidation(false)
                .enableCrossFileValidation(false)
                .build();
    }
    
    public static ValidationConfig securityFocused() {
        return new Builder()
                .securityLevel(SecurityLevel.HIGH)
                .enableSecurityValidation(true)
                .enableStructuralValidation(false)
                .enableSemanticValidation(false)
                .enableComplianceValidation(false)
                .build();
    }
    
    // Getters
    public ValidationLevel getValidationLevel() { return validationLevel; }
    public SecurityLevel getSecurityLevel() { return securityLevel; }
    public boolean isStructuralValidationEnabled() { return enableStructuralValidation; }
    public boolean isSemanticValidationEnabled() { return enableSemanticValidation; }
    public boolean isSecurityValidationEnabled() { return enableSecurityValidation; }
    public boolean isComplianceValidationEnabled() { return enableComplianceValidation; }
    public boolean isCrossFileValidationEnabled() { return enableCrossFileValidation; }
    public boolean isParallelProcessingEnabled() { return enableParallelProcessing; }
    public int getMaxConcurrentValidations() { return maxConcurrentValidations; }
    public long getMaxFileSize() { return maxFileSize; }
    public long getMaxProcessingTime() { return maxProcessingTime; }
    public Set<String> getDisabledRules() { return disabledRules; }
    public Set<String> getEnabledRules() { return enabledRules; }
    
    public boolean isRuleEnabled(String ruleName) {
        if (!enabledRules.isEmpty()) {
            return enabledRules.contains(ruleName);
        }
        return !disabledRules.contains(ruleName);
    }
    
    @Override
    public String toString() {
        return String.format("ValidationConfig[level=%s, security=%s, parallel=%s, maxConcurrent=%d]", 
                           validationLevel, securityLevel, enableParallelProcessing, maxConcurrentValidations);
    }
}
