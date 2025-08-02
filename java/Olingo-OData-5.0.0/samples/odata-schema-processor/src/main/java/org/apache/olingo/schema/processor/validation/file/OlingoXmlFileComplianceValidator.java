package org.apache.olingo.schema.processor.validation.file;

import org.apache.olingo.schema.processor.validation.file.framework.ModularOlingoXmlValidator;
import org.apache.olingo.compliance.engine.core.SchemaRegistry;
import org.apache.olingo.compliance.engine.core.DefaultSchemaRegistry;
import org.apache.olingo.compliance.core.model.XmlComplianceResult;

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
    public XmlComplianceResult validateFile(File xmlFile, SchemaRegistry registry) {
        return delegate.validateFile(xmlFile, registry);
    }
    
    @Override
    public XmlComplianceResult validateFile(Path xmlPath, SchemaRegistry registry) {
        return delegate.validateFile(xmlPath, registry);
    }
    
    @Override
    public XmlComplianceResult validateContent(String xmlContent, String fileName, SchemaRegistry registry) {
        return delegate.validateContent(xmlContent, fileName, registry);
    }
}
