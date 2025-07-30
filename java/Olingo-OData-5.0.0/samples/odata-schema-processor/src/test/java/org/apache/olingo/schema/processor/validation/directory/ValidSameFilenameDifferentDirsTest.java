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

@DisplayName("Valid Same Filename Different Dirs Test")
public class ValidSameFilenameDifferentDirsTest {

    private final String TEST_RESOURCES_BASE = "src/test/resources/validator/directory";

    // Test method would validate that:
    // - Directory contains schema.xml files in both dirA and dirB subdirectories
    // - Each file has a different namespace (DirA.Schema vs DirB.Schema)
    // - Files have different entities (EntityA vs EntityB)
    // - This should be compliant per OData 4.0 since namespaces are different
    private DirectorySchemaValidator validator;

    @BeforeEach
    void setUp() {
        ModularOlingoXmlValidator fileValidator = new ModularOlingoXmlValidator();
        validator = new DirectorySchemaValidator(fileValidator);
    }

    @Test
    @DisplayName("Same filename in different dirs with different namespaces should be compliant")
    void testValidSameFilenameDifferentDirs() {
        Path testDir = Paths.get(TEST_RESOURCES_BASE, "valid-valid-same-filename-different-dirs").toAbsolutePath();
        DirectoryValidationResult result = validator.validateDirectory(testDir);
        assertTrue(result.isCompliant(), "Directory should be compliant - different namespaces");
        assertEquals(2, result.getTotalFilesProcessed(), "Should process 2 files");
        assertEquals(2, result.getValidFiles(), "Both files should be individually valid");
        assertFalse(result.hasConflicts(), "Should not detect any conflicts");
    }
}
