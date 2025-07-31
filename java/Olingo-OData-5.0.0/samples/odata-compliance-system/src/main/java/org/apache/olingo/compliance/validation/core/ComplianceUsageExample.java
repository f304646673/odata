package org.apache.olingo.compliance.validation.core;

import org.apache.olingo.compliance.file.ComplianceErrorType;

/**
 * 使用示例：演示如何使用合规性辅助判断结构体系统
 */
public class ComplianceUsageExample {
    
    public static void main(String[] args) {
        try {
            // 1. 创建验证管理器并加载已知的模式文件
            ComplianceValidationManager manager = new ComplianceValidationManager();
            
            // 从标准OData模式目录加载知识库
            manager.loadKnowledgeBase("src/test/resources/validation/schemas");
            
            // 配置验证参数
            manager.getConfigurationManager()
                   .setStrictMode(true)
                   .setEnableCaching(true)
                   .setEnableStatistics(true);
            
            // 2. 创建验证上下文用于验证新的XML文件
            ComplianceContext context = manager.createValidationContext();
            
            // 3. 验证单个文件
            ComplianceContext.ValidationResult result = manager.validateFile(
                "src/test/resources/validation/single/invalid/type-error/invalid-complextype-inherits-entitytype/invalid-complextype-inherits-entitytype.xml",
                context
            );
            
            System.out.println("验证结果:");
            System.out.println("文件: " + result.getFilePath());
            System.out.println("是否有效: " + result.isValid());
            System.out.println("验证时间: " + result.getValidationTimeMs() + "ms");
            System.out.println("问题数量: " + result.getIssues().size());
            
            // 打印具体问题
            result.getIssues().forEach(issue -> {
                System.out.println("  - " + issue.getErrorType() + ": " + issue.getMessage());
            });
            
            // 4. 批量验证目录中的所有文件
            ComplianceContext batchContext = manager.validateDirectory(
                "src/test/resources/validation/single/invalid"
            );
            
            // 5. 查看统计信息
            ComplianceContext.ComplianceStatistics stats = batchContext.getStatistics();
            System.out.println("\n批量验证统计:");
            System.out.println(stats.toString());
            
            // 6. 使用合规性知识库进行跨文件验证
            ComplianceKnowledgeBase knowledgeBase = manager.getGlobalKnowledgeBase();
            
            // 检查类型继承关系
            boolean validInheritance = knowledgeBase.isValidInheritance(
                "TestNamespace.TestComplexType", 
                "TestNamespace.TestEntityType"
            );
            System.out.println("\n继承关系检查:");
            System.out.println("ComplexType -> EntityType 继承是否有效: " + validInheritance);
            
            // 7. 动态添加新的类型信息到上下文
            ComplianceKnowledgeBase.TypeDefinition newComplexType = 
                new ComplianceKnowledgeBase.TypeDefinition(
                    "NewNamespace.CustomComplexType",
                    "NewNamespace",
                    "CustomComplexType",
                    ComplianceKnowledgeBase.TypeKind.COMPLEX_TYPE,
                    null, // 没有基类型
                    null  // 没有额外属性
                );
            
            context.addTemporaryTypeDefinition(newComplexType);
            
            // 检查新添加的类型
            boolean isNewTypeDefined = context.isTypeDefined("NewNamespace.CustomComplexType");
            System.out.println("新添加的类型是否已定义: " + isNewTypeDefined);
            
            // 8. 构建包含所有临时状态的完整知识库
            ComplianceKnowledgeBase completeKb = context.buildCompleteKnowledgeBase();
            System.out.println("完整知识库中的类型数量: " + completeKb.getAllDefinedTypes().size());
            
        } catch (Exception e) {
            System.err.println("验证过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 演示如何在测试中使用合规性上下文
     */
    public static class TestUsageExample {
        
        public void demonstrateTestUsage() throws Exception {
            // 创建测试专用的验证管理器
            ComplianceValidationManager testManager = new ComplianceValidationManager();
            
            // 加载测试模式
            testManager.loadKnowledgeBase("src/test/resources/validation/schemas/test-schema.xml");
            
            // 创建测试上下文
            ComplianceContext testContext = testManager.createValidationContext();
            
            // 验证测试文件
            ComplianceContext.ValidationResult result = testManager.validateFile(
                "src/test/resources/validation/single/invalid/type-error/invalid-complextype-inherits-entitytype/invalid-complextype-inherits-entitytype.xml",
                testContext
            );
            
            // 断言验证结果
            assert !result.isValid() : "ComplexType继承EntityType应该是无效的";
            assert result.getIssues().size() > 0 : "应该有验证错误";
            
            // 检查特定的错误类型
            boolean hasInvalidBaseTypeError = result.getIssues().stream()
                .anyMatch(issue -> issue.getErrorType() == ComplianceErrorType.INVALID_BASE_TYPE);
            
            assert hasInvalidBaseTypeError : "应该包含无效基类型错误";
        }
    }
    
    /**
     * 演示如何扩展知识库
     */
    public static class ExtensionExample {
        
        public void demonstrateKnowledgeBaseExtension() throws Exception {
            ComplianceValidationManager manager = new ComplianceValidationManager();
            
            // 创建自定义知识库
            ComplianceKnowledgeBase.Builder customBuilder = new ComplianceKnowledgeBase.Builder();
            
            // 添加自定义实体类型
            ComplianceKnowledgeBase.TypeDefinition customEntity = 
                new ComplianceKnowledgeBase.TypeDefinition(
                    "Custom.Entity",
                    "Custom",
                    "Entity",
                    ComplianceKnowledgeBase.TypeKind.ENTITY_TYPE,
                    null,
                    null
                );
            
            customBuilder.addTypeDefinition(customEntity);
            
            // 添加自定义复杂类型（正确的继承关系）
            ComplianceKnowledgeBase.TypeDefinition customComplex = 
                new ComplianceKnowledgeBase.TypeDefinition(
                    "Custom.ComplexType",
                    "Custom",
                    "ComplexType",
                    ComplianceKnowledgeBase.TypeKind.COMPLEX_TYPE,
                    null, // 复杂类型可以没有基类型
                    null
                );
            
            customBuilder.addTypeDefinition(customComplex);
            
            ComplianceKnowledgeBase customKb = customBuilder.build();
            
            // 合并到管理器
            manager.mergeKnowledgeBase(customKb);
            
            // 验证扩展后的知识库
            ComplianceKnowledgeBase finalKb = manager.getGlobalKnowledgeBase();
            
            assert finalKb.isTypeDefined("Custom.Entity") : "自定义实体类型应该存在";
            assert finalKb.isTypeDefined("Custom.ComplexType") : "自定义复杂类型应该存在";
            assert finalKb.isEntityType("Custom.Entity") : "Custom.Entity应该是实体类型";
            assert finalKb.isComplexType("Custom.ComplexType") : "Custom.ComplexType应该是复杂类型";
        }
    }
}
