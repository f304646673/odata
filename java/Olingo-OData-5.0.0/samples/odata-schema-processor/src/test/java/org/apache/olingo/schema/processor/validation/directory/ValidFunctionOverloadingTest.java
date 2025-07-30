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
 * Test for valid-function-overloading directory validation.
 * This directory should be compliant as it contains Function overloading,
 * which is allowed by OData 4.0 when signatures are different.
 */
@DisplayName("Valid Function Overloading Directory Test")
public class ValidFunctionOverloadingTest {

    private DirectorySchemaValidator validator;
    private final String TEST_RESOURCES_BASE = "src/test/resources/validator/directory";

    @BeforeEach
    void setUp() {
        ModularOlingoXmlValidator fileValidator = new ModularOlingoXmlValidator();
        validator = new DirectorySchemaValidator(fileValidator);
    }

    @Test
    @DisplayName("Function overloading with different signatures should be compliant")
    void testValidFunctionOverloading() {
        Path testDir = Paths.get(TEST_RESOURCES_BASE, "valid-function-overloading").toAbsolutePath();
        DirectoryValidationResult result = validator.validateDirectory(testDir);
        
        // This should be compliant because OData 4.0 allows Function overloading
        // with different signatures, even if they have the same name
        assertTrue(result.isCompliant(), "Directory should be compliant - Function overloading is allowed by OData 4.0");
        assertEquals(2, result.getTotalFilesProcessed(), "Should process 2 files");
        assertEquals(2, result.getValidFiles(), "Both files should be individually valid");
        assertEquals(0, result.getInvalidFiles(), "No files should be invalid");
        assertFalse(result.hasConflicts(), "Should not detect conflicts for valid overloading");
    }
}
