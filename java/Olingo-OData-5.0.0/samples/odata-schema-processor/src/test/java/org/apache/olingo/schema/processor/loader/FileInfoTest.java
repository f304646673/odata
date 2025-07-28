//package org.apache.olingo.schema.processor.loader;
//
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Set;
//
//import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
///**
// * FileInfo的单元测试
// * 测试文件信息跟踪和依赖关系管理功
// */
//public class FileInfoTest {
//
//    private Path testFilePath;
//
//    @BeforeEach
//    public void setUp() {
//        testFilePath = Paths.get("test", "schema.xml");
//    }
//
//    @Test
//    public void testBuilder_BasicConstruction() {
//        FileInfo.Builder builder = new FileInfo.Builder(testFilePath);
//        FileInfo fileInfo = builder.build();
//
//        assertEquals("File path should match", testFilePath, fileInfo.getFilePath());
//        assertTrue("Should have no schemas initially", fileInfo.getSchemas().isEmpty());
//        assertTrue("Should have no dependencies initially", fileInfo.getDependencies().isEmpty());
//        assertTrue("Should have no dependents initially", fileInfo.getDependents().isEmpty());
//        assertTrue("Should have no validation errors initially", fileInfo.getValidationErrors().isEmpty());
//        assertTrue("Should have no validation warnings initially", fileInfo.getValidationWarnings().isEmpty());
//        assertTrue("Should be valid initially", fileInfo.isValid());
//    }
//
//    @Test
//    public void testBuilder_WithSchemas() {
//        CsdlSchema schema1 = mock(CsdlSchema.class);
//        CsdlSchema schema2 = mock(CsdlSchema.class);
//        when(schema1.getNamespace()).thenReturn("com.example.test1");
//        when(schema2.getNamespace()).thenReturn("com.example.test2");
//
//        List<CsdlSchema> schemas = Arrays.asList(schema1, schema2);
//
//        FileInfo.Builder builder = new FileInfo.Builder(testFilePath);
//        builder.addSchemas(schemas);
//        FileInfo fileInfo = builder.build();
//
//        assertEquals("Should have 2 schemas", 2, fileInfo.getSchemas().size());
//        assertTrue("Should contain schema1", fileInfo.getSchemas().contains(schema1));
//        assertTrue("Should contain schema2", fileInfo.getSchemas().contains(schema2));
//    }
//
//    @Test
//    public void testBuilder_WithSingleSchema() {
//        CsdlSchema schema = mock(CsdlSchema.class);
//        when(schema.getNamespace()).thenReturn("com.example.test");
//
//        FileInfo.Builder builder = new FileInfo.Builder(testFilePath);
//        builder.addSchema(schema);
//        FileInfo fileInfo = builder.build();
//
//        assertEquals("Should have 1 schema", 1, fileInfo.getSchemas().size());
//        assertTrue("Should contain the schema", fileInfo.getSchemas().contains(schema));
//    }
//
//    @Test
//    public void testBuilder_WithDependencies() {
//        Set<String> dependencies = Set.of("com.example.dep1", "com.example.dep2");
//
//        FileInfo.Builder builder = new FileInfo.Builder(testFilePath);
//        builder.addDependencies(dependencies);
//        FileInfo fileInfo = builder.build();
//
//        assertEquals("Should have 2 dependencies", 2, fileInfo.getDependencies().size());
//        assertTrue("Should contain dep1", fileInfo.getDependencies().contains("com.example.dep1"));
//        assertTrue("Should contain dep2", fileInfo.getDependencies().contains("com.example.dep2"));
//    }
//
//    @Test
//    public void testBuilder_WithSingleDependency() {
//        FileInfo.Builder builder = new FileInfo.Builder(testFilePath);
//        builder.addDependency("com.example.dep");
//        FileInfo fileInfo = builder.build();
//
//        assertEquals("Should have 1 dependency", 1, fileInfo.getDependencies().size());
//        assertTrue("Should contain the dependency", fileInfo.getDependencies().contains("com.example.dep"));
//    }
//
//    @Test
//    public void testBuilder_WithValidationErrors() {
//        FileInfo.Builder builder = new FileInfo.Builder(testFilePath);
//        builder.addValidationError("Error 1");
//        builder.addValidationError("Error 2");
//        FileInfo fileInfo = builder.build();
//
//        assertEquals("Should have 2 errors", 2, fileInfo.getValidationErrors().size());
//        assertTrue("Should contain error 1", fileInfo.getValidationErrors().contains("Error 1"));
//        assertTrue("Should contain error 2", fileInfo.getValidationErrors().contains("Error 2"));
//        assertFalse("Should be invalid with errors", fileInfo.isValid());
//    }
//
//    @Test
//    public void testBuilder_WithValidationWarnings() {
//        FileInfo.Builder builder = new FileInfo.Builder(testFilePath);
//        builder.addValidationWarning("Warning 1");
//        builder.addValidationWarning("Warning 2");
//        FileInfo fileInfo = builder.build();
//
//        assertEquals("Should have 2 warnings", 2, fileInfo.getValidationWarnings().size());
//        assertTrue("Should contain warning 1", fileInfo.getValidationWarnings().contains("Warning 1"));
//        assertTrue("Should contain warning 2", fileInfo.getValidationWarnings().contains("Warning 2"));
//        assertTrue("Should still be valid with only warnings", fileInfo.isValid());
//    }
//
//    @Test
//    public void testAddDependents() {
//        FileInfo.Builder builder = new FileInfo.Builder(testFilePath);
//        FileInfo fileInfo = builder.build();
//
//        Path dependent1 = Paths.get("test", "dependent1.xml");
//        Path dependent2 = Paths.get("test", "dependent2.xml");
//        Set<Path> dependents = Set.of(dependent1, dependent2);
//
//        fileInfo.addDependents(dependents);
//
//        assertEquals("Should have 2 dependent files", 2, fileInfo.getDependentFiles().size());
//        assertTrue("Should contain dependent1", fileInfo.getDependentFiles().contains(dependent1));
//        assertTrue("Should contain dependent2", fileInfo.getDependentFiles().contains(dependent2));
//    }
//
//    @Test
//    public void testAddSingleDependent() {
//        FileInfo.Builder builder = new FileInfo.Builder(testFilePath);
//        FileInfo fileInfo = builder.build();
//
//        Path dependent = Paths.get("test", "dependent.xml");
//
//        fileInfo.addDependent(dependent);
//
//        assertEquals("Should have 1 dependent file", 1, fileInfo.getDependentFiles().size());
//        assertTrue("Should contain the dependent", fileInfo.getDependentFiles().contains(dependent));
//    }
//
//    @Test
//    public void testHasCircularDependency_NoDependencies() {
//        FileInfo.Builder builder = new FileInfo.Builder(testFilePath);
//        FileInfo fileInfo = builder.build();
//
//        Set<String> ownNamespaces = Set.of("com.example.own");
//
//        assertFalse("Should not have circular dependency with no dependencies",
//                   fileInfo.hasCircularDependency(ownNamespaces));
//    }
//
//    @Test
//    public void testHasCircularDependency_NoCircularDependency() {
//        FileInfo.Builder builder = new FileInfo.Builder(testFilePath);
//        builder.addDependency("com.example.other");
//        FileInfo fileInfo = builder.build();
//
//        Set<String> ownNamespaces = Set.of("com.example.own");
//
//        assertFalse("Should not have circular dependency",
//                   fileInfo.hasCircularDependency(ownNamespaces));
//    }
//
//    @Test
//    public void testHasCircularDependency_WithCircularDependency() {
//        FileInfo.Builder builder = new FileInfo.Builder(testFilePath);
//        builder.addDependency("com.example.own");
//        FileInfo fileInfo = builder.build();
//
//        Set<String> ownNamespaces = Set.of("com.example.own");
//
//        assertTrue("Should have circular dependency",
//                  fileInfo.hasCircularDependency(ownNamespaces));
//    }
//
//    @Test
//    public void testToString() {
//        CsdlSchema schema = mock(CsdlSchema.class);
//        when(schema.getNamespace()).thenReturn("com.example.test");
//
//        FileInfo.Builder builder = new FileInfo.Builder(testFilePath);
//        builder.addSchema(schema);
//        builder.addDependency("com.example.dep");
//        builder.addValidationError("Test error");
//        FileInfo fileInfo = builder.build();
//
//        String result = fileInfo.toString();
//
//        assertNotNull("toString should not return null", result);
//        assertTrue("Should contain file name", result.contains("schema.xml"));
//        assertTrue("Should contain schema count", result.contains("schemas=1"));
//        assertTrue("Should contain dependencies", result.contains("dependencies="));
//        assertTrue("Should contain valid status", result.contains("valid="));
//    }
//
//    @Test
//    public void testImmutability_Schemas() {
//        CsdlSchema schema = mock(CsdlSchema.class);
//
//        FileInfo.Builder builder = new FileInfo.Builder(testFilePath);
//        builder.addSchema(schema);
//        FileInfo fileInfo = builder.build();
//
//        List<CsdlSchema> schemas = fileInfo.getSchemas();
//        schemas.clear(); // Try to modify the returned list
//
//        // Original should still have the schema
//        assertEquals("Original schemas should not be affected", 1, fileInfo.getSchemas().size());
//    }
//
//    @Test
//    public void testImmutability_Dependencies() {
//        FileInfo.Builder builder = new FileInfo.Builder(testFilePath);
//        builder.addDependency("com.example.dep");
//        FileInfo fileInfo = builder.build();
//
//        Set<String> dependencies = fileInfo.getDependencies();
//        dependencies.clear(); // Try to modify the returned set
//
//        // Original should still have the dependency
//        assertEquals("Original dependencies should not be affected", 1, fileInfo.getDependencies().size());
//    }
//
//    @Test
//    public void testImmutability_ValidationErrors() {
//        FileInfo.Builder builder = new FileInfo.Builder(testFilePath);
//        builder.addValidationError("Test error");
//        FileInfo fileInfo = builder.build();
//
//        List<String> errors = fileInfo.getValidationErrors();
//        errors.clear(); // Try to modify the returned list
//
//        // Original should still have the error
//        assertEquals("Original errors should not be affected", 1, fileInfo.getValidationErrors().size());
//    }
//
//    @Test
//    public void testComprehensiveScenario() {
//        // 综合测试场景
//        CsdlSchema schema1 = mock(CsdlSchema.class);
//        CsdlSchema schema2 = mock(CsdlSchema.class);
//        when(schema1.getNamespace()).thenReturn("com.example.test1");
//        when(schema2.getNamespace()).thenReturn("com.example.test2");
//
//        FileInfo.Builder builder = new FileInfo.Builder(testFilePath);
//        builder.addSchema(schema1);
//        builder.addSchema(schema2);
//        builder.addDependency("com.example.external1");
//        builder.addDependency("com.example.external2");
//        builder.addValidationWarning("Minor warning");
//
//        FileInfo fileInfo = builder.build();
//
//        // 添加被依赖关
//        Path dependent1 = Paths.get("test", "dep1.xml");
//        Path dependent2 = Paths.get("test", "dep2.xml");
//        fileInfo.addDependent(dependent1);
//        fileInfo.addDependent(dependent2);
//
//        // 验证所有属
//        assertEquals("File path should match", testFilePath, fileInfo.getFilePath());
//        assertEquals("Should have 2 schemas", 2, fileInfo.getSchemas().size());
//        assertEquals("Should have 2 dependencies", 2, fileInfo.getDependencies().size());
//        assertEquals("Should have 2 dependent files", 2, fileInfo.getDependentFiles().size());
//        assertEquals("Should have 1 warning", 1, fileInfo.getValidationWarnings().size());
//        assertTrue("Should have no errors", fileInfo.getValidationErrors().isEmpty());
//        assertTrue("Should be valid", fileInfo.isValid());
//
//        // 验证循环依赖检
//        Set<String> ownNamespaces = Set.of("com.example.test1", "com.example.test2");
//        assertFalse("Should not have circular dependency",
//                   fileInfo.hasCircularDependency(ownNamespaces));
//
//        Set<String> circularNamespaces = Set.of("com.example.external1");
//        assertTrue("Should detect circular dependency",
//                  fileInfo.hasCircularDependency(circularNamespaces));
//    }
//}
