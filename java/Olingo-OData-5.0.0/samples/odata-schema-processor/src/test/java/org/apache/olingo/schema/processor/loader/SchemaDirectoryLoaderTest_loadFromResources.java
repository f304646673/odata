//package org.apache.olingo.schema.processor.loader;
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
// * 测试 SchemaDirectoryLoader.loadFromResources 方法
// */
//@ExtendWith(MockitoExtension.class)
//public class SchemaDirectoryLoaderTest_loadFromResources {
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
//    public void testLoadFromResourcesNonExistentPath() {
//        // 执行测试
//        SchemaDirectoryLoader.LoadResult result = loader.loadFromResources("non/existent/path");
//
//        // 验证结果
//        assertNotNull("Result should not be null", result);
//        assertFalse("Should fail for non-existent resource", result.isSuccess());
//        assertFalse("Should have errors", result.getErrors().isEmpty());
//        assertTrue("Should contain resource not found error",
//                  result.getErrors().get(0).contains("Resource directory not found"));
//        assertEquals("Should have 0 files loaded", 0, result.getFileCount());
//        assertEquals("Should have 0 schemas loaded", 0, result.getSchemaCount());
//    }
//
//    @Test
//    public void testLoadFromResourcesNull() {
//        // 执行测试
//        SchemaDirectoryLoader.LoadResult result = loader.loadFromResources(null);
//
//        // 验证结果
//        assertNotNull("Result should not be null", result);
//        assertFalse("Should fail for null input", result.isSuccess());
//        assertFalse("Should have errors", result.getErrors().isEmpty());
//    }
//
//    @Test
//    public void testLoadFromResourcesEmpty() {
//        // 执行测试
//        SchemaDirectoryLoader.LoadResult result = loader.loadFromResources("");
//
//        // 验证结果
//        assertNotNull("Result should not be null", result);
//        assertFalse("Should fail for empty input", result.isSuccess());
//        assertFalse("Should have errors", result.getErrors().isEmpty());
//    }
//
//    @Test
//    public void testLoadFromResourcesWhitespace() {
//        // 执行测试
//        SchemaDirectoryLoader.LoadResult result = loader.loadFromResources("   ");
//
//        // 验证结果
//        assertNotNull("Result should not be null", result);
//        assertFalse("Should fail for whitespace input", result.isSuccess());
//        assertFalse("Should have errors", result.getErrors().isEmpty());
//    }
//
//    @Test
//    public void testLoadFromResourcesValidButNonExistent() {
//        // 执行测试
//        SchemaDirectoryLoader.LoadResult result = loader.loadFromResources("test-schemas");
//
//        // 验证结果
//        assertNotNull("Result should not be null", result);
//        assertFalse("Should fail for non-existent resource", result.isSuccess());
//        assertFalse("Should have errors", result.getErrors().isEmpty());
//        assertTrue("Should contain resource directory not found error",
//                  result.getErrors().stream().anyMatch(e -> e.contains("Resource directory not found")));
//    }
//
//    @Test
//    public void testLoadFromResourcesNestedPath() {
//        // 执行测试
//        SchemaDirectoryLoader.LoadResult result = loader.loadFromResources("schemas/test/nested");
//
//        // 验证结果
//        assertNotNull("Result should not be null", result);
//        assertFalse("Should fail for non-existent nested resource", result.isSuccess());
//        assertFalse("Should have errors", result.getErrors().isEmpty());
//    }
//
//    @Test
//    public void testLoadFromResourcesCurrentDirectory() {
//        // 尝试加载当前目录作为资源（可能存在）
//        SchemaDirectoryLoader.LoadResult result = loader.loadFromResources(".");
//
//        // 验证结果
//        assertNotNull("Result should not be null", result);
//        // 不检查成功状态，因为这依赖于实际的文件系统
//        assertNotNull("Errors list should not be null", result.getErrors());
//        assertNotNull("Warnings list should not be null", result.getWarnings());
//        assertNotNull("Files list should not be null", result.getLoadedFiles());
//        assertNotNull("Schemas list should not be null", result.getSchemas());
//    }
//
//    @Test
//    public void testLoadFromResourcesCommonResourcePaths() {
//        // 测试一些常见的资源路径
//        String[] commonPaths = {
//            "META-INF",
//            "schemas",
//            "xml",
//            "resources",
//            "config"
//        };
//
//        for (String path : commonPaths) {
//            SchemaDirectoryLoader.LoadResult result = loader.loadFromResources(path);
//
//            assertNotNull("Result should not be null for path: " + path, result);
//            assertNotNull("Errors list should not be null for path: " + path, result.getErrors());
//            assertNotNull("Warnings list should not be null for path: " + path, result.getWarnings());
//        }
//    }
//
//    @Test
//    public void testLoadFromResourcesSpecialCharacters() {
//        // 测试包含特殊字符的路径
//        String[] specialPaths = {
//            "test-schemas",
//            "test_schemas",
//            "schemas/sub-dir",
//            "schemas.test"
//        };
//
//        for (String path : specialPaths) {
//            SchemaDirectoryLoader.LoadResult result = loader.loadFromResources(path);
//
//            assertNotNull("Result should not be null for special path: " + path, result);
//            // 大多数应该失败，但不应该抛出异常
//            assertNotNull("Errors list should not be null for special path: " + path, result.getErrors());
//        }
//    }
//}
