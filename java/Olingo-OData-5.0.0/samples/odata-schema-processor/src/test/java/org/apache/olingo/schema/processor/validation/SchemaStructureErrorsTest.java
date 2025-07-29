//package org.apache.olingo.schema.processor.validation;
//
//import java.io.File;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
///**
// * Test class for schema structure errors using XmlFileComplianceValidator
// */
//public class SchemaStructureErrorsTest {
//
//    private XmlFileComplianceValidator validator;
//    private static final String SCHEMA_STRUCTURE_ERRORS_DIR = "src/test/resources/validator/02-schema-structure-errors";
//
//    @BeforeEach
//    public void setUp() {
//        validator = new OlingoXmlFileComplianceValidator();
//    }
//
//    @Test
//    public void testConflictingNamespace1() {
//        Path testFilePath = Paths.get(SCHEMA_STRUCTURE_ERRORS_DIR, "conflicting-namespace/conflicting-namespace-1.xml");
//        File xmlFile = testFilePath.toFile();
//        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
//        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
//        XmlComplianceResult result = validator.validateFile(xmlFile);
//        assertNotNull(result, "Result should not be null");
//        assertFalse(result.isCompliant(), "Schema structure error file should not be compliant: conflicting-namespace-1.xml");
//        assertTrue(result.hasErrors(), "Schema structure error file should have errors: conflicting-namespace-1.xml");
//        boolean foundNamespaceError = result.getErrors().stream().anyMatch(e -> e.contains("Conflicting EntityType name") || e.contains("defined in 2 locations"));
//        assertTrue(foundNamespaceError, "应检测到命名空间冲突相关错误: " + result.getErrors());
//    }
//
//    @Test
//    public void testConflictingNamespace2() {
//        Path testFilePath = Paths.get(SCHEMA_STRUCTURE_ERRORS_DIR, "conflicting-namespace/conflicting-namespace-2.xml");
//        File xmlFile = testFilePath.toFile();
//        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
//        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
//        XmlComplianceResult result = validator.validateFile(xmlFile);
//        assertNotNull(result, "Result should not be null");
//        assertFalse(result.isCompliant(), "Schema structure error file should not be compliant: conflicting-namespace-2.xml");
//        assertTrue(result.hasErrors(), "Schema structure error file should have errors: conflicting-namespace-2.xml");
//        boolean foundNamespaceError = result.getErrors().stream().anyMatch(e -> e.contains("Conflicting EntityType name") || e.contains("defined in 2 locations"));
//        assertTrue(foundNamespaceError, "应检测到命名空间冲突相关错误: " + result.getErrors());
//    }
//
//    @Test
//    public void testInvalidNamespaceFormat() {
//        Path testFilePath = Paths.get(SCHEMA_STRUCTURE_ERRORS_DIR, "invalid-namespace-format.xml");
//        File xmlFile = testFilePath.toFile();
//        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
//        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
//        XmlComplianceResult result = validator.validateFile(xmlFile);
//        assertNotNull(result, "Result should not be null");
//        assertFalse(result.isCompliant(), "Schema structure error file should not be compliant: invalid-namespace-format.xml");
//        assertTrue(result.hasErrors(), "Schema structure error file should have errors: invalid-namespace-format.xml");
//        boolean foundInvalidNamespace = result.getErrors().stream().anyMatch(e -> e.contains("invalid namespace") || e.contains("命名空间格式") || e.contains("not a valid URI") || e.contains("Invalid namespace format"));
//        assertTrue(foundInvalidNamespace, "应检测到命名空间格式相关错误: " + result.getErrors());
//    }
//
//    @Test
//    public void testMissingEdmxRoot() {
//        Path testFilePath = Paths.get(SCHEMA_STRUCTURE_ERRORS_DIR, "missing-edmx-root.xml");
//        File xmlFile = testFilePath.toFile();
//        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
//        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
//        XmlComplianceResult result = validator.validateFile(xmlFile);
//        assertNotNull(result, "Result should not be null");
//        assertFalse(result.isCompliant(), "Schema structure error file should not be compliant: missing-edmx-root.xml");
//        assertTrue(result.hasErrors(), "Schema structure error file should have errors: missing-edmx-root.xml");
//        boolean foundEdmxRootError = result.getErrors().stream().anyMatch(e -> e.contains("edmx") || e.contains("root element") || e.contains("缺少 Edmx") || e.contains("缺少根元素") || e.contains("Failed to read complete metadata file") || e.contains("Failed at Schema"));
//        assertTrue(foundEdmxRootError, "应检测到缺少Edmx根元素相关错误: " + result.getErrors());
//    }
//
//    @Test
//    public void testMissingNamespace() {
//        Path testFilePath = Paths.get(SCHEMA_STRUCTURE_ERRORS_DIR, "missing-namespace.xml");
//        File xmlFile = testFilePath.toFile();
//        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
//        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
//        XmlComplianceResult result = validator.validateFile(xmlFile);
//        assertNotNull(result, "Result should not be null");
//        assertFalse(result.isCompliant(), "Schema structure error file should not be compliant: missing-namespace.xml");
//        assertTrue(result.hasErrors(), "Schema structure error file should have errors: missing-namespace.xml");
//        boolean foundMissingNamespace = result.getErrors().stream().anyMatch(e -> e.contains("missing namespace") || e.contains("缺少命名空间") || e.contains("Namespace attribute is required") || e.contains("Schema must have a valid namespace"));
//        assertTrue(foundMissingNamespace, "应检测到缺少命名空间相关错误: " + result.getErrors());
//    }
//
//    @Test
//    public void testMissingSchemaElement() {
//        Path testFilePath = Paths.get(SCHEMA_STRUCTURE_ERRORS_DIR, "missing-schema-element.xml");
//        File xmlFile = testFilePath.toFile();
//        assertTrue(xmlFile.exists(), "Test file should exist: " + testFilePath);
//        assertTrue(xmlFile.length() > 0, "Test file should not be empty: " + testFilePath);
//        XmlComplianceResult result = validator.validateFile(xmlFile);
//        assertNotNull(result, "Result should not be null");
//        assertFalse(result.isCompliant(), "Schema structure error file should not be compliant: missing-schema-element.xml");
//        assertTrue(result.hasErrors(), "Schema structure error file should have errors: missing-schema-element.xml");
//        boolean foundMissingSchema = result.getErrors().stream().anyMatch(e -> e.contains("missing schema") || e.contains("缺少Schema") || e.contains("Schema element is required") || e.contains("Failed to read complete metadata file") || e.contains("Failed at EntityType"));
//        assertTrue(foundMissingSchema, "应检测到缺少Schema元素相关错误: " + result.getErrors());
//    }
//}
