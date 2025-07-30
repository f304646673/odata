package org.apache.olingo.schema.processor.validation.file;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Result of XML file compliance validation.
 * Contains detailed information about validation success/failure,
 * errors, warnings, and metadata about the validated file.
 */
public class XmlComplianceResult {
    
    private final boolean compliant;
    private final List<String> errors;
    private final List<String> warnings;
    private final Set<String> referencedNamespaces;
    private final Map<String, Object> metadata;
    private final String fileName;
    private final long validationTimeMs;
    
    /**
     * Constructor for XmlComplianceResult
     * 
     * @param compliant whether the XML file is compliant
     * @param errors list of validation errors
     * @param warnings list of validation warnings
     * @param referencedNamespaces set of namespaces referenced in the file
     * @param metadata additional metadata about the validation
     * @param fileName name of the validated file
     * @param validationTimeMs time taken for validation in milliseconds
     */
    public XmlComplianceResult(boolean compliant, List<String> errors, List<String> warnings,
                              Set<String> referencedNamespaces, Map<String, Object> metadata,
                              String fileName, long validationTimeMs) {
        this.compliant = compliant;
        this.errors = errors;
        this.warnings = warnings;
        this.referencedNamespaces = referencedNamespaces;
        this.metadata = metadata;
        this.fileName = fileName;
        this.validationTimeMs = validationTimeMs;
    }
    
    /**
     * @return true if the XML file is compliant with OData 4.0 specification
     */
    public boolean isCompliant() {
        return compliant;
    }
    
    /**
     * @return list of validation errors (empty if compliant)
     */
    public List<String> getErrors() {
        return errors;
    }
    
    /**
     * @return list of validation warnings
     */
    public List<String> getWarnings() {
        return warnings;
    }
    
    /**
     * @return set of namespaces referenced in the validated file
     */
    public Set<String> getReferencedNamespaces() {
        return referencedNamespaces;
    }
    
    /**
     * @return additional metadata about the validation
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    /**
     * @return name of the validated file
     */
    public String getFileName() {
        return fileName;
    }
    
    /**
     * @return time taken for validation in milliseconds
     */
    public long getValidationTimeMs() {
        return validationTimeMs;
    }
    
    /**
     * @return true if there are any errors
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
    
    /**
     * @return true if there are any warnings
     */
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }
    
    /**
     * @return number of errors found
     */
    public int getErrorCount() {
        return errors != null ? errors.size() : 0;
    }
    
    /**
     * @return number of warnings found
     */
    public int getWarningCount() {
        return warnings != null ? warnings.size() : 0;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("XmlComplianceResult{");
        sb.append("fileName='").append(fileName).append("'");
        sb.append(", compliant=").append(compliant);
        sb.append(", errors=").append(getErrorCount());
        sb.append(", warnings=").append(getWarningCount());
        sb.append(", validationTime=").append(validationTimeMs).append("ms");
        sb.append("}");
        return sb.toString();
    }
}
