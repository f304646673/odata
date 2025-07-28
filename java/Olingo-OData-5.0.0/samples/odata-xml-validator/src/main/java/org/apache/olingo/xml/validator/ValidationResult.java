package org.apache.olingo.xml.validator;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.nio.file.Path;

/**
 * Represents the result of XML validation operations.
 * Contains validation errors, warnings, and metadata about the validation process.
 */
public class ValidationResult {
    
    private final boolean valid;
    private final List<ValidationError> errors;
    private final List<ValidationWarning> warnings;
    private final Path filePath;
    private final long validationTimeMs;
    
    /**
     * Creates a new validation result.
     * 
     * @param valid whether the validation passed
     * @param errors list of validation errors
     * @param warnings list of validation warnings
     * @param filePath path to the validated file/directory
     * @param validationTimeMs time taken for validation in milliseconds
     */
    public ValidationResult(boolean valid, List<ValidationError> errors, 
                          List<ValidationWarning> warnings, Path filePath, long validationTimeMs) {
        this.valid = valid;
        this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
        this.warnings = warnings != null ? new ArrayList<>(warnings) : new ArrayList<>();
        this.filePath = filePath;
        this.validationTimeMs = validationTimeMs;
    }
    
    /**
     * Creates a successful validation result with no errors or warnings.
     */
    public static ValidationResult success(Path filePath, long validationTimeMs) {
        return new ValidationResult(true, new ArrayList<>(), new ArrayList<>(), filePath, validationTimeMs);
    }
    
    /**
     * Creates a failed validation result with errors.
     */
    public static ValidationResult failure(List<ValidationError> errors, Path filePath, long validationTimeMs) {
        return new ValidationResult(false, errors, new ArrayList<>(), filePath, validationTimeMs);
    }
    
    /**
     * Creates a validation result with both errors and warnings.
     */
    public static ValidationResult withErrorsAndWarnings(List<ValidationError> errors, 
                                                       List<ValidationWarning> warnings, 
                                                       Path filePath, long validationTimeMs) {
        boolean isValid = errors == null || errors.isEmpty();
        return new ValidationResult(isValid, errors, warnings, filePath, validationTimeMs);
    }
    
    /**
     * @return true if validation passed without errors
     */
    public boolean isValid() {
        return valid && (errors == null || errors.isEmpty());
    }
    
    /**
     * @return unmodifiable list of validation errors
     */
    public List<ValidationError> getErrors() {
        return Collections.unmodifiableList(errors);
    }
    
    /**
     * @return unmodifiable list of validation warnings
     */
    public List<ValidationWarning> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }
    
    /**
     * @return path to the validated file or directory
     */
    public Path getFilePath() {
        return filePath;
    }
    
    /**
     * @return validation time in milliseconds
     */
    public long getValidationTimeMs() {
        return validationTimeMs;
    }
    
    /**
     * @return total number of errors
     */
    public int getErrorCount() {
        return errors.size();
    }
    
    /**
     * @return total number of warnings
     */
    public int getWarningCount() {
        return warnings.size();
    }
    
    /**
     * Adds an error to this validation result.
     */
    public void addError(ValidationError error) {
        if (error != null) {
            this.errors.add(error);
        }
    }
    
    /**
     * Adds a warning to this validation result.
     */
    public void addWarning(ValidationWarning warning) {
        if (warning != null) {
            this.warnings.add(warning);
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ValidationResult{");
        sb.append("valid=").append(valid);
        sb.append(", errors=").append(errors.size());
        sb.append(", warnings=").append(warnings.size());
        sb.append(", filePath=").append(filePath);
        sb.append(", validationTimeMs=").append(validationTimeMs);
        sb.append('}');
        return sb.toString();
    }
}
