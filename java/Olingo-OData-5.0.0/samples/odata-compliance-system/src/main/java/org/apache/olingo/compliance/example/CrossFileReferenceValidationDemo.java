package org.apache.olingo.compliance.example;

import java.io.File;
import java.util.List;

import org.apache.olingo.compliance.core.model.XmlComplianceResult;
import org.apache.olingo.compliance.engine.core.SchemaExtractor;
import org.apache.olingo.compliance.engine.core.SchemaRegistry;
import org.apache.olingo.compliance.validator.directory.DirectoryValidationManager;
import org.apache.olingo.compliance.validator.file.EnhancedRegistryAwareXmlValidator;

/**
 * 跨文件引用验证功能使用示例
 * 
 * 演示如何使用新的SchemaRegistry机制进行跨文件引用验证
 */
public class CrossFileReferenceValidationDemo {
    
    /**
     * 示例：增量验证
     * 使用已有的基础数据验证新文件
     */
    public void incrementalValidationExample(String newSchemaFile, SchemaRegistry baseRegistry) {
        try {
            // 使用增强的验证器进行单文件验证
            EnhancedRegistryAwareXmlValidator validator = new EnhancedRegistryAwareXmlValidator();
            
            File xmlFile = new File(newSchemaFile);
            XmlComplianceResult result = validator.validateWithRegistry(xmlFile, baseRegistry);
            
            System.out.println("单文件验证结果:");
            System.out.println("- 文件: " + xmlFile.getName());
            System.out.println("- 验证通过: " + result.isCompliant());
            System.out.println("- 问题数量: " + result.getIssues().size());
            System.out.println("- 验证时间: " + result.getValidationTimeMs() + "ms");
            
            if (!result.isCompliant()) {
                System.out.println("验证问题:");
                result.getIssues().forEach(issue -> 
                    System.out.println("  - " + issue.getMessage()));
            } else {
                System.out.println("文件验证通过，可以将其Schema信息合并到基础数据中");
                
                // 如果验证通过，可以提取新文件的Schema并合并到基础Registry中
                SchemaRegistry newFileRegistry = extractSchemaFromFile(xmlFile);
                baseRegistry.merge(newFileRegistry);
                System.out.println("Schema信息已合并到基础数据中");
            }
            
        } catch (Exception e) {
            System.err.println("增量验证过程中发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 示例：目录验证
     * 验证整个目录的Schema文件
     */
    public void directoryValidationExample(String schemaDirectory) {
        try {
            DirectoryValidationManager manager = new DirectoryValidationManager();
            
            DirectoryValidationManager.DirectoryValidationResult result = 
                manager.validateDirectory(schemaDirectory);
            
            System.out.println("目录验证结果:");
            System.out.println("- 总文件数: " + result.getTotalFiles());
            System.out.println("- 有效文件数: " + result.getValidFileCount());
            System.out.println("- 总问题数: " + result.getTotalIssueCount());
            System.out.println("- 验证时间: " + result.getValidationTimeMs() + "ms");
            
            if (result.isValid()) {
                System.out.println("目录验证通过，所有跨文件引用都正确");
            } else {
                System.out.println("发现问题:");
                result.getAllIssues().forEach(issue -> 
                    System.out.println("  - " + issue.getMessage()));
            }
            
        } catch (Exception e) {
            System.err.println("目录验证过程中发生错误: " + e.getMessage());
        }
    }
    
    // 辅助方法
    
    private SchemaRegistry extractSchemaFromFile(File xmlFile) throws Exception {
        SchemaExtractor extractor = new SchemaExtractor();
        SchemaRegistry registry = new SchemaRegistry();
        
        List<SchemaRegistry.SchemaDefinition> schemas = extractor.extractSchemas(xmlFile);
        for (SchemaRegistry.SchemaDefinition schema : schemas) {
            registry.registerSchema(schema);
        }
        
        return registry;
    }
    
    /**
     * 主方法：演示使用场景
     */
    public static void main(String[] args) {
        CrossFileReferenceValidationDemo example = new CrossFileReferenceValidationDemo();
        
        System.out.println("=== 跨文件引用验证功能演示 ===\n");
        
        // 示例参数（实际使用时需要提供真实路径）
        String schemaDirectory = "/path/to/schema/directory";
        String newSchemaFile = "/path/to/new/schema.xml";
        
        System.out.println("1. 目录验证演示:");
        example.directoryValidationExample(schemaDirectory);
        
        System.out.println("\n2. 增量验证演示:");
        SchemaRegistry baseRegistry = new SchemaRegistry(); // 假设已有基础数据
        example.incrementalValidationExample(newSchemaFile, baseRegistry);
        
        System.out.println("\n=== 演示完成 ===");
    }
}
