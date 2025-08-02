package org.apache.olingo.compliance.test.validation.multiple.invalid;

import org.apache.olingo.compliance.core.model.ComplianceErrorType;
import org.apache.olingo.compliance.core.model.ComplianceResult;
import org.apache.olingo.compliance.engine.core.impl.DefaultSchemaRegistryImpl;
import org.apache.olingo.compliance.validator.ComplianceValidator;
import org.apache.olingo.compliance.validator.impl.ComplianceValidatorImpl;
import org.apache.olingo.compliance.engine.core.SchemaRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ElementConflictErrorTest {

    private ComplianceValidator validator;
    private SchemaRegistry schemaRegistry;

    @BeforeEach
    void setUp() {
        validator = new ComplianceValidatorImpl();
        schemaRegistry = new DefaultSchemaRegistryImpl();
    }

    @Test
    void testNamespaceConflictDetection() {
        String testDirectory = "src/test/resources/validation/multiple/invalid/element-conflicts/namespace-conflicts";

        ComplianceResult result = validator.validateDirectory(testDirectory, schemaRegistry, true);

        assertFalse(result.isCompliant());
        assertTrue(result.getIssues().stream()
            .anyMatch(issue -> issue.getErrorType() == ComplianceErrorType.NAMESPACE_CONFLICT));
    }

    @Test
    void testTypeDefinitionConflicts() {
        String testDirectory = "src/test/resources/validation/multiple/invalid/element-conflicts/typedefinition-conflicts";

        ComplianceResult result = validator.validateDirectory(testDirectory, schemaRegistry, true);

        assertFalse(result.isCompliant());
        assertTrue(result.getIssues().stream()
            .anyMatch(issue -> issue.getErrorType() == ComplianceErrorType.NAMESPACE_CONFLICT));
    }
}
