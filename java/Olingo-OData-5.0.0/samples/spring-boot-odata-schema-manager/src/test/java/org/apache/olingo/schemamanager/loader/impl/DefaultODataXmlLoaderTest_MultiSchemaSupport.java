package org.apache.olingo.schemamanager.loader.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.loader.ODataXmlLoader;
import org.apache.olingo.schemamanager.parser.ODataSchemaParser;
import org.apache.olingo.schemamanager.repository.SchemaRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 测试 XmlFileInfo 的多Schema支持
 */
@ExtendWith(MockitoExtension.class)
class DefaultODataXmlLoaderTest_MultiSchemaSupport {

    @Mock
    private ODataSchemaParser parser;

    @Mock
    private SchemaRepository repository;

    private DefaultODataXmlLoader loader;

    @BeforeEach
    void setUp() throws Exception {
        loader = new DefaultODataXmlLoader();
        java.lang.reflect.Field parserField = DefaultODataXmlLoader.class.getDeclaredField("parser");
        parserField.setAccessible(true);
        parserField.set(loader, parser);
        java.lang.reflect.Field repositoryField = DefaultODataXmlLoader.class.getDeclaredField("repository");
        repositoryField.setAccessible(true);
        repositoryField.set(loader, repository);
    }

    @Test
    void testLoadFromInputStream_MultiSchemaSuccess() throws IOException {
        // 模拟多Schema解析结果
        CsdlSchema schema1 = new CsdlSchema();
        schema1.setNamespace("Company.Management");
        
        CsdlSchema schema2 = new CsdlSchema();
        schema2.setNamespace("Company.HR");
        
        CsdlSchema schema3 = new CsdlSchema();
        schema3.setNamespace("Company.Finance");
        
        List<ODataSchemaParser.SchemaWithDependencies> schemaList = new ArrayList<>();
        schemaList.add(new ODataSchemaParser.SchemaWithDependencies(schema1, new ArrayList<>()));
        schemaList.add(new ODataSchemaParser.SchemaWithDependencies(schema2, java.util.Arrays.asList("Company.Management")));
        schemaList.add(new ODataSchemaParser.SchemaWithDependencies(schema3, java.util.Arrays.asList("Company.Management", "Company.HR")));
        
        ODataSchemaParser.ParseResult mockParseResult = ODataSchemaParser.ParseResult.success(schemaList);
        when(parser.parseSchema(any(), anyString())).thenReturn(mockParseResult);

        String xmlContent = "<test>multi-schema content</test>";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes());
        
        // 执行测试
        ODataXmlLoader.LoadResult result = loader.loadFromInputStream(inputStream, "multi-schema.xml");
        
        // 验证结果
        assertNotNull(result, "LoadResult不应为null");
        assertEquals(1, result.getTotalFiles(), "总文件数应为1");
        assertEquals(1, result.getSuccessfulFiles(), "成功文件数应为1");
        assertEquals(0, result.getFailedFiles(), "失败文件数应为0");
        assertTrue(result.getErrorMessages().isEmpty(), "不应有错误信息");
        
        // 验证XmlFileInfo
        Map<String, ODataXmlLoader.XmlFileInfo> loadedFiles = result.getLoadedFiles();
        assertTrue(loadedFiles.containsKey("multi-schema.xml"), "应包含加载的文件");
        
        ODataXmlLoader.XmlFileInfo fileInfo = loadedFiles.get("multi-schema.xml");
        assertNotNull(fileInfo, "文件信息不应为null");
        assertTrue(fileInfo.isLoadSuccess(), "加载应成功");
        assertTrue(fileInfo.hasMultipleSchemas(), "应检测到多个Schema");
        assertEquals(3, fileInfo.getSchemaCount(), "Schema数量应为3");
        
        // 验证Schema信息
        List<ODataXmlLoader.SchemaInfo> schemas = fileInfo.getSchemas();
        assertEquals(3, schemas.size(), "Schema列表大小应为3");
        
        // 验证第一个Schema
        ODataXmlLoader.SchemaInfo mgmtSchema = fileInfo.getSchemaByNamespace("Company.Management");
        assertNotNull(mgmtSchema, "应找到Management Schema");
        assertEquals("Company.Management", mgmtSchema.getNamespace());
        assertTrue(mgmtSchema.getDependencies().isEmpty(), "Management Schema应无依赖");
        
        // 验证第二个Schema
        ODataXmlLoader.SchemaInfo hrSchema = fileInfo.getSchemaByNamespace("Company.HR");
        assertNotNull(hrSchema, "应找到HR Schema");
        assertEquals("Company.HR", hrSchema.getNamespace());
        assertEquals(1, hrSchema.getDependencies().size(), "HR Schema应有1个依赖");
        assertTrue(hrSchema.getDependencies().contains("Company.Management"), "HR Schema应依赖Management");
        
        // 验证第三个Schema
        ODataXmlLoader.SchemaInfo financeSchema = fileInfo.getSchemaByNamespace("Company.Finance");
        assertNotNull(financeSchema, "应找到Finance Schema");
        assertEquals("Company.Finance", financeSchema.getNamespace());
        assertEquals(2, financeSchema.getDependencies().size(), "Finance Schema应有2个依赖");
        assertTrue(financeSchema.getDependencies().contains("Company.Management"), "Finance Schema应依赖Management");
        assertTrue(financeSchema.getDependencies().contains("Company.HR"), "Finance Schema应依赖HR");
        
        // 验证getAllNamespaces
        List<String> allNamespaces = fileInfo.getAllNamespaces();
        assertEquals(3, allNamespaces.size(), "所有namespace数量应为3");
        assertTrue(allNamespaces.contains("Company.Management"), "应包含Management namespace");
        assertTrue(allNamespaces.contains("Company.HR"), "应包含HR namespace");
        assertTrue(allNamespaces.contains("Company.Finance"), "应包含Finance namespace");
        
        // 验证向后兼容性
        assertEquals("Company.Management", fileInfo.getNamespace(), "向后兼容方法应返回第一个namespace");
        assertTrue(fileInfo.getDependencies().isEmpty(), "向后兼容方法应返回第一个Schema的依赖");
        
        // 验证repository调用
        verify(repository).addSchema(eq(schema1), eq("multi-schema.xml"));
        verify(repository).addSchema(eq(schema2), eq("multi-schema.xml"));
        verify(repository).addSchema(eq(schema3), eq("multi-schema.xml"));
    }

    @Test
    void testLoadFromInputStream_SingleSchemaBackwardCompatibility() throws IOException {
        // 测试单Schema的向后兼容性
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("TestService");
        
        List<ODataSchemaParser.SchemaWithDependencies> schemaList = new ArrayList<>();
        schemaList.add(new ODataSchemaParser.SchemaWithDependencies(schema, java.util.Arrays.asList("SomeDependency")));
        
        ODataSchemaParser.ParseResult mockParseResult = ODataSchemaParser.ParseResult.success(schemaList);
        when(parser.parseSchema(any(), anyString())).thenReturn(mockParseResult);

        String xmlContent = "<test>single-schema content</test>";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes());
        
        ODataXmlLoader.LoadResult result = loader.loadFromInputStream(inputStream, "single-schema.xml");
        
        assertNotNull(result);
        assertEquals(1, result.getSuccessfulFiles());
        
        ODataXmlLoader.XmlFileInfo fileInfo = result.getLoadedFiles().get("single-schema.xml");
        assertNotNull(fileInfo);
        assertFalse(fileInfo.hasMultipleSchemas(), "单Schema不应检测为多Schema");
        assertEquals(1, fileInfo.getSchemaCount(), "Schema数量应为1");
        assertEquals("TestService", fileInfo.getNamespace(), "向后兼容方法应正常工作");
        assertEquals(1, fileInfo.getDependencies().size(), "向后兼容方法应返回依赖");
        assertTrue(fileInfo.getDependencies().contains("SomeDependency"), "应包含依赖");
    }

    @Test
    void testLoadFromInputStream_EmptySchemaList() throws IOException {
        // 测试空Schema列表
        List<ODataSchemaParser.SchemaWithDependencies> emptySchemaList = new ArrayList<>();
        ODataSchemaParser.ParseResult mockParseResult = ODataSchemaParser.ParseResult.success(emptySchemaList);
        when(parser.parseSchema(any(), anyString())).thenReturn(mockParseResult);

        String xmlContent = "<test>empty content</test>";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes());
        
        ODataXmlLoader.LoadResult result = loader.loadFromInputStream(inputStream, "empty.xml");
        
        assertNotNull(result);
        assertEquals(1, result.getSuccessfulFiles());
        
        ODataXmlLoader.XmlFileInfo fileInfo = result.getLoadedFiles().get("empty.xml");
        assertNotNull(fileInfo);
        assertFalse(fileInfo.hasMultipleSchemas(), "空列表不应检测为多Schema");
        assertEquals(0, fileInfo.getSchemaCount(), "Schema数量应为0");
        assertNull(fileInfo.getNamespace(), "namespace应为null");
        assertTrue(fileInfo.getDependencies().isEmpty(), "依赖应为空");
        assertTrue(fileInfo.getAllNamespaces().isEmpty(), "所有namespace应为空");
    }

    @Test
    void testGetSchemaByNamespace_NotFound() throws IOException {
        // 测试通过不存在的namespace查找Schema
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("ExistingNamespace");
        
        List<ODataSchemaParser.SchemaWithDependencies> schemaList = new ArrayList<>();
        schemaList.add(new ODataSchemaParser.SchemaWithDependencies(schema, new ArrayList<>()));
        
        ODataSchemaParser.ParseResult mockParseResult = ODataSchemaParser.ParseResult.success(schemaList);
        when(parser.parseSchema(any(), anyString())).thenReturn(mockParseResult);

        String xmlContent = "<test>content</test>";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes());
        
        ODataXmlLoader.LoadResult result = loader.loadFromInputStream(inputStream, "test.xml");
        ODataXmlLoader.XmlFileInfo fileInfo = result.getLoadedFiles().get("test.xml");
        
        assertNull(fileInfo.getSchemaByNamespace("NonExistentNamespace"), "不存在的namespace应返回null");
        assertNotNull(fileInfo.getSchemaByNamespace("ExistingNamespace"), "存在的namespace应返回Schema");
    }
}
