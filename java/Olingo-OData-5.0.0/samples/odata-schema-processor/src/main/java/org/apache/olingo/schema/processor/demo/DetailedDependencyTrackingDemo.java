package org.apache.olingo.schema.processor.demo;

import org.apache.olingo.schema.processor.model.extended.ExtendedCsdlActionImport;
import java.util.Set;
import java.util.List;

/**
 * 详细依赖跟踪功能的演示类
 */
public class DetailedDependencyTrackingDemo {
    
    public static void main(String[] args) {
        System.out.println("=== OData Schema Element Detailed Dependency Tracking Demo ===");
        
        // 创建一个扩展的ActionImport
        ExtendedCsdlActionImport actionImport = new ExtendedCsdlActionImport();
        actionImport.setName("CreateCustomer");
        actionImport.setFullyQualifiedName("com.example.service.CreateCustomer");
        actionImport.setAction("com.example.actions.CreateCustomerAction");
        actionImport.setEntitySet("com.example.entities.CustomerSet");
        
        System.out.println("\n1. 基本信息:");
        System.out.println("   Name: " + actionImport.getName());
        System.out.println("   Fully Qualified Name: " + actionImport.getFullyQualifiedName());
        System.out.println("   Action: " + actionImport.getAction());
        System.out.println("   Entity Set: " + actionImport.getEntitySet());
        
        // 展示传统依赖跟踪
        System.out.println("\n2. 传统依赖跟踪 (仅命名空间):");
        Set<String> dependencies = actionImport.getDependencies();
        for (String dep : dependencies) {
            System.out.println("   - " + dep);
        }
        System.out.println("   依赖数量: " + actionImport.getDependencyCount());
        
        // 展示详细依赖跟踪
        System.out.println("\n3. 详细依赖跟踪 (元素级别):");
        Set<ExtendedCsdlActionImport.DetailedDependency> detailedDeps = actionImport.getDetailedDependencies();
        for (ExtendedCsdlActionImport.DetailedDependency dep : detailedDeps) {
            System.out.println("   - " + dep.toString());
            System.out.println("     源元素: " + dep.getSourceElement());
            System.out.println("     目标命名空间: " + dep.getTargetNamespace());
            System.out.println("     目标元素: " + dep.getTargetElement());
            System.out.println("     依赖类型: " + dep.getDependencyType());
            System.out.println("     属性名: " + dep.getPropertyName());
            System.out.println("     完整目标名: " + dep.getFullTargetName());
            System.out.println();
        }
        
        // 按类型过滤依赖
        System.out.println("\n4. 按类型过滤依赖:");
        Set<ExtendedCsdlActionImport.DetailedDependency> actionDeps = 
            actionImport.getDependenciesByType("ACTION_REFERENCE");
        System.out.println("   ACTION_REFERENCE dependencies:");
        for (ExtendedCsdlActionImport.DetailedDependency dep : actionDeps) {
            System.out.println("     - " + dep.getFullTargetName());
        }
        
        Set<ExtendedCsdlActionImport.DetailedDependency> entitySetDeps = 
            actionImport.getDependenciesByType("ENTITY_SET");
        System.out.println("   ENTITY_SET dependencies:");
        for (ExtendedCsdlActionImport.DetailedDependency dep : entitySetDeps) {
            System.out.println("     - " + dep.getFullTargetName());
        }
        
        // 按命名空间过滤依赖
        System.out.println("\n5. 按命名空间过滤依赖:");
        Set<ExtendedCsdlActionImport.DetailedDependency> actionNamespaceDeps = 
            actionImport.getDetailedDependenciesByNamespace("com.example.actions");
        System.out.println("   com.example.actions namespace dependencies:");
        for (ExtendedCsdlActionImport.DetailedDependency dep : actionNamespaceDeps) {
            System.out.println("     - " + dep.getTargetElement() + " (" + dep.getDependencyType() + ")");
        }
        
        Set<ExtendedCsdlActionImport.DetailedDependency> entityNamespaceDeps = 
            actionImport.getDetailedDependenciesByNamespace("com.example.entities");
        System.out.println("   com.example.entities namespace dependencies:");
        for (ExtendedCsdlActionImport.DetailedDependency dep : entityNamespaceDeps) {
            System.out.println("     - " + dep.getTargetElement() + " (" + dep.getDependencyType() + ")");
        }
        
        // 展示依赖链
        System.out.println("\n6. 依赖链:");
        List<String> chains = actionImport.getDependencyChainStrings();
        for (String chain : chains) {
            System.out.println("   " + chain);
        }
        
        // 获取所有依赖的元素名称
        System.out.println("\n7. 所有依赖的元素名称:");
        Set<String> elementNames = actionImport.getAllDependentElementNames();
        for (String name : elementNames) {
            System.out.println("   - " + name);
        }
        
        // 演示手动添加详细依赖
        System.out.println("\n8. 手动添加详细依赖:");
        actionImport.addDetailedDependency("com.example.types", "CustomerType", "ENTITY_TYPE", "returnType");
        System.out.println("   添加了依赖: com.example.types.CustomerType (ENTITY_TYPE:returnType)");
        
        System.out.println("\n   更新后的详细依赖:");
        detailedDeps = actionImport.getDetailedDependencies();
        for (ExtendedCsdlActionImport.DetailedDependency dep : detailedDeps) {
            System.out.println("   - " + dep.toString());
        }
        
        System.out.println("\n=== Demo 完成 ===");
    }
}
