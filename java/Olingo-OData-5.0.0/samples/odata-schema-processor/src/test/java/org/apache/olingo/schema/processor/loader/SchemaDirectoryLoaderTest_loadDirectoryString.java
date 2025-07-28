package org.apache.olingo.schema.processor.loader;

import java.nio.file.Paths;

import org.apache.olingo.schema.processor.parser.ODataXmlParser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * 测试 SchemaDirectoryLoader.loadDirectory(String) 方法
 */
@RunWith(MockitoJUnitRunner.class)
public class SchemaDirectoryLoaderTest_loadDirectoryString {

    @Mock
    private ODataXmlParser mockXmlParser;
    
    private SchemaDirectoryLoader loader;
    
    @Before
    public void setUp() {
        loader = new SchemaDirectoryLoader(mockXmlParser);
    }
    
    @Test
    public void testLoadDirectoryStringNonExistentPath() {
        // 执行测试
        SchemaDirectoryLoader.LoadResult result = loader.loadDirectory("non/existent/path");
        
        // 验证结果
        assertNotNull("Result should not be null", result);
        assertFalse("Should fail for non-existent path", result.isSuccess());
        assertFalse("Should have errors", result.getErrors().isEmpty());
        assertTrue("Should contain directory not found error", 
                  result.getErrors().get(0).contains("Directory does not exist"));
        assertEquals("Should have 0 files loaded", 0, result.getFileCount());
        assertEquals("Should have 0 schemas loaded", 0, result.getSchemaCount());
    }
    
    @Test
    public void testLoadDirectoryStringNull() {
        // 执行测试
        String nullPath = null;
        SchemaDirectoryLoader.LoadResult result = loader.loadDirectory(nullPath);
        
        // 验证结果
        assertNotNull("Result should not be null", result);
        assertFalse("Should fail for null input", result.isSuccess());
        assertFalse("Should have errors", result.getErrors().isEmpty());
    }
    
    @Test
    public void testLoadDirectoryStringEmpty() {
        // 执行测试
        SchemaDirectoryLoader.LoadResult result = loader.loadDirectory("");
        
        // 验证结果
        assertNotNull("Result should not be null", result);
        assertFalse("Should fail for empty input", result.isSuccess());
        assertFalse("Should have errors", result.getErrors().isEmpty());
    }
    
    @Test
    public void testLoadDirectoryStringCurrentDirectory() {
        // 执行测试 - 使用当前目录
        SchemaDirectoryLoader.LoadResult result = loader.loadDirectory(".");
        
        // 验证结果
        assertNotNull("Result should not be null", result);
        // 不检查成功状态，因为依赖于实际的文件内容
        assertNotNull("Errors list should not be null", result.getErrors());
        assertNotNull("Warnings list should not be null", result.getWarnings());
        assertNotNull("Files list should not be null", result.getLoadedFiles());
        assertNotNull("Schemas list should not be null", result.getSchemas());
    }
    
    @Test
    public void testLoadDirectoryStringAbsolutePath() {
        // 执行测试 - 使用绝对路径
        String absolutePath = Paths.get("").toAbsolutePath().toString();
        SchemaDirectoryLoader.LoadResult result = loader.loadDirectory(absolutePath);
        
        // 验证结果
        assertNotNull("Result should not be null", result);
        assertNotNull("Errors list should not be null", result.getErrors());
        assertNotNull("Warnings list should not be null", result.getWarnings());
        assertNotNull("Files list should not be null", result.getLoadedFiles());
        assertNotNull("Schemas list should not be null", result.getSchemas());
    }
    
    @Test
    public void testLoadDirectoryStringRelativePath() {
        // 执行测试 - 使用相对路径
        SchemaDirectoryLoader.LoadResult result = loader.loadDirectory("./src");
        
        // 验证结果
        assertNotNull("Result should not be null", result);
        // 这个测试的结果取决于是否存在src目录
        assertNotNull("Errors list should not be null", result.getErrors());
        assertNotNull("Files list should not be null", result.getLoadedFiles());
    }
    
    @Test
    public void testLoadDirectoryStringParentDirectory() {
        // 执行测试 - 使用父目录路径
        SchemaDirectoryLoader.LoadResult result = loader.loadDirectory("..");
        
        // 验证结果
        assertNotNull("Result should not be null", result);
        assertNotNull("Errors list should not be null", result.getErrors());
        assertNotNull("Files list should not be null", result.getLoadedFiles());
    }
    
    @Test
    public void testLoadDirectoryStringWithSpaces() {
        // 执行测试 - 使用包含空格的路径（可能不存在）
        SchemaDirectoryLoader.LoadResult result = loader.loadDirectory("path with spaces");
        
        // 验证结果
        assertNotNull("Result should not be null", result);
        assertFalse("Should fail for non-existent path with spaces", result.isSuccess());
        assertFalse("Should have errors", result.getErrors().isEmpty());
    }
    
    @Test
    public void testLoadDirectoryStringNestedPath() {
        // 执行测试 - 使用嵌套路径
        SchemaDirectoryLoader.LoadResult result = loader.loadDirectory("very/deep/nested/path");
        
        // 验证结果
        assertNotNull("Result should not be null", result);
        assertFalse("Should fail for non-existent nested path", result.isSuccess());
        assertFalse("Should have errors", result.getErrors().isEmpty());
    }
    
    @Test
    public void testLoadDirectoryStringCommonPaths() {
        // 测试一些常见的目录路径
        String[] commonPaths = {
            "src",
            "target",
            "resources",
            "test",
            "main"
        };
        
        for (String path : commonPaths) {
            SchemaDirectoryLoader.LoadResult result = loader.loadDirectory(path);
            
            assertNotNull("Result should not be null for path: " + path, result);
            assertNotNull("Errors list should not be null for path: " + path, result.getErrors());
            assertNotNull("Files list should not be null for path: " + path, result.getLoadedFiles());
        }
    }
}
