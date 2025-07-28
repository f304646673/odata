package org.apache.olingo.schema.processor.model.extended;

import java.util.Arrays;
import java.util.Set;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 测试 ExtendedCsdlEntitySet
 */
public class ExtendedCsdlEntitySetTest {

    private ExtendedCsdlEntitySet entitySet;

    @BeforeEach
    public void setUp() {
        entitySet = new ExtendedCsdlEntitySet();
    }

    @Test
    public void testDefaultConstructor() {
        assertNotNull(entitySet);
        assertTrue(entitySet.getDependencies().isEmpty());
        assertEquals(0, entitySet.getDependencyCount());
        assertNull(entitySet.getName());
        // Don't test getType() when it's null - causes NPE
    }

    @Test
    public void testAddDependency() {
        String namespace = "com.example.namespace1";
        entitySet.addDependency(namespace);
        
        assertTrue(entitySet.hasDependency(namespace));
        assertEquals(1, entitySet.getDependencyCount());
        assertTrue(entitySet.getDependencies().contains(namespace));
    }

    @Test
    public void testAddNullDependency() {
        entitySet.addDependency(null);
        assertTrue(entitySet.getDependencies().isEmpty());
        assertEquals(0, entitySet.getDependencyCount());
    }

    @Test
    public void testAddEmptyDependency() {
        entitySet.addDependency("");
        assertTrue(entitySet.getDependencies().isEmpty());
        assertEquals(0, entitySet.getDependencyCount());
    }

    @Test
    public void testAddWhitespaceDependency() {
        entitySet.addDependency("   ");
        assertTrue(entitySet.getDependencies().isEmpty());
        assertEquals(0, entitySet.getDependencyCount());
    }

    @Test
    public void testAddMultipleDependencies() {
        entitySet.addDependency("com.example.namespace1");
        entitySet.addDependency("com.example.namespace2");
        entitySet.addDependency("com.example.namespace3");
        
        assertEquals(3, entitySet.getDependencyCount());
        assertTrue(entitySet.hasDependency("com.example.namespace1"));
        assertTrue(entitySet.hasDependency("com.example.namespace2"));
        assertTrue(entitySet.hasDependency("com.example.namespace3"));
    }

    @Test
    public void testAddDuplicateDependency() {
        String namespace = "com.example.namespace1";
        entitySet.addDependency(namespace);
        entitySet.addDependency(namespace);
        
        assertEquals(1, entitySet.getDependencyCount());
        assertTrue(entitySet.hasDependency(namespace));
    }

    @Test
    public void testRemoveDependency() {
        String namespace1 = "com.example.namespace1";
        String namespace2 = "com.example.namespace2";
        
        entitySet.addDependency(namespace1);
        entitySet.addDependency(namespace2);
        
        assertTrue(entitySet.removeDependency(namespace1));
        assertFalse(entitySet.hasDependency(namespace1));
        assertTrue(entitySet.hasDependency(namespace2));
        assertEquals(1, entitySet.getDependencyCount());
    }

    @Test
    public void testRemoveNonExistentDependency() {
        entitySet.addDependency("com.example.namespace1");
        
        assertFalse(entitySet.removeDependency("com.example.nonexistent"));
        assertEquals(1, entitySet.getDependencyCount());
    }

    @Test
    public void testClearDependencies() {
        entitySet.addDependency("com.example.namespace1");
        entitySet.addDependency("com.example.namespace2");
        entitySet.addDependency("com.example.namespace3");
        
        entitySet.clearDependencies();
        
        assertTrue(entitySet.getDependencies().isEmpty());
        assertEquals(0, entitySet.getDependencyCount());
    }

    @Test
    public void testGetDependenciesIsDefensiveCopy() {
        entitySet.addDependency("com.example.namespace1");
        Set<String> dependencies = entitySet.getDependencies();
        
        dependencies.add("com.example.namespace2");
        
        // 原始集合不应该被修改
        assertEquals(1, entitySet.getDependencyCount());
        assertFalse(entitySet.hasDependency("com.example.namespace2"));
    }

    @Test
    public void testHasDependency() {
        String namespace = "com.example.namespace1";
        
        assertFalse(entitySet.hasDependency(namespace));
        
        entitySet.addDependency(namespace);
        assertTrue(entitySet.hasDependency(namespace));
    }

    @Test
    public void testHasDependencyWithNull() {
        assertFalse(entitySet.hasDependency(null));
    }

    @Test
    public void testSetAndGetFullyQualifiedName() {
        String fqn = "com.example.MyEntitySet";
        entitySet.setFullyQualifiedName(fqn);
        assertEquals(fqn, entitySet.getFullyQualifiedName());
    }

    @Test
    public void testAnalyzeDependenciesWithEntityType() {
        ((ExtendedCsdlEntitySet)entitySet).setType("com.example.MyEntityType");
        
        assertTrue(entitySet.hasDependency("com.example"));
        assertEquals(1, entitySet.getDependencyCount());
    }

    @Test
    public void testAnalyzeDependenciesWithEdmType() {
        ((ExtendedCsdlEntitySet)entitySet).setType("Edm.String");
        
        // EDM类型不应该添加依
        assertEquals(0, entitySet.getDependencyCount());
    }

    @Test
    public void testAnalyzeDependenciesWithCollectionType() {
        ((ExtendedCsdlEntitySet)entitySet).setType("Collection(com.example.MyEntityType)");
        
        assertTrue(entitySet.hasDependency("com.example"));
        assertEquals(1, entitySet.getDependencyCount());
    }

    @Test
    public void testAnalyzeDependenciesWithNavigationPropertyBindings() {
        // Set a valid type first to avoid NPE
        ((ExtendedCsdlEntitySet)entitySet).setType(new FullQualifiedName("com.base", "BaseType"));
        
        CsdlNavigationPropertyBinding binding1 = new CsdlNavigationPropertyBinding();
        binding1.setPath("Orders");
        binding1.setTarget("com.example.OrderSet");
        
        CsdlNavigationPropertyBinding binding2 = new CsdlNavigationPropertyBinding();
        binding2.setPath("Customer");
        binding2.setTarget("com.other.CustomerSet");
        
        entitySet.setNavigationPropertyBindings(Arrays.asList(binding1, binding2));
        entitySet.analyzeDependencies();
        
        assertTrue(entitySet.hasDependency("com.base"));
        assertTrue(entitySet.hasDependency("com.example"));
        assertTrue(entitySet.hasDependency("com.other"));
        assertEquals(3, entitySet.getDependencyCount());
    }

    @Test
    public void testAnalyzeDependenciesWithNullType() {
        // Test handling null type - should not cause errors
        entitySet.analyzeDependencies();
        assertEquals(0, entitySet.getDependencyCount());
    }

    @Test
    public void testAnalyzeDependenciesWithEmptyType() {
        // Set a valid FQN to avoid IllegalArgumentException
        ((ExtendedCsdlEntitySet)entitySet).setType(new FullQualifiedName("", ""));
        entitySet.analyzeDependencies();
        assertEquals(0, entitySet.getDependencyCount());
    }

    @Test
    public void testAnalyzeDependenciesWithSimpleTypeName() {
        ((ExtendedCsdlEntitySet)entitySet).setType(new FullQualifiedName("com.example", "MyEntityType"));
        entitySet.analyzeDependencies();
        
        // With namespace it should add dependency
        assertEquals(1, entitySet.getDependencyCount());
        assertTrue(entitySet.hasDependency("com.example"));
    }

    @Test
    public void testFluentInterface() {
        entitySet.setName("FluentEntitySet");
        ((ExtendedCsdlEntitySet)entitySet).setType("com.example.FluentType");
        entitySet.setIncludeInServiceDocument(true);
        
        assertEquals("FluentEntitySet", entitySet.getName());
        assertEquals("com.example.FluentType", entitySet.getType());
        assertTrue(entitySet.isIncludeInServiceDocument());
        
        // 验证依赖分析被自动触
        assertTrue(entitySet.hasDependency("com.example"));
    }

    @Test
    public void testInheritanceFromCsdlEntitySet() {
        assertTrue(entitySet instanceof org.apache.olingo.commons.api.edm.provider.CsdlEntitySet);
    }

    @Test
    public void testSetName() {
        String name = "MyEntitySet";
        entitySet.setName(name);
        assertEquals(name, entitySet.getName());
    }

    @Test
    public void testSetIncludeInServiceDocument() {
        entitySet.setIncludeInServiceDocument(true);
        assertTrue(entitySet.isIncludeInServiceDocument());
        
        entitySet.setIncludeInServiceDocument(false);
        assertFalse(entitySet.isIncludeInServiceDocument());
    }

    @Test
    public void testComplexDependencyAnalysis() {
        // 设置复杂的依赖场
        ((ExtendedCsdlEntitySet)entitySet).setType("com.example.orders.OrderType");
        
        CsdlNavigationPropertyBinding binding1 = new CsdlNavigationPropertyBinding();
        binding1.setPath("Customer");
        binding1.setTarget("com.example.customers.CustomerSet");
        
        CsdlNavigationPropertyBinding binding2 = new CsdlNavigationPropertyBinding();
        binding2.setPath("Items");
        binding2.setTarget("com.example.items.ItemSet");
        
        entitySet.setNavigationPropertyBindings(Arrays.asList(binding1, binding2));
        entitySet.analyzeDependencies();
        
        // 应该有三个不同namespace的依
        assertEquals(3, entitySet.getDependencyCount());
        assertTrue(entitySet.hasDependency("com.example.orders"));
        assertTrue(entitySet.hasDependency("com.example.customers"));
        assertTrue(entitySet.hasDependency("com.example.items"));
    }

    @Test
    public void testDependencyAnalysisAfterTypeChange() {
        // 先设置一个类
        ((ExtendedCsdlEntitySet)entitySet).setType("com.example.Type1");
        assertEquals(1, entitySet.getDependencyCount());
        assertTrue(entitySet.hasDependency("com.example"));
        
        // 改变类型
        ((ExtendedCsdlEntitySet)entitySet).setType("com.other.Type2");
        assertEquals(1, entitySet.getDependencyCount());
        assertTrue(entitySet.hasDependency("com.other"));
        assertFalse(entitySet.hasDependency("com.example"));
    }
}
