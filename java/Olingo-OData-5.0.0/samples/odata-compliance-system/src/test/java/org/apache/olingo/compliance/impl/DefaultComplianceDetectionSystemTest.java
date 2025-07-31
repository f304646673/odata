package org.apache.olingo.compliance.impl;

import org.apache.olingo.compliance.api.ComplianceDetectionSystem;
import org.apache.olingo.compliance.api.ComplianceResult;
import org.apache.olingo.compliance.api.DependencyTreeManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DefaultComplianceDetectionSystem.
 */
class DefaultComplianceDetectionSystemTest {
    
    private ComplianceDetectionSystem system;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        system = new DefaultComplianceDetectionSystem();
    }
    
    @Test
    void testCreateDefaultSystem() {
        assertNotNull(system);
        assertNotNull(system.getFilePathRepository());
        assertNotNull(system.getNamespaceSchemaRepository());
        assertNotNull(system.getDependencyTreeManager());
    }
    
    @Test
    void testValidateNonExistentFile() {
        Path nonExistentFile = tempDir.resolve("non-existent.xml");
        
        ComplianceResult result = system.validateFile(nonExistentFile);
        
        assertNotNull(result);
        assertEquals(nonExistentFile.toString(), result.getFilePath());
        assertFalse(result.isCompliant());
        assertTrue(result.getErrors().size() > 0);
    }
    
    @Test
    void testValidateEmptyFile() throws IOException {
        Path emptyFile = tempDir.resolve("empty.xml");
        Files.createFile(emptyFile);
        
        ComplianceResult result = system.validateFile(emptyFile);
        
        assertNotNull(result);
        assertEquals(emptyFile.toString(), result.getFilePath());
        // Empty file should not be compliant
        assertFalse(result.isCompliant());
    }
    
    @Test
    void testValidateValidODataFile() throws IOException {
        Path validFile = tempDir.resolve("valid.xml");
        String validODataContent = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
            "  <edmx:DataServices>\n" +
            "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"TestService\">\n" +
            "      <EntityType Name=\"Product\">\n" +
            "        <Key>\n" +
            "          <PropertyRef Name=\"ID\"/>\n" +
            "        </Key>\n" +
            "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
            "        <Property Name=\"Name\" Type=\"Edm.String\"/>\n" +
            "      </EntityType>\n" +
            "      <EntityContainer Name=\"TestContainer\">\n" +
            "        <EntitySet Name=\"Products\" EntityType=\"TestService.Product\"/>\n" +
            "      </EntityContainer>\n" +
            "    </Schema>\n" +
            "  </edmx:DataServices>\n" +
            "</edmx:Edmx>";
        
        Files.write(validFile, validODataContent.getBytes());
        
        ComplianceResult result = system.validateFile(validFile);
        
        assertNotNull(result);
        assertEquals(validFile.toString(), result.getFilePath());
        assertTrue(result.isCompliant());
        assertEquals(0, result.getErrors().size());
        assertTrue(result.getProcessingTimeMs() >= 0);
    }
    
    @Test
    void testValidateDirectory() {
        List<ComplianceResult> results = system.validateDirectory(tempDir, false);
        
        assertNotNull(results);
        assertTrue(results.isEmpty()); // Empty directory
    }
    
    @Test
    void testRepositoryInitialState() {
        assertEquals(0, system.getFilePathRepository().size());
        assertEquals(0, system.getNamespaceSchemaRepository().size());
        
        DependencyTreeManager.DependencyStatistics stats = system.getDependencyTreeManager().getStatistics();
        assertEquals(0, stats.getTotalElements());
        assertEquals(0, stats.getElementsWithDependencies());
    }
}
