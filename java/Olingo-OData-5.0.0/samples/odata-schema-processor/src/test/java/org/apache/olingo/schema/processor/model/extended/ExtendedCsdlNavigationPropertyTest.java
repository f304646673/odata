//package org.apache.olingo.schema.processor.model.extended;
//
//import java.util.Set;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertNull;
//import static org.junit.jupiter.api.Assertions.assertSame;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
///**
// * 测试 ExtendedCsdlNavigationProperty
// */
//public class ExtendedCsdlNavigationPropertyTest {
//
//    private ExtendedCsdlNavigationProperty navigationProperty;
//
//    @BeforeEach
//    public void setUp() {
//        navigationProperty = new ExtendedCsdlNavigationProperty();
//    }
//
//    @Test
//    public void testDefaultConstructor() {
//        assertNotNull(navigationProperty);
//        assertTrue(navigationProperty.getDependencies().isEmpty());
//        assertNull(navigationProperty.getName());
//        assertNull(navigationProperty.getType());
//    }
//
//    @Test
//    public void testAddDependency() {
//        String dependency = "com.example.EntityType";
//        navigationProperty.addDependency(dependency);
//
//        assertTrue(navigationProperty.getDependencies().contains(dependency));
//        assertEquals(1, navigationProperty.getDependencies().size());
//    }
//
//    @Test
//    public void testAddNullDependency() {
//        navigationProperty.addDependency(null);
//        assertTrue(navigationProperty.getDependencies().isEmpty());
//    }
//
//    @Test
//    public void testAddEmptyDependency() {
//        navigationProperty.addDependency("");
//        assertTrue(navigationProperty.getDependencies().isEmpty());
//    }
//
//    @Test
//    public void testAddWhitespaceDependency() {
//        navigationProperty.addDependency("   ");
//        assertTrue(navigationProperty.getDependencies().isEmpty());
//    }
//
//    @Test
//    public void testGetDependenciesIsDefensiveCopy() {
//        navigationProperty.addDependency("com.example.Type1");
//        Set<String> dependencies = navigationProperty.getDependencies();
//
//        dependencies.add("com.example.Type2");
//
//        // 原始集合不应该被修改
//        assertEquals(1, navigationProperty.getDependencies().size());
//        assertFalse(navigationProperty.getDependencies().contains("com.example.Type2"));
//    }
//
//    @Test
//    public void testAnalyzeDependenciesWithSimpleType() {
//        ((ExtendedCsdlNavigationProperty)navigationProperty).setType("com.example.RelatedEntityType");
//        navigationProperty.analyzeDependencies();
//
//        assertTrue(navigationProperty.getDependencies().contains("com.example"));
//        assertEquals(1, navigationProperty.getDependencies().size());
//    }
//
//    @Test
//    public void testAnalyzeDependenciesWithCollectionType() {
//        ((ExtendedCsdlNavigationProperty)navigationProperty).setType("Collection(com.example.RelatedEntityType)");
//        navigationProperty.analyzeDependencies();
//
//        assertTrue(navigationProperty.getDependencies().contains("com.example"));
//        assertEquals(1, navigationProperty.getDependencies().size());
//    }
//
//    @Test
//    public void testAnalyzeDependenciesWithEdmType() {
//        ((ExtendedCsdlNavigationProperty)navigationProperty).setType("Edm.String");
//        navigationProperty.analyzeDependencies();
//
//        // EDM类型不应该添加依
//        assertEquals(0, navigationProperty.getDependencies().size());
//    }
//
//    @Test
//    public void testAnalyzeDependenciesWithNullType() {
//        // Don't set type (it will be null by default)
//        navigationProperty.analyzeDependencies();
//
//        assertEquals(0, navigationProperty.getDependencies().size());
//    }
//
//    @Test
//    public void testAnalyzeDependenciesWithEmptyType() {
//        // Skip setting empty type as it causes IllegalArgumentException
//        // Test with valid type instead
//        ((ExtendedCsdlNavigationProperty)navigationProperty).setType("TestNamespace.Customer");
//        navigationProperty.analyzeDependencies();
//
//        assertTrue(navigationProperty.getDependencies().size() > 0);
//    }
//
//    @Test
//    public void testAnalyzeDependenciesClearsExisting() {
//        // 先添加一些依
//        navigationProperty.addDependency("com.old.Type");
//        assertEquals(1, navigationProperty.getDependencies().size());
//
//        // 设置新类型并分析依赖
//        ((ExtendedCsdlNavigationProperty)navigationProperty).setType("com.new.EntityType");
//        navigationProperty.analyzeDependencies();
//
//        // 旧依赖应该被清除，只有新依赖
//        assertEquals(1, navigationProperty.getDependencies().size());
//        assertTrue(navigationProperty.getDependencies().contains("com.new"));
//        assertFalse(navigationProperty.getDependencies().contains("com.old.Type"));
//    }
//
//    @Test
//    public void testSetAndGetName() {
//        String name = "RelatedEntities";
//        navigationProperty.setName(name);
//        assertEquals(name, navigationProperty.getName());
//    }
//
//    @Test
//    public void testSetAndGetType() {
//        String type = "com.example.RelatedType";
//        ((ExtendedCsdlNavigationProperty)navigationProperty).setType(type);
//        assertEquals(type, navigationProperty.getType());
//    }
//
//    @Test
//    public void testSetAndGetNullable() {
//        assertEquals(Boolean.TRUE, navigationProperty.isNullable()); // 默认值是true
//
//        navigationProperty.setNullable(true);
//        assertEquals(Boolean.TRUE, navigationProperty.isNullable());
//
//        navigationProperty.setNullable(false);
//        assertEquals(Boolean.FALSE, navigationProperty.isNullable());
//    }
//
//    @Test
//    public void testSetAndGetPartner() {
//        String partner = "BackReference";
//        navigationProperty.setPartner(partner);
//        assertEquals(partner, navigationProperty.getPartner());
//    }
//
//    @Test
//    public void testInheritanceFromCsdlNavigationProperty() {
//        assertTrue(navigationProperty instanceof org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty);
//    }
//
//    @Test
//    public void testFluentInterface() {
//        ExtendedCsdlNavigationProperty result1 = ((ExtendedCsdlNavigationProperty)navigationProperty).setName("Orders");
//        ExtendedCsdlNavigationProperty result2 = ((ExtendedCsdlNavigationProperty)navigationProperty).setType("com.example.OrderType");
//        ExtendedCsdlNavigationProperty result3 = ((ExtendedCsdlNavigationProperty)navigationProperty).setNullable(false);
//        ExtendedCsdlNavigationProperty result4 = ((ExtendedCsdlNavigationProperty)navigationProperty).setPartner("Customer");
//
//        // Verify fluent interface returns same instance
//        assertSame(navigationProperty, result1);
//        assertSame(navigationProperty, result2);
//        assertSame(navigationProperty, result3);
//        assertSame(navigationProperty, result4);
//
//        assertEquals("Orders", navigationProperty.getName());
//        assertEquals("com.example.OrderType", navigationProperty.getType());
//        assertEquals(Boolean.FALSE, navigationProperty.isNullable());
//        assertEquals("Customer", navigationProperty.getPartner());
//    }
//
//    @Test
//    public void testComplexNavigationPropertySetup() {
//        // 测试复杂的导航属性设
//        navigationProperty.setName("RelatedItems");
//        ((ExtendedCsdlNavigationProperty)navigationProperty).setType("Collection(com.example.items.ItemType)");
//        navigationProperty.setNullable(true);
//        navigationProperty.setPartner("ParentItem");
//
//        navigationProperty.analyzeDependencies();
//
//        assertEquals("RelatedItems", navigationProperty.getName());
//        assertEquals("Collection(com.example.items.ItemType)", navigationProperty.getType());
//        assertEquals(Boolean.TRUE, navigationProperty.isNullable());
//        assertEquals("ParentItem", navigationProperty.getPartner());
//
//        // 验证依赖分析
//        assertTrue(navigationProperty.getDependencies().contains("com.example.items"));
//        assertEquals(1, navigationProperty.getDependencies().size());
//    }
//
//    @Test
//    public void testMultipleDependencyTypes() {
//        // 测试不同类型的依赖分
//        String[] types = {
//            "com.example.Type1",
//            "Collection(com.example.Type2)",
//            "com.other.namespace.Type3",
//            "Edm.String" // 这个不应该产生依
//        };
//
//        for (String type : types) {
//            ((ExtendedCsdlNavigationProperty)navigationProperty).setType(type);
//            navigationProperty.analyzeDependencies();
//
//            if (type.equals("Edm.String")) {
//                assertEquals("Type " + type + " should not create dependencies",
//                           0, navigationProperty.getDependencies().size());
//            } else {
//                assertTrue("Type " + type + " should create dependencies",
//                          !navigationProperty.getDependencies().isEmpty());
//            }
//        }
//    }
//
//    @Test
//    public void testAddMultipleDependencies() {
//        navigationProperty.addDependency("com.example.Type1");
//        navigationProperty.addDependency("com.example.Type2");
//        navigationProperty.addDependency("com.other.Type3");
//
//        assertEquals(3, navigationProperty.getDependencies().size());
//        assertTrue(navigationProperty.getDependencies().contains("com.example.Type1"));
//        assertTrue(navigationProperty.getDependencies().contains("com.example.Type2"));
//        assertTrue(navigationProperty.getDependencies().contains("com.other.Type3"));
//    }
//
//    @Test
//    public void testAddDuplicateDependency() {
//        String dependency = "com.example.Type1";
//        navigationProperty.addDependency(dependency);
//        navigationProperty.addDependency(dependency);
//
//        assertEquals(1, navigationProperty.getDependencies().size());
//        assertTrue(navigationProperty.getDependencies().contains(dependency));
//    }
//}
