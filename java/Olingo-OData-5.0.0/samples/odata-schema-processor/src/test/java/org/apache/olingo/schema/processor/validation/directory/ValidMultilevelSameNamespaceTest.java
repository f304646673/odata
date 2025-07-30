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
 * Test for multilevel-same-namespace directory validation.
 * This directory SHOULD be compliant as it contains files with the same namespace 
 * but different entities, which is allowed in OData 4.0 specification.
 */
@DisplayName("Multilevel Same Namespace Directory Test")
public class ValidMultilevelSameNamespaceTest {

    private DirectorySchemaValidator validator;
    private final String TEST_RESOURCES_BASE = "src/test/resources/validator/directory";

    @BeforeEach
    void setUp() {
        ModularOlingoXmlValidator fileValidator = new ModularOlingoXmlValidator();
        validator = new DirectorySchemaValidator(fileValidator);
    }

    @Test
    @DisplayName("Same namespace with different entities should be compliant")
    void testMultilevelSameNamespace() {
        Path testDir = Paths.get(TEST_RESOURCES_BASE, "valid-multilevel-same-namespace").toAbsolutePath();
        DirectoryValidationResult result = validator.validateDirectory(testDir);
        
        // This should now be compliant per OData 4.0 specification
        // Same namespace across multiple files is allowed if there are no element conflicts
        assertTrue(result.isCompliant(), "Directory should be compliant - same namespace with different entities is allowed per OData 4.0");
        assertEquals(2, result.getTotalFilesProcessed(), "Should process 2 files");
        assertEquals(2, result.getValidFiles(), "Both files should be individually valid");
        assertEquals(0, result.getInvalidFiles(), "No files should be invalid");
        assertFalse(result.hasConflicts(), "Should not have conflicts - different entities in same namespace is allowed");
        assertFalse(result.hasGlobalErrors(), "Should not have global errors");
    }
}
