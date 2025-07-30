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
 * Test for invalid-conflict-duplicate-action-names directory validation.
 * This directory should NOT be compliant as it contains files with duplicate Action names.
 */
@DisplayName("Invalid Conflict Duplicate Action Names Directory Test")
public class InvalidConflictDuplicateActionNamesTest {

    private DirectorySchemaValidator validator;
    private final String TEST_RESOURCES_BASE = "src/test/resources/validator/directory";

    @BeforeEach
    void setUp() {
        ModularOlingoXmlValidator fileValidator = new ModularOlingoXmlValidator();
        validator = new DirectorySchemaValidator(fileValidator);
    }

    @Test
    @DisplayName("Should detect duplicate Action name conflicts")
    void testConflictDuplicateActionNames() {
        Path testDir = Paths.get(TEST_RESOURCES_BASE, "invalid-conflict-duplicate-action-names").toAbsolutePath();
        DirectoryValidationResult result = validator.validateDirectory(testDir);
        
        assertFalse(result.isCompliant(), "Directory should not be compliant due to duplicate Action names");
        assertEquals(2, result.getTotalFilesProcessed(), "Should process 2 files");
        assertEquals(2, result.getValidFiles(), "Both files should be individually valid");
        assertEquals(0, result.getInvalidFiles(), "No files should be invalid");
        assertTrue(result.hasConflicts(), "Should detect Action name conflicts");
        assertTrue(result.hasGlobalErrors(), "Should have global errors");
        
        // Check for specific conflicts
        boolean hasDuplicateElementConflict = result.getConflicts().stream()
            .anyMatch(conflict -> conflict.getDescription().contains("PromoteEmployee"));
        assertTrue(hasDuplicateElementConflict, "Should detect duplicate Action name conflicts");
    }
}
