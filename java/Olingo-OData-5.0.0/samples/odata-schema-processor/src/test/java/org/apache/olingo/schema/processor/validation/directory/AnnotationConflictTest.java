package org.apache.olingo.schema.processor.validation.directory;

import org.apache.olingo.schema.processor.validation.file.framework.ModularOlingoXmlValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for annotation-conflict directory validation.
 * This directory should NOT be compliant as it contains annotation conflicts.
 */
@DisplayName("Annotation Conflict Directory Test")
public class AnnotationConflictTest {

    private DirectorySchemaValidator validator;
    private final String TEST_RESOURCES_BASE = "src/test/resources/validator/directory";

    @BeforeEach
    void setUp() {
        ModularOlingoXmlValidator fileValidator = new ModularOlingoXmlValidator();
        validator = new DirectorySchemaValidator(fileValidator);
    }

    @Test
    @DisplayName("Should detect annotation conflicts")
    void testAnnotationConflict() {
        Path testDir = Paths.get(TEST_RESOURCES_BASE, "annotation-conflict").toAbsolutePath();
        DirectoryValidationResult result = validator.validateDirectory(testDir);
        
        assertFalse(result.isCompliant(), "Directory should not be compliant due to annotation conflicts");
        assertEquals(2, result.getTotalFilesProcessed(), "Should process 2 files");
        assertEquals(2, result.getValidFiles(), "Both files should be individually valid");
        assertEquals(0, result.getInvalidFiles(), "No files should be invalid");
        assertTrue(result.hasConflicts(), "Should detect annotation conflicts");
        
        // Check for specific annotation conflict
        boolean hasAnnotationConflict = result.getConflicts().stream()
            .anyMatch(conflict -> conflict.getDescription().toLowerCase().contains("annotation"));
        assertTrue(hasAnnotationConflict, "Should detect annotation conflicts");
    }
}
