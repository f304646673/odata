package org.apache.olingo.compliance.test.validation.multiple.invalid;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.olingo.compliance.validator.directory.DirectoryValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.olingo.compliance.core.model.ComplianceErrorType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * Test class for validating OData schema element conflicts across multiple XML files.
 * This tests directory-level validation for invalid scenarios where schema elements
 * conflict across multiple files.
 */
@DisplayName("Element Conflict Error Tests")
public class ElementConflictErrorTest {

    private Path multipleValidationRoot;
    private Path invalidDirectoriesRoot;
    private DirectoryValidation validationManager;

    /**
     * Test case definition for invalid directory scenarios
     */
    private static class InvalidDirectoryTestCase {
        final String subdirectory;
        final ComplianceErrorType errorType;
        final String expectedMessageFragment;
        final String description;

        InvalidDirectoryTestCase(String subdirectory, ComplianceErrorType errorType, String expectedMessageFragment, String description) {
            this.subdirectory = subdirectory;
            this.errorType = errorType;
            this.expectedMessageFragment = expectedMessageFragment;
            this.description = description;
        }
    }

    /**
     * Provides test cases for invalid directory scenarios
     */
    private static final List<InvalidDirectoryTestCase> INVALID_DIRECTORY_TEST_CASES = Arrays.asList(
        new InvalidDirectoryTestCase("element-conficts/scenario1-entitytype-conflicts", ComplianceErrorType.ELEMENT_CONFLICT, "EntityType", "EntityType conflicts in same namespace"),
        new InvalidDirectoryTestCase("element-conficts/scenario1-complextype-conflicts", ComplianceErrorType.ELEMENT_CONFLICT, "ComplexType", "ComplexType conflicts in same namespace"),
        new InvalidDirectoryTestCase("element-conficts/scenario1-enumtype-conflicts", ComplianceErrorType.ELEMENT_CONFLICT, "EnumType", "EnumType conflicts in same namespace"),
        new InvalidDirectoryTestCase("element-conficts/scenario1-function-conflicts", ComplianceErrorType.ELEMENT_CONFLICT, "Function", "Function conflicts with same signature"),
        new InvalidDirectoryTestCase("element-conficts/scenario1-action-conflicts", ComplianceErrorType.ELEMENT_CONFLICT, "Action", "Action conflicts with same signature"),
        new InvalidDirectoryTestCase("element-conficts/scenario1-typedefinition-conflicts", ComplianceErrorType.ELEMENT_CONFLICT, "TypeDefinition", "TypeDefinition conflicts in same namespace"),
        new InvalidDirectoryTestCase("element-conficts/scenario1-alias-conflicts", ComplianceErrorType.ALIAS_CONFLICT, "alias", "Schema alias conflicts across namespaces"),
        new InvalidDirectoryTestCase("element-conficts/scenario1-term-conflicts", ComplianceErrorType.ELEMENT_CONFLICT, "Term", "Term conflicts in same namespace"),
        new InvalidDirectoryTestCase("element-conficts/scenario1-annotation-conflicts", ComplianceErrorType.ELEMENT_CONFLICT, "Annotation", "Annotation conflicts for same target"),
        new InvalidDirectoryTestCase("element-conficts/scenario1-entitycontainer-conflicts", ComplianceErrorType.ELEMENT_CONFLICT, "EntityContainer", "EntityContainer conflicts in same namespace"),
        new InvalidDirectoryTestCase("element-conficts/scenario1-entityset-conflicts", ComplianceErrorType.ELEMENT_CONFLICT, "EntitySet", "EntitySet conflicts in same container"),
        new InvalidDirectoryTestCase("element-conficts/scenario1-singleton-conflicts", ComplianceErrorType.ELEMENT_CONFLICT, "Singleton", "Singleton conflicts in same container"),
        new InvalidDirectoryTestCase("element-conficts/scenario1-functionimport-conflicts", ComplianceErrorType.ELEMENT_CONFLICT, "FunctionImport", "FunctionImport conflicts in same container"),
        new InvalidDirectoryTestCase("element-conficts/scenario1-actionimport-conflicts", ComplianceErrorType.ELEMENT_CONFLICT, "ActionImport", "ActionImport conflicts in same container"),
        new InvalidDirectoryTestCase("element-conficts/scenario1-navigationproperty-conflicts", ComplianceErrorType.ELEMENT_CONFLICT, "NavigationProperty", "NavigationProperty conflicts in same entity"),
        new InvalidDirectoryTestCase("element-conficts/scenario1-referentialconstraint-conflicts", ComplianceErrorType.ELEMENT_CONFLICT, "ReferentialConstraint", "ReferentialConstraint conflicts"),
        new InvalidDirectoryTestCase("alias-conflicts", ComplianceErrorType.ALIAS_CONFLICT, "alias", "Cross-namespace alias conflicts"),
        new InvalidDirectoryTestCase("invalid-inheritance", ComplianceErrorType.INVALID_INHERITANCE_HIERARCHY, "inheritance", "Invalid inheritance hierarchy"),
        new InvalidDirectoryTestCase("dependency-conflicts", ComplianceErrorType.SCHEMA_DEPENDENCY_ERROR, "dependency", "Schema dependency conflicts")
    );

    @BeforeEach
    public void setUp() {
        multipleValidationRoot = Paths.get("src/test/resources/validation/multiple");
        invalidDirectoriesRoot = multipleValidationRoot.resolve("invalid");
        validationManager = new DirectoryValidation();
    }

    /**
     * Test each invalid directory scenario
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("getInvalidDirectoryTestCases")
    @DisplayName("Invalid Directory Validation")
    public void testInvalidDirectory(InvalidDirectoryTestCase testCase) throws IOException {
        // Arrange
        Path testDirectory = invalidDirectoriesRoot.resolve(testCase.subdirectory);
        
        // Skip test if directory doesn't exist
        if (!Files.exists(testDirectory) || !Files.isDirectory(testDirectory)) {
            System.out.printf("Skipping test for non-existent directory: %s%n", testDirectory);
            return;
        }

        // Act
        DirectoryValidation.DirectoryValidationResult result = validationManager.validateSingleDirectory(testDirectory.toString());

        // Assert
        assertNotNull(result, "Validation result should not be null for " + testCase.description);
        assertFalse(result.isValid(), "Should have validation errors for " + testCase.description);
        assertTrue(result.getTotalIssueCount() > 0, "Should have at least one issue for " + testCase.description);
        
        // Check for specific error type
        boolean hasExpectedErrorType = result.getAllIssues().stream()
            .anyMatch(issue -> issue.getErrorType() == testCase.errorType);
        assertTrue(hasExpectedErrorType, 
            String.format("Should have %s error for %s. Found error types: %s", 
                testCase.errorType, testCase.description,
                result.getAllIssues().stream()
                    .map(issue -> issue.getErrorType().toString())
                    .collect(Collectors.joining(", "))));

        // Check for expected message fragment
        boolean hasExpectedMessage = result.getAllIssues().stream()
            .anyMatch(issue -> issue.getMessage().toLowerCase().contains(testCase.expectedMessageFragment.toLowerCase()));
        assertTrue(hasExpectedMessage, 
            String.format("Should contain '%s' in error messages for %s. Found messages: %s", 
                testCase.expectedMessageFragment, testCase.description,
                result.getAllIssues().stream()
                    .map(issue -> issue.getMessage())
                    .collect(Collectors.joining("; "))));
    }

    /**
     * Test that all invalid directories actually exist
     */
    @Test
    @DisplayName("Verify Invalid Test Directories Exist")
    public void testInvalidDirectoriesExist() throws IOException {
        int existingDirs = 0;
        int totalDirs = INVALID_DIRECTORY_TEST_CASES.size();
        
        for (InvalidDirectoryTestCase testCase : INVALID_DIRECTORY_TEST_CASES) {
            Path testDirectory = invalidDirectoriesRoot.resolve(testCase.subdirectory);
            if (Files.exists(testDirectory) && Files.isDirectory(testDirectory)) {
                existingDirs++;
                
                // Verify directory has XML files
                List<Path> xmlFiles = Files.list(testDirectory)
                    .filter(p -> p.toString().endsWith(".xml"))
                    .collect(Collectors.toList());
                
                assertFalse(xmlFiles.isEmpty(), 
                    String.format("Invalid test directory should contain XML files: %s", testDirectory));
            }
        }
        
        System.out.printf("Found %d/%d invalid test directories%n", existingDirs, totalDirs);
        assertTrue(existingDirs > 0, "At least some invalid test directories should exist");
    }

    /**
     * Test common element conflict scenarios
     */
    @Test
    @DisplayName("Element Conflicts - EntityType")
    public void testEntityTypeConflicts() throws IOException {
        testDirectoryForErrorType("element-conficts/scenario1-entitytype-conflicts", 
            ComplianceErrorType.ELEMENT_CONFLICT, "EntityType");
    }

    @Test
    @DisplayName("Element Conflicts - ComplexType")
    public void testComplexTypeConflicts() throws IOException {
        testDirectoryForErrorType("element-conficts/scenario1-complextype-conflicts", 
            ComplianceErrorType.ELEMENT_CONFLICT, "ComplexType");
    }

    @Test
    @DisplayName("Element Conflicts - EnumType")
    public void testEnumTypeConflicts() throws IOException {
        testDirectoryForErrorType("element-conficts/scenario1-enumtype-conflicts", 
            ComplianceErrorType.ELEMENT_CONFLICT, "EnumType");
    }

    @Test
    @DisplayName("Alias Conflicts")
    public void testAliasConflicts() throws IOException {
        testDirectoryForErrorType("alias-conflicts", 
            ComplianceErrorType.ALIAS_CONFLICT, "alias");
    }

    /**
     * Helper method to test a directory for specific error type
     */
    private void testDirectoryForErrorType(String subdirectory, ComplianceErrorType expectedErrorType, String expectedMessageFragment) throws IOException {
        Path testDirectory = invalidDirectoriesRoot.resolve(subdirectory);
        
        if (!Files.exists(testDirectory)) {
            System.out.printf("Skipping test for non-existent directory: %s%n", testDirectory);
            return;
        }

        DirectoryValidation.DirectoryValidationResult result = validationManager.validateDirectory(testDirectory.toString());
        
        assertNotNull(result, "Validation result should not be null");
        assertFalse(result.isValid(), "Should have validation errors");
        
        boolean hasExpectedErrorType = result.getAllIssues().stream()
            .anyMatch(issue -> issue.getErrorType() == expectedErrorType);
        assertTrue(hasExpectedErrorType, 
            String.format("Should have %s error. Found: %s", 
                expectedErrorType,
                result.getAllIssues().stream()
                    .map(issue -> issue.getErrorType().toString())
                    .collect(Collectors.joining(", "))));
    }

    /**
     * Provides test cases for parameterized tests
     */
    private static Object[][] getInvalidDirectoryTestCases() {
        return INVALID_DIRECTORY_TEST_CASES.stream()
            .map(testCase -> new Object[]{testCase})
            .toArray(Object[][]::new);
    }
}
