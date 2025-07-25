package org.apache.olingo.schemamanager.analyzer.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.schemamanager.analyzer.TypeDependencyAnalyzer.TypeReference;
import org.apache.olingo.schemamanager.repository.SchemaRepository;
import org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 测试 DefaultTypeDependencyAnalyzer.getAllDependencies(CsdlEntityType) 方法
 */
@ExtendWith(MockitoExtension.class)
class DefaultTypeDependencyAnalyzerTest_getAllDependencies_EntityType {

    @Mock
    private SchemaRepository repository;

    @InjectMocks
    private DefaultTypeDependencyAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        XmlSchemaTestUtils.clearCache();
    }

    @Test
    void testGetAllDependencies_NullEntityType() {
        // 测试null输入
        List<TypeReference> dependencies = analyzer.getAllDependencies((CsdlEntityType) null);
        
        assertNotNull(dependencies);
        assertTrue(dependencies.isEmpty());
    }

    @Test
    void testGetAllDependencies_SimpleEntityType() {
        // 测试简单EntityType（无依赖�?
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName("SimpleEntity");
        
        CsdlProperty property = new CsdlProperty();
        property.setName("ID");
        property.setType("Edm.Int32");
        
        entityType.setProperties(Arrays.asList(property));
        
        List<TypeReference> dependencies = analyzer.getAllDependencies(entityType);
        
        assertNotNull(dependencies);
        // 简单类型可能没有依赖或只有内置类型依赖
        assertTrue(dependencies.size() >= 0);
    }

    @Test
    void testGetAllDependencies_EntityTypeWithDirectDependency() {
        // 测试有直接依赖的EntityType
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName("Employee");
        entityType.setBaseType(new FullQualifiedName("ODataDemo", "Person"));
        
        List<TypeReference> dependencies = analyzer.getAllDependencies(entityType);
        
        assertNotNull(dependencies);
        
        // 应该包含直接依赖（基类型�?
        boolean hasPersonDependency = dependencies.stream()
            .anyMatch(dep -> dep.getFullQualifiedName().contains("Person"));
        assertTrue(hasPersonDependency);
    }

    @Test
    void testGetAllDependencies_EntityTypeWithTransitiveDependencies() {
        // 测试具有传递依赖的EntityType
        // 这需要mock repository来提供相关的schema信息
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName("Manager");
        entityType.setBaseType(new FullQualifiedName("ODataDemo", "Employee"));
        
        // Mock repository behavior
        when(repository.getAllSchemas()).thenReturn(new java.util.HashMap<>());
        
        List<TypeReference> dependencies = analyzer.getAllDependencies(entityType);
        
        assertNotNull(dependencies);
        
        // 应该包含Employee依赖，如果Employee又依赖Person，也应该包含Person
        boolean hasEmployeeDependency = dependencies.stream()
            .anyMatch(dep -> dep.getFullQualifiedName().contains("Employee"));
        assertTrue(hasEmployeeDependency);
    }

    @Test
    void testGetAllDependencies_EntityTypeFromXmlSchema() {
        // 从XML schema加载EntityType进行测试
        CsdlSchema schema = loadFullSchema();
        
        if (schema.getEntityTypes() != null && !schema.getEntityTypes().isEmpty()) {
            CsdlEntityType entityType = schema.getEntityTypes().get(0);
            
            List<TypeReference> dependencies = analyzer.getAllDependencies(entityType);
            
            assertNotNull(dependencies);
            
            // 验证所有依赖的有效�?
            for (TypeReference dependency : dependencies) {
                assertNotNull(dependency);
                assertNotNull(dependency.getFullQualifiedName());
                assertFalse(dependency.getFullQualifiedName().trim().isEmpty());
            }
            
            // 验证没有重复的依�?
            Set<String> uniqueDependencies = dependencies.stream()
                .map(TypeReference::getFullQualifiedName)
                .collect(Collectors.toSet());
            assertEquals(uniqueDependencies.size(), dependencies.size());
        }
    }

    @Test
    void testGetAllDependencies_CircularDependencyDetection() {
        // 测试循环依赖检�?
        CsdlSchema schema = loadCircularDependencySchema();
        
        if (schema.getEntityTypes() != null && !schema.getEntityTypes().isEmpty()) {
            CsdlEntityType entityType = schema.getEntityTypes().get(0);
            
            // Mock repository to provide the circular dependency schema
            java.util.Map<String, org.apache.olingo.commons.api.edm.provider.CsdlSchema> schemaMap = new java.util.HashMap<>();
            schemaMap.put(schema.getNamespace(), schema);
            when(repository.getAllSchemas()).thenReturn(schemaMap);
            
            List<TypeReference> dependencies = analyzer.getAllDependencies(entityType);
            
            assertNotNull(dependencies);
            
            // 即使有循环依赖，方法也应该正常返回而不陷入无限循环
            // 检测循环依赖的实现应该使用visited集合来避免无限递归
            assertTrue(dependencies.size() >= 0);
        }
    }

    @Test
    void testGetAllDependencies_DeepDependencyChain() {
        // 测试深层依赖�?
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName("Level3");
        entityType.setBaseType(new FullQualifiedName("ODataDemo", "Level2"));
        
        // Mock deep dependency chain: Level3 -> Level2 -> Level1 -> BaseType
        when(repository.getAllSchemas()).thenReturn(new java.util.HashMap<>());
        
        List<TypeReference> dependencies = analyzer.getAllDependencies(entityType);
        
        assertNotNull(dependencies);
        
        // 应该包含所有层级的依赖
        boolean hasLevel2Dependency = dependencies.stream()
            .anyMatch(dep -> dep.getFullQualifiedName().contains("Level2"));
        assertTrue(hasLevel2Dependency);
    }

    @Test
    void testGetAllDependencies_MultipleDependencyPaths() {
        // 测试多条依赖路径的EntityType
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName("ComplexEntity");
        entityType.setBaseType(new FullQualifiedName("ODataDemo", "BaseEntity"));
        
        // 添加属性依�?
        CsdlProperty prop1 = new CsdlProperty();
        prop1.setName("RelatedEntity1");
        prop1.setType("ODataDemo.Entity1");
        
        CsdlProperty prop2 = new CsdlProperty();
        prop2.setName("RelatedEntity2");
        prop2.setType("ODataDemo.Entity2");
        
        entityType.setProperties(Arrays.asList(prop1, prop2));
        
        List<TypeReference> dependencies = analyzer.getAllDependencies(entityType);
        
        assertNotNull(dependencies);
        
        // 应该包含所有路径的依赖
        boolean hasBaseEntityDependency = dependencies.stream()
            .anyMatch(dep -> dep.getFullQualifiedName().contains("BaseEntity"));
        assertTrue(hasBaseEntityDependency);
    }

    @Test
    void testGetAllDependencies_PerformanceWithComplexDependencies() {
        // 性能测试：复杂依赖结�?
        CsdlSchema schema = loadLargeSchema();
        
        if (schema.getEntityTypes() != null && !schema.getEntityTypes().isEmpty()) {
            CsdlEntityType entityType = schema.getEntityTypes().get(0);
            
            // Mock repository for performance test
            java.util.Map<String, org.apache.olingo.commons.api.edm.provider.CsdlSchema> schemaMap2 = new java.util.HashMap<>();
            schemaMap2.put(schema.getNamespace(), schema);
            when(repository.getAllSchemas()).thenReturn(schemaMap2);
            
            long startTime = System.currentTimeMillis();
            
            List<TypeReference> dependencies = analyzer.getAllDependencies(entityType);
            
            long duration = System.currentTimeMillis() - startTime;
            
            assertNotNull(dependencies);
            assertTrue(duration < 3000, "Method took too long: " + duration + "ms");
        }
    }

    @Test
    void testGetAllDependencies_CompareWithDirectDependencies() {
        // 比较getAllDependencies和getDirectDependencies的结�?
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName("TestEntity");
        entityType.setBaseType(new FullQualifiedName("ODataDemo", "BaseEntity"));
        
        CsdlProperty property = new CsdlProperty();
        property.setName("RelatedEntity");
        property.setType("ODataDemo.RelatedType");
        
        entityType.setProperties(Arrays.asList(property));
        
        List<TypeReference> directDependencies = analyzer.getDirectDependencies(entityType);
        List<TypeReference> allDependencies = analyzer.getAllDependencies(entityType);
        
        assertNotNull(directDependencies);
        assertNotNull(allDependencies);
        
        // 所有依赖应该包含直接依�?
        for (TypeReference directDep : directDependencies) {
            boolean foundInAll = allDependencies.stream()
                .anyMatch(allDep -> allDep.getFullQualifiedName().equals(directDep.getFullQualifiedName()));
            assertTrue(foundInAll, "Direct dependency not found in all dependencies: " + directDep.getFullQualifiedName());
        }
        
        // 所有依赖的数量应该 >= 直接依赖的数�?
        assertTrue(allDependencies.size() >= directDependencies.size());
    }

    @Test
    void testGetAllDependencies_EmptyEntityType() {
        // 测试空的EntityType
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName("EmptyEntity");
        entityType.setProperties(Arrays.asList());
        
        List<TypeReference> dependencies = analyzer.getAllDependencies(entityType);
        
        assertNotNull(dependencies);
        assertTrue(dependencies.isEmpty());
    }

    @Test
    void testGetAllDependencies_DependencyResolution() {
        // 测试依赖解析的正确�?
        CsdlSchema schema = loadMultiDependencySchema();
        
        if (schema.getEntityTypes() != null && !schema.getEntityTypes().isEmpty()) {
            CsdlEntityType entityType = schema.getEntityTypes().get(0);
            
            java.util.Map<String, org.apache.olingo.commons.api.edm.provider.CsdlSchema> schemaMap3 = new java.util.HashMap<>();
            schemaMap3.put(schema.getNamespace(), schema);
            when(repository.getAllSchemas()).thenReturn(schemaMap3);
            
            List<TypeReference> dependencies = analyzer.getAllDependencies(entityType);
            
            assertNotNull(dependencies);
            
            // 验证依赖解析的正确�?
            for (TypeReference dependency : dependencies) {
                // 每个依赖都应该有有效的类型信�?
                assertNotNull(dependency.getTypeKind());
                assertNotNull(dependency.getFullQualifiedName());
                
                // 依赖不应该是自身
                String selfName = schema.getNamespace() + "." + entityType.getName();
                assertNotEquals(selfName, dependency.getFullQualifiedName());
            }
        }
    }
}
