package org.apache.olingo.schemamanager.loader.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.loader.ODataXmlLoader;
import org.apache.olingo.schemamanager.parser.ODataSchemaParser;
import org.apache.olingo.schemamanager.repository.SchemaRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultODataXmlLoaderTest {

    @Mock
    private ODataSchemaParser parser;

    @Mock
    private SchemaRepository repository;

    private DefaultODataXmlLoader loader;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        loader = new DefaultODataXmlLoader();
        // Use reflection to set private fields for testing
        try {
            java.lang.reflect.Field parserField = DefaultODataXmlLoader.class.getDeclaredField("parser");
            parserField.setAccessible(true);
            parserField.set(loader, parser);
            
            java.lang.reflect.Field repositoryField = DefaultODataXmlLoader.class.getDeclaredField("repository");
            repositoryField.setAccessible(true);
            repositoryField.set(loader, repository);
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup test", e);
        }
    }

    @Test
    void testLoadFromDirectory_Success() throws IOException {
        // 使用测试资源文件而不是硬编码XML
        String xmlContent = loadTestResourceAsString("xml-schemas/valid/simple-schema.xml");
        
        File xmlFile = tempDir.resolve("test.xml").toFile();
        try (FileWriter writer = new FileWriter(xmlFile)) {
            writer.write(xmlContent);
        }

        // Mock解析结果
        CsdlSchema mockSchema = new CsdlSchema();
        mockSchema.setNamespace("TestService"); // 与simple-schema.xml中的namespace保持一致
        
        ODataSchemaParser.ParseResult mockParseResult = new ODataSchemaParser.ParseResult(
            mockSchema, new ArrayList<>(), true, null
        );

        when(parser.parseSchema(any(), anyString())).thenReturn(mockParseResult);

        // 执行测试
        ODataXmlLoader.LoadResult result = loader.loadFromDirectory(tempDir.toString());

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalFiles());
        assertEquals(1, result.getSuccessfulFiles());
        assertEquals(0, result.getFailedFiles());
        assertTrue(result.getErrorMessages().isEmpty());

        // 验证repository被调用
        verify(repository).addSchema(eq(mockSchema), anyString());
    }

    @Test
    void testLoadFromDirectory_NonExistentDirectory() {
        // 测试不存在的目录
        ODataXmlLoader.LoadResult result = loader.loadFromDirectory("/non/existent/directory");

        assertNotNull(result);
        assertEquals(0, result.getTotalFiles());
        assertEquals(0, result.getSuccessfulFiles());
        assertEquals(1, result.getFailedFiles());
        assertFalse(result.getErrorMessages().isEmpty());
        assertTrue(result.getErrorMessages().get(0).contains("Directory not found"));
    }

    @Test
    void testLoadFromDirectory_ParseFailure() throws IOException {
        // 使用测试资源中的无效XML文件内容
        String invalidXmlContent = loadTestResourceAsString("xml-schemas/invalid/malformed-xml.xml");
        
        File xmlFile = tempDir.resolve("invalid.xml").toFile();
        try (FileWriter writer = new FileWriter(xmlFile)) {
            writer.write(invalidXmlContent);
        }

        // Mock解析失败
        ODataSchemaParser.ParseResult mockParseResult = new ODataSchemaParser.ParseResult(
            null, new ArrayList<>(), false, "Parse error"
        );

        when(parser.parseSchema(any(), anyString())).thenReturn(mockParseResult);

        // 执行测试
        ODataXmlLoader.LoadResult result = loader.loadFromDirectory(tempDir.toString());

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalFiles());
        assertEquals(0, result.getSuccessfulFiles());
        assertEquals(1, result.getFailedFiles());
        assertFalse(result.getErrorMessages().isEmpty());

        // 验证repository没有被调用
        verify(repository, never()).addSchema(any(), anyString());
    }

    @Test
    void testLoadFromDirectory_EmptyDirectory() {
        // 测试空目录
        ODataXmlLoader.LoadResult result = loader.loadFromDirectory(tempDir.toString());

        assertNotNull(result);
        assertEquals(0, result.getTotalFiles());
        assertEquals(0, result.getSuccessfulFiles());
        assertEquals(0, result.getFailedFiles());
        assertTrue(result.getErrorMessages().isEmpty());
    }

    @Test
    void testLoadFromDirectory_RecursiveLoading() throws IOException {
        // 创建子目录和XML文件
        File subDir = tempDir.resolve("subdir").toFile();
        subDir.mkdir();
        
        // 使用测试资源文件的内容
        String simpleSchemaContent = loadTestResourceAsString("xml-schemas/valid/simple-schema.xml");
        String complexSchemaContent = loadTestResourceAsString("xml-schemas/valid/complex-types-schema.xml");
        
        File xmlFile1 = tempDir.resolve("test1.xml").toFile();
        File xmlFile2 = subDir.toPath().resolve("test2.xml").toFile();
        
        try (FileWriter writer = new FileWriter(xmlFile1)) {
            writer.write(simpleSchemaContent);
        }
        try (FileWriter writer = new FileWriter(xmlFile2)) {
            writer.write(complexSchemaContent);
        }

        // Mock解析结果
        CsdlSchema mockSchema = new CsdlSchema();
        mockSchema.setNamespace("TestService"); // 与资源文件中的namespace保持一致
        
        ODataSchemaParser.ParseResult mockParseResult = new ODataSchemaParser.ParseResult(
            mockSchema, new ArrayList<>(), true, null
        );

        when(parser.parseSchema(any(), anyString())).thenReturn(mockParseResult);

        // 执行测试
        ODataXmlLoader.LoadResult result = loader.loadFromDirectory(tempDir.toString());

        // 验证结果 - 应该找到两个XML文件
        assertNotNull(result);
        assertEquals(2, result.getTotalFiles());
        assertEquals(2, result.getSuccessfulFiles());
        assertEquals(0, result.getFailedFiles());

        // 验证repository被调用两次
        verify(repository, times(2)).addSchema(eq(mockSchema), anyString());
    }

    @Test
    void testLoadSingleFile_Success() throws IOException {
        // 使用测试资源文件内容
        String xmlContent = loadTestResourceAsString("xml-schemas/valid/simple-schema.xml");
        
        File xmlFile = tempDir.resolve("single.xml").toFile();
        try (FileWriter writer = new FileWriter(xmlFile)) {
            writer.write(xmlContent);
        }

        // Mock解析结果
        CsdlSchema mockSchema = new CsdlSchema();
        mockSchema.setNamespace("TestService"); // 与simple-schema.xml中的namespace保持一致
        
        ODataSchemaParser.ParseResult mockParseResult = new ODataSchemaParser.ParseResult(
            mockSchema, new ArrayList<>(), true, null
        );

        when(parser.parseSchema(any(), anyString())).thenReturn(mockParseResult);

        // 执行测试
        ODataXmlLoader.LoadResult result = loader.loadSingleFile(xmlFile.getAbsolutePath());

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalFiles());
        assertEquals(1, result.getSuccessfulFiles());
        assertEquals(0, result.getFailedFiles());
        assertTrue(result.getErrorMessages().isEmpty());

        verify(repository).addSchema(eq(mockSchema), anyString());
    }

    @Test
    void testLoadSingleFile_FileNotFound() {
        // 测试不存在的文件
        ODataXmlLoader.LoadResult result = loader.loadSingleFile("/non/existent/file.xml");

        assertNotNull(result);
        assertEquals(1, result.getTotalFiles());
        assertEquals(0, result.getSuccessfulFiles());
        assertEquals(1, result.getFailedFiles());
        assertFalse(result.getErrorMessages().isEmpty());

        verify(repository, never()).addSchema(any(), anyString());
    }

    @Test
    void testLoadFromInputStream_Success() throws IOException {
        // 使用测试资源文件内容而不是硬编码的XML
        String xmlContent = loadTestResourceAsString("xml-schemas/valid/simple-schema.xml");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes());

        // Mock解析结果
        CsdlSchema mockSchema = new CsdlSchema();
        mockSchema.setNamespace("TestService"); // 与simple-schema.xml中的namespace保持一致
        
        ODataSchemaParser.ParseResult mockParseResult = new ODataSchemaParser.ParseResult(
            mockSchema, new ArrayList<>(), true, null
        );

        when(parser.parseSchema(any(), anyString())).thenReturn(mockParseResult);

        // 执行测试
        ODataXmlLoader.LoadResult result = loader.loadFromInputStream(inputStream, "test-source");

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalFiles());
        assertEquals(1, result.getSuccessfulFiles());
        assertEquals(0, result.getFailedFiles());
        assertTrue(result.getErrorMessages().isEmpty());

        Map<String, ODataXmlLoader.XmlFileInfo> loadedFiles = result.getLoadedFiles();
        assertTrue(loadedFiles.containsKey("test-source"));
        assertEquals("TestService", loadedFiles.get("test-source").getNamespace());

        verify(repository).addSchema(eq(mockSchema), eq("test-source"));
    }

    @Test
    void testLoadFromInputStream_ParseFailure() throws IOException {
        // 使用测试资源中的无效XML内容
        String xmlContent = loadTestResourceAsString("xml-schemas/invalid/malformed-xml.xml");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes());

        // Mock解析失败
        ODataSchemaParser.ParseResult mockParseResult = new ODataSchemaParser.ParseResult(
            null, new ArrayList<>(), false, "Parse error"
        );

        when(parser.parseSchema(any(), anyString())).thenReturn(mockParseResult);

        // 执行测试
        ODataXmlLoader.LoadResult result = loader.loadFromInputStream(inputStream, "test-source");

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalFiles());
        assertEquals(0, result.getSuccessfulFiles());
        assertEquals(1, result.getFailedFiles());
        assertFalse(result.getErrorMessages().isEmpty());
        assertTrue(result.getErrorMessages().get(0).contains("Parse error"));

        verify(repository, never()).addSchema(any(), anyString());
    }

    @Test
    void testLoadFromClasspathDirectory_NotImplemented() {
        // 测试classpath加载（当前为简化实现）
        ODataXmlLoader.LoadResult result = loader.loadFromClasspathDirectory("test/path");

        assertNotNull(result);
        assertEquals(0, result.getTotalFiles());
        assertEquals(0, result.getSuccessfulFiles());
        assertEquals(1, result.getFailedFiles());
        assertFalse(result.getErrorMessages().isEmpty());
        assertTrue(result.getErrorMessages().get(0).contains("not yet implemented"));
    }

    @Test
    void testGetLoadedFiles() {
        // 测试获取已加载文件
        Map<String, ODataXmlLoader.XmlFileInfo> loadedFiles = loader.getLoadedFiles();
        assertNotNull(loadedFiles);
        assertTrue(loadedFiles.isEmpty()); // 初始状态应该为空
    }

    @Test
    void testClear() {
        // 测试清理功能
        loader.clear();
        
        // 验证repository的clear方法被调用
        verify(repository).clear();
        
        // 验证内部loadedFiles被清理
        Map<String, ODataXmlLoader.XmlFileInfo> loadedFiles = loader.getLoadedFiles();
        assertTrue(loadedFiles.isEmpty());
    }

    @Test
    void testLoadFromInputStream_Exception() throws IOException {
        // 模拟parser抛出异常
        when(parser.parseSchema(any(), anyString())).thenThrow(new RuntimeException("Test exception"));

        // 使用测试资源文件内容
        String xmlContent = loadTestResourceAsString("xml-schemas/valid/simple-schema.xml");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes());

        // 执行测试
        ODataXmlLoader.LoadResult result = loader.loadFromInputStream(inputStream, "test-source");

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalFiles());
        assertEquals(0, result.getSuccessfulFiles());
        assertEquals(1, result.getFailedFiles());
        assertFalse(result.getErrorMessages().isEmpty());
        assertTrue(result.getErrorMessages().get(0).contains("Test exception"));

        verify(repository, never()).addSchema(any(), anyString());
    }

    // // ==== 使用测试资源文件的测试方法 ====

    @Test
    void testLoadFromDirectory_WithTestResources_ValidSchemas() {
        // Set mock return
        CsdlSchema mockSchema = new CsdlSchema();
        mockSchema.setNamespace("TestService");
        ODataSchemaParser.ParseResult mockResult = new ODataSchemaParser.ParseResult(mockSchema, new ArrayList<>(), true, null);
        when(parser.parseSchema(any(InputStream.class), anyString())).thenReturn(mockResult);

        // Test valid Schema directory
        String validSchemasPath = "src/test/resources/xml-schemas/valid";
        ODataXmlLoader.LoadResult result = loader.loadFromDirectory(validSchemasPath);

        // Verify results
        assertNotNull(result);
        assertTrue(result.getTotalFiles() > 0);
        assertEquals(result.getTotalFiles(), result.getSuccessfulFiles());
        assertEquals(0, result.getFailedFiles());
        assertTrue(result.getErrorMessages().isEmpty());

        // Verify parser was called
        verify(parser, atLeastOnce()).parseSchema(any(InputStream.class), anyString());
        verify(repository, atLeastOnce()).addSchema(any(CsdlSchema.class), anyString());
    }

    @Test
    void testLoadFromDirectory_WithTestResources_InvalidSchemas() {
        // Set mock to throw exception
        when(parser.parseSchema(any(InputStream.class), anyString()))
            .thenThrow(new RuntimeException("Invalid XML format"));

        // Test invalid Schema directory
        String invalidSchemasPath = "src/test/resources/xml-schemas/invalid";
        ODataXmlLoader.LoadResult result = loader.loadFromDirectory(invalidSchemasPath);

        // Verify results
        assertNotNull(result);
        assertTrue(result.getTotalFiles() > 0);
        assertEquals(0, result.getSuccessfulFiles());
        assertEquals(result.getTotalFiles(), result.getFailedFiles());
        assertFalse(result.getErrorMessages().isEmpty());

        // Verify no Schema was added to repository
        verify(repository, never()).addSchema(any(CsdlSchema.class), anyString());
    }

    @Test
    void testLoadFromDirectory_WithTestResources_MultiFileDirectory() {
        // Set mock to return different Schemas
        CsdlSchema productsSchema = new CsdlSchema();
        productsSchema.setNamespace("Products");
        CsdlSchema salesSchema = new CsdlSchema();
        salesSchema.setNamespace("Sales");
        CsdlSchema inventorySchema = new CsdlSchema();
        inventorySchema.setNamespace("Inventory");

        when(parser.parseSchema(any(InputStream.class), anyString()))
            .thenReturn(new ODataSchemaParser.ParseResult(productsSchema, new ArrayList<>(), true, null))
            .thenReturn(new ODataSchemaParser.ParseResult(salesSchema, new ArrayList<>(), true, null))
            .thenReturn(new ODataSchemaParser.ParseResult(inventorySchema, new ArrayList<>(), true, null));

        // Test multi-file directory
        String multiFilePath = "src/test/resources/xml-schemas/multi-file";
        ODataXmlLoader.LoadResult result = loader.loadFromDirectory(multiFilePath);

        // Verify results
        assertNotNull(result);
        assertEquals(3, result.getTotalFiles()); // products, sales, inventory
        assertEquals(3, result.getSuccessfulFiles());
        assertEquals(0, result.getFailedFiles());
        assertTrue(result.getErrorMessages().isEmpty());

        // Verify all Schemas were added
        verify(parser, times(3)).parseSchema(any(InputStream.class), anyString());
        verify(repository, times(3)).addSchema(any(CsdlSchema.class), anyString());
    }

    @Test
    void testLoadFromDirectory_WithTestResources_EmptyDirectory() {
        // Test empty directory
        String emptyDirPath = "src/test/resources/xml-schemas/empty-directory";
        ODataXmlLoader.LoadResult result = loader.loadFromDirectory(emptyDirPath);

        // Verify results
        assertNotNull(result);
        assertEquals(0, result.getTotalFiles());
        assertEquals(0, result.getSuccessfulFiles());
        assertEquals(0, result.getFailedFiles());
        assertTrue(result.getErrorMessages().isEmpty());

        // Verify parser was not called
        verify(parser, never()).parseSchema(any(InputStream.class), anyString());
        verify(repository, never()).addSchema(any(CsdlSchema.class), anyString());
    }

    @Test
    void testLoadFromDirectory_WithTestResources_LargeSchema() {
        // Set mock to return large Schema
        CsdlSchema largeSchema = new CsdlSchema();
        largeSchema.setNamespace("TestService");
        ODataSchemaParser.ParseResult mockResult = new ODataSchemaParser.ParseResult(largeSchema, new ArrayList<>(), true, null);
        when(parser.parseSchema(any(InputStream.class), anyString())).thenReturn(mockResult);

        // Test large Schema file
        String performancePath = "src/test/resources/xml-schemas/performance";
        long startTime = System.currentTimeMillis();
        
        ODataXmlLoader.LoadResult result = loader.loadFromDirectory(performancePath);
        
        long endTime = System.currentTimeMillis();

        // Verify results
        assertNotNull(result);
        assertEquals(1, result.getTotalFiles());
        assertEquals(1, result.getSuccessfulFiles());
        assertEquals(0, result.getFailedFiles());
        assertTrue(result.getErrorMessages().isEmpty());

        // Verify performance (should complete within reasonable time)
        assertTrue(endTime - startTime < 5000, "Loading took too long: " + (endTime - startTime) + "ms");

        verify(parser, times(1)).parseSchema(any(InputStream.class), anyString());
        verify(repository, times(1)).addSchema(any(CsdlSchema.class), anyString());
    }

    @Test
    void testLoadFromFile_WithTestResources_ComplexSchema() throws IOException {
        // Set mock to return complex Schema
        CsdlSchema complexSchema = new CsdlSchema();
        complexSchema.setNamespace("TestService");
        ODataSchemaParser.ParseResult mockResult = new ODataSchemaParser.ParseResult(complexSchema, new ArrayList<>(), true, null);
        when(parser.parseSchema(any(InputStream.class), anyString())).thenReturn(mockResult);

        // Test complex type Schema file
        Path complexSchemaFile = Paths.get("src/test/resources/xml-schemas/valid/complex-types-schema.xml");
        assertTrue(Files.exists(complexSchemaFile), "Test resource file should exist");

        ODataXmlLoader.LoadResult result = loader.loadSingleFile(complexSchemaFile.toString());

        // Verify results
        assertNotNull(result);
        assertEquals(1, result.getTotalFiles());
        assertEquals(1, result.getSuccessfulFiles());
        assertEquals(0, result.getFailedFiles());
        assertTrue(result.getErrorMessages().isEmpty());

        verify(parser, times(1)).parseSchema(any(InputStream.class), anyString());
        verify(repository, times(1)).addSchema(any(CsdlSchema.class), anyString());
    }

    @Test
    void testLoadFromFile_WithTestResources_MalformedXml() {
        // Set mock to throw parsing exception
        when(parser.parseSchema(any(InputStream.class), anyString()))
            .thenThrow(new RuntimeException("XML parsing failed"));

        // Test malformed XML file
        String malformedXmlFile = "src/test/resources/xml-schemas/invalid/malformed-xml.xml";
        ODataXmlLoader.LoadResult result = loader.loadSingleFile(malformedXmlFile);

        // Verify results
        assertNotNull(result);
        assertEquals(1, result.getTotalFiles());
        assertEquals(0, result.getSuccessfulFiles());
        assertEquals(1, result.getFailedFiles());
        assertFalse(result.getErrorMessages().isEmpty());
        assertTrue(result.getErrorMessages().get(0).contains("XML parsing failed"));

        verify(parser, times(1)).parseSchema(any(InputStream.class), anyString());
        verify(repository, never()).addSchema(any(CsdlSchema.class), anyString());
    }

    // ==== 辅助方法 ====

    private String getTestResourcePath(String relativePath) {
        return "src/test/resources/" + relativePath;
    }

    private boolean isTestResourceExists(String relativePath) {
        Path resourcePath = Paths.get("src/test/resources/" + relativePath);
        return Files.exists(resourcePath);
    }
    
    /**
     * 从测试资源目录加载文件内容为字符串
     */
    private String loadTestResourceAsString(String relativePath) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(relativePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + relativePath);
            }
            // 使用Java 8兼容的方式读取InputStream
            return readInputStreamToString(inputStream);
        }
    }
    
    /**
     * 从测试资源目录获取InputStream
     */
    private InputStream loadTestResourceAsStream(String relativePath) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(relativePath);
        if (inputStream == null) {
            throw new IllegalArgumentException("Resource not found: " + relativePath);
        }
        return inputStream;
    }
    
    /**
     * 读取InputStream内容为字符串 (Java 8兼容)
     */
    private String readInputStreamToString(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            sb.append(new String(buffer, 0, bytesRead, java.nio.charset.StandardCharsets.UTF_8));
        }
        return sb.toString();
    }
}
