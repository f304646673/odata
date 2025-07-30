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
 * Test for valid-action-overloading directory validation.
 * This directory should be compliant as it contains files with valid Action overloading.
 */
@DisplayName("Valid Action Overloading Directory Test")
public class ValidActionOverloadingTest {

    private DirectorySchemaValidator validator;
    private final String TEST_RESOURCES_BASE = "src/test/resources/validator/directory";

    @BeforeEach
    void setUp() {
        ModularOlingoXmlValidator fileValidator = new ModularOlingoXmlValidator();
        validator = new DirectorySchemaValidator(fileValidator);
    }

    @Test
    @DisplayName("Should allow valid Action overloading")
    void testValidActionOverloading() {
        Path testDir = Paths.get(TEST_RESOURCES_BASE, "valid-action-overloading").toAbsolutePath();
        DirectoryValidationResult result = validator.validateDirectory(testDir);
        
        assertTrue(result.isCompliant(), "Directory should be compliant with valid Action overloading");
        assertEquals(2, result.getTotalFilesProcessed(), "Should process 2 files");
        assertEquals(2, result.getValidFiles(), "Both files should be individually valid");
        assertEquals(0, result.getInvalidFiles(), "No files should be invalid");
        assertFalse(result.hasConflicts(), "Should not detect conflicts for valid overloading");
        assertFalse(result.hasGlobalErrors(), "Should not have global errors");
    }
}
