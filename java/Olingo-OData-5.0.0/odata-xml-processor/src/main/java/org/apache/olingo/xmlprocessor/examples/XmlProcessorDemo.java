package org.apache.olingo.xmlprocessor.examples;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.xmlprocessor.parser.impl.CsdlXmlParserImpl;
import org.apache.olingo.xmlprocessor.parser.ODataXmlParser;

import java.io.InputStream;
import java.util.List;

/**
 * OData XML处理器示例程序
 * 演示如何使用XML处理器解析OData schema
 */
public class XmlProcessorDemo {
    
    public static void main(String[] args) {
        XmlProcessorDemo demo = new XmlProcessorDemo();
        
        System.out.println("=== OData XML Processor Demo ===");
        
        // 示例1：解析基础schema
        demo.parseBasicSchema();
        
        // 示例2：解析扩展schema
        demo.parseExtendedSchema();
        
        // 示例3：解析有冲突的schema
        demo.parseConflictingSchema();
        
        // 示例4：验证XML格式
        demo.validateXmlFormat();
    }
    
    /**
     * 解析基础schema示例
     */
    private void parseBasicSchema() {
        System.out.println("\n--- 解析基础Schema ---");
        
        CsdlXmlParserImpl parser = new CsdlXmlParserImpl();
        
        try (InputStream inputStream = getClass().getResourceAsStream("/test-schemas/basic-schema.xml")) {
            if (inputStream == null) {
                System.out.println("错误：找不到basic-schema.xml文件");
                return;
            }
            
            ODataXmlParser.ParseResult result = parser.parseSchemas(inputStream, "basic-schema.xml");
            
            if (result.isSuccess()) {
                System.out.println("✓ 解析成功！");
                System.out.println("解析到 " + result.getSchemas().size() + " 个schema");
                
                for (CsdlSchema schema : result.getSchemas()) {
                    System.out.println("  命名空间: " + schema.getNamespace());
                    if (schema.getEntityTypes() != null) {
                        System.out.println("  EntityTypes: " + schema.getEntityTypes().size());
                        schema.getEntityTypes().forEach(et -> 
                            System.out.println("    - " + et.getName()));
                    }
                    if (schema.getComplexTypes() != null) {
                        System.out.println("  ComplexTypes: " + schema.getComplexTypes().size());
                        schema.getComplexTypes().forEach(ct -> 
                            System.out.println("    - " + ct.getName()));
                    }
                    if (schema.getEnumTypes() != null) {
                        System.out.println("  EnumTypes: " + schema.getEnumTypes().size());
                        schema.getEnumTypes().forEach(et -> 
                            System.out.println("    - " + et.getName()));
                    }
                }
            } else {
                System.out.println("✗ 解析失败！");
                result.getErrors().forEach(error -> System.out.println("  错误: " + error));
            }
            
            if (!result.getWarnings().isEmpty()) {
                System.out.println("警告:");
                result.getWarnings().forEach(warning -> System.out.println("  " + warning));
            }
            
        } catch (Exception e) {
            System.out.println("异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 解析扩展schema示例
     */
    private void parseExtendedSchema() {
        System.out.println("\n--- 解析扩展Schema ---");
        
        CsdlXmlParserImpl parser = new CsdlXmlParserImpl();
        
        try (InputStream inputStream = getClass().getResourceAsStream("/test-schemas/extended-schema.xml")) {
            if (inputStream == null) {
                System.out.println("错误：找不到extended-schema.xml文件");
                return;
            }
            
            ODataXmlParser.ParseResult result = parser.parseSchemas(inputStream, "extended-schema.xml");
            
            if (result.isSuccess()) {
                System.out.println("✓ 解析成功！");
                System.out.println("解析到 " + result.getSchemas().size() + " 个schema");
                
                for (CsdlSchema schema : result.getSchemas()) {
                    System.out.println("  命名空间: " + schema.getNamespace());
                    if (schema.getEntityTypes() != null) {
                        System.out.println("  EntityTypes: " + schema.getEntityTypes().size());
                        schema.getEntityTypes().forEach(et -> 
                            System.out.println("    - " + et.getName()));
                    }
                }
            } else {
                System.out.println("✗ 解析失败！");
                result.getErrors().forEach(error -> System.out.println("  错误: " + error));
            }
            
        } catch (Exception e) {
            System.out.println("异常: " + e.getMessage());
        }
    }
    
    /**
     * 解析有冲突的schema示例
     */
    private void parseConflictingSchema() {
        System.out.println("\n--- 解析冲突Schema ---");
        
        CsdlXmlParserImpl parser = new CsdlXmlParserImpl();
        
        try (InputStream inputStream = getClass().getResourceAsStream("/test-schemas/conflicting-schema.xml")) {
            if (inputStream == null) {
                System.out.println("错误：找不到conflicting-schema.xml文件");
                return;
            }
            
            ODataXmlParser.ParseResult result = parser.parseSchemas(inputStream, "conflicting-schema.xml");
            
            if (result.isSuccess()) {
                System.out.println("✓ 解析成功！");
                System.out.println("解析到 " + result.getSchemas().size() + " 个schema");
                
                for (CsdlSchema schema : result.getSchemas()) {
                    System.out.println("  命名空间: " + schema.getNamespace());
                    if (schema.getEntityTypes() != null) {
                        System.out.println("  EntityTypes: " + schema.getEntityTypes().size());
                    }
                }
            } else {
                System.out.println("✗ 解析失败！");
                result.getErrors().forEach(error -> System.out.println("  错误: " + error));
            }
            
        } catch (Exception e) {
            System.out.println("异常: " + e.getMessage());
        }
    }
    
    /**
     * 验证XML格式示例
     */
    private void validateXmlFormat() {
        System.out.println("\n--- 验证XML格式 ---");
        
        CsdlXmlParserImpl parser = new CsdlXmlParserImpl();
        
        // 测试有效的XML
        String validXml = "<?xml version=\"1.0\"?><edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\"><edmx:DataServices><Schema Namespace=\"Test\" xmlns=\"http://docs.oasis-open.org/odata/ns/edm\"></Schema></edmx:DataServices></edmx:Edmx>";
        ODataXmlParser.ValidationResult validResult = parser.validateXmlFormat(validXml);
        
        System.out.println("有效XML验证结果: " + (validResult.isValid() ? "✓ 有效" : "✗ 无效"));
        if (!validResult.getErrors().isEmpty()) {
            validResult.getErrors().forEach(error -> System.out.println("  错误: " + error));
        }
        
        // 测试无效的XML
        String invalidXml = "这不是有效的XML";
        ODataXmlParser.ValidationResult invalidResult = parser.validateXmlFormat(invalidXml);
        
        System.out.println("无效XML验证结果: " + (invalidResult.isValid() ? "✓ 有效" : "✗ 无效"));
        if (!invalidResult.getErrors().isEmpty()) {
            invalidResult.getErrors().forEach(error -> System.out.println("  错误: " + error));
        }
    }
}
