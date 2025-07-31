package org.apache.olingo.compliance.examples;

import java.io.File;

import org.apache.olingo.compliance.file.ModernXmlFileComplianceValidator;
import org.apache.olingo.compliance.file.XmlComplianceResult;

/**
 * 演示如何使用移植后的 ModernXmlFileComplianceValidator 来检测单个 OData XML 文件的合规性
 */
public class SingleFileValidationDemo {
    
    public static void main(String[] args) {
        System.out.println("=== OData 4.0 单文件合规性检测演示 ===");
        
        // 创建验证器实例 - 使用严格模式
        ModernXmlFileComplianceValidator validator = ModernXmlFileComplianceValidator.strict();
        
        // 测试文件目录
        String testDir = "src/test/resources/validation/single";
        
        // 测试一个有效的文件
        testFile(validator, testDir + "/valid/valid-complex.xml", "有效的XML文件");
        
        // 测试一个无效的文件 
        testFile(validator, testDir + "/invalid/invalid-xml/invalid-invalid-xml.xml", "XML格式错误");
        
        // 测试一个属性错误的文件
        testFile(validator, testDir + "/invalid/attribute-error/invalid-name-not-identifier.xml", "属性错误");
        
        System.out.println("\n=== 演示完成 ===");
    }
    
    private static void testFile(ModernXmlFileComplianceValidator validator, String filePath, String description) {
        System.out.println("\n--- 测试 " + description + " ---");
        System.out.println("文件: " + filePath);
        
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("❌ 文件不存在: " + file.getAbsolutePath());
            return;
        }
        
        try {
            // 执行验证
            XmlComplianceResult result = validator.validateFile(file);
            
            // 输出结果
            System.out.println("合规性: " + (result.isCompliant() ? "✅ 通过" : "❌ 不通过"));
            System.out.println("验证时间: " + result.getValidationTimeMs() + "ms");
            
            if (result.hasErrors()) {
                System.out.println("错误数量: " + result.getErrorCount());
                System.out.println("错误详情:");
                for (String error : result.getErrors()) {
                    System.out.println("  - " + error);
                }
            }
            
            if (result.hasWarnings()) {
                System.out.println("警告数量: " + result.getWarningCount());
                System.out.println("警告详情:");
                for (String warning : result.getWarnings()) {
                    System.out.println("  - " + warning);
                }
            }
            
        } catch (Exception e) {
            System.out.println("❌ 验证过程中出现异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
