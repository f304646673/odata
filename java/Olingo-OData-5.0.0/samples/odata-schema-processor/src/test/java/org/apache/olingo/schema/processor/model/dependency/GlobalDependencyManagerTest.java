package org.apache.olingo.schema.processor.model.dependency;

import java.util.Set;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class GlobalDependencyManagerTest {
    
    private GlobalDependencyManager manager;
    private CsdlDependencyNode node1;
    private CsdlDependencyNode node2;
    private CsdlDependencyNode node3;
    
    @Before
    public void setUp() {
        GlobalDependencyManager.resetInstance();
        manager = GlobalDependencyManager.getInstance();
        
        node1 = new CsdlDependencyNode(new FullQualifiedName("test", "Entity1"), 
                                      CsdlDependencyNode.DependencyType.ENTITY_TYPE);
        node2 = new CsdlDependencyNode(new FullQualifiedName("test", "Entity2"), 
                                      CsdlDependencyNode.DependencyType.ENTITY_TYPE);
        node3 = new CsdlDependencyNode(new FullQualifiedName("test", "Action1"), 
                                      CsdlDependencyNode.DependencyType.ACTION_REFERENCE);
    }
    
    @After
    public void tearDown() {
        GlobalDependencyManager.resetInstance();
    }
    
    @Test
    public void testSingletonPattern() {
        GlobalDependencyManager manager1 = GlobalDependencyManager.getInstance();
        GlobalDependencyManager manager2 = GlobalDependencyManager.getInstance();
        
        assertTrue(manager1 == manager2); // 同一个实例
    }
    
    @Test
    public void testRegisterElement() {
        CsdlDependencyNode registered = manager.registerElement("entity1", 
                                                               node1.getFullyQualifiedName(), 
                                                               node1.getDependencyType());
        
        assertNotNull(registered);
        assertEquals("Entity1", registered.getName());
        assertEquals(CsdlDependencyNode.DependencyType.ENTITY_TYPE, registered.getDependencyType());
        
        // 重复注册应该返回相同实例
        CsdlDependencyNode registered2 = manager.registerElement("entity1", 
                                                                node1.getFullyQualifiedName(), 
                                                                node1.getDependencyType());
        assertTrue(registered == registered2);
    }
    
    @Test
    public void testGetElement() {
        manager.registerElement("entity1", node1.getFullyQualifiedName(), node1.getDependencyType());
        
        CsdlDependencyNode retrieved = manager.getElement("entity1");
        assertNotNull(retrieved);
        assertEquals("Entity1", retrieved.getName());
        
        CsdlDependencyNode notFound = manager.getElement("nonexistent");
        assertNull(notFound);
    }
    
    @Test
    public void testAddDependency() {
        manager.registerElement("entity1", node1.getFullyQualifiedName(), node1.getDependencyType());
        manager.registerElement("entity2", node2.getFullyQualifiedName(), node2.getDependencyType());
        
        assertTrue(manager.addDependency("entity1", "entity2"));
        
        Set<CsdlDependencyNode> deps = manager.getDirectDependencies("entity1");
        assertEquals(1, deps.size());
        assertTrue(deps.contains(manager.getElement("entity2")));
        
        Set<CsdlDependencyNode> dependents = manager.getDirectDependents("entity2");
        assertEquals(1, dependents.size());
        assertTrue(dependents.contains(manager.getElement("entity1")));
    }
    
    @Test
    public void testAddDependencyWithInvalidIds() {
        manager.registerElement("entity1", node1.getFullyQualifiedName(), node1.getDependencyType());
        
        assertFalse(manager.addDependency("entity1", "nonexistent"));
        assertFalse(manager.addDependency("nonexistent", "entity1"));
        assertFalse(manager.addDependency("nonexistent1", "nonexistent2"));
    }
    
    @Test
    public void testRemoveDependency() {
        manager.registerElement("entity1", node1.getFullyQualifiedName(), node1.getDependencyType());
        manager.registerElement("entity2", node2.getFullyQualifiedName(), node2.getDependencyType());
        
        manager.addDependency("entity1", "entity2");
        assertTrue(manager.removeDependency("entity1", "entity2"));
        
        Set<CsdlDependencyNode> deps = manager.getDirectDependencies("entity1");
        assertTrue(deps.isEmpty());
        
        Set<CsdlDependencyNode> dependents = manager.getDirectDependents("entity2");
        assertTrue(dependents.isEmpty());
    }
    
    @Test
    public void testGetElementsByType() {
        manager.registerElement("entity1", node1.getFullyQualifiedName(), node1.getDependencyType());
        manager.registerElement("entity2", node2.getFullyQualifiedName(), node2.getDependencyType());
        manager.registerElement("action1", node3.getFullyQualifiedName(), node3.getDependencyType());
        
        Set<CsdlDependencyNode> entityTypes = manager.getElementsByType(CsdlDependencyNode.DependencyType.ENTITY_TYPE);
        assertEquals(2, entityTypes.size());
        
        Set<CsdlDependencyNode> actionRefs = manager.getElementsByType(CsdlDependencyNode.DependencyType.ACTION_REFERENCE);
        assertEquals(1, actionRefs.size());
    }
    
    @Test
    public void testGetElementsByNamespace() {
        manager.registerElement("entity1", node1.getFullyQualifiedName(), node1.getDependencyType());
        manager.registerElement("entity2", node2.getFullyQualifiedName(), node2.getDependencyType());
        manager.registerElement("action1", node3.getFullyQualifiedName(), node3.getDependencyType());
        
        Set<CsdlDependencyNode> testNamespace = manager.getElementsByNamespace("test");
        assertEquals(3, testNamespace.size());
        
        Set<CsdlDependencyNode> otherNamespace = manager.getElementsByNamespace("other");
        assertTrue(otherNamespace.isEmpty());
    }
    
    @Test
    public void testGetAllDependencies() {
        manager.registerElement("entity1", node1.getFullyQualifiedName(), node1.getDependencyType());
        manager.registerElement("entity2", node2.getFullyQualifiedName(), node2.getDependencyType());
        manager.registerElement("action1", node3.getFullyQualifiedName(), node3.getDependencyType());
        
        manager.addDependency("entity1", "entity2");
        manager.addDependency("action1", "entity1");
        
        Set<CsdlDependencyNode> allDeps = manager.getAllDependencies("action1");
        assertEquals(2, allDeps.size()); // entity1 and entity2
        
        Set<CsdlDependencyNode> allDependents = manager.getAllDependents("entity2");
        assertEquals(2, allDependents.size()); // entity1 and action1
    }
    
    @Test
    public void testGetDependencyPath() {
        manager.registerElement("entity1", node1.getFullyQualifiedName(), node1.getDependencyType());
        manager.registerElement("entity2", node2.getFullyQualifiedName(), node2.getDependencyType());
        manager.registerElement("action1", node3.getFullyQualifiedName(), node3.getDependencyType());
        
        manager.addDependency("action1", "entity1");
        manager.addDependency("entity1", "entity2");
        
        var path = manager.getDependencyPath("action1", "entity2");
        assertNotNull(path);
        assertEquals(3, path.size());
        assertEquals("Action1", path.get(0).getName());
        assertEquals("Entity1", path.get(1).getName());
        assertEquals("Entity2", path.get(2).getName());
    }
    
    @Test
    public void testGetRootAndLeafNodes() {
        manager.registerElement("entity1", node1.getFullyQualifiedName(), node1.getDependencyType());
        manager.registerElement("entity2", node2.getFullyQualifiedName(), node2.getDependencyType());
        manager.registerElement("action1", node3.getFullyQualifiedName(), node3.getDependencyType());
        
        manager.addDependency("action1", "entity1");
        manager.addDependency("entity1", "entity2");
        
        Set<CsdlDependencyNode> rootNodes = manager.getRootNodes();
        assertEquals(1, rootNodes.size());
        assertEquals("Entity2", rootNodes.iterator().next().getName());
        
        Set<CsdlDependencyNode> leafNodes = manager.getLeafNodes();
        assertEquals(1, leafNodes.size());
        assertEquals("Action1", leafNodes.iterator().next().getName());
    }
    
    @Test
    public void testGetTopologicalOrder() {
        manager.registerElement("entity1", node1.getFullyQualifiedName(), node1.getDependencyType());
        manager.registerElement("entity2", node2.getFullyQualifiedName(), node2.getDependencyType());
        manager.registerElement("action1", node3.getFullyQualifiedName(), node3.getDependencyType());
        
        manager.addDependency("action1", "entity1");
        manager.addDependency("entity1", "entity2");
        
        var topologicalOrder = manager.getTopologicalOrder();
        assertEquals(3, topologicalOrder.size());
        
        // 验证拓扑顺序：entity2 -> entity1 -> action1
        int index1 = topologicalOrder.indexOf(manager.getElement("entity1"));
        int index2 = topologicalOrder.indexOf(manager.getElement("entity2"));
        int indexAction = topologicalOrder.indexOf(manager.getElement("action1"));
        
        assertTrue(index2 < index1);
        assertTrue(index1 < indexAction);
    }
    
    @Test
    public void testCircularDependencyDetection() {
        manager.registerElement("entity1", node1.getFullyQualifiedName(), node1.getDependencyType());
        manager.registerElement("entity2", node2.getFullyQualifiedName(), node2.getDependencyType());
        
        manager.addDependency("entity1", "entity2");
        assertFalse(manager.hasCircularDependencies());
        
        manager.addDependency("entity2", "entity1"); // 创建循环
        assertTrue(manager.hasCircularDependencies());
        assertTrue(manager.hasCircularDependency("entity1"));
        assertTrue(manager.hasCircularDependency("entity2"));
    }
    
    @Test
    public void testUnregisterElement() {
        manager.registerElement("entity1", node1.getFullyQualifiedName(), node1.getDependencyType());
        manager.registerElement("entity2", node2.getFullyQualifiedName(), node2.getDependencyType());
        
        manager.addDependency("entity1", "entity2");
        
        assertTrue(manager.unregisterElement("entity1"));
        assertNull(manager.getElement("entity1"));
        
        // 验证依赖关系被清除
        Set<CsdlDependencyNode> dependents = manager.getDirectDependents("entity2");
        assertTrue(dependents.isEmpty());
    }
    
    @Test
    public void testClear() {
        manager.registerElement("entity1", node1.getFullyQualifiedName(), node1.getDependencyType());
        manager.registerElement("entity2", node2.getFullyQualifiedName(), node2.getDependencyType());
        manager.addDependency("entity1", "entity2");
        
        manager.clear();
        
        assertNull(manager.getElement("entity1"));
        assertNull(manager.getElement("entity2"));
        assertTrue(manager.getRootNodes().isEmpty());
        assertTrue(manager.getLeafNodes().isEmpty());
    }
    
    @Test
    public void testGetStatistics() {
        manager.registerElement("entity1", node1.getFullyQualifiedName(), node1.getDependencyType());
        manager.registerElement("entity2", node2.getFullyQualifiedName(), node2.getDependencyType());
        manager.addDependency("entity1", "entity2");
        
        String statistics = manager.getStatistics();
        assertTrue(statistics.contains("nodes=2"));
        assertTrue(statistics.contains("circular=false"));
    }
}
