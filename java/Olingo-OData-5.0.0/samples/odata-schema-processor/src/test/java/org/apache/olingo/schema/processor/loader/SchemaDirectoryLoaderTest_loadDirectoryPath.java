//package org.apache.olingo.schema.processor.loader;
//
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//import org.apache.olingo.schema.processor.parser.ODataXmlParser;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
///**
// * 测试 SchemaDirectoryLoader.loadDirectory(Path) 方法（已弃用
// */
//@ExtendWith(MockitoExtension.class)
//public class SchemaDirectoryLoaderTest_loadDirectoryPath {
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
//    public void testLoadDirectoryPathNonExistent() {
//        // 执行测试
//        Path nonExistentPath = Paths.get("non/existent/path");
//        SchemaDirectoryLoader.LoadResult result = loader.loadDirectory(nonExistentPath);
//
//        // 验证结果
//        assertNotNull("Result should not be null", result);
//        assertFalse("Should fail for non-existent path", result.isSuccess());
//        assertFalse("Should have errors", result.getErrors().isEmpty());
//        assertTrue("Should contain directory not found error",
//                  result.getErrors().get(0).contains("Directory does not exist"));
//        assertEquals("Should have 0 files loaded", 0, result.getFileCount());
//        assertEquals("Should have 0 schemas loaded", 0, result.getSchemaCount());
//    }
//
//    @Test
//    public void testLoadDirectoryPathNull() {
//        // 执行测试
//        Path nullPath = null;
//        SchemaDirectoryLoader.LoadResult result = loader.loadDirectory(nullPath);
//
//        // 验证结果
//        assertNotNull("Result should not be null", result);
//        assertFalse("Should fail for null input", result.isSuccess());
//        assertFalse("Should have errors", result.getErrors().isEmpty());
//    }
//
//    @Test
//    public void testLoadDirectoryPathCurrentDirectory() {
//        // 执行测试 - 使用当前目录
//        Path currentPath = Paths.get(".");
//        SchemaDirectoryLoader.LoadResult result = loader.loadDirectory(currentPath);
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
//    public void testLoadDirectoryPathAbsolute() {
//        // 执行测试 - 使用绝对路径
//        Path absolutePath = Paths.get("").toAbsolutePath();
//        SchemaDirectoryLoader.LoadResult result = loader.loadDirectory(absolutePath);
//
//        // 验证结果
//        assertNotNull("Result should not be null", result);
//        assertNotNull("Errors list should not be null", result.getErrors());
//        assertNotNull("Warnings list should not be null", result.getWarnings());
//        assertNotNull("Files list should not be null", result.getLoadedFiles());
//        assertNotNull("Schemas list should not be null", result.getSchemas());
//    }
//
//    @Test
//    public void testLoadDirectoryPathRelative() {
//        // 执行测试 - 使用相对路径
//        Path relativePath = Paths.get("./src");
//        SchemaDirectoryLoader.LoadResult result = loader.loadDirectory(relativePath);
//
//        // 验证结果
//        assertNotNull("Result should not be null", result);
//        // 结果取决于src目录是否存在
//        assertNotNull("Errors list should not be null", result.getErrors());
//        assertNotNull("Files list should not be null", result.getLoadedFiles());
//    }
//
//    @Test
//    public void testLoadDirectoryPathParent() {
//        // 执行测试 - 使用父目
//        Path parentPath = Paths.get("..");
//        SchemaDirectoryLoader.LoadResult result = loader.loadDirectory(parentPath);
//
//        // 验证结果
//        assertNotNull("Result should not be null", result);
//        assertNotNull("Errors list should not be null", result.getErrors());
//        assertNotNull("Files list should not be null", result.getLoadedFiles());
//    }
//
//    @Test
//    public void testLoadDirectoryPathWithSpecialCharacters() {
//        // 执行测试 - 使用包含特殊字符的路径
//        Path specialPath = Paths.get("test-dir_123");
//        SchemaDirectoryLoader.LoadResult result = loader.loadDirectory(specialPath);
//
//        // 验证结果
//        assertNotNull("Result should not be null", result);
//        // 大多数情况下应该失败，因为目录不存在
//        assertFalse("Should fail for non-existent special path", result.isSuccess());
//        assertFalse("Should have errors", result.getErrors().isEmpty());
//    }
//
//    @Test
//    public void testLoadDirectoryPathNested() {
//        // 执行测试 - 使用深层嵌套路径
//        Path nestedPath = Paths.get("very", "deep", "nested", "path");
//        SchemaDirectoryLoader.LoadResult result = loader.loadDirectory(nestedPath);
//
//        // 验证结果
//        assertNotNull("Result should not be null", result);
//        assertFalse("Should fail for non-existent nested path", result.isSuccess());
//        assertFalse("Should have errors", result.getErrors().isEmpty());
//    }
//
//    @Test
//    public void testLoadDirectoryPathCommonDirectories() {
//        // 测试一些常见的目录
//        Path[] commonPaths = {
//            Paths.get("src"),
//            Paths.get("target"),
//            Paths.get("test"),
//            Paths.get("main"),
//            Paths.get("resources")
//        };
//
//        for (Path path : commonPaths) {
//            SchemaDirectoryLoader.LoadResult result = loader.loadDirectory(path);
//
//            assertNotNull("Result should not be null for path: " + path, result);
//            assertNotNull("Errors list should not be null for path: " + path, result.getErrors());
//            assertNotNull("Files list should not be null for path: " + path, result.getLoadedFiles());
//        }
//    }
//
//    @Test
//    public void testLoadDirectoryPathFile() {
//        // 执行测试 - 使用文件路径而不是目录路径
//        Path filePath = Paths.get("pom.xml");
//        SchemaDirectoryLoader.LoadResult result = loader.loadDirectory(filePath);
//
//        // 验证结果
//        assertNotNull("Result should not be null", result);
//        // 如果文件存在但不是目录，应该失败或者至少没有文件被加载
//        assertEquals("Should have 0 files loaded when pointing to file", 0, result.getFileCount());
//    }
//}
