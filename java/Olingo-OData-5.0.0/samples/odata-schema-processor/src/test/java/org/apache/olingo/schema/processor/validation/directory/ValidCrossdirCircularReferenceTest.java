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
 * Test for crossdir-circular-reference directory validation.
 * This directory SHOULD be compliant as circular references are allowed in OData 4.0.
 */
@DisplayName("Crossdir Circular Reference Directory Test")
public class ValidCrossdirCircularReferenceTest {

    private DirectorySchemaValidator validator;
    private final String TEST_RESOURCES_BASE = "src/test/resources/validator/directory";

    @BeforeEach
    void setUp() {
        ModularOlingoXmlValidator fileValidator = new ModularOlingoXmlValidator();
        validator = new DirectorySchemaValidator(fileValidator);
    }

    @Test
    @DisplayName("Circular references should be compliant in OData 4.0")
    void testCrossdirCircularReference() {
        Path testDir = Paths.get(TEST_RESOURCES_BASE, "valid-crossdir-circular-reference").toAbsolutePath();
        DirectoryValidationResult result = validator.validateDirectory(testDir);
        
        assertTrue(result.isCompliant(), "Directory should be compliant - circular references are allowed in OData 4.0");
        assertEquals(3, result.getTotalFilesProcessed(), "Should process 2 files");
        assertEquals(3, result.getValidFiles(), "Both files should be individually valid");
        assertEquals(0, result.getInvalidFiles(), "No files should be invalid");
        assertFalse(result.hasConflicts(), "Should not detect conflicts for circular references");
        assertFalse(result.hasGlobalErrors(), "Should not have global errors");
    }
}
