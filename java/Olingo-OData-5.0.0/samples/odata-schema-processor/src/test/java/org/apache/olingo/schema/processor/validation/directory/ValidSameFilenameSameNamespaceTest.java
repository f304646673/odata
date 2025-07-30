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

@DisplayName("Valid Same Filename Same Namespace Test")
public class ValidSameFilenameSameNamespaceTest {

    private final String TEST_RESOURCES_BASE = "src/test/resources/validator/directory";

    // Test method would validate that:
    // - Directory contains common.xml files in both level1 and level2 subdirectories
    // - Both files use the same namespace (SharedNamespace)
    // - Files have different entities (EntityX vs EntityY)
    // - This should be compliant per OData 4.0 since there are no element conflicts
    private DirectorySchemaValidator validator;

    @BeforeEach
    void setUp() {
        ModularOlingoXmlValidator fileValidator = new ModularOlingoXmlValidator();
        validator = new DirectorySchemaValidator(fileValidator);
    }

    @Test
    @DisplayName("Same filename in different dirs with same namespace and no element conflicts should be compliant")
    void testValidSameFilenameSameNamespace() {
        Path testDir = Paths.get(TEST_RESOURCES_BASE, "valid-valid-same-filename-same-namespace").toAbsolutePath();
        DirectoryValidationResult result = validator.validateDirectory(testDir);
        assertTrue(result.isCompliant(), "Directory should be compliant - same namespace, no element conflicts");
        assertEquals(2, result.getTotalFilesProcessed(), "Should process 2 files");
        assertEquals(2, result.getValidFiles(), "Both files should be individually valid");
        assertFalse(result.hasConflicts(), "Should not detect any conflicts");
    }
}
