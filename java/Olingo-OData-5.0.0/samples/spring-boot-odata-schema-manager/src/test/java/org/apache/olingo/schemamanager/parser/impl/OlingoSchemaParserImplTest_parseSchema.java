package org.apache.olingo.schemamanager.parser.impl;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.parser.ODataSchemaParser;
import org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils;
import static org.apache.olingo.schemamanager.testutil.XmlSchemaTestUtils.getTestResourcesBase;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 测试 OlingoSchemaParserImpl.parseSchema() 方法
 */
class OlingoSchemaParserImplTest_parseSchema {

    private OlingoSchemaParserImpl parser;

    @BeforeEach
    void setUp() {
        parser = new OlingoSchemaParserImpl();
        XmlSchemaTestUtils.clearCache();
    }

    @Test
    void testParseSchema_ValidSimpleSchema() throws Exception {
        // 测试解析简单有效的Schema
        try (InputStream inputStream = new FileInputStream(
                Paths.get(getTestResourcesBase(), "loader/valid/simple-schema.xml").toFile())) {
            
            ODataSchemaParser.ParseResult result = parser.parseSchema(inputStream, "simple-schema.xml");
            
            assertTrue(result.isSuccess());
            assertNotNull(result.getSchema());
            assertNull(result.getErrorMessage());
            assertEquals("TestService", result.getSchema().getNamespace());
            assertNotNull(result.getDependencies());
        }
    }

    @Test
    void testParseSchema_ValidFullSchema() throws Exception {
        // 测试解析包含完整元素的Schema
        try (InputStream inputStream = new FileInputStream(
                Paths.get(getTestResourcesBase(), "loader/valid/full-schema.xml").toFile())) {
            
            ODataSchemaParser.ParseResult result = parser.parseSchema(inputStream, "full-schema.xml");
            
            assertTrue(result.isSuccess());
            assertNotNull(result.getSchema());
            assertNull(result.getErrorMessage());
            
            CsdlSchema schema = result.getSchema();
            assertNotNull(schema.getNamespace());
            assertFalse(schema.getEntityTypes().isEmpty());
            assertNotNull(schema.getEntityContainer());
        }
    }

    @Test
    void testParseSchema_ValidComplexTypesSchema() throws Exception {
        // 测试解析包含复杂类型的Schema
        try (InputStream inputStream = new FileInputStream(
                Paths.get(getTestResourcesBase(), "loader/valid/complex-types-schema.xml").toFile())) {
            
            ODataSchemaParser.ParseResult result = parser.parseSchema(inputStream, "complex-types-schema.xml");
            
            assertTrue(result.isSuccess());
            assertNotNull(result.getSchema());
            assertNull(result.getErrorMessage());
            
            CsdlSchema schema = result.getSchema();
            assertFalse(schema.getComplexTypes().isEmpty());
        }
    }

    @Test
    void testParseSchema_ValidWithDependencies() throws Exception {
        // 测试解析包含依赖关系的Schema
        try (InputStream inputStream = new FileInputStream(
                Paths.get(getTestResourcesBase(), "loader/complex/multi-dependency-schema.xml").toFile())) {
            
            ODataSchemaParser.ParseResult result = parser.parseSchema(inputStream, "multi-dependency-schema.xml");
            
            assertTrue(result.isSuccess());
            assertNotNull(result.getSchema());
            assertNull(result.getErrorMessage());
            assertNotNull(result.getDependencies());
        }
    }

    @Test
    void testParseSchema_InvalidXml() throws Exception {
        // 测试解析无效的XML
        try (InputStream inputStream = new FileInputStream(
                Paths.get(getTestResourcesBase(), "loader/invalid/malformed-xml.xml").toFile())) {
            
            ODataSchemaParser.ParseResult result = parser.parseSchema(inputStream, "malformed-xml.xml");
            
            assertFalse(result.isSuccess());
            assertNull(result.getSchema());
            assertNotNull(result.getErrorMessage());
            assertTrue(result.getDependencies().isEmpty());
        }
    }

    @Test
    void testParseSchema_InvalidTypes() throws Exception {
        // 测试解析包含无效类型的Schema
        try (InputStream inputStream = new FileInputStream(
                Paths.get(getTestResourcesBase(), "loader/invalid/invalid-types.xml").toFile())) {
            
            ODataSchemaParser.ParseResult result = parser.parseSchema(inputStream, "invalid-types.xml");
            
            // 即使包含无效类型，如果XML格式正确，解析仍可能成功
            // 具体行为取决于解析器实现
            assertNotNull(result);
        }
    }

    @Test
    void testParseSchema_EmptyStream() throws Exception {
        // 测试空输入流
        try (InputStream inputStream = new ByteArrayInputStream(new byte[0])) {
            ODataSchemaParser.ParseResult result = parser.parseSchema(inputStream, "empty.xml");
            
            assertFalse(result.isSuccess());
            assertNull(result.getSchema());
            assertNotNull(result.getErrorMessage());
        }
    }

    @Test
    void testParseSchema_InvalidXmlContent() throws Exception {
        // 测试无效的XML内容
        String invalidXml = "<?xml version=\"1.0\"?><invalid>content</invalid>";
        try (InputStream inputStream = new ByteArrayInputStream(invalidXml.getBytes())) {
            ODataSchemaParser.ParseResult result = parser.parseSchema(inputStream, "invalid.xml");
            
            assertFalse(result.isSuccess());
            assertNull(result.getSchema());
            assertNotNull(result.getErrorMessage());
        }
    }

    @Test
    void testParseSchema_NullSourceName() throws Exception {
        // 测试null源名称
        try (InputStream inputStream = new FileInputStream(
                Paths.get(getTestResourcesBase(), "loader/valid/simple-schema.xml").toFile())) {
            
            ODataSchemaParser.ParseResult result = parser.parseSchema(inputStream, null);
            
            // 应该能正常解析，即使sourceName为null
            assertTrue(result.isSuccess());
            assertNotNull(result.getSchema());
        }
    }

    @Test
    void testParseSchema_CircularDependency() throws Exception {
        // 测试解析包含循环依赖的Schema
        try (InputStream inputStream = new FileInputStream(
                Paths.get(getTestResourcesBase(), "loader/complex/circular-dependency-schema.xml").toFile())) {
            
            ODataSchemaParser.ParseResult result = parser.parseSchema(inputStream, "circular-dependency-schema.xml");
            
            // 解析本身应该成功，循环依赖检测在后续步骤
            assertTrue(result.isSuccess());
            assertNotNull(result.getSchema());
        }
    }

    @Test
    void testParseSchema_LargeSchema() throws Exception {
        // 测试解析大型Schema（性能测试）
        try (InputStream inputStream = new FileInputStream(
                Paths.get(getTestResourcesBase(), "loader/performance/large-schema.xml").toFile())) {
            
            long startTime = System.currentTimeMillis();
            ODataSchemaParser.ParseResult result = parser.parseSchema(inputStream, "large-schema.xml");
            long endTime = System.currentTimeMillis();
            
            assertTrue(result.isSuccess());
            assertNotNull(result.getSchema());
            
            // 简单的性能断言（应该在合理时间内完成）
            assertTrue(endTime - startTime < 5000, "Parse should complete within 5 seconds");
        }
    }
}
