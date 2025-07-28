package org.apache.olingo.schema.processor.model.dependency;

import java.util.List;
import java.util.Set;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * CsdlDependencyNode的单元测试
 */
public class CsdlDependencyNodeTest {
    
    private CsdlDependencyNode sourceNode;
    private CsdlDependencyNode targetNode1;
    private CsdlDependencyNode targetNode2;
    private CsdlDependencyNode targetNode3;
    
    @BeforeEach
    public void setUp() {
        sourceNode = new CsdlDependencyNode(
            new FullQualifiedName("test.namespace", "SourceEntity"),
            CsdlDependencyNode.DependencyType.TYPE_REFERENCE
        );
        
        targetNode1 = new CsdlDependencyNode(
            new FullQualifiedName("target.namespace", "TargetEntity1"),
            CsdlDependencyNode.DependencyType.ENTITY_SET
        );
        
        targetNode2 = new CsdlDependencyNode(
            new FullQualifiedName("target.namespace", "TargetEntity2"),
            CsdlDependencyNode.DependencyType.ACTION_REFERENCE
        );
        
        targetNode3 = new CsdlDependencyNode(
            new FullQualifiedName("target.namespace", "TargetEntity3"),
            CsdlDependencyNode.DependencyType.BASE_TYPE
        );
    }
    
    @Test
    public void testBasicNodeCreation() {
        assertNotNull(sourceNode);
        assertEquals("SourceEntity", sourceNode.getName());
        assertEquals(new FullQualifiedName("test.namespace", "SourceEntity"), sourceNode.getFullyQualifiedName());
        assertEquals(CsdlDependencyNode.DependencyType.TYPE_REFERENCE, sourceNode.getDependencyType());
        assertTrue(sourceNode.getDependencies().isEmpty());
        assertTrue(sourceNode.getDependents().isEmpty());
    }
    
    @Test
    public void testNodeCreationWithProperty() {
        CsdlDependencyNode nodeWithProperty = new CsdlDependencyNode(
            "EntityWithProperty",
            new FullQualifiedName("test.namespace", "EntityWithProperty"),
            CsdlDependencyNode.DependencyType.NAVIGATION_TARGET,
            "navigationProperty"
        );
        
        assertEquals("EntityWithProperty", nodeWithProperty.getName());
        assertEquals("navigationProperty", nodeWithProperty.getPropertyName());
        assertEquals(CsdlDependencyNode.DependencyType.NAVIGATION_TARGET, nodeWithProperty.getDependencyType());
    }
    
    @Test
    public void testAddDependency() {
        sourceNode.addDependency(targetNode1);
        
        assertTrue(sourceNode.getDependencies().contains(targetNode1));
        assertTrue(targetNode1.getDependents().contains(sourceNode));
        assertEquals(1, sourceNode.getDependencies().size());
        assertEquals(1, targetNode1.getDependents().size());
    }
    
    @Test
    public void testAddMultipleDependencies() {
        sourceNode.addDependency(targetNode1);
        sourceNode.addDependency(targetNode2);
        
        assertEquals(2, sourceNode.getDependencies().size());
        assertTrue(sourceNode.getDependencies().contains(targetNode1));
        assertTrue(sourceNode.getDependencies().contains(targetNode2));
        
        assertTrue(targetNode1.getDependents().contains(sourceNode));
        assertTrue(targetNode2.getDependents().contains(sourceNode));
    }
    
    @Test
    public void testRemoveDependency() {
        sourceNode.addDependency(targetNode1);
        sourceNode.addDependency(targetNode2);
        
        boolean removed = sourceNode.removeDependency(targetNode1);
        
        assertTrue(removed);
        assertFalse(sourceNode.getDependencies().contains(targetNode1));
        assertTrue(sourceNode.getDependencies().contains(targetNode2));
        assertFalse(targetNode1.getDependents().contains(sourceNode));
    }
    
    @Test
    public void testAddDependent() {
        targetNode1.addDependent(sourceNode);
        
        assertTrue(targetNode1.getDependents().contains(sourceNode));
        assertTrue(sourceNode.getDependencies().contains(targetNode1));
    }
    
    @Test
    public void testRemoveDependent() {
        targetNode1.addDependent(sourceNode);
        boolean removed = targetNode1.removeDependent(sourceNode);
        
        assertTrue(removed);
        assertFalse(targetNode1.getDependents().contains(sourceNode));
        assertFalse(sourceNode.getDependencies().contains(targetNode1));
    }
    
    @Test
    public void testPreventSelfDependency() {
        sourceNode.addDependency(sourceNode);
        
        assertTrue(sourceNode.getDependencies().isEmpty());
        assertTrue(sourceNode.getDependents().isEmpty());
    }
    
    @Test
    public void testGetAllDependencies() {
        // 构建依赖链：sourceNode -> targetNode1 -> targetNode2 -> targetNode3
        sourceNode.addDependency(targetNode1);
        targetNode1.addDependency(targetNode2);
        targetNode2.addDependency(targetNode3);
        
        Set<CsdlDependencyNode> allDependencies = sourceNode.getAllDependencies();
        
        assertEquals(3, allDependencies.size());
        assertTrue(allDependencies.contains(targetNode1));
        assertTrue(allDependencies.contains(targetNode2));
        assertTrue(allDependencies.contains(targetNode3));
        assertFalse(allDependencies.contains(sourceNode));
    }
    
    @Test
    public void testGetAllDependents() {
        // 构建依赖链：targetNode3 <- targetNode2 <- targetNode1 <- sourceNode
        sourceNode.addDependency(targetNode1);
        targetNode1.addDependency(targetNode2);
        targetNode2.addDependency(targetNode3);
        
        Set<CsdlDependencyNode> allDependents = targetNode3.getAllDependents();
        
        assertEquals(3, allDependents.size());
        assertTrue(allDependents.contains(sourceNode));
        assertTrue(allDependents.contains(targetNode1));
        assertTrue(allDependents.contains(targetNode2));
        assertFalse(allDependents.contains(targetNode3));
    }
    
    @Test
    public void testGetDependencyPath() {
        // 构建依赖链：sourceNode -> targetNode1 -> targetNode2 -> targetNode3
        sourceNode.addDependency(targetNode1);
        targetNode1.addDependency(targetNode2);
        targetNode2.addDependency(targetNode3);
        
        List<CsdlDependencyNode> path = sourceNode.getDependencyPath(targetNode3);
        
        assertNotNull(path);
        assertEquals(4, path.size());
        assertEquals(sourceNode, path.get(0));
        assertEquals(targetNode1, path.get(1));
        assertEquals(targetNode2, path.get(2));
        assertEquals(targetNode3, path.get(3));
    }
    
    @Test
    public void testGetDependencyPathNotFound() {
        sourceNode.addDependency(targetNode1);
        
        List<CsdlDependencyNode> path = sourceNode.getDependencyPath(targetNode3);
        
        assertNull(path);
    }
    
    @Test
    public void testCircularDependencyDetection() {
        // 创建循环依赖：sourceNode -> targetNode1 -> targetNode2 -> sourceNode
        sourceNode.addDependency(targetNode1);
        targetNode1.addDependency(targetNode2);
        targetNode2.addDependency(sourceNode);
        
        assertTrue(sourceNode.hasCircularDependency());
        assertTrue(targetNode1.hasCircularDependency());
        assertTrue(targetNode2.hasCircularDependency());
    }
    
    @Test
    public void testNoCircularDependency() {
        // 创建非循环依赖：sourceNode -> targetNode1 -> targetNode2
        sourceNode.addDependency(targetNode1);
        targetNode1.addDependency(targetNode2);
        
        assertFalse(sourceNode.hasCircularDependency());
        assertFalse(targetNode1.hasCircularDependency());
        assertFalse(targetNode2.hasCircularDependency());
    }
    
    @Test
    public void testCircularDependencyWithSelfLoop() {
        // 间接循环依赖检测已经在testPreventSelfDependency中测试了自环
        // 这里测试更复杂的情况
        sourceNode.addDependency(targetNode1);
        targetNode1.addDependency(targetNode2);
        targetNode2.addDependency(targetNode1); // 创建子循环
        
        assertTrue(sourceNode.hasCircularDependency());
    }
    
    @Test
    public void testEquals() {
        CsdlDependencyNode node1 = new CsdlDependencyNode(
            new FullQualifiedName("test.namespace", "TestEntity"),
            CsdlDependencyNode.DependencyType.TYPE_REFERENCE
        );
        
        CsdlDependencyNode node2 = new CsdlDependencyNode(
            new FullQualifiedName("test.namespace", "TestEntity"),
            CsdlDependencyNode.DependencyType.TYPE_REFERENCE
        );
        
        assertEquals(node1, node2);
        assertEquals(node1.hashCode(), node2.hashCode());
    }
    
    @Test
    public void testNotEquals() {
        CsdlDependencyNode node1 = new CsdlDependencyNode(
            new FullQualifiedName("test.namespace", "TestEntity"),
            CsdlDependencyNode.DependencyType.TYPE_REFERENCE
        );
        
        CsdlDependencyNode node2 = new CsdlDependencyNode(
            new FullQualifiedName("test.namespace", "TestEntity"),
            CsdlDependencyNode.DependencyType.ENTITY_SET
        );
        
        assertNotEquals(node1, node2);
    }
    
    @Test
    public void testSetters() {
        sourceNode.setName("NewName");
        sourceNode.setPropertyName("newProperty");
        sourceNode.setDependencyType(CsdlDependencyNode.DependencyType.FUNCTION_REFERENCE);
        
        assertEquals("NewName", sourceNode.getName());
        assertEquals("newProperty", sourceNode.getPropertyName());
        assertEquals(CsdlDependencyNode.DependencyType.FUNCTION_REFERENCE, sourceNode.getDependencyType());
    }
    
    @Test
    public void testSetFullyQualifiedName() {
        FullQualifiedName newFqn = new FullQualifiedName("new.namespace", "NewEntity");
        sourceNode.setFullyQualifiedName(newFqn);
        
        assertEquals(newFqn, sourceNode.getFullyQualifiedName());
        assertEquals("NewEntity", sourceNode.getName());
    }
    
    @Test
    public void testToString() {
        sourceNode.setPropertyName("testProperty");
        String result = sourceNode.toString();
        
        assertTrue(result.contains("test.namespace.SourceEntity"));
        assertTrue(result.contains("TYPE_REFERENCE"));
        assertTrue(result.contains("testProperty"));
        assertTrue(result.contains("deps=0"));
        assertTrue(result.contains("dependents=0"));
    }
    
    @Test
    public void testAnnotations() {
        assertTrue(sourceNode.getAnnotations().isEmpty());
        
        // Note: We can't easily test annotations without creating CsdlAnnotation instances
        // This is acceptable for now as the main focus is on dependency functionality
    }
}
