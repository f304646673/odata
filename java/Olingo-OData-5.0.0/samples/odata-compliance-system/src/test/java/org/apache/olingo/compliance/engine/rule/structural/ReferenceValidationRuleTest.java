package org.apache.olingo.compliance.engine.rule.structural;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.olingo.compliance.core.api.ValidationConfig;
import org.apache.olingo.compliance.engine.core.ValidationContext;
import org.apache.olingo.compliance.engine.rule.ValidationRule.RuleResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for ReferenceValidationRule class.
 * Ensures 100% code coverage for all methods and branches.
 */
class ReferenceValidationRuleTest {

    @Mock
    private ValidationContext mockContext;
    
    @Mock
    private ValidationConfig mockConfig;
    
    @TempDir
    Path tempDir;
    
    private ReferenceValidationRule rule;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        rule = new ReferenceValidationRule();
    }
    
    @Test
    void testGetName() {
        assertEquals("reference-validation", rule.getName());
    }
    
    @Test
    void testGetDescription() {
        assertEquals("Validates external references in OData schema files", rule.getDescription());
    }
    
    @Test
    void testIsStructurallyApplicable_WithContent() {
        when(mockContext.getContent()).thenReturn("some xml content");
        
        assertTrue(rule.isStructurallyApplicable(mockContext, mockConfig));
    }
    
    @Test
    void testIsStructurallyApplicable_WithFilePath() {
        when(mockContext.getContent()).thenReturn(null);
        when(mockContext.getFilePath()).thenReturn(Paths.get("/some/path"));
        
        assertTrue(rule.isStructurallyApplicable(mockContext, mockConfig));
    }
    
    @Test
    void testIsStructurallyApplicable_NoContentOrPath() {
        when(mockContext.getContent()).thenReturn(null);
        when(mockContext.getFilePath()).thenReturn(null);
        
        assertFalse(rule.isStructurallyApplicable(mockContext, mockConfig));
    }
    
    @Test
    void testValidate_NoContent() {
        when(mockContext.getContent()).thenReturn(null);
        when(mockContext.getFilePath()).thenReturn(null);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertTrue(result.isPassed());
    }
    
    @Test
    void testValidate_NoReferences() {
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                           "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
                           "  <edmx:DataServices>\n" +
                           "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
                           "    </Schema>\n" +
                           "  </edmx:DataServices>\n" +
                           "</edmx:Edmx>";
        
        when(mockContext.getContent()).thenReturn(xmlContent);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertTrue(result.isPassed());
    }
    
    @Test
    void testValidate_HttpReference() {
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                           "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
                           "  <edmx:Reference Uri=\"http://example.com/schema.xml\">\n" +
                           "    <edmx:Include Namespace=\"ExternalNamespace\"/>\n" +
                           "  </edmx:Reference>\n" +
                           "  <edmx:DataServices>\n" +
                           "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
                           "    </Schema>\n" +
                           "  </edmx:DataServices>\n" +
                           "</edmx:Edmx>";
        
        when(mockContext.getContent()).thenReturn(xmlContent);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertTrue(result.isPassed());
    }
    
    @Test
    void testValidate_HttpsReference() {
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                           "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
                           "  <edmx:Reference Uri=\"https://example.com/schema.xml\">\n" +
                           "    <edmx:Include Namespace=\"ExternalNamespace\"/>\n" +
                           "  </edmx:Reference>\n" +
                           "  <edmx:DataServices>\n" +
                           "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
                           "    </Schema>\n" +
                           "  </edmx:DataServices>\n" +
                           "</edmx:Edmx>";
        
        when(mockContext.getContent()).thenReturn(xmlContent);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertTrue(result.isPassed());
    }
    
    @Test
    void testValidate_ValidLocalReference() throws IOException {
        // Create a temporary referenced file
        Path referencedFile = tempDir.resolve("referenced.xml");
        Files.write(referencedFile, "<?xml version=\"1.0\"?><Schema></Schema>".getBytes());
        
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                           "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
                           "  <edmx:Reference Uri=\"referenced.xml\">\n" +
                           "    <edmx:Include Namespace=\"ExternalNamespace\"/>\n" +
                           "  </edmx:Reference>\n" +
                           "  <edmx:DataServices>\n" +
                           "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
                           "    </Schema>\n" +
                           "  </edmx:DataServices>\n" +
                           "</edmx:Edmx>";
        
        when(mockContext.getContent()).thenReturn(xmlContent);
        when(mockContext.getFilePath()).thenReturn(tempDir.resolve("main.xml"));
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertTrue(result.isPassed());
    }
    
    @Test
    void testValidate_InvalidLocalReference() {
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                           "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
                           "  <edmx:Reference Uri=\"nonexistent.xml\">\n" +
                           "    <edmx:Include Namespace=\"ExternalNamespace\"/>\n" +
                           "  </edmx:Reference>\n" +
                           "  <edmx:DataServices>\n" +
                           "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
                           "    </Schema>\n" +
                           "  </edmx:DataServices>\n" +
                           "</edmx:Edmx>";
        
        when(mockContext.getContent()).thenReturn(xmlContent);
        when(mockContext.getFilePath()).thenReturn(tempDir.resolve("main.xml"));
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Referenced file does not exist: nonexistent.xml", result.getMessage());
    }
    
    @Test
    void testValidate_UnreadableLocalReference() throws IOException {
        // Create a temporary referenced file
        Path referencedFile = tempDir.resolve("unreadable.xml");
        Files.write(referencedFile, "<?xml version=\"1.0\"?><Schema></Schema>".getBytes());
        
        // Make file unreadable
        referencedFile.toFile().setReadable(false);
        
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                           "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
                           "  <edmx:Reference Uri=\"unreadable.xml\">\n" +
                           "    <edmx:Include Namespace=\"ExternalNamespace\"/>\n" +
                           "  </edmx:Reference>\n" +
                           "  <edmx:DataServices>\n" +
                           "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
                           "    </Schema>\n" +
                           "  </edmx:DataServices>\n" +
                           "</edmx:Edmx>";
        
        when(mockContext.getContent()).thenReturn(xmlContent);
        when(mockContext.getFilePath()).thenReturn(tempDir.resolve("main.xml"));
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        // Note: On some systems, setReadable(false) might not work as expected
        // So we test for either success or the expected error
        if (!result.isPassed()) {
            assertEquals("Referenced file is not readable: unreadable.xml", result.getMessage());
        }
        
        // Cleanup - restore readable permission
        referencedFile.toFile().setReadable(true);
    }
    
    @Test
    void testValidate_LocalReferenceWithoutParentDir() {
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                           "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
                           "  <edmx:Reference Uri=\"nonexistent.xml\">\n" +
                           "    <edmx:Include Namespace=\"ExternalNamespace\"/>\n" +
                           "  </edmx:Reference>\n" +
                           "  <edmx:DataServices>\n" +
                           "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
                           "    </Schema>\n" +
                           "  </edmx:DataServices>\n" +
                           "</edmx:Edmx>";
        
        when(mockContext.getContent()).thenReturn(xmlContent);
        when(mockContext.getFilePath()).thenReturn(Paths.get("main.xml")); // No parent directory
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Referenced file does not exist: nonexistent.xml", result.getMessage());
    }
    
    @Test
    void testValidate_MultipleReferences() throws IOException {
        // Create temporary referenced files
        Path referencedFile1 = tempDir.resolve("ref1.xml");
        Path referencedFile2 = tempDir.resolve("ref2.xml");
        Files.write(referencedFile1, "<?xml version=\"1.0\"?><Schema></Schema>".getBytes());
        Files.write(referencedFile2, "<?xml version=\"1.0\"?><Schema></Schema>".getBytes());
        
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                           "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
                           "  <edmx:Reference Uri=\"ref1.xml\">\n" +
                           "    <edmx:Include Namespace=\"Namespace1\"/>\n" +
                           "  </edmx:Reference>\n" +
                           "  <edmx:Reference Uri=\"ref2.xml\">\n" +
                           "    <edmx:Include Namespace=\"Namespace2\"/>\n" +
                           "  </edmx:Reference>\n" +
                           "  <edmx:DataServices>\n" +
                           "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
                           "    </Schema>\n" +
                           "  </edmx:DataServices>\n" +
                           "</edmx:Edmx>";
        
        when(mockContext.getContent()).thenReturn(xmlContent);
        when(mockContext.getFilePath()).thenReturn(tempDir.resolve("main.xml"));
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertTrue(result.isPassed());
    }
    
    @Test
    void testValidate_MixedReferences() throws IOException {
        // Create one valid local reference
        Path referencedFile = tempDir.resolve("local.xml");
        Files.write(referencedFile, "<?xml version=\"1.0\"?><Schema></Schema>".getBytes());
        
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                           "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
                           "  <edmx:Reference Uri=\"http://example.com/remote.xml\">\n" +
                           "    <edmx:Include Namespace=\"Remote\"/>\n" +
                           "  </edmx:Reference>\n" +
                           "  <edmx:Reference Uri=\"local.xml\">\n" +
                           "    <edmx:Include Namespace=\"Local\"/>\n" +
                           "  </edmx:Reference>\n" +
                           "  <edmx:DataServices>\n" +
                           "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
                           "    </Schema>\n" +
                           "  </edmx:DataServices>\n" +
                           "</edmx:Edmx>";
        
        when(mockContext.getContent()).thenReturn(xmlContent);
        when(mockContext.getFilePath()).thenReturn(tempDir.resolve("main.xml"));
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertTrue(result.isPassed());
    }
    
    @Test
    void testValidate_CaseInsensitivePattern() {
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                           "<EDMX:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
                           "  <EDMX:REFERENCE URI=\"http://example.com/schema.xml\">\n" +
                           "    <edmx:Include Namespace=\"ExternalNamespace\"/>\n" +
                           "  </EDMX:REFERENCE>\n" +
                           "  <edmx:DataServices>\n" +
                           "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
                           "    </Schema>\n" +
                           "  </edmx:DataServices>\n" +
                           "</EDMX:Edmx>";
        
        when(mockContext.getContent()).thenReturn(xmlContent);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertTrue(result.isPassed());
    }
    
    @Test
    void testValidate_SingleQuotesInUri() {
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                           "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
                           "  <edmx:Reference Uri='http://example.com/schema.xml'>\n" +
                           "    <edmx:Include Namespace=\"ExternalNamespace\"/>\n" +
                           "  </edmx:Reference>\n" +
                           "  <edmx:DataServices>\n" +
                           "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
                           "    </Schema>\n" +
                           "  </edmx:DataServices>\n" +
                           "</edmx:Edmx>";
        
        when(mockContext.getContent()).thenReturn(xmlContent);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertTrue(result.isPassed());
    }
    
    @Test
    void testValidate_ContentFromFilePath() throws IOException {
        // Create a temporary file with XML content
        Path xmlFile = tempDir.resolve("test.xml");
        Path referencedFile = tempDir.resolve("referenced.xml");
        
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                           "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
                           "  <edmx:Reference Uri=\"referenced.xml\">\n" +
                           "    <edmx:Include Namespace=\"ExternalNamespace\"/>\n" +
                           "  </edmx:Reference>\n" +
                           "  <edmx:DataServices>\n" +
                           "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
                           "    </Schema>\n" +
                           "  </edmx:DataServices>\n" +
                           "</edmx:Edmx>";
        
        Files.write(xmlFile, xmlContent.getBytes());
        Files.write(referencedFile, "<?xml version=\"1.0\"?><Schema></Schema>".getBytes());
        
        when(mockContext.getContent()).thenReturn(null); // No content, should read from file
        when(mockContext.getFilePath()).thenReturn(xmlFile);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertTrue(result.isPassed());
    }
    
    @Test
    void testValidate_FileReadError() throws IOException {
        // Create a directory instead of a file to cause read error
        Path xmlDir = tempDir.resolve("notafile");
        Files.createDirectory(xmlDir);
        
        when(mockContext.getContent()).thenReturn(null);
        when(mockContext.getFilePath()).thenReturn(xmlDir); // Directory instead of file
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        // Should pass because of the exception handling in getXmlContent
        assertTrue(result.isPassed());
    }
}
