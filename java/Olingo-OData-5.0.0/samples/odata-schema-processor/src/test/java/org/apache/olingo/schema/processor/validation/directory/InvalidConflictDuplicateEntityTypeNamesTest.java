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
 * Test for conflict-duplicate-entitytype-names directory validation.
 * This directory should NOT be compliant as it contains files with duplicate EntityType names.
 */
@DisplayName("Invalid Conflict Duplicate EntityType Names Directory Test")
public class InvalidConflictDuplicateEntityTypeNamesTest {

    private DirectorySchemaValidator validator;
    private final String TEST_RESOURCES_BASE = "src/test/resources/validator/directory";

    @BeforeEach
    void setUp() {
        ModularOlingoXmlValidator fileValidator = new ModularOlingoXmlValidator();
        validator = new DirectorySchemaValidator(fileValidator);
    }

    @Test
    @DisplayName("Should detect duplicate EntityType name conflicts")
    void testConflictDuplicateEntityTypeNames() {
        Path testDir = Paths.get(TEST_RESOURCES_BASE, "invalid-conflict-duplicate-entitytype-names").toAbsolutePath();
        DirectoryValidationResult result = validator.validateDirectory(testDir);
        
        assertFalse(result.isCompliant(), "Directory should not be compliant due to duplicate EntityType names");
        assertEquals(2, result.getTotalFilesProcessed(), "Should process 2 files");
        assertEquals(2, result.getValidFiles(), "Both files should be individually valid");
        assertEquals(0, result.getInvalidFiles(), "No files should be invalid");
        assertTrue(result.hasConflicts(), "Should detect EntityType name conflicts");
        assertTrue(result.hasGlobalErrors(), "Should have global errors");
        
        // Check for specific conflicts
        boolean hasDuplicateElementConflict = result.getConflicts().stream()
            .anyMatch(conflict -> conflict.getDescription().contains("Customer") || 
                                 conflict.getDescription().contains("Customers"));
        assertTrue(hasDuplicateElementConflict, "Should detect duplicate EntityType name conflicts");
    }
}
