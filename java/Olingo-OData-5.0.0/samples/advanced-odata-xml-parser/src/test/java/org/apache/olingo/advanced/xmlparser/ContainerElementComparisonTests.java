// package org.apache.olingo.advanced.xmlparser;

// import java.io.File;
// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Paths;
// import java.util.List;

// import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
// import org.apache.olingo.server.core.SchemaBasedEdmProvider;
// import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;

// /**
//  * Comprehensive unit tests for container element comparison methods in AdvancedMetadataParser.
//  * Tests the strict OData 4.0 spec-compliant comparison logic for EntitySets, ActionImports,
//  * FunctionImports, Singletons, and NavigationPropertyBindings.
//  */
// class ContainerElementComparisonTests {

//     private AdvancedMetadataParser parser;
//     private String testResourcesPath;

//     @BeforeEach
//     void setUp() {
//         parser = new AdvancedMetadataParser();
//         // Get the test resources path
//         testResourcesPath = getClass().getClassLoader().getResource("test-xml").getPath();
//         if (testResourcesPath.startsWith("/") && System.getProperty("os.name").toLowerCase().contains("windows")) {
//             testResourcesPath = testResourcesPath.substring(1);
//         }
//     }

//     private String loadXmlFromResource(String resourcePath) throws IOException {
//         return new String(Files.readAllBytes(Paths.get(testResourcesPath, resourcePath)));
//     }

//     @Test
//     @DisplayName("Test EntitySet comparison - identical entity sets")
//     void testEntitySetComparisonIdentical() {
//         assertDoesNotThrow(() -> {
//             String schemaPath = testResourcesPath + "test-xml/entityset-identical-schema1.xml");
//             String schema2 = schema1; // Identical schema

//             File tempFile1 = File.createTempFile("schema1", ".xml");
//             File tempFile2 = File.createTempFile("schema2", ".xml");
//             tempFile1.deleteOnExit();
//             tempFile2.deleteOnExit();

//             try {
//                 java.nio.file.Files.write(tempFile1.toPath(), schema1.getBytes());
//                 java.nio.file.Files.write(tempFile2.toPath(), schema2.getBytes());

//                 SchemaBasedEdmProvider provider1 = parser.buildEdmProvider(tempFile1.getAbsolutePath());
//                 SchemaBasedEdmProvider provider2 = parser.buildEdmProvider(tempFile2.getAbsolutePath());

//                 assertNotNull(provider1);
//                 assertNotNull(provider2);

//                 // Get schemas and compare them
//                 List<CsdlSchema> schemas1 = provider1.getSchemas();
//                 List<CsdlSchema> schemas2 = provider2.getSchemas();

//                 assertEquals(1, schemas1.size());
//                 assertEquals(1, schemas2.size());

//                 CsdlSchema csdlSchema1 = schemas1.get(0);
//                 CsdlSchema csdlSchema2 = schemas2.get(0);

//                 // Use reflection to test the private method areSchemasIdentical
//                 java.lang.reflect.Method method = AdvancedMetadataParser.class.getDeclaredMethod("areSchemasIdentical", CsdlSchema.class, CsdlSchema.class);
//                 method.setAccessible(true);
//                 boolean identical = (Boolean) method.invoke(parser, csdlSchema1, csdlSchema2);

//                 assertTrue(identical, "Identical schemas should be detected as identical");

//             } finally {
//                 tempFile1.delete();
//                 tempFile2.delete();
//             }
//         });
//     }

//     @Test
//     @DisplayName("Test EntitySet comparison - different entity sets")
//     void testEntitySetComparisonDifferent() {
//         assertDoesNotThrow(() -> {
//             String schema1 = loadXmlFromResource("entityset-different-schema1.xml");
//             String schema2 = loadXmlFromResource("entityset-different-schema2.xml");

//             File tempFile1 = File.createTempFile("schema1", ".xml");
//             File tempFile2 = File.createTempFile("schema2", ".xml");
//             tempFile1.deleteOnExit();
//             tempFile2.deleteOnExit();

//             try {
//                 java.nio.file.Files.write(tempFile1.toPath(), schema1.getBytes());
//                 java.nio.file.Files.write(tempFile2.toPath(), schema2.getBytes());

//                 SchemaBasedEdmProvider provider1 = parser.buildEdmProvider(tempFile1.getAbsolutePath());
//                 SchemaBasedEdmProvider provider2 = parser.buildEdmProvider(tempFile2.getAbsolutePath());

//                 List<CsdlSchema> schemas1 = provider1.getSchemas();
//                 List<CsdlSchema> schemas2 = provider2.getSchemas();

//                 CsdlSchema csdlSchema1 = schemas1.get(0);
//                 CsdlSchema csdlSchema2 = schemas2.get(0);

//                 // Use reflection to test the private method areSchemasIdentical
//                 java.lang.reflect.Method method = AdvancedMetadataParser.class.getDeclaredMethod("areSchemasIdentical", CsdlSchema.class, CsdlSchema.class);
//                 method.setAccessible(true);
//                 boolean identical = (Boolean) method.invoke(parser, csdlSchema1, csdlSchema2);

//                 assertFalse(identical, "Different schemas should not be detected as identical");

//             } finally {
//                 tempFile1.delete();
//                 tempFile2.delete();
//             }
//         });
//     }

//     @Test
//     @DisplayName("Test ActionImport comparison")
//     void testActionImportComparison() {
//         assertDoesNotThrow(() -> {
//             String schema1 = loadXmlFromResource("actionimport-schema1.xml");
//             String schema2 = loadXmlFromResource("actionimport-schema2.xml");

//             File tempFile1 = File.createTempFile("schema1", ".xml");
//             File tempFile2 = File.createTempFile("schema2", ".xml");
//             tempFile1.deleteOnExit();
//             tempFile2.deleteOnExit();

//             try {
//                 java.nio.file.Files.write(tempFile1.toPath(), schema1.getBytes());
//                 java.nio.file.Files.write(tempFile2.toPath(), schema2.getBytes());

//                 SchemaBasedEdmProvider provider1 = parser.buildEdmProvider(tempFile1.getAbsolutePath());
//                 SchemaBasedEdmProvider provider2 = parser.buildEdmProvider(tempFile2.getAbsolutePath());

//                 List<CsdlSchema> schemas1 = provider1.getSchemas();
//                 List<CsdlSchema> schemas2 = provider2.getSchemas();

//                 CsdlSchema csdlSchema1 = schemas1.get(0);
//                 CsdlSchema csdlSchema2 = schemas2.get(0);

//                 // Use reflection to test the private method areSchemasIdentical
//                 java.lang.reflect.Method method = AdvancedMetadataParser.class.getDeclaredMethod("areSchemasIdentical", CsdlSchema.class, CsdlSchema.class);
//                 method.setAccessible(true);
//                 boolean identical = (Boolean) method.invoke(parser, csdlSchema1, csdlSchema2);

//                 assertFalse(identical, "Schemas with different ActionImports should not be identical");

//             } finally {
//                 tempFile1.delete();
//                 tempFile2.delete();
//             }
//         });
//     }

//     @Test
//     @DisplayName("Test FunctionImport comparison")
//     void testFunctionImportComparison() {
//         assertDoesNotThrow(() -> {
//             String schema1 = loadXmlFromResource("functionimport-schema1.xml");
//             String schema2 = loadXmlFromResource("functionimport-schema2.xml");

//             File tempFile1 = File.createTempFile("schema1", ".xml");
//             File tempFile2 = File.createTempFile("schema2", ".xml");
//             tempFile1.deleteOnExit();
//             tempFile2.deleteOnExit();

//             try {
//                 java.nio.file.Files.write(tempFile1.toPath(), schema1.getBytes());
//                 java.nio.file.Files.write(tempFile2.toPath(), schema2.getBytes());

//                 SchemaBasedEdmProvider provider1 = parser.buildEdmProvider(tempFile1.getAbsolutePath());
//                 SchemaBasedEdmProvider provider2 = parser.buildEdmProvider(tempFile2.getAbsolutePath());

//                 List<CsdlSchema> schemas1 = provider1.getSchemas();
//                 List<CsdlSchema> schemas2 = provider2.getSchemas();

//                 CsdlSchema csdlSchema1 = schemas1.get(0);
//                 CsdlSchema csdlSchema2 = schemas2.get(0);

//                 // Use reflection to test the private method areSchemasIdentical
//                 java.lang.reflect.Method method = AdvancedMetadataParser.class.getDeclaredMethod("areSchemasIdentical", CsdlSchema.class, CsdlSchema.class);
//                 method.setAccessible(true);
//                 boolean identical = (Boolean) method.invoke(parser, csdlSchema1, csdlSchema2);

//                 assertFalse(identical, "Schemas with different FunctionImport properties should not be identical");

//             } finally {
//                 tempFile1.delete();
//                 tempFile2.delete();
//             }
//         });
//     }

//     @Test
//     @DisplayName("Test Singleton comparison")
//     void testSingletonComparison() {
//         assertDoesNotThrow(() -> {
//             String schema1 = loadXmlFromResource("singleton-schema1.xml");
//             String schema2 = loadXmlFromResource("singleton-schema2.xml");

//             File tempFile1 = File.createTempFile("schema1", ".xml");
//             File tempFile2 = File.createTempFile("schema2", ".xml");
//             tempFile1.deleteOnExit();
//             tempFile2.deleteOnExit();

//             try {
//                 java.nio.file.Files.write(tempFile1.toPath(), schema1.getBytes());
//                 java.nio.file.Files.write(tempFile2.toPath(), schema2.getBytes());

//                 SchemaBasedEdmProvider provider1 = parser.buildEdmProvider(tempFile1.getAbsolutePath());
//                 SchemaBasedEdmProvider provider2 = parser.buildEdmProvider(tempFile2.getAbsolutePath());

//                 List<CsdlSchema> schemas1 = provider1.getSchemas();
//                 List<CsdlSchema> schemas2 = provider2.getSchemas();

//                 CsdlSchema csdlSchema1 = schemas1.get(0);
//                 CsdlSchema csdlSchema2 = schemas2.get(0);

//                 // Use reflection to test the private method areSchemasIdentical
//                 java.lang.reflect.Method method = AdvancedMetadataParser.class.getDeclaredMethod("areSchemasIdentical", CsdlSchema.class, CsdlSchema.class);
//                 method.setAccessible(true);
//                 boolean identical = (Boolean) method.invoke(parser, csdlSchema1, csdlSchema2);

//                 assertFalse(identical, "Schemas with different Singleton NavigationPropertyBindings should not be identical");

//             } finally {
//                 tempFile1.delete();
//                 tempFile2.delete();
//             }
//         });
//     }

//     @Test
//     @DisplayName("Test NavigationPropertyBinding comparison")
//     void testNavigationPropertyBindingComparison() {
//         assertDoesNotThrow(() -> {
//             String schemaWithBindings = loadXmlFromResource("navbindings-with-bindings.xml");
//             String schemaWithoutBindings = loadXmlFromResource("navbindings-without-bindings.xml");

//             File tempFile1 = File.createTempFile("schema1", ".xml");
//             File tempFile2 = File.createTempFile("schema2", ".xml");
//             tempFile1.deleteOnExit();
//             tempFile2.deleteOnExit();

//             try {
//                 java.nio.file.Files.write(tempFile1.toPath(), schemaWithBindings.getBytes());
//                 java.nio.file.Files.write(tempFile2.toPath(), schemaWithoutBindings.getBytes());

//                 SchemaBasedEdmProvider provider1 = parser.buildEdmProvider(tempFile1.getAbsolutePath());
//                 SchemaBasedEdmProvider provider2 = parser.buildEdmProvider(tempFile2.getAbsolutePath());

//                 List<CsdlSchema> schemas1 = provider1.getSchemas();
//                 List<CsdlSchema> schemas2 = provider2.getSchemas();

//                 CsdlSchema csdlSchema1 = schemas1.get(0);
//                 CsdlSchema csdlSchema2 = schemas2.get(0);

//                 // Use reflection to test the private method areSchemasIdentical
//                 java.lang.reflect.Method method = AdvancedMetadataParser.class.getDeclaredMethod("areSchemasIdentical", CsdlSchema.class, CsdlSchema.class);
//                 method.setAccessible(true);
//                 boolean identical = (Boolean) method.invoke(parser, csdlSchema1, csdlSchema2);

//                 assertFalse(identical, "Schemas with different NavigationPropertyBindings should not be identical");

//             } finally {
//                 tempFile1.delete();
//                 tempFile2.delete();
//             }
//         });
//     }
// }
