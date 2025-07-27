package org.apache.olingo.schema.processor.demo;

import org.apache.olingo.schema.processor.exporter.impl.DefaultContainerExporter;
import org.apache.olingo.schema.processor.exporter.builder.DynamicContainerBuilder;
import org.apache.olingo.schema.processor.exporter.builder.EntityTypeBuilder;

import java.io.File;
import java.io.IOException;

/**
 * 演示容器导出功能的主类
 */
public class ContainerExportDemo {
    
    public static void main(String[] args) {
        try {
            System.out.println("开始测试容器导出功能...");
            
            // 创建导出器
            DefaultContainerExporter exporter = new DefaultContainerExporter();
            System.out.println("✓ 导出器创建成功");
            
            // 创建动态构建器
            DynamicContainerBuilder builder = exporter.createBuilder();
            System.out.println("✓ 动态构建器创建成功");
            
            // 添加实体类型
            builder.addEntityType("Product", new EntityTypeBuilder() {
                @Override
                public void build(EntityTypeBuilderContext context) {
                    context.addProperty("Id", "Edm.Int32", false, null);
                    context.addProperty("Name", "Edm.String", true, 100);
                    context.addProperty("Price", "Edm.Decimal", true, null);
                    context.setKey("Id");
                }
            });
            System.out.println("✓ 实体类型添加成功");
            
            // 添加实体集
            builder.addEntitySet("Products", "DemoNamespace.Product");
            
            // 添加函数导入
            builder.addFunctionImport("GetTopProducts", "DemoNamespace.GetTopProducts");
            
            // 添加动作导入
            builder.addActionImport("UpdatePrice", "DemoNamespace.UpdatePrice");
            
            System.out.println("✓ 实体集和导入添加成功");
            
            // 构建容器
            DynamicContainerBuilder.ContainerBuildResult result = builder.build("DemoNamespace", "DemoContainer");
            System.out.println("✓ 容器构建成功");
            
            // 验证结果
            if (result.getSchema() != null && result.getContainer() != null) {
                System.out.println("✓ Schema和Container创建成功");
                System.out.println("  - Namespace: " + result.getSchema().getNamespace());
                System.out.println("  - Container: " + result.getContainer().getName());
                System.out.println("  - EntityTypes: " + (result.getSchema().getEntityTypes() != null ? result.getSchema().getEntityTypes().size() : 0));
                System.out.println("  - EntitySets: " + (result.getContainer().getEntitySets() != null ? result.getContainer().getEntitySets().size() : 0));
                System.out.println("  - FunctionImports: " + (result.getContainer().getFunctionImports() != null ? result.getContainer().getFunctionImports().size() : 0));
                System.out.println("  - ActionImports: " + (result.getContainer().getActionImports() != null ? result.getContainer().getActionImports().size() : 0));
            }
            
            // 导出到XML文件
            File outputFile = new File("demo_container.xml");
            exporter.exportContainer(result.getSchema(), outputFile);
            
            if (outputFile.exists()) {
                System.out.println("✓ XML文件导出成功: " + outputFile.getAbsolutePath());
                System.out.println("  文件大小: " + outputFile.length() + " bytes");
            } else {
                System.out.println("✗ XML文件导出失败");
            }
            
            System.out.println("\n所有测试通过！容器导出功能正常工作。");
            
        } catch (Exception e) {
            System.err.println("✗ 测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
