package org.apache.olingo.schema.validation.engine;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ODataSchemaValidationEngineTest {

    @TempDir
    Path tempDir;

    private ODataSchemaValidationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new ODataSchemaValidationEngine();
    }

    @Test
    void testConstructorWithValidParameters() {
        assertDoesNotThrow(() -> new ODataSchemaValidationEngine());
    }

    @Test
    void testProcessDirectoryWithNullPath() {
        assertThrows(IllegalArgumentException.class, () -> engine.processDirectory(null));
    }

    @Test
    void testProcessDirectoryWithNonExistentPath() throws IOException {
        Path nonExistentPath = tempDir.resolve("nonexistent");
        assertThrows(IllegalArgumentException.class, () -> engine.processDirectory(nonExistentPath));
    }

    @Test
    void testProcessDirectoryWithXmlFiles() throws Exception {
        // 创建包含XML文件的目录
        Path testDir = tempDir.resolve("xmltest");
        Files.createDirectory(testDir);
        
        // 创建简单的XML文件
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                           "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n" +
                           "  <edmx:DataServices>\n" +
                           "    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"Test\">\n" +
                           "    </Schema>\n" +
                           "  </edmx:DataServices>\n" +
                           "</edmx:Edmx>";
        
        Files.write(testDir.resolve("test.xml"), xmlContent.getBytes());
        
        // 执行测试
        IntegrationResult result = engine.processDirectory(testDir);
        
        // 验证结果
        assertNotNull(result);
        // 由于我们使用真实的DirectoryValidationManager，结果可能因实现而异
        // 这里主要测试不会抛出异常且能返回结果
    }

    @Test
    void testProcessDirectoryWithEmptyDirectory() throws Exception {
        // 创建空目录
        Path emptyDir = tempDir.resolve("empty");
        Files.createDirectory(emptyDir);
        
        // 执行测试
        IntegrationResult result = engine.processDirectory(emptyDir);
        
        // 验证结果
        assertNotNull(result);
        // 空目录应该成功处理，没有文件处理
    }

    @Test
    void testProcessDirectoryWithMixedFiles() throws Exception {
        // 创建包含混合文件的目录
        Path testDir = tempDir.resolve("mixedtest");
        Files.createDirectory(testDir);
        
        // 创建XML文件
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root></root>";
        Files.write(testDir.resolve("valid.xml"), xmlContent.getBytes());
        
        // 创建非XML文件
        Files.write(testDir.resolve("text.txt"), "some text content".getBytes());
        
        // 执行测试
        IntegrationResult result = engine.processDirectory(testDir);
        
        // 验证结果 - 只处理XML文件
        assertNotNull(result);
        // 结果取决于具体的validation engine实现
        // 这里主要测试不会抛出异常
    }
    
    @Test
    void testEngineClose() {
        // 测试引擎可以正常关闭
        assertDoesNotThrow(() -> engine.close());
    }
    
    @Test
    void testIntegrationResultStates() {
        // 测试各种结果状态
        
        IntegrationResult success = IntegrationResult.success(null, null, null);
        assertTrue(success.isSuccess());
        assertFalse(success.isError());
        
        IntegrationResult failure = IntegrationResult.failure("test failure");
        assertTrue(failure.isError());
        assertFalse(failure.isSuccess());
        
        IntegrationResult validationFailure = IntegrationResult.validationFailure(null);
        assertTrue(validationFailure.isValidationFailure());
        assertFalse(validationFailure.isSuccess());
        
        IntegrationResult conflict = IntegrationResult.conflictDetected(null, null, null);
        assertTrue(conflict.hasConflicts());
        assertFalse(conflict.isSuccess());
    }
}
