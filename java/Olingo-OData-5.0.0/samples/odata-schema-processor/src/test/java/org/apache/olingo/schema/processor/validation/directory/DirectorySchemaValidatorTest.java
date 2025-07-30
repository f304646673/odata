package org.apache.olingo.schema.processor.validation.directory;

import org.apache.olingo.schema.processor.validation.file.XmlComplianceResult;
import org.apache.olingo.schema.processor.validation.file.framework.ModularOlingoXmlValidator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for DirectorySchemaValidator.
 * Tests various scenarios including valid cases, conflicts, and edge cases.
 */
class DirectorySchemaValidatorTest {
    
    private DirectorySchemaValidator validator;
    private Path testResourcesPath;
    
    @BeforeEach
    void setUp() {
        validator = new DirectorySchemaValidator(new ModularOlingoXmlValidator());
        
        // Get the test resources directory
        testResourcesPath = Paths.get("src/test/resources/validation/directory");
    }
    
    @AfterEach
    void tearDown() {
        if (validator != null) {
            validator.shutdown();
        }
    }
    
    @Test
    @DisplayName("Should validate directory with separate namespaces successfully")
    void testValidDirectoryWithSeparateNamespaces() {
        // Given
        Path validDirectory = testResourcesPath.resolve("valid-separate-namespaces");
        
        // When
        DirectoryValidationResult result = validator.validateDirectory(validDirectory);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isCompliant(), "Directory should be compliant");
        assertEquals(2, result.getTotalFilesProcessed(), "Should process 2 files");
        assertEquals(2, result.getValidFiles(), "Both files should be valid");
        assertEquals(0, result.getInvalidFiles(), "No files should be invalid");
        assertFalse(result.hasConflicts(), "Should have no conflicts");
        assertFalse(result.hasGlobalErrors(), "Should have no global errors");
        
        // Check namespace mapping
        Map<String, Set<String>> namespaceToFiles = result.getNamespaceToFiles();
        assertEquals(2, namespaceToFiles.size(), "Should have 2 namespaces");
        assertTrue(namespaceToFiles.containsKey("Namespace1"), "Should contain Namespace1");
        assertTrue(namespaceToFiles.containsKey("Namespace2"), "Should contain Namespace2");
        
        // Check individual file results
        assertNotNull(result.getFileResult("schema1.xml"));
        assertNotNull(result.getFileResult("schema2.xml"));
        assertTrue(result.getFileResult("schema1.xml").isCompliant());
        assertTrue(result.getFileResult("schema2.xml").isCompliant());
    }
    
    @Test
    @DisplayName("Should detect duplicate element conflicts")
    void testDuplicateElementConflicts() {
        // Given
        Path conflictDirectory = testResourcesPath.resolve("conflict-duplicate-elements");
        
        // When
        DirectoryValidationResult result = validator.validateDirectory(conflictDirectory);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isCompliant(), "Directory should not be compliant due to conflicts");
        assertEquals(2, result.getTotalFilesProcessed(), "Should process 2 files");
        assertTrue(result.hasConflicts(), "Should have conflicts");
        
        // Check conflicts - may have multiple conflicts due to all elements in the same namespace
        assertTrue(result.getConflicts().size() >= 1, "Should have at least 1 conflict, found: " + result.getConflicts().size());
        
        // Check that there is at least one duplicate element conflict
        boolean hasDuplicateElementConflict = result.getConflicts().stream()
            .anyMatch(conflict -> conflict.getType() == SchemaConflict.ConflictType.DUPLICATE_ELEMENT);
        assertTrue(hasDuplicateElementConflict, "Should have at least one duplicate element conflict");
        
        // Check that Customer entity is involved in conflicts
        boolean hasCustomerConflict = result.getConflicts().stream()
            .anyMatch(conflict -> "Customer".equals(conflict.getElementName()));
        assertTrue(hasCustomerConflict, "Should have conflict involving Customer entity");
    }
    
    @Test
    @DisplayName("Should detect duplicate namespace schema conflicts")
    void testDuplicateNamespaceConflicts() {
        // Given
        Path conflictDirectory = testResourcesPath.resolve("conflict-duplicate-namespace");
        
        // When
        DirectoryValidationResult result = validator.validateDirectory(conflictDirectory);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isCompliant(), "Directory should not be compliant due to conflicts");
        assertTrue(result.hasConflicts(), "Should have conflicts");
        
        // Check for duplicate namespace schema conflict
        boolean hasDuplicateNamespaceConflict = result.getConflicts().stream()
            .anyMatch(conflict -> conflict.getType() == SchemaConflict.ConflictType.DUPLICATE_NAMESPACE_SCHEMA);
        assertTrue(hasDuplicateNamespaceConflict, "Should have duplicate namespace schema conflict");
        
        // Check namespace conflict
        assertTrue(result.hasNamespaceConflicts("DuplicateNamespace"), 
                  "Should detect conflicts in DuplicateNamespace");
    }
    
    @Test
    @DisplayName("Should handle mixed valid and invalid files")
    void testMixedValidInvalidFiles() {
        // Given
        Path mixedDirectory = testResourcesPath.resolve("mixed-valid-invalid");
        
        // When
        DirectoryValidationResult result = validator.validateDirectory(mixedDirectory);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isCompliant(), "Directory should not be compliant due to invalid files");
        assertEquals(2, result.getTotalFilesProcessed(), "Should process 2 files");
        assertEquals(1, result.getValidFiles(), "Should have 1 valid file");
        assertEquals(1, result.getInvalidFiles(), "Should have 1 invalid file");
        
        // Check individual file results
        XmlComplianceResult validResult = result.getFileResult("valid.xml");
        XmlComplianceResult invalidResult = result.getFileResult("invalid.xml");
        
        assertNotNull(validResult);
        assertNotNull(invalidResult);
        assertTrue(validResult.isCompliant(), "valid.xml should be compliant");
        assertFalse(invalidResult.isCompliant(), "invalid.xml should not be compliant");
    }
    
    @Test
    @DisplayName("Should handle non-existent directory")
    void testNonExistentDirectory() {
        // Given
        Path nonExistentDirectory = testResourcesPath.resolve("non-existent");
        
        // When
        DirectoryValidationResult result = validator.validateDirectory(nonExistentDirectory);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isCompliant(), "Should not be compliant");
        assertTrue(result.hasGlobalErrors(), "Should have global errors");
        assertEquals(0, result.getTotalFilesProcessed(), "Should process 0 files");
        assertTrue(result.getGlobalErrors().get(0).contains("does not exist"));
    }
    
    @Test
    @DisplayName("Should handle empty directory")
    void testEmptyDirectory(@TempDir Path tempDir) throws IOException {
        // Given - empty directory
        
        // When
        DirectoryValidationResult result = validator.validateDirectory(tempDir);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isCompliant(), "Empty directory should be compliant");
        assertEquals(0, result.getTotalFilesProcessed(), "Should process 0 files");
        assertFalse(result.hasConflicts(), "Should have no conflicts");
        assertFalse(result.hasGlobalErrors(), "Should have no global errors");
        assertEquals(1, result.getGlobalWarnings().size(), "Should have warning about no files");
    }
    
    @Test
    @DisplayName("Should validate with custom file pattern")
    void testCustomFilePattern(@TempDir Path tempDir) throws IOException {
        // Given
        Files.write(tempDir.resolve("schema.xml"), getValidXmlContent("TestNamespace").getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("config.xml"), getValidXmlContent("ConfigNamespace").getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("data.txt"), "not xml content".getBytes(StandardCharsets.UTF_8));

        // When - validate only files starting with "schema"
        DirectoryValidationResult result = validator.validateDirectory(tempDir, "schema*.xml");
        
        // Then
        assertNotNull(result);
        assertTrue(result.isCompliant(), "Should be compliant");
        assertEquals(1, result.getTotalFilesProcessed(), "Should process only 1 file matching pattern");
        assertEquals(1, result.getValidFiles(), "Should have 1 valid file");
        assertTrue(result.getFileResults().containsKey("schema.xml"));
        assertFalse(result.getFileResults().containsKey("config.xml"));
        assertFalse(result.getFileResults().containsKey("data.txt"));
    }
    
    @Test
    @DisplayName("Should provide comprehensive result summary")
    void testResultSummary() {
        // Given
        Path validDirectory = testResourcesPath.resolve("valid-separate-namespaces");
        
        // When
        DirectoryValidationResult result = validator.validateDirectory(validDirectory);
        String summary = result.getSummary();
        
        // Then
        assertNotNull(summary);
        assertTrue(summary.contains("Directory Validation Summary"));
        assertTrue(summary.contains("Total Files: 2"));
        assertTrue(summary.contains("Valid Files: 2"));
        assertTrue(summary.contains("Invalid Files: 0"));
        assertTrue(summary.contains("Conflicts: 0"));
        assertTrue(summary.contains("Overall Compliant: true"));
        assertTrue(summary.contains("Validation Time:"));
    }
    
    @Test
    @DisplayName("Should handle concurrent validation properly")
    void testConcurrentValidation(@TempDir Path tempDir) throws IOException {
        // Given - create multiple valid XML files
        for (int i = 1; i <= 10; i++) {
            String content = getValidXmlContent("Namespace" + i);
            Files.write(tempDir.resolve("schema" + i + ".xml"), content.getBytes(StandardCharsets.UTF_8));
        }
        
        // When
        DirectoryValidationResult result = validator.validateDirectory(tempDir);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isCompliant(), "All files should be valid");
        assertEquals(10, result.getTotalFilesProcessed(), "Should process 10 files");
        assertEquals(10, result.getValidFiles(), "All 10 files should be valid");
        assertEquals(0, result.getInvalidFiles(), "No files should be invalid");
        assertEquals(10, result.getNamespaceToFiles().size(), "Should have 10 different namespaces");
    }
    
    @Test
    @DisplayName("Should detect namespace conflicts correctly")
    void testNamespaceConflictDetection() {
        // Given
        Path conflictDirectory = testResourcesPath.resolve("conflict-duplicate-namespace");
        
        // When
        DirectoryValidationResult result = validator.validateDirectory(conflictDirectory);
        
        // Then
        assertTrue(result.hasNamespaceConflicts("DuplicateNamespace"));
        assertFalse(result.hasNamespaceConflicts("NonExistentNamespace"));
        
        Set<String> files = result.getFilesForNamespace("DuplicateNamespace");
        assertEquals(2, files.size());
        assertTrue(files.contains("ns1.xml"));
        assertTrue(files.contains("ns2.xml"));
    }
    
    @Test
    @DisplayName("Should handle files with XML parsing errors")
    void testXmlParsingErrors(@TempDir Path tempDir) throws IOException {
        // Given
        Files.write(tempDir.resolve("valid.xml"), getValidXmlContent("ValidNamespace").getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("malformed.xml"), "<?xml version=\"1.0\"?><invalid><unclosed>".getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("empty.xml"), "".getBytes(StandardCharsets.UTF_8));

        // When
        DirectoryValidationResult result = validator.validateDirectory(tempDir);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isCompliant(), "Should not be compliant due to parsing errors");
        assertEquals(3, result.getTotalFilesProcessed(), "Should attempt to process 3 files");
        assertTrue(result.getInvalidFiles() >= 2, "At least 2 files should be invalid");
        
        // Check that valid file still validates correctly
        XmlComplianceResult validResult = result.getFileResult("valid.xml");
        assertNotNull(validResult);
        assertTrue(validResult.isCompliant(), "Valid file should still be compliant");
    }
    
    @Test
    @DisplayName("Should validate performance with large number of files")
    void testPerformanceWithManyFiles(@TempDir Path tempDir) throws IOException {
        // Given - create 50 valid XML files
        for (int i = 1; i <= 50; i++) {
            String content = getValidXmlContent("Namespace" + i);
            Files.write(tempDir.resolve("schema" + i + ".xml"), content.getBytes(StandardCharsets.UTF_8));
        }
        
        // When
        long startTime = System.currentTimeMillis();
        DirectoryValidationResult result = validator.validateDirectory(tempDir);
        long validationTime = System.currentTimeMillis() - startTime;
        
        // Then
        assertNotNull(result);
        assertTrue(result.isCompliant(), "All files should be valid");
        assertEquals(50, result.getTotalFilesProcessed(), "Should process 50 files");
        assertEquals(50, result.getValidFiles(), "All 50 files should be valid");
        
        // Performance assertion - should complete within reasonable time
        assertTrue(validationTime < 30000, "Validation should complete within 30 seconds");
        
        // Validation time in result should be reasonable
        assertTrue(result.getValidationTimeMs() > 0, "Validation time should be positive");
        assertTrue(result.getValidationTimeMs() < 30000, "Validation time should be reasonable");
    }
    
    /**
     * Helper method to generate valid XML content for testing
     */
    private String getValidXmlContent(String namespace) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "    <edmx:DataServices>\n" +
            "        <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"" + namespace + "\">\n" +
            "            <EntityType Name=\"TestEntity\">\n" +
            "                <Key>\n" +
            "                    <PropertyRef Name=\"ID\"/>\n" +
            "                </Key>\n" +
            "                <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "                <Property Name=\"Name\" Type=\"Edm.String\" MaxLength=\"100\"/>\n" +
            "            </EntityType>\n" +
            "            \n" +
            "            <EntityContainer Name=\"TestContainer\">\n" +
            "                <EntitySet Name=\"TestEntities\" EntityType=\"" + namespace + ".TestEntity\"/>\n" +
            "            </EntityContainer>\n" +
            "        </Schema>\n" +
            "    </edmx:DataServices>\n" +
            "</edmx:Edmx>";
    }
}
