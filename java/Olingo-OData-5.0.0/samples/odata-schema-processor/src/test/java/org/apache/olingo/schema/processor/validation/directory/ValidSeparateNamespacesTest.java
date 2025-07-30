package org.apache.olingo.schema.processor.validation.directory;

import org.apache.olingo.schema.processor.validation.directory.DirectorySchemaValidator;
import org.apache.olingo.schema.processor.validation.directory.DirectoryValidationResult;
import org.apache.olingo.schema.processor.validation.file.framework.ModularOlingoXmlValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for valid-separate-namespaces directory validation.
 * This directory should be compliant as it contains files with different namespaces.
 */
@DisplayName("Valid Separate Namespaces Directory Test")
public class ValidSeparateNamespacesTest {

    private DirectorySchemaValidator validator;
    private final String TEST_RESOURCES_BASE = "src/test/resources/validator/directory";

    @BeforeEach
    void setUp() {
        ModularOlingoXmlValidator fileValidator = new ModularOlingoXmlValidator();
        validator = new DirectorySchemaValidator(fileValidator);
    }

    @Test
    @DisplayName("Should be compliant with separate namespaces")
    void testValidSeparateNamespaces() {
        Path testDir = Paths.get(TEST_RESOURCES_BASE, "valid-separate-namespaces").toAbsolutePath();
        DirectoryValidationResult result = validator.validateDirectory(testDir);
        
        assertTrue(result.isCompliant(), "Directory with separate namespaces should be compliant");
        assertEquals(2, result.getTotalFilesProcessed(), "Should process 2 files");
        assertEquals(2, result.getValidFiles(), "Both files should be valid");
        assertEquals(0, result.getInvalidFiles(), "No files should be invalid");
        assertFalse(result.hasConflicts(), "Should not have conflicts");
        assertFalse(result.hasGlobalErrors(), "Should not have global errors");
    }
}
