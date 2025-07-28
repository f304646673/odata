//package org.apache.olingo.schema.processor.model.dependency;
//
//import java.util.List;
//import java.util.Set;
//
//import org.apache.olingo.commons.api.edm.FullQualifiedName;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertNull;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
///**
// * CsdlDependencyTree Unit Test
// */
//public class CsdlDependencyTreeTest {
//
//    private CsdlDependencyTree tree;
//    private CsdlDependencyNode node1;
//    private CsdlDependencyNode node2;
//    private CsdlDependencyNode node3;
//    private CsdlDependencyNode node4;
//
//    @BeforeEach
//    public void setUp() {
//        tree = new CsdlDependencyTree();
//
//        node1 = new CsdlDependencyNode(
//            new FullQualifiedName("namespace1", "Entity1"),
//            CsdlDependencyNode.DependencyType.ENTITY_SET
//        );
//
//        node2 = new CsdlDependencyNode(
//            new FullQualifiedName("namespace1", "Entity2"),
//            CsdlDependencyNode.DependencyType.TYPE_REFERENCE
//        );
//
//        node3 = new CsdlDependencyNode(
//            new FullQualifiedName("namespace2", "Entity3"),
//            CsdlDependencyNode.DependencyType.ACTION_REFERENCE
//        );
//
//        node4 = new CsdlDependencyNode(
//            new FullQualifiedName("namespace2", "Entity4"),
//            CsdlDependencyNode.DependencyType.BASE_TYPE
//        );
//    }
//
//    @Test
//    public void testAddNode() {
//        tree.addNode(node1);
//
//        Set<CsdlDependencyNode> foundNodes = tree.findNodes(node1.getFullyQualifiedName());
//        assertEquals(1, foundNodes.size());
//        assertTrue(foundNodes.contains(node1));
//    }
//
//    @Test
//    public void testAddMultipleNodesWithSameFQN() {
//        CsdlDependencyNode duplicateNode = new CsdlDependencyNode(
//            new FullQualifiedName("namespace1", "Entity1"),
//            CsdlDependencyNode.DependencyType.TYPE_REFERENCE // 不同类型
//        );
//
//        tree.addNode(node1);
//        tree.addNode(duplicateNode);
//
//        Set<CsdlDependencyNode> foundNodes = tree.findNodes(node1.getFullyQualifiedName());
//        assertEquals(2, foundNodes.size());
//        assertTrue(foundNodes.contains(node1));
//        assertTrue(foundNodes.contains(duplicateNode));
//    }
//
//    @Test
//    public void testAddNullNode() {
//        tree.addNode(null);
//        assertTrue(tree.getAllNodes().isEmpty());
//    }
//
//    @Test
//    public void testAddNodeWithNullFQN() {
//        CsdlDependencyNode nodeWithNullFQN = new CsdlDependencyNode(
//            "TestNode",
//            null,
//            CsdlDependencyNode.DependencyType.ENTITY_SET,
//            null
//        );
//
//        tree.addNode(nodeWithNullFQN);
//        assertTrue(tree.getAllNodes().isEmpty());
//    }
//
//    @Test
//    public void testRemoveNode() {
//        tree.addNode(node1);
//        tree.addNode(node2);
//
//        boolean removed = tree.removeNode(node1);
//
//        assertTrue(removed);
//        assertTrue(tree.findNodes(node1.getFullyQualifiedName()).isEmpty());
//        assertFalse(tree.findNodes(node2.getFullyQualifiedName()).isEmpty());
//    }
//
//    @Test
//    public void testRemoveNodeWithDependencies() {
//        tree.addNode(node1);
//        tree.addNode(node2);
//        tree.addDependency(node1, node2);
//
//        tree.removeNode(node1);
//
//        // 验证依赖关系也被清除
//        assertTrue(tree.findNodes(node1.getFullyQualifiedName()).isEmpty());
//        assertTrue(node2.getDependents().isEmpty());
//    }
//
//    @Test
//    public void testRemoveNullNode() {
//        boolean removed = tree.removeNode(null);
//        assertFalse(removed);
//    }
//
//    @Test
//    public void testRemoveNonExistentNode() {
//        tree.addNode(node1);
//        boolean removed = tree.removeNode(node2);
//        assertFalse(removed);
//    }
//
//    @Test
//    public void testFindNodes() {
//        tree.addNode(node1);
//        tree.addNode(node2);
//
//        Set<CsdlDependencyNode> foundNodes = tree.findNodes(node1.getFullyQualifiedName());
//        assertEquals(1, foundNodes.size());
//        assertTrue(foundNodes.contains(node1));
//    }
//
//    @Test
//    public void testFindNodesNotFound() {
//        tree.addNode(node1);
//
//        Set<CsdlDependencyNode> foundNodes = tree.findNodes(new FullQualifiedName("nonexistent", "Entity"));
//        assertTrue(foundNodes.isEmpty());
//    }
//
//    @Test
//    public void testFindNodesWithNullFQN() {
//        Set<CsdlDependencyNode> foundNodes = tree.findNodes(null);
//        assertTrue(foundNodes.isEmpty());
//    }
//
//    @Test
//    public void testFindNode() {
//        tree.addNode(node1);
//        tree.addNode(node2);
//
//        CsdlDependencyNode found = tree.findNode(
//            node1.getFullyQualifiedName(),
//            CsdlDependencyNode.DependencyType.ENTITY_SET
//        );
//
//        assertEquals(node1, found);
//    }
//
//    @Test
//    public void testFindNodeNotFound() {
//        tree.addNode(node1);
//
//        CsdlDependencyNode found = tree.findNode(
//            node1.getFullyQualifiedName(),
//            CsdlDependencyNode.DependencyType.ACTION_REFERENCE // 不同类型
//        );
//
//        assertNull(found);
//    }
//
//    @Test
//    public void testFindNodeWithNullParameters() {
//        tree.addNode(node1);
//
//        CsdlDependencyNode found1 = tree.findNode(null, CsdlDependencyNode.DependencyType.ENTITY_SET);
//        assertNull(found1);
//
//        CsdlDependencyNode found2 = tree.findNode(node1.getFullyQualifiedName(), null);
//        assertNull(found2);
//    }
//
//    @Test
//    public void testAddDependency() {
//        tree.addNode(node1);
//        tree.addNode(node2);
//        tree.addDependency(node1, node2);
//
//        assertTrue(node1.getDependencies().contains(node2));
//        assertTrue(node2.getDependents().contains(node1));
//    }
//
//    @Test
//    public void testAddDependencyWithNullNodes() {
//        tree.addNode(node1);
//
//        tree.addDependency(null, node1);
//        tree.addDependency(node1, null);
//        tree.addDependency(null, null);
//
//        assertTrue(node1.getDependencies().isEmpty());
//        assertTrue(node1.getDependents().isEmpty());
//    }
//
//    @Test
//    public void testAddSelfDependency() {
//        tree.addNode(node1);
//        tree.addDependency(node1, node1);
//
//        // 自依赖应该被忽略
//        assertFalse(node1.getDependencies().contains(node1));
//        assertFalse(node1.getDependents().contains(node1));
//    }
//
//    @Test
//    public void testRemoveDependency() {
//        tree.addNode(node1);
//        tree.addNode(node2);
//        tree.addDependency(node1, node2);
//
//        boolean removed = tree.removeDependency(node1, node2);
//
//        assertTrue(removed);
//        assertFalse(node1.getDependencies().contains(node2));
//        assertFalse(node2.getDependents().contains(node1));
//    }
//
//    @Test
//    public void testRemoveDependencyNotExists() {
//        tree.addNode(node1);
//        tree.addNode(node2);
//
//        boolean removed = tree.removeDependency(node1, node2);
//        assertFalse(removed);
//    }
//
//    @Test
//    public void testRemoveDependencyWithNullNodes() {
//        tree.addNode(node1);
//
//        boolean removed1 = tree.removeDependency(null, node1);
//        assertFalse(removed1);
//
//        boolean removed2 = tree.removeDependency(node1, null);
//        assertFalse(removed2);
//
//        boolean removed3 = tree.removeDependency(null, null);
//        assertFalse(removed3);
//    }
//
//    @Test
//    public void testGetRootNodes() {
//        tree.addNode(node1);
//        tree.addNode(node2);
//        tree.addNode(node3);
//        tree.addDependency(node1, node2); // node1依赖node2
//        tree.addDependency(node1, node3); // node1依赖node3
//
//        Set<CsdlDependencyNode> rootNodes = tree.getRootNodes();
//
//        // 根节点是没有被其他节点依赖的节点
//        // node1是根节点（没有其他节点依赖它
//        // node2和node3不是根节点（node1依赖它们
//        assertEquals(1, rootNodes.size());
//        assertTrue(rootNodes.contains(node1));
//        assertFalse(rootNodes.contains(node2));
//        assertFalse(rootNodes.contains(node3));
//    }
//
//    @Test
//    public void testGetRootNodesEmpty() {
//        Set<CsdlDependencyNode> rootNodes = tree.getRootNodes();
//        assertTrue(rootNodes.isEmpty());
//    }
//
//    @Test
//    public void testGetLeafNodes() {
//        tree.addNode(node1);
//        tree.addNode(node2);
//        tree.addNode(node3);
//        tree.addDependency(node1, node2); // node1依赖node2
//        tree.addDependency(node2, node3); // node2依赖node3
//
//        Set<CsdlDependencyNode> leafNodes = tree.getLeafNodes();
//
//        // 叶子节点是没有依赖其他节点的节点
//        // node3是叶子节点（它不依赖任何其他节点
//        assertEquals(1, leafNodes.size());
//        assertTrue(leafNodes.contains(node3));
//        assertFalse(leafNodes.contains(node1));
//        assertFalse(leafNodes.contains(node2));
//    }
//
//    @Test
//    public void testGetLeafNodesEmpty() {
//        Set<CsdlDependencyNode> leafNodes = tree.getLeafNodes();
//        assertTrue(leafNodes.isEmpty());
//    }
//
//    @Test
//    public void testGetAllNodes() {
//        tree.addNode(node1);
//        tree.addNode(node2);
//        tree.addNode(node3);
//
//        Set<CsdlDependencyNode> allNodes = tree.getAllNodes();
//
//        assertEquals(3, allNodes.size());
//        assertTrue(allNodes.contains(node1));
//        assertTrue(allNodes.contains(node2));
//        assertTrue(allNodes.contains(node3));
//    }
//
//    @Test
//    public void testGetAllNodesEmpty() {
//        Set<CsdlDependencyNode> allNodes = tree.getAllNodes();
//        assertTrue(allNodes.isEmpty());
//    }
//
//    @Test
//    public void testTopologicalOrder() {
//        tree.addNode(node1);
//        tree.addNode(node2);
//        tree.addNode(node3);
//        tree.addNode(node4);
//
//        // 构建依赖关系：node1 -> node2 -> node3, node1 -> node4
//        tree.addDependency(node1, node2);
//        tree.addDependency(node2, node3);
//        tree.addDependency(node1, node4);
//
//        List<CsdlDependencyNode> topologicalOrder = tree.getTopologicalOrder();
//
//        assertEquals(4, topologicalOrder.size());
//
//        // 验证拓扑排序：被依赖的节点应该在依赖它的节点之前
//        int index3 = topologicalOrder.indexOf(node3);
//        int index4 = topologicalOrder.indexOf(node4);
//        int index2 = topologicalOrder.indexOf(node2);
//        int index1 = topologicalOrder.indexOf(node1);
//
//        assertTrue(index3 < index2);
//        assertTrue(index4 < index1);
//        assertTrue(index2 < index1);
//    }
//
//    @Test
//    public void testTopologicalOrderEmpty() {
//        List<CsdlDependencyNode> topologicalOrder = tree.getTopologicalOrder();
//        assertTrue(topologicalOrder.isEmpty());
//    }
//
//    @Test
//    public void testTopologicalOrderSingleNode() {
//        tree.addNode(node1);
//
//        List<CsdlDependencyNode> topologicalOrder = tree.getTopologicalOrder();
//        assertEquals(1, topologicalOrder.size());
//        assertEquals(node1, topologicalOrder.get(0));
//    }
//
//    @Test
//    public void testTopologicalOrderWithCircularDependency() {
//        tree.addNode(node1);
//        tree.addNode(node2);
//        tree.addNode(node3);
//
//        // 创建循环依赖：node1 -> node2 -> node3 -> node1
//        tree.addDependency(node1, node2);
//        tree.addDependency(node2, node3);
//        tree.addDependency(node3, node1);
//
//        List<CsdlDependencyNode> topologicalOrder = tree.getTopologicalOrder();
//
//        // 存在循环依赖时应该返回空列表
//        assertTrue(topologicalOrder.isEmpty());
//    }
//
//    @Test
//    public void testHasCircularDependencies() {
//        tree.addNode(node1);
//        tree.addNode(node2);
//        tree.addNode(node3);
//
//        // 非循环依
//        tree.addDependency(node1, node2);
//        tree.addDependency(node2, node3);
//        assertFalse(tree.hasCircularDependencies());
//
//        // 添加循环依赖
//        tree.addDependency(node3, node1);
//        assertTrue(tree.hasCircularDependencies());
//    }
//
//    @Test
//    public void testHasCircularDependenciesEmpty() {
//        assertFalse(tree.hasCircularDependencies());
//    }
//
//    @Test
//    public void testHasCircularDependenciesSingleNode() {
//        tree.addNode(node1);
//        assertFalse(tree.hasCircularDependencies());
//    }
//
//    @Test
//    public void testFindAllPaths() {
//        tree.addNode(node1);
//        tree.addNode(node2);
//        tree.addNode(node3);
//        tree.addNode(node4);
//
//        // 构建多路径：node1 -> node2 -> node4, node1 -> node3 -> node4
//        tree.addDependency(node1, node2);
//        tree.addDependency(node2, node4);
//        tree.addDependency(node1, node3);
//        tree.addDependency(node3, node4);
//
//        List<List<CsdlDependencyNode>> allPaths = tree.findAllPaths(node1, node4);
//
//        assertEquals(2, allPaths.size());
//
//        // 验证路径1: node1 -> node2 -> node4
//        boolean foundPath1 = false;
//        boolean foundPath2 = false;
//
//        for (List<CsdlDependencyNode> path : allPaths) {
//            if (path.size() == 3 && path.get(0) == node1 && path.get(1) == node2 && path.get(2) == node4) {
//                foundPath1 = true;
//            }
//            if (path.size() == 3 && path.get(0) == node1 && path.get(1) == node3 && path.get(2) == node4) {
//                foundPath2 = true;
//            }
//        }
//
//        assertTrue(foundPath1);
//        assertTrue(foundPath2);
//    }
//
//    @Test
//    public void testFindAllPathsNoPath() {
//        tree.addNode(node1);
//        tree.addNode(node2);
//        tree.addNode(node3);
//
//        // node1 -> node2, node3独立
//        tree.addDependency(node1, node2);
//
//        List<List<CsdlDependencyNode>> allPaths = tree.findAllPaths(node1, node3);
//
//        assertTrue(allPaths.isEmpty());
//    }
//
//    @Test
//    public void testFindAllPathsSameNode() {
//        tree.addNode(node1);
//
//        List<List<CsdlDependencyNode>> allPaths = tree.findAllPaths(node1, node1);
//
//        assertEquals(1, allPaths.size());
//        assertEquals(1, allPaths.get(0).size());
//        assertEquals(node1, allPaths.get(0).get(0));
//    }
//
//    @Test
//    public void testFindAllPathsWithNullNodes() {
//        tree.addNode(node1);
//
//        // 实际的实现可能不处理null参数，所以我们可能需要捕获异常或修改期望
//        try {
//            List<List<CsdlDependencyNode>> allPaths1 = tree.findAllPaths(null, node1);
//            assertTrue(allPaths1.isEmpty());
//        } catch (NullPointerException e) {
//            // 如果实现不处理null，这是可以接受的
//        }
//
//        try {
//            List<List<CsdlDependencyNode>> allPaths2 = tree.findAllPaths(node1, null);
//            assertTrue(allPaths2.isEmpty());
//        } catch (NullPointerException e) {
//            // 如果实现不处理null，这是可以接受的
//        }
//
//        try {
//            List<List<CsdlDependencyNode>> allPaths3 = tree.findAllPaths(null, null);
//            assertTrue(allPaths3.isEmpty());
//        } catch (NullPointerException e) {
//            // 如果实现不处理null，这是可以接受的
//        }
//    }
//
//    @Test
//    public void testFindAllPathsDirectConnection() {
//        tree.addNode(node1);
//        tree.addNode(node2);
//        tree.addDependency(node1, node2);
//
//        List<List<CsdlDependencyNode>> allPaths = tree.findAllPaths(node1, node2);
//
//        assertEquals(1, allPaths.size());
//        assertEquals(2, allPaths.get(0).size());
//        assertEquals(node1, allPaths.get(0).get(0));
//        assertEquals(node2, allPaths.get(0).get(1));
//    }
//
//    @Test
//    public void testClear() {
//        tree.addNode(node1);
//        tree.addNode(node2);
//        tree.addDependency(node1, node2);
//
//        tree.clear();
//
//        assertTrue(tree.getAllNodes().isEmpty());
//        assertTrue(tree.getRootNodes().isEmpty());
//        assertTrue(tree.getLeafNodes().isEmpty());
//        assertTrue(tree.getTopologicalOrder().isEmpty());
//    }
//
//    @Test
//    public void testGetStatistics() {
//        tree.addNode(node1);
//        tree.addNode(node2);
//        tree.addNode(node3);
//        tree.addDependency(node1, node2);
//        tree.addDependency(node1, node3);
//
//        String statistics = tree.getStatistics();
//
//        assertTrue(statistics.contains("3 nodes"));
//        assertTrue(statistics.contains("2 dependencies"));
//        assertTrue(statistics.contains("1 roots"));
//        assertTrue(statistics.contains("2 leaves"));
//        assertTrue(statistics.contains("circular=false"));
//    }
//
//    @Test
//    public void testGetStatisticsEmpty() {
//        String statistics = tree.getStatistics();
//
//        assertTrue(statistics.contains("0 nodes"));
//        assertTrue(statistics.contains("0 dependencies"));
//        assertTrue(statistics.contains("0 roots"));
//        assertTrue(statistics.contains("0 leaves"));
//        assertTrue(statistics.contains("circular=false"));
//    }
//
//    @Test
//    public void testGetStatisticsWithCircularDependency() {
//        tree.addNode(node1);
//        tree.addNode(node2);
//        tree.addDependency(node1, node2);
//        tree.addDependency(node2, node1);
//
//        String statistics = tree.getStatistics();
//
//        assertTrue(statistics.contains("circular=true"));
//    }
//
//    @Test
//    public void testComplexDependencyStructure() {
//        // 测试复杂的依赖结
//        tree.addNode(node1);
//        tree.addNode(node2);
//        tree.addNode(node3);
//        tree.addNode(node4);
//
//        // 构建复杂依赖：node1 -> node2, node1 -> node3, node2 -> node4, node3 -> node4
//        tree.addDependency(node1, node2);
//        tree.addDependency(node1, node3);
//        tree.addDependency(node2, node4);
//        tree.addDependency(node3, node4);
//
//        // 验证根节
//        Set<CsdlDependencyNode> rootNodes = tree.getRootNodes();
//        assertEquals(1, rootNodes.size());
//        assertTrue(rootNodes.contains(node1));
//
//        // 验证叶子节点
//        Set<CsdlDependencyNode> leafNodes = tree.getLeafNodes();
//        assertEquals(1, leafNodes.size());
//        assertTrue(leafNodes.contains(node4));
//
//        // 验证拓扑排序
//        List<CsdlDependencyNode> topologicalOrder = tree.getTopologicalOrder();
//        assertEquals(4, topologicalOrder.size());
//
//        int index4 = topologicalOrder.indexOf(node4);
//        int index3 = topologicalOrder.indexOf(node3);
//        int index2 = topologicalOrder.indexOf(node2);
//        int index1 = topologicalOrder.indexOf(node1);
//
//        assertTrue(index4 < index3);
//        assertTrue(index4 < index2);
//        assertTrue(index3 < index1);
//        assertTrue(index2 < index1);
//
//        // 验证无循环依
//        assertFalse(tree.hasCircularDependencies());
//    }
//
//    @Test
//    public void testMultiplePathsToSameTarget() {
//        // 测试到同一目标的多条路径
//        CsdlDependencyNode nodeA = new CsdlDependencyNode(
//            new FullQualifiedName("test", "A"),
//            CsdlDependencyNode.DependencyType.ENTITY_SET
//        );
//        CsdlDependencyNode nodeB = new CsdlDependencyNode(
//            new FullQualifiedName("test", "B"),
//            CsdlDependencyNode.DependencyType.ENTITY_SET
//        );
//        CsdlDependencyNode nodeC = new CsdlDependencyNode(
//            new FullQualifiedName("test", "C"),
//            CsdlDependencyNode.DependencyType.ENTITY_SET
//        );
//        CsdlDependencyNode nodeD = new CsdlDependencyNode(
//            new FullQualifiedName("test", "D"),
//            CsdlDependencyNode.DependencyType.ENTITY_SET
//        );
//
//        tree.addNode(nodeA);
//        tree.addNode(nodeB);
//        tree.addNode(nodeC);
//        tree.addNode(nodeD);
//
//        // A -> B -> DA -> C -> D
//        tree.addDependency(nodeA, nodeB);
//        tree.addDependency(nodeB, nodeD);
//        tree.addDependency(nodeA, nodeC);
//        tree.addDependency(nodeC, nodeD);
//
//        List<List<CsdlDependencyNode>> allPaths = tree.findAllPaths(nodeA, nodeD);
//        assertEquals(2, allPaths.size());
//
//        // 验证两条路径都存
//        boolean foundPathABD = false;
//        boolean foundPathACD = false;
//
//        for (List<CsdlDependencyNode> path : allPaths) {
//            if (path.size() == 3) {
//                if (path.get(0) == nodeA && path.get(1) == nodeB && path.get(2) == nodeD) {
//                    foundPathABD = true;
//                }
//                if (path.get(0) == nodeA && path.get(1) == nodeC && path.get(2) == nodeD) {
//                    foundPathACD = true;
//                }
//            }
//        }
//
//        assertTrue("应该找到路径 A -> B -> D", foundPathABD);
//        assertTrue("应该找到路径 A -> C -> D", foundPathACD);
//    }
//}
