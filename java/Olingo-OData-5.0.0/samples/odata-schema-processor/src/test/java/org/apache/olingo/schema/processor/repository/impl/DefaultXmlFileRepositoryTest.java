package org.apache.olingo.schema.processor.repository.impl;

import org.apache.olingo.schema.processor.repository.XmlFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DefaultXmlFileRepository测试类
 */
class DefaultXmlFileRepositoryTest {
    
    private DefaultXmlFileRepository repository;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        repository = new DefaultXmlFileRepository();
    }
    
    @Test
    void testLoadFromDirectory_Success() throws IOException {
        // 创建测试XML文件
        Path xmlFile1 = tempDir.resolve("test1.xml");
        Files.write(xmlFile1, createTestXmlContent1().getBytes());
        
        Path xmlFile2 = tempDir.resolve("test2.xml");
        Files.write(xmlFile2, createTestXmlContent2().getBytes());
        
        // 测试加载
        XmlFileRepository.LoadResult result = repository.loadFromDirectory(tempDir);
        
        assertTrue(result.isSuccess());
        assertEquals(2, result.getTotalFiles());
        assertEquals(2, result.getSuccessfulFiles());
        assertTrue(result.getErrors().isEmpty());
    }
    
    @Test
    void testLoadFromDirectory_NonExistentPath() {
        Path nonExistentPath = tempDir.resolve("nonexistent");
        
        XmlFileRepository.LoadResult result = repository.loadFromDirectory(nonExistentPath);
        
        assertFalse(result.isSuccess());
        assertEquals(0, result.getTotalFiles());
        assertEquals(0, result.getSuccessfulFiles());
        assertFalse(result.getErrors().isEmpty());
        assertTrue(result.getErrors().get(0).contains("does not exist"));
    }
    
    @Test
    void testLoadFromDirectory_EmptyDirectory() {
        XmlFileRepository.LoadResult result = repository.loadFromDirectory(tempDir);
        
        assertTrue(result.isSuccess());
        assertEquals(0, result.getTotalFiles());
        assertEquals(0, result.getSuccessfulFiles());
        assertTrue(result.getErrors().isEmpty());
    }
    
    @Test
    void testGetXmlContent() throws IOException {
        Path xmlFile = tempDir.resolve("test.xml");
        String content = createTestXmlContent1();
        Files.write(xmlFile, content.getBytes());
        
        repository.loadFromDirectory(tempDir);
        
        String retrievedContent = repository.getXmlContent(xmlFile);
        assertEquals(content, retrievedContent);
    }
    
    @Test
    void testGetAllXmlFiles() throws IOException {
        Path xmlFile1 = tempDir.resolve("test1.xml");
        Path xmlFile2 = tempDir.resolve("test2.xml");
        
        Files.write(xmlFile1, createTestXmlContent1().getBytes());
        Files.write(xmlFile2, createTestXmlContent2().getBytes());
        
        repository.loadFromDirectory(tempDir);
        
        Set<Path> xmlFiles = repository.getAllXmlFiles();
        assertEquals(2, xmlFiles.size());
        assertTrue(xmlFiles.contains(xmlFile1));
        assertTrue(xmlFiles.contains(xmlFile2));
    }
    
    @Test
    void testNamespaceMapping() throws IOException {
        Path xmlFile = tempDir.resolve("test.xml");
        Files.write(xmlFile, createTestXmlContent1().getBytes());
        
        repository.loadFromDirectory(tempDir);
        
        Map<Path, Set<String>> fileToNamespace = repository.getFileToNamespaceMapping();
        assertTrue(fileToNamespace.containsKey(xmlFile));
        assertTrue(fileToNamespace.get(xmlFile).contains("Microsoft.OData.Core.Test.Common"));
        
        Map<String, Set<Path>> namespaceToFile = repository.getNamespaceToFileMapping();
        assertTrue(namespaceToFile.containsKey("Microsoft.OData.Core.Test.Common"));
        assertTrue(namespaceToFile.get("Microsoft.OData.Core.Test.Common").contains(xmlFile));
    }
    
    @Test
    void testIsNamespaceDefined() throws IOException {
        Path xmlFile = tempDir.resolve("test.xml");
        Files.write(xmlFile, createTestXmlContent1().getBytes());
        
        repository.loadFromDirectory(tempDir);
        
        assertTrue(repository.isNamespaceDefined("Microsoft.OData.Core.Test.Common"));
        assertFalse(repository.isNamespaceDefined("NonExistent.Namespace"));
    }
    
    @Test
    void testGetFilesDefiningNamespace() throws IOException {
        Path xmlFile1 = tempDir.resolve("test1.xml");
        Path xmlFile2 = tempDir.resolve("test2.xml");
        
        Files.write(xmlFile1, createTestXmlContent1().getBytes());
        Files.write(xmlFile2, createTestXmlContent2().getBytes());
        
        repository.loadFromDirectory(tempDir);
        
        Set<Path> filesForCommon = repository.getFilesDefiningNamespace("Microsoft.OData.Core.Test.Common");
        assertEquals(1, filesForCommon.size());
        assertTrue(filesForCommon.contains(xmlFile1));
        
        Set<Path> filesForAddress = repository.getFilesDefiningNamespace("Microsoft.OData.Core.Test.Address");
        assertEquals(1, filesForAddress.size());
        assertTrue(filesForAddress.contains(xmlFile2));
        
        Set<Path> filesForNonExistent = repository.getFilesDefiningNamespace("NonExistent");
        assertTrue(filesForNonExistent.isEmpty());
    }
    
    @Test
    void testLoadFromDirectory_NestedDirectories() throws IOException {
        // 创建嵌套目录结构
        Path subDir1 = tempDir.resolve("sub1");
        Path subDir2 = tempDir.resolve("sub2");
        Files.createDirectories(subDir1);
        Files.createDirectories(subDir2);
        
        Path xmlFile1 = subDir1.resolve("test1.xml");
        Path xmlFile2 = subDir2.resolve("test2.xml");
        
        Files.write(xmlFile1, createTestXmlContent1().getBytes());
        Files.write(xmlFile2, createTestXmlContent2().getBytes());
        
        XmlFileRepository.LoadResult result = repository.loadFromDirectory(tempDir);
        
        assertTrue(result.isSuccess());
        assertEquals(2, result.getTotalFiles());
        assertEquals(2, result.getSuccessfulFiles());
        
        Set<Path> allFiles = repository.getAllXmlFiles();
        assertTrue(allFiles.contains(xmlFile1));
        assertTrue(allFiles.contains(xmlFile2));
    }
    
    private String createTestXmlContent1() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
               "  <edmx:DataServices>\n" +
               "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"Microsoft.OData.Core.Test.Common\">\n" +
               "      <EntityType Name=\"Person\">\n" +
               "        <Key>\n" +
               "          <PropertyRef Name=\"ID\"/>\n" +
               "        </Key>\n" +
               "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
               "        <Property Name=\"Name\" Type=\"Edm.String\"/>\n" +
               "      </EntityType>\n" +
               "    </Schema>\n" +
               "  </edmx:DataServices>\n" +
               "</edmx:Edmx>";
    }
    
    private String createTestXmlContent2() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
               "  <edmx:DataServices>\n" +
               "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"Microsoft.OData.Core.Test.Address\">\n" +
               "      <Using Namespace=\"Microsoft.OData.Core.Test.Common\" Alias=\"Common\"/>\n" +
               "      <EntityType Name=\"Address\">\n" +
               "        <Key>\n" +
               "          <PropertyRef Name=\"ID\"/>\n" +
               "        </Key>\n" +
               "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
               "        <Property Name=\"Street\" Type=\"Edm.String\"/>\n" +
               "      </EntityType>\n" +
               "    </Schema>\n" +
               "  </edmx:DataServices>\n" +
               "</edmx:Edmx>";
    }
}
