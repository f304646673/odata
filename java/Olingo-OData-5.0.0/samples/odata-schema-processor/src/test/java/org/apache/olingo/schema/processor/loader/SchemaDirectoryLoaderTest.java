//package org.apache.olingo.schema.processor.loader;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.Collections;
//import java.util.List;
//
//import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
//import org.apache.olingo.schema.processor.parser.ODataXmlParser;
//import org.apache.olingo.schema.processor.repository.SchemaRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.junit.jupiter.api.Test;
//
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
///**
// * SchemaDirectoryLoader的综合单元测试
// * 测试完整的目录加载和验证流程
// */
//@ExtendWith(MockitoExtension.class)
//public class SchemaDirectoryLoaderTest {
//
//    @Mock
//    private ODataXmlParser mockXmlParser;
//
//    @Mock
//    private SchemaRepository mockRepository;
//
//    private SchemaDirectoryLoader loader;
//
//    @BeforeEach
//    public void setUp() {
//        loader = new SchemaDirectoryLoader(mockXmlParser);
//    }
//
//    @Test
//    public void testConstructorWithParser() {
//        SchemaDirectoryLoader newLoader = new SchemaDirectoryLoader(mockXmlParser);
//        assertNotNull("Loader should not be null", newLoader);
//    }
//
//    @Test
//    public void testConstructorWithNullParser() {
//        assertThrows(IllegalArgumentException.class, () -> new SchemaDirectoryLoader(null));
//    }
//
//    @Test
//    public void testValidateAndLoadDirectoryWithPath() {
//        Path testPath = Paths.get(".");
//
//        SchemaDirectoryLoader.LoadResult result = loader.validateAndLoadDirectory(testPath);
//
//        assertNotNull("Result should not be null", result);
//        assertNotNull("Errors list should not be null", result.getErrors());
//        assertNotNull("Files list should not be null", result.getLoadedFiles());
//        assertNotNull("Schemas list should not be null", result.getSchemas());
//    }
//
//    @Test
//    public void testValidateAndLoadDirectoryWithNullPath() {
//        SchemaDirectoryLoader.LoadResult result = loader.validateAndLoadDirectory((Path) null);
//
//        assertNotNull("Result should not be null", result);
//        assertFalse("Should fail for null path", result.isSuccess());
//        assertFalse("Should have errors", result.getErrors().isEmpty());
//    }
//
//    @Test
//    public void testLoadSchemasToRepositoryWithNullSchemas() {
//        try {
//            loader.loadSchemasToRepository(null, mockRepository);
//            fail("Should throw exception for null schemas");
//        } catch (IllegalArgumentException e) {
//            // Expected
//        }
//    }
//
//    @Test
//    public void testLoadSchemasToRepositoryWithNullRepository() {
//        List<CsdlSchema> schemas = Collections.emptyList();
//
//        try {
//            loader.loadSchemasToRepository(schemas, null);
//            fail("Should throw exception for null repository");
//        } catch (IllegalArgumentException e) {
//            // Expected
//        }
//    }
//
//    @Test
//    public void testLoadSchemasToRepositoryWithEmptySchemas() {
//        List<CsdlSchema> schemas = Collections.emptyList();
//
//        // Should not throw exception for empty schemas
//        loader.loadSchemasToRepository(schemas, mockRepository);
//
//        // Verify that repository was not modified
//        verifyNoInteractions(mockRepository);
//    }
//
//    @Test
//    public void testValidateSchemaReferencesWithNull() {
//        SchemaDirectoryLoader.ValidationResult result = loader.validateSchemaReferences(null);
//
//        assertNotNull("Result should not be null", result);
//        assertFalse("Should fail for null schemas", result.isValid());
//        assertFalse("Should have errors", result.getErrors().isEmpty());
//    }
//
//    @Test
//    public void testValidateSchemaReferencesWithEmpty() {
//        List<CsdlSchema> schemas = Collections.emptyList();
//
//        SchemaDirectoryLoader.ValidationResult result = loader.validateSchemaReferences(schemas);
//
//        assertNotNull("Result should not be null", result);
//        assertTrue("Should succeed for empty schemas", result.isValid());
//        assertTrue("Should have no errors", result.getErrors().isEmpty());
//    }
//
//    @Test
//    public void testLoadFromResourcesWithNonExistent() {
//        SchemaDirectoryLoader.LoadResult result = loader.loadFromResources("non/existent/resource");
//
//        assertNotNull("Result should not be null", result);
//        // Result depends on whether resource exists
//        assertNotNull("Errors list should not be null", result.getErrors());
//        assertNotNull("Files list should not be null", result.getLoadedFiles());
//    }
//
//    @Test
//    public void testLoadFromResourcesWithNull() {
//        SchemaDirectoryLoader.LoadResult result = loader.loadFromResources(null);
//
//        assertNotNull("Result should not be null", result);
//        assertFalse("Should fail for null resource path", result.isSuccess());
//        assertFalse("Should have errors", result.getErrors().isEmpty());
//    }
//
//    @Test
//    public void testLoadFromResourcesWithEmpty() {
//        SchemaDirectoryLoader.LoadResult result = loader.loadFromResources("");
//
//        assertNotNull("Result should not be null", result);
//        assertFalse("Should fail for empty resource path", result.isSuccess());
//        assertFalse("Should have errors", result.getErrors().isEmpty());
//    }
//
//    @Test
//    public void testLoadDirectoryWithStringPath() {
//        String testPath = ".";
//
//        SchemaDirectoryLoader.LoadResult result = loader.loadDirectory(testPath);
//
//        assertNotNull("Result should not be null", result);
//        assertNotNull("Errors list should not be null", result.getErrors());
//        assertNotNull("Files list should not be null", result.getLoadedFiles());
//        assertNotNull("Schemas list should not be null", result.getSchemas());
//    }
//
//    @Test
//    public void testLoadDirectoryWithPathObject() {
//        Path testPath = Paths.get(".");
//
//        SchemaDirectoryLoader.LoadResult result = loader.loadDirectory(testPath);
//
//        assertNotNull("Result should not be null", result);
//        assertNotNull("Errors list should not be null", result.getErrors());
//        assertNotNull("Files list should not be null", result.getLoadedFiles());
//        assertNotNull("Schemas list should not be null", result.getSchemas());
//    }
//
//    @Test
//    public void testLoadResultProperties() {
//        // Test that we can create a LoadResult and access its properties
//        Path testPath = Paths.get(".");
//        SchemaDirectoryLoader.LoadResult result = loader.loadDirectory(testPath);
//
//        assertNotNull("Result should not be null", result);
//        assertTrue("File count should be non-negative", result.getFileCount() >= 0);
//        assertTrue("Schema count should be non-negative", result.getSchemaCount() >= 0);
//        assertNotNull("Errors should not be null", result.getErrors());
//        assertNotNull("Warnings should not be null", result.getWarnings());
//        assertNotNull("Loaded files should not be null", result.getLoadedFiles());
//        assertNotNull("Schemas should not be null", result.getSchemas());
//        assertNotNull("File info map should not be null", result.getFileInfoMap());
//    }
//
//    @Test
//    public void testMultipleOperations() {
//        // Test that multiple operations don't interfere with each other
//        SchemaDirectoryLoader.LoadResult result1 = loader.loadDirectory(".");
//        SchemaDirectoryLoader.LoadResult result2 = loader.loadFromResources("test");
//        SchemaDirectoryLoader.ValidationResult result3 = loader.validateSchemaReferences(Collections.emptyList());
//
//        assertNotNull("First result should not be null", result1);
//        assertNotNull("Second result should not be null", result2);
//        assertNotNull("Third result should not be null", result3);
//
//        // Each result should be independent
//        assertNotSame("Results should be different objects", result1, result2);
//    }
//
//    @Test
//    public void testLoaderStatelessness() {
//        // Verify that the loader doesn't maintain state between calls
//        SchemaDirectoryLoader.LoadResult result1 = loader.loadDirectory(".");
//        SchemaDirectoryLoader.LoadResult result2 = loader.loadDirectory(".");
//
//        assertNotNull("First result should not be null", result1);
//        assertNotNull("Second result should not be null", result2);
//
//        // Results should be separate objects
//        assertNotSame("Results should be different objects", result1, result2);
//    }
//
//    @Test
//    public void testValidationResultProperties() {
//        SchemaDirectoryLoader.ValidationResult result = loader.validateSchemaReferences(Collections.emptyList());
//
//        assertNotNull("Result should not be null", result);
//        assertNotNull("Errors should not be null", result.getErrors());
//        assertNotNull("Warnings should not be null", result.getWarnings());
//
//        // For empty list, should be valid
//        assertTrue("Should be valid for empty list", result.isValid());
//        assertTrue("Should have no errors for empty list", result.getErrors().isEmpty());
//    }
//
//    @Test
//    public void testFileInfoAccess() {
//        SchemaDirectoryLoader.LoadResult result = loader.loadDirectory(".");
//
//        assertNotNull("Result should not be null", result);
//        assertNotNull("File info map should not be null", result.getFileInfoMap());
//
//        // Test getAllDefinedNamespaces method
//        assertNotNull("All defined namespaces should not be null", result.getAllDefinedNamespaces());
//    }
//
//    @Test
//    public void testErrorHandling() {
//        // Test various error conditions
//        SchemaDirectoryLoader.LoadResult result1 = loader.loadDirectory((String) null);
//        SchemaDirectoryLoader.LoadResult result2 = loader.loadFromResources(null);
//        SchemaDirectoryLoader.ValidationResult result3 = loader.validateSchemaReferences(null);
//
//        // All should fail gracefully
//        assertNotNull("First result should not be null", result1);
//        assertNotNull("Second result should not be null", result2);
//        assertNotNull("Third result should not be null", result3);
//
//        assertFalse("First result should not be successful", result1.isSuccess());
//        assertFalse("Second result should not be successful", result2.isSuccess());
//        assertFalse("Third result should not be valid", result3.isValid());
//    }
//}
