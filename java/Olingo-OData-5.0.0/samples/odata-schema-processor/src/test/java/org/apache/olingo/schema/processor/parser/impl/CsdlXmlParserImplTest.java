//package org.apache.olingo.schema.processor.parser.impl;
//
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//import org.apache.olingo.schema.processor.parser.ODataXmlParser;
//import org.apache.olingo.schema.processor.test.util.TestResourceLoader;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
///**
// * CsdlXmlParserImpl测试
// */
//public class CsdlXmlParserImplTest {
//
//    private CsdlXmlParserImpl parser;
//
//    @BeforeEach
//    public void setUp() {
//        parser = new CsdlXmlParserImpl();
//    }
//
//    /**
//     * 测试parseSchemas(Path filePath)方法 - 有效路径
//     */
//    @Test
//    public void testParseFromPathValid() throws Exception {
//        Path validPath = Paths.get(getClass().getResource("/test-xml/valid-multiple-types.xml").toURI());
//
//        ODataXmlParser.ParseResult result = parser.parseSchemas(validPath);
//
//        assertNotNull(result);
//        assertTrue("Parse should succeed", result.isSuccess());
//        assertNotNull("Schemas should not be null", result.getSchemas());
//        assertEquals("Should have one schema", 1, result.getSchemas().size());
//    }
//
//    /**
//     * 测试parseSchemas(Path filePath)方法 - 不存在的文件
//     */
//    @Test
//    public void testParseFromPathNonExistent() throws Exception {
//        Path nonExistentPath = Paths.get("non-existent-file.xml");
//
//        ODataXmlParser.ParseResult result = parser.parseSchemas(nonExistentPath);
//
//        assertNotNull(result);
//        assertFalse("Parse should fail", result.isSuccess());
//        assertNotNull("Errors should not be null", result.getErrors());
//        assertFalse("Should have errors", result.getErrors().isEmpty());
//        assertTrue("Error should mention file not exists",
//                   result.getErrors().get(0).contains("File does not exist"));
//    }
//
//    /**
//     * 测试parseSchemas(String xmlContent, String sourceName)方法的异常处
//     */
//    @Test
//    public void testParseXmlContentWithException() throws Exception {
//        // 模拟IO异常 - 使用null内容会触发异
//        ODataXmlParser.ParseResult result = parser.parseSchemas((String)null, "null-test");
//
//        assertNotNull(result);
//        assertFalse("Parse should fail", result.isSuccess());
//        assertNotNull("Errors should not be null", result.getErrors());
//        assertFalse("Should have errors", result.getErrors().isEmpty());
//        assertTrue("Error should mention parsing failure",
//                   result.getErrors().get(0).contains("Failed to parse XML content"));
//    }
//
//    /**
//     * 测试validateXmlFormat方法的所有路径
//     */
//    @Test
//    public void testValidateXmlFormatAllPaths() throws Exception {
//        // 测试null输入
//        ODataXmlParser.ValidationResult result = parser.validateXmlFormat(null);
//        assertFalse("Validation should fail for null", result.isValid());
//        assertTrue("Should contain null error",
//                   result.getErrors().get(0).contains("null"));
//
//        // 测试空字符串
//        result = parser.validateXmlFormat("");
//        assertFalse("Validation should fail for empty", result.isValid());
//        assertTrue("Should contain empty error",
//                   result.getErrors().get(0).contains("empty"));
//
//        // 测试只有空白字符
//        result = parser.validateXmlFormat("   \n\t   ");
//        assertFalse("Validation should fail for whitespace", result.isValid());
//        assertTrue("Should contain empty after trim error",
//                   result.getErrors().get(0).contains("empty"));
//
//        // 测试非XML格式
//        result = parser.validateXmlFormat("not xml content");
//        assertFalse("Validation should fail for non-XML", result.isValid());
//        assertFalse("Should have errors", result.getErrors().isEmpty());
//        // 只检查是否有错误，不检查具体错误消息内
//
//        // 测试有效的XML
//        String validXml = TestResourceLoader.loadXmlContent("valid-multiple-types.xml");
//        result = parser.validateXmlFormat(validXml);
//        assertTrue("Validation should succeed for valid XML", result.isValid());
//        assertTrue("Should have no errors", result.getErrors().isEmpty());
//    }
//}
