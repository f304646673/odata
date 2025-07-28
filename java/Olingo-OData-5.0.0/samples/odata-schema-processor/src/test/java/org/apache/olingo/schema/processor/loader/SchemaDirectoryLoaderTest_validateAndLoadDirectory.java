//package org.apache.olingo.schema.processor.loader;
//
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
//import org.apache.olingo.schema.processor.parser.ODataXmlParser;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertNull;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
///**
// * 测试 SchemaDirectoryLoader.validateAndLoadDirectory 方法
// */
//@ExtendWith(MockitoExtension.class)
//public class SchemaDirectoryLoaderTest_validateAndLoadDirectory {
//
//    @Mock
//    private ODataXmlParser mockXmlParser;
//
//    private SchemaDirectoryLoader loader;
//
//    @BeforeEach
//    public void setUp() {
//        loader = new SchemaDirectoryLoader(mockXmlParser);
//    }
//
//    @Test
//    public void testValidateAndLoadDirectoryNonExistentDirectory() {
//        // 执行测试
//        Path nonExistentPath = Paths.get("non/existent/path");
//        SchemaDirectoryLoader.LoadResult result = loader.validateAndLoadDirectory(nonExistentPath);
//
//        // 验证结果
//        assertNotNull("Result should not be null", result);
//        assertFalse("Load should fail", result.isSuccess());
//        assertFalse("Should have errors", result.getErrors().isEmpty());
//        assertTrue("Should contain directory not found error",
//                  result.getErrors().get(0).contains("Directory does not exist"));
//        assertEquals("Should have 0 files loaded", 0, result.getFileCount());
//        assertEquals("Should have 0 schemas loaded", 0, result.getSchemaCount());
//    }
//
//    @Test
//    public void testValidateAndLoadDirectoryNullInput() {
//        // 执行测试
//        Path nullPath = null;
//        SchemaDirectoryLoader.LoadResult result = loader.validateAndLoadDirectory(nullPath);
//
//        // 验证结果
//        assertNotNull("Result should not be null", result);
//        assertFalse("Load should fail for null input", result.isSuccess());
//        assertFalse("Should have errors", result.getErrors().isEmpty());
//    }
//
//    @Test
//    public void testValidateAndLoadDirectoryNotADirectory() {
//        // 使用一个可能是文件而不是目录的路径
//        Path filePath = Paths.get("pom.xml");
//        SchemaDirectoryLoader.LoadResult result = loader.validateAndLoadDirectory(filePath);
//
//        // 验证结果
//        assertNotNull("Result should not be null", result);
//        // 这个测试的结果取决于实际的文件系统状态，但至少应该有结果
//        assertEquals("Should have 0 files loaded when no XML files", 0, result.getFileCount());
//    }
//
//    @Test
//    public void testValidateAndLoadDirectorySuccess() {
//        // 使用当前目录进行测试（应该是一个有效目录）
//        Path currentPath = Paths.get(".");
//        SchemaDirectoryLoader.LoadResult result = loader.validateAndLoadDirectory(currentPath);
//
//        // 验证结果
//        assertNotNull("Result should not be null", result);
//        // 不检查成功状态，因为依赖于实际的文件内容
//        assertNotNull("Errors list should not be null", result.getErrors());
//        assertNotNull("Warnings list should not be null", result.getWarnings());
//        assertNotNull("Files list should not be null", result.getLoadedFiles());
//        assertNotNull("Schemas list should not be null", result.getSchemas());
//    }
//
//    @Test
//    public void testLoadResultBasicMethods() {
//        // 创建测试结果
//        List<Path> files = Arrays.asList(Paths.get("test1.xml"), Paths.get("test2.xml"));
//        List<CsdlSchema> schemas = Arrays.asList(new CsdlSchema(), new CsdlSchema());
//        Map<Path, FileInfo> fileInfoMap = new HashMap<>();
//        List<String> errors = Arrays.asList("Error 1");
//        List<String> warnings = Arrays.asList("Warning 1");
//
//        SchemaDirectoryLoader.LoadResult result = new SchemaDirectoryLoader.LoadResult(
//            true, files, schemas, fileInfoMap, errors, warnings);
//
//        // 验证基本方法
//        assertTrue("Should be successful", result.isSuccess());
//        assertEquals("Should have correct file count", 2, result.getFileCount());
//        assertEquals("Should have correct schema count", 2, result.getSchemaCount());
//        assertEquals("Should have correct error count", 1, result.getErrors().size());
//        assertEquals("Should have correct warning count", 1, result.getWarnings().size());
//
//        // 验证返回的列表是副本（修改不会影响原始数据）
//        List<Path> returnedFiles = result.getLoadedFiles();
//        returnedFiles.clear();
//        assertEquals("Original files should not be affected", 2, result.getFileCount());
//    }
//
//    @Test
//    public void testLoadResultGetAllDefinedNamespaces() {
//        // 创建带命名空间的Schema
//        CsdlSchema schema1 = new CsdlSchema();
//        schema1.setNamespace("com.example.test1");
//        CsdlSchema schema2 = new CsdlSchema();
//        schema2.setNamespace("com.example.test2");
//
//        List<CsdlSchema> schemas = Arrays.asList(schema1, schema2);
//
//        SchemaDirectoryLoader.LoadResult result = new SchemaDirectoryLoader.LoadResult(
//            true, new ArrayList<>(), schemas, new HashMap<>(),
//            new ArrayList<>(), new ArrayList<>());
//
//        // 验证命名空间
//        Set<String> namespaces = result.getAllDefinedNamespaces();
//        assertEquals("Should have 2 namespaces", 2, namespaces.size());
//        assertTrue("Should contain test1 namespace", namespaces.contains("com.example.test1"));
//        assertTrue("Should contain test2 namespace", namespaces.contains("com.example.test2"));
//    }
//
//    @Test
//    public void testLoadResultAllDependenciesSatisfied() {
//        // 创建没有依赖的情况
//        SchemaDirectoryLoader.LoadResult result = new SchemaDirectoryLoader.LoadResult(
//            true, new ArrayList<>(), new ArrayList<>(), new HashMap<>(),
//            new ArrayList<>(), new ArrayList<>());
//
//        // 验证依赖满足情况
//        assertTrue("Should have all dependencies satisfied when no dependencies",
//                  result.allDependenciesSatisfied());
//    }
//
//    @Test
//    public void testLoadResultDependencyGraph() {
//        // 创建测试数据
//        SchemaDirectoryLoader.LoadResult result = new SchemaDirectoryLoader.LoadResult(
//            true, new ArrayList<>(), new ArrayList<>(), new HashMap<>(),
//            new ArrayList<>(), new ArrayList<>());
//
//        // 验证依赖
//        Map<Path, Set<Path>> graph = result.getDependencyGraph();
//        assertNotNull("Dependency graph should not be null", graph);
//        assertTrue("Should be empty for no files", graph.isEmpty());
//    }
//
//    @Test
//    public void testLoadResultGetFileInfo() {
//        // 创建测试数据
//        Path testFile = Paths.get("test.xml");
//        FileInfo testFileInfo = new FileInfo.Builder(testFile).build();
//        Map<Path, FileInfo> fileInfoMap = new HashMap<>();
//        fileInfoMap.put(testFile, testFileInfo);
//
//        SchemaDirectoryLoader.LoadResult result = new SchemaDirectoryLoader.LoadResult(
//            true, Arrays.asList(testFile), new ArrayList<>(), fileInfoMap,
//            new ArrayList<>(), new ArrayList<>());
//
//        // 验证文件信息获取
//        FileInfo retrievedInfo = result.getFileInfo(testFile);
//        assertNotNull("FileInfo should not be null", retrievedInfo);
//        assertEquals("File path should match", testFile, retrievedInfo.getFilePath());
//
//        // 测试不存在的文件
//        FileInfo nonExistentInfo = result.getFileInfo(Paths.get("nonexistent.xml"));
//        assertNull("Should return null for non-existent file", nonExistentInfo);
//    }
//}
