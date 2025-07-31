package org.apache.olingo.compliance.file;

import java.io.File;
import java.nio.file.Path;

import org.apache.olingo.compliance.validation.api.ValidationResult;
import org.apache.olingo.compliance.validation.impl.ConfigurableSchemaValidator;

/**
 * Backward compatibility adapter for the new validation architecture.
 * This class implements the old XmlFileComplianceValidator interface using the new system.
 */
public class ModernXmlFileComplianceValidator implements XmlFileComplianceValidator {
    
    private final ConfigurableSchemaValidator validator;
    
    public ModernXmlFileComplianceValidator() {
        this.validator = ConfigurableSchemaValidator.standard();
    }
    
    public ModernXmlFileComplianceValidator(ConfigurableSchemaValidator validator) {
        this.validator = validator;
    }
    
    @Override
    public XmlComplianceResult validateFile(File xmlFile) {
        ValidationResult result = validator.validateFile(xmlFile);
        return adaptResult(result);
    }
    
    @Override
    public XmlComplianceResult validateFile(Path xmlPath) {
        ValidationResult result = validator.validateFile(xmlPath);
        return adaptResult(result);
    }
    
    @Override
    public XmlComplianceResult validateContent(String xmlContent, String fileName) {
        ValidationResult result = validator.validateContent(xmlContent, fileName);
        return adaptResult(result);
    }
    
    /**
     * Adapts the new ValidationResult to the old XmlComplianceResult format.
     */
    private XmlComplianceResult adaptResult(ValidationResult result) {
        return new XmlComplianceResult(
            result.isCompliant(),
            result.getErrors(),
            result.getWarnings(),
            java.util.Collections.emptySet(), // Empty set for now, can be enhanced later
            result.getMetadata(),
            result.getFileName(),
            result.getProcessingTimeMs()
        );
    }
    
    /**
     * Gets the underlying modern validator for advanced operations.
     */
    public ConfigurableSchemaValidator getModernValidator() {
        return validator;
    }
    
    /**
     * Creates a validator with security focus for compatibility.
     */
    public static ModernXmlFileComplianceValidator securityFocused() {
        return new ModernXmlFileComplianceValidator(ConfigurableSchemaValidator.securityFocused());
    }
    
    /**
     * Creates a validator with strict validation for compatibility.
     */
    public static ModernXmlFileComplianceValidator strict() {
        return new ModernXmlFileComplianceValidator(ConfigurableSchemaValidator.strict());
    }
}
