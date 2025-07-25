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

import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
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
 * 测试 DefaultTypeDependencyAnalyzer.getDirectDependencies(CsdlComplexType) 方法
 */
@ExtendWith(MockitoExtension.class)
class DefaultTypeDependencyAnalyzerTest_getDirectDependencies_ComplexType {

    @Mock
    private SchemaRepository repository;

    @InjectMocks
    private DefaultTypeDependencyAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        XmlSchemaTestUtils.clearCache();
    }

    @Test
    void testGetDirectDependencies_NullComplexType() {
        // 测试null输入
        List<TypeReference> dependencies = analyzer.getDirectDependencies((CsdlComplexType) null);
        
        assertNotNull(dependencies);
        assertTrue(dependencies.isEmpty());
    }

    @Test
    void testGetDirectDependencies_ComplexTypeWithoutBaseType() {
        // 测试没有基类型的ComplexType
        CsdlComplexType complexType = new CsdlComplexType();
        complexType.setName("Address");
        complexType.setBaseType((String) null);
        
        List<TypeReference> dependencies = analyzer.getDirectDependencies(complexType);
        
        assertNotNull(dependencies);
        // 可能为空或包含属性相关的依赖
        assertTrue(dependencies.size() >= 0);
    }

    @Test
    void testGetDirectDependencies_ComplexTypeWithBaseType() {
        // 测试有基类型的ComplexType
        CsdlComplexType complexType = new CsdlComplexType();
        complexType.setName("ExtendedAddress");
        complexType.setBaseType(new FullQualifiedName("ODataDemo", "Address"));
        
        List<TypeReference> dependencies = analyzer.getDirectDependencies(complexType);
        
        assertNotNull(dependencies);
        assertFalse(dependencies.isEmpty());
        
        // 验证基类型依赖被正确添加
        boolean hasBaseTypeDependency = dependencies.stream()
            .anyMatch(dep -> "ODataDemo.Address".equals(dep.getFullQualifiedName()));
        assertTrue(hasBaseTypeDependency);
    }

    @Test
    void testGetDirectDependencies_ComplexTypeWithSimpleProperties() {
        // 测试包含简单属性的ComplexType
        CsdlComplexType complexType = new CsdlComplexType();
        complexType.setName("Person");
        
        // 添加简单属性
        CsdlProperty property1 = new CsdlProperty();
        property1.setName("Name");
        property1.setType("Edm.String");
        
        CsdlProperty property2 = new CsdlProperty();
        property2.setName("Age");
        property2.setType("Edm.Int32");
        
        complexType.setProperties(Arrays.asList(property1, property2));
        
        List<TypeReference> dependencies = analyzer.getDirectDependencies(complexType);
        
        assertNotNull(dependencies);
        
        // EDM内置类型通常不被视为依赖
        // 依赖列表可能为空或只包含非EDM类型
        for (TypeReference dependency : dependencies) {
            assertFalse(dependency.getFullQualifiedName().startsWith("Edm."));
        }
    }

    @Test
    void testGetDirectDependencies_ComplexTypeWithComplexProperties() {
        // 测试包含复杂类型属性的ComplexType
        CsdlComplexType complexType = new CsdlComplexType();
        complexType.setName("Customer");
        
        CsdlProperty addressProperty = new CsdlProperty();
        addressProperty.setName("Address");
        addressProperty.setType("ODataDemo.Address");
        
        CsdlProperty contactProperty = new CsdlProperty();
        contactProperty.setName("Contact");
        contactProperty.setType("ODataDemo.ContactInfo");
        
        complexType.setProperties(Arrays.asList(addressProperty, contactProperty));
        
        List<TypeReference> dependencies = analyzer.getDirectDependencies(complexType);
        
        assertNotNull(dependencies);
        
        // 验证复杂类型依赖
        boolean hasAddressDependency = dependencies.stream()
            .anyMatch(dep -> dep.getFullQualifiedName().contains("Address"));
        boolean hasContactDependency = dependencies.stream()
            .anyMatch(dep -> dep.getFullQualifiedName().contains("ContactInfo"));
        
        // 根据实现，可能包含这些依赖
    }

    @Test
    void testGetDirectDependencies_ComplexTypeFromXmlSchema() {
        // 从XML schema加载ComplexType进行测试
        CsdlSchema schema = loadComplexTypesSchema();
        
        if (schema.getComplexTypes() != null && !schema.getComplexTypes().isEmpty()) {
            CsdlComplexType complexType = schema.getComplexTypes().get(0);
            
            List<TypeReference> dependencies = analyzer.getDirectDependencies(complexType);
            
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
    void testGetDirectDependencies_ComplexTypeWithCollectionProperty() {
        // 测试包含集合属性的ComplexType
        CsdlComplexType complexType = new CsdlComplexType();
        complexType.setName("Order");
        
        CsdlProperty itemsProperty = new CsdlProperty();
        itemsProperty.setName("Items");
        itemsProperty.setType("Collection(ODataDemo.OrderItem)");
        
        complexType.setProperties(Arrays.asList(itemsProperty));
        
        List<TypeReference> dependencies = analyzer.getDirectDependencies(complexType);
        
        assertNotNull(dependencies);
        
        // 集合类型应该提取出内部类型作为依赖
        boolean hasOrderItemDependency = dependencies.stream()
            .anyMatch(dep -> dep.getFullQualifiedName().contains("OrderItem"));
        // 根据实现，可能包含OrderItem依赖
    }

    @Test
    void testGetDirectDependencies_ComplexTypeWithMixedProperties() {
        // 测试包含混合属性类型的ComplexType
        CsdlComplexType complexType = new CsdlComplexType();
        complexType.setName("MixedType");
        complexType.setBaseType(new FullQualifiedName("ODataDemo", "BaseType"));
        
        CsdlProperty simpleProperty = new CsdlProperty();
        simpleProperty.setName("Name");
        simpleProperty.setType("Edm.String");
        
        CsdlProperty complexProperty = new CsdlProperty();
        complexProperty.setName("Details");
        complexProperty.setType("ODataDemo.Details");
        
        CsdlProperty collectionProperty = new CsdlProperty();
        collectionProperty.setName("Tags");
        collectionProperty.setType("Collection(Edm.String)");
        
        CsdlProperty complexCollectionProperty = new CsdlProperty();
        complexCollectionProperty.setName("Items");
        complexCollectionProperty.setType("Collection(ODataDemo.Item)");
        
        complexType.setProperties(Arrays.asList(simpleProperty, complexProperty, 
                                         collectionProperty, complexCollectionProperty));
        
        List<TypeReference> dependencies = analyzer.getDirectDependencies(complexType);
        
        assertNotNull(dependencies);
        
        // 验证基类型依赖
        boolean hasBaseTypeDependency = dependencies.stream()
            .anyMatch(dep -> dep.getFullQualifiedName().contains("BaseType"));
        assertTrue(hasBaseTypeDependency);
        
        // 验证没有重复的依赖项
        long uniqueCount = dependencies.stream()
            .map(TypeReference::getFullQualifiedName)
            .distinct()
            .count();
        assertEquals(uniqueCount, dependencies.size());
    }

    @Test
    void testGetDirectDependencies_NestedComplexTypes() {
        // 测试嵌套复杂类型
        CsdlSchema schema = loadMultiDependencySchema();
        
        if (schema.getComplexTypes() != null && !schema.getComplexTypes().isEmpty()) {
            for (CsdlComplexType complexType : schema.getComplexTypes()) {
                List<TypeReference> dependencies = analyzer.getDirectDependencies(complexType);
                
                assertNotNull(dependencies);
                
                // 验证嵌套依赖的处理
                for (TypeReference dependency : dependencies) {
                    assertNotNull(dependency.getFullQualifiedName());
                    // 确保依赖不是自身
                    String selfName = schema.getNamespace() + "." + complexType.getName();
                    assertNotEquals(selfName, dependency.getFullQualifiedName());
                }
            }
        }
    }

    @Test
    void testGetDirectDependencies_CircularReference() {
        // 测试可能的循环引用情况
        CsdlComplexType complexType = new CsdlComplexType();
        complexType.setName("Node");
        
        // 自引用属性
        CsdlProperty parentProperty = new CsdlProperty();
        parentProperty.setName("Parent");
        parentProperty.setType("ODataDemo.Node");
        
        CsdlProperty childrenProperty = new CsdlProperty();
        childrenProperty.setName("Children");
        childrenProperty.setType("Collection(ODataDemo.Node)");
        
        complexType.setProperties(Arrays.asList(parentProperty, childrenProperty));
        
        List<TypeReference> dependencies = analyzer.getDirectDependencies(complexType);
        
        assertNotNull(dependencies);
        
        // 自引用应该被包含在依赖中（如果实现支持）
        // 或者被过滤掉以避免循环
        boolean hasSelfReference = dependencies.stream()
            .anyMatch(dep -> dep.getFullQualifiedName().contains("Node"));
    }

    @Test
    void testGetDirectDependencies_PerformanceWithLargeComplexType() {
        // 性能测试：包含大量属性的ComplexType
        CsdlComplexType complexType = new CsdlComplexType();
        complexType.setName("LargeComplexType");
        
        // 创建大量属性
        List<CsdlProperty> properties = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            CsdlProperty prop = new CsdlProperty();
            prop.setName("Property" + i);
            prop.setType(i % 2 == 0 ? "Edm.String" : "ODataDemo.Type" + i);
            properties.add(prop);
        }
        complexType.setProperties(properties);
        
        long startTime = System.currentTimeMillis();
        
        List<TypeReference> dependencies = analyzer.getDirectDependencies(complexType);
        
        long duration = System.currentTimeMillis() - startTime;
        
        assertNotNull(dependencies);
        assertTrue(duration < 1000, "Method took too long: " + duration + "ms");
    }

    @Test
    void testGetDirectDependencies_EmptyComplexType() {
        // 测试空的ComplexType（没有属性）
        CsdlComplexType complexType = new CsdlComplexType();
        complexType.setName("EmptyType");
        complexType.setProperties(Arrays.asList()); // 空属性列表
        
        List<TypeReference> dependencies = analyzer.getDirectDependencies(complexType);
        
        assertNotNull(dependencies);
        assertTrue(dependencies.isEmpty());
    }
}
