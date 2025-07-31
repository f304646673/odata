package org.apache.olingo.compliance.validator.file;

import org.apache.olingo.compliance.core.model.ComplianceErrorType;
import org.apache.olingo.compliance.engine.core.SchemaRegistry;
import org.apache.olingo.compliance.core.model.XmlComplianceResult;
import org.apache.olingo.compliance.core.model.ComplianceIssue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * 带有Schema Registry支持的单文件验证器
 * 可以检测跨文件的类型依赖和继承关系
 */
public class RegistryAwareXmlValidator {
    
    public RegistryAwareXmlValidator() {
        // 不再依赖可能有问题的ModernXmlFileComplianceValidator
    }
    
    /**
     * 使用Schema Registry验证单个文件
     * @param xmlFile 要验证的文件
     * @param registry Schema注册表，包含已知的类型定义
     * @return 验证结果，包含类型依赖错误
     */
    public XmlComplianceResult validateWithRegistry(File xmlFile, SchemaRegistry registry) {
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
            
            // 进行Registry验证
            issues.addAll(validateTypeReferences(xmlFile, registry));
            issues.addAll(validateInheritanceRelations(xmlFile, registry));
            
        } catch (Exception e) {
            issues.add(new ComplianceIssue(
                ComplianceErrorType.VALIDATION_ERROR,
                "Failed to validate with registry: " + e.getMessage(),
                null,
                xmlFile.getAbsolutePath(),
                ComplianceIssue.Severity.ERROR
            ));
        }
        
        // 创建验证结果
        boolean isCompliant = issues.isEmpty() || 
                             issues.stream().noneMatch(issue -> issue.getSeverity() == ComplianceIssue.Severity.ERROR);
        
        return createResult(isCompliant, issues, xmlFile);
    }
    
    /**
     * 创建验证结果
     */
    private XmlComplianceResult createResult(boolean isCompliant, List<ComplianceIssue> issues, File xmlFile) {
        return new XmlComplianceResult(
            isCompliant,
            issues,
            new HashSet<>(),   // referencedNamespaces
            new HashMap<>(),   // metadata
            xmlFile.getName(),
            0L                 // validationTimeMs
        );
    }
    
    /**
     * 验证类型引用是否存在
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
     * 验证继承关系是否有效
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
}
