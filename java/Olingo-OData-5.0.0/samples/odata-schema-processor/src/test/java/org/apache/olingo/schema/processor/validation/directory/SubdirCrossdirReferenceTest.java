package org.apache.olingo.schema.processor.validation.directory;

import org.apache.olingo.schema.processor.validation.file.framework.ModularOlingoXmlValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for subdir-crossdir-reference directory validation.
 * This directory SHOULD be compliant as cross-directory references are allowed in OData 4.0.
 */
@DisplayName("Subdir Crossdir Reference Directory Test")
public class SubdirCrossdirReferenceTest {

    private DirectorySchemaValidator validator;
    private final String TEST_RESOURCES_BASE = "src/test/resources/validator/directory";

    @BeforeEach
    void setUp() {
        ModularOlingoXmlValidator fileValidator = new ModularOlingoXmlValidator();
        validator = new DirectorySchemaValidator(fileValidator);
    }

    @Test
    @DisplayName("Cross-directory references should be compliant in OData 4.0")
    void testSubdirCrossdirReference() {
        Path testDir = Paths.get(TEST_RESOURCES_BASE, "subdir-crossdir-reference").toAbsolutePath();
        DirectoryValidationResult result = validator.validateDirectory(testDir);
        
        assertTrue(result.isCompliant(), "Directory should be compliant - cross-directory references are allowed in OData 4.0");
        assertEquals(2, result.getTotalFilesProcessed(), "Should process 2 files");
        assertEquals(2, result.getValidFiles(), "Both files should be individually valid");
        assertEquals(0, result.getInvalidFiles(), "No files should be invalid");
        assertFalse(result.hasConflicts(), "Should not detect conflicts for cross-directory references");
        assertFalse(result.hasGlobalErrors(), "Should not have global errors");
    }
}
