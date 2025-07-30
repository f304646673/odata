package org.apache.olingo.schema.processor.validation.directory;

import org.apache.olingo.schema.processor.validation.file.framework.ModularOlingoXmlValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for mixed-valid-invalid directory validation.
 * This directory should NOT be compliant as it contains both valid and invalid files.
 */
@DisplayName("Mixed Valid Invalid Directory Test")
public class MixedValidInvalidTest {

    private DirectorySchemaValidator validator;
    private final String TEST_RESOURCES_BASE = "src/test/resources/validator/directory";

    @BeforeEach
    void setUp() {
        ModularOlingoXmlValidator fileValidator = new ModularOlingoXmlValidator();
        validator = new DirectorySchemaValidator(fileValidator);
    }

    @Test
    @DisplayName("Should not be compliant with mixed valid and invalid files")
    void testMixedValidInvalid() {
        Path testDir = Paths.get(TEST_RESOURCES_BASE, "mixed-valid-invalid").toAbsolutePath();
        DirectoryValidationResult result = validator.validateDirectory(testDir);
        
        assertFalse(result.isCompliant(), "Directory should not be compliant due to invalid files");
        assertEquals(2, result.getTotalFilesProcessed(), "Should process 2 files");
        assertEquals(1, result.getValidFiles(), "Should have 1 valid file");
        assertEquals(1, result.getInvalidFiles(), "Should have 1 invalid file");
        // May or may not have conflicts depending on implementation
    }
}
