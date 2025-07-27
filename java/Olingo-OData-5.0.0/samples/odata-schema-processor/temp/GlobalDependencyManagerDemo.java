package org.apache.olingo.schema.processor.demo;

import java.util.List;
import java.util.Set;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.schema.processor.model.dependency.CsdlDependencyNode;
import org.apache.olingo.schema.processor.model.dependency.GlobalDependencyManager;
import org.apache.olingo.schema.processor.model.extended.ExtendedCsdlActionImport;
import org.apache.olingo.schema.processor.model.extended.ExtendedCsdlEntityType;

/**
 * 全局依赖管理器演示
 * 展示如何通过全局依赖管理器实现跨类型的依赖查询和管理
 */
public class GlobalDependencyManagerDemo {
    
    public static void main(String[] args) {
        System.out.println("=== 全局依赖管理器演示 ===");
        
        // 重置全局实例（确保干净的状态）
        GlobalDependencyManager.resetInstance();
        GlobalDependencyManager manager = GlobalDependencyManager.getInstance();
        
        demonstrateBasicUsage(manager);
        demonstrateCrossTypeQueries(manager);
        demonstrateGlobalAnalysis(manager);
        
        System.out.println("=== 演示完成 ===");
    }
    
    private static void demonstrateBasicUsage(GlobalDependencyManager manager) {
        System.out.println("\n1. 基本使用演示:");
        System.out.println("------------------");
        
        // 创建原始的CSDL对象
        CsdlEntityType customerTypeCsdl = new CsdlEntityType();
        customerTypeCsdl.setName("Customer");
        
        // 创建扩展的实体类型
        ExtendedCsdlEntityType customerType = new ExtendedCsdlEntityType(
            customerTypeCsdl, 
            "com.example.entities.Customer",
            new FullQualifiedName("com.example.entities", "Customer")
        );
        
        // 创建扩展的ActionImport
        ExtendedCsdlActionImport actionImport = new ExtendedCsdlActionImport();
        actionImport.setName("CreateCustomer");
        
        // 建立依赖关系：ActionImport依赖EntityType
        actionImport.addTreeDependency("com.example.entities", "Customer", 
                                     CsdlDependencyNode.DependencyType.ENTITY_TYPE, "targetEntity");
        
        // 通过全局管理器查询
        System.out.println("ActionImport的直接依赖: " + actionImport.getDirectDependencies().size());
        System.out.println("EntityType的直接被依赖者: " + customerType.getDirectDependents().size());
        
        // 全局统计
        System.out.println("全局统计: " + manager.getStatistics());
    }
    
    private static void demonstrateCrossTypeQueries(GlobalDependencyManager manager) {
        System.out.println("\n2. 跨类型依赖查询演示:");
        System.out.println("----------------------");
        
        // 创建更多元素形成复杂依赖网络
        CsdlEntityType orderTypeCsdl = new CsdlEntityType();
        orderTypeCsdl.setName("Order");
        ExtendedCsdlEntityType orderType = new ExtendedCsdlEntityType(
            orderTypeCsdl,
            "com.example.entities.Order",
            new FullQualifiedName("com.example.entities", "Order")
        );
        
        CsdlEntityType productTypeCsdl = new CsdlEntityType();
        productTypeCsdl.setName("Product");
        ExtendedCsdlEntityType productType = new ExtendedCsdlEntityType(
            productTypeCsdl,
            "com.example.entities.Product",
            new FullQualifiedName("com.example.entities", "Product")
        );
        
        ExtendedCsdlActionImport orderService = new ExtendedCsdlActionImport();
        orderService.setName("OrderService");
        
        // 建立复杂依赖关系
        orderService.addTreeDependency("com.example.entities", "Order", 
                                      CsdlDependencyNode.DependencyType.ENTITY_TYPE, "order");
        orderService.addTreeDependency("com.example.entities", "Customer", 
                                      CsdlDependencyNode.DependencyType.ENTITY_TYPE, "customer");
        
        orderType.addTreeDependency("com.example.entities", "Customer", 
                                   CsdlDependencyNode.DependencyType.ENTITY_TYPE, "customer");
        orderType.addTreeDependency("com.example.entities", "Product", 
                                   CsdlDependencyNode.DependencyType.ENTITY_TYPE, "product");
        
        // 跨类型查询
        System.out.println("按类型查询 - 所有EntityType:");
        Set<CsdlDependencyNode> entityTypes = manager.getElementsByType(CsdlDependencyNode.DependencyType.ENTITY_TYPE);
        for (CsdlDependencyNode node : entityTypes) {
            System.out.println("  " + node.getName());
        }
        
        System.out.println("按类型查询 - 所有ActionImport:");
        Set<CsdlDependencyNode> actionImports = manager.getElementsByType(CsdlDependencyNode.DependencyType.ACTION_REFERENCE);
        for (CsdlDependencyNode node : actionImports) {
            System.out.println("  " + node.getName());
        }
        
        System.out.println("按命名空间查询 - com.example.entities:");
        Set<CsdlDependencyNode> entitiesNamespace = manager.getElementsByNamespace("com.example.entities");
        for (CsdlDependencyNode node : entitiesNamespace) {
            System.out.println("  " + node.getName() + " (" + node.getDependencyType() + ")");
        }
        
        System.out.println("按命名空间查询 - com.example.services:");
        Set<CsdlDependencyNode> servicesNamespace = manager.getElementsByNamespace("com.example.services");
        for (CsdlDependencyNode node : servicesNamespace) {
            System.out.println("  " + node.getName() + " (" + node.getDependencyType() + ")");
        }
    }
    
    private static void demonstrateGlobalAnalysis(GlobalDependencyManager manager) {
        System.out.println("\n3. 全局依赖分析演示:");
        System.out.println("--------------------");
        
        // 获取根节点和叶子节点
        System.out.println("根节点 (不依赖其他节点的节点):");
        Set<CsdlDependencyNode> rootNodes = manager.getRootNodes();
        for (CsdlDependencyNode node : rootNodes) {
            System.out.println("  " + node.getName() + " (" + node.getDependencyType() + ")");
        }
        
        System.out.println("叶子节点 (不被其他节点依赖的节点):");
        Set<CsdlDependencyNode> leafNodes = manager.getLeafNodes();
        for (CsdlDependencyNode node : leafNodes) {
            System.out.println("  " + node.getName() + " (" + node.getDependencyType() + ")");
        }
        
        // 拓扑排序
        System.out.println("拓扑排序 (依赖顺序):");
        List<CsdlDependencyNode> topologicalOrder = manager.getTopologicalOrder();
        for (int i = 0; i < topologicalOrder.size(); i++) {
            CsdlDependencyNode node = topologicalOrder.get(i);
            System.out.println("  " + (i + 1) + ". " + node.getName() + " (" + node.getDependencyType() + ")");
        }
        
        // 依赖路径查询
        System.out.println("依赖路径查询 - 从OrderService到Product:");
        List<CsdlDependencyNode> path = manager.getDependencyPath("com.example.services.OrderService", "com.example.entities.Product");
        if (path != null && !path.isEmpty()) {
            StringBuilder pathStr = new StringBuilder();
            for (int i = 0; i < path.size(); i++) {
                if (i > 0) pathStr.append(" -> ");
                pathStr.append(path.get(i).getName());
            }
            System.out.println("  " + pathStr.toString());
        } else {
            System.out.println("  未找到依赖路径");
        }
        
        // 检查循环依赖
        System.out.println("循环依赖检查: " + (manager.hasCircularDependencies() ? "存在" : "不存在"));
        
        // 最终统计
        System.out.println("最终统计: " + manager.getStatistics());
        
        // 演示相互查询能力
        System.out.println("\n依赖和被依赖的相互查询能力:");
        System.out.println("Customer的所有直接被依赖者:");
        Set<CsdlDependencyNode> customerDependents = manager.getDirectDependents("com.example.entities.Customer");
        for (CsdlDependencyNode node : customerDependents) {
            System.out.println("  " + node.getName() + " (" + node.getDependencyType() + ")");
        }
        
        System.out.println("OrderService的所有直接依赖:");
        Set<CsdlDependencyNode> orderServiceDeps = manager.getDirectDependencies("com.example.services.OrderService");
        for (CsdlDependencyNode node : orderServiceDeps) {
            System.out.println("  " + node.getName() + " (" + node.getDependencyType() + ")");
        }
    }
}
