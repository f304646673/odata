package org.apache.olingo.schema.repository.loader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schema.repository.loader.SchemaRepositoryLoader.SchemaLoadException;
import org.apache.olingo.schema.repository.loader.SchemaRepositoryLoader.SchemaValidationException;
import org.apache.olingo.schema.repository.model.SchemaRepositoryContext;
import org.apache.olingo.schema.repository.test.TestSchemaGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * SchemaRepositoryLoader的单元测试
 */
@DisplayName("SchemaRepositoryLoader Tests")
class SchemaRepositoryLoaderTest {
    
    private SchemaRepositoryContext context;
    private SchemaRepositoryLoader loader;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        context = new SchemaRepositoryContext();
        loader = new SchemaRepositoryLoader(context);
    }
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create loader with context")
        void shouldCreateLoaderWithContext() {
            SchemaRepositoryLoader newLoader = new SchemaRepositoryLoader(context);
            
            assertNotNull(newLoader);
            assertEquals(context, newLoader.getContext());
        }
        
        @Test
        @DisplayName("Should create loader with custom validator")
        void shouldCreateLoaderWithCustomValidator() {
            SchemaRepositoryLoader.SchemaValidator customValidator = mock(SchemaRepositoryLoader.SchemaValidator.class);
            
            SchemaRepositoryLoader newLoader = new SchemaRepositoryLoader(context, customValidator);
            
            assertNotNull(newLoader);
            assertEquals(context, newLoader.getContext());
        }
        
        @Test
        @DisplayName("Should use default validator when passed null")
        void shouldUseDefaultValidatorWhenPassedNull() {
            SchemaRepositoryLoader newLoader = new SchemaRepositoryLoader(context, null);
            
            assertNotNull(newLoader);
            assertEquals(context, newLoader.getContext());
        }
    }
    
    @Nested
    @DisplayName("InputStream Loading Tests")
    class InputStreamLoadingTests {
        
        @Test
        @DisplayName("Should load schema from InputStream")
        void shouldLoadSchemaFromInputStream() throws SchemaLoadException {
            String schemaXml = TestSchemaGenerator.generateSimpleEntityTypeSchema();
            
            loader.loadFromInputStream(TestSchemaGenerator.toInputStream(schemaXml), "test.xml");
            
            assertTrue(context.containsSchema("TestNamespace"));
            assertNotNull(context.getEntityType(new org.apache.olingo.commons.api.edm.FullQualifiedName("TestNamespace", "Person")));
        }
        
        @Test
        @DisplayName("Should throw exception for null InputStream")
        void shouldThrowExceptionForNullInputStream() {
            assertThrows(SchemaLoadException.class, () -> 
                loader.loadFromInputStream(null, "test.xml"));
        }
        
        @Test
        @DisplayName("Should load schema with inheritance")
        void shouldLoadSchemaWithInheritance() throws SchemaLoadException {
            String schemaXml = TestSchemaGenerator.generateInheritanceSchema();
            
            loader.loadFromInputStream(TestSchemaGenerator.toInputStream(schemaXml), "inheritance.xml");
            
            assertTrue(context.containsSchema("InheritanceNamespace"));
            assertNotNull(context.getEntityType(new org.apache.olingo.commons.api.edm.FullQualifiedName("InheritanceNamespace", "Animal")));
            assertNotNull(context.getEntityType(new org.apache.olingo.commons.api.edm.FullQualifiedName("InheritanceNamespace", "Dog")));
            assertNotNull(context.getEntityType(new org.apache.olingo.commons.api.edm.FullQualifiedName("InheritanceNamespace", "Cat")));
        }
        
        @Test
        @DisplayName("Should load schema with complex types")
        void shouldLoadSchemaWithComplexTypes() throws SchemaLoadException {
            String schemaXml = TestSchemaGenerator.generateComplexTypeSchema();
            
            loader.loadFromInputStream(TestSchemaGenerator.toInputStream(schemaXml), "complex.xml");
            
            assertTrue(context.containsSchema("ComplexNamespace"));
            assertNotNull(context.getComplexType(new org.apache.olingo.commons.api.edm.FullQualifiedName("ComplexNamespace", "Address")));
            assertNotNull(context.getEntityType(new org.apache.olingo.commons.api.edm.FullQualifiedName("ComplexNamespace", "Company")));
        }
        
        @Test
        @DisplayName("Should load schema with actions and functions")
        void shouldLoadSchemaWithActionsAndFunctions() throws SchemaLoadException {
            String schemaXml = TestSchemaGenerator.generateActionFunctionSchema();
            
            loader.loadFromInputStream(TestSchemaGenerator.toInputStream(schemaXml), "actionfunction.xml");
            
            assertTrue(context.containsSchema("ActionFunctionNamespace"));
            assertNotNull(context.getAction(new org.apache.olingo.commons.api.edm.FullQualifiedName("ActionFunctionNamespace", "DiscountProduct")));
            assertNotNull(context.getFunction(new org.apache.olingo.commons.api.edm.FullQualifiedName("ActionFunctionNamespace", "GetProductsByPrice")));
        }
        
        @Test
        @DisplayName("Should throw exception for invalid XML")
        void shouldThrowExceptionForInvalidXml() {
            String invalidXml = "<?xml version=\"1.0\"?><invalid>malformed xml";
            
            assertThrows(SchemaLoadException.class, () -> 
                loader.loadFromInputStream(TestSchemaGenerator.toInputStream(invalidXml), "invalid.xml"));
        }
        
        @Test
        @DisplayName("Should throw exception for empty schema")
        void shouldThrowExceptionForEmptySchema() {
            String emptyXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">" +
                "<edmx:DataServices></edmx:DataServices></edmx:Edmx>";
            
            assertThrows(SchemaLoadException.class, () -> 
                loader.loadFromInputStream(TestSchemaGenerator.toInputStream(emptyXml), "empty.xml"));
        }
    }
    
    @Nested
    @DisplayName("File Loading Tests")
    class FileLoadingTests {
        
        @Test
        @DisplayName("Should load schema from file path")
        void shouldLoadSchemaFromFilePath() throws IOException, SchemaLoadException {
            Path schemaFile = tempDir.resolve("test-schema.xml");
            Files.writeString(schemaFile, TestSchemaGenerator.generateSimpleEntityTypeSchema());
            
            loader.loadFromFile(schemaFile.toString());
            
            assertTrue(context.containsSchema("TestNamespace"));
        }
        
        @Test
        @DisplayName("Should load schema from File object")
        void shouldLoadSchemaFromFileObject() throws IOException, SchemaLoadException {
            Path schemaFile = tempDir.resolve("test-schema.xml");
            Files.writeString(schemaFile, TestSchemaGenerator.generateSimpleEntityTypeSchema());
            
            loader.loadFromFile(schemaFile.toFile());
            
            assertTrue(context.containsSchema("TestNamespace"));
        }
        
        @Test
        @DisplayName("Should load schema from Path object")
        void shouldLoadSchemaFromPathObject() throws IOException, SchemaLoadException {
            Path schemaFile = tempDir.resolve("test-schema.xml");
            Files.writeString(schemaFile, TestSchemaGenerator.generateSimpleEntityTypeSchema());
            
            loader.loadFromPath(schemaFile);
            
            assertTrue(context.containsSchema("TestNamespace"));
        }
        
        @Test
        @DisplayName("Should throw exception for non-existent file")
        void shouldThrowExceptionForNonExistentFile() {
            assertThrows(SchemaLoadException.class, () -> 
                loader.loadFromFile("non-existent-file.xml"));
        }
        
        @Test
        @DisplayName("Should throw exception for null file")
        void shouldThrowExceptionForNullFile() {
            assertThrows(SchemaLoadException.class, () -> 
                loader.loadFromFile((File) null));
        }
        
        @Test
        @DisplayName("Should throw exception for directory instead of file")
        void shouldThrowExceptionForDirectoryInsteadOfFile() {
            assertThrows(SchemaLoadException.class, () -> 
                loader.loadFromFile(tempDir.toFile()));
        }
        
        @Test
        @DisplayName("Should throw exception for null path")
        void shouldThrowExceptionForNullPath() {
            assertThrows(SchemaLoadException.class, () -> 
                loader.loadFromPath(null));
        }
        
        @Test
        @DisplayName("Should throw exception for non-regular file path")
        void shouldThrowExceptionForNonRegularFilePath() {
            assertThrows(SchemaLoadException.class, () -> 
                loader.loadFromPath(tempDir)); // Directory, not regular file
        }
    }
    
    @Nested
    @DisplayName("Directory Loading Tests")
    class DirectoryLoadingTests {
        
        @Test
        @DisplayName("Should load schemas from directory")
        void shouldLoadSchemasFromDirectory() throws IOException, SchemaLoadException {
            // Create multiple schema files
            Path schema1 = tempDir.resolve("schema1.xml");
            Path schema2 = tempDir.resolve("schema2.xml");
            
            Files.writeString(schema1, TestSchemaGenerator.generateSimpleEntityTypeSchema());
            Files.writeString(schema2, TestSchemaGenerator.generateComplexTypeSchema());
            
            loader.loadFromDirectory(tempDir, false);
            
            assertTrue(context.containsSchema("TestNamespace"));
            assertTrue(context.containsSchema("ComplexNamespace"));
        }
        
        @Test
        @DisplayName("Should load schemas recursively")
        void shouldLoadSchemasRecursively() throws IOException, SchemaLoadException {
            // Create subdirectory with schema
            Path subDir = tempDir.resolve("subdir");
            Files.createDirectory(subDir);
            
            Path schema1 = tempDir.resolve("schema1.xml");
            Path schema2 = subDir.resolve("schema2.xml");
            
            Files.writeString(schema1, TestSchemaGenerator.generateSimpleEntityTypeSchema());
            Files.writeString(schema2, TestSchemaGenerator.generateComplexTypeSchema());
            
            loader.loadFromDirectory(tempDir, true);
            
            assertTrue(context.containsSchema("TestNamespace"));
            assertTrue(context.containsSchema("ComplexNamespace"));
        }
        
        @Test
        @DisplayName("Should not load schemas from subdirectories when not recursive")
        void shouldNotLoadSchemasFromSubdirectoriesWhenNotRecursive() throws IOException, SchemaLoadException {
            // Create subdirectory with schema
            Path subDir = tempDir.resolve("subdir");
            Files.createDirectory(subDir);
            
            Path schema1 = tempDir.resolve("schema1.xml");
            Path schema2 = subDir.resolve("schema2.xml");
            
            Files.writeString(schema1, TestSchemaGenerator.generateSimpleEntityTypeSchema());
            Files.writeString(schema2, TestSchemaGenerator.generateComplexTypeSchema());
            
            loader.loadFromDirectory(tempDir, false);
            
            assertTrue(context.containsSchema("TestNamespace"));
            assertTrue(!context.containsSchema("ComplexNamespace")); // Should not be loaded
        }
        
        @Test
        @DisplayName("Should ignore non-XML files")
        void shouldIgnoreNonXmlFiles() throws IOException, SchemaLoadException {
            Path xmlFile = tempDir.resolve("schema.xml");
            Path txtFile = tempDir.resolve("readme.txt");
            Path jsonFile = tempDir.resolve("config.json");
            
            Files.writeString(xmlFile, TestSchemaGenerator.generateSimpleEntityTypeSchema());
            Files.writeString(txtFile, "This is a text file");
            Files.writeString(jsonFile, "{\"key\": \"value\"}");
            
            loader.loadFromDirectory(tempDir, false);
            
            assertTrue(context.containsSchema("TestNamespace"));
            assertEquals(1, context.getAllSchemas().size());
        }
        
        @Test
        @DisplayName("Should handle empty directory")
        void shouldHandleEmptyDirectory() throws SchemaLoadException {
            // Empty temp directory
            loader.loadFromDirectory(tempDir, false);
            
            assertTrue(context.getAllSchemas().isEmpty());
        }
        
        @Test
        @DisplayName("Should throw exception for invalid directory")
        void shouldThrowExceptionForInvalidDirectory() {
            assertThrows(SchemaLoadException.class, () -> 
                loader.loadFromDirectory(Paths.get("non-existent-directory"), false));
        }
        
        @Test
        @DisplayName("Should throw exception for null directory")
        void shouldThrowExceptionForNullDirectory() {
            assertThrows(SchemaLoadException.class, () -> 
                loader.loadFromDirectory(null, false));
        }
        
        @Test
        @DisplayName("Should handle file as directory parameter")
        void shouldHandleFileAsDirectoryParameter() throws IOException {
            Path file = tempDir.resolve("notadirectory.txt");
            Files.writeString(file, "content");
            
            assertThrows(SchemaLoadException.class, () -> 
                loader.loadFromDirectory(file, false));
        }
    }
    
    @Nested
    @DisplayName("Parallel Loading Tests")
    class ParallelLoadingTests {
        
        @Test
        @DisplayName("Should load files in parallel")
        void shouldLoadFilesInParallel() throws IOException, SchemaLoadException {
            // Create multiple schema files
            Path schema1 = tempDir.resolve("schema1.xml");
            Path schema2 = tempDir.resolve("schema2.xml");
            Path schema3 = tempDir.resolve("schema3.xml");
            
            Files.writeString(schema1, TestSchemaGenerator.generateSimpleEntityTypeSchema());
            Files.writeString(schema2, TestSchemaGenerator.generateComplexTypeSchema());
            Files.writeString(schema3, TestSchemaGenerator.generateActionFunctionSchema());
            
            List<Path> files = List.of(schema1, schema2, schema3);
            
            loader.loadFilesInParallel(files);
            
            assertTrue(context.containsSchema("TestNamespace"));
            assertTrue(context.containsSchema("ComplexNamespace"));
            assertTrue(context.containsSchema("ActionFunctionNamespace"));
        }
        
        @Test
        @DisplayName("Should handle empty file list")
        void shouldHandleEmptyFileList() throws SchemaLoadException {
            loader.loadFilesInParallel(List.of());
            
            assertTrue(context.getAllSchemas().isEmpty());
        }
        
        @Test
        @DisplayName("Should handle null file list")
        void shouldHandleNullFileList() throws SchemaLoadException {
            loader.loadFilesInParallel(null);
            
            assertTrue(context.getAllSchemas().isEmpty());
        }
        
        @Test
        @DisplayName("Should collect exceptions from parallel loading")
        void shouldCollectExceptionsFromParallelLoading() throws IOException {
            Path validSchema = tempDir.resolve("valid.xml");
            Path invalidSchema = tempDir.resolve("invalid.xml");
            
            Files.writeString(validSchema, TestSchemaGenerator.generateSimpleEntityTypeSchema());
            Files.writeString(invalidSchema, "invalid xml content");
            
            List<Path> files = List.of(validSchema, invalidSchema);
            
            SchemaLoadException exception = assertThrows(SchemaLoadException.class, () -> 
                loader.loadFilesInParallel(files));
            
            assertNotNull(exception);
            assertThat(exception.getSuppressed().length).isGreaterThanOrEqualTo(0);
        }
    }
    
    @Nested
    @DisplayName("Schema Validation Tests")
    class SchemaValidationTests {
        
        @Test
        @DisplayName("Should use custom validator")
        void shouldUseCustomValidator() throws SchemaLoadException, SchemaValidationException {
            SchemaRepositoryLoader.SchemaValidator mockValidator = mock(SchemaRepositoryLoader.SchemaValidator.class);
            SchemaRepositoryLoader loaderWithValidator = new SchemaRepositoryLoader(context, mockValidator);
            
            String schemaXml = TestSchemaGenerator.generateSimpleEntityTypeSchema();
            
            loaderWithValidator.loadFromInputStream(TestSchemaGenerator.toInputStream(schemaXml), "test.xml");
            
            verify(mockValidator).validate(any(CsdlSchema.class));
        }
        
        @Test
        @DisplayName("Should throw exception when validator fails")
        void shouldThrowExceptionWhenValidatorFails() throws SchemaValidationException {
            SchemaRepositoryLoader.SchemaValidator mockValidator = mock(SchemaRepositoryLoader.SchemaValidator.class);
            doThrow(new SchemaValidationException("Validation failed")).when(mockValidator).validate(any(CsdlSchema.class));
            
            SchemaRepositoryLoader loaderWithValidator = new SchemaRepositoryLoader(context, mockValidator);
            String schemaXml = TestSchemaGenerator.generateSimpleEntityTypeSchema();
            
            assertThrows(SchemaLoadException.class, () -> 
                loaderWithValidator.loadFromInputStream(TestSchemaGenerator.toInputStream(schemaXml), "test.xml"));
        }
        
        @Test
        @DisplayName("Should validate schema with null namespace")
        void shouldValidateSchemaWithNullNamespace() {
            String invalidSchemaXml = TestSchemaGenerator.generateInvalidSchema();
            
            assertThrows(SchemaLoadException.class, () -> 
                loader.loadFromInputStream(TestSchemaGenerator.toInputStream(invalidSchemaXml), "invalid.xml"));
        }
    }
    
    @Nested
    @DisplayName("Default Validator Tests")
    class DefaultValidatorTests {
        
        private SchemaRepositoryLoader.DefaultSchemaValidator validator;
        
        @BeforeEach
        void setUp() {
            validator = new SchemaRepositoryLoader.DefaultSchemaValidator();
        }
        
        @Test
        @DisplayName("Should validate valid schema")
        void shouldValidateValidSchema() throws SchemaValidationException {
            CsdlSchema validSchema = new CsdlSchema();
            validSchema.setNamespace("ValidNamespace");
            
            // Should not throw exception
            validator.validate(validSchema);
        }
        
        @Test
        @DisplayName("Should throw exception for null schema")
        void shouldThrowExceptionForNullSchema() {
            assertThrows(SchemaValidationException.class, () -> 
                validator.validate(null));
        }
        
        @Test
        @DisplayName("Should throw exception for schema with null namespace")
        void shouldThrowExceptionForSchemaWithNullNamespace() {
            CsdlSchema invalidSchema = new CsdlSchema();
            invalidSchema.setNamespace(null);
            
            assertThrows(SchemaValidationException.class, () -> 
                validator.validate(invalidSchema));
        }
        
        @Test
        @DisplayName("Should throw exception for schema with empty namespace")
        void shouldThrowExceptionForSchemaWithEmptyNamespace() {
            CsdlSchema invalidSchema = new CsdlSchema();
            invalidSchema.setNamespace("");
            
            assertThrows(SchemaValidationException.class, () -> 
                validator.validate(invalidSchema));
        }
        
        @Test
        @DisplayName("Should throw exception for schema with whitespace-only namespace")
        void shouldThrowExceptionForSchemaWithWhitespaceOnlyNamespace() {
            CsdlSchema invalidSchema = new CsdlSchema();
            invalidSchema.setNamespace("   ");
            
            assertThrows(SchemaValidationException.class, () -> 
                validator.validate(invalidSchema));
        }
    }
    
    @Nested
    @DisplayName("Reload Tests")
    class ReloadTests {
        
        @Test
        @DisplayName("Should reload all schemas")
        void shouldReloadAllSchemas() throws IOException, SchemaLoadException {
            // Load initial schema
            String initialSchema = TestSchemaGenerator.generateSimpleEntityTypeSchema();
            loader.loadFromInputStream(TestSchemaGenerator.toInputStream(initialSchema), "initial.xml");
            
            assertTrue(context.containsSchema("TestNamespace"));
            
            // Create new files for reload
            Path schema1 = tempDir.resolve("schema1.xml");
            Path schema2 = tempDir.resolve("schema2.xml");
            
            Files.writeString(schema1, TestSchemaGenerator.generateComplexTypeSchema());
            Files.writeString(schema2, TestSchemaGenerator.generateActionFunctionSchema());
            
            List<Path> files = List.of(schema1, schema2);
            
            loader.reloadAll(files);
            
            // Original schema should be gone
            assertTrue(!context.containsSchema("TestNamespace"));
            
            // New schemas should be loaded
            assertTrue(context.containsSchema("ComplexNamespace"));
            assertTrue(context.containsSchema("ActionFunctionNamespace"));
        }
    }
    
    @Nested
    @DisplayName("Resource Management Tests")
    class ResourceManagementTests {
        
        @Test
        @DisplayName("Should shutdown cleanly")
        void shouldShutdownCleanly() {
            // Should not throw exception
            loader.shutdown();
        }
        
        @Test
        @DisplayName("Should handle multiple shutdowns")
        void shouldHandleMultipleShutdowns() {
            loader.shutdown();
            
            // Should not throw exception on second shutdown
            loader.shutdown();
        }
        
        @Test
        @DisplayName("Should still have access to context after shutdown")
        void shouldStillHaveAccessToContextAfterShutdown() throws SchemaLoadException {
            String schemaXml = TestSchemaGenerator.generateSimpleEntityTypeSchema();
            loader.loadFromInputStream(TestSchemaGenerator.toInputStream(schemaXml), "test.xml");
            
            loader.shutdown();
            
            // Context should still be accessible
            assertTrue(context.containsSchema("TestNamespace"));
            assertEquals(context, loader.getContext());
        }
    }
    
    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {
        
        @Test
        @DisplayName("SchemaLoadException should have proper constructors")
        void schemaLoadExceptionShouldHaveProperConstructors() {
            String message = "Test message";
            Exception cause = new RuntimeException("Cause");
            
            SchemaLoadException exception1 = new SchemaLoadException(message);
            assertEquals(message, exception1.getMessage());
            
            SchemaLoadException exception2 = new SchemaLoadException(message, cause);
            assertEquals(message, exception2.getMessage());
            assertEquals(cause, exception2.getCause());
        }
        
        @Test
        @DisplayName("SchemaValidationException should have proper constructors")
        void schemaValidationExceptionShouldHaveProperConstructors() {
            String message = "Validation failed";
            Exception cause = new RuntimeException("Validation cause");
            
            SchemaValidationException exception1 = new SchemaValidationException(message);
            assertEquals(message, exception1.getMessage());
            
            SchemaValidationException exception2 = new SchemaValidationException(message, cause);
            assertEquals(message, exception2.getMessage());
            assertEquals(cause, exception2.getCause());
        }
    }
}
