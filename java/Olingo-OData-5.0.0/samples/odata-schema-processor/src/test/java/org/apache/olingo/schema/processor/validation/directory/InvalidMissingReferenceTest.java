package org.apache.olingo.schema.processor.validation.directory;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.olingo.schema.processor.validation.file.framework.ModularOlingoXmlValidator;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test for missing-reference directory validation.
 * This directory should NOT be compliant as it contains missing references.
 */
@DisplayName("Missing Reference Directory Test")
public class InvalidMissingReferenceTest {

    private DirectorySchemaValidator validator;
    private final String TEST_RESOURCES_BASE = "src/test/resources/validator/directory";

    @BeforeEach
    void setUp() {
        ModularOlingoXmlValidator fileValidator = new ModularOlingoXmlValidator();
        validator = new DirectorySchemaValidator(fileValidator);
    }

    @Test
    @DisplayName("Should detect missing references as non-compliant")
    void testMissingReference() {
        Path testDir = Paths.get(TEST_RESOURCES_BASE, "invalid-missing-reference").toAbsolutePath();
        DirectoryValidationResult result = validator.validateDirectory(testDir);
        
        assertFalse(result.isCompliant(), "Directory should not be compliant due to missing references");
        
        // The exact number of files may vary depending on directory structure
        assertTrue(result.getTotalFilesProcessed() > 0, "Should process at least 1 file");
        
        // Check that either we have invalid files or conflicts indicating missing references
        boolean hasIssues = result.getInvalidFiles() > 0 || result.hasConflicts() || result.hasGlobalErrors();
        assertTrue(hasIssues, "Should detect missing references through invalid files, conflicts, or global errors");
    }
}
