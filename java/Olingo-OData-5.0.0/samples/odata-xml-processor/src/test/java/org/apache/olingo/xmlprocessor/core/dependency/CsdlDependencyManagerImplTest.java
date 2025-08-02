package org.apache.olingo.xmlprocessor.core.dependency;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.xmlprocessor.core.dependency.impl.CsdlDependencyManagerImpl;
import org.apache.olingo.xmlprocessor.core.dependency.model.CsdlDependencyNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * CsdlDependencyManagerImpl的单元测试
 */
public class CsdlDependencyManagerImplTest {

    private DependencyManager manager;

    @BeforeEach
    public void setUp() {
        manager = new CsdlDependencyManagerImpl();
    }

    @Test
    public void testRegisterElement() {
        FullQualifiedName fqn = new FullQualifiedName("Test.Namespace", "TestEntity");

        CsdlDependencyNode node = manager.registerElement(
            "TestEntity",
            fqn,
            CsdlDependencyNode.DependencyType.ENTITY_TYPE,
            "Test.Namespace"
        );

        assertNotNull(node);
        assertEquals("TestEntity", node.getElementId());
        assertEquals(fqn, node.getFullyQualifiedName());
        assertEquals(CsdlDependencyNode.DependencyType.ENTITY_TYPE, node.getDependencyType());
    }

    @Test
    public void testGetElement() {
        FullQualifiedName fqn = new FullQualifiedName("Test.Namespace", "TestEntity");
        manager.registerElement("TestEntity", fqn, CsdlDependencyNode.DependencyType.ENTITY_TYPE, "Test.Namespace");

        CsdlDependencyNode node = manager.getElement("TestEntity");
        assertNotNull(node);
        assertEquals("TestEntity", node.getElementId());
    }

    @Test
    public void testAddDependency() {
        // 注册两个元素
        FullQualifiedName fqn1 = new FullQualifiedName("Test.Namespace", "Entity1");
        FullQualifiedName fqn2 = new FullQualifiedName("Test.Namespace", "Entity2");

        manager.registerElement("Entity1", fqn1, CsdlDependencyNode.DependencyType.ENTITY_TYPE, "Test.Namespace");
        manager.registerElement("Entity2", fqn2, CsdlDependencyNode.DependencyType.ENTITY_TYPE, "Test.Namespace");

        // 添加依赖关系
        boolean result = manager.addDependency("Entity1", "Entity2");
        assertTrue(result);

        // 验证依赖关系
        Set<CsdlDependencyNode> dependencies = manager.getDirectDependencies("Entity1");
        assertEquals(1, dependencies.size());
        assertTrue(dependencies.stream().anyMatch(node -> "Entity2".equals(node.getElementId())));
    }

    @Test
    public void testRemoveDependency() {
        // 注册元素并添加依赖
        FullQualifiedName fqn1 = new FullQualifiedName("Test.Namespace", "Entity1");
        FullQualifiedName fqn2 = new FullQualifiedName("Test.Namespace", "Entity2");

        manager.registerElement("Entity1", fqn1, CsdlDependencyNode.DependencyType.ENTITY_TYPE, "Test.Namespace");
        manager.registerElement("Entity2", fqn2, CsdlDependencyNode.DependencyType.ENTITY_TYPE, "Test.Namespace");
        manager.addDependency("Entity1", "Entity2");

        // 移除依赖关系
        boolean result = manager.removeDependency("Entity1", "Entity2");
        assertTrue(result);

        // 验证依赖关系已移除
        Set<CsdlDependencyNode> dependencies = manager.getDirectDependencies("Entity1");
        assertEquals(0, dependencies.size());
    }

    @Test
    public void testGetElementsByType() {
        // 注册不同类型的元素
        FullQualifiedName entityFqn = new FullQualifiedName("Test.Namespace", "TestEntity");
        FullQualifiedName complexFqn = new FullQualifiedName("Test.Namespace", "TestComplex");

        manager.registerElement("TestEntity", entityFqn, CsdlDependencyNode.DependencyType.ENTITY_TYPE, "Test.Namespace");
        manager.registerElement("TestComplex", complexFqn, CsdlDependencyNode.DependencyType.COMPLEX_TYPE, "Test.Namespace");

        // 按类型查询
        Set<CsdlDependencyNode> entityTypes = manager.getElementsByType(CsdlDependencyNode.DependencyType.ENTITY_TYPE);
        Set<CsdlDependencyNode> complexTypes = manager.getElementsByType(CsdlDependencyNode.DependencyType.COMPLEX_TYPE);

        assertEquals(1, entityTypes.size());
        assertEquals(1, complexTypes.size());
        assertTrue(entityTypes.stream().anyMatch(node -> "TestEntity".equals(node.getElementId())));
        assertTrue(complexTypes.stream().anyMatch(node -> "TestComplex".equals(node.getElementId())));
    }

    @Test
    public void testGetElementsByNamespace() {
        // 注册不同命名空间的元素
        FullQualifiedName fqn1 = new FullQualifiedName("Namespace1", "Entity1");
        FullQualifiedName fqn2 = new FullQualifiedName("Namespace2", "Entity2");

        manager.registerElement("Entity1", fqn1, CsdlDependencyNode.DependencyType.ENTITY_TYPE, "Namespace1");
        manager.registerElement("Entity2", fqn2, CsdlDependencyNode.DependencyType.ENTITY_TYPE, "Namespace2");

        // 按命名空间查询
        Set<CsdlDependencyNode> ns1Elements = manager.getElementsByNamespace("Namespace1");
        Set<CsdlDependencyNode> ns2Elements = manager.getElementsByNamespace("Namespace2");

        assertEquals(1, ns1Elements.size());
        assertEquals(1, ns2Elements.size());
        assertTrue(ns1Elements.stream().anyMatch(node -> "Entity1".equals(node.getElementId())));
        assertTrue(ns2Elements.stream().anyMatch(node -> "Entity2".equals(node.getElementId())));
    }

    @Test
    public void testCircularDependencyDetection() {
        // 注册三个元素
        FullQualifiedName fqn1 = new FullQualifiedName("Test.Namespace", "Entity1");
        FullQualifiedName fqn2 = new FullQualifiedName("Test.Namespace", "Entity2");
        FullQualifiedName fqn3 = new FullQualifiedName("Test.Namespace", "Entity3");

        manager.registerElement("Entity1", fqn1, CsdlDependencyNode.DependencyType.ENTITY_TYPE, "Test.Namespace");
        manager.registerElement("Entity2", fqn2, CsdlDependencyNode.DependencyType.ENTITY_TYPE, "Test.Namespace");
        manager.registerElement("Entity3", fqn3, CsdlDependencyNode.DependencyType.ENTITY_TYPE, "Test.Namespace");

        // 创建循环依赖：Entity1 -> Entity2 -> Entity3 -> Entity1
        manager.addDependency("Entity1", "Entity2");
        manager.addDependency("Entity2", "Entity3");
        manager.addDependency("Entity3", "Entity1");

        // 检测循环依赖
        assertTrue(manager.hasCircularDependencies());
        assertTrue(manager.hasCircularDependency("Entity1"));
        assertTrue(manager.hasCircularDependency("Entity2"));
        assertTrue(manager.hasCircularDependency("Entity3"));
    }

    @Test
    public void testTopologicalOrder() {
        // 注册元素并建立依赖关系
        FullQualifiedName fqn1 = new FullQualifiedName("Test.Namespace", "Entity1");
        FullQualifiedName fqn2 = new FullQualifiedName("Test.Namespace", "Entity2");
        FullQualifiedName fqn3 = new FullQualifiedName("Test.Namespace", "Entity3");

        manager.registerElement("Entity1", fqn1, CsdlDependencyNode.DependencyType.ENTITY_TYPE, "Test.Namespace");
        manager.registerElement("Entity2", fqn2, CsdlDependencyNode.DependencyType.ENTITY_TYPE, "Test.Namespace");
        manager.registerElement("Entity3", fqn3, CsdlDependencyNode.DependencyType.ENTITY_TYPE, "Test.Namespace");

        // 建立依赖关系：Entity3 -> Entity2 -> Entity1
        manager.addDependency("Entity3", "Entity2");
        manager.addDependency("Entity2", "Entity1");

        // 获取拓扑排序
        List<CsdlDependencyNode> order = manager.getTopologicalOrder();
        assertEquals(3, order.size());

        // 验证排序正确性（Entity1应该在Entity2之前，Entity2应该在Entity3之前）
        int entity1Index = -1, entity2Index = -1, entity3Index = -1;
        for (int i = 0; i < order.size(); i++) {
            String elementId = order.get(i).getElementId();
            if ("Entity1".equals(elementId)) entity1Index = i;
            else if ("Entity2".equals(elementId)) entity2Index = i;
            else if ("Entity3".equals(elementId)) entity3Index = i;
        }

        assertTrue(entity1Index < entity2Index);
        assertTrue(entity2Index < entity3Index);
    }

    @Test
    public void testClear() {
        // 注册元素并添加依赖
        FullQualifiedName fqn1 = new FullQualifiedName("Test.Namespace", "Entity1");
        FullQualifiedName fqn2 = new FullQualifiedName("Test.Namespace", "Entity2");

        manager.registerElement("Entity1", fqn1, CsdlDependencyNode.DependencyType.ENTITY_TYPE, "Test.Namespace");
        manager.registerElement("Entity2", fqn2, CsdlDependencyNode.DependencyType.ENTITY_TYPE, "Test.Namespace");
        manager.addDependency("Entity1", "Entity2");

        // 验证元素已注册
        assertNotNull(manager.getElement("Entity1"));
        assertNotNull(manager.getElement("Entity2"));

        // 清空
        manager.clear();

        // 验证已清空
        assertNull(manager.getElement("Entity1"));
        assertNull(manager.getElement("Entity2"));
        assertEquals(0, manager.getAllElements().size());
    }

    @Test
    public void testUnregisterElement() {
        // 注册元素
        FullQualifiedName fqn = new FullQualifiedName("Test.Namespace", "TestEntity");
        manager.registerElement("TestEntity", fqn, CsdlDependencyNode.DependencyType.ENTITY_TYPE, "Test.Namespace");

        // 验证已注册
        assertNotNull(manager.getElement("TestEntity"));

        // 注销元素
        boolean result = manager.unregisterElement("TestEntity");
        assertTrue(result);

        // 验证已注销
        assertNull(manager.getElement("TestEntity"));
    }

    @Test
    public void testMultipleInstances() {
        // 创建两个独立的管理器实例
        DependencyManager manager1 = new CsdlDependencyManagerImpl();
        DependencyManager manager2 = new CsdlDependencyManagerImpl();

        // 在第一个管理器中注册元素
        FullQualifiedName fqn1 = new FullQualifiedName("Test.Namespace", "Entity1");
        manager1.registerElement("Entity1", fqn1, CsdlDependencyNode.DependencyType.ENTITY_TYPE, "Test.Namespace");

        // 在第二个管理器中注册不同的元素
        FullQualifiedName fqn2 = new FullQualifiedName("Test.Namespace", "Entity2");
        manager2.registerElement("Entity2", fqn2, CsdlDependencyNode.DependencyType.ENTITY_TYPE, "Test.Namespace");

        // 验证两个管理器是独立的
        assertNotNull(manager1.getElement("Entity1"));
        assertNull(manager1.getElement("Entity2"));

        assertNull(manager2.getElement("Entity1"));
        assertNotNull(manager2.getElement("Entity2"));
    }
}
