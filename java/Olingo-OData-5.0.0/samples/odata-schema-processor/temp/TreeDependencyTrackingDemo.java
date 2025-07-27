package org.apache.olingo.schema.processor.demo;

import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.schema.processor.model.dependency.CsdlDependencyNode;
import org.apache.olingo.schema.processor.model.dependency.CsdlDependencyTree;
import org.apache.olingo.schema.processor.model.dependency.GlobalDependencyManager;
import org.apache.olingo.schema.processor.model.extended.ExtendedCsdlActionImport;

/**
 * 演示基于Olingo底层数据结构的树状依赖关系追踪功能
 */
public class TreeDependencyTrackingDemo {
    
    public static void main(String[] args) {
        System.out.println("=== 基于Olingo底层数据结构的树状依赖关系追踪演示 ===\n");
        
        // 演示1: 基本依赖关系
        demonstrateBasicDependencies();
        
        // 演示2: 复杂的多层依赖关系
        demonstrateComplexDependencies();
        
        // 演示3: 循环依赖检测
        demonstrateCircularDependencyDetection();
        
        // 演示4: 依赖路径查找
        demonstrateDependencyPathFinding();
        
        // 演示5: 拓扑排序
        demonstrateTopologicalSorting();
        
        System.out.println("=== 演示完成 ===");
    }
    
    /**
     * 演示1: 基本依赖关系
     */
    private static void demonstrateBasicDependencies() {
        System.out.println("1. 基本依赖关系演示:");
        System.out.println("------------------");
        
        ExtendedCsdlActionImport actionImport = new ExtendedCsdlActionImport();
        actionImport.setName("CustomerActionImport");
        actionImport.setFullyQualifiedName("com.example.CustomerActionImport");
        
        // 添加不同类型的依赖
        actionImport.addTreeDependency("com.example.entities", "Customer", 
                                     CsdlDependencyNode.DependencyType.ENTITY_SET, "entitySet");
        actionImport.addTreeDependency("com.example.actions", "CreateCustomer", 
                                     CsdlDependencyNode.DependencyType.ACTION_REFERENCE, "action");
        actionImport.addTreeDependency("com.example.types", "Address", 
                                     CsdlDependencyNode.DependencyType.TYPE_REFERENCE, "parameter");
        
        // 显示依赖信息
        System.out.println("ActionImport: " + actionImport.getSelfNode());
        System.out.println("直接依赖数量: " + actionImport.getDirectDependencies().size());
        
        for (CsdlDependencyNode dep : actionImport.getDirectDependencies()) {
            System.out.println("  -> " + dep);
        }
        
        // 显示按类型分组的依赖
        System.out.println("\n按类型分组的依赖:");
        System.out.println("  ENTITY_SET: " + actionImport.getDependenciesByType(CsdlDependencyNode.DependencyType.ENTITY_SET).size());
        System.out.println("  ACTION_REFERENCE: " + actionImport.getDependenciesByType(CsdlDependencyNode.DependencyType.ACTION_REFERENCE).size());
        System.out.println("  TYPE_REFERENCE: " + actionImport.getDependenciesByType(CsdlDependencyNode.DependencyType.TYPE_REFERENCE).size());
        
        // 显示按命名空间分组的依赖
        System.out.println("\n按命名空间分组的依赖:");
        System.out.println("  com.example.entities: " + actionImport.getDependenciesByNamespace("com.example.entities").size());
        System.out.println("  com.example.actions: " + actionImport.getDependenciesByNamespace("com.example.actions").size());
        System.out.println("  com.example.types: " + actionImport.getDependenciesByNamespace("com.example.types").size());
        
        System.out.println();
    }
    
    /**
     * 演示2: 复杂的多层依赖关系
     */
    private static void demonstrateComplexDependencies() {
        System.out.println("2. 复杂多层依赖关系演示:");
        System.out.println("----------------------");
        
        CsdlDependencyTree tree = new CsdlDependencyTree();
        
        // 创建多个依赖节点
        CsdlDependencyNode orderService = new CsdlDependencyNode(
            new FullQualifiedName("com.example.services", "OrderService"),
            CsdlDependencyNode.DependencyType.ACTION_REFERENCE
        );
        
        CsdlDependencyNode orderEntity = new CsdlDependencyNode(
            new FullQualifiedName("com.example.entities", "Order"),
            CsdlDependencyNode.DependencyType.ENTITY_SET
        );
        
        CsdlDependencyNode customerEntity = new CsdlDependencyNode(
            new FullQualifiedName("com.example.entities", "Customer"),
            CsdlDependencyNode.DependencyType.ENTITY_SET
        );
        
        CsdlDependencyNode addressType = new CsdlDependencyNode(
            new FullQualifiedName("com.example.types", "Address"),
            CsdlDependencyNode.DependencyType.TYPE_REFERENCE
        );
        
        CsdlDependencyNode countryType = new CsdlDependencyNode(
            new FullQualifiedName("com.example.types", "Country"),
            CsdlDependencyNode.DependencyType.TYPE_REFERENCE
        );
        
        // 添加节点到树
        tree.addNode(orderService);
        tree.addNode(orderEntity);
        tree.addNode(customerEntity);
        tree.addNode(addressType);
        tree.addNode(countryType);
        
        // 建立依赖关系
        tree.addDependency(orderService, orderEntity);      // OrderService -> Order
        tree.addDependency(orderEntity, customerEntity);    // Order -> Customer
        tree.addDependency(customerEntity, addressType);    // Customer -> Address
        tree.addDependency(addressType, countryType);       // Address -> Country
        
        // 显示树的统计信息
        System.out.println("依赖树统计信息:");
        System.out.println(tree.getStatistics());
        
        // 显示根节点和叶子节点
        System.out.println("\n根节点 (没有被其他节点依赖的节点):");
        for (CsdlDependencyNode root : tree.getRootNodes()) {
            System.out.println("  " + root);
        }
        
        System.out.println("\n叶子节点 (不依赖其他节点的节点):");
        for (CsdlDependencyNode leaf : tree.getLeafNodes()) {
            System.out.println("  " + leaf);
        }
        
        // 显示OrderService的所有依赖
        System.out.println("\nOrderService的所有依赖:");
        for (CsdlDependencyNode dep : orderService.getAllDependencies()) {
            System.out.println("  " + dep);
        }
        
        // 显示Country的所有被依赖者
        System.out.println("\nCountry的所有被依赖者:");
        for (CsdlDependencyNode dependent : countryType.getAllDependents()) {
            System.out.println("  " + dependent);
        }
        
        System.out.println();
    }
    
    /**
     * 演示3: 循环依赖检测
     */
    private static void demonstrateCircularDependencyDetection() {
        System.out.println("3. 循环依赖检测演示:");
        System.out.println("------------------");
        
        CsdlDependencyTree tree = new CsdlDependencyTree();
        
        // 创建三个节点
        CsdlDependencyNode nodeA = new CsdlDependencyNode(
            new FullQualifiedName("test.namespace", "NodeA"),
            CsdlDependencyNode.DependencyType.TYPE_REFERENCE
        );
        
        CsdlDependencyNode nodeB = new CsdlDependencyNode(
            new FullQualifiedName("test.namespace", "NodeB"),
            CsdlDependencyNode.DependencyType.TYPE_REFERENCE
        );
        
        CsdlDependencyNode nodeC = new CsdlDependencyNode(
            new FullQualifiedName("test.namespace", "NodeC"),
            CsdlDependencyNode.DependencyType.TYPE_REFERENCE
        );
        
        tree.addNode(nodeA);
        tree.addNode(nodeB);
        tree.addNode(nodeC);
        
        // 创建线性依赖: A -> B -> C
        tree.addDependency(nodeA, nodeB);
        tree.addDependency(nodeB, nodeC);
        
        System.out.println("线性依赖 (A -> B -> C):");
        System.out.println("是否存在循环依赖: " + tree.hasCircularDependencies());
        
        // 添加循环依赖: C -> A
        tree.addDependency(nodeC, nodeA);
        
        System.out.println("\n添加循环依赖 (C -> A) 后:");
        System.out.println("是否存在循环依赖: " + tree.hasCircularDependencies());
        System.out.println("NodeA有循环依赖: " + nodeA.hasCircularDependency());
        System.out.println("NodeB有循环依赖: " + nodeB.hasCircularDependency());
        System.out.println("NodeC有循环依赖: " + nodeC.hasCircularDependency());
        
        System.out.println();
    }
    
    /**
     * 演示4: 依赖路径查找
     */
    private static void demonstrateDependencyPathFinding() {
        System.out.println("4. 依赖路径查找演示:");
        System.out.println("------------------");
        
        ExtendedCsdlActionImport actionImport = new ExtendedCsdlActionImport(
            "ComplexActionImport", 
            new FullQualifiedName("com.example", "ComplexActionImport")
        );
        
        // 创建多层依赖
        actionImport.addDependency("entitySet", 
                                 new FullQualifiedName("level1.namespace", "Level1Entity"), 
                                 CsdlDependencyNode.DependencyType.ENTITY_SET);
        
        // 获取全局依赖管理器来查找节点
        GlobalDependencyManager manager = GlobalDependencyManager.getInstance();
        
        // 手动创建更深层的依赖
        CsdlDependencyNode level1Node = manager.getElement("level1.namespace.Level1Entity");
        
        CsdlDependencyNode level2Node = new CsdlDependencyNode(
            new FullQualifiedName("level2.namespace", "Level2Entity"),
            CsdlDependencyNode.DependencyType.TYPE_REFERENCE
        );
        
        CsdlDependencyNode level3Node = new CsdlDependencyNode(
            new FullQualifiedName("level3.namespace", "Level3Entity"),
            CsdlDependencyNode.DependencyType.BASE_TYPE
        );
        
        tree.addNode(level2Node);
        tree.addNode(level3Node);
        tree.addDependency(level1Node, level2Node);
        tree.addDependency(level2Node, level3Node);
        
        // 查找从ActionImport到Level3Entity的路径
        List<CsdlDependencyNode> path = actionImport.getSelfNode().getDependencyPath(level3Node);
        
        if (path != null) {
            System.out.println("从ComplexActionImport到Level3Entity的依赖路径:");
            for (int i = 0; i < path.size(); i++) {
                if (i > 0) {
                    System.out.print(" -> ");
                }
                System.out.print(path.get(i).getFullyQualifiedName());
            }
            System.out.println();
        } else {
            System.out.println("未找到依赖路径");
        }
        
        // 查找所有可能的路径
        List<List<CsdlDependencyNode>> allPaths = tree.findAllPaths(actionImport.getSelfNode(), level3Node);
        System.out.println("\n所有可能的路径数量: " + allPaths.size());
        
        System.out.println();
    }
    
    /**
     * 演示5: 拓扑排序
     */
    private static void demonstrateTopologicalSorting() {
        System.out.println("5. 拓扑排序演示:");
        System.out.println("--------------");
        
        CsdlDependencyTree tree = new CsdlDependencyTree();
        
        // 创建一个复杂的依赖图
        CsdlDependencyNode service = new CsdlDependencyNode(
            new FullQualifiedName("app", "Service"),
            CsdlDependencyNode.DependencyType.ACTION_REFERENCE
        );
        
        CsdlDependencyNode controller = new CsdlDependencyNode(
            new FullQualifiedName("app", "Controller"),
            CsdlDependencyNode.DependencyType.TYPE_REFERENCE
        );
        
        CsdlDependencyNode repository = new CsdlDependencyNode(
            new FullQualifiedName("app", "Repository"),
            CsdlDependencyNode.DependencyType.TYPE_REFERENCE
        );
        
        CsdlDependencyNode entity = new CsdlDependencyNode(
            new FullQualifiedName("app", "Entity"),
            CsdlDependencyNode.DependencyType.ENTITY_SET
        );
        
        CsdlDependencyNode database = new CsdlDependencyNode(
            new FullQualifiedName("app", "Database"),
            CsdlDependencyNode.DependencyType.TYPE_REFERENCE
        );
        
        // 添加所有节点
        tree.addNode(service);
        tree.addNode(controller);
        tree.addNode(repository);
        tree.addNode(entity);
        tree.addNode(database);
        
        // 建立依赖关系
        tree.addDependency(service, controller);     // Service -> Controller
        tree.addDependency(controller, repository);  // Controller -> Repository
        tree.addDependency(repository, entity);      // Repository -> Entity
        tree.addDependency(repository, database);    // Repository -> Database
        
        // 获取拓扑排序
        List<CsdlDependencyNode> topologicalOrder = tree.getTopologicalOrder();
        
        System.out.println("拓扑排序结果 (被依赖的节点在前):");
        for (int i = 0; i < topologicalOrder.size(); i++) {
            System.out.println((i + 1) + ". " + topologicalOrder.get(i).getFullyQualifiedName());
        }
        
        System.out.println("\n这个顺序表示了节点的依赖层次，可以用于:");
        System.out.println("- 编译顺序确定");
        System.out.println("- 部署顺序规划");
        System.out.println("- 删除顺序规划（逆序）");
        
        System.out.println();
    }
}
