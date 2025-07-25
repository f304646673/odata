package org.apache.olingo.schemamanager.analyzer.impl;

import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.analyzer.TypeDependencyAnalyzer.TypeReference;
import org.apache.olingo.schemamanager.repository.SchemaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * 测试 DefaultTypeDependencyAnalyzer.getDependents() 方法
 */
class DefaultTypeDependencyAnalyzerTest_getDependents {

    @Mock
    private SchemaRepository repository;

    private DefaultTypeDependencyAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        analyzer = new DefaultTypeDependencyAnalyzer(repository);
    }

    @Test
    void testGetDependents_NullType() {
        // 测试空类型参数
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            analyzer.getDependents(null);
        });
        assertEquals("Type name cannot be null or empty", exception.getMessage());
    }

    @Test
    void testGetDependents_EmptyType() {
        // 测试空字符串类型参数
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            analyzer.getDependents("");
        });
        assertEquals("Type name cannot be null or empty", exception.getMessage());
    }

    @Test
    void testGetDependents_TypeNotFound() {
        // 测试不存在的类型
        Map<String, CsdlSchema> schemas = new HashMap<>();
        when(repository.getAllSchemas()).thenReturn(schemas);
        
        List<TypeReference> dependents = analyzer.getDependents("NonExistent.Type");
        
        assertNotNull(dependents);
        assertTrue(dependents.isEmpty());
    }

    @Test
    void testGetDependents_NoDependent() {
        // 测试没有依赖者的类型
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("ODataDemo");
        
        // 创建一个独立的实体类型
        CsdlEntityType independentType = new CsdlEntityType();
        independentType.setName("IndependentType");
        schema.setEntityTypes(Arrays.asList(independentType));
        
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put("ODataDemo", schema);
        when(repository.getAllSchemas()).thenReturn(schemas);
        
        List<TypeReference> dependents = analyzer.getDependents("ODataDemo.IndependentType");
        
        assertNotNull(dependents);
        assertTrue(dependents.isEmpty());
    }

    @Test
    void testGetDependents_TypeWithSingleDependent() {
        // 测试有单个依赖者的类型
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("ODataDemo");
        
        // 创建基础类型
        CsdlComplexType baseType = new CsdlComplexType();
        baseType.setName("BaseType");
        
        // 创建依赖类型（引用基础类型）
        CsdlComplexType dependentType = new CsdlComplexType();
        dependentType.setName("DependentType");
        CsdlProperty property = new CsdlProperty();
        property.setName("BaseProperty");
        property.setType("ODataDemo.BaseType");
        dependentType.setProperties(Arrays.asList(property));
        
        schema.setComplexTypes(Arrays.asList(baseType, dependentType));
        
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put("ODataDemo", schema);
        when(repository.getAllSchemas()).thenReturn(schemas);
        
        List<TypeReference> dependents = analyzer.getDependents("ODataDemo.BaseType");
        
        assertNotNull(dependents);
        assertEquals(1, dependents.size());
        assertEquals("ODataDemo.DependentType", dependents.get(0).getFullQualifiedName());
    }

    @Test
    void testGetDependents_TypeWithMultipleDependents() {
        // 测试有多个依赖者的类型
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("ODataDemo");
        
        // 创建基础类型
        CsdlComplexType baseType = new CsdlComplexType();
        baseType.setName("BaseType");
        
        // 创建第一个依赖类型
        CsdlComplexType dependent1 = new CsdlComplexType();
        dependent1.setName("Dependent1");
        CsdlProperty prop1 = new CsdlProperty();
        prop1.setName("BaseProperty1");
        prop1.setType("ODataDemo.BaseType");
        dependent1.setProperties(Arrays.asList(prop1));
        
        // 创建第二个依赖类型
        CsdlComplexType dependent2 = new CsdlComplexType();
        dependent2.setName("Dependent2");
        CsdlProperty prop2 = new CsdlProperty();
        prop2.setName("BaseProperty2");
        prop2.setType("ODataDemo.BaseType");
        dependent2.setProperties(Arrays.asList(prop2));
        
        schema.setComplexTypes(Arrays.asList(baseType, dependent1, dependent2));
        
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put("ODataDemo", schema);
        when(repository.getAllSchemas()).thenReturn(schemas);
        
        List<TypeReference> dependents = analyzer.getDependents("ODataDemo.BaseType");
        
        assertNotNull(dependents);
        assertEquals(2, dependents.size());
        
        List<String> dependentNames = new ArrayList<>();
        for (TypeReference ref : dependents) {
            dependentNames.add(ref.getFullQualifiedName());
        }
        
        assertTrue(dependentNames.contains("ODataDemo.Dependent1"));
        assertTrue(dependentNames.contains("ODataDemo.Dependent2"));
    }

    @Test
    void testGetDependents_EntityWithNavigation() {
        // 测试实体类型间的导航依赖
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("ODataDemo");
        
        // 创建被引用的实体类型
        CsdlEntityType targetEntity = new CsdlEntityType();
        targetEntity.setName("TargetEntity");
        CsdlProperty keyProp = new CsdlProperty();
        keyProp.setName("ID");
        keyProp.setType("Edm.String");
        targetEntity.setProperties(Arrays.asList(keyProp));
        CsdlPropertyRef keyRef = new CsdlPropertyRef();
        keyRef.setName("ID");
        targetEntity.setKey(Arrays.asList(keyRef));
        
        // 创建引用的实体类型
        CsdlEntityType sourceEntity = new CsdlEntityType();
        sourceEntity.setName("SourceEntity");
        CsdlProperty sourceProp = new CsdlProperty();
        sourceProp.setName("TargetRef");
        sourceProp.setType("ODataDemo.TargetEntity");
        sourceEntity.setProperties(Arrays.asList(sourceProp));
        
        schema.setEntityTypes(Arrays.asList(targetEntity, sourceEntity));
        
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put("ODataDemo", schema);
        when(repository.getAllSchemas()).thenReturn(schemas);
        
        List<TypeReference> dependents = analyzer.getDependents("ODataDemo.TargetEntity");
        
        assertNotNull(dependents);
        assertEquals(1, dependents.size());
        assertEquals("ODataDemo.SourceEntity", dependents.get(0).getFullQualifiedName());
    }

    @Test
    void testGetDependents_CrossSchemaReference() {
        // 测试跨 Schema 的依赖关系
        CsdlSchema schema1 = new CsdlSchema();
        schema1.setNamespace("Schema1");
        
        CsdlComplexType baseType = new CsdlComplexType();
        baseType.setName("BaseType");
        schema1.setComplexTypes(Arrays.asList(baseType));
        
        CsdlSchema schema2 = new CsdlSchema();
        schema2.setNamespace("Schema2");
        
        CsdlComplexType dependentType = new CsdlComplexType();
        dependentType.setName("DependentType");
        CsdlProperty property = new CsdlProperty();
        property.setName("BaseProperty");
        property.setType("Schema1.BaseType");
        dependentType.setProperties(Arrays.asList(property));
        schema2.setComplexTypes(Arrays.asList(dependentType));
        
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put("Schema1", schema1);
        schemas.put("Schema2", schema2);
        when(repository.getAllSchemas()).thenReturn(schemas);
        
        List<TypeReference> dependents = analyzer.getDependents("Schema1.BaseType");
        
        assertNotNull(dependents);
        assertEquals(1, dependents.size());
        assertEquals("Schema2.DependentType", dependents.get(0).getFullQualifiedName());
    }

    @Test
    void testGetDependents_IndirectDependency() {
        // 测试间接依赖（A -> B -> C, 查询 C 的依赖者应该只返回 B）
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("ODataDemo");
        
        // 创建类型 C
        CsdlComplexType typeC = new CsdlComplexType();
        typeC.setName("TypeC");
        
        // 创建类型 B（依赖 C）
        CsdlComplexType typeB = new CsdlComplexType();
        typeB.setName("TypeB");
        CsdlProperty propB = new CsdlProperty();
        propB.setName("PropC");
        propB.setType("ODataDemo.TypeC");
        typeB.setProperties(Arrays.asList(propB));
        
        // 创建类型 A（依赖 B）
        CsdlComplexType typeA = new CsdlComplexType();
        typeA.setName("TypeA");
        CsdlProperty propA = new CsdlProperty();
        propA.setName("PropB");
        propA.setType("ODataDemo.TypeB");
        typeA.setProperties(Arrays.asList(propA));
        
        schema.setComplexTypes(Arrays.asList(typeA, typeB, typeC));
        
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put("ODataDemo", schema);
        when(repository.getAllSchemas()).thenReturn(schemas);
        
        // 查询 TypeC 的直接依赖者
        List<TypeReference> dependents = analyzer.getDependents("ODataDemo.TypeC");
        
        assertNotNull(dependents);
        assertEquals(1, dependents.size());
        assertEquals("ODataDemo.TypeB", dependents.get(0).getFullQualifiedName());
    }

    @Test
    void testGetDependents_CircularReference() {
        // 测试循环引用（A -> B -> A）
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("ODataDemo");
        
        // 创建类型 A
        CsdlComplexType typeA = new CsdlComplexType();
        typeA.setName("TypeA");
        CsdlProperty propA = new CsdlProperty();
        propA.setName("PropB");
        propA.setType("ODataDemo.TypeB");
        typeA.setProperties(Arrays.asList(propA));
        
        // 创建类型 B（引用 A）
        CsdlComplexType typeB = new CsdlComplexType();
        typeB.setName("TypeB");
        CsdlProperty propB = new CsdlProperty();
        propB.setName("PropA");
        propB.setType("ODataDemo.TypeA");
        typeB.setProperties(Arrays.asList(propB));
        
        schema.setComplexTypes(Arrays.asList(typeA, typeB));
        
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put("ODataDemo", schema);
        when(repository.getAllSchemas()).thenReturn(schemas);
        
        // 查询 TypeA 的依赖者
        List<TypeReference> dependentsA = analyzer.getDependents("ODataDemo.TypeA");
        assertNotNull(dependentsA);
        assertEquals(1, dependentsA.size());
        assertEquals("ODataDemo.TypeB", dependentsA.get(0).getFullQualifiedName());
        
        // 查询 TypeB 的依赖者
        List<TypeReference> dependentsB = analyzer.getDependents("ODataDemo.TypeB");
        assertNotNull(dependentsB);
        assertEquals(1, dependentsB.size());
        assertEquals("ODataDemo.TypeA", dependentsB.get(0).getFullQualifiedName());
    }
}
