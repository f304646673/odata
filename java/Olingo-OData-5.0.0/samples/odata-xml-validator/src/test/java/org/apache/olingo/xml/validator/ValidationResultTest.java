package org.apache.olingo.xml.validator;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;

/**
 * Unit tests for ValidationResult class.
 */
public class ValidationResultTest {
    
    private Path testPath;
    private List<ValidationError> errors;
    private List<ValidationWarning> warnings;
    
    @Before
    public void setUp() {
        testPath = Paths.get("test.xml");
        errors = new ArrayList<>();
        warnings = new ArrayList<>();
    }
    
    @Test
    public void testSuccessfulValidationResult() {
        ValidationResult result = ValidationResult.success(testPath, 100L);
        
        assertTrue("Should be valid", result.isValid());
        assertEquals("Should have no errors", 0, result.getErrorCount());
        assertEquals("Should have no warnings", 0, result.getWarningCount());
        assertEquals("Should have correct path", testPath, result.getFilePath());
        assertEquals("Should have correct time", 100L, result.getValidationTimeMs());
    }
    
    @Test
    public void testFailureValidationResult() {
        errors.add(ValidationError.of(ValidationError.ErrorType.XML_FORMAT_ERROR, "Test error", testPath));
        
        ValidationResult result = ValidationResult.failure(errors, testPath, 200L);
        
        assertFalse("Should not be valid", result.isValid());
        assertEquals("Should have one error", 1, result.getErrorCount());
        assertEquals("Should have no warnings", 0, result.getWarningCount());
        assertEquals("Should have correct path", testPath, result.getFilePath());
        assertEquals("Should have correct time", 200L, result.getValidationTimeMs());
    }
    
    @Test
    public void testValidationResultWithErrorsAndWarnings() {
        errors.add(ValidationError.of(ValidationError.ErrorType.SCHEMA_STRUCTURE_ERROR, "Schema error", testPath));
        warnings.add(ValidationWarning.of(ValidationWarning.WarningType.BEST_PRACTICE_VIOLATION, "Warning message", testPath));
        
        ValidationResult result = ValidationResult.withErrorsAndWarnings(errors, warnings, testPath, 300L);
        
        assertFalse("Should not be valid with errors", result.isValid());
        assertEquals("Should have one error", 1, result.getErrorCount());
        assertEquals("Should have one warning", 1, result.getWarningCount());
        assertEquals("Should have correct path", testPath, result.getFilePath());
        assertEquals("Should have correct time", 300L, result.getValidationTimeMs());
    }
    
    @Test
    public void testValidationResultWithWarningsOnly() {
        warnings.add(ValidationWarning.of(ValidationWarning.WarningType.DEPRECATED_FEATURE, "Deprecated", testPath));
        
        ValidationResult result = ValidationResult.withErrorsAndWarnings(null, warnings, testPath, 150L);
        
        assertTrue("Should be valid with warnings only", result.isValid());
        assertEquals("Should have no errors", 0, result.getErrorCount());
        assertEquals("Should have one warning", 1, result.getWarningCount());
    }
    
    @Test
    public void testAddError() {
        ValidationResult result = ValidationResult.success(testPath, 100L);
        
        assertTrue("Should initially be valid", result.isValid());
        
        ValidationError error = ValidationError.of(ValidationError.ErrorType.ELEMENT_DEFINITION_ERROR, "New error", testPath);
        result.addError(error);
        
        assertEquals("Should have one error after adding", 1, result.getErrorCount());
        assertTrue("Should contain added error", result.getErrors().contains(error));
    }
    
    @Test
    public void testAddWarning() {
        ValidationResult result = ValidationResult.success(testPath, 100L);
        
        assertEquals("Should initially have no warnings", 0, result.getWarningCount());
        
        ValidationWarning warning = ValidationWarning.of(ValidationWarning.WarningType.MISSING_OPTIONAL_ELEMENT, "New warning", testPath);
        result.addWarning(warning);
        
        assertEquals("Should have one warning after adding", 1, result.getWarningCount());
        assertTrue("Should contain added warning", result.getWarnings().contains(warning));
    }
    
    @Test
    public void testAddNullError() {
        ValidationResult result = ValidationResult.success(testPath, 100L);
        result.addError(null);
        
        assertEquals("Should not add null error", 0, result.getErrorCount());
    }
    
    @Test
    public void testAddNullWarning() {
        ValidationResult result = ValidationResult.success(testPath, 100L);
        result.addWarning(null);
        
        assertEquals("Should not add null warning", 0, result.getWarningCount());
    }
    
    @Test
    public void testImmutableLists() {
        errors.add(ValidationError.of(ValidationError.ErrorType.XML_FORMAT_ERROR, "Test error", testPath));
        warnings.add(ValidationWarning.of(ValidationWarning.WarningType.COMPATIBILITY_WARNING, "Test warning", testPath));
        
        ValidationResult result = ValidationResult.withErrorsAndWarnings(errors, warnings, testPath, 100L);
        
        List<ValidationError> returnedErrors = result.getErrors();
        List<ValidationWarning> returnedWarnings = result.getWarnings();
        
        try {
            returnedErrors.add(ValidationError.of(ValidationError.ErrorType.DEPENDENCY_ERROR, "Should not add", testPath));
            fail("Should not be able to modify returned errors list");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
        
        try {
            returnedWarnings.add(ValidationWarning.of(ValidationWarning.WarningType.ENCODING_WARNING, "Should not add", testPath));
            fail("Should not be able to modify returned warnings list");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }
    
    @Test
    public void testToString() {
        ValidationResult result = ValidationResult.success(testPath, 150L);
        String str = result.toString();
        
        assertNotNull("toString should not return null", str);
        assertTrue("Should contain class name", str.contains("ValidationResult"));
        assertTrue("Should contain validation time", str.contains("150"));
    }
}
