package org.apache.olingo.xmlprocessor.parser.impl;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.xmlprocessor.parser.ODataXmlParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CsdlXmlParserImpl单元测试
 */
class CsdlXmlParserImplTest {
    
    private CsdlXmlParserImpl parser;
    
    @BeforeEach
    void setUp() {
        parser = new CsdlXmlParserImpl();
    }
    
    @Test
    @DisplayName("应该成功解析有效的OData XML")
    void shouldParseValidODataXml() {
        // Given
        String validXml = createValidODataXml();
        
        // When
        ODataXmlParser.ParseResult result = parser.parseSchemas(validXml, "test-source");
        
        // Then
        assertTrue(result.isSuccess(), "解析应该成功");
        assertNotNull(result.getSchemas(), "Schemas不应为null");
        assertTrue(result.getErrors().isEmpty(), "不应有错误");
    }
    
    @Test
    @DisplayName("应该处理空的XML内容")
    void shouldHandleEmptyXmlContent() {
        // Given
        String emptyXml = "";
        
        // When
        ODataXmlParser.ParseResult result = parser.parseSchemas(emptyXml, "empty-source");
        
        // Then
        assertFalse(result.isSuccess(), "解析应该失败");
        assertFalse(result.getErrors().isEmpty(), "应该有错误信息");
    }
    
    @Test
    @DisplayName("应该处理null的XML内容")
    void shouldHandleNullXmlContent() {
        // When & Then
        assertThrows(Exception.class, () -> {
            parser.parseSchemas((String) null, "null-source");
        }, "应该抛出异常");
    }
    
    @Test
    @DisplayName("应该处理无效的XML格式")
    void shouldHandleInvalidXmlFormat() {
        // Given
        String invalidXml = "这不是有效的XML";
        
        // When
        ODataXmlParser.ParseResult result = parser.parseSchemas(invalidXml, "invalid-source");
        
        // Then
        assertFalse(result.isSuccess(), "解析应该失败");
        assertFalse(result.getErrors().isEmpty(), "应该有错误信息");
    }
    
    @Test
    @DisplayName("应该验证有效的XML格式")
    void shouldValidateValidXmlFormat() {
        // Given
        String validXml = createValidODataXml();
        
        // When
        ODataXmlParser.ValidationResult result = parser.validateXmlFormat(validXml);
        
        // Then
        assertTrue(result.isValid(), "XML应该有效");
        assertTrue(result.getErrors().isEmpty(), "不应有错误");
    }
    
    @Test
    @DisplayName("应该验证无效的XML格式")
    void shouldValidateInvalidXmlFormat() {
        // Given
        String invalidXml = "无效的XML内容";
        
        // When
        ODataXmlParser.ValidationResult result = parser.validateXmlFormat(invalidXml);
        
        // Then
        assertFalse(result.isValid(), "XML应该无效");
        assertFalse(result.getErrors().isEmpty(), "应该有错误信息");
    }
    
    @Test
    @DisplayName("应该处理缺少XML声明的情况")
    void shouldHandleMissingXmlDeclaration() {
        // Given
        String xmlWithoutDeclaration = "<edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">" +
                "<edmx:DataServices>" +
                "<Schema Namespace=\"Test\" xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">" +
                "</Schema>" +
                "</edmx:DataServices>" +
                "</edmx:Edmx>";
        
        // When
        ODataXmlParser.ValidationResult result = parser.validateXmlFormat(xmlWithoutDeclaration);
        
        // Then
        assertFalse(result.getWarnings().isEmpty(), "应该有警告信息");
        assertTrue(result.getWarnings().stream()
                .anyMatch(warning -> warning.contains("Missing XML declaration")), 
                "应该有缺少XML声明的警告");
    }
    
    @Test
    @DisplayName("应该从resources目录解析XML文件")
    void shouldParseXmlFromResource() {
        // Given
        String resourcePath = "/test-schemas/basic-schema.xml";
        
        // When
        ODataXmlParser.ParseResult result = parser.parseFromResource(resourcePath);
        
        // Then
        assertTrue(result.isSuccess(), "解析应该成功");
        assertNotNull(result.getSchemas(), "Schemas不应为null");
        assertFalse(result.getSchemas().isEmpty(), "应该有至少一个schema");
    }
    
    @Test
    @DisplayName("应该处理不存在的资源文件")
    void shouldHandleNonExistentResource() {
        // Given
        String nonExistentPath = "/non-existent/file.xml";
        
        // When
        ODataXmlParser.ParseResult result = parser.parseFromResource(nonExistentPath);
        
        // Then
        assertFalse(result.isSuccess(), "解析应该失败");
        assertFalse(result.getErrors().isEmpty(), "应该有错误信息");
        assertTrue(result.getErrors().stream()
                .anyMatch(error -> error.contains("Resource not found")), 
                "应该有资源未找到的错误");
    }
    
    @Test
    @DisplayName("应该解析包含EntityType的schema")
    void shouldParseSchemaWithEntityTypes() {
        // Given
        String xmlWithEntityType = createXmlWithEntityType();
        
        // When
        ODataXmlParser.ParseResult result = parser.parseSchemas(xmlWithEntityType, "entity-type-source");
        
        // Then
        assertTrue(result.isSuccess(), "解析应该成功");
        assertFalse(result.getSchemas().isEmpty(), "应该有schemas");
        
        CsdlSchema schema = result.getSchemas().get(0);
        assertNotNull(schema.getEntityTypes(), "应该有EntityTypes");
        assertFalse(schema.getEntityTypes().isEmpty(), "EntityTypes不应为空");
    }
    
    @Test
    @DisplayName("应该解析包含ComplexType的schema")
    void shouldParseSchemaWithComplexTypes() {
        // Given
        String xmlWithComplexType = createXmlWithComplexType();
        
        // When
        ODataXmlParser.ParseResult result = parser.parseSchemas(xmlWithComplexType, "complex-type-source");
        
        // Then
        assertTrue(result.isSuccess(), "解析应该成功");
        assertFalse(result.getSchemas().isEmpty(), "应该有schemas");
        
        CsdlSchema schema = result.getSchemas().get(0);
        assertNotNull(schema.getComplexTypes(), "应该有ComplexTypes");
        assertFalse(schema.getComplexTypes().isEmpty(), "ComplexTypes不应为空");
    }
    
    @Test
    @DisplayName("应该解析包含EnumType的schema")
    void shouldParseSchemaWithEnumTypes() {
        // Given
        String xmlWithEnumType = createXmlWithEnumType();
        
        // When
        ODataXmlParser.ParseResult result = parser.parseSchemas(xmlWithEnumType, "enum-type-source");
        
        // Then
        assertTrue(result.isSuccess(), "解析应该成功");
        assertFalse(result.getSchemas().isEmpty(), "应该有schemas");
        
        CsdlSchema schema = result.getSchemas().get(0);
        assertNotNull(schema.getEnumTypes(), "应该有EnumTypes");
        assertFalse(schema.getEnumTypes().isEmpty(), "EnumTypes不应为空");
    }
    
    /**
     * 创建有效的OData XML
     */
    private String createValidODataXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">" +
                "<edmx:DataServices>" +
                "<Schema Namespace=\"TestService.Models\" xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">" +
                "</Schema>" +
                "</edmx:DataServices>" +
                "</edmx:Edmx>";
    }
    
    /**
     * 创建包含EntityType的XML
     */
    private String createXmlWithEntityType() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">" +
                "<edmx:DataServices>" +
                "<Schema Namespace=\"TestService.Models\" xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">" +
                "<EntityType Name=\"TestEntity\">" +
                "<Key><PropertyRef Name=\"ID\"/></Key>" +
                "<Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>" +
                "<Property Name=\"Name\" Type=\"Edm.String\"/>" +
                "</EntityType>" +
                "</Schema>" +
                "</edmx:DataServices>" +
                "</edmx:Edmx>";
    }
    
    /**
     * 创建包含ComplexType的XML
     */
    private String createXmlWithComplexType() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">" +
                "<edmx:DataServices>" +
                "<Schema Namespace=\"TestService.Models\" xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">" +
                "<ComplexType Name=\"TestComplex\">" +
                "<Property Name=\"Street\" Type=\"Edm.String\"/>" +
                "<Property Name=\"City\" Type=\"Edm.String\"/>" +
                "</ComplexType>" +
                "</Schema>" +
                "</edmx:DataServices>" +
                "</edmx:Edmx>";
    }
    
    /**
     * 创建包含EnumType的XML
     */
    private String createXmlWithEnumType() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">" +
                "<edmx:DataServices>" +
                "<Schema Namespace=\"TestService.Models\" xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">" +
                "<EnumType Name=\"TestEnum\">" +
                "<Member Name=\"Value1\" Value=\"0\"/>" +
                "<Member Name=\"Value2\" Value=\"1\"/>" +
                "</EnumType>" +
                "</Schema>" +
                "</edmx:DataServices>" +
                "</edmx:Edmx>";
    }
}
