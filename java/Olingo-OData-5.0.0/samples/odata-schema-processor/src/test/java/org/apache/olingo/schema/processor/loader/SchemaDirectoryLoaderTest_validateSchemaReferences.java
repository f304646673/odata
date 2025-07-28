package org.apache.olingo.schema.processor.loader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
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
 * 测试 SchemaDirectoryLoader.validateSchemaReferences 方法
 */
@RunWith(MockitoJUnitRunner.class)
public class SchemaDirectoryLoaderTest_validateSchemaReferences {

    @Mock
    private ODataXmlParser mockXmlParser;
    
    private SchemaDirectoryLoader loader;
    
    @Before
    public void setUp() {
        loader = new SchemaDirectoryLoader(mockXmlParser);
    }
    
    @Test
    public void testValidateSchemaReferencesNull() {
        // 执行测试
        SchemaDirectoryLoader.ValidationResult result = loader.validateSchemaReferences(null);
        
        // 验证结果
        assertNotNull("Result should not be null", result);
        assertFalse("Should not be valid for null input", result.isValid());
        assertFalse("Should have errors", result.getErrors().isEmpty());
        assertTrue("Should contain null error", result.getErrors().get(0).contains("null or empty"));
    }
    
    @Test
    public void testValidateSchemaReferencesEmpty() {
        // 执行测试
        List<CsdlSchema> emptySchemas = new ArrayList<>();
        SchemaDirectoryLoader.ValidationResult result = loader.validateSchemaReferences(emptySchemas);
        
        // 验证结果
        assertNotNull("Result should not be null", result);
        assertFalse("Should not be valid for empty input", result.isValid());
        assertFalse("Should have errors", result.getErrors().isEmpty());
        assertTrue("Should contain empty error", result.getErrors().get(0).contains("null or empty"));
    }
    
    @Test
    public void testValidateSchemaReferencesValidSingle() {
        // 准备测试数据
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("com.example.test");
        List<CsdlSchema> schemas = Arrays.asList(schema);
        
        // 执行测试
        SchemaDirectoryLoader.ValidationResult result = loader.validateSchemaReferences(schemas);
        
        // 验证结果
        assertNotNull("Result should not be null", result);
        assertTrue("Should be valid for simple schema", result.isValid());
        assertTrue("Should have no errors", result.getErrors().isEmpty());
    }
    
    @Test
    public void testValidateSchemaReferencesInvalidNamespace() {
        // 准备测试数据 - 创建无效命名空间的Schema
        CsdlSchema invalidSchema = new CsdlSchema();
        invalidSchema.setNamespace(""); // 空命名空间
        List<CsdlSchema> schemas = Arrays.asList(invalidSchema);
        
        // 执行测试
        SchemaDirectoryLoader.ValidationResult result = loader.validateSchemaReferences(schemas);
        
        // 验证结果
        assertNotNull("Result should not be null", result);
        assertFalse("Should not be valid for invalid namespace", result.isValid());
        assertFalse("Should have errors", result.getErrors().isEmpty());
    }
    
    @Test
    public void testValidateSchemaReferencesDuplicateNamespace() {
        // 准备测试数据 - 创建重复命名空间的Schema
        CsdlSchema schema1 = new CsdlSchema();
        schema1.setNamespace("com.example.duplicate");
        CsdlSchema schema2 = new CsdlSchema();
        schema2.setNamespace("com.example.duplicate");
        List<CsdlSchema> schemas = Arrays.asList(schema1, schema2);
        
        // 执行测试
        SchemaDirectoryLoader.ValidationResult result = loader.validateSchemaReferences(schemas);
        
        // 验证结果
        assertNotNull("Result should not be null", result);
        assertFalse("Should not be valid for duplicate namespace", result.isValid());
        assertFalse("Should have errors", result.getErrors().isEmpty());
        assertTrue("Should contain duplicate error", 
                  result.getErrors().stream().anyMatch(e -> e.contains("Duplicate namespace")));
    }
    
    @Test
    public void testValidateSchemaReferencesNullNamespace() {
        // 准备测试数据 - 创建null命名空间的Schema
        CsdlSchema nullNamespaceSchema = new CsdlSchema();
        nullNamespaceSchema.setNamespace(null);
        List<CsdlSchema> schemas = Arrays.asList(nullNamespaceSchema);
        
        // 执行测试
        SchemaDirectoryLoader.ValidationResult result = loader.validateSchemaReferences(schemas);
        
        // 验证结果
        assertNotNull("Result should not be null", result);
        assertFalse("Should not be valid for null namespace", result.isValid());
        assertFalse("Should have errors", result.getErrors().isEmpty());
        assertTrue("Should contain null namespace error", 
                  result.getErrors().stream().anyMatch(e -> e.contains("null or empty namespace")));
    }
    
    @Test
    public void testValidateSchemaReferencesWhitespaceNamespace() {
        // 准备测试数据 - 创建只有空白字符的命名空间的Schema
        CsdlSchema whitespaceSchema = new CsdlSchema();
        whitespaceSchema.setNamespace("   ");
        List<CsdlSchema> schemas = Arrays.asList(whitespaceSchema);
        
        // 执行测试
        SchemaDirectoryLoader.ValidationResult result = loader.validateSchemaReferences(schemas);
        
        // 验证结果
        assertNotNull("Result should not be null", result);
        assertFalse("Should not be valid for whitespace namespace", result.isValid());
        assertFalse("Should have errors", result.getErrors().isEmpty());
    }
    
    @Test
    public void testValidateSchemaReferencesInvalidNamespaceFormat() {
        // 准备测试数据 - 创建格式无效的命名空间
        CsdlSchema invalidFormatSchema = new CsdlSchema();
        invalidFormatSchema.setNamespace("invalid namespace with spaces");
        List<CsdlSchema> schemas = Arrays.asList(invalidFormatSchema);
        
        // 执行测试
        SchemaDirectoryLoader.ValidationResult result = loader.validateSchemaReferences(schemas);
        
        // 验证结果
        assertNotNull("Result should not be null", result);
        assertFalse("Should not be valid for invalid format namespace", result.isValid());
        assertFalse("Should have errors", result.getErrors().isEmpty());
        assertTrue("Should contain format error", 
                  result.getErrors().stream().anyMatch(e -> e.contains("Invalid namespace format")));
    }
    
    @Test
    public void testValidateSchemaReferencesMultipleValid() {
        // 准备测试数据 - 多个有效的Schema
        CsdlSchema schema1 = new CsdlSchema();
        schema1.setNamespace("com.example.schema1");
        CsdlSchema schema2 = new CsdlSchema();
        schema2.setNamespace("com.example.schema2");
        CsdlSchema schema3 = new CsdlSchema();
        schema3.setNamespace("org.test.schema3");
        
        List<CsdlSchema> schemas = Arrays.asList(schema1, schema2, schema3);
        
        // 执行测试
        SchemaDirectoryLoader.ValidationResult result = loader.validateSchemaReferences(schemas);
        
        // 验证结果
        assertNotNull("Result should not be null", result);
        assertTrue("Should be valid for multiple valid schemas", result.isValid());
        assertTrue("Should have no errors", result.getErrors().isEmpty());
    }
    
    @Test
    public void testValidationResultBasic() {
        // 创建验证结果
        SchemaDirectoryLoader.ValidationResult result = new SchemaDirectoryLoader.ValidationResult();
        
        // 验证初始状态
        assertTrue("Should be valid initially", result.isValid());
        assertFalse("Should have no warnings initially", result.hasWarnings());
        assertTrue("Should have no errors", result.getErrors().isEmpty());
        assertTrue("Should have no warnings", result.getWarnings().isEmpty());
        
        // 添加错误和警告
        result.addError("Test error");
        result.addWarning("Test warning");
        
        // 验证状态变化
        assertFalse("Should not be valid after adding error", result.isValid());
        assertTrue("Should have warnings after adding warning", result.hasWarnings());
        assertEquals("Should have 1 error", 1, result.getErrors().size());
        assertEquals("Should have 1 warning", 1, result.getWarnings().size());
        assertEquals("Error content should match", "Test error", result.getErrors().get(0));
        assertEquals("Warning content should match", "Test warning", result.getWarnings().get(0));
        
        // 验证返回的列表是副本
        List<String> errors = result.getErrors();
        errors.clear();
        assertEquals("Original errors should not be affected", 1, result.getErrors().size());
    }
}
