package org.apache.olingo.schema.processor.validation.file;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for encoding and charset errors using XmlFileComplianceValidator
 */
public class EncodingCharsetErrorsTest {

    private XmlFileComplianceValidator validator;
    private static final String ERROR_SCHEMAS_DIR = "src/test/resources/validator/file/07-encoding-charset-errors";

    @BeforeEach
    public void setUp() {
        validator = new OlingoXmlFileComplianceValidator();
    }

    @Test
    public void testIso88591WithUnicode() {
        Path testFilePath = Paths.get(ERROR_SCHEMAS_DIR, "iso-8859-1-with-unicode.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "Encoding error file should have errors: iso-8859-1-with-unicode.xml");
        boolean foundEncodingError = result.getErrors().stream().anyMatch(e ->
            e.contains("Invalid") && e.contains("Property name"));
        assertTrue(foundEncodingError, "应检测到编码相关错误: " + result.getErrors());
    }

    @Test
    public void testNoEncodingDeclaration() {
        Path testFilePath = Paths.get(ERROR_SCHEMAS_DIR, "no-encoding-declaration.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "Encoding error file should have errors: no-encoding-declaration.xml");
        boolean foundEncodingError = result.getErrors().stream().anyMatch(e ->
            e.contains("Invalid") && (e.contains("Property name") || e.contains("EntityType name")));
        assertTrue(foundEncodingError, "应检测到编码声明相关错误: " + result.getErrors());
    }

    @Test
    public void testUtf8ContentWrongDeclaration() {
        Path testFilePath = Paths.get(ERROR_SCHEMAS_DIR, "utf8-content-wrong-declaration.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "Encoding error file should have errors: utf8-content-wrong-declaration.xml");
        boolean foundEncodingError = result.getErrors().stream().anyMatch(e ->
            e.contains("Invalid") && e.contains("Property name"));
        assertTrue(foundEncodingError, "应检测到UTF-8编码相关错误: " + result.getErrors());
    }

    @Test
    public void testUtf8WithBom() {
        Path testFilePath = Paths.get(ERROR_SCHEMAS_DIR, "utf8-with-bom.xml");
        File xmlFile = testFilePath.toFile();
        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
        XmlComplianceResult result = validator.validateFile(xmlFile);
        assertNotNull(result, "Result should not be null");
        assertTrue(result.hasErrors(), "Encoding error file should have errors: utf8-with-bom.xml");
        boolean foundBomError = result.getErrors().stream().anyMatch(e ->
            e.contains("前言中不允许有内容") || e.contains("ParseError") || e.contains("BOM"));
        assertTrue(foundBomError, "应检测到BOM相关错误: " + result.getErrors());
    }
}
