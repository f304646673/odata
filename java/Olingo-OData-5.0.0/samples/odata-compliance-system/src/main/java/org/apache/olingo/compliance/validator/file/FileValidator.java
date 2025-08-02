package org.apache.olingo.compliance.validator.file;

import java.io.File;
import java.nio.file.Path;

import org.apache.olingo.compliance.core.model.ComplianceResult;
import org.apache.olingo.compliance.engine.core.SchemaRegistry;

/**
 * Interface for validating single OData 4.0 XML files for compliance.
 * This validator focuses on single-file validation using Olingo's internal methods.
 * All validation methods require a SchemaRegistry for cross-file reference validation.
 */
public interface FileValidator {
    
    /**
     * Validates a single OData 4.0 XML file for compliance.
     * 
     * @param xmlFile the XML file to validate
     * @param registry the schema registry containing type definitions for cross-file reference validation
     * @return ComplianceResult containing validation results
     */
    ComplianceResult validateFile(File xmlFile, SchemaRegistry registry);

    /**
     * Validates a single OData 4.0 XML file for compliance.
     * 
     * @param xmlPath the path to the XML file to validate
     * @param registry the schema registry containing type definitions for cross-file reference validation
     * @return ComplianceResult containing validation results
     */
    ComplianceResult validateFile(Path xmlPath, SchemaRegistry registry);

    /**
     * Validates a single OData 4.0 XML file from content string.
     * 
     * @param xmlContent the XML content as string
     * @param fileName optional file name for error reporting
     * @param registry the schema registry containing type definitions for cross-file reference validation
     * @return ComplianceResult containing validation results
     */
    ComplianceResult validateContent(String xmlContent, String fileName, SchemaRegistry registry);
}
