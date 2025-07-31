package org.apache.olingo.compliance.file;

/**
 * Represents a specific compliance issue (error or warning) with its type, severity, and details.
 * This provides structured information for precise validation.
 */
public class ComplianceIssue {
    
    /**
     * Severity levels for compliance issues
     */
    public enum Severity {
        ERROR("Error"),    // Compliance violation that makes the file invalid
        WARNING("Warning"); // Potential issue that doesn't invalidate the file
        
        private final String displayName;
        
        Severity(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private final ComplianceErrorType errorType;
    private final String message;
    private final String elementName;
    private final String location;
    private final Severity severity;
    
    /**
     * Constructor for ComplianceIssue with all details
     * 
     * @param errorType the specific type of compliance error
     * @param message detailed error message
     * @param elementName name of the element causing the error (optional)
     * @param location location/path of the error in the XML (optional)
     * @param severity severity level of the issue
     */
    public ComplianceIssue(ComplianceErrorType errorType, String message, String elementName, String location, Severity severity) {
        this.errorType = errorType;
        this.message = message;
        this.elementName = elementName;
        this.location = location;
        this.severity = severity;
    }
    
    /**
     * Constructor for ComplianceIssue with just type and message (defaults to ERROR severity)
     * 
     * @param errorType the specific type of compliance error
     * @param message detailed error message
     */
    public ComplianceIssue(ComplianceErrorType errorType, String message) {
        this(errorType, message, null, null, Severity.ERROR);
    }
    
    /**
     * Constructor for ComplianceIssue with type, message, and severity
     * 
     * @param errorType the specific type of compliance error
     * @param message detailed error message
     * @param severity severity level of the issue
     */
    public ComplianceIssue(ComplianceErrorType errorType, String message, Severity severity) {
        this(errorType, message, null, null, severity);
    }
    
    /**
     * @return the specific type of compliance error
     */
    public ComplianceErrorType getErrorType() {
        return errorType;
    }
    
    /**
     * @return detailed error message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * @return name of the element causing the error
     */
    public String getElementName() {
        return elementName;
    }
    
    /**
     * @return location/path of the error in the XML
     */
    public String getLocation() {
        return location;
    }
    
    /**
     * @return severity level of the issue
     */
    public Severity getSeverity() {
        return severity;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(severity.getDisplayName()).append("] ");
        sb.append(errorType.getDescription());
        if (elementName != null && !elementName.isEmpty()) {
            sb.append(" [").append(elementName).append("]");
        }
        sb.append(": ").append(message);
        if (location != null && !location.isEmpty()) {
            sb.append(" at ").append(location);
        }
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        ComplianceIssue that = (ComplianceIssue) o;
        
        if (errorType != that.errorType) return false;
        if (severity != that.severity) return false;
        if (message != null ? !message.equals(that.message) : that.message != null) return false;
        if (elementName != null ? !elementName.equals(that.elementName) : that.elementName != null) return false;
        return location != null ? location.equals(that.location) : that.location == null;
    }
    
    @Override
    public int hashCode() {
        int result = errorType != null ? errorType.hashCode() : 0;
        result = 31 * result + (severity != null ? severity.hashCode() : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (elementName != null ? elementName.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        return result;
    }
}
