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
 * Test for valid-same-namespace-different-containers directory validation.
 * This directory should be compliant as it contains different EntityContainers
 * in the same namespace, which is allowed by OData 4.0 specification.
 */
@DisplayName("Valid Same Namespace Different Containers Directory Test")
public class ValidSameNamespaceDifferentContainersTest {

    private DirectorySchemaValidator validator;
    private final String TEST_RESOURCES_BASE = "src/test/resources/validator/directory";

    @BeforeEach
    void setUp() {
        ModularOlingoXmlValidator fileValidator = new ModularOlingoXmlValidator();
        validator = new DirectorySchemaValidator(fileValidator);
    }

    @Test
    @DisplayName("Different EntityContainers in same namespace should be compliant")
    void testValidSameNamespaceDifferentContainers() {
        Path testDir = Paths.get(TEST_RESOURCES_BASE, "valid-same-namespace-different-containers").toAbsolutePath();
        DirectoryValidationResult result = validator.validateDirectory(testDir);
        
        // This should be compliant because although both files share the same namespace (DuplicateNamespace),
        // they have different EntityContainer names (MainContainer and SecondContainer)
        // OData 4.0 allows multiple EntityContainers with different names in the same namespace
        assertTrue(result.isCompliant(), "Directory should be compliant - different EntityContainers in same namespace is allowed by OData 4.0");
        assertEquals(2, result.getTotalFilesProcessed(), "Should process 2 files");
        assertEquals(2, result.getValidFiles(), "Both files should be individually valid");
        assertEquals(0, result.getInvalidFiles(), "No files should be invalid");
        assertFalse(result.hasConflicts(), "Should not detect any conflicts");
    }
}
