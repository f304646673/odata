//package org.apache.olingo.schema.processor.model.dependency;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertNull;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//import java.util.List;
//import java.util.Set;
//
//import org.apache.olingo.commons.api.edm.FullQualifiedName;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
///**
// * GlobalDependencyManager的单元测试
// */
//public class GlobalDependencyManagerTest {
//
//    private GlobalDependencyManager manager;
//    private CsdlDependencyNode node1;
//    private CsdlDependencyNode node2;
//    private CsdlDependencyNode node3;
//
//    @BeforeEach
//    public void setUp() {
//        // 重置单例实例
//        GlobalDependencyManager.resetInstance();
//        manager = GlobalDependencyManager.getInstance();
//
//        // 创建测试节点
//        node1 = new CsdlDependencyNode("Entity1",
//                new FullQualifiedName("com.example", "Entity1"),
//                CsdlDependencyNode.DependencyType.ENTITY_TYPE, null);
//        node2 = new CsdlDependencyNode("Entity2",
//                new FullQualifiedName("com.example", "Entity2"),
//                CsdlDependencyNode.DependencyType.ENTITY_TYPE, null);
//        node3 = new CsdlDependencyNode("Action1",
//                new FullQualifiedName("com.example", "Action1"),
//                CsdlDependencyNode.DependencyType.ACTION_REFERENCE, null);
//    }
//
//    @AfterEach
//    public void tearDown() {
//        GlobalDependencyManager.resetInstance();
//    }
//
//    @Test
//    public void testSingletonPattern() {
//        GlobalDependencyManager manager1 = GlobalDependencyManager.getInstance();
//        GlobalDependencyManager manager2 = GlobalDependencyManager.getInstance();
//
//        assertTrue(manager1 == manager2); // 同一个实例
//    }
//
//    @Test
//    public void testRegisterElement() {
//        CsdlDependencyNode registered = manager.registerElement("entity1",
//                                                               node1.getFullyQualifiedName(),
//                                                               node1.getDependencyType(), null);
//
//        assertNotNull(registered);
//        assertEquals("Entity1", registered.getName());
//        assertEquals(CsdlDependencyNode.DependencyType.ENTITY_TYPE, registered.getDependencyType());
//
//        // 重复注册应该返回相同实例
//        CsdlDependencyNode registered2 = manager.registerElement("entity1",
//                                                                node1.getFullyQualifiedName(),
//                                                                node1.getDependencyType(), null);
//
//        assertTrue(registered == registered2);
//    }
//
//    @Test
//    public void testAddDependency() {
//        manager.registerElement("entity1", node1.getFullyQualifiedName(), node1.getDependencyType(), null);
//        manager.registerElement("entity2", node2.getFullyQualifiedName(), node2.getDependencyType(), null);
//
//        // 添加依赖关系: entity1 -> entity2
//        boolean result = manager.addDependency("entity1", "entity2");
//        assertTrue(result);
//
//        // 验证依赖关系
//        Set<CsdlDependencyNode> dependencies = manager.getDirectDependencies("entity1");
//        assertEquals(1, dependencies.size());
//        assertTrue(dependencies.contains(manager.getElement("entity2")));
//
//        // 验证反向依赖关系
//        Set<CsdlDependencyNode> dependents = manager.getDirectDependents("entity2");
//        assertEquals(1, dependents.size());
//        assertTrue(dependents.contains(manager.getElement("entity1")));
//    }
//
//    @Test
//    public void testRemoveDependency() {
//        manager.registerElement("entity1", node1.getFullyQualifiedName(), node1.getDependencyType(), null);
//        manager.registerElement("entity2", node2.getFullyQualifiedName(), node2.getDependencyType(), null);
//
//        // 添加依赖关系
//        manager.addDependency("entity1", "entity2");
//
//        // 移除依赖关系
//        boolean result = manager.removeDependency("entity1", "entity2");
//        assertTrue(result);
//
//        // 验证依赖关系已移除
//        Set<CsdlDependencyNode> dependencies = manager.getDirectDependencies("entity1");
//        assertEquals(0, dependencies.size());
//
//        Set<CsdlDependencyNode> dependents = manager.getDirectDependents("entity2");
//        assertEquals(0, dependents.size());
//    }
//
//    @Test
//    public void testGetAllDependencies() {
//        manager.registerElement("entity1", node1.getFullyQualifiedName(), node1.getDependencyType(), null);
//        manager.registerElement("entity2", node2.getFullyQualifiedName(), node2.getDependencyType(), null);
//        manager.registerElement("action1", node3.getFullyQualifiedName(), node3.getDependencyType(), null);
//
//        // 建立依赖 entity1 -> entity2 -> action1
//        manager.addDependency("entity1", "entity2");
//        manager.addDependency("entity2", "action1");
//
//        Set<CsdlDependencyNode> allDeps = manager.getAllDependencies("entity1");
//        assertEquals(2, allDeps.size());
//        assertTrue(allDeps.contains(manager.getElement("entity2")));
//        assertTrue(allDeps.contains(manager.getElement("action1")));
//    }
//
//    @Test
//    public void testGetDependenciesByType() {
//        manager.registerElement("entity1", node1.getFullyQualifiedName(), node1.getDependencyType(), null);
//        manager.registerElement("entity2", node2.getFullyQualifiedName(), node2.getDependencyType(), null);
//        manager.registerElement("action1", node3.getFullyQualifiedName(), node3.getDependencyType(), null);
//
//        manager.addDependency("entity1", "entity2");
//        manager.addDependency("entity1", "action1");
//
//        Set<CsdlDependencyNode> entityDeps =
//            manager.getElementsByType(CsdlDependencyNode.DependencyType.ENTITY_TYPE);
//        assertEquals(2, entityDeps.size());
//        assertTrue(entityDeps.contains(manager.getElement("entity1")));
//        assertTrue(entityDeps.contains(manager.getElement("entity2")));
//
//        Set<CsdlDependencyNode> actionDeps =
//            manager.getElementsByType(CsdlDependencyNode.DependencyType.ACTION_REFERENCE);
//        assertEquals(1, actionDeps.size());
//        assertTrue(actionDeps.contains(manager.getElement("action1")));
//    }
//
//    @Test
//    public void testGetDependenciesByNamespace() {
//        manager.registerElement("entity1", node1.getFullyQualifiedName(), node1.getDependencyType(), null);
//        manager.registerElement("entity2", node2.getFullyQualifiedName(), node2.getDependencyType(), null);
//        manager.registerElement("action1", node3.getFullyQualifiedName(), node3.getDependencyType(), null);
//
//        manager.addDependency("entity1", "entity2");
//        manager.addDependency("entity1", "action1");
//
//        Set<CsdlDependencyNode> namespaceDeps =
//            manager.getElementsByNamespace("com.example");
//        assertEquals(3, namespaceDeps.size());
//        assertTrue(namespaceDeps.contains(manager.getElement("entity1")));
//        assertTrue(namespaceDeps.contains(manager.getElement("entity2")));
//        assertTrue(namespaceDeps.contains(manager.getElement("action1")));
//    }
//
//    @Test
//    public void testGetDependencyPath() {
//        manager.registerElement("entity1", node1.getFullyQualifiedName(), node1.getDependencyType(), null);
//        manager.registerElement("entity2", node2.getFullyQualifiedName(), node2.getDependencyType(), null);
//        manager.registerElement("action1", node3.getFullyQualifiedName(), node3.getDependencyType(), null);
//
//        // 建立依赖 entity1 -> entity2 -> action1
//        manager.addDependency("entity1", "entity2");
//        manager.addDependency("entity2", "action1");
//
//        List<CsdlDependencyNode> path = manager.getDependencyPath("entity1", "action1");
//        assertNotNull(path);
//        assertEquals(3, path.size());
//        assertEquals(manager.getElement("entity1"), path.get(0));
//        assertEquals(manager.getElement("entity2"), path.get(1));
//        assertEquals(manager.getElement("action1"), path.get(2));
//
//        // 测试不存在的路径
//        List<CsdlDependencyNode> noPath = manager.getDependencyPath("action1", "entity1");
//        assertNull(noPath);
//    }
//
//    @Test
//    public void testHasCircularDependency() {
//        manager.registerElement("entity1", node1.getFullyQualifiedName(), node1.getDependencyType(), null);
//        manager.registerElement("entity2", node2.getFullyQualifiedName(), node2.getDependencyType(), null);
//        manager.registerElement("action1", node3.getFullyQualifiedName(), node3.getDependencyType(), null);
//
//        // 建立线性依赖链
//        manager.addDependency("entity1", "entity2");
//        manager.addDependency("entity2", "action1");
//
//        assertFalse(manager.hasCircularDependency("entity1"));
//        assertFalse(manager.hasCircularDependency("entity2"));
//        assertFalse(manager.hasCircularDependency("action1"));
//
//        // 建立循环依赖
//        manager.addDependency("action1", "entity1");
//
//        assertTrue(manager.hasCircularDependency("entity1"));
//        assertTrue(manager.hasCircularDependency("entity2"));
//        assertTrue(manager.hasCircularDependency("action1"));
//    }
//
//    @Test
//    public void testGetRootElements() {
//        manager.registerElement("entity1", node1.getFullyQualifiedName(), node1.getDependencyType(), null);
//        manager.registerElement("entity2", node2.getFullyQualifiedName(), node2.getDependencyType(), null);
//        manager.registerElement("action1", node3.getFullyQualifiedName(), node3.getDependencyType(), null);
//
//        // 建立依赖关系: entity1 -> entity2, action1是独立的
//        manager.addDependency("entity1", "entity2");
//
//        Set<CsdlDependencyNode> roots = manager.getRootNodes();
//        assertEquals(2, roots.size());
//
//        // Get the actual registered nodes
//        CsdlDependencyNode entity1Node = manager.getElement("entity1");
//        CsdlDependencyNode action1Node = manager.getElement("action1");
//
//        assertNotNull("entity1 node should not be null", entity1Node);
//        assertNotNull("action1 node should not be null", action1Node);
//
//        // entity1是根节点（没有被其他节点依赖），action1也是根节点（独立
//        assertTrue("roots should contain entity1", roots.contains(entity1Node));
//        assertTrue("roots should contain action1", roots.contains(action1Node));
//    }
//
//    @Test
//    public void testGetLeafElements() {
//        manager.registerElement("entity1", node1.getFullyQualifiedName(), node1.getDependencyType(), null);
//        manager.registerElement("entity2", node2.getFullyQualifiedName(), node2.getDependencyType(), null);
//        manager.registerElement("action1", node3.getFullyQualifiedName(), node3.getDependencyType(), null);
//
//        // 建立依赖关系: entity1 -> entity2, action1是独立的
//        manager.addDependency("entity1", "entity2");
//
//        Set<CsdlDependencyNode> leaves = manager.getLeafNodes();
//        assertEquals(2, leaves.size());
//
//        // Get the actual registered nodes
//        CsdlDependencyNode entity2Node = manager.getElement("entity2");
//        CsdlDependencyNode action1Node = manager.getElement("action1");
//
//        assertNotNull(entity2Node);
//        assertNotNull(action1Node);
//
//        // entity2是叶子节点（没有依赖其他节点），action1也是叶子节点（独立）
//        assertTrue(leaves.contains(entity2Node));
//        assertTrue(leaves.contains(action1Node));
//    }
//
//    @Test
//    public void testGetTopologicalOrder() {
//        manager.registerElement("entity1", node1.getFullyQualifiedName(), node1.getDependencyType(), null);
//        manager.registerElement("entity2", node2.getFullyQualifiedName(), node2.getDependencyType(), null);
//        manager.registerElement("action1", node3.getFullyQualifiedName(), node3.getDependencyType(), null);
//
//        // 建立依赖关系: entity1 -> entity2 -> action1
//        manager.addDependency("entity1", "entity2");
//        manager.addDependency("entity2", "action1");
//
//        List<CsdlDependencyNode> order = manager.getTopologicalOrder();
//        assertNotNull(order);
//        assertEquals(3, order.size());
//
//        // Get the actual registered nodes
//        CsdlDependencyNode entity1Node = manager.getElement("entity1");
//        CsdlDependencyNode entity2Node = manager.getElement("entity2");
//        CsdlDependencyNode action1Node = manager.getElement("action1");
//
//        // 验证拓扑排序的正确性：拓扑排序应该从叶子节点到根节
//        // action1（叶子）-> entity2 -> entity1（根
//        int entity1Index = order.indexOf(entity1Node);
//        int entity2Index = order.indexOf(entity2Node);
//        int action1Index = order.indexOf(action1Node);
//
//        assertTrue(entity1Index >= 0);
//        assertTrue(entity2Index >= 0);
//        assertTrue(action1Index >= 0);
//
//        // 拓扑排序应该把被依赖的节点（依赖目标）排在前
//        assertTrue("action1 should come before entity2", action1Index < entity2Index);
//        assertTrue("entity2 should come before entity1", entity2Index < entity1Index);
//    }
//
//    @Test
//    public void testUnregisterElement() {
//        manager.registerElement("entity1", node1.getFullyQualifiedName(), node1.getDependencyType(), null);
//        manager.registerElement("entity2", node2.getFullyQualifiedName(), node2.getDependencyType(), null);
//
//        manager.addDependency("entity1", "entity2");
//
//        // 验证元素存在
//        assertNotNull(manager.getElement("entity1"));
//        assertNotNull(manager.getElement("entity2"));
//
//        // 注销元素
//        boolean result = manager.unregisterElement("entity1");
//        assertTrue(result);
//
//        // 验证元素已移除
//        assertNull(manager.getElement("entity1"));
//        assertNotNull(manager.getElement("entity2"));
//
//        // 验证依赖关系已清除
//        Set<CsdlDependencyNode> dependents = manager.getDirectDependents("entity2");
//        assertEquals(0, dependents.size());
//    }
//}
