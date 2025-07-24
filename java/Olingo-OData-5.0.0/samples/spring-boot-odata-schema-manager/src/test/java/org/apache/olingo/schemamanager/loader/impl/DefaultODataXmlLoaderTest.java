package org.apache.olingo.schemamanager.loader.impl;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.loader.ODataXmlLoader;
import org.apache.olingo.schemamanager.loader.impl.DefaultODataXmlLoader;
import org.apache.olingo.schemamanager.parser.ODataSchemaParser;
import org.apache.olingo.schemamanager.parser.impl.OlingoSchemaParserImpl;
import org.apache.olingo.schemamanager.repository.SchemaRepository;
import org.apache.olingo.schemamanager.repository.impl.InMemorySchemaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    // @Test
    // void testLoadFromDirectory_Success() throws IOException {
    //     // 创建测试XML文件
    //     File xmlFile = tempDir.resolve("test.xml").toFile();
    //     try (FileWriter writer = new FileWriter(xmlFile)) {
    //         writer.write("<?xml version=\"1.0\"?><edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\"/>");
    //     }

    //     // Mock解析结果
    //     CsdlSchema mockSchema = new CsdlSchema();
    //     mockSchema.setNamespace("TestNamespace");
        
    //     ODataSchemaParser.ParseResult mockParseResult = new ODataSchemaParser.ParseResult(
    //         mockSchema, new ArrayList<>(), true, null
    //     );

    //     when(parser.parseSchema(any(), anyString())).thenReturn(mockParseResult);

    //     // 执行测试
    //     ODataXmlLoader.LoadResult result = loader.loadFromDirectory(tempDir.toString());

    //     // 验证结果
    //     assertNotNull(result);
    //     assertEquals(1, result.getTotalFiles());
    //     assertEquals(1, result.getSuccessfulFiles());
    //     assertEquals(0, result.getFailedFiles());
    //     assertTrue(result.getErrorMessages().isEmpty());

    //     // 验证repository被调用
    //     verify(repository).addSchema(eq(mockSchema), anyString());
    // }

    // @Test
    // void testLoadFromDirectory_NonExistentDirectory() {
    //     // 测试不存在的目录
    //     ODataXmlLoader.LoadResult result = loader.loadFromDirectory("/non/existent/directory");

    //     assertNotNull(result);
    //     assertEquals(0, result.getTotalFiles());
    //     assertEquals(0, result.getSuccessfulFiles());
    //     assertEquals(1, result.getFailedFiles());
    //     assertFalse(result.getErrorMessages().isEmpty());
    //     assertTrue(result.getErrorMessages().get(0).contains("Directory not found"));
    // }

    // @Test
    // void testLoadFromDirectory_ParseFailure() throws IOException {
    //     // 创建测试XML文件
    //     File xmlFile = tempDir.resolve("invalid.xml").toFile();
    //     try (FileWriter writer = new FileWriter(xmlFile)) {
    //         writer.write("invalid xml content");
    //     }

    //     // Mock解析失败
    //     ODataSchemaParser.ParseResult mockParseResult = new ODataSchemaParser.ParseResult(
    //         null, new ArrayList<>(), false, "Parse error"
    //     );

    //     when(parser.parseSchema(any(), anyString())).thenReturn(mockParseResult);

    //     // 执行测试
    //     ODataXmlLoader.LoadResult result = loader.loadFromDirectory(tempDir.toString());

    //     // 验证结果
    //     assertNotNull(result);
    //     assertEquals(1, result.getTotalFiles());
    //     assertEquals(0, result.getSuccessfulFiles());
    //     assertEquals(1, result.getFailedFiles());
    //     assertFalse(result.getErrorMessages().isEmpty());

    //     // 验证repository没有被调用
    //     verify(repository, never()).addSchema(any(), anyString());
    // }

    // @Test
    // void testLoadFromDirectory_EmptyDirectory() {
    //     // 测试空目录
    //     ODataXmlLoader.LoadResult result = loader.loadFromDirectory(tempDir.toString());

    //     assertNotNull(result);
    //     assertEquals(0, result.getTotalFiles());
    //     assertEquals(0, result.getSuccessfulFiles());
    //     assertEquals(0, result.getFailedFiles());
    //     assertTrue(result.getErrorMessages().isEmpty());
    // }

    // @Test
    // void testLoadFromDirectory_RecursiveLoading() throws IOException {
    //     // 创建子目录和XML文件
    //     File subDir = tempDir.resolve("subdir").toFile();
    //     subDir.mkdir();
        
    //     File xmlFile1 = tempDir.resolve("test1.xml").toFile();
    //     File xmlFile2 = subDir.toPath().resolve("test2.xml").toFile();
        
    //     try (FileWriter writer = new FileWriter(xmlFile1)) {
    //         writer.write("<?xml version=\"1.0\"?><edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\"/>");
    //     }
    //     try (FileWriter writer = new FileWriter(xmlFile2)) {
    //         writer.write("<?xml version=\"1.0\"?><edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\"/>");
    //     }

    //     // Mock解析结果
    //     CsdlSchema mockSchema = new CsdlSchema();
    //     mockSchema.setNamespace("TestNamespace");
        
    //     ODataSchemaParser.ParseResult mockParseResult = new ODataSchemaParser.ParseResult(
    //         mockSchema, new ArrayList<>(), true, null
    //     );

    //     when(parser.parseSchema(any(), anyString())).thenReturn(mockParseResult);

    //     // 执行测试
    //     ODataXmlLoader.LoadResult result = loader.loadFromDirectory(tempDir.toString());

    //     // 验证结果 - 应该找到两个XML文件
    //     assertNotNull(result);
    //     assertEquals(2, result.getTotalFiles());
    //     assertEquals(2, result.getSuccessfulFiles());
    //     assertEquals(0, result.getFailedFiles());

    //     // 验证repository被调用两次
    //     verify(repository, times(2)).addSchema(eq(mockSchema), anyString());
    // }

    // @Test
    // void testLoadSingleFile_Success() throws IOException {
    //     File xmlFile = tempDir.resolve("single.xml").toFile();
    //     try (FileWriter writer = new FileWriter(xmlFile)) {
    //         writer.write("<?xml version=\"1.0\"?><edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\"/>");
    //     }

    //     // Mock解析结果
    //     CsdlSchema mockSchema = new CsdlSchema();
    //     mockSchema.setNamespace("TestNamespace");
        
    //     ODataSchemaParser.ParseResult mockParseResult = new ODataSchemaParser.ParseResult(
    //         mockSchema, new ArrayList<>(), true, null
    //     );

    //     when(parser.parseSchema(any(), anyString())).thenReturn(mockParseResult);

    //     // 执行测试
    //     ODataXmlLoader.LoadResult result = loader.loadSingleFile(xmlFile.getAbsolutePath());

    //     // 验证结果
    //     assertNotNull(result);
    //     assertEquals(1, result.getTotalFiles());
    //     assertEquals(1, result.getSuccessfulFiles());
    //     assertEquals(0, result.getFailedFiles());
    //     assertTrue(result.getErrorMessages().isEmpty());

    //     verify(repository).addSchema(eq(mockSchema), anyString());
    // }

    // @Test
    // void testLoadSingleFile_FileNotFound() {
    //     // 测试不存在的文件
    //     ODataXmlLoader.LoadResult result = loader.loadSingleFile("/non/existent/file.xml");

    //     assertNotNull(result);
    //     assertEquals(1, result.getTotalFiles());
    //     assertEquals(0, result.getSuccessfulFiles());
    //     assertEquals(1, result.getFailedFiles());
    //     assertFalse(result.getErrorMessages().isEmpty());

    //     verify(repository, never()).addSchema(any(), anyString());
    // }

    // @Test
    // void testLoadFromInputStream_Success() {
    //     String xmlContent = "<?xml version=\"1.0\"?><edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\"/>";
    //     ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes());

    //     // Mock解析结果
    //     CsdlSchema mockSchema = new CsdlSchema();
    //     mockSchema.setNamespace("TestNamespace");
        
    //     ODataSchemaParser.ParseResult mockParseResult = new ODataSchemaParser.ParseResult(
    //         mockSchema, new ArrayList<>(), true, null
    //     );

    //     when(parser.parseSchema(any(), anyString())).thenReturn(mockParseResult);

    //     // 执行测试
    //     ODataXmlLoader.LoadResult result = loader.loadFromInputStream(inputStream, "test-source");

    //     // 验证结果
    //     assertNotNull(result);
    //     assertEquals(1, result.getTotalFiles());
    //     assertEquals(1, result.getSuccessfulFiles());
    //     assertEquals(0, result.getFailedFiles());
    //     assertTrue(result.getErrorMessages().isEmpty());

    //     Map<String, ODataXmlLoader.XmlFileInfo> loadedFiles = result.getLoadedFiles();
    //     assertTrue(loadedFiles.containsKey("test-source"));
    //     assertEquals("TestNamespace", loadedFiles.get("test-source").getNamespace());

    //     verify(repository).addSchema(eq(mockSchema), eq("test-source"));
    // }

    // @Test
    // void testLoadFromInputStream_ParseFailure() {
    //     String xmlContent = "invalid xml";
    //     ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes());

    //     // Mock解析失败
    //     ODataSchemaParser.ParseResult mockParseResult = new ODataSchemaParser.ParseResult(
    //         null, new ArrayList<>(), false, "Parse error"
    //     );

    //     when(parser.parseSchema(any(), anyString())).thenReturn(mockParseResult);

    //     // 执行测试
    //     ODataXmlLoader.LoadResult result = loader.loadFromInputStream(inputStream, "test-source");

    //     // 验证结果
    //     assertNotNull(result);
    //     assertEquals(1, result.getTotalFiles());
    //     assertEquals(0, result.getSuccessfulFiles());
    //     assertEquals(1, result.getFailedFiles());
    //     assertFalse(result.getErrorMessages().isEmpty());
    //     assertTrue(result.getErrorMessages().get(0).contains("Parse error"));

    //     verify(repository, never()).addSchema(any(), anyString());
    // }

    // @Test
    // void testLoadFromClasspathDirectory_NotImplemented() {
    //     // 测试classpath加载（当前为简化实现）
    //     ODataXmlLoader.LoadResult result = loader.loadFromClasspathDirectory("test/path");

    //     assertNotNull(result);
    //     assertEquals(0, result.getTotalFiles());
    //     assertEquals(0, result.getSuccessfulFiles());
    //     assertEquals(1, result.getFailedFiles());
    //     assertFalse(result.getErrorMessages().isEmpty());
    //     assertTrue(result.getErrorMessages().get(0).contains("not yet implemented"));
    // }

    // @Test
    // void testGetLoadedFiles() {
    //     // 测试获取已加载文件
    //     Map<String, ODataXmlLoader.XmlFileInfo> loadedFiles = loader.getLoadedFiles();
    //     assertNotNull(loadedFiles);
    //     assertTrue(loadedFiles.isEmpty()); // 初始状态应该为空
    // }

    // @Test
    // void testClear() {
    //     // 测试清理功能
    //     loader.clear();
        
    //     // 验证repository的clear方法被调用
    //     verify(repository).clear();
        
    //     // 验证内部loadedFiles被清理
    //     Map<String, ODataXmlLoader.XmlFileInfo> loadedFiles = loader.getLoadedFiles();
    //     assertTrue(loadedFiles.isEmpty());
    // }

    // @Test
    // void testLoadFromInputStream_Exception() {
    //     // 模拟parser抛出异常
    //     when(parser.parseSchema(any(), anyString())).thenThrow(new RuntimeException("Test exception"));

    //     String xmlContent = "<?xml version=\"1.0\"?><edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\"/>";
    //     ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes());

    //     // 执行测试
    //     ODataXmlLoader.LoadResult result = loader.loadFromInputStream(inputStream, "test-source");

    //     // 验证结果
    //     assertNotNull(result);
    //     assertEquals(1, result.getTotalFiles());
    //     assertEquals(0, result.getSuccessfulFiles());
    //     assertEquals(1, result.getFailedFiles());
    //     assertFalse(result.getErrorMessages().isEmpty());
    //     assertTrue(result.getErrorMessages().get(0).contains("Test exception"));

    //     verify(repository, never()).addSchema(any(), anyString());
    // }

    // // ==== 使用测试资源文件的测试方法 ====

    // @Test
    // void testLoadFromDirectory_WithTestResources_ValidSchemas() {
    //     // 设置mock返回
    //     CsdlSchema mockSchema = new CsdlSchema();
    //     mockSchema.setNamespace("TestService");
    //     ODataSchemaParser.ParseResult mockResult = new ODataSchemaParser.ParseResult(mockSchema, new ArrayList<>());
    //     when(parser.parseSchema(any(ByteArrayInputStream.class))).thenReturn(mockResult);

    //     // 测试有效的Schema目录
    //     String validSchemasPath = "src/test/resources/xml-schemas/valid";
    //     ODataXmlLoader.LoadResult result = loader.loadFromDirectory(validSchemasPath);

    //     // 验证结果
    //     assertNotNull(result);
    //     assertTrue(result.getTotalFiles() > 0);
    //     assertEquals(result.getTotalFiles(), result.getSuccessfulFiles());
    //     assertEquals(0, result.getFailedFiles());
    //     assertTrue(result.getErrorMessages().isEmpty());

    //     // 验证解析器被调用
    //     verify(parser, atLeastOnce()).parseSchema(any(ByteArrayInputStream.class));
    //     verify(repository, atLeastOnce()).addSchema(any(CsdlSchema.class), anyString());
    // }

    // @Test
    // void testLoadFromDirectory_WithTestResources_InvalidSchemas() {
    //     // 设置mock抛出异常
    //     when(parser.parseSchema(any(ByteArrayInputStream.class)))
    //         .thenThrow(new RuntimeException("Invalid XML format"));

    //     // 测试无效的Schema目录
    //     String invalidSchemasPath = "src/test/resources/xml-schemas/invalid";
    //     ODataXmlLoader.LoadResult result = loader.loadFromDirectory(invalidSchemasPath);

    //     // 验证结果
    //     assertNotNull(result);
    //     assertTrue(result.getTotalFiles() > 0);
    //     assertEquals(0, result.getSuccessfulFiles());
    //     assertEquals(result.getTotalFiles(), result.getFailedFiles());
    //     assertFalse(result.getErrorMessages().isEmpty());

    //     // 验证没有Schema被添加到repository
    //     verify(repository, never()).addSchema(any(CsdlSchema.class), anyString());
    // }

    // @Test
    // void testLoadFromDirectory_WithTestResources_MultiFileDirectory() {
    //     // 设置mock返回不同的Schema
    //     CsdlSchema productsSchema = new CsdlSchema();
    //     productsSchema.setNamespace("Products");
    //     CsdlSchema salesSchema = new CsdlSchema();
    //     salesSchema.setNamespace("Sales");
    //     CsdlSchema inventorySchema = new CsdlSchema();
    //     inventorySchema.setNamespace("Inventory");

    //     when(parser.parseSchema(any(ByteArrayInputStream.class)))
    //         .thenReturn(new ODataSchemaParser.ParseResult(productsSchema, new ArrayList<>()))
    //         .thenReturn(new ODataSchemaParser.ParseResult(salesSchema, new ArrayList<>()))
    //         .thenReturn(new ODataSchemaParser.ParseResult(inventorySchema, new ArrayList<>()));

    //     // 测试多文件目录
    //     String multiFilePath = "src/test/resources/xml-schemas/multi-file";
    //     ODataXmlLoader.LoadResult result = loader.loadFromDirectory(multiFilePath);

    //     // 验证结果
    //     assertNotNull(result);
    //     assertEquals(3, result.getTotalFiles()); // products, sales, inventory
    //     assertEquals(3, result.getSuccessfulFiles());
    //     assertEquals(0, result.getFailedFiles());
    //     assertTrue(result.getErrorMessages().isEmpty());

    //     // 验证所有Schema都被添加
    //     verify(parser, times(3)).parseSchema(any(ByteArrayInputStream.class));
    //     verify(repository, times(3)).addSchema(any(CsdlSchema.class), anyString());
    // }

    // @Test
    // void testLoadFromDirectory_WithTestResources_EmptyDirectory() {
    //     // 测试空目录
    //     String emptyDirPath = "src/test/resources/xml-schemas/empty-directory";
    //     ODataXmlLoader.LoadResult result = loader.loadFromDirectory(emptyDirPath);

    //     // 验证结果
    //     assertNotNull(result);
    //     assertEquals(0, result.getTotalFiles());
    //     assertEquals(0, result.getSuccessfulFiles());
    //     assertEquals(0, result.getFailedFiles());
    //     assertTrue(result.getErrorMessages().isEmpty());

    //     // 验证没有调用解析器
    //     verify(parser, never()).parseSchema(any(ByteArrayInputStream.class));
    //     verify(repository, never()).addSchema(any(CsdlSchema.class), anyString());
    // }

    // @Test
    // void testLoadFromDirectory_WithTestResources_LargeSchema() {
    //     // 设置mock返回大型Schema
    //     CsdlSchema largeSchema = new CsdlSchema();
    //     largeSchema.setNamespace("TestService");
    //     ODataSchemaParser.ParseResult mockResult = new ODataSchemaParser.ParseResult(largeSchema, new ArrayList<>());
    //     when(parser.parseSchema(any(ByteArrayInputStream.class))).thenReturn(mockResult);

    //     // 测试大型Schema文件
    //     String performancePath = "src/test/resources/xml-schemas/performance";
    //     long startTime = System.currentTimeMillis();
        
    //     ODataXmlLoader.LoadResult result = loader.loadFromDirectory(performancePath);
        
    //     long endTime = System.currentTimeMillis();

    //     // 验证结果
    //     assertNotNull(result);
    //     assertEquals(1, result.getTotalFiles());
    //     assertEquals(1, result.getSuccessfulFiles());
    //     assertEquals(0, result.getFailedFiles());
    //     assertTrue(result.getErrorMessages().isEmpty());

    //     // 验证性能（应该在合理时间内完成）
    //     assertTrue(endTime - startTime < 5000, "Loading took too long: " + (endTime - startTime) + "ms");

    //     verify(parser, times(1)).parseSchema(any(ByteArrayInputStream.class));
    //     verify(repository, times(1)).addSchema(any(CsdlSchema.class), anyString());
    // }

    // @Test
    // void testLoadFromFile_WithTestResources_ComplexSchema() throws IOException {
    //     // 设置mock返回复杂Schema
    //     CsdlSchema complexSchema = new CsdlSchema();
    //     complexSchema.setNamespace("TestService");
    //     ODataSchemaParser.ParseResult mockResult = new ODataSchemaParser.ParseResult(complexSchema, new ArrayList<>());
    //     when(parser.parseSchema(any(ByteArrayInputStream.class))).thenReturn(mockResult);

    //     // 测试复杂类型Schema文件
    //     Path complexSchemaFile = Paths.get("src/test/resources/xml-schemas/valid/complex-types-schema.xml");
    //     assertTrue(Files.exists(complexSchemaFile), "Test resource file should exist");

    //     ODataXmlLoader.LoadResult result = loader.loadFromFile(complexSchemaFile.toString());

    //     // 验证结果
    //     assertNotNull(result);
    //     assertEquals(1, result.getTotalFiles());
    //     assertEquals(1, result.getSuccessfulFiles());
    //     assertEquals(0, result.getFailedFiles());
    //     assertTrue(result.getErrorMessages().isEmpty());

    //     verify(parser, times(1)).parseSchema(any(ByteArrayInputStream.class));
    //     verify(repository, times(1)).addSchema(any(CsdlSchema.class), anyString());
    // }

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
}
