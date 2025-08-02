package org.apache.olingo.compliance.validator.file.impl;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.compliance.core.api.ValidationConfig;
import org.apache.olingo.compliance.core.model.ComplianceErrorType;
import org.apache.olingo.compliance.core.model.ComplianceIssue;
import org.apache.olingo.compliance.core.model.ComplianceResult;
import org.apache.olingo.compliance.engine.core.impl.DefaultValidationEngineImpl;
import org.apache.olingo.compliance.engine.core.SchemaRegistry;
import org.apache.olingo.compliance.engine.core.ValidationContext;
import org.apache.olingo.compliance.engine.core.ValidationEngine;
import org.apache.olingo.compliance.validator.file.FileValidator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * 增强的Registry-aware验证器，支持跨文件引用验证
 *
 * 合并了RegistryAwareXmlValidator和EnhancedRegistryAwareXmlValidator的功能：
 * 1. 基础的类型引用和继承关系验证
 * 2. 增强的ValidationEngine支持，包含跨文件引用验证规则
 * 3. 完整的XmlFileComplianceValidator接口实现
 * 4. Schema Registry注入到ValidationContext的机制
 */
public class FileValidatorImpl implements FileValidator {

    private final ValidationEngine engine;
    
    public FileValidatorImpl() {
        this.engine = createEnhancedValidationEngine();
    }
    
    public FileValidatorImpl(ValidationEngine customEngine) {
        this.engine = customEngine;
    }
    
    /**
     * 使用Schema Registry验证单个文件，支持跨文件引用检查
     * @param xmlFile 要验证的文件
     * @param registry Schema注册表，包含已知的类型定义
     * @return 验证结果，包含跨文件引用检查结果
     */
    public ComplianceResult validateWithRegistry(File xmlFile, SchemaRegistry registry) {
        List<ComplianceIssue> issues = new ArrayList<>();
        
        try {
            // 进行基础的XML格式检查
            if (!xmlFile.exists()) {
                issues.add(new ComplianceIssue(
                    ComplianceErrorType.VALIDATION_ERROR,
                    "File does not exist",
                    null,
                    xmlFile.getAbsolutePath(),
                    ComplianceIssue.Severity.ERROR
                ));
                return createResult(false, issues, xmlFile);
            }

            // 1. 基础的Registry验证（原RegistryAwareXmlValidator功能）
            issues.addAll(validateTypeReferences(xmlFile, registry));
            issues.addAll(validateInheritanceRelations(xmlFile, registry));

            // 2. 增强的ValidationEngine验证（原EnhancedRegistryAwareXmlValidator功能）
            ValidationContext context = ValidationContext.forFile(xmlFile.toPath());

            // 解析Schema（如果需要）
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
            
            // 使用ValidationEngine进行验证（包括跨文件引用检查）
            ValidationConfig config = ValidationConfig.standard();
            org.apache.olingo.compliance.core.api.ValidationResult result = engine.validate(context, config);
            
            // 添加ValidationEngine的验证结果
            for (String error : result.getErrors()) {
                issues.add(createComplianceIssue(error, xmlFile, ComplianceIssue.Severity.ERROR));
            }
            
            for (String warning : result.getWarnings()) {
                issues.add(createComplianceIssue(warning, xmlFile, ComplianceIssue.Severity.WARNING));
            }
            
        } catch (Exception e) {
            issues.add(createComplianceIssue(
                "Failed to validate with enhanced registry: " + e.getMessage(), 
                xmlFile, 
                ComplianceIssue.Severity.ERROR
            ));
        }

        // 创建验证结果
        boolean isCompliant = issues.isEmpty() ||
                             issues.stream().noneMatch(issue -> issue.getSeverity() == ComplianceIssue.Severity.ERROR);

        return createResult(isCompliant, issues, xmlFile);
    }

    /**
     * 验证类型引用是否存在（来自RegistryAwareXmlValidator）
     */
    private List<ComplianceIssue> validateTypeReferences(File xmlFile, SchemaRegistry registry) {
        List<ComplianceIssue> issues = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            // 安全配置
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);

            // 检查Property元素的Type属性
            NodeList properties = document.getElementsByTagNameNS("*", "Property");
            for (int i = 0; i < properties.getLength(); i++) {
                Element property = (Element) properties.item(i);
                String typeName = property.getAttribute("Type");

                if (typeName != null && !typeName.isEmpty() && !isBuiltInType(typeName)) {
                    if (!registry.isTypeExists(typeName)) {
                        issues.add(new ComplianceIssue(
                            ComplianceErrorType.TYPE_NOT_EXIST,
                            "Type '" + typeName + "' is not defined in any schema",
                            null,
                            xmlFile.getAbsolutePath(),
                            ComplianceIssue.Severity.ERROR
                        ));
                    }
                }
            }

            // 检查NavigationProperty元素的Type属性
            NodeList navProperties = document.getElementsByTagNameNS("*", "NavigationProperty");
            for (int i = 0; i < navProperties.getLength(); i++) {
                Element navProperty = (Element) navProperties.item(i);
                String typeName = navProperty.getAttribute("Type");

                if (typeName != null && !typeName.isEmpty()) {
                    // 处理Collection(TypeName)格式
                    String actualTypeName = extractTypeFromCollection(typeName);
                    if (!isBuiltInType(actualTypeName) && !registry.isTypeExists(actualTypeName)) {
                        issues.add(new ComplianceIssue(
                            ComplianceErrorType.TYPE_NOT_EXIST,
                            "Type '" + actualTypeName + "' is not defined in any schema",
                            null,
                            xmlFile.getAbsolutePath(),
                            ComplianceIssue.Severity.ERROR
                        ));
                    }
                }
            }

        } catch (Exception e) {
            issues.add(new ComplianceIssue(
                ComplianceErrorType.VALIDATION_ERROR,
                "Failed to validate type references: " + e.getMessage(),
                null,
                xmlFile.getAbsolutePath(),
                ComplianceIssue.Severity.ERROR
            ));
        }

        return issues;
    }

    /**
     * 验证继承关系是否有效（来自RegistryAwareXmlValidator）
     */
    private List<ComplianceIssue> validateInheritanceRelations(File xmlFile, SchemaRegistry registry) {
        List<ComplianceIssue> issues = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            // 安全配置
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);

            // 获取当前文件的Schema命名空间
            NodeList schemas = document.getElementsByTagNameNS("*", "Schema");
            String currentNamespace = "";
            if (schemas.getLength() > 0) {
                Element schema = (Element) schemas.item(0);
                currentNamespace = schema.getAttribute("Namespace");
            }

            // 检查EntityType的继承关系
            NodeList entityTypes = document.getElementsByTagNameNS("*", "EntityType");
            for (int i = 0; i < entityTypes.getLength(); i++) {
                Element entityType = (Element) entityTypes.item(i);
                String typeName = entityType.getAttribute("Name");
                String baseType = entityType.getAttribute("BaseType");

                if (baseType != null && !baseType.isEmpty()) {
                    String fullTypeName = currentNamespace + "." + typeName;
                    if (!registry.isValidBaseType(fullTypeName, baseType)) {
                        ComplianceErrorType errorType = determineInheritanceErrorType(baseType, registry);
                        String message = determineInheritanceErrorMessage(typeName, baseType, errorType);

                        issues.add(new ComplianceIssue(
                            errorType,
                            message,
                            null,
                            xmlFile.getAbsolutePath(),
                            ComplianceIssue.Severity.ERROR
                        ));
                    }
                }
            }

            // 检查ComplexType的继承关系
            NodeList complexTypes = document.getElementsByTagNameNS("*", "ComplexType");
            for (int i = 0; i < complexTypes.getLength(); i++) {
                Element complexType = (Element) complexTypes.item(i);
                String typeName = complexType.getAttribute("Name");
                String baseType = complexType.getAttribute("BaseType");

                if (baseType != null && !baseType.isEmpty()) {
                    String fullTypeName = currentNamespace + "." + typeName;
                    if (!registry.isValidBaseType(fullTypeName, baseType)) {
                        ComplianceErrorType errorType = determineInheritanceErrorType(baseType, registry);
                        String message = determineInheritanceErrorMessage(typeName, baseType, errorType);

                        issues.add(new ComplianceIssue(
                            errorType,
                            message,
                            null,
                            xmlFile.getAbsolutePath(),
                            ComplianceIssue.Severity.ERROR
                        ));
                    }
                }
            }

        } catch (Exception e) {
            issues.add(new ComplianceIssue(
                ComplianceErrorType.VALIDATION_ERROR,
                "Failed to validate inheritance relations: " + e.getMessage(),
                null,
                xmlFile.getAbsolutePath(),
                ComplianceIssue.Severity.ERROR
            ));
        }

        return issues;
    }

    /**
     * 创建增强的ValidationEngine，包含跨文件引用验证规则
     */
    private ValidationEngine createEnhancedValidationEngine() {
        DefaultValidationEngineImpl validationEngine = new DefaultValidationEngineImpl();
        
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
     * 检查是否是内置类型
     */
    private boolean isBuiltInType(String typeName) {
        return typeName.startsWith("Edm.") ||
               typeName.equals("String") ||
               typeName.equals("Int32") ||
               typeName.equals("Boolean") ||
               typeName.equals("DateTimeOffset") ||
               typeName.equals("Guid") ||
               typeName.equals("Decimal") ||
               typeName.equals("Double") ||
               typeName.equals("Single") ||
               typeName.equals("Byte") ||
               typeName.equals("SByte") ||
               typeName.equals("Int16") ||
               typeName.equals("Int64") ||
               typeName.equals("Binary") ||
               typeName.equals("Date") ||
               typeName.equals("TimeOfDay") ||
               typeName.equals("Duration");
    }

    /**
     * 从Collection(TypeName)格式中提取类型名
     */
    private String extractTypeFromCollection(String typeName) {
        if (typeName.startsWith("Collection(") && typeName.endsWith(")")) {
            return typeName.substring(11, typeName.length() - 1);
        }
        return typeName;
    }

    /**
     * 根据基类型确定继承错误的类型
     */
    private ComplianceErrorType determineInheritanceErrorType(String baseType, SchemaRegistry registry) {
        // 如果基类型完全不存在，则是依赖错误
        if (!registry.isTypeExists(baseType)) {
            return ComplianceErrorType.SCHEMA_DEPENDENCY_ERROR;
        }

        // 如果基类型存在但不是合法的基类型，则是继承层次结构错误
        return ComplianceErrorType.INVALID_INHERITANCE_HIERARCHY;
    }

    /**
     * 根据错误类型生成适当的错误消息
     */
    private String determineInheritanceErrorMessage(String typeName, String baseType, ComplianceErrorType errorType) {
        if (errorType == ComplianceErrorType.SCHEMA_DEPENDENCY_ERROR) {
            return "Schema dependency error: Type '" + typeName + "' references non-existent base type '" + baseType + "'";
        } else {
            return "Invalid inheritance hierarchy: Type '" + typeName + "' cannot inherit from '" + baseType + "'";
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
    private ComplianceResult createResult(boolean isCompliant, List<ComplianceIssue> issues, File xmlFile) {
        return new ComplianceResult(
            isCompliant,
            issues,
            new HashSet<>(), // referencedNamespaces
            new HashMap<>(), // metadata
            xmlFile.getAbsolutePath(),
            System.currentTimeMillis()
        );
    }
    
    // 实现XmlFileComplianceValidator接口的方法
    
    @Override
    public ComplianceResult validateFile(File xmlFile, SchemaRegistry registry) {
        return validateWithRegistry(xmlFile, registry);
    }
    
    @Override
    public ComplianceResult validateFile(Path xmlPath, SchemaRegistry registry) {
        return validateFile(xmlPath.toFile(), registry);
    }
    
    @Override
    public ComplianceResult validateContent(String xmlContent, String fileName, SchemaRegistry registry) {
        try {
            // 创建临时文件来处理字符串内容
            File tempFile = File.createTempFile("temp_validation_", ".xml");
            tempFile.deleteOnExit();
            
            // 写入内容到临时文件
            java.nio.file.Files.write(tempFile.toPath(), xmlContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // 使用文件验证方法
            ComplianceResult result = validateFile(tempFile, registry);

            // 更新结果中的文件名信息
            if (fileName != null && !fileName.isEmpty()) {
                // 创建新的结果对象，使用提供的文件名
                return new ComplianceResult(
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
            
            return new ComplianceResult(
                false,
                issues,
                new HashSet<>(),
                new HashMap<>(),
                fileName != null ? fileName : "unknown",
                System.currentTimeMillis()
            );
        }
    }
}
