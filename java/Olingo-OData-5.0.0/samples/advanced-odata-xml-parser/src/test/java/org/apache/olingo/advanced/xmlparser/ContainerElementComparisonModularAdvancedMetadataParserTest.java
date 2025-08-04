package org.apache.olingo.advanced.xmlparser;

import java.util.List;

import org.apache.olingo.advanced.xmlparser.core.AdvancedMetadataParser;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.server.core.SchemaBasedEdmProvider;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive unit tests for container element comparison methods in AdvancedMetadataParser.
 * Tests the strict OData 4.0 spec-compliant comparison logic for EntitySets, ActionImports,
 * FunctionImports, Singletons, and NavigationPropertyBindings.
 */
class ContainerElementComparisonModularAdvancedMetadataParser {

    private AdvancedMetadataParser parser;
    private String testResourcesPath;
    private String testResourcesRootPath;

    @BeforeEach
    void setUp() {
        parser = new AdvancedMetadataParser();
        // Get the test resources path
        testResourcesPath = getClass().getClassLoader().getResource("test-xml").getPath();
        testResourcesRootPath = getClass().getClassLoader().getResource(".").getPath();
        if (testResourcesPath.startsWith("/") && System.getProperty("os.name").toLowerCase().contains("windows")) {
            testResourcesPath = testResourcesPath.substring(1);
        }
    }

    @Test
    @DisplayName("Test EntitySet comparison - identical entity sets")
    void testEntitySetComparisonIdentical() {
        assertDoesNotThrow(() -> {
            String schemaPath = testResourcesRootPath + "/test-xml/entityset-identical-schema1.xml";

            try {
                SchemaBasedEdmProvider provider1 = parser.buildEdmProvider(schemaPath);
                SchemaBasedEdmProvider provider2 = parser.buildEdmProvider(schemaPath);

                assertNotNull(provider1);
                assertNotNull(provider2);

                // Get schemas and compare them
                List<CsdlSchema> schemas1 = provider1.getSchemas();
                List<CsdlSchema> schemas2 = provider2.getSchemas();

                assertEquals(1, schemas1.size());
                assertEquals(1, schemas2.size());

                CsdlSchema csdlSchema1 = schemas1.get(0);
                CsdlSchema csdlSchema2 = schemas2.get(0);

                // Use reflection to test the private method areSchemasIdentical
                java.lang.reflect.Method method = AdvancedMetadataParser.class.getDeclaredMethod("areSchemasIdentical", CsdlSchema.class, CsdlSchema.class);
                method.setAccessible(true);
                boolean identical = (Boolean) method.invoke(parser, csdlSchema1, csdlSchema2);

                assertTrue(identical, "Identical schemas should be detected as identical");

            } finally {
            }
        });
    }

    @Test
    @DisplayName("Test EntitySet comparison - different entity sets")
    void testEntitySetComparisonDifferent() {
        assertDoesNotThrow(() -> {
            String schema1Path = testResourcesRootPath + "/test-xml/entityset-different-schema1.xml";
            String schema2Path = testResourcesRootPath + "/test-xml/entityset-different-schema2.xml";

            try {

                SchemaBasedEdmProvider provider1 = parser.buildEdmProvider(schema1Path);
                SchemaBasedEdmProvider provider2 = parser.buildEdmProvider(schema2Path);

                List<CsdlSchema> schemas1 = provider1.getSchemas();
                List<CsdlSchema> schemas2 = provider2.getSchemas();

                CsdlSchema csdlSchema1 = schemas1.get(0);
                CsdlSchema csdlSchema2 = schemas2.get(0);

                // Use reflection to test the private method areSchemasIdentical
                java.lang.reflect.Method method = AdvancedMetadataParser.class.getDeclaredMethod("areSchemasIdentical", CsdlSchema.class, CsdlSchema.class);
                method.setAccessible(true);
                boolean identical = (Boolean) method.invoke(parser, csdlSchema1, csdlSchema2);

                assertFalse(identical, "Different schemas should not be detected as identical");

            } finally {
            }
        });
    }

    @Test
    @DisplayName("Test ActionImport comparison")
    void testActionImportComparison() {
        assertDoesNotThrow(() -> {
            String schema1Path = testResourcesRootPath + "/test-xml/actionimport-schema1.xml";
            String schema2Path = testResourcesRootPath + "/test-xml/actionimport-schema2.xml";
            try {

                SchemaBasedEdmProvider provider1 = parser.buildEdmProvider(schema1Path);
                SchemaBasedEdmProvider provider2 = parser.buildEdmProvider(schema2Path);

                List<CsdlSchema> schemas1 = provider1.getSchemas();
                List<CsdlSchema> schemas2 = provider2.getSchemas();

                CsdlSchema csdlSchema1 = schemas1.get(0);
                CsdlSchema csdlSchema2 = schemas2.get(0);

                // Use reflection to test the private method areSchemasIdentical
                java.lang.reflect.Method method = AdvancedMetadataParser.class.getDeclaredMethod("areSchemasIdentical", CsdlSchema.class, CsdlSchema.class);
                method.setAccessible(true);
                boolean identical = (Boolean) method.invoke(parser, csdlSchema1, csdlSchema2);

                assertFalse(identical, "Schemas with different ActionImports should not be identical");

            } finally {
            }
        });
    }

    @Test
    @DisplayName("Test FunctionImport comparison")
    void testFunctionImportComparison() {
        assertDoesNotThrow(() -> {
            String schema1Path = testResourcesRootPath + "/test-xml/functionimport-schema1.xml";
            String schema2Path = testResourcesRootPath + "/test-xml/functionimport-schema2.xml";

            try {
                SchemaBasedEdmProvider provider1 = parser.buildEdmProvider(schema1Path);
                SchemaBasedEdmProvider provider2 = parser.buildEdmProvider(schema2Path);

                List<CsdlSchema> schemas1 = provider1.getSchemas();
                List<CsdlSchema> schemas2 = provider2.getSchemas();

                CsdlSchema csdlSchema1 = schemas1.get(0);
                CsdlSchema csdlSchema2 = schemas2.get(0);

                // Use reflection to test the private method areSchemasIdentical
                java.lang.reflect.Method method = AdvancedMetadataParser.class.getDeclaredMethod("areSchemasIdentical", CsdlSchema.class, CsdlSchema.class);
                method.setAccessible(true);
                boolean identical = (Boolean) method.invoke(parser, csdlSchema1, csdlSchema2);

                assertFalse(identical, "Schemas with different FunctionImport properties should not be identical");

            } finally {
            }
        });
    }

    @Test
    @DisplayName("Test Singleton comparison")
    void testSingletonComparison() {
        assertDoesNotThrow(() -> {
            String schema1Path = testResourcesRootPath + "/test-xml/singleton-schema1.xml";
            String schema2Path = testResourcesRootPath + "/test-xml/singleton-schema2.xml";

            try {
                SchemaBasedEdmProvider provider1 = parser.buildEdmProvider(schema1Path);
                SchemaBasedEdmProvider provider2 = parser.buildEdmProvider(schema2Path);

                List<CsdlSchema> schemas1 = provider1.getSchemas();
                List<CsdlSchema> schemas2 = provider2.getSchemas();

                CsdlSchema csdlSchema1 = schemas1.get(0);
                CsdlSchema csdlSchema2 = schemas2.get(0);

                // Use reflection to test the private method areSchemasIdentical
                java.lang.reflect.Method method = AdvancedMetadataParser.class.getDeclaredMethod("areSchemasIdentical", CsdlSchema.class, CsdlSchema.class);
                method.setAccessible(true);
                boolean identical = (Boolean) method.invoke(parser, csdlSchema1, csdlSchema2);

                assertFalse(identical, "Schemas with different Singleton NavigationPropertyBindings should not be identical");

            } finally {
            }
        });
    }

    @Test
    @DisplayName("Test NavigationPropertyBinding comparison")
    void testNavigationPropertyBindingComparison() {
        assertDoesNotThrow(() -> {
            String schema1Path = testResourcesRootPath + "/test-xml/navbindings-with-bindings.xml";
            String schema2Path = testResourcesRootPath + "/test-xml/navbindings-without-bindings.xml";
            
            try {
                SchemaBasedEdmProvider provider1 = parser.buildEdmProvider(schema1Path);
                SchemaBasedEdmProvider provider2 = parser.buildEdmProvider(schema2Path);

                List<CsdlSchema> schemas1 = provider1.getSchemas();
                List<CsdlSchema> schemas2 = provider2.getSchemas();

                CsdlSchema csdlSchema1 = schemas1.get(0);
                CsdlSchema csdlSchema2 = schemas2.get(0);

                // Use reflection to test the private method areSchemasIdentical
                java.lang.reflect.Method method = AdvancedMetadataParser.class.getDeclaredMethod("areSchemasIdentical", CsdlSchema.class, CsdlSchema.class);
                method.setAccessible(true);
                boolean identical = (Boolean) method.invoke(parser, csdlSchema1, csdlSchema2);

                assertFalse(identical, "Schemas with different NavigationPropertyBindings should not be identical");

            } finally {
            }
        });
    }
}
