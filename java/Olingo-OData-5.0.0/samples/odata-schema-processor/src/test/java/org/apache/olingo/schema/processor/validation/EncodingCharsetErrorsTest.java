package org.apache.olingo.schema.processor.validation;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for encoding and charset errors using XmlFileComplianceValidator
 */
public class EncodingCharsetErrorsTest {

    private XmlFileComplianceValidator validator;
    private static final String ENCODING_CHARSET_ERRORS_DIR = "src/test/resources/validator/07-encoding-charset-errors";

    @BeforeEach
    public void setUp() {
        validator = new OlingoXmlFileComplianceValidator();
    }

    @Test
    public void testUtf8EncodingError() {
        Path testFilePath = Paths.get(ENCODING_CHARSET_ERRORS_DIR, "utf8-encoding-error.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isCompliant(), "Encoding charset error file should not be compliant: utf8-encoding-error.xml");
        assertTrue(result.hasErrors(), "Encoding charset error file should have errors: utf8-encoding-error.xml");
        boolean foundEncodingError = result.getErrors().stream().anyMatch(e -> e.toLowerCase().contains("utf-8") || e.toLowerCase().contains("encoding") || e.contains("编码错误"));
        assertTrue(foundEncodingError, "应检测到UTF-8编码相关错误: " + result.getErrors());
    }

    @Test
    public void testUtf16EncodingError() {
        Path testFilePath = Paths.get(ENCODING_CHARSET_ERRORS_DIR, "utf16-encoding-error.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isCompliant(), "Encoding charset error file should not be compliant: utf16-encoding-error.xml");
        assertTrue(result.hasErrors(), "Encoding charset error file should have errors: utf16-encoding-error.xml");
        boolean foundEncodingError = result.getErrors().stream().anyMatch(e -> e.toLowerCase().contains("utf-16") || e.toLowerCase().contains("encoding") || e.contains("编码错误"));
        assertTrue(foundEncodingError, "应检测到UTF-16编码相关错误: " + result.getErrors());
    }

    @Test
    public void testInvalidCharacterEncoding() {
        Path testFilePath = Paths.get(ENCODING_CHARSET_ERRORS_DIR, "invalid-character-encoding.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isCompliant(), "Encoding charset error file should not be compliant: invalid-character-encoding.xml");
        assertTrue(result.hasErrors(), "Encoding charset error file should have errors: invalid-character-encoding.xml");
        boolean foundEncodingError = result.getErrors().stream().anyMatch(e -> e.toLowerCase().contains("invalid") || e.toLowerCase().contains("encoding") || e.contains("编码错误"));
        assertTrue(foundEncodingError, "应检测到无效字符编码相关错误: " + result.getErrors());
    }

    @Test
    public void testBomMismatch() {
        Path testFilePath = Paths.get(ENCODING_CHARSET_ERRORS_DIR, "bom-mismatch.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isCompliant(), "Encoding charset error file should not be compliant: bom-mismatch.xml");
        assertTrue(result.hasErrors(), "Encoding charset error file should have errors: bom-mismatch.xml");
        boolean foundBomError = result.getErrors().stream().anyMatch(e -> e.toLowerCase().contains("bom") || e.contains("字节顺序标记") || e.contains("byte order mark"));
        assertTrue(foundBomError, "应检测到BOM相关错误: " + result.getErrors());
    }

    @Test
    public void testIso88591WithUnicode() {
        Path testFilePath = Paths.get(ENCODING_CHARSET_ERRORS_DIR, "iso-8859-1-with-unicode.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isCompliant(), "Encoding charset error file should not be compliant: iso-8859-1-with-unicode.xml");
        assertTrue(result.hasErrors(), "Encoding charset error file should have errors: iso-8859-1-with-unicode.xml");
        boolean foundEncodingError = result.getErrors().stream().anyMatch(e -> e.toLowerCase().contains("encoding") || e.contains("编码错误") || e.contains("unicode"));
        assertTrue(foundEncodingError, "应检测到编码相关错误: " + result.getErrors());
    }

    @Test
    public void testNoEncodingDeclaration() {
        Path testFilePath = Paths.get(ENCODING_CHARSET_ERRORS_DIR, "no-encoding-declaration.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isCompliant(), "Encoding charset error file should not be compliant: no-encoding-declaration.xml");
        assertTrue(result.hasErrors(), "Encoding charset error file should have errors: no-encoding-declaration.xml");
        boolean foundEncodingError = result.getErrors().stream().anyMatch(e -> e.toLowerCase().contains("encoding") || e.contains("编码声明") || e.contains("declaration"));
        assertTrue(foundEncodingError, "应检测到编码声明相关错误: " + result.getErrors());
    }

    @Test
    public void testUtf8ContentWrongDeclaration() {
        Path testFilePath = Paths.get(ENCODING_CHARSET_ERRORS_DIR, "utf8-content-wrong-declaration.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isCompliant(), "Encoding charset error file should not be compliant: utf8-content-wrong-declaration.xml");
        assertTrue(result.hasErrors(), "Encoding charset error file should have errors: utf8-content-wrong-declaration.xml");
        boolean foundEncodingError = result.getErrors().stream().anyMatch(e -> e.toLowerCase().contains("utf-8") || e.toLowerCase().contains("encoding") || e.contains("编码错误"));
        assertTrue(foundEncodingError, "应检测到UTF-8编码相关错误: " + result.getErrors());
    }

    @Test
    public void testUtf8WithBom() {
        Path testFilePath = Paths.get(ENCODING_CHARSET_ERRORS_DIR, "utf8-with-bom.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isCompliant(), "Encoding charset error file should not be compliant: utf8-with-bom.xml");
        assertTrue(result.hasErrors(), "Encoding charset error file should have errors: utf8-with-bom.xml");
        boolean foundBomError = result.getErrors().stream().anyMatch(e -> e.toLowerCase().contains("bom") || e.contains("字节顺序标记") || e.contains("byte order mark"));
        assertTrue(foundBomError, "应检测到BOM相关错误: " + result.getErrors());
    }
}
