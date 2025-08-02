package org.apache.olingo.xmlprocessor.demo;

import java.util.List;
import java.util.Set;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.xmlprocessor.core.dependency.model.CsdlDependencyNode;
import org.apache.olingo.xmlprocessor.core.dependency.DependencyManager;
import org.apache.olingo.xmlprocessor.core.dependency.impl.CsdlDependencyManagerImpl;

/**
 * 高级依赖关系管理演示
 * 展示如何使用依赖管理器进行复杂的依赖分析
 */
public class AdvancedDependencyDemo {
    
    public static void main(String[] args) {
        System.out.println("=== 高级依赖关系管理演示 ===\n");

        try {
            // 创建新的依赖管理器实例（而不是使用全局单例）
            DependencyManager manager = new CsdlDependencyManagerImpl();

            // 演示基本的依赖关系管理
            demonstrateBasicDependencies(manager);

            // 演示复杂的依赖分析
            demonstrateComplexAnalysis(manager);

            // 演示循环依赖检测
            demonstrateCircularDependencyDetection(manager);

            // 演示依赖管理器功能
            demonstrateDependencyManager(manager);

            System.out.println("\n=== 演示完成 ===");

        } catch (Exception e) {
            System.err.println("演示过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void demonstrateBasicDependencies(DependencyManager manager) {
        System.out.println("1. 基本依赖关系管理");
        System.out.println("-------------------");

        // 注册一些示例元素
        FullQualifiedName orderFqn = new FullQualifiedName("Example.Service", "Order");
        FullQualifiedName customerFqn = new FullQualifiedName("Example.Service", "Customer");
        FullQualifiedName productFqn = new FullQualifiedName("Example.Service", "Product");

        // 注册元素
        manager.registerElement("Order", orderFqn, CsdlDependencyNode.DependencyType.ENTITY_TYPE, "Example.Service");
        manager.registerElement("Customer", customerFqn, CsdlDependencyNode.DependencyType.ENTITY_TYPE, "Example.Service");
        manager.registerElement("Product", productFqn, CsdlDependencyNode.DependencyType.ENTITY_TYPE, "Example.Service");

        // 添加依赖关系
        manager.addDependency("Order", "Customer");
        manager.addDependency("Order", "Product");

        // 查看依赖关系
        Set<CsdlDependencyNode> orderDeps = manager.getDirectDependencies("Order");
        System.out.println("Order 直接依赖: " + orderDeps.size() + " 个元素");

        System.out.println();
    }

    private static void demonstrateComplexAnalysis(DependencyManager manager) {
        System.out.println("2. 复杂依赖分析");
        System.out.println("---------------");

        // 添加更多元素和依赖关系
        FullQualifiedName orderItemFqn = new FullQualifiedName("Example.Service", "OrderItem");
        manager.registerElement("OrderItem", orderItemFqn, CsdlDependencyNode.DependencyType.ENTITY_TYPE, "Example.Service");

        // 建立多层依赖关系
        manager.addDependency("OrderItem", "Order");
        manager.addDependency("OrderItem", "Product");

        // 分析根节点和叶子节点
        Set<CsdlDependencyNode> rootNodes = manager.getRootNodes();
        Set<CsdlDependencyNode> leafNodes = manager.getLeafNodes();

        System.out.println("根节点数量: " + rootNodes.size());
        System.out.println("叶子节点数量: " + leafNodes.size());

        // 获取拓扑排序
        List<CsdlDependencyNode> topologicalOrder = manager.getTopologicalOrder();
        System.out.println("拓扑排序结果: " + topologicalOrder.size() + " 个元素");

        System.out.println();
    }
    
    private static void demonstrateCircularDependencyDetection(DependencyManager manager) {
        System.out.println("3. 循环依赖检测");
        System.out.println("---------------");

        // 检查是否存在循环依赖
        boolean hasCircular = manager.hasCircularDependencies();
        System.out.println("是否存在循环依赖: " + hasCircular);

        // 如果不存在循环依赖，创建一个来演示检测功能
        if (!hasCircular) {
            // 注册新元素
            FullQualifiedName categoryFqn = new FullQualifiedName("Example.Service", "Category");
            manager.registerElement("Category", categoryFqn, CsdlDependencyNode.DependencyType.ENTITY_TYPE, "Example.Service");

            // 创建循环依赖
            manager.addDependency("Product", "Category");
            manager.addDependency("Category", "Product");

            // 再次检测
            hasCircular = manager.hasCircularDependencies();
            System.out.println("创建循环依赖后: " + hasCircular);
        }

        System.out.println();
    }

    private static void demonstrateDependencyManager(DependencyManager manager) {
        System.out.println("4. 依赖管理器高级功能");
        System.out.println("-------------------");

        // 按类型查询元素
        Set<CsdlDependencyNode> entityTypes = manager.getElementsByType(CsdlDependencyNode.DependencyType.ENTITY_TYPE);
        System.out.println("EntityType 数量: " + entityTypes.size());

        // 按命名空间查询元素
        Set<CsdlDependencyNode> serviceElements = manager.getElementsByNamespace("Example.Service");
        System.out.println("Example.Service 命名空间元素数量: " + serviceElements.size());

        // 获取统计信息
        String statistics = manager.getStatistics();
        System.out.println("统计信息: " + statistics);

        // 获取所有元素
        Set<CsdlDependencyNode> allElements = manager.getAllElements();
        System.out.println("总元素数量: " + allElements.size());

        System.out.println();
    }
}
