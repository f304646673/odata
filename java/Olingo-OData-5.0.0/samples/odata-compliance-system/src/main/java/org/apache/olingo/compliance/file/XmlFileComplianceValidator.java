package org.apache.olingo.compliance.file;

import java.io.File;
import java.nio.file.Path;

/**
 * Interface for validating single OData 4.0 XML files for compliance.
 * This validator focuses on single-file validation using Olingo's internal methods.
 */
public interface XmlFileComplianceValidator {
    
    /**
     * Validates a single OData 4.0 XML file for compliance.
     * 
     * @param xmlFile the XML file to validate
     * @return XmlComplianceResult containing validation results
     */
    XmlComplianceResult validateFile(File xmlFile);
    
    /**
     * Validates a single OData 4.0 XML file for compliance.
     * 
     * @param xmlPath the path to the XML file to validate
     * @return XmlComplianceResult containing validation results
     */
    XmlComplianceResult validateFile(Path xmlPath);
    
    /**
     * Validates a single OData 4.0 XML file from content string.
     * 
     * @param xmlContent the XML content as string
     * @param fileName optional file name for error reporting
     * @return XmlComplianceResult containing validation results
     */
    XmlComplianceResult validateContent(String xmlContent, String fileName);
}
