package org.apache.olingo.schema.processor.loader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schema.processor.parser.ODataXmlParser;
import org.apache.olingo.schema.processor.repository.SchemaRepository;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 测试 SchemaDirectoryLoader.loadSchemasToRepository 方法
 */
@ExtendWith(MockitoExtension.class)
public class SchemaDirectoryLoaderTest_loadSchemasToRepository {

    @Mock
    private ODataXmlParser mockXmlParser;
    
    @Mock
    private SchemaRepository mockRepository;
    
    private SchemaDirectoryLoader loader;
    
    @BeforeEach
    public void setUp() {
        loader = new SchemaDirectoryLoader(mockXmlParser);
    }
    
    @Test
    public void testLoadSchemasToRepositorySuccess() {
        // 准备测试数据
        CsdlSchema schema1 = new CsdlSchema();
        schema1.setNamespace("com.example.test1");
        CsdlSchema schema2 = new CsdlSchema();
        schema2.setNamespace("com.example.test2");
        
        List<CsdlSchema> schemas = Arrays.asList(schema1, schema2);
        
        // 执行测试
        loader.loadSchemasToRepository(schemas, mockRepository);
        
        // 验证调用
        verify(mockRepository, times(2)).addSchema(any(CsdlSchema.class));
        verify(mockRepository).addSchema(schema1);
        verify(mockRepository).addSchema(schema2);
    }
    
    @Test
    public void testLoadSchemasToRepositoryEmptyList() {
        // 准备测试数据
        List<CsdlSchema> emptySchemas = new ArrayList<>();
        
        // 执行测试
        loader.loadSchemasToRepository(emptySchemas, mockRepository);
        
        // 验证没有调用
        verify(mockRepository, never()).addSchema(any(CsdlSchema.class));
    }
    
    @Test
    public void testLoadSchemasToRepositoryNullList() {
        // 执行测试
        loader.loadSchemasToRepository(null, mockRepository);
        
        // 验证没有调用
        verify(mockRepository, never()).addSchema(any(CsdlSchema.class));
    }
    
    @Test
    public void testLoadSchemasToRepositoryWithExceptions() {
        // 准备测试数据
        CsdlSchema schema1 = new CsdlSchema();
        schema1.setNamespace("com.example.test1");
        CsdlSchema schema2 = new CsdlSchema();
        schema2.setNamespace("com.example.test2");
        
        List<CsdlSchema> schemas = Arrays.asList(schema1, schema2);
        
        // 模拟第一个schema添加时抛出异常，第二个成
        doThrow(new RuntimeException("Repository error")).when(mockRepository).addSchema(schema1);
        doNothing().when(mockRepository).addSchema(schema2);
        
        // 执行测试，不应该抛出异常
        loader.loadSchemasToRepository(schemas, mockRepository);
        
        // 验证两个schema都尝试添加了
        verify(mockRepository, times(2)).addSchema(any(CsdlSchema.class));
        verify(mockRepository).addSchema(schema1);
        verify(mockRepository).addSchema(schema2);
    }
    
    @Test
    public void testLoadSchemasToRepositorySingleSchema() {
        // 准备测试数据
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("com.example.single");
        
        List<CsdlSchema> schemas = Arrays.asList(schema);
        
        // 执行测试
        loader.loadSchemasToRepository(schemas, mockRepository);
        
        // 验证调用
        verify(mockRepository, times(1)).addSchema(schema);
    }
    
    @Test
    public void testLoadSchemasToRepositoryNullRepository() {
        // 准备测试数据
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("com.example.test");
        List<CsdlSchema> schemas = Arrays.asList(schema);
        
        // 执行测试，应该抛出异
        try {
            loader.loadSchemasToRepository(schemas, null);
            fail("Should throw NullPointerException for null repository");
        } catch (NullPointerException e) {
            // 预期的异
        }
    }
    
    @Test
    public void testLoadSchemasToRepositoryWithNullNamespace() {
        // 准备测试数据
        CsdlSchema schemaWithNullNamespace = new CsdlSchema();
        schemaWithNullNamespace.setNamespace(null);
        
        List<CsdlSchema> schemas = Arrays.asList(schemaWithNullNamespace);
        
        // 执行测试
        loader.loadSchemasToRepository(schemas, mockRepository);
        
        // 验证仍然尝试添加
        verify(mockRepository, times(1)).addSchema(schemaWithNullNamespace);
    }
}
