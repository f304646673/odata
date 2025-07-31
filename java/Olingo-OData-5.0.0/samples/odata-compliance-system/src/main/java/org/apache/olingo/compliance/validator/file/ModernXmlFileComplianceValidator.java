package org.apache.olingo.compliance.validator.file;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.compliance.core.model.ComplianceIssue;
import org.apache.olingo.compliance.core.model.ComplianceErrorType;
import org.apache.olingo.compliance.core.model.XmlComplianceResult;
import org.apache.olingo.compliance.core.api.ValidationResult;

/**
 * Backward compatibility adapter for the new validation architecture.
 * This class implements the old XmlFileComplianceValidator interface using the new system.
 */
public class ModernXmlFileComplianceValidator implements XmlFileComplianceValidator {
    
    private final ConfigurableSchemaValidator validator;
    private final EnhancedXmlFileComplianceValidator enhancedValidator;
    
    public ModernXmlFileComplianceValidator() {
        this.validator = ConfigurableSchemaValidator.standard();
        this.enhancedValidator = new EnhancedXmlFileComplianceValidator();
    }
    
    public ModernXmlFileComplianceValidator(ConfigurableSchemaValidator validator) {
        this.validator = validator;
        this.enhancedValidator = new EnhancedXmlFileComplianceValidator();
    }
    
    @Override
    public XmlComplianceResult validateFile(File xmlFile) {
        // First try the enhanced validator for detailed compliance checking
        XmlComplianceResult enhancedResult = enhancedValidator.validateFile(xmlFile);
        if (!enhancedResult.hasIssues()) {
            // If enhanced validator finds no issues, also run schema validation
            ValidationResult result = validator.validateFile(xmlFile);
            return adaptResult(result);
        }
        return enhancedResult;
    }
    
    @Override
    public XmlComplianceResult validateFile(Path xmlPath) {
        // First try the enhanced validator for detailed compliance checking
        XmlComplianceResult enhancedResult = enhancedValidator.validateFile(xmlPath);
        if (!enhancedResult.hasIssues()) {
            // If enhanced validator finds no issues, also run schema validation
            ValidationResult result = validator.validateFile(xmlPath);
            return adaptResult(result);
        }
        return enhancedResult;
    }
    
    @Override
    public XmlComplianceResult validateContent(String xmlContent, String fileName) {
        // First try the enhanced validator for detailed compliance checking
        XmlComplianceResult enhancedResult = enhancedValidator.validateContent(xmlContent, fileName);
        if (!enhancedResult.hasIssues()) {
            // If enhanced validator finds no issues, also run schema validation
            ValidationResult result = validator.validateContent(xmlContent, fileName);
            return adaptResult(result);
        }
        return enhancedResult;
    }
    
    /**
     * Adapts the new ValidationResult to the unified XmlComplianceResult format.
     */
    private XmlComplianceResult adaptResult(ValidationResult result) {
        List<ComplianceIssue> allIssues = new ArrayList<>();
        
        // Convert string errors to ComplianceIssue objects with ERROR severity
        for (String errorMsg : result.getErrors()) {
            allIssues.add(new ComplianceIssue(
                ComplianceErrorType.VALIDATION_ERROR, 
                errorMsg, 
                ComplianceIssue.Severity.ERROR
            ));
        }
        
        // Convert string warnings to ComplianceIssue objects with WARNING severity
        for (String warningMsg : result.getWarnings()) {
            allIssues.add(new ComplianceIssue(
                ComplianceErrorType.VALIDATION_ERROR, 
                warningMsg, 
                ComplianceIssue.Severity.WARNING
            ));
        }
        
        return new XmlComplianceResult(
            result.isCompliant(),
            allIssues,
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
