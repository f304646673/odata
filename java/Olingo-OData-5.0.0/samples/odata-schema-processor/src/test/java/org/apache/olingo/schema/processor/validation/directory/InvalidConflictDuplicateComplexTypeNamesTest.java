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
 * Test for invalid-conflict-duplicate-complextype-names directory validation.
 * This directory should NOT be compliant as it contains multiple files with
 * the same namespace and the same ComplexType name, which violates OData 4.0.
 */
@DisplayName("Invalid Conflict Duplicate ComplexType Names Directory Test")
public class InvalidConflictDuplicateComplexTypeNamesTest {

    private DirectorySchemaValidator validator;
    private final String TEST_RESOURCES_BASE = "src/test/resources/validator/directory";

    @BeforeEach
    void setUp() {
        ModularOlingoXmlValidator fileValidator = new ModularOlingoXmlValidator();
        validator = new DirectorySchemaValidator(fileValidator);
    }

    @Test
    @DisplayName("Same ComplexType names in same namespace should be non-compliant")
    void testConflictDuplicateComplexTypeNames() {
        Path testDir = Paths.get(TEST_RESOURCES_BASE, "invalid-conflict-duplicate-complextype-names").toAbsolutePath();
        DirectoryValidationResult result = validator.validateDirectory(testDir);
        
        // This should be non-compliant because both files have the same namespace (ConflictNamespace)
        // and the same ComplexType name (Address), which violates OData 4.0
        // OData 4.0 requires unique ComplexType names within a namespace
        assertFalse(result.isCompliant(), "Directory should not be compliant - duplicate ComplexType names in same namespace violates OData 4.0");
        assertEquals(2, result.getTotalFilesProcessed(), "Should process 2 files");
        assertEquals(2, result.getValidFiles(), "Both files should be individually valid");
        assertEquals(0, result.getInvalidFiles(), "No files should be invalid");
        assertTrue(result.hasConflicts(), "Should detect ComplexType name conflicts");
        
        // Check for specific element conflict
        boolean hasElementConflict = result.getConflicts().stream()
            .anyMatch(conflict -> conflict.getDescription().contains("Address") && 
                                 conflict.getType() == SchemaConflict.ConflictType.DUPLICATE_ELEMENT);
        assertTrue(hasElementConflict, "Should detect duplicate ComplexType element conflicts");
    }
}
