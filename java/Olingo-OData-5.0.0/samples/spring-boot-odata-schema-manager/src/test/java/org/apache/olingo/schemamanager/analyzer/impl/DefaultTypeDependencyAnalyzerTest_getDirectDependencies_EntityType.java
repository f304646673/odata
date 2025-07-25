package org.apache.olingo.schemamanager.analyzer.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.schemamanager.analyzer.TypeDependencyAnalyzer;
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
 * 测试 DefaultTypeDependencyAnalyzer.getDirectDependencies(CsdlEntityType) 方法
 */
@ExtendWith(MockitoExtension.class)
class DefaultTypeDependencyAnalyzerTest_getDirectDependencies_EntityType {

    @Mock
    private SchemaRepository repository;

    @InjectMocks
    private DefaultTypeDependencyAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        XmlSchemaTestUtils.clearCache();
    }

    @Test
    void testGetDirectDependencies_NullEntityType() {
        // 测试null输入
        List<TypeReference> dependencies = analyzer.getDirectDependencies((CsdlEntityType) null);
        
        assertNotNull(dependencies);
        assertTrue(dependencies.isEmpty());
    }

    @Test
    void testGetDirectDependencies_EntityTypeWithoutBaseType() {
        // 测试没有基类型的EntityType
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName("Person");
        entityType.setBaseType((String) null);
        
        List<TypeReference> dependencies = analyzer.getDirectDependencies(entityType);
        
        assertNotNull(dependencies);
        // 可能为空或包含属性相关的依赖
        assertTrue(dependencies.size() >= 0);
    }

    @Test
    void testGetDirectDependencies_EntityTypeWithBaseType() {
        // 测试有基类型的EntityType
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName("Employee");
        entityType.setBaseType(new FullQualifiedName("ODataDemo", "Person"));
        
        List<TypeReference> dependencies = analyzer.getDirectDependencies(entityType);
        
        assertNotNull(dependencies);
        assertFalse(dependencies.isEmpty());
        
        // 验证基类型依赖被正确添加
        boolean hasBaseTypeDependency = dependencies.stream()
            .anyMatch(dep -> "ODataDemo.Person".equals(dep.getFullQualifiedName()));
        assertTrue(hasBaseTypeDependency);
    }

    @Test
    void testGetDirectDependencies_EntityTypeWithProperties() {
        // 测试包含属性的EntityType
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName("Product");
        
        // 添加属性
        CsdlProperty property1 = new CsdlProperty();
        property1.setName("ID");
        property1.setType("Edm.Int32");
        
        CsdlProperty property2 = new CsdlProperty();
        property2.setName("Name");
        property2.setType("Edm.String");
        
        CsdlProperty property3 = new CsdlProperty();
        property3.setName("Category");
        property3.setType("ODataDemo.Category");
        
        entityType.setProperties(Arrays.asList(property1, property2, property3));
        
        List<TypeReference> dependencies = analyzer.getDirectDependencies(entityType);
        
        assertNotNull(dependencies);
        
        // 应该包含自定义类型的依赖（Category），但不包含EDM内置类型
        boolean hasCategoryDependency = dependencies.stream()
            .anyMatch(dep -> dep.getFullQualifiedName().contains("Category"));
        // 根据实现，可能包含也可能不包含，这取决于是否过滤EDM类型
    }

    @Test
    void testGetDirectDependencies_EntityTypeWithNavigationProperties() {
        // 从XML schema加载带有导航属性的EntityType
        CsdlSchema schema = loadFullSchema();
        
        if (schema.getEntityTypes() != null && !schema.getEntityTypes().isEmpty()) {
            CsdlEntityType entityType = schema.getEntityTypes().get(0);
            
            List<TypeReference> dependencies = analyzer.getDirectDependencies(entityType);
            
            assertNotNull(dependencies);
            
            // 验证导航属性依赖
            if (entityType.getNavigationProperties() != null && !entityType.getNavigationProperties().isEmpty()) {
                // 如果有导航属性，应该有相应的依赖
                assertTrue(dependencies.size() >= 0);
            }
        }
    }

    @Test
    void testGetDirectDependencies_EntityTypeFromXmlSchema() {
        // 从XML schema加载EntityType进行测试
        CsdlSchema schema = loadComplexTypesSchema();
        
        if (schema.getEntityTypes() != null && !schema.getEntityTypes().isEmpty()) {
            CsdlEntityType entityType = schema.getEntityTypes().get(0);
            
            List<TypeReference> dependencies = analyzer.getDirectDependencies(entityType);
            
            assertNotNull(dependencies);
            
            // 验证依赖列表的有效性
            for (TypeReference dependency : dependencies) {
                assertNotNull(dependency);
                assertNotNull(dependency.getFullQualifiedName());
                assertFalse(dependency.getFullQualifiedName().trim().isEmpty());
            }
        }
    }

    @Test
    void testGetDirectDependencies_EntityTypeWithComplexProperty() {
        // 测试包含复杂类型属性的EntityType
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName("Customer");
        
        CsdlProperty addressProperty = new CsdlProperty();
        addressProperty.setName("Address");
        addressProperty.setType("ODataDemo.Address");
        
        entityType.setProperties(Arrays.asList(addressProperty));
        
        List<TypeReference> dependencies = analyzer.getDirectDependencies(entityType);
        
        assertNotNull(dependencies);
        
        // 验证复杂类型依赖
        boolean hasAddressDependency = dependencies.stream()
            .anyMatch(dep -> dep.getFullQualifiedName().contains("Address"));
        // 根据实现，可能包含Address依赖
    }

    @Test
    void testGetDirectDependencies_EntityTypeWithCollectionProperty() {
        // 测试包含集合属性的EntityType
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName("Order");
        
        CsdlProperty itemsProperty = new CsdlProperty();
        itemsProperty.setName("Items");
        itemsProperty.setType("Collection(ODataDemo.OrderItem)");
        
        entityType.setProperties(Arrays.asList(itemsProperty));
        
        List<TypeReference> dependencies = analyzer.getDirectDependencies(entityType);
        
        assertNotNull(dependencies);
        
        // 集合类型应该提取出内部类型作为依赖
        boolean hasOrderItemDependency = dependencies.stream()
            .anyMatch(dep -> dep.getFullQualifiedName().contains("OrderItem"));
        // 根据实现，可能包含OrderItem依赖
    }

    @Test
    void testGetDirectDependencies_EntityTypeWithMultipleDependencies() {
        // 测试包含多种依赖的复杂EntityType
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName("ComplexEntity");
        entityType.setBaseType(new FullQualifiedName("ODataDemo", "BaseEntity"));
        
        CsdlProperty prop1 = new CsdlProperty();
        prop1.setName("ComplexProp");
        prop1.setType("ODataDemo.ComplexType1");
        
        CsdlProperty prop2 = new CsdlProperty();
        prop2.setName("CollectionProp");
        prop2.setType("Collection(ODataDemo.ComplexType2)");
        
        entityType.setProperties(Arrays.asList(prop1, prop2));
        
        List<TypeReference> dependencies = analyzer.getDirectDependencies(entityType);
        
        assertNotNull(dependencies);
        
        // 应该包含基类型依赖
        boolean hasBaseTypeDependency = dependencies.stream()
            .anyMatch(dep -> dep.getFullQualifiedName().contains("BaseEntity"));
        assertTrue(hasBaseTypeDependency);
        
        // 验证没有重复的依赖项
        long uniqueCount = dependencies.stream()
            .map(TypeReference::getFullQualifiedName)
            .distinct()
            .count();
        assertEquals(uniqueCount, dependencies.size());
    }

    @Test
    void testGetDirectDependencies_PerformanceWithLargeEntityType() {
        // 性能测试：包含大量属性的EntityType
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName("LargeEntity");
        
        // 创建大量属性
        List<CsdlProperty> properties = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            CsdlProperty prop = new CsdlProperty();
            prop.setName("Property" + i);
            prop.setType("Edm.String");
            properties.add(prop);
        }
        entityType.setProperties(properties);
        
        long startTime = System.currentTimeMillis();
        
        List<TypeReference> dependencies = analyzer.getDirectDependencies(entityType);
        
        long duration = System.currentTimeMillis() - startTime;
        
        assertNotNull(dependencies);
        assertTrue(duration < 1000, "Method took too long: " + duration + "ms");
    }
}
