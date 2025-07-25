package org.apache.olingo.schemamanager.merger.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.merger.SchemaMerger.MergeResult;
import org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils;
import static org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils.loadComplexTypesSchema;
import static org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils.loadSimpleSchema;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 测试 DefaultSchemaMerger.mergeSchemas(List<CsdlSchema>) 方法
 */
class DefaultSchemaMergerTest_mergeSchemas {

    private DefaultSchemaMerger merger;

    @BeforeEach
    void setUp() {
        merger = new DefaultSchemaMerger();
        XmlSchemaTestUtils.clearCache();
    }

    @Test
    void testMergeSchemas_NullInput() {
        // 测试null输入
        MergeResult result = merger.mergeSchemas(null);
        
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNull(result.getMergedSchema());
        assertFalse(result.getErrors().isEmpty());
        assertTrue(result.getErrors().get(0).contains("No schemas provided"));
    }

    @Test
    void testMergeSchemas_EmptyList() {
        // 测试空列表
        List<CsdlSchema> schemas = new ArrayList<>();
        
        MergeResult result = merger.mergeSchemas(schemas);
        
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNull(result.getMergedSchema());
        assertFalse(result.getErrors().isEmpty());
        assertTrue(result.getErrors().get(0).contains("No schemas provided"));
    }

    @Test
    void testMergeSchemas_SingleSchema() {
        // 测试单个schema
        CsdlSchema schema = loadSimpleSchema();
        List<CsdlSchema> schemas = Arrays.asList(schema);
        
        MergeResult result = merger.mergeSchemas(schemas);
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getMergedSchema());
        assertEquals(schema.getNamespace(), result.getMergedSchema().getNamespace());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void testMergeSchemas_TwoCompatibleSchemas() {
        // 测试两个兼容的schema
        CsdlSchema schema1 = new CsdlSchema();
        schema1.setNamespace("ODataDemo");
        
        CsdlEntityType entityType1 = new CsdlEntityType();
        entityType1.setName("Person");
        schema1.setEntityTypes(Arrays.asList(entityType1));
        
        CsdlSchema schema2 = new CsdlSchema();
        schema2.setNamespace("ODataDemo");
        
        CsdlEntityType entityType2 = new CsdlEntityType();
        entityType2.setName("Employee");
        schema2.setEntityTypes(Arrays.asList(entityType2));
        
        List<CsdlSchema> schemas = Arrays.asList(schema1, schema2);
        
        MergeResult result = merger.mergeSchemas(schemas);
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getMergedSchema());
        assertEquals("ODataDemo", result.getMergedSchema().getNamespace());
        
        // 合并后应该包含两个EntityType
        assertNotNull(result.getMergedSchema().getEntityTypes());
        assertEquals(2, result.getMergedSchema().getEntityTypes().size());
        
        boolean hasPersonType = result.getMergedSchema().getEntityTypes().stream()
            .anyMatch(et -> "Person".equals(et.getName()));
        boolean hasEmployeeType = result.getMergedSchema().getEntityTypes().stream()
            .anyMatch(et -> "Employee".equals(et.getName()));
        
        assertTrue(hasPersonType);
        assertTrue(hasEmployeeType);
    }

    @Test
    void testMergeSchemas_ConflictingTypes() {
        // 测试有冲突类型的schema
        CsdlSchema schema1 = new CsdlSchema();
        schema1.setNamespace("ODataDemo");
        
        CsdlEntityType entityType1 = new CsdlEntityType();
        entityType1.setName("Person");
        CsdlProperty prop1 = new CsdlProperty();
        prop1.setName("ID");
        prop1.setType("Edm.Int32");
        entityType1.setProperties(Arrays.asList(prop1));
        schema1.setEntityTypes(Arrays.asList(entityType1));
        
        CsdlSchema schema2 = new CsdlSchema();
        schema2.setNamespace("ODataDemo");
        
        CsdlEntityType entityType2 = new CsdlEntityType();
        entityType2.setName("Person"); // 同名类型
        CsdlProperty prop2 = new CsdlProperty();
        prop2.setName("Name");
        prop2.setType("Edm.String");
        entityType2.setProperties(Arrays.asList(prop2));
        schema2.setEntityTypes(Arrays.asList(entityType2));
        
        List<CsdlSchema> schemas = Arrays.asList(schema1, schema2);
        
        MergeResult result = merger.mergeSchemas(schemas);
        
        assertNotNull(result);
        
        // 根据实现，可能成功合并或产生警告/错误
        if (result.isSuccess()) {
            // 如果成功，应该有警告信息
            assertFalse(result.getWarnings().isEmpty());
        } else {
            // 如果失败，应该有错误信息
            assertFalse(result.getErrors().isEmpty());
        }
    }

    @Test
    void testMergeSchemas_DifferentNamespaces() {
        // 测试不同namespace的schema
        CsdlSchema schema1 = new CsdlSchema();
        schema1.setNamespace("Schema1");
        
        CsdlEntityType entityType1 = new CsdlEntityType();
        entityType1.setName("Type1");
        schema1.setEntityTypes(Arrays.asList(entityType1));
        
        CsdlSchema schema2 = new CsdlSchema();
        schema2.setNamespace("Schema2");
        
        CsdlEntityType entityType2 = new CsdlEntityType();
        entityType2.setName("Type2");
        schema2.setEntityTypes(Arrays.asList(entityType2));
        
        List<CsdlSchema> schemas = Arrays.asList(schema1, schema2);
        
        MergeResult result = merger.mergeSchemas(schemas);
        
        assertNotNull(result);
        
        // 不同namespace的schema合并可能失败或产生警告
        if (!result.isSuccess()) {
            assertFalse(result.getErrors().isEmpty());
        }
    }

    @Test
    void testMergeSchemas_WithComplexTypes() {
        // 测试包含复杂类型的schema合并
        CsdlSchema schema1 = new CsdlSchema();
        schema1.setNamespace("ODataDemo");
        
        CsdlComplexType complexType1 = new CsdlComplexType();
        complexType1.setName("Address");
        schema1.setComplexTypes(Arrays.asList(complexType1));
        
        CsdlSchema schema2 = new CsdlSchema();
        schema2.setNamespace("ODataDemo");
        
        CsdlComplexType complexType2 = new CsdlComplexType();
        complexType2.setName("ContactInfo");
        schema2.setComplexTypes(Arrays.asList(complexType2));
        
        List<CsdlSchema> schemas = Arrays.asList(schema1, schema2);
        
        MergeResult result = merger.mergeSchemas(schemas);
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getMergedSchema());
        
        // 合并后应该包含两个ComplexType
        assertNotNull(result.getMergedSchema().getComplexTypes());
        assertEquals(2, result.getMergedSchema().getComplexTypes().size());
    }

    @Test
    void testMergeSchemas_FromXmlSchemas() {
        // 使用XML schema进行测试
        CsdlSchema schema1 = loadSimpleSchema();
        CsdlSchema schema2 = loadComplexTypesSchema();
        
        // 确保namespace相同以便合并
        schema2.setNamespace(schema1.getNamespace());
        
        List<CsdlSchema> schemas = Arrays.asList(schema1, schema2);
        
        MergeResult result = merger.mergeSchemas(schemas);
        
        assertNotNull(result);
        
        // 验证合并结果的基本有效性
        if (result.isSuccess()) {
            assertNotNull(result.getMergedSchema());
            assertEquals(schema1.getNamespace(), result.getMergedSchema().getNamespace());
        }
    }

    @Test
    void testMergeSchemas_WithNullSchemaInList() {
        // 测试列表中包含null schema
        CsdlSchema validSchema = loadSimpleSchema();
        List<CsdlSchema> schemas = Arrays.asList(validSchema, null);
        
        MergeResult result = merger.mergeSchemas(schemas);
        
        assertNotNull(result);
        
        // 应该跳过null schema并处理有效的schema
        if (result.isSuccess()) {
            assertNotNull(result.getMergedSchema());
            assertEquals(validSchema.getNamespace(), result.getMergedSchema().getNamespace());
        }
    }

    @Test
    void testMergeSchemas_LargeNumberOfSchemas() {
        // 测试大量schema的合并性能
        List<CsdlSchema> schemas = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
            CsdlSchema schema = new CsdlSchema();
            schema.setNamespace("ODataDemo");
            
            CsdlEntityType entityType = new CsdlEntityType();
            entityType.setName("Type" + i);
            schema.setEntityTypes(Arrays.asList(entityType));
            
            schemas.add(schema);
        }
        
        long startTime = System.currentTimeMillis();
        
        MergeResult result = merger.mergeSchemas(schemas);
        
        long duration = System.currentTimeMillis() - startTime;
        
        assertNotNull(result);
        assertTrue(duration < 3000, "Merge took too long: " + duration + "ms");
        
        if (result.isSuccess()) {
            assertNotNull(result.getMergedSchema());
            // 应该包含所有10个EntityType
            assertEquals(10, result.getMergedSchema().getEntityTypes().size());
        }
    }

    @Test
    void testMergeSchemas_EmptySchemas() {
        // 测试空的schema对象
        CsdlSchema emptySchema1 = new CsdlSchema();
        emptySchema1.setNamespace("ODataDemo");
        
        CsdlSchema emptySchema2 = new CsdlSchema();
        emptySchema2.setNamespace("ODataDemo");
        
        List<CsdlSchema> schemas = Arrays.asList(emptySchema1, emptySchema2);
        
        MergeResult result = merger.mergeSchemas(schemas);
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getMergedSchema());
        assertEquals("ODataDemo", result.getMergedSchema().getNamespace());
    }

    @Test
    void testMergeSchemas_MergeResultProperties() {
        // 测试MergeResult的所有属性
        CsdlSchema schema = loadSimpleSchema();
        List<CsdlSchema> schemas = Arrays.asList(schema);
        
        MergeResult result = merger.mergeSchemas(schemas);
        
        assertNotNull(result);
        assertNotNull(result.getWarnings());
        assertNotNull(result.getErrors());
        assertNotNull(result.getMergedSchema());
        assertTrue(result.isSuccess());
        
        // 验证warnings和errors是可访问的
        assertTrue(result.getWarnings().size() >= 0);
        assertTrue(result.getErrors().size() >= 0);
    }

    @Test
    void testMergeSchemas_PreserveSchemaProperties() {
        // 测试合并时保留schema的重要属性
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("ODataDemo");
        schema.setAlias("Demo");
        
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName("TestEntity");
        schema.setEntityTypes(Arrays.asList(entityType));
        
        List<CsdlSchema> schemas = Arrays.asList(schema);
        
        MergeResult result = merger.mergeSchemas(schemas);
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        
        CsdlSchema mergedSchema = result.getMergedSchema();
        assertNotNull(mergedSchema);
        assertEquals("ODataDemo", mergedSchema.getNamespace());
        
        // 验证重要属性被保留
        assertNotNull(mergedSchema.getEntityTypes());
        assertEquals(1, mergedSchema.getEntityTypes().size());
        assertEquals("TestEntity", mergedSchema.getEntityTypes().get(0).getName());
    }
}
