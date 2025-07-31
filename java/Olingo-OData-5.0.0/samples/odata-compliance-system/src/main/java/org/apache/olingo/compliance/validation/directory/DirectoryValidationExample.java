package org.apache.olingo.compliance.validation.directory;

import org.apache.olingo.compliance.file.ComplianceIssue;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * 目录验证使用示例
 * 演示如何使用DirectoryValidationManager进行多文件OData XML验证
 */
public class DirectoryValidationExample {
    
    public static void main(String[] args) {
        try {
            // 创建目录验证管理器
            DirectoryValidationManager validationManager = new DirectoryValidationManager();
            
            // 示例1：验证有效的目录结构
            System.out.println("=== 示例1：验证有效的目录结构 ===");
            validateValidScenario(validationManager);
            
            System.out.println("\n=== 示例2：验证无效的目录结构 ===");
            validateInvalidScenario(validationManager);
            
            System.out.println("\n=== 示例3：生成详细的冲突报告 ===");
            generateConflictReport(validationManager);
            
        } catch (Exception e) {
            System.err.println("验证过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 验证有效场景
     */
    private static void validateValidScenario(DirectoryValidationManager validationManager) throws IOException {
        String validDirectoryPath = "src/test/resources/validation/multiple/valid/scenario1-separate-namespaces";
        
        System.out.println("验证目录: " + validDirectoryPath);
        
        DirectoryValidationManager.DirectoryValidationResult result = 
            validationManager.validateDirectory(validDirectoryPath);
        
        System.out.println("验证结果:");
        System.out.println("  - 总文件数: " + result.getTotalFiles());
        System.out.println("  - 有效文件数: " + result.getValidFileCount());
        System.out.println("  - 是否有效: " + result.isValid());
        System.out.println("  - 冲突数量: " + result.getConflictIssues().size());
        System.out.println("  - 解析错误数: " + result.getAllIssues().size());
        System.out.println("  - 验证时间: " + result.getValidationTimeMs() + "ms");
        
        // 显示统计信息
        if (result.getStatistics() != null) {
            System.out.println("  - 统计信息: " + result.getStatistics().toString());
        }
        
        // 显示每个文件的验证结果
        System.out.println("文件级验证结果:");
        result.getValidationResults().forEach((filePath, validationResult) -> {
            System.out.println("  - " + filePath + ": " + 
                             (validationResult.isCompliant() ? "有效" : "无效") + 
                             " (问题数: " + validationResult.getIssues().size() + ")");
        });
    }
    
    /**
     * 验证无效场景
     */
    private static void validateInvalidScenario(DirectoryValidationManager validationManager) throws IOException {
        String invalidDirectoryPath = "src/test/resources/validation/multiple/invalid/scenario1-element-conflicts";
        
        System.out.println("验证目录: " + invalidDirectoryPath);
        
        DirectoryValidationManager.DirectoryValidationResult result = 
            validationManager.validateDirectory(invalidDirectoryPath);
        
        System.out.println("验证结果:");
        System.out.println("  - 总文件数: " + result.getTotalFiles());
        System.out.println("  - 有效文件数: " + result.getValidFileCount());
        System.out.println("  - 是否有效: " + result.isValid());
        System.out.println("  - 冲突数量: " + result.getConflictIssues().size());
        System.out.println("  - 解析错误数: " + result.getAllIssues().size());
        System.out.println("  - 总问题数: " + result.getTotalIssueCount());
        
        // 显示冲突详情
        if (!result.getConflictIssues().isEmpty()) {
            System.out.println("冲突详情:");
            result.getConflictIssues().forEach(issue -> {
                System.out.println("  - " + issue.getErrorType() + ": " + issue.getMessage());
                System.out.println("    位置: " + issue.getLocation());
            });
        }
        
        // 显示文件级验证错误
        System.out.println("文件级验证结果:");
        result.getValidationResults().forEach((filePath, validationResult) -> {
            System.out.println("  - " + filePath + ": " + 
                             (validationResult.isCompliant() ? "有效" : "无效"));
            
            if (!validationResult.isCompliant()) {
                validationResult.getIssues().forEach(issue -> {
                    System.out.println("    * " + issue.getErrorType() + ": " + issue.getMessage());
                });
            }
        });
    }
    
    /**
     * 生成详细的冲突报告
     */
    private static void generateConflictReport(DirectoryValidationManager validationManager) throws IOException {
        // 创建一个测试用的冲突检测器
        SchemaConflictDetector conflictDetector = new SchemaConflictDetector();
        
        // 这里可以手动创建一些Schema信息来演示冲突检测
        System.out.println("演示冲突检测功能...");
        
        // 创建有冲突的Schema映射
        java.util.Map<String, java.util.Set<DirectoryValidationManager.SchemaInfo>> namespaceMap = 
            new java.util.HashMap<>();
        
        // 添加有元素冲突的Schema
        java.util.Set<String> schema1Elements = java.util.Set.of("EntityType:Customer", "ComplexType:Address");
        java.util.Set<String> schema2Elements = java.util.Set.of("EntityType:Customer", "ComplexType:ProductInfo");
        
        DirectoryValidationManager.SchemaInfo schema1 = new DirectoryValidationManager.SchemaInfo(
            "Demo.Namespace", "Demo1", "/demo/file1.xml", schema1Elements
        );
        
        DirectoryValidationManager.SchemaInfo schema2 = new DirectoryValidationManager.SchemaInfo(
            "Demo.Namespace", "Demo2", "/demo/file2.xml", schema2Elements
        );
        
        namespaceMap.put("Demo.Namespace", java.util.Set.of(schema1, schema2));
        
        // 生成冲突报告
        SchemaConflictDetector.ConflictDetectionReport report = 
            conflictDetector.generateReport(namespaceMap);
        
        System.out.println("冲突检测报告:");
        System.out.println("  - 报告摘要: " + report.toString());
        System.out.println("  - 是否有冲突: " + report.hasConflicts());
        System.out.println("  - 总冲突数: " + report.getTotalConflictCount());
        
        if (report.hasConflicts()) {
            System.out.println("详细冲突信息:");
            
            if (!report.getElementConflicts().isEmpty()) {
                System.out.println("  元素冲突:");
                report.getElementConflicts().forEach(issue -> 
                    System.out.println("    - " + issue.getMessage())
                );
            }
            
            if (!report.getAliasConflicts().isEmpty()) {
                System.out.println("  别名冲突:");
                report.getAliasConflicts().forEach(issue -> 
                    System.out.println("    - " + issue.getMessage())
                );
            }
            
            if (!report.getCrossNamespaceAliasConflicts().isEmpty()) {
                System.out.println("  跨命名空间别名冲突:");
                report.getCrossNamespaceAliasConflicts().forEach(issue -> 
                    System.out.println("    - " + issue.getMessage())
                );
            }
        }
    }
    
    /**
     * 演示批量验证多个目录
     */
    public static void demonstrateBatchValidation() {
        try {
            DirectoryValidationManager validationManager = new DirectoryValidationManager();
            
            String[] testDirectories = {
                "src/test/resources/validation/multiple/valid/scenario1-separate-namespaces",
                "src/test/resources/validation/multiple/valid/scenario2-same-namespace-no-conflicts",
                "src/test/resources/validation/multiple/invalid/scenario1-element-conflicts",
                "src/test/resources/validation/multiple/invalid/scenario2-alias-conflicts"
            };
            
            System.out.println("=== 批量验证演示 ===");
            
            int validDirectories = 0;
            int invalidDirectories = 0;
            long totalTime = 0;
            int totalFiles = 0;
            
            for (String directory : testDirectories) {
                System.out.println("\n验证目录: " + directory);
                
                try {
                    DirectoryValidationManager.DirectoryValidationResult result = 
                        validationManager.validateDirectory(directory);
                    
                    if (result.isValid()) {
                        validDirectories++;
                        System.out.println("  ✅ 验证通过");
                    } else {
                        invalidDirectories++;
                        System.out.println("  ❌ 验证失败");
                        System.out.println("    冲突: " + result.getConflictIssues().size());
                        System.out.println("    解析错误: " + result.getAllIssues().size());
                    }
                    
                    totalTime += result.getValidationTimeMs();
                    totalFiles += result.getTotalFiles();
                    
                    System.out.println("    文件数: " + result.getTotalFiles());
                    System.out.println("    验证时间: " + result.getValidationTimeMs() + "ms");
                    
                } catch (Exception e) {
                    invalidDirectories++;
                    System.out.println("  ❌ 验证异常: " + e.getMessage());
                }
            }
            
            System.out.println("\n=== 批量验证汇总 ===");
            System.out.println("总目录数: " + testDirectories.length);
            System.out.println("有效目录: " + validDirectories);
            System.out.println("无效目录: " + invalidDirectories);
            System.out.println("总文件数: " + totalFiles);
            System.out.println("总验证时间: " + totalTime + "ms");
            System.out.println("平均每目录验证时间: " + (totalTime / testDirectories.length) + "ms");
            
        } catch (Exception e) {
            System.err.println("批量验证过程中发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 演示目录验证的最佳实践
     */
    public static void demonstrateBestPractices() {
        System.out.println("=== 目录验证最佳实践 ===");
        
        System.out.println("1. 目录结构建议:");
        System.out.println("   - 将相关的Schema文件放在同一目录下");
        System.out.println("   - 使用有意义的文件名和目录名");
        System.out.println("   - 按功能模块组织子目录");
        
        System.out.println("\n2. 命名空间管理:");
        System.out.println("   - 确保命名空间的唯一性和一致性");
        System.out.println("   - 避免跨文件的元素名称冲突");
        System.out.println("   - 谨慎使用命名空间别名");
        
        System.out.println("\n3. 依赖关系处理:");
        System.out.println("   - 基础类型文件应该在依赖文件之前加载");
        System.out.println("   - 避免循环依赖");
        System.out.println("   - 确保所有引用的类型都有定义");
        
        System.out.println("\n4. 验证策略:");
        System.out.println("   - 先进行单文件验证，再进行目录级验证");
        System.out.println("   - 关注冲突检测结果");
        System.out.println("   - 定期运行完整的目录验证");
        
        System.out.println("\n5. 性能优化:");
        System.out.println("   - 对于大型目录，考虑分批验证");
        System.out.println("   - 利用缓存机制提升性能");
        System.out.println("   - 监控验证时间和资源使用");
    }
}
