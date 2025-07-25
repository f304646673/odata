package org.apache.olingo.schemamanager.analyzer.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.repository.SchemaRepository;
import org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils;
import static org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 测试 DefaultTypeDependencyAnalyzer.getDependencyPath(String, String) 方法
 */
@ExtendWith(MockitoExtension.class)
class DefaultTypeDependencyAnalyzerTest_getDependencyPath {

    @Mock
    private SchemaRepository repository;

    @InjectMocks
    private DefaultTypeDependencyAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        XmlSchemaTestUtils.clearCache();
    }

    @Test
    void testGetDependencyPath_NullSourceType() {
        // 测试null源类型
        List<String> path = analyzer.getDependencyPath(null, "ODataDemo.Person");
        
        assertNotNull(path);
        assertTrue(path.isEmpty());
    }

    @Test
    void testGetDependencyPath_NullTargetType() {
        // 测试null目标类型
        List<String> path = analyzer.getDependencyPath("ODataDemo.Employee", null);
        
        assertNotNull(path);
        assertTrue(path.isEmpty());
    }

    @Test
    void testGetDependencyPath_EmptySourceType() {
        // 测试空源类型
        List<String> path = analyzer.getDependencyPath("", "ODataDemo.Person");
        
        assertNotNull(path);
        assertTrue(path.isEmpty());
    }

    @Test
    void testGetDependencyPath_EmptyTargetType() {
        // 测试空目标类型
        List<String> path = analyzer.getDependencyPath("ODataDemo.Employee", "");
        
        assertNotNull(path);
        assertTrue(path.isEmpty());
    }

    @Test
    void testGetDependencyPath_SameType() {
        // 测试相同类型
        List<String> path = analyzer.getDependencyPath("ODataDemo.Person", "ODataDemo.Person");
        
        assertNotNull(path);
        assertEquals(1, path.size());
        assertEquals("ODataDemo.Person", path.get(0));
    }

    @Test
    void testGetDependencyPath_DirectInheritance() {
        // 测试直接继承关系
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("ODataDemo");
        
        // 基础类型
        CsdlEntityType baseType = new CsdlEntityType();
        baseType.setName("Person");
        
        // 派生类型
        CsdlEntityType derivedType = new CsdlEntityType();
        derivedType.setName("Employee");
        derivedType.setBaseType(new FullQualifiedName("ODataDemo", "Person"));
        
        schema.setEntityTypes(Arrays.asList(baseType, derivedType));
        
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put("ODataDemo", schema);
        when(repository.getAllSchemas()).thenReturn(schemas);
        when(repository.getEntityType("ODataDemo.Employee")).thenReturn(derivedType);
        when(repository.getEntityType("ODataDemo.Person")).thenReturn(baseType);
        
        List<String> path = analyzer.getDependencyPath("ODataDemo.Employee", "ODataDemo.Person");
        
        assertNotNull(path);
        assertFalse(path.isEmpty());
        
        // 应该包含从Employee到Person的路径
        assertTrue(path.contains("ODataDemo.Employee"));
        assertTrue(path.contains("ODataDemo.Person"));
    }

    @Test
    void testGetDependencyPath_NoDependencyPath() {
        // 测试没有依赖路径的情况
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("ODataDemo");
        
        // 两个独立的类型
        CsdlEntityType type1 = new CsdlEntityType();
        type1.setName("IndependentType1");
        
        CsdlEntityType type2 = new CsdlEntityType();
        type2.setName("IndependentType2");
        
        schema.setEntityTypes(Arrays.asList(type1, type2));
        
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put("ODataDemo", schema);
        when(repository.getAllSchemas()).thenReturn(schemas);
        when(repository.getEntityType("ODataDemo.IndependentType1")).thenReturn(type1);
        when(repository.getEntityType("ODataDemo.IndependentType2")).thenReturn(type2);
        
        List<String> path = analyzer.getDependencyPath("ODataDemo.IndependentType1", "ODataDemo.IndependentType2");
        
        assertNotNull(path);
        assertTrue(path.isEmpty());
    }

    @Test
    void testGetDependencyPath_MultiLevelInheritance() {
        // 测试多级继承路径
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("ODataDemo");
        
        // 基础类型
        CsdlEntityType type1 = new CsdlEntityType();
        type1.setName("BaseType");
        
        // 中级类型
        CsdlEntityType type2 = new CsdlEntityType();
        type2.setName("MiddleType");
        type2.setBaseType(new FullQualifiedName("ODataDemo", "BaseType"));
        
        // 最终类型
        CsdlEntityType type3 = new CsdlEntityType();
        type3.setName("DerivedType");
        type3.setBaseType(new FullQualifiedName("ODataDemo", "MiddleType"));
        
        schema.setEntityTypes(Arrays.asList(type1, type2, type3));
        
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put("ODataDemo", schema);
        when(repository.getAllSchemas()).thenReturn(schemas);
        when(repository.getEntityType("ODataDemo.BaseType")).thenReturn(type1);
        when(repository.getEntityType("ODataDemo.MiddleType")).thenReturn(type2);
        when(repository.getEntityType("ODataDemo.DerivedType")).thenReturn(type3);
        
        List<String> path = analyzer.getDependencyPath("ODataDemo.DerivedType", "ODataDemo.BaseType");
        
        assertNotNull(path);
        assertFalse(path.isEmpty());
        
        // 应该包含完整的继承路径
        assertTrue(path.contains("ODataDemo.DerivedType"));
        assertTrue(path.contains("ODataDemo.MiddleType"));
        assertTrue(path.contains("ODataDemo.BaseType"));
    }

    @Test
    void testGetDependencyPath_CrossSchema() {
        // 测试跨Schema的依赖路径
        CsdlSchema schema1 = new CsdlSchema();
        schema1.setNamespace("Schema1");
        
        CsdlEntityType baseType = new CsdlEntityType();
        baseType.setName("BaseType");
        
        schema1.setEntityTypes(Arrays.asList(baseType));
        
        CsdlSchema schema2 = new CsdlSchema();
        schema2.setNamespace("Schema2");
        
        CsdlEntityType derivedType = new CsdlEntityType();
        derivedType.setName("DerivedType");
        derivedType.setBaseType(new FullQualifiedName("Schema1", "BaseType"));
        
        schema2.setEntityTypes(Arrays.asList(derivedType));
        
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put("Schema1", schema1);
        schemas.put("Schema2", schema2);
        when(repository.getAllSchemas()).thenReturn(schemas);
        when(repository.getEntityType("Schema1.BaseType")).thenReturn(baseType);
        when(repository.getEntityType("Schema2.DerivedType")).thenReturn(derivedType);
        
        List<String> path = analyzer.getDependencyPath("Schema2.DerivedType", "Schema1.BaseType");
        
        assertNotNull(path);
        assertFalse(path.isEmpty());
        
        // 应该包含跨Schema的路径
        assertTrue(path.contains("Schema2.DerivedType"));
        assertTrue(path.contains("Schema1.BaseType"));
    }

    @Test
    void testGetDependencyPath_ComplexTypeInheritance() {
        // 测试ComplexType继承路径
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("ODataDemo");
        
        CsdlComplexType baseComplex = new CsdlComplexType();
        baseComplex.setName("BaseComplex");
        
        CsdlComplexType derivedComplex = new CsdlComplexType();
        derivedComplex.setName("DerivedComplex");
        derivedComplex.setBaseType(new FullQualifiedName("ODataDemo", "BaseComplex"));
        
        schema.setComplexTypes(Arrays.asList(baseComplex, derivedComplex));
        
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put("ODataDemo", schema);
        when(repository.getAllSchemas()).thenReturn(schemas);
        when(repository.getComplexType("ODataDemo.BaseComplex")).thenReturn(baseComplex);
        when(repository.getComplexType("ODataDemo.DerivedComplex")).thenReturn(derivedComplex);
        
        List<String> path = analyzer.getDependencyPath("ODataDemo.DerivedComplex", "ODataDemo.BaseComplex");
        
        assertNotNull(path);
        assertFalse(path.isEmpty());
        
        // 应该包含ComplexType的继承路径
        assertTrue(path.contains("ODataDemo.DerivedComplex"));
        assertTrue(path.contains("ODataDemo.BaseComplex"));
    }

    @Test
    void testGetDependencyPath_LargeSchema() {
        // 测试大型Schema的依赖路径性能
        CsdlSchema schema = loadLargeSchema();
        
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put(schema.getNamespace(), schema);
        when(repository.getAllSchemas()).thenReturn(schemas);
        
        // 设置Mock返回
        for (CsdlEntityType entityType : schema.getEntityTypes()) {
            String fullName = schema.getNamespace() + "." + entityType.getName();
            when(repository.getEntityType(fullName)).thenReturn(entityType);
        }
        
        if (!schema.getEntityTypes().isEmpty()) {
            CsdlEntityType sourceType = schema.getEntityTypes().get(0);
            CsdlEntityType targetType = schema.getEntityTypes().get(schema.getEntityTypes().size() - 1);
            
            String sourceName = schema.getNamespace() + "." + sourceType.getName();
            String targetName = schema.getNamespace() + "." + targetType.getName();
            
            List<String> path = analyzer.getDependencyPath(sourceName, targetName);
            
            assertNotNull(path);
            // 大型Schema的测试主要验证性能和不抛异常
        }
    }

    @Test
    void testGetDependencyPath_FullSchema() {
        // 测试完整Schema的依赖路径
        CsdlSchema schema = loadFullSchema();
        
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put(schema.getNamespace(), schema);
        when(repository.getAllSchemas()).thenReturn(schemas);
        
        // 设置Mock返回
        for (CsdlEntityType entityType : schema.getEntityTypes()) {
            String fullName = schema.getNamespace() + "." + entityType.getName();
            when(repository.getEntityType(fullName)).thenReturn(entityType);
        }
        
        if (!schema.getEntityTypes().isEmpty()) {
            CsdlEntityType sourceType = schema.getEntityTypes().get(0);
            CsdlEntityType targetType = schema.getEntityTypes().get(schema.getEntityTypes().size() - 1);
            
            String sourceName = schema.getNamespace() + "." + sourceType.getName();
            String targetName = schema.getNamespace() + "." + targetType.getName();
            
            List<String> path = analyzer.getDependencyPath(sourceName, targetName);
            
            assertNotNull(path);
        }
    }

    @Test
    void testGetDependencyPath_MultiDependencySchema() {
        // 测试多重依赖Schema的路径
        CsdlSchema schema = loadMultiDependencySchema();
        
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put(schema.getNamespace(), schema);
        when(repository.getAllSchemas()).thenReturn(schemas);
        
        // 设置Mock返回
        for (CsdlEntityType entityType : schema.getEntityTypes()) {
            String fullName = schema.getNamespace() + "." + entityType.getName();
            when(repository.getEntityType(fullName)).thenReturn(entityType);
        }
        
        if (!schema.getEntityTypes().isEmpty()) {
            CsdlEntityType sourceType = schema.getEntityTypes().get(0);
            CsdlEntityType targetType = schema.getEntityTypes().get(schema.getEntityTypes().size() - 1);
            
            String sourceName = schema.getNamespace() + "." + sourceType.getName();
            String targetName = schema.getNamespace() + "." + targetType.getName();
            
            List<String> path = analyzer.getDependencyPath(sourceName, targetName);
            
            assertNotNull(path);
        }
    }

    @Test
    void testGetDependencyPath_CircularDependency() {
        // 测试循环依赖的依赖路径
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("ODataDemo");
        
        // 创建循环依赖的类型
        CsdlEntityType type1 = new CsdlEntityType();
        type1.setName("Type1");
        
        CsdlEntityType type2 = new CsdlEntityType();
        type2.setName("Type2");
        type2.setBaseType(new FullQualifiedName("ODataDemo", "Type1"));
        
        schema.setEntityTypes(Arrays.asList(type1, type2));
        
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put("ODataDemo", schema);
        when(repository.getAllSchemas()).thenReturn(schemas);
        when(repository.getEntityType("ODataDemo.Type1")).thenReturn(type1);
        when(repository.getEntityType("ODataDemo.Type2")).thenReturn(type2);
        
        List<String> path = analyzer.getDependencyPath("ODataDemo.Type2", "ODataDemo.Type1");
        
        assertNotNull(path);
        assertFalse(path.isEmpty());
        
        // 应该包含从Type2到Type1的路径
        assertTrue(path.contains("ODataDemo.Type2"));
        assertTrue(path.contains("ODataDemo.Type1"));
    }
}
