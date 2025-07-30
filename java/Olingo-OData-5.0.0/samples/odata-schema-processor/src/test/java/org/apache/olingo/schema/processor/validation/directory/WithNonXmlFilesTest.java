package org.apache.olingo.schema.processor.validation.directory;

import org.apache.olingo.schema.processor.validation.file.framework.ModularOlingoXmlValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for with-non-xml-files directory validation.
 * This directory should be compliant as it contains one valid XML file and ignores non-XML files.
 */
@DisplayName("With Non-XML Files Directory Test")
public class WithNonXmlFilesTest {

    private DirectorySchemaValidator validator;
    private final String TEST_RESOURCES_BASE = "src/test/resources/validator/directory";

    @BeforeEach
    void setUp() {
        ModularOlingoXmlValidator fileValidator = new ModularOlingoXmlValidator();
        validator = new DirectorySchemaValidator(fileValidator);
    }

    @Test
    @DisplayName("Should be compliant ignoring non-XML files")
    void testWithNonXmlFiles() {
        Path testDir = Paths.get(TEST_RESOURCES_BASE, "with-non-xml-files").toAbsolutePath();
        DirectoryValidationResult result = validator.validateDirectory(testDir);
        
        assertTrue(result.isCompliant(), "Directory should be compliant, ignoring non-XML files");
        assertEquals(1, result.getTotalFilesProcessed(), "Should process only the XML file");
        assertEquals(1, result.getValidFiles(), "The XML file should be valid");
        assertEquals(0, result.getInvalidFiles(), "No files should be invalid");
        assertFalse(result.hasConflicts(), "Should not have conflicts");
        assertFalse(result.hasGlobalErrors(), "Should not have global errors");
    }
}
