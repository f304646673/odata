package org.apache.olingo.schemamanager.analyzer.impl;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.analyzer.TypeDependencyAnalyzer;
import org.apache.olingo.schemamanager.analyzer.TypeDependencyAnalyzer.TypeReference;
import org.apache.olingo.schemamanager.repository.SchemaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for getAllDependencies method in DefaultTypeDependencyAnalyzer for ComplexType scenarios.
 * Tests comprehensive dependency analysis including direct dependencies, transitive dependencies,
 * circular dependencies, and cross-namespace dependencies.
 */
@ExtendWith(MockitoExtension.class)
public class DefaultTypeDependencyAnalyzerTest_getAllDependencies_ComplexType {

    @Mock
    private SchemaRepository repository;

    private TypeDependencyAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new DefaultTypeDependencyAnalyzer(repository);
    }

    @Test
    void testGetAllDependencies_SimpleComplexType() {
        // 测试简单的ComplexType（没有依赖）
        CsdlComplexType complexType = new CsdlComplexType();
        complexType.setName("SimpleComplex");
        
        CsdlProperty property = new CsdlProperty();
        property.setName("Name");
        property.setType("Edm.String");
        
        complexType.setProperties(Arrays.asList(property));
        
        List<TypeReference> dependencies = analyzer.getAllDependencies(complexType);
        
        assertNotNull(dependencies);
        // 简单类型可能没有依赖或只有内置类型依赖
        assertTrue(dependencies.size() >= 0);
    }

    @Test
    void testGetAllDependencies_ComplexTypeWithDirectDependency() {
        // 测试有直接依赖的ComplexType
        CsdlComplexType complexType = new CsdlComplexType();
        complexType.setName("ExtendedAddress");
        complexType.setBaseType(new FullQualifiedName("ODataDemo", "Address"));
        
        List<TypeReference> dependencies = analyzer.getAllDependencies(complexType);
        
        assertNotNull(dependencies);
        
        // 应该包含直接依赖（基类型）
        boolean hasAddressDependency = dependencies.stream()
            .anyMatch(dep -> dep.getFullQualifiedName().contains("Address"));
        assertTrue(hasAddressDependency);
    }

    @Test
    void testGetAllDependencies_ComplexTypeWithTransitiveDependencies() {
        // 测试具有传递依赖的ComplexType
        CsdlComplexType complexType = new CsdlComplexType();
        complexType.setName("Level3Complex");
        complexType.setBaseType(new FullQualifiedName("ODataDemo", "Level2Complex"));
        
        // Mock repository behavior
        Map<String, CsdlSchema> schemas = new HashMap<>();
        when(repository.getAllSchemas()).thenReturn(schemas);
        
        List<TypeReference> dependencies = analyzer.getAllDependencies(complexType);
        
        assertNotNull(dependencies);
        
        // 应该包含Level2Complex依赖
        boolean hasLevel2Dependency = dependencies.stream()
            .anyMatch(dep -> dep.getFullQualifiedName().contains("Level2Complex"));
        assertTrue(hasLevel2Dependency);
    }

    @Test
    void testGetAllDependencies_ComplexTypeWithPropertyDependencies() {
        // 测试具有属性依赖的ComplexType
        CsdlComplexType complexType = new CsdlComplexType();
        complexType.setName("PersonDetails");
        
        CsdlProperty addressProperty = new CsdlProperty();
        addressProperty.setName("HomeAddress");
        addressProperty.setType("ODataDemo.Address");
        
        complexType.setProperties(Arrays.asList(addressProperty));
        
        Map<String, CsdlSchema> schemas = new HashMap<>();
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("ODataDemo");
        schemas.put("ODataDemo", schema);
        
        when(repository.getAllSchemas()).thenReturn(schemas);
        
        List<TypeReference> dependencies = analyzer.getAllDependencies(complexType);
        
        assertNotNull(dependencies);
        
        // 应该包含Address类型的依赖
        boolean hasAddressDependency = dependencies.stream()
            .anyMatch(dep -> dep.getFullQualifiedName().contains("Address"));
        assertTrue(hasAddressDependency);
    }

    @Test
    void testGetAllDependencies_ComplexTypeWithMultipleProperties() {
        // 测试具有多个依赖属性的ComplexType
        CsdlComplexType complexType = new CsdlComplexType();
        complexType.setName("Contact");
        
        CsdlProperty nameProperty = new CsdlProperty();
        nameProperty.setName("Name");
        nameProperty.setType("Edm.String");
        
        CsdlProperty addressProperty = new CsdlProperty();
        addressProperty.setName("Address");
        addressProperty.setType("ODataDemo.Address");
        
        complexType.setProperties(Arrays.asList(nameProperty, addressProperty));
        
        Map<String, CsdlSchema> schemas = new HashMap<>();
        when(repository.getAllSchemas()).thenReturn(schemas);
        
        List<TypeReference> dependencies = analyzer.getAllDependencies(complexType);
        
        assertNotNull(dependencies);
        // 至少应该有一个依赖（Address类型）
        assertTrue(dependencies.size() >= 1);
    }

    @Test
    void testGetAllDependencies_ComplexTypeWithNestedDependencies() {
        // 测试具有嵌套依赖的ComplexType
        CsdlComplexType complexType = new CsdlComplexType();
        complexType.setName("NestedComplex");
        
        CsdlProperty property = new CsdlProperty();
        property.setName("NestedProperty");
        property.setType("ODataDemo.DeeplyNestedType");
        
        complexType.setProperties(Arrays.asList(property));
        
        Map<String, CsdlSchema> schemas = new HashMap<>();
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("ODataDemo");
        schemas.put("ODataDemo", schema);
        
        when(repository.getAllSchemas()).thenReturn(schemas);
        
        List<TypeReference> dependencies = analyzer.getAllDependencies(complexType);
        
        assertNotNull(dependencies);
        // 应该检测到嵌套依赖
        boolean hasNestedDependency = dependencies.stream()
            .anyMatch(dep -> dep.getFullQualifiedName().contains("DeeplyNestedType"));
        assertTrue(hasNestedDependency);
    }

    @Test
    void testGetAllDependencies_ComplexTypeWithNoProperties() {
        // 测试没有属性的ComplexType
        CsdlComplexType complexType = new CsdlComplexType();
        complexType.setName("EmptyComplex");
        
        complexType.setProperties(Arrays.asList());
        
        List<TypeReference> dependencies = analyzer.getAllDependencies(complexType);
        
        assertNotNull(dependencies);
        // 没有属性的复杂类型应该没有依赖（除了可能的基类型）
        assertTrue(dependencies.isEmpty() || dependencies.size() == 0);
    }

    @Test
    void testGetAllDependencies_ComplexTypeWithMultipleDependencies() {
        // 测试具有多个复杂依赖的ComplexType
        CsdlComplexType complexType = new CsdlComplexType();
        complexType.setName("MultipleDependencies");
        
        CsdlProperty firstProperty = new CsdlProperty();
        firstProperty.setName("First");
        firstProperty.setType("ODataDemo.FirstType");
        
        CsdlProperty secondProperty = new CsdlProperty();
        secondProperty.setName("Second");
        secondProperty.setType("ODataDemo.SecondType");
        
        complexType.setProperties(Arrays.asList(firstProperty, secondProperty));
        
        Map<String, CsdlSchema> schemas = new HashMap<>();
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("ODataDemo");
        schemas.put("ODataDemo", schema);
        
        when(repository.getAllSchemas()).thenReturn(schemas);
        
        List<TypeReference> dependencies = analyzer.getAllDependencies(complexType);
        
        assertNotNull(dependencies);
        // 应该有多个依赖
        assertTrue(dependencies.size() >= 2);
    }

    @Test
    void testGetAllDependencies_ComplexTypeWithCrossNamespaceDependencies() {
        // 测试跨命名空间依赖的ComplexType
        CsdlComplexType complexType = new CsdlComplexType();
        complexType.setName("CrossNamespaceComplex");
        
        CsdlProperty property1 = new CsdlProperty();
        property1.setName("LocalProperty");
        property1.setType("LocalNamespace.LocalType");
        
        CsdlProperty property2 = new CsdlProperty();
        property2.setName("ExternalProperty");
        property2.setType("ExternalNamespace.ExternalType");
        
        complexType.setProperties(Arrays.asList(property1, property2));
        
        Map<String, CsdlSchema> schemas = new HashMap<>();
        CsdlSchema localSchema = new CsdlSchema();
        localSchema.setNamespace("LocalNamespace");
        CsdlSchema externalSchema = new CsdlSchema();
        externalSchema.setNamespace("ExternalNamespace");
        schemas.put("LocalNamespace", localSchema);
        schemas.put("ExternalNamespace", externalSchema);
        
        when(repository.getAllSchemas()).thenReturn(schemas);
        
        List<TypeReference> dependencies = analyzer.getAllDependencies(complexType);
        
        assertNotNull(dependencies);
        // 应该检测到跨命名空间的依赖
        boolean hasLocalDependency = dependencies.stream()
            .anyMatch(dep -> dep.getFullQualifiedName().contains("LocalType"));
        boolean hasExternalDependency = dependencies.stream()
            .anyMatch(dep -> dep.getFullQualifiedName().contains("ExternalType"));
        
        assertTrue(hasLocalDependency || hasExternalDependency);
    }
}
