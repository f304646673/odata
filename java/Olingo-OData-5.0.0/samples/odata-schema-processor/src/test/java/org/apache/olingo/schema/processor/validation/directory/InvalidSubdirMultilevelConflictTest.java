package org.apache.olingo.schema.processor.validation.directory;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.olingo.schema.processor.validation.file.framework.ModularOlingoXmlValidator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test for subdir-multilevel-conflict directory validation.
 * This directory should NOT be compliant as it contains conflicting elements
 * (same namespace with same EntityType name).
 */
@DisplayName("Subdir Multilevel Conflict Directory Test")
public class InvalidSubdirMultilevelConflictTest {

    private DirectorySchemaValidator validator;
    private final String TEST_RESOURCES_BASE = "src/test/resources/validator/directory";

    @BeforeEach
    void setUp() {
        ModularOlingoXmlValidator fileValidator = new ModularOlingoXmlValidator();
        validator = new DirectorySchemaValidator(fileValidator);
    }

    @Test
    @DisplayName("Should detect conflicts for same namespace with same EntityType")
    void testSubdirMultilevelConflict() {
        Path testDir = Paths.get(TEST_RESOURCES_BASE, "invalid-subdir-multilevel-conflict").toAbsolutePath();
        DirectoryValidationResult result = validator.validateDirectory(testDir);
        
        assertFalse(result.isCompliant(), "Directory should not be compliant due to element conflicts");
        assertEquals(2, result.getTotalFilesProcessed(), "Should process 2 files");
        assertEquals(2, result.getValidFiles(), "Both files should be individually valid");
        assertEquals(0, result.getInvalidFiles(), "No files should be invalid");
        assertTrue(result.hasConflicts(), "Should detect element conflicts");
        
        // Check for specific element conflict
        boolean hasElementConflict = result.getConflicts().stream()
            .anyMatch(conflict -> conflict.getDescription().toLowerCase().contains("duplicate") ||
                                 conflict.getDescription().toLowerCase().contains("conflict"));
        assertTrue(hasElementConflict, "Should detect element conflicts");
    }
}
