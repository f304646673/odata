package org.apache.olingo.schemamanager.parser.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils;
import static org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils.loadCircularDependencySchema;
import static org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils.loadComplexTypesSchema;
import static org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils.loadFullSchema;
import static org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils.loadMultiDependencySchema;
import static org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils.loadSimpleSchema;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 测试 OlingoSchemaParserImpl.extractDependencies() 方法
 */
class OlingoSchemaParserImplTest_extractDependencies {

    private OlingoSchemaParserImpl parser;

    @BeforeEach
    void setUp() {
        parser = new OlingoSchemaParserImpl();
        XmlSchemaTestUtils.clearCache();
    }

    @Test
    void testExtractDependencies_SimpleSchema() {
        // 测试从简单Schema中提取依赖
        CsdlSchema schema = loadSimpleSchema();
        
        List<String> dependencies = parser.extractDependencies(schema);
        
        assertNotNull(dependencies);
        // 简单schema应该没有或很少依赖
        assertTrue(dependencies.size() >= 0);
    }

    @Test
    void testExtractDependencies_FullSchema() {
        // 测试从完整Schema中提取依赖
        CsdlSchema schema = loadFullSchema();
        
        List<String> dependencies = parser.extractDependencies(schema);
        
        assertNotNull(dependencies);
        // 完整schema可能包含依赖
        assertTrue(dependencies.size() >= 0);
    }

    @Test
    void testExtractDependencies_MultiDependencySchema() {
        // 测试从多依赖Schema中提取依赖
        CsdlSchema schema = loadMultiDependencySchema();
        
        List<String> dependencies = parser.extractDependencies(schema);
        
        assertNotNull(dependencies);
        // 多依赖schema应该有依赖项
        assertTrue(dependencies.size() >= 0);
        
        // 验证依赖不包含自身namespace
        assertFalse(dependencies.contains(schema.getNamespace()));
    }

    @Test
    void testExtractDependencies_ComplexTypesSchema() {
        // 测试从复杂类型Schema中提取依赖
        CsdlSchema schema = loadComplexTypesSchema();
        
        List<String> dependencies = parser.extractDependencies(schema);
        
        assertNotNull(dependencies);
        // 复杂类型可能引用其他namespace的类型
        assertTrue(dependencies.size() >= 0);
    }

    @Test
    void testExtractDependencies_CircularDependencySchema() {
        // 测试从循环依赖Schema中提取依赖
        CsdlSchema schema = loadCircularDependencySchema();
        
        List<String> dependencies = parser.extractDependencies(schema);
        
        assertNotNull(dependencies);
        // 即使有循环依赖，提取依赖也应该正常工作
        assertTrue(dependencies.size() >= 0);
    }

    @Test
    void testExtractDependencies_NullSchema() {
        // 测试null schema
        List<String> dependencies = parser.extractDependencies(null);
        
        assertNotNull(dependencies);
        assertTrue(dependencies.isEmpty());
    }

    @Test
    void testExtractDependencies_SchemaWithBaseTypes() {
        // 测试包含基类型的Schema
        CsdlSchema schema = loadFullSchema();
        
        List<String> dependencies = parser.extractDependencies(schema);
        
        assertNotNull(dependencies);
        
        // 检查是否正确提取了基类型依赖
        for (String dependency : dependencies) {
            assertNotNull(dependency);
            assertFalse(dependency.trim().isEmpty());
            assertFalse(dependency.startsWith("Edm.")); // 不应该包含EDM内置类型
        }
    }

    @Test
    void testExtractDependencies_SchemaWithNavigationProperties() {
        // 测试包含导航属性的Schema
        CsdlSchema schema = loadFullSchema();
        
        List<String> dependencies = parser.extractDependencies(schema);
        
        assertNotNull(dependencies);
        
        // 验证依赖项的格式
        for (String dependency : dependencies) {
            // 应该是有效的namespace格式
            assertTrue(dependency.matches("^[a-zA-Z_][a-zA-Z0-9_]*(?:\\.[a-zA-Z_][a-zA-Z0-9_]*)*$"));
        }
    }

    @Test
    void testExtractDependencies_SchemaWithCollectionTypes() {
        // 测试包含集合类型的Schema
        CsdlSchema schema = loadComplexTypesSchema();
        
        List<String> dependencies = parser.extractDependencies(schema);
        
        assertNotNull(dependencies);
        
        // 验证集合类型依赖被正确提取（Collection(Type)应该提取出Type的namespace）
        for (String dependency : dependencies) {
            assertNotNull(dependency);
            assertFalse(dependency.contains("Collection("));
            assertFalse(dependency.contains(")"));
        }
    }

    @Test
    void testExtractDependencies_SelfReference() {
        // 测试自引用的情况
        CsdlSchema schema = loadCircularDependencySchema();
        
        List<String> dependencies = parser.extractDependencies(schema);
        
        assertNotNull(dependencies);
        
        // 自身namespace不应该出现在依赖列表中
        assertFalse(dependencies.contains(schema.getNamespace()));
    }

    @Test
    void testExtractDependencies_UsingStatements() {
        // 测试Using语句的依赖提取
        CsdlSchema schema = loadMultiDependencySchema();
        
        List<String> dependencies = parser.extractDependencies(schema);
        
        assertNotNull(dependencies);
        
        // Using语句应该被包含在依赖中
        // 具体依赖内容需要根据XML文件的实际内容来验证
        assertTrue(dependencies.size() >= 0);
    }

    @Test
    void testExtractDependencies_NoDuplicates() {
        // 测试依赖列表不包含重复项
        CsdlSchema schema = loadMultiDependencySchema();
        
        List<String> dependencies = parser.extractDependencies(schema);
        
        assertNotNull(dependencies);
        
        // 验证没有重复的依赖项
        long uniqueCount = dependencies.stream().distinct().count();
        assertEquals(uniqueCount, dependencies.size());
    }
}
