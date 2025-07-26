package org.apache.olingo.schemamanager.analyzer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test class for DependencyTreeNode
 */
@DisplayName("Dependency Tree Node Tests")
public class DependencyTreeNodeTest {
    
    private DependencyTreeNode node1;
    private DependencyTreeNode node2;
    private DependencyTreeNode node3;
    
    @BeforeEach
    void setUp() {
        node1 = new DependencyTreeNode("Entity1", "com.example.Entity1", 
                                     DependencyTreeNode.ElementType.ENTITY_TYPE, null);
        node2 = new DependencyTreeNode("Entity2", "com.example.Entity2", 
                                     DependencyTreeNode.ElementType.ENTITY_TYPE, null);
        node3 = new DependencyTreeNode("Property1", "com.example.Entity1.Property1", 
                                     DependencyTreeNode.ElementType.PROPERTY, "com.example.Entity1");
    }
    
    @Test
    @DisplayName("Should create node with correct properties")
    void testNodeCreation() {
        assertEquals("Entity1", node1.getElementName());
        assertEquals("com.example.Entity1", node1.getFullQualifiedName());
        assertEquals(DependencyTreeNode.ElementType.ENTITY_TYPE, node1.getElementType());
        assertNull(node1.getParentElementName());
        assertNull(node1.getMetadata());
        
        assertEquals("Property1", node3.getElementName());
        assertEquals("com.example.Entity1.Property1", node3.getFullQualifiedName());
        assertEquals(DependencyTreeNode.ElementType.PROPERTY, node3.getElementType());
        assertEquals("com.example.Entity1", node3.getParentElementName());
    }
    
    @Test
    @DisplayName("Should create node with metadata")
    void testNodeCreationWithMetadata() {
        String metadata = "test metadata";
        DependencyTreeNode nodeWithMetadata = new DependencyTreeNode(
            "TestNode", "com.example.TestNode", 
            DependencyTreeNode.ElementType.COMPLEX_TYPE, null, metadata);
        
        assertEquals(metadata, nodeWithMetadata.getMetadata());
    }
    
    @Test
    @DisplayName("Should add and remove dependencies correctly")
    void testAddRemoveDependencies() {
        // Initially no dependencies
        assertFalse(node1.hasDependencies());
        assertTrue(node1.getDependencies().isEmpty());
        
        // Add dependency
        node1.addDependency(node2);
        assertTrue(node1.hasDependencies());
        assertEquals(1, node1.getDependencies().size());
        assertTrue(node1.getDependencies().contains(node2));
        
        // Check bidirectional relationship
        assertTrue(node2.hasDependents());
        assertTrue(node2.getDependents().contains(node1));
        
        // Remove dependency
        node1.removeDependency(node2);
        assertFalse(node1.hasDependencies());
        assertFalse(node2.hasDependents());
    }
    
    @Test
    @DisplayName("Should handle duplicate dependencies correctly")
    void testDuplicateDependencies() {
        // Add same dependency twice
        node1.addDependency(node2);
        node1.addDependency(node2);
        
        // Should only be added once
        assertEquals(1, node1.getDependencies().size());
        assertEquals(1, node2.getDependents().size());
    }
    
    @Test
    @DisplayName("Should handle null dependencies gracefully")
    void testNullDependencies() {
        // Adding null dependency should be handled gracefully
        node1.addDependency(null);
        assertFalse(node1.hasDependencies());
        
        // Removing null dependency should be handled gracefully
        node1.removeDependency(null);
        assertFalse(node1.hasDependencies());
    }
    
    @Test
    @DisplayName("Should get all transitive dependencies correctly")
    void testGetAllDependencies() {
        // Create dependency chain: node1 -> node2 -> node3
        node1.addDependency(node2);
        node2.addDependency(node3);
        
        // Get all dependencies for node1
        List<DependencyTreeNode> allDeps = node1.getAllDependencies();
        
        // Should contain both node2 and node3
        assertEquals(2, allDeps.size());
        assertTrue(allDeps.contains(node2));
        assertTrue(allDeps.contains(node3));
    }
    
    @Test
    @DisplayName("Should get all transitive dependents correctly")
    void testGetAllDependents() {
        // Create dependency chain: node1 -> node2 -> node3
        node1.addDependency(node2);
        node2.addDependency(node3);
        
        // Get all dependents for node3
        List<DependencyTreeNode> allDependents = node3.getAllDependents();
        
        // Should contain both node2 and node1
        assertEquals(2, allDependents.size());
        assertTrue(allDependents.contains(node2));
        assertTrue(allDependents.contains(node1));
    }
    
    @Test
    @DisplayName("Should handle circular dependencies in traversal")
    void testCircularDependencyHandling() {
        // Create circular dependency: node1 -> node2 -> node1
        node1.addDependency(node2);
        node2.addDependency(node1);
        
        // Should not cause infinite loop
        List<DependencyTreeNode> deps1 = node1.getAllDependencies();
        List<DependencyTreeNode> deps2 = node2.getAllDependencies();
        
        // Should handle circular reference gracefully
        assertNotNull(deps1);
        assertNotNull(deps2);
        assertTrue(deps1.contains(node2));
        assertTrue(deps2.contains(node1));
    }
    
    @Test
    @DisplayName("Should correctly identify leaf and root nodes")
    void testLeafAndRootIdentification() {
        // Initially all nodes are both leaf and root
        assertTrue(node1.isLeaf());
        assertTrue(node1.isRoot());
        assertTrue(node2.isLeaf());
        assertTrue(node2.isRoot());
        
        // Create dependency: node1 -> node2
        node1.addDependency(node2);
        
        // node1 is no longer leaf, node2 is no longer root
        assertFalse(node1.isLeaf());
        assertTrue(node1.isRoot());
        assertTrue(node2.isLeaf());
        assertFalse(node2.isRoot());
    }
    
    @Test
    @DisplayName("Should manage tags correctly")
    void testTagManagement() {
        // Initially no tags
        assertTrue(node1.getTags().isEmpty());
        assertFalse(node1.hasTag("test"));
        
        // Add tag
        node1.addTag("test");
        assertTrue(node1.hasTag("test"));
        assertEquals(1, node1.getTags().size());
        
        // Add another tag
        node1.addTag("important");
        assertTrue(node1.hasTag("important"));
        assertEquals(2, node1.getTags().size());
        
        // Remove tag
        node1.removeTag("test");
        assertFalse(node1.hasTag("test"));
        assertTrue(node1.hasTag("important"));
        assertEquals(1, node1.getTags().size());
        
        // Handle null tag gracefully
        node1.addTag(null);
        assertEquals(1, node1.getTags().size());
    }
    
    @Test
    @DisplayName("Should calculate depth correctly")
    void testDepthCalculation() {
        // Single node has depth 0
        assertEquals(0, node1.getDepth());
        
        // Create chain: node1 -> node2 -> node3
        node1.addDependency(node2);
        node2.addDependency(node3);
        
        // Check depths
        assertEquals(2, node1.getDepth()); // Depends on node2 (depth 1) which depends on node3 (depth 0)
        assertEquals(1, node2.getDepth()); // Depends on node3 (depth 0)
        assertEquals(0, node3.getDepth()); // No dependencies
    }
    
    @Test
    @DisplayName("Should handle circular dependencies in depth calculation")
    void testDepthCalculationWithCircularDependency() {
        // Create circular dependency: node1 -> node2 -> node1
        node1.addDependency(node2);
        node2.addDependency(node1);
        
        // Should not cause infinite loop
        int depth1 = node1.getDepth();
        int depth2 = node2.getDepth();
        
        // Should return some finite depth
        assertTrue(depth1 >= 0);
        assertTrue(depth2 >= 0);
    }
    
    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void testEqualsAndHashCode() {
        // Create another node with same FQN and type
        DependencyTreeNode sameNode = new DependencyTreeNode("Entity1", "com.example.Entity1", 
                                                            DependencyTreeNode.ElementType.ENTITY_TYPE, null);
        
        // Should be equal
        assertEquals(node1, sameNode);
        assertEquals(node1.hashCode(), sameNode.hashCode());
        
        // Different FQN should not be equal
        DependencyTreeNode differentNode = new DependencyTreeNode("Entity2", "com.example.Entity2", 
                                                                 DependencyTreeNode.ElementType.ENTITY_TYPE, null);
        assertNotEquals(node1, differentNode);
        
        // Different type should not be equal
        DependencyTreeNode differentTypeNode = new DependencyTreeNode("Entity1", "com.example.Entity1", 
                                                                     DependencyTreeNode.ElementType.COMPLEX_TYPE, null);
        assertNotEquals(node1, differentTypeNode);
        
        // Test reflexivity
        assertEquals(node1, node1);
        
        // Test null
        assertNotEquals(node1, null);
        
        // Test different class
        assertNotEquals(node1, "string");
    }
    
    @Test
    @DisplayName("Should have meaningful toString representation")
    void testToString() {
        String toString = node1.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("com.example.Entity1"));
        assertTrue(toString.contains("ENTITY_TYPE"));
        assertTrue(toString.contains("deps="));
        assertTrue(toString.contains("dependents="));
    }
}
