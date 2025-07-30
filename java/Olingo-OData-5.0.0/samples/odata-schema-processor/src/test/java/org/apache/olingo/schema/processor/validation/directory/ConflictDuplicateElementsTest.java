package org.apache.olingo.schema.processor.validation.directory;

import org.apache.olingo.schema.processor.validation.file.framework.ModularOlingoXmlValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for conflict-duplicate-elements directory validation.
 * This directory should NOT be compliant as it contains files with duplicate elements.
 */
@DisplayName("Conflict Duplicate Elements Directory Test")
public class ConflictDuplicateElementsTest {

    private DirectorySchemaValidator validator;
    private final String TEST_RESOURCES_BASE = "src/test/resources/validator/directory";

    @BeforeEach
    void setUp() {
        ModularOlingoXmlValidator fileValidator = new ModularOlingoXmlValidator();
        validator = new DirectorySchemaValidator(fileValidator);
    }

    @Test
    @DisplayName("Should detect duplicate element conflicts")
    void testConflictDuplicateElements() {
        Path testDir = Paths.get(TEST_RESOURCES_BASE, "conflict-duplicate-elements").toAbsolutePath();
        DirectoryValidationResult result = validator.validateDirectory(testDir);
        
        assertFalse(result.isCompliant(), "Directory should not be compliant due to element conflicts");
        assertEquals(2, result.getTotalFilesProcessed(), "Should process 2 files");
        assertEquals(2, result.getValidFiles(), "Both files should be individually valid");
        assertEquals(0, result.getInvalidFiles(), "No files should be invalid");
        assertTrue(result.hasConflicts(), "Should detect element conflicts");
        assertTrue(result.hasGlobalErrors(), "Should have global errors");
        
        // Check for specific conflicts
        boolean hasDuplicateElementConflict = result.getConflicts().stream()
            .anyMatch(conflict -> conflict.getDescription().contains("Customer") || 
                                 conflict.getDescription().contains("Customers"));
        assertTrue(hasDuplicateElementConflict, "Should detect duplicate element conflicts");
    }
}
