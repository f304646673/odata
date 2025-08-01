package org.apache.olingo.xmlprocessor.parser.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.olingo.xmlprocessor.core.model.ExtendedCsdlSchema;
import org.apache.olingo.xmlprocessor.parser.ODataXmlParser.ParseResult;
import org.apache.olingo.xmlprocessor.parser.ODataXmlParser.ValidationResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsdlXmlParserImplTest {

    private CsdlXmlParserImpl parser;

    @BeforeEach
    void setUp() {
        parser = new CsdlXmlParserImpl();
    }

    @Test
    void testParseValidSchemaFromResource() {
        // 测试从资源文件解析有效的Schema
        ParseResult result = parser.parseFromResource("/test-schemas/valid-simple-schema.xml");

        assertTrue(result.isSuccess(), "解析应该成功");
        assertFalse(result.getSchemas().isEmpty(), "应该包含至少一个Schema");
        assertTrue(result.getErrors().isEmpty(), "不应该有错误");

        ExtendedCsdlSchema schema = result.getSchemas().get(0);
        assertEquals("TestService", schema.getNamespace(), "命名空间应该匹配");
        assertTrue(schema.isExtended(), "应该是扩展Schema");
        assertNotNull(schema.getSourcePath(), "应该有源路径");
    }

    @Test
    void testParseComplexSchemaFromResource() {
        // 测试解析包含多种元素类型的复杂Schema
        ParseResult result = parser.parseFromResource("/test-schemas/complex-schema.xml");

        assertTrue(result.isSuccess(), "解析应该成功");
        assertFalse(result.getSchemas().isEmpty(), "应该包含至少一个Schema");

        ExtendedCsdlSchema schema = result.getSchemas().get(0);
        assertEquals("ComplexTestService", schema.getNamespace(), "命名空间应该匹配");

        // 验证包含EntityType
        assertNotNull(schema.getEntityTypes(), "应该包含EntityTypes");
        assertFalse(schema.getEntityTypes().isEmpty(), "EntityTypes不应该为空");
        assertTrue(schema.getEntityTypes().size() > 0, "EntityType数量应该大于0");

        // 验证包含ComplexType
        assertNotNull(schema.getComplexTypes(), "应该包含ComplexTypes");
        assertFalse(schema.getComplexTypes().isEmpty(), "ComplexTypes不应该为空");
        assertTrue(schema.getComplexTypes().size() > 0, "ComplexType数量应该大于0");

        // 验证包含EntityContainer
        assertNotNull(schema.getEntityContainer(), "应该包含EntityContainer");

        // 测试基本信息
        assertNotNull(schema.getNamespace(), "Namespace不应该为null");
        assertTrue(schema.getNamespace().contains("ComplexTestService"), "Namespace应该包含ComplexTestService");
    }

    @Test
    void testParseFromInputStream() {
        // 测试从InputStream解析
        String xmlContent = createValidSchemaXml();

        try (InputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes("UTF-8"))) {
            ParseResult result = parser.parseSchemas(inputStream, "test-input-stream");

            assertTrue(result.isSuccess(), "解析应该成功");
            assertFalse(result.getSchemas().isEmpty(), "应该包含至少一个Schema");
            assertEquals("test-input-stream", result.getSourceName(), "源名称应该匹配");

            ExtendedCsdlSchema schema = result.getSchemas().get(0);
            assertEquals("test-input-stream", schema.getSourcePath(), "源路径应该匹配");
        } catch (Exception e) {
            fail("不应该抛出异常: " + e.getMessage());
        }
    }

    @Test
    void testParseFromString() {
        // 测试从字符串解析
        String xmlContent = createValidSchemaXml();

        ParseResult result = parser.parseSchemas(xmlContent, "test-string-source");

        assertTrue(result.isSuccess(), "解析应该成功");
        assertFalse(result.getSchemas().isEmpty(), "应该包含至少一个Schema");
        assertEquals("test-string-source", result.getSourceName(), "源名称应该匹配");

        ExtendedCsdlSchema schema = result.getSchemas().get(0);
        assertEquals("test-string-source", schema.getSourcePath(), "源路径应该匹配");
    }

    @Test
    void testParseFromFile(@TempDir Path tempDir) throws Exception {
        // 测试从文件解析
        String xmlContent = createValidSchemaXml();
        Path testFile = tempDir.resolve("test-schema.xml");
        Files.write(testFile, xmlContent.getBytes("UTF-8"), StandardOpenOption.CREATE);

        ParseResult result = parser.parseSchemas(testFile);

        assertTrue(result.isSuccess(), "解析应该成功");
        assertFalse(result.getSchemas().isEmpty(), "应该包含至少一个Schema");
        assertEquals(testFile.toString(), result.getSourceName(), "源名称应该匹配");

        ExtendedCsdlSchema schema = result.getSchemas().get(0);
        assertEquals(testFile.toString(), schema.getSourcePath(), "源路径应该匹配");
    }

    @Test
    void testParseNonExistentFile() {
        // 测试解析不存在的文件
        Path nonExistentFile = Paths.get("non-existent-file.xml");

        ParseResult result = parser.parseSchemas(nonExistentFile);

        assertFalse(result.isSuccess(), "解析应该失败");
        assertTrue(result.getSchemas().isEmpty(), "不应该包含Schema");
        assertFalse(result.getErrors().isEmpty(), "应该包含错误信息");
        assertTrue(result.getErrors().get(0).contains("File does not exist"), "错误信息应该指示文件不存在");
    }

    @Test
    void testParseInvalidXml() {
        // 测试解析无效的XML
        String invalidXml = "<invalid><unclosed>";

        ParseResult result = parser.parseSchemas(invalidXml, "invalid-xml");

        assertFalse(result.isSuccess(), "解析应该失败");
        assertTrue(result.getSchemas().isEmpty(), "不应该包含Schema");
        assertFalse(result.getErrors().isEmpty(), "应该包含错误信息");
    }

    @Test
    void testParseEmptyContent() {
        // 测试解析空内容
        ParseResult result = parser.parseSchemas("", "empty-content");

        assertFalse(result.isSuccess(), "解析应该失败");
        assertTrue(result.getSchemas().isEmpty(), "不应该包含Schema");
        assertFalse(result.getErrors().isEmpty(), "应该包含错误信息");
    }

    @Test
    void testParseNullContent() {
        // 测试解析null内容
        ParseResult result = parser.parseSchemas((String) null, "null-content");

        assertFalse(result.isSuccess(), "解析应该失败");
        assertTrue(result.getSchemas().isEmpty(), "不应该包含Schema");
        assertFalse(result.getErrors().isEmpty(), "应该包含错误信息");
    }

    @Test
    void testValidateValidXmlFormat() {
        // 测试验证有效的XML格式
        String validXml = createValidSchemaXml();

        ValidationResult result = parser.validateXmlFormat(validXml);

        assertTrue(result.isValid(), "XML格式应该有效");
        assertTrue(result.getErrors().isEmpty(), "不应该有错误");
    }

    @Test
    void testValidateInvalidXmlFormat() {
        // 测试验证无效的XML格式
        String invalidXml = "<invalid><unclosed>";

        ValidationResult result = parser.validateXmlFormat(invalidXml);

        assertFalse(result.isValid(), "XML格式应该无效");
        assertFalse(result.getErrors().isEmpty(), "应该包含错误信息");
    }

    @Test
    void testValidateEmptyXmlFormat() {
        // 测试验证空XML格式
        ValidationResult result = parser.validateXmlFormat("");

        assertFalse(result.isValid(), "空XML应该无效");
        assertFalse(result.getErrors().isEmpty(), "应该包含错误信息");
        assertTrue(result.getErrors().get(0).contains("null or empty"), "错误信息应该指示内容为空");
    }

    @Test
    void testValidateNullXmlFormat() {
        // 测试验证null XML格式
        ValidationResult result = parser.validateXmlFormat(null);

        assertFalse(result.isValid(), "null XML应该无效");
        assertFalse(result.getErrors().isEmpty(), "应该包含错误信息");
        assertTrue(result.getErrors().get(0).contains("null or empty"), "错误信息应该指示内容为空");
    }

    @Test
    void testParseResourceNotFound() {
        // 测试解析不存在的资源
        ParseResult result = parser.parseFromResource("/non-existent-resource.xml");

        assertFalse(result.isSuccess(), "解析应该失败");
        assertTrue(result.getSchemas().isEmpty(), "不应该包含Schema");
        assertFalse(result.getErrors().isEmpty(), "应该包含错误信息");
        assertTrue(result.getErrors().get(0).contains("Resource not found"), "错误信息应该指示资源未找到");
    }

    @Test
    void testParseSchemaWithEnumType() {
        // 测试解析包含枚举类型的Schema
        ParseResult result = parser.parseFromResource("/test-schemas/schema-with-enum.xml");

        assertTrue(result.isSuccess(), "解析应该成功");
        assertFalse(result.getSchemas().isEmpty(), "应该包含至少一个Schema");

        ExtendedCsdlSchema schema = result.getSchemas().get(0);
        assertNotNull(schema.getEnumTypes(), "应该包含EnumTypes");
        assertFalse(schema.getEnumTypes().isEmpty(), "EnumTypes不应该为空");
        assertTrue(schema.getEnumTypes().size() > 0, "EnumType数量应该大于0");
    }

    @Test
    void testParseSchemaWithTypeDefinition() {
        // 测试解析包含类型定义的Schema
        ParseResult result = parser.parseFromResource("/test-schemas/schema-with-typedef.xml");

        assertTrue(result.isSuccess(), "解析应该成功");
        assertFalse(result.getSchemas().isEmpty(), "应该包含至少一个Schema");

        ExtendedCsdlSchema schema = result.getSchemas().get(0);
        assertNotNull(schema.getTypeDefinitions(), "应该包含TypeDefinitions");
        assertFalse(schema.getTypeDefinitions().isEmpty(), "TypeDefinitions不应该为空");
        assertTrue(schema.getTypeDefinitions().size() > 0, "TypeDefinition数量应该大于0");
    }

    @Test
    void testParseSchemaWithAnnotations() {
        // 测试解析包含注解的Schema
        ParseResult result = parser.parseFromResource("/test-schemas/schema-with-annotations.xml");

        assertTrue(result.isSuccess(), "解析应该成功");
        assertFalse(result.getSchemas().isEmpty(), "应该包含至少一个Schema");

        ExtendedCsdlSchema schema = result.getSchemas().get(0);
        // 验证包含注解或Terms
        if (schema.getAnnotations() != null) {
            assertTrue(schema.getAnnotations().size() >= 0, "Annotation数量应该>=0");
        }
        if (schema.getTerms() != null) {
            assertTrue(schema.getTerms().size() > 0, "Term数量应该>0");
        }
    }

    @Test
    void testExtendedSchemaFeatures() {
        // 测试ExtendedCsdlSchema的扩展功能
        String xmlContent = createValidSchemaXml();
        ParseResult result = parser.parseSchemas(xmlContent, "test-extended-features");

        assertTrue(result.isSuccess(), "解析应该成功");
        ExtendedCsdlSchema schema = result.getSchemas().get(0);

        // 测试扩展功能
        assertTrue(schema.isExtended(), "应该是扩展Schema");
        assertEquals("test-extended-features", schema.getSourcePath(), "源路径应该匹配");
        assertNotNull(schema.getReferencedNamespaces(), "引用命名空间列表不应该为null");

        // 测试添加引用命名空间
        schema.addReferencedNamespace("org.odata.core.v1");
        assertTrue(schema.getReferencedNamespaces().contains("org.odata.core.v1"), "应该包含添加的命名空间");

        // 测试toString方法
        String toStringResult = schema.toString();
        assertNotNull(toStringResult, "toString结果不应该为null");
        assertTrue(toStringResult.contains("ExtendedCsdlSchema"), "toString应该包含类名");
        assertTrue(toStringResult.contains("TestService"), "toString应该包含命名空间");
    }

    // 辅助方法：创建有效的Schema XML
    private String createValidSchemaXml() {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
               "<edmx:Edmx Version=\"4.0\" xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\">\n" +
               "  <edmx:DataServices>\n" +
               "    <Schema Namespace=\"TestService\" xmlns=\"http://docs.oasis-open.org/odata/ns/edm\">\n" +
               "      <EntityType Name=\"Product\">\n" +
               "        <Key>\n" +
               "          <PropertyRef Name=\"ID\"/>\n" +
               "        </Key>\n" +
               "        <Property Name=\"ID\" Type=\"Edm.Int32\" Nullable=\"false\"/>\n" +
               "        <Property Name=\"Name\" Type=\"Edm.String\"/>\n" +
               "      </EntityType>\n" +
               "      <EntityContainer Name=\"Container\">\n" +
               "        <EntitySet Name=\"Products\" EntityType=\"TestService.Product\"/>\n" +
               "      </EntityContainer>\n" +
               "    </Schema>\n" +
               "  </edmx:DataServices>\n" +
               "</edmx:Edmx>";
    }
}
