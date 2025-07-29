package org.apache.olingo.schema.processor.validation;

import org.apache.olingo.schema.processor.validation.framework.ModularOlingoXmlValidator;

import java.io.File;
import java.nio.file.Path;

/**
 * Olingo-based implementation of XmlFileComplianceValidator.
 * This class now delegates to the modular validation framework for better maintainability.
 *
 * @deprecated Consider using ModularOlingoXmlValidator directly for better control
 */
public class OlingoXmlFileComplianceValidator implements XmlFileComplianceValidator {
    
    private final ModularOlingoXmlValidator delegate;

    public OlingoXmlFileComplianceValidator() {
        this.delegate = new ModularOlingoXmlValidator();
    }

    @Override
    public XmlComplianceResult validateFile(File xmlFile) {
        return delegate.validateFile(xmlFile);
    }
    
    @Override
    public XmlComplianceResult validateFile(Path xmlPath) {
        return delegate.validateFile(xmlPath);
    }
    
    @Override
    public XmlComplianceResult validateContent(String xmlContent, String fileName) {
        return delegate.validateContent(xmlContent, fileName);
    }
}
