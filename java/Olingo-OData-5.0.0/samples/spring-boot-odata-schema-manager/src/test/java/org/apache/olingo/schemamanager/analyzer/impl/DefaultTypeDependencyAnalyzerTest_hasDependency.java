package org.apache.olingo.schemamanager.analyzer.impl;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.analyzer.TypeDependencyAnalyzer;
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
 * Unit tests for hasDependency method in DefaultTypeDependencyAnalyzer.
 * Tests dependency checking scenarios including direct dependencies, inheritance,
 * property references, and various edge cases.
 */
@ExtendWith(MockitoExtension.class)
public class DefaultTypeDependencyAnalyzerTest_hasDependency {

    @Mock
    private SchemaRepository repository;

    private TypeDependencyAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new DefaultTypeDependencyAnalyzer(repository);
    }

    @Test
    void testHasDependency_DirectInheritance() {
        // 测试直接继承依赖
        CsdlEntityType baseType = new CsdlEntityType();
        baseType.setName("Person");
        
        CsdlEntityType derivedType = new CsdlEntityType();
        derivedType.setName("Employee");
        derivedType.setBaseType(new FullQualifiedName("ODataDemo", "Person"));
        
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("ODataDemo");
        schema.setEntityTypes(Arrays.asList(baseType, derivedType));
        
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put("ODataDemo", schema);
        when(repository.getAllSchemas()).thenReturn(schemas);
        when(repository.getEntityType("ODataDemo.Employee")).thenReturn(derivedType);
        when(repository.getEntityType("ODataDemo.Person")).thenReturn(baseType);
        
        boolean result = analyzer.hasDependency("ODataDemo.Employee", "ODataDemo.Person");
        
        assertTrue(result);
    }

    @Test
    void testHasDependency_PropertyReference() {
        // 测试属性引用依赖
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName("Customer");
        
        CsdlProperty addressProperty = new CsdlProperty();
        addressProperty.setName("Address");
        addressProperty.setType("ODataDemo.Address");
        
        entityType.setProperties(Arrays.asList(addressProperty));
        
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("ODataDemo");
        schema.setEntityTypes(Arrays.asList(entityType));
        
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put("ODataDemo", schema);
        when(repository.getAllSchemas()).thenReturn(schemas);
        when(repository.getEntityType("ODataDemo.Customer")).thenReturn(entityType);
        
        boolean result = analyzer.hasDependency("ODataDemo.Customer", "ODataDemo.Address");
        
        assertTrue(result);
    }

    @Test
    void testHasDependency_NoDependency() {
        // 测试没有依赖的情况
        CsdlEntityType person = new CsdlEntityType();
        person.setName("Person");
        
        CsdlEntityType product = new CsdlEntityType();
        product.setName("Product");
        
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("ODataDemo");
        schema.setEntityTypes(Arrays.asList(person, product));
        
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put("ODataDemo", schema);
        when(repository.getAllSchemas()).thenReturn(schemas);
        when(repository.getEntityType("ODataDemo.Person")).thenReturn(person);
        when(repository.getEntityType("ODataDemo.Product")).thenReturn(product);
        
        boolean result = analyzer.hasDependency("ODataDemo.Person", "ODataDemo.Product");
        
        assertFalse(result);
    }

    @Test
    void testHasDependency_CircularReference() {
        // 测试循环引用
        CsdlEntityType entityA = new CsdlEntityType();
        entityA.setName("EntityA");
        
        CsdlProperty propertyRefB = new CsdlProperty();
        propertyRefB.setName("RefToB");
        propertyRefB.setType("ODataDemo.EntityB");
        entityA.setProperties(Arrays.asList(propertyRefB));
        
        CsdlEntityType entityB = new CsdlEntityType();
        entityB.setName("EntityB");
        
        CsdlProperty propertyRefA = new CsdlProperty();
        propertyRefA.setName("RefToA");
        propertyRefA.setType("ODataDemo.EntityA");
        entityB.setProperties(Arrays.asList(propertyRefA));
        
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("ODataDemo");
        schema.setEntityTypes(Arrays.asList(entityA, entityB));
        
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put("ODataDemo", schema);
        when(repository.getAllSchemas()).thenReturn(schemas);
        when(repository.getEntityType("ODataDemo.EntityA")).thenReturn(entityA);
        when(repository.getEntityType("ODataDemo.EntityB")).thenReturn(entityB);
        
        boolean resultAToB = analyzer.hasDependency("ODataDemo.EntityA", "ODataDemo.EntityB");
        boolean resultBToA = analyzer.hasDependency("ODataDemo.EntityB", "ODataDemo.EntityA");
        
        assertTrue(resultAToB);
        assertTrue(resultBToA);
    }

    @Test
    void testHasDependency_SelfReference() {
        // 测试自引用
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName("TreeNode");
        
        CsdlProperty parentProperty = new CsdlProperty();
        parentProperty.setName("Parent");
        parentProperty.setType("ODataDemo.TreeNode");
        
        entityType.setProperties(Arrays.asList(parentProperty));
        
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("ODataDemo");
        schema.setEntityTypes(Arrays.asList(entityType));
        
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put("ODataDemo", schema);
        when(repository.getAllSchemas()).thenReturn(schemas);
        when(repository.getEntityType("ODataDemo.TreeNode")).thenReturn(entityType);
        
        boolean result = analyzer.hasDependency("ODataDemo.TreeNode", "ODataDemo.TreeNode");
        
        assertTrue(result);
    }

    @Test
    void testHasDependency_NonexistentType() {
        // 测试不存在的类型
        Map<String, CsdlSchema> schemas = new HashMap<>();
        when(repository.getAllSchemas()).thenReturn(schemas);
        when(repository.getEntityType("ODataDemo.NonExistent")).thenReturn(null);
        
        boolean result = analyzer.hasDependency("ODataDemo.NonExistent", "ODataDemo.SomeType");
        
        assertFalse(result);
    }

    @Test
    void testHasDependency_CrossNamespaceDependency() {
        // 测试跨命名空间依赖
        CsdlEntityType orderType = new CsdlEntityType();
        orderType.setName("Order");
        
        CsdlProperty customerProperty = new CsdlProperty();
        customerProperty.setName("Customer");
        customerProperty.setType("CustomerModule.Customer");
        
        orderType.setProperties(Arrays.asList(customerProperty));
        
        CsdlSchema orderSchema = new CsdlSchema();
        orderSchema.setNamespace("OrderModule");
        orderSchema.setEntityTypes(Arrays.asList(orderType));
        
        CsdlEntityType customerType = new CsdlEntityType();
        customerType.setName("Customer");
        
        CsdlSchema customerSchema = new CsdlSchema();
        customerSchema.setNamespace("CustomerModule");
        customerSchema.setEntityTypes(Arrays.asList(customerType));
        
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put("OrderModule", orderSchema);
        schemas.put("CustomerModule", customerSchema);
        
        when(repository.getAllSchemas()).thenReturn(schemas);
        when(repository.getEntityType("OrderModule.Order")).thenReturn(orderType);
        when(repository.getEntityType("CustomerModule.Customer")).thenReturn(customerType);
        
        boolean result = analyzer.hasDependency("OrderModule.Order", "CustomerModule.Customer");
        
        assertTrue(result);
    }

    @Test
    void testHasDependency_MultiLevelInheritance() {
        // 测试多级继承
        CsdlEntityType baseType = new CsdlEntityType();
        baseType.setName("Person");
        
        CsdlEntityType middleType = new CsdlEntityType();
        middleType.setName("Employee");
        middleType.setBaseType(new FullQualifiedName("ODataDemo", "Person"));
        
        CsdlEntityType derivedType = new CsdlEntityType();
        derivedType.setName("Manager");
        derivedType.setBaseType(new FullQualifiedName("ODataDemo", "Employee"));
        
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("ODataDemo");
        schema.setEntityTypes(Arrays.asList(baseType, middleType, derivedType));
        
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put("ODataDemo", schema);
        
        when(repository.getAllSchemas()).thenReturn(schemas);
        when(repository.getEntityType("ODataDemo.Manager")).thenReturn(derivedType);
        when(repository.getEntityType("ODataDemo.Employee")).thenReturn(middleType);
        when(repository.getEntityType("ODataDemo.Person")).thenReturn(baseType);
        
        boolean resultManagerToPerson = analyzer.hasDependency("ODataDemo.Manager", "ODataDemo.Person");
        boolean resultManagerToEmployee = analyzer.hasDependency("ODataDemo.Manager", "ODataDemo.Employee");
        
        assertTrue(resultManagerToPerson);
        assertTrue(resultManagerToEmployee);
    }

    @Test
    void testHasDependency_MultipleProperties() {
        // 测试多个属性引用
        CsdlEntityType orderType = new CsdlEntityType();
        orderType.setName("Order");
        
        CsdlProperty customerProperty = new CsdlProperty();
        customerProperty.setName("Customer");
        customerProperty.setType("ODataDemo.Customer");
        
        CsdlProperty productProperty = new CsdlProperty();
        productProperty.setName("Product");
        productProperty.setType("ODataDemo.Product");
        
        orderType.setProperties(Arrays.asList(customerProperty, productProperty));
        
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("ODataDemo");
        schema.setEntityTypes(Arrays.asList(orderType));
        
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put("ODataDemo", schema);
        
        when(repository.getAllSchemas()).thenReturn(schemas);
        when(repository.getEntityType("ODataDemo.Order")).thenReturn(orderType);
        
        boolean resultToCustomer = analyzer.hasDependency("ODataDemo.Order", "ODataDemo.Customer");
        boolean resultToProduct = analyzer.hasDependency("ODataDemo.Order", "ODataDemo.Product");
        
        assertTrue(resultToCustomer);
        assertTrue(resultToProduct);
    }

    @Test
    void testHasDependency_EmptySchema() {
        // 测试空模式
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace("ODataDemo");
        schema.setEntityTypes(Arrays.asList());
        
        Map<String, CsdlSchema> schemas = new HashMap<>();
        schemas.put("ODataDemo", schema);
        
        when(repository.getAllSchemas()).thenReturn(schemas);
        when(repository.getEntityType("ODataDemo.EntityA")).thenReturn(null);
        when(repository.getEntityType("ODataDemo.EntityB")).thenReturn(null);
        
        boolean result = analyzer.hasDependency("ODataDemo.EntityA", "ODataDemo.EntityB");
        
        assertFalse(result);
    }

    @Test
    void testHasDependency_NullParameters() {
        // 测试null参数
        assertThrows(IllegalArgumentException.class, () -> {
            analyzer.hasDependency(null, "ODataDemo.SomeType");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            analyzer.hasDependency("ODataDemo.SomeType", null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            analyzer.hasDependency(null, null);
        });
    }

    @Test
    void testHasDependency_EmptyParameters() {
        // 测试空字符串参数
        assertThrows(IllegalArgumentException.class, () -> {
            analyzer.hasDependency("", "ODataDemo.SomeType");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            analyzer.hasDependency("ODataDemo.SomeType", "");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            analyzer.hasDependency("", "");
        });
    }
}
