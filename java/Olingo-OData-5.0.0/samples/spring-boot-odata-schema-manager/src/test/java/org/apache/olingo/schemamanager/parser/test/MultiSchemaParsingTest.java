package org.apache.olingo.schemamanager.parser.test;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.parser.ODataSchemaParser.ParseResult;
import org.apache.olingo.schemamanager.parser.ODataSchemaParser.SchemaWithDependencies;
import org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 多Schema XML解析测试
 */
public class MultiSchemaParsingTest {

    @BeforeEach
    void setUp() {
        XmlSchemaTestUtils.clearCache();
    }

    @Test
    void testParseMultiSchemaXml() {
        // 测试解析包含多个Schema的XML
        ParseResult result = XmlSchemaTestUtils.loadMultiSchemaXml();
        
        assertNotNull(result, "ParseResult不应为null");
        assertTrue(result.isSuccess(), "解析应该成功");
        assertNull(result.getErrorMessage(), "不应有错误信息");
        
        List<SchemaWithDependencies> schemas = result.getSchemas();
        assertNotNull(schemas, "Schema列表不应为null");
        assertEquals(3, schemas.size(), "应该解析出3个Schema");
        assertTrue(result.hasMultipleSchemas(), "应该检测到多个Schema");
        assertEquals(3, result.getSchemaCount(), "Schema数量应为3");
    }

    @Test
    void testSchemaNamespaces() {
        // 测试Schema的namespace
        ParseResult result = XmlSchemaTestUtils.loadMultiSchemaXml();
        
        Set<String> expectedNamespaces = new HashSet<>();
        expectedNamespaces.add("Company.Management");
        expectedNamespaces.add("Company.HR");
        expectedNamespaces.add("Company.Finance");
        
        Set<String> actualNamespaces = new HashSet<>();
        for (SchemaWithDependencies schemaWithDeps : result.getSchemas()) {
            actualNamespaces.add(schemaWithDeps.getNamespace());
        }
        
        assertEquals(expectedNamespaces, actualNamespaces, "Schema的namespace应该匹配");
    }

    @Test
    void testSchemaByNamespace() {
        // 测试按namespace获取Schema
        ParseResult result = XmlSchemaTestUtils.loadMultiSchemaXml();
        
        SchemaWithDependencies mgmtSchema = result.getSchemaByNamespace("Company.Management");
        assertNotNull(mgmtSchema, "应该能找到Management Schema");
        assertEquals("Company.Management", mgmtSchema.getNamespace());
        
        SchemaWithDependencies hrSchema = result.getSchemaByNamespace("Company.HR");
        assertNotNull(hrSchema, "应该能找到HR Schema");
        assertEquals("Company.HR", hrSchema.getNamespace());
        
        SchemaWithDependencies financeSchema = result.getSchemaByNamespace("Company.Finance");
        assertNotNull(financeSchema, "应该能找到Finance Schema");
        assertEquals("Company.Finance", financeSchema.getNamespace());
        
        SchemaWithDependencies nonExistentSchema = result.getSchemaByNamespace("NonExistent.Namespace");
        assertNull(nonExistentSchema, "不存在的namespace应该返回null");
    }

    @Test
    void testSchemaDependencies() {
        // 测试Schema依赖关系
        ParseResult result = XmlSchemaTestUtils.loadMultiSchemaXml();
        
        // Management Schema应该没有依赖
        SchemaWithDependencies mgmtSchema = result.getSchemaByNamespace("Company.Management");
        List<String> mgmtDependencies = mgmtSchema.getDependencies();
        assertTrue(mgmtDependencies.isEmpty(), "Management Schema应该没有依赖");
        
        // HR Schema应该依赖Management
        SchemaWithDependencies hrSchema = result.getSchemaByNamespace("Company.HR");
        List<String> hrDependencies = hrSchema.getDependencies();
        assertEquals(1, hrDependencies.size(), "HR Schema应该有1个依赖");
        assertTrue(hrDependencies.contains("Company.Management"), "HR Schema应该依赖Company.Management");
        
        // Finance Schema应该依赖Management和HR
        SchemaWithDependencies financeSchema = result.getSchemaByNamespace("Company.Finance");
        List<String> financeDependencies = financeSchema.getDependencies();
        assertEquals(2, financeDependencies.size(), "Finance Schema应该有2个依赖");
        assertTrue(financeDependencies.contains("Company.Management"), "Finance Schema应该依赖Company.Management");
        assertTrue(financeDependencies.contains("Company.HR"), "Finance Schema应该依赖Company.HR");
    }

    @Test
    void testSchemaTypes() {
        // 测试Schema中的类型定义
        ParseResult result = XmlSchemaTestUtils.loadMultiSchemaXml();
        
        // 检查Management Schema的类型
        SchemaWithDependencies mgmtSchema = result.getSchemaByNamespace("Company.Management");
        CsdlSchema mgmtCsdl = mgmtSchema.getSchema();
        assertEquals(2, mgmtCsdl.getEntityTypes().size(), "Management Schema应该有2个EntityType");
        assertEquals(0, mgmtCsdl.getComplexTypes().size(), "Management Schema应该有0个ComplexType");
        assertEquals(0, mgmtCsdl.getEnumTypes().size(), "Management Schema应该有0个EnumType");
        
        // 检查HR Schema的类型
        SchemaWithDependencies hrSchema = result.getSchemaByNamespace("Company.HR");
        CsdlSchema hrCsdl = hrSchema.getSchema();
        assertEquals(1, hrCsdl.getEntityTypes().size(), "HR Schema应该有1个EntityType");
        assertEquals(1, hrCsdl.getComplexTypes().size(), "HR Schema应该有1个ComplexType");
        assertEquals(1, hrCsdl.getEnumTypes().size(), "HR Schema应该有1个EnumType");
        
        // 检查Finance Schema的类型
        SchemaWithDependencies financeSchema = result.getSchemaByNamespace("Company.Finance");
        CsdlSchema financeCsdl = financeSchema.getSchema();
        assertEquals(2, financeCsdl.getEntityTypes().size(), "Finance Schema应该有2个EntityType");
        assertEquals(0, financeCsdl.getComplexTypes().size(), "Finance Schema应该有0个ComplexType");
        assertEquals(0, financeCsdl.getEnumTypes().size(), "Finance Schema应该有0个EnumType");
    }

    @Test
    void testDuplicateNamespaceError() {
        // 测试重复namespace的错误处理
        ParseResult result = XmlSchemaTestUtils.loadDuplicateNamespaceSchemaXml();
        
        assertNotNull(result, "ParseResult不应为null");
        assertFalse(result.isSuccess(), "解析应该失败");
        assertNotNull(result.getErrorMessage(), "应该有错误信息");
        assertTrue(result.getErrorMessage().contains("重复") || result.getErrorMessage().contains("Duplicate"), 
                  "错误信息应该包含'重复'或'Duplicate'，实际信息: " + result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Company.Test"), 
                  "错误信息应该包含重复的namespace，实际信息: " + result.getErrorMessage());
    }

    @Test
    void testBackwardCompatibility() {
        // 测试向后兼容性
        ParseResult result = XmlSchemaTestUtils.loadMultiSchemaXml();
        
        // 使用旧的API应该返回第一个Schema
        CsdlSchema firstSchema = result.getSchema();
        assertNotNull(firstSchema, "向后兼容方法应该返回第一个Schema");
        assertEquals("Company.Management", firstSchema.getNamespace(), "应该返回第一个Schema");
        
        // 使用旧的Dependencies API
        List<String> firstDependencies = result.getDependencies();
        assertNotNull(firstDependencies, "向后兼容方法应该返回第一个Schema的依赖");
        assertTrue(firstDependencies.isEmpty(), "第一个Schema没有依赖");
    }

    @Test
    void testUtilityMethods() {
        // 测试工具方法
        assertTrue(XmlSchemaTestUtils.hasMultipleSchemas("loader/multi-schema/multi-schema.xml"), 
                   "应该检测到多个Schema");
        
        List<String> namespaces = XmlSchemaTestUtils.getSchemaNamespaces("loader/multi-schema/multi-schema.xml");
        assertEquals(3, namespaces.size(), "应该返回3个namespace");
        assertTrue(namespaces.contains("Company.Management"), "应该包含Company.Management");
        assertTrue(namespaces.contains("Company.HR"), "应该包含Company.HR");
        assertTrue(namespaces.contains("Company.Finance"), "应该包含Company.Finance");
    }
}
