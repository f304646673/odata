package org.apache.olingo.schema.processor.examples;

import org.apache.olingo.schema.processor.validation.SchemaReferenceValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Schema引用验证示例
 * 
 * 演示如何验证OData Schema文件中的依赖引用是否正确
 */
public class SchemaValidationExample {
    
    private static final Logger logger = LoggerFactory.getLogger(SchemaValidationExample.class);
    
    public static void main(String[] args) {
        SchemaValidationExample example = new SchemaValidationExample();
        example.demonstrateSchemaValidation();
    }
    
    public void demonstrateSchemaValidation() {
        logger.info("开始Schema引用验证示例...");
        
        SchemaReferenceValidator validator = new SchemaReferenceValidator();
        
        // 测试正确的Schema文件
        testValidSchema(validator);
        
        // 测试错误的Schema文件
        testInvalidSchema(validator);
        
        logger.info("Schema引用验证示例完成！");
    }
    
    private void testValidSchema(SchemaReferenceValidator validator) {
        logger.info("\n=== 测试正确的Schema文件 ===");
        
        try {
            Path validSchemaFile = Paths.get("examples/schemas/common/Products.xml");
            SchemaReferenceValidator.ValidationResult result = validator.validateSchemaReferences(validSchemaFile);
            
            logger.info("文件: {}", validSchemaFile);
            logger.info("验证结果: {}", result.isValid() ? "有效" : "无效");
            
            if (result.isValid()) {
                logger.info("✓ Schema文件格式正确，所有依赖都已正确引用");
                logger.info("引用的namespace: {}", result.getReferencedNamespaces());
                logger.info("声明的引用: {}", result.getDeclaredReferences());
            } else {
                logger.error("✗ Schema文件存在问题:");
                for (String error : result.getErrors()) {
                    logger.error("  - {}", error);
                }
            }
            
        } catch (Exception e) {
            logger.error("验证过程中发生错误", e);
        }
    }
    
    private void testInvalidSchema(SchemaReferenceValidator validator) {
        logger.info("\n=== 测试错误的Schema文件 ===");
        
        try {
            Path invalidSchemaFile = Paths.get("examples/schemas/common/Products-Invalid.xml");
            SchemaReferenceValidator.ValidationResult result = validator.validateSchemaReferences(invalidSchemaFile);
            
            logger.info("文件: {}", invalidSchemaFile);
            logger.info("验证结果: {}", result.isValid() ? "有效" : "无效");
            
            if (!result.isValid()) {
                logger.info("✓ 正确检测到Schema文件中的依赖问题:");
                for (String error : result.getErrors()) {
                    logger.info("  - {}", error);
                }
                
                logger.info("引用的namespace: {}", result.getReferencedNamespaces());
                logger.info("缺失的引用: {}", result.getMissingReferences());
                
                // 显示修正建议
                logger.info("\n修正建议:");
                for (String missingNs : result.getMissingReferences()) {
                    logger.info("需要添加对 {} 的引用:", missingNs);
                    logger.info("  <edmx:Reference Uri=\"path/to/{}.xml\">", missingNs.replace(".", "-"));
                    logger.info("    <edmx:Include Namespace=\"{}\"/>", missingNs);
                    logger.info("  </edmx:Reference>");
                }
            } else {
                logger.error("✗ 验证器未能检测到Schema文件中的依赖问题");
            }
            
        } catch (Exception e) {
            logger.error("验证过程中发生错误", e);
        }
    }
}
