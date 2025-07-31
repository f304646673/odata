package org.apache.olingo.compliance.validation.impl;

import java.io.File;
import java.nio.file.Path;

import org.apache.olingo.compliance.validation.api.SchemaValidator;
import org.apache.olingo.compliance.validation.api.ValidationConfig;
import org.apache.olingo.compliance.validation.api.ValidationResult;
import org.apache.olingo.compliance.validation.core.ValidationContext;
import org.apache.olingo.compliance.validation.core.ValidationEngine;
import org.apache.olingo.compliance.validation.rules.security.XxeAttackRule;
import org.apache.olingo.compliance.validation.rules.semantic.AnnotationValidationRule;
import org.apache.olingo.compliance.validation.rules.semantic.ComplianceRule;
import org.apache.olingo.compliance.validation.rules.structural.ElementDefinitionRule;
import org.apache.olingo.compliance.validation.rules.structural.ReferenceValidationRule;
import org.apache.olingo.compliance.validation.rules.structural.SchemaNamespaceRule;
import org.apache.olingo.compliance.validation.strategies.FileValidationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configurable implementation of SchemaValidator that uses the new modular architecture.
 * This validator can be configured with different validation rules and strategies.
 */
public class ConfigurableSchemaValidator implements SchemaValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigurableSchemaValidator.class);
    
    private final ValidationEngine engine;
    private final ValidationConfig defaultConfig;
    
    public ConfigurableSchemaValidator() {
        this(ValidationConfig.standard());
    }
    
    public ConfigurableSchemaValidator(ValidationConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
        this.engine = createDefaultEngine();
        initializeDefaultRules();
    }
    
    public ConfigurableSchemaValidator(ValidationEngine engine, ValidationConfig defaultConfig) {
        this.engine = engine;
        this.defaultConfig = defaultConfig;
    }
    
    @Override
    public ValidationResult validateFile(File xmlFile) {
        if (xmlFile == null) {
            return ValidationResult.error("unknown", "File cannot be null", 0);
        }
        return validateFile(xmlFile.toPath());
    }
    
    @Override
    public ValidationResult validateFile(Path xmlPath) {
        if (xmlPath == null) {
            return ValidationResult.error("unknown", "Path cannot be null", 0);
        }
        
        ValidationContext context = ValidationContext.forFile(xmlPath);
        return engine.validate(context, defaultConfig);
    }
    
    @Override
    public ValidationResult validateContent(String xmlContent, String fileName) {
        if (xmlContent == null) {
            return ValidationResult.error(fileName != null ? fileName : "unknown", 
                                        "Content cannot be null", 0);
        }
        
        ValidationContext context = ValidationContext.forContent(xmlContent, 
                                                                fileName != null ? fileName : "content");
        return engine.validate(context, defaultConfig);
    }
    
    @Override
    public ValidationResult validateDirectory(Path directoryPath) {
        return validateDirectory(directoryPath, "*.xml");
    }
    
    @Override
    public ValidationResult validateDirectory(Path directoryPath, String filePattern) {
        if (directoryPath == null) {
            return ValidationResult.error("unknown", "Directory path cannot be null", 0);
        }
        
        // For now, return a simple implementation
        // This would be enhanced with a DirectoryValidationStrategy
        ValidationContext context = ValidationContext.forFile(directoryPath);
        context.setProperty("filePattern", filePattern);
        context.setProperty("isDirectory", true);
        
        return engine.validate(context, defaultConfig);
    }
    
    /**
     * Validates with a custom configuration.
     */
    public ValidationResult validateFile(Path xmlPath, ValidationConfig config) {
        if (xmlPath == null) {
            return ValidationResult.error("unknown", "Path cannot be null", 0);
        }
        
        ValidationContext context = ValidationContext.forFile(xmlPath);
        return engine.validate(context, config);
    }
    
    /**
     * Validates content with a custom configuration.
     */
    public ValidationResult validateContent(String xmlContent, String fileName, ValidationConfig config) {
        if (xmlContent == null) {
            return ValidationResult.error(fileName != null ? fileName : "unknown", 
                                        "Content cannot be null", 0);
        }
        
        ValidationContext context = ValidationContext.forContent(xmlContent, 
                                                                fileName != null ? fileName : "content");
        return engine.validate(context, config);
    }
    
    /**
     * Gets the underlying validation engine for advanced configuration.
     */
    public ValidationEngine getEngine() {
        return engine;
    }
    
    /**
     * Gets the default validation configuration.
     */
    public ValidationConfig getDefaultConfig() {
        return defaultConfig;
    }
    
    private ValidationEngine createDefaultEngine() {
        DefaultValidationEngine defaultEngine = new DefaultValidationEngine();
        
        // Register strategies
        defaultEngine.registerStrategy(new FileValidationStrategy());
        
        return defaultEngine;
    }
    
    private void initializeDefaultRules() {
                // Register structural validation rules
        engine.registerRule(new SchemaNamespaceRule());
        engine.registerRule(new ElementDefinitionRule());
        engine.registerRule(new ReferenceValidationRule());
        
        // Register security validation rules
        engine.registerRule(new XxeAttackRule());
        
        // Register semantic validation rules
        engine.registerRule(new AnnotationValidationRule());
        engine.registerRule(new ComplianceRule());
        
        logger.info("Initialized ConfigurableSchemaValidator with {} rules", 
                   engine.getRegisteredRules().size());
    }
    
    /**
     * Builder for creating customized validators.
     */
    public static class Builder {
        private ValidationConfig config = ValidationConfig.standard();
        private ValidationEngine engine;
        
        public Builder config(ValidationConfig config) {
            this.config = config;
            return this;
        }
        
        public Builder engine(ValidationEngine engine) {
            this.engine = engine;
            return this;
        }
        
        public Builder strict() {
            this.config = ValidationConfig.strict();
            return this;
        }
        
        public Builder lenient() {
            this.config = ValidationConfig.lenient();
            return this;
        }
        
        public Builder securityFocused() {
            this.config = ValidationConfig.securityFocused();
            return this;
        }
        
        public Builder performanceOptimized() {
            this.config = ValidationConfig.performanceOptimized();
            return this;
        }
        
        public ConfigurableSchemaValidator build() {
            if (engine != null) {
                return new ConfigurableSchemaValidator(engine, config);
            } else {
                return new ConfigurableSchemaValidator(config);
            }
        }
    }
    
    /**
     * Creates a builder for customizing the validator.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Creates a standard validator.
     */
    public static ConfigurableSchemaValidator standard() {
        return new ConfigurableSchemaValidator();
    }
    
    /**
     * Creates a strict validator.
     */
    public static ConfigurableSchemaValidator strict() {
        return new ConfigurableSchemaValidator(ValidationConfig.strict());
    }
    
    /**
     * Creates a lenient validator.
     */
    public static ConfigurableSchemaValidator lenient() {
        return new ConfigurableSchemaValidator(ValidationConfig.lenient());
    }
    
    /**
     * Creates a security-focused validator.
     */
    public static ConfigurableSchemaValidator securityFocused() {
        return new ConfigurableSchemaValidator(ValidationConfig.securityFocused());
    }
}
