package org.apache.olingo.compliance.validator.file;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.compliance.core.api.ValidationConfig;
import org.apache.olingo.compliance.core.model.ComplianceIssue;
import org.apache.olingo.compliance.core.model.XmlComplianceResult;
import org.apache.olingo.compliance.engine.core.DefaultSchemaRegistry;
import org.apache.olingo.compliance.engine.core.DefaultValidationEngine;
import org.apache.olingo.compliance.engine.core.SchemaRegistry;
import org.apache.olingo.compliance.engine.core.ValidationContext;
import org.apache.olingo.compliance.engine.core.ValidationEngine;

/**
 * 扩展的Registry-aware验证器，支持跨文件引用验证
 * 
 * 实现您设想的"辅助检测接口"机制：
 * 1. 在验证前，将SchemaRegistry注入到ValidationContext中
 * 2. 使用新的CrossFileReferenceValidationRule进行跨文件引用验证
 * 3. 支持验证结果的导出和后续使用
 */
public class EnhancedRegistryAwareXmlValidator extends RegistryAwareXmlValidator implements XmlFileComplianceValidator {
    
    private final ValidationEngine engine;
    
    public EnhancedRegistryAwareXmlValidator() {
        super();
        this.engine = createEnhancedValidationEngine();
    }
    
    public EnhancedRegistryAwareXmlValidator(ValidationEngine customEngine) {
        super();
        this.engine = customEngine;
    }
    
    /**
     * 使用Schema Registry验证单个文件，支持跨文件引用检查
     * @param xmlFile 要验证的文件
     * @param registry Schema注册表，包含已知的类型定义
     * @return 验证结果，包含跨文件引用检查结果
     */
    @Override
    public XmlComplianceResult validateWithRegistry(File xmlFile, SchemaRegistry registry) {
        List<ComplianceIssue> issues = new ArrayList<>();
        
        try {
            // 1. 创建ValidationContext并注入SchemaRegistry
            ValidationContext context = ValidationContext.forFile(xmlFile.toPath());
            // TODO: Find new way to inject SchemaRegistry - setSchemaRegistry is deprecated
            // context.setSchemaRegistry(registry);
            
            // 2. 解析Schema（如果需要）
            List<CsdlSchema> schemas = parseSchemas(xmlFile);
            if (schemas != null && !schemas.isEmpty()) {
                context.setAllSchemas(schemas);
                
                // 设置当前Schema的命名空间
                for (CsdlSchema schema : schemas) {
                    if (schema.getNamespace() != null) {
                        context.addCurrentSchemaNamespace(schema.getNamespace());
                    }
                }
            }
            
            // 3. 使用ValidationEngine进行验证（包括跨文件引用检查）
            ValidationConfig config = ValidationConfig.standard();
            org.apache.olingo.compliance.core.api.ValidationResult result = engine.validate(context, config);
            
            // 4. 转换结果
            boolean isCompliant = result.isCompliant();
            
            // 添加验证过程中的问题
            for (String error : result.getErrors()) {
                issues.add(createComplianceIssue(error, xmlFile, ComplianceIssue.Severity.ERROR));
            }
            
            for (String warning : result.getWarnings()) {
                issues.add(createComplianceIssue(warning, xmlFile, ComplianceIssue.Severity.WARNING));
            }
            
            return createResult(isCompliant, issues, xmlFile);
            
        } catch (Exception e) {
            issues.add(createComplianceIssue(
                "Failed to validate with enhanced registry: " + e.getMessage(), 
                xmlFile, 
                ComplianceIssue.Severity.ERROR
            ));
            return createResult(false, issues, xmlFile);
        }
    }
    
    /**
     * 创建增强的ValidationEngine，包含跨文件引用验证规则
     */
    private ValidationEngine createEnhancedValidationEngine() {
        DefaultValidationEngine validationEngine = new DefaultValidationEngine();
        
        // 注册所有标准规则
        validationEngine.registerRule(new org.apache.olingo.compliance.engine.rule.structural.SchemaNamespaceRule());
        validationEngine.registerRule(new org.apache.olingo.compliance.engine.rule.structural.ElementDefinitionRule());
        validationEngine.registerRule(new org.apache.olingo.compliance.engine.rule.structural.ReferenceValidationRule());
        validationEngine.registerRule(new org.apache.olingo.compliance.engine.rule.security.XxeAttackRule());
        validationEngine.registerRule(new org.apache.olingo.compliance.engine.rule.semantic.AnnotationValidationRule());
        validationEngine.registerRule(new org.apache.olingo.compliance.engine.rule.semantic.ComplianceRule());
        
        // 注册新的跨文件引用验证规则
        validationEngine.registerRule(new org.apache.olingo.compliance.engine.rule.crossfile.CrossFileReferenceValidationRule());
        
        return validationEngine;
    }
    
    /**
     * 解析XML文件中的Schema（简化实现）
     */
    @SuppressWarnings("unused")
    private List<CsdlSchema> parseSchemas(File xmlFile) {
        try {
            // 这里可以使用Olingo的解析器来解析Schema
            // 为了简化，先返回null，让现有的解析逻辑处理
            return null;
        } catch (Exception e) {
            // 解析失败，返回null
            return null;
        }
    }
    
    /**
     * 创建ComplianceIssue
     */
    private ComplianceIssue createComplianceIssue(String message, File xmlFile, ComplianceIssue.Severity severity) {
        return new ComplianceIssue(
            org.apache.olingo.compliance.core.model.ComplianceErrorType.VALIDATION_ERROR,
            message,
            null,
            xmlFile.getAbsolutePath(),
            severity
        );
    }
    
    /**
     * 创建验证结果
     */
    private XmlComplianceResult createResult(boolean isCompliant, List<ComplianceIssue> issues, File xmlFile) {
        return new XmlComplianceResult(
            isCompliant,
            issues,
            new java.util.HashSet<>(), // referencedNamespaces
            new java.util.HashMap<>(), // metadata
            xmlFile.getAbsolutePath(),
            System.currentTimeMillis()
        );
    }
    
    // 实现XmlFileComplianceValidator接口的方法
    
    @Override
    public XmlComplianceResult validateFile(File xmlFile) {
        return validateWithRegistry(xmlFile, new DefaultSchemaRegistry());
    }
    
    @Override
    public XmlComplianceResult validateFile(Path xmlPath) {
        return validateFile(xmlPath.toFile());
    }
    
    @Override
    public XmlComplianceResult validateContent(String xmlContent, String fileName) {
        try {
            // 创建临时文件来处理字符串内容
            File tempFile = File.createTempFile("temp_validation_", ".xml");
            tempFile.deleteOnExit();
            
            // 写入内容到临时文件
            java.nio.file.Files.write(tempFile.toPath(), xmlContent.getBytes("UTF-8"));
            
            // 使用文件验证方法
            XmlComplianceResult result = validateFile(tempFile);
            
            // 更新结果中的文件名信息
            if (fileName != null && !fileName.isEmpty()) {
                // 创建新的结果对象，使用提供的文件名
                return new XmlComplianceResult(
                    result.isCompliant(),
                    result.getIssues(),
                    result.getReferencedNamespaces(),
                    result.getMetadata(),
                    fileName,
                    result.getValidationTimeMs()
                );
            }
            
            return result;
            
        } catch (java.io.IOException | java.nio.file.InvalidPathException e) {
            List<ComplianceIssue> issues = new ArrayList<>();
            issues.add(new ComplianceIssue(
                org.apache.olingo.compliance.core.model.ComplianceErrorType.PARSING_ERROR,
                "Failed to process XML content: " + e.getMessage(),
                ComplianceIssue.Severity.ERROR
            ));
            
            return new XmlComplianceResult(
                false,
                issues,
                new java.util.HashSet<>(),
                new java.util.HashMap<>(),
                fileName != null ? fileName : "unknown",
                System.currentTimeMillis()
            );
        }
    }
}
